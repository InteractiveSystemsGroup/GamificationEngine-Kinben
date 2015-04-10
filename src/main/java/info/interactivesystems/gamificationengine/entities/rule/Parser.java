package info.interactivesystems.gamificationengine.entities.rule;

import java.util.LinkedList;

/**
 * Parses following LL(1) grammar:
 * 
 * <ul>
 * <li>expression -> term or_op</li>
 * <li>or_op -> OR term or_op</li>
 * <li>or_op -> EPSILON</li>
 * 
 * <li>term -> and term_op</li>
 * <li>term_op -> AND and term_op</li>
 * <li>term_op -> EPSILON</li>
 * 
 * <li>and -> argument and_op</li>
 * <li>and_op -> EPSILON</li>
 * 
 * <li>argument -> OPEN_BRACKET expression CLOSE_BRACKET</li>
 * <li>argument -> NUMBER</li>
 * 
 * <li>value -> NUMBER</li>
 * </ul>
 */
public class Parser {
	LinkedList<Token> tokens;
	Token lookahead;

	public ExpressionNode parse(String s) {
		Tokenizer tokenizer = new Tokenizer();
		tokenizer.tokenize(s);
		return parse(tokenizer.getTokens());
	}

	public ExpressionNode parse(LinkedList<Token> tokens) {

		if (tokens.isEmpty()) {
			throw new ParserException("No tokens provided");
		}

		// no side effects on tokens reference
		this.tokens = new LinkedList<>(tokens);
		lookahead = this.tokens.getFirst();

		ExpressionNode expression = expression();

		if (Token.Type.EPSILON != lookahead.token) {
			throw new ParserException("Unexpected symbol %s found", lookahead);
		}

		return expression;
	}

	private void nextToken() {
		tokens.pop();
		// at the end of input we return an epsilon token
		if (tokens.isEmpty()) {
			lookahead = new Token(Token.Type.EPSILON, "");
		} else {
			lookahead = tokens.getFirst();
		}
	}

	private ExpressionNode expression() {
		// expression -> term or_op
		ExpressionNode expr = term();
		return orOp(expr);
	}

	private ExpressionNode orOp(ExpressionNode expr) {
		// or_op -> OR term or_op
		if (Token.Type.OR == lookahead.token) {
			OrExpressionNode or;
			if (ExpressionNode.Type.OR_NODE == expr.getType()) {
				or = (OrExpressionNode) expr;
			} else {
				or = new OrExpressionNode(expr);
			}

			nextToken();
			ExpressionNode t = term();
			or.add(t);

			return orOp(or);
		}

		// or_op -> EPSILON
		return expr;
	}

	private ExpressionNode term() {
		// term -> and term_op
		ExpressionNode f = and();
		return termOp(f);
	}

	private ExpressionNode termOp(ExpressionNode expression) {
		// term_op -> AND and term_op
		if (Token.Type.AND == lookahead.token) {
			AndExpressionNode prod;

			if (ExpressionNode.Type.AND_NODE == expression.getType()) {
				prod = (AndExpressionNode) expression;
			} else {
				prod = new AndExpressionNode(expression);
			}

			nextToken();
			ExpressionNode f = and();
			prod.add(f);

			return termOp(prod);
		}

		// term_op -> EPSILON
		return expression;
	}

	private ExpressionNode and() {
		// and -> argument and_op
		// and_op -> EPSILON
		return argument();
	}

	private ExpressionNode argument() {
		// argument -> OPEN_BRACKET or CLOSE_BRACKET
		if (Token.Type.OPEN_BRACKET == lookahead.token) {
			nextToken();
			ExpressionNode expr = expression();
			if (Token.Type.CLOSE_BRACKET != lookahead.token) {
				throw new ParserException("Closing brackets expected", lookahead);
			}
			nextToken();
			return expr;
		}

		// argument -> value
		return value();
	}

	private ExpressionNode value() {
		// argument -> NUMBER
		if (Token.Type.NUMBER == lookahead.token) {
			ExpressionNode expr = new ConstantExpressionNode(lookahead.sequence);
			nextToken();
			return expr;
		}

		if (Token.Type.EPSILON == lookahead.token) {
			throw new ParserException("Unexpected end of input");
		} else {
			throw new ParserException("Unexpected symbol %s found", lookahead);
		}
	}
}