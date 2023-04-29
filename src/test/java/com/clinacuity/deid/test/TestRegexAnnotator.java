
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
import com.clinacuity.deid.ae.EnsembleAnnotator;
import com.clinacuity.deid.ae.EnsemblePartialAnnotator;
import com.clinacuity.deid.mains.DeidPipeline;
import org.apache.logging.log4j.LogManager;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class TestRegexAnnotator extends JcasTestBase {
    private static final String TEST_PATH = "src/test/resources/RegexAnnotator/";
    private static AnalysisEngine regexAnnotator;

    static {
        try {
            regexAnnotator = AnalysisEngineFactory.createEngine(EnsemblePartialAnnotator.class,
                    EnsembleAnnotator.EXCLUDES_LIST, "mira, opennlp, crf, rnn, svm",
                    EnsembleAnnotator.REGEX_CONCEPTS_FILE, DeidPipeline.REGEX_CONCEPTS_FILE);
        } catch (ResourceInitializationException e) {
            Assert.fail("Failed to create Regex annotator " + e.toString());
        }
    }


    @Override
    protected void Init() {
        logger = LogManager.getLogger();
    }

    @Test
    public void ssnTest() {
        assertTrue(testPiiText(TEST_PATH + "ssnTest", regexAnnotator,
                Arrays.asList(13, 22, 44, 55, 71, 82),
                Arrays.asList("SSN", "SSN", "SSN"), false));
    }

    @Test
    public void ipTest() {
        assertTrue(testPiiText(TEST_PATH + "ipTest", regexAnnotator,
                Arrays.asList(14, 29, 45, 55),
                Arrays.asList("ElectronicAddress", "ElectronicAddress"), false));
    }

    @Test
    public void ElectronicAddressTest() {
        assertTrue(testPiiText(TEST_PATH + "eAddressTest", regexAnnotator,
                Arrays.asList(12, 35, 48, 74, 81, 101, 107, 131),
                Arrays.asList("ElectronicAddress", "ElectronicAddress", "ElectronicAddress", "ElectronicAddress"), false));
    }

    @Test
    public void urlTest() {
        assertTrue(testPiiText(TEST_PATH + "urlTest", regexAnnotator,
                Arrays.asList(5, 25, 26, 62, 63, 76, 77, 129, 130, 287, 288, 459),
                Arrays.asList("ElectronicAddress", "ElectronicAddress", "ElectronicAddress", "ElectronicAddress", "ElectronicAddress", "ElectronicAddress"), true));
    }

    @Test
    @Ignore
    public void collateTest() {
        //these are artificially created to test collator
        //this is for new ensemble method, should remove overlapping regex Pii, done in RegexAnnotator
        //the xmi was artificially made, base is 389-02.txt in test/resources/RegexAnnotator
        // then create*Dummy* methods below were placed in RegexAnnotator, called from process then, aCas was serialized to xmi
        //the dummy annotations don't relate to real things

        //This test methodology won't work with new RegExAnnotatorThread b/c it doesn't collate Pii from CAS
        //but from a List it maintains (b/c threads can't change CAS)
        //Have to make a new text file with the text made to create issues for collation to fix
        assertTrue(testPiiXmi(TEST_PATH + "testRegexCollator", regexAnnotator,
                Arrays.asList(10, 17, 19, 20, 20, 25, 30, 35, 35, 40, 45, 55, 58, 72, 100, 105, 102, 108, 301, 310),
                Arrays.asList("SSN", "SSN", "SSN", "SSN", "SSN", "SSN", "SSN", "SSN", "ElectronicAddress", "SSN"), false));
    }
    /*
    private void createTestDummies() {
        createDummy(10, 15);
        createDummy(10, 15);//exact

        createDummy(12, 17);//expand this or the 10-15 to 10-17 and remove other

        createDummy(20, 25);
        createDummy(19, 20);//just touch, should expand?

        createDummy(30, 35);
        createDummy(35, 40);//just touch, should expand?

        createDummy(50, 55);
        createDummy(45, 52);//should expand

        createDummy(60, 65);
        createDummy(58, 72);//should remove shorter

        createDummy(100, 105);
        createDummy(102, 108, "ElectronicAddress");
    }

    private void createDummy(int begin, int end) {
        createDummy(begin, end, "SSN");
    }

    private void createDummy(int begin, int end, String subtype) {
        PiiAnnotation dummy = new PiiAnnotation(jCas, begin, end);
        dummy.setPiiType("ID");
        dummy.setPiiSubtype(subtype);
        dummy.setMethod("RegexDummy");
        dummy.setConfidence(.5f);
        dummy.addToIndexes();
    }*/
}
