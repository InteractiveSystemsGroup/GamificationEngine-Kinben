package info.interactivesystems.gamificationengine.entities.rule;

import javax.persistence.Entity;

@Entity
public class AndExpressionNode extends SequenceExpressionNode {

	public AndExpressionNode() {
		super();
	}

	public AndExpressionNode(ExpressionNode node) {
		super(node);
	}

	@Override
	public Type getType() {
		return Type.AND_NODE;
	}

	@Override
	public boolean evaluate() {
		// initialize with identity
		boolean and = true;
		for (ExpressionNode t : terms) {
			and &= t.evaluate();
		}
		return and;
	}

	public void accept(ExpressionNodeVisitor visitor) {
		for (ExpressionNode t : terms) {
			t.accept(visitor);
		}
	}
}
