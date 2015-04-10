package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;

import javax.ws.rs.core.Response;

public class ValidateUtils {

	/**
	 * Validates whether assigned object is null.
	 * 
	 * @param id
	 *            for output
	 * @param object
	 *            to be tested
	 *
	 * @return validated object identity
	 */
	public static <T> T requireNotNull(int id, T object) {
		if (object == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No such id: %s", id);
		}
		return object;
	}

	/**
	 * Validates whether assigned value is greater then zero.
	 * 
	 * @param id
	 *            for test
	 * @return validated integer identity
	 */
	public static int requireGreaterThenZero(int id) {
		if (id <= 0) {
			throw new ApiError(Response.Status.FORBIDDEN, "transferred integer has to be greater then zero");
		}
		return id;
	}

	/**
	 * Parses assigned string to an integer and validates it whether it is
	 * greater then zero. Supposes a valid string digit were passed.
	 * 
	 * @param id
	 *            for test
	 * @return validated integer identity
	 */
	public static int requireGreaterThenZero(String id) {
		return requireGreaterThenZero(Integer.valueOf(id));
	}
}
