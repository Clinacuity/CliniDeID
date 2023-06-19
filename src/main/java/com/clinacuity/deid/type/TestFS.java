
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



   
/* Apache UIMA v3 - First created by JCasGen Sat Apr 11 12:57:35 EDT 2020 */

package com.clinacuity.deid.type;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;

import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;


import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.cas.BooleanArray;


/** 
 * Updated by JCasGen Sat Apr 11 12:57:35 EDT 2020
 * XML source: src/main/resources/desc/TestFS.xml
 * @generated */
public class TestFS extends Annotation {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final String _TypeName = "com.clinacuity.deid.type.TestFS";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final int typeIndexID = JCasRegistry.register(TestFS.class);
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
   
  public static final String _FeatName_item = "item";


  /* Feature Adjusted Offsets */
  private static final CallSite _FC_item = TypeSystemImpl.createCallSite(TestFS.class, "item");
  private static final MethodHandle _FH_item = _FC_item.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  protected TestFS() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public TestFS(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public TestFS(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public TestFS(JCas jcas, int begin, int end) {
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
  //* Feature: item

  /** getter for item - gets 
   * @generated
   * @return value of the feature 
   */
  public BooleanArray getItem() { return (BooleanArray)(_getFeatureValueNc(wrapGetIntCatchException(_FH_item)));}
    
  /** setter for item - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setItem(BooleanArray v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_item), v);
  }    
    
    
  /** indexed getter for item - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public boolean getItem(int i) {
     return ((BooleanArray)(_getFeatureValueNc(wrapGetIntCatchException(_FH_item)))).get(i);} 

  /** indexed setter for item - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setItem(int i, boolean v) {
    ((BooleanArray)(_getFeatureValueNc(wrapGetIntCatchException(_FH_item)))).set(i, v);
  }  
  }

    