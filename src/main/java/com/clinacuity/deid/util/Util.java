
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

package com.clinacuity.deid.util;

import com.clinacuity.deid.type.BaseToken;
import com.clinacuity.deid.type.DictionaryAnnotation;
import com.clinacuity.deid.type.FeatureVector;
import com.clinacuity.deid.type.PiiAnnotation;
import com.clinacuity.deid.type.Sentence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;


public class Util {
    public static final Map<String, String> PII_SUB_TO_PARENT_TYPE = Collections.unmodifiableMap(Map.ofEntries(
            Map.entry("Profession", "OCCUPATION"),
            Map.entry("Date", "TEMPORAL"), Map.entry("Age", "TEMPORAL"), Map.entry("ClockTime", "TEMPORAL"), Map.entry("Season", "TEMPORAL"), Map.entry("DayOfWeek", "TEMPORAL"),
            Map.entry("OtherIDNumber", "IDENTIFIER"), Map.entry("SSN", "IDENTIFIER"),
            Map.entry("Provider", "NAME"), Map.entry("Patient", "NAME"), Map.entry("Relative", "NAME"),
            Map.entry("ElectronicAddress", "CONTACT_INFORMATION"), Map.entry("PhoneFax", "CONTACT_INFORMATION"),
            Map.entry("Zip", "ADDRESS1"), Map.entry("Street", "ADDRESS1"), Map.entry("City", "ADDRESS1"),
            // Map.entry("StreetCity", "ADDRESS1"), Map.entry("StateCountry", "ADDRESS1"),
            Map.entry("Country", "ADDRESS1"), Map.entry("State", "ADDRESS1"),
            Map.entry("OtherGeo", "LOCATION"), Map.entry("OtherOrgName", "LOCATION"), Map.entry("HealthCareUnitName", "LOCATION")));
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern NON_ALPHA = Pattern.compile("[^A-Za-z]+");

    private Util() {
    }

    public static String getDocumentId(JCas jcas) {//TODO: this needs verification, but only used in training and logging.
        String path = Utilities.getFileName(jcas);
        if (path.lastIndexOf(File.pathSeparatorChar) >= 0) {
            return path.substring(path.lastIndexOf(File.pathSeparatorChar) + 1);
        } else {
            return path;
        }
    }

    //TODO: combine the below, only difference seeps to be which indices are put in map (0,1) vs (1,0)
    public static void readWvMap(String fileName, Map<String, Integer> map) throws ResourceInitializationException {
        String str = "";
        try (FileReader fr = new FileReader(fileName);
             BufferedReader txtin = new BufferedReader(fr)) {
            while ((str = txtin.readLine()) != null) {
                String s[] = str.split(" ", 2);
                map.put(s[0], Integer.parseInt(s[1]));
            }
        } catch (IOException ex) {
            LOGGER.throwing(ex);
            throw new ResourceInitializationException(ex);
        }
    }

    public static void readSvmMap(String fileName, Map<String, Integer> map) throws ResourceInitializationException {
        String str = "";
        try (FileReader fr = new FileReader(fileName);
             BufferedReader txtin = new BufferedReader(fr)) {
            while ((str = txtin.readLine()) != null) {
                String strA[] = str.split(" ", 2);//TODO: Static Pattern?
                map.put(strA[1], Integer.parseInt(strA[0]));
            }
        } catch (IOException ex) {
            LOGGER.throwing(ex);
            throw new ResourceInitializationException(ex);
        }
    }

    public static void readSvmTemplate(String fileName, List<String> list) throws ResourceInitializationException {
        String str = "";
        try (FileReader fr = new FileReader(fileName);
             BufferedReader txtin = new BufferedReader(fr)) {
            //U0101:%x[-2,0]/%x[-1,0]

            while ((str = txtin.readLine()) != null) {
                if (!str.trim().isEmpty() && !str.startsWith("#")) {
                    list.add(str.trim());
                }
            }
        } catch (IOException ex) {
            LOGGER.throwing(ex);
            throw new ResourceInitializationException(ex);
        }
    }

    //TODO: again, only difference is map using 0,1 or 1,0
    //Is this the sampe as CombinedC
    public static void readMap(String fileName, Map<String, String> map, String del) throws ResourceInitializationException {
        String str = "";
        try (FileReader fr = new FileReader(fileName);
             BufferedReader txtin = new BufferedReader(fr)) {
            while ((str = txtin.readLine()) != null) {
                String s[] = str.split(del, 2);
                map.put(s[0], s[1]);
            }
        } catch (IOException ex) {
            LOGGER.throwing(ex);
            throw new ResourceInitializationException(ex);
        }
    }

    public static void readMapR(String fileName, Map<String, String> map, String del) throws ResourceInitializationException {
        String str = "";
        try (FileReader fr = new FileReader(fileName);
             BufferedReader txtin = new BufferedReader(fr)) {
            while ((str = txtin.readLine()) != null) {
                String s[] = str.split(del, 2);
                map.put(s[1], s[0]);
            }
        } catch (IOException ex) {
            LOGGER.throwing(ex);
            throw new ResourceInitializationException(ex);
        }
    }

    public static void setTagMap(JCas jCas, String documentId, Map<Integer, String> tagMap) {

        AnnotationIndex<?> piisIndex = jCas.getAnnotationIndex(PiiAnnotation.type);
        FSIterator<?> pIterator = piisIndex.iterator();
        String docTxt = jCas.getDocumentText();

        TreeMap<Integer, String> tMap = new TreeMap<>();
        TreeMap<Integer, String> tMapId = new TreeMap<>();

        while (pIterator.hasNext()) {
            PiiAnnotation p = (PiiAnnotation) pIterator.next();
            String piiSubtype = p.getPiiSubtype();

            int b = p.getBegin();
            int e = p.getEnd();
            String pId = p.getId();

            boolean first = true;
            for (int i = b; i < e; i++) {
                if (first) {
                    if (!Character.isWhitespace(docTxt.charAt(i))) {
                        tMap.put(i, "B-" + piiSubtype);
                        first = false;
                        tMapId.put(i, pId);
                    }
                } else {
                    tMap.put(i, "I-" + piiSubtype);
                    tMapId.put(i, pId);
                }
            }
        }

        ArrayList<BaseToken> eBs = new ArrayList<BaseToken>();
        FSIterator<?> bIterator = jCas.getAnnotationIndex(BaseToken.type).iterator();
        while (bIterator.hasNext()) {
            BaseToken tokO = (BaseToken) bIterator.next();
            eBs.add(tokO);
        }

        for (int i = 0; i < eBs.size(); i++) {
            BaseToken tok = eBs.get(i);
            int start = tok.getBegin();
            int end = tok.getEnd();

            for (int j = start; j < end; j++) {
                if (tMap.containsKey(j)) {
                    String sId = tMapId.get(j);
                    String sTag = tMap.get(j);
                    if (sTag.startsWith("B-") || sTag.startsWith("I-")) {
                        if (tMapId.containsKey(end - 1)) {
                            String eId = tMapId.get(end - 1);
                            if (sId.equals(eId)) {
                                // done
                                tagMap.put(start, sTag);
                            } else { // two piis in one word token
                                String eTag = tMap.get(end - 1);
                                // use the last pii tag
                                tagMap.put(start, "B" + eTag.substring(1));//  eTag.replaceFirst("I-", "B-"));
                            }
                        } else { // word tag contains a pii
                            tagMap.put(start, sTag);
                        }
                    }
                    break;
                } // if
            } // for j
        } // for i

    }

    //setTagMap and addFeatures have been changed

    public static String getValueWv(Map<String, Integer> map, String tStr) {

        String str = "<emp>";

        String tmp = tStr;

        tmp = NON_ALPHA.matcher(tmp).replaceAll("");//tmp.replaceAll("[^A-Za-z]+", "");
        tmp = tmp.toLowerCase();

        //TODO: why toLowerCase? if it had any letters in it temp wouldn't be empty
        if (tmp.isEmpty()) {
            tmp = tStr.toLowerCase();
        }
        //TODO: is there already a comparable map that is string to string instead of toString?
        if (!tmp.isEmpty() && map.containsKey(tmp)) {
            str = Integer.toString(map.get(tmp));
        } else if (!map.containsKey(tmp)) {
            str = "<unk>";
        }

        return str;
    }

    public static void addFeatures(JCas jCas, Map<String, Integer> wvMap) throws AnalysisEngineProcessException {
        String documentId = Util.getDocumentId(jCas);

        //FSIterator<?> sentences = jCas.getAnnotationIndex(Sentence.type).iterator();
        //AnnotationIndex<?> BaseTokenIndex = jCas.getAnnotationIndex(BaseToken.type);
        //AnnotationIndex<?> DictionaryIndex = jCas.getAnnotationIndex(DictionaryAnnotation.type);

        FeatureVectorWrapper fvW = new FeatureVectorWrapper();

        TreeMap<Integer, String> tagMap = new TreeMap<>();
        setTagMap(jCas, documentId, tagMap);

        //tried:         ContainmentIndex supposed to do well at getting covered stuff, didn't seem to help
        for (Sentence sentence : jCas.getAnnotationIndex(Sentence.class)) {

//        while (sentences.hasNext()) {
            //          Sentence sentence = (Sentence) sentences.next();
            int sNum = sentence.getSentNo() + 1;

            //rewrote with selectCovered, left for reference as this is Young's code.
//            ArrayList<BaseToken> eBs = new ArrayList<>();
//            FSIterator<?> bIterator = BaseTokenIndex.subiterator(sentence);
//            while (bIterator.hasNext()) {
//                BaseToken tokO = (BaseToken) bIterator.next();
//                eBs.add(tokO);
//            }
            List<BaseToken> eBs = JCasUtil.selectCovered(jCas, BaseToken.class, sentence);
            boolean ifFirstWord = true;
            String preTag = "O";
            for (int i = 0; i < eBs.size(); i++) {
                BaseToken tok = eBs.get(i);
                String cText = tok.getCoveredText();
                String pos = tok.getPartOfSpeech();
                String ner = tok.getNer();
                String np = tok.getChunk();

                int start = tok.getBegin();
                int end = tok.getEnd();

                FeatureVector fv = new FeatureVector(jCas);
                fvW.setFv(fv);

                String tag = "O";
                if (tagMap.containsKey(start)) {
                    tag = tagMap.get(start);
                }

                // prevent wrong tag transition
                if (tag.startsWith("I-")) {
                    String piiType = tag.substring(2);//replaceFirst("I-", "");
                    if (!preTag.equalsIgnoreCase("B-" + piiType) && !preTag.equalsIgnoreCase("I-" + piiType)) {
                        tag = "B" + tag.substring(1);// tag.replaceFirst("I-", "B-");
                    }
                }
                fv.setBegin(start);
                fv.setEnd(end);

                fvW.setBaseFeature(cText, pos, tag);
                fvW.setUniFv();
                fvW.setNer(ner);
                fvW.setNp(np);

                fvW.setTNum(i);
                fvW.setSNum(sNum);
                fvW.setFileName(documentId);

                fvW.setWv(getValueWv(wvMap, cText));

                if (ifFirstWord) {
                    fvW.setFirstWord(true);
                    ifFirstWord = false;
                } else {
                    fvW.setFirstWord(false);
                }

                if (i == eBs.size() - 1) {
                    fvW.setLastWord(true);
                } else {
                    fvW.setLastWord(false);
                }

                //TODO: these aren't used by machine learning yet, to be tried later, have to adjust code that makes the feature vectors given to the classifiers
//                List<DictionaryAnnotation> dictAnnots = JCasUtil.selectCovered(jCas, DictionaryAnnotation.class, tok);
//                if (!dictAnnots.isEmpty()) {
//                    assignDictionaryFlags(fv, dictAnnots.get(0));
//                }
                fv.addToIndexes();
                preTag = tag;

            }

        }
    }

    private static void assignDictionaryFlags(FeatureVector fv, DictionaryAnnotation dict) {//for future use with ensemble engines
        if (dict.getMit_common_words()) {
            fv.setMit_common_words(1);
        }
        if (dict.getMit_commonest_words()) {
            fv.setMit_commonest_words(1);
        }
        if (dict.getMit_countries()) {
            fv.setMit_countries(1);
        }
        if (dict.getMit_lastnames_ambig()) {
            fv.setMit_lastnames_ambig(1);
        }
        if (dict.getMit_lastnames_popular()) {
            fv.setMit_lastnames_popular(1);
        }
        if (dict.getMit_lastnames_unambig()) {
            fv.setMit_lastnames_unambig(1);
        }
        if (dict.getMit_locations_unambig()) {
            fv.setMit_locations_unambig(1);
        }
        if (dict.getMit_names_ambig()) {
            fv.setMit_names_ambig(1);
        }
        if (dict.getMit_names_popular()) {
            fv.setMit_names_popular(1);
        }
        if (dict.getMit_names_unambig()) {
            fv.setMit_names_unambig(1);
        }
        if (dict.getNationalities()) {
            fv.setNationalities(1);
        }
    }

    public static void addPii(JCas jCas, int b, int e, String piiType, String piiSubtype, String id, String method, float conf) {
        PiiAnnotation s = new PiiAnnotation(jCas, b, e);
        s.setId(id);
        s.setPiiSubtype(piiSubtype);
        s.setPiiType(piiType);
        s.setMethod(method);
        s.setConfidence(conf);
        s.addToIndexes();
    }

    public static void addPii(List<PiiAnnotation> newPii, JCas jCas, int b, int e, String piiType, String piiSubtype, String id, String method, float conf) {
        PiiAnnotation s = new PiiAnnotation(jCas, b, e);
        s.setId(id);
        s.setPiiSubtype(piiSubtype);
        s.setPiiType(piiType);
        s.setMethod(method);
        s.setConfidence(conf);
        newPii.add(s);
    }

    public static void readPredict(List<PiiAnnotation> piiNew, JCas jCas, List<String> inst, String method, float conf) {

        String del = "\t";
        String cTag = "O";
        String nTag = "O";

        String begin = "";
        String end = "";
        String file = "";

        String sBegin = "0";

        boolean start = false;

        HashMap<String, Integer> ids = new HashMap<>();

        //rearranged so that each string was only split once
        if (!inst.isEmpty()) {
            String nStr = inst.get(0);
            String[] cStrA;
            String[] nStrA = nStr.split(del);

            for (int i = 0; i < inst.size(); i++) {
                //cStr=nStr;
                cStrA = nStrA;

                if (i < inst.size() - 1) {
                    nStr = inst.get(i + 1);
                } else {
                    nTag = "O";
                }
                nStrA = nStr.split(del);


                //TODO: possible speed ups:
                //map ids of file to id is somewhat pointless unless made static
                //just make ids an int
                //does ID really need to be string? can it just be an int counter instead of Pi?

                //<unk>	60	61	0	1	110-01.txt	O	O
                //          -8      -7      -6      -5      -4              -3      -2      -1
                if (cStrA.length < 3) {
                    cTag = "O";
                } else {//TODO: this seems fragile
                    begin = cStrA[cStrA.length - 7];
                    end = cStrA[cStrA.length - 6];
                    file = cStrA[cStrA.length - 3];
                    cTag = cStrA[cStrA.length - 1];
                }

                if (nStrA.length < 3) {
                    nTag = "O";
                } else {
                    nTag = nStrA[nStrA.length - 1];
                }
                // sequence adjustment start

                String cType = "O";
                String nType = "O";
                if (!"O".equals(cTag)) {
                    cType = cTag.substring(2);//replace("B-", "").replace("I-", "");
                }
                if (!"O".equals(nTag)) {
                    nType = nTag.substring(2);//replace("B-", "").replace("I-", "");
                }

                if (cTag.startsWith("B-")) {
                    if (nTag.startsWith("I-") && !cType.equals(nType)) {
                        nTag = "I-" + cType;
                    }
                } else if (cTag.startsWith("I-")) {
                    if (nTag.startsWith("I-") && !cType.equals(nType)) {
                        nTag = "I-" + cType;
                    }
                } else if ("O".equals(cTag) && nTag.startsWith("I-")) {
                    nTag = "O";
                }

                // sequence adjustment end

                if (cTag.startsWith("B-")) {

                    if (nTag.startsWith("B-") || "O".equals(nTag)) {

                        String piiSubtype = cTag.substring(2);//replace("B-", "");
                        String piiType = Util.PII_SUB_TO_PARENT_TYPE.get(piiSubtype);

                        if (ids.containsKey(file)) {
                            int idc = ids.get(file) + 1;
                            ids.put(file, idc);
                        } else {
                            ids.put(file, 0);
                        }

                        addPii(piiNew, jCas, Integer.parseInt(begin), Integer.parseInt(end), piiType, piiSubtype, "P" + ids.get(file), method, conf);
                        start = false;
                    } else if (nTag.startsWith("I-")) {
                        start = true;
                        sBegin = begin;
                    }
                } else if (cTag.startsWith("I-")) {
                    if ((nTag.startsWith("B-") || "O".equals(nTag))) {
                        if (start) {
                            String piiSubtype = cTag.substring(2);//replace("I-", "");
                            String piiType = Util.PII_SUB_TO_PARENT_TYPE.get(piiSubtype);

                            if (ids.containsKey(file)) {
                                int idc = ids.get(file) + 1;
                                ids.put(file, idc);
                            } else {
                                ids.put(file, 0);
                            }

                            addPii(piiNew, jCas, Integer.parseInt(sBegin), Integer.parseInt(end), piiType, piiSubtype, "P" + ids.get(file), method, conf);
                            start = false;
                        }
                    } else if (nTag.startsWith("I-")) {
                        start = true;
                    }
                } else if ("O".equals(cTag)) {
                    start = false;
                }
            }
        }
    }

    public static void writePiisFilter(JCas jCas, String dir, String fname, String method) throws FileNotFoundException {
        FSIterator<?> piis = jCas.getAnnotationIndex(PiiAnnotation.type).iterator();
        ArrayList<PiiAnnotation> pList = new ArrayList<>();

        while (piis.hasNext()) {
            PiiAnnotation pii = (PiiAnnotation) piis.next();
            if (method.equalsIgnoreCase("vote")) {
                float conf = pii.getConfidence();
                if (conf == 1.0f) {
                    pList.add(pii);
                }
            } else {
                if (method.equalsIgnoreCase(pii.getMethod())) {
                    pList.add(pii);
                }
            }
        }

        writePiis(pList, dir, fname);
    }

    private static void writePiis(List<PiiAnnotation> list, String dir, String fname) throws FileNotFoundException {
        try (FileOutputStream fos = new FileOutputStream(new File(dir, fname));
             PrintStream out = new PrintStream(fos)) {
            for (PiiAnnotation pii : list) {
                String pStr = "<" + pii.getPiiType() + " id=\"" + pii.getId() + "\" start=\"" + pii.getBegin()
                        + "\" end=\"" + pii.getEnd() + "\" TYPE=\"" + pii.getPiiSubtype() + "\""
                        + " text=\"" + purgeString(pii.getCoveredText()) + "\"" + "/>";
                out.println(pStr);
            }
            out.flush();
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e2) {
            LOGGER.throwing(e2);
        }
    }

    private static String purgeString(String in) {
        StringBuilder out = new StringBuilder(); // Used to hold the output.
        char c; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            c = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if (Character.isAlphabetic(c) || Character.isDigit(c)) {
                out.append(c);
            }
        }
        return out.toString();
    }
}
