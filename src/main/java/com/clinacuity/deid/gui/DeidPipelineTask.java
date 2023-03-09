
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

package com.clinacuity.deid.gui;

import com.clinacuity.clinideid.message.DeidLevel;
import com.clinacuity.deid.ae.EnsembleAnnotator;
import com.clinacuity.deid.mains.DeidPipeline;
import com.clinacuity.deid.util.PiiOptions;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@SuppressWarnings("restriction")
public class DeidPipelineTask extends Task<String> {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger LOGGER = LogManager.getLogger();
    private IntegerProperty fileCount = new SimpleIntegerProperty(this, "", 0);
    private IntegerProperty currentIndex = new SimpleIntegerProperty(this, "", 0);
    private String inputDir;
    private String outputDir;
    private boolean resynthesisSelected;
    private boolean resynthesisMapSelected;
    private boolean piiTaggingSelected;
    private boolean piiTaggingCategorySelected;
    private boolean piiWriterSelected;
    private boolean rawXmiOutputSelected;
    private boolean cleanXmiOutputSelected;
    private DeidLevel deidLevel;
    private int portRnn;
    private DeidPipeline pipeline;
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
    private boolean fileInputToggle;
    private boolean dbInputToggle;
    private boolean inputCda;
    private boolean inputText;
    private boolean fileOutputSelected;
    private boolean dbOutputSelected;
    private Future<Boolean> pipelineCreationResult;
    private DeidRunnerController deidRunnerController;
    private Label progressLabel;
    private String license;
    private PiiOptions piiOptions;

    DeidPipelineTask(String inputD, String outputD, DeidPipeline pipe, Future<Boolean> pipelineCreationResult, DeidRunnerController deidRunnerController, Label progressLabel) {
        inputDir = inputD;
        outputDir = outputD;
        pipeline = pipe;
        this.pipelineCreationResult = pipelineCreationResult;
        this.deidRunnerController = deidRunnerController;
        this.progressLabel = progressLabel;
        DeidPipeline.setTask(this);
    }

    public final void setFileCount(int value) {
        fileCount.setValue(value);
    }

    final void setInputDirectory(String directory) {
        inputDir = directory;
        pipeline.setInput(inputDir);
    }

    final void setOutputDirectory(String directory) {
        outputDir = directory;
        pipeline.setOutput(outputDir);
    }

    final ReadOnlyIntegerProperty fileCountProperty() {
        return fileCount;
    }

    final ReadOnlyIntegerProperty currentFileIndexProperty() {
        return currentIndex;
    }

    final void setResynthesisSelected(boolean selected) {
        resynthesisSelected = selected;
    }

    final void setResynthesisMapSelected(boolean selected) {
        resynthesisMapSelected = selected;
    }

    final void setPiiTaggingSelected(boolean selected) {
        piiTaggingSelected = selected;
    }

    final void setPiiTaggingCategorySelected(boolean selected) {
        piiTaggingCategorySelected = selected;
    }

    final void setPiiWriterSelected(boolean selected) {
        piiWriterSelected = selected;
    }

    final void setRawXmiOutputSelected(boolean selected) {
        rawXmiOutputSelected = selected;
    }

    final void setDeidLevel(DeidLevel value) {
        deidLevel = value;
    }

    final void setPortRnn(int portNumber) {
        portRnn = portNumber;
    }

    void setDbInputToggle(boolean dbInputToggle) {
        this.dbInputToggle = dbInputToggle;
    }

    void setDbName(String dbName) {
        this.dbName = dbName;
    }

    void setDbColumnId(String dbColumnId) {
        this.dbColumnId = dbColumnId;
    }

    void setDbColumnText(String dbColumnText) {
        this.dbColumnText = dbColumnText;
    }

    void setDbTableName(String dbTableName) {
        this.dbTableName = dbTableName;
    }

    void setDbServer(String dbServer) {
        this.dbServer = dbServer;
    }

    void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

    void setDbms(String dbms) {
        this.dbms = dbms;
    }

    void setDbSchema(String dbSchema) {
        this.dbSchema = dbSchema;
    }

    void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    void setDbQuery(String dbQuery) {
        this.dbQuery = dbQuery;
    }

    void setFileOutputSelected(boolean fileOutputSelected) {
        this.fileOutputSelected = fileOutputSelected;
    }

    void setDbOutputSelected(boolean dbOutputSelected) {
        this.dbOutputSelected = dbOutputSelected;
    }

    void setFileInputToggle(boolean fileInputToggle) {
        this.fileInputToggle = fileInputToggle;
    }

    public void setInputCda(boolean inputCda) {
        this.inputCda = inputCda;
    }

    public void setInputText(boolean inputText) {
        this.inputText = inputText;
    }

    final void setCleanXmiOutputSelected(boolean selected) {
        cleanXmiOutputSelected = selected;
    }

    @Override
    protected String call() {
        //check if pipeline is finished and if RNN is ready,
        /*
        if (!DeidPipeline.getExcludes().contains("rnn")) {
            int tries = 10;
            while (tries > 0 && !EnsembleAnnotator.tryConnectRnn()) {
                LOGGER.debug("Waiting on RNN service ... ");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    deidRunnerController.toggleProcessing(false);
                    deidRunnerController.errorLogModal("Interrupted", e);
                    Thread.currentThread().interrupt();//not sure about this
                }
                tries--;
            }
            if (tries > 0) {
                LOGGER.debug("RNN service ready");
            } else {
                if (!EnsembleAnnotator.tryConnectRnn()) {
                    LOGGER.debug("RNN failed to start");
                    DeidPipeline.setExcludes("RNN");
                } else {
                    LOGGER.debug("RNN service ready final");
                }
            }
        }*/

        if (pipelineCreationResult.isDone()) {//check if pipeline done creating processing engines
            LOGGER.debug("Already done creating pipeline");
        } else {
            LOGGER.debug("Will wait on pipeline creation");
        }
        try {
            Boolean ok = pipelineCreationResult.get();//wait for completetion of pipeline creation thread
            if (!ok) {
                deidRunnerController.errorLogModal("Pipeline creation failed.", null);
                deidRunnerController.toggleProcessing(false);
                return "Pipeline creation failed.";
            }
        } catch (InterruptedException | ExecutionException e) {
            deidRunnerController.errorLogModal("Pipeline creation failed.", e);
            deidRunnerController.toggleProcessing(false);
            Thread.currentThread().interrupt();
            return "Pipeline creation failed";
        }
        DeidRunnerController.executorServiceshutdown();
        LOGGER.debug("Pipeline creation completed");

        String dateTime = LocalDateTime.now().format(FORMATTER);
        deidRunnerController.progressBoxAppendText("Engines loaded\n" + dateTime + ": Process beginning\n");
        Platform.runLater(() -> progressLabel.setText("Starting processing"));

//        pipeline.setLicense(license);
        pipeline.setResynthesisSelected(resynthesisSelected);
        pipeline.setResynthesisMapSelected(resynthesisMapSelected);
        pipeline.setPiiTaggingSelected(piiTaggingSelected);
        pipeline.setPiiTaggingCategorySelected(piiTaggingCategorySelected);
        pipeline.setPiiWriterSelected(piiWriterSelected);
        pipeline.setRawXmiOutputSelected(rawXmiOutputSelected);
        pipeline.setCleanXmiOutputSelected(cleanXmiOutputSelected);
        pipeline.setDeidLevel(deidLevel);
        DeidPipeline.setPortRnn(portRnn);
        pipeline.setInput(inputDir);
        pipeline.setOutput(outputDir);
        pipeline.setInputCda(inputCda);
        pipeline.setInputText(inputText);
        pipeline.setPiiOptions(piiOptions);

        pipeline.setFileInputToggle(fileInputToggle);
        pipeline.setDbInputToggle(dbInputToggle);
        pipeline.setDbName(dbName);
        pipeline.setDbColumnId(dbColumnId);
        pipeline.setDbColumnText(dbColumnText);
        pipeline.setDbTableName(dbTableName);
        pipeline.setDbServer(dbServer);
        pipeline.setDbPort(dbPort);
        pipeline.setDbms(dbms);
        pipeline.setDbSchema(dbSchema);
        pipeline.setDbUsername(dbUsername);
        pipeline.setDbPassword(dbPassword);
        pipeline.setDbQuery(dbQuery);
        pipeline.setFileOutputSelected(fileOutputSelected);
        pipeline.setDbOutputSelected(dbOutputSelected);
        pipeline.setupPreCreatedEngines();

        String errorMessage = pipeline.execute(true);
        if (!errorMessage.isEmpty()) {
            return errorMessage;
        }
        return "";
    }

    /**
     * Wrapper to call the {@link Task#succeeded()} method
     */
    public void succeed() {
        succeeded();
    }

    /**
     * Wrapper to call the {@link Task#failed()} method
     */
    public void fail() {
        failed();
    }

    /**
     * Updates the current progress, which in turn notifies the GUI.
     * <p>
     * A parameter can passed to specify an index for the update. If no value is passed, the value of {@link DeidPipelineTask#currentIndex} will automatically
     * increment by one.
     * </p>
     */
    public void update() {
        update(currentIndex.getValue() + 1);
    }

    /**
     * Updates the current progress, which in turn notifies the GUI.
     * <p>
     * A parameter can passed to specify an index for the update. If no value is passed, the value of {@link DeidPipelineTask#currentIndex} will automatically
     * increment by one.
     * </p>
     *
     * @param index The new value of {@link DeidPipelineTask#currentIndex}
     */
    public void update(int index) {
        currentIndex.setValue(index);
        updateProgress(currentIndex.getValue(), fileCount.getValue());
    }

    /**
     * Wrapper to call the {@link Task#setException(Throwable)} method.
     *
     * @param e The exception to set for this {@link Task} object.
     */
    public void setTaskException(Throwable e) {
        setException(e);
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public void setPiiOptions(PiiOptions piiOptions) {
        this.piiOptions = piiOptions;
    }
}
