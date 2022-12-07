
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

import org.apache.uima.resource.ResourceInitializationException;

/**
 *
 *
 */
public class RegexAnnotatorConfigException extends ResourceInitializationException {

    /**
    *
    */
    private static final long serialVersionUID = 2867637574651305994L;

    /**
     * Creates a new exception with a the specified message.
     *
     * @param aMessageKey
     *            an identifier that maps to the message for this exception. The message may contain placeholders for arguments as defined by the
     *            {@link java.text.MessageFormat MessageFormat} class.
     * @param aArguments
     *            The arguments to the message. <code>null</code> may be used if the message has no arguments.
     */
    public RegexAnnotatorConfigException(String aMessageKey, Object[] aArguments) {
        super("RegexAnnotator", aMessageKey, aArguments);
    }

    /**
     * Creates a new exception with the specified cause and a message from the {@link #STANDARD_MESSAGE_CATALOG}.
     *
     * @param aMessageKey
     *            an identifier that maps to the message for this exception. The message may contain placeholders for arguments as defined by the
     *            {@link java.text.MessageFormat MessageFormat} class.
     * @param aArguments
     *            The arguments to the message. <code>null</code> may be used if the message has no arguments.
     * @param aCause
     *            the original exception that caused this exception to be thrown, if any
     */
    public RegexAnnotatorConfigException(String aMessageKey, Object[] aArguments, Throwable aCause) {
        super("RegexAnnotator", aMessageKey, aArguments, aCause);
    }

}
