package info.interactivesystems.gamificationengine.entities.rule;

public interface ExpressionNodeVisitor {

	public void visit(ConstantExpressionNode node);

	public void visit(NotExpressionNode node);

	public void visit(OrExpressionNode node);

	public void visit(AndExpressionNode node);
}