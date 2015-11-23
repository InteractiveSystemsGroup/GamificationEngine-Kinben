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
	 * Stores a new organisation in the data base.
	 * 
	 * @param organisation
	 *            The {@link Organisation} that should be stored in the data base.
	 * @return The id of the created data base entry.
	 */
	public void insertOrganisation(Organisation organisation) {
		em.persist(organisation);
	}

	/**
	 * Gets the organisation from the data base.
	 * 
	 * @param id
	 *           The id of the requsted organisaiton.
	 * @return The {@link Organisation} that is associated with the passed id.
	 */
	public Organisation getOrganisation(int id) {
		return em.find(Organisation.class, id);
	}

	/**
	 * Gets all organisations which belong to an email address.
	 * 
	 * @param email
	 *            The email address of the requested organisaiton.
	 * @return The {@link List<Organisation>} that are associated with the email addess.
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
	 * Gets all organisations which are associated with the specific API key.
	 * 
	 * @param apiKey
	 *           The API key to which the organisation belongs to.
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
	 * Checks whether the data base contains the passed API key.
	 * 
	 * @param apiKey
	 *           The API key that is tested. This is represented by a {@link CharSequence} 
	 *           or null.
	 * @return True if the API key exists in data base, if null false is returned.
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
