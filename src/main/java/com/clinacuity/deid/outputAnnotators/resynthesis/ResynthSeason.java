
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

package com.clinacuity.deid.outputAnnotators.resynthesis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.StringTokenizer;

import static com.clinacuity.deid.mains.DeidPipeline.PII_LOG;

public class ResynthSeason extends ResynthDate {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<String, String> SEASON_TO_DATE = Map.of("fall", "October 15", "autumn", "October 15",
            "autumntime", "October 15", "spring", "April 15", "springtime", "April 15",
            "summer", "July 15", "summertime", "July 15", "winter", "January 15", "wintertime", "January 15");

    public ResynthSeason() {
    }

    @Override
    public String getAndUpdateResynthesizedValue(String cleaned) {
        //for reference: if desire is to resynth to the season that the daysOffset would result in then:
        //(offset -45)/91==# of seasons to shift
        //array of seasons in order, find index of oldPii in array, then index = (index + # of seasons) % 4,
        //resulting index is new season. Roughly 20-25% chance of no-change
        if (resynthesisMap.contains(cleaned.toLowerCase())) {
            return matchCase(cleaned, resynthesisMap.getNewValue(cleaned.toLowerCase()));
        }

        // convert to date, add shift, round off to just that month
        if (SEASON_TO_DATE.containsKey(cleaned.toLowerCase())) {
            return getAndRoundOffDateFromSeason(cleaned, SEASON_TO_DATE.get(cleaned.toLowerCase()));
        } else {
            //try to pull a word out
            StringTokenizer tokenizer = new StringTokenizer(cleaned);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (SEASON_TO_DATE.containsKey(token.toLowerCase())) {
                    return getAndRoundOffDateFromSeason(cleaned, SEASON_TO_DATE.get((token.toLowerCase())));
                }
            }
            //TODO?  What to do now?
            LOGGER.log(PII_LOG, "{}", () -> "Season issue, can't process " + cleaned);
            return getAndRoundOffDateFromSeason(cleaned, "January");
        }
    }

    private String getAndRoundOffDateFromSeason(String oldPii, String fromSeason) {
        String newDate = matchCase(oldPii, super.getAndUpdateResynthesizedValue(fromSeason));

        int spaceIndex = newDate.indexOf(' ');
        if (spaceIndex > 0) {
            String month = newDate.substring(0, spaceIndex);
            resynthesisMap.setNewValue(oldPii.toLowerCase(), month);
            return month;
        } else {
            LOGGER.log(PII_LOG, "{}", () -> "No space in resulting date for season " + fromSeason + " to " + newDate);
            resynthesisMap.setNewValue(oldPii.toLowerCase(), newDate);
            return newDate;
        }
    }

}