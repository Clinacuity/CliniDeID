
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

package com.clinacuity.deid.outputAnnotators;

import com.clinacuity.deid.type.DocumentInformationAnnotation;
import com.clinacuity.deid.type.PiiAnnotation;
import com.clinacuity.deid.util.ConnectionProperties;
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

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

import static com.clinacuity.deid.util.Utilities.getFileName;

public class PiiWriterAnnotator extends JCasAnnotator_ImplBase {
    public static final String OUTPUT_EXTENSION_PARAM = "FileExtension";
    public static final String OUTPUT_TO_FILE = "outputToFile";
    public static final String OUTPUT_TO_DB = "outputToDb";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String NEW_LINE = System.getProperty("line.separator");
    @ConfigurationParameter(name = OUTPUT_EXTENSION_PARAM, defaultValue = ".detectedPII.txt", mandatory = false, description = "The file extension to append to the output")
    private String fileExtension;
    @ConfigurationParameter(name = OUTPUT_TO_DB, defaultValue = "false", mandatory = false, description = "Decides whether to store in DB")
    private boolean outputToDb;
    @ConfigurationParameter(name = OUTPUT_TO_FILE, defaultValue = "true", mandatory = false, description = "Decides whether to print to file")
    private boolean outputToFile;
    private PreparedStatement psStmt;
    private int documentIndex = 0;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        documentIndex = 0;
        if (outputToDb) {
            Connection connection = ConnectionProperties.getInstance().getConnection();
            try {
                psStmt = connection.prepareStatement("Insert into NOTE_ANNOTATIONS (run_id, span_begin, span_end, category ) VALUES (?, ?, ?, ?)");
            } catch (SQLException e) {
                LOGGER.throwing(e);
                throw new ResourceInitializationException(e);
            }
        }
        LOGGER.debug("PiiWriterAnnotator initialized");
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        DocumentInformationAnnotation docInfo = JCasUtil.selectSingle(jCas, DocumentInformationAnnotation.class);
        if (docInfo.getPiiWriterSelected()) {
            LOGGER.debug("PiiWriterAnnotator begin");
        } else {
            LOGGER.debug("PiiWriterAnnotator skipped");
            return;
        }
        long runId = -1;
        if (outputToDb) {
            runId = Utilities.getRunId(jCas);
        }
        StringBuilder buffer = new StringBuilder(2000);//2000 is nothing magical, just an estimate
        for (PiiAnnotation annotation : jCas.getAnnotationIndex(PiiAnnotation.class)) {
            if (outputToFile) {
                buffer.append("\"");
                buffer.append(annotation.getPiiType());
                buffer.append("\",\"");
                buffer.append(annotation.getPiiSubtype());
                buffer.append("\",\"");
                buffer.append(annotation.getCoveredText());
                buffer.append("\"").append(NEW_LINE);
            }
            if (outputToDb) {
                try {
                    psStmt.setLong(1, runId);
                    psStmt.setInt(2, annotation.getBegin());
                    psStmt.setInt(3, annotation.getEnd());
                    psStmt.setString(4, annotation.getPiiSubtype());
                    psStmt.addBatch();
                } catch (SQLException e) {
                    LOGGER.throwing(e);
                }
            }
        }
        if (outputToFile) {
            String fileName = getFileName(jCas);
            if (fileName.equals("")) {
                fileName = "doc_" + documentIndex++;
            }
            fileName = docInfo.getPiiWriterDirectory() + fileName + fileExtension;
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(buffer.toString());
            } catch (IOException e) {
                LOGGER.throwing(e);
                throw new AnalysisEngineProcessException(e);
            }
        }
        if (outputToDb) {
            try {
                int[] results = psStmt.executeBatch();
                LOGGER.debug("{}", () -> "DB results: " + Arrays.toString(results));
            } catch (SQLException e) {
                LOGGER.throwing(e);
            }
        }

        documentIndex++;
    }
}
