
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

package com.clinacuity.deid.ae.regex.impl;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

import com.clinacuity.deid.ae.regex.Annotation;
import com.clinacuity.deid.ae.regex.Concept;
import com.clinacuity.deid.ae.regex.Feature;
import com.clinacuity.deid.ae.regex.Rule;
import com.clinacuity.deid.type.PiiAnnotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.annotator.AnnotatorContextException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.clinacuity.deid.mains.DeidPipeline.PII_LOG;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * There are two UIMA fit parameters for what xml file contains the regular expressions and types to use.
 * REGEX_CONCEPTS_FILE is a single string while REGEX_CONCEPTS_FILES is assumed to be list of strings
 * If the single is null then the list is used otherwise it creates a list of just that file
 * so that the rest of the code is the same.
 * Currently, we have only ever used 1 xml file with regex in them.
 * This will work with UIMAfit or the xml file
 */
public class RegExAnnotatorThread implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    PiiAnnotation dummyPii;
    /**
     * Invokes this annotator's analysis logic. This annotator uses the java regular expression package to find annotations using the regular expressions
     * defined by its configuration parameters.
     */
    int idNumber = 0;
    private Concept[] regexConcepts;
    //   private boolean lastRuleExceptionMatch = false;
//    private AnnotationFS lastRuleExceptionAnnotation = null;
    private NumberFormat floatNumberFormat = null;
    private NumberFormat integerNumberFormat = null;
    private JCas jCas;
    private CountDownLatch doneSignal;
    private List<PiiAnnotation> newPii = new ArrayList<>();
    //private String fileName;

    /**
     * Performs any startup tasks required by this annotator. This implementation reads the configuration parameters and compiles the regular expressions.
     */

    public RegExAnnotatorThread(Concept[] regexConcepts, NumberFormat floatNumberFormat, NumberFormat integerNumberFormat, JCas jCas, CountDownLatch doneSignal, List<PiiAnnotation> newPii, String fileName) {
        this.floatNumberFormat = floatNumberFormat;
        this.integerNumberFormat = integerNumberFormat;
        this.jCas = jCas;
        this.doneSignal = doneSignal;
        this.newPii = newPii;
        this.regexConcepts = regexConcepts;
        dummyPii = new PiiAnnotation(jCas);
        //this.fileName = fileName;
    }

    /**
     * @param context
     * @param param
     * @param defaultValue
     * @return returns the boolean parameter value
     * @throws AnnotatorContextException
     */
    private static String[] safeGetConfigParameterStringArrayValue(UimaContext context, String param, String[] defaultValue) {
        String[] array = (String[]) context.getConfigParameterValue(param);
        if (array != null && array.length > 0) {
            return array;
        }
        return defaultValue;
    }

    /**
     * Acquires references to CAS Type and Feature objects that are later used during the run method.
     */
    public void typeSystemInit(TypeSystem aTypeSystem) throws AnalysisEngineProcessException {
        // initialize types for the regex concepts
        if (regexConcepts != null) {
            try {
                for (int i = 0; i < regexConcepts.length; i++) {
                    ((Concept_impl) regexConcepts[i]).typeInit(aTypeSystem);
                }
            } catch (ResourceInitializationException ex) {
                throw new RegexAnnotatorProcessException(ex);
            }
        }
    }

    public void run() {
        LOGGER.debug("RegExAnnotator Thread begin");
        newPii.clear();
        idNumber = 0;
        String matchValue = null;
        //AnnotationFS currentAnnot = null;
        //TODO: is there a way around getCas for DocumentAnnotation? DocumentInformationAnnotation doesn't have its end set
        org.apache.uima.jcas.tcas.Annotation currentAnnot = jCas.getCas().getDocumentAnnotation();

        // get the specified feature path value from the current annotation to run the regex on
        matchValue = jCas.getDocumentText();//conceptRules[ruleCount].getMatchTypeFeaturePath().getValue(currentAnnot);
        if (matchValue == null || matchValue.length() == 0) {
            return;
        }
        try {
            // iterate over all concepts one after the other to process them
            for (Concept regexConcept : regexConcepts) {
                // list of all annotation that must be added to the CAS for this concept

                // get the rules for the current concept
                Rule[] conceptRules = regexConcept.getRules();
                boolean foundMatch = false;

                for (int ruleCount = 0; ruleCount < conceptRules.length; ruleCount++) {
                    Pattern pattern = conceptRules[ruleCount].getRegexPattern();
                    Matcher matcher = pattern.matcher(matchValue);

                    // check the match strategy we have for this rule
                    // MatchStrategy - MATCH_ALL
                    if (conceptRules[ruleCount].getMatchStrategy() == Rule.MATCH_ALL) {
                        int pos = 0;
                        while (matcher.find(pos)) {
                            // create annotations and features
                            processConceptInstructions(matcher, currentAnnot, matchValue, regexConcept, ruleCount);

                            // set match found
                            foundMatch = true;
                            //}
                            // set start match position for the next match to the
                            // current end match position
                            pos = matcher.end();

                        }
                    }
                    // MatchStrategy - MATCH_COMPLETE
                    else if (conceptRules[ruleCount].getMatchStrategy() == Rule.MATCH_COMPLETE) {
                        if (matcher.matches()) {
                            // create annotations and features
                            processConceptInstructions(matcher, currentAnnot, matchValue, regexConcept, ruleCount);
                            // set match found
                            foundMatch = true;
                        }
                    }
                    // MatchStrategy - MATCH_FIRST
                    else if (conceptRules[ruleCount].getMatchStrategy() == Rule.MATCH_FIRST) {
                        if (matcher.find()) {
                            // create annotations and features
                            processConceptInstructions(matcher, currentAnnot, matchValue, regexConcept, ruleCount);
                            // set match found
                            foundMatch = true;
                        }
                    }

                    if (foundMatch && !regexConcept.processAllConceptRules()) { // check setting of processAllConceptRules to decide if we go on with the next
                        // rule or not
                        // we found a match for the current rule and we don't want go on with further rules of this concept
                        break;
                    }
                }

                // reset last rule exception annotation since we move to the next rule
                // and everything is new
                //lastRuleExceptionAnnotation = null;
            }
            collatePii();

            //TODO: I don't think there is any need to actually filter for Regex
            //FilterForThreads.filter(newPii, jCas, fileName, "Regex");
        } catch (Exception e) {
            LOGGER.throwing(e);
        }
        LOGGER.debug("Regex Thread done");
        doneSignal.countDown();
    }

    private void collatePii() {
        //regex can create overlapping PIIAnnotations when multiple patterns match same spot
        //used for setting ID, seems to be needed in Young's code?
        Set<Integer> indexToRemove = new HashSet<>();
        //check overlaps, if exact then just remove, else expand to largest and remove other
        for (int checkIndex = 0; checkIndex < newPii.size(); checkIndex++) {
            if (indexToRemove.contains(checkIndex)) {//skip if already removed it
                continue;
            }
            PiiAnnotation older = newPii.get(checkIndex);

            for (int i = 0; i < newPii.size(); i++) {//check all in case expansion causes new overlap
                if (indexToRemove.contains(i) || i == checkIndex) {//skip if already removed it or same annotation
                    continue;
                }
                PiiAnnotation newer = newPii.get(i);
                if (older.getBegin() == newer.getBegin() && older.getEnd() == newer.getEnd()) {
                    //current regex wouldn't allow different types to have exact overlap
                    indexToRemove.add(i);
                    LOGGER.log(PII_LOG, "{}", () -> "removing exact overlap regex " + newer.toString());
                } else if (older.getBegin() <= newer.getBegin() && older.getEnd() > newer.getBegin() ||
                        older.getBegin() > newer.getBegin() && (older.getEnd() <= newer.getEnd() || older.getBegin() < newer.getEnd())) {
                    //currently this does not combine situation like 5-10 and 10-18, intuition is that regex would have matched if it should have been joined
                    if (older.getPiiSubtype().equals(newer.getPiiSubtype())) {
                        LOGGER.log(PII_LOG, "{}", () -> "removing overlapping regex " + newer.getCoveredText() + " type: " + newer.getPiiSubtype() +
                                ", \n\texpanding: " + older.getBegin() + "-" + older.getEnd() + ", " + min(older.getBegin(), newer.getBegin()) + "-" + max(older.getEnd(), newer.getEnd()));
                        older.setBegin(min(older.getBegin(), newer.getBegin()));
                        older.setEnd(max(older.getEnd(), newer.getEnd()));
                        indexToRemove.add(i);
                    } else {
                        LOGGER.log(PII_LOG, "{}", () -> "Regex overlap with mismatch types, not sure what to do " + older.toString() + "\n and: " + newer.toString());
                    }
                }
            }
        }
        Integer[] toRemove = indexToRemove.toArray(new Integer[0]);
        Arrays.sort(toRemove);
        for (int i = toRemove.length - 1; i >= 0; i--) {
            newPii.remove(toRemove[i].intValue());
        }
    }

    /**
     * The createAnnotations method creates the annotations and features for the given rule matches.
     *
     * @param matcher      current regex matcher
     * @param annot        match type annotation
     * @param matchingText text that is used to match
     * @param concept      current concept
     * @param ruleIndex    current rule index
     */
    private void processConceptInstructions(Matcher matcher, AnnotationFS annot, String matchingText, Concept concept,
                                            int ruleIndex) throws RegexAnnotatorProcessException, CASException {
        // get annotations that should be created
        Annotation[] annotations = concept.getAnnotations();
        for (Annotation annotation : annotations) {
            // get local start and end position of the match in the matchingText
            int localStart = annotation.getBegin().getMatchPosition(matcher);
            int localEnd = annotation.getEnd().getMatchPosition(matcher);

            // check if match group positions are valid. If they are invalid,
            // the match group is available but has no matching content
            if (localStart == -1 || localEnd == -1) {
                // match group positions are invalid, so we cannot create the annotation
                continue;
            }

            // create annotation start and begin positions dependent of the rule matching
            if (concept.getRules()[ruleIndex].isFeaturePathMatch()) {
                // we match a feature path, use a source annotation boundaries for the annotation that is created
                localStart = annot.getBegin();
                localEnd = annot.getEnd();
            } else {
                // we match no feature path, make positions absolute to the document text -> add match type annotation offset.
                localStart = annot.getBegin() + localStart;
                localEnd = annot.getBegin() + localEnd;
            }
            // create annotation for this match
            PiiAnnotation newPiiAnnotation = new PiiAnnotation(jCas, localStart, localEnd);

            // get features for the current annotation
            Feature[] features = annotation.getFeatures();

            for (Feature feature : features) {
                int type = feature.getType();

                // check if we have a reference feature or not
                if (type == Feature.FLOAT_FEATURE || type == Feature.INTEGER_FEATURE || type == Feature.STRING_FEATURE) {
                    // we have no reference feature replace match groups in the feature value
                    String featureValue = feature.getValue();
                    // TODO: should these ParseExceptions re-throw?
                    // set feature value at the annotation in dependence of the feature type
                    if (type == Feature.FLOAT_FEATURE && featureValue != null) {
                        try {
                            Number number = floatNumberFormat.parse(featureValue);
                            newPiiAnnotation.setFloatValue(newPiiAnnotation.getType().getFeatureByBaseName(feature.getName()), number.floatValue());
                        } catch (ParseException ex) {
                            LOGGER.warn("number_format_conversion {} {} float", featureValue, feature.getFeature().getName());
                        }
                    } else if (type == Feature.INTEGER_FEATURE && featureValue != null) {
                        try {
                            Number number = integerNumberFormat.parse(featureValue);
                            newPiiAnnotation.setIntValue(newPiiAnnotation.getType().getFeatureByBaseName(feature.getName()), number.intValue());
                        } catch (ParseException ex) {
                            LOGGER.warn("number_format_conversion {} {} integer", featureValue, feature.getFeature().getName());
                        }
                    }
                    if (type == Feature.STRING_FEATURE) {
                        newPiiAnnotation.setStringValue(newPiiAnnotation.getType().getFeatureByBaseName(feature.getName()), featureValue);
                    }
                } else if (type == Feature.RULEID_FEATURE) {
                    String ruleId = concept.getRules()[ruleIndex].getId();
                    newPiiAnnotation.setStringValue(newPiiAnnotation.getType().getFeatureByBaseName(feature.getName()), ruleId);
                } else if (type == Feature.CONFIDENCE_FEATURE) {
                    float confidence = concept.getRules()[ruleIndex].getConfidence();
                    newPiiAnnotation.setConfidence(confidence);
                }
            }

            newPiiAnnotation.setId("P" + idNumber);
            idNumber++;
            this.newPii.add(newPiiAnnotation);

        } // end of annotation processing
    }

}

