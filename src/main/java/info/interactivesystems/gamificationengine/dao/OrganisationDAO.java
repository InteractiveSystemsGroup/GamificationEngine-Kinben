package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.Organisation;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Data access for developer organisations.
 */
@Named
@Stateless
public class OrganisationDAO {

	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	/**
	 * Store a new organisation in the database.
	 * 
	 * @param organisation
	 *            the {@link Organisation} that should be stored in the database
	 * @return the id of the created database entry
	 */
	public void insertOrganisation(Organisation organisation) {
		em.persist(organisation);
	}

	/**
	 * Get the organisation from the database.
	 * 
	 * @param id
	 *            the requested id
	 * @return {@link Organisation}
	 */
	public Organisation getOrganisation(int id) {
		return em.find(Organisation.class, id);
	}

	/**
	 * Get all Organisations which belong to an email address
	 * 
	 * @param email
	 *            the requested email address
	 * @return {@link List<Organisation>}
	 */
	public List<Organisation> getAllOrganisations(String email) {
		Query query = em.createQuery("select entity from Organisation entity join entity.managers m  where m.email=:email");
		query.setParameter("email", email);

		List list = query.getResultList();

		if (list.isEmpty()) {
			return null;
		}

		return list;
	}

	/**
	 * Get all Organisations which belong to an api key
	 * 
	 * @param apiKey
	 *            the requested api key
	 * @return {@link List<Organisation>}
	 */
	public Organisation getOrganisationByApiKey(String apiKey) {
		Query query = em.createQuery("select entity from Organisation entity where entity.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);

		List list = query.getResultList();

		if (list.isEmpty()) {
			return null;
		}

		return ((Organisation) list.get(0));
	}

	/**
	 * Checks whether the data base contains given api key.
	 * 
	 * @param apiKey
	 *            a {@link CharSequence} or a null
	 * @return true if apiKey exists in data base, false else or null.
	 */
	public boolean checkApiKey(CharSequence apiKey) {
		if (apiKey != null) {
			try {
				Query query = em.createQuery("select entity from Organisation entity where entity.apiKey=:apiKey");
				query.setParameter("apiKey", apiKey);
				return query.getSingleResult() != null;
			} catch (NoResultException e) {
				// don't care
			}
		}
		return false;
	}
}
