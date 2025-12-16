package budgettracker;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    private static final int WORK_FACTOR = 12;

    /**
     * Hashes a plaintext password using the BCrypt algorithm.
     *
     * @param plainTextPassword The raw password string from the user.
     * @return The securely hashed password string, including the salt and cost
     * factor.
     */
    public static String hashPassword(String plainTextPassword) {
        // Generates a random salt and performs the hash
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Checks a plaintext password against a stored hashed password during
     * login.
     *
     * @param plainTextPassword The raw password string provided by the user.
     * @param storedHash The hash retrieved from the database.
     * @return true if the password matches the hash, false otherwise.
     */
    public static boolean checkPassword(String plainTextPassword, String storedHash) {
        // Performs the comparison securely
        return BCrypt.checkpw(plainTextPassword, storedHash);
    }

    
    /**
     * Checks if a plain-text password matches a hashed password stored in the
     * database.
     *
     * @param plainTextPassword The password entered by the user.
     * @param hashedPassword The password hash retrieved from the database.
     * @return true if the passwords match, false otherwise.
     */
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            return false;
        }

        // BCrypt handles the complex comparison, including extracting the salt
        // from the hashedPassword and running the plain-text password through the same hash process.
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (Exception e) {
            // Log or handle case where hash is malformed (e.g., database corruption)
            System.err.println("Password verification error: " + e.getMessage());
            return false;
        }
    }
}
