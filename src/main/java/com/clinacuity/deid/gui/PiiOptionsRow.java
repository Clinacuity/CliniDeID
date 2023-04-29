
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

package com.clinacuity.deid.gui;

import javafx.beans.property.SimpleStringProperty;

public class PiiOptionsRow {
    private final SimpleStringProperty filename;
    private final SimpleStringProperty lastUsed;
    private final SimpleStringProperty created;
    private final String filePath;

    public PiiOptionsRow(String filename, String lastUsed, String created, String filePath) {
        this.filename = new SimpleStringProperty(filename);
        this.lastUsed = new SimpleStringProperty(lastUsed);
        this.created = new SimpleStringProperty(created);
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFilename() {
        return filename.get();
    }

    public void setFilename(String filename) {
        this.filename.set(filename);
    }

    public String getLastUsed() {
        return lastUsed.get();
    }

    public void setLastUsed(String lastUsed) {
        this.lastUsed.set(lastUsed);
    }

    public String getCreated() {
        return created.get();
    }

    public void setCreated(String created) {
        this.created.set(created);
    }
}
