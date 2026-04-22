package com.fernando.microservices.payment_service.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class WompiSignatureUtil {

    public static String buildIntegritySignature(
            String reference, long amountInCents, String currency, String secret) {
        try {
            String raw = reference + amountInCents + currency + secret;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build integrity signature", e);
        }
    }
}