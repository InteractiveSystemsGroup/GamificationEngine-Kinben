package info.interactivesystems.gamificationengine.api.exeption;

import javax.ws.rs.core.Response;

public class CredentialException extends ApiError {

	private static final long serialVersionUID = 1150052689036897642L;

	public CredentialException(CharSequence email) {
		super(Response.Status.UNAUTHORIZED, "The credentials are wrong for %s", email);
	}
}
