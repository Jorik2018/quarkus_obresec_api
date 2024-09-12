package gob.regionancash.obresec.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("crypto")
public class CryptoController {

    @POST
    @Path("code-challenge")
    public String generateCodeChallenge(String codeVerifier) {
        try {
            // Generate SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            
            // Convert the hashed bytes to Base64URL (as per PKCE requirements)
            String base64String = Base64.getEncoder().encodeToString(hashedBytes);
            
            // Modify to Base64URL format: replace + with -, / with _, and remove padding
            String codeChallenge = base64String
                .replace('+', '-')
                .replace('/', '_')
                .replace("=", "");
            
            return codeChallenge;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating code challenge", e);
        }
    }
}
