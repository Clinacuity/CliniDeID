
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

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class YesOrNoModal extends GeneralModal {
    private static boolean answerYes = false;

    //add deny button, change button actions to set answerYes accordingly
    public static boolean createAndShowModal(String title, String message, String confirmButtonText, String denyButtonText) {
        GeneralModal.createModal(title, message, confirmButtonText);
        HBox buttonBox = (HBox) box.getChildren().get(4);
        Button confirmButton = (Button) buttonBox.getChildren().get(0);
        Button denyButton = new Button(denyButtonText);

        //copy confirmButton's (the default button) style
        denyButton.getStyleClass().addAll(confirmButton.getStyleClass());
        denyButton.setStyle(confirmButton.getStyle());
        denyButton.setOnAction(event -> closeModalDeny());
        if (confirmButtonText.length() > denyButtonText.length()) {
            denyButton.maxWidthProperty().bind(confirmButton.widthProperty());
            denyButton.minWidthProperty().bind(confirmButton.widthProperty());
        } else {
            confirmButton.maxWidthProperty().bind(denyButton.widthProperty());
            confirmButton.minWidthProperty().bind(denyButton.widthProperty());
        }

        Region spacer = new Region();
        spacer.setPrefWidth(150);//this is dependent on the width of the buttons, i.e. the length of the button's texts
        spacer.setMaxWidth(500);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        //currently that is OK, this is only used in one place, TODO: make it computed from button's widths, but they are 0 now
        buttonBox.getChildren().add(0, denyButton);
        buttonBox.getChildren().add(1, spacer);

        confirmButton.setOnAction(event -> closeModalConfirm());
        modal = new Modal(box, 400, 200);
        modal.showAndWait();
        return answerYes;
    }

    private static void closeModalConfirm() {
        answerYes = true;
        modal.close();
    }

    private static void closeModalDeny() {
        answerYes = false;
        modal.close();
    }
}
