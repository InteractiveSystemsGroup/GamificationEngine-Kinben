package info.interactivesystems.gamificationengine.entities.rule;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public abstract class SequenceExpressionNode extends ExpressionNode {

	@JsonProperty
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	protected List<ExpressionNode> terms;

	public SequenceExpressionNode() {
		terms = new LinkedList<>();
	}

	public SequenceExpressionNode(ExpressionNode node) {
		terms = new LinkedList<>();
		terms.add(node);
	}

	public void add(ExpressionNode node) {
		// don't add self, else creating loop
		if (equals(node)) {
			return;
		}
		terms.add(node);
	}

	@Override
	public String toString() {
		return "SequenceExpressionNode{" + "terms=" + terms + '}';
	}
}