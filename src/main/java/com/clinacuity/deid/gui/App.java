
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

import com.clinacuity.deid.ae.EnsembleAnnotator;
import com.clinacuity.deid.mains.DeidPipeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import java.io.File;
import java.io.IOException;

public class App extends Application {
    private static final Logger LOGGER = LogManager.getLogger();

    private static Application application = null;
    private static Scene scene = null;

    public static Window getWindow() {
        return scene.getWindow();
    }

    public static void getWebPage(String page) {//does need to be public, despite IntelliJ
        application.getHostServices().showDocument(page);
    }

    public static void main(String[] args) {
        if (args.length >= 2 && (args[0].startsWith("-exclude") || "-x".equals(args[0]))) {
            if (!DeidPipeline.setExcludes(args[1])) {
                System.out.println("Error in excludes parameter");
                return;
            }
            //remove those args before passing to launch in case something in future uses parameters
            String[] args2 = new String[args.length - 2];
            System.arraycopy(args, 2, args2, 0, args.length - 2);
            args = args2;
        }
        launch(args);
    }

    public static void cleanupNonGui() {
        LOGGER.debug("App stopping services");
        stopRnn();
        if (EnsembleAnnotator.pool != null && !EnsembleAnnotator.pool.isShutdown()) {
            LOGGER.debug("App stopping EnsembleAnnotator's pool");
            EnsembleAnnotator.pool.shutdown();
        }
    }

    private static void stopRnn() {
        if (DeidPipeline.getExcludes().contains("rnn")) {
            return;
        }
        if (DeidPipeline.rnn != null) {// && DeidPipeline.rnn.isAlive()) {//isAlive is false always and hasExited is always true, not sure what to test
            try {
                EnsembleAnnotator.stopRnn();//this takes a little time
                Thread.sleep(500);
            } catch (AnalysisEngineProcessException | InterruptedException e) {
                LOGGER.throwing(e);
                if (DeidPipeline.rnn != null) {
                    DeidPipeline.rnn.destroyForcibly();
                }
                Thread.currentThread().interrupt();
            }
        }

        ProcessBuilder rnnKiller = new ProcessBuilder("data" + File.separator + "rnn" + File.separator + "stopKillRnn." + DeidPipeline.getScriptExtension());//WINDOWS??? TODO
        try {
            rnnKiller.start();
            Thread.sleep(300);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Issue stopping RNN");
            LOGGER.throwing(e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void stop() {
        cleanupNonGui();
        DeidRunnerController.executorServiceshutdown();
    }

    @Override
    public synchronized void start(Stage primaryStage) {
        if (application == null) {
            application = this;
        }

        if (scene == null) {
            synchronized (App.class) {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/gui/DeidRunner.fxml"));
                    scene = new Scene(root, 1500, 800);
                    primaryStage.setResizable(false);
                    primaryStage.setTitle("CliniDeID");
                    primaryStage.getIcons().add(new Image(App.class.getResourceAsStream("/gui/CliniDeID-iconSmall.png")));
                    primaryStage.setScene(scene);
                    scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
                    scene.setFill(Color.TRANSPARENT);
                    //primaryStage.initStyle(StageStyle.TRANSPARENT);
                    primaryStage.show();
                } catch (IOException e) {
                    LOGGER.throwing(e);
                }
            }
        }
    }
}
