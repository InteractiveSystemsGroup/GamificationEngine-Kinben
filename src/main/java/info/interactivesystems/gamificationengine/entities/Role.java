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
 * occupation or the department in which she/he works. But the roles can also be a 
 * part of an invented role system that isn’t oriented towards the work context. All 
 * roles are specific to the respective created organisation. 
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
	 * Gets the id of a role.
	 * 
	 * @return int of the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of a role.
	 * 
	 * @param id
	 *            The id of the role.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the name of a role.
	 * 
	 * @return the name of the role as String.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of a role.
	 * 
	 * @param name
	 *            The name for a role.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the organisation which the role belongs to and in which a player can
	 * have this role.
	 * 
	 * @return an organisation object.
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Sets the organisation which the role belongs to and in which a player can
	 * have this role.
	 * 
	 * @param belongsTo
	 *            The organisation object which is associated with the role.
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * This method checks if the API key of a role is equal to the organisation's one, which 
	 * means the role belongs to this organisation. If the role's API key is equal to the organiton's one
	 * it returns true otherwise false.
	 * 
	 * @param organisation
	 *            The Organisation with thate the API key of the role should be compared. This Organisation 
	 *            object may not be null.
	 * @return boolean
	 * 			  If both API keys are equal the mehtod return true otherwise false.
	 */
	public boolean belongsTo(Organisation organisation) {
		return getBelongsTo().getApiKey().equals(organisation.getApiKey());
	}

}
