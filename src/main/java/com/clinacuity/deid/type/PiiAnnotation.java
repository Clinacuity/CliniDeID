
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
public class PiiAnnotation extends Annotation {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final String _TypeName = "com.clinacuity.deid.type.PiiAnnotation";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final int typeIndexID = JCasRegistry.register(PiiAnnotation.class);
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
   
  public static final String _FeatName_id = "id";
  public static final String _FeatName_piiType = "piiType";
  public static final String _FeatName_piiSubtype = "piiSubtype";
  public static final String _FeatName_method = "method";
  public static final String _FeatName_confidence = "confidence";


  /* Feature Adjusted Offsets */
  private static final CallSite _FC_id = TypeSystemImpl.createCallSite(PiiAnnotation.class, "id");
  private static final MethodHandle _FH_id = _FC_id.dynamicInvoker();
  private static final CallSite _FC_piiType = TypeSystemImpl.createCallSite(PiiAnnotation.class, "piiType");
  private static final MethodHandle _FH_piiType = _FC_piiType.dynamicInvoker();
  private static final CallSite _FC_piiSubtype = TypeSystemImpl.createCallSite(PiiAnnotation.class, "piiSubtype");
  private static final MethodHandle _FH_piiSubtype = _FC_piiSubtype.dynamicInvoker();
  private static final CallSite _FC_method = TypeSystemImpl.createCallSite(PiiAnnotation.class, "method");
  private static final MethodHandle _FH_method = _FC_method.dynamicInvoker();
  private static final CallSite _FC_confidence = TypeSystemImpl.createCallSite(PiiAnnotation.class, "confidence");
  private static final MethodHandle _FH_confidence = _FC_confidence.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  protected PiiAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public PiiAnnotation(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public PiiAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public PiiAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: id

  /** getter for id - gets 
   * @generated
   * @return value of the feature 
   */
  public String getId() { return _getStringValueNc(wrapGetIntCatchException(_FH_id));}
    
  /** setter for id - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setId(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_id), v);
  }    
    
   
    
  //*--------------*
  //* Feature: piiType

  /** getter for piiType - gets
   * @generated
   * @return value of the feature 
   */
  public String getPiiType() { return _getStringValueNc(wrapGetIntCatchException(_FH_piiType));}
    
  /** setter for piiType - sets
   * @generated
   * @param v value to set into the feature 
   */
  public void setPiiType(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_piiType), v);
  }    
    
   
    
  //*--------------*
  //* Feature: piiSubtype

  /** getter for piiSubtype - gets
   * @generated
   * @return value of the feature 
   */
  public String getPiiSubtype() { return _getStringValueNc(wrapGetIntCatchException(_FH_piiSubtype));}
    
  /** setter for piiSubtype - sets
   * @generated
   * @param v value to set into the feature 
   */
  public void setPiiSubtype(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_piiSubtype), v);
  }    
    
   
    
  //*--------------*
  //* Feature: method

  /** getter for method - gets 
   * @generated
   * @return value of the feature 
   */
  public String getMethod() { return _getStringValueNc(wrapGetIntCatchException(_FH_method));}
    
  /** setter for method - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMethod(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_method), v);
  }    
    
   
    
  //*--------------*
  //* Feature: confidence

  /** getter for confidence - gets 
   * @generated
   * @return value of the feature 
   */
  public float getConfidence() { return _getFloatValueNc(wrapGetIntCatchException(_FH_confidence));}
    
  /** setter for confidence - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setConfidence(float v) {
    _setFloatValueNfc(wrapGetIntCatchException(_FH_confidence), v);
  }    
    
  }

    