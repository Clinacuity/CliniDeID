
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

package com.clinacuity.deid.outputAnnotators;

import com.clinacuity.deid.readers.CdaXmlToText;
import com.clinacuity.deid.type.DocumentInformationAnnotation;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.IntegerArray;
import org.jdom2.Comment;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Set;

//this does the work of taking the replacement text (resynthesized or tagged) and putting it back into the original CDA XML document
//entry point is process with parameters of the JCas (for the offset information and original xml in SourceDocumentAnnotation)
// and replacementText with the new text to write,
// returns string with text to be written as xml file or stored in db or ...
public class CdaTextToXml {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Set<String> PII_TAGS = Set.of("streetaddressline", "city", "state", "postalcode", "country");
    private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();

    private CdaTextToXml() {
    }

    public static String process(JCas jCas, String replacementText) throws AnalysisEngineProcessException {
        DocumentInformationAnnotation docInfo = JCasUtil.selectSingle(jCas, DocumentInformationAnnotation.class);
        IntegerArray numberOfLines = docInfo.getNumberOfLines();
        ArrayList<String> replacementLines = new ArrayList<>();
        replacementText.lines().forEach(replacementLines::add);
        PrimitiveIterator.OfInt numberOfLinesIterator = numberOfLines.iterator();
        Iterator<String> replacementIterator = replacementLines.iterator();
        String result;
        Document document;
        //factory.setNamespaceAware(true);//If want to make namespace aware.
        try {
            DocumentBuilder documentBuilder = FACTORY.newDocumentBuilder();
            org.w3c.dom.Document w3cDocument = documentBuilder.parse(IOUtils.toInputStream(docInfo.getOriginalXml()));//where can original text be, DB result set already advanced to next record
            document = new DOMBuilder().build(w3cDocument);
            Element classElement = document.getRootElement();
            processElement(classElement, replacementIterator, numberOfLinesIterator);
            XMLOutputter xmlOutput = new XMLOutputter();
            Format format = Format.getPrettyFormat();
            format.setEncoding("UTF-8");
            xmlOutput.setFormat(format);
            if (replacementIterator.hasNext()) {
                LOGGER.error("Mismatch for replacementLines size {} and numberOfLines size: {}", replacementLines.size(), numberOfLines.size());
                throw new AnalysisEngineProcessException("Mismatch for replacementLines", null);
            }
            if (numberOfLinesIterator.hasNext()) {
                LOGGER.error("Mismatch for numberOfLines:  size {}", numberOfLines.size());
                throw new AnalysisEngineProcessException("Mismatch for numberOfLines", null);
            }
            result = xmlOutput.outputString(document);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new AnalysisEngineProcessException(e);
        }
        return result;
    }

    private static void processElement(Element classElement, Iterator<String> resynthLines, PrimitiveIterator.OfInt numberOfLines) throws AnalysisEngineProcessException {
        List<Element> list = classElement.getChildren();
        for (Element element : list) {
            if (element.getName().equalsIgnoreCase("component")) {
                processComponent(element, resynthLines, numberOfLines);
            } else if (element.getAttribute("extension") != null) {
                element.setAttribute("extension", getNextResynthLines(resynthLines, numberOfLines));
            } else if (element.getName().equalsIgnoreCase("title")) {
                element.setText(getNextResynthLines(resynthLines, numberOfLines));
            } else if (element.getName().equalsIgnoreCase("effectiveTime")
                    || element.getName().equalsIgnoreCase("birthTime")
                    || element.getName().equalsIgnoreCase("time")) {
                processTime(resynthLines, numberOfLines, element);
            } else if (PII_TAGS.contains(element.getName().toLowerCase())) {
                element.setText(getNextResynthLines(resynthLines, numberOfLines));
            } else if (element.getName().equalsIgnoreCase("name")) {
                processName(element, resynthLines, numberOfLines);
            } else if (element.getName().equalsIgnoreCase("telecom")) {
                if (element.getAttribute("value") != null) {
                    element.setAttribute("value", getNextResynthLines(resynthLines, numberOfLines).replace("\n", ""));
                }
            } else {
                processElement(element, resynthLines, numberOfLines);
            }
        }
    }

    private static void processName(Element element, Iterator<String> resynthLines, PrimitiveIterator.OfInt numberOfLines) throws AnalysisEngineProcessException {
        List<Element> nameChildren = element.getChildren();
        if (nameChildren.isEmpty()) {
            if (element.getValue() != null) {
                element.setText(getNextResynthLines(resynthLines, numberOfLines));
            }
        } else {//given, family, suffix, prefix, and duplicates of those
            for (Element nameChild : nameChildren) {
                if (!CdaXmlToText.NOT_NAME.contains(nameChild.getValue().toLowerCase())) {
                    nameChild.setText(getNextResynthLines(resynthLines, numberOfLines));
                }
            }
        }
    }

    private static String getNextResynthLines(Iterator<String> resynthLines, PrimitiveIterator.OfInt numberOfLines) throws AnalysisEngineProcessException {
        try {
            int count = numberOfLines.nextInt();
            StringBuilder result = new StringBuilder(2000);
            result.append(resynthLines.next());
            while (count > 1) {
                result.append("\n").append(resynthLines.next());
                count--;
            }
            return result.toString();
        } catch (NoSuchElementException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private static void processTime(Iterator<String> resynthLines, PrimitiveIterator.OfInt numberOfLines, Element element) throws AnalysisEngineProcessException {
        if (element.getAttribute("value") != null) {//NameSpace??
            element.setAttribute("value", getNextResynthLines(resynthLines, numberOfLines).replace("\n", ""));
        } else if (!element.getChildren().isEmpty()) {//for children of high, low, do nothing with period
            List<Element> timeChildren = element.getChildren();
            for (Element timeElement : timeChildren) {
                if ((timeElement.getName().equalsIgnoreCase("high") || timeElement.getName().equalsIgnoreCase("low")) && timeElement.getAttributeValue("value") != null) {
                    timeElement.setAttribute("value", getNextResynthLines(resynthLines, numberOfLines));
                }
            }
        } else if (element.getValue() != null && element.getValue().length() > 0) {
            element.setText(getNextResynthLines(resynthLines, numberOfLines));
        }
    }

    private static void processComponent(Element classElement, Iterator<String> resynthLines, PrimitiveIterator.OfInt numberOfLines) throws AnalysisEngineProcessException {
        List<Element> list = classElement.getChildren();
        for (Element element : list) {
            if (element.getName().equalsIgnoreCase("text")) {
                processText(element, resynthLines, numberOfLines);
            } else if (element.getName().equalsIgnoreCase("content")) {
                element.setText(getNextResynthLines(resynthLines, numberOfLines));
            } else if (element.getName().equalsIgnoreCase("title")) {
                element.setText(getNextResynthLines(resynthLines, numberOfLines));
            } else if (element.getName().equalsIgnoreCase("templateId") && element.getAttribute("extension") != null) {
                classElement.setAttribute("extension", getNextResynthLines(resynthLines, numberOfLines));
            } else if (element.getName().equalsIgnoreCase("effectiveTime")
                    || element.getName().equalsIgnoreCase("birthTime")
                    || element.getName().equalsIgnoreCase("time")) {
                processTime(resynthLines, numberOfLines, element);
            } else if (element.getName().equalsIgnoreCase("telecom")) {
                if (element.getAttribute("value") != null) {
                    element.setAttribute("value", getNextResynthLines(resynthLines, numberOfLines).replace("\n", ""));
                }
            } else if (element.getName().equalsIgnoreCase("name")) {
                processName(element, resynthLines, numberOfLines);
            } else {
                processComponent(element, resynthLines, numberOfLines);
            }
        }
    }

    private static void processText(Element element, Iterator<String> resynthLines, PrimitiveIterator.OfInt numberOfLines) throws AnalysisEngineProcessException {
        if (element.getAttribute("mediaType") != null) {//leave it alone
            return;
        }
        List<Content> contents = element.getContent();
        for (Content part : contents) {
            if (part instanceof Text) {
                ((Text) part).setText(getNextResynthLines(resynthLines, numberOfLines));
            } else if (part instanceof Element) {
                processText((Element) part, resynthLines, numberOfLines);
            } else if (!(part instanceof Comment)) {// do nothing with Comment
                LOGGER.error("Content neither Element nor Text nor Comment, Ctype: {}, Value: {}", part.getCType(), part.getValue());
            }
        }
    }
}
