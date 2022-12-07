
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
import com.clinacuity.deid.outputAnnotators.resynthesis.ResynthesisAnnotator;
import org.apache.logging.log4j.LogManager;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.junit.Assert;

public class TestResynthesisAnnotator extends JcasTestBase {
    private static final String TEST_PATH = "src/test/resources/DeidLevelCleaner/";
    private static AnalysisEngine resynthesisAnnotator;

    @Override
    protected void Init() {
        logger = LogManager.getLogger();
        try {
            resynthesisAnnotator = AnalysisEngineFactory.createEngine(ResynthesisAnnotator.class);
        } catch (Exception e) {
            logger.throwing(e);
            Assert.fail();
        }
    }

    //TODO: add tests:
    // with a predefined map exact tests are possible for each type as known shifts and replacements
    // separately test reading/writing of map, creating new map entries (key is found and value is !=key && value in resynth text)
    // Individual resynthesizers have tests already, some of which verify that map is working

}
