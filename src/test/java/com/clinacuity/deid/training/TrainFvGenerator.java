
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

package com.clinacuity.deid.training;

import com.clinacuity.deid.type.FeatureVector;
import com.clinacuity.deid.type.Sentence;
import com.clinacuity.deid.util.FeatureVectorWrapper;
import com.clinacuity.deid.util.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class TrainFvGenerator extends JCasAnnotator_ImplBase {
    public static final String SVM_MIRA_FV_FILENAME = "svmMiraFvFileName";
    public static final String RNN_FV_FILENAME = "rnnFvFileName";
    private static final Logger logger = LogManager.getLogger();
    private ArrayList<String> crfFvs;
    private ArrayList<String> rnnFvs;
    private int fileCnt = 0;
    private int fvIdx = 1;
    @ConfigurationParameter(name = SVM_MIRA_FV_FILENAME, description = "Name of output file with crf feature vectors")
    private String svmMiraFvFileName;
    @ConfigurationParameter(name = RNN_FV_FILENAME, description = "Name of output file with rnn feature vectors")
    private String rnnFvFileName;

    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);
        crfFvs = new ArrayList<>();
        rnnFvs = new ArrayList<>();
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        String filename = Utilities.getFileName(jCas);
        logger.debug("FV before {}, crfFvs: {}, rnnFvs: {}", filename, crfFvs.size(), rnnFvs.size());
        FSIterator<?> sentences = jCas.getAnnotationIndex(Sentence.type).iterator();
        AnnotationIndex<?> FeatureVectorIndex = jCas.getAnnotationIndex(FeatureVector.type);

        while (sentences.hasNext()) {
            Sentence sentence = (Sentence) sentences.next();
            FSIterator<?> fvIterator = FeatureVectorIndex.subiterator(sentence);
            while (fvIterator.hasNext()) {
                FeatureVector fv = (FeatureVector) fvIterator.next();
                crfFvs.add(FeatureVectorWrapper.getStr(fv));
                rnnFvs.add(FeatureVectorWrapper.getNnStr(fv));
            }
            crfFvs.add("");
            rnnFvs.add("");
        }

        fileCnt++;

        if (fileCnt > 2400) {
            try {
                fvFileWrite(svmMiraFvFileName + fvIdx, crfFvs);
                fvFileWrite(rnnFvFileName + fvIdx, rnnFvs);
            } catch (IOException e) {
                logger.throwing(e);
                throw new AnalysisEngineProcessException(e);
            }
            crfFvs.clear();
            rnnFvs.clear();
            fileCnt = 0;
            fvIdx++;
        }
        logger.debug("FV after {}, crfFvs: {}, rnnFvs: {}", filename, crfFvs.size(), rnnFvs.size());

    }

    private void fvFileWrite(String fileName, ArrayList<String> list) throws IOException {//TODO refactor with try w/ resources
        logger.debug("fvFileWrite {}", fileName);

        File fvFileS = new File(fileName);
        if (!fvFileS.exists()) {
            fvFileS.createNewFile();
        }
        PrintStream out = new PrintStream(new FileOutputStream(fvFileS));

        for (String s : list) {
            out.println(s);
        }

        out.flush();
        out.close();
    }

    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        logger.debug("collection process complete");

        try {
            if (fvIdx == 1) {
                fvFileWrite(svmMiraFvFileName, crfFvs);
                fvFileWrite(rnnFvFileName, rnnFvs);
            } else {
                fvFileWrite(svmMiraFvFileName + fvIdx, crfFvs);
                fvFileWrite(rnnFvFileName + fvIdx, rnnFvs);
            }
        } catch (IOException e) {
            logger.throwing(e);
            throw new AnalysisEngineProcessException(e);
        }
    }

}
//RNN: For pretrained word embedding, you need to download ‘komninos_english_embeddings.gz
//As different from other classifiers, RNN only needs “word” for training.
//You can use “CoNLL format file” generated by ‘TrainFvGenerator’ AE. (‘RnnFvFile’ parameter)