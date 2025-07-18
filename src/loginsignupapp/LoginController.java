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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.stream.Collectors;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private WebView captchaWebView;

    private boolean isCaptchaVerified = false;

    @FXML
    private void initialize() {
        // Toggle password field
        passwordVisibleField.managedProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordVisibleField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordField.managedProperty().bind(showPasswordCheckBox.selectedProperty().not());
        passwordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

        // Load CAPTCHA
        WebEngine engine = captchaWebView.getEngine();
        engine.setJavaScriptEnabled(true);
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaApp", this);
            }
        });

        // Load from localhost server
        captchaWebView.getEngine().load("http://localhost:8000/recaptcha.html");
    }

    // Called from recaptcha.html
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
    void handleLogin(ActionEvent event) {
        if (!isCaptchaVerified) {
            showAlert("CAPTCHA Error", "Please complete the CAPTCHA.");
            return;
        }

        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Username and Password are required.");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT PASSWORD FROM USERS WHERE USERNAME = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashed = rs.getString("PASSWORD");
                if (PasswordUtils.checkPassword(password, hashed)) {
                    showPopup("Login Successful!");
                } else {
                    showAlert("Login Failed", "Invalid credentials.");
                }
            } else {
                showAlert("Login Failed", "User not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not connect.");
        }
    }

    @FXML
    void goToSignup(ActionEvent event) {
        loadScene("Signup.fxml", "Sign Up");
    }

    @FXML
    void goToForgetPassword(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("ForgetPassword.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("Reset Password");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadScene(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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