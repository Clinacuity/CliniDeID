
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

package com.clinacuity.deid.mains;

import com.clinacuity.clinideid.message.DeidLevel;
import com.clinacuity.deid.ae.DeidLevelCleaner;
import com.clinacuity.deid.ae.DictionaryAnnotator;
import com.clinacuity.deid.ae.EnsembleAnnotator;
import com.clinacuity.deid.ae.EnsemblePartialAnnotator;
import com.clinacuity.deid.ae.FeatureAnnotator;
import com.clinacuity.deid.ae.OpenNlpAnnotator;
import com.clinacuity.deid.ae.PostPiiProcessing;
import com.clinacuity.deid.ae.VoteAnnotator;
import com.clinacuity.deid.gui.App;
import com.clinacuity.deid.gui.DeidPipelineTask;
import com.clinacuity.deid.gui.DeidRunnerController;
import com.clinacuity.deid.gui.modals.WarningModal;
import com.clinacuity.deid.outputAnnotators.DocumentListAnnotator;
import com.clinacuity.deid.outputAnnotators.PiiTagging;
import com.clinacuity.deid.outputAnnotators.PiiWriterAnnotator;
import com.clinacuity.deid.outputAnnotators.RawXmiOutputAnnotator;
import com.clinacuity.deid.outputAnnotators.resynthesis.ResynthesisAnnotator;
import com.clinacuity.deid.readers.DbCollectionReader;
import com.clinacuity.deid.readers.FileSystemCollectionReader;
import com.clinacuity.deid.readers.GeneralCollectionReader;
import com.clinacuity.deid.service.DeidProperties;
import com.clinacuity.deid.util.ConnectionProperties;
import com.clinacuity.deid.util.PiiOptions;
import com.clinacuity.deid.util.SimpleTimer;
import com.clinacuity.deid.util.Util;
import com.clinacuity.deid.util.Utilities;
import javafx.application.Platform;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.ml.jar.GenericJarClassifierFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.apache.uima.fit.factory.JCasFactory.createJCas;

public class DeidPipeline {
    public static final Pattern PUNCTUATION_MATCH = Pattern.compile("[^a-zA-Z0-9]+");
//    public static final String LICENSE_FILE = "LICENSE.KEY";
    public static final String VERSION = "1.9.0";
    public static final String DATA_PATH = "data";
    public static final Level PII_LOG = Level.forName("PII", 350);
    //public paths for use in tests
    public static final String RNN_MODEL_FILE = DATA_PATH + "/models/rnn/rnn-U3-FullSplit.h5";
    public static final int BROWN_CUTOFF = 7;
    public static final String BROWN_FILE = DATA_PATH + "/models/malletCrf/brown-AllI2B2-4-1000.ser";
    public static final String SVM_MODEL_FILE = DATA_PATH + "/models/svm/svmModel-U3-FullSplit";
    public static final String SVM_FEATURE_INDEX_FILE = DATA_PATH + "/models/svm/svmMap-U3-FullSplit";
    public static final String SVM_LABEL_FILE = DATA_PATH + "/i2b2/bioDeid_musc_split.txt";
    public static final String SVM_TEMPLATE_FILE = DATA_PATH + "/i2b2/template_crf_deid_musc";
    public static final String WORD_VECTOR_CL_FILE = DATA_PATH + "/i2b2/musc/enwik9_deid_cl.txt";
    public static final String REGEX_CONCEPTS_FILE = DATA_PATH + "/regex/ensembleRegex.xml";
    public static final String CRF_MODEL_FILE = DATA_PATH + "/models/malletCrf/crf-U3-FullSplit.jar";
    public static final String MIRA_MODEL_FILE = DATA_PATH + "/models/mira/mira-U3-FullSplit";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String OS_NAME = getOsName();
    public static final String SCRIPT_EXTENSION = DeidPipeline.getScriptExtension();
    private static final JAXBContext JAXB_CONTEXT = makeJaxbContext();//thread safe
//    private static final int UPDATE_LICENSE_FREQUENCY = 10;  //must be non-zero, update license every time fileCounter is multiple of this
    public static Process rnn = null;
    protected static Set<String> excludes = new HashSet<>();
    protected static DeidPipelineTask deidPipelineTask;//would be good to be non-static, would require further separation from GUI
    protected static int portRnn = 4444;  //4444 is used by service/demo
    protected PiiOptions piiOptions;
    protected GeneralCollectionReader reader;
    protected JCas jCas;
//    protected DeidProperties properties = null;
    protected String outputDir;
    protected String inputDir;
    //TODO: consider Map of arg to value for output options and for DB input values, Properties like class?
    protected DeidLevel deidLevel;
    protected boolean resynthesisSelected = false;
    protected boolean resynthesisMapSelected = false;
    protected boolean piiTaggingSelected = false;
    protected boolean piiTaggingCategorySelected = false;
    protected boolean piiWriterSelected = false;
    protected boolean rawXmiOutputSelected = false;
    protected boolean cleanXmiOutputSelected = false;
    protected AnalysisEngine[] analysisEngines;
    protected boolean fileOutputSelected;
    protected boolean dbOutputSelected;
    private ArrayList<String> failingFileNames = new ArrayList<>();
    private String timeStart;
    private SimpleTimer timer = new SimpleTimer();
    private List<AnalysisEngine> preCreatedEngines;
    private boolean fileInputToggle;
    private boolean dbInputToggle;
    private boolean inputText;
    private boolean inputCda;
    private String dbName;
    private String dbColumnId;
    private String dbColumnText;
    private String dbTableName;
    private String dbServer;
    private String dbPort;
    private String dbms;
    private String dbSchema;
    private String dbUsername;
    private String dbPassword;
    private String dbQuery;
    private int preCreatedEnginesSize; //size of list w/o output annotators added
//    private boolean licenseProcessed = false;
//    private String license = null;

    public DeidPipeline(DeidPipelineTask _deidPipelineTask, String inputDirP, String outputDirP) {
        if (inputDirP != null) {
            inputDir = addTrailingSlash(inputDirP);
        }
        outputDir = addTrailingSlash(outputDirP);
        deidPipelineTask = _deidPipelineTask;
    }

    protected DeidPipeline() {
    }

    private static JAXBContext makeJaxbContext() {
        try {
            return JAXBContext.newInstance(PiiOptions.class);
        } catch (JAXBException e) {
            LOGGER.throwing(e);
            throw new RuntimeException("Failed to make JAXBContext instance", e);
        }
    }

    public static Set<String> getExcludes() {
        return excludes;
    }

    public static boolean setExcludes(String command) {
        boolean ok = true;
        Set<String> options = Set.of("rnn", "svm", "mira", "crf", "opennlp", "regex");
        String[] toExclude = command.toLowerCase().split("[-,]");
        for (String ae : toExclude) {
            if (options.contains(ae)) {
                excludes.add(ae);
            } else {
                System.out.println("The exclude option of " + ae + " is not valid, it should be OPENNLP, RNN, SVM, MIRA, or CRF");
                ok = false;
            }
        }
        return ok;
    }

    public static String getOs() {
        return OS_NAME;
    }

    private static String getOsName() {
        String osProperty = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((osProperty.contains("mac")) || (osProperty.contains("darwin"))) {
            return "mac";
        } else if (osProperty.contains("win")) {
            return "windows"; //does version matter?
        } else if (osProperty.contains("nux")) {
            return "unix";
        } else {
            return "other";
        }
    }

    @SuppressWarnings("unused")
    protected static void createEngineDescription(String descriptorTargetFile, Class<? extends AnalysisComponent> c) {
        try {
            AnalysisEngineDescription description = AnalysisEngineFactory.createEngineDescription(c);
            java.io.FileOutputStream stream = new java.io.FileOutputStream(new java.io.File(descriptorTargetFile));
            description.toXML(stream);
            stream.close();
        } catch (Exception e) {
            LOGGER.throwing(e);
        }

        LOGGER.error("The application will now stop -- please see the descriptor file");
        System.exit(0);
    }

    protected static Options makeCliOptions() {
        Options options = new Options();//so TestDeidPipeline can use them
        OptionGroup inputSource = new OptionGroup();
        inputSource.addOption(new Option("idb", "inputDatabase", false, "Input source is a database."));
        inputSource.addOption(new Option("if", "inputFile", false, "Input source is directory of text files."));
        inputSource.setRequired(true);
        options.addOptionGroup(inputSource);

        options.addOption("odb", "outputDatabase", false, "If present, outputs written to CliniDeID database.");
        options.addOption("of", "outputFile", false, "If present, outputs written to files in output directory.");
        options.addRequiredOption("od", "outputDir", true, "Output directory for processed file list, annotations, and notes (if outputFile chosen).");
        options.addOption("h", "help", false, "Prints this message.");

        options.addOption("id", "inputDir", true, "Input directory from which to read the files.");
        options.addOption("t", "outputTypes", true, "resynthesis, generaltag, categorytag, detectedPII, complete, filtered, map, or all.  Combine with ',' (no spaces) as in 'resynthesis,filtered,generaltag'. Default is resynthesis.");
        options.addOption("cda", "cda", false, "Use HL7 CDA XML format for input and output");
        options.addOption("txt", "text", false, "Use plain text (default) format for input and output");

        options.addOption("l", "level", true, "Level of deidentification " + Arrays.toString(DeidLevel.values()) + ", default is " + DeidLevel.defaultLevel.toString() + ".");
        options.addOption("x", "exclude", true, "Annotators to exclude, one or more of RNN MIRA CRF SVM, combine with ',' as in 'RNN,MIRA'. Default is not to exclude any.");
        options.addOption("r", "rnnPort", true, "Port number for RNN to use. Defaults to finding open port.");

        options.addOption("ds", "dbServer", true, "Server for input database");
        options.addOption("dp", "dbPort", true, "Server port for input database");
        options.addOption("dn", "dbName", true, "Database name for input");
        options.addOption("dt", "dbTableName", true, "Name of table with notes for input database");
        options.addOption("di", "dbColumnId", true, "Column name for ID for input database");
        options.addOption("dx", "dbColumnText", true, "Column name for text notes for input database");
        options.addOption("dc", "dbSchema", true, "PreQuery statement for input database");
        options.addOption("du", "dbUsername", true, "Username for input database");
        options.addOption("dp", "dbPassword", true, "Password for input database");
        options.addOption("dm", "dbms", true, "Type of input database, one of: " + ConnectionProperties.SUPPORTED_DB.toString());
        options.addOption("dq", "dbQuery", true, "Where clause for input database select statement");

        options.addOption("piiConfig", "loadPiiConfiguration", true, "Filename of pii configuration file to use. Will replace any level value given");
        options.addOption("pii", "piiOptions", true, "Individual options for pii as PiiSubtype-true|false pairs in comma separated list. See Readme for details. If level or loadPiiConfiguration is used then they are done first with these changes afterwards.");
        return options;
    }

    protected static void processBasicCliOptions(DeidPipeline pipeline, Options options, CommandLine commandLine) {
        if (commandLine.hasOption("help")) {
            HelpFormatter helper = new HelpFormatter();
            helper.printHelp(100, "runCliniDeIDcommandLine", null, options, "See Readme for more details", true);
            System.exit(0);
        }
        if (commandLine.hasOption("inputDatabase")) {
            pipeline.dbInputToggle = true;
            if (!pipeline.getDbInputs(commandLine)) {
                System.exit(1);
            }
        } else {//must be file
            pipeline.fileInputToggle = true;
            pipeline.inputDir = addTrailingSlash(commandLine.getOptionValue("inputDir"));
        }

        if (commandLine.hasOption("cda")) {
            pipeline.inputCda = true;
        } else if (commandLine.hasOption("text")) {
            pipeline.inputText = true;
        } else {//default
            pipeline.inputText = true;
        }

        if (commandLine.hasOption("outputDatabase")) {
            pipeline.dbOutputSelected = true;
            String errorMessage = ConnectionProperties.dbOutputCheck();
            if (!errorMessage.isEmpty()) {
                System.out.println(errorMessage);
                System.exit(1);
            }
        }
        if (commandLine.hasOption("outputFile")) {
            pipeline.fileOutputSelected = true;
        }
        if (!pipeline.dbOutputSelected && !pipeline.fileOutputSelected) {
            System.out.println("Must select at least one of outputFile or outputDatabase.");
            System.exit(1);
        }
        if (commandLine.hasOption("outputTypes")) {
            if (!pipeline.setOutputTypeOptions(commandLine.getOptionValue("outputTypes"))) {
                System.out.println("OutputType parameter of " + commandLine.getOptionValue("outputTypes") +
                        " is not supported. Choose from resynthesis, generaltag, categorytag, detectedPii, complete, or filtered. Join multiple options with a , as in complete,filtered");
                System.exit(1);
            }
        } else {
            pipeline.setOutputTypeOptions("resynthesis");
        }

        if (commandLine.hasOption("rnnPort")) {
            try {
                portRnn = Integer.parseInt(commandLine.getOptionValue("rnnPort"));
            } catch (NumberFormatException e) {
                System.out.println("The value given for port (" + commandLine.getOptionValue("rnnPort") + ") is not a number");
                System.exit(1);
            }
            if (portRnn < 1024 || portRnn > 65535) {
                System.out.println("The value given for port (" + portRnn + ") is out of range");
                System.exit(1);
            }
        }
        if (commandLine.hasOption("level")) {
            try {
                pipeline.deidLevel = DeidLevel.valueOf(commandLine.getOptionValue("level").toLowerCase());
            } catch (java.lang.IllegalArgumentException e) {
                String message = "";
                for (DeidLevel lev : DeidLevel.values()) {
                    message += lev.toString() + " ";
                }
                System.out.println("Deidentification level " + commandLine.getOptionValue("level").toLowerCase() + " not supported, must be one of " + message);
                System.exit(1);
            }
        } else {
            pipeline.deidLevel = DeidLevel.defaultLevel;
        }
        if (commandLine.hasOption("exclude") && !setExcludes(commandLine.getOptionValue("exclude"))) {
            System.exit(2);
        }
        else if (commandLine.hasOption("x")) {
            if (commandLine.getOptionValue("x") != null) {
                if (!setExcludes(commandLine.getOptionValue("x"))) {
                    System.exit(2);
                }
            }else if (commandLine.getArgList()!=null) {
                if (!setExcludes(commandLine.getArgList().get(0))) {
                    System.exit(2);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Path path = Paths.get("log");
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            System.err.println("Could not create log directory: " + e.toString());
            return;
        }
        CommandLineParser parser = new DefaultParser();
        Options options = makeCliOptions();
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (MissingOptionException | UnrecognizedOptionException e) {
            LOGGER.error("Missed or unrecognized command line options: {}", e.toString());
            String error = e.toString();
            int colonIndex = error.indexOf(':');
            String message;
            if (colonIndex >= 0) {
                message = error.substring(colonIndex + 2);
            } else {
                message = error.substring(0, error.indexOf('\n'));
            }
            System.out.println(message);
            HelpFormatter helper = new HelpFormatter();
            helper.printHelp(100, "runCliniDeIDcommandLine", null, options, "See Readme.txt for more details", true);
            System.exit(1);
        }

        DeidPipeline pipeline = new DeidPipeline(null, null, commandLine.getOptionValue("outputDir"));
        processBasicCliOptions(pipeline, options, commandLine);

        String errorMessage = processPiiOptions(pipeline, commandLine);
        if (!errorMessage.isEmpty()) {
            System.out.println(errorMessage);
            System.exit(2);
        }

        errorMessage += checkModelPaths();
        if (!errorMessage.isEmpty()) {
            System.out.println(errorMessage);
            System.exit(2);
        }

        System.out.println(pipeline.execute(false));
        //stop RNN
        App.cleanupNonGui();
    }

    protected static String processPiiOptions(DeidPipeline pipeline, CommandLine commandLine) {
        final Map<String, Boolean> trueFalseValues = Map.of("true", true, "t", true, "false", false, "f", false);
        StringBuilder errorMessage = new StringBuilder();
        PiiOptions piiOptions = new PiiOptions(pipeline.getDeidLevel());
        if (commandLine.hasOption("piiConfig")) {
            String filename = commandLine.getOptionValue("piiConfig");
            piiOptions = DeidPipeline.readPiiOptions(new File(filename));
            if (piiOptions == null) {
                return "Error reading PII configuration file " + filename;
            }
        }
        if (commandLine.hasOption("piiOptions")) {
            pipeline.setDeidLevel(DeidLevel.custom);
            String[] items = commandLine.getOptionValue("piiOptions").split(",");
            for (String item : items) {
                String[] keyValue = item.split("[-=]");
                if (keyValue.length == 2) {
                    if (PiiOptions.SPECIALS.contains(keyValue[0])) {
                        if (trueFalseValues.containsKey(keyValue[1].toLowerCase())) {
                            if (trueFalseValues.get(keyValue[1].toLowerCase())) {
                                errorMessage.append("Pii option: ").append(keyValue[0]).append(" is special and cannot just be true, must be false or integer (see Readme)\n");
                            } else {
                                piiOptions.setOption(keyValue[0], false);
                                piiOptions.setSpecialOption(keyValue[0], 0);
                            }
                        } else {
                            int value;
                            try {
                                value = Integer.parseInt(keyValue[1].toLowerCase());
                                if (value != 0) {
                                    piiOptions.setOption(keyValue[0], true);
                                } else {
                                    piiOptions.setOption(keyValue[0], false);
                                }
                                piiOptions.setSpecialOption(keyValue[0], value);
                            } catch (NumberFormatException e) {
                                errorMessage.append("Pii option: ").append(keyValue[0]).append(" should have integer value instead of ").append(keyValue[1]).append("\n");
                            }
                        }
                    } else if (Util.PII_SUB_TO_PARENT_TYPE.containsKey(keyValue[0])) {
                        if (trueFalseValues.containsKey(keyValue[1].toLowerCase())) {
                            piiOptions.setOption(keyValue[0], trueFalseValues.get(keyValue[1].toLowerCase()));
                        } else {
                            errorMessage.append("Pii option: ").append(keyValue[0]).append(" has invalid value: ").append(keyValue[1]).append("\n");
                        }
                    } else {
                        errorMessage.append("Pii option: ").append(keyValue[0]).append(" is invalid\n");
                    }
                } else {
                    errorMessage.append("Pii option: ").append(item).append(" is not in valid form of PiiSubtype-value\n");
                }
            }
        }
        pipeline.setPiiOptions(piiOptions);
        return errorMessage.toString();
    }

    protected static String checkModelPaths() {//make sure all model related files exist before beginning loading of any of them
        String[] filesToCheck = { /*RNN_MODEL_FILE,*/ SVM_MODEL_FILE, SVM_FEATURE_INDEX_FILE, SVM_LABEL_FILE, SVM_TEMPLATE_FILE,
                WORD_VECTOR_CL_FILE, REGEX_CONCEPTS_FILE, CRF_MODEL_FILE, BROWN_FILE, MIRA_MODEL_FILE};
        StringBuilder errorMessage = new StringBuilder();
        for (String file : filesToCheck) {
            File f = new File(file);
            if (!f.isFile() || !f.canRead()) {
                LOGGER.error("{} couldn't be found/read", file);
                errorMessage.append(file).append(" couldn't be found and/or read ");
            }
        }
        return errorMessage.toString();
    }

    public static String addTrailingSlash(String path) {
        if (path.length() > 0 && path.charAt(path.length() - 1) != File.separatorChar) {
            return path + File.separator;
        }
        return path;
    }

    public static List<AnalysisEngine> getProcessingAnalysisEngines() {
        List<AnalysisEngine> analysisEngines = new ArrayList<>();

        if (excludes.contains("opennlp")) {
            LOGGER.debug("Excluded OpenNLP engine, ensemble won't work");
        } else {
            analysisEngines.add(getAnalysisEngine(OpenNlpAnnotator.class));
        }
        if (Thread.currentThread().isInterrupted()) {
            throw new RuntimeException("interrupted");
        }

        analysisEngines.add(getAnalysisEngine(DictionaryAnnotator.class));

        analysisEngines.add(getAnalysisEngine(FeatureAnnotator.class,
                FeatureAnnotator.WORD_VECTOR_CL_FILE_NAME, WORD_VECTOR_CL_FILE));

        if (Thread.currentThread().isInterrupted()) {//test, make these inits and checks into loop ?
            throw new RuntimeException("interrupted");
        }

        EnsembleAnnotator.setRnnPortNumber(portRnn);
        if (excludes.isEmpty()) {
            analysisEngines.add(getAnalysisEngine(EnsembleAnnotator.class,
                    EnsembleAnnotator.RNN_HOST_NAME, "localhost", EnsembleAnnotator.RNN_PORT_NUMBER, portRnn,
                    EnsembleAnnotator.SVM_MODEL_FILE, SVM_MODEL_FILE,
                    EnsembleAnnotator.SVM_FEATURE_INDEX_FILE_NAME, SVM_FEATURE_INDEX_FILE,
                    EnsembleAnnotator.SVM_LABEL_FILE_NAME, SVM_LABEL_FILE,
                    EnsembleAnnotator.SVM_TEMPLATE_FILE_NAME, SVM_TEMPLATE_FILE,
                    EnsembleAnnotator.MIRA_MODEL_FILE, MIRA_MODEL_FILE,
                    EnsembleAnnotator.BROWN_CLUSTERS_FILENAME, BROWN_FILE, EnsembleAnnotator.BROWN_CUTOFF, BROWN_CUTOFF,
                    GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, new File(CRF_MODEL_FILE),
                    EnsembleAnnotator.REGEX_CONCEPTS_FILE, REGEX_CONCEPTS_FILE));
        } else {
            analysisEngines.add(getAnalysisEngine(EnsemblePartialAnnotator.class,
                    EnsemblePartialAnnotator.EXCLUDES_LIST, excludes.toString(),
                    EnsembleAnnotator.RNN_HOST_NAME, "localhost", EnsembleAnnotator.RNN_PORT_NUMBER, portRnn,
                    EnsembleAnnotator.SVM_MODEL_FILE, SVM_MODEL_FILE,
                    EnsembleAnnotator.SVM_FEATURE_INDEX_FILE_NAME, SVM_FEATURE_INDEX_FILE,
                    EnsembleAnnotator.SVM_LABEL_FILE_NAME, SVM_LABEL_FILE,
                    EnsembleAnnotator.SVM_TEMPLATE_FILE_NAME, SVM_TEMPLATE_FILE,
                    EnsembleAnnotator.MIRA_MODEL_FILE, MIRA_MODEL_FILE,
                    EnsembleAnnotator.BROWN_CLUSTERS_FILENAME, BROWN_FILE, EnsembleAnnotator.BROWN_CUTOFF, BROWN_CUTOFF,
                    GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, new File(CRF_MODEL_FILE),
                    EnsembleAnnotator.REGEX_CONCEPTS_FILE, REGEX_CONCEPTS_FILE));
        }

        analysisEngines.add(getAnalysisEngine(VoteAnnotator.class, VoteAnnotator.THRESH_HOLD, 1));

        analysisEngines.add(getAnalysisEngine(PostPiiProcessing.class));

        if (Thread.currentThread().isInterrupted()) {
            throw new RuntimeException("interrupted");
        }
        return analysisEngines;
    }

    public static AnalysisEngine getAnalysisEngine(Class<? extends AnalysisComponent> clazz, Object... configurationData) {
        try {
            return AnalysisEngineFactory.createEngine(clazz, configurationData);
        } catch (ResourceInitializationException e) {
            if (e.getCause().toString().contains("connect to RNN")) {
                //   engineCreationFailure.add("RNN");
                return null;
            }
            LOGGER.throwing(e);
            if (deidPipelineTask != null) {
                deidPipelineTask.setTaskException(e);
                deidPipelineTask.fail();
            }
            throw new RuntimeException(e);
        }
    }

//    public static String readLicenseFile(boolean createFile) {
//        final Path path = Paths.get(LICENSE_FILE);
//        String license = "";
//        if (Files.exists(path) && Files.isRegularFile(path)) {
//            if (!Files.isReadable(path) && !path.toFile().setReadable(true)) {
//                LOGGER.error("License file existed but it is not readable");
//                return "License file existed but it is not readable";
//            }
//            try (FileReader fr = new FileReader(LICENSE_FILE);
//                 BufferedReader br = new BufferedReader(fr)) {
//                license = br.readLine();
//            } catch (IOException e) {
//                LOGGER.error("License file existed and was readable, but failed to read");
//                LOGGER.throwing(e);
//                return "License file existed and was readable, but failed to read";
//            }
//        } else {
//            //prompt for license and create file
//            if (createFile) {
//                Scanner keyboard = new Scanner(System.in);
//                do {
//                    System.out.println("Please enter the 36 character license key: ");
//                    license = keyboard.nextLine();
//                } while (license.length() != 36);
//
//                try (FileWriter fw = new FileWriter(LICENSE_FILE)) {
//                    fw.write(license);
//                } catch (IOException e) {
//                    LOGGER.throwing(e);
//                    LOGGER.error("Couldn't create license file");
//                    throw new RuntimeException(e);
//                }
//            } else {
//                return null;
//            }
//        }
//        return license;
//    }

    public static PiiOptions readPiiOptions(File inputFile) {
        try {
            Unmarshaller jaxbUnmarshaller = JAXB_CONTEXT.createUnmarshaller();//not thread safe so make new one instead of static
            return (PiiOptions) jaxbUnmarshaller.unmarshal(inputFile);
        } catch (JAXBException | IllegalArgumentException e) {
            LOGGER.throwing(e);
        }
        return null;
    }

    public static String writePiiOptions(File outputFile, PiiOptions piiOptions) {
        try {
            Marshaller marshaller = JAXB_CONTEXT.createMarshaller();//not thread safe so make new one instead of static
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);// enable pretty-print XML output
            marshaller.marshal(piiOptions, outputFile);
        } catch (JAXBException ex) {
            LOGGER.throwing(ex);
            return outputFile.getName() + " could not be created. Please use a valid filename and ensure that you have permission to write";
        }
        return "";
    }

    public static String getScriptExtension() {
        switch (DeidPipeline.getOs()) {
            case "windows":
                return "bat";
            case "mac":
            case "unix": // TODO: need to test differences
                return "sh";
            default:
                return "sh";
        }
    }

    public static boolean setRnnPermsissions() {
//        final String[] executables = {"rnn" + File.separator + "startRnn." + SCRIPT_EXTENSION, "rnn" + File.separator + "stopKillRnn." + SCRIPT_EXTENSION};
//        try {
//            for (String execFile : executables) {
//                File file = new File(DeidPipeline.DATA_PATH + File.separator + execFile);
//                boolean success;
//                success = file.setExecutable(true) && file.setReadable(true) && file.setWritable(true);//is writable always needed?
//                if (!success) {
//                    throw new SecurityException("failed to set permissions for " + execFile);
//                }
//            }
//        } catch (SecurityException e) {//the permissions sets could throw a SecurityException as well
//            LOGGER.throwing(e);
//            LOGGER.debug("failure to set permissions for RNN, setting RNN to be excluded");
//            DeidPipeline.setExcludes("RNN");//allows system to carry on
//            return false;
//        }
        return true;
    }

    public static void setPortRnn(int portNumber) {//check if valid port or assume caller does?
        portRnn = portNumber;
    }

    public static void setTask(DeidPipelineTask task) {
        deidPipelineTask = task;
    }

    public static int startRnnService() {
//        int portRnn;
//        try {
//            ServerSocket rnnTempSocket = new ServerSocket(0);
//            portRnn = rnnTempSocket.getLocalPort();
//            rnnTempSocket.close();
//            try {
//                ServerSocket rnnTempSocket2 = new ServerSocket(portRnn);
//                rnnTempSocket2.close();
//            } catch (IOException e) {
//                LOGGER.error("Failure to start RNN");
//            }
//            LOGGER.debug("Starting python RNN service command: {} {} {}", DeidPipeline.DATA_PATH + File.separator + "rnn" + File.separator + "startRnn." + DeidPipeline.SCRIPT_EXTENSION, DeidPipeline.RNN_MODEL_FILE, portRnn);
//            ProcessBuilder rnn = new ProcessBuilder(DeidPipeline.DATA_PATH + File.separator + "rnn" + File.separator + "startRnn." + DeidPipeline.SCRIPT_EXTENSION, DeidPipeline.DATA_PATH, DeidPipeline.RNN_MODEL_FILE, Integer.toString(portRnn));
//            DeidPipeline.rnn = rnn.start();
//        } catch (IOException e) {
//            LOGGER.error("Failure to start RNN");
//            LOGGER.throwing(e);
//            return -1;
//        }
//        EnsembleAnnotator.setRnnPortNumber(portRnn);
//        //This is for debugging service I/O
////        InputStream is = DeidPipeline.rnn.getInputStream();
////        BufferedReader er2 = new BufferedReader(new InputStreamReader(DeidPipeline.rnn.getErrorStream()));
////        InputStream er = (DeidPipeline.rnn.getErrorStream());
//        //    logger.debug("start RNN's pwd: {}", new String (((ByteArrayInputStream)((ProcessPipeInputStream)is).in).buf));
//
//        LOGGER.debug("Python RNN service started");
        return portRnn;
    }

    public List<String> getFailingFileNames() {
        return failingFileNames;
    }

    private DeidLevel getDeidLevel() {
        return deidLevel;
    }

    public void setDeidLevel(DeidLevel level) {
        deidLevel = level;
    }

    public void setPiiOptions(PiiOptions piiOptions) {
        this.piiOptions = piiOptions;
    }

//    public boolean processLicense() {
//        licenseProcessed = true;
//        properties = generateProperties();
//        try {
//            if (!properties.validateLicense()) {
//                //inform user
//                LOGGER.error("License Error");
//                if (deidPipelineTask != null) {
//                    Platform.runLater(() -> WarningModal.createAndShowModal("License Error", "License has no remaining files"));
//                }
//                licenseProcessed = false;
//                return false;
//            }
//        } catch (RuntimeException e) {
//            LOGGER.error("License Error, exception trying to validate");
//            LOGGER.throwing(e);
//            if (deidPipelineTask != null) {
//                Platform.runLater(() -> WarningModal.createAndShowModal("License Error", "License processing failed"));
//            }
//            licenseProcessed = false;
//            return false;
//        }
//        return true;
//    }

    private boolean getDbInputs(CommandLine commandLine) {
        String errorMessage = "";
        if (commandLine.hasOption("dbms")) {
            dbms = commandLine.getOptionValue("dbms");
            if (!ConnectionProperties.supportedDbms(dbms)) {
                errorMessage += "DBMS " + dbms + " not supported ";
            }
        } else {
            errorMessage += "Must specify DBMS ";
        }
        if (commandLine.hasOption("dbTableName")) {
            dbTableName = commandLine.getOptionValue("dbTableName");
        } else {
            errorMessage += "Must specify DB table name ";
        }
        if (commandLine.hasOption("dbColumnId")) {
            dbColumnId = commandLine.getOptionValue("dbColumnId");
        } else {
            errorMessage += "Must specify name of column with id ";
        }
        if (commandLine.hasOption("dbColumnText")) {
            dbColumnText = commandLine.getOptionValue("dbColumnText");
        } else {
            errorMessage += "Must specify name of column with text to process ";
        }
        if (commandLine.hasOption("dbName")) {
            dbName = commandLine.getOptionValue("dbName");
        } else {
            errorMessage += "Must specify name of database ";
        }

        if (!errorMessage.isEmpty()) {
            System.out.println(errorMessage);
            return false;
        }
        if (commandLine.hasOption("dbServer")) {
            dbServer = commandLine.getOptionValue("dbServer");
        }
        if (commandLine.hasOption("dbPort")) {
            dbPort = commandLine.getOptionValue("dbPort");
        }
        if (commandLine.hasOption("dbSchema")) {
            dbSchema = commandLine.getOptionValue("dbSchema");
        }
        if (commandLine.hasOption("dbUsername")) {
            dbUsername = commandLine.getOptionValue("dbUsername");
        }
        if (commandLine.hasOption("dbPassword")) {
            dbPassword = commandLine.getOptionValue("dbPassword");
        }
        if (commandLine.hasOption("dbQuery")) {
            dbQuery = commandLine.getOptionValue("dbQuery");
        }
        return true;
    }

    public String checkInputDirectory() {
        String extension = "txt";//TODO:consider treat all files according to inputText regardless of extension? Detect type?
        if (inputCda) {
            extension = "xml";
        }
        File directory = new File(inputDir);
        if (directory.exists()) {
            if (!directory.canRead()) {
                return "Input directory " + inputDir + " is not readable. ";
            }
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (!file.isHidden() && file.canRead() && file.isFile() && FilenameUtils.getExtension(file.toString()).equals(extension)) {
                    return "";
                }
            }
        }
        int fileLimit =999999999;// properties.getLicense().getValue().getFileLimit();
        int fileSizeLimit = 999999999;// properties.getLicense().getValue().getMaxFileSize();
        if (fileLimit == 0 || fileSizeLimit == 0) {
            return "License issue, no remaining files allowed to be processed. ";
        }
        return "No files with extension ." + extension + " in input directory " + inputDir + ". ";
    }

    public String anyRemainingLicenseUsage() {
        int fileLimit = 999999999;// properties.getLicense().getValue().getFileLimit();
        int fileSizeLimit = 999999999;// properties.getLicense().getValue().getMaxFileSize();
        if (fileLimit == 0 || fileSizeLimit == 0) {
            return "License issue, no remaining files allowed to be processed. ";
        }
        return "";
    }

    private AnalysisEngine[] getAnalysisEngines() {
        List<AnalysisEngine> engines = new ArrayList<>(getProcessingAnalysisEngines());
        if (rawXmiOutputSelected) {
            engines.add(getAnalysisEngine(RawXmiOutputAnnotator.class,
                    RawXmiOutputAnnotator.OUTPUT_EXTENSION_PARAM, ".complete-system-output", RawXmiOutputAnnotator.PRETTY_PRINT_PARAM, true,
                    RawXmiOutputAnnotator.IS_CLEAN, false,
                    RawXmiOutputAnnotator.OUTPUT_TO_FILE, fileOutputSelected, RawXmiOutputAnnotator.OUTPUT_TO_DB, dbOutputSelected));
        }
        engines.add(getAnalysisEngine(DeidLevelCleaner.class));
        engines.addAll(getOutputAnalysisEngines());
        engines.add(getAnalysisEngine(DocumentListAnnotator.class, DocumentListAnnotator.OUTPUT_DIRECTORY_PARAM, outputDir));
        return engines.toArray(new AnalysisEngine[0]);
    }

    private boolean setOutputTypeOptions(String choice) {
        choice = choice.toLowerCase();
        if ("all".equals(choice)) {
            selectAllPii();
        } else if (choice.contains(",") || choice.contains("-")) {
            String[] chosens = choice.split("[-,]");
            for (String option : chosens) {
                if (!setOutputType(option)) {
                    return false;
                }
            }
        } else {
            return (setOutputType(choice));
        }
        return true;
    }

    private boolean setOutputType(String option) {
        if ("categorytag".equals(option)) {
            piiTaggingCategorySelected = true;
        } else if ("generaltag".equals(option)) {
            piiTaggingSelected = true;
        } else if ("detected".equals(option) || "detectedpii".equals(option)) {
            piiWriterSelected = true;
        } else if ("resynthesis".equals(option) || "resynth".equals(option)) {
            resynthesisSelected = true;
        } else if ("complete".equals(option) || "raw".equals(option)) {
            rawXmiOutputSelected = true;
        } else if ("filtered".equals(option) || "clean".equals(option)) {
            cleanXmiOutputSelected = true;
        } else if ("map".equals(option)) {
            resynthesisMapSelected = true;
            resynthesisSelected = true;//can't have map w/o resynthesis at this point,
            // TODO: would make sense to print map w/o resynthesis if maps persist
        } else {
            return false;
        }
        return true;
    }

    public void setResynthesisSelected(boolean selected) {
        resynthesisSelected = selected;
    }

    public void setResynthesisMapSelected(boolean selected) {
        resynthesisMapSelected = selected;
    }

    public void setPiiTaggingSelected(boolean selected) {
        piiTaggingSelected = selected;
    }

    public void setPiiTaggingCategorySelected(boolean selected) {
        piiTaggingCategorySelected = selected;
    }

    public void setPiiWriterSelected(boolean selected) {
        piiWriterSelected = selected;
    }

    public void setRawXmiOutputSelected(boolean selected) {
        rawXmiOutputSelected = selected;
    }

    public void setCleanXmiOutputSelected(boolean selected) {
        cleanXmiOutputSelected = selected;
    }

    protected void tryCreateJCas() {
        jCas = null;
        try {
            jCas = createJCas();
        } catch (UIMAException e) {
            throwException(e);
        }
        if (deidPipelineTask != null) {
            deidPipelineTask.update(0);
        }
    }

    protected void runPipeline() {
        runPipeline(true);
    }

    protected void runPipeline(boolean output) {
        DocumentListAnnotator.clearTotalCharactersProcessed();
        long amount = reader.getFileCount();
        try {
            int filesProcessed = 0;
            try {
                if (deidPipelineTask != null) {
                    while (reader.hasNext()) {
                        if (Thread.currentThread().isInterrupted()) {
                            LOGGER.debug("Broke runPipeline");
                            break;
                        }
                        jCas.reset();
                        reader.getNext(jCas);
                        filesProcessed = processCurrentJCas(filesProcessed);
                        deidPipelineTask.update();
                    }
                } else {
                    while (reader.hasNext()) {
                        jCas.reset();
                        reader.getNext(jCas);
                        filesProcessed = processCurrentJCas(filesProcessed);
                    }
                }
            } catch (AnalysisEngineProcessException e) {
                LOGGER.debug("Interrupted");
            }
            if (output) {
                DocumentListAnnotator.writeFile(makeOptionsString());
            }
//            properties.getLicense().setFilesProcessed(filesProcessed);
        } catch (CollectionException | IOException e) {
            throwException(e);
        }
        long total = timer.getTotal();
        LOGGER.info("Total time: {} seconds, average per document: {} seconds", total / 1000000000L, total / 1000000000f / amount);
    }

    protected int processCurrentJCas(int filesProcessed) throws AnalysisEngineProcessException {
        try {
            timer.start();
            SimplePipeline.runPipeline(jCas, analysisEngines);
            timer.stopCumulative();
            filesProcessed++;//only counted if successful
//            if (filesProcessed % UPDATE_LICENSE_FREQUENCY == 0) {//try 3 times in case of server issues
//                try {
//                    properties.updateLicense();
                    //for some reason ResourceAccessException is stated as not to be thrown
//                } catch (ResourceAccessException e) {//couldn't connect with server
//                    try {
//                        properties.updateLicense();
//                    } catch (ResourceAccessException e2) {//couldn't connect with server
//                        try {
//                            properties.updateLicense();
//                        } catch (ResourceAccessException e3) {//couldn't connect with server
//                            throw new RuntimeException("License Failure");
//                        }
//                    }
//                } catch (Exception e2) {
//                    LOGGER.throwing(e2);
//                    throw new RuntimeException("License Failure");
//                }
            //}
        } catch (AnalysisEngineProcessException e) {
            timer.stopCumulative();
            if (e.getCause() == null) {
                addFailedFile(jCas);
                LOGGER.throwing(e);
            } else if (e.getCause().toString().contains("Interrupt")) {
                LOGGER.debug("Interrupted, in pipeline");
                throw e;
            } else {
                addFailedFile(jCas);
                LOGGER.throwing(e);
            }
        }
        return filesProcessed;
    }

    public boolean createAnalysisEngines() {
        try {
            preCreatedEngines = new ArrayList<>(getProcessingAnalysisEngines());//up to post processing
            preCreatedEnginesSize = preCreatedEngines.size();
        } catch (RuntimeException e) {
            LOGGER.throwing(e);
            return false;
        }
        return true;
    }

    public void setupPreCreatedEngines() {//even though jCas contains option info, the directories are still done via config parameters
        //this means that GUI can't create output annotators until user makes selections, or if user re-runs w/ different foldres
        while (preCreatedEngines.size() > preCreatedEnginesSize) {
            preCreatedEngines.remove(preCreatedEngines.size() - 1);
        }
        //add final choices of engines to pipeline:
        if (rawXmiOutputSelected) {
            preCreatedEngines.add(getAnalysisEngine(RawXmiOutputAnnotator.class,
                    RawXmiOutputAnnotator.OUTPUT_EXTENSION_PARAM, ".complete-system-output", RawXmiOutputAnnotator.PRETTY_PRINT_PARAM, true,
                    RawXmiOutputAnnotator.IS_CLEAN, false,
                    RawXmiOutputAnnotator.OUTPUT_TO_DB, dbOutputSelected, RawXmiOutputAnnotator.OUTPUT_TO_FILE, fileOutputSelected));
        }
        preCreatedEngines.add(getAnalysisEngine(DeidLevelCleaner.class));
        preCreatedEngines.addAll(getOutputAnalysisEngines());
        preCreatedEngines.add(getAnalysisEngine(DocumentListAnnotator.class, DocumentListAnnotator.OUTPUT_DIRECTORY_PARAM, outputDir));
    }

    private String makeOptionsString() {//used for logging and for Options in database output table DEID_RUN
        String options = "Input source " + ((dbInputToggle) ? String.join(" ", dbms, dbName,
                dbTableName, dbColumnId, dbColumnText, dbSchema, dbServer, dbPort, dbUsername, dbQuery) : inputDir);
        options += "\nInput type " + ((inputText) ? "text" : "CDA XML");
        options += "\nOutput ";
        if (dbOutputSelected) {
            options += "Database ";
        }
        if (fileOutputSelected) {
            options += outputDir;
        }
        options += "\nLevel: " + deidLevel;
        options += "\nOutput Types: ";
        if (piiTaggingCategorySelected) {
            options += "Tagging Category, ";
        }
        if (piiTaggingSelected) {
            options += "Tagging General, ";
        }
        if (piiWriterSelected) {
            options += "PII Annotations, ";
        }
        if (resynthesisSelected) {
            options += "Resynthesised, ";
        }
        if (resynthesisMapSelected) {
            options += "Map, ";
        }
        if (cleanXmiOutputSelected) {
            options += "Filtered, ";
        }
        if (rawXmiOutputSelected) {
            options += "Complete, ";
        }
        options = options.substring(0, options.length() - 2); //chop off extra , and space from output types.
        if (!excludes.isEmpty()) {
            options += "\nExcluding: " + excludes.toString();
        }
        return options;
    }

    private void logOptions() {
        LOGGER.debug("{}", () -> makeOptionsString());
        LOGGER.debug("Rnn Port {}", portRnn);
    }

    public String execute(boolean usePreCreatedEngines) {
//        if (!licenseProcessed && !processLicense()) {
//            return "License error2";
//        }
        timeStart = LocalDateTime.now().format(DeidPipelineTask.FORMATTER);
        logOptions();
        timer.start();

        String errorMessage = getCollectionReader();
        if (!errorMessage.isEmpty()) {
            if (deidPipelineTask != null) {
                deidPipelineTask.fail();
            }
            return errorMessage;
        }
//        int maxProcessableFiles = properties.requestProcess(reader.getFileCount());
//        if (maxProcessableFiles == 0) {
//            LOGGER.debug("Requested {} files, but none remaining", reader.getFileCount());
//            return "No remaining files for license";
//        }
//        LOGGER.debug("{}", () -> "properties: " + properties.toString());

        reader.setMaximumProcessableFiles(999999999);
        if (usePreCreatedEngines) {
            analysisEngines = preCreatedEngines.toArray(new AnalysisEngine[0]);
        } else {
//            if (!DeidPipeline.getExcludes().contains("rnn")) {
//                if (setRnnPermsissions()) {
//                    portRnn = startRnnService();
//                    if (portRnn == -1) {
//                        LOGGER.error("Couldn't start RNN service");
//                        return "Couldn't start RNN service";
//                    }
//                } else {
//                    LOGGER.error("Couldn't start RNN service");
//                    return "Couldn't start RNN service";
//                }
//            }
            analysisEngines = getAnalysisEngines();
        }
        tryCreateJCas();
        timer.stopPrint("total initialization time: ", 1000000);
        runPipeline();
        //properties.completeRequest();
        writeHistoryInfo();
        if (deidPipelineTask != null) {
            deidPipelineTask.succeed();
        }
        try {
            reader.close();//close any DB or file connections
        } catch (IOException e) {//log issue, but nothing to tell user since it is closing
            LOGGER.throwing(e);
        }
        return "";
    }

    private void writeHistoryInfo() {
        String runName = PUNCTUATION_MATCH.matcher(timeStart).replaceAll("-");
        String options = makeOptionsString();
        try (FileWriter listfw = new FileWriter(DeidRunnerController.LIST_OF_RUNS_PATH, true);
             BufferedWriter listWriter = new BufferedWriter(listfw);
             FileWriter fw = new FileWriter("log/" + runName + ".log");
             BufferedWriter writer = new BufferedWriter(fw)) {
            listWriter.write(timeStart + " (" + DocumentListAnnotator.getTotalCharactersProcessed() / 5000 + " note equivalents)");
            listWriter.newLine();
            writer.write(options + "\n");
            writer.write(String.join(System.lineSeparator(), DocumentListAnnotator.getFileList()));
            writer.write(System.lineSeparator());
            writer.write("Total 5000 character note equivalents processed: " + DocumentListAnnotator.getTotalCharactersProcessed() / 5000 + System.lineSeparator());
            //no access to progressBox
        } catch (IOException e) {
            LOGGER.throwing(e);
        }
    }

    private String logAndGetUiMssage(UIMAException e) {
        LOGGER.throwing(e);
        if (deidPipelineTask != null) {
            deidPipelineTask.setTaskException(e);
            deidPipelineTask.fail();
        }
        if (e.getCause() != null) {//messages thrown to be used for UI
            return e.getCause().toString();
        } else if (e.getMessageKey() != null) {
            return e.getMessageKey();
        } else {
            return e.toString();
        }
    }

    protected String getCollectionReader() {
        TypeSystemDescription typeSystemDescription = null;
        try {
            typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription();
        } catch (ResourceInitializationException e) {
            return logAndGetUiMssage(e);
        }
        if (fileInputToggle) {
            try {
                reader = (FileSystemCollectionReader) CollectionReaderFactory.createReader(FileSystemCollectionReader.class, typeSystemDescription,
                        FileSystemCollectionReader.INPUT_DIRECTORY_PARAMETER, inputDir,
                        FileSystemCollectionReader.RECURSIVE_PARAMETER, false,
                        FileSystemCollectionReader.INPUT_CDA, inputCda,
                        FileSystemCollectionReader.FILE_LIMIT, getFileLimit(),
                        FileSystemCollectionReader.FILE_SIZE_LIMIT, getFileSizeLimit(),

                        GeneralCollectionReader.DEID_LEVEL, deidLevel,
                        GeneralCollectionReader.OUTPUT_CLEAN, cleanXmiOutputSelected,
                        GeneralCollectionReader.OUTPUT_GENERAL_TAG, piiTaggingSelected,
                        GeneralCollectionReader.OUTPUT_CATEGORY_TAG, piiTaggingCategorySelected,
                        GeneralCollectionReader.OUTPUT_PII, piiWriterSelected,
                        GeneralCollectionReader.OUTPUT_RESYNTHESIS, resynthesisSelected,
                        GeneralCollectionReader.OUTPUT_RESYNTHESIS_MAP, resynthesisMapSelected,
                        GeneralCollectionReader.OUTPUT_RAW, rawXmiOutputSelected,

                        GeneralCollectionReader.CLEAN_DIRECTORY, outputDir,
                        GeneralCollectionReader.GENERAL_TAG_DIRECTORY, outputDir,
                        GeneralCollectionReader.CATEGORY_TAG_DIRECTORY, outputDir,
                        GeneralCollectionReader.PII_DIRECTORY, outputDir,
                        GeneralCollectionReader.RESYNTHESIS_DIRECTORY, outputDir,
                        GeneralCollectionReader.RESYNTHESIS_MAP_DIRECTORY, outputDir,
                        GeneralCollectionReader.RAW_DIRECTORY, outputDir);
            } catch (UIMAException e) {
                return logAndGetUiMssage(e);
            }
        } else if (dbInputToggle) {
            try {
                reader = (GeneralCollectionReader) CollectionReaderFactory.createReader(DbCollectionReader.class, typeSystemDescription,
                        DbCollectionReader.DBMS, dbms, DbCollectionReader.SERVER, dbServer, DbCollectionReader.PORT, dbPort,
                        DbCollectionReader.DB_NAME, dbName, DbCollectionReader.TABLE_NAME, dbTableName,
                        DbCollectionReader.COLUMN_ID, dbColumnId, DbCollectionReader.COLUMN_TEXT, dbColumnText,
                        DbCollectionReader.DEID_LEVEL, deidLevel,
                        DbCollectionReader.SCHEMA, dbSchema,
                        DbCollectionReader.USERNAME, dbUsername, DbCollectionReader.PASSWORD, dbPassword,
                        DbCollectionReader.QUERY, dbQuery, DbCollectionReader.DB_OUTPUT_SELECT, dbOutputSelected,
                        DbCollectionReader.OPTION_STRING, makeOptionsString(),
                        DbCollectionReader.INPUT_CDA, inputCda,
                        DbCollectionReader.FILE_LIMIT, getFileLimit(), DbCollectionReader.FILE_SIZE_LIMIT, getFileSizeLimit(),

                        GeneralCollectionReader.DEID_LEVEL, deidLevel,
                        GeneralCollectionReader.OUTPUT_CLEAN, cleanXmiOutputSelected,
                        GeneralCollectionReader.OUTPUT_GENERAL_TAG, piiTaggingSelected,
                        GeneralCollectionReader.OUTPUT_CATEGORY_TAG, piiTaggingCategorySelected,
                        GeneralCollectionReader.OUTPUT_PII, piiWriterSelected,
                        GeneralCollectionReader.OUTPUT_RESYNTHESIS, resynthesisSelected,
                        GeneralCollectionReader.OUTPUT_RESYNTHESIS_MAP, resynthesisMapSelected,
                        GeneralCollectionReader.OUTPUT_RAW, rawXmiOutputSelected,
                        GeneralCollectionReader.CLEAN_DIRECTORY, outputDir,
                        GeneralCollectionReader.GENERAL_TAG_DIRECTORY, outputDir,
                        GeneralCollectionReader.CATEGORY_TAG_DIRECTORY, outputDir,
                        GeneralCollectionReader.PII_DIRECTORY, outputDir,
                        GeneralCollectionReader.RESYNTHESIS_DIRECTORY, outputDir,
                        GeneralCollectionReader.RESYNTHESIS_MAP_DIRECTORY, outputDir,
                        GeneralCollectionReader.RAW_DIRECTORY, outputDir
                );
            } catch (UIMAException e) {
                return logAndGetUiMssage(e);
            }
        } else {//shouldn't be here, maybe future for service?
            LOGGER.error("Neither file nor db inputToggle was true");
            return "Neither file nor db inputToggle was true";
        }

        if (reader == null) {
            LOGGER.error("Reader was null");
            return "Reader was null";
        } else {
            if (deidPipelineTask != null) {
                deidPipelineTask.setFileCount(reader.getFileCount());
            }
        }
        reader.setPiiOptions(piiOptions);
        return "";
    }

    private List<AnalysisEngine> getOutputAnalysisEngines() {// Post-processing engines
        List<AnalysisEngine> outputAnalysisEngines = new ArrayList<>();
        if (cleanXmiOutputSelected) {
            outputAnalysisEngines.add(getAnalysisEngine(RawXmiOutputAnnotator.class,
                    RawXmiOutputAnnotator.OUTPUT_EXTENSION_PARAM, ".filtered-system-output", RawXmiOutputAnnotator.PRETTY_PRINT_PARAM, true,
                    RawXmiOutputAnnotator.IS_CLEAN, true,
                    RawXmiOutputAnnotator.OUTPUT_TO_DB, dbOutputSelected, RawXmiOutputAnnotator.OUTPUT_TO_FILE, fileOutputSelected));
        }
        if (piiWriterSelected) {
            outputAnalysisEngines.add(getAnalysisEngine(PiiWriterAnnotator.class,
                    PiiWriterAnnotator.OUTPUT_EXTENSION_PARAM, ".detectedPII.txt",
                    PiiWriterAnnotator.OUTPUT_TO_DB, dbOutputSelected, PiiWriterAnnotator.OUTPUT_TO_FILE, fileOutputSelected));
        }
        String extension;
        if (inputCda) {
            extension = ".xml";
        } else {
            extension = ".txt";
        }
        if (piiTaggingSelected) {
            outputAnalysisEngines.add(getAnalysisEngine(PiiTagging.class,
                    PiiTagging.CATEGORY, "none", PiiTagging.OUTPUT_EXTENSION_PARAM, ".deid.piiTag" + extension,
                    PiiTagging.OUTPUT_TO_DB, dbOutputSelected, PiiTagging.OUTPUT_TO_FILE, fileOutputSelected));
        }
        if (piiTaggingCategorySelected) {
            outputAnalysisEngines.add(getAnalysisEngine(PiiTagging.class,
                    PiiTagging.CATEGORY, "top", PiiTagging.OUTPUT_EXTENSION_PARAM, ".deid.piiCategoryTag" + extension,
                    PiiTagging.OUTPUT_TO_DB, dbOutputSelected, PiiTagging.OUTPUT_TO_FILE, fileOutputSelected));
        }
        if (resynthesisSelected) {
            outputAnalysisEngines.add(getAnalysisEngine(ResynthesisAnnotator.class,
                    ResynthesisAnnotator.OUTPUT_EXTENSION_PARAM, ".deid.resynthesis" + extension,
                    ResynthesisAnnotator.SAVE_FOR_SERVICE, false,
                    ResynthesisAnnotator.OUTPUT_TO_DB, dbOutputSelected, ResynthesisAnnotator.OUTPUT_TO_FILE, fileOutputSelected));
        }

        return outputAnalysisEngines;
    }

    private void selectAllPii() {
        resynthesisSelected = true;
        resynthesisMapSelected = true;
        piiTaggingSelected = true;
        piiTaggingCategorySelected = true;
        piiWriterSelected = true;
        cleanXmiOutputSelected = true;
        rawXmiOutputSelected = true;
    }

    private void throwException(Exception e) {
        LOGGER.throwing(e);
        if (deidPipelineTask != null) {
            deidPipelineTask.setTaskException(e);
            deidPipelineTask.fail();
        }
        throw new RuntimeException(e);
    }

    private void addFailedFile(JCas jCas) {
        failingFileNames.add(Utilities.getFileName(jCas));
    }

    private DeidProperties generateProperties() {
//        if (license == null) {
//            license = readLicenseFile(true);
//        }
        DeidProperties deidProperties = new DeidProperties();

        Properties newProperties = new Properties();
        try (FileInputStream input = new FileInputStream(new File("classes/application.yml"))) {
            newProperties.load(input);
        } catch (IOException e) {
            try (FileInputStream input2 = new FileInputStream(new File("src/main/resources/application.yml"))) {
                newProperties.load(input2);
            } catch (IOException ex) {
                throw new RuntimeException("Did not find application properties file; cannot initialize critical properties.");
            }
        }

//        if (license != null && license.length() > 0) {
//            deidProperties.getLicense().getValue().setKey(UUID.fromString(license));
//        }

//        if (System.getProperty("clinacuity.deid.baseUrl") != null) {
//            deidProperties.getApi().setBaseUrl(System.getProperty("clinacuity.deid.baseUrl"));
//        } else {
//            deidProperties.getApi().setBaseUrl(newProperties.getProperty("base-url").replaceAll("\"", ""));
//        }
//
//        deidProperties.getApi().setUpdateLicense(newProperties.getProperty("update-license").replaceAll("\"", ""));
//        deidProperties.getApi().setValidateLicense(newProperties.getProperty("validate-license").replaceAll("\"", ""));
//        deidProperties.getApi().setRequestLicense(newProperties.getProperty("request-license").replaceAll("\"", ""));
//        deidProperties.getApi().setCompleteRequest(newProperties.getProperty("complete-request").replaceAll("\"", ""));

        return deidProperties;
    }

    private int getFileLimit() {
        return 999999999;
//        return properties.getLicense().getValue().getFileLimit();
    }

    private int getFileSizeLimit() {

        return 999999999;
        //return properties.getLicense().getValue().getMaxFileSize();
    }

    public void setInput(String inputDir) {
        this.inputDir = inputDir;
    }

    public void setInputCda(boolean inputCda) {
        this.inputCda = inputCda;
    }

    public void setInputText(boolean inputText) {
        this.inputText = inputText;
    }

    public void setOutput(String outputDir) {
        this.outputDir = outputDir;
    }

    public void setDbInputToggle(boolean dbInputToggle) {
        this.dbInputToggle = dbInputToggle;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setDbColumnId(String dbColumnId) {
        this.dbColumnId = dbColumnId;
    }

    public void setDbColumnText(String dbColumnText) {
        this.dbColumnText = dbColumnText;
    }

    public void setDbTableName(String dbTableName) {
        this.dbTableName = dbTableName;
    }

    public void setDbServer(String dbServer) {
        this.dbServer = dbServer;
    }

    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

    public void setDbms(String dbms) {
        this.dbms = dbms;
    }

    public void setDbSchema(String dbSchema) {
        this.dbSchema = dbSchema;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public void setDbQuery(String dbQuery) {
        this.dbQuery = dbQuery;
    }

    public void setFileOutputSelected(boolean fileOutputSelected) {
        this.fileOutputSelected = fileOutputSelected;
    }

    public void setDbOutputSelected(boolean dbOutputSelected) {
        this.dbOutputSelected = dbOutputSelected;
    }

    public void setFileInputToggle(boolean fileInputToggle) {
        this.fileInputToggle = fileInputToggle;
    }

    public String getLastFilenameProcessed() {
        return reader.getLastFilenameProcessed();
    }

//    public void setLicense(String license) {
//        this.license = license;
//    }

    public void resetDictionaryAnnotator() {//if GUI stops pipeline during DictionaryAnnotator, Lucene gets messed up
        //this method finds the precreated DictionaryAnnotator and recreates it there.
        if (preCreatedEngines.size() > 0) {
            LOGGER.debug("Looking for Dictionary Annotator");
            int index = 0;
            while (index < preCreatedEngines.size()) {
                //logger.debug("index: {}, name: {}", index, preCreatedEngines.get(index).getAnalysisEngineMetaData().getName());
                if (preCreatedEngines.get(index).getAnalysisEngineMetaData().getName().contains("DictionaryAnnotator")) {
                    LOGGER.debug("Found at {}", index);
                    preCreatedEngines.set(index, getAnalysisEngine(DictionaryAnnotator.class));
                    return;
                }
                index++;
            }
        }
    }
}
