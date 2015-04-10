package info.interactivesystems.gamificationengine.entities.rule;

import info.interactivesystems.gamificationengine.entities.task.Task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetTask implements ExpressionNodeVisitor {

	private static final Logger log = LoggerFactory.getLogger(SetTask.class);

	private Task task;

	public SetTask(Task task) {
		this.task = task;
	}

	@Override
	public void visit(ConstantExpressionNode node) {
		if (task.getId() == node.getValue()) {
			node.setObject(task, Task.class);
		} else {
			log.warn("Trying to assign task with id: %d, on node with value: %d", task.getId(), node.getValue());
		}
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