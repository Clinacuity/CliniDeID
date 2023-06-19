
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

package com.clinacuity.deid.ae;


import com.clinacuity.deid.type.FeatureVector;
import com.clinacuity.deid.type.PiiAnnotation;
import com.clinacuity.deid.type.Sentence;
import com.clinacuity.deid.util.FeatureVectorWrapper;
import com.clinacuity.deid.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RnnAnnotatorThread implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private String hostName;
    private int portNumber;
    private Socket kkSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int annotationNumber;
    private JCas jCas;
    private CountDownLatch doneSignal;
    private List<PiiAnnotation> newPii;
    private String fileName;

    RnnAnnotatorThread(String hostName, int portNumber,
                       int annotationNumber, JCas jCas, CountDownLatch doneSignal, List<PiiAnnotation> newPii, String fileName) {
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.annotationNumber = annotationNumber;
        this.jCas = jCas;
        this.doneSignal = doneSignal;
        this.newPii = newPii;
        this.fileName = fileName;
    }

    //public void collectionProcessComplete() throws AnalysisEngineProcessException {
    static void stopRnn(String hostName, int portNumber) throws AnalysisEngineProcessException {
        try (Socket kkSocket = new Socket(hostName, portNumber);
             PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()))) {
            LOGGER.debug("RNN complete, turning off service");
            String endMsg = "--<eosc>--";
            byte[] bText = endMsg.getBytes();
            String nStr = new String(bText, UTF_8);
            out.println(nStr);
            LOGGER.debug("Sent RNN end message");
        } catch (IOException e) {
            LOGGER.throwing(e);
            throw new AnalysisEngineProcessException(e);
        }
    }

    static boolean tryConnect(String hostName, int portNumber) {
        try (//test that RNN connection is live
             Socket kkSocket = new Socket(hostName, portNumber);
             PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()))) {
            LOGGER.debug("Test RNN connection to host {} on port {}", hostName, portNumber);
            out.println(new String("  --<eoscE>--".getBytes(), UTF_8));
            out.flush();
            String str;
            StringBuilder oStr = new StringBuilder();
            try {
                while ((str = in.readLine()) != null) {
                    oStr.append(str).append("\n");
                }
            } catch (IOException e) {
                LOGGER.throwing(e);
                throw new AnalysisEngineProcessException(e);
            }
            String gotBack = oStr.toString();

            if (gotBack.length() > 0) {
                LOGGER.warn("RNN initialize got non-empty data back from RNN");
            }
            in.close();
            out.close();
            if (!kkSocket.isClosed()) {
                kkSocket.close();
            }
        } catch (IOException | AnalysisEngineProcessException e) {
            return false;
        }
        return true;
    }

    private void readTag(String oStr, ArrayList<String> outs) throws AnalysisEngineProcessException {
        String str;
        try (StringReader sr = new StringReader(oStr);
             BufferedReader reader = new BufferedReader(sr)) {
            while ((str = reader.readLine()) != null) {
                outs.add(str.trim());
            }
        } catch (IOException e) {
            LOGGER.throwing(e);
            throw new AnalysisEngineProcessException(e);
        }
    }
//overall work:
    /*Iterate over sentences, subiterator for FeatureVectors in sentence
        iterate over FV in sence
            append result of FeatureVectorWrapper.getNnStr of FV
            newline
        newline
        predict resulting string
        readTag
        readPredict
    */

    private void disconnect() throws IOException {
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
        if (!kkSocket.isClosed()) {
            kkSocket.close();
        }
    }

    private void connect() throws IOException {
        kkSocket = new Socket(hostName, portNumber);
        out = new PrintWriter(kkSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
    }

    private String predict(String iStr) throws AnalysisEngineProcessException {
        StringBuilder oStr = new StringBuilder();
        if (iStr != null) {
            //next 4 were commented out using the 5th, Young asked to have them back
            String endMsgE = "--<eoscE>--";
            iStr += endMsgE;
            byte[] bText = iStr.getBytes();
            String nStr = new String(bText, UTF_8);
//            out.println(iStr + "--<eoscE>--");//nStr);
            out.println(nStr);
            out.flush();
        } else {
            LOGGER.debug("iStr was NULL");
        }
        String str;
        try {
            while ((str = in.readLine()) != null) {
                oStr.append(str).append("\n");
            }
        } catch (IOException e) {
            LOGGER.throwing(e);
            throw new AnalysisEngineProcessException(e);
        }
        return oStr.toString();
    }

    public void run() {
        LOGGER.debug("RNN Thread Begin");
        annotationNumber = 400;
        try {//See SvmAnnotatorThread for comments on performance
            StringBuilder sb = new StringBuilder();
            for (Sentence sentence : jCas.getAnnotationIndex(Sentence.class)) {
                for (FeatureVector fv : JCasUtil.selectCovered(FeatureVector.class, sentence)) {
                    sb.append(FeatureVectorWrapper.getNnStr(fv)).append("\n");
                }
                sb.append("\n");
            }
            //TODO could disconnect be sooner?

            connect();
            //logger.debug("RNN Thread connected");
            String oStr = predict(sb.toString());
            //logger.debug("RNN Thread returned");
            ArrayList<String> outs = new ArrayList<>();
            readTag(oStr, outs);

            String method = "RNN";
            readPredictRnn(outs, method, 0.0f);
            disconnect();

            FilterForThreads.filter(newPii, jCas, fileName, "RNN");
        } catch (Exception e) {
            LOGGER.throwing(e);
        }
        LOGGER.debug("RNN Thread done");
        doneSignal.countDown();
    }

    private void readPredictRnn(ArrayList<String> inst, String method, float conf) {
        String del = "\t";
        String cTag = "O";
        String nTag = "O";

        String begin = "";
        String end = "";
        //String file = "";

        String sBegin = "0";

        boolean start = false;

        // HashMap<String, Integer> ids = new HashMap<>();
        if (!inst.isEmpty()) {
            String nStr = inst.get(0);
            String[] cStrA;
            String[] nStrA = nStr.split(del);
            for (int i = 0; i < inst.size(); i++) {
                //cStr=nStr;
                cStrA = nStrA;

                if (i < inst.size() - 1) {
                    nStr = inst.get(i + 1);
                }
                nStrA = nStr.split(del);
                //TODO: possible speed ups: precompile regex for del, consider StringTokenizer instead of regex split
                //only pull out cTag from cStrA w/o splitting it, maybe by doing a findLast call
                //then delay split until cTag isn't "O"
                //ditto for nStrA, only have split stuff for nStr wihen it will exist (above if checks)
                //test speed of instead of replace, using substring(2)
                //
                //map ids of file to id is pointless unless made static
                //just make ids an int
                //does ID really need to be string? can it just be an int counter instead of Pi?

                // String cStrA[] = cStr.split(del);
                // String nStrA[] = nStr.split(del);

                //<unk>	60	61		O	O
                //          -8      -7      -6      -5      -4              -3      -2      -1
                //removed file, token number, sentence number and token itself from returned data from RunDeid.py
                if (cStrA.length < 3) {
                    cTag = "O";
                } else {//TODO: this seems fragile
                    begin = cStrA[cStrA.length - 4];
                    end = cStrA[cStrA.length - 3];
//                file = cStrA[cStrA.length - 3];
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

                        Util.addPii(newPii, jCas, Integer.parseInt(begin), Integer.parseInt(end), piiType, piiSubtype, "P" + annotationNumber++, method, conf);
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
                            Util.addPii(newPii, jCas, Integer.parseInt(sBegin), Integer.parseInt(end), piiType, piiSubtype, "P" + annotationNumber++, method, conf);
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
}

