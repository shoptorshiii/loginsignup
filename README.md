# JavaFX Login/Signup Application with Oracle DB and reCAPTCHA - loginsignup

**A secure JavaFX desktop application for user authentication, featuring Oracle DB, BCrypt password hashing, and reCAPTCHA v2, all powered by a local HTTP server.**

## ✨ Features

* **User Registration:** Allows new users to sign up with a username and password.
* **User Login:** Authenticates existing users against the stored credentials.
* **Password Visibility Toggle:** Users can choose to show/hide their password for easier input.
* **Secure Password Hashing:** Passwords are securely hashed using BCrypt before being stored in the database.
* **Google reCAPTCHA v2 Integration:** Protects login and signup forms from bots.
* **Forgot Password Functionality:** Allows users to reset their password.
* **Oracle Database Integration:** Stores and manages user accounts.
* **Success/Error Pop-ups:** Provides clear feedback to the user.
* **Simple Local HTTP Server:** Hosts the `recaptcha.html` file required for `WebView` functionality.

## 🚀 Technologies Used

* **JavaFX:** For building the desktop user interface.
* **Java (JDK 8+ recommended):** The core programming language.
* **Oracle Database:** For persistent storage of user data.
* **JDBC:** Java Database Connectivity for interacting with Oracle.
* **BCrypt (JBCrypt library):** For secure password hashing.
* **Google reCAPTCHA v2:** Anti-bot mechanism.
* **`com.sun.net.httpserver`:** Built-in Java module for the simple local HTTP server.
* **FXML:** XML-based markup for defining UI layouts.

## 📂 Project Structure Overview

* `src/main/java/loginsignupapp/`:
    * `LoginSignupApp.java`: The main entry point of the application.
    * `LoginController.java`: Controller for the login screen, handles login logic and reCAPTCHA.
    * `SignupController.java`: Controller for the signup screen, handles registration logic and reCAPTCHA.
    * `ForgetPasswordController.java`: Controller for resetting user passwords.
    * `SuccessPopupController.java`: Controller for displaying generic success messages.
    * `reCAPTCHAVerifier.java`: Utility for handling reCAPTCHA verification with Google's API.
    * `SimpleHttpServer.java`: **The local HTTP server** that serves `recaptcha.html`.
* `src/main/java/database/`:
    * `DBConnection.java`: Handles establishing and providing a connection to the Oracle database.
    * `PasswordUtils.java`: Contains static methods for password hashing and verification (BCrypt).
* `src/main/java/`:
    * `TestOracleConnection.java`: A standalone utility to test the database connection.
* `src/main/resources/`:
    * `Login.fxml`: FXML layout for the login screen.
    * `Signup.fxml`: FXML layout for the signup screen.
    * `ForgetPassword.fxml`: FXML layout for the forgot password screen.
    * `SuccessPopup.fxml`: FXML layout for the success message pop-up.
    * `recaptcha.html`: The HTML file containing the Google reCAPTCHA widget.
* `.gitignore`: Specifies files and directories to be ignored by Git (e.g., compiled classes, IDE files).

## 🛠️ Setup and Installation

### 1. Prerequisites

* **Java Development Kit (JDK 8 or higher):** Make sure `JAVA_HOME` is set.
* **Oracle Database:** A running Oracle instance (e.g., Express Edition - XE) with a configured user (e.g., `SYSTEM` or a dedicated user) and password.
* **Oracle JDBC Driver (ojdbcX.jar):** Download the appropriate driver for your Oracle version and add it to your project's build path (e.g., in `lib` folder and configured in your IDE).
* **JBCrypt Library:** Add the JBCrypt library to your project's dependencies.
    * **Maven:**
        ```xml
        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>0.4</version> </dependency>
        ```
    * **Gradle:**
        ```groovy
        implementation 'org.mindrot:jbcrypt:0.4' // Check for the latest version
        ```
    * **Manual:** Download `jbcrypt-0.4.jar` and add it to your project's build path.
* **Google reCAPTCHA Keys:**
    * Go to [Google reCAPTCHA Admin Console](https://www.google.com/recaptcha/admin/).
    * Register a new site (choose reCAPTCHA v2 "I'm not a robot" checkbox).
    * Add `localhost` and `localhost:8080` to your domains.
    * You will get a **Site Key** and a **Secret Key**.

### 2. Database Setup

1.  **Create the `USERS` table in your Oracle database:**
    ```sql
    CREATE TABLE USERS (
        USERNAME VARCHAR2(255) PRIMARY KEY,
        PASSWORD VARCHAR2(255) NOT NULL
    );
    ```
2.  **Update `DBConnection.java`:**
    Your current `DBConnection.java` is configured as:
    ```java
    // In src/main/java/database/DBConnection.java
    public class DBConnection {
        public static Connection getConnection() {
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                return DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "SYSTEM", "admin");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
    ```
    These are the credentials (`SYSTEM` and `admin`) currently in your `DBConnection.java`. If your database username or password is different, please update this file locally.

3.  **Update `reCAPTCHAVerifier.java`:**
    Your current `reCAPTCHAVerifier.java` uses:
    ```java
    // In src/main/java/loginsignupapp/reCAPTCHAVerifier.java
    private final String SECRET_KEY = "6Lf1p4ErAAAAADrj39vih1XbSj6ubZ1MqVUMuuPt"; // Your reCAPTCHA Secret Key
    ```
    This secret key (`6Lf1p4ErAAAAADrj39vih1XbSj6ubZ1MqVUMuuPt`) is already embedded. If you generated a new one, update this file.
    Also, ensure the `loadCaptcha()` method points to the correct address of your local server:
    ```java
    public void loadCaptcha() {
        webView.getEngine().load("http://localhost:8080/recaptcha.html"); // Ensure port matches SimpleHttpServer (default 8080)
    }
    ```
    This URL is set to `http://localhost:8080/recaptcha.html`, which matches your `SimpleHttpServer.java`'s default port.

4.  **Update `recaptcha.html`:**
    Your `recaptcha.html` currently has:
    ```html
    <div class="g-recaptcha"
         data-sitekey="6Lf1p4ErAAAAACQRRK8IXAemvgJwSLsU5CTZqu3O" data-callback="onCaptchaSuccess">
    </div>
    ```
    This site key (`6Lf1p4ErAAAAACQRRK8IXAemvgJwSLsU5CTZqu3O`) is already embedded. If you generated a new one, update this file.

## 🏃 How to Run the Application

1.  **Start the Local HTTP Server:**
    Open your terminal or command prompt, navigate to your project's compiled classes directory (e.g., `target/classes` if using Maven, or `bin` if using Eclipse, or directly in `src/main/java` if running from source with correct classpath). Then run:
    ```bash
    java loginsignupapp.SimpleHttpServer
    ```
    You should see output similar to: `Server running at http://localhost:8080/recaptcha.html`
    **Keep this server running in the background.**

2.  **Run the JavaFX Application:**
    Open your IDE (IntelliJ IDEA, Eclipse, NetBeans, etc.).
    * Locate `src/main/java/loginsignupapp/LoginSignupApp.java`.
    * Run this file as a Java Application.

    Alternatively, from the command line:
    ```bash
    # IMPORTANT: You must set the correct paths for your JavaFX SDK 'lib' directory,
    # ojdbcX.jar, and jbcrypt-0.4.jar on your system.
    # The paths below are PLACEHOLDERS and will NOT work directly.
    # Replace 'YOUR_JAVAFX_SDK_LIB_PATH', 'YOUR_OJBDC_JAR_PATH', and 'YOUR_JBCRYPT_JAR_PATH'
    # with the actual absolute paths on your machine.

    # If using modular Java (JDK 11+ and JavaFX SDK):
    java --module-path YOUR_JAVAFX_SDK_LIB_PATH --add-modules javafx.controls,javafx.fxml,javafx.web \
         -cp ".:YOUR_OJBDC_JAR_PATH:YOUR_JBCRYPT_JAR_PATH" loginsignupapp.LoginSignupApp
    ```

3.  **Sign Up a New User:** Since your existing database users might have plain-text passwords, use the "Sign Up" button on the login screen to create a new user account through the application. This ensures the password is correctly hashed and stored.
4.  **Log In:** Use the newly created user's credentials to log in.

## 🐛 Troubleshooting

* **"Login Failed: Invalid credentials."**: This usually means the username/password combination doesn't match, or the password stored in the database is not hashed correctly (e.g., plain text). **Solution:** Delete existing users from your `USERS` table (`DELETE FROM USERS; COMMIT;`) and then sign up a new user through the application.
* **"Error: Could not establish database connection."**: Check your `DBConnection.java` for correct Oracle URL, username, and password. Ensure the Oracle database is running and the `ojdbcX.jar` is in your project's build path. You can use `TestOracleConnection.java` to diagnose.
* **reCAPTCHA not loading or showing errors**:
    * Ensure `SimpleHttpServer.java` is running.
    * Verify the URL in `reCAPTCHAVerifier.java` (`http://localhost:8080/recaptcha.html`) matches the server's port.
    * Check your `recaptcha.html` for the correct Google Site Key and that it's accessible at the specified URL.
    * Ensure your Google reCAPTCHA admin console has `localhost` and `localhost:8080` listed as allowed domains.
    * If using JDK 11+, ensure you've added the `javafx.web` module to your runtime arguments.
* **UI elements are too big/small or scrollbars appear**: Adjust the `prefHeight`, `prefWidth`, `minHeight`, `maxHeight`, `minWidth`, `maxWidth` properties in your `Login.fxml` and `Signup.fxml` files, especially for the `WebView` and the main `AnchorPane`/`VBox`. Adjust `spacing` and `padding` in `VBox` also.

---
Feel free to contribute, open issues, or suggest improvements!

**Important Note for Users:** You will need to manually locate and provide the absolute file paths for your local JavaFX SDK, Oracle JDBC driver (`ojdbcX.jar`), and JBCrypt library (`jbcrypt-0.4.jar`) when setting up this project, especially if running from the command line or configuring your IDE. These paths are unique to each developer's machine setup.
````
