
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.clinacuity.deid.mains.DeidPipeline.PII_LOG;

public class ResynthDayOfWeek extends Resynthesizer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern MULTIPLE_DAY = Pattern.compile("[smtwrfau]+");

    private static final Map<String, Integer> DAY_TO_INT;
    private static final String[][] ALL_VALUES = {{"m", "t", "w", "r", "f", "a", "u"},
            {"mon", "tue", "wed", "thu", "fri", "sat", "sun"},
            {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"}};
    private static final Map<String, Integer> TYPES;

    static {
        Map<String, Integer> intMap = new HashMap<>();
        for (int type = 0; type < 3; type++) {
            for (int i = 0; i < 7; i++) {
                intMap.put(ALL_VALUES[type][i], i);
            }
        }
        intMap.put("th", intMap.get("thu"));
        intMap.put("thur", intMap.get("thu"));
        intMap.put("thurs", intMap.get("thu"));
        intMap.put("su", intMap.get("sun"));
        intMap.put("s", intMap.get("sat"));
        intMap.put("tues", intMap.get("tue"));
        DAY_TO_INT = intMap;

        Map<String, Integer> typeMap = new HashMap<>();
        for (int type = 0; type < 3; type++) {
            for (int i = 0; i < 7; i++) {
                typeMap.put(ALL_VALUES[type][i], type);
            }
        }
        typeMap.put("th", 1);
        typeMap.put("thur", 1);
        typeMap.put("thurs", 1);
        typeMap.put("su", 1);
        typeMap.put("s", 0);
        typeMap.put("tues", 1);
        TYPES = typeMap;
    }

    public ResynthDayOfWeek() {
    }

    @Override
    public String getAndUpdateResynthesizedValue(String oldPii) {//TODO: not consistent in that if M -> T, Monday may not go to Tuesday
        //find which type of day of week format this is by searching each set
        //when found pick random, but different value, from corresponding array
        String lowerCase = trimToLetter(oldPii);
        String suffix = "";
        if (lowerCase.endsWith("s")) {
            lowerCase = lowerCase.substring(0, lowerCase.length() - 1);
            suffix = "s";
        }
        if (resynthesisMap.contains(lowerCase)) {
            return matchCase(oldPii, resynthesisMap.getNewValue(lowerCase) + suffix);
        }
        String newValue = null;
        int type = TYPES.getOrDefault(lowerCase, -1);
        if (type == -1) {
            if (MULTIPLE_DAY.matcher(lowerCase).matches()) {
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < lowerCase.length(); i++) {
                    String answer = getAndUpdateResynthesizedValue(lowerCase.substring(i, i + 1));
                    if (answer != null) {
                        result.append(answer);
                    }
                }
                if (result.length() > 0) {
                    newValue = result.toString();
                } else {
                    LOGGER.log(PII_LOG, "{}", () -> "DayOfWeek problem with " + oldPii);
                    newValue = "monday";
                }
            } else {//unknown, just pick something
                LOGGER.log(PII_LOG, "{}", () -> "DayOfWeek problem with " + oldPii);
                resynthesisMap.setNewValue(lowerCase, "monday");
                return matchCase(oldPii, "monday");
            }
        } else {
            int index = DAY_TO_INT.get(lowerCase);
            index = Math.abs(index + resynthesisMap.getDaysOffset()) % 7;
            newValue = ALL_VALUES[type][index];
        }
        resynthesisMap.setNewValue(lowerCase, newValue);
        return matchCase(oldPii, newValue);
    }
}