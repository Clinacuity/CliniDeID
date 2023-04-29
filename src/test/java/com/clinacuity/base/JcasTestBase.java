
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

package com.clinacuity.base;

import com.clinacuity.deid.type.PiiAnnotation;
import com.clinacuity.deid.util.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


import static org.apache.uima.fit.factory.JCasFactory.createJCas;

public abstract class JcasTestBase {
    protected static Logger logger;
    private static ThreadLocal<JCas> JCAS = new ThreadLocal<JCas>() {
        @Override
        protected JCas initialValue() {
            try {
                return createJCas();
            } catch (UIMAException e) {
                LogManager.getLogger().throwing(e);
                Assert.fail();
            }
            return null;
        }
    };
    protected JCas jCas;

    /**
     * Override this method to initialize the test class itself. This method is called from
     * the @Before method in the parent class
     * {@link com.clinacuity.base.JcasTestBase}
     */
    protected abstract void Init();

    @Before
    public void setUp() {
        jCas = JCAS.get();
        jCas.reset();
        Init();
    }

    @After
    public void takeDown() { }

    //Generic logger, just logs begin, end, and covered text
    protected <T extends Annotation> void logAnnotations(Class<T> Thing, String message) {
        logger.warn("-----------------{} Annotations--------------------------------", message);
        for (T ann : jCas.getAnnotationIndex(Thing)) {
            logger.warn("{} - {}: {}", ann.getBegin(), ann.getEnd(), ann.getCoveredText());
        }
    }

    //iterates through Thing annotations creating string of all the begin,end, values
    //Resulting List is used as correct answers by other methods
    protected <T extends Annotation> void makeList(Class<T> Thing, String message) {
        logger.warn("\t\t*****************   Array list for {} annotations", message);
        StringBuilder sb = new StringBuilder("Arrays.asList(");
        for (T ann : jCas.getAnnotationIndex(Thing)) {
            sb.append(ann.getBegin()).append(", ").append(ann.getEnd()).append(", ");
        }
        String list = sb.toString();
        logger.error("{}{}", list.substring(0, list.length() - 2), ");");
    }

    //runs analysis on filename either text or xmi, indices is list of begin1,end1, begin2,end2 spans
    //file should be in testPath/types  (note added s) folder
    //calls testJCas which iterates through Thing annotations comparing with spans in indices
    protected <T extends Annotation> boolean testText(String testPath, AnalysisEngine analysisEngine, Class<T> Thing, String type, String fileName, List<Integer> indices) {//defaults to no logging
        return testText(testPath, analysisEngine, Thing, type, fileName, indices, false);
    }

    protected <T extends Annotation> boolean testText(String testPath, AnalysisEngine analysisEngine, Class<T> Thing, String type, String fileName, List<Integer> indices, boolean log) {
        String text = null;
        try {
            text = new String(Files.readAllBytes(Paths.get(testPath + fileName + ".txt")));
        } catch (IOException e) {
            logger.throwing(e);
            return false;
        }
        jCas.setDocumentText(text);
        return textJCas(analysisEngine, Thing, type, indices, log);
    }

    protected <T extends Annotation> boolean testXmi(String testPath, AnalysisEngine analysisEngine, Class<T> Thing, String type, String fileName, List<Integer> indices) {
        return testXmi(testPath, analysisEngine, Thing, type, fileName, indices, false);
    }

    protected <T extends Annotation> boolean testXmi(String testPath, AnalysisEngine analysisEngine, Class<T> Thing, String type, String fileName, List<Integer> indices, boolean log) {
        try {
            Serializer.DeserializeJcasFromFile(jCas, testPath + fileName + ".xmi");
        } catch (Exception e) {
            logger.throwing(e);
            return false;
        }
        return textJCas(analysisEngine, Thing, type, indices, log);
    }

    protected <T extends Annotation> boolean textJCas(AnalysisEngine analysisEngine, Class<T> Thing, String type, List<Integer> indices, boolean log) {
        int annIndex = 0;
        try {
            SimplePipeline.runPipeline(jCas, analysisEngine);
            if (log) {
                logAnnotations(Thing, type);
            }
            boolean pass = true;
            for (T ann : jCas.getAnnotationIndex(Thing)) {// check that each Annotation's begin & end match
                if (ann.getBegin() != indices.get(annIndex * 2) || ann.getEnd() != indices.get(annIndex * 2 + 1)) {
                    logger.error("{} Ann {} for |{}| begin/end expected: {} - {} got {} - {}", type, annIndex,
                            ann.getCoveredText(), indices.get(annIndex * 2), indices.get(annIndex * 2 + 1), ann.getBegin(), ann.getEnd());
                    pass = false;
                }
                annIndex++;
            }
            if (!(pass && annIndex * 2 == indices.size())) {
                throw new ArrayIndexOutOfBoundsException();
            } else {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.debug("{} Test Fails: Correct# {}\n{}", type, indices.size() / 2, indices.toString());
            logAnnotations(Thing, type);
            return false;
        } catch (Exception e) {
            logger.throwing(e);
            return false;
        }

    }

    //Lists generated from Gary's bin/makePiiArray.pl script
    //wrapper for testPii to load either text file or xmi into JCas
    protected boolean testPiiText(String pathFileName, AnalysisEngine analysisEngine, List<Integer> indices, List<String> subTypes, boolean log) {
        String text = null;
        try {
            text = new String(Files.readAllBytes(Paths.get(pathFileName + ".txt")));
        } catch (IOException e) {
            logger.throwing(e);
            return false;
        }
        jCas.setDocumentText(text);
        return testPii(analysisEngine, indices, subTypes, log);
    }

    protected boolean testPiiXmi(String pathFileName, AnalysisEngine analysisEngine, List<Integer> indices, List<String> subTypes, boolean log) {
        try {
            Serializer.DeserializeJcasFromFile(jCas, pathFileName + ".xmi");
        } catch (SAXException | IOException e) {
            logger.throwing(e);
            return false;
        }
        return testPii(analysisEngine, indices, subTypes, log);
    }

    //Used by classifiers to test if spans and PiiSubTypes are correct
    //input xmi file is result of OpenNlp and Feature Annotators running
    protected boolean testPii(AnalysisEngine analysisEngine, List<Integer> indices, List<String> subTypes, boolean log) {
        int annIndex = 0;
        try {
            //Serializer.DeserializeJcasFromFile(jCas, pathFileName + ".xmi");
            if (analysisEngine != null) {
                SimplePipeline.runPipeline(jCas, analysisEngine);
            }
            if (log) {
                for (PiiAnnotation ann : jCas.getAnnotationIndex(PiiAnnotation.class)) {
                    logger.debug("{} - {}: Type: {}, SubType: {}, Confidence: {}, ID: {}, Method: {} ({})",
                            ann.getBegin(), ann.getEnd(), ann.getPiiType(), ann.getPiiSubtype(), ann.getConfidence(), ann.getId(), ann.getMethod(), ann.getCoveredText());
                }
            }
            boolean pass = true;
            for (PiiAnnotation ann : jCas.getAnnotationIndex(PiiAnnotation.class)) {// check that each Annotation's begin & end match
                if (ann.getBegin() != indices.get(annIndex * 2) || ann.getEnd() != indices.get(annIndex * 2 + 1) || !ann.getPiiSubtype().equals(subTypes.get(annIndex))) {
                    logger.error("PiiAnn {} for |{}| begin/end expected: {} - {} type: {} but got {} - {} {}", annIndex,
                            ann.getCoveredText(), indices.get(annIndex * 2), indices.get(annIndex * 2 + 1), subTypes.get(annIndex), ann.getBegin(), ann.getEnd(), ann.getPiiSubtype());
                    pass = false;
                }
                annIndex++;
            }
            if (!(pass && annIndex * 2 == indices.size())) {
                throw new ArrayIndexOutOfBoundsException();
            } else {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Test Fails. {} correct answers:\n{}\n{}", indices.size() / 2, indices.toString(), subTypes.toString());
            logger.error("-----------------PiiAnnotations--------------------------------");
            for (PiiAnnotation ann : jCas.getAnnotationIndex(PiiAnnotation.class)) {
                logger.error("{} - {}: Type: {}, SubType: {}, Confidence: {}, ID: {}, Method: {} ({})",
                        ann.getBegin(), ann.getEnd(), ann.getPiiType(), ann.getPiiSubtype(), ann.getConfidence(), ann.getId(), ann.getMethod(), ann.getCoveredText());
            }
            return false;
        } catch (Exception e) {
            logger.throwing(e);
            return false;
        }
    }

}
