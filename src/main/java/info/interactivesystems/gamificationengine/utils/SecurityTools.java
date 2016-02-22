package info.interactivesystems.gamificationengine.utils;

import java.util.UUID;

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

}
