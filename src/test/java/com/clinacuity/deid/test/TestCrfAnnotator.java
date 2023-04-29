
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
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class TestCrfAnnotator extends JcasTestBase {
    private static final String TEST_PATH = "src/test/resources/EnsembleAnnotators/";
    private static AnalysisEngine crfAnnotator;

    static {
        try {
            crfAnnotator = AnalysisEngineFactory.createEngine(EnsemblePartialAnnotator.class,
                    EnsembleAnnotator.EXCLUDES_LIST, "mira, opennlp, regex, rnn, svm",
                    EnsembleAnnotator.BROWN_CLUSTERS_FILENAME, DeidPipeline.BROWN_FILE, EnsembleAnnotator.BROWN_CUTOFF, DeidPipeline.BROWN_CUTOFF,
                    GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, new File(DeidPipeline.CRF_MODEL_FILE));
        } catch (ResourceInitializationException e) {
            e.printStackTrace();
            Assert.fail("Failed to create CRF annotator " + e.toString());
        }
    }

    @Override
    protected void Init() {
        logger = LogManager.getLogger();
    }

    //These test what CRF does, not that it does it correctly
    @Test
    public void testCrf107706597() {
        assertTrue(testPiiXmi(TEST_PATH + "107706597", crfAnnotator,
                Arrays.asList(0, 7, 8, 12, 60, 64, 158, 165, 166, 170, 294, 301, 302, 306, 373, 378),
                Arrays.asList("Date", "ClockTime", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "PhoneFax"), false));
    }

    @Test
    public void testCrf116411981() {
        assertTrue(testPiiXmi(TEST_PATH + "116411981", crfAnnotator,
                Arrays.asList(73, 79, 80, 86, 87, 94, 96, 100, 104, 116, 156, 168, 226, 238, 304, 316, 1578, 1582),
                Arrays.asList("DayOfWeek", "DayOfWeek", "ClockTime", "ClockTime", "PhoneFax", "PhoneFax", "PhoneFax", "PhoneFax", "HealthCareUnitName"), false));
    }
}
