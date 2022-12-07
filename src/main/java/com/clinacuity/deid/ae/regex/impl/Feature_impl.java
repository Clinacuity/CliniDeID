
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
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.clinacuity.deid.ae.regex.impl;

import com.clinacuity.deid.ae.regex.extension.Normalization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.analysis_engine.annotator.AnnotatorInitializationException;
import org.apache.uima.cas.Type;
import org.apache.uima.resource.ResourceInitializationException;

import com.clinacuity.deid.ae.regex.Feature;

/**
 *
 *
 */
public class Feature_impl implements Feature {
    private static final Logger logger = LogManager.getLogger();

    private final int type;

    private final int normalizationType;

    private final boolean hasNormalizer;

    private final String name;

    private final String value;

    private final String implClass;

    private Normalization normalizer;

    private org.apache.uima.cas.Feature feature;

    public Feature_impl(int type, String name, String value, int normalizationType, String implClass) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.feature = null;
        this.normalizationType = normalizationType;
        this.implClass = implClass;
        this.normalizer = null;

        if (this.normalizationType > 0) {
            this.hasNormalizer = true;
        } else {
            this.hasNormalizer = false;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Feature#getType()
     */
    public int getType() {
        return this.type;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Feature#getName()
     */
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Feature#getValue()
     */
    public String getValue() {
        return this.value;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Feature#getFeature()
     */
    public org.apache.uima.cas.Feature getFeature() {
        return this.feature;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Feature#normalize(java.lang.String, java.lang.String)
     */
    public String normalize(String input, String ruleId) throws Exception {
        if (this.hasNormalizer) {
            // check normalizer type
            if (this.normalizationType == Feature.CUSTOM_NORMALIZATION) {
                return this.normalizer.normalize(input, ruleId);
            } else if (this.normalizationType == Feature.TO_LOWER_NORMALIZATION) {
                return input.toLowerCase();
            } else if (this.normalizationType == Feature.TO_UPPER_NORMALIZATION) {
                return input.toUpperCase();
            } else if (this.normalizationType == Feature.TRIM_NORMALIZATION) {
                return input.trim();
            }
        }
        return input;
    }

    /**
     * @param annotationType
     * @throws ResourceInitializationException
     */
    public void typeInit(Type annotationType) throws ResourceInitializationException {
        // get feature by name from the specified annotation type
        this.feature = annotationType.getFeatureByBaseName(this.name);

        // throw exception if the feature does not exist
        if (this.feature == null) {
            throw new ResourceInitializationException(AnnotatorInitializationException.FEATURE_NOT_FOUND,
                    new Object[] { Feature_impl.class.getName(), this.name });
        }
    }

    /**
     * Initialize the feature definition. Try to instantiate the custom normalization class if specified.
     *
     */
    public void initialize() throws RegexAnnotatorConfigException {
        // check if the normalization implementation exists in case of custom
        // normalization
        if (this.normalizationType == Feature.CUSTOM_NORMALIZATION) {

            // check if the normalizer class is specified
            if (this.implClass == null) {
                throw new RegexAnnotatorConfigException("regex_annotator_error_custom_normalizer_not_specified",
                        new Object[] { this.name });
            } else {
                try {
                    // try to get the custom normalization class
                    Class<?> normalizerClass = this.getClass().getClassLoader().loadClass(this.implClass);
                    Object obj = normalizerClass.newInstance();
                    if (obj instanceof Normalization) {
                        this.normalizer = (Normalization) obj;
                    } else {
                        throw new RegexAnnotatorConfigException("regex_annotator_error_custom_normalizer_wrong_interface",
                                new Object[] { this.implClass });
                    }
                } catch (Exception ex) {
                    throw new RegexAnnotatorConfigException("regex_annotator_error_creating_custom_normalizer",
                            new Object[] { this.implClass }, ex);
                }
            }
        }
        logger.debug("Feature_impl initialized");

    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Feature: ");
        buffer.append("\n  Name: ");
        buffer.append(this.name);
        if (this.type == Feature.FLOAT_FEATURE) {
            buffer.append("\n  Type: float");
        } else if (this.type == Feature.INTEGER_FEATURE) {
            buffer.append("\n  Type: integer");
        } else if (this.type == Feature.STRING_FEATURE) {
            buffer.append("\n  Type: string");
        } else if (this.type == Feature.REFERENCE_FEATURE) {
            buffer.append("\n  Type: reference");
        } else if (this.type == Feature.CONFIDENCE_FEATURE) {
            buffer.append("\n  Type: confidence");
        } else if (this.type == Feature.RULEID_FEATURE) {
            buffer.append("\n  Type: ruleId");
        }
        buffer.append("\n  Value: ");
        buffer.append(this.value);
        if (this.normalizationType > 0) {
            if (this.normalizationType == Feature.TO_LOWER_NORMALIZATION) {
                buffer.append("\n  Normalization: ToLowerCase");
            } else if (this.normalizationType == Feature.TO_UPPER_NORMALIZATION) {
                buffer.append("\n  Normalization: ToUpperCase");
            } else if (this.normalizationType == Feature.TRIM_NORMALIZATION) {
                buffer.append("\n  Normalization: Trim");
            }
            if (this.normalizationType == Feature.CUSTOM_NORMALIZATION) {
                buffer.append("\n  Normalization: " + this.implClass);
            }
        }

        buffer.append("\n");

        return buffer.toString();
    }

}
