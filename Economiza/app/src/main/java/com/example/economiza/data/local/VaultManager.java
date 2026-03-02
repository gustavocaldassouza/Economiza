package com.example.economiza.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Manages the encrypted vault lifecycle:
 * - Password creation & hashing (PBKDF2-HMAC-SHA256)
 * - Key derivation from user password
 * - Vault-exists detection
 * - Password verification
 *
 * The database key is NEVER stored directly. Instead:
 * - A random 16-byte salt is stored in plain SharedPreferences
 * - A PBKDF2 hash of the password is stored (for verification only)
 * - On each unlock, the key is re-derived from password + salt in memory
 */
public class VaultManager {

    private static final String PREFS_NAME = "vault_prefs";
    private static final String KEY_SALT = "vault_salt";
    private static final String KEY_VERIFY_HASH = "vault_verify_hash";
    private static final String KEY_VAULT_EXISTS = "vault_exists";

    // PBKDF2 parameters – 310,000 iterations is OWASP 2023 recommendation for
    // SHA-256
    private static final int PBKDF2_ITERATIONS = 310_000;
    private static final int KEY_LENGTH_BITS = 256; // database encryption key
    private static final int VERIFY_LENGTH_BITS = 256; // separate hash for verification
    private static final int SALT_BYTES = 16;

    private final SharedPreferences prefs;

    public VaultManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Returns true if a vault has been created on this device. */
    public boolean vaultExists() {
        return prefs.getBoolean(KEY_VAULT_EXISTS, false);
    }

    /**
     * Creates a new vault with the given password.
     * Generates a fresh random salt, stores it, and stores a PBKDF2 verification
     * hash.
     *
     * @throws IllegalStateException if a vault already exists
     */
    public byte[] createVault(String password) {
        if (vaultExists())
            throw new IllegalStateException("Vault already exists.");

        byte[] salt = generateSalt();
        prefs.edit()
                .putString(KEY_SALT, Base64.encodeToString(salt, Base64.DEFAULT))
                .putString(KEY_VERIFY_HASH, deriveVerificationHash(password, salt))
                .putBoolean(KEY_VAULT_EXISTS, true)
                .apply();

        return deriveDatabaseKey(password, salt);
    }

    /**
     * Unlocks the vault with the given password.
     *
     * @return the raw 256-bit database key if password is correct, null if wrong.
     */
    public byte[] unlockVault(String password) {
        if (!vaultExists())
            return null;

        byte[] salt = getSalt();
        if (salt == null)
            return null;

        String expectedHash = prefs.getString(KEY_VERIFY_HASH, null);
        String actualHash = deriveVerificationHash(password, salt);

        if (!constantTimeEquals(expectedHash, actualHash)) {
            return null; // Wrong password
        }

        return deriveDatabaseKey(password, salt);
    }

    /**
     * Deletes all vault metadata (for "factory reset"). Does NOT delete the DB
     * file.
     */
    public void destroyVault() {
        prefs.edit().clear().apply();
    }

    // ─── Private helpers ────────────────────────────────────────────────────────

    private byte[] getSalt() {
        String saltStr = prefs.getString(KEY_SALT, null);
        if (saltStr == null)
            return null;
        return Base64.decode(saltStr, Base64.DEFAULT);
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Derives the AES-256 database key from password + salt using
     * PBKDF2-HMAC-SHA256.
     */
    private byte[] deriveDatabaseKey(String password, byte[] salt) {
        return pbkdf2(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS);
    }

    /**
     * Derives a separate verification hash using a domain-separated salt so the
     * verification hash cannot be confused with the encryption key.
     */
    private String deriveVerificationHash(String password, byte[] salt) {
        byte[] domainSalt = domainSeparate(salt, "verify");
        byte[] hash = pbkdf2(password, domainSalt, PBKDF2_ITERATIONS, VERIFY_LENGTH_BITS);
        return Base64.encodeToString(hash, Base64.DEFAULT);
    }

    /**
     * Prepends a domain string to the salt to avoid key-reuse between key and
     * verifier.
     */
    private byte[] domainSeparate(byte[] salt, String domain) {
        byte[] domainBytes = domain.getBytes();
        byte[] combined = new byte[domainBytes.length + salt.length];
        System.arraycopy(domainBytes, 0, combined, 0, domainBytes.length);
        System.arraycopy(salt, 0, combined, domainBytes.length, salt.length);
        return combined;
    }

    private byte[] pbkdf2(String password, byte[] salt, int iterations, int keyBits) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyBits);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("PBKDF2 key derivation failed", e);
        }
    }

    /** Constant-time string comparison to prevent timing attacks. */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null)
            return false;
        if (a.length() != b.length())
            return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}
