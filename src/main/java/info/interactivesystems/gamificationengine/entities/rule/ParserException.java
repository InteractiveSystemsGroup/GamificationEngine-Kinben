package info.interactivesystems.gamificationengine.entities.rule;

public class ParserException extends RuntimeException {
	private static final long serialVersionUID = 1350028997647477814L;

	public ParserException(String message) {
		super(message);
	}

	public ParserException(String message, Object symbol) {
		super(String.format(message, symbol));
	}
}