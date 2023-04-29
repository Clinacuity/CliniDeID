
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.resource.ResourceInitializationException;

import com.clinacuity.deid.ae.regex.Feature;
import com.clinacuity.deid.ae.regex.FeaturePath;
import com.clinacuity.deid.ae.regex.FilterFeature;
import com.clinacuity.deid.ae.regex.RegexVariables;
import com.clinacuity.deid.ae.regex.Rule;
import com.clinacuity.deid.ae.regex.RuleException;

/**
 * Implementation of the Rule interface.
 */
public class Rule_impl implements Rule {
    private static final Logger logger = LogManager.getLogger();
    // rule regex string
    private String regex;

    // rule ID
    private final String id;

    // rule confidence value
    private final float confidence;

    // rule match type as string value
    private final String matchTypeStr;

    // rule featurePath object
    private FeaturePath_impl featurePath;

    // true if a featurePath was specified for this rule
    private boolean isFeaturePathMatch = false;

    // rule match strategy
    private final int matchStrategy;

    // compiled rule regex pattern
    private Pattern pattern;// TODO: can this be static? final?

    // resolved rule match type
    private Type matchType;

    // rule filter features
    private ArrayList<FilterFeature> filterFeatures;

    // rule update features
    private ArrayList<Feature> updateFeatures;

    // rule exceptions
    private ArrayList<RuleException> exceptions;

    // concept variables
    private RegexVariables variables;

    private HashMap<String, Integer> matchGroupNames;

    /**
     * Constructor to create a new Rule object.
     *
     * @param regex
     *            regex pattern as String
     * @param matchStrategy
     *            matching strategy
     * @param matchType
     *            match type as String
     * @param id
     *            rule id (can also be null)
     * @param confidence
     *            confidence value
     * @param featurePath
     *            featurePath (can also be null)
     */
    public Rule_impl(String regex, int matchStrategy, String matchType, String id, float confidence, String featurePath,
            RegexVariables variables) {
        this.regex = regex;
        this.matchStrategy = matchStrategy;
        this.matchTypeStr = matchType;
        this.filterFeatures = new ArrayList<FilterFeature>();
        this.updateFeatures = new ArrayList<Feature>();
        this.exceptions = new ArrayList<RuleException>();
        this.pattern = null;
        this.id = id;
        this.confidence = confidence;
        this.featurePath = new FeaturePath_impl(featurePath);
        // set FeaturePath matching mode if a feature path is specified
        if (featurePath != null) {
            this.isFeaturePathMatch = true;
        }
        this.variables = variables;
        this.matchGroupNames = new HashMap<String, Integer>();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Rule#addFilterFeature(org.apache.uima.annotator.regex.Feature)
     */
    public void addFilterFeature(FilterFeature aFeature) {
        this.filterFeatures.add(aFeature);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Rule#getMatchTypeFilterFeatures()
     */
    public FilterFeature[] getMatchTypeFilterFeatures() {
        return this.filterFeatures.toArray(new FilterFeature[0]);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Rule#getMatchStrategy()
     */
    public int getMatchStrategy() {
        return this.matchStrategy;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Rule#getMatchType()
     */
    public Type getMatchType() {
        return this.matchType;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Rule#getRegex()
     */
    public Pattern getRegexPattern() {
        return this.pattern;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Rule#getConfidence()
     */
    public float getConfidence() {
        return this.confidence;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Rule#getId()
     */
    public String getId() {
        return this.id;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Rule#addUpdateFeature(org.apache.uima.annotator.regex.Feature)
     */
    public void addUpdateFeature(Feature aFeature) {
        this.updateFeatures.add(aFeature);

    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Rule#getMatchTypeUpdateFeatures()
     */
    public Feature[] getMatchTypeUpdateFeatures() {
        return this.updateFeatures.toArray(new Feature[0]);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Rule#addException(org.apache.uima.annotator.regex.Exception)
     */
    public void addException(RuleException aException) {
        this.exceptions.add(aException);

    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Rule#getExceptions()
     */
    public RuleException[] getExceptions() {
        return this.exceptions.toArray(new RuleException[0]);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Rule#getMatchTypeFeaturePath()
     */
    public FeaturePath getMatchTypeFeaturePath() {
        return this.featurePath;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Rule#isFeaturePathMatch()
     */
    public boolean isFeaturePathMatch() {
        return this.isFeaturePathMatch;
    }

    /**
     * @param ts
     * @throws ResourceInitializationException
     */
    public void typeInit(TypeSystem ts) throws ResourceInitializationException {
        // initialize the match type
        if (this.matchTypeStr != null) {
            this.matchType = ts.getType(this.matchTypeStr);
            if (this.matchType == null) {
                throw new RegexAnnotatorConfigException("regex_annotator_error_resolving_types", new Object[] { this.matchTypeStr });
            }
        }

        // initialize match type filters
        FilterFeature[] filterFeats = getMatchTypeFilterFeatures();
        for (int i = 0; i < filterFeats.length; i++) {
            ((FilterFeature_impl) filterFeats[i]).typeInit(this.matchType);
        }

        // initialize match type update features
        Feature[] updateFeats = getMatchTypeUpdateFeatures();
        for (int i = 0; i < updateFeats.length; i++) {
            ((Feature_impl) updateFeats[i]).typeInit(this.matchType);
        }

        // initialize rule exceptions
        RuleException[] ruleExceptions = getExceptions();
        for (int i = 0; i < ruleExceptions.length; i++) {
            ((RuleException_impl) ruleExceptions[i]).typeInit(ts);
        }

        // initialize featurePath element
        this.featurePath.initialize(this.matchType);
        logger.debug("Rule_impl typeInit complete");
    }

    /**
     * @throws RegexAnnotatorConfigException
     */
    public void initialize() throws RegexAnnotatorConfigException {
        // check if regular expression contains a regex variable, it must be
        // replaced first
        if (this.regex.indexOf(RegexVariables.VARIABLE_START) > -1) {
            // we have to replace the regex variables
            replaceRegexVariables();
        }

        // evaluate match group names
        if (this.regex.indexOf(Rule.MATCH_GROUP_START) > -1) {
            evaluateMatchGroupNames();
        }

        // compile regex
        this.pattern = Pattern.compile(this.regex);

        // initialize match type filters
        FilterFeature[] filterFeats = getMatchTypeFilterFeatures();
        for (int i = 0; i < filterFeats.length; i++) {
            ((FilterFeature_impl) filterFeats[i]).initialize();
        }

        // initialize match type update features
        Feature[] updateFeats = getMatchTypeUpdateFeatures();
        for (int i = 0; i < updateFeats.length; i++) {
            ((Feature_impl) updateFeats[i]).initialize();
        }

        // initialize rule exceptions
        RuleException[] ruleExceptions = getExceptions();
        for (int i = 0; i < ruleExceptions.length; i++) {
            ((RuleException_impl) ruleExceptions[i]).initialize();
        }
        logger.debug("Rule_impl initialized");

    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Rule#getMatchGroupNumber(java.lang.String)
     */
    public int getMatchGroupNumber(String matchGroupName) {
        Integer value = this.matchGroupNames.get(matchGroupName.toLowerCase());
        if (value != null) {
            return value.intValue();
        } else {
            return -1;
        }
    }

    /**
     * replace the variables used in the regular expression pattern
     *
     * @throws RegexAnnotatorConfigException
     */
    private void replaceRegexVariables() throws RegexAnnotatorConfigException {
        // create a regex matcher for the variable pattern
        Matcher matcher = RegexVariables.VARIABLE_REGEX_PATTERN.matcher(this.regex);

        // find all variables in the regular expression
        int pos = 0;
        HashSet<String> variableSet = new HashSet<String>();
        while (matcher.find(pos)) {

            // get match area for match group 1
            int varStart = matcher.start(1);
            int varEnd = matcher.end(1);

            // add match group 1 content (variable name) to the variable list
            variableSet.add(this.regex.substring(varStart, varEnd));

            // current end match position
            pos = matcher.end();
        }

        // replace all found variables in the regular expression
        for (String variableName : variableSet) {

            // check if variables are defined
            if (this.variables == null) {
                throw new RegexAnnotatorConfigException("regex_annotator_error_variable_not_found", new Object[] { variableName, this.id });
            } else {
                // get variable value for the variable name
                String varValue = this.variables.getVariableValue(variableName);
                if (varValue != null) {
                    // create variable expression that must be replaced
                    String variablePattern = RegexVariables.VARIABLE_REGEX_BEGIN + variableName + RegexVariables.VARIABLE_REGEX_END;
                    // replace variable with the variable value
                    this.regex = this.regex.replaceAll(variablePattern, varValue);
                } else {
                    throw new RegexAnnotatorConfigException("regex_annotator_error_variable_not_found",
                            new Object[] { variableName, this.id });
                }
            }
        }
    }

    /**
     * replace the variables used in the regular expression pattern
     *
     * @throws RegexAnnotatorConfigException
     */
    private void evaluateMatchGroupNames() {
        // create a regex matcher for the match group pattern
        Matcher matcher = Rule.MATCH_GROUP_REGEX_PATTERN.matcher(this.regex);

        ArrayList<String> names = new ArrayList<String>();

        // find all match group names in the regular expression
        int pos = 0;
        while (matcher.find(pos)) {

            // get match area for match group 1
            int varStart = matcher.start(1);
            int varEnd = matcher.end(1);

            // count match groups
            int groupCounter = 1;
            for (int i = 0; i < varEnd; i++) {
                if (this.regex.charAt(i) == '(') {
                    if (this.regex.charAt(i + 1) != '?') {
                        groupCounter++;
                    }
                }
            }
            String matchGroupName = this.regex.substring(varStart, varEnd);
            // add first match group content (match group name) to the variable list
            this.matchGroupNames.put(matchGroupName.toLowerCase(), Integer.valueOf(groupCounter));
            // store match group name with original case
            names.add(matchGroupName);

            // current end match position
            pos = matcher.end();
        }

        // replace all found match group names in the regular expression - never
        // needed
        for (String matchGroupName : names) {

            // create variable expression that must be replaced
            String matchGroupNamePattern = Rule.MATCH_GROUP_REGEX_BEGIN + matchGroupName + Rule.MATCH_GROUP_REGEX_END;
            // replace variable with the variable value
            this.regex = this.regex.replaceAll(matchGroupNamePattern, "");
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer buffer = new StringBuffer();
        buffer.append("Rule ");
        if (this.id != null) {
            buffer.append(this.id);
        }
        buffer.append("\n");
        buffer.append("Regex: ");
        buffer.append(this.regex);
        if (this.matchStrategy == Rule.MATCH_ALL) {
            buffer.append("\nMatch strategy: MATCH_ALL");
        } else if (this.matchStrategy == Rule.MATCH_COMPLETE) {
            buffer.append("\nMatch strategy: MATCH_COMPLETE");
        } else if (this.matchStrategy == Rule.MATCH_FIRST) {
            buffer.append("\nMatch strategy: MATCH_FIRST");
        }
        buffer.append("\nMatch type: ");
        buffer.append(this.matchTypeStr);

        buffer.append("\nFeaturePath: ");
        buffer.append(this.featurePath.getFeaturePath());

        if (this.confidence != 0.0) {
            buffer.append("\nConfidence: ");
            buffer.append(this.confidence);
        }

        FilterFeature[] filterFeats = getMatchTypeFilterFeatures();
        if (filterFeats.length > 0) {
            buffer.append("\nMatch type filter features: \n");
        }
        for (int i = 0; i < filterFeats.length; i++) {
            buffer.append(filterFeats[i].toString());
        }
        buffer.append("\n");

        RuleException[] ruleExceptions = getExceptions();
        if (ruleExceptions.length > 0) {
            buffer.append("\nRule exceptions: \n");
        }
        for (int i = 0; i < ruleExceptions.length; i++) {
            buffer.append(ruleExceptions[i].toString());
        }
        buffer.append("\n");

        return buffer.toString();
    }

}
