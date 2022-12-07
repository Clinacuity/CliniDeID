
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

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.clinacuity.deid.ae.regex.impl;

import com.clinacuity.deid.ae.regex.Annotation;
import com.clinacuity.deid.ae.regex.Concept;
import com.clinacuity.deid.ae.regex.ConceptFileParser;
import com.clinacuity.deid.ae.regex.Feature;
import com.clinacuity.deid.ae.regex.FilterFeature;
import com.clinacuity.deid.ae.regex.Position;
import com.clinacuity.deid.ae.regex.RegexVariables;
import com.clinacuity.deid.ae.regex.Rule;
import com.clinacuity.deid.ae.regex.RuleException;
import org.apache.incubator.uima.regex.AnnotationDocument;
import org.apache.incubator.uima.regex.ConceptDocument;
import org.apache.incubator.uima.regex.ConceptSetDocument;
import org.apache.incubator.uima.regex.CreateAnnotationsDocument;
import org.apache.incubator.uima.regex.ExceptionDocument;
import org.apache.incubator.uima.regex.FeatureDocument;
import org.apache.incubator.uima.regex.RuleDocument;
import org.apache.incubator.uima.regex.RulesDocument;
import org.apache.incubator.uima.regex.SetFeatureDocument;
import org.apache.incubator.uima.regex.VariableDocument;
import org.apache.incubator.uima.regex.VariablesDocument;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;

import java.io.InputStream;
import java.util.ArrayList;

/**
 *
 */
public class ConceptFileParser_impl implements ConceptFileParser {

    /*
     * (non-Javadoc)
     *
     * @see org.apache.uima.annotator.regex.ConceptFileParser#parseConceptFile(java.lang.String,
     *      java.io.InputStream)
     */
    public Concept[] parseConceptFile(String conceptFilePathName,
                                      InputStream conceptFileStream) throws ResourceInitializationException {
        ArrayList<Concept> conceptList = new ArrayList<>();

        // parse regex concept file and extract content to local objects
        ConceptSetDocument conceptSetDoc;
        try {
            conceptSetDoc = (ConceptSetDocument) XmlBeans.getContextTypeLoader().parse(conceptFileStream, ConceptSetDocument.type, (XmlOptions) null);
        } catch (Exception ex) {
            throw new RegexAnnotatorConfigException(
                    "regex_annotator_error_parsing_rule_set_file",
                    new Object[]{conceptFilePathName}, ex);
        }

        // validate input file
//      ArrayList<XmlError> validationErrors = new ArrayList<XmlError>();
//      XmlOptions validationOptions = new XmlOptions();
//      validationOptions.setErrorListener(validationErrors);
//
//      boolean isValid = conceptSetDoc.validate(validationOptions);
//
//      // output the errors if the XML is invalid.
//      if (!isValid) {
//         Iterator<XmlError> iter = validationErrors.iterator();
//         StringBuffer errorMessages = new StringBuffer();
//         while (iter.hasNext()) {
//            errorMessages.append("\n>> ");
//            errorMessages.append(iter.next());
//         }
//         throw new RegexAnnotatorConfigException(
//               "regex_annotator_error_xml_validation", new Object[] {
//                     conceptFilePathName, errorMessages.toString() });
//      }

        // get concept file regex variables and store them all to the variables
        // object
        VariablesDocument.Variables variablesDoc = conceptSetDoc.getConceptSet()
                .getVariables();
        RegexVariables variables = null;
        if (variablesDoc != null) {
            VariableDocument.Variable[] varArray = variablesDoc.getVariableArray();
            if (varArray.length > 0) {
                variables = new RegexVariables_impl();
                for (int i = 0; i < varArray.length; i++) {
                    String value = varArray[i].getValue().replaceAll("\\\\", "\\\\\\\\");
                    variables.addVariable(varArray[i].getName(), value);
                }
            }
        }

        // ***************************************************
        // get the concepts from the concept file document
        // ***************************************************
        ConceptSetDocument.ConceptSet conceptSet = conceptSetDoc.getConceptSet();
        ConceptDocument.Concept[] concepts = conceptSet.getConceptArray();
        for (int i = 0; i < concepts.length; i++) {
            // get concept meta data
            String conceptName = concepts[i].getName();
            boolean processAllRules = concepts[i].getProcessAllRules();

            // create new concept object
            Concept concept = new Concept_impl(
                    conceptName, processAllRules);

            // ********************************
            // get all rules for this concept
            // ********************************
            RulesDocument.Rules rules = concepts[i].getRules();
            RuleDocument.Rule[] ruleList = rules.getRuleArray();
            for (int r = 0; r < ruleList.length; r++) {
                // get rule meta data
                String regex = ruleList[r].getRegEx();
                String matchType = ruleList[r].getMatchType();
                int matchStrategy = ruleList[r].getMatchStrategy().intValue();
                String id = ruleList[r].getRuleId();
                String featurePath = ruleList[r].getFeaturePath();
                float confidence = (float) 0.0;
                if (ruleList[r].getConfidence() != null) {
                    confidence = ruleList[r].getConfidence().floatValue();
                }

                // create new rule and add all rule settings
                // additionally add a reference to the regex variables object
                // to resolve regex variables
                Rule rule = new Rule_impl(regex, matchStrategy, matchType, id,
                        confidence, featurePath, variables);

                // ********************************
                // get match type filter features
                // ********************************
                if (ruleList[r].getMatchTypeFilter() != null) {
                    // iterate over all filter features and add them to the rule
                    FeatureDocument.Feature[] filterFeatures = ruleList[r]
                            .getMatchTypeFilter().getFeatureArray();
                    for (int x = 0; x < filterFeatures.length; x++) {
                        String featureName = filterFeatures[x].getFeaturePath();
                        String featureValue = filterFeatures[x].getStringValue();

                        // create new filter feature and add them to the rule
                        FilterFeature filterFeature = new FilterFeature_impl(
                                featureName, featureValue);
                        rule.addFilterFeature(filterFeature);
                    }
                }
                // ***********************************************
                // get all update match type annotation features
                // ***********************************************
                if (ruleList[r].getUpdateMatchTypeAnnotation() != null) {
                    // iterate over all match type annotation update features and add
                    // them to the rule
                    SetFeatureDocument.SetFeature[] updateFeatures = ruleList[r]
                            .getUpdateMatchTypeAnnotation().getSetFeatureArray();
                    for (int x = 0; x < updateFeatures.length; x++) {
                        String featureName = updateFeatures[x].getName();
                        String featureValue = updateFeatures[x].getStringValue();
                        int featureType = updateFeatures[x].getType().intValue();
                        int normalization = 0;
                        if (updateFeatures[x].getNormalization() != null) {
                            normalization = updateFeatures[x].getNormalization()
                                    .intValue();
                        }
                        String implClass = updateFeatures[x].getClass1();
                        // create new feature and add them to the rule
                        Feature updateFeature = new Feature_impl(featureType,
                                featureName, featureValue, normalization, implClass);
                        rule.addUpdateFeature(updateFeature);
                    }
                }

                // **********************************
                // get all exceptions for this rule
                // **********************************
                if (ruleList[r].getRuleExceptions() != null) {
                    // iterate over all match type annotation update features and add
                    // them to the rule
                    ExceptionDocument.Exception[] exceptions = ruleList[r]
                            .getRuleExceptions().getExceptionArray();
                    for (int x = 0; x < exceptions.length; x++) {
                        String exceptionMatchType = exceptions[x].getMatchType();
                        String regexPattern = exceptions[x].getStringValue();

                        // create new Exception object and add them to the rule
                        RuleException exception = new RuleException_impl(
                                exceptionMatchType, regexPattern);
                        rule.addException(exception);
                    }
                }

                // add rule to the concept
                concept.addRule(rule);
            }

            // **************************************
            // get all annotations for this concept
            // **************************************
            CreateAnnotationsDocument.CreateAnnotations annotations = concepts[i]
                    .getCreateAnnotations();
            AnnotationDocument.Annotation[] annotationList = annotations
                    .getAnnotationArray();
            for (int a = 0; a < annotationList.length; a++) {

                // create annotation position objects
                int beginMatchGroup = annotationList[a].getBegin().getGroup().intValue();
                int beginLocation = annotationList[a].getBegin().getLocation()
                        .intValue();
                int endMatchGroup = annotationList[a].getEnd().getGroup().intValue();
                int endLocation = annotationList[a].getEnd().getLocation()
                        .intValue();

                Position begin = new Position_impl(beginMatchGroup, beginLocation);
                Position end = new Position_impl(endMatchGroup, endLocation);

                // create annotation object
                String id = annotationList[a].getId();
                String type = annotationList[a].getType();
                String validationClass = annotationList[a].getValidate();

                Annotation annotation = new Annotation_impl(id, type, begin, end,
                        validationClass);

                // read out feature values and add it to the annotation
                SetFeatureDocument.SetFeature[] features = annotationList[a]
                        .getSetFeatureArray();
                for (int f = 0; f < features.length; f++) {
                    String name = features[f].getName();
                    int featureType = features[f].getType().intValue();
                    String value = features[f].getStringValue();
                    int normalization = 0;
                    if (features[f].getNormalization() != null) {
                        normalization = features[f].getNormalization().intValue();
                    }
                    String implClass = features[f].getClass1();

                    Feature feature = new Feature_impl(featureType, name, value,
                            normalization, implClass);
                    annotation.addFeature(feature);
                }
                // add annotation to rule
                concept.addAnnotation(annotation);
            }
            conceptList.add(concept);
        }

        return conceptList.toArray(new Concept[0]);
    }
}
