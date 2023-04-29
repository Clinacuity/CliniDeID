
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
public class Chunk extends Annotation {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final String _TypeName = "com.clinacuity.deid.type.Chunk";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final int typeIndexID = JCasRegistry.register(Chunk.class);
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
   
  public static final String _FeatName_chunkType = "chunkType";


  /* Feature Adjusted Offsets */
  private static final CallSite _FC_chunkType = TypeSystemImpl.createCallSite(Chunk.class, "chunkType");
  private static final MethodHandle _FH_chunkType = _FC_chunkType.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  protected Chunk() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public Chunk(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Chunk(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Chunk(JCas jcas, int begin, int end) {
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
  //* Feature: chunkType

  /** getter for chunkType - gets 
   * @generated
   * @return value of the feature 
   */
  public String getChunkType() { return _getStringValueNc(wrapGetIntCatchException(_FH_chunkType));}
    
  /** setter for chunkType - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setChunkType(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_chunkType), v);
  }    
    
  }

    