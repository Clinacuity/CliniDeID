
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

import com.clinacuity.deid.nlp.lookup.LuceneDictionary;
import com.clinacuity.deid.type.BaseToken;
import com.clinacuity.deid.type.DictionaryAnnotation;
import com.clinacuity.deid.util.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.util.ArrayList;

import static com.clinacuity.deid.mains.DeidPipeline.PII_LOG;

/**
 * This class implements the Dictionary Annotator. It annotates word-tokens and chunks that appear as an entry within the dictionaries. <br>
 * <br>
 * The dictionaries have previously been off-line indexed using Lucene. <br>
 * Currently there are dictionaries for person names (last and first names), locations, countries, cities, counties, nationalities, companies, and common words.
 * <p>
 * <br>
 * <br>
 * Optionally, the annotator can develop fuzzy searches according to a similarity threshold specified by input parameters.
 *
 * @author Oscar Ferrandez-Escamez
 * @version December 2011
 */
public class DictionaryAnnotator extends JCasAnnotator_ImplBase {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean FUZZY = true;
    private static final float FUZZY_SIM = .74f;
    private ArrayList<DictionaryEntry> dictionaries;

    /**
     * Initialization before processing the CAS (load parameters from the configuration file). Performs any startup tasks required by this annotator. <br>
     * Initializes the list of dictionaries as well as the required parameters for fuzzy searches (if applicable).
     */
    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
        dictionaries = new ArrayList<>();
        final String[] dicts = {"luceneIndex/mit/names/UScensus1990/lastnames_ambig", "luceneIndex/mit/names/UScensus1990/lastnames_unambig", "luceneIndex/mit/names/UScensus1990/lastnames_popular", "luceneIndex/mit/names/UScensus1990/names_ambig", "luceneIndex/mit/names/UScensus1990/names_unambig", "luceneIndex/mit/names/UScensus1990/names_popular", "luceneIndex/mit/commonWords", "luceneIndex/mit/locations/locations_unambig", "luceneIndex/mit/countries", "luceneIndex/companies", "luceneIndex/commonestWords", "luceneIndex/US_cities", "luceneIndex/US_counties", "luceneIndex/nationalities"};
        boolean error = false;
        for (String dict : dicts) {
            DictionaryEntry dictEntry = new DictionaryEntry();
            String[] dname = dict.split("/");//could use last index of / and substring
            dictEntry.name = dname[dname.length - 1];
            try {
                dictEntry.ludic = new LuceneDictionary(Utilities.getExternalFile(dict).getAbsolutePath(), "name", 1, 0, new KeywordAnalyzer());
            } catch (Exception e) {
                LOGGER.error("Loading lucene dict {}", dictEntry.name);
                LOGGER.throwing(e);
                error = true;
            }
            dictionaries.add(dictEntry);
        }
        if (error) {
            throw (new ResourceInitializationException());//logs will have the details
        }
        LOGGER.debug("DictionaryAnnotator initialized");
    }

    /**
     * Invokes this annotator's analysis logic. This annotator annotates word-tokens and chunks using Lucene-based index of dictionaries. <br>
     * <br>
     * {@link DictionaryAnnotation}s will be added to the CAS with a feature indicating the dictionary used.
     *
     * @param jCas the JCas to process
     * @throws AnalysisEngineProcessException if a failure occurs during processing.
     */
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        LOGGER.debug("DictionaryAnnotator begin");
        if (FUZZY)
            applyLuceneFuzzySearch(jCas);
        else
            applyLuceneSearch(jCas);
    }

    /**
     * Applies keyword searches to word-tokens and chunks according to the list of dictionaries.
     *
     * @param jCas the CAS
     */
    private void applyLuceneSearch(JCas jCas) throws AnalysisEngineProcessException {
        for (BaseToken word : jCas.getAnnotationIndex(BaseToken.class)) {//TODO: consider using hash map or an array of booleans inside annotation instead of many booleans,
            DictionaryAnnotation annot = null;
            for (int i = 0; i < dictionaries.size(); i++) {
                try {
                    if (dictionaries.get(i).ludic.exactTermInDict(word.getCoveredText())) {
                        if (annot == null) {
                            annot = new DictionaryAnnotation(jCas, word.getBegin(), word.getEnd());
                            annot.addToIndexes();
                        }
                        setAnnotations(annot, i);
                    }
                } catch (Exception e) {
                    LOGGER.log(PII_LOG, "ERROR: Looking up term {} in {}", word.getCoveredText(), dictionaries.get(i).name);
                    LOGGER.throwing(e);
                    throw new AnalysisEngineProcessException(e);
                }
            }
        }
        /*
        logger.debug("DictionaryAnnotator begin Chunk");

        for (Chunk chk : jCas.getAnnotationIndex(Chunk.class)) {
            boolean found = false;
            DictionaryAnnotation annot = new DictionaryAnnotation(jCas);
            for (int i = 0; i < dictionaries.size(); i++) {
                try {
                    if (dictionaries.get(i).ludic.exactTermInDict(chk.getCoveredText())) {
                        found = true;
                        setAnnotations(annot, i);
                    }
                } catch (Exception e) {
                    logger.log(PII_LOG, "ERROR: Looking up term (chunk) {} in {}", chk.getCoveredText(), dictionaries.get(i).name);
                    logger.throwing(e);
                }
            }

            if (found) {
                annot.setBegin(chk.getBegin());
                annot.setEnd(chk.getEnd());
                annot.addToIndexes();
            }
        }*/
    }

    /**
     * Applies fuzzy searches to word-tokens and chunks. Keyword searches will be considered for the dictionary of countries, since it was demonstrated that no
     * improvement was achieved regarding fuzzy country searches.
     *
     * @param jCas the CAS
     */
    private void applyLuceneFuzzySearch(JCas jCas) throws AnalysisEngineProcessException {
        for (BaseToken word : jCas.getAnnotationIndex(BaseToken.class)) {
            boolean found = false;
            DictionaryAnnotation annot = new DictionaryAnnotation(jCas);
            // logger.debug(annot.getCoveredText());
            for (int i = 0; i < dictionaries.size(); i++) {
                boolean inDict = false;
                try {
                    if (// dictionaries.get(i).name.equals("locations_unambig") ||
                        // dictionaries.get(i).name.equals("US_cities") ||
                        // dictionaries.get(i).name.equals("US_counties") ||
                        // dictionaries.get(i).name.equals("countries") ||
                            dictionaries.get(i).name.contains("names"))
                        // || dictionaries.get(i).name.equals("companies"))
                        inDict = dictionaries.get(i).ludic.exactFuzzyTermInDict(word.getCoveredText(), FUZZY_SIM);
                    else
                        inDict = dictionaries.get(i).ludic.exactTermInDict(word.getCoveredText());
                    if (inDict) {
                        found = true;
                        setAnnotations(annot, i);
                    }
                } catch (IOException e) {
                    LOGGER.log(PII_LOG, "ERROR: Looking up term {} in {}", word.getCoveredText(), dictionaries.get(i).name);
                    LOGGER.throwing(e);
                    throw new AnalysisEngineProcessException(e);
                }
            }
            if (found) {
                annot.setBegin(word.getBegin());
                annot.setEnd(word.getEnd());
                annot.addToIndexes();
            }
        }
        /*
        logger.debug("DictionaryAnnotator begin Chunk2");
        for (Chunk chk : jCas.getAnnotationIndex(Chunk.class)) {
            boolean found = false;
            DictionaryAnnotation annot = new DictionaryAnnotation(jCas);
            for (int i = 0; i < dictionaries.size(); i++) {
                boolean inDict = false;
                try {
                    // fuzzy search for all textual dicts
                    if (// dictionaries.get(i).name.equals("locations_unambig") ||
                        // dictionaries.get(i).name.equals("US_cities") ||
                        // dictionaries.get(i).name.equals("US_counties") ||
                        // dictionaries.get(i).name.equals("countries") ||
                    dictionaries.get(i).name.contains("names"))
                        // || dictionaries.get(i).name.equals("companies"))
                        inDict = dictionaries.get(i).ludic.exactFuzzyTermInDict(chk.getCoveredText(), fuzzySim);
                    else
                        inDict = dictionaries.get(i).ludic.exactTermInDict(chk.getCoveredText());
                    if (inDict) {
                        found = true;
                        setAnnotations(annot, i);
                    }
                } catch (Exception e) {
                    logger.log(PII_LOG, "ERROR: Looking up term (chunk) {} in {}", chk.getCoveredText(), dictionaries.get(i).name);
                    logger.throwing(e);
                }
            }

            if (found) {
                annot.setBegin(chk.getBegin());
                annot.setEnd(chk.getEnd());
                annot.addToIndexes();
            }
        }*/
    }

    /**
     * Sets true if the annotation value is found in a dictionary
     *
     * @param annot the DictionaryAnnotation to set a lookup value on
     * @param i     an iterator
     */
    private void setAnnotations(DictionaryAnnotation annot, int i) {
        switch (dList.fromString(dictionaries.get(i).name)) {//TODO with array of booleans in annotation, could use int enum instead and then no switch at all
            case lastnames_ambig:
                annot.setMit_lastnames_ambig(true);
                break;
            case lastnames_unambig:
                annot.setMit_lastnames_unambig(true);
                break;
            case lastnames_popular:
                annot.setMit_lastnames_popular(true);
                break;
            case names_ambig:
                annot.setMit_names_ambig(true);
                break;
            case names_unambig:
                annot.setMit_names_unambig(true);
                break;
            case names_popular:
                annot.setMit_names_popular(true);
                break;
            case commonWords:
                annot.setMit_common_words(true);
                break;
            case locations_unambig:
                annot.setMit_locations_unambig(true);
                break;
            case countries:
                annot.setMit_countries(true);
                break;
            case companies:
                annot.setWiki_gate_mit_companies(true);
                break;
            case commonestWords:
                annot.setMit_commonest_words(true);
                break;
            case US_cities:
                annot.setUS_cities(true);
                break;
            case US_counties:
                annot.setUS_counties(true);
                break;
            case nationalities:
                annot.setNationalities(true);
                break;
            default:
                LOGGER.error("No dict recognized!!!");
        }
    }

    private enum dList {
        lastnames_ambig, lastnames_unambig, lastnames_popular, names_ambig, names_unambig, names_popular, locations_unambig, commonWords, countries, companies, commonestWords, US_cities, US_counties, nationalities;

        public static dList fromString(String str) {
            return valueOf(str);
        }
    }

    private static class DictionaryEntry {
        String name;
        LuceneDictionary ludic;

        DictionaryEntry() {
            name = null;
            ludic = null;
        }
    }
}
