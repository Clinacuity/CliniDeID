
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

package com.clinacuity.deid.outputAnnotators.resynthesis.fromFile;

import com.clinacuity.deid.outputAnnotators.resynthesis.ResynthesisMap;
import com.clinacuity.deid.outputAnnotators.resynthesis.Resynthesizer;

import java.util.concurrent.ThreadLocalRandom;

public abstract class ResynthesizerFromFile extends Resynthesizer {
    protected String[] data;

    protected ResynthesizerFromFile(String filename) {
        data = readObjectFileToArray(filename);
    }

    public static String getAndUpdateResynthesizedValue(String oldPii, String[] data, ResynthesisMap resynthesisMap) {
        if (resynthesisMap.contains(oldPii.toLowerCase())) {
            return matchCase(oldPii, resynthesisMap.getNewValue(oldPii.toLowerCase()));
        }
        String newValue;
        do {
            newValue = data[ThreadLocalRandom.current().nextInt(data.length)];
        } while (newValue.equals(oldPii));
        resynthesisMap.setNewValue(oldPii.toLowerCase(), newValue);
        return matchCase(oldPii, newValue);
    }

    @Override
    public String getAndUpdateResynthesizedValue(String oldPii) {
        return getAndUpdateResynthesizedValue(oldPii, data, resynthesisMap);
    }
}
