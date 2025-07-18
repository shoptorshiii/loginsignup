package loginsignupapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class SuccessPopupController {

    @FXML private Label messageLabel;

    public void setMessage(String message) {
        messageLabel.setText(message);
    }
    
    @FXML
    private void handleOk(ActionEvent event) {
        // Get the stage from the event source and close it
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}