
/*
# © Copyright 2019-2022, Clinacuity Inc. All Rights Reserved.
#
# This file is part of CliniDeID.
# CliniDeID is free software: you can redistribute it and/or modify it under the terms of the
# GNU General Public License as published by the Free Software Foundation,
# either version 3 of the License, or any later version.
# CliniDeID is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
# PURPOSE. See the GNU General Public License for more details.
# You should have received a copy of the GNU General Public License along with CliniDeID.
# If not, see <https://www.gnu.org/licenses/>.
# =========================================================================   
*/

package com.clinacuity.deid.ae;
/* This annotator runs CRF, RNN, Mira, Regex, and RNN in parallel threads
It does the initialization for them all here so that models are loaded once.
 */

import com.clinacuity.deid.ae.regex.Concept;
import com.clinacuity.deid.ae.regex.ConceptFileParser;
import com.clinacuity.deid.ae.regex.impl.ConceptFileParser_impl;
import com.clinacuity.deid.ae.regex.impl.Concept_impl;
import com.clinacuity.deid.ae.regex.impl.RegExAnnotatorThread;
import com.clinacuity.deid.type.BaseToken;
import com.clinacuity.deid.type.PiiAnnotation;
import com.clinacuity.deid.util.SvmTemplateItem;
import com.clinacuity.deid.util.Util;
import com.clinacuity.deid.util.Utilities;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import edu.lium.mira.Mira;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.initializable.InitializableFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.SequenceClassifier;
import org.cleartk.ml.SequenceClassifierFactory;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.extractor.TypePathExtractor;
import org.cleartk.ml.feature.function.CharacterCategoryPatternFunction;
import org.cleartk.ml.feature.function.CharacterNgramFeatureFunction;
import org.cleartk.ml.feature.function.ContainsHyphenFeatureFunction;
import org.cleartk.ml.feature.function.FeatureFunctionExtractor;
import org.cleartk.ml.feature.function.NumericTypeFeatureFunction;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.util.ReflectionUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EnsembleAnnotator extends JCasAnnotator_ImplBase {
    public static final String EXCLUDES_LIST = "excludesList";
    public static final String RNN_PORT_NUMBER = "rnnPortNumber";
    public static final String RNN_HOST_NAME = "hostName";
    public static final String MIRA_MODEL_FILE = "miraModelFileName";
    public static final String BROWN_CLUSTERS_FILENAME = "brownClustersFileName";
    public static final String BROWN_CUTOFF = "brownCutoff";
    public static final String SVM_MODEL_FILE = "svmModelFileName";
    public static final String SVM_LABEL_FILE_NAME = "svmLabelFileName";
    public static final String SVM_FEATURE_INDEX_FILE_NAME = "svmFeatureIndexFileName";
    public static final String SVM_TEMPLATE_FILE_NAME = "svmTemplateFileName";
    public static final String REGEX_CONCEPTS_FILE = "conceptFile";
    protected static final int NUMBER_OF_THREADS = 4;
    private static final Logger LOGGER = LogManager.getLogger();
    public static ExecutorService pool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);//NOT THREAD SAFE, static to allow App to shut them down in instance of crash/interruption
    //it is possible that it could be avoided if EnsembleAnnotator caught any issues and shut it down itself, but I'm not sure that is possible
    static HashMap<String, String> brownClusters = null;
    @ConfigurationParameter(name = BROWN_CLUSTERS_FILENAME, mandatory = false)
    static String brownClustersFileName;
    @ConfigurationParameter(name = BROWN_CUTOFF, defaultValue = "7", mandatory = false)
    static int brownCutoff; // number of bits to use from brown cluster
    @ConfigurationParameter(name = RNN_HOST_NAME, description = "host name", defaultValue = "localhost", mandatory = false)
    static String hostName = "localhost";
    @ConfigurationParameter(name = RNN_PORT_NUMBER, description = "port number", defaultValue = "4444", mandatory = false)
    static int rnnPortNumber;
    @ConfigurationParameter(name = REGEX_CONCEPTS_FILE, mandatory = false)
    static String conceptFile;
    @ConfigurationParameter(name = EXCLUDES_LIST, mandatory = false)
    protected String excludesList = "";
    protected Model linearModel;
    protected HashMap<String, String> svmLbl;
    protected HashMap<String, String> svmLblR;
    protected HashMap<String, Integer> fIdx;
    protected Mira mira;
    //For CRF: not sure, but don't see why extractors and chunking can't be static
    protected FeatureExtractor1<BaseToken> extractor;
    protected CleartkExtractor<BaseToken, BaseToken> contextExtractor;
    protected SequenceClassifier<String> classifier;
    protected Concept[] regexConcepts;
    protected NumberFormat floatNumberFormat = null;
    protected NumberFormat integerNumberFormat = null;
    protected List<SvmTemplateItem> svmTmplItem;
    @ConfigurationParameter(name = MIRA_MODEL_FILE, description = "path and name of model file", defaultValue = "data/ensemble/miralium-master_musc_on/deid_model_musc", mandatory = false)
    private String miraModelFileName;
    @ConfigurationParameter(name = SVM_MODEL_FILE, description = "path and name of model file", defaultValue = "data/ensemble/liblinear_musc_on/deid_model_musc", mandatory = false)
    private String svmModelFileName;
    @ConfigurationParameter(name = SVM_LABEL_FILE_NAME, description = "?", defaultValue = "data/ensemble/i2b2/bioDeid_musc_split.txt", mandatory = false)
    private String svmLabelFileName;
    @ConfigurationParameter(name = SVM_FEATURE_INDEX_FILE_NAME, description = "?", defaultValue = "data/ensemble/liblinear_musc_on/wDeidMap_musc", mandatory = false)
    private String svmFeatureIndexFileName;
    @ConfigurationParameter(name = SVM_TEMPLATE_FILE_NAME, description = "?", defaultValue = "data/ensemble/i2b2/template_crf_deid_musc", mandatory = false)
    private String svmTemplateFileName;
    private UimaContext uimaContext;

    public static void setRnnPortNumber(int port) {
        rnnPortNumber = port;
    }

    public static void stopRnn() throws AnalysisEngineProcessException {
        RnnAnnotatorThread.stopRnn(hostName, rnnPortNumber);
    }

    public static boolean tryConnectRnn() {
        return RnnAnnotatorThread.tryConnect(hostName, rnnPortNumber);
    }

    static void initializeRnn() throws ResourceInitializationException {
        LOGGER.debug("initRnn");
        int tries = 9;
        while (tries > 0 && !tryConnectRnn()) {
            LOGGER.debug("RNN, not ready, sleep then retrying");
            try {
                long start = System.currentTimeMillis();
                Thread.sleep(5000);
                LOGGER.error("Try: {} Slept for {}", tries, (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                LOGGER.throwing(e);
                Thread.currentThread().interrupt();
                throw new ResourceInitializationException(e);
            }
            tries--;
        }
        if (tries > 0) {
            LOGGER.debug("RNN connected to get out of loop");
        } else if (!tryConnectRnn()) {
            LOGGER.debug("RNN Failed last attempt");
            throw new ResourceInitializationException("Failed to connect to RNN, python service failed. ", null);
        }
    }

    private static void initializeBrownClusters() throws ResourceInitializationException {
        try (FileInputStream fis = new FileInputStream(brownClustersFileName);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            @SuppressWarnings("unchecked") // for casting Brown Cluster's serialized object, have to have separate variable or suppress whole method
            HashMap<String, String> tempClusters = (HashMap<String, String>) ois.readObject();
            brownClusters = tempClusters;
        } catch (IOException | ClassNotFoundException e2) {
            throw new ResourceInitializationException(e2);
        }
    }

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);
        this.uimaContext = uimaContext;
        initializeSubClassSpecific();
    }

    //This allows EnsemblePartialAnnotator to override with checks for which to load
    protected void initializeSubClassSpecific() throws ResourceInitializationException {
        CountDownLatch doneSignal = new CountDownLatch(NUMBER_OF_THREADS);
        List<Boolean> flags = new ArrayList<>(NUMBER_OF_THREADS);
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            flags.add(false);
        }

        //this order doesn't matter, but the error messages in waitAndFinishInitialize assume a specific order
        int index = 0;
        InitializeEnsembleThread svm = new InitializeEnsembleThread(doneSignal, flags, index++, "Svm", this::initializeSvm);
        pool.execute(svm);
        InitializeEnsembleThread crf = new InitializeEnsembleThread(doneSignal, flags, index++, "Crf", this::initializeCrf);
        pool.execute(crf);
        InitializeEnsembleThread miraThread = new InitializeEnsembleThread(doneSignal, flags, index++, "Mira", this::initializeMira);
        pool.execute(miraThread);
        InitializeEnsembleThread regex = new InitializeEnsembleThread(doneSignal, flags, index++, "Regex", this::initializeRegex);
        pool.execute(regex);
        // putting the RNN thread later so that the python service has more time to start in case the Thread.sleep() call finishes too fast
//        InitializeEnsembleThread rnn = new InitializeEnsembleThread(doneSignal, flags, index++, "Rnn", EnsembleAnnotator::initializeRnn);
//        pool.execute(rnn);

        waitAndFinishInitialize("", doneSignal, flags);
    }

    void waitAndFinishInitialize(String partial, CountDownLatch doneSignal, List<Boolean> flags) throws ResourceInitializationException {
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            LOGGER.throwing(e);
            Thread.currentThread().interrupt();
            throw new ResourceInitializationException(e);
        }

        for (Boolean f : flags) {
            if (!f) {
                String vals = flags.toString();
                LOGGER.error("Thread failed to initialize in Ensemble{}Annotator (SVM, CRF, MIRA, Regex, RNN): {}", partial, vals);
                throw new ResourceInitializationException("Some threads failed to initialize in Ensemble" + partial + "Annotator: (SVM, CRF, MIRA, Regex, RNN) " + vals, null);
            }
        }
        FilterForThreads.initialize();
        LOGGER.debug("Ensemble{}Annotator Initialized", partial);
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        /*CountDownLatch is used to wait for all NUMBER_OF_THREADSA threads to complete,
        Each thread is give its own ArrayList<PiiAnnotation> to put the Pii it finds in.
        Then after all done they are put in jCas, can't change jCas until all complete
        Consider filtering in each thread to increase parallelism
        All threads catch their own exceptions and log them, but don't stop the AE from continuing with the rest.
        Thread pool should keep from truly creating/destroying threads underneath but more direct reuse could possibly increase speed.
         */
        CountDownLatch doneSignal = new CountDownLatch(NUMBER_OF_THREADS);
        @SuppressWarnings("unchecked")
        List<PiiAnnotation>[] newPii = (ArrayList<PiiAnnotation>[]) new ArrayList<?>[NUMBER_OF_THREADS];
        Arrays.setAll(newPii, ArrayList::new);
        String fileName = Utilities.getFileName(jCas);
        int piiIndex = 0;
        SvmAnnotatorThread svm = new SvmAnnotatorThread(linearModel, svmLbl, svmLblR, fIdx, svmTmplItem, jCas, doneSignal, newPii[piiIndex++], fileName);
        pool.execute(svm);
//        RnnAnnotatorThread rnn = new RnnAnnotatorThread(hostName, rnnPortNumber, 600, jCas, doneSignal, newPii[piiIndex++], fileName);
//        pool.execute(rnn);
        MiraAnnotatorThread miraThread = new MiraAnnotatorThread(mira, jCas, doneSignal, newPii[piiIndex++], fileName);
        pool.execute(miraThread);
        CrfAnnotatorThread crf = new CrfAnnotatorThread(brownClusters, brownCutoff, extractor, contextExtractor, classifier, jCas, doneSignal, newPii[piiIndex++], fileName);
        pool.execute(crf);
        RegExAnnotatorThread regex = new RegExAnnotatorThread(regexConcepts, floatNumberFormat, integerNumberFormat, jCas, doneSignal, newPii[piiIndex++], fileName);
        pool.execute(regex);
        finishAndCopy(doneSignal, jCas, newPii);
        LOGGER.debug("Ensemble done");
    }

    protected void finishAndCopy(CountDownLatch doneSignal, JCas jCas, List<PiiAnnotation>[] newPii) throws AnalysisEngineProcessException {
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            LOGGER.throwing(e);
            Thread.currentThread().interrupt();
            throw new AnalysisEngineProcessException(e);
        }
        for (List<PiiAnnotation> piiAnnotations : newPii) {//may be faster to clear then to recreate
            for (PiiAnnotation annot : piiAnnotations) {
                annot.addToIndexes(jCas);
            }
        }
    }

    private List<SvmTemplateItem> makeTemplateItemList(ArrayList<String> tmpl) {
        List<SvmTemplateItem> data = new ArrayList<>();
        for (String fs : tmpl) {
            String[] fsA = fs.split(":", 2);
            String fsF = fsA[1].replace("%x", "").replace("[", "").replace("]", "");

            String[] fA = fsF.split("/");
            SvmTemplateItem item = new SvmTemplateItem(fsA[0]);

            String[] ff = fA[0].split(",");
            item.rI0 = Integer.parseInt(ff[0]);
            item.cI0 = Integer.parseInt(ff[1]);
            if (fA.length > 1) {
                ff = fA[1].split(",");
                item.hasSecond = true;
                item.rI1 = Integer.parseInt(ff[0]);
                item.cI1 = Integer.parseInt(ff[1]);
            }
            if (fA.length > 2) {
                ff = fA[2].split(",");
                item.hasThird = true;
                item.rI2 = Integer.parseInt(ff[0]);
                item.cI2 = Integer.parseInt(ff[1]);
            }

            data.add(item);
        }
        return data;
    }

    void initializeSvm() throws ResourceInitializationException {
        svmLbl = new HashMap<>();
        Util.readMap(svmLabelFileName, svmLbl, "\t");

        svmLblR = new HashMap<>();
        Util.readMapR(svmLabelFileName, svmLblR, "\t");

        fIdx = new HashMap<>(4000000);

        ArrayList<String> svmTemplate = new ArrayList<>();

        try {
            Util.readSvmMap(svmFeatureIndexFileName, fIdx);
            Util.readSvmTemplate(svmTemplateFileName, svmTemplate);
            svmTmplItem = makeTemplateItemList(svmTemplate);
        } catch (NumberFormatException e) {
            LOGGER.throwing(e);
            throw new ResourceInitializationException(e);
        }

        Linear.disableDebugOutput();
        LOGGER.debug("reading SVM model ....");
        try {
            linearModel = Linear.loadModel(new File(svmModelFileName));
            LOGGER.debug("SVM model loaded");
        } catch (IOException e) {
            LOGGER.throwing(e);
            throw new ResourceInitializationException(e);
        }
    }

    void initializeMira() throws ResourceInitializationException {
        LOGGER.debug("reading MIRA model ....");
        mira = new Mira();
        try {
            mira.loadModel(miraModelFileName);
            LOGGER.debug("MIRA model loaded");
        } catch (Exception e) {
            LOGGER.throwing(e);
            throw new ResourceInitializationException(e);
        }
    }

    private void makeCrfClassifier() throws ResourceInitializationException {
        String classifierFactoryClassName = "org.cleartk.ml.jar.SequenceJarClassifierFactory";
        SequenceClassifierFactory factory = InitializableFactory.create(uimaContext, classifierFactoryClassName, SequenceClassifierFactory.class);
        SequenceClassifier untypedClassifier;
        try {
            untypedClassifier = factory.createClassifier();
        } catch (IOException e) {
            LOGGER.throwing(e);
            return;
        }
        classifier = ReflectionUtil.uncheckedCast(untypedClassifier);
        InitializableFactory.initialize(untypedClassifier, uimaContext);
    }

    void initializeCrf() throws ResourceInitializationException {
        initializeBrownClusters();
        makeCrfClassifier();
        // TODO: Is there a way to get a previous word's label? Get CRF annotations and check for begin/end of previous token?
        // make this an array, then command line parameters for which to include to allow scripting a lot of models?
        //BaseToken: POS fine, chunk and Ner are BIO format; tag and normalizedForm are null
        extractor = new CrfAnnotatorThread.CombinedExtractor1Extended<>(
                new FeatureFunctionExtractor<>(new CoveredTextExtractor<>(),
                        new CharacterCategoryPatternFunction<>(CharacterCategoryPatternFunction.PatternType.REPEATS_MERGED)),
                new TypePathExtractor<>(BaseToken.class, "partOfSpeech"),
                // new TypePathExtractorLemma<>(BaseToken.class, "canonicalForm"),
                new FeatureFunctionExtractor<>(new CoveredTextExtractor<>(),
                        FeatureFunctionExtractor.BaseFeatures.EXCLUDE, new CrfAnnotatorThread.BrownClusterFunction()),
                new CrfAnnotatorThread.TypePathExtractorNameDictionary<>(BaseToken.class, "nameDictionary"),
                new TypePathExtractor<>(BaseToken.class, "chunk"),
//                     new TypePathExtractor<>(BaseToken.class, "ner"),//tried adding this, but didn't improve
//                    new TypePathExtractorChunk<>(BaseToken.class, "chunk"),
                new FeatureFunctionExtractor<>(new CoveredTextExtractor<>(),
                        FeatureFunctionExtractor.BaseFeatures.EXCLUDE, new CrfAnnotatorThread.WordShapeFunction()),
                //  new TypePathExtractor<>(BaseToken.class, "tokenNumber"), // this is token number in document, not within sentence, TODO: try within
                // sentence
                new FeatureFunctionExtractor<>(new CoveredTextExtractor<>(),
                        FeatureFunctionExtractor.BaseFeatures.EXCLUDE, new ContainsHyphenFeatureFunction()),
                new FeatureFunctionExtractor<>(new CoveredTextExtractor<>(),
                        FeatureFunctionExtractor.BaseFeatures.EXCLUDE, new NumericTypeFeatureFunction()),
//consider: token number in sentence vs. in document, dictionary values other than name,  retesting N-grams, context window size (currently +-2)

                // N-grams: had higher recall and lower precision w/o Ngrams so removed them
                new FeatureFunctionExtractor<>(new CoveredTextExtractor<>(),
                        FeatureFunctionExtractor.BaseFeatures.EXCLUDE, new CharacterNgramFeatureFunction(CharacterNgramFeatureFunction.Orientation.RIGHT_TO_LEFT, 0, 2)),
                new FeatureFunctionExtractor<>(new CoveredTextExtractor<>(),
                        FeatureFunctionExtractor.BaseFeatures.EXCLUDE, new CharacterNgramFeatureFunction(CharacterNgramFeatureFunction.Orientation.LEFT_TO_RIGHT, 0, 2))
        );
        // extractor2 = new CleartkExtractor<>(BaseToken.class, new TypePathExtractorLemma<>(BaseToken.class, "canonicalForm"),
        // new Ngram(new Preceding(1), new Focus()));
        contextExtractor = new CleartkExtractor<>(BaseToken.class, extractor, new CleartkExtractor.Preceding(2), new CleartkExtractor.Following(2));


        // the chunking definition: Tokens will be combined to form PiiAnnotation, with labels
        // from the "mentionType" attribute so that we get B-location, I-person, etc.
        // chunking = new BioChunking<>(BaseToken.class, PiiAnnotation.class, "piiSubtype");
        LOGGER.debug("NamedEntityChunkerTinitialized {}-{}  model: {}", brownClustersFileName, brownCutoff,
                GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH);
    }

    void initializeRegex() throws ResourceInitializationException {
        // default initialization for number format
        floatNumberFormat = NumberFormat.getNumberInstance();
        integerNumberFormat = NumberFormat.getIntegerInstance();

        ConceptFileParser parser = new ConceptFileParser_impl();
        try (FileInputStream stream = new FileInputStream(conceptFile)) {
            LOGGER.debug("rule set file: {}", conceptFile);
            regexConcepts = parser.parseConceptFile(conceptFile, stream);
        } catch (IOException e) {
            try (FileInputStream stream = new FileInputStream(conceptFile)) {
                regexConcepts = parser.parseConceptFile(new File(conceptFile).getAbsolutePath(), stream);
            } catch (IOException ex) {
                LOGGER.throwing(ex);
                throw new ResourceInitializationException(e);
            }
        }

        HashSet<String> conceptNames = new HashSet<>(regexConcepts.length);
        for (Concept con : regexConcepts) {//check for duplicates
            String name = con.getName();
            if (name == null) {//skip if not set
                continue;
            }
            if (conceptNames.contains(name)) {// check for duplicate names which can occur
                LOGGER.warn("duplicate concept name: {}", name);
            } else {
                conceptNames.add(name);
            }
        }
        for (Concept con : regexConcepts) {
            ((Concept_impl) con).initialize();
        }
    }

    interface ThreadInitializable {
        void initialize() throws ResourceInitializationException;
    }

    static class InitializeEnsembleThread implements Runnable {
        List<Boolean> flags;
        int index;
        CountDownLatch done;
        ThreadInitializable init;
        String label;

        InitializeEnsembleThread(CountDownLatch done, List<Boolean> flags, int index, String label, ThreadInitializable init) {
            this.flags = flags;
            this.done = done;
            this.index = index;
            this.init = init;
            this.label = label;
        }

        public void run() {
            LOGGER.debug("Initialize {} Thread", label);
            try {
                init.initialize();
                LOGGER.debug("{} Thread initialized", label);
                flags.set(index, true);
            } catch (ResourceInitializationException e) {
                LOGGER.error("{} failed to initialize", label);
                LOGGER.throwing(e);
            }
            done.countDown();
        }
    }
}


