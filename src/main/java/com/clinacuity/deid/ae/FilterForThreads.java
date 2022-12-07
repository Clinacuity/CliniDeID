
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

package com.clinacuity.deid.ae;

import com.clinacuity.deid.type.PiiAnnotation;
import com.clinacuity.deid.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.jcas.JCas;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.clinacuity.deid.mains.DeidPipeline.PII_LOG;

public class FilterForThreads {
    //public static final String DRUGS_FILE_PARAM = "DrugFilePath";
    private static final Logger LOGGER = LogManager.getLogger();

    // Pattern stateAbbreviations = Pattern.compile("AK|AL|AR|AS|AZ|CA|CO|CT|DC|DE|FL|FM|GA|GU|HI|IA|ID|IL|IN|KS|KY|LA|MA|MD|ME|MH|MI|MN|MO|MP|MS|MT|NC|ND|NE|NH|NJ|NM|NV|NY|OH|OK|OR|PA|PR|PW|RI|SC|SD|TN|TX|UT|VA|VI|VT|WA|WI|WV|WY");
    //private Set<String> stateAbbr = Set.of("AK", "AL", "AR", "AS", "AZ", "CA", "CO", "CT", "DC", "DE", "FL", "FM", "GA", "GU", "HI", "IA", "ID", "IL", "IN", "KS", "KY", "LA", "MA", "MD", "ME", "MH", "MI", "MN", "MO", "MP", "MS", "MT", "NC", "ND", "NE", "NH", "NJ", "NM", "NV", "NY", "OH", "OK", "OR", "PA", "PR", "PW", "RI", "SC", "SD", "TN", "TX", "UT", "VA", "VI", "VT", "WA", "WI", "WV", "WY");
    private static final Pattern AGE_WITH_MONTHS = Pattern.compile("(\\d\\d?(?:[.]\\d|\\s+\\d/\\d)?)\\s*mo(?:nth|[.])?s?");
    private static final Pattern AGE_SPECIAL = Pattern.compile("tween|teenagers?|pre-?teens?|twenties|thirties|\\d+y\\d+\\.\\d+m", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALID_AGES = Pattern.compile("(?:1[012]\\d|\\d\\d?)(?:'?s)?|\\d\\d?(?:[.]\\d|\\s+\\d/\\d)?"); //0-129 is valid range, no digits before or after, but other stuff allowed for 's

    private static final Set<String> DECADES = Set.of("ninety", "eighty", "seventy", "sixty", "fifty", "forty", "thirty", "twenty");
    private static final Set<String> ONES = Set.of("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
            "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen",
            "teen", "teenager", "teenage", "teens", "preteen", "preteens", "zero");
    private static final Pattern WORD_AGES = Pattern.compile("(?:one[- ]hundred[- ](?:and[- ])?)?([a-z]+)(?:[- ]([a-z]+))?", Pattern.CASE_INSENSITIVE);

    private static final Pattern[] DATE_PATTERNS = {
            Pattern.compile("(\\d\\d?)[-/ .\n]+(\\d\\d?)"),  //mm/dd or d/m or - or space separate
            Pattern.compile("(\\d\\d?)[-/ .\n]+(\\d\\d?)[,-/ .\n]+(\\d+)"), //mm/dd/yy or yyyy
            Pattern.compile("([a-z]+)(?:th|st|nd|rd)?[,-/ .\n]+(?:\\s*of\\s*)?(\\d+)(?:th|st|nd|rd)?", Pattern.CASE_INSENSITIVE),// Sept dd or Sept yy or yyyy, period for abbreviations or Sept of yyyy
            Pattern.compile("([a-z]+)[-/ .\n]+(\\d\\d?)[-/ ,\n]+(\\d+)", Pattern.CASE_INSENSITIVE),// Sept 3,? 2019
            Pattern.compile("(\\d\\d?(?:\\d\\d)?)[-/ \n]+(\\d+)"),  // yy/yy mm/yy
            Pattern.compile("'?\\s*(\\d+)(?:(?:'?[sS])?|(th|st|nd|rd)?)"),  // yy or yyyy or 'yy or yy's

            Pattern.compile("((?:new\\s+years|martin\\s+luther\\s+king'?s?|martin\\s+luther\\s+king\\s+jr|" +
                    "valentine'?s?|president'?s?|st\\s+patrick'?s?|palm\\s+sunday|good\\s+friday|passover|easter|" +
                    "confederate\\s+memorial|cinco\\s+de\\s+mayo|mother'?s?|ramadan|memorial|flag|" + "" +
                    "father'?s?|independence|labor|rosh\\s+hashana|yom\\s+kippur|columbus|" + "" +
                    "halloween|all\\s+saint'?s?|all\\s+soul'?s?|veteran'?s?|thanksgiving|" +
                    "hanukkah|chanukah|christmas\\s+eve|Christmas)(?:\\s+day)?)", Pattern.CASE_INSENSITIVE),

            Pattern.compile("([a-z]+)", Pattern.CASE_INSENSITIVE), //month
            Pattern.compile("(\\d\\d?)[-/ ,\n]+([a-z]+)[-/ .\n]+(\\d\\d(?:\\d\\d)?)", Pattern.CASE_INSENSITIVE),// 3 Sept 2019
            Pattern.compile("(\\d+)[-/ .\n]+(\\d\\d?)[-/ .\n]+(\\d\\d?)"),
    };

    private static final Set<String> MONTHS = Set.of("january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december",
            "jan", "feb", "mar", "apr", "jun", "jul", "aug", "sep", "oct", "nov", "dec", "sept");
    private static final Set<String> SEASONS = Set.of("fall", "winter", "spring", "summer", "autumn", "summertime", "wintertime", "springtime");
    //    private final Pattern exceptionPattern = Pattern.compile("^\\d{1,3}-(point|pack|month|day|hour|degree|pound|French|hydroxy|cm|lead|LEAD|range)$");
    private static final Validator[] VALIDATE_DATE_PATTERN_GROUPS = new Validator[DATE_PATTERNS.length];//code for validating parts matched by corresponding datePatterns[],
    private static final Set<String> COMMON_WORDS_ANY_CASE = Set.of("history", "of", "was", "and", "who", "patient", "the", "her", "him", "doctor");
    private static final Set<String> COMMON_WORDS_CASED = Set.of("in", "or", "is", "a"); //case sensitive incase of actual initials or state abbreviation, ??? AA SA
    private static final Pattern BEGIN = Pattern.compile("^.*?([a-z0-9])", Pattern.CASE_INSENSITIVE);
    private static final Pattern END = Pattern.compile("([a-z0-9])([^a-z0-9]*)$", Pattern.CASE_INSENSITIVE);
    private static int id = 10000;//static so it doesn't get reset each time 

    private static void initializeValidators() {//need something for 21001231000000, should be YYYYMMDDHHMMSS

        VALIDATE_DATE_PATTERN_GROUPS[0] = (mat, annot, newPii, jCas) -> {
            int g1 = Integer.parseInt(mat.group(1));
            int g2 = Integer.parseInt(mat.group(2));
            return (g1 < 1 || g2 < 1 || (g1 > 12 && g2 > 12));
        };
        VALIDATE_DATE_PATTERN_GROUPS[1] = (mat, annot, newPii, jCas) -> {
            int g1 = Integer.parseInt(mat.group(1));
            int g2 = Integer.parseInt(mat.group(2));
            int g3 = Integer.parseInt(mat.group(3));
            return (g1 < 1 || g2 < 1 || g1 > 31 || g2 > 31 || (g1 > 12 && g2 > 12) || (g3 > 99 && g3 < 1880) || g3 > 2200);
        };
        VALIDATE_DATE_PATTERN_GROUPS[2] = (mat, annot, newPii, jCas) -> {
            String month = mat.group(1).toLowerCase();
            int g2 = Integer.parseInt(mat.group(2));
            if (SEASONS.contains(month) && ((g2 > 9 && g2 < 100) || g2 > 1880 && g2 < 2200)) {
                //split into Season and Date
                Util.addPii(newPii, jCas, annot.getBegin(), annot.getBegin() + month.length(), "TEMPORAL", "Season", "P" + id, annot.getMethod(), .0f);
                id++;
                annot.setBegin(annot.getEnd() - mat.group(2).length());
                return false;
            } else return ((!MONTHS.contains(month)) || g2 < 1 || (g2 > 99 && g2 < 1880) || g2 > 2200);
        };
        VALIDATE_DATE_PATTERN_GROUPS[3] = (mat, annot, newPii, jCas) -> {
            String month = mat.group(1).toLowerCase();
            int g2 = Integer.parseInt(mat.group(2));
            int g3 = Integer.parseInt(mat.group(3));
            return ((!MONTHS.contains(month)) || g2 < 1 || g2 > 31 || (g3 > 99 && g3 < 1880) || g3 > 2200);
        };
        VALIDATE_DATE_PATTERN_GROUPS[4] = (mat, annot, newPii, jCas) -> {
            int g1 = Integer.parseInt(mat.group(1));
            int g2 = Integer.parseInt(mat.group(2));
            return (g1 < 1 || g2 < 1 || (g1 > 99 && g1 < 1880) || g1 > 2200 || (g2 > 99 && g2 < 1880) || g2 > 2200);
        };
        VALIDATE_DATE_PATTERN_GROUPS[5] = (mat, annot, newPii, jCas) -> {
            int g1 = Integer.parseInt(mat.group(1));
            String ext = mat.group(2);
            return (g1 > 2200 || (g1 < 1880 && g1 > 99) || (ext != null && g1 > 31));
        };
        VALIDATE_DATE_PATTERN_GROUPS[6] = (mat, annot, newPii, jCas) -> {//why this empty block?
            return false;
        };
        VALIDATE_DATE_PATTERN_GROUPS[7] = (mat, annot, newPii, jCas) -> {
            String month = mat.group(1).toLowerCase();
            if (SEASONS.contains(month)) {
                annot.setPiiSubtype("Season");
                return false;
            }
            return !MONTHS.contains(month);
        };
        VALIDATE_DATE_PATTERN_GROUPS[8] = (mat, annot, newPii, jCas) -> {
            int g2 = Integer.parseInt(mat.group(1));
            String month = mat.group(2).toLowerCase();
            int g3 = Integer.parseInt(mat.group(3));
            return ((!MONTHS.contains(month)) || g2 < 1 || g2 > 31 || (g3 > 99 && g3 < 1880) || g3 > 2200);
        };
        VALIDATE_DATE_PATTERN_GROUPS[9] = (mat, annot, newPii, jCas) -> {
            int g1 = Integer.parseInt(mat.group(1));
            int g2 = Integer.parseInt(mat.group(2));
            int g3 = Integer.parseInt(mat.group(3));
            return (g3 < 1 || g2 < 1 || g3 > 31 || g2 > 31 || (g3 > 12 && g2 > 12) || g1 < 1880 || g1 > 2200);
        };
    }

    public static void filter(List<PiiAnnotation> newPii, JCas jCas, String fileName, String source) {
        LOGGER.debug("{}", () -> " Thread filtering for " + source + " part Begin");
        //trim spacing and punctuation, then pass to appropriate validator (currently Date and Age)
        //List<PiiAnnotation> toRemove = new ArrayList<>();
        Set<Integer> indexToRemove = new HashSet<>();
        for (int checkIndex = 0; checkIndex < newPii.size(); checkIndex++) {
            PiiAnnotation annot = newPii.get(checkIndex);
            String text = annot.getCoveredText();
            Matcher mat = BEGIN.matcher(text);
            if (mat.find()) {
                int b = mat.start(1);
                mat = END.matcher(text);
                if (mat.find()) {
                    int e = mat.end(1);
                    if ((text.charAt(0) != '(' || !annot.getPiiSubtype().equals("PhoneFax")) && (text.charAt(0) != '\'' || (!annot.getPiiSubtype().equals("Date") && !annot.getPiiSubtype().equals("Age")))) {
                        //don't remove beginning ( from phones or beginning ' from Date or Age
                        annot.setBegin(annot.getBegin() + b);
                    }
                    if (text.charAt(text.length() - 1) != '.' || (annot.getPiiType().equals("TEMPORAL") || annot.getPiiType().equals("IDENTIFIER"))) {
                        //only remove trailing periods from TEMPORAL and IDENTIFIER
                        annot.setEnd(annot.getEnd() - (text.length() - e));
                    }
                } else {
                    LOGGER.error("Shouldn't be here for {} from {}, {} - {}", fileName, source, annot.getBegin(), annot.getEnd());
                }
            } else {
                indexToRemove.add(checkIndex);
            }
            //TODO: evaluate the below, so far it removes only 31 FP but they got removed eventually by the voting process. Still, seems good to have fewer
            //will eval again with the new spaced models as it seemed to be a bigger issue with them
            if (COMMON_WORDS_ANY_CASE.contains(annot.getCoveredText().toLowerCase()) ||
                    COMMON_WORDS_CASED.contains(annot.getCoveredText()) ||
                    (annot.getEnd() - annot.getBegin() > 60)) {//some synthesized professions get very long
                String doc = jCas.getDocumentText();
                int start = Integer.max(0, annot.getBegin() - 20);
                int end = Integer.min(doc.length(), annot.getEnd() + 20);
                LOGGER.log(PII_LOG, "{}", () -> fileName + ":    " + source + " Removing COMMON " + annot.getBegin() + "-" + annot.getEnd() + " [" + annot.getCoveredText() + "] " +
                        doc.substring(start, end));
                indexToRemove.add(checkIndex);
                continue;
            }
            if (annot.getPiiSubtype().equals("Date")) {
                if (!isValidDate(newPii, jCas, annot, fileName, source)) {//jCas passed incase a Date is split into Season and Date as jCas needed to create PiiAnnotation
                    indexToRemove.add(checkIndex);
                }
            } else if (annot.getPiiSubtype().equals("Age")) {
                if (!validateAge(annot, fileName, source)) {
                    indexToRemove.add(checkIndex);
                }
            }
        }
        if (!indexToRemove.isEmpty()) {
            Integer[] indicesToRemove = indexToRemove.toArray(new Integer[0]);
            Arrays.sort(indicesToRemove);
            for (int i = indicesToRemove.length - 1; i >= 0; i--) {
                newPii.remove(indicesToRemove[i].intValue());
            }
        }
    }

    private static boolean isValidDate(List<PiiAnnotation> newPii, JCas jCas, PiiAnnotation annot, String fileName, String source) {
        for (int index = 0; index < DATE_PATTERNS.length; index++) {
            Matcher mat = DATE_PATTERNS[index].matcher(annot.getCoveredText());
            if (mat.matches()) {
                try {
                    if (VALIDATE_DATE_PATTERN_GROUPS[index].validate(mat, annot, newPii, jCas)) {
                        int temp = index;
                        LOGGER.log(PII_LOG, "{}", () -> fileName + ":    " + source + " Removing invalid Date " + annot.getBegin() + "-" + annot.getEnd() + " for index " + temp + " [" + annot.getCoveredText() + "] pattern: " + mat.pattern());
                        return false;
                    }
                } catch (NullPointerException | NumberFormatException e) {
                    LOGGER.throwing(e);
                    LOGGER.error("Invalid date threw exception in validation");
                    return false;
                }
                return true;
            }
        }
        return true;
    }

    private static boolean validateAge(PiiAnnotation annot, String fileName, String source) {
        Matcher mat = AGE_SPECIAL.matcher(annot.getCoveredText());//match means it was a valid age, otherwise keep trying
        if (!mat.matches()) {
            mat = VALID_AGES.matcher(annot.getCoveredText());
            if (!mat.matches()) {
                mat = AGE_WITH_MONTHS.matcher(annot.getCoveredText());
                if (mat.matches()) {//remove months
                    annot.setEnd(annot.getBegin() + mat.group(1).length());
                } else {
                    mat = WORD_AGES.matcher(annot.getCoveredText());
                    if (mat.find()) {
                        String part1 = mat.group(1);
                        if (!(DECADES.contains(part1) || ONES.contains(part1))) {
                            LOGGER.log(PII_LOG, "{}", () -> fileName + ":    " + source + " Removing invalid age " + annot.getBegin() + "-" + annot.getEnd() + " " + annot.getCoveredText());
                            return false;
                        } else {
                            String part2 = mat.group(2);
                            if (part2 != null && !(DECADES.contains(part2) || ONES.contains(part2))) {
                                LOGGER.log(PII_LOG, "{}", () -> fileName + ":    " + source + " Removing invalid age " + annot.getBegin() + "-" + annot.getEnd() + " " + annot.getCoveredText());
                                return false;
                            }
                        }
                    } else {
                        LOGGER.log(PII_LOG, "{}", () -> fileName + ":    " + source + " Removing invalid age " + annot.getBegin() + "-" + annot.getEnd() + " " + annot.getCoveredText());
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void initialize() {
        initializeValidators();
        id = 100000;
        /*
        try (FileReader fr = new FileReader(Utilities.getExternalFile(drugsCsvFileParam));
             BufferedReader reader = new BufferedReader(fr);) {// TODO: why use file with useless info in it, clean the file, would remove need for split and lowercase
            String line;
            while ((line = reader.readLine()) != null) {
                String[] temp = line.split(",");
                drugNameSet.add(temp[2].toLowerCase());
            }
        } catch (IOException e) {
            logger.throwing(e);
        }
        */
    }

    private interface Validator {//annot is in case validator wants to change type (to Season) or span (to split it), jCas is if splitting and creating new annotation
        boolean validate(Matcher mat, PiiAnnotation annot, List<PiiAnnotation> newPii, JCas jCas);
    }

//    private void removeExceptionOtherIdNumbers(List<PiiAnnotation> piiAnnotations) {
//        List<PiiAnnotation> toRemove = new ArrayList<>();
//        for (PiiAnnotation annot : piiAnnotations) {
//            if (annot.getPiiSubtype().equals("OtherIDNumber") && exceptionPattern.matcher(annot.getCoveredText()).find()) {
//                logger.log(PII_LOG, "{}", () -> "OtherIDNumber removed for exception: " + annot.getBegin() + "-" + annot.getEnd() + " " + annot.getCoveredText() + " " + annot.getMethod());
//                // annot.removeFromIndexes();
//                toRemove.add(annot);
//            }
//        }
//        for (PiiAnnotation annot : toRemove) {
//            annot.removeFromIndexes();
//            piiAnnotations.remove(annot);
//        }
//    }
//
//    private void logMachineLearningAnnot(PiiAnnotation annot, String intro) {
//        logger.log(PII_LOG, "{}: {} {}-{} ({}) by {}", intro, annot.getPiiSubtype(), annot.getBegin(), annot.getEnd(), annot.getCoveredText(), annot.getMethod());
//    }
}
