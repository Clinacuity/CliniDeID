
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

package com.clinacuity.deid.outputAnnotators.resynthesis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ResynthState extends Resynthesizer {
    // private static final List<String[]> stateLists = Arrays.asList(stateNames, stateTwoLetter, stateAbbr);
    private static final String STATE_NAMES_FILE = "statesFullName.obj";
    private static final String[] STATE_NAMES = readObjectFileToArray(STATE_NAMES_FILE);
    public static final Map<String, Integer> STATE_NAMES_MAP = Collections.unmodifiableMap(makeMap(STATE_NAMES));
    private static final String STATE_TWO_LETTER_FILE = "statesTwoLetter.obj";
    private static final String[] STATE_TWO_LETTER = readObjectFileToArray(STATE_TWO_LETTER_FILE);
    public static final Map<String, Integer> STATE_TWO_LETTER_MAP = Collections.unmodifiableMap(makeMap(STATE_TWO_LETTER));
    private static final String STATE_ABBR_FILE = "statesAbbr.obj";
    private static final String[] STATE_ABBR = readObjectFileToArray(STATE_ABBR_FILE);
    public static final Map<String, Integer> STATE_ABBR_MAP = Collections.unmodifiableMap(makeMap(STATE_ABBR));

    public ResynthState() {
    }

    private static Map<String, Integer> makeMap(String[] data) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < data.length; i++) {
            map.put(data[i].toLowerCase(), i);
        }
        return map;
    }


    public String getAndMultiUpdateResynthesizedValue(String oldPii, String[] data, int oldIndex) {
        String newValue;
        String finalValue;
        int resynthIndex;
        do {
            resynthIndex = ThreadLocalRandom.current().nextInt(data.length);
            finalValue = data[resynthIndex];
        } while (finalValue.equalsIgnoreCase(oldPii));
        resynthesisMap.setNewValue(oldPii, finalValue);
        //using resynthIndex, get matching from all versions
        if (data != STATE_NAMES) {
            String oldValue = STATE_NAMES[oldIndex].toLowerCase();
            newValue = STATE_NAMES[resynthIndex];
            resynthesisMap.setNewValue(oldValue, newValue);
        }
        if (data != STATE_TWO_LETTER) {
            String oldValue = STATE_TWO_LETTER[oldIndex].toLowerCase();
            newValue = STATE_TWO_LETTER[resynthIndex];
            resynthesisMap.setNewValue(oldValue, newValue);
        }
        if (data != STATE_ABBR) {
            String oldValue = STATE_ABBR[oldIndex].toLowerCase();
            if (!oldValue.equalsIgnoreCase(STATE_TWO_LETTER[oldIndex].toLowerCase()) && !oldValue.equalsIgnoreCase(STATE_NAMES[oldIndex].toLowerCase())) {
                newValue = STATE_ABBR[resynthIndex];
                resynthesisMap.setNewValue(oldValue, newValue);
            }
        }
        return finalValue;
    }

    @Override
    public String getAndUpdateResynthesizedValue(String oldPii) {
        //need to be consistent with all forms, so if FL -> GA then Florida -> Georgia
        String lowered = oldPii.toLowerCase().replaceAll("\\.", "");
        if (resynthesisMap.contains(lowered)) {
            return resynthesisMap.getNewValue(lowered);
        } else if (STATE_TWO_LETTER_MAP.containsKey(lowered)) {
            return getAndMultiUpdateResynthesizedValue(lowered, STATE_TWO_LETTER, STATE_TWO_LETTER_MAP.get(lowered));
        } else if (STATE_ABBR_MAP.containsKey(lowered)) {
            return getAndMultiUpdateResynthesizedValue(lowered, STATE_ABBR, STATE_ABBR_MAP.get(lowered));
        } else if (STATE_ABBR_MAP.containsKey(lowered + ".")) {
            return getAndMultiUpdateResynthesizedValue(lowered, STATE_ABBR, STATE_ABBR_MAP.get(lowered + "."));
        } else if (STATE_NAMES_MAP.containsKey(lowered)) {
            return getAndMultiUpdateResynthesizedValue(lowered, STATE_NAMES, STATE_NAMES_MAP.get(lowered));
        } else {//shouldn't get here, but just in case it was an invalid state to begin with
            return getAndMultiUpdateResynthesizedValue(lowered, STATE_NAMES, 0);
        }
    }
}
