package info.interactivesystems.gamificationengine.entities.rule;

import static com.google.common.truth.Truth.assertThat;

import java.util.LinkedList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TokenizerTest {

	@Test
	public void testSimpleExpression() {
		Tokenizer tokenizer = new Tokenizer();

		tokenizer.tokenize(" 1 * 2 + 3 **");

		LinkedList<Token> expectedTokens = new LinkedList<>();
		expectedTokens.add(new Token(Token.Type.NUMBER, "1"));
		expectedTokens.add(new Token(Token.Type.AND, "*"));
		expectedTokens.add(new Token(Token.Type.NUMBER, "2"));
		expectedTokens.add(new Token(Token.Type.OR, "+"));
		expectedTokens.add(new Token(Token.Type.NUMBER, "3"));
		expectedTokens.add(new Token(Token.Type.AND, "*"));
		expectedTokens.add(new Token(Token.Type.AND, "*"));

		assertThat(tokenizer.getTokens()).isEqualTo(expectedTokens);
	}

	@Test
	public void testSimpleBracketExpression() {
		Tokenizer tokenizer = new Tokenizer();

		tokenizer.tokenize(" 1 * (2 + 3) **");

		LinkedList<Token> expectedTokens = new LinkedList<>();
		expectedTokens.add(new Token(Token.Type.NUMBER, "1"));
		expectedTokens.add(new Token(Token.Type.AND, "*"));
		expectedTokens.add(new Token(Token.Type.OPEN_BRACKET, "("));
		expectedTokens.add(new Token(Token.Type.NUMBER, "2"));
		expectedTokens.add(new Token(Token.Type.OR, "+"));
		expectedTokens.add(new Token(Token.Type.NUMBER, "3"));
		expectedTokens.add(new Token(Token.Type.CLOSE_BRACKET, ")"));
		expectedTokens.add(new Token(Token.Type.AND, "*"));
		expectedTokens.add(new Token(Token.Type.AND, "*"));

		assertThat(tokenizer.getTokens()).isEqualTo(expectedTokens);
	}

	@Test
	public void testTokenizerDoesNotContainEpsilon() {
		Tokenizer tokenizer = new Tokenizer();

		tokenizer.tokenize("");
		assertThat(tokenizer.getTokens()).doesNotContain(new Token(Token.Type.EPSILON, ""));
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testInvalidExpression() {
		Tokenizer tokenizer = new Tokenizer();

		thrown.expect(ParserException.class);
		tokenizer.tokenize(" 1 - 1 ");
	}

}