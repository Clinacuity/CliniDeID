
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
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.LowLevelCAS;
import org.apache.uima.cas.impl.LowLevelTypeSystem;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemUtils;
import org.apache.uima.cas.impl.TypeSystemUtils.PathValid;
import org.apache.uima.cas.text.AnnotationFS;

import com.clinacuity.deid.ae.regex.FeaturePath;

/**
 * FeaturePath implementation that validates and the given featurePath and returns the featurePath value for a specified annotation.
 */
public class FeaturePath_impl implements FeaturePath {
    private static final Logger logger = LogManager.getLogger();

    // featurePath string, separated by "/"
    private String featurePathString;

    // featurePath element names
    private ArrayList<String> featurePathElementNames;

    // featurePath element features
    private ArrayList<Feature> featurePathElements;

    /**
     * Constructor to create a new featurePath object with a given featurePath string.
     *
     * @param featurePath
     *            featurePath string separated by "/"
     */
    public FeaturePath_impl(String featurePath) {
        this.featurePathString = featurePath;
        this.featurePathElementNames = new ArrayList<>();
        this.featurePathElements = null;
    }

    /**
     * Initialize the object's featurePath for the given type. If the featurePath is not valid an exception is thrown.
     *
     * @param type
     *            CAS type to used to initialize the featurePath
     */
    public void initialize(Type type) throws RegexAnnotatorConfigException {
        // initialization must only be done if a featurePath value was specified
        if (this.featurePathString != null) {
            // check featurePath for invalid character sequences
            if (this.featurePathString.indexOf("//") > -1) {
                // invalid featurePath syntax
                throw new RegexAnnotatorConfigException("regex_annotator_error_invalid_feature_path_syntax",
                        new Object[] { this.featurePathString, "//" });
            }

            // parse featurePath into path elements
            StringTokenizer tokenizer = new StringTokenizer(this.featurePathString, "/");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                this.featurePathElementNames.add(token);
            }

            // validate featurePath for given type
            PathValid pathValid = TypeSystemUtils.isPathValid(type, this.featurePathElementNames);
            if (PathValid.NEVER == pathValid) {
                // invalid featurePath - throw an configuration exception
                throw new RegexAnnotatorConfigException("regex_annotator_error_validating_feature_path",
                        new Object[] { this.featurePathString, type.getName() });
            } else if (PathValid.ALWAYS == pathValid) {
                // the featurePath is always valid, so we can resolve and cache the
                // path elements
                this.featurePathElements = new ArrayList<>();
                Type currentType = type;
                // iterate over all featurePathNames and store the resolved CAS
                // feature in the featurePathElements list
                for (int i = 0; i < this.featurePathElementNames.size(); i++) {
                    // get feature
                    Feature feature = currentType.getFeatureByBaseName(this.featurePathElementNames.get(i));
                    // store feature
                    this.featurePathElements.add(feature);

                    // get current feature type to resolve the next feature name
                    currentType = feature.getRange();
                }
            }
        }
        logger.debug("FeaturePath_impl initialized");

    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.FeaturePath#getValue(org.apache.uima.cas.text.AnnotationFS)
     */
    public String getValue(AnnotationFS annotFS) throws RegexAnnotatorProcessException {

        // handle special case where no featurePath was specified
        // (featurePathString == null)
        if (this.featurePathElementNames.size() == 0) {
            // no featurePath was specified, return the coveredText of the annotFS
            // as matching text
            return annotFS.getCoveredText();
        } else {
            // we have a feature path that must be evaluated

            // featurePathValue
            String featurePathValue = null;
            // check if further featurePath elements are possible
            boolean noFurtherElementsPossible = false;

            // set current FS values
            FeatureStructure currentFS = annotFS;
            Type currentType = annotFS.getType();

            // resolve feature path value
            for (int i = 0; i < this.featurePathElementNames.size(); i++) {

                // if we had in the last iteration a primitive feature or a FS that
                // was
                // not set, the feature path is not valid for this annotation.
                if (noFurtherElementsPossible) {
                    return null;
                }

                // get the Feature for the current featurePath element. If the
                // featurePath is always valid the featurePath Feature elements are
                // cached, otherwise the feature names must be resolved by name
                Feature feature;
                if (this.featurePathElements == null) {
                    // resolve Feature by name
                    feature = currentType.getFeatureByBaseName(this.featurePathElementNames.get(i));
                } else {
                    // use cached Feature element
                    feature = this.featurePathElements.get(i);
                }

                // if feature is null the feature was not defined
                if (feature == null) {
                    return null;
                }

                // get feature type and type code
                Type featureType = feature.getRange();
                LowLevelTypeSystem llts = ((TypeImpl) featureType).getTypeSystem().getLowLevelTypeSystem();
                int featureTypeCode = llts.ll_getTypeClass(llts.ll_getCodeForType(featureType));

                // switch feature type code
                switch (featureTypeCode) {
                case LowLevelCAS.TYPE_CLASS_STRING:
                    featurePathValue = currentFS.getStringValue(feature);
                    noFurtherElementsPossible = true;
                    break;
                case LowLevelCAS.TYPE_CLASS_INT:
                    featurePathValue = Integer.toString(currentFS.getIntValue(feature));
                    noFurtherElementsPossible = true;
                    break;
                case LowLevelCAS.TYPE_CLASS_BOOLEAN:
                    featurePathValue = Boolean.toString(currentFS.getBooleanValue(feature));
                    noFurtherElementsPossible = true;
                    break;
                case LowLevelCAS.TYPE_CLASS_BYTE:
                    featurePathValue = Byte.toString(currentFS.getByteValue(feature));
                    noFurtherElementsPossible = true;
                    break;
                case LowLevelCAS.TYPE_CLASS_DOUBLE:
                    featurePathValue = Double.toString(currentFS.getDoubleValue(feature));
                    noFurtherElementsPossible = true;
                    break;
                case LowLevelCAS.TYPE_CLASS_FLOAT:
                    featurePathValue = Float.toString(currentFS.getFloatValue(feature));
                    noFurtherElementsPossible = true;
                    break;
                case LowLevelCAS.TYPE_CLASS_LONG:
                    featurePathValue = Long.toString(currentFS.getLongValue(feature));
                    noFurtherElementsPossible = true;
                    break;
                case LowLevelCAS.TYPE_CLASS_INVALID:
                    featurePathValue = null;
                    noFurtherElementsPossible = true;
                    break;
                case LowLevelCAS.TYPE_CLASS_SHORT:
                    featurePathValue = Short.toString(currentFS.getShortValue(feature));
                    noFurtherElementsPossible = true;
                    break;
                case LowLevelCAS.TYPE_CLASS_FS:
                    currentFS = currentFS.getFeatureValue(feature);
                    if (currentFS == null) {
                        // FS value not set - feature path cannot return a valid value
                        noFurtherElementsPossible = true;
                        featurePathValue = null;
                    } else {
                        currentType = currentFS.getType();
                    }
                    break;
                default:
                    throw new RegexAnnotatorProcessException("regex_annotator_error_feature_path_element_not_supported",
                            new Object[] { currentType.getName(), this.featurePathElementNames.get(i), this.featurePathString });
                }
            }

            // the featurePath was processed correctly, check the featurePath value
            if (featurePathValue != null) {
                return featurePathValue;
            } else {
                // it seems that the last featurePath element was a FS
                // check now if the FS is of type AnnotationFS
                if (currentFS != null) {
                    if (currentFS instanceof AnnotationFS) {
                        // the last FS was an Annotation, so return the covered Text
                        // of the annotation as featurePath value
                        return ((AnnotationFS) currentFS).getCoveredText();
                    }
                }
                return null;
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.FeaturePath#getFeaturePath()
     */
    public String getFeaturePath() {
        return this.featurePathString;
    }

}
