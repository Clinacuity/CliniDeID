
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

import com.clinacuity.clinideid.message.DeidLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@XmlRootElement(name = "PiiOptions")
@XmlAccessorType(XmlAccessType.FIELD)
public class PiiOptions {
    public static final int NONE = 0;
    public static final int ALL = 1;
    public static final int DATE_MONTH_DAY_ONLY = 2;
    public static final int AGE_GT89_ONLY = 2;
    public static final int ZIP_3DIGIT_ONLY = 2;
    public static final Set<String> SPECIALS = Set.of("Age", "Date", "Zip");
    private static final Logger LOGGER = LogManager.getLogger();
    private Map<String, Boolean> options = new HashMap<>(Util.PII_SUB_TO_PARENT_TYPE.keySet().size());
    private Map<String, Integer> specialOptions = new HashMap<>();
    private String lastUsed = ""; //for use in GUI table display, is saved in configuration xml files, updated by pipeline running

    public PiiOptions() {
        Util.PII_SUB_TO_PARENT_TYPE.keySet().forEach(type -> options.put(type, false));
        for (String type : SPECIALS) {
            specialOptions.put(type, NONE);
        }
    }

    public PiiOptions(PiiOptions source) {
        for (String type : Util.PII_SUB_TO_PARENT_TYPE.keySet()) {
            options.put(type, source.getOption(type));
        }
        for (String type : SPECIALS) {
            specialOptions.put(type, source.getSpecialOption(type));
        }
        this.lastUsed = source.lastUsed;
    }

    public PiiOptions(DeidLevel initial) {
        setLevel(initial);
    }

    public String getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(String lastUsed) {
        this.lastUsed = lastUsed;
    }

    public void setLevel(DeidLevel initial) {
        for (String type : Util.PII_SUB_TO_PARENT_TYPE.keySet()) {
            options.put(type, true);//start with everything, remove those not needed
        }
        if (initial.equals(DeidLevel.beyond)) {
            for (String type : SPECIALS) {
                specialOptions.put(type, ALL);
            }

        } else if (initial.equals(DeidLevel.strict)) {
            specialOptions.put("Date", DATE_MONTH_DAY_ONLY);
            specialOptions.put("Age", AGE_GT89_ONLY);
            specialOptions.put("Zip", ZIP_3DIGIT_ONLY);

            options.put("Provider", false);
            options.put("Season", false);
            options.put("Profession", false);
            options.put("State", false);
            options.put("Country", false);

        } else if (initial.equals(DeidLevel.limited)) {
            specialOptions.put("Date", NONE);
            specialOptions.put("Age", NONE);
            specialOptions.put("Zip", NONE);

            options.put("City", false);
            options.put("Provider", false);
            options.put("State", false);
            options.put("Country", false);
            options.put("Season", false);
            options.put("DayOfWeek", false);
            options.put("Profession", false);
            options.put("ClockTime", false);
            options.put("Zip", false);
            options.put("Age", false);
            options.put("Date", false);
        }
    }

    public void printOptions() {//debug use only
        for (Map.Entry<String, Boolean> item : options.entrySet()) {
            LOGGER.debug("{}: {}", item.getKey(), item.getValue());
        }
        for (Map.Entry<String, Integer> item : specialOptions.entrySet()) {
            LOGGER.debug("{}: {}", item.getKey(), item.getValue());
        }
    }

    public void setOption(String piiType, boolean option) {
        options.put(piiType, option);
    }

    public void setSpecialOption(String piiType, int option) {
        if (!specialOptions.containsKey(piiType)) {
            LOGGER.error("setSpecialOption with NON-special {}", piiType);
            return;
        }
        if (option == 0) {
            options.put(piiType, false);
        } else {
            options.put(piiType, true);
        }
        specialOptions.put(piiType, option);
    }

    public Boolean getOption(String piiType) {
        return options.get(piiType);
    }

    public int getSpecialOption(String piiType) {
        if (!specialOptions.containsKey(piiType)) {
            LOGGER.error("setSpecial with NON-special {}", piiType);
            return -1;
        }
        return specialOptions.get(piiType);
    }

    public Map<String, Boolean> getOptionMap() {
        return options;
    }

    public Map<String, Integer> getSpecialMap() {
        return specialOptions;
    }

    public void clear() {
        options.replaceAll((key, value) -> false);
        specialOptions.replaceAll((key, value) -> NONE);
    }
}
