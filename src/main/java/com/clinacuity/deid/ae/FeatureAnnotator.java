
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

package com.clinacuity.deid.ae;

import com.clinacuity.deid.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.HashMap;

public class FeatureAnnotator extends JCasAnnotator_ImplBase {
    public static final String WORD_VECTOR_CL_FILE_NAME = "wordVectorClFileName";
    private static final Logger LOGGER = LogManager.getLogger();
    private HashMap<String, Integer> wordVectorCl = new HashMap<>(220000);
    @ConfigurationParameter(name = WORD_VECTOR_CL_FILE_NAME, description = "Name of file  with word vectors")
    private String wordVectorClFileName;

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);
        Util.readWvMap(wordVectorClFileName, wordVectorCl);
        LOGGER.debug("FeatureAnnotator Initialized");
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        Util.addFeatures(jCas, wordVectorCl);
    }
}
