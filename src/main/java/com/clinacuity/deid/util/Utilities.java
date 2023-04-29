
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

import com.clinacuity.deid.type.DocumentInformationAnnotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Utilities {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ROOT_DIR = System.getProperty("user.dir") + "/data/";

    private Utilities() {
    }

    /**
     * Verifies whether a given path points to a directory. If it's a file (thus, not a directory), it will throw an exception. If the directory doesn't exist,
     * it gets created. If the creation fails, it throws an exception.
     *
     * @param path String to the directory. Can be either relative or absolute.
     */
    public static void verifyDirectory(String path) throws FileSystemException {
        File directory = new File(path);
        if (directory.exists()) {
            if (!directory.isDirectory()) {
                NotDirectoryException e = new NotDirectoryException("<" + path + ">  -- not a directory");
                LOGGER.throwing(e);
                throw e;
            }
        } else {
            if (directory.mkdirs()) {
                LOGGER.info("Created directory: {}", path);
            } else {
                FileSystemException e = new FileSystemException("Unable to create directory " + path);
                LOGGER.throwing(e);
                throw e;
            }
        }
    }

    public static PreparedStatement getInsertPreparedStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("Insert into NOTE_DEID (run_id, text_deid, pii_treatment) VALUES (?, ?, ?");
    }

    public static void dbInsertPreparedQuery(JCas jCas, PreparedStatement psStmt, String output, String categoryName) {
        long runId = Utilities.getRunId(jCas);
        try {
            psStmt.setLong(1, runId);
            psStmt.setString(2, output);
            psStmt.setString(3, categoryName);
            int ret = psStmt.executeUpdate();
            LOGGER.debug("DB Result: {}", ret);
        } catch (SQLException e) {
            LOGGER.throwing(e);
        }
    }

    /**
     * Returns a temporary file with the contents of the file within the jar package. If no file type is specified, the file is assumed to be a txt file.
     *
     * @param path Path to the contents within the jar package
     * @return A temporary file with the contents of the given file
     * @throws FileNotFoundException Thrown when the resource stream cannot be loaded, the temp file cannot be created, or the FileWriter cannot be closed
     */
    public static File getExternalFile(String path) throws FileNotFoundException {
        File file = new File(ROOT_DIR + path);
        if (!file.exists()) {
            FileNotFoundException ex = new FileNotFoundException(file.getAbsolutePath());
            LOGGER.error(ex);
            throw ex;
        }
        return file;
    }

    public static long getRunId(JCas jCas) {
        try {
            DocumentInformationAnnotation documentInformation = JCasUtil.selectSingle(jCas, DocumentInformationAnnotation.class);
            return documentInformation.getRunId();
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }

    public static void writeText(String directory, String fileName, String extension, String outputText) throws AnalysisEngineProcessException {
        File outputFileName = new File(directory, fileName + extension);
        try (FileWriter file = new FileWriter(outputFileName);
             BufferedWriter writer = new BufferedWriter(file)) {
            writer.write(outputText);
        } catch (IOException e) {
            LOGGER.throwing(e);
            throw new AnalysisEngineProcessException(e);
        }
    }

    public static String getFileName(JCas jCas) {
        //can't put documentIndex here b/c it could be incremented multiple times per document if there are multiple writers
        //Some tests run this w/o having a DocumentInformationAnnotation which throws IllegalArgumentException
//        return JCasUtil.selectSingle(jCas, DocumentInformationAnnotation.class).getFileName();
//    }
        for (DocumentInformationAnnotation documentInformation : jCas.getAnnotationIndex(DocumentInformationAnnotation.class)) {
            String path = documentInformation.getFileName();
            if (!path.equals("")) {
                return path;
            }
        }
        return "";
    }

    public static String getFullPathAndFileName(JCas jCas) {
        //can't put documentIndex here b/c it could be incremented multiple times per document if there are multiple writers
        for (DocumentInformationAnnotation documentInformation : jCas.getAnnotationIndex(DocumentInformationAnnotation.class)) {
            String path = documentInformation.getFilePath();
            if (!path.equals("")) {
                return path;
            }
        }
        return "";
    }

    // Only used by developers to make new objects, production code would use at most the readSerializedObject method
    public static boolean writeSerializeObject(String filename, Object object) {
        try (FileOutputStream fos = new FileOutputStream(filename); // can't use getExternalFile(filename)); as it assumes file exists
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(object);
        } catch (IOException e) {
            LOGGER.throwing(e);
            return false;
        }
        return true;
    }

    public static Object readSerializedObject(String filename) throws ResourceInitializationException {
        try (FileInputStream fis = new FileInputStream(getExternalFile(filename));
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            try (FileInputStream fis = new FileInputStream(filename);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                return ois.readObject();
            } catch (IOException | ClassNotFoundException e2) {

                LOGGER.throwing(e);
                throw new ResourceInitializationException(e);
            }
//
//            logger.throwing(e);
//            throw new ResourceInitializationException(e);
        }
    }

    public static Object readSerializedObject(Path filename) throws ResourceInitializationException {
        try (FileInputStream fis = new FileInputStream(filename.toFile());
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.throwing(e);
            throw new ResourceInitializationException(e);
        }
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }


    public static boolean isCapitalizedWord(String word) {// replacement for matches("^[A-Z].+")
        return word.length() > 1 && Character.isUpperCase(word.charAt(0));
    }

    public static String quoteCommaString(String... values) {//puts ' around its params and separate with commas
        if (values.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("'").append(values[0]).append("'");
        for (int i = 1; i < values.length; i++) {
            sb.append(", '").append(values[i]).append("'");
        }
        return sb.toString();
    }

    //for possible future use
    private static void encryptDecrypt() {//this encrypt/decrypts text file correctly, but not a zip file
        String DATA_PATH = "/Users/garyunderwood/noBackup/crypto/";
        String textFile = DATA_PATH + "plain.txt";//"GT1.zip";
        String cryptFile = DATA_PATH + "cryptoText.enc";
        String decryptFile = DATA_PATH + "decrypt.txt";
        encryptFiles(textFile, cryptFile);
        decryptFiles(cryptFile, decryptFile);

        cryptFile = DATA_PATH + "cryptoZip.enc";
        encryptFiles(DATA_PATH + "GT1.zip", cryptFile);
        decryptFiles(cryptFile, DATA_PATH + "decrypt.zip");
    }

    private static void encryptFiles(String textFile, String cryptFile) {
        try {
            byte[] textBytes = Files.readAllBytes(Paths.get(textFile));
            String key = "Clinacuity2018GaryAndrewStephane"; // 256 bit key
            byte[] encrypted = encrypt(textBytes, key);
            Files.write(Paths.get(cryptFile), encrypted);
        } catch (IOException | GeneralSecurityException e) {
            LOGGER.throwing(e);
            throw new RuntimeException(e);
        }
    }

    private static byte[] encrypt(byte[] input, String key) throws GeneralSecurityException {
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return cipher.doFinal(input);
    }

    private static void decryptFiles(String cryptFile, String decryptFile) {
        try {
            String key = "Clinacuity2018GaryAndrewStephane"; // 256 bit key
            byte[] encrypted = Files.readAllBytes(Paths.get(cryptFile));
            byte[] decrypted = decrypt(encrypted, key);
            Files.write(Paths.get(decryptFile), decrypted);
        } catch (IOException | GeneralSecurityException e) {
            LOGGER.throwing(e);
            throw new RuntimeException(e);
        }
    }

    private static byte[] decrypt(byte[] encrypted, String key) throws GeneralSecurityException {
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        return cipher.doFinal(encrypted);
    }
}
