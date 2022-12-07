
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

package com.clinacuity.deid.outputAnnotators;

import com.clinacuity.deid.type.DocumentInformationAnnotation;
import com.clinacuity.deid.util.Serializer;
import com.clinacuity.deid.util.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.xml.sax.SAXException;

import java.io.IOException;

public class RawXmiOutputAnnotator extends JCasAnnotator_ImplBase {
    // weird name since it isn't the extension as .xml is apppened after it
    public static final String OUTPUT_EXTENSION_PARAM = "FileExtension";
    public static final String PRETTY_PRINT_PARAM = "PrettyPrintXmi";
    public static final String OUTPUT_TO_FILE = "outputToFile";
    public static final String OUTPUT_TO_DB = "outputToDb";
    public static final String IS_CLEAN = "isClean";
    private static final Logger LOGGER = LogManager.getLogger();
    @ConfigurationParameter(name = OUTPUT_EXTENSION_PARAM, defaultValue = ".raw", mandatory = false, description = "The file extension to append to the output")
    private String fileExtension;
    @ConfigurationParameter(name = PRETTY_PRINT_PARAM, defaultValue = "true", mandatory = false)
    private boolean prettyPrintParam;
    @ConfigurationParameter(name = OUTPUT_TO_DB, defaultValue = "false", mandatory = false, description = "Decides whether to store in DB")
    private boolean outputToDb;
    @ConfigurationParameter(name = OUTPUT_TO_FILE, defaultValue = "true", mandatory = false, description = "Decides whether to print to file ")
    private boolean outputToFile;
    @ConfigurationParameter(name = IS_CLEAN, description = "if not clean then raw output, for which flag to check in DocumentInformationAnnotation")
    private boolean isClean;
    private int documentIndex = 0;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        documentIndex = 0;
        LOGGER.debug("RawXmiOutputAnnotator initialized");
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        DocumentInformationAnnotation docInfo = JCasUtil.selectSingle(jCas, DocumentInformationAnnotation.class);
        String outputDirectory;
        if (isClean && docInfo.getCleanXmiOutputSelected()) {
            outputDirectory = docInfo.getRawXmiDirectory();
            LOGGER.debug("RawXmiOutputAnnotator Raw output begin");
        } else if (!isClean && docInfo.getRawXmiOutputSelected()) {
            outputDirectory = docInfo.getCleanXmiDirectory();
            LOGGER.debug("RawXmiOutputAnnotator Clean output begin");
        } else {
            LOGGER.debug("RawXmiOutputAnnotator skipped");
            return;
        }
        if (outputToFile) {
            String fileName = Utilities.getFileName(jCas);
            if (fileName.equals("")) {
                fileName = "doc_" + documentIndex++;
            }
            try {
                Serializer.SerializeToXmi(jCas, outputDirectory + fileName + fileExtension + ".xml", prettyPrintParam);
            } catch (SAXException | IOException e) {
                LOGGER.throwing(e);
                throw new AnalysisEngineProcessException(e);
            }
        }
    }
}
