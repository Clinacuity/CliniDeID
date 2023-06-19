
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

package com.clinacuity.deid.gui.modals;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class TextBoxModal extends GeneralModal {
    private static TextField textBox = new TextField();

    //create TextField for data entry, use custom close to verify license key is correct length
    public static void createAndShowModal(String title, String message) {
        GeneralModal.createModal(title, message, "OK");
        textBox.setPadding(new Insets(2, 5, 2, 5));
        //need to get the close button and change its action event to TextBox's and add textBox to modal
        HBox buttonBox = (HBox) box.getChildren().get(4);
        ((Button) buttonBox.getChildren().get(0)).setOnAction(event -> closeModal());
        box.getChildren().add(3, textBox);
        modal = new Modal(box, 400, 200);
        modal.showAndWait();
    }

    protected static void closeModal() {
        if (textBox.getText().length() != 36) {
            TextArea messageArea = new TextArea("License must be 36 characters, not " + textBox.getText().length());
            messageArea.setEditable(false);
            messageArea.getStyleClass().add("text-medium-normal");
            messageArea.setWrapText(true);
            messageArea.setMaxHeight(Double.MAX_VALUE);
            messageArea.setFocusTraversable(false);
            box.getChildren().set(2, messageArea);
        } else {
            modal.close();
        }
    }

    public static String getValue() {
        return textBox.getText();
    }
}
