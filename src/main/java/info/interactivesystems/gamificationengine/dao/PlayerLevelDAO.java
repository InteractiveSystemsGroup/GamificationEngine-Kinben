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
	 * @param playerLevelId
	 * 			The id of the requested player level.
	 * @param apiKey
	 * 			   The API key affiliated to one specific organisation, to which
	 *            the player level belongs to.
	 * @return The {@link PlayerLevel} which is associated with the passed API key.
	 */
	public PlayerLevel getPlayerLevel(int playerLevelId, String apiKey) {
		Query query = em.createQuery("select pL from PlayerLevel pL where pL.belongsTo.apiKey=:apiKey and pL.id = :id", PlayerLevel.class);
		List list = QueryUtils.configureQuery(query, playerLevelId, apiKey);
		if (list.isEmpty()) {
			return null;
		}
		return ((PlayerLevel) list.get(0));
	}

	/**
	 * Get all player levels of one specific organisation.
	 * 
	 * @param apiKey
	 * 			The API key affiliated to one specific organisation, to which
	 *            all player levels belongs to.
	 * @return The list with all player levels of one specific organisation.
	 */
	public List<PlayerLevel> getPlayerLevels(String apiKey) {
		Query query = em.createQuery("select pl from PlayerLevel pl where pl.belongsTo.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}

	/**
	 * Removes a player level from the data base.
	 * 
	 * @param pLId
	 * 			The id of the requested player level.
	 * @param apiKey
	 * 			 The API key affiliated to one specific organisation, to which
	 *            all player levels belongs to.
	 * @return The {@link PlayerLevel} which is associated with the passed id and API key.
	 */
	public PlayerLevel deletePlayerLevel(int pLId, String apiKey) {
		PlayerLevel playerLevel = getPlayerLevel(pLId, apiKey);
		
		if (playerLevel != null) {
			em.remove(playerLevel);
		}
		
		return playerLevel;
	}
}
