package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.PlayerGroup;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Named
@Stateless
public class PlayerGroupDAO {

	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	/**
	 * Stores a new group of players in the data base.
	 * 
	 * @param group
	 *            The group of players which should be stored in the data base.
	 * @return The generated id of the group. 
	 */
	public int insertGroup(PlayerGroup group) {
		em.persist(group);
		return group.getId();
	}

	/**
	 * Gets a group of players by its id and API key.
	 * 
	 * @param id
	 *          The id of the requested group of players.
	 * @param apiKey
	 *            The API key of the organisation to which the group of players belongs to.
	 * @return The {@link PlayerGroup} that is associated with the passed id and API key.
	 */
	public PlayerGroup getPlayerGroup(int id, String apiKey) {
		Query query = em.createQuery("select g from PlayerGroup g where g.belongsTo.apiKey=:apiKey and g.id = :id", PlayerGroup.class);
		List list = QueryUtils.configureQuery(query, id, apiKey);
		if (list.isEmpty()) {
			return null;
		}
		return ((PlayerGroup) list.get(0));
	}

	
	/**
	 * Gets all groups of players which are associated with a specific organisation.
	 * 
	 * @param apiKey
	 *            The API key of the organisation to which the group of group players belongs to.
	 * @return The {@link List} of {@link PlayerGroup}s which belong to the passed API key.
	 */
	public List<PlayerGroup> getAllGroups(String apiKey) {
		Query query = em.createQuery("select g from PlayerGroup g where g.belongsTo.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);

		return query.getResultList();

	}
	
	/**
	 * Removes a group of player from the data base.
	 * 
	 * @param groupId
	 * 			The id of the requested group of players.
	 * @param apiKey
	 *            The API key of the organisation to which the group of group players belongs to.
	 * @return The {@link PlayerGroup} which is associated with the passed id and API key.
	 */
	public PlayerGroup deletePlayerGroup(int groupId, String apiKey) {
		PlayerGroup plGroup = getPlayerGroup(groupId, apiKey);
		
		if(plGroup != null){
			em.remove(plGroup);
		}
		return plGroup;
	}
}
