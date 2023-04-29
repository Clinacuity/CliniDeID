
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

import com.clinacuity.deid.type.DocumentInformationAnnotation;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class FileSystemCollectionReader extends GeneralCollectionReader {
    public static final String INPUT_DIRECTORY_PARAMETER = "inputDirString";
    public static final String ENCODING_PARAMETER = "encoding";
    public static final String LANGUAGE_PARAMETER = "language";
    public static final String RECURSIVE_PARAMETER = "recursiveDirectories";

    private static final Logger LOGGER = LogManager.getLogger();
    @ConfigurationParameter(name = INPUT_DIRECTORY_PARAMETER, defaultValue = ".", mandatory = false,
            description = "The directory from which the pipeline will consume files.")
    protected String inputDirString;
    @ConfigurationParameter(name = ENCODING_PARAMETER, defaultValue = "UTF-8", mandatory = false,
            description = "The type of encoding the file reader will use when reading the file input streams.")
    protected String encoding;
    @ConfigurationParameter(name = LANGUAGE_PARAMETER, defaultValue = "en-us", mandatory = false,
            description = "The language in which the files are expected; this may affect some annotators.")
    protected String language;
    @ConfigurationParameter(name = RECURSIVE_PARAMETER, defaultValue = "false", mandatory = false,
            description = "Determines whether the collection reader will read files in sub-directories or not.")
    protected boolean isRecursive;

    protected List<File> fileList = new ArrayList<>();
    protected int currentIndex = 0;

    protected String extension;

    public FileSystemCollectionReader() {
    }

    /**
     * Initializes the collection reader with the specified parameters
     *
     * @param _inputDir  The directory from which the pipeline will consume files.
     * @param _recursion Determines whether the collection reader will read files in sub-directories or not.
     */
    public FileSystemCollectionReader(String _inputDir, boolean _recursion) {
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
    public FileSystemCollectionReader(String _inputDir, String _encoding, String _language, boolean _recursion) {
        setConfigParameterValue(INPUT_DIRECTORY_PARAMETER, _inputDir);
        setConfigParameterValue(RECURSIVE_PARAMETER, _recursion);
        setConfigParameterValue(ENCODING_PARAMETER, _encoding);
        setConfigParameterValue(LANGUAGE_PARAMETER, _language);
    }

    public int getFileCount() {
        return fileList.size();
    }

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        // If the input directory does not exist, the application will log the exception and exit.
        if (inputCda) {
            extension = "xml";
        } else {
            extension = "txt";
        }
        try {
            File inputDirectory = new File(inputDirString);
            if (fileLimit <= 0) {
                throw new ResourceInitializationException(" fileLimit is < = 0, please contact support@clinacuity.com", null);
            } else if (inputDirectory.isDirectory()) {
                getAllFilesFromDirectory(inputDirectory);
                if (getFileCount() == 0) {
                    throw new ResourceInitializationException("No text files found in " + inputDirString, null);
                }
            } else {
                throw new ResourceInitializationException(new NotDirectoryException("Not a directory:" + inputDirString));
            }
        } catch (FileNotFoundException e) {
            LOGGER.throwing(e);
            throw new ResourceInitializationException(e);
        }

        String[] properties = {"os.name", "os.version", "os.arch", "file.encoding",
                "java.specification.version", "java.runtime.name", "java.vm.name=", "java.version"};
        for (String prop : properties) {
            LOGGER.debug("{}", () -> prop + "     " + System.getProperty(prop));
        }
    }

    @Override
    public void getNext(JCas jCas) throws IOException, CollectionException {
        File currentFile = fileList.get(currentIndex);
        int lastDot = currentFile.getName().lastIndexOf('.');
        String name = currentFile.getName().substring(0, lastDot);
        DocumentInformationAnnotation documentInformation = makeSourceDocumentInformation(jCas, name, currentFile.getAbsolutePath(), (int) currentFile.length());
        jCas.setDocumentLanguage(language);
        makePiiOptionMapAnnotation(jCas);
        //unknown if FileUtils properly throw exceptions for encoding errors
//        InputStreamReader char_input = new InputStreamReader(
//                new FileInputStream("some_input.utf8"),
//                Charset.forName("UTF-8").newDecoder()
//        );
//        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
//        encoder.onMalformedInput(CodingErrorAction.REPORT);
//        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        if (inputCda) {
            documentInformation.setOriginalXml(FileUtils.file2String(currentFile, encoding));
            jCas.setDocumentText(CdaXmlToText.process(jCas, currentFile));
            documentInformation.setOutputToCda(true);
        } else {
            jCas.setDocumentText(FileUtils.file2String(currentFile, encoding));
            documentInformation.setOutputToCda(false);
        }
        currentIndex++;
        LOGGER.debug("{}", () -> "Loaded document: " + documentInformation.getFilePath() + ", length: " + jCas.getDocumentText().length() + " " + currentIndex + " of " + fileList.size());
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public boolean hasNext() throws IOException, CollectionException {
        return currentIndex < fileList.size() && currentIndex < fileLimit;
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[]{
                new ProgressImpl(currentIndex, fileList.size(), Progress.ENTITIES)
        };
    }

    /**
     * If the input directory does not exist, the application will log and throw the exception.
     */

    protected void getAllFilesFromDirectory(File directory) throws FileNotFoundException {
        getAllFilesFromDirectory(directory, extension);
    }

    protected void getAllFilesFromDirectory(File directory, String extension) throws FileNotFoundException {
        if (directory.exists()) {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (!file.isHidden()) {
                    if (file.isFile() && FilenameUtils.getExtension(file.toString()).equals(extension)) {
                        if (file.length() < fileSizeLimit) {
                            fileList.add(file);
                        }// TODO: this quietly ignores files that are too large, should inform user
                    } else if (file.isFile() && file.toString().indexOf('.') != -1 && file.toString().substring(file.toString().indexOf('.') + 1).equals(extension)) {
                        if (file.length() < fileSizeLimit) {
                            fileList.add(file);
                        }
                    } else {
                        if (file.isDirectory() && isRecursive) {
                            getAllFilesFromDirectory(file);
                        }
                    }
                }

                if (fileList.size() >= fileLimit) {
                    break;
                }
            }
        } else {
            throw new FileNotFoundException();
        }
    }

    public void setMaximumProcessableFiles(int maximumProcessableFiles) {
        if (fileLimit > maximumProcessableFiles) {
            fileLimit = maximumProcessableFiles;
        }
    }

    @Override
    public String getLastFilenameProcessed() {
        if (currentIndex < 1) {
            return "";
        }
        return fileList.get(currentIndex - 1).getAbsolutePath();
    }
}
