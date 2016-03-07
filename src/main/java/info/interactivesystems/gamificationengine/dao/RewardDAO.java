package info.interactivesystems.gamificationengine.dao;

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
	 * @param id
	 * 			The id of the requested reward.
	 * @param apiKey
	 *           The API key of the organisation to which the reward belongs to. 
	 * @return The {@link Reward} which is associated with the passed id and API key. 
	 */
	public Reward getReward(int id, String apiKey) {
		Query query = em.createQuery("select r from Reward r where r.belongsTo.apiKey=:apiKey and r.id in (:id)", Reward.class);
		query.setParameter("apiKey", apiKey);
		query.setParameter("id", id);

		return (Reward) query.getResultList();
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
	 * Gets all rewards with the passed ids which match also the passed API key.
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
	
	/**
	 * Removes a reward from the data base.
	 * 
	 * @param id
	 * 		 The id of the reward which should be deleted.
	 * @param apiKey
	 *           The API key of the organisation to which the reward belongs to. 
	 * @return The {@link Reward} that is associated with the passed id and API key.
	 */
	public Reward deleteReward(int id, String apiKey) {
		Reward reward = getReward(id, apiKey);
		
		if(reward!=null){
			em.remove(reward);
		}
		return reward;
	}
}
