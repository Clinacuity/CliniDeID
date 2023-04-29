
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ResynthesisMap {
    private static final int[] AGE_OPTIONS = {-3, -2, -1, 1, 2, 3};
    private static final Comparator<Map.Entry<String, String>> KEY_COMPARATOR = (e1, e2) -> {
        String v1 = e1.getKey();
        String v2 = e2.getKey();
        return v1.compareTo(v2);
    };
    private String id; //not a number in case it exceeds bounds or is alpha-numeric
    private int ageOffset;
    private int daysOffset;
    private int yearOffset;
    private int minutesOffset;
    private Map<String, String> mapping;  //all types, key should always be lowercase, new value will be in 'usual' case: proper parts capitalized

    public ResynthesisMap() {
        /*Offsets:
        Age     +- 1-3
        days    - 50-364   not 365 so month/day will always change
        years   - 1-20
        minutes +- 15-120, note that am/pm can be wrong if it isn't part of the same annotation ('in the AM' wouldn't be changed)*/
        ageOffset = AGE_OPTIONS[ThreadLocalRandom.current().nextInt(AGE_OPTIONS.length)];
        yearOffset = ThreadLocalRandom.current().nextInt(-20, -2);
        do {
            daysOffset = ThreadLocalRandom.current().nextInt(-364, -50);
        } while (daysOffset % 7 == 0);

        minutesOffset = ThreadLocalRandom.current().nextInt(15, 120 + 1);
        if (ThreadLocalRandom.current().nextBoolean()) {
            minutesOffset *= -1;
        }
        mapping = new HashMap<>();
    }

    public boolean contains(String oldValue) {
        return mapping.containsKey(oldValue);
    }

    public String getNewValue(String oldValue) {
        return mapping.getOrDefault(oldValue, null);
    }

    public void setNewValue(String oldValue, String newValue) {
        mapping.put(oldValue, newValue);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getAgeOffset() {
        return ageOffset;
    }

    public void setAgeOffset(int ageOffset) {
        this.ageOffset = ageOffset;
    }

    public int getDaysOffset() {
        return daysOffset;
    }

    public void setDaysOffset(int daysOffset) {
        this.daysOffset = daysOffset;
    }

    public int getMinutesOffset() {
        return minutesOffset;
    }

    public void setMinutesOffset(int minutesOffset) {
        this.minutesOffset = minutesOffset;
    }

    public int getYearOffset() {
        return yearOffset;
    }

    public void setYearOffset(int yearOffset) {
        this.yearOffset = yearOffset;
    }

    public String getOutput() {
        StringBuilder mapOutput = new StringBuilder(30 * mapping.size());
        mapOutput.append("Age     offset: ").append(ageOffset).append("\n");
        mapOutput.append("Minutes offset: ").append(minutesOffset).append("\n");
        mapOutput.append("Days    offset: ").append(daysOffset).append("\n");
        mapOutput.append("Years   offset: ").append(yearOffset).append("\n");
        List<Map.Entry<String, String>> keys = new ArrayList<>(mapping.entrySet());
        keys.sort(KEY_COMPARATOR);
        for (Map.Entry<String, String> pair : keys) {
            mapOutput.append(String.format("%30s --> %s%n", pair.getKey(), pair.getValue()));
        }
        return mapOutput.toString();
    }
}
