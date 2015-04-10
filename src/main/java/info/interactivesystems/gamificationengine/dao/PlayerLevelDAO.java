package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.PlayerLevel;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Named
@Stateless
public class PlayerLevelDAO {

	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	public int insertPlayerLevel(PlayerLevel pLevel) {
		em.persist(pLevel);
		em.flush();
		return pLevel.getId();
	}

	public PlayerLevel getPlayerLevel(String organisationId, int playerLevelId) {
		// EntityPK epk = new EntityPK();
		// epk.setOrganisationId(organisationId);
		// epk.setId(playerLevelId);
		// return em.find(PlayerLevel.class, epk);
		return em.find(PlayerLevel.class, playerLevelId);
	}

	public List<PlayerLevel> getPlayerLevels(String apiKey) {
		Query query = em.createQuery("select pl from PlayerLevel pl where pl.belongsTo.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}

	public PlayerLevel deletePlayerLevel(String apikey, int pLId) {
		PlayerLevel playerLevel = getPlayerLevel(apikey, pLId);
		em.remove(playerLevel);
		return playerLevel;
	}
}
