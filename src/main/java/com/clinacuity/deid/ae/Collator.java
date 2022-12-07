
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

import com.clinacuity.deid.type.PiiAnnotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.ArrayList;

/**
 * This class explores all system annotations producing <i> collations </i> between annotations of the same type.
 *
 * @author Oscar Ferrandez-Escamez
 * @version December 2011
 */
public class Collator extends JCasAnnotator_ImplBase {
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * Annotations to remove from the CAS.
     */
    private ArrayList<Annotation> annots2remove;
    /**
     * Annotations to add to the CAS (new collations).
     */
    private ArrayList<Annotation> annotations;

    /**
     * Initialization before processing the CAS (load parameters from the configuration file). Performs any startup tasks required by this annotator.
     */
    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
        annots2remove = new ArrayList<>();
        annotations = new ArrayList<>();
        LOGGER.debug("CollatorAnnotator initialized");
    }

    private void processAnnotations(JCas jCas) {
        //switching the order of these two loops results in +11 FN and -22FP (I2B2 2014 Big, 3/14/2018)
        //for smaller test set +0 FN and -6 FP, so it is picky
        //Why collate Dictionary?, if we don't exact: TP -314 FP +243 (Doctor and Patient), fully TP -20 FP -51,
        //assume it is b/c features with filters?
        for (PiiAnnotation candidate : jCas.getAnnotationIndex(PiiAnnotation.class)) {
            //processAnnotation(candidate);
            // annots2remove.add(candidate);
        }
    }

    /**
     * Invokes this annotator's analysis logic. This annotator looks for <i>collations</i> between annotations sharing the same Pii type. The longest
     * annotations will be considered.
     *
     * @param jCas the JCas to process
     * @throws AnalysisEngineProcessException if a failure occurs during processing.
     */

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        // for the collator to work, we have first to unify the CRF annotation to the types collator is going to collate, i.e. the "pii.types"
        // then, 1) find the CRF annots and 2) create new pii.type annots
        // optional -> remove the CRF annots
        LOGGER.debug("Collator begin");
        annotations.clear();

        // TODO fix this workaround of ArrayIndexOutOfBoundsException occurring sometimes
        try {
            processAnnotations(jCas);
        } catch (ArrayIndexOutOfBoundsException e) {
            LOGGER.debug(e);
            processAnnotations(jCas);
        } finally {
            annots2remove.forEach(ann -> ann.removeFromIndexes());
            annotations.forEach(ann -> ann.addToIndexes());
        }
    }

    /**
     * Looks for collations according to the input annotation and previous processed annotations.
     *
     * @param candidate {@link Annotation} to process
     */
    private void processAnnotation(Annotation candidate) {
        // collate annotations according to the input annot
        boolean overlap = false; // it will show if new annot doesn't overlap, so addition
        Annotation older;
        //TODO: speed improvements possible with sorting annotations to prevent having to compare to all previous
        for (int i = 0; i < annotations.size(); i++) {
            older = annotations.get(i);
            // see the offsets and the types
            //do nothing if not overlap, or different but not both names, or both dictonary but not both name
            if (candidate.getBegin() > older.getEnd() || candidate.getEnd() < older.getBegin()
                    || candidate.getTypeIndexID() != older.getTypeIndexID())
                ;

            else {
                overlap = true;
                // exact overlap or fully-contained
                if (candidate.getBegin() >= older.getBegin() && candidate.getEnd() <= older.getEnd()) {

                } else if (candidate.getBegin() < older.getBegin() && ( /* candidate.getEnd() == older.getBegin() || */ // redundant
                        candidate.getEnd() <= older.getEnd())) {
                    // back overlap and inside (no surpass the boundaries) annotation new start

                } else if (candidate.getBegin() <= older.getBegin() && candidate.getEnd() >= older.getEnd()) {
                    // back overlap and outside (surpass the boundaries)

                } else if (candidate.getBegin() > older.getBegin() && candidate.getBegin() <= older.getEnd()
                        && candidate.getEnd() >= older.getEnd()) {
                    // inside overlap or continuous(=) and outside (surpass or not(=) the boundaries)

                } else {
                    LOGGER.debug("Collator: reached else, unknown overlap type");
                }
            }
        }

        if (!overlap) {
            annotations.add(candidate);
        }
    }


}
