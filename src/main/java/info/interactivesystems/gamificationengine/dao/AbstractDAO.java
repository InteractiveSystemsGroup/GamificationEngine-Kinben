package info.interactivesystems.gamificationengine.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class AbstractDAO<T> {

	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	/**
	 * Persist an entity.
	 * 
	 * @param entity
	 *            required entity that should be stored in database
	 */
	public void persist(T entity) {
		em.persist(entity);
		em.flush();
	}

	/**
	 * Removes an entity from the database.
	 * 
	 * @param entity
	 *            required entity that sould be deleted
	 */
	public void remove(T entity) {
		em.remove(entity);
	}
}
