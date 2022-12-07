
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

package com.clinacuity.deid.training;

import com.clinacuity.deid.Other.TestDeidPipeline;
import com.clinacuity.deid.ae.DictionaryAnnotator;
import com.clinacuity.deid.ae.OpenNlpAnnotator;
import com.clinacuity.deid.mains.DeidPipeline;
import com.clinacuity.deid.readers.FileSystemCollectionReader;
import com.clinacuity.deid.readers.GeneralCollectionReader;
import com.clinacuity.deid.readers.XmlCollectionReaderClearTkTraining;
import com.clinacuity.deid.type.BaseToken;
import com.clinacuity.deid.type.PiiAnnotation;
import com.clinacuity.deid.util.Serializer;
import com.clinacuity.deid.util.Utilities;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.jar.Train;
import org.cleartk.ml.mallet.MalletCrfStringOutcomeDataWriter;
import org.cleartk.util.ViewUriUtil;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class RunTrainClearTkMallet {
    public static final int BROWN_CUTOFF = 7;
    public static final String BROWN_FILE = "models/malletCrf/brown-AllI2B2-4-1000.ser";
    private static final Logger logger = LogManager.getLogger();

    private RunTrainClearTkMallet() {// shouldn't instantiate this class
    }

    // all AEs that run before NER is done. NER does feature extraction, so whatever is needed for features should be done here
    // the numExtra is the number of extra spots in returned array to reserve for future AEs
    // should do this with ArrayList instead of array, but couldn't get SimplePipeline to work with ArrayList.toArray or anything
    private static AnalysisEngine[] createPreProcessingAEs(int numExtra) throws ResourceInitializationException {
        AnalysisEngine uri2DocText = AnalysisEngineFactory.createEngine(UriToDocumentTextAnnotator.getDescription());//needed?
        AnalysisEngine openNlpAnnotator = AnalysisEngineFactory.createEngine(OpenNlpAnnotator.class);
        AnalysisEngine DictionaryAnnotator = AnalysisEngineFactory.createEngine(DictionaryAnnotator.class);

        // can't get ArrayList.toArray(new AnalysisEngine[0]) to work, keep getting null when run pipeline
        if (numExtra == 1) {
            return new AnalysisEngine[]{openNlpAnnotator, DictionaryAnnotator, null};
        } else if (numExtra == 2) {
            return new AnalysisEngine[]{uri2DocText, openNlpAnnotator, DictionaryAnnotator, null, null};
        } else {
            return new AnalysisEngine[]{uri2DocText, openNlpAnnotator, DictionaryAnnotator};
        }
    }

    // given directory of input .txt files (with -ne.xml files for gold annoations), create xmi files in xmiDirectory
    // runs preprocessing AEs on text and saves them
    private static void createXmi(String inputDirectory, String xmiDirectory) {
        try {
            CollectionReader reader = UriCollectionReader.getCollectionReaderFromDirectory(new File(inputDirectory),
                    MascTextFileFilter.class, null);// filter for txt files, not the xml or ne.xml files
            AnalysisEngine[] aes = createPreProcessingAEs(1);
            //TODO: update for DocumentInformationAnnotation options // aes[aes.length - 1] = AnalysisEngineFactory.createEngine(RawXmiOutputAnnotator.class, RawXmiOutputAnnotator.OUTPUT_DIRECTORY_PARAM, xmiDirectory);
            SimplePipeline.runPipeline(reader, aes);
        } catch (Exception e) {
            logger.throwing(e);
            throw new RuntimeException(e);
        }
    }

    // train txt files and create model using ClearTK's Mallet wrapper
    // each file should have a -ne.xml version with gold annotations
    private static void trainClearTkMallet(String dataDirectory, String modelDirectory, String extraOpt) { // , String w2vModel) {
        MuscGoldCollectionReader.isTraining = true;
        try {
            CollectionReader reader = UriCollectionReader.getCollectionReaderFromDirectory(new File(dataDirectory),
                    MascTextFileFilter.class, null);
            AnalysisEngine goldAnnotator = AnalysisEngineFactory.createEngine(GoldAnnotator.class);
            AnalysisEngine nerEngine = AnalysisEngineFactory.createEngine(NamedEntityChunker.class,
                    CleartkSequenceAnnotator.PARAM_IS_TRAINING, true, NamedEntityChunker.BROWN_CLUSTERS_FILENAME, BROWN_FILE,
                    NamedEntityChunker.BROWN_CUTOFF, BROWN_CUTOFF, NamedEntityChunker.OPTION, extraOpt,
                    // NamedEntityChunker.WORD2VEC_HASHMAP_MODEL_FILENAME, w2vModel,
                    DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, new File(modelDirectory),
                    DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME, MalletCrfStringOutcomeDataWriter.class);
            AnalysisEngine[] aes = createPreProcessingAEs(2);
            aes[aes.length - 2] = goldAnnotator;
            aes[aes.length - 1] = nerEngine;
            SimplePipeline.runPipeline(reader, aes);
            Train.main(new File(modelDirectory));
        } catch (Exception e) {
            logger.throwing(e);
            throw new RuntimeException(e);
        }
    }

    private static void trainClearTkMalletCombined(String dataDirectory, String modelDirectory, String extraOpt) {
        CombinedCorpusGoldCollectionReader.isTraining = true;
        try {
            TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription();
            CollectionReader reader = CollectionReaderFactory.createReader(CombinedCorpusGoldCollectionReader.class,
                    typeSystemDescription, CombinedCorpusGoldCollectionReader.INPUT_DIRECTORY_PARAMETER, dataDirectory,
                    CombinedCorpusGoldCollectionReader.INPUT_EXTENSION_PARAM, "xml",
                    CombinedCorpusGoldCollectionReader.RECURSIVE_PARAMETER, false,
                    FileSystemCollectionReader.FILE_LIMIT, 1000000, FileSystemCollectionReader.FILE_SIZE_LIMIT, 100000);
            AnalysisEngine nerEngine = AnalysisEngineFactory.createEngine(NamedEntityChunker.class,
                    CleartkSequenceAnnotator.PARAM_IS_TRAINING, true, NamedEntityChunker.BROWN_CLUSTERS_FILENAME, BROWN_FILE,
                    NamedEntityChunker.BROWN_CUTOFF, BROWN_CUTOFF, NamedEntityChunker.OPTION, extraOpt,
                    DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, new File(modelDirectory),
                    DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME, MalletCrfStringOutcomeDataWriter.class);
            AnalysisEngine[] aes = createPreProcessingAEs(1);
            aes[aes.length - 1] = nerEngine;
            SimplePipeline.runPipeline(reader, aes);
            Train.main(new File(modelDirectory));
        } catch (Exception e) {
            logger.throwing(e);
            throw new RuntimeException(e);
        }
    }

    private static void trainClearTkMalletMusc(String dataDirectory, String modelDirectory, String extraOpt) {
        MuscGoldCollectionReader.isTraining = true;
        logger.debug("Trying {}", dataDirectory);
        try {
            TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription();
            CollectionReader reader = CollectionReaderFactory.createReader(MuscGoldCollectionReader.class,
                    typeSystemDescription, MuscGoldCollectionReader.INPUT_DIRECTORY_PARAMETER, dataDirectory,
                    modelDirectory, MuscGoldCollectionReader.RECURSIVE_PARAMETER, false,
                    FileSystemCollectionReader.FILE_LIMIT, 1000000, FileSystemCollectionReader.FILE_SIZE_LIMIT, 100000);
            AnalysisEngine nerEngine = AnalysisEngineFactory.createEngine(NamedEntityChunker.class,
                    CleartkSequenceAnnotator.PARAM_IS_TRAINING, true, NamedEntityChunker.BROWN_CLUSTERS_FILENAME, BROWN_FILE,
                    NamedEntityChunker.BROWN_CUTOFF, BROWN_CUTOFF, NamedEntityChunker.OPTION, extraOpt,
                    DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, new File(modelDirectory),
                    DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME, MalletCrfStringOutcomeDataWriter.class);
            AnalysisEngine[] aes = createPreProcessingAEs(1);
            aes[aes.length - 1] = nerEngine;
            SimplePipeline.runPipeline(reader, aes);
            Train.main(new File(modelDirectory));
        } catch (Exception e) {
            logger.throwing(e);
            throw new RuntimeException(e);
        }
    }

    // input folder should have .xmi files with preprocessing already done, this does goldAnnotator and NER (with feature extractions)
    private static void trainClearTkMalletFromXmi(String xmiDirectory, String modelDirectory, String extraOpt) { // , String w2vModel) {
        try {
            CollectionReader reader = CollectionReaderFactory.createReader(XmlCollectionReaderClearTkTraining.class,
                    XmlCollectionReaderClearTkTraining.PARAM_INPUTDIR, xmiDirectory, XmlCollectionReaderClearTkTraining.PARAM_FAILUNKNOWN,
                    "true");
            AnalysisEngine goldAnnotator = AnalysisEngineFactory.createEngine(GoldAnnotator.class);
            AnalysisEngine nerEngine = AnalysisEngineFactory.createEngine(NamedEntityChunker.class,
                    CleartkSequenceAnnotator.PARAM_IS_TRAINING, true, NamedEntityChunker.BROWN_CLUSTERS_FILENAME, BROWN_FILE,
                    NamedEntityChunker.BROWN_CUTOFF, BROWN_CUTOFF, NamedEntityChunker.OPTION, extraOpt,
                    DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, new File(modelDirectory),
                    DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME, MalletCrfStringOutcomeDataWriter.class);

            SimplePipeline.runPipeline(reader, goldAnnotator, nerEngine);
            Train.main(new File(modelDirectory));
        } catch (Exception e) {
            logger.throwing(e);
            throw new RuntimeException(e);
        }
    }

    // runs on text files and creates .xmi and .result (human readable NER entries) files
    private static void testClearTkMallet(String testDirectory, String modelFile, String outputDirectory, String extraOpt) { // , String w2vModel) {
        try {
            System.out.println(
                    "testing: " + testDirectory + " on model " + modelFile + " option: " + extraOpt + " writing to " + outputDirectory);
            CollectionReader reader = UriCollectionReader.getCollectionReaderFromDirectory(new File(testDirectory));

            AnalysisEngine nerEngine = AnalysisEngineFactory.createEngine(NamedEntityChunker.class, // DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
                    CleartkSequenceAnnotator.PARAM_IS_TRAINING, false, NamedEntityChunker.BROWN_CLUSTERS_FILENAME, BROWN_FILE,
                    NamedEntityChunker.BROWN_CUTOFF, BROWN_CUTOFF, NamedEntityChunker.OPTION, extraOpt,
                    // NamedEntityChunker.WORD2VEC_HASHMAP_MODEL_FILENAME, w2vModel,
                    GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, new File(modelFile));
            AnalysisEngine printer = AnalysisEngineFactory.createEngine(WriteCrfXmi.class, WriteCrfXmi.PARAM_OUTPUT_FOLDER,
                    outputDirectory);
            AnalysisEngine[] aes = createPreProcessingAEs(2);
            aes[aes.length - 2] = nerEngine;
            aes[aes.length - 1] = printer;
            SimplePipeline.runPipeline(reader, aes);
        } catch (Exception e) {
            logger.throwing(e);
            throw new RuntimeException(e);
        }
    }

    private static void testMalletCrf(String testDirectory, String modelFile, String outputDirectory) {
        try {
            System.out.println(
                    "testing: " + testDirectory + " on model " + modelFile + " writing to " + outputDirectory);
            TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription();
            GeneralCollectionReader reader = (GeneralCollectionReader) CollectionReaderFactory.createReader(FileSystemCollectionReader.class,
                    typeSystemDescription, FileSystemCollectionReader.INPUT_DIRECTORY_PARAMETER, testDirectory,
                    FileSystemCollectionReader.RECURSIVE_PARAMETER, false);
            AnalysisEngine openNlpAnnotator = AnalysisEngineFactory.createEngine(OpenNlpAnnotator.class);
            // AnalysisEngine DictionaryAnnotator = AnalysisEngineFactory.createEngine(DictionaryAnnotator.class);
            AnalysisEngine nerEngine = AnalysisEngineFactory.createEngine(NamedEntityChunker.class, // DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
                    CleartkSequenceAnnotator.PARAM_IS_TRAINING, false, NamedEntityChunker.BROWN_CLUSTERS_FILENAME, BROWN_FILE,
                    NamedEntityChunker.BROWN_CUTOFF, BROWN_CUTOFF,
                    GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, new File(modelFile));
            AnalysisEngine printer = AnalysisEngineFactory.createEngine(WriteCrfXmi.class, WriteCrfXmi.PARAM_OUTPUT_FOLDER,
                    outputDirectory);
            AnalysisEngine[] aes = {openNlpAnnotator, nerEngine, printer};
            SimplePipeline.runPipeline(reader, aes);
        } catch (Exception e) {
            logger.throwing(e);
            throw new RuntimeException(e);
        }
    }

    // runs on xmi files that have preprocessing done and creates .xmi and .result (human readable NER entries) files
    private static void testClearTkMalletXmi(String testDirectory, String modelFile, String outputDirectory, String extraOpt) { // , String w2vModel) {
        try {
            CollectionReader reader = CollectionReaderFactory.createReader(XmlCollectionReaderClearTkTraining.class,
                    XmlCollectionReaderClearTkTraining.PARAM_INPUTDIR, testDirectory, XmlCollectionReaderClearTkTraining.PARAM_FAILUNKNOWN,
                    "true");
            AnalysisEngine nerEngine = AnalysisEngineFactory.createEngine(NamedEntityChunker.class,
                    CleartkSequenceAnnotator.PARAM_IS_TRAINING, false, NamedEntityChunker.BROWN_CLUSTERS_FILENAME, BROWN_FILE,
                    NamedEntityChunker.BROWN_CUTOFF, BROWN_CUTOFF, NamedEntityChunker.OPTION, extraOpt,
                    // NamedEntityChunker.WORD2VEC_HASHMAP_MODEL_FILENAME, w2vModel,
                    GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, new File(modelFile));
            AnalysisEngine printer = AnalysisEngineFactory.createEngine(WriteCrfXmi.class, WriteCrfXmi.PARAM_OUTPUT_FOLDER,
                    outputDirectory);
            SimplePipeline.runPipeline(reader, nerEngine, printer);
        } catch (Exception e) {
            logger.throwing(e);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        final String message = "mvn exec:java@clearTk -Dexec.args=\"-c command -i inputDir ...\"";
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addRequiredOption("c", "command", true, "one of: traincombinedxml createXmi trainText trainXmi test testXmi trainMuscText writeAnnotsTexts");// testXmi?
        options.addRequiredOption("i", "inputDir", true, "Input directory from which to read training or testing data files");
        options.addOption("m", "modelDir", true, "Directory of the model for training, name of model file in testing");
        options.addOption("o", "outDir", true, "Directory for output for xmi");
        options.addOption("h", "help", false, "Prints this message");
        options.addOption("e", "extra", true, "Extra parameter given to NamedEntityChunker");
        // options.addOption("w", "w2vModel", true, "Word2Vec hashMap serialized object file");

        CommandLine commandLine = null;
        HelpFormatter helper = new HelpFormatter();
        try {
            commandLine = parser.parse(options, args);
        } catch (MissingOptionException e) {
            logger.error("Missed some command line options");
            logger.throwing(e);
            helper.printHelp(message, options);
            System.exit(-1);
        }

        if (commandLine.hasOption("help")) {
            helper.printHelp(message, options);
            System.exit(0);
        }
        String command = commandLine.getOptionValue("command").toLowerCase();
        String inputDir = commandLine.getOptionValue("inputDir");
        String modelDir = null;
        String extraOpt = "0";
        if (commandLine.hasOption('e')) {
            extraOpt = commandLine.getOptionValue('e');
        }
        // String w2vModel = null;

        if (command.contains("train") || command.contains("test")) { // any type of train or test uses modelDir
            if (!commandLine.hasOption("modelDir")) {
                helper.printHelp(message, options);
                System.exit(-1);
            } else {
                modelDir = commandLine.getOptionValue("modelDir");
                if (command.contains("train")) {
                    Files.createDirectories(Paths.get(modelDir));
                }
            }
            // if (!commandLine.hasOption("w2vModel")) {
            // helper.printHelp(message, options);
            // System.exit(-1);
            // } else {
            // w2vModel = commandLine.getOptionValue("w2vModel");
            // }
        }

        String outDir = null;
        if ("createxmi".equals(command) || command.contains("test")) { // any type of test or createXmi uses outDir
            if (!commandLine.hasOption("outDir")) {
                helper.printHelp(message, options);
                System.exit(-1);
            } else {
                outDir = commandLine.getOptionValue("outDir");
                Files.createDirectories(Paths.get("outDir"));// it checks if outDir already exists
            }
        }

        // createXmi trainText trainXmi test testXmi, trainMuscText
        if ("traintext".equals(command)) {
            trainClearTkMallet(inputDir, modelDir, extraOpt);
        } else if ("trainmusctext".equals(command)) {
            trainClearTkMalletMusc(inputDir, modelDir, extraOpt);
        } else if ("traincombinedxml".equals(command)) {
            trainClearTkMalletCombined(inputDir, modelDir, extraOpt);
        } else if ("trainxmi".equals(command) || "trainxml".equals(command)) {
            trainClearTkMalletFromXmi(inputDir, modelDir, extraOpt);
        } else if ("createxmi".equals(command)) {
            createXmi(inputDir, outDir);
        } else if ("writeannotstexts".equals(command)) {
            String annotsDir = commandLine.getOptionValue("modelDir");
            outDir = commandLine.getOptionValue("outDir");
            writeAnnots(inputDir, annotsDir, outDir);
        } else if ("test".equals(command)) {
            testClearTkMallet(inputDir, modelDir, outDir, extraOpt);
        } else if ("testxmi".equals(command)) {
            testClearTkMalletXmi(inputDir, modelDir, outDir, extraOpt);
        } else if ("maketextcorpus".equals(command)) {
            outDir = commandLine.getOptionValue("outDir");
            createTextCorpus(inputDir, outDir);
        } else if ("makebrown".equals(command)) {
            // outDir = commandLine.getOptionValue("outDir");
            // createBrownInputFile(inputDir, outDir);
            createBrownHashSet("/Users/garyunderwood/NER/brown-cluster/brown-unified-4-1000.txt");
        } else if ("testmallet".equals(command)) {
            testMalletCrf(inputDir, modelDir, outDir);

        } else {
            System.out.println("command " + command + " is invalid.");
            helper.printHelp(message, options);
            System.exit(-1);
        }
    }

    private static void writeAnnots(String dataDirectory, String annotsDir, String textsDir) {
        CombinedCorpusGoldCollectionReader.isTraining = true;
        try {
            TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription();
            CollectionReader reader = CollectionReaderFactory.createReader(CombinedCorpusGoldCollectionReader.class,
                    typeSystemDescription, CombinedCorpusGoldCollectionReader.INPUT_DIRECTORY_PARAMETER, dataDirectory,
                    CombinedCorpusGoldCollectionReader.RECURSIVE_PARAMETER, false,
                    FileSystemCollectionReader.FILE_LIMIT, 1000000, FileSystemCollectionReader.FILE_SIZE_LIMIT, 100000);
            AnalysisEngine annotsWriter = AnalysisEngineFactory.createEngine(TestDeidPipeline.CasConsumerWriteAnnotation.class,
                    TestDeidPipeline.CasConsumerWriteAnnotation.PARAM_OUTPUT_FOLDER, annotsDir,
                    TestDeidPipeline.CasConsumerWriteAnnotation.PRINT_ORIGINAL_TEXT, false);
            //AnalysisEngine textWriter = AnalysisEngineFactory.createEngine(WriteDocumentText.class, WriteDocumentText.PARAM_OUTPUT_FOLDER, textsDir);
            AnalysisEngine[] aes = {annotsWriter};
            SimplePipeline.runPipeline(reader, aes);
        } catch (Exception e) {
            logger.throwing(e);
            throw new RuntimeException(e);
        }

    }

    private static void createTextCorpus(String inputDirectory, String outputDirectory) {
        CombinedCorpusGoldCollectionReader.isTraining = true;//don't need to make PII annotations
        try {
            TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription();
            CollectionReader reader = CollectionReaderFactory.createReader(CombinedCorpusGoldCollectionReader.class,
                    typeSystemDescription, CombinedCorpusGoldCollectionReader.INPUT_DIRECTORY_PARAMETER, inputDirectory,
                    CombinedCorpusGoldCollectionReader.RECURSIVE_PARAMETER, false,
                    FileSystemCollectionReader.FILE_LIMIT, 1000000, FileSystemCollectionReader.FILE_SIZE_LIMIT, 1000000);
            AnalysisEngine textWriter = AnalysisEngineFactory.createEngine(WriteDocumentText.class, WriteDocumentText.PARAM_OUTPUT_FOLDER, outputDirectory);
            AnalysisEngine[] aes = {textWriter};
            SimplePipeline.runPipeline(reader, aes);
        } catch (Exception e) {
            logger.throwing(e);
            throw new RuntimeException(e);
        }
    }

    public static void createBrownInputFile(String dataDirectory, String outputDirectory) {
        try {
            TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription();
            GeneralCollectionReader reader = (GeneralCollectionReader) CollectionReaderFactory.createReader(FileSystemCollectionReader.class,
                    typeSystemDescription, FileSystemCollectionReader.INPUT_DIRECTORY_PARAMETER, dataDirectory,
                    FileSystemCollectionReader.RECURSIVE_PARAMETER, false);
            AnalysisEngine openNlpAnnotator = AnalysisEngineFactory.createEngine(OpenNlpAnnotator.class);
            AnalysisEngine tokenWriter = AnalysisEngineFactory.createEngine(WriteBrownInputFile.class, WriteBrownInputFile.PARAM_OUTPUT_FOLDER, outputDirectory);
            AnalysisEngine[] aes = {openNlpAnnotator, tokenWriter};
            SimplePipeline.runPipeline(reader, aes);
        } catch (UIMAException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void createBrownHashSet(String inputTextFile) {// never run by end user
        String outputSerialFile = inputTextFile.replace(".txt", ".ser");
        HashMap<String, String> clusters = new HashMap<>();
        try (FileOutputStream fos = new FileOutputStream(outputSerialFile); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            List<String> text = Files.readAllLines(Paths.get(inputTextFile));
            for (String line : text) {
                String[] parts = line.split("\\s+");
                clusters.put(parts[1], parts[0]); // for liang which is bitstring then token
            }
            oos.writeObject(clusters);
        } catch (IOException e) {
            e.printStackTrace();
        }
        testBrownHashSet(outputSerialFile);
    }

    private static void testBrownHashSet(String serialFile) {
        // just for testing that serialized HashMap was created correctly. never run by end user
        try (FileInputStream fis = new FileInputStream(serialFile); ObjectInputStream ois = new ObjectInputStream(fis)) {
            @SuppressWarnings("unchecked") // for casting Brown Cluster's serialized object
                    HashMap<String, String> clusters = (HashMap<String, String>) ois.readObject();
            System.out.println("patient: " + clusters.get("patient"));
            System.out.println("drug: " + clusters.get("drug"));
        } catch (IOException | ClassNotFoundException e) {
            logger.throwing(e);
            e.printStackTrace();
        }
    }

    public static class WriteDocumentText extends JCasAnnotator_ImplBase {
        static final String PARAM_OUTPUT_FOLDER = "outputFolder";
        @ConfigurationParameter(name = PARAM_OUTPUT_FOLDER)
        private String outputFolder;

        @Override
        public void initialize(UimaContext context) throws ResourceInitializationException {
            super.initialize(context);
            outputFolder = DeidPipeline.addTrailingSlash(outputFolder);
            new File(outputFolder).mkdirs();
            logger.debug("Writing Text to {}", outputFolder);
        }

        @Override
        public void process(JCas jCas) throws AnalysisEngineProcessException {
            String textFile = Utilities.getFileName(jCas);
            String outputFileName;
            if (textFile.endsWith(".xml")) {
                outputFileName = outputFolder + textFile.replace(".xml", ".txt");

            } else {
                outputFileName = outputFolder + textFile + ".txt";
            }
            logger.debug("outputting to file: {}", outputFileName);
            try (FileWriter fWriter = new FileWriter(outputFileName);
                 BufferedWriter writer = new BufferedWriter(fWriter)) {
                writer.write(jCas.getDocumentText());
            } catch (IOException e) {
                logger.throwing(e);
                throw new AnalysisEngineProcessException(e);
            }
        }
    }


    /**
     * class gets filename from URI, creates 2 output files in outputFolder:
     * <p>
     * filename.results with NamedEntityMentions printed for human viewing filename.xmi as Serialized cas
     *
     * @author garyunderwood
     */
    public static class WriteCrfXmi extends JCasAnnotator_ImplBase {
        public static final String PARAM_OUTPUT_FOLDER = "outputFolder";
        @ConfigurationParameter(name = PARAM_OUTPUT_FOLDER)
        private String outputFolder;

        @Override
        public void initialize(UimaContext context) throws ResourceInitializationException {
            super.initialize(context);
            outputFolder = DeidPipeline.addTrailingSlash(outputFolder);
            new File(outputFolder).mkdirs();
        }

        @Override
        public void process(JCas jCas) throws AnalysisEngineProcessException {
            String textFile;
            try {
                java.net.URI uriName = ViewUriUtil.getURI(jCas);
                textFile = uriName.getPath();
            } catch (Exception e) {
                textFile = Utilities.getFileName(jCas);
            }
            String outputFileName = outputFolder;
            if (textFile.endsWith(".txt") || textFile.endsWith(".xml")) {
                outputFileName += textFile.replaceAll("\\.[a-z]+", ".results");
            } else {
                outputFileName += textFile + ".results";
            }
            logger.debug("outputting: {}", outputFileName);
            try (FileWriter fWriter = new FileWriter(outputFileName);
                 BufferedWriter writer = new BufferedWriter(fWriter)) {
                for (PiiAnnotation ann : jCas.getAnnotationIndex(PiiAnnotation.class)) {
                    writer.write(String.format("Pii: %5d - %5d (%8s): %s%n", ann.getBegin(), ann.getEnd(), ann.getPiiSubtype(),
                            ann.getCoveredText().replaceAll("[\\r\\n]+", "*NL*")));
                }

                if (textFile.endsWith(".txt") || textFile.endsWith(".xml")) {
                    outputFileName = textFile.replaceAll("\\.[a-z]+", ".xml");
                } else {
                    outputFileName = textFile + ".xml";
                }
                Serializer.SerializeToXmi(jCas, outputFolder + outputFileName, true);
            } catch (Exception e) {
                logger.throwing(e);
                throw new AnalysisEngineProcessException(e);
            }
        }
    }


    public static class WriteBrownInputFile extends JCasAnnotator_ImplBase {
        public static final String PARAM_OUTPUT_FOLDER = "outputFolder";
        private static final Pattern letters = Pattern.compile("[a-zA-Z]+");
        @ConfigurationParameter(name = PARAM_OUTPUT_FOLDER)
        private String outputFolder;

        @Override
        public void initialize(UimaContext context) throws ResourceInitializationException {
            super.initialize(context);
            outputFolder = DeidPipeline.addTrailingSlash(outputFolder);
            new File(outputFolder).mkdirs();
        }

        @Override
        public void process(JCas jCas) throws AnalysisEngineProcessException {
            File textFile = new File(Utilities.getFileName(jCas));
            String outputFileName = outputFolder + textFile.getName().replaceAll("\\.[a-z]+", ".results");
            logger.debug("outputting: {}", outputFileName);
            try (FileWriter fWriter = new FileWriter(outputFileName);
                 BufferedWriter writer = new BufferedWriter(fWriter)) {
                for (BaseToken ann : jCas.getAnnotationIndex(BaseToken.class)) {
                    if (letters.matcher(ann.getCoveredText()).matches()) {// (!ann.getPartOfSpeech().equals("punctuation")) {
                        writer.write(ann.getCoveredText() + " ");
                    }
                }
            } catch (Exception e) {
                logger.throwing(e);
                throw new AnalysisEngineProcessException(e);
            }
        }
    }

    public static class MascTextFileFilter implements IOFileFilter {
        @Override
        public boolean accept(File file) {
            return file.getPath().endsWith(".txt");
        }

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".txt");
        }
    }
}
