
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

import com.clinacuity.deid.outputAnnotators.resynthesis.fromFile.ResynthesizerFromFile;
import com.clinacuity.deid.outputAnnotators.resynthesis.names.ResynthesizerName;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResynthElectronicAddress extends ResynthesizerFromFile {
    private static final String URL_PATH = "urlList.obj";
    private static final Pattern URL_PATTERN = Pattern.compile("(\\w+\\.\\w+\\.?\\w*\\.?\\w*)");
    private static final Pattern IP_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");

    public ResynthElectronicAddress() {
        super(URL_PATH);
    }

    @Override
    public String getAndUpdateResynthesizedValue(String oldPii) {
        String loweredPii = oldPii.toLowerCase();
        String cleanedPii = loweredPii;

        Matcher mat = URL_PATTERN.matcher(loweredPii);
        if (!cleanedPii.contains("@") && mat.find()) {
            cleanedPii = mat.group(0);
        }
        if (resynthesisMap.contains(cleanedPii)) {
            return resynthesisMap.getNewValue(cleanedPii);
        }
        if (cleanedPii.contains("@")) {//email
            String firstName = ResynthesizerName.UNISEX_FIRST_NAMES[ThreadLocalRandom.current().nextInt(ResynthesizerName.UNISEX_FIRST_NAMES.length)];
            String lastName = ResynthesizerName.LAST_NAMES[ThreadLocalRandom.current().nextInt(ResynthesizerName.LAST_NAMES.length)];
            String url = data[ThreadLocalRandom.current().nextInt(data.length)];
            resynthesisMap.setNewValue(cleanedPii, firstName + "-" + lastName + "@" + url);
        } else if (IP_PATTERN.matcher(cleanedPii).find()) {
            int ip1 = ThreadLocalRandom.current().nextInt(1, 256);
            int ip2 = ThreadLocalRandom.current().nextInt(1, 256);
            int ip3 = ThreadLocalRandom.current().nextInt(1, 256);
            int ip4 = ThreadLocalRandom.current().nextInt(1, 256);
            resynthesisMap.setNewValue(cleanedPii, ip1 + "." + ip2 + "." + ip3 + "." + ip4);
        } else {//will this attempt to find again?
            resynthesisMap.setNewValue(cleanedPii, "www." + super.getAndUpdateResynthesizedValue(cleanedPii));
        }
        return resynthesisMap.getNewValue(cleanedPii);
    }
}
