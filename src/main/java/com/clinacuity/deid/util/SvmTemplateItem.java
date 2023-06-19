
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

public class SvmTemplateItem {
    public String label; //fsA[0]
    public int rI0;
    public int cI0;
    public int rI1;
    public int cI1;
    public int rI2;
    public int cI2;
    public boolean hasSecond = false;
    public boolean hasThird = false;

    public SvmTemplateItem(String label) {
        this.label = label;
    }
}