
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

package com.clinacuity.deid.training;

import com.clinacuity.deid.readers.FileSystemCollectionReader;
import com.clinacuity.deid.type.DocumentInformationAnnotation;
import com.clinacuity.deid.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.List;
import java.util.Map;

public class CombinedCorpusGoldCollectionReader extends FileSystemCollectionReader {
    public static final String INPUT_EXTENSION_PARAM = "fileExtension";
    private static final Logger logger = LogManager.getLogger();
    public static boolean isTraining = false;
    private static Map<String, String> piiParentTypeMap = NamedEntityChunker.createSubToParentTypeMap();
    @ConfigurationParameter(name = INPUT_EXTENSION_PARAM, defaultValue = ".xml", mandatory = false, description = "The file extension to look for.")
    private String fileExtension;
    private Map<String, String> subTypeMuscToDeid = Map.ofEntries(
            Map.entry("HCUnit", "HealthCareUnitName"), Map.entry("OtherID", "OtherIDNumber"),
            Map.entry("OtherOrg", "OtherOrgName"), Map.entry("eAddress", "ElectronicAddress"));

    /**
     * Initializes the collection reader with the specified parameters
     *
     * @param _inputDir  The directory from which the pipeline will consume files.
     * @param _recursion Determines whether the collection reader will read files in sub-directories or not.
     */
    public CombinedCorpusGoldCollectionReader(String _inputDir, boolean _recursion) {//TODO: is this used?
        setConfigParameterValue(INPUT_DIRECTORY_PARAMETER, _inputDir);
        setConfigParameterValue(RECURSIVE_PARAMETER, _recursion);
    }

    public CombinedCorpusGoldCollectionReader() {//needed for instantiating through UIMA-Fit
    }

    /**
     * Initializes the collection reader with the specified parameters
     *
     * @param _inputDir  The directory from which the pipeline will consume files.
     * @param _encoding  The type of encoding the file reader will use when reading the file input streams.
     * @param _language  The language in which the files are expected; this may affect some annotators.
     * @param _recursion Determines whether the collection reader will read files in sub-directories or not.
     */
    public CombinedCorpusGoldCollectionReader(String _inputDir, String _encoding, String _language, boolean _recursion) {
        setConfigParameterValue(INPUT_DIRECTORY_PARAMETER, _inputDir);
        setConfigParameterValue(RECURSIVE_PARAMETER, _recursion);
        setConfigParameterValue(ENCODING_PARAMETER, _encoding);
        setConfigParameterValue(LANGUAGE_PARAMETER, _language);
    }

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        // If the input directory does not exist, the application will log the exception and exit.
        if (fileExtension.charAt(0) == '.') {
            fileExtension = fileExtension.substring(1);
        }
        try {
            File inputDirectory = new File(inputDirString);
            if (inputDirectory.isDirectory()) {
                getAllFilesFromDirectory(inputDirectory, fileExtension);
            } else {
                throw new ResourceInitializationException(new NotDirectoryException("Not a directory"));
            }
        } catch (FileNotFoundException e) {
            LogManager.getLogger().throwing(e);
            System.exit(-1);
        }
    }

    @Override
    public void getNext(JCas jCas) throws IOException, CollectionException {
        File currentFile = fileList.get(currentIndex);
        logger.debug("{}", () -> "Loading document: " + currentFile.getAbsolutePath() + "  " + currentIndex + " of " + fileList.size());
        int id = 0;
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            org.jdom.Document document = saxBuilder.build(currentFile);
            Element classElement = document.getRootElement();
            List<Element> list = classElement.getChildren();
            boolean gotText = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getName().equals("Sofa")) {//I don't think this is needed anymore
                    jCas.setDocumentText(list.get(i).getAttribute("sofaString").getValue());
                    if (gotText) {
                        logger.debug("Got text from both Sofa and TEXT");
                    }
                    gotText = true;
                } else if (list.get(i).getName().equals("TEXT")) {
                    String text = list.get(i).getText();
                    jCas.setDocumentText(text);
                    if (gotText) {
                        logger.debug("Got text from both Sofa and TEXT");
                    }
                    gotText = true;
                } else if (isTraining && list.get(i).getName().equals("PHI")) {//custom:PHI?
                    String subType = list.get(i).getAttributeValue("TYPE");
                    if (!piiParentTypeMap.containsKey(subType)) {
                        if (!subTypeMuscToDeid.containsKey(subType)) {
                            logger.error("{}", subType);
                        }
                        subType = subTypeMuscToDeid.get(subType);
                    }

                    if (subType == null) {
                        int i2 = i;
                        logger.error("{}", () -> "type is null for " + currentFile + " i: " + i2 + ", item: " + list.get(i2).getAttributes().toString());
                        continue;
                    }
                    int start = Integer.parseInt(list.get(i).getAttributeValue("begin"));
                    int end = Integer.parseInt(list.get(i).getAttributeValue("end"));
                    if (piiParentTypeMap.containsKey(subType) && start < end) {//known pii subtype name
                        Util.addPii(jCas, start, end, piiParentTypeMap.get(subType), subType, "P" + String.valueOf(id), "REF", 1.0f);
                        id++;
                    } else {
                        int i2 = i;
                        String t = subType;
                        logger.error("{}", () -> "unknown TYPE or start !< end " + start + "-" + end + " type: " + t + " " + list.get(i2));
                    }
                }
//                else if (!"NULL".equals(list.get(i).getName())) {//this seems flaky
//                    int i2 = i;
//                    logger.log(PII_LOG, "{}", () -> "ERROR: unknown  name " + list.get(i2).getName() + " in entry " + list.get(i2) + " " + list.get(i2).getText());
//                }
            }
            if (!gotText) {
                logger.error("No TEXT or Sofa found");
            }
        } catch (JDOMException e) {
            throw new CollectionException(e);
        }

        DocumentInformationAnnotation documentInformation = new DocumentInformationAnnotation(jCas);
        documentInformation.setDocumentType("txt");
        documentInformation.setFilePath(currentFile.getAbsolutePath());
        documentInformation.setFileSize((int) currentFile.length());
        documentInformation.setFileName(currentFile.getName().split("\\.")[0]);
        documentInformation.addToIndexes();

        jCas.setDocumentLanguage(language);

        currentIndex++;
//        for (PiiAnnotation pii : jCas.getAnnotationIndex(PiiAnnotation.class)) {
//            logger.log(PII_LOG, "{}", () -> String.format("3 %5d-%5d %20s %20s (%s)", pii.getBegin(), pii.getEnd(), pii.getPiiType(), pii.getPiiSubtype(), pii.getCoveredText()));
//        }
        logger.debug("{}", () -> "Loaded document: " + currentFile.getAbsolutePath() + ", length: " + jCas.getDocumentText().length() + " " + currentIndex + " of " + fileList.size());
    }

}
