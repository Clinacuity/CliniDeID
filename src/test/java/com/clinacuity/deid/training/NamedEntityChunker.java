
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

/*
 * Copyright (c) 2012, Regents of the University of Colorado All rights reserved. Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of the University of Colorado at Boulder
 * nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. THIS
 * SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.clinacuity.deid.training;

import com.clinacuity.deid.clearTkChunking.WordShape;
import com.clinacuity.deid.type.BaseToken;
import com.clinacuity.deid.type.Chunk;
import com.clinacuity.deid.type.DictionaryAnnotation;
import com.clinacuity.deid.type.PiiAnnotation;
import com.clinacuity.deid.type.Sentence;
import com.clinacuity.deid.util.Utilities;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instances;
import org.cleartk.ml.chunking.BioChunking;
import org.cleartk.ml.feature.TypePathFeature;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Following;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.extractor.TypePathExtractor;
import org.cleartk.ml.feature.function.CharacterCategoryPatternFunction;
import org.cleartk.ml.feature.function.CharacterCategoryPatternFunction.PatternType;
import org.cleartk.ml.feature.function.CharacterNgramFeatureFunction;
import org.cleartk.ml.feature.function.ContainsHyphenFeatureFunction;
import org.cleartk.ml.feature.function.FeatureFunction;
import org.cleartk.ml.feature.function.FeatureFunctionExtractor;
import org.cleartk.ml.feature.function.NumericTypeFeatureFunction;
import org.cleartk.ml.jar.GenericJarClassifierFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.clinacuity.deid.mains.DeidPipeline.PII_LOG;

/**
 * This is based on named entity chunking example's NamedEntityChunker by Steven Bethard
 * <p>
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 *
 * @author Steven Bethard
 * @author Gary Underwood
 */
public class NamedEntityChunker extends CleartkSequenceAnnotator<String> {
    public static final String BROWN_CLUSTERS_FILENAME = "brownClustersFileName";
    public static final String BROWN_CUTOFF = "brownCutoff";
    public static final String OPTION = "option";
    public static final String DATA_WRITER_CLASS_NAME = "dataWriterClassName";
    private static final Logger logger = LogManager.getLogger();
    private static final Set<String> badZipcodes = Set.of("000", "002", "003", "004", "099", "213", "269", "343", "345", "348", "353", "419", "428", "429", "517", "518", "519", "529", "533", "536", "552", "568", "578", "579", "589", "621", "632", "642", "643", "659", "663", "682", "694", "695", "696", "697", "698", "699", "702", "709", "715", "732", "742", "771", "817", "818", "819", "839", "848", "849", "854", "858", "861", "862", "866", "867", "868", "869", "872", "876", "886", "887", "888", "892", "896", "899", "909", "929", "987");
    private static final Pattern zipFormat = Pattern.compile("\\d{5}(.?\\d{4})?");
    private static final Map<String, String> subToParentTypeMap = createSubToParentTypeMap();
    private static HashMap<String, String> brownClusters = null;
    @ConfigurationParameter(name = BROWN_CLUSTERS_FILENAME)
    private static String brownClustersFileName;
    @ConfigurationParameter(name = BROWN_CUTOFF, defaultValue = "7")
    private static int brownCutoff; // number of bits to use from brown cluster
    @ConfigurationParameter(name = OPTION, defaultValue = "0")
    private static String optionGeneric;
    // not sure, but don't see why extractors and chunking can't be static
    private FeatureExtractor1<BaseToken> extractor;
    private CleartkExtractor<BaseToken, BaseToken> contextExtractor;
    private BioChunking<BaseToken, PiiAnnotation> chunking;
    @ConfigurationParameter(name = DATA_WRITER_CLASS_NAME, defaultValue = "org.cleartk.ml.jar.SequenceDataWriter_ImplBase")
    private String dataWriterClassName;
    private int idNum = 0;

    public static Map<String, String> createSubToParentTypeMap() {
        return Map.ofEntries(
                Map.entry("Profession", "OCCUPATION"),
                Map.entry("Date", "TEMPORAL"), Map.entry("Age", "TEMPORAL"), Map.entry("ClockTime", "TEMPORAL"), Map.entry("Season", "TEMPORAL"), Map.entry("DayOfWeek", "TEMPORAL"),
                Map.entry("OtherIDNumber", "IDENTIFIER"), Map.entry("SSN", "IDENTIFIER"),
                Map.entry("Provider", "NAME"), Map.entry("Patient", "NAME"), Map.entry("Relative", "NAME"), Map.entry("OtherPerson", "NAME"),
                Map.entry("ElectronicAddress", "CONTACT_INFORMATION"), Map.entry("PhoneFax", "CONTACT_INFORMATION"),
                Map.entry("Zip", "ADDRESS1"), Map.entry("Street", "ADDRESS1"),
                Map.entry("City", "ADDRESS1"), Map.entry("State", "ADDRESS1"), Map.entry("Country", "ADDRESS1"),
                Map.entry("OtherGeo", "LOCATION"), Map.entry("OtherOrgName", "LOCATION"), Map.entry("HealthCareUnitName", "LOCATION"));
    }

    private static void initializeBrownClusters() throws ResourceInitializationException {// new Brown testfile
        try (FileInputStream fis = new FileInputStream(Utilities.getExternalFile(brownClustersFileName));
             ObjectInputStream ois = new ObjectInputStream(fis);) {
            @SuppressWarnings("unchecked") // for casting Brown Cluster's serialized object, have to have separate variable or suppress whole method
                    HashMap<String, String> tempClusters = (HashMap<String, String>) ois.readObject();
            brownClusters = tempClusters;
        } catch (IOException | ClassNotFoundException e2) {
            throw new ResourceInitializationException(e2);
        }
    }

    //experiment to test combining several features, resulted in similar training time and results, -10% data, likely do this later when features are more set
//    private class TypePathExtractorPosLemmaBrown extends TypePathExtractor<BaseToken> {
//        public TypePathExtractorPosLemmaBrown(Class<BaseToken> focusClass, String typePath) {
//            super(focusClass, typePath);
//        }
//
//        @Override
//        public List<Feature> extract(JCas view, BaseToken focusAnnotation) throws CleartkExtractorException {
//            List<Feature> features = new ArrayList<>();
//            features.add(new Feature("partOfSpeech", focusAnnotation.getPartOfSpeech()));
//            String token = focusAnnotation.getCoveredText();
//            String result = brownClusters.get(token);
//            if (result == null) {
//                result = brownClusters.get(token.toLowerCase());
//            }
//            if (result != null && result.length() > brownCutoff) {// don't truncate 'null' or bit string that is already short enough
//                result = result.substring(0, brownCutoff);
//            }
//            features.add(new Feature("Brown", result));
//            return features;
//        }
//    }


    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        if (brownClusters == null) {// is this if needed?
            initializeBrownClusters();
        }

        // TODO: Is there a way to get a previous word's label? Get CRF annotations and check for begin/end of previous token?
        // make this an array, then command line parameters for which to include to allow scripting a lot of models?
        if ("0".equals(optionGeneric)) {
            //BaseToken: POS fine, chunk and Ner are BIO format; tag and normalizedForm are null
            extractor = new CombinedExtractor1Extended<BaseToken>(
                    new FeatureFunctionExtractor<>(new CoveredTextExtractor<>(),
                            new CharacterCategoryPatternFunction<>(PatternType.REPEATS_MERGED)),
                    new TypePathExtractor<>(BaseToken.class, "partOfSpeech"),
                    // new TypePathExtractorLemma<>(BaseToken.class, "canonicalForm"),
                    new FeatureFunctionExtractor<>(new CoveredTextExtractor<>(),
                            FeatureFunctionExtractor.BaseFeatures.EXCLUDE, new BrownClusterFunction()),
                    new TypePathExtractorNameDictionary<>(BaseToken.class, "nameDictionary"),
                    new TypePathExtractor<>(BaseToken.class, "chunk"),
//                     new TypePathExtractor<>(BaseToken.class, "ner"),//tried adding this, but didn't improve
//                    new TypePathExtractorChunk<>(BaseToken.class, "chunk"),
                    new FeatureFunctionExtractor<>(new CoveredTextExtractor<>(),
                            FeatureFunctionExtractor.BaseFeatures.EXCLUDE, new WordShapeFunction()),
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
            contextExtractor = new CleartkExtractor<>(BaseToken.class, extractor, new Preceding(2), new Following(2));

        }
        // the chunking definition: Tokens will be combined to form PiiAnnotation, with labels
        // from the "mentionType" attribute so that we get B-location, I-person, etc.

        chunking = new BioChunking<>(BaseToken.class, PiiAnnotation.class, "piiSubtype");
        logger.debug("NamedEntityChunker initialized {}-{} option: {} model: {}", brownClustersFileName, brownCutoff, optionGeneric,
                GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH);
    }

    // for each sentence, create features for each token then either create training data or classify those tokens
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        idNum = 0;
        logger.debug("NamedEntityChunker begin");
        for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
            List<BaseToken> tokens = JCasUtil.selectCovered(jCas, BaseToken.class, sentence);
            List<List<Feature>> featureLists = new ArrayList<>();
            for (BaseToken token : tokens) {
                List<Feature> features = new ArrayList<>();
                features.addAll(extractor.extract(jCas, token));
                features.addAll(contextExtractor.extract(jCas, token));
                featureLists.add(features);
            }
            if (this.isTraining()) {// extract the goldPiiAnnotation
                List<PiiAnnotation> namedEntityMentions = JCasUtil.selectCovered(jCas, PiiAnnotation.class, sentence);
                List<String> outcomes = chunking.createOutcomes(jCas, tokens, namedEntityMentions); // convert PiiAnnotation to BIO
                dataWriter.write(Instances.toInstances(outcomes, featureLists));
            } else { // during classification, convert classifier outcomes into PiiAnnotation
                List<String> outcomes = classifier.classify(featureLists);// get BIO classes
                chunking.createChunks(jCas, tokens, outcomes);// create PiiAnnotations
            }
        }
        completePiiAnnotations(jCas);
    }

    private void completePiiAnnotations(JCas jCas) {//set confidence, method, ID, and parent of CRF made PiiAnnotation, verify zips
        List<PiiAnnotation> toRemove = new ArrayList<>();
        for (PiiAnnotation ann : jCas.getAnnotationIndex(PiiAnnotation.class)) {
            if (ann.getMethod() != null) {
                continue;//leave other PiiAnnotations alone
            }
            if (ann.getPiiSubtype().contains("Zip") && !isValidZipcode(ann)) {
                toRemove.add(ann);
            }
            ann.setConfidence(0.0f);
            ann.setMethod("oldCRF");
            ann.setId("P" + idNum);
            idNum++;
            ann.setPiiType(subToParentTypeMap.getOrDefault(ann.getPiiSubtype(), "UNKNOWN"));
        }
        toRemove.forEach(PiiAnnotation::removeFromIndexes);
    }

    private boolean isValidZipcode(PiiAnnotation zip) {
        if (zipFormat.matcher(zip.getCoveredText()).matches() && !badZipcodes.contains(zip.getCoveredText().substring(0, 3))) {
            return true;
        } else {
            logger.log(PII_LOG, "{}", () -> "oldCRF Zipcode prediction invalid format or prefix " + zip.getBegin() + " " + zip.getEnd() + " " + zip.getCoveredText());
            return false;
        }
    }

    private static class WordShapeFunction implements FeatureFunction {
        public List<Feature> apply(Feature feature) {
            return Collections.singletonList(new Feature("WordShape", WordShape.wordShapeBuilder((String) feature.getValue())));
        }
    }

    private static class BrownClusterFunction implements FeatureFunction {
        public List<Feature> apply(Feature feature) {
            String token = (String) feature.getValue();
            String result = brownClusters.get(token);
            if (result == null) {
                result = brownClusters.get(token.toLowerCase());
            }
            if (result != null && result.length() > brownCutoff) {// don't truncate 'null' or bit string that is already short enough
                result = result.substring(0, brownCutoff);
            }
            return Collections.singletonList(new Feature("BrownCluster", result));
        }
    }

    // feature of chunk for token, on Med2 train, big test, got 2.6 higher recall, 1.7 lower precision
    private static class TypePathExtractorChunk<T extends Annotation> extends TypePathExtractor<T> {
        TypePathExtractorChunk(Class<T> focusClass, String typePath) {
            super(focusClass, typePath);
        }

        @Override
        public List<org.cleartk.ml.Feature> extract(JCas view, Annotation focusAnnotation) throws CleartkExtractorException {
            List<Chunk> chunkAnnot = JCasUtil.selectCovered(view, Chunk.class, focusAnnotation);
            if (chunkAnnot == null || chunkAnnot.isEmpty()) {// no chunk annotation for Token, create empty feature
                // return Collections.emptyList();//need to try both this and below,
                return Collections
                        .singletonList(new TypePathFeature(this.getFeatureName(), "none", super.getPath(), super.getFeatureName()));
            }
            TypePathFeature x = new TypePathFeature(this.getFeatureName(), chunkAnnot.get(0).getChunkType(), super.getPath(), super.getFeatureName());
            return Collections.singletonList(x);
//                    new TypePathFeature(this.getFeatureName(), chunkAnnot.get(0).getChunkType(), super.getPath(), super.getFeatureName()));
        }
    }

    // Create features based on which dictionaries token is found in
    private static class TypePathExtractorNameDictionary<T extends Annotation> extends TypePathExtractor<T> {
        TypePathExtractorNameDictionary(Class<T> focusClass, String typePath) {
            super(focusClass, typePath);
        }

        @Override
        public List<org.cleartk.ml.Feature> extract(JCas view, Annotation focusAnnotation) throws CleartkExtractorException {
            List<DictionaryAnnotation> dictAnnots = JCasUtil.selectCovered(view, DictionaryAnnotation.class, focusAnnotation);
            if (dictAnnots == null || dictAnnots.isEmpty()) {// no Dictionary annotation for Token, create empty feature
                // return Collections.emptyList();//tried both this and below, nearly the same results but emptyList was .1% F1 and .3% recall lower,
                return Collections.singletonList(new TypePathFeature("Dictionary", "none", super.getPath(), super.getFeatureName()));
            }
            List<org.cleartk.ml.Feature> features = new ArrayList<>();
            String[] parts = dictAnnots.get(0).toString().split("\\s+");// each dictionary presence is a separate boolean, this allows way to iterate through

            for (int i = 8; i < parts.length; i += 2) {// TODO: this 8 is dependent on DictionaryAnnotation's ordering of parts and its toString's ordering,
                // reflection seems too expensive
                // System.out.println(i + ": " + parts[i]);
                if (parts[i].equals("true")) {
                    features.add(new Feature(parts[i - 1], "true"));// adding false for others lowered numbers on small train/test sets
                }
            }
            return features;
        }
    }

    // Gets lemma (which is in canonicalForm) from WordToken if available
    //This can't work as normalized form is currently empty
   /* private class TypePathExtractorLemma<T extends Annotation> extends TypePathExtractor<T> {
        TypePathExtractorLemma(Class<T> focusClass, String typePath) {
            super(focusClass, typePath);
        }

        @Override
        public List<Feature> extract(JCas view, Annotation focusAnnotation) throws CleartkExtractorException {
            if (focusAnnotation instanceof BaseToken) {
                return Collections.singletonList(new Feature("Lemma", ((BaseToken) focusAnnotation).getNormalizedForm()));
            } else {
                return Collections.singletonList(new Feature("Lemma", focusAnnotation.getCoveredText()));
            }
        }
    }*/

    public static class CombinedExtractor1Extended<T extends Annotation> implements FeatureExtractor1<T> {
        private List<FeatureExtractor1<T>> extractors;

        @SafeVarargs
        public CombinedExtractor1Extended(FeatureExtractor1<T>... extractsToAdd) {
            extractors = Lists.newArrayList();
            Collections.addAll(extractors, extractsToAdd);
        }

        public List<FeatureExtractor1<T>> getExtractors() {
            return this.extractors;
        }

        /**
         * Extract features from the <tt>Annotation</tt> using the sub-extractors. The parameters are passed on as they are.
         *
         * @return the combined list of features generated by the sub-extractors. If <tt>name</tt> was set in the constructor, the top-level context of all features
         * will have that as their name.
         */
        public List<Feature> extract(JCas view, T focusAnnotation) throws CleartkExtractorException {
            List<Feature> result = new ArrayList<>();
            for (FeatureExtractor1<T> extractor : this.extractors) {
                result.addAll(extractor.extract(view, focusAnnotation));
            }
            return result;
        }
    }
}
// public static final String WORD2VEC_MODEL_FILENAME = "word2VecModelFileName";
// @ConfigurationParameter(name = WORD2VEC_MODEL_FILENAME, defaultValue = "CRFmodels/w2vModelB-5-1-100-42-5.bin")
// private static String word2VecModelFileName;
//
// public static final String WORD2VEC_HASHMAP_MODEL_FILENAME = "word2VecHashMapModelFileName";
// @ConfigurationParameter(name = WORD2VEC_HASHMAP_MODEL_FILENAME)
// private static String word2VecHashMapModelFileName;

// initialize:
// private Word2Vec model = null;
// private Map<String, String[]> mapWord2Vec = null;
// try {
// // model = TryWordVec.readModel(Utilities.getExternalFile(word2VecModelFileName));
// mapWord2Vec = TryWordVec.readMap(Utilities.getExternalFile(word2VecHashMapModelFileName));
// } catch (IOException e) {
// logger.throwing(e);
// }
// private class TypePathWord2VecFromMap<T extends Annotation> extends TypePathExtractor<T> {
// public TypePathWord2VecFromMap(Class<T> focusClass, String typePath) {
// super(focusClass, typePath);
// }
//
// @Override
// public List<org.cleartk.ml.Feature> extract(JCas view, Annotation focusAnnotation) throws CleartkExtractorException {
// if (focusAnnotation instanceof WordToken) {// ignore punctuation and numbers
// String word = focusAnnotation.getCoveredText();
// String[] values = mapWord2Vec.get(word);
// if (values == null) {// try lowercase version, currently using capitalization as it occurs in training text,
// values = mapWord2Vec.get(word.toLowerCase());
// }
// if (values == null) {// try canonical form
// values = mapWord2Vec.get(((WordToken) focusAnnotation).getCanonicalForm());
// }
// if (values == null) {// word not found,
// return Collections.singletonList(new TypePathFeature(null, 0, super.getPath(), super.getFeatureName()));
// }
// List<org.cleartk.ml.Feature> results = new ArrayList<>();
// for (String val : values) {
// results.add(new TypePathFeature(null, val, super.getPath(), super.getFeatureName()));
// }
// return results;
// } else {
// return Collections.singletonList(new TypePathFeature(null, 0, super.getPath(), super.getFeatureName()));
// }
// }
// }
//
// // Gets Word2Vec value
// private class TypePathWord2Vec<T extends Annotation> extends TypePathExtractor<T> {
// public TypePathWord2Vec(Class<T> focusClass, String typePath) {
// super(focusClass, typePath);
// }
//
// @Override
// public List<org.cleartk.ml.Feature> extract(JCas view, Annotation focusAnnotation) throws CleartkExtractorException {
// if (focusAnnotation instanceof WordToken) {// ignore punctuation and numbers
// String word = focusAnnotation.getCoveredText();
// double[] values = model.getWordVector(word);
// if (values == null) {// try lowercase version, currently using capitalization as it occurs in training text
// values = model.getWordVector(word.toLowerCase());
// }
// if (values == null) {// try canonical form
// values = model.getWordVector(((WordToken) focusAnnotation).getCanonicalForm());
// }
// if (values == null) {// word not found
// return Collections.singletonList(new TypePathFeature(null, 0, super.getPath(), super.getFeatureName()));
// }
// List<org.cleartk.ml.Feature> results = new ArrayList<>();
// for (double val : values) {
// results.add(new TypePathFeature(null, Double.toString(val), super.getPath(), super.getFeatureName()));
// }
// return results;
// } else {
// return Collections.singletonList(new TypePathFeature(null, 0, super.getPath(), super.getFeatureName()));
// }
// }
// }
