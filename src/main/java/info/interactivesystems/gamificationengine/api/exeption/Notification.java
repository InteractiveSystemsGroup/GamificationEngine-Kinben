package info.interactivesystems.gamificationengine.api.exeption;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Notification {

	private final List<ErrorMessage> errors = new ArrayList<>();
	
	public static Notification of(CharSequence... messages) {
		Notification notification = new Notification();
		Stream.of(messages).forEach(notification::addError);
		return notification;
	}

	public void addError(CharSequence message) {
		errors.add(ErrorMessage.of(message));
	}

	public List<ErrorMessage> getErrors() {
		return errors;
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

}