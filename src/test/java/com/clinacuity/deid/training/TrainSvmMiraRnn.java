
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

import com.clinacuity.clinideid.message.DeidLevel;
import com.clinacuity.deid.ae.FeatureAnnotator;
import com.clinacuity.deid.ae.OpenNlpAnnotator;
import com.clinacuity.deid.readers.FileSystemCollectionReader;
import com.clinacuity.deid.readers.GeneralCollectionReader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;

public class TrainSvmMiraRnn {
    private static final Logger logger = LogManager.getLogger();
    private static String inputDir;
    private static String svmMiraOutputFile;
    private static String rnnOutputFile;

    public static void main(String[] args) throws Exception {
        //exec not setup in pom
        final String message = "mvn exec:java@?? -Dexec.args=\"-c svmMiraOutputFile -r rnnOutputFile -i inputDir ...\"";
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addRequiredOption("i", "inputDir", true, "Input directory from which to read training or testing data files");
        options.addRequiredOption("c", "svmMiraOutputFile", true, "name of output file of feature vectors for mira and svm");
        options.addRequiredOption("r", "rnnOutputFile", true, "name of output file of rnn feature vectors");
        options.addOption("m", "musc", false, "Use MUSC gold reader instead of Combined");
        options.addOption("h", "help", false, "Prints this message");

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
        inputDir = commandLine.getOptionValue("inputDir");
        svmMiraOutputFile = commandLine.getOptionValue("svmMiraOutputFile");
        rnnOutputFile = commandLine.getOptionValue("rnnOutputFile");
        if (commandLine.hasOption("musc")) {
            trainSvmMusc();
        } else {
            trainSvm();
        }
    }

    private static void trainSvm() {
        CombinedCorpusGoldCollectionReader.isTraining = true;
        try {
            TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription();
            CollectionReader reader = CollectionReaderFactory.createReader(CombinedCorpusGoldCollectionReader.class,
                    typeSystemDescription, CombinedCorpusGoldCollectionReader.INPUT_DIRECTORY_PARAMETER, inputDir,
                    CombinedCorpusGoldCollectionReader.RECURSIVE_PARAMETER, false, CombinedCorpusGoldCollectionReader.INPUT_EXTENSION_PARAM, "xml",
                    FileSystemCollectionReader.FILE_SIZE_LIMIT, 1000000, FileSystemCollectionReader.FILE_LIMIT, 1000000,
                    GeneralCollectionReader.DEID_LEVEL, DeidLevel.defaultLevel,
                    GeneralCollectionReader.OUTPUT_CLEAN, false,
                    GeneralCollectionReader.OUTPUT_GENERAL_TAG, false,
                    GeneralCollectionReader.OUTPUT_CATEGORY_TAG, false,
                    GeneralCollectionReader.OUTPUT_PII, false,
                    GeneralCollectionReader.OUTPUT_RESYNTHESIS, false,
                    GeneralCollectionReader.OUTPUT_RAW, false);
//            AnalysisEngine rawxmi =AnalysisEngineFactory.createEngine(RunTrainClearTkMallet.WriteCrfXmi.class,
//                    RunTrainClearTkMallet.WriteCrfXmi.PARAM_OUTPUT_FOLDER, svmMiraOutputFile);

            AnalysisEngine[] aes = createPreProcessingAEs();
            SimplePipeline.runPipeline(reader, aes);
        } catch (Exception e) {
            logger.throwing(e);
            throw new RuntimeException(e);
        }
    }


    private static void trainSvmMusc() {
        MuscGoldCollectionReader.isTraining = true;
        try {
            TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription();
            CollectionReader reader = CollectionReaderFactory.createReader(MuscGoldCollectionReader.class,
                    typeSystemDescription, MuscGoldCollectionReader.INPUT_DIRECTORY_PARAMETER, inputDir,
                    MuscGoldCollectionReader.RECURSIVE_PARAMETER, false,
                    FileSystemCollectionReader.FILE_LIMIT, 1000000, FileSystemCollectionReader.FILE_SIZE_LIMIT, 100000,
                    GeneralCollectionReader.DEID_LEVEL, DeidLevel.defaultLevel,
                    GeneralCollectionReader.OUTPUT_CLEAN, false,
                    GeneralCollectionReader.OUTPUT_GENERAL_TAG, false,
                    GeneralCollectionReader.OUTPUT_CATEGORY_TAG, false,
                    GeneralCollectionReader.OUTPUT_PII, false,
                    GeneralCollectionReader.OUTPUT_RESYNTHESIS, false,
                    GeneralCollectionReader.OUTPUT_RAW, false);

//            AnalysisEngine rawxmi =AnalysisEngineFactory.createEngine(RunTrainClearTkMallet.WriteCrfXmi.class,
//                    RunTrainClearTkMallet.WriteCrfXmi.PARAM_OUTPUT_FOLDER, svmMiraOutputFile);

            AnalysisEngine[] aes = createPreProcessingAEs();
            SimplePipeline.runPipeline(reader, aes);
        } catch (Exception e) {
            logger.throwing(e);
            throw new RuntimeException(e);
        }
    }

    private static AnalysisEngine[] createPreProcessingAEs() throws ResourceInitializationException {
        AnalysisEngine openNlpAnnotator = AnalysisEngineFactory.createEngine(OpenNlpAnnotator.class);
        //AnalysisEngine dictionaryAnnotator = AnalysisEngineFactory.createEngine(DictionaryAnnotator.class);
        //dictionary not currently used by SVM, Mira, or RNN (only by CRF which doesn't use this for training)

        AnalysisEngine featureGenerator = AnalysisEngineFactory.createEngine(FeatureAnnotator.class,
                FeatureAnnotator.WORD_VECTOR_CL_FILE_NAME, "data/i2b2/musc/enwik9_deid_cl.txt");
        AnalysisEngine trainFvGenerator = AnalysisEngineFactory.createEngine(TrainFvGenerator.class,
                TrainFvGenerator.SVM_MIRA_FV_FILENAME, svmMiraOutputFile,
                TrainFvGenerator.RNN_FV_FILENAME, rnnOutputFile);

        return new AnalysisEngine[]{openNlpAnnotator, /*dictionaryAnnotator,*/ featureGenerator, trainFvGenerator};
    }
}
