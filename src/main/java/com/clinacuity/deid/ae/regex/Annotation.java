
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

import org.apache.uima.cas.Type;

/**
 *
 */
public interface Annotation {

   /**
    * Get the annotation identifier.
    *
    * @return returns the annotation identifier
    */
   public String getId();

   /**
    * Get the annotation type of this annotation.
    *
    * @return returns the annotation type of this annotation
    */
   public Type getAnnotationType();

   /**
    * Get the annotation begin position of this annotation.
    *
    * @return returns the annotation begin position.
    */
   public Position getBegin();

   /**
    * Get the annotation end position of this annotation.
    *
    * @return returns the annotation end position.
    */
   public Position getEnd();

   /**
    * Adds the given feature to this annotation.
    *
    * @param aFeat
    *           The feature to be added.
    */
   public void addFeature(Feature aFeat);

   /**
    * Returns the feature array for this annotation
    *
    * @return returns the feature array for this annotation
    */
   public Feature[] getFeatures();

   /**
    * Validate the covered Text of the annotation and checks if the annotation
    * is valid or not.
    *
    * @param coveredText covered text of the annotation that should be created
    *
    * @param ruleID ruleID (if specified) of the rule that created the match
    *
    * @return true if the annotation is valid or if not validation was
    *         specified. If the annotation is invalid, false is returned.
    */
   public boolean validate(String coveredText, String ruleID) throws Exception;

   /**
    * Returns true if for the current annotation an validator is available.
    *
    * @return Returns true if an validator is available.
    */
   public boolean hasValidator();

}
