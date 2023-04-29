
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
public class DocumentId extends Annotation {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final String _TypeName = "com.clinacuity.deid.type.DocumentId";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final int typeIndexID = JCasRegistry.register(DocumentId.class);
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
   
  public static final String _FeatName_documentId = "documentId";


  /* Feature Adjusted Offsets */
  private static final CallSite _FC_documentId = TypeSystemImpl.createCallSite(DocumentId.class, "documentId");
  private static final MethodHandle _FH_documentId = _FC_documentId.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  protected DocumentId() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public DocumentId(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public DocumentId(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public DocumentId(JCas jcas, int begin, int end) {
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
  //* Feature: documentId

  /** getter for documentId - gets 
   * @generated
   * @return value of the feature 
   */
  public String getDocumentId() { return _getStringValueNc(wrapGetIntCatchException(_FH_documentId));}
    
  /** setter for documentId - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDocumentId(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_documentId), v);
  }    
    
  }

    