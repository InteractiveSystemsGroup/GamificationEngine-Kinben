package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.Organisation;
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

	public PlayerGroup getOrganisation(int groupId) {
		return em.find(PlayerGroup.class, groupId);
	}

	
	public PlayerGroup getGroupByApiKey(String apiKey) {
		Query query = em.createQuery("select g from PlayerGroup g where g.belongsTo.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);

		return (PlayerGroup) query.getSingleResult();
	}

	/** 
	 * Gets a group of players by its id and organisation.
	 * 
	 * @param groupId
	 * 			The id of the requested group of players.
	 * @param organisation
	 * 			The organisaiton the group of players is associated with.
	 * @return The {@link PlayerGroup} which is associated with the passed id and organisation.
	 */
	public PlayerGroup getPlayerGroupByIdAndOrganisation(int groupId, Organisation organisation) {
		PlayerGroup plGroup = em.find(PlayerGroup.class, groupId);
		if (plGroup != null) {
			if (plGroup.belongsTo(organisation)) {
				return plGroup;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Removes a group of player from the data base.
	 * 
	 * @param groupId
	 * 			The id of the requested group of players.
	 * @param organisation
	 * 			The organisaiton the group of players is associated with.
	 * @return The {@link PlayerGroup} which is associated with the passed id and organisation.
	 */
	public PlayerGroup deletePlayerGroupByIdAndOrganisation(int groupId, Organisation organisation) {
		PlayerGroup plGroup = getPlayerGroupByIdAndOrganisation(groupId, organisation);
		em.remove(plGroup);
		return plGroup;
	}

	/**
	 * Gets all groups of players which are associated with a specific organisation.
	 * 
	 * @param organisation
	 * 			The organisation to which the groups of players belong to.
	 * @return The {@link List<PlayerGroup>} of all groups which belong to the passed organisaiton.
	 */
	public List<PlayerGroup> getAllGroupsByOrganisation(Organisation organisation) {

		String apiKey = organisation.getApiKey();

		Query query = em.createQuery("select g from PlayerGroup g where g.belongsTo.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);

		return query.getResultList();

	}

}
