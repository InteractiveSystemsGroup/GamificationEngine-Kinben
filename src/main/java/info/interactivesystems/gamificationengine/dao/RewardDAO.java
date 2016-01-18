package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.rewards.Reward;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Named
@Stateless
public class RewardDAO {
	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	/**
	 * Stores a new reward in the data base.
	 * 
	 * @param reward
	 * 			The reward which should be stored in the data base.
	 * @return The generated id of the reward. 
	 */
	public int insertReward(Reward reward) {
		em.persist(reward);
		em.flush();
		return reward.getId();
	}

	/**
	 * Gets the reward by its id.
	 * 
	 * @param rewardId
	 * 			The id of the requested reward.
	 * @return The {@link Reward} which is associated with the passed id. 
	 */
	public Reward getReward(int rewardId) {
		return em.find(Reward.class, rewardId);
	}

	/**
	 * Gets a reward by its id and organisation.
	 * 
	 * @param id
	 * 			The id of the requested reward.
	 * @param organisation
	 * 			The organisaiton the reward is associated with.
	 * @return The {@link Reward} which is associated with the passed id and organisation.
	 */
	public Reward getRewardByIdAndOrganisation(int id, Organisation organisation) {
		Reward reward = em.find(Reward.class, id);
		if (reward != null)
			if (reward.belongsTo(organisation)) {
				return reward;
			} else {
				return null;
			}
		else {
			return null;
		}
	}

	/**
	 * Gets all rewards which are associated with the passed API key.
	 * 
	 * @param apiKey
	 * 			The API key of the organisation to which the rewards belong to. 
	 * @return A {@link List} of {@link Reward}s which are associated with the passed 
	 * 			API key.
	 */
	public List<Reward> getRewards(String apiKey) {
		Query query = em.createQuery("select r from Reward r join r.belongsTo a where a.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}

	/**
	 * Removes a reward from the data base.
	 * 
	 * @param id
	 * 		 The id of the reward which should be deleted.
	 * @param organisation
	 * 		 The organisaiton the reward is associated with.
	 * @return The {@link Reward} that is associated with the passed id and organisation.
	 */
	public Reward deleteRewardByIdAndOrganisation(int id, Organisation organisation) {
		Reward reward = getRewardByIdAndOrganisation(id, organisation);
		em.remove(reward);
		return reward;
	}

	/**
	 * Gets all rewards with the passed ids which match the also passed API key.
	 * 
	 * @param ids
	 * 			A list of reward ids. 
	 * @param apiKey
	 * 			The API key of the organisation to which the rewards belong to. 
	 * @return A {@link List} of {@link Reward}s which are associated with the passed 
	 * 			API key.
	 */
	public List<Reward> getRewards(List<Integer> ids, String apiKey) {
		Query query = em.createQuery("select r from Reward r where r.belongsTo.apiKey=:apiKey and r.id in (:ids)", Reward.class);
		query.setParameter("apiKey", apiKey);
		query.setParameter("ids", ids);
		return query.getResultList();
	}
}
