
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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class GeneralModal {
    protected static final Logger logger = LogManager.getLogger();
    protected static Modal modal = null;
    protected static VBox box;

    protected GeneralModal() { }

    //create area of modal, but not modal itself to allow customizations before modal creation
    //default box with a label, text area for message, and a single button that just closes the modal
    protected static void createModal(String title, String message, String buttonText) {
        box = new VBox();
        box.getStylesheets().add("/app.css");
        box.getStyleClass().add("card");
        box.setMaxHeight(Double.MAX_VALUE);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-header-two");//TODO: is there a way to use the FXML file for these? Ditto TextBoxModal
        titleLabel.setPadding(new Insets(5.0d, 0.0d, 5.0d, 10.0d));

        TextArea messageArea = new TextArea(message);
        messageArea.setEditable(false);
        messageArea.getStyleClass().add("text-medium-normal");
        messageArea.setWrapText(true);
        messageArea.setMaxHeight(Double.MAX_VALUE);
        messageArea.setFocusTraversable(false);
        VBox.setVgrow(messageArea, Priority.ALWAYS);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.setPadding(new Insets(15.0d, 15.0d, 15.0d, 15.0d));
        buttonBox.maxWidthProperty().bind(box.widthProperty());
        buttonBox.setSpacing(15.0d);

        Button closeButton = new Button(buttonText);
        closeButton.getStyleClass().addAll("button-blue", "text-medium-normal");
        closeButton.setStyle("-fx-font-size: 18;");
        closeButton.setStyle("-fx-padding: 0 2 0 2;");
        closeButton.setOnAction(event -> modal.close());

        buttonBox.getChildren().addAll(closeButton);
        box.getChildren().addAll(titleLabel, new Separator(), messageArea, new Separator(), buttonBox);
    }
}
