
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

package com.clinacuity.deid.outputAnnotators.resynthesis;

import com.clinacuity.clinideid.message.DeidLevel;
import com.clinacuity.deid.outputAnnotators.CdaTextToXml;
import com.clinacuity.deid.outputAnnotators.resynthesis.fromFile.ResynthCity;
import com.clinacuity.deid.outputAnnotators.resynthesis.fromFile.ResynthCountry;
import com.clinacuity.deid.outputAnnotators.resynthesis.fromFile.ResynthHealthCareUnitName;
import com.clinacuity.deid.outputAnnotators.resynthesis.fromFile.ResynthOtherGeo;
import com.clinacuity.deid.outputAnnotators.resynthesis.fromFile.ResynthOtherOrg;
import com.clinacuity.deid.outputAnnotators.resynthesis.fromFile.ResynthProfession;
import com.clinacuity.deid.outputAnnotators.resynthesis.names.ResynthNamePatient;
import com.clinacuity.deid.outputAnnotators.resynthesis.names.ResynthNameProvider;
import com.clinacuity.deid.outputAnnotators.resynthesis.names.ResynthNameRelative;
import com.clinacuity.deid.outputAnnotators.resynthesis.randomChar.ResynthOtherIDNumber;
import com.clinacuity.deid.outputAnnotators.resynthesis.randomChar.ResynthPhoneFax;
import com.clinacuity.deid.outputAnnotators.resynthesis.randomChar.ResynthSsn;
import com.clinacuity.deid.outputAnnotators.resynthesis.randomChar.ResynthZipcode;
import com.clinacuity.deid.readers.CdaXmlToText;
import com.clinacuity.deid.readers.GeneralCollectionReader;
import com.clinacuity.deid.type.DocumentInformationAnnotation;
import com.clinacuity.deid.type.PiiAnnotation;
import com.clinacuity.deid.type.PiiOptionMapAnnotation;
import com.clinacuity.deid.util.ConnectionProperties;
import com.clinacuity.deid.util.PiiOptions;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static com.clinacuity.deid.mains.DeidPipeline.PII_LOG;

public class ResynthesisAnnotator extends JCasAnnotator_ImplBase {
    public static final String OUTPUT_EXTENSION_PARAM = "fileExtension";
    public static final String SAVE_FOR_SERVICE = "saveForService";
    public static final String OUTPUT_TO_FILE = "outputToFile";
    public static final String OUTPUT_TO_DB = "outputToDb";
    private static final Logger LOGGER = LogManager.getLogger();

    private static String resynthesizedOutput;
    Map<String, Resynthesizer> resynthesizers = new HashMap<>();
    //possible is output directly to file, output goes to static variable used by demo service or output to DB
    @ConfigurationParameter(name = OUTPUT_TO_FILE, description = "Decides whether to print to file ")
    private boolean outputToFile;
    @ConfigurationParameter(name = SAVE_FOR_SERVICE, defaultValue = "false", mandatory = false, description = "Decides whether to store in memory")
    private boolean saveForService;
    @ConfigurationParameter(name = OUTPUT_TO_DB, defaultValue = "false", mandatory = false, description = "Decides whether to save to DB")
    private boolean outputToDb;
    @ConfigurationParameter(name = OUTPUT_EXTENSION_PARAM, defaultValue = ".resynthesis", mandatory = false, description = "The file extension to append to the output")
    private String fileExtension;
    private PreparedStatement psStmt;

    //this is showing as not used, but I thought service needed it? TODO: check this, remove if can
    public static String getResynthesizedOutput() {
        return resynthesizedOutput;
    }

    public static void setResynthesizedOutput(String text) {//for Tests only
        resynthesizedOutput = text;
    }

    //this was added around 4/2/2019 and I thought it was needed, but not sure why it isn't called
    //maybe a security issue with possible Pii in static place?
    public static void clearResynthesizedOutput() {
        resynthesizedOutput = "";
    }


    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        if (!outputToDb && !outputToFile && !saveForService) {
            throw new ResourceInitializationException("No output destination", null);
        }

        //TODO: connect to DB of mappings

        if (outputToDb) {
            Connection connection = ConnectionProperties.getInstance().getConnection();
            try {
                psStmt = Utilities.getInsertPreparedStatement(connection);
            } catch (SQLException e) {
                LOGGER.throwing(e);
                throw new ResourceInitializationException(e);
            }
        }
        try {
            resynthesizers.put("Age", new ResynthAge());
            resynthesizers.put("ClockTime", new ResynthClockTime());
            resynthesizers.put("Date", new ResynthDate());
            resynthesizers.put("DayOfWeek", new ResynthDayOfWeek());
            resynthesizers.put("ElectronicAddress", new ResynthElectronicAddress());
            resynthesizers.put("HealthCareUnitName", new ResynthHealthCareUnitName());
            resynthesizers.put("OtherGeo", new ResynthOtherGeo());
            resynthesizers.put("OtherIDNumber", new ResynthOtherIDNumber());
            resynthesizers.put("OtherOrgName", new ResynthOtherOrg());
            resynthesizers.put("Patient", new ResynthNamePatient());
            resynthesizers.put("PhoneFax", new ResynthPhoneFax());
            resynthesizers.put("Profession", new ResynthProfession());
            resynthesizers.put("Provider", new ResynthNameProvider());
            resynthesizers.put("Relative", new ResynthNameRelative());
            resynthesizers.put("SSN", new ResynthSsn());
            resynthesizers.put("Season", new ResynthSeason());
            resynthesizers.put("State", new ResynthState());
            resynthesizers.put("Country", new ResynthCountry());
            resynthesizers.put("City", new ResynthCity());
            resynthesizers.put("Street", new ResynthStreet());
            resynthesizers.put("Zip", new ResynthZipcode());
            //Date Season DayOfWeek ClockTime Age Profession Patient Provider Relative OtherIDNumber SSN
            //Street City State Country Zip HealthCareUnitName OtherOrgName OtherGeo PhoneFax ElectronicAddress
            //TEMPORAL OCCUPATION NAME IDENTIFIER ADDRESS1 LOCATION CONTACT_INFORMATION

        } catch (RuntimeException e) {//catch any file I/O issues or anything during above construtions
            LOGGER.throwing(e);
            throw new ResourceInitializationException(e);
        }
        LOGGER.debug("ResynthesisAnnotator initialized");
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        String fileName = Utilities.getFileName(jCas);
        DocumentInformationAnnotation docInfo = JCasUtil.selectSingle(jCas, DocumentInformationAnnotation.class);
        if (docInfo.getResynthesisSelected()) {
            LOGGER.debug("{}", () -> "ResynthesisAnnotator begin on " + fileName);
        } else {
            return;
        }
        DeidLevel deidLevel = DeidLevel.valueOf(docInfo.getLevel());//probably a separate annotation once full options are available

        String text = jCas.getDocumentText();
        StringBuilder textSb = new StringBuilder(text);
        PiiOptionMapAnnotation piiOptionMapAnnotation;
        try {//TODO this is only until service is updated to get PiiOptions instead of level
            piiOptionMapAnnotation = JCasUtil.selectSingle(jCas, PiiOptionMapAnnotation.class);
        } catch (IllegalArgumentException e) {
            piiOptionMapAnnotation = GeneralCollectionReader.makePiiOptionMapAnnotation(jCas, new PiiOptions(deidLevel));
        }

        //TODO: check DB if map exists for this record, probably via new ID field added to DocumentInformationAnnotation
        //else create a new empty map, if map is created, automatially create new offset for age, days, minutes, and years (not all levels would need all)
        ResynthesisMap resynthesisMap = new ResynthesisMap();
        for (Resynthesizer resynthesizer : resynthesizers.values()) {
            resynthesizer.setMap(resynthesisMap);
            resynthesizer.setLevel(deidLevel);
            resynthesizer.setPiiOptions(piiOptionMapAnnotation);
        }

        //iterate through PII to add names as keys for new map
        //TODO: using any available info from structured data, determine first/last names and initials and such

        ((ResynthNamePatient) resynthesizers.get("Patient")).createOrUpdateNameMappings(jCas);
        ((ResynthNameProvider) resynthesizers.get("Provider")).createOrUpdateNameMappings(jCas);
        ((ResynthNameRelative) resynthesizers.get("Relative")).createOrUpdateNameMappings(jCas);

        //iterate through PII starting from end. Replace with either mapped value (if exists) or new value (and insert into map)
        //separate methods for each type in their own classes for modularity
        // above done with inheritance for the method and then map from piiSubtype to class implementing base

        FSIterator<PiiAnnotation> iter = jCas.getAnnotationIndex(PiiAnnotation.class).iterator();
        iter.moveToLast();
        while (iter.hasPrevious()) {
            PiiAnnotation item = iter.get();
            //check for type and go to map for converter

            if (!resynthesizers.containsKey(item.getPiiSubtype())) {
                LOGGER.error("pii subtype {} not found in resynthesizers", item.getPiiSubtype());
            } else {
                String newValue = resynthesizers.get(item.getPiiSubtype()).getAndUpdateResynthesizedValue(item.getCoveredText());
                if (newValue.length() == 0) {
                    LOGGER.log(PII_LOG, "{}", () -> "problem with newValue " + newValue + " from " + item.getCoveredText() + " of type " + item.getPiiSubtype());
                } else {
                    if (docInfo.getOutputToCda()) {
                        int oldCount = CdaXmlToText.countNewLines(item.getCoveredText());
                        int newCount = CdaXmlToText.countNewLines(newValue);
                        if (newCount == oldCount) {
                            textSb.replace(item.getBegin(), item.getEnd(), newValue);
                        } else if (newCount < oldCount) {
                            textSb.replace(item.getBegin(), item.getEnd(), newValue + "\n".repeat(oldCount - newCount));
                        } else {//resynth text having more newlines then original shouldn't happen, but just in case
                            //replace \n with space until count is OKO.
                            String newValueMinusNewlines=newValue;
                            for (int count = oldCount; count < newCount; count++) {
                                newValueMinusNewlines = replaceFirst(newValueMinusNewlines, "\n", "");//String.replaceFirst uses regex
                            }
                            textSb.replace(item.getBegin(), item.getEnd(), newValueMinusNewlines);
                        }
                    } else {
                        textSb.replace(item.getBegin(), item.getEnd(), newValue);
                    }
                }
            }
            iter.moveToPrevious();
        }

        //TODO: save map
        String output = "";
        if (docInfo.getOutputMapToFile()) {
            String outputDirectoryMap = docInfo.getOutputMapDirectory();
            String mapOutput = resynthesisMap.getOutput();
            Utilities.writeText(outputDirectoryMap, fileName, ".map.txt", mapOutput);
        }
        if (docInfo.getOutputToCda()) {
            output = CdaTextToXml.process(jCas, textSb.toString());
        } else {
            output = textSb.toString();
        }
        if (outputToFile) {
            String outputDirectory = docInfo.getResynthesisDirectory();
            Utilities.writeText(outputDirectory, fileName, fileExtension, output);
        }
        if (outputToDb) {
            Utilities.dbInsertPreparedQuery(jCas, psStmt, output, "Resynthesized");
        }
        if (saveForService) {
            resynthesizedOutput = output;
        } else {
            resynthesizedOutput = "";
        }
    }

    /**
     * Replaces the first subsequence of the <tt>source</tt> string that matches
     * the literal target string with the specified literal replacement string.
     *
     * @param source source string on which the replacement is made
     * @param toFind the string to be replaced
     * @param replacement the replacement string
     * @return the resulting string
     */
    private static String replaceFirst(String source, String toFind, String replacement) {
        int index = source.indexOf(toFind);
        if (index == -1) {
            return source;
        }

        return source.substring(0, index)
                .concat(replacement)
                .concat(source.substring(index+toFind.length()));
    }
}
//TODO this was from previous version which handled ClockTime before the rest, this code updated the spans of the rest
//We may need this again if trying to resynthesize and preserve/update spans
//        int spanOffset = 0;
//        for (PiiAnnotation annotation : jCas.getAnnotationIndex(PiiAnnotation.class)) {
//            annotation.setBegin(annotation.getBegin() + spanOffset);
//            annotation.setEnd(annotation.getEnd() + spanOffset);
//            String piiType = annotation.getPiiSubtype();
//            if ("ClockTime".equals(piiType)) {//adjust offset (from previous changes), process and change text in textSb, get new offset
//                int oldOffset = spanOffset;
//                spanOffset += processClockTime(textSb, annotation, resynthesisMap.getMinutesOffset());
//                if (oldOffset != spanOffset) {
//                    annotation.setEnd(annotation.getEnd() + (spanOffset - oldOffset));
//                }
//            }
//        }
