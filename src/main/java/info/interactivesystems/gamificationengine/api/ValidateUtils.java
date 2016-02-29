package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;

import javax.ws.rs.core.Response;

public class ValidateUtils {

	/**
	 * Validates whether the assigned object is null.
	 * 
	 * @param id
	 *           The id of the object. This is needed for output.
	 * @param object
	 *          The object that is tested.
	 *
	 * @return Validated object identity
	 */
	public static <T> T requireNotNull(int id, T object) {
		if (object == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No such id: %s", id);
		}
		return object;
	}

	/**
	 * Validates whether the assigned value is greater than zero.
	 * 
	 * @param id
	 *         The id that is is tested. 
	 * @return Validated integer identity.
	 */
	public static int requireGreaterThanZero(int id) {
		if (id <= 0) {
			throw new ApiError(Response.Status.FORBIDDEN, "transferred integer has to be greater than zero");
		}
		return id;
	}

	/**
	 * Parses the assigned String to an integer and validates it whether it is
	 * greater than zero. Supposes a valid string digit was passed.
	 * 
	 * @param id
	 *         The String of id that is tested.
	 * @return Validated integer identity.
	 */
	public static int requireGreaterThanZero(String id) {
		return requireGreaterThanZero(Integer.valueOf(id));
	}
}
