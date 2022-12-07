
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

public class TestMiraAnnotator extends JcasTestBase {
    private static final String testPath = "src/test/resources/EnsembleAnnotators/";
    private static AnalysisEngine miraAnnotator;

    static {
        try {
            miraAnnotator = AnalysisEngineFactory.createEngine(EnsemblePartialAnnotator.class,
                    EnsembleAnnotator.EXCLUDES_LIST, "regex, opennlp, crf, rnn, svm",
                    EnsembleAnnotator.MIRA_MODEL_FILE, DeidPipeline.MIRA_MODEL_FILE);
        } catch (ResourceInitializationException e) {
            e.printStackTrace();
            Assert.fail("Exception in TestFeatureAnnotator Init creating engine " + e.toString());
        }
    }

    @Override
    protected void Init() {
        logger = LogManager.getLogger();
    }

    //These test what it does, not what correct answers are, tests take too long
    @Ignore
    @Test
    public void testMira107706597() {//87-91?
        assertTrue(testPiiXmi(testPath + "107706597", miraAnnotator,
                Arrays.asList(0, 7, 8, 12, 60, 64, 158, 165, 166, 170, 294, 301, 302, 306, 356, 369, 373, 378),
                Arrays.asList("Date", "ClockTime", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Provider", "PhoneFax"), true));
    }

    @Test
    @Ignore
    public void testMira116411981() {
        assertTrue(testPiiXmi(testPath + "116411981", miraAnnotator,
                Arrays.asList(73, 79, 80, 86, 87, 94, 96, 103, 104, 116, 156, 168, 226, 238, 304, 316, 827, 849, 1567, 1575),
                Arrays.asList("DayOfWeek", "DayOfWeek", "ClockTime", "ClockTime", "PhoneFax", "PhoneFax", "PhoneFax", "PhoneFax", "ElectronicAddress", "City"), false));
    }
}
