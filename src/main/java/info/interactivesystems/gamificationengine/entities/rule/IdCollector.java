package info.interactivesystems.gamificationengine.entities.rule;

import java.util.List;

public class IdCollector implements ExpressionNodeVisitor {

	private List<Integer> list;

	public IdCollector(List<Integer> list) {
		this.list = list;
	}

	@Override
	public void visit(ConstantExpressionNode node) {
		list.add(node.getValue());
	}

	@Override
	public void visit(NotExpressionNode node) {
		// ignored
	}

	@Override
	public void visit(OrExpressionNode node) {
		// ignored
	}

	@Override
	public void visit(AndExpressionNode node) {
		// ignored
	}
}
