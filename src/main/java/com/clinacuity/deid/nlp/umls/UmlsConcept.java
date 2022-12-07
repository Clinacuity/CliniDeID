
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

package com.clinacuity.deid.nlp.umls;

/**
 * This class encloses UMLS Concepts.
 * 
 * @author Julien Thibault (Textractor)
 *
 */
public class UmlsConcept {
    public String CUI;
    public String preferredTerm;
    public String term;
    public String TUI;
    public String terminology;
    public String definition;

    /**
     * 
     * @param iCUI
     * @param iPreferredTerm
     * @param iTerm
     * @param iTUI
     * @param iTerminology
     * @param iDefinition
     */
    public UmlsConcept(String iCUI, String iPreferredTerm, String iTerm, String iTUI, String iTerminology, String iDefinition) {
        CUI = iCUI;
        preferredTerm = iPreferredTerm;
        term = iTerm;
        TUI = iTUI;
        terminology = iTerminology;
        definition = iDefinition;
    }
}
