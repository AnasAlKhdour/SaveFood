package com.example.savefoodapp.security;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {

    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 256;
    private static final int ITERATIONS = 100000;

    public static String generateSalt() {

        byte[] salt = new byte[SALT_LENGTH];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);

        return Base64.encodeToString(
                salt,
                Base64.NO_WRAP
        );
    }

    public static String hashPassword(
            String password,
            String salt
    ) {

        try {

            byte[] saltBytes = Base64.decode(
                    salt,
                    Base64.NO_WRAP
            );

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    saltBytes,
                    ITERATIONS,
                    HASH_LENGTH
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            byte[] hash = factory
                    .generateSecret(spec)
                    .getEncoded();

            spec.clearPassword();

            return Base64.encodeToString(
                    hash,
                    Base64.NO_WRAP
            );

        } catch (GeneralSecurityException e) {

            throw new RuntimeException(
                    "Password hashing failed",
                    e
            );
        }
    }

    public static boolean verifyPassword(
            String password,
            String salt,
            String storedHash
    ) {

        String generatedHash = hashPassword(
                password,
                salt
        );

        return generatedHash.equals(storedHash);
    }
}