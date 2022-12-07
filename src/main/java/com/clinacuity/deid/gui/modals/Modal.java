
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

package com.clinacuity.deid.gui.modals;

import com.clinacuity.deid.gui.App;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Modal {
    private static final Logger logger = LogManager.getLogger();
    private static final double DEFAULT_MAX_HEIGHT = 200.0d;
    private static final double DEFAULT_MAX_WIDTH = 300.0d;

    private Stage stage = new Stage();
    private boolean isInitialized = false;

    /**
     * Creates a Modal pop-up window with the given content.
     * <p>Optionally, the width and height can also be set,</p>
     * <p>as well as whether the modal can be resized by the user</p>
     *
     * @param modalContent The content which will be displayed on the Modal
     */
    public Modal(Parent modalContent) {
        this(App.getWindow(), modalContent, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT, false);
    }

    /**
     * Creates a Modal pop-up window with the given content.
     * <p>Optionally, the width and height can also be set,</p>
     * <p>as well as whether the modal can be resized by the user</p>
     *
     * @param modalContent The content which will be displayed on the Modal
     * @param width        The maximum width of the modal
     * @param height       The maximum height of the modal
     */
    public Modal(Parent modalContent, double width, double height) {
        this(App.getWindow(), modalContent, width, height, false);
    }

    /**
     * Creates a Modal pop-up window with the given content.
     * <p>Optionally, the width and height can also be set,</p>
     * <p>as well as whether the modal can be resized by the user</p>
     *
     * @param modalContent The content which will be displayed on the Modal
     * @param width        The maximum width of the modal
     * @param height       The maximum height of the modal
     * @param isResizable  Whether the modal can be resized by the user or its content
     */
    public Modal(Parent modalContent, double width, double height, boolean isResizable) {
        this(App.getWindow(), modalContent, width, height, isResizable);
    }

    /**
     * Creates a Modal pop-up window with the given content, width, and height values.
     * <p>If the width and height are not set, the default values will be used.</p>
     * <p>If the Resizable flag is not set, it is assumed the modal cannot be resized.</p>
     *
     * @param window       The window to which the modal will be attached
     * @param modalContent The content which will be displayed on the Modal
     */
    public Modal(Window window, Parent modalContent) {
        this(window, modalContent, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT, false);
    }

    /**
     * Creates a Modal pop-up window with the given content, width, and height values.
     * <p>If the width and height are not set, the default values will be used.</p>
     * <p>If the Resizable flag is not set, it is assumed the modal cannot be resized.</p>
     *
     * @param window       The window to which the modal will be attached
     * @param modalContent The content which will be displayed on the Modal
     * @param width        The maximum width of the modal
     * @param height       The maximum height of the modal
     */
    public Modal(Window window, Parent modalContent, double width, double height) {
        this(window, modalContent, width, height, false);
    }

    /**
     * Creates a Modal pop-up window with the given content, width, and height values.
     * <p>If the width and height are not set, the default values will be used.</p>
     * <p>If the Resizable flag is not set, it is assumed the modal cannot be resized.</p>
     *
     * @param window       The window to which the modal will be attached
     * @param modalContent The content which will be displayed on the Modal
     * @param width        The maximum width of the modal
     * @param height       The maximum height of the modal
     * @param isResizable  Whether the modal can be resized by the user or its content
     */
    public Modal(Window window, Parent modalContent, double width, double height, boolean isResizable) {
        logger.debug("Initialized modal");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(window.getScene().getWindow());
        stage.setResizable(isResizable);

        Scene scene = new Scene(modalContent, width, height);

        stage.setScene(scene);
        isInitialized = true;
    }

    public void show() {
        if (isInitialized) {
            logger.debug("Showing modal");
            stage.show();
        } else {
            logger.error("Not initialized...?");
        }
    }

    public void showAndWait() {
        if (isInitialized) {
            logger.debug("Showing modal");
            stage.showAndWait();
        } else {
            logger.error("Not initialized...?");
        }
    }

    public void close() {
        if (isInitialized && stage.isShowing()) {
            stage.close();
        }
    }

    public Parent getContent() {
        return stage.getScene().getRoot();
    }
}
