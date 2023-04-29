
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

/**
 * RegexVariables interface.
 */
public interface RegexVariables {

   public static final String VARIABLE_START = "\\v";

   public static final String VARIABLE_REGEX_BEGIN = "\\\\v\\{";

   public static final String VARIABLE_REGEX_END = "\\}";

   public static final Pattern VARIABLE_REGEX_PATTERN = Pattern
         .compile(VARIABLE_REGEX_BEGIN + "(\\w+)" + VARIABLE_REGEX_END);

   /**
    * Adds a variable to the Variables object.
    *
    * @param varName
    *           variable name
    *
    * @param varValue
    *           variable value
    */
   public void addVariable(String varName, String varValue);

   /**
    * returns the value of the specified variable or <code>null</code> if the
    * variable does not exist
    *
    * @param varName
    *           variable name
    *
    * @return returns the variable value of <code>null</code> if the variable
    *         does not exist
    *
    */
   public String getVariableValue(String varName);
}
