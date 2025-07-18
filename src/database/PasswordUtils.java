package database;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Handles password hashing and verification.
 */
public class PasswordUtils {

    /**
     * Hashes a plain-text password using BCrypt.
     * @param plainTextPassword The password to hash.
     * @return A securely hashed password string.
     */
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    /**
     * Checks a plain-text password against a stored BCrypt hash.
     * @param plainTextPassword The password to check.
     * @param hashedPassword The stored hash from the database.
     * @return true if the password matches the hash, false otherwise.
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        // It's important to check that the hashed password is not null or empty
        if (hashedPassword == null || !hashedPassword.startsWith("$2a$")) {
            return false;
        }
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}