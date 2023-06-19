
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
package com.clinacuity.deid.ae.regex;

import java.util.regex.Pattern;

import org.apache.uima.cas.Type;

/**
 * Rule interface defines the access to the Rule settings and allows to add new
 * Rule settings.
 */
public interface Rule {

   public static final int MATCH_FIRST = 1;

   public static final int MATCH_ALL = 2;

   public static final int MATCH_COMPLETE = 3;

   public static final String MATCH_GROUP_START = "\\m";

   public static final String MATCH_GROUP_REGEX_BEGIN = "\\\\m\\{";

   public static final String MATCH_GROUP_REGEX_END = "\\}";

   public static final Pattern MATCH_GROUP_REGEX_PATTERN = Pattern
         .compile(MATCH_GROUP_REGEX_BEGIN + "(\\w+)" + MATCH_GROUP_REGEX_END);

   /**
    * Get the regular expression of this rule.
    *
    * @return returns the regular expression of this rule.
    */
   public Pattern getRegexPattern();

   /**
    * Get the rule id
    *
    * @return returns the rule id
    */
   public String getId();

   /**
    * Get the rule confidence
    *
    * @return returns the rule confidence
    */
   public float getConfidence();

   /**
    * Get the match strategy of this rule;
    *
    * @return returns the match strategy of this rule.
    */
   public int getMatchStrategy();

   /**
    * Get the match type of this rule.
    *
    * @return returns the match type of this rule.
    */
   public Type getMatchType();

   /**
    * Adds the given feature to the match type filter features
    *
    * @param aFeature
    *           The feature to be added.
    */
   public void addFilterFeature(FilterFeature aFeature);

   /**
    * Returns the match type filter features
    *
    * @return returns the match type feature filters
    */
   public FilterFeature[] getMatchTypeFilterFeatures();

   /**
    * Adds the given feature to the match type annotation update features
    *
    * @param aFeature
    *           The feature to be added.
    */
   public void addUpdateFeature(Feature aFeature);

   /**
    * Returns the match type annotation update features
    *
    * @return returns the match type annotation update features
    */
   public Feature[] getMatchTypeUpdateFeatures();

   /**
    * Adds the given exception to this rule
    *
    * @param aException
    *           The exception to be added.
    */
   public void addException(RuleException aException);

   /**
    * Returns the exceptions for this rule
    *
    * @return returns the exceptions for this rule
    */
   public RuleException[] getExceptions();

   /**
    * Returns the match type featurePath object.
    *
    * @return returns the match type featurePath object
    */
   public FeaturePath getMatchTypeFeaturePath();

   /**
    * Returns true if a featurePath for this rule was specified.
    *
    * @return true if a featurePath was specified for this rule
    */
   public boolean isFeaturePathMatch();

   /**
    * Returns the match group number for the given match group name or -1 if
    * the match group name is not available.
    *
    * @param matchGroupName match group name
    *
    * @return returns the match group number for the given name or -1 if the match group does not exist.
    */
   public int getMatchGroupNumber(String matchGroupName);

}
