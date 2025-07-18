package loginsignupapp;

import database.DBConnection;
import database.PasswordUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ForgetPasswordController {

    @FXML private TextField usernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private Label resetMessageLabel;

    @FXML
    private void handleResetPassword() {
        String username = usernameField.getText();
        String newPassword = newPasswordField.getText();

        if (username.isEmpty() || newPassword.isEmpty()) {
            resetMessageLabel.setText("All fields are required.");
            return;
        }
        
        String hashedPassword = PasswordUtils.hashPassword(newPassword);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE USERS SET PASSWORD = ? WHERE USERNAME = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, hashedPassword);
            stmt.setString(2, username);

            int updatedRows = stmt.executeUpdate();
            if (updatedRows > 0) {
                resetMessageLabel.setText("Password reset successful!");
            } else {
                resetMessageLabel.setText("User not found.");
            }
        } catch (SQLException e) {
            resetMessageLabel.setText("Error: Could not update password.");
            e.printStackTrace();
        }
    }
}