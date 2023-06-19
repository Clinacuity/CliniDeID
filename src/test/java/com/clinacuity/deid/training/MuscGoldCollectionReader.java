
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
import com.clinacuity.deid.type.PiiAnnotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MuscGoldCollectionReader extends FileSystemCollectionReader {//JCasCollectionReader_ImplBase {
    //    public static final String INPUT_DIRECTORY_PARAMETER = "inputDirString";
//    public static final String OUTPUT_DIRECTORY_PARAMETER = "outputDirString";
//    public static final String ENCODING_PARAMETER = "encoding";
//    public static final String LANGUAGE_PARAMETER = "language";
//    public static final String RECURSIVE_PARAMETER = "recursiveDirectories";
    private static final Logger logger = LogManager.getLogger();
    private static final Map<String, Set<String>> piiAttributes = createPiiMap();//map Pii attribute names to set of possible values
    private static final Map<String, String> piiConversion = createConversionMap();
    private static final Map<String, String> parentMap = NamedEntityChunker.createSubToParentTypeMap();
    public static boolean isTraining = false;
    //    @ConfigurationParameter(name = INPUT_DIRECTORY_PARAMETER, defaultValue = ".", mandatory = false,
//            description = "The directory from which the pipeline will consume files.")
//    private String inputDirString;
//    @ConfigurationParameter(name = OUTPUT_DIRECTORY_PARAMETER, defaultValue = "./output", mandatory = false,
//            description = "The directory into which the pipeline will save annotated files.")
//    private String outputDirString;
//    @ConfigurationParameter(name = ENCODING_PARAMETER, defaultValue = "UTF-8", mandatory = false,
    //           description = "The type of encoding the file reader will use when reading the file input streams.")
    //  private String encoding;
    //@ConfigurationParameter(name = LANGUAGE_PARAMETER, defaultValue = "en-us", mandatory = false,
    //      description = "The language in which the files are expected; this may affect some annotators.")
    //   private String language;
    // @ConfigurationParameter(name = RECURSIVE_PARAMETER, defaultValue = "false", mandatory = false,
    //        description = "Determines whether the collection reader will read files in sub-directories or not.")
    //private boolean isRecursive;
    //private List<File> fileList = new ArrayList<>();
    //private int currentIndex = 0;
    private int id = 0;

    public MuscGoldCollectionReader() {
    }

    /**
     * Initializes the collection reader with the specified parameters
     *
     * @param _inputDir  The directory from which the pipeline will consume files.
     * @param _recursion Determines whether the collection reader will read files in sub-directories or not.
     */
    public MuscGoldCollectionReader(String _inputDir, boolean _recursion) {
        setConfigParameterValue(INPUT_DIRECTORY_PARAMETER, _inputDir);
        setConfigParameterValue(RECURSIVE_PARAMETER, _recursion);
    }

    /**
     * Initializes the collection reader with the specified parameters
     *
     * @param _inputDir  The directory from which the pipeline will consume files.
     * @param _encoding  The type of encoding the file reader will use when reading the file input streams.
     * @param _language  The language in which the files are expected; this may affect some annotators.
     * @param _recursion Determines whether the collection reader will read files in sub-directories or not.
     */
    public MuscGoldCollectionReader(String _inputDir, String _encoding, String _language, boolean _recursion) {
        setConfigParameterValue(INPUT_DIRECTORY_PARAMETER, _inputDir);
        setConfigParameterValue(RECURSIVE_PARAMETER, _recursion);
        setConfigParameterValue(ENCODING_PARAMETER, _encoding);
        setConfigParameterValue(LANGUAGE_PARAMETER, _language);
    }

    private static Map<String, String> createConversionMap() {//TODO: update once we get new MUSC corpus with DayOfWeek, Season, and Profession annotations
        Map<String, String> map = Map.of("DateTime", "Date", "HCUnit", "HealthCareUnitName", "eAddress", "ElectronicAddress",
                "OtherID", "OtherIDNumber", "OtherOrg", "OtherOrgName");
        return map;
    }

    private static Map<String, Set<String>> createPiiMap() {//TODO: This will need updated for Schema 2 with ClockTime, Season, DayOfWeek and Profession
        Map<String, Set<String>> piiAttribs = new HashMap<>();
        Set<String> list;

        list = new HashSet<>();
        list.add("DateTime");
        list.add("Age");
        piiAttribs.put("Time", list);
        list = new HashSet<>();
        list.add("SSN");
        list.add("OtherID");
        piiAttribs.put("Identifier", list);
        list = new HashSet<>();
        list.add("Provider");
        list.add("Relative");
        list.add("Patient");
        // list.add("OtherPerson");
        piiAttribs.put("Name", list);
        list = new HashSet<>();
        list.add("eAddress");
        list.add("PhoneFax");
        piiAttribs.put("Contact_Information", list);
        list = new HashSet<>();
        list.add("Zip");
        list.add("Street");
        list.add("City");
        list.add("State");
        list.add("Country");
        list.add("OtherGeo");
        piiAttribs.put("Address", list);
        list = new HashSet<>();
        list.add("OtherOrg");
        list.add("HCUnit");
        piiAttribs.put("Location", list);
        return piiAttribs;
    }

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        // If the input directory does not exist, the application will log the exception and exit.
        try {
            File inputDirectory = new File(inputDirString);
            if (inputDirectory.isDirectory()) {
                getAllFilesFromDirectory(inputDirectory, "xml");//TODO?? remove one?
                //getAllFilesFromDirectory(inputDirectory, "xmi");
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

        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            org.jdom.Document document = saxBuilder.build(currentFile);
            Element classElement = document.getRootElement();
            List<Element> list = classElement.getChildren();

            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getName().equals("Sofa")) {
                    jCas.setDocumentText(list.get(i).getAttribute("sofaString").getValue());
                } else if (isTraining && list.get(i).getName().equals("Pii")) {//cutom:PHI?
                    List<Attribute> attribs = list.get(i).getAttributes();
                    int start = 0;
                    int end = 0;
                    for (Attribute attrib : attribs) {
                        if ("begin".equals(attrib.getName())) {
                            start = attrib.getIntValue();
                        } else if ("end".equals(attrib.getName())) {
                            end = attrib.getIntValue();
                        } else if (piiAttributes.containsKey(attrib.getName())) {//known attribute name
                            Set<String> values = piiAttributes.get(attrib.getName());
                            if (values.contains(attrib.getValue())) {//known attribute value, create gold annotation for training
                                //GSKnowtatorAnnotation crfAnnotation = new GSKnowtatorAnnotation(jCas, start, end);
                                PiiAnnotation piiAnnotation = new PiiAnnotation(jCas, start, end);
                                piiAnnotation.setPiiSubtype(piiConversion.getOrDefault(attrib.getValue(), attrib.getValue()));
                                piiAnnotation.setPiiType(parentMap.get(piiAnnotation.getPiiSubtype()));//attrib.getName());
                                piiAnnotation.setConfidence(1.0f);
                                piiAnnotation.setMethod("Gold");
                                piiAnnotation.setId("P" + id);
                                id++;
                                piiAnnotation.addToIndexes();
                            } else {
                                logger.error("unknown value {}, should be in {} for attribute name {} in entry: {}", attrib.getValue(), values.toString(), attrib.getName(), list.get(i));
                            }
                        } else if (!("id".equals(attrib.getName()) || "sofa".equals(attrib.getName()))) {//this seems flaky
                            logger.error("unknown attribute name {} in entry: {}", attrib.getName(), list.get(i));
                        }
                    }
                }
            }
        } catch (JDOMException e) {
            throw new CollectionException(e);
        }

        DocumentInformationAnnotation documentInformation = new DocumentInformationAnnotation(jCas);
        documentInformation.setDocumentType("txt");
        documentInformation.setFilePath(currentFile.getAbsolutePath());
        documentInformation.setFileSize((int) currentFile.length() / 1024);
        documentInformation.setFileName(currentFile.getName().split("\\.")[0]);
        documentInformation.addToIndexes();

        jCas.setDocumentLanguage(language);

        currentIndex++;
        logger.debug("Loading document: {}", documentInformation.getFilePath());
    }

}
