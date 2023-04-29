
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

package com.clinacuity.deid.ae;

import com.clinacuity.deid.outputAnnotators.resynthesis.ResynthStreet;
import com.clinacuity.deid.outputAnnotators.resynthesis.Resynthesizer;
import com.clinacuity.deid.type.PiiAnnotation;
import com.clinacuity.deid.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.clinacuity.deid.mains.DeidPipeline.PII_LOG;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class PostPiiProcessing extends JCasAnnotator_ImplBase {
    private static final Logger logger = LogManager.getLogger();
    /**
     * Priority type list.
     */
    //original List:  sensitive to type names I2B2 vs MUSC, Doctor, Patient, other names, types?
    //private static final Pattern spaceOrCommaOrPeriod = Pattern.compile("\\s|,|\\.");
    //overlapping, annot1 has better priority
    //private static final Pattern streetAll = Pattern.compile("\\s*\\d+\\s+\\w+(\\s+\\w+){0,3}\\s+([sS]t|[sS]treet|[aA]venue|[aA]ve|[Bb]lvd|[bB]oulevard|[sS]uite|[pP]ark|[dD]rive|[dD]r|[lL]ane|[lL]n|[Ww]ay|[Pp]ky|[pP]arkway|[Rr]oute|rt|RT|Rt|[rR]oad|[rR]d|[pP]ass|Square|Sq|[Pp]laza|[lL]ink|[bB]end|[gG]ardens?|[cC]ircle|[rR]ow|[tT]urn|[hH]wy|[hH]ighway|[cC]ir|[cC]ourt|[cC]rossing|[tT]rail|[rR]un|[pP]ike|[tT]errace|Place|[pP]l|[lL]oop|[pP]arade|[aA]lley|ST|STREET|AVENUE|AVE|BLVD|BOULEVARD|SUITE|PARK|DRIVE|DR|LANE|LN|WAY|PKY|PARKWAY|ROUTE|RT|ROAD|RD|PASS|SQUARE|PLAZA|LINK|BEND|GARDENS?|CIRCLE|ROW|TURN|HWY|HIGHWAY|CIR|COURT|CROSSING|TRAIL|RUN|PIKE|TERRACE|PLACE|PL|LOOP|PARADE|ALLEY)\\s*");

    //private static final Pattern streetPost = Pattern.compile("[sS]t|[sS]treet|[aA]venue|[aA]ve|[Bb]lvd|[bB]oulevard|[sS]uite|[pP]ark|[dD]rive|[dD]r|[lL]ane|[lL]n|[Ww]ay|[Pp]ky|[pP]arkway|[Rr]oute|rt|RT|Rt|[rR]oad|[rR]d|[pP]ass|Square|Sq|[Pp]laza|[lL]ink|[bB]end|[gG]ardens?|[cC]ircle|[rR]ow|[tT]urn|[hH]wy|[hH]ighway|[cC]ir|[cC]ourt|[cC]rossing|[tT]rail|[rR]un|[pP]ike|[tT]errace|Place|[pP]l|[lL]oop|[pP]arade|[aA]lley|ST|STREET|AVENUE|AVE|BLVD|BOULEVARD|SUITE|PARK|DRIVE|DR|LANE|LN|WAY|PKY|PARKWAY|ROUTE|RT|ROAD|RD|PASS|SQUARE|PLAZA|LINK|BEND|GARDENS?|CIRCLE|ROW|TURN|HWY|HIGHWAY|CIR|COURT|CROSSING|TRAIL|RUN|PIKE|TERRACE|PLACE|PL|LOOP|PARADE|ALLEY");
    private static final Pattern containsDigits = Pattern.compile("0123456789");
    private static final Map<String, Integer> priorityMap = makePriorityMap();
    private static final Pattern punctuations = Pattern.compile("^[-,():;?/!]|[-,():;?/!]$");
    //private static Pattern cityState = Pattern.compile("(\\w+)\\s*,?\\s*([A-Z][A-Z])\\s*$");
    // Pattern stateAbbreviations = Pattern.compile("AK|AL|AR|AS|AZ|CA|CO|CT|DC|DE|FL|FM|GA|GU|HI|IA|ID|IL|IN|KS|KY|LA|MA|MD|ME|MH|MI|MN|MO|MP|MS|MT|NC|ND|NE|NH|NJ|NM|NV|NY|OH|OK|OR|PA|PR|PW|RI|SC|SD|TN|TX|UT|VA|VI|VT|WA|WI|WV|WY");
    //private Set<String> stateAbbr = Set.of("AK", "AL", "AR", "AS", "AZ", "CA", "CO", "CT", "DC", "DE", "FL", "FM", "GA", "GU", "HI", "IA", "ID", "IL", "IN", "KS", "KY", "LA", "MA", "MD", "ME", "MH", "MI", "MN", "MO", "MP", "MS", "MT", "NC", "ND", "NE", "NH", "NJ", "NM", "NV", "NY", "OH", "OK", "OR", "PA", "PR", "PW", "RI", "SC", "SD", "TN", "TX", "UT", "VA", "VI", "VT", "WA", "WI", "WV", "WY");

    /**
     * temporary Begin and End of annotations
     */
    private String text;
    //private Map<Integer, PiiAnnotation> allMla;
    private int annotId;

    private static Map<String, Integer> makePriorityMap() {//these are not really special and have not been thoroughly explored. Rarely matters.
        return Map.ofEntries(Map.entry("SSN", 1), Map.entry("Patient", 2), Map.entry("Provider", 3), Map.entry("Relative", 4),
                Map.entry("OtherPerson", 5), Map.entry("OtherIDNumber", 6), Map.entry("ElectronicAddress", 7), Map.entry("HealthCareUnitName", 8),
                Map.entry("PhoneFax", 9), Map.entry("Age", 10), Map.entry("Street", 11), Map.entry("City", 12),
                Map.entry("State", 13), Map.entry("Country", 14),
                Map.entry("OtherOrgName", 15), Map.entry("Zip", 16), Map.entry("OtherGeo", 17),
                Map.entry("ClockTime", 18), Map.entry("DayOfWeek", 19), Map.entry("Season", 20), Map.entry("Date", 21),
                Map.entry("Profession", 22));
    }

    public static void setCityOrStreet(PiiAnnotation annot) {
        //Assumptions:
        //1)      if it ends in a street suffix (blvd, st, road, ...) then it is a street
        //otherwise:
        //      if it begins with a number then it is a street
        //      if it begins with a letter then it is a city

        String lowerTrimmed = Resynthesizer.trimToLetterDigit(annot.getCoveredText());
        int lastSpaceIndex = lowerTrimmed.lastIndexOf(' ');
        if (lastSpaceIndex > 0) {
            String suffix = lowerTrimmed.substring(lastSpaceIndex + 1);
            if (ResynthStreet.SUFFIXES_SET.contains(suffix)) {
                annot.setPiiSubtype("Street");
                return;
            }
        }
        int index = 0;
        while (index < lowerTrimmed.length()) {
            if (Character.isDigit(lowerTrimmed.charAt(index))) {
                annot.setPiiSubtype("Street");
                return;
            } else if (Character.isLetter(lowerTrimmed.charAt(index))) {
                annot.setPiiSubtype("City");
                return;
            }
            index++;
        }
        annot.removeFromIndexes(); //only can get here if no digits and no letters
    }

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        logger.debug("PostPiiProcessing Begin");
        text = jCas.getDocumentText();
        //jCas.getAnnotationIndex(PiiAnnotation.class).forEach(piiAnnotations::add);//TODO: forgot how to do this faster with Collection


        //    splitStreetCityStateCountry(jCas);

        findRepeatedName(jCas);

        //allMla = new TreeMap<>();
        //jCas.getAnnotationIndex(PiiAnnotation.class).forEach(annot -> allMla.put(annot.getBegin(), annot));

        //annots2remove.forEach(ann -> ann.removeFromIndexes());

//        if (logger.isDebugEnabled()) {// since this only writes debug code, don't bother iterating unless debugging
//            for (Annotation candLog : jCas.getAnnotationIndex()) {
//                logger.log(PII_LOG,"The following annotation will be retained: {}", candLog.getCoveredText());
//            }
//            jCas.getAnnotationIndex(PiiAnnotation.class).forEach(annot -> logger.log(PII_LOG,"{}\t{}\t{}\t{}", annot.getBegin(), annot.getEnd(),
//                    annot.getCoveredText().replaceAll("\n|\r", ""), annot.getPiiSubtype()));
//        }

        collatePii(jCas);

    }

    private void findRepeatedName(JCas jCas) {
        //if String is annotated as Name then search for that elsewhere in document and add annotations if not present
        //consider also scanning for parts of string and initials (i.e. if Bob Smith is name, then so is Bob or Smith)

        Map<Integer, PiiAnnotation> annotationBegins = new HashMap<>();
        Map<String, String> namesToSubtype = new HashMap<>();
        String id = "P0";
        for (PiiAnnotation annot : jCas.getAnnotationIndex(PiiAnnotation.class)) {
            //if these are in order??, we can just store last one
            if (annot.getId().compareTo(id) > 0) {//should parse out integer and compare as numbers
                id = annot.getId();
            }
            if (annot.getConfidence() >= 1.0) {
                annotationBegins.put(annot.getBegin(), annot);
                // if (annot.getPiiType().equals("NAME")) {  //original
                //tried with all, but made date and times worse on FN and ages much worse on FP
                //all but TEMPORAL reduced FN but a lot of FP (Precision   -1.6%, Recall   +0.4% both HCU, FP from StateCountry)
                //all but TEMPORAL and StateCountry seems to work best
                //if those change a lot then revisit
                if (!"TEMPORAL".equals(annot.getPiiType())
                        && !"Country".equalsIgnoreCase(annot.getPiiSubtype())
                        && !"State".equalsIgnoreCase(annot.getPiiSubtype())) {
                    namesToSubtype.put(annot.getCoveredText(), annot.getPiiSubtype());
                    //adding initials to list results in 68 FP and only 1 more TP: version 14
                    //tried adding parts, version 15, then removing trailing . and only trying strings >=2, all resulted in increased FP and FN
                }
            }
            if (!annot.getPiiSubtype().equals("Date") && punctuations.matcher(annot.getCoveredText()).find()) {
                logger.log(PII_LOG, "{}", () -> "WARN: Annot begin/ends with punctuation: " + annot.getBegin() + " " + annot.getEnd() + " " + annot.getPiiSubtype() + " by " + annot.getMethod() + " (" + annot.getCoveredText() + ")");
            }
        }
        annotId = Integer.parseInt(id.substring(1));
        findNamesFromList(jCas, annotationBegins, namesToSubtype);
        //  findEndingProviders(jCas, "^\\s*([A-Z][A-Z]+)[-:/](\\d+)");
        //  findEndingProviders(jCas, "^\\s*([A-Z][A-Z]+)[-:/]([A-Z][A-Z]+)");
    }


    private void findEndingProviders(JCas jCas, String pat) {
        final Pattern finder = Pattern.compile(pat);
        String[] lines = text.split("\\r*\\n");
        int index = lines.length - 1;
        int offset = text.length();
        while (index >= 0 && lines.length - index < 10) {
            Matcher mat = finder.matcher(lines[index]);
            int start = 0;
            offset = offset - 2 - lines[index].length();//now only -1FP +12FP
            while (mat.find(start)) {
                //need to find in text
                int innerStart = offset;
                Pattern p = Pattern.compile(Pattern.quote(mat.group(1)));
                Matcher m = p.matcher(text);
                while (m.find(innerStart)) {//insensitive: if got -5 FN +95 FP, while got -5 FN +200 FP but root was -11FN  , w/o ins -1 FN +23 FP, also -1 FP ID,-8FN +16 FP
                    int begin = m.start(0);
                    int end = m.end(0);
                    logger.log(PII_LOG, "{}", () -> "Post End " + begin + " " + end + " " + mat.group(1));
                    Util.addPii(jCas, begin, end, "NAME", "Provider", "P" + Integer.toString(annotId), "PostPii-end", 1.0f);
                    innerStart = end + 1;
                }
                p = Pattern.compile(Pattern.quote(mat.group(2)));
                m = p.matcher(text);
                innerStart = offset;
                while (m.find(innerStart)) {
                    int begin = m.start(0);
                    int end = m.end(0);
                    logger.log(PII_LOG, "{}", () -> "Post End " + begin + " " + end + " " + mat.group(2));
                    Util.addPii(jCas, begin, end, "CONTACT_INFORMATION", "PhoneFax", "P" + Integer.toString(annotId), "PostPii-end", 1.0f);
                    innerStart = end + 1;
                }
                start = mat.end(1) + 1;
            }
            index--;
        }
    }

    private void findNamesFromList(JCas jCas, Map<Integer, PiiAnnotation> annotationBegins, Map<String, String> namesToSubtype) {
        int start;
        Collection<String> names = namesToSubtype.keySet();
        for (String name : names) {//if name has ( in it, causes problem, Pattern.quote?
            Matcher finder = null;
            try {
                finder = Pattern.compile("\\b" + name + "\\b", Pattern.CASE_INSENSITIVE).matcher(text);//best combo is not using Pattern.quote and using CASE_INSENSITIVE
            } catch (Exception e) {
                logger.log(PII_LOG, "ERROR: name [[{}]] couldn't be made into a pattern, so skipping finding repeated forms of it.", name);
                continue;
            }
            while (finder.find()) {
                start = finder.start();
                if (!annotationBegins.containsKey(start)) {//found name that isn't already annotated, need to check coverage/overlap
                    annotId++;
                    //TODO: need method to include original source(s)
                    Util.addPii(jCas, start, start + name.length(), "NAME", namesToSubtype.get(name), "P" + annotId, "PostPii", 1.0f);
                    int s = start;
                    logger.log(PII_LOG, "{}", () -> "Added PII " + s + "-" + (s + name.length()) + " " + namesToSubtype.get(name) + " " + name);
                } else {//already annotated, but is it a name? if not should we add name and remove other or leave it?
                    String old = annotationBegins.get(start).getPiiSubtype();
                    if (!"Patient".equals(old) && !old.equals(namesToSubtype.get(name))) {
                        PiiAnnotation annotation = annotationBegins.get(start);
                        logger.debug("{}", () -> "Changing type from " + annotation.getPiiSubtype() + " to " + namesToSubtype.get(name) + " for " + annotation.getBegin() + "-" + annotation.getEnd());
                        annotation.setPiiSubtype(namesToSubtype.get(name));

                    }
                }
            }
        }
    }

    private void collatePii(JCas jCas) {//new annotations may overlap, consider joining adjacent of same type separated only by whitespace ( or punctuation?)
        List<PiiAnnotation> allPii = new ArrayList<>();

        for (PiiAnnotation annot : jCas.getAnnotationIndex(PiiAnnotation.class)) {
            if (annot.getConfidence() >= 1.0) {//in case regex isn't first annotator to run
                allPii.add(annot);
            }
        }

        Set<Integer> indexToRemove = new HashSet<>();
        //check overlaps, if exact then just remove, else expand to largest and remove other
        boolean flag = true;
        while (flag) {//made no difference

            flag = false;
            for (int checkIndex = 0; checkIndex < allPii.size(); checkIndex++) {
                if (indexToRemove.contains(checkIndex)) {//skip if already removed it
                    continue;
                }
                PiiAnnotation older = allPii.get(checkIndex);

                for (int newerIndex = 0; newerIndex < allPii.size(); newerIndex++) {//check all in case expansion causes new overlap
                    if (indexToRemove.contains(newerIndex) || newerIndex == checkIndex) {//skip if already removed it or same annotation
                        continue;
                    }
                    PiiAnnotation newer = allPii.get(newerIndex);
                    if (older.getBegin() == newer.getBegin() && older.getEnd() == newer.getEnd()) {
                        if (newer.getMethod().equals("Structured")) {//replace older with newer and remove newer
                            older = newer;
                            indexToRemove.add(newerIndex);
                        } else if (older.getMethod().equals("Structured")) {
                            indexToRemove.add(newerIndex);
                        } else if (older.getPiiSubtype().equals(newer.getPiiSubtype())) {
                            indexToRemove.add(newerIndex);
                            older.setMethod(newer.getMethod() + "-" + older.getMethod());
                            // logger.log(PII_LOG, "{}", () -> "removing exact overlap PII " + newer.toString());
                        } else {
                            String olderString = older.toString();
                            if (older.getPiiSubtype().equals("Date") && containsDigits.matcher(older.getCoveredText()).find()) {//if Date contains some digits, probably correct
                                logger.log(PII_LOG, "{}", () -> "removing newer overlap PII: " + newer.toString() + " because older Date contained digits: " + olderString);
                                indexToRemove.add(newerIndex);
                            } else if (newer.getPiiSubtype().equals("Date") && containsDigits.matcher(newer.getCoveredText()).find()) {//if Date contains some digits, probably correct
                                logger.log(PII_LOG, "{}", () -> "removing older overlap PII: " + olderString + " because newer Date contained digits: " + newer.toString());
                                older = newer;
                                flag = true;
                                indexToRemove.add(newerIndex);
                            } else {//priority
                                if (priorityMap.get(older.getPiiSubtype()) < priorityMap.get(newer.getPiiSubtype())) {//lower priority number is more important
                                    logger.log(PII_LOG, "{}", () -> "removing newer overlap PII: " + newer.toString() + " because older has better priority: " + olderString);
                                    indexToRemove.add(newerIndex);
                                } else {
                                    logger.log(PII_LOG, "{}", () -> "removing older overlap PII: " + olderString + " because newer has better priority: " + newer.toString());
                                    older = newer;
                                    flag = true;
                                    indexToRemove.add(newerIndex);
                                }
                            }
                        }
                    } else if (older.getBegin() <= newer.getBegin() && older.getEnd() > newer.getBegin() ||
                            older.getBegin() > newer.getBegin() && (older.getEnd() <= newer.getEnd() || older.getBegin() < newer.getEnd())) {
                        //currently this does not combine situation like 5-10 and 10-18, intuition is that regex would have matched if it should have been joined
                        PiiAnnotation olderCopy = older;
                        if (newer.getMethod().equals("Structured")) {//replace older with newer and remove newer
                            older=newer;
                            indexToRemove.add(newerIndex);
                        } else if (older.getMethod().equals("Structured")) {
                            indexToRemove.add(newerIndex);
                        } else if (older.getPiiSubtype().equals(newer.getPiiSubtype())) {
                            logger.log(PII_LOG, "{}", () -> "removing overlapping PII (" + newer.getCoveredText() + ") type: " + newer.getPiiSubtype() + "\n\texpanding: " +
                                    olderCopy.getBegin() + "-" + olderCopy.getEnd() + " to " + min(olderCopy.getBegin(), newer.getBegin()) + "-" + max(olderCopy.getEnd(), newer.getEnd()));
                            older.setBegin(min(older.getBegin(), newer.getBegin()));
                            older.setEnd(max(older.getEnd(), newer.getEnd()));
                            indexToRemove.add(newerIndex);
                        } else {
                            //Elbert within Elberton-Elbert Hospital 0176_gs, 200-03, 174-0?
                            //TODO: this needs validate, but not sure best approach
                            logger.log(PII_LOG, "{}", () -> "PII overlap with mismatch types, kept larger span " + olderCopy.toString() + "\n and " + newer.toString());
                            //could check if word hospital is present in HCU?
                            if (older.getEnd() - older.getBegin() > newer.getEnd() - newer.getBegin()) {
                                indexToRemove.add(newerIndex);
                            } else {
                                older = newer;
                                flag = true;
                                indexToRemove.add(newerIndex);//removing older index could mess up loop
                            }
                        }
                    }
                }
            }
        }
        for (int index : indexToRemove) {
            allPii.get(index).removeFromIndexes();
        }
    }

    private void logMachineLearningAnnot(PiiAnnotation annot, String intro) {
        logger.log(PII_LOG, "{}: {} {}-{} ({}) by {}", intro, annot.getPiiSubtype(), annot.getBegin(), annot.getEnd(), annot.getCoveredText(), annot.getMethod());
    }
/*
    private boolean handleCityStateZip(PiiAnnotation zipcode) {
        Matcher mat;
        if (zipcode.getPiiSubtype().equals("Zipcode")) {
            String before = doc.substring(Math.max(0, zipcode.getBegin() - 100), zipcode.getBegin());
            mat = cityState.matcher(before);
            if (mat.find() && stateAbbr.contains(mat.group(2))) {
               logger.log(PII_LOG, "Annot1 Zip, context looks like City,State Zip {} ({})", mat.group(2), before);
                //make sure city and state make it through
                //get covering?
                int pos = annotationListClass.indexOf(zipcode);
                boolean foundState = false;
                boolean foundCity = false;
                int stateOffset = before.length() - mat.end(2);
                int stateBegin = zipcode.getBegin() - stateOffset - 2;
                int cityOffset = before.length() - mat.end(1);
                int cityEnd = zipcode.getBegin() - cityOffset;

                for (int index = pos - 1; index >= 0 && !foundState; index--) {//this isn't always finding it 116-04
                    PiiAnnotation temp = annotationListClass.get(index);
                    if (temp.getEnd() <= zipcode.getEnd() && temp.getBegin() > zipcode.getBegin() - 5) {
                        foundState = true;
                        if (temp.getPiiSubtype().equals("StateCountry")) {
                            //make sure this one succeeds through cleaning
                        } else {
                            //kill it, should be a state
                            _annotBegin.put(temp, temp.getEnd());
                            createPiiAnnotation(stateBegin, stateBegin + 2, "StateCountry");
                        }
                    } else if (temp.getEnd() <= stateBegin && temp.getEnd() >= stateBegin - 6) {
                        //looking for a StreetCity that ends before State, but only a little before
                        foundCity = true;
                        if (temp.getPiiSubtype().equals("StreetCity")) {
                            //make sure it succeeds
                        } else {//issue is multiword city
                            _annotBegin.put(temp, temp.getEnd());//kill it
                            createStreetAndCityAnnotations(before, stateBegin, cityEnd, zipcode.getBegin());
                        }
                    }
                }
                //if no annotation to replace, then just add
                if (!foundState) {
                    createPiiAnnotation(stateBegin, stateBegin + 2, "StateCountry");
                }
                if (!foundCity) {
                    createStreetAndCityAnnotations(before, stateBegin, cityEnd, zipcode.getBegin());
                }
                return true;
            }
        }
        return false;
    }

    private void createStreetAndCityAnnotations(String before, int stateBegin, int cityEnd, int zipBegin) {
        String[] parts = before.split("\\n|\\s\\s+");
        //last should be state then before that then before that city, but 1 or more words?
        //before that may be the street or a name (because of column orientation in original document)
        logger.log(PII_LOG,"PARTS: {} of {}", parts.length, Arrays.toString(parts));
        int offset = 0;
        if (parts.length < 1) {
            return;
        }
        if (!parts[parts.length - 1].contains(" ")) {//only has state, remove it
            offset = 1;
        }
        int posStreet = before.lastIndexOf(parts[parts.length - 1]);
        int streetBegin = zipBegin - (before.length() - posStreet);
        createPiiAnnotation(streetBegin, cityEnd, "StreetCity");
        //try street now
        if (parts.length > 1 + offset && streetAll.matcher(parts[parts.length - 2 - offset]).matches()) {
            posStreet = before.lastIndexOf(parts[parts.length - 2 - offset]);
            streetBegin = zipBegin - (before.length() - posStreet);//city or zip end?
            createPiiAnnotation(streetBegin, streetBegin + parts[parts.length - 2 - offset].length(), "StreetCity");
        } else if (parts.length > 2 + offset && streetAll.matcher(parts[parts.length - 3 - offset]).matches()) {
            posStreet = before.lastIndexOf(parts[parts.length - 3 - offset]);
            streetBegin = zipBegin - (before.length() - posStreet);
            createPiiAnnotation(streetBegin, streetBegin + parts[parts.length - 3 - offset].length(), "StreetCity");
        }//else might be a name

    }

    private void createPiiAnnotation(int begin, int end, String type) {
        if (begin >= end || begin < 0 || end >= doc.length()) {
            logger.error("Tried to make bad machine annotation {}-{} of {}", begin, end, type);
            return;
        }
        if (allMla.containsKey(begin)) {
            PiiAnnotation old = allMla.get(begin);
            String t = old.getCoveredText();
            if (old.getPiiSubtype().equals(type)) {
                return;
            } else {
                logger.log(PII_LOG,New overlaps old: {}-{} type {} from {} new {}-{} type {}", old.getBegin(), old.getEnd(), old.getPiiSubtype(), old.getMethod(), begin, end, type);
            }
        }
        PiiAnnotation newLocation = new PiiAnnotation(jCasClass, begin, end);
        newLocation.setPiiSubtype(type);
        newLocation.setMethod("Cleaner addition from city, ST zip");
        newLocation.setPiiType("Pii");
        newLocation.addToIndexes();
        allMla.put(begin, newLocation);
    }

    private boolean handleContactId(PiiAnnotation annot1, PiiAnnotation annot2) {
        final Pattern mrn = Pattern.compile("[mM][Rr][Nn]?#?:?\\s*$");
        if (annot2.getPiiSubtype().equals("PhoneNumber") && annot1.getPiiSubtype().equals("OtherIDNumber")) {
            String context = doc.substring(Math.max(0, annot2.getBegin() - 10), annot2.getBegin());
            if (mrn.matcher(context).find()) {
                logger.debug("Context implies id number not contact A");
                _annotBegin.put(annot2, annot2.getEnd());
                return true;
            }

        } else if (annot1.getPiiSubtype().equals("PhoneNumber") && annot2.getPiiSubtype().equals("OtherIDNumber")) {
            String context = doc.substring(Math.max(0, annot2.getBegin() - 10), annot2.getBegin());
            if (mrn.matcher(context).find()) {
                logger.debug("Context implies id number not contact B");
                _annotBegin.put(annot1, annot1.getEnd());
                return true;
            }
        }
        return false;
    }

    private boolean compareWithRegex(PiiAnnotation annot1, PiiAnnotation annot2) {
        // if (annot1.getPiiSubtype().equals("StreetCity")) {
        logMachineLearningAnnot(annot1, "Annot1");
        logMachineLearningAnnot(annot2, "Annot2");
        if (handleContactId(annot1, annot2)) {
            return true;
        }
        if (handleCityStateZip(annot1)) {
            _annotBegin.put(annot2, annot2.getEnd());//invalidate the other annot, what if other is 9 digit vs 5 digit zip?
            return true;
        }
        if (handleCityStateZip(annot2)) {
            _annotBegin.put(annot1, annot1.getEnd());//invalidate the other annot, what if other is 9 digit vs 5 digit zip?
            return true;
        }
        return false;
    }

    private String stringTempPiiAnnotation(PiiAnnotation annotation) {
        int begin = _annotBegin.get(annotation);
        int end = _annotEnd.get(annotation);
        return (String.format("%5d - %5d %25s \t[%s]", begin, end, annotation.getPiiSubtype(),
                annotation.getCoveredText().substring(begin - annotation.getBegin(), end - annotation.getBegin())));
    }
*/
}
