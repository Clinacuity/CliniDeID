
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

import com.clinacuity.clinideid.message.DeidLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.clinacuity.deid.mains.DeidPipeline.PII_LOG;

public class ResynthDate extends Resynthesizer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern FOUR_DIGIT_YEAR = Pattern.compile("\\d{4}");

    private static final SimpleDateFormat[] DATE_FORMATS = {//these are not synchronized
            new SimpleDateFormat("MMMMM d, yyyy"),
            new SimpleDateFormat("MMMMM d yyyy"),

            new SimpleDateFormat("MMM d, yyyy"),
            new SimpleDateFormat("MMM d yyyy"),
            new SimpleDateFormat("MMM. d yyyy"),
            new SimpleDateFormat("MMM. d, yyyy"),
            new SimpleDateFormat("M/d, yyyy"),
            new SimpleDateFormat("M/d yyyy"),

            new SimpleDateFormat("M/d/yyyy"),
            new SimpleDateFormat("M/d/yy"),

            new SimpleDateFormat("M-d-yyyy"),

            new SimpleDateFormat("d MMMMM yyyy"),
            new SimpleDateFormat("d MMM yyyy"),
            new SimpleDateFormat("d MMMMM"),
            new SimpleDateFormat("d MMM"),

            new SimpleDateFormat("M-d-yy"),
            new SimpleDateFormat("MMMMM/d"),
            new SimpleDateFormat("MMMMM-d"),
            new SimpleDateFormat("MMMMM d"),
            new SimpleDateFormat("MMM/d"),
            new SimpleDateFormat("MMM-d"),
            new SimpleDateFormat("MMM d"),
            new SimpleDateFormat("MMM. d"),
            new SimpleDateFormat("M/d"),
            new SimpleDateFormat("M-yy"),
            new SimpleDateFormat("M yy"),
            new SimpleDateFormat("M-yyyy"),
            new SimpleDateFormat("M yyyy"),
            new SimpleDateFormat("M-d"),
            new SimpleDateFormat("M d"),
            new SimpleDateFormat("M/yyyy"),
            new SimpleDateFormat("yyyy"),
            new SimpleDateFormat("yy"),
            new SimpleDateFormat("yyyy-M-d"),
            new SimpleDateFormat("yyyy-MMM-d"),
            new SimpleDateFormat("yyyy-MMMMM-d"),
            new SimpleDateFormat("d-MMMMM-yyyy"),
            new SimpleDateFormat("d-MMM-yyyy"),
            new SimpleDateFormat("MMMMM-d-yyyy"),
            new SimpleDateFormat("MMM-d-yyyy"),
            new SimpleDateFormat("yyyy.M.d"),
            new SimpleDateFormat("M.d.yy"),
            new SimpleDateFormat("M.d.yyyy"),
            new SimpleDateFormat("MMMMM, yyyy"),
            new SimpleDateFormat("MMM, yyyy"),
            new SimpleDateFormat("MMMMM yyyy"),
            new SimpleDateFormat("MMM. yyyy"),
            new SimpleDateFormat("MMM yyyy"),
            new SimpleDateFormat("MMMMM yy"),
            new SimpleDateFormat("MMM yy"),
            new SimpleDateFormat("MMMMM"),
            new SimpleDateFormat("MMM"),
            new SimpleDateFormat("M-yyyy"),
            new SimpleDateFormat("MMMMM/yyyy"),
            new SimpleDateFormat("M/yyyy"),
            new SimpleDateFormat("M/yy"),
            new SimpleDateFormat("d"),

            new SimpleDateFormat("dd M"),
            new SimpleDateFormat("yyyy/MM/dd"),
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("yyyy MM dd"),
            new SimpleDateFormat("MM-/yyyy"),
            new SimpleDateFormat("MM/-yyyy"),
            new SimpleDateFormat("MM-/dd"),
            new SimpleDateFormat("MM/-dd"),
            new SimpleDateFormat("yyyy/MM"),
            new SimpleDateFormat("DD/yyyy"),
            new SimpleDateFormat("DD/-yyyy"),
            new SimpleDateFormat("DD-/yyyy"),

            new SimpleDateFormat("yyyy/dd/MM"),
            new SimpleDateFormat("yyyy dd MM"),
            new SimpleDateFormat("yyyy-dd-MM")
    };
    private static final Pattern[] SPECIAL_FORMS = {
            Pattern.compile("(\\d{4})\\s*[-/]\\s*(\\d{4})", Pattern.CASE_INSENSITIVE), //2014/2018
    };
    private static final Pattern SEPT_ISSUE = Pattern.compile("(sept)\\b", Pattern.CASE_INSENSITIVE);
    private static final Map<String, String> HOLIDAY_TO_MONTH = Map.ofEntries(Map.entry("new years", "January 1"),
            Map.entry("martin luther king", "January 15"), Map.entry("martin luther king jr", "January 15"),
            Map.entry("valentines", "February 14"), Map.entry("valentine", "February 14"),
            Map.entry("president", "February 19"), Map.entry("presidents", "February 19"),
            Map.entry("st patricks", "March 17"), Map.entry("st patrick", "March 17"),
            Map.entry("palm sunday", "March 25"), Map.entry("good friday", "March 30"),
            Map.entry("passover", "March 31"), Map.entry("easter", "April 1"),
            Map.entry("confederate memorial", "April 30"), Map.entry("cinco de mayo", "May 5"),
            Map.entry("mothers", "May 13"), Map.entry("mother's", "May 13"), Map.entry("ramadan", "May 16"),
            Map.entry("memorial", "May 28"), Map.entry("father's", "June 17"),
            Map.entry("flag", "June 14"), Map.entry("fathers", "June 17"), Map.entry("independence day", "July 4"),
            Map.entry("labor", "September 3"), Map.entry("rosh hashana", "September 10"), Map.entry("yom kippur", "September 19"),
            Map.entry("columbus", "October 8"), Map.entry("halloween", "October 31"), Map.entry("all saints", "November 1"),
            Map.entry("all souls", "November 2"), Map.entry("veterans", "November 11"), Map.entry("veteran", "November 11"),
            Map.entry("thanksgiving", "November 22"),
            Map.entry("hanukkah", "December 3"), Map.entry("chanukah", "December 3"), Map.entry("christmas eve", "December 24"),
            Map.entry("christmas", "December 25"), Map.entry("new years eve", "December 31"));
    private static final String[] SUFFIXES = {"th", "TH", "'s", "'S", "s", "S"};  //should be ordered longest to shortest so 's comes before s
    private static final String[] PREFIXES = {"'"};  //should be ordered longest to shortest so 's comes before s
    private static final Pattern MIDDLE = Pattern.compile(("\\s+(of|in)\\s+"), Pattern.CASE_INSENSITIVE);
    private static final Pattern NEW_LINE = Pattern.compile("\n+", Pattern.MULTILINE);
    private final GregorianCalendar calendar = new GregorianCalendar();

    public ResynthDate() {
        calendar.setLenient(false);
        for (SimpleDateFormat form : DATE_FORMATS) {
            form.setLenient(false);
        }
    }

    @Override
    public String getAndUpdateResynthesizedValue(String oldPii) {
        if (!resynthesisMap.contains(oldPii.toLowerCase())) {
            resynthesisMap.setNewValue(oldPii.toLowerCase(), getNewDate(oldPii));
        }
        return resynthesisMap.getNewValue(oldPii.toLowerCase());
    }

    private String getNewDate(String oldPii) {
        String cleaned = NEW_LINE.matcher(oldPii).replaceAll(" "); //tolower trim?

        if (HOLIDAY_TO_MONTH.containsKey(oldPii.toLowerCase())) {
            cleaned = HOLIDAY_TO_MONTH.get(oldPii.toLowerCase());
        } else {
            int lastSpaceIndex = oldPii.lastIndexOf(' ');
            if (lastSpaceIndex >= 0) {
                String sub = oldPii.substring(0, lastSpaceIndex);
                if (HOLIDAY_TO_MONTH.containsKey(sub.toLowerCase())) {
                    cleaned = HOLIDAY_TO_MONTH.get(sub.toLowerCase());
                }
            }
        }
        String suffix = "";
        for (String suf : SUFFIXES) {
            if (cleaned.endsWith(suf)) {
                suffix = suf;
                cleaned = cleaned.substring(0, cleaned.length() - suf.length());
                break;
            }
        }
        String prefix = "";
        for (String pre : PREFIXES) {
            if (cleaned.startsWith(pre)) {
                prefix = pre;
                cleaned = cleaned.substring(pre.length());
                break;
            }
        }
        String middlePart = null;
        Matcher mat = MIDDLE.matcher(cleaned);
        if (mat.find()) {
            cleaned = MIDDLE.matcher(cleaned).replaceAll(" ");
            middlePart = mat.group(1);
        }
        mat = SEPT_ISSUE.matcher(cleaned);
        if (mat.find()) {
            cleaned = mat.replaceAll("sep");
        }
        boolean processed = false;
        String newDate;
        List<String> newDates = new ArrayList<>();
        ParsePosition index = new ParsePosition(0);
        for (SimpleDateFormat form : DATE_FORMATS) {
            index.setIndex(0);
            Date date = form.parse(cleaned, index);

            if (date == null || index.getIndex() != cleaned.length()) {
                continue;
            }
            calendar.setTime(date);
            adjustCalendar();
            newDate = form.format(calendar.getTime());
            if (middlePart != null) {
                int spaceIndex = newDate.indexOf(' ');
                if (spaceIndex > 0) {
                    newDate = newDate.substring(0, spaceIndex + 1) + middlePart + newDate.substring(spaceIndex);
                }
            }
            newDates.add(prefix + newDate + suffix);//TODO should suffix be added after choice from list is made?
            processed = true;
        }

        if (newDates.size() == 0) {
            mat = SPECIAL_FORMS[0].matcher(cleaned);
            if (mat.find() && level == DeidLevel.beyond) {
                processed = true;
                int year1 = Integer.parseInt(mat.group(1));
                year1 += resynthesisMap.getYearOffset();
                int year2 = Integer.parseInt(mat.group(2));
                year2 += resynthesisMap.getYearOffset();
                newDates.add(year1 + "/" + year2);
            }
        }
        if (!processed) {//TODO ??? What should be done?
            LOGGER.log(PII_LOG, "Couldn't parse date: [[{}]]\n", cleaned);
            return cleaned;
        }
        boolean cleanedHasFourDigitYear = FOUR_DIGIT_YEAR.matcher(cleaned).find();
        newDates.removeIf(dat -> FOUR_DIGIT_YEAR.matcher(dat).find() != cleanedHasFourDigitYear);
        //TODO: same as above for full month names, then for abbreviated names. Issue with May

        //TODO: suffix of th implies day, suffix of 's or s implies year???
        if (newDates.size() == 1) {
            return newDates.get(0);
        } else {
//            logger.log(PII_LOG,"For {} got: \t{}", cleaned, newDates.toString());
            for (String date : newDates) {
                if (date.length() == cleaned.length()) {
//                    logger.log(PII_LOG,"Choice by exact length {}\n", date);
                    return date;
                }
            }
            for (String date : newDates) {
                if (Math.abs(date.length() - cleaned.length()) <= 1) {
//                    logger.log(PII_LOG,"Choice by +-1 {}\n", date);
                    return date;
                }
            }
            for (String date : newDates) {
                if (Math.abs(date.length() - cleaned.length()) <= 2) {
//                    logger.log(PII_LOG,"Choice by +-2 {}\n", date);
                    return date;
                }
            }
            int longest = 0;
            String candidate = "";
            for (String date : newDates) {
                if (date.length() > longest) {
                    candidate = date;
                    longest = date.length();
                }
            }
            //logger.log(PII_LOG,"Choice by first longest {}\n", candidate);
            return candidate;
        }
    }

    private void adjustCalendar() {
        calendar.add(Calendar.DATE, resynthesisMap.getDaysOffset());
        if (level == DeidLevel.beyond && calendar.get(Calendar.YEAR) != 0) {//strict doesn't do years, limited doesn't do dates at all
            //don't add year to date that didn't have them
            if (calendar.get(Calendar.YEAR) < 1900) {
                if (calendar.get(Calendar.YEAR) < 25) {
                    calendar.add(Calendar.YEAR, 2000);//this could be wrong for a birthdate, but otherwise likely correct
                } else {
                    calendar.add(Calendar.YEAR, 1900);
                }
            }
            calendar.add(Calendar.YEAR, resynthesisMap.getYearOffset());
        }
    }
}
