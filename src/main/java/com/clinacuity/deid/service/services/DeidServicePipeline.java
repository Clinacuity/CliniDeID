
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

package com.clinacuity.deid.service.services;

import com.clinacuity.clinideid.entity.CliniDeidLicense;
import com.clinacuity.clinideid.message.DeidClientMessage;
import com.clinacuity.clinideid.message.DeidLevel;
import com.clinacuity.deid.ae.DeidLevelCleaner;
import com.clinacuity.deid.outputAnnotators.DocumentListAnnotator;
import com.clinacuity.deid.outputAnnotators.PiiTagging;
import com.clinacuity.deid.outputAnnotators.resynthesis.ResynthesisAnnotator;
import com.clinacuity.deid.readers.FileSystemCollectionReader;
import com.clinacuity.deid.readers.GeneralCollectionReader;
import com.clinacuity.deid.service.DeidProperties;
import com.clinacuity.deid.service.license.LicenseService;
import com.clinacuity.deid.type.DocumentInformationAnnotation;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.clinacuity.deid.mains.DeidPipeline.getAnalysisEngine;
import static com.clinacuity.deid.mains.DeidPipeline.getProcessingAnalysisEngines;
import static org.apache.uima.fit.factory.JCasFactory.createJCas;

@Component
public class DeidServicePipeline {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String OUT_PATH = "./";
    private static final String FILE_NAME = "temp";
    private static final String OUT_EXTENSION = ".txt";

    private final DeidProperties properties;
    private final LicenseService licenseService;
    private final JCas jCas;
    private List<AnalysisEngine> analysisEngines = getProcessingAnalysisEngines();

    @Autowired
    public DeidServicePipeline(DeidProperties properties, LicenseService licenseService) throws UIMAException {
        this.properties = properties;
        this.licenseService = licenseService;
        jCas = createJCas();
        addOutputAnnotators();
        LOGGER.info("CliniDeID Service Pipeline initialized.");
    }

    private void addOutputAnnotators() {
        analysisEngines.add(getAnalysisEngine(DeidLevelCleaner.class));
        analysisEngines.add(getAnalysisEngine(ResynthesisAnnotator.class,
                ResynthesisAnnotator.OUTPUT_TO_FILE, true,
                ResynthesisAnnotator.OUTPUT_TO_DB, false,
                ResynthesisAnnotator.SAVE_FOR_SERVICE, true,
                ResynthesisAnnotator.OUTPUT_EXTENSION_PARAM, OUT_EXTENSION));
        analysisEngines.add(getAnalysisEngine(PiiTagging.class,
                PiiTagging.CATEGORY, "top",
                PiiTagging.OUTPUT_EXTENSION_PARAM, OUT_EXTENSION,
                PiiTagging.OUTPUT_TO_DB, false,
                PiiTagging.OUTPUT_TO_FILE, true));
        analysisEngines.add(getAnalysisEngine(PiiTagging.class,
                PiiTagging.CATEGORY, "none",
                PiiTagging.OUTPUT_EXTENSION_PARAM, OUT_EXTENSION,
                PiiTagging.OUTPUT_TO_DB, false,
                PiiTagging.OUTPUT_TO_FILE, true));
    }

    public String runPipeline(DeidClientMessage message, UUID license) {
        properties.getLicense().getValue().setKey(license);
        if (properties.validateLicense()) {
            String processedText = runPipeline(message);
            properties.updateLicense();
            return processedText;
        } else {
            LOGGER.warn("This license has expired, please contact support@clinacuity.com");
            return "This license has expired, please contact support@clinacuity.com";
        }
    }

    public String runPipeline(DeidClientMessage message) {
        jCas.reset();
        jCas.setDocumentLanguage("en-us");
        jCas.setDocumentText(message.getMessage());
        configureDocumentInformationAnnotation(jCas, FILE_NAME, "", message);

        try {
            SimplePipeline.runPipeline(jCas, analysisEngines.toArray(new AnalysisEngine[0]));
            String out;
            try {
                File file = new File(OUT_PATH + FILE_NAME + OUT_EXTENSION);
                if (file.exists()) {
                    out = FileUtils.readFileToString(new File(OUT_PATH + FILE_NAME + OUT_EXTENSION), "utf-8");
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                } else {
                    out = "error";
                }
            } catch (IOException e) {
                LOGGER.throwing(e);
                out = "error";
            }
            jCas.reset();
            return out;
        } catch (AnalysisEngineProcessException e) {
            LOGGER.throwing(e);
            return "Failed to process text:" + e.getMessage() + " because " + e.getCause();
        }
    }

    //path... should already have been created if they will be needed
    public void runPipelineOnBatch(File file, DeidLevel level, boolean resynthesis, boolean categories, boolean general,
                                   String batchOutPath, String pathGeneralized, String pathCategorized, String pathResynthesized) {
        if (properties.isBusy()) {
            LOGGER.error("Tried to process a batch when the pipeline was already busy");
            return;
        }

        properties.setBusy(true);
        if (file.isFile()) {
            LOGGER.throwing(new IOException("The path sent to the pipeline is a file, not a directory!"));
            return;
        }
        DocumentListAnnotator.clearTotalCharactersProcessed();
        analysisEngines.add(getAnalysisEngine(DocumentListAnnotator.class, DocumentListAnnotator.OUTPUT_DIRECTORY_PARAM, batchOutPath));

        jCas.reset();
        jCas.setDocumentLanguage("en-us");

        try {
            CliniDeidLicense license = properties.getLicense().getValue();

            TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription();

            FileSystemCollectionReader reader = (FileSystemCollectionReader) CollectionReaderFactory.createReader(
                    FileSystemCollectionReader.class, typeSystemDescription,
                    FileSystemCollectionReader.INPUT_DIRECTORY_PARAMETER, file.getAbsolutePath() + File.separator,
                    FileSystemCollectionReader.RECURSIVE_PARAMETER, false,
                    FileSystemCollectionReader.FILE_LIMIT, license.getFileLimit(), //TODO FIX/test THIS and all licensing issues *************
                    FileSystemCollectionReader.FILE_SIZE_LIMIT, license.getMaxFileSize(),

                    GeneralCollectionReader.DEID_LEVEL, level,
                    GeneralCollectionReader.OUTPUT_CLEAN, false,
                    GeneralCollectionReader.OUTPUT_GENERAL_TAG, general,
                    GeneralCollectionReader.OUTPUT_CATEGORY_TAG, categories,
                    GeneralCollectionReader.OUTPUT_PII, false,
                    GeneralCollectionReader.OUTPUT_RESYNTHESIS, resynthesis,
                    GeneralCollectionReader.OUTPUT_RAW, false,

                    GeneralCollectionReader.GENERAL_TAG_DIRECTORY, pathGeneralized,
                    GeneralCollectionReader.CATEGORY_TAG_DIRECTORY, pathCategorized,
                    GeneralCollectionReader.RESYNTHESIS_DIRECTORY, pathResynthesized
            );
            licenseService.requestProcess(reader.getFileCount());
            SimplePipeline.runPipeline(reader, analysisEngines.toArray(new AnalysisEngine[0]));
            licenseService.finishProcess();
        } catch (IOException | UIMAException e) {
            properties.setBusy(false);
        }
        properties.setBusy(false);
    }

    private void configureDocumentInformationAnnotation(JCas jCas, String name, String path, DeidClientMessage message) {
        DocumentInformationAnnotation docInfo = new DocumentInformationAnnotation(jCas);

        docInfo.setDocumentType("txt");//update elsewhere if needed
        docInfo.setFilePath(path);
        docInfo.setFileSize(message.getMessage().length());
        docInfo.setFileName(name);
        docInfo.setLevel(message.getDeidLevel().toString());
        docInfo.setOutputToCda(false);//UPDATE this if service supports CDA and message contains choice
        docInfo.setResynthesisSelected(message.getOutputResynthesis());
        docInfo.setPiiTaggingSelected(message.getOutputGeneralTags());
        docInfo.setPiiTaggingCategorySelected(message.getOutputCategoryTags());

        //I think this is only ued when batch is false, but not a big penalty to check
        String resynthesisOut = message.getIsBatch() ? "" + "/resynthesized" : OUT_PATH;
        String categoryOut = message.getIsBatch() ? "" + "/categories/" : OUT_PATH;
        String generalOut = message.getIsBatch() ? "" + "/generalized/" : OUT_PATH;

        docInfo.setResynthesisDirectory(resynthesisOut);
        docInfo.setPiiTaggingCategoryDirectory(categoryOut);
        docInfo.setPiiTaggingGeneralDirectory(generalOut);

        docInfo.addToIndexes();
    }
}
