
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
import com.clinacuity.deid.util.Util;
import edu.lium.mira.Mira;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MiraAnnotatorThread implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private String fileName;
    private Mira mira;
    private JCas jCas;
    private CountDownLatch doneSignal;
    private List<PiiAnnotation> newPii;

    MiraAnnotatorThread(Mira mira, JCas jCas, CountDownLatch doneSignal, List<PiiAnnotation> newPii, String fileName) {
        this.mira = mira;
        this.jCas = jCas;
        this.doneSignal = doneSignal;
        this.newPii = newPii;
        this.fileName = fileName;
    }

    public void run() {
        LOGGER.debug("MIRA Thread Begin");
        try {//See SvmAnnotatorThread for performance comments
            ArrayList<String> insts = new ArrayList<>();
            for (Sentence sentence : jCas.getAnnotationIndex(Sentence.class)) {
                for (FeatureVector fv : JCasUtil.selectCovered(FeatureVector.class, sentence)) {
                    insts.add(FeatureVectorWrapper.getStr(fv));
                }
                insts.add("");
            }
            ArrayList<String> outs = new ArrayList<>();
            predict(insts, outs);
            Util.readPredict(newPii, jCas, outs, "MIRA", 0.0f);
            FilterForThreads.filter(newPii, jCas, fileName, "MIRA");
        } catch (Exception e) {
            LOGGER.throwing(e);
        }
        LOGGER.debug("MIRA Thread done");
        doneSignal.countDown();
    }

    public void predict(ArrayList<String> insts, ArrayList<String> outs) {
        mira.test(insts, outs);
    }

}
