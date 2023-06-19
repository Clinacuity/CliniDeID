
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

// package com.clinacuity.deid.ae;
//
// import java.io.File;
// import java.io.FileInputStream;
// import java.io.FileOutputStream;
// import java.io.IOException;
// import java.io.ObjectInputStream;
// import java.io.ObjectOutputStream;
// import java.util.Arrays;
// import java.util.Collection;
// import java.util.HashMap;
// import java.util.Map;
//
// import org.deeplearning4j.models.word2vec.VocabWord;
// import org.deeplearning4j.models.word2vec.Word2Vec;
// import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
// import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
// import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
// import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
// import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
// import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
//
// public class TryWordVec {
// public static void main(String[] argv) {
// new TryWordVec();
// }
//
// public TryWordVec() {
// int layerSize = 10;
// String modelFile = "/Users/garyunderwood/deid/src/test/resources/CRFAllAnnotator/2014/w2vModels/w2vModelSub-5-1-" + layerSize
// + "-42-5.bin";
// Word2Vec model = createModel("/Users/garyunderwood/NER/allTokens.txt", layerSize);
// System.out.println("model seen: " + Arrays.toString(model.getWordVector("seen")));
// writeModel(modelFile, model);
// makeHashMap(modelFile,
// "/Users/garyunderwood/deid/src/test/resources/CRFAllAnnotator/2014/w2vHashMaps/w2vHashMapSub" + layerSize + "-3.ser");
// }
//
// public void makeHashMap(String modelFile, String outputMap) {// params are filenames for Word2Vec saved model and name of output map, just loads model and
// // calls other makeHashMap
// Word2Vec model;
// try {
// model = readModel(modelFile);
// makeHashMap(model, outputMap);
// } catch (ClassNotFoundException | IOException e) {
// e.printStackTrace();
// }
// Map<String, String[]> map = readMap(outputMap);
// System.out.println("From serialized seen: " + Arrays.toString(map.get("seen")));
// }
//
// public void makeHashMap(Word2Vec model, String outputFile) {// makes hash map of word to vector from model, serializes it and writes to outputFile
// HashMap<String, String[]> map = new HashMap<>();
// VocabCache<VocabWord> words = model.getVocab();
// Collection<VocabWord> wordsC = words.vocabWords();
// for (VocabWord vocabWord : wordsC) {
// String word = vocabWord.getWord();
// String[] rounded = new String[model.getLayerSize()];
// double[] values = model.getWordVector(word);
// for (int i = 0; i < values.length; i++) {
// // currently rounding to 3 digits, then removing the 0. from the numbers, so that -0.13556131 becomes 136, reduces training data significantly
// rounded[i] = String.format("%.3f", values[i]).replace("0.", "");
// }
// map.put(word, rounded);
// }
// System.out.println(Arrays.toString(model.getWordVector("seen")));
// System.out.println(Arrays.toString(map.get("seen")));
// try (FileOutputStream fos = new FileOutputStream(outputFile); ObjectOutputStream oos = new ObjectOutputStream(fos);) {
// oos.writeObject(map);
// } catch (IOException e) {
// e.printStackTrace();
// }
// }
//
// public static Map<String, String[]> readMap(String inputFile) {// open file and read/return the HashMap
// return readMap(new File(inputFile));
// }
//
// public static Map<String, String[]> readMap(File inputFile) {// read serialized hash map and return object
// try (FileInputStream fis = new FileInputStream(inputFile); ObjectInputStream ois = new ObjectInputStream(fis);) {
// @SuppressWarnings("unchecked") // for casting Word2Vector's data in hashmap serialized object
// HashMap<String, String[]> data = (HashMap<String, String[]>) ois.readObject();
// return data;
// } catch (IOException | ClassNotFoundException e) {
// e.printStackTrace();
// }
// return null;
// }
//
// public static Word2Vec createModel(String inputFile, int layerSize) {// layer size is vector size
// SentenceIterator iter = new LineSentenceIterator(new File(inputFile));
// // iter.setPreProcessor(new SentencePreProcessor() {
// // @Override
// // public String preProcess(String sentence) {
// // return sentence.toLowerCase();
// // }
// // });
// // Split on white spaces in the line to get words
// TokenizerFactory t = new DefaultTokenizerFactory();
// t.setTokenPreProcessor(new CommonPreprocessor());
//
// Word2Vec vec = new Word2Vec.Builder().minWordFrequency(5).iterations(1).layerSize(layerSize).seed(42).windowSize(5).iterate(iter)
// .tokenizerFactory(t).build(); // layerSize was 100, rest are their defaults
// vec.fit();
// return vec;
// }
//
// public static void writeModel(String outputFile, Word2Vec model) {// serialize model to file
// try (FileOutputStream fos = new FileOutputStream(outputFile); ObjectOutputStream oos = new ObjectOutputStream(fos);) {
// oos.writeObject(model);
// } catch (IOException e) {
// e.printStackTrace();
// }
// }
//
// public static Word2Vec readModel(String inputFile) throws ClassNotFoundException, IOException {
// return readModel(new File(inputFile));
// }
//
// public static Word2Vec readModel(File inputFile) throws ClassNotFoundException, IOException {
// try (FileInputStream fis = new FileInputStream(inputFile); ObjectInputStream ois = new ObjectInputStream(fis);) {
// return (Word2Vec) ois.readObject();
// }
// }
// }
