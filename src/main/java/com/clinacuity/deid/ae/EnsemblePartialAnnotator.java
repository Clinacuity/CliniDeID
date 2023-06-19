
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

import com.clinacuity.deid.ae.regex.impl.RegExAnnotatorThread;
import com.clinacuity.deid.type.PiiAnnotation;
import com.clinacuity.deid.util.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

//This class does the same work as EnsembleAnnotator except that it supports excluding one or more parts
//Separated so that EnsembleAnnotator is simpler and faster (no ifs in run() )
public class EnsemblePartialAnnotator extends EnsembleAnnotator {
    private static final Logger LOGGER = LogManager.getLogger();
    private int numberThreads;

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);
    }

    @Override
    protected void initializeSubClassSpecific() throws ResourceInitializationException {
        int threadCount = NUMBER_OF_THREADS;
        String[] possibleExcludes = {"crf", "mira", "regex", "svm", "rnn"};
        for (String checkAe : possibleExcludes) {
            if (excludesList.contains(checkAe)) {
                threadCount--;
            }
        }
        if (threadCount == 0) {//nothing to do, things will mess up later if we don't return here
            return;
        }
        pool.shutdown();//already been initialized to NUMBER_OF_THREADS as static variable
        pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);
        List<Boolean> flags = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            flags.add(false);
        }

        if (excludesList.contains("crf")) {
            LOGGER.debug("Excluded mallet CRF engine");
        } else {
            InitializeEnsembleThread crf = new InitializeEnsembleThread(doneSignal, flags, numberThreads++, "Crf", this::initializeCrf);
            pool.execute(crf);
        }
        if (excludesList.contains("rnn")) {
            LOGGER.debug("Excluded RNN");
        } else {
            InitializeEnsembleThread rnn = new InitializeEnsembleThread(doneSignal, flags, numberThreads++, "Rnn", EnsembleAnnotator::initializeRnn);
            pool.execute(rnn);
        }
        if (excludesList.contains("svm")) {
            LOGGER.debug("Excluded SVM");
        } else {
            InitializeEnsembleThread svm = new InitializeEnsembleThread(doneSignal, flags, numberThreads++, "Svm", this::initializeSvm);
            pool.execute(svm);
        }
        if (excludesList.contains("mira")) {
            LOGGER.debug("Excluded MIRA");
        } else {
            InitializeEnsembleThread mira = new InitializeEnsembleThread(doneSignal, flags, numberThreads++, "Mira", this::initializeMira);
            pool.execute(mira);
        }
        if (excludesList.contains("regex")) {
            LOGGER.debug("Excluded Regex");
        } else {
            InitializeEnsembleThread regex = new InitializeEnsembleThread(doneSignal, flags, numberThreads++, "Regex", this::initializeRegex);
            pool.execute(regex);
        }
        waitAndFinishInitialize("Partial", doneSignal, flags);
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        if (numberThreads == 0) {
            return;
        }
        CountDownLatch doneSignal = new CountDownLatch(numberThreads);
        @SuppressWarnings("unchecked")
        List<PiiAnnotation>[] newPii = (ArrayList<PiiAnnotation>[]) new ArrayList<?>[numberThreads];
        Arrays.setAll(newPii, ArrayList::new);
        int piiIndex = 0;
        String fileName = Utilities.getFileName(jCas);

        if (!excludesList.contains("svm")) {
            SvmAnnotatorThread svm = new SvmAnnotatorThread(linearModel, svmLbl, svmLblR, fIdx, svmTmplItem, jCas, doneSignal, newPii[piiIndex++], fileName);
            pool.execute(svm);
        }
        if (!excludesList.contains("rnn")) {
            RnnAnnotatorThread rnn = new RnnAnnotatorThread(hostName, rnnPortNumber, 400, jCas, doneSignal, newPii[piiIndex++], fileName);
            pool.execute(rnn);
        }
        if (!excludesList.contains("mira")) {
            MiraAnnotatorThread miraThread = new MiraAnnotatorThread(mira, jCas, doneSignal, newPii[piiIndex++], fileName);
            pool.execute(miraThread);
        }
        if (!excludesList.contains("crf")) {
            CrfAnnotatorThread crf = new CrfAnnotatorThread(brownClusters, brownCutoff, extractor, contextExtractor, classifier, jCas, doneSignal, newPii[piiIndex++], fileName);
            pool.execute(crf);
        }
        if (!excludesList.contains("regex")) {
            RegExAnnotatorThread regex = new RegExAnnotatorThread(regexConcepts, floatNumberFormat, integerNumberFormat, jCas, doneSignal, newPii[piiIndex++], fileName);
            pool.execute(regex);
        }
        finishAndCopy(doneSignal, jCas, newPii);
        LOGGER.debug("EnsemblePartial done");
    }
}
