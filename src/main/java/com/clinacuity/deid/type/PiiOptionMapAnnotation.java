
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



   
/* Apache UIMA v3 - First created by JCasGen Thu Apr 09 14:39:22 EDT 2020 */

package com.clinacuity.deid.type;

import org.apache.uima.UimaSerializable;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FeatureStructureImplC;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.BooleanArray;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;


/**
 * Updated by JCasGen Thu Apr 09 14:39:22 EDT 2020
 * XML source: src/main/resources/desc/PiiOptionMapAnnotation.xml
 *
 */
public class PiiOptionMapAnnotation extends Annotation implements UimaSerializable {

    /**
     * @ordered
     */
    @SuppressWarnings("hiding")
    public static final String _TypeName = "com.clinacuity.deid.type.PiiOptionMapAnnotation";

    /**
     * @ordered
     */
    @SuppressWarnings("hiding")
    public static final int typeIndexID = JCasRegistry.register(PiiOptionMapAnnotation.class);
    /**
     * @ordered
     */
    @SuppressWarnings("hiding")
    public static final int type = typeIndexID;
    public static final String _FeatName_keys = "keys";


    /* *******************
     *   Feature Offsets *
     * *******************/
    public static final String _FeatName_values = "values";
    public static final String _FeatName_keysSpecial = "keysSpecial";
    public static final String _FeatName_valuesSpecial = "valuesSpecial";
    /* Feature Adjusted Offsets */
    private static final CallSite _FC_keys = TypeSystemImpl.createCallSite(PiiOptionMapAnnotation.class, "keys");
    private static final MethodHandle _FH_keys = _FC_keys.dynamicInvoker();
    private static final CallSite _FC_values = TypeSystemImpl.createCallSite(PiiOptionMapAnnotation.class, "values");
    private static final MethodHandle _FH_values = _FC_values.dynamicInvoker();
    private static final CallSite _FC_keysSpecial = TypeSystemImpl.createCallSite(PiiOptionMapAnnotation.class, "keysSpecial");
    private static final MethodHandle _FH_keysSpecial = _FC_keysSpecial.dynamicInvoker();
    private static final CallSite _FC_valuesSpecial = TypeSystemImpl.createCallSite(PiiOptionMapAnnotation.class, "valuesSpecial");
    private static final MethodHandle _FH_valuesSpecial = _FC_valuesSpecial.dynamicInvoker();
    /**
     * Never called.  Disable default constructor
     *
     */
    protected PiiOptionMapAnnotation() {/* intentionally empty block */}

    /**
     * Internal - constructor used by generator
     *
     * @param casImpl the CAS this Feature Structure belongs to
     * @param type    the type of this Feature Structure
     */
    public PiiOptionMapAnnotation(TypeImpl type, CASImpl casImpl) {
        super(type, casImpl);
        readObject();
    }

    /**
     * @param jcas JCas to which this Feature Structure belongs
     */
    public PiiOptionMapAnnotation(JCas jcas) {
        super(jcas);
        readObject();
    }

    /**
     * @param jcas  JCas to which this Feature Structure belongs
     * @param begin offset to the begin spot in the SofA
     * @param end   offset to the end spot in the SofA
     */
    public PiiOptionMapAnnotation(JCas jcas, int begin, int end) {
        super(jcas);
        setBegin(begin);
        setEnd(end);
        readObject();
    }

    /**
     * @return index of the type
     */
    @Override
    public int getTypeIndexID() {
        return typeIndexID;
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
    //* Feature: keys

    /**
     * getter for keys - gets
     *
     * @return value of the feature
     */
    public StringArray getKeys() {
        return (StringArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_keys)));
    }

    /**
     * setter for keys - sets
     *
     * @param v value to set into the feature
     */
    public void setKeys(StringArray v) {
        _setFeatureValueNcWj(wrapGetIntCatchException(_FH_keys), v);
    }


    /**
     * indexed getter for keys - gets an indexed value -
     *
     * @param i index in the array to get
     * @return value of the element at index i
     */
    public String getKeys(int i) {
        return ((StringArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_keys)))).get(i);
    }

    /**
     * indexed setter for keys - sets an indexed value -
     *
     * @param i index in the array to set
     * @param v value to set into the array
     */
    public void setKeys(int i, String v) {
        ((StringArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_keys)))).set(i, v);
    }


    //*--------------*
    //* Feature: values

    /**
     * getter for values - gets
     *
     * @return value of the feature
     */
    public BooleanArray getValues() {
        return (BooleanArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_values)));
    }

    /**
     * setter for values - sets
     *
     * @param v value to set into the feature
     */
    public void setValues(BooleanArray v) {
        _setFeatureValueNcWj(wrapGetIntCatchException(_FH_values), v);
    }


    /**
     * indexed getter for values - gets an indexed value -
     *
     * @param i index in the array to get
     * @return value of the element at index i
     */
    public boolean getValues(int i) {
        return ((BooleanArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_values)))).get(i);
    }

    /**
     * indexed setter for values - sets an indexed value -
     *
     * @param i index in the array to set
     * @param v value to set into the array
     */
    public void setValues(int i, boolean v) {
        ((BooleanArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_values)))).set(i, v);
    }


    //*--------------*
    //* Feature: keysSpecial

    /**
     * getter for keysSpecial - gets
     *
     * @return value of the feature
     */
    public StringArray getKeysSpecial() {
        return (StringArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_keysSpecial)));
    }

    /**
     * setter for keysSpecial - sets
     *
     * @param v value to set into the feature
     */
    public void setKeysSpecial(StringArray v) {
        _setFeatureValueNcWj(wrapGetIntCatchException(_FH_keysSpecial), v);
    }


    /**
     * indexed getter for keysSpecial - gets an indexed value -
     *
     * @param i index in the array to get
     * @return value of the element at index i
     */
    public String getKeysSpecial(int i) {
        return ((StringArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_keysSpecial)))).get(i);
    }

    /**
     * indexed setter for keysSpecial - sets an indexed value -
     *
     * @param i index in the array to set
     * @param v value to set into the array
     */
    public void setKeysSpecial(int i, String v) {
        ((StringArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_keysSpecial)))).set(i, v);
    }


    //*--------------*
    //* Feature: valuesSpecial

    /**
     * getter for valuesSpecial - gets
     *
     * @return value of the feature
     */
    public IntegerArray getValuesSpecial() {
        return (IntegerArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_valuesSpecial)));
    }

    /**
     * setter for valuesSpecial - sets
     *
     * @param v value to set into the feature
     */
    public void setValuesSpecial(IntegerArray v) {
        _setFeatureValueNcWj(wrapGetIntCatchException(_FH_valuesSpecial), v);
    }


    /**
     * indexed getter for valuesSpecial - gets an indexed value -
     *
     * @param i index in the array to get
     * @return value of the element at index i
     */
    public int getValuesSpecial(int i) {
        return ((IntegerArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_valuesSpecial)))).get(i);
    }

    /**
     * indexed setter for valuesSpecial - sets an indexed value -
     *
     * @param i index in the array to set
     * @param v value to set into the array
     */
    public void setValuesSpecial(int i, int v) {
        ((IntegerArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_valuesSpecial)))).set(i, v);
    }

    private Map<String, Boolean> include = new HashMap<>();
    private Map<String, Integer> special = new HashMap<>();

    public Boolean getIncludeValue(String key) {
        return include.get(key);
    }

    public void setIncludeValue(String key, Boolean value) {
        include.put(key, value);
    }

    public Map<String, Boolean> getIncludeMap() {
        return include;
    }

    public void setIncludeMap(Map<String, Boolean> map) {
        include = map;
    }

    public Integer getSpecialValue(String key) {
        return special.get(key);
    }

    public void setSpecialValue(String key, Integer value) {
        special.put(key, value);
    }

    public Map<String, Integer> getSpecialMap() {
        return special;
    }

    public void setSpecialMap(Map<String, Integer> map) {
        special = map;
    }

    @Override
    public void _init_from_cas_data() {
        StringArray keys = getKeys();
        BooleanArray values = getValues();
        include.clear();
        for (int i = keys.size() - 1; i >= 0; i--) {
            include.put(keys.get(i), values.get(i));
        }

        StringArray keysSpecial = getKeysSpecial();
        IntegerArray valuesSpecial = getValuesSpecial();
        special.clear();
        for (int i = keysSpecial.size() - 1; i >= 0; i--) {
            special.put(keysSpecial.get(i), valuesSpecial.get(i));
        }

    }

    @Override
    public void _save_to_cas_data() {
        int i = 0;
        StringArray keys = new StringArray(_casView.getJCasImpl(), include.size());
        BooleanArray values = new BooleanArray(_casView.getJCasImpl(), include.size());
        for (Map.Entry<String, Boolean> entry : include.entrySet()) {
            keys.set(i, entry.getKey());
            values.set(i, entry.getValue());
            i++;
        }
        setKeys(keys);
        setValues(values);

        StringArray keys2 = new StringArray(_casView.getJCasImpl(), special.size());
        IntegerArray values2 = new IntegerArray(_casView.getJCasImpl(), special.size());
        i = 0;
        for (Map.Entry<String, Integer> entry : special.entrySet()) {
            keys2.set(i, entry.getKey());
            values2.set(i, entry.getValue());
            i++;
        }
        setKeysSpecial(keys2);
        setValuesSpecial(values2);

    }

    public FeatureStructureImplC _superClone() {
        return clone();
    }
}

    