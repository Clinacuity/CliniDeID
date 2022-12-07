
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

package com.clinacuity.deid.readers;

import com.clinacuity.deid.type.DocumentInformationAnnotation;
import com.clinacuity.deid.util.Util;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.IntegerArray;
import org.jdom2.Comment;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.input.DOMBuilder;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.clinacuity.deid.mains.DeidPipeline.PII_LOG;
import static java.util.Map.entry;

public class CdaXmlToText {
    //these are values in children of a name that aren't actually part of the person's name, public b/c CdaXmlAnnotator uses it as well
    public static final Set<String> NOT_NAME = Set.of("dr", "dr.", "md", "m.d.", "mr", "mr.", "mrs", "mrs.", "miss", "d.o.", "pharm.d.", "rn");
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern TABS = Pattern.compile("\\t+");
    private static final Map<String, String> TYPE_HINTS = makeTypeHints();
    private static final Set<String> PII_TAGS = Set.of("streetaddressline", "city", "state", "postalcode", "country");
    private static final Pattern PHONE_PART = Pattern.compile("(\\D*(?:\\+1)?)(\\d*(?<!\\()\\(?\\d+\\)?\\s*-?\\s*\\d+\\s*-?\\s*\\d+)(.*?)");//might not catch the extension if it has ext or x, but the main part of the number will be matched
    private static final Pattern VALUE_HAS_DATE = Pattern.compile("^((?:19|20)\\d{6})");
    private static final Pattern VALUE_IS_DATE_TIME = Pattern.compile("^((?:19|20)\\d{6})(\\d{3,4})$");
    private static final Pattern VALUE_IS_DATE_TIME_DASH_TIME = Pattern.compile("^((?:19|20)\\d{6})(\\d{3,4})-(\\d{3,4})$");
    private static final Pattern VALUE_IS_DATE_UNKNOWN_DASH_TIME = Pattern.compile("^((?:19|20)\\d{6})(\\d+-)(\\d{3,4})$");
    private static final DocumentBuilder DOCUMENT_BUILDER = makeBuilder();

    private CdaXmlToText() {
    }  //no need to instantiate class, all static

    private static DocumentBuilder makeBuilder() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant for external entity attacks
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant for external entity attacks
            return factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {//This isn't  possible b/c the above attributes are legal
            LOGGER.throwing(e);
        }
        return null;
    }

    private static Map<String, String> makeTypeHints() {
        //keys should be lowercased tag names, value is the Pii of that tag's value
        return Map.ofEntries(entry("legalauthenticator", "Provider"), entry("author", "Provider"), entry("encounterparticipant", "Provider"), entry("dataenterer", "Provider"),
                entry("assignedperson", "Provider"), entry("intendedRecipient", "Provider"), entry("informationrecipient", "Provider"),
                entry("participant", "Patient"), entry("patient", "Patient"), entry("patient_encounter", "Patient"), entry("patientpatient", "Patient"), entry("recordtarget", "Patient"),
                entry("providerorganization", "HealthCareUnitName"), entry("custodian", "HealthCareUnitName"), entry("healthcarefacility", "HealthCareUnitName"), entry("receivedorganization", "HealthCareUnitName"), entry("representedorganization", "HealthCareUnitName"),
                entry("streetaddressline", "Street"), entry("city", "City"),
                entry("state", "State"), entry("country", "Country"),
                entry("relatedperson", "Relative"),
                entry("postalcode", "Zip")
        );
    }

    public static int countNewLines(String value) {
        int newLineCount = 0;
        for (int index = 0; index < value.length(); ++index) {
            if (value.charAt(index) == '\n') {
                newLineCount++;
            }
        }
        return newLineCount;
    }

    //entrance point: JCas and either String or File with text to parse
    public static String process(JCas jCas, String inputText) throws IOException {
        try {
            org.w3c.dom.Document w3cDocument = DOCUMENT_BUILDER.parse(IOUtils.toInputStream(inputText));
            return internalProcess(jCas, w3cDocument);
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    public static String process(JCas jCas, File currentFile) throws IOException {
        try {
            org.w3c.dom.Document w3cDocument = DOCUMENT_BUILDER.parse(currentFile);
            return internalProcess(jCas, w3cDocument);
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    //parses xml and sets up values for offset information
    private static String internalProcess(JCas jCas, org.w3c.dom.Document w3cDocument) {
        List<Integer> numberOfLines = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        int idNumber = 1;
        Document document;
        document = new DOMBuilder().build(w3cDocument);
        Element classElement = document.getRootElement();
        processElement(jCas, text, numberOfLines, classElement, "", idNumber);

        DocumentInformationAnnotation documentInformation = JCasUtil.selectSingle(jCas, DocumentInformationAnnotation.class);
        documentInformation.setDocumentType("cda.xml");
        IntegerArray vals = new IntegerArray(jCas, numberOfLines.size());
        for (int index = 0; index < numberOfLines.size(); index++) {
            vals.set(index, numberOfLines.get(index));
        }
        documentInformation.setNumberOfLines(vals);
        LOGGER.debug("CdaXmlToText end");
        return text.toString();
    }

    //utility to both add to text and create Pii, numberOfLines is optionally updated in case this is part of something larger (i.e. effective time with date and Clock)
    private static void addTextAndPii(JCas jCas, StringBuilder text, List<Integer> numberOfLines, String toAdd, String piiSubType, boolean updateNumberOfLines, int idNumber) {
        text.append(toAdd).append("\n");
        int length = toAdd.length();
        if (updateNumberOfLines) {
            numberOfLines.add(1 + countNewLines((toAdd)));//what if toAdd has \n?
        }
        if (toAdd.length() == 0) {
            return;
        }
        int trimEnd = 0;
        while (" ".equals(text.substring(text.length() - 2 - trimEnd, text.length() - 2 - trimEnd + 1))) {//-2 so that it is on the first spot to be considered
            trimEnd++;
        }
        int trimBegin = 0;
        while (" ".equals(text.substring(text.length() - 1 - length + trimBegin, text.length() - length + trimBegin + 1))) {
            trimBegin++;
        }
        Util.addPii(jCas, text.length() - 1 - length + trimBegin, text.length() - 1 - trimEnd, Util.PII_SUB_TO_PARENT_TYPE.get(piiSubType), piiSubType, "P" + idNumber, "Structured", 1.0f);
    }

    private static void addTextAndPii(JCas jCas, StringBuilder text, List<Integer> numberOfLines, String toAdd, String piiSubType, int idNumber) {
        addTextAndPii(jCas, text, numberOfLines, toAdd, piiSubType, true, idNumber);
    }

    /* find elements that should have PII either as their value or an attribute's value
    add them to text with a newline, create PiiAnnotation for them
    also add any extraneous text with newlines around the actual PII to keep them ordered
    numberOfLines.get(i) is the # of lines from text to be later put back in the element, often just a 1
    parentPiiSubType is passed through the recursion and updated if a tag indicates which PiiSubtype, or left as is (in case an ancestor tag is the indicator)
    * */
    private static void processElement(JCas jCas, StringBuilder text, List<Integer> numberOfLines, Element classElement, String parentPiiSubType, int idNumber) {
        List<Element> list = classElement.getChildren();
        for (Element element : list) {
            //possibly exclude when attribute nullFlavor="UNK" or element.getValue() is empty, or add an empty line?
            //idNumber needs to be returned or count returned and +=
            if (element.getName().equalsIgnoreCase("component")) {
                processComponent(jCas, text, numberOfLines, element, idNumber);
            } else if (element.getAttribute("extension") != null) {
                addTextAndPii(jCas, text, numberOfLines, element.getAttribute("extension").getValue(), "OtherIDNumber", idNumber);
            } else if (element.getName().equalsIgnoreCase("title")) { // SampleCDA, top level title "HCU Consultation Note"????
                text.append(element.getValue()).append("\n");
                numberOfLines.add(1 + countNewLines(element.getValue()));
            } else if (element.getName().equalsIgnoreCase("effectiveTime")//these are actually all date
                    || element.getName().equalsIgnoreCase("birthTime")
                    || element.getName().equalsIgnoreCase("time")) {
                processTimeElement(jCas, text, numberOfLines, element, idNumber);
            } else if (PII_TAGS.contains(element.getName().toLowerCase())) {
                addTextAndPii(jCas, text, numberOfLines, element.getValue(), TYPE_HINTS.getOrDefault(element.getName().toLowerCase(), parentPiiSubType), idNumber);
            } else if (element.getName().equalsIgnoreCase("name")) {
                processName(jCas, text, element, numberOfLines, parentPiiSubType, idNumber);
            } else if (element.getName().equalsIgnoreCase("telecom")) {
                //value="tel:+15072842511"
                processPhone(jCas, text, numberOfLines, element.getAttributeValue("value"), idNumber);
            } else {
                processElement(jCas, text, numberOfLines, element, TYPE_HINTS.getOrDefault(element.getName().toLowerCase(), parentPiiSubType), idNumber);
            }
        }
    }

    private static void processName(JCas jCas, StringBuilder text, Element element, List<Integer> numberOfLines, String parentPiiSubType, int idNumber) {
        List<Element> nameChildren = element.getChildren();
        if (nameChildren.isEmpty()) {
            if (element.getValue() != null) {
                addTextAndPii(jCas, text, numberOfLines, element.getValue(), TYPE_HINTS.getOrDefault(element.getName().toLowerCase(), parentPiiSubType), idNumber);
            } else {
                LOGGER.log(PII_LOG, "{}", () -> "Possible ERROR Element name had no children and no value " + element.toString());
            }
        } else {//does this need getContent in case of values not in children?
            int length = 0;
            for (Element nameChild : nameChildren) {
                if (!NOT_NAME.contains(nameChild.getValue().toLowerCase())) {
                    text.append(nameChild.getValue()).append("\n");
                    length += nameChild.getValue().length() + 1;
//                    numberOfLines.add(countNewLines(nameChild.getValue()));
                    numberOfLines.add(1);
                }
            }
            Util.addPii(jCas, text.length() - length, text.length() - 1, Util.PII_SUB_TO_PARENT_TYPE.get(parentPiiSubType), parentPiiSubType, "P" + idNumber++, "Structured", 1.0f);
        }
    }

    private static void processTimeElement(JCas jCas, StringBuilder text, List<Integer> numberOfLines, Element element, int idNumber) {
        if (element.getAttribute("value") != null) {
            processTimeValue(jCas, text, numberOfLines, element.getAttribute("value").getValue(), idNumber);
            //  logger.debug("added {}, numberOfLines size {}, val {}", element.getAttribute("value").getValue(), numberOfLines.size(), numberOfLines.get(numberOfLines.size()-1));
        } else if (!element.getChildren().isEmpty()) {//for children of high, low, do nothing with period
            List<Element> timeChildren = element.getChildren();
            for (Element timeElement : timeChildren) {
                if ((timeElement.getName().equalsIgnoreCase("high") || timeElement.getName().equalsIgnoreCase("low")) && timeElement.getAttributeValue("value") != null) {
                    processTimeValue(jCas, text, numberOfLines, timeElement.getAttributeValue("value"), idNumber);
                }
            }
        } else if (element.getValue() != null && element.getValue().length() > 0) {
            processTimeValue(jCas, text, numberOfLines, element.getValue(), idNumber);
        }
    }

    private static void processTimeValue(JCas jCas, StringBuilder text, List<Integer> numberOfLines, String value, int idNumber) {
        Matcher mat = VALUE_IS_DATE_TIME_DASH_TIME.matcher(value);
        if (mat.matches()) {
            addTextAndPii(jCas, text, numberOfLines, mat.group(1), "Date", false, idNumber);
            addTextAndPii(jCas, text, numberOfLines, mat.group(2), "ClockTime", false, idNumber);
            text.append("-").append("\n");
            addTextAndPii(jCas, text, numberOfLines, mat.group(3), "ClockTime", false, idNumber);
            numberOfLines.add(4);
        } else {
            mat = VALUE_IS_DATE_TIME.matcher(value);
            if (mat.matches()) {
                addTextAndPii(jCas, text, numberOfLines, mat.group(1), "Date", false, idNumber);
                addTextAndPii(jCas, text, numberOfLines, mat.group(2), "ClockTime", false, idNumber);
                numberOfLines.add(2);
            } else {
                mat = VALUE_IS_DATE_UNKNOWN_DASH_TIME.matcher(value);
                if (mat.matches()) {
                    addTextAndPii(jCas, text, numberOfLines, mat.group(1), "Date", false, idNumber);
                    text.append(mat.group(2)).append("\n");
                    addTextAndPii(jCas, text, numberOfLines, mat.group(3), "ClockTime", false, idNumber);
                    numberOfLines.add(3);
                } else {
                    mat = VALUE_HAS_DATE.matcher(value);
                    if (mat.matches()) {
                        addTextAndPii(jCas, text, numberOfLines, mat.group(1), "Date", idNumber);
                    } else if (mat.reset().find()) {
                        addTextAndPii(jCas, text, numberOfLines, mat.group(1), "Date", false, idNumber);
                        text.append(value.substring(mat.group(1).length())).append("\n");
                        numberOfLines.add(2);
                    } else {//who knows what it is, maybe treat it like OtherIdNumber?
                        addTextAndPii(jCas, text, numberOfLines, value, "Date", idNumber);
                    }
                }
            }
        }
    }

    private static void processComponent(JCas jCas, StringBuilder text, List<Integer> numberOfLines, Element classElement, int idNumber) {
        List<Element> list = classElement.getChildren();
        for (Element element : list) {
            if (element.getName().equalsIgnoreCase("text")) {
                processText(text, numberOfLines, element);
            } else if (element.getName().equalsIgnoreCase("content")) {
                text.append(element.getValue()).append("\n");
                numberOfLines.add(1 + countNewLines(element.getValue()));
            } else if (element.getName().equalsIgnoreCase("title")) {
                text.append(element.getValue()).append("\n");
                numberOfLines.add(1 + countNewLines(element.getValue()));
            } else if (element.getName().equalsIgnoreCase("templateId") && element.getAttribute("extension") != null) {
                addTextAndPii(jCas, text, numberOfLines, element.getAttributeValue("extension"), "Date", idNumber);
            } else if (element.getName().equalsIgnoreCase("effectiveTime")
                    || element.getName().equalsIgnoreCase("birthTime")
                    || element.getName().equalsIgnoreCase("time")) {
                processTimeElement(jCas, text, numberOfLines, element, idNumber);
            } else if (element.getName().equalsIgnoreCase("telecom")) {
                processPhone(jCas, text, numberOfLines, element.getAttributeValue("value"), idNumber);
            } else if (element.getName().equalsIgnoreCase("name")) {
                processName(jCas, text, element, numberOfLines, "Patient", idNumber);
            } else {
                processComponent(jCas, text, numberOfLines, element, idNumber);
            }
        }
    }

    private static void processPhone(JCas jCas, StringBuilder text, List<Integer> numberOfLines, String value, int idNumber) {
        if (value != null) {
            Matcher mat = PHONE_PART.matcher(value);
            if (mat.find()) {
                text.append(mat.group(1)).append("\n");
                addTextAndPii(jCas, text, numberOfLines, mat.group(2), "PhoneFax", false, idNumber);
                text.append(mat.group(3)).append("\n");
                numberOfLines.add(3);
            } else {
                addTextAndPii(jCas, text, numberOfLines, value, "PhoneFax", idNumber);
            }
        }
    }

    private static void processText(StringBuilder text, List<Integer> numberOfLines, Element element) {
        if (element.getAttribute("mediaType") != null) {//leave it alone
            return;
        }
        List<Content> contents = element.getContent();
        for (Content part : contents) {
            if (part instanceof Text) {//use .replace("\t"," "); ??
                Matcher mat = TABS.matcher(part.getValue() + "\n");
                String result = mat.replaceAll(" ");
                text.append(result);
                numberOfLines.add(countNewLines(result));
            } else if (part instanceof Element) {
                processText(text, numberOfLines, (Element) part);
            } else if (!(part instanceof Comment)) {// do nothing with Comment
                LOGGER.error("Content neither Element nor text, Ctype: {}, Value: {}", part.getCType(), part.getValue());
            }
        }
    }
}
