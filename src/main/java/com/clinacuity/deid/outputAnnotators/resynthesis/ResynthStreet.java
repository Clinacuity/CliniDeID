
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

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class ResynthStreet extends Resynthesizer {
    private static final String STREETS_PATH = "streetNames.obj";
    private static final String SUFFIXES_PATH = "streetSuffixes.obj";
    private static final String SUFFIXES_ALL_PATH = "streetSuffixesAll.obj";
    public static final Set<String> SUFFIXES_SET = Set.of(readObjectFileToArray(SUFFIXES_ALL_PATH));
    private static final String[] SUFFIXES = readObjectFileToArray(SUFFIXES_PATH);
    private static final String[] STREETS = readObjectFileToArray(STREETS_PATH);

    public ResynthStreet() {
    }

    //TODO: combine isCityOrUnknownOrStreet with getAndUpdateResynthesizedValue.
    //Difficulty is getAndUpdateResynthesizedValue needs to know WHERE digit or suffix or whatever was found

    @Override
    public String getAndUpdateResynthesizedValue(String oldPii) {
        //determine street vs city, consider if contain both street and city indicated by comma?
        //street if has suffix at end or starts (after punctuation/spacing) with digits, else city (starts with letter)
        String lowerTrimmed = trimToLetterDigit(oldPii);
        String newValue = "";
        int lastSpaceIndex = lowerTrimmed.lastIndexOf(' ');
        boolean hasSuffix = false;
        if (lastSpaceIndex > 0) {
            String suffix = lowerTrimmed.substring(lastSpaceIndex + 1);
            if (SUFFIXES_SET.contains(suffix)) {
                hasSuffix = true;
                lowerTrimmed = lowerTrimmed.substring(0, lastSpaceIndex);
            }
        }
        if (hasSuffix) {
            newValue = processStreet(lowerTrimmed, true);
        } else {
            newValue = processStreet(lowerTrimmed, false);
        }
        int index = 0;
        while (index < oldPii.length() && !(Character.isLetter(oldPii.charAt(index)))) {//find first 'word'
            index++;
        }
        if (index + 1 < oldPii.length() && Character.isUpperCase(oldPii.charAt(index)) && Character.isUpperCase(oldPii.charAt(index + 1))) {
            //two capitals in a row-->uppercase the whole thing
            newValue = newValue.toUpperCase();
        }
        return newValue;
    }

    private String processStreet(String lowerTrimmed, boolean hasSuffix) {
        if (resynthesisMap.contains(lowerTrimmed)) {
            return resynthesisMap.getNewValue(lowerTrimmed);
        }
        int address = ThreadLocalRandom.current().nextInt(100, 1000);
        String newValue = address + " " + STREETS[ThreadLocalRandom.current().nextInt(STREETS.length)];
        if (hasSuffix) {
            newValue += " " + SUFFIXES[ThreadLocalRandom.current().nextInt(SUFFIXES.length)];
        }
        resynthesisMap.setNewValue(lowerTrimmed, newValue);
        return newValue;
    }
}
