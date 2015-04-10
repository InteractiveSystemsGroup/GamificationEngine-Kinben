package info.interactivesystems.gamificationengine.entities.rule;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class ExpressionNode {

	enum Type {
		CONSTANT_NODE, OR_NODE, AND_NODE, NOT_NODE
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public abstract Type getType();

	public abstract boolean evaluate();

	public abstract void accept(ExpressionNodeVisitor visitor);

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ExpressionNode that = (ExpressionNode) o;

		if (id != that.id)
			return false;
		if (getType() != that.getType())
			return false;
		if (evaluate() != that.evaluate())
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int hashCode = 1;
		hashCode = 31 * hashCode + id;
		hashCode = 31 * hashCode + (getType() == null ? 0 : getType().hashCode());
		hashCode = 31 * hashCode + (evaluate() ? 0 : 1);
		return hashCode;
	}

	@Override
	public String toString() {
		return "type: " + getType() + ", value: " + evaluate();
	}
}