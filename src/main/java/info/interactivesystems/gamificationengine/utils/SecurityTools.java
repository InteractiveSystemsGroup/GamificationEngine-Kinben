package info.interactivesystems.gamificationengine.utils;

import java.util.UUID;

public class SecurityTools {

	private SecurityTools() {
	}

	/**
	 * Generates api-key by creating a new universal unique identifier (UUID).
	 * TODO: Maybe change this to a encrypted timestamp/salt pair to decrypt
	 * creation date.
	 * 
	 * @return a randomly generated code used as api key
	 */
	public static String generateApiKey() {
		return UUID.randomUUID().toString();
	}

}
