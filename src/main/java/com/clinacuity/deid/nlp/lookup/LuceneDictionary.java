
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

package com.clinacuity.deid.nlp.lookup;

import com.clinacuity.deid.nlp.umls.UmlsConcept;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around Lucene dictionary.
 *
 * @author Julien Thibault (textractor) - modified by Oscar Ferrandez-Escamez
 */
public class LuceneDictionary {
    private static final Logger logger = LogManager.getLogger();
    private IndexSearcher _searcher;
    private String _lookupFieldName;
    private IndexReader _indexReader;
    private Analyzer _analyzer;

    private int _maxNumHits;

    /**
     * Instantiate new Lucene dictionary.
     *
     * @param luceneIndexPath
     * @param lookupFieldName
     * @param maxHits
     * @param cutoff
     * @throws IOException
     */
    public LuceneDictionary(String luceneIndexPath, String lookupFieldName, int maxHits, float cutoff, Analyzer a) throws IOException {
        File indexFile = new File(luceneIndexPath);
        if (indexFile.exists()) {
            FSDirectory fsDirectory = FSDirectory.open(new File(luceneIndexPath));

            // read Lucene index
            _indexReader = IndexReader.open(fsDirectory);
            fsDirectory.close();
            _searcher = new IndexSearcher(_indexReader);
            _analyzer = a;

            _lookupFieldName = lookupFieldName;
            _maxNumHits = maxHits;
            // _minHitScore = cutoff;

            // logger.debug("[LUCENE] Dictionary '{}' loaded", luceneIndexPath, loaded);
            // logger.debug("\tMax hits: {}", _maxNumHits);
            // logger.debug("\tField: {}", _lookupFieldName);
        } else
            throw new IOException("Lucene index not found at '" + luceneIndexPath + "'");
    }

    /**
     * Instantiate new Lucene dictionary
     * <p>
     * by default uses standard analyzer (specially for UMLS).
     *
     * @param luceneIndexPath
     * @param lookupFieldName
     * @param maxHits
     * @param cutoff
     * @throws IOException
     */
    public LuceneDictionary(String luceneIndexPath, String lookupFieldName, int maxHits, float cutoff) throws IOException {
        File indexFile = new File(luceneIndexPath);
        if (indexFile.exists()) {

            // read Lucene index
            _indexReader = IndexReader.open(FSDirectory.open(new File(luceneIndexPath)));
            _searcher = new IndexSearcher(_indexReader);
            _analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);

            _lookupFieldName = lookupFieldName;
            _maxNumHits = maxHits;
            // _minHitScore = cutoff;

            // logger.debug("[LUCENE] Dictionary '{}' loaded", luceneIndexPath);
            // logger.debug("\tMax hits: {}", _maxNumHits);
            // logger.debug("\tField: {}", _lookupFieldName);
        } else
            throw new IOException("Lucene index not found at '" + luceneIndexPath + "' in " + System.getProperty("user.dir"));
    }

    /**
     * Lookup a term in the dictionary.
     *
     * @param term Term to lookup
     * @return List of hits
     * @throws IOException
     */
    public List<IndexHit> exactTermLookup(String term) throws IOException {
        TopScoreDocCollector collector;
        ArrayList<IndexHit> hitCollection = new ArrayList<>();
        String normTerm = term.toLowerCase().trim();
        // logger.error("Exact Term Lookup for: {}", normTerm);

        try {
            // create query (original)
            TermQuery _lastQuery = new TermQuery(new Term(_lookupFieldName, normTerm));
            // logger.error(_lastQuery.toString());

            // QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, _lookupFieldName, _analyzer);
            // Query query = parser.parse(QueryParser.escape(normTerm));

            // create search
            collector = TopScoreDocCollector.create(_maxNumHits, true);

            // logger.debug("[LUCENE] Looking up '{}':", normTerm);
            // _searcher.search( query, collector);

            _searcher.search(_lastQuery, collector);

            // collect hits
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            int numTotalHits = collector.getTotalHits();
            // logger.debug("\tNumber of hits: {}", numTotalHits);

            int end = Math.min(numTotalHits, _maxNumHits);
            for (int i = 0; i < end; i++) {
                // if (hits[i].score > _minHitScore)
                ScoreDoc hit = hits[i];
                hitCollection.add(new IndexHit(hit, getConcept(hit)));
                // logger.debug("\thit: {}", hits[i].toString());
            }
            return hitCollection;
        } catch (IOException ioe) {
            throw ioe;
        } finally {
            //collector = null;
        }
    }

    /**
     * Lookup a phrase in the dictionary.
     *
     * @param phrase Term to lookup
     * @return List of hits
     * @throws IOException
     */
    public List<IndexHit> standardTermLookup(String phrase) throws IOException {
        TopScoreDocCollector collector;
        ArrayList<IndexHit> hitCollection = new ArrayList<>();
        String normPhrase = phrase.toLowerCase().trim();
        // logger.error("Standard Term Lookup for: {}", normPhrase);
        try {
            // create phrase query
            QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, _lookupFieldName, _analyzer);
            Query query = parser.parse(QueryParser.escape(normPhrase));
            // logger.error(query.toString());

            /*
             * PhraseQuery q = new PhraseQuery(); _lastQuery = q; String[] tokens = phrase.trim().toLowerCase().split(" "); for (String token : tokens){ q.add(
             * new Term(_lookupFieldName, token) ); }
             */

            // create search
            collector = TopScoreDocCollector.create(_maxNumHits, true);
            // _searcher.search(q, collector);
            // logger.debug("[LUCENE] Looking up '{}':", normPhrase);
            _searcher.search(query, collector);

            // collect hits
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            int numTotalHits = collector.getTotalHits();
            // logger.debug("\tNumber of hits: {}", numTotalHits);

            int end = Math.min(numTotalHits, _maxNumHits);
            for (int i = 0; i < end; i++) {
                // if (hits[i].score > _minHitScore)
                ScoreDoc hit = hits[i];
                hitCollection.add(new IndexHit(hit, getConcept(hit)));
                // logger.debug("\thit: {}", hits[i].toString());
            }
            return hitCollection;
        } catch (ParseException ioe) {
            throw new IOException(ioe);
        } catch (IOException ioe) {
            throw ioe;
        } finally {
            // collector = null;
        }
    }

    /**
     * Lookup a term in the dictionary.
     *
     * @param term Term to lookup
     * @param x    indicates NO LOWERCASE
     * @return List of hits
     * @throws IOException
     */
    public List<IndexHit> exactTermLookup(String term, int x) throws IOException {
        TopScoreDocCollector collector;
        ArrayList<IndexHit> hitCollection = new ArrayList<IndexHit>();
        String normTerm = term.trim();
        // logger.error("Exact Term Lookup for: {}", normTerm);

        try {
            // create query
            TermQuery _lastQuery = new TermQuery(new Term(_lookupFieldName, normTerm));

            // QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, _lookupFieldName, _analyzer);
            // Query query = parser.parse(QueryParser.escape(normTerm));

            // create search
            collector = TopScoreDocCollector.create(_maxNumHits, true);

            // logger.debug("[LUCENE] Looking up '{}':", normTerm);
            // _searcher.search( query, collector);
            _searcher.search(_lastQuery, collector);

            // collect hits
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            int numTotalHits = collector.getTotalHits();
            // logger.debug("\tNumber of hits: {}", numTotalHits);

            int end = Math.min(numTotalHits, _maxNumHits);
            for (int i = 0; i < end; i++) {
                // if (hits[i].score > _minHitScore)
                ScoreDoc hit = hits[i];
                hitCollection.add(new IndexHit(hit, getConcept(hit)));
                // logger.debug("\thit: {}", hits[i].toString());
            }
            return hitCollection;
        } catch (IOException ioe) {
            throw ioe;// new Exception(ioe);
        } finally {
            //collector = null;
        }
    }

    /**
     * Lookup a phrase in the dictionary.
     *
     * @param phrase Term to lookup
     * @param x      indicates NO LOWERCASE
     * @return List of hits
     * @throws IOException
     */
    public List<IndexHit> standardTermLookup(String phrase, int x) throws IOException {
        TopScoreDocCollector collector;
        ArrayList<IndexHit> hitCollection = new ArrayList<IndexHit>();
        String normPhrase = phrase.trim();
        // logger.error("Standard Term Lookup for: {}", normPhrase);
        try {
            // create phrase query
            QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, _lookupFieldName, _analyzer);
            Query query = parser.parse(QueryParser.escape(normPhrase));
            /*
             * PhraseQuery q = new PhraseQuery(); _lastQuery = q; String[] tokens = phrase.trim().toLowerCase().split(" "); for (String token : tokens){ q.add(
             * new Term(_lookupFieldName, token) ); }
             */

            // create search
            collector = TopScoreDocCollector.create(_maxNumHits, true);
            // _searcher.search(q, collector);
            // logger.debug("[LUCENE] Looking up '{}':", normPhrase);
            _searcher.search(query, collector);

            // collect hits
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            int numTotalHits = collector.getTotalHits();
            // logger.debug("\tNumber of hits: {}", numTotalHits);

            int end = Math.min(numTotalHits, _maxNumHits);
            for (int i = 0; i < end; i++) {
                // if (hits[i].score > _minHitScore)
                ScoreDoc hit = hits[i];
                hitCollection.add(new IndexHit(hit, getConcept(hit)));
                // logger.debug("\thit: {}", hits[i].toString());
            }
            return hitCollection;
        } catch (ParseException ioe) {
            throw new IOException(ioe);
        } catch (IOException ioe) {
            throw ioe;
        } finally {
            //collector = null;
        }
    }

    /**
     * Return the number of entries in the index.
     *
     * @return Number of entries
     */
    public int getNumberOfEntries() {
        return _indexReader.numDocs();
    }

    private String getConcept(ScoreDoc hit) throws CorruptIndexException, IOException {
        Document doc = _searcher.doc(hit.doc);

        return doc.get("name");

    }

    /**
     * Lookup a term in the dictionary.
     *
     * @param term Term to lookup
     * @return true if found
     * @throws IOException
     */
    public boolean exactTermInDict(String term) throws IOException {
        TopScoreDocCollector collector;

        String normTerm = term.toLowerCase().trim();
        // logger.error("Exact Term inDict Lookup for: {}", normTerm);

        try {
            // create query
            TermQuery _lastQuery = new TermQuery(new Term(_lookupFieldName, normTerm));

            // QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, _lookupFieldName, _analyzer);
            // Query query = parser.parse(QueryParser.escape(normTerm));

            // create search
            collector = TopScoreDocCollector.create(_maxNumHits, true);

            // logger.debug("[LUCENE] Looking up '{}':", normTerm);
            // _searcher.search( query, collector);

            _searcher.search(_lastQuery, collector);

            if (collector.getTotalHits() > 0)
                return true;

            return false;
        } catch (IOException ioe) {
            throw ioe;//new Exception(ioe);
        } finally {
            // collector = null;//TODO  WHY!!!
        }
    }

    /**
     * Lookup a phrase in the dictionary.
     *
     * @param phrase Term to lookup
     * @return true if found
     * @throws IOException
     */
    public boolean standardTermInDict(String phrase) throws IOException {
        TopScoreDocCollector collector;

        String normPhrase = phrase.toLowerCase().trim();
        // logger.error("Standard Term inDict Lookup for: {}", normPhrase);
        try {
            // create phrase query
            QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, _lookupFieldName, _analyzer);
            Query query = parser.parse(QueryParser.escape(normPhrase));
            /*
             * PhraseQuery q = new PhraseQuery(); _lastQuery = q; String[] tokens = phrase.trim().toLowerCase().split(" "); for (String token : tokens){ q.add(
             * new Term(_lookupFieldName, token) ); }
             */

            // create search
            collector = TopScoreDocCollector.create(_maxNumHits, true);
            // _searcher.search(q, collector);
            // logger.debug("[LUCENE] Looking up '{}':", normPhrase);
            _searcher.search(query, collector);

            // collect hits
            if (collector.getTotalHits() > 0)
                return true;

            return false;
        } catch (ParseException ioe) {
            throw new IOException(ioe);
        } catch (IOException ioe) {
            throw ioe;// new Exception(ioe);
        } finally {
            //collector = null;
        }
    }

    /**
     * Lookup a phrase in the dictionary.
     *
     * @param term Term to lookup
     * @param x    indicating NO LOWERCASE
     * @return true if found
     * @throws IOException
     */
    public boolean exactTermInDict(String term, int x) throws IOException {
        TopScoreDocCollector collector;

        String normTerm = term.trim();
        // logger.error("Exact Term inDict Lookup for: {}", normTerm);

        try {
            // create query
            TermQuery _lastQuery = new TermQuery(new Term(_lookupFieldName, normTerm));

            // QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, _lookupFieldName, _analyzer);
            // Query query = parser.parse(QueryParser.escape(normTerm));

            // create search
            collector = TopScoreDocCollector.create(_maxNumHits, true);

            // logger.debug("[LUCENE] Looking up '{}':", normTerm);
            // _searcher.search( query, collector);
            _searcher.search(_lastQuery, collector);

            if (collector.getTotalHits() > 0)
                return true;

            return false;
        } catch (IOException ioe) {
            throw ioe;//new Exception(ioe);
        } finally {
            //collector = null;
        }
    }

    /**
     * Lookup a phrase in the dictionary.
     *
     * @param phrase Term to lookup
     * @param x      Indicating NO LOWERCASE
     * @return true if found
     * @throws IOException
     */
    public boolean standardTermInDict(String phrase, int x) throws IOException {
        TopScoreDocCollector collector;

        String normPhrase = phrase.trim();
        // logger.error("Standard Term inDict Lookup for: {}", normPhrase);
        try {
            // create phrase query
            QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, _lookupFieldName, _analyzer);
            Query query = parser.parse(QueryParser.escape(normPhrase));
            /*
             * PhraseQuery q = new PhraseQuery(); _lastQuery = q; String[] tokens = phrase.trim().toLowerCase().split(" "); for (String token : tokens){ q.add(
             * new Term(_lookupFieldName, token) ); }
             */

            // create search
            collector = TopScoreDocCollector.create(_maxNumHits, true);
            // _searcher.search(q, collector);
            // logger.debug("[LUCENE] Looking up '{}':", normPhrase);
            _searcher.search(query, collector);

            // collect hits
            if (collector.getTotalHits() > 0)
                return true;

            return false;
        } catch (ParseException ioe) {
            throw new IOException(ioe);
        } catch (IOException ioe) {
            throw ioe;// new Exception(ioe);
        } finally {
            // collector = null;
        }
    }

    /**
     * Lookup a phrase in the dictionary using fuzzy query search. <br>
     * <br>
     * <p>
     * fuzzy search applies the next formula: lucene_sim = 1 - (edit_distance/shorter_length_of_two_terms) <br>
     * if the minimun_similarity specified as input parameter is lower than lucene_similarity, then a match is found
     *
     * @param term Term to lookup
     * @param sim  minimum_similarity
     * @return true if found
     * @throws IOException
     */
    public boolean exactFuzzyTermInDict(String term, float sim) throws IOException {
        TopScoreDocCollector collector;

        String normTerm = term.toLowerCase().trim();
        // logger.error("Exact Term inDict Lookup for: {}", normTerm);

        try {
            FuzzyQuery fq = new FuzzyQuery(new Term(_lookupFieldName, normTerm), (int) sim);
            // logger.error("{} Using FUZZY --> ", term);

            // create search
            collector = TopScoreDocCollector.create(_maxNumHits, true);

            _searcher.search(fq, collector);
            // logger.error(" hits {}\n{}", collector.getTotalHits(), _searcher.explain(fq, 1).toString());

            if (collector.getTotalHits() > 0)
                return true;

            return false;
        } catch (IOException ioe) {
            throw ioe;// new Exception(ioe);
        } finally {
            // collector = null;  //TODO WHY!!!  I think this causes issues when we stop the GUI
        }
    }

    /**
     * Lookup a phrase in the dictionary. <br>
     * <br>
     * <p>
     * fuzzy search applies the next formula: lucene_sim = 1 - (edit_distance/shorter_length_of_two_terms) <br>
     * if the minimun_similarity specified as input parameter is lower than lucene_similarity, then a match is found
     *
     * @param term Term to lookup
     * @param x    indicating NO LOWERCASE
     * @param sim  min_sim
     * @return true if found
     * @throws IOException
     */
    public boolean exactFuzzyTermInDict(String term, int x, float sim) throws IOException {
        TopScoreDocCollector collector;

        String normTerm = term.trim();
        // logger.error("Exact Term inDict Lookup for: {}", normTerm);

        try {
            FuzzyQuery fq = new FuzzyQuery(new Term(_lookupFieldName, normTerm), (int) sim);
            // logger.error("{} Using FUZZY --> ", term);

            // create search
            collector = TopScoreDocCollector.create(_maxNumHits, true);

            _searcher.search(fq, collector);
            // logger.error(" hits {}\n{}", collector.getTotalHits(), _searcher.explain(fq, 1).toString());

            if (collector.getTotalHits() > 0)
                return true;

            return false;
        } catch (IOException ioe) {
            throw ioe;//new Exception(ioe);
        } finally {
            //collector = null;
        }
    }

    /**
     * Lookup a phrase in the dictionary (specially for UMLS).
     *
     * @param phrase Term to lookup
     * @return List of hits
     * @throws IOException
     */
    public List<IndexHit> lookupPhrase(String phrase) throws IOException {
        TopScoreDocCollector collector;
        ArrayList<IndexHit> hitCollection = new ArrayList<IndexHit>();
        String normPhrase = phrase.toLowerCase().trim();
        try {
            // create phrase query
            QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, _lookupFieldName, _analyzer);
            Query query = parser.parse(QueryParser.escape(normPhrase));

            // create search
            collector = TopScoreDocCollector.create(_maxNumHits, true);
            // _searcher.search(q, collector);
            // logger.debug("[LUCENE] Looking up '{}':", normPhrase);
            _searcher.search(query, collector);

            // collect hits
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            int numTotalHits = collector.getTotalHits();
            // logger.debug("\tNumber of hits: {}", numTotalHits);

            int end = Math.min(numTotalHits, _maxNumHits);
            for (int i = 0; i < end; i++) {
                // if (hits[i].score > _minHitScore)
                ScoreDoc hit = hits[i];
                hitCollection.add(new IndexHit(hit, createUmlsConcept(hit)));
                // logger.debug("\thit: {}", hits[i].toString());
            }
            return hitCollection;
        } catch (ParseException ioe) {
            throw new IOException(ioe);
        } catch (IOException ioe) {
            throw ioe;//new Exception(ioe);
        } finally {
            // collector = null;
        }
    }

    /**
     * Creates a UMLS concept.
     *
     * @param hit
     * @return
     * @throws CorruptIndexException
     * @throws IOException
     */
    private UmlsConcept createUmlsConcept(ScoreDoc hit) throws CorruptIndexException, IOException {
        Document doc = _searcher.doc(hit.doc);

        String name = doc.get("STR");
        String concept = doc.get("PSTR");
        String code = doc.get("CUI");
        String semType = doc.get("TUI");
        String terminology = doc.get("SAB");
        String definition = doc.get("DEF");

        return new UmlsConcept(code, concept, name, semType, terminology, definition);
    }

    public void closeIndexReader() throws IOException {
        _indexReader.close();
    }
}
