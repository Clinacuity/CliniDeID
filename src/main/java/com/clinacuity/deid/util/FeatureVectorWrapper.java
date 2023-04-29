
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.clinacuity.deid.util;

import com.clinacuity.deid.type.FeatureVector;

/**
 * @author jun
 */
public class FeatureVectorWrapper {
    public static final String NV = "__nil__";
    public static final String SP = "?#@?";
    private static final String GREEK = "(alpha|beta|gamma|delta|epsilon|zeta|eta|theta|iota|kappa|lambda|mu|nu|xi|omicron|pi|rho|sigma|tau|upsilon|phi|chi|psi|omega)";
    private FeatureVector fv;

    private static int setInitCaps(String str) {
        if (str.matches("[A-Z].*")) return 1;
        return 0;
    }

    private static int setInitCapsAlpha(String str) {
        if (str.matches("[A-Z][a-z].*")) return 1;
        return 0;
    }

    private static int setAllCaps(String str) {
        if (str.matches("[A-Z]+")) return 1;
        return 0;
    }

    private static int setCapsMix(String str) {
        if (str.matches("[A-Za-z]+")) return 1;
        return 0;
    }

    private static int setHasDigit(String str) {
        if (str.matches(".*[0-9].*")) return 1;
        return 0;
    }

    private static int setSingleDigit(String str) {
        if (str.matches("[0-9]")) return 1;
        return 0;
    }

    private static int setDoubleDigit(String str) {
        if (str.matches("[0-9][0-9]")) return 1;
        return 0;
    }

    private static int setNaturalNumber(String str) {
        if (str.matches("[0-9]+")) return 1;
        return 0;
    }

    private static int setRealNumber(String str) {
        if (str.matches("[-0-9]+[.,]+[0-9.,]+")) return 1;
        return 0;
    }

    private static int setHasDash(String str) {
        if (str.matches(".*-.*")) return 1;
        return 0;
    }

    private static int setInitDash(String str) {
        if (str.matches("-.*")) return 1;
        return 0;
    }

    private static int setEndDash(String str) {
        if (str.matches(".*-")) return 1;
        return 0;
    }

    public static int setHasSlash(String str) {
        if (str.matches(".*/.*")) return 1;
        return 0;
    }

    public static int setInitSlash(String str) {
        if (str.matches("/.*")) return 1;
        return 0;
    }

    public static int setEndSlash(String str) {
        if (str.matches(".*/")) return 1;
        return 0;
    }

    public static int setHasColon(String str) {
        if (str.matches(".*:.*")) return 1;
        return 0;
    }

    public static int setInitColon(String str) {
        if (str.matches(":.*")) return 1;
        return 0;
    }

    public static int setEndColon(String str) {
        if (str.matches(".*:")) return 1;
        return 0;
    }

    private static int setAlphaNumeric1(String str) {
        if (str.matches(".*[A-Za-z].*[0-9].*")) return 1;
        return 0;
    }

    private static int setAlphaNumeric2(String str) {
        if (str.matches(".*[0-9].*[A-Za-z].*")) return 1;
        return 0;
    }

    public static int setRoman(String str) {
        if (str.matches("[ivxdlcm]+") || str.matches("[IVXDLCM]+")) return 1;
        return 0;
    }

    public static int setHasRoman(String str) {
        if (str.matches(".*\\b[ivxdlcm]+\\b.*") || str.matches(".*\\b[IVXDLCM]+\\b.*")) return 1;
        return 0;
    }

    public static int setGreek(String str) {
        if (str.toLowerCase().matches(GREEK)) return 1;
        return 0;
    }

    public static int setHasGreek(String str) {
        if (str.toLowerCase().matches(".*\\b" + GREEK + "\\b.*")) return 1;
        return 0;
    }

    private static int setPunctuation(String str) {
        if (str.matches("[,.;:?!-+]")) return 1;
        return 0;
    }

    private static String setPrefix(String str, int len) {
        if (str.length() < len) {
            return str;
        }
        return str.substring(0, len);
    }

    private static String setSurffix(String str, int len) {
        if (str.length() < len) {
            return str;
        }
        return str.substring(str.length() - len);
    }

    private static String setWordShape(String wc) {
        wc = wc.replaceAll("[A-Z]", "A");
        wc = wc.replaceAll("[a-z]", "a");
        wc = wc.replaceAll("[0-9]", "0");
        wc = wc.replaceAll("[^A-Za-z0-9]", "x");
        return wc;
    }

    private static String setWordShapeD(String wc) {
        wc = wc.replaceAll("[A-Z]", "A");
        wc = wc.replaceAll("[a-z]", "a");
        wc = wc.replaceAll("[0-9]", "0");
        return wc;
    }

    public static String getNnStr(FeatureVector fv) {
        return (
                fv.getWord() + "\t" +  // 0
                        fv.getBegin() + "\t" + //
                        fv.getEnd() + "\t" + //
                        fv.getTNum() + "\t" +
                        fv.getSNum() + "\t" +
                        fv.getFileName() + "\t" +
                        fv.getTag());
    }

    public static String getStr(FeatureVector fv) {

        return (
                fv.getWord() + "\t" +  // 0
                        fv.getPos() + "\t" +    // 1

                        fv.getPrefix1() + "\t" + //
                        fv.getPrefix2() + "\t" +
                        fv.getPrefix3() + "\t" +
                        fv.getPrefix4() + "\t" +
                        fv.getPrefix5() + "\t" +
                        fv.getSurffix1() + "\t" +
                        fv.getSurffix2() + "\t" +
                        fv.getSurffix3() + "\t" + //9
                        fv.getSurffix4() + "\t" + //10
                        fv.getSurffix5() + "\t" + //11

                        fv.getAllCaps() + "\t" + //
                        fv.getInitCaps() + "\t" +
                        fv.getInitCapsAlpha() + "\t" +
                        fv.getCapsMix() + "\t" +
                        fv.getHasDigit() + "\t" +
                        fv.getSingleDigit() + "\t" +
                        fv.getDoubleDigit() + "\t" +
                        fv.getNaturalNumber() + "\t" +
                        fv.getRealNumber() + "\t" + //20
                        fv.getHasDash() + "\t" +
                        fv.getInitDash() + "\t" + // 22
                        fv.getEndDash() + "\t" +
                        fv.getAlphaNumeric1() + "\t" +
                        fv.getAlphaNumeric2() + "\t" +
                        fv.getPunctuation() + "\t" + //26

                        fv.getFirstWord() + "\t" + //27
                        fv.getLastWord() + "\t" + //28
                        fv.getLword() + "\t" + //29
                        fv.getNer() + "\t" + //30
                        fv.getNp() + "\t" + //31
                        fv.getWv() + "\t" + //32
                        fv.getWordShape() + "\t" + //33
                        fv.getWordShapeD() + "\t" + //34

                        fv.getBegin() + "\t" + //
                        fv.getEnd() + "\t" + //

                        fv.getTNum() + "\t" +
                        fv.getSNum() + "\t" +
                        fv.getFileName() + "\t" +
                        fv.getTag());
    }

    public void setFv(FeatureVector fv) {
        this.fv = fv;
    }

    void setBaseFeature(String word, String pos, String tag) {
        fv.setWord(word);
        fv.setLword(word.toLowerCase());
        fv.setPos(pos);
        fv.setTag(tag);
    }

    public void setNer(String ner) {
        fv.setNer(ner);
    }

    public void setNp(String np) {
        fv.setNp(np);
    }

    public void setTNum(int tNum) {
        fv.setTNum(tNum);
    }

    void setSNum(int sNum) {
        fv.setSNum(sNum);
    }

    public void setFileName(String fileName) {
        fv.setFileName(fileName);
    }

    public void setFirstWord(boolean flag) {
        if (flag) fv.setFirstWord(1);
        else fv.setFirstWord(0);
    }

    public void setLastWord(boolean flag) {
        if (flag) fv.setLastWord(1);
        else fv.setLastWord(0);
    }

    public void setBegin(int start) {
        fv.setBegin(start);
    }

    public void setEnd(int end) {
        fv.setEnd(end);
    }

    public void setWv(String wv) {
        fv.setWv(wv);
    }

    void setUniFv() {

        String word = fv.getWord();

        fv.setAllCaps(setAllCaps(word));
        fv.setInitCaps(setInitCaps(word));
        fv.setInitCapsAlpha(setInitCapsAlpha(word));
        fv.setCapsMix(setCapsMix(word));
        fv.setHasDigit(setHasDigit(word));
        fv.setSingleDigit(setSingleDigit(word));
        fv.setDoubleDigit(setDoubleDigit(word));
        fv.setNaturalNumber(setNaturalNumber(word));
        fv.setRealNumber(setRealNumber(word));

        fv.setHasDash(setHasDash(word));
        fv.setInitDash(setInitDash(word));
        fv.setEndDash(setEndDash(word));

        fv.setAlphaNumeric1(setAlphaNumeric1(word));
        fv.setAlphaNumeric2(setAlphaNumeric2(word));
        fv.setPunctuation(setPunctuation(word));

        fv.setWordShape(setWordShape(word));
        fv.setWordShapeD(setWordShapeD(word));

        String tmp = word.toLowerCase();

        fv.setPrefix1(setPrefix(tmp, 1));
        fv.setPrefix2(setPrefix(tmp, 2));
        fv.setPrefix3(setPrefix(tmp, 3));
        fv.setPrefix4(setPrefix(tmp, 4));
        fv.setPrefix5(setPrefix(tmp, 5));
        fv.setSurffix1(setSurffix(tmp, 1));
        fv.setSurffix2(setSurffix(tmp, 2));
        fv.setSurffix3(setSurffix(tmp, 3));
        fv.setSurffix4(setSurffix(tmp, 4));
        fv.setSurffix5(setSurffix(tmp, 5));
    }
}
