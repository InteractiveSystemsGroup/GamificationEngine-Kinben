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
 * An Organisation holds an API key. Each organisation may be managed by many
 * people, but at least by one. The api key is uniquely in whole application. It
 * may be changed, for this reason it has no primary key.
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
	 * Set the id of the organisation
	 * 
	 * @param the
	 *            new id of the organisation
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get the organisation's id.
	 * 
	 * @return organisation's id as int
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get all accounts of the organisation. Each account belongs to a manager.
	 * 
	 * @return Collection of all organisation's manager accounts
	 */
	public Collection<Account> getManagers() {
		return managers;
	}

	/**
	 * The list of the organisation's accounts. Each account belongs to a
	 * manager.
	 * 
	 * @param List
	 *            of all organisation's accounts
	 */
	public void setManagers(Collection<Account> managers) {
		this.managers = managers;
	}

	/**
	 * Get the organisation's name.
	 * 
	 * @return name of the organisation
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of an organisation.
	 * 
	 * @param the
	 *            name of the organisation
	 * 
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the organisation's unique apiKey.
	 * 
	 * @return the apiKey as a String
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * Set the organisation's apiKey.
	 * 
	 * @param apiKey
	 */
	public void setApiKey(@NotNull String apiKey) {
		this.apiKey = apiKey;
	}

	/**
	 * Add a new manager's account to the organisation's list of mccounts.
	 * 
	 * @param account
	 *            which should be added to the list
	 */
	public void addManager(@NotNull Account account) {
		getManagers().add(account);
	}
}
