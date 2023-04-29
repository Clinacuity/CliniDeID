
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

package com.clinacuity.deid.outputAnnotators.resynthesis.randomChar;

import com.clinacuity.deid.outputAnnotators.resynthesis.Resynthesizer;

import java.util.concurrent.ThreadLocalRandom;

public abstract class ResynthesizerRandomCharacters extends Resynthesizer {

    public static String getNewValue(String oldPii) {
        char[] result = new char[oldPii.length()];
        for (int index = 0; index < oldPii.length(); index++) {
            if (Character.isDigit(oldPii.charAt(index))) {
                result[index] = (char) ThreadLocalRandom.current().nextInt('0', '9' + 1);
            } else if (Character.isUpperCase(oldPii.charAt(index))) {
                result[index] = (char) ThreadLocalRandom.current().nextInt('A', 'Z' + 1);
            } else if (Character.isLowerCase(oldPii.charAt(index))) {
                result[index] = (char) ThreadLocalRandom.current().nextInt('a', 'a' + 1);
            } else {
                result[index] = oldPii.charAt(index);
            }
        }
        return new String(result);
    }

    @Override
    public String getAndUpdateResynthesizedValue(String oldPii) {
        if (resynthesisMap.contains(oldPii.toLowerCase())) {
            return matchCase(oldPii.toLowerCase(), resynthesisMap.getNewValue(oldPii.toLowerCase()));
        }
        resynthesisMap.setNewValue(oldPii.toLowerCase(), getNewValue(oldPii));
        return resynthesisMap.getNewValue(oldPii.toLowerCase());
    }

}
