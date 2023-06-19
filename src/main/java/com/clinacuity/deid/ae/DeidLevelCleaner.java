
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

import com.clinacuity.clinideid.message.DeidLevel;
import com.clinacuity.deid.outputAnnotators.resynthesis.ResynthAge;
import com.clinacuity.deid.outputAnnotators.resynthesis.Resynthesizer;
import com.clinacuity.deid.type.DocumentInformationAnnotation;
import com.clinacuity.deid.type.PiiAnnotation;
import com.clinacuity.deid.type.PiiOptionMapAnnotation;
import com.clinacuity.deid.util.PiiOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeidLevelCleaner extends JCasAnnotator_ImplBase {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern FOUR_DIGIT_YEAR = Pattern.compile("\\s*(19|20|21)\\d{2}\\s*");
    private static final Pattern YEAR_MONTH_DAY_NUMERIC = Pattern.compile("(\\s*(?:19|20|21)\\d{2}[-/ ])\\d\\d?[-/ ]\\d\\d?");
    private static final Pattern MONTH_DAY_YEAR_NUMERIC_2_OR_4_DIGIT = Pattern.compile("^(\\s*\\d\\d?[-/ ]\\d\\d?)[-/ ]((?:(19|20|21)\\d{2})|\\d\\d)");//2 digit must be year since > 31
    private static final Pattern MONTH_DAY_YEAR_WORD = Pattern.compile("(\\s*[a-z]+\\.?\\s+\\d+)\\s*(?:,|\\s)\\s*\\d+");//Matched September 2071 as (september) (207) (1)
    private static final Pattern MONTH_YEAR = Pattern.compile("^\\s*(\\w+)\\.?\\s*[-/]?\\s*('\\d\\d|\\d{4})"); //Sept. 2014, 5/2014, May '94
    private DeidLevel deidLevel;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
        LOGGER.debug("DeidLevelCleaner initialized");
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        LOGGER.debug("DeidLevelCleaner begin");
        deidLevel = DeidLevel.valueOf(JCasUtil.selectSingle(jCas, DocumentInformationAnnotation.class).getLevel());
        ArrayList<Annotation> annots2remove = new ArrayList<>();
        for (Annotation cand : jCas.getAnnotationIndex()) {//remove everything but DocumentInformationAnnotation and final (confidence 1.0) PiiAnnotations
            if (cand.getTypeIndexID() != DocumentInformationAnnotation.type//TODO: is it more performant to save these 2 annotations when getting level/options and put it back vs having this check?
                    && cand.getTypeIndexID() != PiiOptionMapAnnotation.type
                    && (!(cand.getTypeIndexID() == PiiAnnotation.type && ((PiiAnnotation) cand).getConfidence() >= 1.0))) {
                annots2remove.add(cand);
            }
        }
        annots2remove.forEach(Annotation::removeFromIndexes);
        if (deidLevel.equals(DeidLevel.beyond)) {//beyond leaves everything so nothing left to do
            return;
        }
        if (deidLevel.equals(DeidLevel.custom)) {
            processCustom(jCas);
            return;
        }

        ArrayList<PiiAnnotation> toRemove = new ArrayList<>();
        /*Three levels to be considered: Beyond, HIPAA Strict Safe Harbor, and HIPAA Limited.
          Beyond annotates everything so done. Otherwise, only remove if right type and that checker says to remove.
          Details in each ...ToBeRemoved method*/
        //Map of PiiSubtype to class implementing an interface to execute or lambda thingy instead of if/else?
        //Don't bother to refactor until DEID-323 that expands options
        for (PiiAnnotation annot : jCas.getAnnotationIndex(PiiAnnotation.class)) {
            if ((annot.getPiiSubtype().equals("Age")) && ageToBeRemoved(annot)) {
                toRemove.add(annot);
            } else if (annot.getPiiSubtype().equals("Date") && dateTimeToBeRemoved(annot)) {
                toRemove.add(annot);
            } else if (annot.getPiiSubtype().equals("Street") && deidLevel.equals(DeidLevel.limited)) {
                toRemove.add(annot);
            } else if (annot.getPiiSubtype().equals("City")) {
                toRemove.add(annot);
            } else if (annot.getPiiSubtype().startsWith("Zip") && deidLevel.equals(DeidLevel.limited)) {//3 digit funny thing done by resynth
                toRemove.add(annot);
            } else if (annot.getPiiSubtype().equals("Provider")) {//only used at beyond level
                toRemove.add(annot);
            } else if (annot.getPiiSubtype().equals("State")) {//only used at beyond level
                toRemove.add(annot);
            } else if (annot.getPiiSubtype().equals("Country")) {//only used at beyond level
                toRemove.add(annot);
            } else if (annot.getPiiSubtype().equals("Season")) {//only used at beyond level
                toRemove.add(annot);
            } else if (annot.getPiiSubtype().equals("DayOfWeek")) {//only used at beyond level
                toRemove.add(annot);
            } else if (annot.getPiiSubtype().equals("Profession")) {  //only used at beyond level
                toRemove.add(annot);
            } else if (annot.getPiiSubtype().equals("ClockTime") && deidLevel.equals(DeidLevel.limited)) {//removed at limited only
                toRemove.add(annot);
            } else if (annot.getPiiSubtype().equals("Relative")) {//only at beyond
                toRemove.add(annot);
            } else if (annot.getPiiSubtype().equals("Season") && deidLevel.equals(DeidLevel.limited)) {//removed at limited only
                toRemove.add(annot);
            }
        }
    }
    //Every time identifier shorter than a year should be included in the Safe Harbor,
    // and all time identifiers left intact in a limited dataset.
    // Relatives should be removed in both.

    private void processCustom(JCas jCas) {
        ArrayList<PiiAnnotation> toRemove = new ArrayList<>();
        PiiOptionMapAnnotation piiOptionMapAnnotation = JCasUtil.selectSingle(jCas, PiiOptionMapAnnotation.class);
        for (PiiAnnotation annot : jCas.getAnnotationIndex(PiiAnnotation.class)) {
            if (!piiOptionMapAnnotation.getIncludeValue(annot.getPiiSubtype())) {
                toRemove.add(annot);
            } else if (PiiOptions.SPECIALS.contains(annot.getPiiSubtype())) {
                if (piiOptionMapAnnotation.getSpecialValue(annot.getPiiSubtype()) == PiiOptions.NONE) { //this shouldn't happen as include would be false
                    toRemove.add(annot);
                } else if (piiOptionMapAnnotation.getSpecialValue(annot.getPiiSubtype()) != PiiOptions.ALL &&
                        isSpecialToBeRemoved(annot, piiOptionMapAnnotation.getSpecialValue(annot.getPiiSubtype()))) {
                    toRemove.add(annot);
                }
            }
        }
        toRemove.forEach(PiiAnnotation::removeFromIndexes);

    }

    private boolean ageGreaterThan89(PiiAnnotation annot) {
        String text = annot.getCoveredText().toLowerCase();
        int age = 0;
        if (text.endsWith("'s")) {//chop off 's for something like age in 80's
            text = text.substring(0, text.length() - 2);
        } else if (text.endsWith("s")) {//chop off 's for something like age in 80s
            text = text.substring(0, text.length() - 1);
        }
        try {
            age = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            age = ResynthAge.convertWordsToInt(ResynthAge.DASH_AND_SPACES.matcher(Resynthesizer.trimToLetterDigit(text)).replaceAll(" "));
        }
        return age >= 90;
    }

    private boolean ageToBeRemoved(PiiAnnotation annot) {
        if (deidLevel.equals(DeidLevel.strict)) {//removes ages <=89
            return !ageGreaterThan89(annot);
        } else if (deidLevel.equals(DeidLevel.limited)) {//removes all ages, could just be else but left in case future levels values for deidLevel
            return true;
        }
        return false;
    }

    private boolean removeYear(PiiAnnotation annot) {//if annot is only year then return true to remove it, else shrink span to remove year
        String text = annot.getCoveredText();
        Matcher mat = MONTH_DAY_YEAR_WORD.matcher(text);
        if (mat.find()) {//e.g. May 5, 2017, move end of annotation over to just after 5 leaving comma and year out
            int end = mat.end(1);
            annot.setEnd(annot.getBegin() + end);
            return false;
        }
        if (FOUR_DIGIT_YEAR.matcher(text).matches()) {//it is just the year, remove it
            return true;
        }
        mat = MONTH_DAY_YEAR_NUMERIC_2_OR_4_DIGIT.matcher(text);
        if (mat.find()) {//e.g. 5/15/2013, change annotation end to just before /2013
            int end = mat.end(1);
            annot.setEnd(annot.getBegin() + end);
            return false;
        }
        mat = YEAR_MONTH_DAY_NUMERIC.matcher(text);
        if (mat.find()) {//e.g. 2163-11-02, set annotation begin to just after 2163
            int end = mat.end(1);
            annot.setBegin(annot.getBegin() + end);
            return false;
        }
        mat = MONTH_YEAR.matcher(text);
        if (mat.find()) {
            int end = mat.end(1);
            annot.setEnd(annot.getBegin() + end);
            return false;
        }
        return false;
    }

    private boolean dateTimeToBeRemoved(PiiAnnotation annot) {//  Strict does all except year, Limited does none
        if (deidLevel.equals(DeidLevel.strict)) {
            return removeYear(annot);
        } else if (deidLevel.equals(DeidLevel.limited)) {
            return true;
        }
        return false;
    }

    private boolean isSpecialToBeRemoved(PiiAnnotation annot, int special) {
        if (annot.getPiiSubtype().equals("Date")) {
            if (special == PiiOptions.DATE_MONTH_DAY_ONLY) {
                return removeYear(annot);
            }
        } else if (annot.getPiiSubtype().equals("Age")) {
            if (special == PiiOptions.AGE_GT89_ONLY) {//if not really needed as it is only other option than NONE and ALL
                return !ageGreaterThan89(annot);
            }
        } else if (annot.getPiiSubtype().equals("Zip")) {//leave for resynth for 3 digit issues
            return false;
        }
        return false;
    }
}
