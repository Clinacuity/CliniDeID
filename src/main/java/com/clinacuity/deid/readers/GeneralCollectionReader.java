
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

package com.clinacuity.deid.readers;

import com.clinacuity.clinideid.message.DeidLevel;
import com.clinacuity.deid.type.DocumentInformationAnnotation;
import com.clinacuity.deid.type.PiiOptionMapAnnotation;
import com.clinacuity.deid.util.PiiOptions;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

public abstract class GeneralCollectionReader extends JCasCollectionReader_ImplBase implements CollectionReader {
    public static final String FILE_LIMIT = "fileLimit";
    public static final String FILE_SIZE_LIMIT = "fileSizeLimit";
    public static final String INPUT_CDA = "inputCda";
    public static final String DEID_LEVEL = "level";
    public static final String OUTPUT_CLEAN = "outputClean";
    public static final String OUTPUT_GENERAL_TAG = "outputGeneralTag";
    public static final String OUTPUT_CATEGORY_TAG = "outputCategoryTag";
    public static final String OUTPUT_PII = "outputPii";
    public static final String OUTPUT_RESYNTHESIS = "outputResynthesis";
    public static final String OUTPUT_RESYNTHESIS_MAP = "outputResynthesisMap";
    public static final String OUTPUT_RAW = "outputRaw";
    public static final String CLEAN_DIRECTORY = "cleanDirectory";
    public static final String GENERAL_TAG_DIRECTORY = "generalTagDirectory";
    public static final String CATEGORY_TAG_DIRECTORY = "categoryTagDirectory";
    public static final String PII_DIRECTORY = "piiDirectory";
    public static final String RESYNTHESIS_DIRECTORY = "resynthesisDirectory";
    public static final String RESYNTHESIS_MAP_DIRECTORY = "resynthesisMapDirectory";
    public static final String RAW_DIRECTORY = "rawDirectory";
    protected PiiOptions piiOptions = null;
    @ConfigurationParameter(name = INPUT_CDA, defaultValue = "false", mandatory = false, description = "Decides whether to read plain text or HL7 CDA XML format")
    protected boolean inputCda;
    @ConfigurationParameter(name = FILE_LIMIT, defaultValue = "0", mandatory = false, description = "Determines how many files may be processed")
    protected int fileLimit;
    @ConfigurationParameter(name = FILE_SIZE_LIMIT, defaultValue = "0", mandatory = false, description = "Files larger than this property (in bytes) will be filtered out and not processed.")
    protected int fileSizeLimit;
    @ConfigurationParameter(name = DEID_LEVEL, mandatory = false)
    protected DeidLevel deidLevel = DeidLevel.defaultLevel;
    @ConfigurationParameter(name = OUTPUT_CLEAN, mandatory = false)
    protected boolean outputClean;
    @ConfigurationParameter(name = OUTPUT_GENERAL_TAG, mandatory = false)
    protected boolean outputGeneralTag;
    @ConfigurationParameter(name = OUTPUT_CATEGORY_TAG, mandatory = false)
    protected boolean outputCategoryTag;
    @ConfigurationParameter(name = OUTPUT_PII, mandatory = false)
    protected boolean outputPii;
    @ConfigurationParameter(name = OUTPUT_RESYNTHESIS, mandatory = false)
    protected boolean outputResynthesis;
    @ConfigurationParameter(name = OUTPUT_RESYNTHESIS_MAP, mandatory = false)
    protected boolean outputResynthesisMap;
    @ConfigurationParameter(name = OUTPUT_RAW, mandatory = false)
    protected boolean outputRaw;
    @ConfigurationParameter(name = CLEAN_DIRECTORY, mandatory = false, defaultValue = "")
    protected String cleanDirectory;
    @ConfigurationParameter(name = GENERAL_TAG_DIRECTORY, mandatory = false, defaultValue = "")
    protected String generalTagDirectory;
    @ConfigurationParameter(name = CATEGORY_TAG_DIRECTORY, mandatory = false, defaultValue = "")
    protected String categoryTagDirectory;
    @ConfigurationParameter(name = PII_DIRECTORY, mandatory = false, defaultValue = "")
    protected String piiDirectory;
    @ConfigurationParameter(name = RESYNTHESIS_DIRECTORY, mandatory = false, defaultValue = "")
    protected String resynthesisDirectory;
    @ConfigurationParameter(name = RAW_DIRECTORY, mandatory = false, defaultValue = "")
    protected String rawDirectory;
    @ConfigurationParameter(name = RESYNTHESIS_MAP_DIRECTORY, mandatory = false, defaultValue = "")
    protected String resynthesisMapDirectory;

//    public static PiiOptionAnnotation makePiiOptionAnnotation(JCas jCas, PiiOptions piiOptions) {
//        PiiOptionAnnotation annot = new PiiOptionAnnotation(jCas);
//
//        BooleanArray options = new BooleanArray(jCas, PII_TO_INDEX.size());
//        for (String piiSubtype : PII_TO_INDEX.keySet()) {
//            options.set(PII_TO_INDEX.get(piiSubtype), piiOptions.getOption(piiSubtype));
//        }
//
//        IntegerArray specialOptions = new IntegerArray(jCas, SPECIAL_TO_INDEX.size());
//        for (String piiSubtype : SPECIAL_TO_INDEX.keySet()) {
//            specialOptions.set(SPECIAL_TO_INDEX.get(piiSubtype), piiOptions.getSpecialOption(piiSubtype));
//        }
//        annot.setInclude(options);
//        annot.setSpecial(specialOptions);
//        annot.addToIndexes();
//        return annot;
//    }


    public static PiiOptionMapAnnotation makePiiOptionMapAnnotation(JCas jCas, PiiOptions piiOptions) {
        PiiOptionMapAnnotation annot = new PiiOptionMapAnnotation(jCas);
        annot.setIncludeMap(piiOptions.getOptionMap());
        annot.setSpecialMap(piiOptions.getSpecialMap());
        annot.addToIndexes();
        return annot;
    }

    public void setPiiOptions(PiiOptions piiOption) {
        piiOptions = new PiiOptions(piiOption);//make copy just in case
    }

    public PiiOptionMapAnnotation makePiiOptionMapAnnotation(JCas jCas) {
        if (piiOptions == null) {
            piiOptions = new PiiOptions(DeidLevel.defaultLevel);
        }
        return makePiiOptionMapAnnotation(jCas, piiOptions);
    }

    public abstract int getFileCount();

    public abstract void setMaximumProcessableFiles(int v);

    public abstract String getLastFilenameProcessed();

    protected DocumentInformationAnnotation makeSourceDocumentInformation(JCas jCas, String name, String path, int size) {
        DocumentInformationAnnotation docInfo = new DocumentInformationAnnotation(jCas);
        docInfo.setDocumentType("txt");//update elsewhere if needed
        docInfo.setFilePath(path);
        docInfo.setFileSize(size);
        docInfo.setFileName(name);
        docInfo.setLevel(deidLevel.toString());
        docInfo.setCleanXmiOutputSelected(outputClean);
        docInfo.setPiiTaggingCategorySelected(outputCategoryTag);
        docInfo.setPiiTaggingSelected(outputGeneralTag);
        docInfo.setPiiWriterSelected(outputPii);
        docInfo.setRawXmiOutputSelected(outputRaw);
        docInfo.setResynthesisSelected(outputResynthesis);
        docInfo.setOutputMapToFile(outputResynthesisMap);
        docInfo.setPiiTaggingGeneralDirectory(generalTagDirectory);
        docInfo.setPiiTaggingCategoryDirectory(categoryTagDirectory);
        docInfo.setPiiWriterDirectory(piiDirectory);
        docInfo.setRawXmiDirectory(rawDirectory);
        docInfo.setCleanXmiDirectory(cleanDirectory);
        docInfo.setResynthesisDirectory(resynthesisDirectory);
        docInfo.setOutputMapDirectory(resynthesisMapDirectory);
        docInfo.addToIndexes();
        return docInfo;
    }
}
