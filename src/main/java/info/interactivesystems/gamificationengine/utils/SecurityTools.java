package info.interactivesystems.gamificationengine.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import javax.ws.rs.core.Response;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;

public class SecurityTools {

	private SecurityTools() {
	}

	/**
	 * Generates API-Key by creating a new universal unique identifier (UUID).
	 * TODO: Maybe change this to a encrypted timestamp/salt pair to decrypt
	 * creation date.
	 * 
	 * @return A randomly generated code used as API key
	 */
	public static String generateApiKey() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Encodes a password to a encoded password with SHA 512.
	 * @param plainText
	 * 			The original password.
	 * @return An encoded password.
	 */
	//Based on http://stackoverflow.com/questions/3103652/hash-string-via-sha-256-in-java
	public static String encryptWithSHA512(String plainText) {
	       MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-512");
			md.update(plainText.getBytes("UTF-8"));
			byte[] hashedPW = md.digest();
			
			String encoded = Base64.getEncoder().encodeToString(hashedPW);
			return encoded;
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new ApiError(Response.Status.FORBIDDEN, "The password cannot be hashed.");
		}
	       
	}
	
}
