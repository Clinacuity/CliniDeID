
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

package com.clinacuity.deid.siteService;

public class DeidResponse {

    private final long id;
    private final String resynthesizedText;

    public DeidResponse(long id, String resynthesizedText) {
        this.id = id;
        this.resynthesizedText = resynthesizedText;
    }

    public long getId() {
        return id;
    }

    public String getResynthesizedText() {
        return resynthesizedText;
    }
}