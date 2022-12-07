
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

package com.clinacuity.deid.ae;


import com.clinacuity.deid.type.FeatureVector;
import com.clinacuity.deid.type.PiiAnnotation;
import com.clinacuity.deid.type.Sentence;
import com.clinacuity.deid.util.FeatureVectorWrapper;
import com.clinacuity.deid.util.SvmTemplateItem;
import com.clinacuity.deid.util.Util;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Predict;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

import static com.clinacuity.deid.mains.DeidPipeline.PII_LOG;

public class SvmAnnotatorThread implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private String fileName;
    private CountDownLatch doneSignal;
    private Model linearModel;
    private HashMap<String, String> svmLbl;
    private HashMap<String, String> svmLblR;
    private HashMap<String, Integer> fIdx;
    private JCas jCas;
    private List<PiiAnnotation> newPii;
    private List<SvmTemplateItem> tmplItem;

    SvmAnnotatorThread(Model linearModel, HashMap<String, String> svmLbl, HashMap<String, String> svmLblR,
                       HashMap<String, Integer> fIdx, List<SvmTemplateItem> tmplItem, JCas jCas, CountDownLatch done, List<PiiAnnotation> newPii, String fileName) {
        this.linearModel = linearModel;
        this.svmLbl = svmLbl;
        this.svmLblR = svmLblR;
        this.fIdx = fIdx;
        this.jCas = jCas;
        this.doneSignal = done;
        this.newPii = newPii;
        this.fileName = fileName;
        this.tmplItem = tmplItem;
    }

    public void run() {
        LOGGER.debug("SVM Thread Begin");
        try {
            //Tried iterators, foreach; subIterator and both versions of selectCovered; indexCovered
            //indexCovered was slower, the rest about the same.
            //Using the most readable
            ArrayList<String> spanInfos = new ArrayList<>();
            ArrayList<ArrayList<String>> insts = new ArrayList<>();

            for (Sentence sentence : jCas.getAnnotationIndex(Sentence.class)) {
                for (FeatureVector fv : JCasUtil.selectCovered(FeatureVector.class, sentence)) {
                    addInst(insts, FeatureVectorWrapper.getStr(fv));
                    spanInfos.add(FeatureVectorWrapper.getNnStr(fv));
                }
                spanInfos.add("");
            }

            // convert to liblinear format
            ArrayList<String> nList = new ArrayList<>();
            convert(insts, nList);
            ArrayList<String> outs = new ArrayList<>();
//
//        for (int i=0; i< insts.size(); i++) {// (ArrayList<String> as : insts) {
//            logger.debug("origin {}", insts.get(i).toString());
//            logger.debug("became {}", nList.get(i));
//        }

            predict(nList, outs);
            ArrayList<String> nOuts = new ArrayList<>();
            merge(spanInfos, outs, nOuts);
            Util.readPredict(newPii, jCas, nOuts, "SVM", 0.0f);
            FilterForThreads.filter(newPii, jCas, fileName, "SVM");
        } catch (Exception e) {
            LOGGER.throwing(e);
        }
        LOGGER.debug("SVM Thread done");
        doneSignal.countDown();
    }

    private void merge(ArrayList<String> spanInfos, ArrayList<String> outs, ArrayList<String> nOuts) {

//        DecimalFormat df = new DecimalFormat("#.######");
//        String[] labels = outs.get(0).replace("labels", "").trim().split(" ");

//        TreeMap<Integer, Integer> lMap = new TreeMap<>();
//        int lI = 0;
//        for (String key : labels) {
//            int i = Integer.parseInt(key);
//            lMap.put(i, lI); // 7 is 0 (first)
//            lI++;
//        }

        int oI = 1;
        for (int i = 0; i < spanInfos.size(); i++) {
            String spanInfo = spanInfos.get(i);
            if (spanInfo.isEmpty()) {
                nOuts.add("");
            } else {
                nOuts.add(spanInfo + "\t" + getSvmTag(outs.get(oI)));//, lMap, df));
                oI++;
            }
        }
    }

    private String getSvmTag(String str) {//, TreeMap<Integer, Integer> lMap, DecimalFormat df) {
        return svmLblR.get(str.substring(0, str.indexOf('.')));
//        String[] strA = str.split(" ");//TODO: faster to use str.substring(0,str.indexOf(" ")); ????
//
//        int label = (int) Double.parseDouble(strA[0]); // answer
//        //String prob = strA[lMap.get(label) + 1];
//        //Double probD = Double.parseDouble(prob);
//        return svmLblR.get(Integer.toString(label));
    }


    private void addInst(ArrayList<ArrayList<String>> sList, String str) {
        String[] strA = str.split("\t");
        ArrayList<String> list = new ArrayList<>(Arrays.asList(strA));
        sList.add(list);
    }

    private void convert(ArrayList<ArrayList<String>> insts, ArrayList<String> out) {
        final String outTag = "O";
        final String outRange = "*null*";
        for (int i = 0; i < insts.size(); i++) {
            ArrayList<String> inst = insts.get(i);

            // label
            String tag = inst.get(inst.size() - 1);
            if (svmLbl.containsKey(tag)) {
                tag = svmLbl.get(tag);
            } else {
                tag = outTag;
                int k = i;
                LOGGER.log(PII_LOG, "{}", () -> "ERROR: " + outTag + " not found in label map, i: " + k + ", features: " + inst.toString());
            }

            TreeSet<Integer> sb = new TreeSet<>();
            // for each feature template

/*            ArrayList<String> orig=new ArrayList<>();

            for (int tmplIndex = 0; tmplIndex < tmpl.size(); tmplIndex++) {
                String fs = tmpl.get(tmplIndex);
                String[] fsA = fs.split(":", 2);
                String fL = fsA[0]; // label
                String[] fA = fsA[1].replace("%x", "").replace("[", "").replace("]", "").split("/");
                StringBuilder fStrBuilder = new StringBuilder();
                for (int k = 0; k < fA.length; k++) {
                    String f = fA[k];
                    String[] ff = f.split(",");
                    int rI = Integer.parseInt(ff[0]) + i;
                    int cI = Integer.parseInt(ff[1]);
                    String tmp;
                    if (rI < 0 || rI >= insts.size()) {
                        tmp = outRange;
                    } else {
                        tmp = insts.get(rI).get(cI);
                    }
                    if (k == 0) {//was fStrBuilder=tmp
                        fStrBuilder.setLength(0);
                        fStrBuilder.append(tmp);
                    } else {
                        fStrBuilder.append("/").append(tmp);// += "/" + tmp;
                    }
                }

                // make each feature
                String fStr = fStrBuilder.toString();
                if (!fStr.contains(outRange)) {
                    fStr = fL + ":" + fStr;
                }
                orig.add( fStr);

                if (fIdx.containsKey(fStr)) {
                    sb.add(fIdx.get(fStr));
                }
            } // for all template
*/
            StringBuilder fStrBuilder = new StringBuilder();
            for (int tmplIndex = 0; tmplIndex < tmplItem.size(); tmplIndex++) {
                boolean isOutRange = false;
                int rI = tmplItem.get(tmplIndex).rI0 + i;
                int cI = tmplItem.get(tmplIndex).cI0;
                fStrBuilder.setLength(0);
                if (rI < 0 || rI >= insts.size()) {
                    fStrBuilder.append(outRange);
                    isOutRange = true;
                } else {
                    fStrBuilder.append(insts.get(rI).get(cI));
                }
                if (tmplItem.get(tmplIndex).hasSecond) {
                    rI = tmplItem.get(tmplIndex).rI1 + i;
                    cI = tmplItem.get(tmplIndex).cI1;
                    if (rI < 0 || rI >= insts.size()) {
                        fStrBuilder.append("/").append(outRange);
                        isOutRange = true;
                    } else {
                        fStrBuilder.append("/").append(insts.get(rI).get(cI));
                    }
                    if (tmplItem.get(tmplIndex).hasThird) {
                        rI = tmplItem.get(tmplIndex).rI2 + i;
                        cI = tmplItem.get(tmplIndex).cI2;
                        if (rI < 0 || rI >= insts.size()) {
                            fStrBuilder.append("/").append(outRange);
                            isOutRange = true;
                        } else {
                            fStrBuilder.append("/").append(insts.get(rI).get(cI));
                        }
                    }
                }
                String fStr;
                if (!isOutRange) {
                    fStr = tmplItem.get(tmplIndex).label + ":" + fStrBuilder.toString();
                } else {
                    fStr = fStrBuilder.toString();
                }

                if (fIdx.containsKey(fStr)) {
                    sb.add(fIdx.get(fStr));
                }
            }

            StringBuilder fvStr = new StringBuilder(tag + " ");
            for (int sbt : sb) {
                fvStr.append(sbt).append(":1 ");// += sbt + ":1 ";
            }
            out.add(fvStr.toString());
        }// for each word
    }

    public void predict(ArrayList<String> insts, ArrayList<String> outs) {
        Predict.doPredict(insts, outs, linearModel);
    }


}
