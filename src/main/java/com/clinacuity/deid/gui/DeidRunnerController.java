
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

package com.clinacuity.deid.gui;

import com.clinacuity.clinideid.message.DeidLevel;
import com.clinacuity.deid.ae.EnsembleAnnotator;
import com.clinacuity.deid.gui.modals.TextBoxModal;
import com.clinacuity.deid.gui.modals.WarningModal;
import com.clinacuity.deid.gui.modals.YesOrNoModal;
import com.clinacuity.deid.mains.DeidPipeline;
import com.clinacuity.deid.outputAnnotators.DocumentListAnnotator;
import com.clinacuity.deid.util.ConnectionProperties;
import com.clinacuity.deid.util.PiiOptions;
import com.clinacuity.deid.util.Utilities;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("ConstantConditions")
public class DeidRunnerController implements Initializable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ERROR_TITLE = "Error!";
    private static final String WARN_TITLE = "Warning!";
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String MAIN_DARK_BLUE = "rgb(29, 149, 231)";
    private static final String LOG_PATH = "log";
    public static final String LIST_OF_RUNS_PATH = LOG_PATH + "/ListOfRuns.txt";
    private static final int ROW_HEIGHT = 25;
    private static final DateTimeFormatter CUSTOM_PII_TIME_STAMP_FORMATTER_DISPLAY = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ssa");
    private static final DateTimeFormatter CUSTOM_PII_TIME_STAMP_FORMATTER_FILENAME = DateTimeFormatter.ofPattern("yyyy-MM-dd hh_mm_ssa");
    private static final Pattern PII_OPTION_ROW_FORMAT = Pattern.compile("^(.*)---(.*)\\.xml$");
    private static final Image DELETE_VIEW = new Image("/gui/delete.png", 15, 15, true, true);
    private static ExecutorService executorService;
    private PiiOptionsRow previousSelectedOptionsRow = null;
    @FXML
    private TableColumn<PiiOptionsRow, String> columnFilename;
    @FXML
    private VBox sidebarDashboard;
    @FXML
    private VBox sidebarHistory;
    @FXML
    private VBox sidebarHelp;
    @FXML
    private TableView<PiiOptionsRow> tablePiiXml;
    @FXML
    private Button customLevel;
    //The CheckBox pii... must have userData with the PiiSubtype they correspond to, PiiOption uses this
    @FXML
    private CheckBox piiRelative;
    @FXML
    private CheckBox piiStreet;
    @FXML
    private CheckBox piiCity;
    @FXML
    private CheckBox piiState;
    @FXML
    private CheckBox piiCountry;
    @FXML
    private CheckBox piiOtherOrgName;
    @FXML
    private CheckBox piiOtherGeo;
    @FXML
    private CheckBox piiHealthCareUnitName;
    @FXML
    private CheckBox piiPhoneFax;
    @FXML
    private CheckBox piiElectronicAddress;
    @FXML
    private CheckBox piiSSN;
    @FXML
    private CheckBox piiOtherIDNumber;
    @FXML
    private CheckBox piiClockTime;
    @FXML
    private CheckBox piiSeason;
    @FXML
    private CheckBox piiDayOfWeek;
    @FXML
    private CheckBox piiProfession;
    @FXML
    private CheckBox piiProvider;
    @FXML
    private CheckBox piiPatient;
    @FXML
    private ToggleGroup piiDates;
    @FXML
    private ToggleGroup piiZips;
    @FXML
    private ToggleGroup piiAges;
    @FXML
    private RadioButton piiZipAll;
    @FXML
    private RadioButton piiZipSafe;
    @FXML
    private RadioButton piiDateAll;
    @FXML
    private RadioButton piiDateMonthDay;
    @FXML
    private TextArea historyData;
    @FXML
    private ImageView successCheck;
    @FXML
    private HBox dashboard;
    @FXML
    private HBox help;
    @FXML
    private VBox customize;
    @FXML
    private HBox history;
    @FXML
    private Label totalProcessed;
    @FXML
    private ImageView iconDashboardBlue;
    @FXML
    private ImageView iconDashboardGrey;
    @FXML
    private ImageView iconHistoryBlue;
    @FXML
    private ImageView iconHistoryGrey;
    @FXML
    private ImageView iconHelpBlue;
    @FXML
    private ImageView iconHelpGrey;
    @FXML
    private Label dashboardLabel;
    @FXML
    private Label historyLabel;
    @FXML
    private ListView<String> historyBox;
    @FXML
    private Label helpLabel;
    @FXML
    private TextField inputDirText;
    @FXML
    private Button inBrowseBtn;
    @FXML
    private TextField outputDirText;
    @FXML
    private Button outBrowseBtn;
    @FXML
    private Button stopButton;
    @FXML
    private Button runButton;
    @FXML
    private HBox fileInputs;
    @FXML
    private RadioButton fileInputToggle;
    @FXML
    private RadioButton inputText;
    @FXML
    private RadioButton inputCda;
    @FXML
    private RadioButton dbInputToggle;
    @FXML
    private GridPane dbInputs;
    @FXML
    private TextField dbName;
    @FXML
    private TextField dbColumnId;
    @FXML
    private TextField dbColumnText;
    @FXML
    private TextField dbTableName;
    @FXML
    private TextField dbServer;
    @FXML
    private TextField dbPort;
    @FXML
    private ComboBox<String> dbms;
    @FXML
    private TextField dbSchema;
    @FXML
    private TextField dbUsername;
    @FXML
    private PasswordField dbPassword;
    @FXML
    private TextField dbQuery;
    @FXML
    private CheckBox fileOutputSelected;
    @FXML
    private TextArea progressBox;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressLabel;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private CheckBox dbOutputSelected;
    @FXML
    private ToggleGroup level;
    @FXML
    private CheckBox resynthesisCheckBox;
    @FXML
    private CheckBox piiTaggingCheckBox;
    @FXML
    private CheckBox piiTaggingCategoryCheckBox;
    @FXML
    private CheckBox piiWriterCheckBox;
    @FXML
    private CheckBox rawXmiCheckBox;
    @FXML
    private CheckBox cleanXmiCheckBox;
    @FXML
    private CheckBox resynthesisMapCheckBox;
    @FXML
    private RadioButton beyond;
    @FXML
    private RadioButton strict;
    @FXML
    private RadioButton limited;
    @FXML
    private RadioButton custom;
    @FXML
    private TextField optionFilename;
    @FXML
    private CheckBox piiDate;
    @FXML
    private CheckBox piiZip;
    @FXML
    private VBox[] sidebarNodes;//easier to disable and invisble with array instead of individually  (for going to custom pii screen)
    @FXML
    private RadioButton piiAgeGt89;
    @FXML
    private RadioButton piiAgeAll;
    @FXML
    private CheckBox piiAge;
    @FXML
    private TextField piiLoadCustomSet;
    @FXML
    private TableColumn<PiiOptionsRow, String> columnDelete;
    @FXML
    private HBox customZipBox;
    @FXML
    private String TOOLTIP_CUSTOM_ZIP;
    @FXML
    private HBox customAgeBox;
    @FXML
    private String TOOLTIP_CUSTOM_AGE;
    @FXML
    private HBox customDateBox;
    @FXML
    private String TOOLTIP_CUSTOM_DATE;

    private boolean stopping = false;
    private DeidPipeline pipe = null;
    private Future<Boolean> pipelineCreationResult;
    private int portRnn = -1;
    private String state = "dashboard";
    private Thread deidPipeline = null;
    private int fileCount;
    private DeidPipelineTask pipelineTask = null;
    private String itemName = "file";//used in display messages, will be "file" or "record" for now
    private String itemNameCapitalized = "File";//instead of doing toUpper of first character and substring overhead
//    private String license;

    private Control[] elements;
    private CheckBox[] piiCheckBoxes;
    private ToggleGroup[] piiRadioGroups;
    private PiiOptions piiOptions = new PiiOptions(DeidLevel.defaultLevel);
    private PiiOptionsRow selectedOptionsRow = null;
    private PiiOptions piiOptionsOriginal = null;
    private boolean piiOptionsChanged = false;
    private boolean fromSelect = false;

    static void executorServiceshutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    private void initializeGuiArraysAndBindings() {
        //can't do this before initialize as FXML elements are all null until now
        Control[] elements2 = {inBrowseBtn, outBrowseBtn, inputDirText, outputDirText, fileInputToggle, dbInputToggle, fileOutputSelected, dbOutputSelected, inputCda, inputText,
                resynthesisCheckBox, piiTaggingCategoryCheckBox, piiTaggingCheckBox, piiWriterCheckBox, rawXmiCheckBox, cleanXmiCheckBox, resynthesisMapCheckBox,
                dbColumnId, dbColumnText, dbms, dbTableName, dbName, dbUsername, dbPassword, dbServer, dbPort, dbSchema, dbQuery, beyond, limited, strict, custom, customLevel};
        elements = elements2;
        CheckBox[] piiBoxes = {piiStreet, piiCity, piiCountry, piiState, piiPatient, piiProvider, piiRelative, piiProfession, piiPhoneFax, piiElectronicAddress,
                piiOtherGeo, piiOtherOrgName, piiOtherIDNumber, piiDayOfWeek, piiClockTime, piiSeason, piiHealthCareUnitName, piiSSN, piiAge, piiDate, piiZip};
        piiCheckBoxes = piiBoxes;
        ToggleGroup[] piiRadios = {piiAges, piiDates, piiZips};
        piiRadioGroups = piiRadios;
        VBox[] sideBarNodes2 = {sidebarDashboard, sidebarHelp, sidebarHistory};
        sidebarNodes = sideBarNodes2;
        //for options with subcategories, checkbox should enable/disable radio buttons
        piiAgeAll.disableProperty().bind(piiAge.selectedProperty().not());
        piiAgeGt89.disableProperty().bind(piiAge.selectedProperty().not());

        piiZipAll.disableProperty().bind(piiZip.selectedProperty().not());
        piiZipSafe.disableProperty().bind(piiZip.selectedProperty().not());

        piiDateAll.disableProperty().bind(piiDate.selectedProperty().not());
        piiDateMonthDay.disableProperty().bind(piiDate.selectedProperty().not());

        //selected checkboxes should have bold text
        for (CheckBox piiBox : piiCheckBoxes) {
            piiBox.setStyle("-fx-text-fill: gray");
            piiBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
                piiOptionsChanged = true;
                selectedOptionsRow = null;
                if (!fromSelect) {
                    tablePiiXml.getSelectionModel().clearSelection();
                }
                if (newValue != null && newValue) {
                    piiBox.setStyle("-fx-text-fill: black");
                } else {
                    piiBox.setStyle("-fx-text-fill: gray");
                }
            });
        }

        Tooltip zipTooltip = new Tooltip(TOOLTIP_CUSTOM_ZIP);
        javafx.scene.control.Tooltip.install(customZipBox, zipTooltip);
        Tooltip ageTooltip = new Tooltip(TOOLTIP_CUSTOM_AGE);
        javafx.scene.control.Tooltip.install(customAgeBox, ageTooltip);
        Tooltip dateTooltip = new Tooltip(TOOLTIP_CUSTOM_DATE);
        javafx.scene.control.Tooltip.install(customDateBox, dateTooltip);
    }

    private void deleteOptionsRow(int row) {
        PiiOptionsRow item = tablePiiXml.getItems().get(row);
        boolean answer = YesOrNoModal.createAndShowModal("Confirmation", "Are you sure you want to delete: " + item.getFilename() + " created on " + item.getCreated() + "?", "Delete", "Cancel");
        if (answer) {
            try {
                Files.delete(Paths.get(item.getFilePath()));
                Platform.runLater(() -> {
                    tablePiiXml.getSelectionModel().clearSelection(row);//avoids index exception
                    tablePiiXml.getItems().remove(row);
                    optionFilename.clear();
                    selectedOptionsRow = null;
                });
            } catch (IOException e) {
                LOGGER.throwing(e);
                WarningModal.createAndShowModal(ERROR_TITLE, "Failed to delete configuration file");
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeGuiArraysAndBindings();

        columnFilename.setSortable(true);
        columnFilename.setSortType(TableColumn.SortType.ASCENDING);
        columnDelete.setSortable(false);
        tablePiiXml.getSortOrder().add(columnFilename);
        tablePiiXml.setMaxHeight(7f * ROW_HEIGHT + 2);
        tablePiiXml.setMaxWidth(850);

        Callback<TableColumn<PiiOptionsRow, String>, TableCell<PiiOptionsRow, String>> cellFactory = new Callback<>() {
            @Override
            public TableCell<PiiOptionsRow, String> call(final TableColumn<PiiOptionsRow, String> param) {
                return new TableCell<>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        Button btn = new Button("", new ImageView(DELETE_VIEW));
                        super.updateItem(item, empty);
                        btn.getStyleClass().add("button-delete");
                        btn.setPadding(Insets.EMPTY);
                        setText(null);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            btn.setOnAction(event -> deleteOptionsRow(getIndex()));
                            setGraphic(btn);
                            setAlignment(Pos.CENTER);
                        }
                    }
                };
            }
        };

        columnDelete.setCellFactory(cellFactory);

        tablePiiXml.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fromSelect = true;
                loadAndSetGuiPiiOptions(newSelection);
                fromSelect = false;
                customize.requestFocus();
            }
        });

        setGuiPiiOption();

        //disable db parts if file input is chosen and vice-versa
        dbInputs.disableProperty().bind(fileInputToggle.selectedProperty());
        fileInputs.disableProperty().bind(dbInputToggle.selectedProperty());

        historyBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.equals("")) {
                return;
            }
            int index = newValue.lastIndexOf(" (");  //- before file size

            String fileName = DeidPipeline.PUNCTUATION_MATCH.matcher(newValue.substring(0, index)).replaceAll("-");
            String filePath = LOG_PATH + File.separatorChar + fileName + ".log";
            try {
                String data = new String(Files.readAllBytes(Paths.get(filePath)));
                historyData.setText(data);
            } catch (IOException e) {
                LOGGER.error("Couldn't open history file {} from run {}", filePath, newValue);
                WarningModal.createAndShowModal(ERROR_TITLE, "Couldn't open history log file for " + newValue);
            }
        });

        //can't do resynthesis map w/o resynthesis
        resynthesisMapCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !resynthesisCheckBox.isSelected()) {
                resynthesisCheckBox.setSelected(true);
            }
        });
        resynthesisCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && resynthesisMapCheckBox.isSelected()) {
                resynthesisMapCheckBox.setSelected(false);
            }
        });
//
//        if (!DeidPipeline.getExcludes().contains("rnn")) {
//            if (!DeidPipeline.setRnnPermsissions()) {
//                WarningModal.createAndShowModal(ERROR_TITLE, "Failed to set permissions for RNN service, it will not be used.");
//            } else {
//                portRnn = DeidPipeline.startRnnService();
//                if (portRnn == -1) {
//                    DeidPipeline.setExcludes("RNN");//allows system to carry on
//                    LOGGER.debug("failure to stat RNN, setting RNN to be excluded");
//                    WarningModal.createAndShowModal(ERROR_TITLE, "Python RNN service failed to start.");
//                }
//            }
//        }
        createAnalysisEngines();
//        license = DeidPipeline.readLicenseFile(false);
    }

    @FXML
    private void setPiiOptionsChanged() {//called by radio buttons
        piiOptionsChanged = true;
        selectedOptionsRow = null;
        if (!fromSelect) {
            tablePiiXml.getSelectionModel().clearSelection();
        }
    }

    private void loadAndSetGuiPiiOptions(PiiOptionsRow newSelection) {//load PiiOptions xml file indicated
        PiiOptions tempOptions = DeidPipeline.readPiiOptions(new File(newSelection.getFilePath()));
        if (tempOptions != null) {
            piiOptions = tempOptions;
            selectedOptionsRow = newSelection;
            piiOptionsChanged = false;
            setGuiPiiOption();
        } else {
            WarningModal.createAndShowModal(ERROR_TITLE, newSelection.getFilename() + " could not be loaded");
        }
    }

    private void createAnalysisEngines() {
        pipe = new DeidPipeline(null, inputDirText.getText(), outputDirText.getText());
        DeidPipeline.setPortRnn(portRnn);//startServices assigned portRnn, but the pipe hadn't been made yet
        MakePipeline pipelineMaker = new MakePipeline(pipe);
        executorService = Executors.newSingleThreadExecutor();
        pipelineCreationResult = executorService.submit(pipelineMaker);
    }

//    void checkAndGetLicenseKey() {
//        if (license == null) {
//            TextBoxModal.createAndShowModal("No LICENSE.KEY file found", "Please enter your 36 character license key: ");
//            license = TextBoxModal.getValue();
//            if (license == null || license.length() != 36) {
//                TextBoxModal.createAndShowModal("Invalid license key", "key");
//                pipelineTask = null;
//                return;
//            }
//            try (FileWriter fw = new FileWriter(DeidPipeline.LICENSE_FILE)) {
//                LOGGER.debug(() -> "Writing new license file " + DeidPipeline.LICENSE_FILE + " beginning with " + license.substring(0, 4));
//                fw.write(license);
//            } catch (IOException e) {
//                LOGGER.throwing(e);
//                LOGGER.error("Couldn't create license file");
//                WarningModal.createAndShowModal(WARN_TITLE, DeidPipeline.LICENSE_FILE + " could not be saved");
//            }
//        }
//    }

    @FXML
    private void runDeid() {
//        checkAndGetLicenseKey();

        LocalDateTime now = LocalDateTime.now();
        String timeStart = now.format(DeidPipelineTask.FORMATTER);

        pipelineTask = new DeidPipelineTask(inputDirText.getText(), outputDirText.getText(), pipe, pipelineCreationResult, this, progressLabel);
//        if (!DeidPipeline.getExcludes().contains("rnn") && !EnsembleAnnotator.tryConnectRnn()) {
//            boolean excludeRnn = YesOrNoModal.createAndShowModal("RNN Service Error", "The RNN failed to start. Would you like to continue without it or stop?", "continue", "stop");
//            if (excludeRnn) {
//                DeidPipeline.setExcludes("rnn");
//            } else {
//                pipelineTask = null;
//                return;
//            }
//        }

        successCheck.setVisible(false);
//        pipe.setLicense(license);
//        if (!pipe.processLicense()) {
//            pipelineTask = null;
//            return;
//        }
        if (!userInputChecks()) {//before changing to processing or any updates, check input fields
            pipelineTask = null;
            return;
        }
//        String message = pipe.anyRemainingLicenseUsage();
//        if (message.length() > 0) {
//            WarningModal.createAndShowModal(ERROR_TITLE, message);
//            pipelineTask = null;
//            return;
//        }

        toggleProcessing(true);

        setupTaskEventHandling();
        setTaskOptions();
        if (selectedOptionsRow != null) {
            piiOptions.setLastUsed(now.format(CUSTOM_PII_TIME_STAMP_FORMATTER_DISPLAY));
            String result = DeidPipeline.writePiiOptions(new File(selectedOptionsRow.getFilePath()), piiOptions);
            if (!result.isEmpty()) {
                WarningModal.createAndShowModal(WARN_TITLE, "Options couldn't be updated, continuing with processing");
            }
            LOGGER.debug("Used option configuration file {}", selectedOptionsRow.getFilePath());
        }

        progressBox.setText(timeStart + " beginning run\n");
        progressBox.appendText("Engines loading\n");

        deidPipeline = new Thread(pipelineTask, "deid-pipeline");
        deidPipeline.start();
    }

    void errorLogModal(String message, Exception e) {
        LOGGER.error(message);
        if (e != null) {
            LOGGER.throwing(e);
        }
        if (!message.contains("User") && !message.contains("user")) {
            message += " Please consult the log";
        }
        String messageFinalish = message;
        Platform.runLater(() -> WarningModal.createAndShowModal(ERROR_TITLE, messageFinalish));
        Platform.runLater(() ->         toggleProcessing(false));
    }

    @FXML
    private void openLogFile() {
        try {//because the Utilities.getExternalFile looks in the /data/ directory we use /../ to get to the sibling directories of the jar
            Desktop.getDesktop().open(Utilities.getExternalFile("../log/DeidLog.log"));
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.warn("Log file does not exist!");
            WarningModal.createAndShowModal(ERROR_TITLE, "Log file does not exist.");
        }
    }

    @FXML
    private void clickedLogo() {
        App.getWebPage("https://www.clinacuity.com/");
    }

    @FXML
    private void clickedDeidLogo() {
        App.getWebPage("https://www.clinacuity.com/");
    }

    /*
    @FXML
    private void openEmail() {
        Desktop desktop;

        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.MAIL)) {
                try {
                    URI mailto = new URI("mailto:support@clinacuity.com?subject=help%20question");
                    desktop.mail(mailto);
                } catch (URISyntaxException | IOException e) {
                    LOGGER.throwing(e);
                }
            }
        }
    }*/

    @FXML
    private void openDocumentationFile() {
        try {
            Desktop.getDesktop().open(Utilities.getExternalFile("/Documentation/Documentation.pdf"));
        } catch (IOException e) {
            WarningModal.createAndShowModal(ERROR_TITLE, "Documentation file does not exist.");
            LOGGER.warn("Documentation file does not exist!");
        }
    }

    @FXML
    private void goDashboard() {
        goBase("dashboard");
        dashboardLabel.setStyle("-fx-text-fill: " + MAIN_DARK_BLUE);
        iconDashboardBlue.setVisible(true);
        iconDashboardGrey.setVisible(false);
        dashboard.setVisible(true);
    }

    private void goBase(String newState) {
        if (state.equals(newState)) {
            return;
        }
        state = newState;
        iconDashboardGrey.setVisible(true);
        iconHistoryGrey.setVisible(true);
        iconHelpGrey.setVisible(true);
        iconDashboardBlue.setVisible(false);
        iconHistoryBlue.setVisible(false);
        iconHelpBlue.setVisible(false);
        helpLabel.setStyle("-fx-text-fill: DimGray");
        historyLabel.setStyle("-fx-text-fill: DimGray");
        dashboardLabel.setStyle("-fx-text-fill: DimGray");
        dashboard.setVisible(false);
        help.setVisible(false);
        history.setVisible(false);

    }

    @FXML
    private void goHistory() {
        goBase("history");
        historyLabel.setStyle("-fx-text-fill: " + MAIN_DARK_BLUE);
        iconHistoryBlue.setVisible(true);
        iconHistoryGrey.setVisible(false);
        history.setVisible(true);
        populateHistoryBox();
    }

    private void populateHistoryBox() {
        historyBox.getItems().clear();
        String line;
        long total = 0;
        try (FileReader reader = new FileReader(LIST_OF_RUNS_PATH);
             BufferedReader r = new BufferedReader(reader)) {
            while ((line = r.readLine()) != null) {
                int index = line.indexOf(" (");
                total += Long.parseLong(line.substring(index + 2, line.indexOf(' ', index + 1)));
                historyBox.getItems().add(0, line);//reverse order, to put newest at top
            }
        } catch (NoSuchFileException | FileNotFoundException e1) {
            //this is fine, just nothing to load
        } catch (IOException e) {//existed, but couldn't load it, should we tell the user?
            LOGGER.throwing(e);
            Platform.runLater(() -> WarningModal.createAndShowModal(WARN_TITLE, "History file found but couldn't be read"));
        }
        totalProcessed.setText("Total of " + total + " note equivalents processed");
    }

    @FXML
    private void goHelp() {
        goBase("help");
        helpLabel.setStyle("-fx-text-fill: " + MAIN_DARK_BLUE);
        iconHelpBlue.setVisible(true);
        iconHelpGrey.setVisible(false);
        help.setVisible(true);
    }

    @FXML
    private void pickInputDir() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Input Directory");
        File userInputDirectory = dirChooser.showDialog(App.getWindow());
        if (userInputDirectory != null) {
            String path = DeidPipeline.addTrailingSlash(userInputDirectory.getAbsolutePath());
            inputDirText.setText(path);
        }
    }

    @FXML
    private void pickOutputDir() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Output Directory");
        File userInputDirectory = dirChooser.showDialog(App.getWindow());
        String path = DeidPipeline.addTrailingSlash(userInputDirectory.getAbsolutePath());
        outputDirText.setText(path);
    }

    @FXML
    private void stopDeid() {
        if (deidPipeline != null && deidPipeline.isAlive() && !deidPipeline.isInterrupted()) {
            deidPipeline.interrupt();
        }
        progressLabel.setText("Stopping ...");
        stopping = true;
        pipe.resetDictionaryAnnotator();
    }

    @FXML
    private void piiLoadDialog() {
        File inputFile;
        if (piiLoadCustomSet.getText() != null && piiLoadCustomSet.getText().length() > 0) {
            inputFile = new File(piiLoadCustomSet.getText());
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialDirectory(new File("."));
            inputFile = fileChooser.showOpenDialog(App.getWindow());
        }
        if (inputFile != null) {//check perms
            PiiOptions temp = DeidPipeline.readPiiOptions(inputFile);
            if (temp != null) {
                piiOptionsChanged = true;
                piiOptions = temp;
                setGuiPiiOption();
                piiLoadCustomSet.setText(inputFile.getAbsolutePath());
                try {
                    Files.copy(Paths.get(inputFile.getAbsolutePath()), Paths.get(inputFile.getName()));
                    Matcher mat = PII_OPTION_ROW_FORMAT.matcher(inputFile.getName());
                    if (mat.matches()) {
                        String name = mat.group(1);
                        String timeStamp = mat.group(2);
                        tablePiiXml.getItems().add(new PiiOptionsRow(name, "", timeStamp, inputFile.getAbsolutePath()));
                        tablePiiXml.getSelectionModel().selectLast();//need to clear any previous selection?
                        tablePiiXml.sort();
                        selectedOptionsRow = tablePiiXml.getSelectionModel().getSelectedItem();
                    }//shouldn't ever fail unless user renamed file
                } catch (IOException e) {
                    LOGGER.throwing(e);
                    WarningModal.createAndShowModal(ERROR_TITLE, "File: " + inputFile.getAbsolutePath() + " was loaded, but couldn't be copied here");
                }
            } else {
                LOGGER.error("Issue reading PiiOptions from {}", inputFile.getAbsolutePath());
                WarningModal.createAndShowModal(ERROR_TITLE, "File: " + inputFile.getAbsolutePath() + " was not a valid options file");
                piiLoadCustomSet.clear();
            }
        }
    }

    private void changeOptions() {
        selectedOptionsRow = null;
        optionFilename.clear();
        tablePiiXml.getSelectionModel().clearSelection();
    }

    @FXML
    private void piiSelectAll() {
        changeOptions();
        piiOptions.setLevel(DeidLevel.beyond);
        setGuiPiiOption();
    }

    @FXML
    private void piiSelectSafeHarbor() {
        changeOptions();
        piiOptions.setLevel(DeidLevel.strict);
        setGuiPiiOption();
    }

    @FXML
    private void piiSelectLimited() {
        changeOptions();
        piiOptions.setLevel(DeidLevel.limited);
        setGuiPiiOption();
    }

    @FXML
    private void piiSelectNone() {
        changeOptions();
        piiOptions.clear();
        setGuiPiiOption();
    }

    private boolean userInputChecks() {
        //return true iff all inputs OK

        //Checks that:  output directory exists and writable,
        //              input directory (if chosen) exists and has files or input database has required fields
        //              at least one output type selected
        //              output database (if chosen) is connectable
        String message = "";
        if (fileInputToggle.isSelected()) {
            if (inputDirText.getText().equals("")) {
                message += "Please select input directory. ";
            } else {
                inputDirText.setText(DeidPipeline.addTrailingSlash(inputDirText.getText()));
                pipe.setInput(inputDirText.getText());
                pipe.setInputCda(inputCda.isSelected());
                pipe.setInputText(inputText.isSelected());
                message += pipe.checkInputDirectory();//needs to know txt vs cda
            }
            itemNameCapitalized = "File";
            itemName = "file";
        } else if (dbInputToggle.isSelected()) {
            if (dbName.getText().isEmpty() || dbColumnId.getText().isEmpty() || dbColumnText.getText().isEmpty()
                    || dbTableName.getText().isEmpty() || dbms.getValue() == null || dbms.getValue().isEmpty()) {
                message += "Missing required database inputs ";
            }
            itemNameCapitalized = "Record";
            itemName = "record";
        } else {
            message += "Choose file or database input. ";
            LOGGER.error("Somehow didn't select file or database input.");
        }

        if (outputDirText.getText().equals("")) {
            message += "Please select output directory. ";
        }
        message += userCanWriteOutputDirectory(outputDirText.getText());
        outputDirText.setText(DeidPipeline.addTrailingSlash(outputDirText.getText()));

        if (!isOutputSelected()) {
            message += "No outputs selected.. ";
        }
        if (dbOutputSelected.isSelected()) {
            message += ConnectionProperties.dbOutputCheck();
        }

        if (!message.isEmpty()) {
            LOGGER.error("Couldn't run pipeline because {}", message);
            WarningModal.createAndShowModal(ERROR_TITLE, message);
            return false;
        }
        return true;
    }

    private void setTaskOptions() {
        if (fileInputToggle.isSelected()) {
            pipelineTask.setInputDirectory(inputDirText.getText());
        } else {
            setDbInputValues();
        }
        pipelineTask.setFileInputToggle(fileInputToggle.isSelected());
        pipelineTask.setDbInputToggle(dbInputToggle.isSelected());
        pipelineTask.setInputCda(inputCda.isSelected());
        pipelineTask.setInputText(inputText.isSelected());

        pipelineTask.setFileOutputSelected(fileOutputSelected.isSelected());
        pipelineTask.setDbOutputSelected(dbOutputSelected.isSelected());
        pipelineTask.setOutputDirectory(outputDirText.getText());

        pipelineTask.setResynthesisSelected(resynthesisCheckBox.isSelected());
        pipelineTask.setResynthesisMapSelected(resynthesisMapCheckBox.isSelected());
        pipelineTask.setPiiTaggingSelected(piiTaggingCheckBox.isSelected());
        pipelineTask.setPiiTaggingCategorySelected(piiTaggingCategoryCheckBox.isSelected());
        pipelineTask.setPiiWriterSelected(piiWriterCheckBox.isSelected());
        pipelineTask.setRawXmiOutputSelected(rawXmiCheckBox.isSelected());
        pipelineTask.setCleanXmiOutputSelected(cleanXmiCheckBox.isSelected());
        pipelineTask.setPiiOptions(piiOptions);

        pipelineTask.setDeidLevel(DeidLevel.valueOf(((RadioButton) level.getSelectedToggle()).getId()));
        pipelineTask.setPortRnn(portRnn);
       // pipelineTask.setLicense(license);
    }

    private void setDbInputValues() {
        pipelineTask.setDbName(dbName.getText());
        pipelineTask.setDbColumnId(dbColumnId.getText());
        pipelineTask.setDbColumnText(dbColumnText.getText());
        pipelineTask.setDbTableName(dbTableName.getText());
        pipelineTask.setDbServer(dbServer.getText());
        pipelineTask.setDbPort(dbPort.getText());
        pipelineTask.setDbms(dbms.getValue());
        pipelineTask.setDbSchema(dbSchema.getText());
        pipelineTask.setDbUsername(dbUsername.getText());
        pipelineTask.setDbPassword(dbPassword.getText());
        pipelineTask.setDbQuery(dbQuery.getText());
    }

    private void setFileSize(int fileCount) {
        this.fileCount = fileCount;
    }

    private void updateFileIndex(int newIndex) {
        String filename = pipe.getLastFilenameProcessed();
        //otherwise, reader goes on to next another file before Platform.runLater starts
        //must do in runLater b/c of UI update must be in FX thread
        Platform.runLater(() -> updateFileIndexUi(filename, newIndex));
    }

    private void updateFileIndexUi(String filename, int newIndex) {
        progressLabel.setVisible(true);
        progressLabel.setText(itemNameCapitalized + "s processed: " + newIndex + " / " + fileCount);
        if (filename != null && !filename.isEmpty()) {//separate so that filename is correct
            progressBox.appendText(filename + ": processed\n");
        }
    }

    private void pipelineOnSucceeded() {
        toggleProcessing(false);
        String errorMessage = pipelineTask.getValue();
        if (!errorMessage.isEmpty()) {
            final String[] prefixes = {"Exception: FATAL: ", "Exception: ERROR: ", "Exception: "};//remove this part before showing to user
            int start = -1;
            int index = -1;
            while (start < 0 && index + 1 < prefixes.length) {
                index++;
                start = errorMessage.indexOf(prefixes[index]);
            }
            if (start < 0) {
                start = 0;
            } else {
                start += prefixes[index].length(); //skip over Exception ... prefix to message
            }
            int end = errorMessage.indexOf(" Position");
            if (end < 0) {//chop of Position if exists
                end = errorMessage.length();
            }
            int hintEnd = errorMessage.indexOf(" Hint");
            if (hintEnd > 0 && hintEnd < end) {//chop off bogus Hint if it exists
                end = hintEnd;
            }
            WarningModal.createAndShowModal(ERROR_TITLE, errorMessage.substring(start, start + 1).toUpperCase() + errorMessage.substring(start + 1, end));

        } else if (stopping) {
            handleStopping();
        } else if (!pipe.getFailingFileNames().isEmpty()) {//TODO: no need for this variable, just check use  !failingFileNames.isEmpty()
            handleFileFailures();
        } else {
            successCheck.setVisible(true);
        }
        progressBox.appendText("Total 5000 character note equivalents processed: " + DocumentListAnnotator.getTotalCharactersProcessed() / 5000 + System.lineSeparator());
    }

    private void handleFileFailures() {
        StringBuilder fileNames = new StringBuilder();
        for (String fileName : pipe.getFailingFileNames()) {
            fileNames.append(NEW_LINE);
            fileNames.append(fileName);
        }
        String message = "There was a failure on " + pipe.getFailingFileNames().size() + " " + itemName + "(s) but the pipeline completed successfully." + NEW_LINE + itemNameCapitalized + "s that failed: " + fileNames.toString();
        WarningModal.createAndShowModal(ERROR_TITLE, message);
        LOGGER.error(message);
        pipe.getFailingFileNames().clear();
    }

    private void handleStopping() {
        stopping = false;
        WarningModal.createAndShowModal("Attention", "Processing was terminated by user");
        if (!pipe.getFailingFileNames().isEmpty()) {//could stop after there was failures
            if (pipe.getFailingFileNames().size() == 1) { // if it is just one thing then probably Dictionary was running
                pipe.getFailingFileNames().clear();
            } else {
                handleFileFailures();
            }
        }
    }

    private void pipelineOnFailed() {
        Throwable exception = pipelineTask.getException();
        LOGGER.throwing(exception);
        toggleProcessing(false);
        pipelineTask.cancel(true);

        String title;
        String message;
        if (stopping) {
            handleStopping();
            return;
        } else if (null == exception.getMessage()) {
            title = ERROR_TITLE;
            message = "Problem " + exception.toString();
//        } else if (exception.getMessage().contains("License")) {
//            title = ERROR_TITLE;
//            message = "License expired or usage limits reached. Contact us at support@clinacuity.com";
        } else {
            title = ERROR_TITLE;
            message = "An unexpected error occurred! Please consult the log";
        }
        WarningModal.createAndShowModal(title, message);
        progressBox.appendText("Total note equivalents processed: " + DocumentListAnnotator.getTotalCharactersProcessed() / 5000);
    }

    void toggleProcessing(boolean isStarting) {
        progressLabel.setVisible(isStarting);
        progressIndicator.setVisible(isStarting);
        progressBar.setVisible(isStarting);
        runButton.setVisible(!isStarting);
        stopButton.setVisible(isStarting);
        for (Control item : elements) {
            item.setDisable(isStarting);
        }
        if (isStarting) {
            progressLabel.setText("Preparing engines . . .");
        } else {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0.0d);
            String dateTime = LocalDateTime.now().format(DeidPipelineTask.FORMATTER);
            progressBox.appendText(dateTime + ": Operations stopped" + (stopping ? (" by user") : "") + ".\n");
        }
    }

    private void setupTaskEventHandling() {
        progressBar.progressProperty().bind(pipelineTask.progressProperty());
        pipelineTask.fileCountProperty().addListener((obs, oldValue, newValue) -> Platform.runLater(() ->
                setFileSize(newValue.intValue())));
        pipelineTask.currentFileIndexProperty().addListener(
                (obs, oldValue, newValue) -> updateFileIndex(newValue.intValue()));
        pipelineTask.setOnSucceeded(event -> pipelineOnSucceeded());
        pipelineTask.setOnFailed(event -> pipelineOnFailed());
        progressBox.textProperty().addListener((obs, oldValue, newValue) -> Platform.runLater(() ->
                progressBox.setScrollTop(Double.MAX_VALUE))); //this will scroll to the bottom
    }

    private String userCanWriteOutputDirectory(String directoryName) {
        Path path = Paths.get(directoryName);
        if (!Files.exists(path)) {//doesn't exist, so make the folders,
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                LOGGER.warn("Output directory does not exist and could not be created!");
                return "Output directory does not exist and could not be created!";
            }
        }
        if (!Files.isExecutable(path)) {//TODO Does Windows need this?
            LOGGER.warn("Execute permissions not enabled for output directory!");
            return "Execute permissions not enabled for output directory!";
        }
        if (!Files.isWritable(path)) {
            LOGGER.warn("Write permissions not enabled for output directory!");
            return "Write permissions not enabled for output directory!";
        }
        return "";
    }

    private boolean isOutputSelected() {
        return (resynthesisCheckBox.isSelected()
                || piiTaggingCheckBox.isSelected()
                || piiTaggingCategoryCheckBox.isSelected()
                || piiWriterCheckBox.isSelected()
                || rawXmiCheckBox.isSelected()
                || cleanXmiCheckBox.isSelected());
    }

    void progressBoxAppendText(String s) {
        progressBox.appendText(s);
    }

    @FXML
    private void customLevel() {
        toggleSidebarIconsForCustom(false);
        getFileListCustomConfigurations();
        piiOptionsOriginal = piiOptions;
        previousSelectedOptionsRow = selectedOptionsRow;
        piiOptionsChanged = false;
    }

    private String[] parseFilename(String filename) {//given filename of PiiOptions configuration, return {setname, formattedCreatedDate} or null if not match
        Matcher mat = PII_OPTION_ROW_FORMAT.matcher(filename);
        String[] data = new String[2];
        if (mat.matches()) {
            data[0] = mat.group(1);
            data[1] = mat.group(2);
            data[1] = data[1].replace('-', '/').replace('_', ':');
            return data;
        }
        return null;
    }

    private void getFileListCustomConfigurations() {
        ObservableList<PiiOptionsRow> data = FXCollections.observableArrayList();
        Path dir = Paths.get(".");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{xml}")) {
            for (Path entry : stream) {
                String fullName = entry.getFileName().toString();
                String[] fileParts = parseFilename(fullName);
                if (fileParts != null) {
                    PiiOptions piiOptionsTemp = DeidPipeline.readPiiOptions(entry.toFile());
                    if (piiOptionsTemp != null) {
                        data.add(new PiiOptionsRow(fileParts[0], piiOptionsTemp.getLastUsed(), fileParts[1], entry.toFile().getAbsolutePath()));
                    }
                }
            }
            tablePiiXml.setItems(data);
        } catch (IOException x) {
            LOGGER.throwing(x);
            WarningModal.createAndShowModal(WARN_TITLE, "Could not read custom configurations");
        }
    }

    @FXML
    private void customCancel() {
        toggleSidebarIconsForCustom(true);
        piiOptions = piiOptionsOriginal;
        setGuiPiiOption(); //revert the GUI elements
        selectedOptionsRow = previousSelectedOptionsRow;
        tablePiiXml.getSelectionModel().clearSelection();
        tablePiiXml.getSelectionModel().select(selectedOptionsRow);
    }

    @FXML
    private void customLevelOff(ActionEvent actionEvent) {
        piiOptions.setLevel(DeidLevel.valueOf(((RadioButton) actionEvent.getSource()).getId()));
        selectedOptionsRow = null;
        tablePiiXml.getSelectionModel().clearSelection();
        setGuiPiiOption();
    }

    private void setGuiPiiOption() {
        for (CheckBox box : piiCheckBoxes) {
            try {
                box.setSelected(piiOptions.getOption((String) box.getUserData()));
            } catch (NullPointerException e) {
                LOGGER.throwing(e);
                LOGGER.error("{}", box.getUserData());
            }
        }
        for (ToggleGroup group : piiRadioGroups) {
            String value = Integer.toString(piiOptions.getSpecialOption((String) group.getUserData()));
            for (Toggle radio : group.getToggles()) {
                try {
                    if ((radio.getUserData()).equals(value)) {
                        radio.setSelected(true);
                    }
                } catch (NullPointerException e) {
                    LOGGER.throwing(e);
                    LOGGER.error("{}", radio.getUserData());
                }
            }
        }
    }

    @FXML
    private boolean customSave() {
        if (!piiOptionsChanged && selectedOptionsRow != null) {
            WarningModal.createAndShowModal("Information", "No changes made to configuration so no new configuration file saved");
            return false;
        }

        setPiiOptionsFromGui();
        if (optionFilename.getText() == null || optionFilename.getText().length() < 1) {//check that it is valid name for file TODO
            WarningModal.createAndShowModal(ERROR_TITLE, "Custom set filename was blank");
            return false;
        }
        String timeStamp = LocalDateTime.now().format(CUSTOM_PII_TIME_STAMP_FORMATTER_FILENAME);
        File outputFile = new File(optionFilename.getText() + "---" + timeStamp + ".xml");
        String message = DeidPipeline.writePiiOptions(outputFile, piiOptions);
        if (message.isEmpty()) {
            WarningModal.createAndShowModal("Success", "Options saved successfully");
            String prettyTimeStamp = timeStamp.replace('-', '/').replace('_', ':');
            tablePiiXml.getItems().add(new PiiOptionsRow(optionFilename.getText(), "new", prettyTimeStamp, outputFile.getAbsolutePath()));
            tablePiiXml.getSelectionModel().selectLast();//need to clear any previous selection?
            tablePiiXml.sort();
            selectedOptionsRow = tablePiiXml.getSelectionModel().getSelectedItem();
            piiOptionsChanged = false;
            return true;
        } else {
            WarningModal.createAndShowModal(ERROR_TITLE, "Options could not be saved " + message);
            return false;
        }
    }

    private void toggleSidebarIconsForCustom(boolean show) {
        dashboard.setVisible(show);
        customize.setVisible(!show);
        for (Node item : sidebarNodes) {
            item.setDisable(!show);
            item.setVisible(show);
        }
    }

    @FXML
    private void customSaveAndUse() {
        if (piiOptionsChanged && !customSave()) { //don't exit if save failed TODO: they could get stuck if bug in save?
            return;
        }
        toggleSidebarIconsForCustom(true);
        if (selectedOptionsRow != null) {
            String filename = optionFilename.getText();
            if (filename.equals("")) {
                filename = selectedOptionsRow.getFilename();
            }
            if (filename != null && filename.length() > 25) {
                filename = filename.substring(0, 25);
            }
            custom.setSelected(true);
            custom.setText("Custom (" + filename + ")");
        }
    }

    private void setPiiOptionsFromGui() {
        for (CheckBox box : piiCheckBoxes) {
            String id = (String) box.getUserData();
            piiOptions.setOption(id, box.isSelected());
        }
        for (ToggleGroup group : piiRadioGroups) {
            String name = (String) group.getUserData();
            if (piiOptions.getOption(name)) {//only look at radio group if checkbox is checked
                int option = Integer.parseInt((String) ((RadioButton) group.getSelectedToggle()).getUserData());
                piiOptions.setSpecialOption(name, option);
            } else {
                piiOptions.setSpecialOption(name, 0);
            }
        }
    }

    private static class MakePipeline implements Callable<Boolean> {
        private DeidPipeline pipe;

        MakePipeline(DeidPipeline p) {//this allows the class to be static as it no longer needs access to enclosing's private pipe
            pipe = p;
        }

        public Boolean call() {
            return pipe.createAnalysisEngines();
        }
    }
}
