package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.Role;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Named
@Stateless
public class RoleDAO {

	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	/**
	 * Stores a new role in the data base.
	 * 
	 * @param role
	 * 			The role which should be stored in the data base.
	 * @return The generated id of the role. 
	 */
	public int insert(Role role) {
		em.persist(role);
		return role.getId();
	}

	/**
	 * Gets the role by its id.
	 * 
	 * @param roleId
	 * 			The id of the requested role.
	 * @return The @link Role} which is associated with the passed id. 
	 */
	public Role getRoleById(int roleId) {
		return em.find(Role.class, roleId);
	}

	/**
	 * Gets all roles which are associated with the passed API key.
	 * 
	 * @param apiKey
	 * 			The API key of the organisation to which the roles belong to. 
	 * @return A {@link List} of {@link Role}s which are associated with the passed 
	 * 			API key.
	 */
	public List<Role> getRoles(String apiKey) {
		Query query = em.createQuery("select r from Role r where r.belongsTo.apiKey=:apiKey", Role.class);
		query.setParameter("apiKey", apiKey);

		return query.getResultList();
	}

	/**
	 * Gets a role by its id and API key.
	 * 
	 * @param id
	 * 			The id of the requested role.
	 * @param apiKey
	 * 			The API key of the organisation to which the role belongs to. 
	 * @return The {@link Role} which is associated with the passed id and API key.
	 */
	public Role getRole(int id, String apiKey) {
		Query query = em.createQuery("select r from Role r where r.belongsTo.apiKey=:apiKey and r.id = :id", Role.class);
		List list = QueryUtils.configureQuery(query, id, apiKey);
		if (list.isEmpty()) {
			return null;
		}
		return ((Role) list.get(0));
	}

	/**
	 * Removes a role from the data base.
	 * 		 
	 * @param roleId
	 * 			The id of the role which should be deleted.
	 * @param apiKey
	 * 			The API key of the organisation to which the role belongs to. 
	 * @return The {@link Role} that is associated with the passed id and API key.
	 */
	public Role deleteRole(int roleId, String apiKey) {
		Role role = getRole(roleId, apiKey);

		if (role != null) {
			em.remove(role);
		}

		return role;
	}

	/**
	 * Gets all roles with the passed ids which match the also passed API key.
	 * 
	 * @param ids
	 *			 A comma separated list of role ids.
	 * @param apiKey
	 * 			The API key of the organisation to which the roles belong to. 
	 * @return A {@link List} of {@link Role}s which are associated with the passed 
	 * 			API key.
	 */
	public List<Role> getRoles(List<Integer> ids, String apiKey) {
		Query query = em.createQuery("select r from Role r where r.belongsTo.apiKey=:apiKey and r.id in (:ids)", Role.class);
		query.setParameter("apiKey", apiKey);
		query.setParameter("ids", ids);
		return query.getResultList();
	}

	public List<Player> getPlayers(List<Integer> ids, String apiKey) {
		Query query = em.createQuery("select p from Player p where p.belongsTo.apiKey=:apiKey and p.id in (:ids)", Player.class);
		query.setParameter("apiKey", apiKey);
		query.setParameter("ids", ids);
		return query.getResultList();
	}
}
