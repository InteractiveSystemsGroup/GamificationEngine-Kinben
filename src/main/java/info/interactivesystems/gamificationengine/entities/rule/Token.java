package info.interactivesystems.gamificationengine.entities.rule;

import javax.validation.constraints.NotNull;

/**
 * Immutable token
 */
public class Token {

	enum Type {
		EPSILON, OR, AND, OPEN_BRACKET, CLOSE_BRACKET, NUMBER
	}

	/* package */final Type token;
	/* package */final CharSequence sequence;

	/**
	 *
	 * @param token
	 *            identifier
	 * @param sequence
	 *            the character sequence the token is of
	 */
	public Token(@NotNull Type token, @NotNull CharSequence sequence) {
		super();
		this.token = token;
		this.sequence = sequence;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Token token1 = (Token) o;

		return sequence.equals(token1.sequence) && token == token1.token;

	}

	@Override
	public int hashCode() {
		int result = token.hashCode();
		result = 31 * result + sequence.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "Token{" + "token=" + token + ", sequence=" + sequence + '}';
	}
}