
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

package com.clinacuity.deid.test;

import com.clinacuity.base.JcasTestBase;
import com.clinacuity.clinideid.message.DeidLevel;
import com.clinacuity.deid.outputAnnotators.CdaTextToXml;
import com.clinacuity.deid.outputAnnotators.resynthesis.ResynthesisAnnotator;
import com.clinacuity.deid.readers.CdaXmlToText;
import com.clinacuity.deid.readers.GeneralCollectionReader;
import com.clinacuity.deid.type.DocumentInformationAnnotation;
import com.clinacuity.deid.util.PiiOptions;
import com.clinacuity.deid.util.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestCdaTextToXml extends JcasTestBase {
    private static final String TEST_PATH = "src/test/resources/CdaTextToXml/";

    private static DocumentInformationAnnotation modifyOrCreateDocumentInformationAnnotation(JCas jCas, String name, String path, int size) {
        DocumentInformationAnnotation docInfo;
        try {
            docInfo = JCasUtil.selectSingle(jCas, DocumentInformationAnnotation.class);
        } catch (IllegalArgumentException e) {
            docInfo = new DocumentInformationAnnotation(jCas);
        }
        docInfo.setDocumentType("txt");//update elsewhere if needed
        docInfo.setFilePath(path);
        docInfo.setFileSize(size);
        docInfo.setFileName(name);
        docInfo.addToIndexes();
        return docInfo;
    }

    @Override
    protected void Init() {
        logger = LogManager.getLogger();
    }

    @Test
    public void testCdaExample1() {//test serialized output from Reader
        Assert.assertTrue(testXmi("CDAexample1"));
    }

    @Test
    public void testSampleCDADocument() {//test serialized output from Reader
        String fileName = ("SampleCDADocument.xmi");
        Assert.assertTrue(testXmi("SampleCDADocument"));
    }

    @Test
    public void testCCDAexample3BeachSoft() {//test serialized output from Reader
        Assert.assertTrue(testXmi("CCDAexample3BeachSoft"));
    }

    private void createXmi(String fileName) {
        File currentFile = new File(TEST_PATH + fileName + ".xml");
        int lastDot = currentFile.getName().lastIndexOf('.');
        String name = currentFile.getName().substring(0, lastDot);
        DocumentInformationAnnotation documentInformation =
                modifyOrCreateDocumentInformationAnnotation(jCas, name, currentFile.getAbsolutePath(), (int) currentFile.length());

        try {
            documentInformation.setOriginalXml(org.apache.uima.util.FileUtils.file2String(currentFile, "UTF-8"));
            jCas.setDocumentText(CdaXmlToText.process(jCas, currentFile));

            Serializer.SerializeToXmi(jCas, TEST_PATH + "newXmi/" + fileName + ".xmi", true);

        } catch (IOException | SAXException e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    private boolean testXmi(String fileName) {
        try {
            Serializer.DeserializeJcasFromFile(jCas, TEST_PATH + fileName + ".xmi");
            String resynthText = new String(Files.readAllBytes(Paths.get(TEST_PATH + fileName + ".deid.resynthesis.txt")));
            String result = CdaTextToXml.process(jCas, resynthText);
            String correct = new String(Files.readAllBytes(Paths.get(TEST_PATH + fileName + ".resynthesis.xml")));
            return result.equals(correct);
        } catch (SAXException | IOException | AnalysisEngineProcessException | NullPointerException e) {
            logger.throwing(e);
            return false;
        }
    }

    @Test
    public void testCombineSampleCdaDocument() {//test that CdaTextToXml and CdaXmlToText work together
        jCas.reset();
        File testFile = new File(TEST_PATH + "combineTest/SampleCDADocumentFromCombined.xml");
        int lastDot = testFile.getName().lastIndexOf('.');
        String name = testFile.getName().substring(0, lastDot);
        DocumentInformationAnnotation documentInformation = modifyOrCreateDocumentInformationAnnotation(jCas, name,
                TEST_PATH + "combineTest/SampleCDADocumentFromCombined.xml", 1);
        documentInformation.setLevel(DeidLevel.beyond.toString());
        GeneralCollectionReader.makePiiOptionMapAnnotation(jCas, new PiiOptions(DeidLevel.beyond));
        try {//CdaTextToXml tests that it used the exact number of lines provided by CdaXmlToText
            documentInformation.setOriginalXml(org.apache.uima.util.FileUtils.file2String(testFile, "UTF-8"));
            documentInformation.setResynthesisSelected(true);
            documentInformation.setOutputToCda(true);
            documentInformation.setResynthesisDirectory(TEST_PATH);
            documentInformation.setLevel(DeidLevel.defaultLevel.toString());
            AnalysisEngine resynth = AnalysisEngineFactory.createEngine(ResynthesisAnnotator.class,
                    ResynthesisAnnotator.OUTPUT_EXTENSION_PARAM, ".deid.resynthesis.xml",
                    ResynthesisAnnotator.SAVE_FOR_SERVICE, true,//must have output destination
                    ResynthesisAnnotator.OUTPUT_TO_DB, false, ResynthesisAnnotator.OUTPUT_TO_FILE, false);
            jCas.setDocumentText(CdaXmlToText.process(jCas, testFile));
            SimplePipeline.runPipeline(jCas, resynth);
            CdaTextToXml.process(jCas, jCas.getDocumentText());//verify that everything lines up correctly
        } catch (IOException | AnalysisEngineProcessException | ResourceInitializationException e) {
            logger.throwing(e);
            Assert.fail("Exception");
        }
    }
}
