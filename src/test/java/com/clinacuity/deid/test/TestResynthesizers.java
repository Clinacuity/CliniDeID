
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
import com.clinacuity.deid.outputAnnotators.resynthesis.ResynthAge;
import com.clinacuity.deid.outputAnnotators.resynthesis.ResynthClockTime;
import com.clinacuity.deid.outputAnnotators.resynthesis.ResynthDate;
import com.clinacuity.deid.outputAnnotators.resynthesis.ResynthDayOfWeek;
import com.clinacuity.deid.outputAnnotators.resynthesis.ResynthElectronicAddress;
import com.clinacuity.deid.outputAnnotators.resynthesis.ResynthState;
import com.clinacuity.deid.outputAnnotators.resynthesis.ResynthStreet;
import com.clinacuity.deid.outputAnnotators.resynthesis.ResynthesisMap;
import com.clinacuity.deid.outputAnnotators.resynthesis.Resynthesizer;
import com.clinacuity.deid.outputAnnotators.resynthesis.fromFile.ResynthCity;
import com.clinacuity.deid.outputAnnotators.resynthesis.fromFile.ResynthCountry;
import com.clinacuity.deid.outputAnnotators.resynthesis.fromFile.ResynthHealthCareUnitName;
import com.clinacuity.deid.outputAnnotators.resynthesis.fromFile.ResynthOtherGeo;
import com.clinacuity.deid.outputAnnotators.resynthesis.fromFile.ResynthOtherOrg;
import com.clinacuity.deid.outputAnnotators.resynthesis.fromFile.ResynthProfession;
import com.clinacuity.deid.outputAnnotators.resynthesis.names.ResynthNamePatient;
import com.clinacuity.deid.outputAnnotators.resynthesis.randomChar.ResynthZipcode;
import org.apache.logging.log4j.LogManager;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestResynthesizers extends JcasTestBase {
    @Override
    protected void Init() {
        logger = LogManager.getLogger();
    }

    //TODO: add tests:
    // with a predefined map exact tests are possible for each type as known shifts and replacements
    // separately test reading/writing of map, creating new map entries (key is found and value is !=key && value in resynth text)

    @Test
    public void testDate() {
        ResynthesisMap rMap = new ResynthesisMap();
        Resynthesizer ren = new ResynthDate();
        ren.setMap(rMap);
        ren.setLevel(DeidLevel.beyond);
        rMap.setDaysOffset(35);
        rMap.setYearOffset(0);
        List<String> results = new ArrayList<>();
        String[] tests = {
                "January 1, 2020", "Jan 2, 2020", "1/3/2020", "1/4/20", "1/5", "2020", "Christmas",
                "10/20", "3 February", "5 February 2020", "Mar-5", "5-2-2020", "6-3-18", "Jan. 5, 2020",
                "2003-09-25", "2003-Sep-25", "25-Sep-2003", "Sep-25-2003", "2003.09.25", "09.25.2003", "9.25.2003", "9.25.03",
                "May of 2063", "2096/2097", "70s", "12th", "Apr", "April 4th",
                "Sept 15", "7/96", "'76", "9-32", "4/26, 2070", "Oct. 2015", "2060's",
                "Apr"
        };
        String[] correct = {
                "February 5, 2020", "Feb 6, 2020", "2/7/2020", "2/8/20", "2/9", "2020", "January 29",
                "11/24", "10 March", "11 March 2020", "Apr-9", "6-6-2020", "7-8-18", "Feb. 9, 2020",
                "2003-10-30", "2003-Oct-30", "30-Oct-2003", "Oct-30-2003", "2003.10.30", "10.30.2003", "10.30.2003", "10.30.03",
                "June of 2063", "2096/2097", "70s", "12th", "May", "May 9th",
                "Oct 20", "8/96", "'76", "10-32", "5/31, 2070", "Nov. 2015", "2060's",
                "May"
                //TODO 12th + 35 is??? probably should be 16th due to assumption of January, but coming out as 12th, seen as year not day of month
        };
        for (String test : tests) {
            results.add(ren.getAndUpdateResynthesizedValue(test));
        }

        if (!compareResults("Date", tests, results, correct)) {
            Assert.fail("some Date tests failed");
        }
    }

    @Test
    public void testZipcode() {
        ResynthesisMap rMap = new ResynthesisMap();
        Resynthesizer ren = new ResynthZipcode();
        ren.setMap(rMap);
        ren.setLevel(DeidLevel.beyond);
        List<String> results = new ArrayList<>();
        String[] tests = {"12345", "23487-1413", "12345"};
        for (String test : tests) {
            results.add(ren.getAndUpdateResynthesizedValue(test));
        }
        Assert.assertTrue(results.size() == 3 && results.get(0).equals(results.get(2))
                && !results.get(0).equals(tests[0]) && !results.get(0).equals(results.get(1)));

    }

    @Test
    public void testZipcode2() {
        ResynthesisMap rMap = new ResynthesisMap();
        Resynthesizer ren = new ResynthZipcode();
        ren.setMap(rMap);
        List<String> results = new ArrayList<>();
        ren.setLevel(DeidLevel.strict);
        String[] tests2 = {"56789", "06311"};
        for (String test : tests2) {
            results.add(ren.getAndUpdateResynthesizedValue(test));
        }
        Assert.assertTrue(results.size() == 2 && results.get(0).substring(0, 3).equals("567") && !results.get(0).equals(tests2[0])
                && results.get(1).substring(0, 3).equals("000"));
    }

    @Test
    public void testEAddress() {
        ResynthesisMap rMap = new ResynthesisMap();
        Resynthesizer ren = new ResynthElectronicAddress();
        ren.setMap(rMap);
        ren.setLevel(DeidLevel.beyond);
        List<String> results = new ArrayList<>();
        String[] tests = {"support@clinacuity.com", "123.45.81.124/asf/asf/vasv", "www.helloword.com"};
//       String[] tests = {"support@clinacuity.com", "support@clinacuity.com", "123.45.81.124", "123.45.81.124/asf/asf/vasv", "www.helloword.com/sdf", "www.helloword.com"};
        for (String test : tests) {
            results.add(ren.getAndUpdateResynthesizedValue(test));
        }
        Assert.assertTrue(allDifferent(tests, results));
        String[] testIp = {"123.45.81.124", "123.45.81.124/asf/asf/vasv"};
        results.clear();
        for (String test : testIp) {
            results.add(ren.getAndUpdateResynthesizedValue(test));
        }
        Assert.assertTrue(changedAndConsistent(testIp[0], results));

    }

    @Test
    public void testState() {
        ResynthesisMap rMap = new ResynthesisMap();
        Resynthesizer ren = new ResynthState();
        ren.setMap(rMap);
        ren.setLevel(DeidLevel.beyond);
        List<String> results = new ArrayList<>();
        String[] tests = {"Fla", "GA", "South Carolina", "Georgia", "SC"};
        for (String test : tests) {
            results.add(ren.getAndUpdateResynthesizedValue(test));
        }
        if (results.size() != tests.length) {
            Assert.fail("mismatch size: tests " + tests.length + " vs results " + results.size());
        }
        if (results.get(0).equalsIgnoreCase("fla") || results.get(0).length() < 3) {
            Assert.fail("Fla became " + results.get(0));
        }
        if (results.get(1).equalsIgnoreCase("GA") || results.get(1).length() != 2) {
            Assert.fail("GA became " + results.get(1));
        }
        if (results.get(2).equalsIgnoreCase("south carolina") || results.get(2).length() < 4) {
            Assert.fail("South Carolina became " + results.get(0));
        }

        int indexTwoLetter = ResynthState.STATE_TWO_LETTER_MAP.get(results.get(1).toLowerCase());//formerly GA
        int indexName = ResynthState.STATE_NAMES_MAP.get(results.get(3).toLowerCase());//formerly Georgia
        if (indexName != indexTwoLetter) {
            Assert.fail("GA became" + results.get(1) + " but Georgia became " + results.get(3));
        }

        indexTwoLetter = ResynthState.STATE_TWO_LETTER_MAP.get(results.get(4).toLowerCase());//formerly SC
        indexName = ResynthState.STATE_NAMES_MAP.get(results.get(2).toLowerCase());//formerly South Carolina
        if (indexName != indexTwoLetter) {
            Assert.fail("SC became" + results.get(1) + " but South Carolina became " + results.get(3));
        }

    }

    @Test
    public void testStreet() {
        ResynthesisMap rMap = new ResynthesisMap();
        Resynthesizer ren = new ResynthStreet();
        ren.setMap(rMap);
        ren.setLevel(DeidLevel.beyond);
        List<String> results = new ArrayList<>();
        String[] tests = {"123 Anywhere St.", "246 Some Longer Avenue", "123 Something", "19383 CAPITALIZED STREET"};
        for (String test : tests) {
            results.add(ren.getAndUpdateResynthesizedValue(test));
        }
        if (results.size() != tests.length) {
            Assert.fail("mismatch size: tests " + tests.length + " vs results " + results.size());
        }
        if (results.get(0).equalsIgnoreCase(tests[0]) || results.get(0).length() < 8 || !Character.isDigit(results.get(0).charAt(0))) {
            Assert.fail(tests[0] + results.get(0));
        }
        //TODO: verify street suffixes exist when present in original test
    }

    @Test
    public void testCity() {
        ResynthesisMap rMap = new ResynthesisMap();
        Resynthesizer ren = new ResynthCity();
        ren.setMap(rMap);
        ren.setLevel(DeidLevel.beyond);
        List<String> results = new ArrayList<>();
        String[] tests = {"City", "Another Longer City", "CAPITAL CITY"};
        for (String test : tests) {
            results.add(ren.getAndUpdateResynthesizedValue(test));
        }
        if (results.size() != tests.length) {
            Assert.fail("mismatch size: tests " + tests.length + " vs results " + results.size());
        }
        if (!Character.isUpperCase(results.get(2).charAt(0)) || !Character.isUpperCase(results.get(2).charAt(1))) {
            Assert.fail("result(2): " + results.get(2) + " should have been all caps");
        }
    }

    @Test
    public void testTime() {
        ResynthesisMap rMap = new ResynthesisMap();
        Resynthesizer ren = new ResynthClockTime();
        rMap.setMinutesOffset(50);
        ren.setMap(rMap);
        ren.setLevel(DeidLevel.beyond);
        List<String> results = new ArrayList<>();

        String[] tests = {"0412", "12:15 am", "12:15 P.M.", "2310", "12:25 am", "10:15 P.M.", "0101", "12:11 pm", "1:11 pm"};
        String[] correct = {"0502", "1:05 pm", "1:05 A.M.", "2111", "10:26 pm", "8:16 P.M.", "2302", "10:12 am", "11:12 am"};
        for (int i = 0; i < tests.length; i++) {
            if (i == 3) {
                rMap.setMinutesOffset(-119);
            }
            results.add(ren.getAndUpdateResynthesizedValue(tests[i]));
        }

        if (!compareResults("ClockTime", tests, results, correct)) {
            Assert.fail("some ClockTime tests failed");
        }
    }

    @Test
    public void testFiles() {//just makes sure they can open their data files
        try {
            Resynthesizer ren = new ResynthHealthCareUnitName();
            ResynthesisMap rMap = new ResynthesisMap();
            ren.setMap(rMap);
            ren.setLevel(DeidLevel.beyond);
            ren.getAndUpdateResynthesizedValue("blah");
            ren = new ResynthElectronicAddress();
            ren = new ResynthCountry();
            ren = new ResynthOtherGeo();
            ren = new ResynthOtherOrg();
            ren = new ResynthNamePatient();
            ren = new ResynthCity();
            ren = new ResynthProfession();
        } catch (RuntimeException e) {
            Assert.fail("Exception loading resynthesizers: " + e.getMessage());
        }
    }

    @Test
    public void testDayOfWeek() {
        ResynthesisMap rMap = new ResynthesisMap();
        rMap.setDaysOffset(5);
        Resynthesizer ren = new ResynthDayOfWeek();
        ren.setMap(rMap);
        ren.setLevel(DeidLevel.beyond);
        List<String> results = new ArrayList<>();

        String[] tests = {"MONDAY", "Tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "mon", "tue", "WED", "thu", "Fri", "sat", "sun", "m", "t", "w", "r", "f", "a", "u"};
        for (String test : tests) {
            results.add(ren.getAndUpdateResynthesizedValue(test));
        }
        String[] answers = {"SATURDAY", "Sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "sat", "sun", "MON", "tue", "Wed", "thu", "fri", "a", "u", "m", "t", "w", "r", "f"};
        boolean hasError = false;
        for (int i = 0; i < results.size(); i++) {
            if (!results.get(i).equals(answers[i])) {
                hasError = true;
                logger.error("Test #{} ({}) got {} but should have been {}", i, tests[i], results.get(i), answers[i]);
            }
        }
        Assert.assertFalse(hasError);

        String result = ren.getAndUpdateResynthesizedValue("FAT");
        if (!"WRU".equals(result)) {
            Assert.fail("FAT become " + result + " but should have been WRU");
        }
    }

    @Test
    public void testAge() {
        ResynthesisMap rMap = new ResynthesisMap();
        Resynthesizer ren = new ResynthAge();
        rMap.setAgeOffset(2);
        ren.setMap(rMap);
        ren.setLevel(DeidLevel.beyond);
        List<String> results = new ArrayList<>();

        String[] tests = {"3", "31", "20's", "forty's", "teenage", "102", "thirty nine", "forty-five", "seventy -five", "eighty -  five",
                "one hundred two", "one hundred and three", "one-hundred four", "one-hundred and five", "one-hundred-and six",
                "one-hundred-and-seven", "one-hundred-eight", "one-hundred-ten", "one-hundred and twelve",

                "thirty one", "one-hundred two", "one-hundred one", "one", "1", "twenties", "nineties", "pre teen", "preteen", "pre-teen"};

        String[] correct = {"5", "33", "22's", "forty two's", "eighteen", "104", "forty one", "forty seven", "seventy seven",
                "eighty seven", "one hundred four", "one hundred five", "one hundred six", "one hundred seven", "one hundred eight",
                "one hundred nine", "one hundred ten", "one hundred twelve", "one hundred fourteen", "twenty nine", "one hundred four",//one hundred four tests that even though offset is -2, it uses the already mapped value of +2
                "ninety nine", "one", "1", "eighteen", "eighty eight", "ten", "ten", "ten"};

        for (int i = 0; i < tests.length; i++) {
            if (i == 19) {
                rMap.setAgeOffset(-2);
            }
            results.add(ren.getAndUpdateResynthesizedValue(tests[i]));
        }

        if (!compareResults("age", tests, results, correct)) {
            Assert.fail("some age tests failed");
        }
    }

    private boolean allDifferent(String[] tests, List<String> results) {
        if (tests.length != results.size()) {
            logger.error("Mismatch lengths tests {} vs results {}", tests.length, results.size());
            return false;
        }
        for (int index = 0; index < tests.length; index++) {
            if (tests[index].equals(results.get(index)) || results.get(index).length() < 1) {
                logger.error("index {}, tests[index] : {}, results index: {}", index, tests[index], results.get(index));
                return false;
            }
        }
        return true;
    }

    private boolean changedAndConsistent(String original, List<String> results) {
        if (results.size() < 2 || original.equals(results.get(0))) {
            logger.error("original {}, results: {}", original, results.toString());
            return false;
        }
        for (int index = 1; index < results.size(); index++) {
            if (!results.get(0).toLowerCase().equals(results.get(index).toLowerCase())) {
                logger.error("results 0: {}, index {}, results index: {}", results.get(0), index, results.get(index));
                return false;
            }
        }
        return true;
    }

    private boolean compareResults(String type, String[] tests, List<String> results, String[] correct) {
        boolean success = true;
        for (int i = 0; i < results.size(); i++) {
            if (!results.get(i).equals(correct[i])) {
                logger.error("{} test {} result was {} should be {}", type, tests[i], results.get(i), correct[i]);
                success = false;
            }
        }
        if (results.size() != correct.length) {
            logger.error("Mismatch lengths, results: {}, correct: {}", results.size(), correct.length);
            success = false;
        }
        return success;
    }
}
