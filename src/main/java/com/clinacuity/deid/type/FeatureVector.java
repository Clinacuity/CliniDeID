
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
public class FeatureVector extends Annotation {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final String _TypeName = "com.clinacuity.deid.type.FeatureVector";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public static final int typeIndexID = JCasRegistry.register(FeatureVector.class);
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
  /* *******************
   *   Feature Offsets *
   * *******************/ 
   
  public static final String _FeatName_allCaps = "allCaps";
  public static final String _FeatName_initCaps = "initCaps";
  public static final String _FeatName_initCapsAlpha = "initCapsAlpha";
  public static final String _FeatName_capsMix = "capsMix";
  public static final String _FeatName_hasDigit = "hasDigit";
  public static final String _FeatName_singleDigit = "singleDigit";
  public static final String _FeatName_doubleDigit = "doubleDigit";
  public static final String _FeatName_naturalNumber = "naturalNumber";
  public static final String _FeatName_realNumber = "realNumber";
  public static final String _FeatName_hasDash = "hasDash";
  public static final String _FeatName_initDash = "initDash";
  public static final String _FeatName_endDash = "endDash";
  public static final String _FeatName_alphaNumeric1 = "alphaNumeric1";
  public static final String _FeatName_alphaNumeric2 = "alphaNumeric2";
  public static final String _FeatName_punctuation = "punctuation";
  public static final String _FeatName_firstWord = "firstWord";
  public static final String _FeatName_lastWord = "lastWord";
  public static final String _FeatName_prefix1 = "prefix1";
  public static final String _FeatName_prefix2 = "prefix2";
  public static final String _FeatName_prefix3 = "prefix3";
  public static final String _FeatName_prefix4 = "prefix4";
  public static final String _FeatName_prefix5 = "prefix5";
  public static final String _FeatName_surffix1 = "surffix1";
  public static final String _FeatName_surffix2 = "surffix2";
  public static final String _FeatName_surffix3 = "surffix3";
  public static final String _FeatName_surffix4 = "surffix4";
  public static final String _FeatName_surffix5 = "surffix5";
  public static final String _FeatName_wordShape = "wordShape";
  public static final String _FeatName_wordShapeD = "wordShapeD";
  public static final String _FeatName_lword = "lword";
  public static final String _FeatName_word = "word";
  public static final String _FeatName_pos = "pos";
  public static final String _FeatName_np = "np";
  public static final String _FeatName_ner = "ner";
  public static final String _FeatName_tag = "tag";
  public static final String _FeatName_tNum = "tNum";
  public static final String _FeatName_sNum = "sNum";
  public static final String _FeatName_fileName = "fileName";
  public static final String _FeatName_wv = "wv";


  /* Feature Adjusted Offsets */
  private static final CallSite _FC_mit_names_ambig = TypeSystemImpl.createCallSite(FeatureVector.class, "mit_names_ambig");
  private static final MethodHandle _FH_mit_names_ambig = _FC_mit_names_ambig.dynamicInvoker();
  private static final CallSite _FC_mit_names_unambig = TypeSystemImpl.createCallSite(FeatureVector.class, "mit_names_unambig");
  private static final MethodHandle _FH_mit_names_unambig = _FC_mit_names_unambig.dynamicInvoker();
  private static final CallSite _FC_mit_names_popular = TypeSystemImpl.createCallSite(FeatureVector.class, "mit_names_popular");
  private static final MethodHandle _FH_mit_names_popular = _FC_mit_names_popular.dynamicInvoker();
  private static final CallSite _FC_mit_lastnames_ambig = TypeSystemImpl.createCallSite(FeatureVector.class, "mit_lastnames_ambig");
  private static final MethodHandle _FH_mit_lastnames_ambig = _FC_mit_lastnames_ambig.dynamicInvoker();
  private static final CallSite _FC_mit_lastnames_unambig = TypeSystemImpl.createCallSite(FeatureVector.class, "mit_lastnames_unambig");
  private static final MethodHandle _FH_mit_lastnames_unambig = _FC_mit_lastnames_unambig.dynamicInvoker();
  private static final CallSite _FC_mit_lastnames_popular = TypeSystemImpl.createCallSite(FeatureVector.class, "mit_lastnames_popular");
  private static final MethodHandle _FH_mit_lastnames_popular = _FC_mit_lastnames_popular.dynamicInvoker();
  private static final CallSite _FC_mit_common_words = TypeSystemImpl.createCallSite(FeatureVector.class, "mit_common_words");
  private static final MethodHandle _FH_mit_common_words = _FC_mit_common_words.dynamicInvoker();
  private static final CallSite _FC_mit_locations_unambig = TypeSystemImpl.createCallSite(FeatureVector.class, "mit_locations_unambig");
  private static final MethodHandle _FH_mit_locations_unambig = _FC_mit_locations_unambig.dynamicInvoker();
  private static final CallSite _FC_mit_countries = TypeSystemImpl.createCallSite(FeatureVector.class, "mit_countries");
  private static final MethodHandle _FH_mit_countries = _FC_mit_countries.dynamicInvoker();
  private static final CallSite _FC_wiki_gate_mit_companies = TypeSystemImpl.createCallSite(FeatureVector.class, "wiki_gate_mit_companies");
  private static final MethodHandle _FH_wiki_gate_mit_companies = _FC_wiki_gate_mit_companies.dynamicInvoker();
  private static final CallSite _FC_mit_commonest_words = TypeSystemImpl.createCallSite(FeatureVector.class, "mit_commonest_words");
  private static final MethodHandle _FH_mit_commonest_words = _FC_mit_commonest_words.dynamicInvoker();
  private static final CallSite _FC_US_cities = TypeSystemImpl.createCallSite(FeatureVector.class, "US_cities");
  private static final MethodHandle _FH_US_cities = _FC_US_cities.dynamicInvoker();
  private static final CallSite _FC_US_counties = TypeSystemImpl.createCallSite(FeatureVector.class, "US_counties");
  private static final MethodHandle _FH_US_counties = _FC_US_counties.dynamicInvoker();
  private static final CallSite _FC_nationalities = TypeSystemImpl.createCallSite(FeatureVector.class, "nationalities");
  private static final MethodHandle _FH_nationalities = _FC_nationalities.dynamicInvoker();
  /* Feature Adjusted Offsets */
  private static final CallSite _FC_allCaps = TypeSystemImpl.createCallSite(FeatureVector.class, "allCaps");
  private static final MethodHandle _FH_allCaps = _FC_allCaps.dynamicInvoker();
  private static final CallSite _FC_initCaps = TypeSystemImpl.createCallSite(FeatureVector.class, "initCaps");
  private static final MethodHandle _FH_initCaps = _FC_initCaps.dynamicInvoker();
  private static final CallSite _FC_initCapsAlpha = TypeSystemImpl.createCallSite(FeatureVector.class, "initCapsAlpha");
  private static final MethodHandle _FH_initCapsAlpha = _FC_initCapsAlpha.dynamicInvoker();
  private static final CallSite _FC_capsMix = TypeSystemImpl.createCallSite(FeatureVector.class, "capsMix");
  private static final MethodHandle _FH_capsMix = _FC_capsMix.dynamicInvoker();
  private static final CallSite _FC_hasDigit = TypeSystemImpl.createCallSite(FeatureVector.class, "hasDigit");
  private static final MethodHandle _FH_hasDigit = _FC_hasDigit.dynamicInvoker();
  private static final CallSite _FC_singleDigit = TypeSystemImpl.createCallSite(FeatureVector.class, "singleDigit");
  private static final MethodHandle _FH_singleDigit = _FC_singleDigit.dynamicInvoker();
  private static final CallSite _FC_doubleDigit = TypeSystemImpl.createCallSite(FeatureVector.class, "doubleDigit");
  private static final MethodHandle _FH_doubleDigit = _FC_doubleDigit.dynamicInvoker();
  private static final CallSite _FC_naturalNumber = TypeSystemImpl.createCallSite(FeatureVector.class, "naturalNumber");
  private static final MethodHandle _FH_naturalNumber = _FC_naturalNumber.dynamicInvoker();
  private static final CallSite _FC_realNumber = TypeSystemImpl.createCallSite(FeatureVector.class, "realNumber");
  private static final MethodHandle _FH_realNumber = _FC_realNumber.dynamicInvoker();
  private static final CallSite _FC_hasDash = TypeSystemImpl.createCallSite(FeatureVector.class, "hasDash");
  private static final MethodHandle _FH_hasDash = _FC_hasDash.dynamicInvoker();
  private static final CallSite _FC_initDash = TypeSystemImpl.createCallSite(FeatureVector.class, "initDash");
  private static final MethodHandle _FH_initDash = _FC_initDash.dynamicInvoker();
  private static final CallSite _FC_endDash = TypeSystemImpl.createCallSite(FeatureVector.class, "endDash");
  private static final MethodHandle _FH_endDash = _FC_endDash.dynamicInvoker();
  private static final CallSite _FC_alphaNumeric1 = TypeSystemImpl.createCallSite(FeatureVector.class, "alphaNumeric1");
  private static final MethodHandle _FH_alphaNumeric1 = _FC_alphaNumeric1.dynamicInvoker();
  private static final CallSite _FC_alphaNumeric2 = TypeSystemImpl.createCallSite(FeatureVector.class, "alphaNumeric2");
  private static final MethodHandle _FH_alphaNumeric2 = _FC_alphaNumeric2.dynamicInvoker();
  private static final CallSite _FC_punctuation = TypeSystemImpl.createCallSite(FeatureVector.class, "punctuation");
  private static final MethodHandle _FH_punctuation = _FC_punctuation.dynamicInvoker();
  private static final CallSite _FC_firstWord = TypeSystemImpl.createCallSite(FeatureVector.class, "firstWord");
  private static final MethodHandle _FH_firstWord = _FC_firstWord.dynamicInvoker();
  private static final CallSite _FC_lastWord = TypeSystemImpl.createCallSite(FeatureVector.class, "lastWord");
  private static final MethodHandle _FH_lastWord = _FC_lastWord.dynamicInvoker();
  private static final CallSite _FC_prefix1 = TypeSystemImpl.createCallSite(FeatureVector.class, "prefix1");
  private static final MethodHandle _FH_prefix1 = _FC_prefix1.dynamicInvoker();
  private static final CallSite _FC_prefix2 = TypeSystemImpl.createCallSite(FeatureVector.class, "prefix2");
  private static final MethodHandle _FH_prefix2 = _FC_prefix2.dynamicInvoker();
  private static final CallSite _FC_prefix3 = TypeSystemImpl.createCallSite(FeatureVector.class, "prefix3");
  private static final MethodHandle _FH_prefix3 = _FC_prefix3.dynamicInvoker();
  private static final CallSite _FC_prefix4 = TypeSystemImpl.createCallSite(FeatureVector.class, "prefix4");
  private static final MethodHandle _FH_prefix4 = _FC_prefix4.dynamicInvoker();
  private static final CallSite _FC_prefix5 = TypeSystemImpl.createCallSite(FeatureVector.class, "prefix5");
  private static final MethodHandle _FH_prefix5 = _FC_prefix5.dynamicInvoker();
  private static final CallSite _FC_surffix1 = TypeSystemImpl.createCallSite(FeatureVector.class, "surffix1");
  private static final MethodHandle _FH_surffix1 = _FC_surffix1.dynamicInvoker();
  private static final CallSite _FC_surffix2 = TypeSystemImpl.createCallSite(FeatureVector.class, "surffix2");
  private static final MethodHandle _FH_surffix2 = _FC_surffix2.dynamicInvoker();
  private static final CallSite _FC_surffix3 = TypeSystemImpl.createCallSite(FeatureVector.class, "surffix3");
  private static final MethodHandle _FH_surffix3 = _FC_surffix3.dynamicInvoker();
  private static final CallSite _FC_surffix4 = TypeSystemImpl.createCallSite(FeatureVector.class, "surffix4");
  private static final MethodHandle _FH_surffix4 = _FC_surffix4.dynamicInvoker();
  private static final CallSite _FC_surffix5 = TypeSystemImpl.createCallSite(FeatureVector.class, "surffix5");
  private static final MethodHandle _FH_surffix5 = _FC_surffix5.dynamicInvoker();
  private static final CallSite _FC_wordShape = TypeSystemImpl.createCallSite(FeatureVector.class, "wordShape");
  private static final MethodHandle _FH_wordShape = _FC_wordShape.dynamicInvoker();
  private static final CallSite _FC_wordShapeD = TypeSystemImpl.createCallSite(FeatureVector.class, "wordShapeD");
  private static final MethodHandle _FH_wordShapeD = _FC_wordShapeD.dynamicInvoker();
  private static final CallSite _FC_lword = TypeSystemImpl.createCallSite(FeatureVector.class, "lword");
  private static final MethodHandle _FH_lword = _FC_lword.dynamicInvoker();
  private static final CallSite _FC_word = TypeSystemImpl.createCallSite(FeatureVector.class, "word");
  private static final MethodHandle _FH_word = _FC_word.dynamicInvoker();
  private static final CallSite _FC_pos = TypeSystemImpl.createCallSite(FeatureVector.class, "pos");
  private static final MethodHandle _FH_pos = _FC_pos.dynamicInvoker();
  private static final CallSite _FC_np = TypeSystemImpl.createCallSite(FeatureVector.class, "np");
  private static final MethodHandle _FH_np = _FC_np.dynamicInvoker();
  private static final CallSite _FC_ner = TypeSystemImpl.createCallSite(FeatureVector.class, "ner");
  private static final MethodHandle _FH_ner = _FC_ner.dynamicInvoker();
  private static final CallSite _FC_tag = TypeSystemImpl.createCallSite(FeatureVector.class, "tag");
  private static final MethodHandle _FH_tag = _FC_tag.dynamicInvoker();
  private static final CallSite _FC_tNum = TypeSystemImpl.createCallSite(FeatureVector.class, "tNum");
  private static final MethodHandle _FH_tNum = _FC_tNum.dynamicInvoker();
  private static final CallSite _FC_sNum = TypeSystemImpl.createCallSite(FeatureVector.class, "sNum");
  private static final MethodHandle _FH_sNum = _FC_sNum.dynamicInvoker();
  private static final CallSite _FC_fileName = TypeSystemImpl.createCallSite(FeatureVector.class, "fileName");
  private static final MethodHandle _FH_fileName = _FC_fileName.dynamicInvoker();
  private static final CallSite _FC_wv = TypeSystemImpl.createCallSite(FeatureVector.class, "wv");
  private static final MethodHandle _FH_wv = _FC_wv.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  protected FeatureVector() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public FeatureVector(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public FeatureVector(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public FeatureVector(JCas jcas, int begin, int end) {
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
  public int getMit_names_ambig() { return _getIntValueNc(wrapGetIntCatchException(_FH_mit_names_ambig));}
    
  /** setter for mit_names_ambig - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_names_ambig(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_mit_names_ambig), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_names_unambig

  /** getter for mit_names_unambig - gets 
   * @generated
   * @return value of the feature 
   */
  public int getMit_names_unambig() { return _getIntValueNc(wrapGetIntCatchException(_FH_mit_names_unambig));}
    
  /** setter for mit_names_unambig - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_names_unambig(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_mit_names_unambig), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_names_popular

  /** getter for mit_names_popular - gets 
   * @generated
   * @return value of the feature 
   */
  public int getMit_names_popular() { return _getIntValueNc(wrapGetIntCatchException(_FH_mit_names_popular));}
    
  /** setter for mit_names_popular - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_names_popular(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_mit_names_popular), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_lastnames_ambig

  /** getter for mit_lastnames_ambig - gets 
   * @generated
   * @return value of the feature 
   */
  public int getMit_lastnames_ambig() { return _getIntValueNc(wrapGetIntCatchException(_FH_mit_lastnames_ambig));}
    
  /** setter for mit_lastnames_ambig - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_lastnames_ambig(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_mit_lastnames_ambig), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_lastnames_unambig

  /** getter for mit_lastnames_unambig - gets 
   * @generated
   * @return value of the feature 
   */
  public int getMit_lastnames_unambig() { return _getIntValueNc(wrapGetIntCatchException(_FH_mit_lastnames_unambig));}
    
  /** setter for mit_lastnames_unambig - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_lastnames_unambig(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_mit_lastnames_unambig), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_lastnames_popular

  /** getter for mit_lastnames_popular - gets 
   * @generated
   * @return value of the feature 
   */
  public int getMit_lastnames_popular() { return _getIntValueNc(wrapGetIntCatchException(_FH_mit_lastnames_popular));}
    
  /** setter for mit_lastnames_popular - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_lastnames_popular(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_mit_lastnames_popular), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_common_words

  /** getter for mit_common_words - gets 
   * @generated
   * @return value of the feature 
   */
  public int getMit_common_words() { return _getIntValueNc(wrapGetIntCatchException(_FH_mit_common_words));}
    
  /** setter for mit_common_words - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_common_words(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_mit_common_words), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_locations_unambig

  /** getter for mit_locations_unambig - gets 
   * @generated
   * @return value of the feature 
   */
  public int getMit_locations_unambig() { return _getIntValueNc(wrapGetIntCatchException(_FH_mit_locations_unambig));}
    
  /** setter for mit_locations_unambig - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_locations_unambig(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_mit_locations_unambig), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_countries

  /** getter for mit_countries - gets 
   * @generated
   * @return value of the feature 
   */
  public int getMit_countries() { return _getIntValueNc(wrapGetIntCatchException(_FH_mit_countries));}
    
  /** setter for mit_countries - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_countries(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_mit_countries), v);
  }    
    
   
    
  //*--------------*
  //* Feature: wiki_gate_mit_companies

  /** getter for wiki_gate_mit_companies - gets 
   * @generated
   * @return value of the feature 
   */
  public int getWiki_gate_mit_companies() { return _getIntValueNc(wrapGetIntCatchException(_FH_wiki_gate_mit_companies));}
    
  /** setter for wiki_gate_mit_companies - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setWiki_gate_mit_companies(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_wiki_gate_mit_companies), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mit_commonest_words

  /** getter for mit_commonest_words - gets 
   * @generated
   * @return value of the feature 
   */
  public int getMit_commonest_words() { return _getIntValueNc(wrapGetIntCatchException(_FH_mit_commonest_words));}
    
  /** setter for mit_commonest_words - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMit_commonest_words(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_mit_commonest_words), v);
  }    
    
   
    
  //*--------------*
  //* Feature: US_cities

  /** getter for US_cities - gets 
   * @generated
   * @return value of the feature 
   */
  public int getUS_cities() { return _getIntValueNc(wrapGetIntCatchException(_FH_US_cities));}
    
  /** setter for US_cities - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setUS_cities(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_US_cities), v);
  }    
    
   
    
  //*--------------*
  //* Feature: US_counties

  /** getter for US_counties - gets 
   * @generated
   * @return value of the feature 
   */
  public int getUS_counties() { return _getIntValueNc(wrapGetIntCatchException(_FH_US_counties));}
    
  /** setter for US_counties - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setUS_counties(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_US_counties), v);
  }    
    
   
    
  //*--------------*
  //* Feature: nationalities

  /** getter for nationalities - gets 
   * @generated
   * @return value of the feature 
   */
  public int getNationalities() { return _getIntValueNc(wrapGetIntCatchException(_FH_nationalities));}
    
  /** setter for nationalities - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setNationalities(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_nationalities), v);
  }    
    
   
    
  //*--------------*
  //* Feature: allCaps

  /** getter for allCaps - gets 
   * @generated
   * @return value of the feature 
   */
  public int getAllCaps() { return _getIntValueNc(wrapGetIntCatchException(_FH_allCaps));}
    
  /** setter for allCaps - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setAllCaps(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_allCaps), v);
  }    
    
   
    
  //*--------------*
  //* Feature: initCaps

  /** getter for initCaps - gets 
   * @generated
   * @return value of the feature 
   */
  public int getInitCaps() { return _getIntValueNc(wrapGetIntCatchException(_FH_initCaps));}
    
  /** setter for initCaps - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setInitCaps(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_initCaps), v);
  }    
    
   
    
  //*--------------*
  //* Feature: initCapsAlpha

  /** getter for initCapsAlpha - gets 
   * @generated
   * @return value of the feature 
   */
  public int getInitCapsAlpha() { return _getIntValueNc(wrapGetIntCatchException(_FH_initCapsAlpha));}
    
  /** setter for initCapsAlpha - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setInitCapsAlpha(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_initCapsAlpha), v);
  }    
    
   
    
  //*--------------*
  //* Feature: capsMix

  /** getter for capsMix - gets 
   * @generated
   * @return value of the feature 
   */
  public int getCapsMix() { return _getIntValueNc(wrapGetIntCatchException(_FH_capsMix));}
    
  /** setter for capsMix - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setCapsMix(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_capsMix), v);
  }    
    
   
    
  //*--------------*
  //* Feature: hasDigit

  /** getter for hasDigit - gets 
   * @generated
   * @return value of the feature 
   */
  public int getHasDigit() { return _getIntValueNc(wrapGetIntCatchException(_FH_hasDigit));}
    
  /** setter for hasDigit - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setHasDigit(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_hasDigit), v);
  }    
    
   
    
  //*--------------*
  //* Feature: singleDigit

  /** getter for singleDigit - gets 
   * @generated
   * @return value of the feature 
   */
  public int getSingleDigit() { return _getIntValueNc(wrapGetIntCatchException(_FH_singleDigit));}
    
  /** setter for singleDigit - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSingleDigit(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_singleDigit), v);
  }    
    
   
    
  //*--------------*
  //* Feature: doubleDigit

  /** getter for doubleDigit - gets 
   * @generated
   * @return value of the feature 
   */
  public int getDoubleDigit() { return _getIntValueNc(wrapGetIntCatchException(_FH_doubleDigit));}
    
  /** setter for doubleDigit - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDoubleDigit(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_doubleDigit), v);
  }    
    
   
    
  //*--------------*
  //* Feature: naturalNumber

  /** getter for naturalNumber - gets 
   * @generated
   * @return value of the feature 
   */
  public int getNaturalNumber() { return _getIntValueNc(wrapGetIntCatchException(_FH_naturalNumber));}
    
  /** setter for naturalNumber - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setNaturalNumber(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_naturalNumber), v);
  }    
    
   
    
  //*--------------*
  //* Feature: realNumber

  /** getter for realNumber - gets 
   * @generated
   * @return value of the feature 
   */
  public int getRealNumber() { return _getIntValueNc(wrapGetIntCatchException(_FH_realNumber));}
    
  /** setter for realNumber - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRealNumber(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_realNumber), v);
  }    
    
   
    
  //*--------------*
  //* Feature: hasDash

  /** getter for hasDash - gets 
   * @generated
   * @return value of the feature 
   */
  public int getHasDash() { return _getIntValueNc(wrapGetIntCatchException(_FH_hasDash));}
    
  /** setter for hasDash - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setHasDash(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_hasDash), v);
  }    
    
   
    
  //*--------------*
  //* Feature: initDash

  /** getter for initDash - gets 
   * @generated
   * @return value of the feature 
   */
  public int getInitDash() { return _getIntValueNc(wrapGetIntCatchException(_FH_initDash));}
    
  /** setter for initDash - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setInitDash(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_initDash), v);
  }    
    
   
    
  //*--------------*
  //* Feature: endDash

  /** getter for endDash - gets 
   * @generated
   * @return value of the feature 
   */
  public int getEndDash() { return _getIntValueNc(wrapGetIntCatchException(_FH_endDash));}
    
  /** setter for endDash - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setEndDash(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_endDash), v);
  }    
    
   
    
  //*--------------*
  //* Feature: alphaNumeric1

  /** getter for alphaNumeric1 - gets 
   * @generated
   * @return value of the feature 
   */
  public int getAlphaNumeric1() { return _getIntValueNc(wrapGetIntCatchException(_FH_alphaNumeric1));}
    
  /** setter for alphaNumeric1 - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setAlphaNumeric1(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_alphaNumeric1), v);
  }    
    
   
    
  //*--------------*
  //* Feature: alphaNumeric2

  /** getter for alphaNumeric2 - gets 
   * @generated
   * @return value of the feature 
   */
  public int getAlphaNumeric2() { return _getIntValueNc(wrapGetIntCatchException(_FH_alphaNumeric2));}
    
  /** setter for alphaNumeric2 - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setAlphaNumeric2(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_alphaNumeric2), v);
  }    
    
   
    
  //*--------------*
  //* Feature: punctuation

  /** getter for punctuation - gets 
   * @generated
   * @return value of the feature 
   */
  public int getPunctuation() { return _getIntValueNc(wrapGetIntCatchException(_FH_punctuation));}
    
  /** setter for punctuation - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPunctuation(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_punctuation), v);
  }    
    
   
    
  //*--------------*
  //* Feature: firstWord

  /** getter for firstWord - gets 
   * @generated
   * @return value of the feature 
   */
  public int getFirstWord() { return _getIntValueNc(wrapGetIntCatchException(_FH_firstWord));}
    
  /** setter for firstWord - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFirstWord(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_firstWord), v);
  }    
    
   
    
  //*--------------*
  //* Feature: lastWord

  /** getter for lastWord - gets 
   * @generated
   * @return value of the feature 
   */
  public int getLastWord() { return _getIntValueNc(wrapGetIntCatchException(_FH_lastWord));}
    
  /** setter for lastWord - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLastWord(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_lastWord), v);
  }    
    
   
    
  //*--------------*
  //* Feature: prefix1

  /** getter for prefix1 - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPrefix1() { return _getStringValueNc(wrapGetIntCatchException(_FH_prefix1));}
    
  /** setter for prefix1 - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPrefix1(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_prefix1), v);
  }    
    
   
    
  //*--------------*
  //* Feature: prefix2

  /** getter for prefix2 - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPrefix2() { return _getStringValueNc(wrapGetIntCatchException(_FH_prefix2));}
    
  /** setter for prefix2 - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPrefix2(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_prefix2), v);
  }    
    
   
    
  //*--------------*
  //* Feature: prefix3

  /** getter for prefix3 - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPrefix3() { return _getStringValueNc(wrapGetIntCatchException(_FH_prefix3));}
    
  /** setter for prefix3 - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPrefix3(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_prefix3), v);
  }    
    
   
    
  //*--------------*
  //* Feature: prefix4

  /** getter for prefix4 - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPrefix4() { return _getStringValueNc(wrapGetIntCatchException(_FH_prefix4));}
    
  /** setter for prefix4 - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPrefix4(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_prefix4), v);
  }    
    
   
    
  //*--------------*
  //* Feature: prefix5

  /** getter for prefix5 - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPrefix5() { return _getStringValueNc(wrapGetIntCatchException(_FH_prefix5));}
    
  /** setter for prefix5 - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPrefix5(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_prefix5), v);
  }    
    
   
    
  //*--------------*
  //* Feature: surffix1

  /** getter for surffix1 - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSurffix1() { return _getStringValueNc(wrapGetIntCatchException(_FH_surffix1));}
    
  /** setter for surffix1 - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSurffix1(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_surffix1), v);
  }    
    
   
    
  //*--------------*
  //* Feature: surffix2

  /** getter for surffix2 - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSurffix2() { return _getStringValueNc(wrapGetIntCatchException(_FH_surffix2));}
    
  /** setter for surffix2 - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSurffix2(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_surffix2), v);
  }    
    
   
    
  //*--------------*
  //* Feature: surffix3

  /** getter for surffix3 - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSurffix3() { return _getStringValueNc(wrapGetIntCatchException(_FH_surffix3));}
    
  /** setter for surffix3 - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSurffix3(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_surffix3), v);
  }    
    
   
    
  //*--------------*
  //* Feature: surffix4

  /** getter for surffix4 - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSurffix4() { return _getStringValueNc(wrapGetIntCatchException(_FH_surffix4));}
    
  /** setter for surffix4 - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSurffix4(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_surffix4), v);
  }    
    
   
    
  //*--------------*
  //* Feature: surffix5

  /** getter for surffix5 - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSurffix5() { return _getStringValueNc(wrapGetIntCatchException(_FH_surffix5));}
    
  /** setter for surffix5 - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSurffix5(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_surffix5), v);
  }    
    
   
    
  //*--------------*
  //* Feature: wordShape

  /** getter for wordShape - gets 
   * @generated
   * @return value of the feature 
   */
  public String getWordShape() { return _getStringValueNc(wrapGetIntCatchException(_FH_wordShape));}
    
  /** setter for wordShape - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setWordShape(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_wordShape), v);
  }    
    
   
    
  //*--------------*
  //* Feature: wordShapeD

  /** getter for wordShapeD - gets 
   * @generated
   * @return value of the feature 
   */
  public String getWordShapeD() { return _getStringValueNc(wrapGetIntCatchException(_FH_wordShapeD));}
    
  /** setter for wordShapeD - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setWordShapeD(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_wordShapeD), v);
  }    
    
   
    
  //*--------------*
  //* Feature: lword

  /** getter for lword - gets 
   * @generated
   * @return value of the feature 
   */
  public String getLword() { return _getStringValueNc(wrapGetIntCatchException(_FH_lword));}
    
  /** setter for lword - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLword(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_lword), v);
  }    
    
   
    
  //*--------------*
  //* Feature: word

  /** getter for word - gets 
   * @generated
   * @return value of the feature 
   */
  public String getWord() { return _getStringValueNc(wrapGetIntCatchException(_FH_word));}
    
  /** setter for word - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setWord(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_word), v);
  }    
    
   
    
  //*--------------*
  //* Feature: pos

  /** getter for pos - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPos() { return _getStringValueNc(wrapGetIntCatchException(_FH_pos));}
    
  /** setter for pos - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPos(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_pos), v);
  }    
    
   
    
  //*--------------*
  //* Feature: np

  /** getter for np - gets 
   * @generated
   * @return value of the feature 
   */
  public String getNp() { return _getStringValueNc(wrapGetIntCatchException(_FH_np));}
    
  /** setter for np - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setNp(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_np), v);
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
    
   
    
  //*--------------*
  //* Feature: tNum

  /** getter for tNum - gets 
   * @generated
   * @return value of the feature 
   */
  public int getTNum() { return _getIntValueNc(wrapGetIntCatchException(_FH_tNum));}
    
  /** setter for tNum - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTNum(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_tNum), v);
  }    
    
   
    
  //*--------------*
  //* Feature: sNum

  /** getter for sNum - gets 
   * @generated
   * @return value of the feature 
   */
  public int getSNum() { return _getIntValueNc(wrapGetIntCatchException(_FH_sNum));}
    
  /** setter for sNum - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSNum(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_sNum), v);
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
  //* Feature: wv

  /** getter for wv - gets 
   * @generated
   * @return value of the feature 
   */
  public String getWv() { return _getStringValueNc(wrapGetIntCatchException(_FH_wv));}
    
  /** setter for wv - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setWv(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_wv), v);
  }    
    
  }

    