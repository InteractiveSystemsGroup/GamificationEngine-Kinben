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
	 *            Entity that should be stored in database. This field  must not be null.
	 */
	public void persist(T entity) {
		em.persist(entity);
		em.flush();
	}

	/**
	 * Removes an entity from the data base.
	 * 
	 * @param entity
	 *            The entity that should be deleted. This field  must not be null.
	 */
	public void remove(T entity) {
		em.remove(entity);
	}
}
