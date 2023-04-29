
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

package com.clinacuity.deid.readers;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

import com.clinacuity.deid.type.DocumentInformationAnnotation;
import com.clinacuity.deid.type.PiiOptionMapAnnotation;
import com.clinacuity.deid.util.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple collection reader that reads CASes in XMI format from a directory in the filesystem.
 */
public class XmlCollectionReaderClearTkTraining extends GeneralCollectionReader {//} JCasCollectionReader_ImplBase {// CollectionReader_ImplBase {
    /**
     * Name of configuration parameter that must be set to the path of a directory containing the XMI files.
     */
    public static final String PARAM_INPUTDIR = "inputDirectory";
    /**
     * Name of the configuration parameter that must be set to indicate if the execution fails if an encountered type is unknown
     */
    public static final String PARAM_FAILUNKNOWN = "failOnUnknownType";
    private static final Logger LOGGER = LogManager.getLogger();
    @ConfigurationParameter(name = PARAM_INPUTDIR)
    private String inputDirectory;
    @ConfigurationParameter(name = PARAM_FAILUNKNOWN)
    private String failOnUnknownType;

    private Boolean mFailOnUnknownType;

    private ArrayList<File> mFiles;

    private int mCurrentIndex;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context); // should this be added?
        // mFailOnUnknownType = (Boolean) getConfigParameterValue(PARAM_FAILUNKNOWN);
        mFailOnUnknownType = Boolean.valueOf(failOnUnknownType);
        if (null == mFailOnUnknownType) {
            mFailOnUnknownType = true; // default to true if not specified
        }
        // File directory = new File(((String) getConfigParameterValue(PARAM_INPUTDIR)).trim());
        File directory = new File(inputDirectory);
        mCurrentIndex = 0;

        // if input directory does not exist or is not a directory, throw exception
        if (!directory.exists() || !directory.isDirectory()) {
            throw new ResourceInitializationException(ResourceConfigurationException.DIRECTORY_NOT_FOUND,
                    new Object[]{PARAM_INPUTDIR, this.getMetaData().getName(), directory.getPath()});
        }

        // get list of .xmi files in the specified directory
        mFiles = new ArrayList<>();
        File[] files = directory.listFiles();
        for (File file : files) {
            if (!file.isDirectory() && (file.getName().endsWith(".xmi") || file.getName().endsWith(".xml"))) {
                mFiles.add(file);
            }
        }
    }

    public int getFileCount() {
        return mFiles.size();
    }

    public void setMaximumProcessableFiles(int x) {
    }  //just for inheritance

    @Override
    public String getLastFilenameProcessed() {
        if (mCurrentIndex < 1) {
            return "";
        }
        return mFiles.get(mCurrentIndex - 1).getAbsolutePath();
    }

    public boolean hasNext() {
        return mCurrentIndex < mFiles.size();
    }

    public void getNext(JCas jCas) throws IOException, CollectionException {
        File currentFile = mFiles.get(mCurrentIndex);
        LOGGER.debug("Loading document: {}, {} of {}", currentFile.getAbsolutePath(), mCurrentIndex + 1, mFiles.size());
        mCurrentIndex++;
        int lastDot = currentFile.getName().lastIndexOf('.');
        String name = currentFile.getName().substring(0, lastDot);
        try {//(FileInputStream inputStream = new FileInputStream(currentFile)) {
//            XmiCasDeserializer.deserialize(inputStream, jCas.getCas(), !mFailOnUnknownType);
            Serializer.DeserializeJcasFromFile(jCas, currentFile.getAbsolutePath());//TODO: redo init to get list of filenames instead of File[]
        } catch (SAXException e) {
            throw new CollectionException(e);
        }
        try {
            DocumentInformationAnnotation doc = JCasUtil.selectSingle(jCas, DocumentInformationAnnotation.class);
            doc.removeFromIndexes();
        } catch (IllegalArgumentException e) {//probably won't have it, but just in case it is there remove it
        }
        try {
            PiiOptionMapAnnotation doc = JCasUtil.selectSingle(jCas, PiiOptionMapAnnotation.class);
            doc.removeFromIndexes();
        } catch (IllegalArgumentException e) {//probably won't have it, but just in case it is there remove it
        }
        makeSourceDocumentInformation(jCas, name, currentFile.getAbsolutePath(), (int) currentFile.length());
        makePiiOptionMapAnnotation(jCas);
    }

    public Progress[] getProgress() {
        return new Progress[]{new ProgressImpl(mCurrentIndex, mFiles.size(), Progress.ENTITIES)};
    }
}
