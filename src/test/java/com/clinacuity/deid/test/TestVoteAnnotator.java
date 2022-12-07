
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

package com.clinacuity.deid.test;

import com.clinacuity.base.JcasTestBase;
import com.clinacuity.deid.ae.VoteAnnotator;
import com.clinacuity.deid.type.PiiAnnotation;
import org.apache.logging.log4j.LogManager;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestVoteAnnotator extends JcasTestBase {
    private static final String TEST_PATH = "src/test/resources/VoteAnnotator/";
    private static AnalysisEngine voteAnnotator = null;
    private int idCount = 1;//so that all created PiiAnnotations have unique id

    @Override
    protected void Init() {
        logger = LogManager.getLogger();
        if (voteAnnotator == null) {
            try {
                voteAnnotator = AnalysisEngineFactory.createEngine(VoteAnnotator.class, VoteAnnotator.THRESH_HOLD, 1);
            } catch (Exception e) {
                logger.throwing(e);
                Assert.fail("Exception in VoteAnnotator Init creating engine");
            }
        }
    }

    //These tests are artificial
    @Test
    public void testRankings() {
        //Regex should win
        makePii(2, 11, "Street", "RNN");
        makePii(2, 11, "City", "CRF");
        makePii(2, 11, "Name", "RegexHigh");
        makePii(2, 11, "Country", "MIRA");
        makePii(2, 11, "Organization", "SVM");

        makePii(17, 26, "Name", "SVM");
        makePii(17, 26, "City", "RegexHigh");

        //RNN should win
        makePii(28, 36, "State", "RNN");
        makePii(28, 36, "City", "CRF");
        makePii(28, 36, "Country", "MIRA");
        makePii(28, 36, "Organization", "SVM");

        //CRF should win
        makePii(38, 41, "City", "CRF");
        makePii(38, 41, "Country", "MIRA");
        makePii(38, 41, "Organization", "SVM");

        //MIRA should win
        makePii(43, 47, "Country", "MIRA");
        makePii(43, 47, "Organization", "SVM");

        //method should combine
        makePii(49, 56, "City", "CRF");
        makePii(49, 56, "City", "MIRA");
        makePii(49, 56, "City", "SVM");

        //low regex should lose
        makePii(60, 65, "Hcu", "RegexLow");
        makePii(60, 65, "City", "MIRA");

        String text = "A RegexName is a Baltimore, Maryland. CRF  MIRAe combine to give space to have more.";
        String[] answers = {"2 11 Name Regex", "17 26 City Regex", "28 36 State RNN", "38 41 City CRF",
                "43 47 Country MIRA", "49 56 City CRF,MIRA,SVM", "60 65 City MIRA"};
        assertTrue(testPii(text, answers));
    }

    @Test
    public void testCollation() {
        makePii(2, 11, "OtherOrg", "SVM");
        makePii(2, 15, "OtherOrg", "RNN");//winner

        makePii(18, 21, "OtherOrg", "MIRA"); //winner
        makePii(18, 23, "OtherOrg", "SVM");

        makePii(27, 31, "OtherOrg", "CRF"); //winner
        makePii(27, 34, "OtherOrg", "SVM");

        String[] answers = {"2 15 OtherOrg RNN", "18 21 OtherOrg MIRA", "27 31 OtherOrg CRF"};
        assertTrue(testPii("This is a junk sentence to take up some space here. Now some more things to make it longer.", answers));
    }

    @Test
    public void testCollationIssue() {//fails, not sure why
        makePii(27, 39, "OtherOrg", "CRF");//fails with 27-30 or 27-39
        makePii(27, 34, "OtherOrg", "SVM");

        String[] answers = {"27 39 OtherOrg CRF"};
        assertTrue(testPii("This is a junk sentence to take up some space here. Now some more things to make it longer.", answers));
    }

    private boolean testPii(String text, String[] correctPii) {
        int index = 0;
        boolean fail = false;
        jCas.setDocumentText(text);
        try {
            SimplePipeline.runPipeline(jCas, voteAnnotator);

            for (PiiAnnotation ph : jCas.getAnnotationIndex(PiiAnnotation.class)) {
                if (ph.getConfidence() < .95) {
//                    logger.debug("NONFINAL: {} - {} Type: {}  Sub: {}  Conf: {}  ID: {}  Method: {}  ({})", ph.getBegin(), ph.getEnd(), ph.getPiiType(), ph.getPiiSubtype(),
//                            ph.getConfidence(), ph.getId(), ph.getMethod(), ph.getCoveredText());
                    continue;
                }
//                else {
//                    logger.debug("   FINAL: {} - {} Type: {}  Sub: {}  Conf: {}  ID: {}  Method: {}  ({})", ph.getBegin(), ph.getEnd(), ph.getPiiType(), ph.getPiiSubtype(),
//                            ph.getConfidence(), ph.getId(), ph.getMethod(), ph.getCoveredText());
//                }
                String found = ph.getBegin() + " " + ph.getEnd() + " " + ph.getPiiSubtype() + " " + ph.getMethod();
                if (!found.equals(correctPii[index])) {
                    fail = true;
                    logger.error("Bad Pii: {}\ncorrect:  {}", found, correctPii[index]);
                }
                index++;
            }
            if (fail || index < correctPii.length) {
                throw new ArrayIndexOutOfBoundsException();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Failure. Got {}, expected {}, fail: {}", index, correctPii.length, fail);
            logPiiAnnotations();
            return false;
        } catch (AnalysisEngineProcessException e) {
            logger.throwing(e);
            return false;
        }
        return true;
    }

    private void logPiiAnnotations() {
        for (PiiAnnotation ph : jCas.getAnnotationIndex(PiiAnnotation.class)) {
            if (ph.getConfidence() > .95) {
                logger.error("{} - {} Type: {}  Sub: {}  Conf: {}  ID: {}  Method: {}  ({})", ph.getBegin(), ph.getEnd(), ph.getPiiType(), ph.getPiiSubtype(),
                        ph.getConfidence(), ph.getId(), ph.getMethod(), ph.getCoveredText());
            }
        }
    }

    private void makePii(int b, int e, String sub, String method) {
        PiiAnnotation ph = new PiiAnnotation(jCas, b, e);
        ph.setPiiType("LOCATION");
        ph.setPiiSubtype(sub);
        if ("RegexHigh".equals(method)) {
            ph.setConfidence(.8f);
            method = "Regex";
        } else if ("RegexLow".equals(method)) {
            ph.setConfidence(.3f);
            method = "Regex";
        } else {
            ph.setConfidence(0f);
        }
        ph.setId(Integer.toString(idCount));
        idCount++;
        ph.setMethod(method);
        ph.addToIndexes();
    }
}
