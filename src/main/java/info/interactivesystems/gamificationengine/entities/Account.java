package info.interactivesystems.gamificationengine.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * An Account is identified by a unique email address. The password ensures the
 * identity. Optionally an account may have information about the user in form
 * of first name and last name.
 */
@Entity
public class Account implements Serializable {

	private static final long serialVersionUID = -683022088070098L;

	@Id
	@NotNull
	private String email;

	@NotNull
	private String password;

	private String firstName;

	private String lastName;

	public Account(String email) {
		super();
		this.email = email;
	}

	public Account() {
		super();
	}

	/**
	 * Gets the email address of an Account.
	 * 
	 * @return String representing the email address.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Set the email address for an Account.
	 * 
	 * @param email
	 *            the email address for the Account
	 */
	public void setEmail(@NotNull String email) {
		this.email = email;
	}

	/**
	 * Gets the password for the Account.
	 * 
	 * @return String representing the password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password for the Account.
	 * 
	 * @param password
	 *            the password for the Account
	 */
	public void setPassword(@NotNull String password) {
		this.password = password;
	}

	/**
	 * Gets the first name of the Account user.
	 * 
	 * @return String representing the first Name.
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Sets the first name of the Account user.
	 * 
	 * @param firstName
	 *            the first name of the Account user
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Gets the last name of the Account user.
	 * 
	 * @return String representing the last name.
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Sets the last name of the Account user.
	 * 
	 * @param lastName
	 *            the last name of the Account user
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}