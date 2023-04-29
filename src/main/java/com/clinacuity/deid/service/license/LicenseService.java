
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

package com.clinacuity.deid.service.license;

import com.clinacuity.deid.service.DeidProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LicenseService {
    private final DeidProperties properties;

    @Autowired
    public LicenseService(DeidProperties properties) {
        this.properties = properties;
    }

    /**
     * Set  the license key which will be used. This should be called before each batch is processed to ensure
     * the correct license is being used.
     *
     * @param key The license key used for the batch.
     */
    public void setLicenseKey(UUID key) {
        properties.getLicense().getValue().setKey(key);
    }

    /**
     * Method which creates an appropriate PendingRequest for this batch. Note the following:
     * <ul>
     *     <li>The batch should not process for demo licenses.</li>
     *     <li>We do not need to update the request at intervals, since a runtime error will cause all data to be lost.</li>
     * </ul>
     *
     * @param amountToProcess The number of files in the batch.
     */
    public void requestProcess(int amountToProcess) {
        properties.requestProcess(amountToProcess);
    }

    /**
     * Calls the API to finalize the current batch and commit the character counts to the database.
     */
    public void finishProcess() {
        properties.completeRequest();
        reset();
    }

    private void reset() {
        properties.getLicense().getValue().setKey(null);
    }
}
