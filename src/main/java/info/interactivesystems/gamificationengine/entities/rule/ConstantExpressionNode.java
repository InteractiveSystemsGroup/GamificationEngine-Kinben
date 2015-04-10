package info.interactivesystems.gamificationengine.entities.rule;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

@Entity
public class ConstantExpressionNode extends ExpressionNode {

	private int value;

	@JsonUnwrapped
	@OneToOne(cascade = CascadeType.ALL)
	private TypedObject object;

	public ConstantExpressionNode() {
	}

	public ConstantExpressionNode(int value) {
		this.value = value;
	}

	public ConstantExpressionNode(CharSequence value) {
		this.value = Integer.valueOf(value.toString());
	}

	@Override
	public Type getType() {
		return Type.CONSTANT_NODE;
	}

	@Override
	public boolean evaluate() {
		return false;
	}

	public int getValue() {
		return value;
	}

	public void accept(ExpressionNodeVisitor visitor) {
		visitor.visit(this);
	}

	public <T extends Serializable> void setObject(T object, Class<T> clazz) {
		this.object = new TypedObject(object, clazz);
	}

	public <T extends Serializable> T getObject(Class<T> clazz) {
		return object.get(clazz);
	}

	public TypedObject getObject() {
		return object;
	}
}