package info.interactivesystems.gamificationengine.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * A role describes which members of an organisation is allowed to do or see
 * particular elements of the engine such as to fulfil a particular task and get
 * its rewards. Each Player can have many different roles such as one for his
 * occupation or the department in which he works.
 */
@Entity
public class Role implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@ManyToOne
	private Organisation belongsTo;

	private String name;

	// GETTER & SETTER
	/**
	 * Get the id of a role.
	 * 
	 * @return int of the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the id of a role.
	 * 
	 * @param id
	 *            the id of the role.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get the name of a role.
	 * 
	 * @return the name of the role as String.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of a role.
	 * 
	 * @param name
	 *            the name for a role.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the organisation which the role belongs to and in which a player can
	 * have this role.
	 * 
	 * @return an organisation object.
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Set the organisation which the role belongs to and in which a player can
	 * have this role.
	 * 
	 * @param belongsTo
	 *            the organisation object.
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * This method checks if the API key of a role is equal to the
	 * organisation's one, which means the role belongs to this organisation.
	 * 
	 * @param organisation
	 *            an Organisation may not be null.
	 * @return boolean
	 */
	public boolean belongsTo(Organisation organisation) {
		return getBelongsTo().getApiKey().equals(organisation.getApiKey());
	}

}
