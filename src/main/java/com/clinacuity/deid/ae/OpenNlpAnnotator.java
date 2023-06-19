
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

import com.clinacuity.deid.type.BaseToken;
import com.clinacuity.deid.type.Chunk;
import com.clinacuity.deid.type.Sentence;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OpenNlpAnnotator extends JCasAnnotator_ImplBase {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String[] MODELS = {"en-ner-percentage", "en-ner-money", "en-ner-time", "en-ner-date", "en-ner-organization", "en-ner-location", "en-ner-person"};
    private static final String PATH_TO_MODELS = "data/openNlpModels/";
    private static final String SENTENCE_MODEL_FILE_NAME = "en-sent.bin";
    private static final String TOKENIZER_MODEL_FILE_NAME = "en-token.bin";
    private static final String POS_MODEL_FILE_NAME = "en-pos-maxent.bin";
    private static final String CHUNKER_MODEL_FILE_NAME = "en-chunker.bin";
    private List<NameFinderME> nerModels = new ArrayList<>();
    private SentenceDetectorME sentenceDetector;
    private TokenizerME tokenizer;
    private POSTaggerME posTagger;
    private ChunkerME chunker;

    @Override
    public void initialize(UimaContext ctx) throws ResourceInitializationException {
        super.initialize(ctx);
        try {
            SentenceModel smd = new SentenceModel(new File(PATH_TO_MODELS + SENTENCE_MODEL_FILE_NAME));
            sentenceDetector = new SentenceDetectorME(smd);
            LOGGER.debug("OpenNLP sentence model loaded");
            TokenizerModel tmd = new TokenizerModel(new File(PATH_TO_MODELS + TOKENIZER_MODEL_FILE_NAME));
            tokenizer = new TokenizerME(tmd);
            LOGGER.debug("OpenNLP tokenizer model loaded");
            POSModel pmd = new POSModel(new File(PATH_TO_MODELS + POS_MODEL_FILE_NAME));
            posTagger = new POSTaggerME(pmd);
            LOGGER.debug("OpenNLP pos model loaded");
            if (Thread.currentThread().isInterrupted()) {
                throw new RuntimeException("interrupted");//GUI stop occurred, don't want to just stop this initialize but stop everything
            }
            ChunkerModel cmd = new ChunkerModel(new File(PATH_TO_MODELS + CHUNKER_MODEL_FILE_NAME));
            chunker = new ChunkerME(cmd);
            LOGGER.debug("OpenNLP chunker model loaded");
        } catch (IOException e) {
            LOGGER.throwing(e);
            throw new ResourceInitializationException(e);
        }

        TokenNameFinderModel md;
        for (String fileName : MODELS) {
            try (InputStream is = new FileInputStream(PATH_TO_MODELS + fileName + ".bin")) {
                md = new TokenNameFinderModel(is);
                nerModels.add(new NameFinderME(md));
                LOGGER.debug("{} ner model initialized", fileName);
            } catch (IOException e) {
                LOGGER.throwing(e);
                throw new ResourceInitializationException(e);
            }
            if (Thread.currentThread().isInterrupted()) {
                throw new RuntimeException("interrupted");
            }
        }

        LOGGER.debug("OpenNlpAnnotator Initialized");
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        LOGGER.debug("OpenNlpAnnotator Begin");
        String text = jCas.getDocumentText();

        Span[] sentSpans = sentenceDetector.sentPosDetect(jCas.getDocumentText());

        ArrayList<Chunk> chList = new ArrayList<>();
        ArrayList<Chunk> nerList = new ArrayList<>();

        int cnt = 0;
        for (Span sentSpan : sentSpans) {
            addSentence(jCas, cnt, sentSpan);
            int start = sentSpan.getStart();

            String sentence = sentSpan.getCoveredText(text).toString();
            Span[] tokSpans = tokenizer.tokenizePos(sentence);

            //aggressive tokenization
            ArrayList<Span> nSs = new ArrayList<>();
            for (Span tokSpan : tokSpans) {
                splitTokens(tokSpan, nSs, sentence);
            }

            String[] tokens = new String[nSs.size()];
            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = nSs.get(i).getCoveredText(sentence).toString();
            }

            String[] tags = posTagger.tag(tokens);
            addBaseTokens(jCas, tags, nSs, start);


            Span[] chunks = chunker.chunkAsSpans(tokens, tags);

            for (Span chunk : chunks) {
                int b = (start + nSs.get(chunk.getStart()).getStart());
                int e = (start + nSs.get(chunk.getEnd() - 1).getEnd());
                addChunk(jCas, b, e, chunk.getType(), chList);
            }
            // if (true) {continue;}

            // ner
            for (NameFinderME model : nerModels) {
                findNer(jCas, model, tokens, nerList, nSs, start);
            }

            cnt++;
        }

        setChunkTag(jCas, chList);
        setNerTag(jCas, nerList);
    }

    private void findNer(JCas jCas, NameFinderME finder, String[] tokens, ArrayList<Chunk> list, ArrayList<Span> nSs, int start) {
        Span[] ners = finder.find(tokens);
        for (Span ner : ners) {
            int b = (start + nSs.get(ner.getStart()).getStart());
            int e = (start + nSs.get(ner.getEnd() - 1).getEnd());
            addChunk(jCas, b, e, ner.getType(), list);
        }
    }

    private void setChunkTag(JCas jCas, ArrayList<Chunk> list) {
        AnnotationIndex<?> BaseTokenIndex = jCas.getAnnotationIndex(BaseToken.type);
        for (Chunk chunk : list) {

            String sType = chunk.getChunkType();

            FSIterator<?> bIterator = BaseTokenIndex.subiterator(chunk);
            boolean ifStart = true;
            while (bIterator.hasNext()) {
                BaseToken tok = (BaseToken) bIterator.next();

                if (ifStart) {
                    tok.setChunk("B-" + sType);
                    ifStart = false;
                } else {
                    tok.setChunk("I-" + sType);
                }
            }
        }

    }

    private void setNerTag(JCas jCas, ArrayList<Chunk> list) {

        AnnotationIndex<?> BaseTokenIndex = jCas.getAnnotationIndex(BaseToken.type);

        for (Chunk ner : list) {

            String sType = ner.getChunkType();

            FSIterator<?> bIterator = BaseTokenIndex.subiterator(ner);
            boolean ifStart = true;
            while (bIterator.hasNext()) {
                BaseToken tok = (BaseToken) bIterator.next();

                if (ifStart) {
                    tok.setNer("B-" + sType);
                    ifStart = false;
                } else {
                    tok.setNer("I-" + sType);
                }
            }
        }

    }

    private void addChunk(JCas jCas, int b, int e, String chType, ArrayList<Chunk> chList) {
        Chunk ch = new Chunk(jCas, b, e);
        ch.setChunkType(chType);
        chList.add(ch);
//        ch.addToIndexes();  //This may be helpful for next oldCRF to get Chunks, but not with current model. Trying with modelWithChunk1
    }

    private void addSentence(JCas jCas, int cnt, Span sentSpan) {
        Sentence s = new Sentence(jCas, sentSpan.getStart(), sentSpan.getEnd());
        s.setSentNo(cnt);
        s.addToIndexes();
    }

    private void addBaseTokens(JCas jCas, String[] tags, ArrayList<Span> nSs, int b) {
        for (int i = 0; i < nSs.size(); i++) {
            Span tok = nSs.get(i);
            BaseToken t = new BaseToken(jCas, tok.getStart() + b, tok.getEnd() + b);
            t.setPartOfSpeech(tags[i]);
            t.setChunk("O");
            t.setNer("O");
            t.addToIndexes();
        }
    }

    private void splitTokens(Span tok, ArrayList<Span> list, String sentence) {

        String str = tok.getCoveredText(sentence).toString();
        char pCh = str.charAt(0);
        int s = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            boolean ifC = false;

            if (Character.isDigit(ch) && i > 0) {
                if (!Character.isDigit(pCh)) {
                    ifC = true;
                }
            } else if (Character.isLowerCase(ch) && i > 0) { // 0 - A // a  //ALMartial -> AL Martial
                if (!(Character.isLowerCase(pCh) || Character.isUpperCase(pCh))) {
                    ifC = true;
                }
            } else if (Character.isUpperCase(ch) && i > 0) { // 0 a - // A  A
                if (!Character.isUpperCase(pCh) || (i < str.length() - 1 && Character.isLowerCase(str.charAt(i + 1)))) {
                    ifC = true;
                }
            } else if (i > 0) {
                if (Character.isDigit(pCh) || Character.isLowerCase(pCh) || Character.isUpperCase(pCh) || Character.isWhitespace(pCh)) {
                    ifC = true;
                }
            }

            if (ifC && !Character.isWhitespace(ch)) {
                //split s i
                int b = s + tok.getStart();
                int e = i + tok.getStart();
                if (Character.isWhitespace(pCh)) {
                    e = i + tok.getStart() - 1;
                }
                Span sp = new Span(b, e);
                list.add(sp);
                s = i;
            }
            pCh = ch;
        }

        int b = s + tok.getStart();
        int e = tok.getStart() + str.length();
        Span sp = new Span(b, e);
        list.add(sp);
    }
}
