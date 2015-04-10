package info.interactivesystems.gamificationengine.entities.rule;

import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class IdCollectorTest {

	@Test
	public void testVisitor() {
		Parser parser = new Parser();

		ExpressionNode expression = parser.parse(" 1 + 2 * 3");

		List<Integer> ids = new ArrayList<>();
		expression.accept(new IdCollector(ids));

		assertThat(ids).containsAllOf(1, 2, 3);
	}

}