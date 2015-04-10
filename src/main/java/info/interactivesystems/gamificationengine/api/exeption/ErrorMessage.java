package info.interactivesystems.gamificationengine.api.exeption;

import java.io.Serializable;

/**
 * Error message bean.
 */
public class ErrorMessage implements Serializable {

	private static final long serialVersionUID = 9039774438272543442L;

	private CharSequence message;

	public static ErrorMessage of(CharSequence message) {
		ErrorMessage errorMessage = new ErrorMessage();
		errorMessage.setMessage(message);
		return errorMessage;
	}

	public void setMessage(CharSequence message) {
		this.message = message;
	}

	public CharSequence getMessage() {
		return message;
	}
}
