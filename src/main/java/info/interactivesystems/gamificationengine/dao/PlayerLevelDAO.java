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

	/**
	 * Stores a new player level in the data base.
	 * 
	 * @param pLevel
	 * 			The player level which should be stored in the data base.
	 * @return The generated id of the player level. 
	 */
	public int insertPlayerLevel(PlayerLevel pLevel) {
		em.persist(pLevel);
		em.flush();
		return pLevel.getId();
	}

	/**
	 * Gets a player level by its id and organisation.
	 * 
	 * @param organisationId
	 * 			The of the organisaiton the player level is associated with.
	 * @param playerLevelId
	 * 			The id of the requested player level.
	 * @return 
	 */
	public PlayerLevel getPlayerLevel(String organisationId, int playerLevelId) {
		return em.find(PlayerLevel.class, playerLevelId);
	}

	public List<PlayerLevel> getPlayerLevels(String apiKey) {
		Query query = em.createQuery("select pl from PlayerLevel pl where pl.belongsTo.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}

	/**
	 * Removes a player level from the data base.
	 * 
	 * @param apikey
	 * 			  The API key of the organisation to which the player level belongs to.
	 * @param pLId
	 * 			The id of the requested player level.
	 * @return The {@link PlayerLevel} which is associated with the passed id and API key.
	 */
	public PlayerLevel deletePlayerLevel(String apikey, int pLId) {
		PlayerLevel playerLevel = getPlayerLevel(apikey, pLId);
		em.remove(playerLevel);
		return playerLevel;
	}
}
