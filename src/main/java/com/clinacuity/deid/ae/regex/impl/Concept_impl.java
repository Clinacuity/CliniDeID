
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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.clinacuity.deid.ae.regex.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.resource.ResourceInitializationException;

import com.clinacuity.deid.ae.regex.Annotation;
import com.clinacuity.deid.ae.regex.Concept;
import com.clinacuity.deid.ae.regex.Feature;
import com.clinacuity.deid.ae.regex.Rule;

/**
 *
 */
public class Concept_impl implements Concept {
    private static final Logger logger = LogManager.getLogger();

    private ArrayList<Annotation> annotations;

    private Annotation[] annotArray;

    private String name;

    private ArrayList<Rule> rules;

    private boolean processAllRules;

    private Rule[] ruleList;

    /**
     * @param name
     */
    public Concept_impl(String name, boolean processAllRules) {
        this.name = name;
        this.processAllRules = processAllRules;
        this.annotations = new ArrayList<>();
        this.rules = new ArrayList<>();
        this.ruleList = null;
        this.annotArray = null;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Rule#addAnnotation(org.apache.uima.annotator.regex.Annotation)
     */
    public void addAnnotation(Annotation aAnnotation) {
        this.annotations.add(aAnnotation);
        this.annotArray = this.annotations.toArray(new Annotation[0]);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Rule#getInstructions()
     */
    public Annotation[] getAnnotations() {
        return this.annotArray;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Concept#addRule(org.apache.uima.annotator.regex.Rule)
     */
    public void addRule(Rule aRule) {
        this.rules.add(aRule);

        // create ruleList array
        this.ruleList = this.rules.toArray(new Rule[0]);
        // sort rules array by confidence
        Arrays.sort(this.ruleList, new RuleComparator());
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Concept#getName()
     */
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Concept#getRules()
     */
    public Rule[] getRules() {
        return this.ruleList;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Concept#processAllConceptRules()
     */
    public boolean processAllConceptRules() {
        return this.processAllRules;
    }

    /**
     * @param ts
     * @throws ResourceInitializationException
     */
    public void typeInit(TypeSystem ts) throws ResourceInitializationException {

        // initialize all rules for this concept
        for (int i = 0; i < this.ruleList.length; i++) {
            ((Rule_impl) this.ruleList[i]).typeInit(ts);
        }

        // initialize all annotations for this concept
        Annotation[] annots = getAnnotations();
        for (int i = 0; i < annots.length; i++) {
            ((Annotation_impl) annots[i]).typeInit(ts);
        }
    }

    /**
     * initialize the concept and the referred rules and annotations.
     *
     * Also do some additional syntax checking.
     *
     * @throws RegexAnnotatorConfigException
     */
    public void initialize() throws RegexAnnotatorConfigException {

        // get a list of annotations
        Annotation[] annots = getAnnotations();

        // initialize all rules for this concept
        for (int i = 0; i < this.ruleList.length; i++) {
            ((Rule_impl) this.ruleList[i]).initialize();
        }

        // initialize all annotations for this concept
        for (int i = 0; i < annots.length; i++) {
            ((Annotation_impl) annots[i]).initialize();
        }

        // check duplicate rule IDs within the same concept
        // store ruleIDs for one concept
        HashSet<String> ruleIds = new HashSet<>(this.ruleList.length);
        for (int x = 0; x < this.ruleList.length; x++) {
            String ruleID = this.ruleList[x].getId();
            // if no ruleID was specified, skip rule
            if (ruleID == null) {
                continue;
            }
            // check if ruleID already exist for this concept
            if (ruleIds.contains(ruleID)) {
                logger.warn("duplicate rule id {} {}", ruleID, name);
            } else {
                // ruleID does not exist for this concept, add it to the ruleID
                // list
                ruleIds.add(ruleID);
            }
        }

        // check if annotation IDs are available in case of reference type
        // features and
        // check if they are unique within the concept
        HashSet<String> referenceIds = new HashSet<>();
        HashSet<String> annotationIds = new HashSet<>();
        for (int a = 0; a < annots.length; a++) {
            String annotID = annots[a].getId();
            // check annotation ID if available
            if (annotID != null) {
                // if annotation ID already exists for the current concept
                // throw an exception, annotation IDs must be unique for a
                // concept
                if (annotationIds.contains(annotID)) {
                    throw new RegexAnnotatorConfigException("regex_annotator_error_duplicate_annotation_id",
                            new Object[] { annotID, this.name });
                } else {
                    annotationIds.add(annotID);
                }
            }
            Feature[] features = annots[a].getFeatures();
            for (int f = 0; f < features.length; f++) {
                // check if the feature is a reference feature
                if (features[f].getType() == Feature.REFERENCE_FEATURE) {
                    // add the annotation ID to the referenceIDs list to check
                    // if this annotation ID is available and unique for the
                    // concept
                    referenceIds.add(features[f].getValue());

                    // check annotation ID for the current annotation. Annotations
                    // that have a reference type feature must also have a valid
                    // annotation ID
                    if (annotID == null) {
                        throw new RegexAnnotatorConfigException("regex_annotator_error_annotation_id_not_available",
                                new Object[] { this.name });
                    }
                }
            }
        }

        // check if all referred annotation IDs are available
        Iterator<String> refIterator = referenceIds.iterator();
        while (refIterator.hasNext()) {
            String refID = refIterator.next();

            // check if refID is available in the anntoationIDs list
            // if it is not available, throw an exception
            if (!annotationIds.contains(refID)) {
                throw new RegexAnnotatorConfigException("regex_annotator_error_referred_annotation_id_not_available",
                        new Object[] { refID, this.name });
            }
        }
        logger.debug("Concept_impl initialized");

    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer buffer = new StringBuffer();
        buffer.append("Concept ");
        if (this.name != null) {
            buffer.append(this.name);
        }
        buffer.append("\nProcessAllConceptRules: " + this.processAllRules + "\n");

        if (this.ruleList.length > 0) {
            buffer.append("\nConcept rules: \n");
        }
        for (int i = 0; i < this.ruleList.length; i++) {
            buffer.append(this.ruleList[i].toString());
        }

        Annotation[] annots = getAnnotations();
        if (annots.length > 0) {
            buffer.append("Annotations: \n");
        }
        // print all annotations for this rule
        for (int i = 0; i < annots.length; i++) {
            buffer.append(annots[i].toString());
        }
        buffer.append("\n");
        return buffer.toString();
    }
}
