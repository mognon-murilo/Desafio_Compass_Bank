package br.com.compass.util;

import org.mindrot.jbcrypt.BCrypt;

public class HashUtil {
    public static String hashPassword(String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt());
    }

    public static boolean verifyPassword(String senha, String hash) {
        return BCrypt.checkpw(senha, hash);
    }
}
