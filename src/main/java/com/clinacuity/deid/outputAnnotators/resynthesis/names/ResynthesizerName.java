
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

package com.clinacuity.deid.outputAnnotators.resynthesis.names;

import com.clinacuity.deid.outputAnnotators.resynthesis.Resynthesizer;
import com.clinacuity.deid.type.PiiAnnotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.clinacuity.deid.mains.DeidPipeline.PII_LOG;

public abstract class ResynthesizerName extends Resynthesizer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String MALE_FIRST_FILENAME = "namesFirstMale.obj";
    //public lists for use by email resythesis
    private static final String UNISEX_FIRST_FILENAME = "namesFirstUnisex.obj";
    public static final String[] UNISEX_FIRST_NAMES = readObjectFileToArray(UNISEX_FIRST_FILENAME);
    //    public static final List<String> unisexFirstNames = Collections.unmodifiableList(Arrays.asList(readObjectFileToArray(UNISEX_FIRST_FILENAME)));
    private static final String LAST_FILENAME = "namesLast.obj";
    public static final String[] LAST_NAMES = readObjectFileToArray(LAST_FILENAME);
    private static final String FEMALE_FIRST_FILENAME = "namesFirstFemale.obj";

    //{2,}+ is to avoid single letters that are initials not names, all patterns made possessive by adding + after {} + or *
    private static final Pattern LAST_COMMA_FIRST = Pattern.compile("^([-a-z]{2,}+)\\s*,\\s*([-a-z]{2,}+)$", Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL);
    private static final Pattern LAST_COMMA_FIRST_REST = Pattern.compile("^([-a-z]{2,}+)\\s*,\\s*([-a-z]{2,}+)\\s*,?\\s*([-a-z].*+)$", Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL);

    private static final String SUFFIX_STRING = "(jr\\.?|sr\\.?|junior|senior|ii|iii|iv|v|(?:the\\s+)?(?:second|third|fourth|fifth|sixth|seventh|\\dth))";
    //    private static final String suffixStringAsComponent = "\\s*\\b" + suffixString + "?\\b";
    private static final Pattern SUFFIXES = Pattern.compile(SUFFIX_STRING, Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL);

    private static final Pattern ONE_WORD = Pattern.compile("[-a-z]{2,}+", Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL);
    private static final Pattern INITIALS = Pattern.compile("[A-Z]{2,3}");
    private static final String[] FEMALE_FIRST_NAMES = readObjectFileToArray(FEMALE_FIRST_FILENAME);
    private static final String[] MALE_FIRST_NAMES = readObjectFileToArray(MALE_FIRST_FILENAME);
    private static final Set<String> FEMALE_FIRST_NAMES_SET = Set.of(FEMALE_FIRST_NAMES);
    private static final Set<String> MALE_FIRST_NAMES_SET = Set.of(MALE_FIRST_NAMES);
    private static final Set<String> UNISEX_FIRST_NAMES_SET = Set.of(UNISEX_FIRST_NAMES);
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+", Pattern.MULTILINE);

    protected ResynthesizerName() {
    }

    protected void processNameComponentsType(JCas jCas, String type) {
        Set<String> candidateFirst = new TreeSet<>();
        Set<String> candidateLast = new TreeSet<>();
        Set<String> candidateOther = new TreeSet<>();
        Set<String> candidateInitials = new TreeSet<>();

        //look for mr/dr before single word annotations--likely last names
        String text = jCas.getDocumentText();

        Set<String> names = new TreeSet<>(new lengthComparator());
        for (PiiAnnotation annotation : jCas.getAnnotationIndex(PiiAnnotation.class)) {
            if (annotation.getPiiSubtype().equals(type)) {
                if (!resynthesisMap.contains(annotation.getCoveredText().toLowerCase()) && ONE_WORD.matcher(annotation.getCoveredText()).matches()) {
                    //find if salutation before this one word name
                    int begin = annotation.getBegin() - 1;
                    while (begin > 2 && text.charAt(begin) == ' ') {
                        begin--;
                    }
                    if (begin > 2 && text.charAt(begin) == '.') {
                        begin--;
                    }
                    if (begin > 2 && (text.charAt(begin) == 's' || text.charAt(begin) == 'S')) {//for mrs
                        begin--;
                    }
                    if (begin >= 1 && (text.charAt(begin) == 'r' || text.charAt(begin) == 'R') &&  //mr or dr
                            (text.charAt(begin - 1) == 'd' || text.charAt(begin - 1) == 'D' || text.charAt(begin - 1) == 'm' || text.charAt(begin - 1) == 'M')) {
                        String newValue;
                        String lowerCaseTrim = trimToLetter(annotation.getCoveredText().toLowerCase());
                        do {
                            newValue = LAST_NAMES[ThreadLocalRandom.current().nextInt(LAST_NAMES.length)];
                        } while (newValue.equals(lowerCaseTrim));
                        resynthesisMap.setNewValue(lowerCaseTrim, newValue);
                        candidateLast.add(lowerCaseTrim);//for initials
                    } else {
                        names.add(annotation.getCoveredText());
                    }
                } else {
                    names.add(annotation.getCoveredText());
                }
            }
        }
        if (names.size() == 0) {
            return;
        }
        for (String nameOrig : names) {
            String nameLowerTrim = trimToLetter(nameOrig);
            if (nameLowerTrim.length() == 0) {
                continue;//it has no letters, nothing to do
            }
            if (candidateFirst.contains(nameLowerTrim) || candidateLast.contains(nameLowerTrim) || candidateOther.contains(nameLowerTrim)
                    || candidateInitials.contains(nameLowerTrim) || resynthesisMap.contains(nameLowerTrim)) {
                continue;//already handled this
            }
            Matcher mat;
            if (nameLowerTrim.contains(" ") || nameLowerTrim.contains("\n")) {
                mat = SUFFIXES.matcher(nameLowerTrim);
                if (mat.find()) {
                    candidateOther.add(mat.group(1));
                    nameLowerTrim = trimToLetter(mat.replaceAll(""));
                }
            }
            mat = LAST_COMMA_FIRST.matcher(nameLowerTrim);
            if (mat.matches()) {
                addParts(candidateFirst, candidateLast, candidateOther, candidateInitials, mat.group(2), mat.group(1), null);
            } else {
                mat = LAST_COMMA_FIRST_REST.matcher(nameLowerTrim);
                if (mat.matches()) {
                    addParts(candidateFirst, candidateLast, candidateOther, candidateInitials, mat.group(2), mat.group(1), mat.group(3));
                } else {
                    if (nameLowerTrim.contains(" ") || nameLowerTrim.contains("\n")) {
                        String[] parts = SPACE_PATTERN.split(nameLowerTrim);
                        processPart(parts[0], candidateFirst, candidateInitials);
                        for (int index = 1; index <= parts.length - 2; index++) {
                            processPart(parts[index], candidateOther, candidateInitials);
                        }
                        processPart(parts[parts.length - 1], candidateLast, candidateInitials);
                    } else {
                        mat = INITIALS.matcher(nameOrig);
                        if (mat.matches()) {
                            candidateInitials.add(nameOrig.toLowerCase());
                        } else {
                            //TODO maybe it last size >0 and first size ==0 then put in first, else dual?
                            if (FEMALE_FIRST_NAMES_SET.contains(nameLowerTrim) || MALE_FIRST_NAMES_SET.contains(nameLowerTrim) || UNISEX_FIRST_NAMES_SET.contains(nameLowerTrim)) {
                                candidateFirst.add(nameLowerTrim);
                            } else {
                                //logger.error("Couldn't determine format of " + nameOrig);
                                if ("Provider".equals(type)) {//assume doctor name is last and patient name is first
                                    candidateLast.add(nameLowerTrim);
                                } else {
                                    candidateFirst.add(nameLowerTrim);
                                }
                            }
                        }
                    }
                }
            }
        }
        logDuplicateCandidates(candidateFirst, candidateLast, candidateOther, candidateInitials);
        List<String> initialsOld = new ArrayList<>();
        List<String> initialsNew = new ArrayList<>();
        for (String name : candidateFirst) {
            if (resynthesisMap.contains(name)) {
                continue;
            }
            String newValue = processFirstNameGender(name);
            handleSingleInitial(candidateInitials, name, newValue);
            initialsOld.add(name.substring(0, 1));
            initialsNew.add(newValue.substring(0, 1));
        }
        int index = 0;
        for (String name : candidateOther) {//TODO: should this also check last names? Or not default to unixes?
            if (resynthesisMap.contains(name)) {
                continue;
            }
            String newValue = processFirstNameGender(name);
            handleInitials(candidateInitials, name, newValue, index, initialsOld, initialsNew);
            index++;
        }
        index = 0;
        for (String name : candidateLast) {
            if (resynthesisMap.contains(name)) {
                continue;
            }
            String newValue;
            do {
                newValue = LAST_NAMES[ThreadLocalRandom.current().nextInt(LAST_NAMES.length)];
            } while (newValue.equals(name));
            resynthesisMap.setNewValue(name.toLowerCase(), newValue);
            handleInitials(candidateInitials, name, newValue, index, initialsOld, initialsNew);
            index++;
        }
        for (index = 0; index < initialsOld.size(); index++) {
            if (!resynthesisMap.contains(initialsOld.get(index))) {
                resynthesisMap.setNewValue(initialsOld.get(index), initialsNew.get(index).toUpperCase());
            }
        }
        for (String name : candidateInitials) {
            //need to use the initials form the first/other/last names after creating new values. But what if there are multiples?
            if (!resynthesisMap.contains(name)) {
                if (!initialsNew.isEmpty()) {//name should be lowercase
                    resynthesisMap.setNewValue(name, initialsNew.get(0).substring(0, 1).toUpperCase());
                } else {
                    LOGGER.log(PII_LOG, "{}", () -> "candidate initials " + name + " but nothing in initialsNew");
                }
            }
        }
    }

    private void handleInitials(Set<String> candidateInitials, String name, String newValue, int index, List<String> initialsOld, List<String> initialsNew) {
        handleSingleInitial(candidateInitials, name, newValue);
        if (initialsOld.size() > index) {
            initialsOld.set(index, initialsOld.get(index) + name.substring(0, 1));
            initialsNew.set(index, initialsNew.get(index) + newValue.substring(0, 1));
        } else {
            initialsOld.add(name.substring(0, 1));
            initialsNew.add(newValue.substring(0, 1));
        }
    }

    private void processPart(String part, Set<String> candidateName, Set<String> candidateInitials) {
        if (part.length() == 2 && part.charAt(1) == '.') {//initial
            candidateInitials.add(part.substring(0, 1));
        } else if (part.length() == 1) {
            candidateInitials.add(part);
        } else {
            candidateName.add(part);
        }
    }

    private void handleSingleInitial(Set<String> candidateInitials, String name, String newValue) {
        if (candidateInitials.contains(name.substring(0, 1))) {
            resynthesisMap.setNewValue(name.substring(0, 1), newValue.substring(0, 1));
            candidateInitials.remove(name.substring(0, 1));
        }
    }

    private void logDuplicateCandidates
            (Set<String> candidateFirst, Set<String> candidateLast, Set<String> candidateOther, Set<String> candidateInitials) {
        //duplicates across candidates
        for (String name : candidateFirst) {
            if (candidateInitials.contains(name)) {
                LOGGER.log(PII_LOG, "{}", () -> name + " in both first and initials ");
            }
            if (candidateLast.contains(name)) {
                LOGGER.log(PII_LOG, "{}", () -> name + " in both first and last");
            }
            if (candidateOther.contains(name)) {
                LOGGER.log(PII_LOG, "{}", () -> name + "in both first and other");
            }
        }

        for (String name : candidateLast) {
            if (candidateInitials.contains(name)) {
                LOGGER.log(PII_LOG, "{}", () -> name + "in both last and initials");
            }
            if (candidateOther.contains(name)) {
                LOGGER.log(PII_LOG, "{}", () -> name + " in both last and other");
            }
        }

        for (String name : candidateOther) {
            if (candidateInitials.contains(name)) {
                LOGGER.log(PII_LOG, "{}", () -> name + " in both other and initials");
            }
        }
    }

    private String processFirstNameGender(String name) {
        String[] newSource;
        String newValue;
        if (MALE_FIRST_NAMES_SET.contains(name)) {
            newSource = MALE_FIRST_NAMES;
        } else if (FEMALE_FIRST_NAMES_SET.contains(name)) {
            newSource = FEMALE_FIRST_NAMES;
        } else {
            newSource = UNISEX_FIRST_NAMES;  //default
        }
        do {
            newValue = newSource[ThreadLocalRandom.current().nextInt(newSource.length)];
        } while (newValue.equals(name));
        resynthesisMap.setNewValue(name.toLowerCase(), newValue);
        return newValue;
    }

    protected String getAndUpdateResynthesizedValueInternal(String oldPii) {
        if (resynthesisMap.contains(oldPii.toLowerCase())) {
            return matchCase(oldPii, resynthesisMap.getNewValue(oldPii.toLowerCase()));
        }
        //Use tokenizer to preserve spacing and punctuation
        //issue with suffixes like 'the 7th' that are two tokens but considered one key
        Matcher mat = SUFFIXES.matcher(oldPii);
        if (mat.find()) {
            String loweredSuffix = mat.group(1);
            if (resynthesisMap.contains(loweredSuffix)) {
                oldPii = mat.replaceAll(resynthesisMap.getNewValue(loweredSuffix));
            }
        }
        StringTokenizer tokenizer = new StringTokenizer(oldPii, " \t\n\r\f-.,", true);
        StringBuilder result = new StringBuilder(oldPii.length());
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (resynthesisMap.contains(token.toLowerCase())) {
                result.append(matchCase(token, resynthesisMap.getNewValue(token.toLowerCase())));
            } else {
                result.append(token);
            }
        }

        return result.toString();
    }

    private void addParts
            (Set<String> candidateFirst, Set<String> candidateLast, Set<String> candidateOther, Set<String> candidateInitials,
             String first, String last, String other) {
        candidateFirst.add(first);
        if (SUFFIXES.matcher(last).matches()) {
            if (other != null) {
                String temp = last;
                last = other;
                other = temp;
            } else {
                LOGGER.log(PII_LOG, "Not sure what to do with suffix {} with first {} and no other", last, first);
            }
        }
        candidateLast.add(last);
        if (other != null && other.length() > 0) {
            candidateOther.add(other);
            String initials = first.substring(0, 1) + other.substring(0, 1) + last.substring(0, 1);
            if (!resynthesisMap.contains(initials)) {
                candidateInitials.add(initials);
            }
        } else {
            String initials = first.substring(0, 1) + last.substring(0, 1);
            if (!resynthesisMap.contains(initials)) {
                candidateInitials.add(initials);
            }
        }
    }

    private static class lengthComparator implements Comparator<String> {
        @Override
        public int compare(String left, String right) {
            return right.length() - left.length();
        }
    }
}
