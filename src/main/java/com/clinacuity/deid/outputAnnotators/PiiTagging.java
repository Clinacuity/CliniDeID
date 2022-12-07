
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

import com.clinacuity.deid.readers.CdaXmlToText;
import com.clinacuity.deid.type.DocumentInformationAnnotation;
import com.clinacuity.deid.type.PiiAnnotation;
import com.clinacuity.deid.util.ConnectionProperties;
import com.clinacuity.deid.util.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;

import static com.clinacuity.deid.util.Utilities.getFileName;

public class PiiTagging extends JCasAnnotator_ImplBase {
    public static final String CATEGORY = "Category";
    public static final String OUTPUT_EXTENSION_PARAM = "FileExtension";
    public static final String OUTPUT_TO_FILE = "outputToFile";
    public static final String OUTPUT_TO_DB = "outputToDb";
    private static final Logger LOGGER = LogManager.getLogger();
    @ConfigurationParameter(name = CATEGORY, defaultValue = "none", description = "One of none top sub for if the category is to be included in the replacement")
    private String category = "";
    @ConfigurationParameter(name = OUTPUT_EXTENSION_PARAM, defaultValue = ".piitag.txt", mandatory = false, description = "The file extension to append to the output")
    private String fileExtension;
    @ConfigurationParameter(name = OUTPUT_TO_DB, defaultValue = "false", mandatory = false, description = "Decides whether to store in DB")
    private boolean outputToDb;
    @ConfigurationParameter(name = OUTPUT_TO_FILE, defaultValue = "true", mandatory = false, description = "Decides whether to print to file ")
    private boolean outputToFile;
    private int documentIndex = 0;
    private PreparedStatement psStmt;
    private String categoryName;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        documentIndex = 0;
        if (!category.equalsIgnoreCase("none") && !category.equalsIgnoreCase("top") && !category.equalsIgnoreCase("sub")) {
            throw new ResourceInitializationException("category not supported: " + category, null);
        }
        if (outputToDb) {
            if (category.equalsIgnoreCase("none")) {
                categoryName = "Tagged (PII)";
            } else if (category.equalsIgnoreCase("top")) {
                categoryName = "Tagged (category)";
            } else if (category.equalsIgnoreCase("sub")) {
                categoryName = "Tagged (sub category)";
            }
            Connection connection = ConnectionProperties.getInstance().getConnection();
            try {
                psStmt = Utilities.getInsertPreparedStatement(connection);
            } catch (SQLException e) {
                LOGGER.throwing(e);
                throw new ResourceInitializationException(e);
            }
        }
        LOGGER.debug("PiiTagging initialized");
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        String outputDirectory = "";
        DocumentInformationAnnotation docInfo = JCasUtil.selectSingle(jCas, DocumentInformationAnnotation.class);
        if (category.equalsIgnoreCase("none")) {
            if (docInfo.getPiiTaggingSelected()) {
                outputDirectory = docInfo.getPiiTaggingGeneralDirectory();
                LOGGER.debug("PiiTagging (no category) begin");
            } else {
                LOGGER.debug("PiiTagging (no category) skipped");
                return;
            }
        } else if (category.equalsIgnoreCase("top")) {
            if (docInfo.getPiiTaggingCategorySelected()) {
                outputDirectory = docInfo.getPiiTaggingCategoryDirectory();
                LOGGER.debug("PiiTagging (top category) begin");
            } else {
                LOGGER.debug("PiiTagging (top category) skipped");
                return;
            }
        }
        //could have else for sub category
        String plainText = jCas.getDocumentText();//consider using StringBuilder and replace instead of String concatenation
        FSIterator<PiiAnnotation> iterator = jCas.getAnnotationIndex(PiiAnnotation.class).iterator();
        iterator.moveToLast();
        String replacement;
        int newLines;
        //TODO: zip codes when in special category
        if (category.equalsIgnoreCase("none")) {
            replacement = "[*** PII ***]";
            while (iterator.hasPrevious()) {
                PiiAnnotation annotation = iterator.get();
                newLines = CdaXmlToText.countNewLines(annotation.getCoveredText());
                String fixNewLines = String.join("", Collections.nCopies(newLines, "\n"));
                plainText = plainText.substring(0, annotation.getBegin()) + replacement + fixNewLines + plainText.substring(annotation.getEnd());
                iterator.moveToPrevious();
            }
        } else if (category.equalsIgnoreCase("top")) {
            while (iterator.hasPrevious()) {
                PiiAnnotation annotation = iterator.get();
                newLines = CdaXmlToText.countNewLines(annotation.getCoveredText());
                String fixNewLines = String.join("", Collections.nCopies(newLines, "\n"));
                replacement = "[*** " + annotation.getPiiType() + " ***]" + fixNewLines;
                plainText = plainText.substring(0, annotation.getBegin()) + replacement + plainText.substring(annotation.getEnd());
                iterator.moveToPrevious();
            }
        }

        String finalText = "";
        if (JCasUtil.selectSingle(jCas, DocumentInformationAnnotation.class).getOutputToCda()) {
            finalText = CdaTextToXml.process(jCas, plainText);//inputDirectory, encoding);
        } else {
            finalText = plainText;
        }
        if (outputToFile) {
            String fileName = getFileName(jCas);
            if (fileName.equals("")) {
                fileName = "doc_" + documentIndex++;
            }
            Utilities.writeText(outputDirectory, fileName, fileExtension, finalText);
        }
        if (outputToDb) {
            Utilities.dbInsertPreparedQuery(jCas, psStmt, finalText, categoryName);
        }
        documentIndex++;
    }
}
