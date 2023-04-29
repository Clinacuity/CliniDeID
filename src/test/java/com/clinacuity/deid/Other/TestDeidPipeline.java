
/*
# © Copyright 2019-2023, Clinacuity Inc. All Rights Reserved.
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

package com.clinacuity.deid.Other;

import com.clinacuity.clinideid.message.DeidLevel;
import com.clinacuity.deid.ae.DeidLevelCleaner;
import com.clinacuity.deid.ae.DictionaryAnnotator;
import com.clinacuity.deid.ae.EnsembleAnnotator;
import com.clinacuity.deid.ae.EnsemblePartialAnnotator;
import com.clinacuity.deid.ae.FeatureAnnotator;
import com.clinacuity.deid.ae.OpenNlpAnnotator;
import com.clinacuity.deid.ae.PostPiiProcessing;
import com.clinacuity.deid.ae.VoteAnnotator;
import com.clinacuity.deid.gui.App;
import com.clinacuity.deid.mains.DeidPipeline;
import com.clinacuity.deid.outputAnnotators.DocumentListAnnotator;
import com.clinacuity.deid.outputAnnotators.PiiTagging;
import com.clinacuity.deid.outputAnnotators.PiiWriterAnnotator;
import com.clinacuity.deid.outputAnnotators.RawXmiOutputAnnotator;
import com.clinacuity.deid.outputAnnotators.resynthesis.ResynthesisAnnotator;
import com.clinacuity.deid.readers.FileSystemCollectionReader;
import com.clinacuity.deid.readers.GeneralCollectionReader;
import com.clinacuity.deid.readers.XmlCollectionReaderClearTkTraining;
import com.clinacuity.deid.training.CombinedCorpusGoldCollectionReader;
import com.clinacuity.deid.training.NamedEntityChunker;
import com.clinacuity.deid.type.PiiAnnotation;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.jar.GenericJarClassifierFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.clinacuity.deid.util.Utilities.getFileName;

public class TestDeidPipeline extends DeidPipeline {
    private static final Logger logger = LogManager.getLogger();
    private String extraOption = "";
    private Map<String, Integer> aeNamesToPosition;
    private int begin = -1;
    private int end = -1;
    private boolean xmiOutput = false;
    private String crfModelFile = CRF_MODEL_FILE;

    private TestDeidPipeline(String inputDir, String outputDir) {
        super(null, inputDir, outputDir);
    }

    private TestDeidPipeline() {
        super();
    }

    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();
        Options options = makeCliOptions();
        options.addOption("e", "extra", true, "extra option");
        options.addOption("f", "firstAEs", true, "Runs first # stages of pipeline");
        options.addOption("b", "begin", true, "start with this stage");//paired with below
        options.addOption("z", "end", true, "end with this stage");
        options.addOption("x", "xmi", false, "Output file after partial pipeline run");//TODO: should this just be always yes
        options.addOption("m", "crfModel", true, "old CRF model");
        options.addOption("mr", "muscResynth", false, "runMuscResynth for creating corpus");


        //TODO: clean this up to share code with DeidPipeline's main for inputDir/outpuDir, command, help, port, resynth, level
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (MissingOptionException e) {
            logger.error("Missed some command line options");
            HelpFormatter helper = new HelpFormatter();
            helper.printHelp("mvn exec:java@cli -Dexec.args=\"arg1,arg1\"", options);
            System.exit(-1);
        }

        TestDeidPipeline testPipeline = new TestDeidPipeline(commandLine.getOptionValue("inputDir"), commandLine.getOptionValue("outputDir"));
        processBasicCliOptions(testPipeline, options, commandLine);

        if (commandLine.hasOption("crfModel")) {
            testPipeline.setCrfModelFile(commandLine.getOptionValue("crfModel"));
            testPipeline.testCrf();
            return;
        }

        if (commandLine.hasOption("xmi")) {
            testPipeline.xmiOutput = true;
        }

        if (commandLine.hasOption("extra")) {
            testPipeline.extraOption = commandLine.getOptionValue("extra");
            if ("postproc".equals(testPipeline.extraOption)) {
                testPipeline.testPostProcessing();
                return;
            } else if (testPipeline.extraOption.equalsIgnoreCase("printSplit")) {
                testPipeline.printSplit();
                return;
            }
        }

        if (commandLine.hasOption("begin") ^ commandLine.hasOption("end")) {
            System.out.println("Must have both or neither begin and end options");
            System.exit(-1);
        }
        if (commandLine.hasOption("begin") && commandLine.hasOption("end")) {
            testPipeline.setBeginEnd(commandLine.getOptionValue("begin"), commandLine.getOptionValue("end"));
        }
        if (commandLine.hasOption("firstAEs")) {
            testPipeline.setBeginEnd("0", commandLine.getOptionValue("firstAEs"));
        }
        if (commandLine.hasOption("muscResynth")) {
            testPipeline.runMuscCreateResynthText();
            return;
        }
        String errorMessage = processPiiOptions(testPipeline, commandLine);
        if (!errorMessage.isEmpty()) {
            System.out.println(errorMessage);
            System.exit(2);
        }
        errorMessage = checkModelPaths();
        if (!errorMessage.isEmpty()) {
            System.out.println(errorMessage);
            System.exit(2);
        }
        testPipeline.testExecute();

        App.cleanupNonGui();
    }

    private void printSplit() {
        CombinedCorpusGoldCollectionReader.isTraining = true;

        try {
            TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription();
            reader = (FileSystemCollectionReader) CollectionReaderFactory.createReader(CombinedCorpusGoldCollectionReader.class,
                    typeSystemDescription, FileSystemCollectionReader.INPUT_DIRECTORY_PARAMETER, inputDir,
                    FileSystemCollectionReader.RECURSIVE_PARAMETER, false,
                    CombinedCorpusGoldCollectionReader.INPUT_EXTENSION_PARAM, "xml",
                    FileSystemCollectionReader.FILE_SIZE_LIMIT, 1000000, FileSystemCollectionReader.FILE_LIMIT, 1000000,

                    GeneralCollectionReader.DEID_LEVEL, DeidLevel.beyond,
                    GeneralCollectionReader.OUTPUT_CLEAN, false,
                    GeneralCollectionReader.OUTPUT_GENERAL_TAG, false,
                    GeneralCollectionReader.OUTPUT_CATEGORY_TAG, false,
                    GeneralCollectionReader.OUTPUT_PII, false,
                    GeneralCollectionReader.OUTPUT_RESYNTHESIS, false,
                    GeneralCollectionReader.OUTPUT_RAW, false,

                    GeneralCollectionReader.CLEAN_DIRECTORY, outputDir,
                    GeneralCollectionReader.GENERAL_TAG_DIRECTORY, outputDir,
                    GeneralCollectionReader.CATEGORY_TAG_DIRECTORY, outputDir,
                    GeneralCollectionReader.PII_DIRECTORY, outputDir,
                    GeneralCollectionReader.RESYNTHESIS_DIRECTORY, outputDir,
                    GeneralCollectionReader.RAW_DIRECTORY, outputDir);
        } catch (UIMAException e) {
            logger.throwing(e);
            return;
        }

        List<AnalysisEngine> analysisEngines = new ArrayList<>();
        analysisEngines.add(getAnalysisEngine(CasConsumerWriteAnnotation.class, CasConsumerWriteAnnotation.PARAM_OUTPUT_FOLDER, outputDir,
                CasConsumerWriteAnnotation.PRINT_ORIGINAL_TEXT, false));
        tryCreateJCas();
        this.analysisEngines = analysisEngines.toArray(new AnalysisEngine[0]);
        runPipeline(false);
    }

    private void setCrfModelFile(String crfModel) {
        crfModelFile = crfModel;
    }

    private Map<String, Integer> createAeMap() {
        Map<String, Integer> map = new HashMap<>();
        int stage = 0;
        if (!excludes.contains("opennlp")) {
            map.put("opennlp", stage++);
        }
        map.put("dict", stage);
        map.put("dictionary", stage++);
        if (!excludes.contains("regex")) {
            map.put("regex", stage++);
        }
        if (!excludes.contains("crf")) {
            map.put("mallet", stage++);
        }
        map.put("feature", stage++);
        if (!excludes.contains("mira")) {
            map.put("mira", stage++);
        }
        if (!excludes.contains("rnn")) {
            map.put("rnn", stage++);
        }
        if (!excludes.contains("svm")) {
            map.put("svm", stage++);
        }
        map.put("filter", stage++);
        map.put("vote", stage);
        map.put("ensemble", stage++);
        map.put("postproc", stage++);
        if (rawXmiOutputSelected) {
            map.put("raw", stage++);
        }
        map.put("deidlevel", stage++);//no 'clean' in name b/c of already using 'clean' for cleanOutput stage
        if (cleanXmiOutputSelected) {
            map.put("clean", stage++);
        }
        if (piiWriterSelected) {
            map.put("piiwrite", stage++);
        }
        if (piiTaggingSelected) {
            map.put("piitag", stage++);
        }
        if (piiTaggingCategorySelected) {
            map.put("piitagcat", stage++);
        }
        if (resynthesisSelected) {
            map.put("resynth", stage++);
        }
        return map;
    }

    private int getAeIndex(String ae) {
        String lowerAe = ae.toLowerCase();
        for (Map.Entry<String, Integer> pair : aeNamesToPosition.entrySet()) {
            if (pair.getKey().contains(lowerAe) || lowerAe.contains((pair.getKey()))) {
                return pair.getValue();
            }
        }
        return -1;
    }

    private void setBeginEnd(String beginParam, String endParam) {
        aeNamesToPosition = createAeMap();
        try {
            begin = Integer.parseInt(beginParam);
        } catch (NumberFormatException e) {
            begin = getAeIndex(beginParam);
            if (begin == -1) {
                logger.error("{} is not valid for begin", beginParam);
                throw new RuntimeException(new Exception(beginParam + " is not valid for begin"));
            }
        }
        try {
            end = Integer.parseInt(endParam);
        } catch (NumberFormatException e) {
            end = getAeIndex(endParam) + 1;//up to and including end?
            if (end == -1) {
                logger.error("{} is not valid for end", endParam);
                throw new RuntimeException(new Exception(endParam + " is not valid for end"));
            }
        }
        if (begin < 0 || end <= begin) {//should it be up to and including?
            logger.error("begin {} and end {} are invalid", beginParam, endParam);
            throw new RuntimeException(new Exception("begin " + begin + " to end " + end + " are not valid"));
        }
        logger.debug("begin: {} -> {}, end: {} -> {}", beginParam, begin, endParam, end);
    }

    private List<AnalysisEngine> getTestMalletCrfEngines() {
        List<AnalysisEngine> analysisEngines = new ArrayList<>();

        analysisEngines.add(getAnalysisEngine(OpenNlpAnnotator.class));

        analysisEngines.add(getAnalysisEngine(DictionaryAnnotator.class));

        analysisEngines.add(getAnalysisEngine(NamedEntityChunker.class, // DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
                CleartkSequenceAnnotator.PARAM_IS_TRAINING, false, NamedEntityChunker.BROWN_CLUSTERS_FILENAME, BROWN_FILE,
                NamedEntityChunker.BROWN_CUTOFF, BROWN_CUTOFF,
                GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, new File(CRF_MODEL_FILE)));

        analysisEngines.add(getAnalysisEngine(RawXmiOutputAnnotator.class,
                RawXmiOutputAnnotator.OUTPUT_EXTENSION_PARAM, ".complete-system-output", RawXmiOutputAnnotator.PRETTY_PRINT_PARAM, true,
                RawXmiOutputAnnotator.OUTPUT_TO_DB, dbOutputSelected, RawXmiOutputAnnotator.OUTPUT_TO_FILE, fileOutputSelected));

        return analysisEngines;
    }

    private List<AnalysisEngine> getEnsembleAnalysisEngines() {
        List<AnalysisEngine> analysisEngines = new ArrayList<>();

        if (excludes.contains("opennlp")) {
            logger.debug("Excluded OpenNLP engine");
        } else {
            analysisEngines.add(getAnalysisEngine(OpenNlpAnnotator.class));
        }
        analysisEngines.add(getAnalysisEngine(DictionaryAnnotator.class));

        analysisEngines.add(getAnalysisEngine(FeatureAnnotator.class,
                FeatureAnnotator.WORD_VECTOR_CL_FILE_NAME, WORD_VECTOR_CL_FILE));

        if (Thread.currentThread().isInterrupted()) {//test, make these inits and checks into loop ?
            throw new RuntimeException("interrupted");
        }
        EnsembleAnnotator.setRnnPortNumber(portRnn);
        if (excludes.isEmpty()) {
            analysisEngines.add(getAnalysisEngine(EnsembleAnnotator.class,
                    EnsembleAnnotator.RNN_HOST_NAME, "localhost", EnsembleAnnotator.RNN_PORT_NUMBER, portRnn,
                    EnsembleAnnotator.SVM_MODEL_FILE, SVM_MODEL_FILE,
                    EnsembleAnnotator.SVM_FEATURE_INDEX_FILE_NAME, SVM_FEATURE_INDEX_FILE,
                    EnsembleAnnotator.SVM_LABEL_FILE_NAME, SVM_LABEL_FILE,
                    EnsembleAnnotator.SVM_TEMPLATE_FILE_NAME, SVM_TEMPLATE_FILE,
                    EnsembleAnnotator.MIRA_MODEL_FILE, MIRA_MODEL_FILE,
                    EnsembleAnnotator.BROWN_CLUSTERS_FILENAME, BROWN_FILE, EnsembleAnnotator.BROWN_CUTOFF, BROWN_CUTOFF,
                    GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, new File(CRF_MODEL_FILE),
                    EnsembleAnnotator.REGEX_CONCEPTS_FILE, REGEX_CONCEPTS_FILE));
        } else {
            analysisEngines.add(getAnalysisEngine(EnsemblePartialAnnotator.class,
                    EnsemblePartialAnnotator.EXCLUDES_LIST, excludes.toString(),
                    EnsembleAnnotator.RNN_HOST_NAME, "localhost", EnsembleAnnotator.RNN_PORT_NUMBER, portRnn,
                    EnsembleAnnotator.SVM_MODEL_FILE, SVM_MODEL_FILE,
                    EnsembleAnnotator.SVM_FEATURE_INDEX_FILE_NAME, SVM_FEATURE_INDEX_FILE,
                    EnsembleAnnotator.SVM_LABEL_FILE_NAME, SVM_LABEL_FILE,
                    EnsembleAnnotator.SVM_TEMPLATE_FILE_NAME, SVM_TEMPLATE_FILE,
                    EnsembleAnnotator.MIRA_MODEL_FILE, MIRA_MODEL_FILE,
                    EnsembleAnnotator.BROWN_CLUSTERS_FILENAME, BROWN_FILE, EnsembleAnnotator.BROWN_CUTOFF, BROWN_CUTOFF,
                    GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, new File(CRF_MODEL_FILE),
                    EnsembleAnnotator.REGEX_CONCEPTS_FILE, REGEX_CONCEPTS_FILE));
        }

        analysisEngines.add(getAnalysisEngine(VoteAnnotator.class, VoteAnnotator.THRESH_HOLD, 1));
        analysisEngines.add(getAnalysisEngine(PostPiiProcessing.class));

        // Post-processing engines
        if (rawXmiOutputSelected) {
            analysisEngines.add(getAnalysisEngine(RawXmiOutputAnnotator.class,
                    RawXmiOutputAnnotator.OUTPUT_EXTENSION_PARAM, ".complete-system-output",
                    RawXmiOutputAnnotator.OUTPUT_TO_FILE, fileOutputSelected, RawXmiOutputAnnotator.OUTPUT_TO_DB, dbOutputSelected,
                    RawXmiOutputAnnotator.IS_CLEAN, false, RawXmiOutputAnnotator.PRETTY_PRINT_PARAM, true));
        }
        analysisEngines.add(getAnalysisEngine(DeidLevelCleaner.class));
        if (cleanXmiOutputSelected) {
            analysisEngines.add(getAnalysisEngine(RawXmiOutputAnnotator.class,
                    RawXmiOutputAnnotator.OUTPUT_EXTENSION_PARAM, ".filtered-system-output",
                    RawXmiOutputAnnotator.OUTPUT_TO_FILE, fileOutputSelected, RawXmiOutputAnnotator.OUTPUT_TO_DB, dbOutputSelected,
                    RawXmiOutputAnnotator.IS_CLEAN, true, RawXmiOutputAnnotator.PRETTY_PRINT_PARAM, true));
        }

        if (piiWriterSelected) {
            analysisEngines.add(getAnalysisEngine(PiiWriterAnnotator.class,
                    PiiWriterAnnotator.OUTPUT_EXTENSION_PARAM, ".detectedPII.txt",
                    PiiWriterAnnotator.OUTPUT_TO_FILE, fileOutputSelected, PiiWriterAnnotator.OUTPUT_TO_DB, dbOutputSelected));
        }

        if (piiTaggingSelected) {
            analysisEngines.add(getAnalysisEngine(PiiTagging.class,
                    PiiTagging.CATEGORY, "none", PiiTagging.OUTPUT_EXTENSION_PARAM, ".deid.piiTag.txt",
                    PiiTagging.OUTPUT_TO_FILE, fileOutputSelected, PiiTagging.OUTPUT_TO_DB, dbOutputSelected));
        }
        if (piiTaggingCategorySelected) {
            analysisEngines.add(getAnalysisEngine(PiiTagging.class,
                    PiiTagging.CATEGORY, "top", PiiTagging.OUTPUT_EXTENSION_PARAM, ".deid.piiCategoryTag.txt",
                    PiiTagging.OUTPUT_TO_FILE, fileOutputSelected, PiiTagging.OUTPUT_TO_DB, dbOutputSelected));
        }
        if (resynthesisSelected) {
            analysisEngines.add(getAnalysisEngine(ResynthesisAnnotator.class,
                    ResynthesisAnnotator.OUTPUT_EXTENSION_PARAM, ".deid.resynthesis.txt",
                    ResynthesisAnnotator.OUTPUT_TO_DB, dbOutputSelected,
                    ResynthesisAnnotator.OUTPUT_TO_FILE, fileOutputSelected));
        }
        analysisEngines.add(getAnalysisEngine(DocumentListAnnotator.class, DocumentListAnnotator.OUTPUT_DIRECTORY_PARAM, outputDir));
        return analysisEngines;
    }

    private void testCrf() {
        getCollectionReader();
        List<AnalysisEngine> engines = new ArrayList<>(getTestMalletCrfEngines());
        analysisEngines = engines.toArray(new AnalysisEngine[0]);
        tryCreateJCas();
        runPipeline();
    }

    private void testPostProcessing() {
        try {
            CollectionReader reader = CollectionReaderFactory.createReader(XmlCollectionReaderClearTkTraining.class,
                    XmlCollectionReaderClearTkTraining.PARAM_INPUTDIR, inputDir, XmlCollectionReaderClearTkTraining.PARAM_FAILUNKNOWN, "true",
                    GeneralCollectionReader.DEID_LEVEL, deidLevel.toString(),
                    GeneralCollectionReader.OUTPUT_CLEAN, true,
                    GeneralCollectionReader.OUTPUT_GENERAL_TAG, false,
                    GeneralCollectionReader.OUTPUT_CATEGORY_TAG, false,
                    GeneralCollectionReader.OUTPUT_PII, false,
                    GeneralCollectionReader.OUTPUT_RESYNTHESIS, false,
                    GeneralCollectionReader.OUTPUT_RAW, true,
                    GeneralCollectionReader.CLEAN_DIRECTORY, outputDir,
                    GeneralCollectionReader.RAW_DIRECTORY, outputDir);
            AnalysisEngine postProc = AnalysisEngineFactory.createEngine(PostPiiProcessing.class);
            AnalysisEngine rawXmi = getAnalysisEngine(RawXmiOutputAnnotator.class,
                    RawXmiOutputAnnotator.OUTPUT_EXTENSION_PARAM, ".complete-system-output", RawXmiOutputAnnotator.PRETTY_PRINT_PARAM, true,
                    RawXmiOutputAnnotator.OUTPUT_TO_DB, dbOutputSelected, RawXmiOutputAnnotator.OUTPUT_TO_FILE, fileOutputSelected);

            AnalysisEngine cleaner = AnalysisEngineFactory.createEngine(DeidLevelCleaner.class);
            AnalysisEngine cleanOut = getAnalysisEngine(RawXmiOutputAnnotator.class,
                    RawXmiOutputAnnotator.OUTPUT_EXTENSION_PARAM, ".filtered-system-output", RawXmiOutputAnnotator.PRETTY_PRINT_PARAM, true,
                    RawXmiOutputAnnotator.OUTPUT_TO_DB, dbOutputSelected, RawXmiOutputAnnotator.OUTPUT_TO_FILE, fileOutputSelected);

            SimplePipeline.runPipeline(reader, postProc, rawXmi, cleaner, cleanOut);
        } catch (UIMAException | IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    private void testExecute() {
        // if (!processLicense()) {
        //     System.out.println("License Error");
        //     return;
        // }
        if (!DeidPipeline.getExcludes().contains("rnn")) {
            if (!DeidPipeline.setRnnPermsissions()) {
                System.out.println("Error setting RNN permissions");
                return;
            }
            portRnn = DeidPipeline.startRnnService();
            if (portRnn == -1) {
                logger.error("Couldn't start RNN service");
                return;
            }
        }
        if (begin > 0) {
            try {
                reader = (GeneralCollectionReader) CollectionReaderFactory.createReader(XmlCollectionReaderClearTkTraining.class,
                        XmlCollectionReaderClearTkTraining.PARAM_INPUTDIR, inputDir, XmlCollectionReaderClearTkTraining.PARAM_FAILUNKNOWN, "true",
                        GeneralCollectionReader.DEID_LEVEL, deidLevel.toString(),
                        GeneralCollectionReader.OUTPUT_CLEAN, cleanXmiOutputSelected,
                        GeneralCollectionReader.OUTPUT_GENERAL_TAG, piiTaggingSelected,
                        GeneralCollectionReader.OUTPUT_CATEGORY_TAG, piiTaggingCategorySelected,
                        GeneralCollectionReader.OUTPUT_PII, piiWriterSelected,
                        GeneralCollectionReader.OUTPUT_RESYNTHESIS, resynthesisSelected,
                        GeneralCollectionReader.OUTPUT_RAW, rawXmiOutputSelected,

                        GeneralCollectionReader.CLEAN_DIRECTORY, outputDir,
                        GeneralCollectionReader.GENERAL_TAG_DIRECTORY, outputDir,
                        GeneralCollectionReader.CATEGORY_TAG_DIRECTORY, outputDir,
                        GeneralCollectionReader.PII_DIRECTORY, outputDir,
                        GeneralCollectionReader.RESYNTHESIS_DIRECTORY, outputDir,
                        GeneralCollectionReader.RAW_DIRECTORY, outputDir);
            } catch (ResourceInitializationException e) {
                logger.throwing(e);
                return;
            }
        } else {
            String message = getCollectionReader();
            if (!message.isEmpty()) {
                logger.error(message);
                return;
            }
        }
        reader.setPiiOptions(piiOptions);
        List<AnalysisEngine> engines = new ArrayList<>(getEnsembleAnalysisEngines());

        if (begin != -1) {
            if (begin > engines.size()) {
                logger.error("begin {} to end {} are invalid", begin, end);
                throw new RuntimeException(new Exception("begin " + begin + " to end " + end + " is are invalid"));
            }
            if (end > engines.size()) {
                end = engines.size();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("These are the engines being considered:");
                for (AnalysisEngine ae : engines) {
                    logger.debug("{}", ae.getAnalysisEngineMetaData().getName());
                }
            }
            //DocListAnnotator?
            //begin is off by ? at resynth instead of level, end is at resynth, after the -begin above, but sublist is [begin,end)
            List<AnalysisEngine> enginesSublist = engines.subList(begin, end);
            if (xmiOutput) {
                enginesSublist.add(getAnalysisEngine(RawXmiOutputAnnotator.class,
                        RawXmiOutputAnnotator.OUTPUT_EXTENSION_PARAM, "", RawXmiOutputAnnotator.PRETTY_PRINT_PARAM, true,
                        RawXmiOutputAnnotator.IS_CLEAN, false,
                        RawXmiOutputAnnotator.OUTPUT_TO_DB, dbOutputSelected, RawXmiOutputAnnotator.OUTPUT_TO_FILE, fileOutputSelected));
            }
            analysisEngines = enginesSublist.toArray(new AnalysisEngine[0]);
            if (logger.isDebugEnabled()) {
                logger.debug("These are the engines being run:");
                for (AnalysisEngine ae : analysisEngines) {
                    logger.debug("{}", ae.getAnalysisEngineMetaData().getName());
                }
            }
        } else {
            if (xmiOutput) {
                engines.add(getAnalysisEngine(RawXmiOutputAnnotator.class,
                        RawXmiOutputAnnotator.OUTPUT_EXTENSION_PARAM, "", RawXmiOutputAnnotator.PRETTY_PRINT_PARAM, true, RawXmiOutputAnnotator.IS_CLEAN, false,
                        RawXmiOutputAnnotator.OUTPUT_TO_DB, dbOutputSelected, RawXmiOutputAnnotator.OUTPUT_TO_FILE, fileOutputSelected));
            }
            analysisEngines = engines.toArray(new AnalysisEngine[0]);
        }
        tryCreateJCas();
        runPipeline();
        if (deidPipelineTask != null) {
            deidPipelineTask.succeed();
        }
    }

    private void runMuscCreateResynthText() {
        //read PII from files, get text from file for resynth, run pipeline with resynth only
        CombinedCorpusGoldCollectionReader.isTraining = true;

        try {
            TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription();

//            reader = (GeneralCollectionReader) CollectionReaderFactory.createReader(XmlCollectionReaderClearTkTraining.class,
//                    XmlCollectionReaderClearTkTraining.PARAM_INPUTDIR, inputDir, XmlCollectionReaderClearTkTraining.PARAM_FAILUNKNOWN, "true");

            reader = (FileSystemCollectionReader) CollectionReaderFactory.createReader(CombinedCorpusGoldCollectionReader.class,
                    typeSystemDescription, FileSystemCollectionReader.INPUT_DIRECTORY_PARAMETER, inputDir,
                    FileSystemCollectionReader.RECURSIVE_PARAMETER, false,
                    CombinedCorpusGoldCollectionReader.INPUT_EXTENSION_PARAM, "xml",
                    FileSystemCollectionReader.FILE_SIZE_LIMIT, 1000000, FileSystemCollectionReader.FILE_LIMIT, 1000000,

                    GeneralCollectionReader.DEID_LEVEL, deidLevel,
                    GeneralCollectionReader.OUTPUT_CLEAN, cleanXmiOutputSelected,
                    GeneralCollectionReader.OUTPUT_GENERAL_TAG, piiTaggingSelected,
                    GeneralCollectionReader.OUTPUT_CATEGORY_TAG, piiTaggingCategorySelected,
                    GeneralCollectionReader.OUTPUT_PII, piiWriterSelected,
                    GeneralCollectionReader.OUTPUT_RESYNTHESIS, resynthesisSelected,
                    GeneralCollectionReader.OUTPUT_RAW, rawXmiOutputSelected,

                    GeneralCollectionReader.CLEAN_DIRECTORY, outputDir,
                    GeneralCollectionReader.GENERAL_TAG_DIRECTORY, outputDir,
                    GeneralCollectionReader.CATEGORY_TAG_DIRECTORY, outputDir,
                    GeneralCollectionReader.PII_DIRECTORY, outputDir,
                    GeneralCollectionReader.RESYNTHESIS_DIRECTORY, outputDir,
                    GeneralCollectionReader.RAW_DIRECTORY, outputDir);
        } catch (UIMAException e) {
            logger.throwing(e);
            return;
        }

        List<AnalysisEngine> analysisEngines = new ArrayList<>();
        analysisEngines.add(getAnalysisEngine(ResynthesisAnnotator.class,
                ResynthesisAnnotator.OUTPUT_TO_DB, false, ResynthesisAnnotator.OUTPUT_TO_FILE, true, ResynthesisAnnotator.SAVE_FOR_SERVICE, false,
                ResynthesisAnnotator.OUTPUT_EXTENSION_PARAM, ".deid.resynthesis.txt"));
        analysisEngines.add(getAnalysisEngine(CasConsumerWriteAnnotation.class, CasConsumerWriteAnnotation.PARAM_OUTPUT_FOLDER, outputDir,
                CasConsumerWriteAnnotation.PRINT_ORIGINAL_TEXT, true));
        analysisEngines.add(getAnalysisEngine(RawXmiOutputAnnotator.class,
                RawXmiOutputAnnotator.OUTPUT_EXTENSION_PARAM, "",
                RawXmiOutputAnnotator.OUTPUT_TO_DB, false, RawXmiOutputAnnotator.OUTPUT_TO_FILE, true, RawXmiOutputAnnotator.PRETTY_PRINT_PARAM, true));
        tryCreateJCas();
        this.analysisEngines = analysisEngines.toArray(new AnalysisEngine[0]);
        runPipeline(false);
    }

    public static class CasConsumerWriteAnnotation extends JCasAnnotator_ImplBase {
        public static final String PARAM_OUTPUT_FOLDER = "outputFolder";
        public static final String PRINT_ORIGINAL_TEXT = "printOriginalText";
        @ConfigurationParameter(name = PARAM_OUTPUT_FOLDER)
        private String outputFolder;
        @ConfigurationParameter(name = PRINT_ORIGINAL_TEXT)
        private boolean printOriginalText = false;
        private Pattern spaceBeginOrEnd = Pattern.compile("^\\s|\\s$");

        @Override
        public void initialize(UimaContext context) throws ResourceInitializationException {
            super.initialize(context);
            outputFolder = DeidPipeline.addTrailingSlash(outputFolder);
            new File(outputFolder).mkdirs();
            logger.debug("Writing Annotations to {}", outputFolder);
        }

        @Override
        public void process(JCas jCas) throws AnalysisEngineProcessException {
            String fileName = getFileName(jCas).replaceAll("\\.*", "") + ".streetCity";
            try (FileWriter writer = new FileWriter(outputFolder + fileName)) {
                for (PiiAnnotation ann : jCas.getAnnotationIndex(PiiAnnotation.class)) {
                    if (ann.getPiiSubtype().equals("Street") || ann.getPiiSubtype().equals("City")) {
                        if (spaceBeginOrEnd.matcher(ann.getCoveredText()).find()) {
                            writer.write("extra space [" + ann.getCoveredText() + "]\n");
                            logger.error("{} had extra space on annotation {}-{} type: {} [{}]", fileName, ann.getBegin(), ann.getEnd(), ann.getPiiSubtype(), ann.getCoveredText());
                        }
//                        writer.write(String.format("PII: %5d - %5d by %6s, Category: %20s, SubType %20s: {%s}%n", ann.getBegin(), ann.getEnd(),
//                                ann.getMethod(), ann.getPiiType(), ann.getPiiSubtype(), ann.getCoveredText().replaceAll("[\\r\\n]", "*NL*")));
                        writer.write(String.format("%5d - %5d, SubType %20s: {%s}%n", ann.getBegin(), ann.getEnd(),
                                ann.getPiiSubtype(), ann.getCoveredText().replaceAll("[\\r\\n]", "*NL*")));
                    }
                }
            } catch (Exception e) {
                logger.throwing(e);
                throw new AnalysisEngineProcessException(e);
            }

            fileName = getFileName(jCas).replaceAll("\\.*", "") + ".stateCountry";
            try (FileWriter writer = new FileWriter(outputFolder + fileName)) {
                for (PiiAnnotation ann : jCas.getAnnotationIndex(PiiAnnotation.class)) {
                    if (ann.getPiiSubtype().equals("State") || ann.getPiiSubtype().equals("Country")) {
                        writer.write(String.format("%5d - %5d SubType %20s: {%s}%n", ann.getBegin(), ann.getEnd(),
                                ann.getPiiSubtype(), ann.getCoveredText().replaceAll("[\\r\\n]", "*NL*")));
                    }
                }
            } catch (Exception e) {
                logger.throwing(e);
                throw new AnalysisEngineProcessException(e);
            }

            if (printOriginalText) {
                fileName = getFileName(jCas).replaceAll("\\.*", "") + ".txt";
                try (FileWriter writer = new FileWriter(outputFolder + fileName)) {
                    writer.write(jCas.getDocumentText());
                } catch (Exception e) {
                    logger.throwing(e);
                    throw new AnalysisEngineProcessException(e);
                }
            }
        }
    }
}
