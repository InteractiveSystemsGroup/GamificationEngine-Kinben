package info.interactivesystems.gamificationengine.entities.rule;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParserTest {

	@Test
	public void testSimpleExpression() {
		Parser parser = new Parser();
		ExpressionNode expression = parser.parse("1 * 2 + 3");

		AndExpressionNode andNode = new AndExpressionNode();
		ConstantExpressionNode constNode1 = new ConstantExpressionNode(1);
		ConstantExpressionNode constNode2 = new ConstantExpressionNode(2);
		andNode.add(constNode1);
		andNode.add(constNode2);

		OrExpressionNode orNode = new OrExpressionNode();
		ConstantExpressionNode constNode3 = new ConstantExpressionNode(3);
		orNode.add(andNode);
		orNode.add(constNode3);

		assertThat(expression.evaluate()).isEqualTo(false);
		assertThat(expression).isEqualTo(orNode);
	}

	@Test
	public void testSimpleBracketsExpression() {
		Parser parser = new Parser();
		ExpressionNode expression = parser.parse("1 * (2 + 3)");

		OrExpressionNode orNode = new OrExpressionNode();
		ConstantExpressionNode constNode2 = new ConstantExpressionNode(2);
		ConstantExpressionNode constNode3 = new ConstantExpressionNode(3);
		orNode.add(constNode2);
		orNode.add(constNode3);

		AndExpressionNode andNode = new AndExpressionNode();
		ConstantExpressionNode constNode1 = new ConstantExpressionNode(1);
		andNode.add(constNode1);
		andNode.add(orNode);

		assertThat(expression.evaluate()).isEqualTo(false);
		assertThat(expression).isEqualTo(andNode);
	}

	@Test
	public void testExpressionResultsToSameTree() {

		ExpressionNode[] expressionNodes = { new Parser().parse("1 * 2 + 3 + 4"), //
				new Parser().parse("(1 * 2) + 3 + 4"), //
				new Parser().parse("3 + 1 * 2 + 4"), //
				new Parser().parse("3 + (1 * 2) + 4"), //
				new Parser().parse("3 + ((1 * 2) + 4)"), //
				new Parser().parse("(3 + (1 * 2)) + 4"), //
				new Parser().parse("(3 + 1 * 2 + 4)"), //
		};

		for (ExpressionNode expressionNode : expressionNodes) {
			for (ExpressionNode expression : expressionNodes) {
				assertThat(expressionNode).isEqualTo(expression);
			}
		}

		ExpressionNode tree = new Parser().parse("(3 + 1) * (2 + 4)");
		for (ExpressionNode expressionNode : expressionNodes) {
			assertThat(expressionNode).isNotEqualTo(tree);
		}
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test(expected = ParserException.class)
	public void testInvalidExpressionEmptyNotParsed() {
		new Parser().parse(" ");
	}

	@Test(expected = ParserException.class)
	public void testInvalidExpressionEmptyBracketsNotParsed() {
		new Parser().parse("( )");
	}

	@Test(expected = ParserException.class)
	public void testInvalidExpressionAndOperatorNotParsed() {
		new Parser().parse("*");
	}

	@Test(expected = ParserException.class)
	public void testInvalidExpressionAndOperatorLeftWrongNotParsed() {
		new Parser().parse("*1");
	}

	@Test(expected = ParserException.class)
	public void testInvalidExpressionAndOperatorRightWrongNotParsed() {
		new Parser().parse("1*");
	}

	@Test(expected = ParserException.class)
	public void testInvalidExpressionOpenBracketNotParsed() {
		new Parser().parse("(");
	}

	@Test(expected = ParserException.class)
	public void testInvalidExpressionClosedBracketNotParsed() {
		new Parser().parse(")");
	}

	@Test(expected = ParserException.class)
	public void testInvalidExpressionWrongPlacedBracketsNotParsed() {
		new Parser().parse(")(");
	}

	@Test(expected = ParserException.class)
	public void testInvalidExpressionOrOperatorNotParsed() {
		new Parser().parse("+");
	}

	@Test(expected = ParserException.class)
	public void testInvalidExpressionOrOperatorLeftWrongNotParsed() {
		new Parser().parse("+1");
	}

	@Test(expected = ParserException.class)
	public void testInvalidExpressionOrOperatorRightWrongNotParsed() {
		new Parser().parse("1+");
	}

	@Test(expected = ParserException.class)
	public void testInvalidExpressionNotParsed() {
		new Parser().parse("1 * +2");
	}
}