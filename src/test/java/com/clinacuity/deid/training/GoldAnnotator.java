
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

import com.clinacuity.deid.type.PiiAnnotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.cleartk.util.ViewUriUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

// This annotator adds CRFAnnotations from a file with the same input name but -ne.xml extension
// That file is created elsewhere (perl script standOff2ClearTkTrain) from gold standard xml files that include standoff annotations
// This annotator is only used for training CRF model for NER
public class GoldAnnotator extends JCasAnnotator_ImplBase {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        logger.debug("GoldAnnotator begin");
        File textFile = new File(ViewUriUtil.getURI(jCas));
        String prefix = textFile.getPath().replaceAll("[.]\\w+$", "");// chop off .xmi or .txt
        // String fileName = Utilities.getFileName(jCas);
        // String prefix = fileName.replaceAll("[.]\\w+$", "");// chop off .xmi or .txt
        List<String> nerEntries;
        try {
            nerEntries = Files.readAllLines(Paths.get(prefix + "-ne.xml"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.throwing(e);
            throw new AnalysisEngineProcessException(e);
        }
        for (String line : nerEntries) {
            String[] parts = line.split(" ");
            PiiAnnotation piiAnnotation = new PiiAnnotation(jCas, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            //CONTACT DATE AGE ID use parts[2], the higher level category
            //LOCATION and NAME use the more specific parts[3]
            if (parts[2].equals("NAME") || parts[2].equals("PERSON") || parts[2].equals("LOCATION")) {// use more specific value
                piiAnnotation.setPiiSubtype(parts[3]);
            } else {
                piiAnnotation.setPiiSubtype(parts[2]);
            }
            piiAnnotation.addToIndexes();
        }
    }
}
