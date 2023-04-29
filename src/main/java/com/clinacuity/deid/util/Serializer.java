
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

package com.clinacuity.deid.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.apache.uima.cas.impl.XmiCasDeserializer.deserialize;

public class Serializer {
    private static final Logger LOGGER = LogManager.getLogger();

    private Serializer() {
    }

    /**
     * This method serializes a JCas object to a file.  It has an option to pretty-print the file.
     *
     * @param jCas           The jCas object to be serialized
     * @param outputFilePath The target file to which the serialized jCas will be saved.
     */
    public static void SerializeToXmi(JCas jCas, String outputFilePath) throws SAXException, IOException {
        try (FileOutputStream output = new FileOutputStream(outputFilePath);) {
            XmiCasSerializer.serialize(jCas.getCas(), output);
        }
    }

    public static void SerializeToXmi(JCas jCas, String outputFilePath, boolean prettyPrint) throws SAXException, IOException {
        try (FileOutputStream output = new FileOutputStream(outputFilePath);) {
            XmiCasSerializer.serialize(jCas.getCas(), jCas.getTypeSystem(), output, prettyPrint, null);
        }
    }

    /**
     * This method deserializes a file and returns a JCas. It takes in a JCas object to ensure the type system description and priorities and the analysis
     * engine meta data all match.
     *
     * @param jcas     The JCas which will be used to determine the type system and pipeline
     * @param filePath The path to the XMI file
     * @return A JCas object matching the input JCas, but with deserialized annotations and text from the file.
     */
    public static void DeserializeJcasFromFile(JCas jcas, String filePath) throws SAXException, IOException {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            DeserializeJcasFromFile(jcas, fileInputStream);
        }
    }

    public static void DeserializeJcasFromFile(JCas jcas, FileInputStream fileInputStream) throws SAXException, IOException {
        CAS cas = jcas.getCas();
        try {
            deserialize(fileInputStream, cas);
        } catch (SAXException | IOException e) {
            LOGGER.throwing(e);
            throw e;
        }
    }

}
