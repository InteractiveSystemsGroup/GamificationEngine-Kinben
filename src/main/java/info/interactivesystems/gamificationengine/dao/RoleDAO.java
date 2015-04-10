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

	public int insert(Role role) {
		em.persist(role);
		return role.getId();
	}

	public Role getRoleById(int roleId) {
		return em.find(Role.class, roleId);
	}

	public List<Role> getRoles(String apiKey) {
		Query query = em.createQuery("select r from Role r where r.belongsTo.apiKey=:apiKey", Role.class);
		query.setParameter("apiKey", apiKey);

		return query.getResultList();
	}

	public Role getRole(int id, String apiKey) {
		Query query = em.createQuery("select r from Role r where r.belongsTo.apiKey=:apiKey and r.id = :id", Role.class);
		List list = QueryUtils.configureQuery(query, id, apiKey);
		if (list.isEmpty()) {
			return null;
		}
		return ((Role) list.get(0));
	}

	public Role delete(int roleId, String apiKey) {
		Role role = getRole(roleId, apiKey);

		if (role != null) {
			em.remove(role);
		}

		return role;
	}

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
