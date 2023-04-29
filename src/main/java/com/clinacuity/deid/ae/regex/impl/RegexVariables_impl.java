
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

import java.util.HashMap;

import com.clinacuity.deid.ae.regex.RegexVariables;

/**
 * Implementation of the RegexVariables interface
 */
public class RegexVariables_impl implements RegexVariables {

    private HashMap<String, String> variables;

    public RegexVariables_impl() {
        this.variables = new HashMap<>();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Variables#addVariable(java.lang.String, java.lang.String)
     */
    public void addVariable(String varName, String varValue) {
        this.variables.put(varName, varValue);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.uima.annotator.regex.Variables#getVariableValue(java.lang.String)
     */
    public String getVariableValue(String varName) {
        return this.variables.get(varName);
    }

}
