package info.interactivesystems.gamificationengine.entities;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;

/**
 * An Organisation represents for example a specific company or an association which 
 * represents a group of people belonging together and which are participating in the 
 * gamification process.
 * An Organisation possessed an generated API key which is needed for all further interactions
 * because all database entries are associated with this unique key and so with the respective 
 * organisation. The API key is uniquely in the whole application. It
 * may be changed, for this reason it has no primary key. 
 * When an Organisation is created it has to be connected with an account. Each organisation 
 * may be managed by many people, but at least by one who is added to the list of the manager 
 * of the respective organisation and so also the Account. 
 */
@Entity
public class Organisation implements Serializable {

	private static final long serialVersionUID = -6830220885028070098L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Collection<Account> managers = new HashSet<>();

	private String name;

	@NotNull
	@Column(unique = true)
	private String apiKey;

	public Organisation(String name) {
		super();
		this.name = name;
	}

	public Organisation() {
		super();
	}

	/**
	 * Sets the id of the created organisation.
	 * 
	 * @param id 
	 *           The new id of the organisation
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the organisation's id.
	 * 
	 * @return organisation's id as int
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets all accounts of the organisation which are associated to this organisation. 
	 * Each account belongs to a manager.
	 * 
	 * @return Collection of all organisation's manager accounts
	 */
	public Collection<Account> getManagers() {
		return managers;
	}

	/**
	 * Sets the list of the organisation's accounts. Each account belongs to a
	 * manager.
	 * 
	 * @param managers
	 *             List of all organisation's accounts
	 */
	public void setManagers(Collection<Account> managers) {
		this.managers = managers;
	}

	/**
	 * Gets the organisation's name and returns it as a String.
	 * 
	 * @return name of the organisation as String.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of an organisation.
	 * 
	 * @param name
	 *            The name of the organisation.
	 * 
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the organisation's unique API key and returns it.
	 * 
	 * @return the apiKey as a String
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * Sets the organisation's API key.
	 * 
	 * @param apiKey
	 *		 	 The API key which is unique and specific for one organisation.
	 */
	public void setApiKey(@NotNull String apiKey) {
		this.apiKey = apiKey;
	}

	/**
	 * Adds a new manager's account to the organisation's list of accounts.
	 * 
	 * @param account
	 *            which should be added to the list
	 */
	public void addManager(@NotNull Account account) {
		getManagers().add(account);
		//this.managers.add(account);
	}
}
