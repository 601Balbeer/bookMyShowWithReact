package com.bms.bms_core.utilities;

import com.bms.bms_core.config.Constants;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    /** Hashes a plaintext password before it is stored. */
    public static String encryptPassword(String password){
        return BCrypt.hashpw(password, BCrypt.gensalt(Constants.PASSWORD_SALT_LENGTH));
    }
    /** Validates password and reports whether it is acceptable. */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
