
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
import com.clinacuity.deid.util.SpanSetComp;
import com.clinacuity.deid.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class VoteAnnotator extends JCasAnnotator_ImplBase {
    public static final String THRESH_HOLD = "th";
    private static final Logger LOGGER = LogManager.getLogger();
    //on 10/11/2018 tested all 6 orderings of MIRA, SVM and oldCRF with 0 differences
    //on 10/15/2018 tested all 4 possible positions for new RNN, putting it at 2 had the best recall and best precision
    private static final Map<String, Integer> TIERS = Map.of("Structured", 0, "Regex", 1, "RNN", 2, "CRF", 2, "MIRA", 2, "SVM", 2, "oldCRF", 2, "RegexLow", 3);
    private static final Map<String, Integer> ORDERS = Map.of("Structured", 0, "Regex", 1, "RNN", 2, "CRF", 3, "MIRA", 4, "SVM", 5, "oldCRF", 6, "RegexLow", 7);
    @ConfigurationParameter(name = THRESH_HOLD, description = "threshold for voting approval", defaultValue = "1")
    private int threshold;

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);
        LOGGER.debug("VoteAnnotator Initialized");
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        FSIterator<PiiAnnotation> piis = jCas.getAnnotationIndex(PiiAnnotation.class).iterator();

        Map<String, Integer> pTier = new HashMap<>();
        Map<String, Integer> pOrder = new HashMap<>();
        Map<String, Integer> pCnt = new HashMap<>();
        Map<String, String> pMethod = new HashMap<>();

        while (piis.hasNext()) {
            PiiAnnotation pii = piis.next();
            String key = pii.getBegin() + " " + pii.getEnd() + " " + pii.getPiiSubtype() + " " + pii.getPiiType();//TODO: any reason for parent type?
            String method = pii.getMethod();

            if ("Regex".equals(method) && pii.getConfidence() < .6f) {
                method = "RegexLow";
            }

            int tier = TIERS.get(method);
            int order = ORDERS.get(method);

            //this doesn't expand if 2 methods overlap, either one or the other's span is chosen according to tier and order
            if (!pTier.containsKey(key) || pTier.get(key) > tier) {
                pTier.put(key, tier);
            }
            if (!pOrder.containsKey(key) || pOrder.get(key) > order) {
                pOrder.put(key, order);
            }
            if (!pCnt.containsKey(key)) {
                pCnt.put(key, 1);
            } else {
                pCnt.put(key, pCnt.get(key) + 1);
            }
            if (!pMethod.containsKey(key)) {
                pMethod.put(key, method);
            } else {
                String val = pMethod.get(key) + "," + method;
                pMethod.put(key, val);
            }
        }

        HashSet<String> cands = new HashSet<>();
        for (Map.Entry<String, Integer> item : pCnt.entrySet()) {
            if (item.getValue() >= threshold) {
                cands.add(item.getKey());
            }
        }

        HashSet<String> outs = new HashSet<>();
        vote(cands, pTier, pOrder, pCnt, outs);

        int idx = 0;
        for (String key : outs) {
            String keyParts[] = key.split(" ", 4);
            String method = pMethod.get(key);
            if (!"Structured".equals(method)) {//Structured already in with confidence of 100%, no need to add again
                Util.addPii(jCas, Integer.parseInt(keyParts[0]), Integer.parseInt(keyParts[1]), keyParts[3], keyParts[2], "P" + idx, method, 1.0f);
                idx++;
            }
        }

    }

    public void vote(Set<String> cands, Map<String, Integer> pTier, Map<String, Integer> pOrder,
                     Map<String, Integer> pCnt, Set<String> outs) {

        HashSet<String> tmps = new HashSet<>(cands);
        HashSet<String> done = new HashSet<>();

        TreeSet<String> nCands = new TreeSet<>(new SpanSetComp());
        //       nCands.addAll(cands);//TODO: how can nCands.size be < cands.size?
        for (String key : cands) {
            nCands.add(key);
        }

        for (String key : nCands) {
            if (done.contains(key)) {
                continue;
            }
            String keyA[] = key.split(" ");

            int s = Integer.parseInt(keyA[0]);
            int e = Integer.parseInt(keyA[1]);

            HashSet<Integer> rs = new HashSet<>();
            for (int i = s; i < e; i++) {
                rs.add(i);
            }

            int tier = pTier.get(key);
            int order = pOrder.get(key);
            int cnt = pCnt.get(key);

            // collect overlap concepts
            HashSet<String> overs = new HashSet<>();
            for (String k : tmps) {
                if (key.equals(k)) {
                    continue;
                }
                if (done.contains(k)) {
                    continue;
                }

                String keyA2[] = k.split(" ");

                int s2 = Integer.parseInt(keyA2[0]);
                int e2 = Integer.parseInt(keyA2[1]);

                boolean ifOverlap = false;
                for (int i = s2; i < e2; i++) {
                    if (rs.contains(i)) {
                        ifOverlap = true;
                        break;
                    }
                }
                if (ifOverlap) {
                    overs.add(k);
                }
            } // for tmp

            if (overs.isEmpty()) {
                // no overlap concept
                outs.add(key);
                done.add(key);
            } else {
                if (tier == 1) {
                    outs.add(key);
                    done.add(key);

                    done.addAll(overs);
//                    for (String k: overs) {
//                        done.add(k);
//                    }
                } else {
                    // collect overlap with same vote
                    TreeSet<String> oSet = new TreeSet<>();
                    boolean ifMost = true;
                    int bestTier = tier;//Gary added bestTier/key to manage Regex SSN being trumped when multiple other AEs had OtherIDNumber
                    String bestKey = "";

                    for (String k : overs) {
                        int cnt1 = pCnt.get(k);
                        if (pTier.get(k) < tier) {
                            bestTier = pTier.get(k);
                            bestKey = k;
                        }
                        if (cnt < cnt1) {
                            ifMost = false;
                        } else if (cnt == cnt1) {
                            oSet.add(k);
                        }
                    }
                    if (bestTier < tier) {
                        outs.add(bestKey);
                        for (String k : overs) {
                            if (k.equals(bestKey)) {
                                continue;
                            }
                            done.add(k);
                        }
                        done.add(key);
                    }
//I think the issue is when Regex is in overlap, the original has 2 or more votes.
                    else if (ifMost) {
                        // if no concept with same vote
                        if (oSet.isEmpty()) {
                            outs.add(key);
                            done.add(key);

                            for (String k : overs) {
                                done.add(k);
                            }
                        } else {
                            // if there are concepts with same vote
                            // by order
                            boolean ifLower = true;
                            for (String k : oSet) {
                                int order1 = pOrder.get(k);

                                if (order > order1) {
                                    ifLower = false;
                                }
                            }

                            if (ifLower) {
                                outs.add(key);
                                done.add(key);

                                for (String k : overs) {
                                    done.add(k);
                                }
                            }// TODO: should there be an else? what if only 1 and not lower?

                        }
                        // if most
                    } else {
                        done.add(key);
                    }// if not most
                }// if tier is not 1
            }// if overs is not empty
        }// for maps
    }

}
