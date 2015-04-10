package info.interactivesystems.gamificationengine.entities.rule;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizer for some expression
 */
public class Tokenizer {

	/**
	 * Information holder for each token
	 */
	private class TokenInfo {
		public final Pattern regex;
		public final Token.Type token;

		public TokenInfo(Pattern regex, Token.Type token) {
			this.regex = regex;
			this.token = token;
		}

		@Override
		public String toString() {
			return "TokenInfo{" + "regex=" + regex + ", token=" + token + '}';
		}
	}

	private LinkedList<TokenInfo> tokenInfos;
	private LinkedList<Token> tokens;

	public Tokenizer() {
		tokenInfos = new LinkedList<>();
		tokens = new LinkedList<>();

		configure();
	}

	private void configure() {

		add("\\(", Token.Type.OPEN_BRACKET);
		add("\\)", Token.Type.CLOSE_BRACKET);
		add("\\+", Token.Type.OR);
		add("\\*", Token.Type.AND);
		add("[0-9]+", Token.Type.NUMBER);
	}

	public void add(String regex, Token.Type token) {
		tokenInfos.add(new TokenInfo(Pattern.compile("^(" + regex + ")"), token));
	}

	public void tokenize(String str) {
		String s = str.trim();
		tokens.clear();

		while (s.length() != 0) {
			boolean match = false;

			for (TokenInfo info : tokenInfos) {
				Matcher m = info.regex.matcher(s);
				if (m.find()) {
					match = true;

					String tok = m.group().trim();
					tokens.add(new Token(info.token, tok));

					s = m.replaceFirst("").trim();
					break;
				}
			}
			if (!match) {
				throw new ParserException("Unexpected character in input %s ", s);
			}
		}
	}

	public LinkedList<Token> getTokens() {
		return tokens;
	}
}