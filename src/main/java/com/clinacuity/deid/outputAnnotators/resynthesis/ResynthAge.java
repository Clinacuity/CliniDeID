
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

package com.clinacuity.deid.outputAnnotators.resynthesis;

import java.util.Map;
import java.util.regex.Pattern;

public class ResynthAge extends Resynthesizer {
    public static final Pattern DASH_AND_SPACES = Pattern.compile("[-\\s]+and[-\\s]+|\\s*-\\s*|\\s\\s+");//level cleaner uses this to remove ages <90
    private static final Map<String, Integer> NUMBER_WORDS = Map.ofEntries(Map.entry("one", 1), Map.entry("two", 2),
            Map.entry("three", 3), Map.entry("four", 4), Map.entry("five", 5), Map.entry("six", 6),
            Map.entry("seven", 7), Map.entry("eight", 8), Map.entry("nine", 9), Map.entry("ten", 10),
            Map.entry("eleven", 11), Map.entry("twelve", 12), Map.entry("thirteen", 13),
            Map.entry("fourteen", 14), Map.entry("fifteen", 15), Map.entry("sixteen", 16),
            Map.entry("seventeen", 17), Map.entry("eighteen", 18), Map.entry("nineteen", 19),
            Map.entry("teen", 16), Map.entry("teenager", 16), Map.entry("teenage", 16),
            Map.entry("teens", 16), Map.entry("preteen", 12), Map.entry("preteens", 12),
            Map.entry("pre-teen", 12), Map.entry("pre-teens", 12), Map.entry("pre teen", 12), Map.entry("pre teens", 12), Map.entry("zero", 0),

            Map.entry("ninety", 90), Map.entry("eighty", 80), Map.entry("seventy", 70), Map.entry("sixty", 60),
            Map.entry("fifty", 50), Map.entry("forty", 40), Map.entry("thirty", 30), Map.entry("twenty", 20)
    );
    private static final Map<String, Integer> TENS_WORDS = Map.of("ninety", 90, "eighty", 80, "seventy", 70,
            "sixty", 60, "fifty", 50, "forty", 40, "thirty", 30, "twenty", 20);
    private static final String[] TENS = {"", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
    private static final String[] ZERO_TO_19 = {"", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
            "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
    private static final Map<String, Integer> DECADES = Map.ofEntries(Map.entry("twentys", 20), Map.entry("twenties", 20),
            Map.entry("thirtys", 30), Map.entry("thirties", 30), Map.entry("fourtys", 40), Map.entry("fourties", 40),
            Map.entry("fiftys", 50), Map.entry("fifties", 50), Map.entry("sixtys", 60), Map.entry("sixties", 60),
            Map.entry("seventys", 70), Map.entry("seventies", 70),
            Map.entry("eigthys", 80), Map.entry("eighties", 80), Map.entry("ninetys", 90), Map.entry("nineties", 90));

    public ResynthAge() {
        //Do Nothing
    }

    public static int convertWordsToInt(String cleaned) {//dashes, 's, 'and' should have been removed, spaces trimmed and collapsed
        //handles 1-199,
        if (NUMBER_WORDS.containsKey(cleaned)) {
            return NUMBER_WORDS.get(cleaned);
        } else if (DECADES.containsKey(cleaned)) {
            return DECADES.get(cleaned);
        }
        int age = 0;
        if (cleaned.startsWith("one hundred")) {
            age = 100;
            int offset = 11;
            if (cleaned.length() > 11 && cleaned.charAt(11) == ' ') {//check length in case cleaned is exactly one hundred, skip over it and any space after it
                offset++;
            }
            cleaned = cleaned.substring(offset);
        }
        int spaceIndex = cleaned.indexOf(' ');//not using split for performance reasons and there should be either 1 or 2 words only
        if (spaceIndex > 0) {
            age += TENS_WORDS.getOrDefault(cleaned.substring(0, spaceIndex), 0);
            cleaned = cleaned.substring(spaceIndex + 1);
        }
        age += NUMBER_WORDS.getOrDefault(cleaned, 0);

        return age;
    }

    private static String convertIntToWords(int age) {//TODO what about original dashes or ands?
        String newValue = "";
        if (age >= 100) {
            newValue = "one hundred ";
            age -= 100;
        }
        if (age >= 20) {
            newValue += TENS[age / 10] + " ";
            age %= 10;
        }
        newValue += ZERO_TO_19[age];
        if (newValue.charAt(newValue.length() - 1) == ' ') {
            return newValue.substring(0, newValue.length() - 1);
        }
        return newValue;
    }

    @Override
    public String getAndUpdateResynthesizedValue(String oldPii) {//no level check needed as ages <=89 would be removed by DeidLevelCleaner if not to be included
        String loweredCopy = trimToLetterDigit(oldPii);
        String cleaned = DASH_AND_SPACES.matcher(loweredCopy).replaceAll(" ");//remove dashes, ands, multiple spaces
        if (resynthesisMap.contains(cleaned)) {
            return resynthesisMap.getNewValue(cleaned);
        }
        int age;
        int index = cleaned.indexOf("'s");
        boolean apostropheS = false;
        boolean word = false;
        if (index > 0) {
            cleaned = cleaned.substring(0, index);
            apostropheS = true;
        }
        try {
            age = Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            word = true;
            age = convertWordsToInt(cleaned);
        }
        age += resynthesisMap.getAgeOffset();
        if (age <= 0) {
            age = 1;
        }
        String newValue;
        if (word) {
            newValue = convertIntToWords(age);
        } else {
            newValue = Integer.toString(age);
        }
        if (apostropheS) {
            newValue += "'s";
        }
        resynthesisMap.setNewValue(loweredCopy, newValue);
        return matchCase(oldPii, newValue);  //could be first word in sentence and spelled out
    }
}
