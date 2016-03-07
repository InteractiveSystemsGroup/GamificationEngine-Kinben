package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.Account;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Data access for an account.
 */
@Named
@Stateless
public class AccountDAO extends AbstractDAO<Account> {

	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	/**
	 * This method finds an account by email.
	 * 
	 * @param email
	 *            The valid email address that is unique.
	 * @return The Account that is associated with the email address
	 * 		   or null if not found.
	 */
	public Account getAccount(String email) {
		return em.find(Account.class, email);
	}

	/**
	 * Gets an account by its email address and password.
	 * 
	 * @param email
	 * 			The valid email address that is unique.
	 * @param password
	 * 			The password which is associated to the given email address.
	 * @return The Account that is associated with the email address
	 * 		   or null if not found.
	 */
	public Account getAccount(String email, String password) {
		
		Query query = em.createQuery("select entity from Account entity where entity.email=:email and entity.password=:password", Account.class);
		query.setParameter("email", email);
		query.setParameter("password", password);
		
		return (Account) query.getResultList().get(0);
	}

	
	/**
	 * Checks if email and password match.
	 * 
	 * @param email
	 *            The email address of an account.
	 * @param password
	 *            The password of an account.
	 * @return Boolean value that is true if email and password match and false if not.
	 */
	public boolean checkCredentials(String email, String password) {
		try {
			Query query = em.createQuery("select entity from Account entity where entity.email=:email and entity.password=:password");
			query.setParameter("email", email);
			query.setParameter("password", password);
			query.getSingleResult();
			return true;
		} catch (NoResultException e) {
			return false;
		}
	}
}
