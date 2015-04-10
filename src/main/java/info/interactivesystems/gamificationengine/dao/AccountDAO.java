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
	 * Find an account by email.
	 * 
	 * @param email
	 *            an valid email adress
	 * @return an Account or null if not found
	 */
	public Account getAccount(String email) {
		return em.find(Account.class, email);
	}

	/**
	 * Check if email and password match
	 * 
	 * @param email
	 *            email of an account
	 * @param password
	 *            password of an account
	 * @return true if email and password match, false if not
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
