package info.interactivesystems.gamificationengine.entities.rule;

import static com.google.common.truth.Truth.assertThat;

import java.util.stream.Collectors;

import org.junit.Test;

import com.google.common.truth.Ordered;

public class ExpressionNodeTest {

	@Test
	public void testSimpleExpression() {

		// 1 * 2 + 3

		AndExpressionNode andNode = new AndExpressionNode();
		ConstantExpressionNode constNode1 = new ConstantExpressionNode(1);
		ConstantExpressionNode constNode2 = new ConstantExpressionNode(2);
		andNode.add(constNode1);
		andNode.add(constNode2);

		OrExpressionNode orNode = new OrExpressionNode();
		ConstantExpressionNode constNode3 = new ConstantExpressionNode(3);
		orNode.add(andNode);
		orNode.add(constNode3);

		assertThat(orNode.evaluate()).isEqualTo(false);

		check(andNode, constNode1, constNode2);
		check(orNode, andNode, constNode3);
	}

	@Test
	public void testSimpleBracketsExpression() {

		// 1 * ( 2 + 3 )

		OrExpressionNode orNode = new OrExpressionNode();
		ConstantExpressionNode constNode2 = new ConstantExpressionNode(2);
		ConstantExpressionNode constNode3 = new ConstantExpressionNode(3);
		orNode.add(constNode2);
		orNode.add(constNode3);

		AndExpressionNode andNode = new AndExpressionNode();
		ConstantExpressionNode constNode1 = new ConstantExpressionNode(1);
		andNode.add(constNode1);
		andNode.add(orNode);

		assertThat(andNode.evaluate()).isEqualTo(false);

		check(orNode, constNode2, constNode3);
		check(andNode, constNode1, orNode);
	}

	public Ordered check(SequenceExpressionNode sequenceExpressionNode, Object first, Object second, Object... rest) {
		return assertThat(sequenceExpressionNode.terms.stream().collect(Collectors.toList())).containsAllOf(first, second, rest);
	}

}