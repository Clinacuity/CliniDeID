
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
package com.clinacuity.deid.ae.regex.extension;


/**
 * The Validation interface is provided to implement a custom validator
 * that can be used to validate an annotation before it is created.
 */
public interface Validation {

/**
 * The validate method validates the covered text of an annotator and returns true or
 * false whether the annotation is correct or not. The validate method is called between
 * a rule match and the annotation creation. The annotation is only created if the method
 * returns true.
 *
 * @param coveredText covered text of the annotation that should be validated
 * @param ruleID ruleID of the rule which created the match
 *
 * @return true if the annotation is valid or false if the annotation is invalid
 *
 * @throws Exception throws an exception if an validation error occurred
 */
public boolean validate(String coveredText, String ruleID) throws Exception;

}
