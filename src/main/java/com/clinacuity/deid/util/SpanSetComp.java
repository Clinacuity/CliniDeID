
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

package com.clinacuity.deid.util;

import java.util.Comparator;

public class SpanSetComp implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        //start + " " + end + " " + sub-type + " " + type
        String oA1[] = o1.split(" ", 4);
        String oA2[] = o2.split(" ", 4);

        int s1 = Integer.parseInt(oA1[0]);
        int s2 = Integer.parseInt(oA2[0]);

        if (s1 != s2) {
            //return new Integer(s1).compareTo(s2);
            return Integer.compare(s1, s2);
        } else {
            int e1 = Integer.parseInt(oA1[1]);
            int e2 = Integer.parseInt(oA2[1]);
            if (e1 != e2) {
                //    return new Integer(e1).compareTo(e2);
                return Integer.compare(e1, e2);
            } else {
                return oA1[2].compareTo(oA2[2]);
            }
        }
    }
}
