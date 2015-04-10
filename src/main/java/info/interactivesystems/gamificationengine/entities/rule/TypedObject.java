package info.interactivesystems.gamificationengine.entities.rule;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
class TypedObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@JsonProperty
	@Lob
	private Serializable object;
	private Class<?> clazz;

	public <T extends Serializable> TypedObject(T object, Class<T> clazz) {
		this.object = object;
		this.clazz = clazz;
	}

	public <T extends Serializable> T get(Class<T> clazz) {
		return clazz.cast(object);
	}

	public TypedObject() {
	}

	public Serializable getObject() {
		return object;
	}
}
