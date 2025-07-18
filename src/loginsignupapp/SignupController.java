package loginsignupapp;

import database.DBConnection;
import database.PasswordUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.io.IOException;
public class SignupController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField passwordVisibleField;
    @FXML private TextField confirmPasswordVisibleField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private WebView captchaWebView;

    private boolean isCaptchaVerified = false;

    @FXML
    private void initialize() {
        // Toggle password visibility
        passwordVisibleField.managedProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordVisibleField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordField.managedProperty().bind(showPasswordCheckBox.selectedProperty().not());
        passwordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

        confirmPasswordVisibleField.managedProperty().bind(showPasswordCheckBox.selectedProperty());
        confirmPasswordVisibleField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        confirmPasswordField.managedProperty().bind(showPasswordCheckBox.selectedProperty().not());
        confirmPasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
        confirmPasswordVisibleField.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        // Load CAPTCHA
        WebEngine engine = captchaWebView.getEngine();
        engine.setJavaScriptEnabled(true);
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaApp", this);
            }
        });

        captchaWebView.getEngine().load("http://localhost:8000/recaptcha.html");
    }

    // Called from CAPTCHA HTML
    public void onSuccess(String token) {
        new Thread(() -> isCaptchaVerified = verifyCaptchaWithGoogle(token)).start();
    }

    private boolean verifyCaptchaWithGoogle(String token) {
        try {
            URL url = new URL("https://www.google.com/recaptcha/api/siteverify");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String secret = "6Lf1p4ErAAAAADrj39vih1XbSj6ubZ1MqVUMuuPt";
            String params = "secret=" + secret + "&response=" + token;

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String response = br.lines().collect(Collectors.joining());
                return response.contains("\"success\": true");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    void handleSignup(ActionEvent event) {
        if (!isCaptchaVerified) {
            showAlert("CAPTCHA Error", "Please complete the CAPTCHA.");
            return;
        }

        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "All fields are required.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match.");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM USERS WHERE USERNAME = ?");
             PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO USERS (USERNAME, PASSWORD) VALUES (?, ?)")) {

            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                showAlert("Error", "Username already exists.");
                return;
            }

            String hashedPassword = PasswordUtils.hashPassword(password);
            insertStmt.setString(1, username);
            insertStmt.setString(2, hashedPassword);

            int rowsInserted = insertStmt.executeUpdate();
            if (rowsInserted > 0) {
                showPopup("Signup Successful!");
                clearFields();
            } else {
                showAlert("Error", "Signup failed. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not connect.");
        }
    }

    @FXML
    void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        passwordVisibleField.clear();
        confirmPasswordVisibleField.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showPopup(String message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SuccessPopup.fxml"));
            Parent popupRoot = loader.load();
            SuccessPopupController controller = loader.getController();
            controller.setMessage(message);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(popupRoot));
            popupStage.setTitle("Success");
            popupStage.setResizable(false);
            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}