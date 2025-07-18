package loginsignupapp;

import javafx.scene.web.WebView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class reCAPTCHAVerifier {

    // ✅ Your actual reCAPTCHA keys
    private final String SECRET_KEY = "6Lf1p4ErAAAAADrj39vih1XbSj6ubZ1MqVUMuuPt";

    private final WebView webView;
    private boolean isVerified = false;

    public reCAPTCHAVerifier(WebView webView) {
        this.webView = webView;
    }

    // ✅ This loads your recaptcha.html from localhost:8000
    public void loadCaptcha() {
        webView.getEngine().load("http://localhost:8000/recaptcha.html");
    }

    // ✅ To be called by JavaScript (if using WebView-to-Java bridge)
    public void onSuccess(String token) {
        new Thread(() -> this.isVerified = verifyTokenOnServer(token)).start();
    }

    // ✅ This returns true if the token has been successfully verified
    public boolean isVerified() {
        return this.isVerified;
    }

    // ✅ Contact Google server to verify the token
    private boolean verifyTokenOnServer(String token) {
        try {
            URL url = new URL("https://www.google.com/recaptcha/api/siteverify");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String params = "secret=" + SECRET_KEY + "&response=" + token;

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = params.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
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
}