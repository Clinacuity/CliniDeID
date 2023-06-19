
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
import com.clinacuity.clinideid.message.DeidLevel;
import com.clinacuity.deid.ae.DeidLevelCleaner;
import com.clinacuity.deid.readers.GeneralCollectionReader;
import com.clinacuity.deid.type.DocumentInformationAnnotation;
import com.clinacuity.deid.type.PiiAnnotation;
import com.clinacuity.deid.util.PiiOptions;
import com.clinacuity.deid.util.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestDeidLevelCleaner extends JcasTestBase {
    private static final String TEST_PATH = "src/test/resources/DeidLevelCleaner/";
    private static Map<DeidLevel, AnalysisEngine> deidLevelCleaners = new HashMap<>();

    @Override
    protected void Init() {
        logger = LogManager.getLogger();
        try {
            deidLevelCleaners.put(DeidLevel.beyond, AnalysisEngineFactory.createEngine(DeidLevelCleaner.class));
            deidLevelCleaners.put(DeidLevel.strict, AnalysisEngineFactory.createEngine(DeidLevelCleaner.class));
            deidLevelCleaners.put(DeidLevel.limited, AnalysisEngineFactory.createEngine(DeidLevelCleaner.class));
        } catch (Exception e) {
            logger.throwing(e);
            Assert.fail();
        }
    }

    @Test
    public void test112_02Beyond() {
        Set<String> annotsAfter = Set.of("PhoneFax 146 151 19792", "PhoneFax 209 217 317-3494", "Date 260 268 02/26/60", "Provider 271 281 Yariel Law", "State 367 369 OH", "Zip 371 376 35594", "Provider 387 390 Law", "Patient 426 430 Hess", "HealthCareUnitName 446 456 Cardiology", "Age 486 488 53", "Date 563 571 02/11/60", "Date 931 932 7", "Patient 1082 1086 Hess", "Patient 1783 1787 Hess", "HealthCareUnitName 2232 2238 clinic", "Patient 2663 2667 Hess", "Provider 2829 2841 Una Trujillo", "City 2963 2968 OLNEY", "PhoneFax 2973 2978 23751", "Date 2984 2992 02/27/60", "Date 2997 3005 02/27/60", "Date 3010 3018 02/26/60");
        testXmi(DeidLevel.beyond, "112-02-beyond", annotsAfter);
    }

    @Test
    public void test112_02Strict() {
        Set<String> annotsAfter = Set.of("PhoneFax 146 151 19792", "PhoneFax 209 217 317-3494", "Date 260 265 02/26", "Zip 371 376 35594", "Patient 426 430 Hess", "HealthCareUnitName 446 456 Cardiology", "Date 563 568 02/11", "Date 931 932 7", "Patient 1082 1086 Hess", "Patient 1783 1787 Hess", "HealthCareUnitName 2232 2238 clinic", "Patient 2663 2667 Hess", "City 2963 2968 OLNEY", "PhoneFax 2973 2978 23751", "Date 2984 2989 02/27", "Date 2997 3002 02/27", "Date 3010 3015 02/26");
        testXmi(DeidLevel.strict, "112-02-strict", annotsAfter);
    }

    @Test
    public void test112_02Limited() {
        Set<String> annotsAfter = Set.of("PhoneFax 146 151 19792", "PhoneFax 209 217 317-3494", "Patient 426 430 Hess", "HealthCareUnitName 446 456 Cardiology", "Patient 1082 1086 Hess", "Patient 1783 1787 Hess", "HealthCareUnitName 2232 2238 clinic", "Patient 2663 2667 Hess", "PhoneFax 2973 2978 23751");
        testXmi(DeidLevel.limited, "112-02-limited", annotsAfter);
    }

    @Test
    public void test389_02Strict() {
        Set<String> annotsAfter = Set.of("HealthCareUnitName 46 66 4am\nTeam E Admission", "HealthCareUnitName 191 204 Turner Clinic", "HealthCareUnitName 1030 1032 ED", "Date 1064 1070 172/79", "Date 2541 2544 5/3", "City 2862 2878 North Wilkesboro", "HealthCareUnitName 3594 3604 ND/NM/NHSM", "HealthCareUnitName 8202 8212 outpatient", "HealthCareUnitName 8762 8764 PT", "PhoneFax 8857 8862 84464");
        testXmi(DeidLevel.strict, "389-02-strict", annotsAfter);
    }

    @Test
    public void test389_02Limited() {
        Set<String> annotsAfter = Set.of("HealthCareUnitName 46 66 4am\nTeam E Admission", "HealthCareUnitName 191 204 Turner Clinic", "HealthCareUnitName 1030 1032 ED", "HealthCareUnitName 3594 3604 ND/NM/NHSM", "HealthCareUnitName 8202 8212 outpatient", "HealthCareUnitName 8762 8764 PT", "PhoneFax 8857 8862 84464");
        testXmi(DeidLevel.limited, "389-02-limited", annotsAfter);
    }

    @Test
    public void test389_02Beyond() {
        Set<String> annotsAfter = Set.of("HealthCareUnitName 46 66 4am\nTeam E Admission", "Provider 91 96 Tyler", "HealthCareUnitName 191 204 Turner Clinic", "Provider 227 232 Arias", "Provider 258 272 Jaquan Mahoney", "Age 337 339 87", "Age 752 754 50", "HealthCareUnitName 1030 1032 ED", "Date 1064 1070 172/79", "Provider 2301 2312 Steven Land", "Date 2541 2547 5/3/32", "City 2862 2878 North Wilkesboro", "Age 3080 3082 40", "Age 3094 3096 40", "HealthCareUnitName 3594 3604 ND/NM/NHSM", "Age 7378 7380 87", "HealthCareUnitName 8202 8212 outpatient", "HealthCareUnitName 8762 8764 PT", "Provider 8830 8844 Jaquan Mahoney", "PhoneFax 8857 8862 84464");
        testXmi(DeidLevel.beyond, "389-02-beyond", annotsAfter);
    }

    private void testXmi(DeidLevel level, String fileName, Set<String> annotsAfterImmut) {
        Set<String> annotsAfter = new HashSet<>(annotsAfterImmut);
        //level is strict beyond or limited.
        //each string in set is "type begin end coveredText" of a PiiAnnotation after level cleaning
        //Assert fails if any PiiAnnotation is not matched (either exists but not in annotsAfter or doesn't exist and is in annotsAfter
        //makeJavaCodeDeidLevelCleaner in extraFiles will convert log entries to Java code to create Set
        try {
            Serializer.DeserializeJcasFromFile(jCas, TEST_PATH + fileName + ".part.xml"); // returns null jCas on failure.
            //logAnnotations("Pii, confidence > 1.0 before level Cleaning");
            DocumentInformationAnnotation documentInformation = JCasUtil.selectSingle(jCas, DocumentInformationAnnotation.class);
            documentInformation.setLevel(DeidLevel.custom.toString());
            GeneralCollectionReader.makePiiOptionMapAnnotation(jCas, new PiiOptions(level));
            SimplePipeline.runPipeline(jCas, deidLevelCleaners.get(level));
            //logAnnotations("Pii, confidence > 1.0 after level Cleaning");
            boolean error = false;
            for (PiiAnnotation ann : jCas.getAnnotationIndex(PiiAnnotation.class)) {//remove from map of sets all found annotations
                if (ann.getConfidence() < .99) {
                    continue;
                }
                String tuple = ann.getPiiSubtype() + " " + ann.getBegin() + " " + ann.getEnd() + " " + ann.getCoveredText();
                if (annotsAfter.contains(tuple)) {
                    annotsAfter.remove(tuple);
                } else {
                    error = true;
                    logger.error("{}", () -> String.format("File: %s, level: %s, Error with: %s, %d - %d: %s", fileName, level, ann.getPiiSubtype(), ann.getBegin(), ann.getEnd(), ann.getCoveredText()));
                }
            }
            if (annotsAfter.size() > 0) {//if set is non-empty then an annotation was missed
                error = true;
                for (String entry : annotsAfter) {
                    logger.error("{}", () -> String.format("File: %s, level: %s, Missing annotation type: %s", fileName, level, entry));
                }
            }
            if (error) {//delay error so that all errors can be printed
                Assert.fail();
            }
        } catch (Exception e) {
            logger.throwing(e);
            Assert.fail();
        }
    }

    private void logAnnotations(String name) {
        logger.warn("-----------------DeidLevelCleaner " + name + " --------------------------------");
        for (PiiAnnotation ann : jCas.getAnnotationIndex(PiiAnnotation.class)) {
            if (ann.getConfidence() > .99) {
                logger.warn("Type: {}, {} - {}: {}", ann.getPiiSubtype(), ann.getBegin(), ann.getEnd(), ann.getCoveredText());
            }
        }
    }
}
