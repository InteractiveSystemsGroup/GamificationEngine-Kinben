package info.interactivesystems.gamificationengine.entities.rule;

import static com.google.common.truth.Truth.assertThat;

import info.interactivesystems.gamificationengine.entities.task.Task;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

public class SetTaskTest {

	@Test
	public void testVisit() {

		Parser parser = new Parser();

		ExpressionNode expression = parser.parse(" 1 + 2 * 3 ");

		Task task1 = new Task();
		task1.setId(1);
		Task task2 = new Task();
		task2.setId(2);
		Task task3 = new Task();
		task3.setId(3);

		expression.accept(new SetTask(task1));
		expression.accept(new SetTask(task2));
		expression.accept(new SetTask(task3));

		ArrayList<Task> list = new ArrayList<>();
		expression.accept(new TypedObjectVisitor(list));

		assertThat(expression.evaluate()).isEqualTo(false);
		assertThat(list).containsAllOf(task1, task2, task3);
	}

	static class TypedObjectVisitor implements ExpressionNodeVisitor {

		private Collection<Task> list;

		public TypedObjectVisitor(Collection<Task> list) {
			this.list = list;
		}

		@Override
		public void visit(ConstantExpressionNode node) {
			list.add(node.getObject(Task.class));
		}

		@Override
		public void visit(NotExpressionNode node) {

		}

		@Override
		public void visit(OrExpressionNode node) {

		}

		@Override
		public void visit(AndExpressionNode node) {

		}
	}
}