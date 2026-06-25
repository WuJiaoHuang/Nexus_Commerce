package org.example.com.securityplatform.jwt;

public record JwtValidationResult(boolean valid, String username, String message) {
    public static JwtValidationResult success(String username) {
        return new JwtValidationResult(true, username, "OK");
    }

    public static JwtValidationResult failure(String message) {
        return new JwtValidationResult(false, null, message);
    }
}
