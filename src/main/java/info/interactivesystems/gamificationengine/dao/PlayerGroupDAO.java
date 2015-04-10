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

	public PlayerGroup deletePlayerGroupByIdAndOrganisation(int groupId, Organisation organisation) {
		PlayerGroup plGroup = getPlayerGroupByIdAndOrganisation(groupId, organisation);
		em.remove(plGroup);
		return plGroup;
	}

	public List<PlayerGroup> getAllGroupsByOrganisation(Organisation organisation) {

		String apiKey = organisation.getApiKey();

		Query query = em.createQuery("select g from PlayerGroup g where g.belongsTo.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);

		return query.getResultList();

	}

}
