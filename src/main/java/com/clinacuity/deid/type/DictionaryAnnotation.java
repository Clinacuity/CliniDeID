
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



   
/* Apache UIMA v3 - First created by JCasGen Tue Feb 14 15:15:14 EST 2017 */

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
public class DictionaryAnnotation extends Annotation {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final String _TypeName = "com.clinacuity.deid.type.DictionaryAnnotation";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final int typeIndexID = JCasRegistry.register(DictionaryAnnotation.class);
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
   
  public static final String _FeatName_mit_names_ambig = "mit_names_ambig";
  public static final String _FeatName_mit_names_unambig = "mit_names_unambig";
  public static final String _FeatName_mit_names_popular = "mit_names_popular";
  public static final String _FeatName_mit_lastnames_ambig = "mit_lastnames_ambig";
  public static final String _FeatName_mit_lastnames_unambig = "mit_lastnames_unambig";
  public static final String _FeatName_mit_lastnames_popular = "mit_lastnames_popular";
  public static final String _FeatName_mit_common_words = "mit_common_words";
  public static final String _FeatName_mit_locations_unambig = "mit_locations_unambig";
  public static final String _FeatName_mit_countries = "mit_countries";
  public static final String _FeatName_wiki_gate_mit_companies = "wiki_gate_mit_companies";
  public static final String _FeatName_mit_commonest_words = "mit_commonest_words";
  public static final String _FeatName_US_cities = "US_cities";
  public static final String _FeatName_US_counties = "US_counties";
  public static final String _FeatName_nationalities = "nationalities";


  /* Feature Adjusted Offsets */
  private static final CallSite _FC_mit_names_ambig = TypeSystemImpl.createCallSite(DictionaryAnnotation.class, "mit_names_ambig");
  private static final MethodHandle _FH_mit_names_ambig = _FC_mit_names_ambig.dynamicInvoker();
  private static final CallSite _FC_mit_names_unambig = TypeSystemImpl.createCallSite(DictionaryAnnotation.class, "mit_names_unambig");
  private static final MethodHandle _FH_mit_names_unambig = _FC_mit_names_unambig.dynamicInvoker();
  private static final CallSite _FC_mit_names_popular = TypeSystemImpl.createCallSite(DictionaryAnnotation.class, "mit_names_popular");
  private static final MethodHandle _FH_mit_names_popular = _FC_mit_names_popular.dynamicInvoker();
  private static final CallSite _FC_mit_lastnames_ambig = TypeSystemImpl.createCallSite(DictionaryAnnotation.class, "mit_lastnames_ambig");
  private static final MethodHandle _FH_mit_lastnames_ambig = _FC_mit_lastnames_ambig.dynamicInvoker();
  private static final CallSite _FC_mit_lastnames_unambig = TypeSystemImpl.createCallSite(DictionaryAnnotation.class, "mit_lastnames_unambig");
  private static final MethodHandle _FH_mit_lastnames_unambig = _FC_mit_lastnames_unambig.dynamicInvoker();
  private static final CallSite _FC_mit_lastnames_popular = TypeSystemImpl.createCallSite(DictionaryAnnotation.class, "mit_lastnames_popular");
  private static final MethodHandle _FH_mit_lastnames_popular = _FC_mit_lastnames_popular.dynamicInvoker();
  private static final CallSite _FC_mit_common_words = TypeSystemImpl.createCallSite(DictionaryAnnotation.class, "mit_common_words");
  private static final MethodHandle _FH_mit_common_words = _FC_mit_common_words.dynamicInvoker();
  private static final CallSite _FC_mit_locations_unambig = TypeSystemImpl.createCallSite(DictionaryAnnotation.class, "mit_locations_unambig");
  private static final MethodHandle _FH_mit_locations_unambig = _FC_mit_locations_unambig.dynamicInvoker();
  private static final CallSite _FC_mit_countries = TypeSystemImpl.createCallSite(DictionaryAnnotation.class, "mit_countries");
  private static final MethodHandle _FH_mit_countries = _FC_mit_countries.dynamicInvoker();
  private static final CallSite _FC_wiki_gate_mit_companies = TypeSystemImpl.createCallSite(DictionaryAnnotation.class, "wiki_gate_mit_companies");
  private static final MethodHandle _FH_wiki_gate_mit_companies = _FC_wiki_gate_mit_companies.dynamicInvoker();
  private static final CallSite _FC_mit_commonest_words = TypeSystemImpl.createCallSite(DictionaryAnnotation.class, "mit_commonest_words");
  private static final MethodHandle _FH_mit_commonest_words = _FC_mit_commonest_words.dynamicInvoker();
  private static final CallSite _FC_US_cities = TypeSystemImpl.createCallSite(DictionaryAnnotation.class, "US_cities");
  private static final MethodHandle _FH_US_cities = _FC_US_cities.dynamicInvoker();
  private static final CallSite _FC_US_counties = TypeSystemImpl.createCallSite(DictionaryAnnotation.class, "US_counties");
  private static final MethodHandle _FH_US_counties = _FC_US_counties.dynamicInvoker();
  private static final CallSite _FC_nationalities = TypeSystemImpl.createCallSite(DictionaryAnnotation.class, "nationalities");
  private static final MethodHandle _FH_nationalities = _FC_nationalities.dynamicInvoker();

   
  /* Feature Adjusted Offsets */
  public static final int _FI_mit_names_ambig = TypeSystemImpl.getAdjustedFeatureOffset("mit_names_ambig");
  public static final int _FI_mit_names_unambig = TypeSystemImpl.getAdjustedFeatureOffset("mit_names_unambig");
  public static final int _FI_mit_names_popular = TypeSystemImpl.getAdjustedFeatureOffset("mit_names_popular");
  public static final int _FI_mit_lastnames_ambig = TypeSystemImpl.getAdjustedFeatureOffset("mit_lastnames_ambig");
  public static final int _FI_mit_lastnames_unambig = TypeSystemImpl.getAdjustedFeatureOffset("mit_lastnames_unambig");
  public static final int _FI_mit_lastnames_popular = TypeSystemImpl.getAdjustedFeatureOffset("mit_lastnames_popular");
  public static final int _FI_mit_common_words = TypeSystemImpl.getAdjustedFeatureOffset("mit_common_words");
  public static final int _FI_mit_locations_unambig = TypeSystemImpl.getAdjustedFeatureOffset("mit_locations_unambig");
  public static final int _FI_mit_countries = TypeSystemImpl.getAdjustedFeatureOffset("mit_countries");
  public static final int _FI_wiki_gate_mit_companies = TypeSystemImpl.getAdjustedFeatureOffset("wiki_gate_mit_companies");
  public static final int _FI_mit_commonest_words = TypeSystemImpl.getAdjustedFeatureOffset("mit_commonest_words");
  public static final int _FI_US_cities = TypeSystemImpl.getAdjustedFeatureOffset("US_cities");
  public static final int _FI_US_counties = TypeSystemImpl.getAdjustedFeatureOffset("US_counties");
  public static final int _FI_nationalities = TypeSystemImpl.getAdjustedFeatureOffset("nationalities");

   
  /** Never called.  Disable default constructor
   * @generated */
  protected DictionaryAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public DictionaryAnnotation(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public DictionaryAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public DictionaryAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: mit_names_ambig

  /** getter for mit_names_ambig - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getMit_names_ambig() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_mit_names_ambig));}
    
  /** setter for mit_names_ambig - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_names_ambig(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_mit_names_ambig), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_names_unambig

  /** getter for mit_names_unambig - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getMit_names_unambig() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_mit_names_unambig));}
    
  /** setter for mit_names_unambig - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_names_unambig(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_mit_names_unambig), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_names_popular

  /** getter for mit_names_popular - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getMit_names_popular() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_mit_names_popular));}
    
  /** setter for mit_names_popular - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_names_popular(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_mit_names_popular), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_lastnames_ambig

  /** getter for mit_lastnames_ambig - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getMit_lastnames_ambig() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_mit_lastnames_ambig));}
    
  /** setter for mit_lastnames_ambig - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_lastnames_ambig(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_mit_lastnames_ambig), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_lastnames_unambig

  /** getter for mit_lastnames_unambig - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getMit_lastnames_unambig() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_mit_lastnames_unambig));}
    
  /** setter for mit_lastnames_unambig - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_lastnames_unambig(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_mit_lastnames_unambig), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_lastnames_popular

  /** getter for mit_lastnames_popular - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getMit_lastnames_popular() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_mit_lastnames_popular));}
    
  /** setter for mit_lastnames_popular - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_lastnames_popular(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_mit_lastnames_popular), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_common_words

  /** getter for mit_common_words - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getMit_common_words() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_mit_common_words));}
    
  /** setter for mit_common_words - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_common_words(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_mit_common_words), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_locations_unambig

  /** getter for mit_locations_unambig - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getMit_locations_unambig() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_mit_locations_unambig));}
    
  /** setter for mit_locations_unambig - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_locations_unambig(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_mit_locations_unambig), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_countries

  /** getter for mit_countries - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getMit_countries() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_mit_countries));}
    
  /** setter for mit_countries - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_countries(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_mit_countries), v);
  }    
    
   
    
  //*--------------*
  //* Feature: wiki_gate_mit_companies

  /** getter for wiki_gate_mit_companies - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getWiki_gate_mit_companies() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_wiki_gate_mit_companies));}
    
  /** setter for wiki_gate_mit_companies - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setWiki_gate_mit_companies(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_wiki_gate_mit_companies), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_commonest_words

  /** getter for mit_commonest_words - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getMit_commonest_words() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_mit_commonest_words));}
    
  /** setter for mit_commonest_words - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_commonest_words(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_mit_commonest_words), v);
  }    
    
   
    
  //*--------------*
  //* Feature: US_cities

  /** getter for US_cities - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getUS_cities() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_US_cities));}
    
  /** setter for US_cities - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setUS_cities(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_US_cities), v);
  }    
    
   
    
  //*--------------*
  //* Feature: US_counties

  /** getter for US_counties - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getUS_counties() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_US_counties));}
    
  /** setter for US_counties - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setUS_counties(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_US_counties), v);
  }    
    
   
    
  //*--------------*
  //* Feature: nationalities

  /** getter for nationalities - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getNationalities() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_nationalities));}
    
  /** setter for nationalities - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setNationalities(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_nationalities), v);
  }    
    
  }

    