package info.interactivesystems.gamificationengine.entities.rule;

import javax.persistence.Entity;

@Entity
public class OrExpressionNode extends SequenceExpressionNode {

	public OrExpressionNode() {
		super();
	}

	public OrExpressionNode(ExpressionNode node) {
		super(node);
	}

	@Override
	public Type getType() {
		return Type.OR_NODE;
	}

	@Override
	public boolean evaluate() {
		// initialize with identity
		boolean or = false;
		for (ExpressionNode t : terms) {
			or |= t.evaluate();
		}
		return or;
	}

	public void accept(ExpressionNodeVisitor visitor) {
		for (ExpressionNode t : terms) {
			t.accept(visitor);
		}
	}
}