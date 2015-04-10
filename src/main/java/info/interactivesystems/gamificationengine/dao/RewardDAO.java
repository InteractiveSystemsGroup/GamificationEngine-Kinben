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

	public int insertReward(Reward reward) {
		em.persist(reward);
		em.flush();
		return reward.getId();
	}

	public Reward getReward(int rewardId) {
		return em.find(Reward.class, rewardId);
	}

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

	public List<Reward> getRewards(String apiKey) {
		Query query = em.createQuery("select r from Reward r join r.belongsTo a where a.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}

	public Reward deleteRewardByIdAndOrganisation(int id, Organisation organisation) {
		Reward reward = getRewardByIdAndOrganisation(id, organisation);
		em.remove(reward);
		return reward;
	}

	public List<Reward> getRewards(List<Integer> ids, String apiKey) {
		Query query = em.createQuery("select r from Reward r where r.belongsTo.apiKey=:apiKey and r.id in (:ids)", Reward.class);
		query.setParameter("apiKey", apiKey);
		query.setParameter("ids", ids);
		return query.getResultList();
	}
}
