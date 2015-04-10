package info.interactivesystems.gamificationengine.api.exeption;

import info.interactivesystems.gamificationengine.api.ResponseSurrogate;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ResteasyViolationException;

@Provider
public class ResteasyViolationExceptionMapper implements ExceptionMapper<ResteasyViolationException> {

	@Override
	public Response toResponse(ResteasyViolationException ex) {
		Response.Status status = Response.Status.PRECONDITION_FAILED;
		List<ResteasyConstraintViolation> constraintViolations = ex.getViolations();

		if (ex.getException() != null) {
			// There are some exceptions during validation
			throw new RuntimeException(ex.getException());
		}
		Notification notification = new Notification();
		constraintViolations.forEach(c -> notification.addError(c.getValue() + ": " + c.getMessage()));

		return ResponseSurrogate.of(status, null, notification);
	}
}