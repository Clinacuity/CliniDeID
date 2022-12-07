
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

package com.clinacuity.deid.outputAnnotators.resynthesis;

import com.clinacuity.clinideid.message.DeidLevel;
import com.clinacuity.deid.type.PiiOptionMapAnnotation;
import com.clinacuity.deid.util.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static com.clinacuity.deid.mains.DeidPipeline.PII_LOG;

public abstract class Resynthesizer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String DATA_PATH = "data/resynthesis/";
    protected ResynthesisMap resynthesisMap;
    protected DeidLevel level;//Eventually this will contain a map for all possible choices
    protected PiiOptionMapAnnotation options;

    protected Resynthesizer() {
    }

    protected static String[] readObjectFileToArray(String objFilename) {
        //Resynthesizer.class b/c static, else getClass()
        // from src/main/resources:  data=Files.readAllLines(Paths.get(Resynthesizer.class.getResource("/resynth/hospitals.txt").getPath()));
        //Path p = Paths.get(Resynthesizer.class.getResource(DATA_PATH+ objFilename + ".enc").getPath());
        Path p = Paths.get(DATA_PATH + objFilename + ".enc");
        try {
//                byte[] b = serialize(x);
            String key = "Clinacuity2018GaryAndrewStephane"; // 256 bit key
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            // encrypt the text
//                cipher.init(Cipher.ENCRYPT_MODE, aesKey);
//                byte[] encrypted = cipher.doFinal(b);//text.getBytes());
//                p = Paths.get(Resynthesizer.class.getResource("/resynth/" + objFilename+".enc").getPath());
//                try (FileOutputStream fos = new FileOutputStream(p.toFile())) {
//                    fos.write(encrypted);
//                }
            // decrypt the text
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] encrypted;
            try (FileInputStream fos = new FileInputStream(p.toFile())) {
                encrypted = fos.readAllBytes();
            }
            byte[] decrypted = cipher.doFinal(encrypted);
            return (String[]) Utilities.deserialize(decrypted);
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | ClassNotFoundException e) {
            LOGGER.throwing(e);
            throw new RuntimeException(e);
        }
    }


    protected static String[] readFileToArray(String filename) {
        List<String> data;
        //TODO: try to move src/main/resources, try to use binary objects
        try {//Resynthesizer.class b/c static, else getClass()
            // from src/main/resources:  data=Files.readAllLines(Paths.get(Resynthesizer.class.getResource("/resynth/hospitals.txt").getPath()));
            data = Files.readAllLines(Paths.get(DATA_PATH + filename), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.throwing(e);
            throw new RuntimeException(e);
        }
        return data.toArray(new String[0]);
    }

    //TODO implement this more fully, multi-word?
    protected static String matchCase(String oldPii, String newValue) {
        if (oldPii.indexOf(' ') >= 0) {
            LOGGER.log(PII_LOG, "{}", () -> "matching case of something with multiple words, old: " + oldPii + " to new: {}" + newValue);
        }
        if (newValue == null || newValue.length() < 1) {
            LOGGER.log(PII_LOG, "{}", () -> "Problem matching case, no newValue for " + oldPii);
            return "";
        }
        if (oldPii.length() < 1) {
            LOGGER.log(PII_LOG, "{}", () -> "Problem matching case, no oldPii for " + newValue);
            return newValue;
        }

        if (oldPii.length() == 1) {
            if (Character.isUpperCase(oldPii.charAt(0))) {
                return newValue.toUpperCase();
            } else {
                return newValue.toLowerCase();
            }
        }
        if (Character.isUpperCase(oldPii.charAt(0)) && Character.isUpperCase(oldPii.charAt(1))) {
            return newValue.toUpperCase();
        } else if (Character.isLowerCase(oldPii.charAt(0)) && Character.isLowerCase(oldPii.charAt(1))) {
            return newValue.toLowerCase();
        } else if (Character.isUpperCase(oldPii.charAt(0)) && Character.isLowerCase(oldPii.charAt(1))) {
            return Character.toUpperCase(newValue.charAt(0)) + newValue.substring(1).toLowerCase();
            //return newValue.substring(0, 1).toUpperCase() + newValue.substring(1).toLowerCase();
        }
        return newValue;
    }

    public static String trimToLetterDigit(String pii) {//returns substring starting from first letter or number and ending at last letter or number from pii
        int lastLetterDigit = pii.length() - 1;
        while (lastLetterDigit >= 0 && !(Character.isLetter(pii.charAt(lastLetterDigit)) || Character.isDigit(pii.charAt(lastLetterDigit)))) {
            lastLetterDigit--;

        }
        if (lastLetterDigit < 0) {
            return "";
        }
        int firstLetterDigit = 0;
        while (firstLetterDigit < pii.length() && !(Character.isLetter(pii.charAt(firstLetterDigit)) || Character.isDigit(pii.charAt(firstLetterDigit)))) {
            firstLetterDigit++;
        }
        if (firstLetterDigit >= pii.length()) {
            return "";
        }
        return pii.substring(firstLetterDigit, lastLetterDigit + 1).toLowerCase();
    }

    public void setMap(ResynthesisMap newMap) {
        resynthesisMap = newMap;
    }

    public void setLevel(DeidLevel level) {
        this.level = level;
    }

    public void setPiiOptions(PiiOptionMapAnnotation options) {
        this.options = options;
    }

    public abstract String getAndUpdateResynthesizedValue(String oldPii);

    protected static String trimToLetter(String name) {//returns substring starting from first letter and ending at last letter from name
        int lastLetter = name.length() - 1;
        while (lastLetter >= 0 && !Character.isLetter(name.charAt(lastLetter))) {
            lastLetter--;
        }
        if (lastLetter < 0) {
            return "";
        }
        int firstLetter = 0;
        while (firstLetter < name.length() && !Character.isLetter(name.charAt(firstLetter))) {
            firstLetter++;
        }
        if (firstLetter >= name.length()) {
            return "";
        }
        return name.substring(firstLetter, lastLetter + 1).toLowerCase();
    }
}
