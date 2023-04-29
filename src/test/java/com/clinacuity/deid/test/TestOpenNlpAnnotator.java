
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

package com.clinacuity.deid.test;

import com.clinacuity.base.JcasTestBase;
import com.clinacuity.deid.ae.OpenNlpAnnotator;
import com.clinacuity.deid.type.BaseToken;
import com.clinacuity.deid.type.Sentence;
import org.apache.logging.log4j.LogManager;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TestOpenNlpAnnotator extends JcasTestBase {
    private static final String TEST_PATH = "src/test/resources/OpenNlpAnnotator/";
    private static AnalysisEngine openNlpAnnotator = null;

    @Override
    protected void Init() {
        logger = LogManager.getLogger();
        if (openNlpAnnotator == null) {
            try {
                openNlpAnnotator = AnalysisEngineFactory.createEngine(OpenNlpAnnotator.class);
            } catch (Exception e) {
                logger.throwing(e);
                Assert.fail("Exception in OpenNlpAnnotatorTest Init creating engine");
            }
        }
    }

    private boolean sentenceTest(String fileName, List<Integer> indices) {
        return testText(TEST_PATH + File.separatorChar + "Sentences" + File.separator, openNlpAnnotator,
                Sentence.class, "Sentence", fileName, indices);
    }

    @Test
    public void testOneSentence() {
        assertTrue(sentenceTest("testOneSentence", Arrays.asList(0, 44)));
    }

    @Test
    public void testTwoSentences() {
        assertTrue(sentenceTest("testTwoSentences", Arrays.asList(0, 44, 45, 97)));
    }

    @Test
    public void testOneSentencePeriodV2() {
        assertTrue(sentenceTest("testOneSentencePeriodV2", Arrays.asList(0, 38)));
    }

    @Test
    public void testTwoSentencesPeriods() {
        assertTrue(sentenceTest("testTwoSentencesPeriods", Arrays.asList(0, 19, 20, 49)));
    }

    @Test
    public void testOneSentenceExclamation() {
        assertTrue(sentenceTest("testOneSentenceExclamation", Arrays.asList(0, 36)));

    }

    @Test
    public void testMister() {
        assertTrue(sentenceTest("testMister", Arrays.asList(0, 42)));
    }

    @Test
    public void testMultiSentence() {
        assertTrue(sentenceTest("testMultiSentence",
                Arrays.asList(0, 128, 130, 219, 220, 412, 414, 590, 592, 699, 702, 845, 847, 967, 968, 1086, 1088, 1152, 1153, 1252)));
    }

    @Test
    public void testListOfSentences() {
        assertTrue(sentenceTest("testListOfSentences", Arrays.asList(0, 27, 28, 61, 62, 86)));
    }

    @Test
    public void testListBetweenSentences() {
        assertTrue(sentenceTest("testListBetweenSentences", Arrays.asList(0, 19, 20, 84)));
    }

    @Test
    public void testListsSentences() {
        assertTrue(sentenceTest("testListsSentences", Arrays.asList(0, 34, 35, 64, 65, 114)));
    }

    @Test
    public void testNewlineInSentence() {
        assertTrue(sentenceTest("testNewlineInSentence", Arrays.asList(1, 79)));
    }

    @Test
    public void testNewlineInSentenceInside() {
        assertTrue(sentenceTest("testNewlineInSentenceInside", Arrays.asList(0, 38)));
    }

    @Test
    public void testConjunctionList() {
        assertTrue(sentenceTest("testConjunctionList", Arrays.asList(0, 40)));
    }

    @Test
    public void testConjunction1Sentence() {
        assertTrue(sentenceTest("testConjunction1Sentence", Arrays.asList(0, 48)));
    }

    @Test
    public void testConjunctionBut() {
        assertTrue(sentenceTest("testConjunctionBut", Arrays.asList(0, 49)));

    }

    @Test
    public void testHourInSentence() {
        assertTrue(sentenceTest("testHourInSentence", Arrays.asList(0, 68)));
    }

    @Test
    public void testTenWords() {
        assertTrue(testText(TEST_PATH + File.separatorChar + "BaseTokens" + File.separator, openNlpAnnotator, BaseToken.class, "BaseToken", "testTenWords", Arrays.asList(0, 3, 4, 7, 8, 13, 14, 18, 19, 23, 24, 27, 28, 33, 34, 39, 40, 44, 45, 48)));
    }

    @Test
    public void testPunctuations() {
        assertTrue(testText(TEST_PATH + File.separatorChar + "BaseTokens" + File.separator, openNlpAnnotator, BaseToken.class, "BaseToken", "testPunctuations", Arrays.asList(0, 1, 1, 2, 2, 5, 5, 6, 6, 7, 7, 10, 10, 12, 12, 13)));
    }

    @Test
    public void testTwoSentencesTokens() {
        assertTrue(testText(TEST_PATH + File.separatorChar + "BaseTokens" + File.separator, openNlpAnnotator, BaseToken.class, "BaseToken", "testTwoSentences", Arrays.asList(0, 3, 4, 9, 10, 15, 16, 19, 20, 25, 26, 30, 31, 34, 35, 39, 40, 43, 43, 44, 45, 48, 49, 53, 54, 57, 58, 61, 62, 68, 69, 73, 74, 76, 77, 80, 81, 86, 87, 92, 93, 96, 96, 97)));
    }

    @Test
    public void testSentenceWithDates() {
        assertTrue(testText(TEST_PATH + File.separatorChar + "BaseTokens" + File.separator, openNlpAnnotator, BaseToken.class, "BaseToken", "testSentenceWithDates", Arrays.asList(0, 2, 2, 3, 4, 16, 17, 23, 24, 28, 29, 32, 33, 41, 42, 49, 50, 53, 54, 59, 60, 62, 63, 64, 64, 66, 67, 68, 69, 71, 71, 73, 73, 74)));
    }

    @Test
    public void testPosTaggerOneSentence() {
        String[] correctPOSs = {"DT", "NN", "VBZ", "."};
        assertTrue(testPosText("testPosTaggerOneSentence", correctPOSs));
    }

    @Test
    public void testTwoSentencesPos() {
        String[] correctPOSs = {"DT", "JJ", "JJ", "NN", "NNS", "IN", "DT", "JJ", "NN", ".", "DT", "JJ", "NN", "VBD", "VBN", "IN", "IN", "DT", "JJ", "JJ", "NN", "."};
        assertTrue(testPosText("testTwoSentences", correctPOSs));
    }

    private boolean testPosText(String fileName, String[] pos) {
        final String typePath = TEST_PATH + File.separatorChar + "Pos" + File.separatorChar;
        int annIndex = 0;
        try {
            String text = new String(Files.readAllBytes(Paths.get(typePath + fileName + ".txt")));
            jCas.setDocumentText(text);
            SimplePipeline.runPipeline(jCas, openNlpAnnotator);
            boolean flag = true;
            for (BaseToken ann : jCas.getAnnotationIndex(BaseToken.class)) {
                if (!pos[annIndex].equals(ann.getPartOfSpeech())) {
                    logger.error("Token number {} ({}) expected POS: {}  got {} ", annIndex,
                            ann.getCoveredText(), pos[annIndex], ann.getPartOfSpeech());
                    flag = false;
                }
                annIndex++;
            }
            if (!(flag && annIndex == pos.length)) {
                throw new ArrayIndexOutOfBoundsException();
            } else {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.debug("POS Test Fails: Correct# {}, got {}\n{}", pos.length, annIndex, Arrays.toString(pos));
            logger.warn("-----------------OpenNlp POS BaseTokens--------------------------------");
            fullBaseTokenLogging();
            //This is for making array parameter for testing
//        StringBuilder sb = new StringBuilder();
//        for (BaseToken ann : jCas.getAnnotationIndex(BaseToken.class)) {
//            sb.append("\"").append(ann.getPartOfSpeech()).append("\", ");
//        }
//        String list = sb.toString();
//        logger.error("String[] correctPOSs = {{}};", list.substring(0, list.length() - 2));
            return false;
        } catch (Exception e) {
            logger.throwing(e);
            return false;
        }
    }

    //Using reflection to test Feature within BaseToken class which uses BIO format
    //indices is list of begin1,end1,begin2,end2 .. spans and types is list of types for value of Feature
    private boolean testFeatureBioSpanAndType(String feature, String fileName, List<Integer> indices, List<String> types) {
        int annIndex = 0;
        boolean insideFeature = false;
        int featureBegin = -1;
        int lastEnd = -1;
        boolean fail = false;
        String featureType = "";
        final String typePath = TEST_PATH + File.separatorChar + feature + "s" + File.separatorChar;
        java.lang.reflect.Method method;
        try {
            method = BaseToken.class.getMethod("get" + feature);
        } catch (Exception e) {
            logger.error("Feature {} is not in BaseToken", feature);
            return false;
        }
        try {
            String text = new String(Files.readAllBytes(Paths.get(typePath + fileName + ".txt")));
            jCas.setDocumentText(text);
            SimplePipeline.runPipeline(jCas, openNlpAnnotator);
            for (BaseToken bt : jCas.getAnnotationIndex(BaseToken.class)) {
                if (((String) method.invoke(bt)).startsWith("B")) {
                    if (insideFeature) {//this is start of new item, need to test last one
                        if (!checkItemSpan(feature, featureBegin, lastEnd, indices, types, annIndex, featureType)) {
                            fail = true;
                        }
                        annIndex += 2;
                    }
                    featureType = ((String) method.invoke(bt)).substring(2);
                    featureBegin = bt.getBegin();
                    lastEnd = bt.getEnd();
                    insideFeature = true;
                } else if (((String) method.invoke(bt)).startsWith("I")) {
                    if (!featureType.equals(((String) method.invoke(bt)).substring(2))) {
                        fail = true;
                        logger.error("For item {}, began with type {} but then I (inside) {}", annIndex, featureType, ((String) method.invoke(bt)).substring(2));
                    }
                    lastEnd = bt.getEnd();
                } else {//"O"
                    if (insideFeature) {//check last one if present
                        insideFeature = false;
                        if (!checkItemSpan(feature, featureBegin, lastEnd, indices, types, annIndex, featureType)) {
                            fail = true;
                        }
                        annIndex += 2;
                    }
                }
            }
            if (insideFeature) {//check if last token was a feature
                if (!checkItemSpan(feature, featureBegin, lastEnd, indices, types, annIndex, featureType)) {
                    fail = true;
                }
                annIndex += 2;
            }
            if (fail || annIndex < indices.size()) {//so that all errors end up in same place
                throw new ArrayIndexOutOfBoundsException();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("{} Test Fails: Correct# {}, got {}\n{}", feature, indices.size() / 2, annIndex / 2, indices.toString(), types.toString());
            fullBaseTokenLogging();
        } catch (Exception e) {
            logger.throwing(e);
            return false;
        }
        return !fail;
    }

    @Test
    public void testBasicChunking() {
        assertTrue(testFeatureBioSpanAndType("Chunk", "testBasicChunking",
                Arrays.asList(0, 4, 6, 19, 21, 26, 27, 29, 30, 40, 41, 43, 44, 58),
                Arrays.asList("NP", "NP", "VP", "PP", "NP", "PP", "NP")));
        // String[] correctChunks = {"Paul",  "a young child", "wrote", "to", "the wizard", "of", "the North Pole"};
    }

    @Test
    public void testFullChunk() {
        // String[] correctChunks = {"A young boy", "Paul", "wrote", "to", "the wizard", "of", "the North Pole"};
        assertTrue(testFeatureBioSpanAndType("Chunk", "testFullChunk",
                Arrays.asList(0, 11, 13, 17, 19, 24, 25, 27, 28, 38, 39, 41, 42, 56),
                Arrays.asList("NP", "NP", "VP", "PP", "NP", "PP", "NP")));
    }

    private void fullBaseTokenLogging() {
        for (BaseToken bt : jCas.getAnnotationIndex(BaseToken.class)) {
            logger.debug("{}", () -> String.format("%5d - %5d POS: %4s  Chunk: %6s Tag: %s  Norm: %s Ner: %12s  (%s)", bt.getBegin(), bt.getEnd(), bt.getPartOfSpeech(),
                    bt.getChunk(), bt.getTag(), bt.getNormalizedForm(), bt.getNer(), bt.getCoveredText()));
        }
    }

    private boolean checkItemSpan(String item, int begin, int end, List<Integer> indices, List<String> types, int index, String type) {
        if (indices.get(index) == begin && indices.get(index + 1) == end && types.get(index / 2).equals(type)) {
            return true;
        } else {
            logger.error("{}", () -> String.format("%s span or type mismatch, expected %5d - %5d type: %12s, got %5d - %5d type: %12s",
                    item, indices.get(index), indices.get(index + 1), types.get(index / 2), begin, end, type));
            return false;
        }
    }

    @Test
    public void testNer1() {//46-56 North Pole location?, 137-146 5/19/2015 date, 169-173 5 am time, 232-239 Walmart organization?
        assertTrue(testFeatureBioSpanAndType("Ner", "testNer1",
                Arrays.asList(13, 17, 67, 74, 84, 90, 100, 103, 121, 133, 177, 186, 197, 205, 210, 216),
                Arrays.asList("person", "location", "money", "percentage", "date", "time", "date", "date")));
    }

    @Test
    public void testNer2() {//22-44 Organization (McDonald's Corporation), 69-105 combined into MUSC organization,
        assertTrue(testFeatureBioSpanAndType("Ner", "testNer2",
                Arrays.asList(0, 10, 24, 30, 33, 45, 69, 87, 91, 105, 119, 129, 131, 145, 147, 151, 159, 162, 182, 186),
                Arrays.asList("person", "person", "organization", "organization", "location", "location", "location", "person", "percentage", "time")));
    }
}
