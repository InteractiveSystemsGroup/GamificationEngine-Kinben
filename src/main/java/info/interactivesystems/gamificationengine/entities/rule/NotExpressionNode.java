package info.interactivesystems.gamificationengine.entities.rule;

public class NotExpressionNode extends ExpressionNode {

	private ExpressionNode argument;

	public NotExpressionNode(ExpressionNode argument) {
		this.argument = argument;
	}

	@Override
	public boolean evaluate() {
		return argument.evaluate();
	}

	@Override
	public Type getType() {
		return ExpressionNode.Type.NOT_NODE;
	}

	public void accept(ExpressionNodeVisitor visitor) {
		visitor.visit(this);
	}
}