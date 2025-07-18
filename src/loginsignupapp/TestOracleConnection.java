package loginSignupapp;

import database.DBConnection;
import java.sql.Connection;

public class TestOracleConnection {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getConnection();
            System.out.println("✅ Connected to Oracle Database successfully!");
            conn.close();
        } catch (Exception e) {
            System.out.println("❌ Connection failed: " + e.getMessage());
        }
    }
}
