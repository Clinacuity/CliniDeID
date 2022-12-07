
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



   
/* Apache UIMA v3 - First created by JCasGen Thu Jun 28 08:50:22 EDT 2018 */

package com.clinacuity.deid.type;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;

import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;


import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Wed Oct 03 12:18:33 EDT 2018
 * XML source: /Users/garyunderwood/tempDeidForEdits/deid/java-app/src/main/resources/desc/ensemble/TypeSystem.xml
 * @generated */
public class BaseToken extends Annotation {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final String _TypeName = "com.clinacuity.deid.type.BaseToken";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final int typeIndexID = JCasRegistry.register(BaseToken.class);
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
   
  public static final String _FeatName_partOfSpeech = "partOfSpeech";
  public static final String _FeatName_normalizedForm = "normalizedForm";
  public static final String _FeatName_ner = "ner";
  public static final String _FeatName_chunk = "chunk";
  public static final String _FeatName_tag = "tag";


  /* Feature Adjusted Offsets */
  private static final CallSite _FC_partOfSpeech = TypeSystemImpl.createCallSite(BaseToken.class, "partOfSpeech");
  private static final MethodHandle _FH_partOfSpeech = _FC_partOfSpeech.dynamicInvoker();
  private static final CallSite _FC_normalizedForm = TypeSystemImpl.createCallSite(BaseToken.class, "normalizedForm");
  private static final MethodHandle _FH_normalizedForm = _FC_normalizedForm.dynamicInvoker();
  private static final CallSite _FC_ner = TypeSystemImpl.createCallSite(BaseToken.class, "ner");
  private static final MethodHandle _FH_ner = _FC_ner.dynamicInvoker();
  private static final CallSite _FC_chunk = TypeSystemImpl.createCallSite(BaseToken.class, "chunk");
  private static final MethodHandle _FH_chunk = _FC_chunk.dynamicInvoker();
  private static final CallSite _FC_tag = TypeSystemImpl.createCallSite(BaseToken.class, "tag");
  private static final MethodHandle _FH_tag = _FC_tag.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  protected BaseToken() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public BaseToken(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public BaseToken(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public BaseToken(JCas jcas, int begin, int end) {
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
  //* Feature: partOfSpeech

  /** getter for partOfSpeech - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPartOfSpeech() { return _getStringValueNc(wrapGetIntCatchException(_FH_partOfSpeech));}
    
  /** setter for partOfSpeech - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPartOfSpeech(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_partOfSpeech), v);
  }    
    
   
    
  //*--------------*
  //* Feature: normalizedForm

  /** getter for normalizedForm - gets 
   * @generated
   * @return value of the feature 
   */
  public String getNormalizedForm() { return _getStringValueNc(wrapGetIntCatchException(_FH_normalizedForm));}
    
  /** setter for normalizedForm - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setNormalizedForm(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_normalizedForm), v);
  }    
    
   
    
  //*--------------*
  //* Feature: ner

  /** getter for ner - gets 
   * @generated
   * @return value of the feature 
   */
  public String getNer() { return _getStringValueNc(wrapGetIntCatchException(_FH_ner));}
    
  /** setter for ner - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setNer(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_ner), v);
  }    
    
   
    
  //*--------------*
  //* Feature: chunk

  /** getter for chunk - gets 
   * @generated
   * @return value of the feature 
   */
  public String getChunk() { return _getStringValueNc(wrapGetIntCatchException(_FH_chunk));}
    
  /** setter for chunk - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setChunk(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_chunk), v);
  }    
    
   
    
  //*--------------*
  //* Feature: tag

  /** getter for tag - gets 
   * @generated
   * @return value of the feature 
   */
  public String getTag() { return _getStringValueNc(wrapGetIntCatchException(_FH_tag));}
    
  /** setter for tag - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTag(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_tag), v);
  }    
    
  }

    