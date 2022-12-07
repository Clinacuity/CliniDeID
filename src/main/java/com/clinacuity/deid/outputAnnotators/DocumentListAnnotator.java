
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

import com.clinacuity.deid.util.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.clinacuity.deid.util.Utilities.verifyDirectory;

public class DocumentListAnnotator extends JCasAnnotator_ImplBase {
    public static final String OUTPUT_DIRECTORY_PARAM = "OutputDirectory";
    private static final Logger LOGGER = LogManager.getLogger();
    @ConfigurationParameter(name = OUTPUT_DIRECTORY_PARAM, defaultValue = "../output/", description = "The output directory to which the raw xmi will be saved")
    private static String OutputDirectory;
    private static List<String> fileList = new ArrayList<>();
    private static long totalCharactersProcessed = 0;

    public static List<String> getFileList() {
        return fileList;
    }

    public static void writeFile(String options) throws IOException {
        String list = String.join(System.lineSeparator(), fileList);
        try (FileWriter fileWriter = new FileWriter(OutputDirectory + File.separator + "ProcessedDocumentsList.txt");
             BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("This is a list of the files processed by CliniDeID along with the timestamp when they finished processing. System was run with the following options" + System.lineSeparator() + options + System.lineSeparator() + System.lineSeparator());
            writer.write(list);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            LOGGER.throwing(e);
            throw e;
        }
    }

    public static long getTotalCharactersProcessed() {
        return totalCharactersProcessed;
    }

    public static void clearTotalCharactersProcessed() {
        totalCharactersProcessed = 0;
    }

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        try {
            verifyDirectory(OutputDirectory);
        } catch (FileSystemException e) {
            throw new ResourceInitializationException(e);
        }
        LOGGER.debug("DocumentListAnnotator initialized");
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        LOGGER.debug("DocumentListAnnotator begin");
        String fileName = Utilities.getFullPathAndFileName(jCas);
        fileList.add(LocalDateTime.now() + " Finished de-identifying: " + fileName);
        totalCharactersProcessed += jCas.getDocumentText().length();
    }
}
