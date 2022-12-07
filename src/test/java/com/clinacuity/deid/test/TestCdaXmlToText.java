
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
import com.clinacuity.clinideid.message.DeidLevel;
import com.clinacuity.deid.readers.FileSystemCollectionReader;
import com.clinacuity.deid.readers.GeneralCollectionReader;
import com.clinacuity.deid.type.DocumentInformationAnnotation;
import com.clinacuity.deid.util.PiiOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class TestCdaXmlToText extends JcasTestBase {
    private static final String TEST_PATH = "src/test/resources/CdaXmlToText";
    private static FileSystemCollectionReader reader;

    static {
        TypeSystemDescription typeSystemDescription = null;
        try {
            typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription();
        } catch (ResourceInitializationException e) {
            logger.throwing(e);
            Assert.fail("Failed to create type system for CdaXmlToText");
        }
        try {
            reader = (FileSystemCollectionReader) CollectionReaderFactory.createReader(FileSystemCollectionReader.class, typeSystemDescription,
                    FileSystemCollectionReader.INPUT_DIRECTORY_PARAMETER, TEST_PATH,
                    FileSystemCollectionReader.FILE_SIZE_LIMIT, 999999,
                    FileSystemCollectionReader.INPUT_CDA, true,
                    FileSystemCollectionReader.FILE_LIMIT, 999999,
                    GeneralCollectionReader.DEID_LEVEL, DeidLevel.defaultLevel,
                    GeneralCollectionReader.OUTPUT_CLEAN, false,
                    GeneralCollectionReader.OUTPUT_GENERAL_TAG, false,
                    GeneralCollectionReader.OUTPUT_CATEGORY_TAG, false,
                    GeneralCollectionReader.OUTPUT_PII, false,
                    GeneralCollectionReader.OUTPUT_RESYNTHESIS, false,
                    GeneralCollectionReader.OUTPUT_RAW, false);
            PiiOptions piiOptions = new PiiOptions(DeidLevel.beyond);
            reader.setPiiOptions(piiOptions);

        } catch (ResourceInitializationException e) {
            logger.throwing(e);
            Assert.fail("Failed to create CdaXmlToText");
        }
    }

    @Override
    protected void Init() {
        logger = LogManager.getLogger();
    }

    @Test
    public void testReads() {
        boolean fail = false;
        String message = "";
        String fileName = "";
        try {
            while (reader.hasNext()) {
                jCas.reset();
                reader.getNext(jCas);
                fileName = JCasUtil.selectSingle(jCas, DocumentInformationAnnotation.class).getFileName();
                IntegerArray numberOfLines = JCasUtil.selectSingle(jCas, DocumentInformationAnnotation.class).getNumberOfLines();
                boolean success = checkFile(fileName, numberOfLines.toString().replaceAll("\\s+", " "));
                if (!success) {
                    fail = true;
                    message += "test on " + fileName + " failed\n";
                }
            }
            if (fail) {
                Assert.fail(message);
            } else {
                logger.debug("CdaXmlToText test on {} succeeded", fileName);
            }
        } catch (CollectionException | IOException e) {
            logger.throwing(e);
            Assert.fail("Failed to run CdaXmlToText");
        }
    }

    private boolean checkFile(String fileName, String numberOfLines) {
        numberOfLines = numberOfLines.substring(numberOfLines.indexOf("length"));
        if ("SampleCDADocument".equals(fileName)) {
            String correctNumberOfLines = "length: 286 Array elements: [ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2, 2, 1, 2, 2, 2, 2, 1, 1, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 2, 1, 1, 1, 1, 1, 2, 2, 1, 2, 1, 2, 1, 2, 2, 1, 1, 2, 2, 1, 2, 1, 2, 2, 1, 1, 1, 1, 1, 2, 2, 1, 2, 1, 2, 2, 1, 1, 1, 1, 2, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 1, 1, 1, 1, 1, 1, 2, 2, 1, 2, 1, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1 ]";
            return numberOfLines.equals(correctNumberOfLines) &&
                    testPii(null, Arrays.asList(0, 4, 42, 50, 51, 55, 56, 64, 65, 72, 73, 85, 86, 90, 91, 99, 100, 107, 108, 120, 121, 125, 126, 130, 131, 149, 150, 155, 156, 175, 176, 184, 185, 189, 190, 194, 195, 199, 200, 209, 210, 218, 219, 227, 228, 235, 236, 248, 249, 253, 697, 701, 1112, 1116, 1117, 1121, 1122, 1130, 1131, 1139, 1252, 1256, 1257, 1261, 1840, 1848, 1849, 1853, 1854, 1862, 1863, 1867, 1868, 1876, 1877, 1881, 1882, 1890, 1891, 1895, 1896, 1904, 1905, 1909, 1910, 1918, 1919, 1923, 1924, 1932, 1933, 1937, 1938, 1946, 1947, 1951, 1952, 1960, 1961, 1965, 1966, 1974, 1975, 1979, 1980, 1988, 1989, 1993, 1994, 2002, 2003, 2007, 2008, 2016, 2017, 2021, 2022, 2030, 2031, 2035, 2036, 2044, 2045, 2049, 2050, 2058, 2059, 2063, 2064, 2072, 2073, 2077, 2078, 2086, 2087, 2091, 2364, 2372, 2436, 2444, 2445, 2449, 2633, 2641, 2642, 2646, 2647, 2655, 2656, 2660, 2661, 2669, 2670, 2674, 2942, 2950, 3030, 3038, 3039, 3047),
                            Arrays.asList("OtherIDNumber", "Date", "OtherIDNumber", "Date", "OtherIDNumber", "Provider", "OtherIDNumber", "Date", "OtherIDNumber", "Provider", "OtherIDNumber", "OtherIDNumber", "HealthCareUnitName", "OtherIDNumber", "Patient", "Date", "OtherIDNumber", "OtherIDNumber", "OtherIDNumber", "OtherIDNumber", "Date", "Date", "OtherIDNumber", "Provider", "OtherIDNumber", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "ClockTime", "Date", "Date", "Date"), false);
        } else if ("CCDAexample3BeachSoft".equals(fileName)) {
            String correctNumberOfLines = "length: 396 Array elements: [ 1, 1, 4, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 2, 1, 1, 2, 1, 2, 2, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 2, 2, 1, 1, 2, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 2, 1, 1, 2, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 2, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 2, 1, 1 ]";
            return numberOfLines.equals(correctNumberOfLines) &&
                    testPii(null, Arrays.asList(0, 13, 45, 53, 54, 58, 61, 65, 66, 75, 76, 92, 93, 102, 103, 105, 106, 111, 112, 114, 121, 135, 137, 151, 152, 166, 167, 175, 176, 192, 193, 202, 203, 205, 206, 211, 212, 214, 221, 235, 237, 256, 257, 273, 274, 283, 284, 286, 287, 292, 293, 295, 296, 301, 302, 337, 345, 359, 361, 378, 379, 387, 388, 390, 391, 396, 397, 399, 400, 408, 409, 430, 432, 440, 441, 443, 444, 449, 450, 452, 459, 473, 475, 492, 493, 513, 514, 523, 524, 545, 546, 554, 555, 557, 558, 563, 564, 566, 573, 584, 586, 597, 598, 607, 608, 629, 630, 638, 639, 641, 642, 647, 648, 650, 657, 668, 670, 688, 689, 694, 695, 710, 717, 731, 733, 754, 756, 764, 765, 767, 768, 773, 774, 776, 777, 798, 799, 808, 809, 811, 812, 817, 818, 820, 821, 836, 837, 859, 860, 881, 882, 891, 892, 894, 895, 900, 901, 903, 904, 912, 913, 934, 936, 944, 945, 947, 948, 953, 954, 956, 963, 977, 979, 996, 997, 1017, 1018, 1026, 1027, 1036, 1037, 1053, 1054, 1063, 1064, 1066, 1067, 1072, 1073, 1075, 1082, 1096, 1098, 1112, 1113, 1121, 1122, 1130, 1131, 1142, 1143, 1152, 1153, 1155, 1156, 1161, 1162, 1164, 1171, 1183, 1185, 1197, 1198, 1209, 1210, 1219, 1220, 1222, 1223, 1228, 1229, 1231, 1238, 1250, 1252, 1264, 1265, 1273, 1274, 1282, 1283, 1291, 1300, 1304, 1305, 1315, 1316, 1337, 1339, 1347, 1348, 1350, 1351, 1356, 1357, 1359, 1366, 1377, 1379, 1401, 1402, 1409, 1410, 1418, 2099, 2107, 2108, 2118, 2119, 2127, 2128, 2138, 2139, 2147, 2148, 2158, 2159, 2167, 2168, 2178, 2179, 2187, 2188, 2196, 2197, 2205, 2206, 2214, 2215, 2225, 2226, 2234, 2235, 2243, 2244, 2252, 2456, 2464, 2465, 2473, 2474, 2488, 2489, 2497, 2498, 2506, 2507, 2517, 2942, 2952, 2953, 2961, 2962, 2970, 2971, 2981, 2982, 2990, 2991, 2999, 3000, 3010, 3011, 3019, 3020, 3028, 3029, 3037, 3038, 3046, 3047, 3061, 3068, 3082, 3084, 3101, 3102, 3112, 3113, 3121, 3122, 3130, 3131, 3139, 3140, 3154, 3161, 3175, 3177, 3194, 3341, 3349, 3350, 3358),
                            Arrays.asList("OtherIDNumber", "Date", "ClockTime", "ClockTime", "OtherIDNumber", "Street", "City", "State", "Zip", "Country", "PhoneFax", "Patient", "Patient", "Date", "Street", "City", "State", "Zip", "Country", "PhoneFax", "Patient", "Street", "City", "State", "Zip", "Country", "OtherIDNumber", "HealthCareUnitName", "PhoneFax", "Street", "City", "State", "Zip", "Country", "Date", "Street", "City", "State", "Zip", "Country", "PhoneFax", "Provider", "HealthCareUnitName", "OtherIDNumber", "Street", "City", "State", "Zip", "Country", "PhoneFax", "Provider", "OtherIDNumber", "Street", "City", "State", "Zip", "Country", "PhoneFax", "Provider", "OtherIDNumber", "HealthCareUnitName", "PhoneFax", "Street", "City", "State", "Zip", "Country", "Street", "City", "State", "Zip", "Country", "Provider", "HealthCareUnitName", "Street", "City", "State", "Zip", "Country", "Date", "Street", "City", "State", "Zip", "Country", "PhoneFax", "Provider", "HealthCareUnitName", "Date", "OtherIDNumber", "Street", "City", "State", "Zip", "Country", "PhoneFax", "Provider", "Date", "Date", "Street", "City", "State", "Zip", "Country", "PhoneFax", "Patient", "Street", "City", "State", "Zip", "Country", "PhoneFax", "Patient", "Date", "Date", "Date", "ClockTime", "OtherIDNumber", "Street", "City", "State", "Zip", "Country", "PhoneFax", "Provider", "OtherIDNumber", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Patient", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Date", "Patient", "PhoneFax", "Patient", "Date", "Date", "Date", "Date", "Patient", "PhoneFax", "Patient", "Date", "Date"), false);
        } else if ("CDAexample1".equals(fileName)) {//don't know why the spacing is different on the array for this test, happened with uimaFit 3.0?
            String correctNumberOfLines = "length: 39 Array elements: [1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 2, 2, 2, 1, 4, 1, 2, 2, 1, 3, 1, 1, 2, 2, 2, 5, 1, 6, 2, 4, 2, 3, 2, 2, 5, 2, 4, 3, 16]";
            return numberOfLines.equals(correctNumberOfLines) &&
                    testPii(null, Arrays.asList(0, 9, 10, 18, 19, 26, 27, 39, 40, 49, 50, 59),
                            Arrays.asList("OtherIDNumber", "Date", "OtherIDNumber", "Provider", "OtherIDNumber", "Patient"), false);
        } else {
            return false;
        }
    }
}
