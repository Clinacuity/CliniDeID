
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



   
/* Apache UIMA v3 - First created by JCasGen Fri Mar 27 10:30:29 EDT 2020 */

package com.clinacuity.deid.type;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;

import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;


import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.cas.IntegerArray;


/** 
 * Updated by JCasGen Fri Mar 27 10:30:29 EDT 2020
 * XML source: src/main/resources/desc/DocumentInformationAnnotation.xml
 * @generated */
public class DocumentInformationAnnotation extends Annotation {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final String _TypeName = "com.clinacuity.deid.type.DocumentInformationAnnotation";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final int typeIndexID = JCasRegistry.register(DocumentInformationAnnotation.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
 
  /* *******************
   *   Feature Offsets *
   * *******************/ 
   
  public static final String _FeatName_documentType = "documentType";
  public static final String _FeatName_fileName = "fileName";
  public static final String _FeatName_filePath = "filePath";
  public static final String _FeatName_runId = "runId";
  public static final String _FeatName_fileSize = "fileSize";
  public static final String _FeatName_numberOfLines = "numberOfLines";
  public static final String _FeatName_originalXml = "originalXml";
  public static final String _FeatName_level = "level";
  public static final String _FeatName_resynthesisSelected = "resynthesisSelected";
  public static final String _FeatName_piiTaggingSelected = "piiTaggingSelected";
  public static final String _FeatName_piiTaggingCategorySelected = "piiTaggingCategorySelected";
  public static final String _FeatName_piiWriterSelected = "piiWriterSelected";
  public static final String _FeatName_rawXmiOutputSelected = "rawXmiOutputSelected";
  public static final String _FeatName_cleanXmiOutputSelected = "cleanXmiOutputSelected";
  public static final String _FeatName_outputToCda = "outputToCda";
  public static final String _FeatName_resynthesisDirectory = "resynthesisDirectory";
  public static final String _FeatName_piiTaggingCategoryDirectory = "piiTaggingCategoryDirectory";
  public static final String _FeatName_piiTaggingGeneralDirectory = "piiTaggingGeneralDirectory";
  public static final String _FeatName_cleanXmiDirectory = "cleanXmiDirectory";
  public static final String _FeatName_rawXmiDirectory = "rawXmiDirectory";
  public static final String _FeatName_piiWriterDirectory = "piiWriterDirectory";
  public static final String _FeatName_outputMapToFile = "outputMapToFile";
  public static final String _FeatName_outputMapDirectory = "outputMapDirectory";


  /* Feature Adjusted Offsets */
  private static final CallSite _FC_documentType = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "documentType");
  private static final MethodHandle _FH_documentType = _FC_documentType.dynamicInvoker();
  private static final CallSite _FC_fileName = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "fileName");
  private static final MethodHandle _FH_fileName = _FC_fileName.dynamicInvoker();
  private static final CallSite _FC_filePath = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "filePath");
  private static final MethodHandle _FH_filePath = _FC_filePath.dynamicInvoker();
  private static final CallSite _FC_runId = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "runId");
  private static final MethodHandle _FH_runId = _FC_runId.dynamicInvoker();
  private static final CallSite _FC_fileSize = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "fileSize");
  private static final MethodHandle _FH_fileSize = _FC_fileSize.dynamicInvoker();
  private static final CallSite _FC_numberOfLines = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "numberOfLines");
  private static final MethodHandle _FH_numberOfLines = _FC_numberOfLines.dynamicInvoker();
  private static final CallSite _FC_originalXml = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "originalXml");
  private static final MethodHandle _FH_originalXml = _FC_originalXml.dynamicInvoker();
  private static final CallSite _FC_level = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "level");
  private static final MethodHandle _FH_level = _FC_level.dynamicInvoker();
  private static final CallSite _FC_resynthesisSelected = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "resynthesisSelected");
  private static final MethodHandle _FH_resynthesisSelected = _FC_resynthesisSelected.dynamicInvoker();
  private static final CallSite _FC_piiTaggingSelected = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "piiTaggingSelected");
  private static final MethodHandle _FH_piiTaggingSelected = _FC_piiTaggingSelected.dynamicInvoker();
  private static final CallSite _FC_piiTaggingCategorySelected = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "piiTaggingCategorySelected");
  private static final MethodHandle _FH_piiTaggingCategorySelected = _FC_piiTaggingCategorySelected.dynamicInvoker();
  private static final CallSite _FC_piiWriterSelected = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "piiWriterSelected");
  private static final MethodHandle _FH_piiWriterSelected = _FC_piiWriterSelected.dynamicInvoker();
  private static final CallSite _FC_rawXmiOutputSelected = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "rawXmiOutputSelected");
  private static final MethodHandle _FH_rawXmiOutputSelected = _FC_rawXmiOutputSelected.dynamicInvoker();
  private static final CallSite _FC_cleanXmiOutputSelected = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "cleanXmiOutputSelected");
  private static final MethodHandle _FH_cleanXmiOutputSelected = _FC_cleanXmiOutputSelected.dynamicInvoker();
  private static final CallSite _FC_outputToCda = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "outputToCda");
  private static final MethodHandle _FH_outputToCda = _FC_outputToCda.dynamicInvoker();
  private static final CallSite _FC_resynthesisDirectory = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "resynthesisDirectory");
  private static final MethodHandle _FH_resynthesisDirectory = _FC_resynthesisDirectory.dynamicInvoker();
  private static final CallSite _FC_piiTaggingCategoryDirectory = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "piiTaggingCategoryDirectory");
  private static final MethodHandle _FH_piiTaggingCategoryDirectory = _FC_piiTaggingCategoryDirectory.dynamicInvoker();
  private static final CallSite _FC_piiTaggingGeneralDirectory = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "piiTaggingGeneralDirectory");
  private static final MethodHandle _FH_piiTaggingGeneralDirectory = _FC_piiTaggingGeneralDirectory.dynamicInvoker();
  private static final CallSite _FC_cleanXmiDirectory = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "cleanXmiDirectory");
  private static final MethodHandle _FH_cleanXmiDirectory = _FC_cleanXmiDirectory.dynamicInvoker();
  private static final CallSite _FC_rawXmiDirectory = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "rawXmiDirectory");
  private static final MethodHandle _FH_rawXmiDirectory = _FC_rawXmiDirectory.dynamicInvoker();
  private static final CallSite _FC_piiWriterDirectory = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "piiWriterDirectory");
  private static final MethodHandle _FH_piiWriterDirectory = _FC_piiWriterDirectory.dynamicInvoker();
  private static final CallSite _FC_outputMapToFile = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "outputMapToFile");
  private static final MethodHandle _FH_outputMapToFile = _FC_outputMapToFile.dynamicInvoker();
  private static final CallSite _FC_outputMapDirectory = TypeSystemImpl.createCallSite(DocumentInformationAnnotation.class, "outputMapDirectory");
  private static final MethodHandle _FH_outputMapDirectory = _FC_outputMapDirectory.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  protected DocumentInformationAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public DocumentInformationAnnotation(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public DocumentInformationAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public DocumentInformationAnnotation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: documentType

  /** getter for documentType - gets 
   * @generated
   * @return value of the feature 
   */
  public String getDocumentType() { return _getStringValueNc(wrapGetIntCatchException(_FH_documentType));}
    
  /** setter for documentType - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDocumentType(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_documentType), v);
  }    
    
   
    
  //*--------------*
  //* Feature: fileName

  /** getter for fileName - gets 
   * @generated
   * @return value of the feature 
   */
  public String getFileName() { return _getStringValueNc(wrapGetIntCatchException(_FH_fileName));}
    
  /** setter for fileName - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFileName(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_fileName), v);
  }    
    
   
    
  //*--------------*
  //* Feature: filePath

  /** getter for filePath - gets 
   * @generated
   * @return value of the feature 
   */
  public String getFilePath() { return _getStringValueNc(wrapGetIntCatchException(_FH_filePath));}
    
  /** setter for filePath - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFilePath(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_filePath), v);
  }    
    
   
    
  //*--------------*
  //* Feature: runId

  /** getter for runId - gets 
   * @generated
   * @return value of the feature 
   */
  public long getRunId() { return _getLongValueNc(wrapGetIntCatchException(_FH_runId));}
    
  /** setter for runId - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRunId(long v) {
    _setLongValueNfc(wrapGetIntCatchException(_FH_runId), v);
  }    
    
   
    
  //*--------------*
  //* Feature: fileSize

  /** getter for fileSize - gets 
   * @generated
   * @return value of the feature 
   */
  public int getFileSize() { return _getIntValueNc(wrapGetIntCatchException(_FH_fileSize));}
    
  /** setter for fileSize - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFileSize(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_fileSize), v);
  }    
    
   
    
  //*--------------*
  //* Feature: numberOfLines

  /** getter for numberOfLines - gets 
   * @generated
   * @return value of the feature 
   */
  public IntegerArray getNumberOfLines() { return (IntegerArray)(_getFeatureValueNc(wrapGetIntCatchException(_FH_numberOfLines)));}
    
  /** setter for numberOfLines - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setNumberOfLines(IntegerArray v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_numberOfLines), v);
  }    
    
    
  /** indexed getter for numberOfLines - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public int getNumberOfLines(int i) {
     return ((IntegerArray)(_getFeatureValueNc(wrapGetIntCatchException(_FH_numberOfLines)))).get(i);} 

  /** indexed setter for numberOfLines - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setNumberOfLines(int i, int v) {
    ((IntegerArray)(_getFeatureValueNc(wrapGetIntCatchException(_FH_numberOfLines)))).set(i, v);
  }  
   
    
  //*--------------*
  //* Feature: originalXml

  /** getter for originalXml - gets 
   * @generated
   * @return value of the feature 
   */
  public String getOriginalXml() { return _getStringValueNc(wrapGetIntCatchException(_FH_originalXml));}
    
  /** setter for originalXml - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setOriginalXml(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_originalXml), v);
  }    
    
   
    
  //*--------------*
  //* Feature: level

  /** getter for level - gets 
   * @generated
   * @return value of the feature 
   */
  public String getLevel() { return _getStringValueNc(wrapGetIntCatchException(_FH_level));}
    
  /** setter for level - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLevel(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_level), v);
  }    
    
   
    
  //*--------------*
  //* Feature: resynthesisSelected

  /** getter for resynthesisSelected - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getResynthesisSelected() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_resynthesisSelected));}
    
  /** setter for resynthesisSelected - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setResynthesisSelected(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_resynthesisSelected), v);
  }    
    
   
    
  //*--------------*
  //* Feature: piiTaggingSelected

  /** getter for piiTaggingSelected - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getPiiTaggingSelected() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_piiTaggingSelected));}
    
  /** setter for piiTaggingSelected - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPiiTaggingSelected(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_piiTaggingSelected), v);
  }    
    
   
    
  //*--------------*
  //* Feature: piiTaggingCategorySelected

  /** getter for piiTaggingCategorySelected - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getPiiTaggingCategorySelected() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_piiTaggingCategorySelected));}
    
  /** setter for piiTaggingCategorySelected - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPiiTaggingCategorySelected(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_piiTaggingCategorySelected), v);
  }    
    
   
    
  //*--------------*
  //* Feature: piiWriterSelected

  /** getter for piiWriterSelected - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getPiiWriterSelected() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_piiWriterSelected));}
    
  /** setter for piiWriterSelected - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPiiWriterSelected(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_piiWriterSelected), v);
  }    
    
   
    
  //*--------------*
  //* Feature: rawXmiOutputSelected

  /** getter for rawXmiOutputSelected - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getRawXmiOutputSelected() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_rawXmiOutputSelected));}
    
  /** setter for rawXmiOutputSelected - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRawXmiOutputSelected(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_rawXmiOutputSelected), v);
  }    
    
   
    
  //*--------------*
  //* Feature: cleanXmiOutputSelected

  /** getter for cleanXmiOutputSelected - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getCleanXmiOutputSelected() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_cleanXmiOutputSelected));}
    
  /** setter for cleanXmiOutputSelected - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setCleanXmiOutputSelected(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_cleanXmiOutputSelected), v);
  }    
    
   
    
  //*--------------*
  //* Feature: outputToCda

  /** getter for outputToCda - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getOutputToCda() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_outputToCda));}
    
  /** setter for outputToCda - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setOutputToCda(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_outputToCda), v);
  }    
    
   
    
  //*--------------*
  //* Feature: resynthesisDirectory

  /** getter for resynthesisDirectory - gets 
   * @generated
   * @return value of the feature 
   */
  public String getResynthesisDirectory() { return _getStringValueNc(wrapGetIntCatchException(_FH_resynthesisDirectory));}
    
  /** setter for resynthesisDirectory - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setResynthesisDirectory(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_resynthesisDirectory), v);
  }    
    
   
    
  //*--------------*
  //* Feature: piiTaggingCategoryDirectory

  /** getter for piiTaggingCategoryDirectory - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPiiTaggingCategoryDirectory() { return _getStringValueNc(wrapGetIntCatchException(_FH_piiTaggingCategoryDirectory));}
    
  /** setter for piiTaggingCategoryDirectory - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPiiTaggingCategoryDirectory(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_piiTaggingCategoryDirectory), v);
  }    
    
   
    
  //*--------------*
  //* Feature: piiTaggingGeneralDirectory

  /** getter for piiTaggingGeneralDirectory - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPiiTaggingGeneralDirectory() { return _getStringValueNc(wrapGetIntCatchException(_FH_piiTaggingGeneralDirectory));}
    
  /** setter for piiTaggingGeneralDirectory - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPiiTaggingGeneralDirectory(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_piiTaggingGeneralDirectory), v);
  }    
    
   
    
  //*--------------*
  //* Feature: cleanXmiDirectory

  /** getter for cleanXmiDirectory - gets 
   * @generated
   * @return value of the feature 
   */
  public String getCleanXmiDirectory() { return _getStringValueNc(wrapGetIntCatchException(_FH_cleanXmiDirectory));}
    
  /** setter for cleanXmiDirectory - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setCleanXmiDirectory(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_cleanXmiDirectory), v);
  }    
    
   
    
  //*--------------*
  //* Feature: rawXmiDirectory

  /** getter for rawXmiDirectory - gets 
   * @generated
   * @return value of the feature 
   */
  public String getRawXmiDirectory() { return _getStringValueNc(wrapGetIntCatchException(_FH_rawXmiDirectory));}
    
  /** setter for rawXmiDirectory - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRawXmiDirectory(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_rawXmiDirectory), v);
  }    
    
   
    
  //*--------------*
  //* Feature: piiWriterDirectory

  /** getter for piiWriterDirectory - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPiiWriterDirectory() { return _getStringValueNc(wrapGetIntCatchException(_FH_piiWriterDirectory));}
    
  /** setter for piiWriterDirectory - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPiiWriterDirectory(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_piiWriterDirectory), v);
  }    
    
   
    
  //*--------------*
  //* Feature: outputMapToFile

  /** getter for outputMapToFile - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getOutputMapToFile() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_outputMapToFile));}
    
  /** setter for outputMapToFile - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setOutputMapToFile(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_outputMapToFile), v);
  }    
    
   
    
  //*--------------*
  //* Feature: outputMapDirectory

  /** getter for outputMapDirectory - gets 
   * @generated
   * @return value of the feature 
   */
  public String getOutputMapDirectory() { return _getStringValueNc(wrapGetIntCatchException(_FH_outputMapDirectory));}
    
  /** setter for outputMapDirectory - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setOutputMapDirectory(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_outputMapDirectory), v);
  }    
    
  }

    