
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

package com.clinacuity.deid.outputAnnotators.resynthesis.randomChar;

import com.clinacuity.clinideid.message.DeidLevel;
import com.clinacuity.deid.util.PiiOptions;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResynthZipcode extends ResynthesizerRandomCharacters {//possibly from RandomCharacter except when only last 3 change?
    private static final Pattern GET_ZIP_5 = Pattern.compile("(\\d{3})(\\d\\d)");
    private static final Set<String> SMALL_ZIPCODE_REGIONS = Set.of("059", "063", "102", "202", "203", "204", "205", "369", "556", "692", "753", "772", "821", "823", "878", "879", "884", "893");

    public ResynthZipcode() {
    }

    @Override
    public String getAndUpdateResynthesizedValue(String oldPii) {//limited does no zip codes so there won't be any here
        String lowered = oldPii.toLowerCase();
        if (resynthesisMap.contains(lowered)) {
            return resynthesisMap.getNewValue(lowered);
        }
        if (level == DeidLevel.strict ||
                (level == DeidLevel.custom && options.getSpecialValue("Zip") == PiiOptions.ZIP_3DIGIT_ONLY)) {
            //Strict does last 2 digits and changes first 3 digits to 000 if that region has <=20,000 people;
            Matcher mat = GET_ZIP_5.matcher(lowered);
            if (!mat.find()) {
                return super.getAndUpdateResynthesizedValue(lowered);//better safe then sorry and just change all digits
            }
            String zip3 = mat.group(1);  //incase more than 5 digits, get first 3 for small zone check
            //change annotation type to zip3 if it needs to become 000xx
            int oldLast2Digits = Integer.parseInt(mat.group(2));
            int newLast2Digits;
            do {
                newLast2Digits = ThreadLocalRandom.current().nextInt(1, 100);
            } while (newLast2Digits == oldLast2Digits);
            if (SMALL_ZIPCODE_REGIONS.contains(zip3)) {//change to 000
                resynthesisMap.setNewValue(lowered, "000" + newLast2Digits);
            } else {
                resynthesisMap.setNewValue(lowered, zip3 + newLast2Digits);
            }
            return resynthesisMap.getNewValue(lowered);
        } else {//level beyond changes all digits, limited does none so annotation would have been removed by cleaner
            return super.getAndUpdateResynthesizedValue(oldPii);
        }
    }
}
