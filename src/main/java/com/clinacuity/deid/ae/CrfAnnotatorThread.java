
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


import com.clinacuity.deid.clearTkChunking.WordShape;
import com.clinacuity.deid.type.BaseToken;
import com.clinacuity.deid.type.Chunk;
import com.clinacuity.deid.type.DictionaryAnnotation;
import com.clinacuity.deid.type.PiiAnnotation;
import com.clinacuity.deid.type.Sentence;
import com.clinacuity.deid.util.Util;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.SequenceClassifier;
import org.cleartk.ml.feature.TypePathFeature;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.extractor.TypePathExtractor;
import org.cleartk.ml.feature.function.FeatureFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import static com.clinacuity.deid.mains.DeidPipeline.PII_LOG;

public class CrfAnnotatorThread implements Runnable {//extends CleartkSequenceAnnotator<String>
    private static final Set<String> BAD_ZIPCODES = Set.of("000", "002", "003", "004", "099", "213", "269", "343", "345", "348", "353", "419", "428", "429", "517", "518", "519", "529", "533", "536", "552", "568", "578", "579", "589", "621", "632", "642", "643", "659", "663", "682", "694", "695", "696", "697", "698", "699", "702", "709", "715", "732", "742", "771", "817", "818", "819", "839", "848", "849", "854", "858", "861", "862", "866", "867", "868", "869", "872", "876", "886", "887", "888", "892", "896", "899", "909", "929", "987");
    private static final Pattern ZIP_FORMAT = Pattern.compile("\\d{5}(.?\\d{4})?");
    private static final Logger LOGGER = LogManager.getLogger();
    private static int brownCutoff = 7; // number of bits to use from brown cluster, static b/c of how used by feature classes
    private static Map<String, String> brownClusters;
    private String fileName;
    private CountDownLatch doneSignal;
    private SequenceClassifier<String> classifier;
    private FeatureExtractor1<BaseToken> extractor;
    private CleartkExtractor<BaseToken, BaseToken> contextExtractor;
    private int idNum = 0;
    private JCas jCas;
    private List<PiiAnnotation> newPii;

    CrfAnnotatorThread(Map<String, String> brownClusters, int brownCutoff,
                       FeatureExtractor1<BaseToken> extractor, CleartkExtractor<BaseToken, BaseToken> contextExtractor,
                       SequenceClassifier<String> classifier, JCas jCas, CountDownLatch down, List<PiiAnnotation> newPii, String fileName) {
        CrfAnnotatorThread.brownClusters = brownClusters;
        CrfAnnotatorThread.brownCutoff = brownCutoff;
        this.extractor = extractor;
        this.contextExtractor = contextExtractor;
        this.classifier = classifier;
        this.jCas = jCas;
        doneSignal = down;
        this.newPii = newPii;
        this.fileName = fileName;
    }


    public void run() {
        idNum = 0;
        LOGGER.debug("CRF Thread begin");
        try {
            for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
                List<BaseToken> tokens = JCasUtil.selectCovered(jCas, BaseToken.class, sentence);
                List<List<Feature>> featureLists = new ArrayList<>();
                for (BaseToken token : tokens) {
                    List<Feature> features = new ArrayList<>();
                    features.addAll(extractor.extract(jCas, token));
                    features.addAll(contextExtractor.extract(jCas, token));
                    featureLists.add(features);
                }
                List<String> outcomes = classifier.classify(featureLists);// get BIO classes
                newPii.addAll(createChunks(jCas, tokens, outcomes));// create PiiAnnotations
            }
            FilterForThreads.filter(newPii, jCas, fileName, "CRF");

        } catch (Exception e) {
            LOGGER.throwing(e);
        }
        LOGGER.debug("CRF Thread done");
        doneSignal.countDown();
    }

    private List<PiiAnnotation> createChunks(JCas jCas, List<BaseToken> subChunks, List<String> outcomes) throws AnalysisEngineProcessException {
        int nSubChunks = subChunks.size();
        int nOutcomes = outcomes.size();
        if (nSubChunks != nOutcomes) {
            String message = "expected the same number of sub-chunks (%d) as outcome s(%d)";
            throw new IllegalArgumentException(String.format(message, nSubChunks, nOutcomes));
        } else {
            org.apache.uima.cas.Feature feature;

            List<ChunkOutcome> chunkOutcomes = new ArrayList<>();

            for (String outcome : outcomes) {
                chunkOutcomes.add(new ChunkOutcome(outcome));
            }

            chunkOutcomes.add(new ChunkOutcome("O"));
            List<PiiAnnotation> chunks = new ArrayList<>();

            for (int i = 0; i < outcomes.size(); ++i) {
                ChunkOutcome outcome = chunkOutcomes.get(i);
                if (outcome.prefix != 'O') {
                    int begin = i;
                    int end = i;

                    while (true) {
                        ChunkOutcome curr = chunkOutcomes.get(end);
                        ChunkOutcome next = chunkOutcomes.get(end + 1);
                        if (this.isEndOfChunk(curr.label, next.prefix, next.label)) {
                            i = end;
                            begin = (subChunks.get(begin)).getBegin();
                            end = (subChunks.get(end)).getEnd();

                            PiiAnnotation ann = new PiiAnnotation(jCas, begin, end);
                            ann.setPiiSubtype(outcome.label);
                            if (ann.getPiiSubtype().contains("Zip") && !isValidZipcode(ann)) {//refactor/reorder to prevent new
                                break;
                            }
                            ann.setConfidence(0.0f);
                            ann.setMethod("oldCRF");
                            ann.setId("P" + idNum);
                            idNum++;
                            ann.setPiiType(Util.PII_SUB_TO_PARENT_TYPE.getOrDefault(ann.getPiiSubtype(), "UNKNOWN"));
                            chunks.add(ann);
                            break;
                        }
                        ++end;
                    }
                }
            }
            return chunks;
        }
    }

    private boolean isEndOfChunk(String currLabel, char nextPrefix, String nextLabel) {
        return nextPrefix == 'O' || nextPrefix == 'B' || !nextLabel.equals(currLabel);
    }

    private boolean isValidZipcode(PiiAnnotation zip) {
        if (ZIP_FORMAT.matcher(zip.getCoveredText()).matches() && !BAD_ZIPCODES.contains(zip.getCoveredText().substring(0, 3))) {
            return true;
        } else {
            LOGGER.log(PII_LOG, "{}", () -> "oldCRF Zipcode prediction invalid format or prefix " + zip.getBegin() + " " + zip.getEnd() + " " + zip.getCoveredText());
            return false;
        }
    }

    private static class ChunkOutcome {
        public char prefix;
        public String label;

        ChunkOutcome(String outcome) {
            this.prefix = outcome.charAt(0);
            this.label = outcome.length() < 2 ? "" : outcome.substring(2);
        }
    }

    static class WordShapeFunction implements FeatureFunction {
        public List<Feature> apply(Feature feature) {
            return Collections.singletonList(new Feature("WordShape", WordShape.wordShapeBuilder((String) feature.getValue())));
        }
    }

    // Create features based on which dictionaries token is found in
    static class TypePathExtractorNameDictionary<T extends Annotation> extends TypePathExtractor<T> {
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

            for (int i = 8; i < parts.length; i += 2) {// TODO: this 8 is dependent on DictionaryAnnotation's ordering of parts and its toString's ordering, reflection seems too expensive
                if (parts[i].equals("true")) {
                    features.add(new Feature(parts[i - 1], "true"));// adding false for others lowered numbers on small train/test sets
                }
            }
            return features;
        }
    }

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

    static class BrownClusterFunction implements FeatureFunction {
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


}
