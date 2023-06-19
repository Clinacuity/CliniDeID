
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.clinacuity.deid.mains.DeidPipeline.PII_LOG;
import static java.util.Map.entry;

public class ResynthClockTime extends Resynthesizer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<Integer, Integer> CLOCK_GROUP_AM_PM = Map.of(0, 3, 2, 2);
    private static final Map<Integer, Integer> CLOCK_GROUP_MINUTE = Map.of(1, 2, 0, 2, 4, 2);
    private static final Pattern[] CLOCK_TIME_PATTERNS = {
            Pattern.compile("(\\d+)\\s*:\\s*(\\d+)(?:\\s*:\\s*\\d\\d)?\\s*([aApP])\\.?[mM]?\\.?"), //optional seconds
            Pattern.compile("(\\d+)\\s*:\\s*(\\d+)(:\\d\\d)?"),//optional part is seconds which we don't change
            Pattern.compile("(\\d+)\\s*([aApP])\\.?[mM]?\\.?"),
            Pattern.compile("(\\d{1,2})"),
            Pattern.compile("(\\d\\d?)(\\d\\d)"),//military with no : can be 3 digit or 4 digit
            Pattern.compile("(\\d{1,2})\\s*o'clock"),
    };
    private static final Map<String, String> SPECIAL_TIMES = Map.ofEntries(
            entry("midnight", "0000"), entry("noon", "1200"), entry("morning", "0900"),
            entry("afternoon", "0300"), entry("evening", "2000"), entry("night", "2200"), entry("dusk", "1800"), entry("dawn", "0600"),
            entry("twilight", "1900"), entry("sunrise", "0600"), entry("sunset", "1800"), entry("midday", "1200")
    );
    private static final Pattern DIGITS_PAT = Pattern.compile(".*(\\d)");
    private static final Pattern O_CLOCK = Pattern.compile("(\\w+)\\s*(?:o'?\\s*)?clock", Pattern.CASE_INSENSITIVE);

    public ResynthClockTime() {
    }

    private static Matcher findTimePattern(String timeText, TimeParts timeParts) {//return null if none of patterns match
        Matcher mat;
        for (int i = 0; i < CLOCK_TIME_PATTERNS.length; i++) {
            mat = CLOCK_TIME_PATTERNS[i].matcher(timeText);
            if (mat.matches()) {
                timeParts.hour = Integer.parseInt(mat.group(1));
                if (CLOCK_GROUP_AM_PM.containsKey(i)) {
                    timeParts.amPm = mat.group(CLOCK_GROUP_AM_PM.get(i));
                }
                if (CLOCK_GROUP_MINUTE.containsKey(i)) {
                    timeParts.minute = Integer.parseInt(mat.group(CLOCK_GROUP_MINUTE.get(i)));
                }
                timeParts.type = i;
                return mat;
            }
        }
        return null;//indication that none of the patterns matched
    }

    public String processClockTime(String originalTimeText) {//returns difference in length between old covered text and resynth time
        /* parse int into a type of time, its hour and min and am/pm parts as available
            shift min, rollover to hour
            reformat according to 'type' from parse
            replace in new then in complete text
            array of patterns, index into array with match is its 'type'
            maps from type to group# for minutes and am/pm, or no mapping if not present         */
        TimeParts timeParts = new TimeParts();
        Matcher mat = findTimePattern(originalTimeText, timeParts);
        if (mat == null) {//failed to match common patterns, so it is a special case
            return handleUncommonTimes(originalTimeText, timeParts);
        } else {
            return changeTime(mat, timeParts, originalTimeText);
        }
    }

    private String handleUncommonTimes(String originalTimeText, TimeParts timeParts) {
        Matcher mat;
        if (SPECIAL_TIMES.containsKey(originalTimeText.toLowerCase())) {//annotation is exactly a special word, e.g. noon
            mat = findTimePattern(SPECIAL_TIMES.get(originalTimeText.toLowerCase()), timeParts);
            return changeTime(mat, timeParts, SPECIAL_TIMES.get(originalTimeText.toLowerCase()));
        } else {//check for contains of special words, like 12noon or '12 noon'
            String timeTextLower = originalTimeText.toLowerCase();
            for (Map.Entry<String,String> specialItem: SPECIAL_TIMES.entrySet()) {
                if (timeTextLower.contains(specialItem.getKey())) {
                    mat = findTimePattern(specialItem.getValue(), timeParts);
                    return changeTime(mat, timeParts, specialItem.getValue());
                }
            }

            //check for something o clock
            Map<String, String> wordToNumber = Map.ofEntries(entry("one", "01"));
            mat = O_CLOCK.matcher(originalTimeText);
            if (mat.find()) {
                String wordTime = mat.group(1);//get word
                String newTimeText;
                if (wordToNumber.containsKey(wordTime)) {
                    newTimeText = wordToNumber.get(wordTime) + "00";
                } else {
                    try {
                        int hour = Integer.parseInt(wordTime);  // for 1 o' clock
                        newTimeText = hour + "00";
                    } catch (NumberFormatException e) {
                        newTimeText = "12:00";  //Need some time to base change on
                    }
                }
                mat = findTimePattern(newTimeText, timeParts);
                return changeTime(mat, timeParts, newTimeText);
            }
        }
        LOGGER.log(PII_LOG, "{}", () -> "WARN: Time [[" + originalTimeText + "]] didn't match any patterns");
        mat = DIGITS_PAT.matcher(originalTimeText);
        if (!mat.find()) {//TODO ???? what to do??
            LOGGER.log(PII_LOG, "{}", () -> "ERROR: Time [[" + originalTimeText + "]] had no digits, could not resynthesize");
            return originalTimeText;
        } else {
            //just perturb any and all digits found in timeText.
            int digit = Integer.parseInt(mat.group(1));
            digit = (digit + 1) % 10;
            StringBuilder newText = new StringBuilder(originalTimeText);//more efficient to do replacements in a StringBuilder
            newText.replace(mat.start(1), mat.end(1), Integer.toString(digit));
            //should/can this be repeated for other digits?
            LOGGER.log(PII_LOG, "{}", () -> "WARN: resynthed [[" + originalTimeText + "]] as [[" + newText.toString() + "]]");
            return newText.toString();
        }
    }

    //given annotation and Matcher for what shape the time is in and its components in timeParts, adjust the time by shift and replace inside timeText
    private String changeTime(Matcher mat, TimeParts timeParts, String timeTextToChange) {
        int originalHour = timeParts.hour;
        if (CLOCK_GROUP_MINUTE.containsKey(timeParts.type)) {
            timeParts.minute += resynthesisMap.getMinutesOffset();
            if (timeParts.minute >= 60) {
                timeParts.hour += timeParts.minute / 60;
                timeParts.minute = timeParts.minute % 60;
            }
            if (timeParts.minute < 0) {
                timeParts.hour -= (int) Math.ceil(-1.0 * timeParts.minute / 60);
                timeParts.minute += 60;
                if (timeParts.minute < 0) {
                    timeParts.minute += 60;
                }
            }
        } else {//no timeParts.minutes in time
            timeParts.hour += Math.ceil(1.0 * resynthesisMap.getMinutesOffset() / 60);
        }
        if (CLOCK_GROUP_AM_PM.containsKey(timeParts.type)) {
            //additional time needs to change a/p-m time
            if (timeParts.hour > 12 || timeParts.hour < 1 ||
                    (originalHour == 12 && timeParts.hour < 12)) {//switch am/pm, keep case
                char amPm = timeParts.amPm.charAt(0);
                if (amPm == 'a') {
                    timeParts.amPm = "p";
                } else if (amPm == 'p') {
                    timeParts.amPm = "a";
                } else if (amPm == 'A') {
                    timeParts.amPm = "P";
                } else if (amPm == 'P') {
                    timeParts.amPm = "A";
                }
                if (timeParts.hour > 12) {
                    timeParts.hour -= 12;
                } else if (timeParts.hour < 1) {
                    timeParts.hour += 12;
                }
            }
        } else {//no am/pm so assume military, +24 removes negative possibilities
            timeParts.hour = (timeParts.hour + 24) % 24;
        }

        StringBuilder newText = new StringBuilder(timeTextToChange);//more efficient to do replacements in a StringBuilder
        if (CLOCK_GROUP_MINUTE.containsKey(timeParts.type)) {//change minute first as it is always 2 digit, hour may change in size messing up mat.start and mat.end values
            newText.replace(mat.start(CLOCK_GROUP_MINUTE.get(timeParts.type)), mat.end(CLOCK_GROUP_MINUTE.get(timeParts.type)), String.format("%02d", timeParts.minute));
        }
        if (CLOCK_GROUP_AM_PM.containsKey(timeParts.type)) {
            newText.replace(mat.start(CLOCK_GROUP_AM_PM.get(timeParts.type)), mat.end(CLOCK_GROUP_AM_PM.get(timeParts.type)), timeParts.amPm);
            newText.replace(mat.start(1), mat.end(1), Integer.toString(timeParts.hour)); //no leading 0 with am/pm
        } else {
            newText.replace(mat.start(1), mat.end(1), String.format("%02d", timeParts.hour));//military time gets leading 0?
        }
        return newText.toString();
    }

    @Override
    public String getAndUpdateResynthesizedValue(String oldPii) {
        String oldLower = oldPii.toLowerCase();
        if (resynthesisMap.contains(oldLower)) {
            return matchCase(oldPii, resynthesisMap.getNewValue(oldLower));
        }
        resynthesisMap.setNewValue(oldLower, processClockTime(oldPii));
        return resynthesisMap.getNewValue(oldLower);
    }

    private static class TimeParts {
        int hour;
        int minute;
        String amPm;
        int type;//index into array of Patterns that this time matched, used for replacing parts with resynthed numbers
    }
}
