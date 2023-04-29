
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
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class TestRnnAnnotator extends JcasTestBase {
    private static final String TEST_PATH = "src/test/resources/EnsembleAnnotators/";
    private static AnalysisEngine RnnAnnotator = initAnnotator();

    private static AnalysisEngine initAnnotator() {
        AnalysisEngine RnnAnnotator = null;
        try {
            if (!DeidPipeline.setRnnPermsissions()) {
                Assert.fail("Failed to set RNN permissions");
            }
            int portRnn = DeidPipeline.startRnnService();//This will have the model filename in it
            Thread.sleep(1000);
            if (portRnn == -1) {
                //logger.error("Couldn't start RNN service");
                Assert.fail("Couldn't start RNN service ");
            }
            RnnAnnotator = AnalysisEngineFactory.createEngine(EnsemblePartialAnnotator.class,
                    EnsembleAnnotator.RNN_HOST_NAME, "localhost", EnsembleAnnotator.RNN_PORT_NUMBER, portRnn,
                    EnsembleAnnotator.EXCLUDES_LIST, "mira, opennlp, crf, svm, regex");
            EnsembleAnnotator.setRnnPortNumber(portRnn);
        } catch (Exception e) {
            Assert.fail("Exception in TestFeatureAnnotator Init creating engine " + e.toString());
        }
        return RnnAnnotator;
    }

    @AfterClass
    public static void tearDown() {
        try {
            EnsembleAnnotator.stopRnn();
        } catch (AnalysisEngineProcessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void Init() {
        logger = LogManager.getLogger();
    }

    //These test what it does, not what correct answers are
    @Ignore
    @Test
    public void testRnn107706597() {
        assertTrue(testPiiXmi(TEST_PATH + "107706597", RnnAnnotator,
                Arrays.asList(0, 7, 8, 12, 60, 64, 158, 165, 166, 170, 294, 301, 302, 306, 365, 369, 373, 378),
                Arrays.asList("Date", "ClockTime", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Provider", "PhoneFax"), false));
    }

    @Ignore
    @Test
    public void testRnn116411981() {
        assertTrue(testPiiXmi(TEST_PATH + "116411981", RnnAnnotator,
                Arrays.asList(73, 79, 80, 86, 87, 94, 96, 103, 104, 116, 156, 168, 226, 238, 304, 316, 827, 849, 1567, 1582),
                Arrays.asList("DayOfWeek", "DayOfWeek", "ClockTime", "ClockTime", "PhoneFax", "PhoneFax", "PhoneFax", "PhoneFax", "ElectronicAddress", "ElectronicAddress"), false));
    }
}