package info.interactivesystems.gamificationengine.api.exeption;

import info.interactivesystems.gamificationengine.api.ResponseSurrogate;

import javax.ejb.ApplicationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Application specific exception to provide custom error responses.
 */
@ApplicationException
public class ApiError extends WebApplicationException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param status
	 *            a HTTP status code.
	 * @param message
	 *            a description of the error cause.
	 */
	public ApiError(Response.Status status, String message) {
		this(status, message, new Object[] {});
	}

	/**
	 * @param status
	 *            a HTTP status code
	 * @param message
	 *            a description of the error cause, may be a formatted string.
	 * @param args
	 *            for format string, may be zero, see {@link String#format test}
	 *            .
	 */
	public ApiError(Response.Status status, String message, Object... args) {
		super(ResponseSurrogate.of(status, null, Notification.of(String.format(message, args))));
	}
}