package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.Player;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Data-access for user organisations. All dependent objects (i.e. userbadges)
 * are implicitly loaded.
 *
 */
@Named
@Stateless
public class PlayerDAO {

	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	/**
	 * Store a new player in the database.
	 * 
	 * @param player
	 *            the player which should be stored in the database
	 */
	public void insert(Player player) {
		em.persist(player);
	}

	/**
	 * Store a list of new players in the database.
	 * 
	 * @param playerList
	 *            the player list which should be stored in the database
	 */
	public void insert(List<Player> playerList) {
		for (Player player : playerList) {
			em.persist(player);
		}
	}

	/**
	 * Get a player by id and api key.
	 * 
	 * @param id
	 *            the requested id
	 * @param apiKey
	 *            a valid api key
	 * @return {@link Player}
	 */
	public Player getPlayer(int id, String apiKey) {
		Query query = em.createQuery("select p from Player p where p.belongsTo.apiKey=:apiKey and p.id = :id", Player.class);
		List list = QueryUtils.configureQuery(query, id, apiKey);
		if (list.isEmpty()) {
			return null;
		}
		return ((Player) list.get(0));
	}

	/**
	 * Get a list of players by ids and api key.
	 * 
	 * @param receiverIds
	 *            a list of requested ids
	 * @param apiKey
	 *            a valid api key
	 * @return {@link List<Player>}
	 */
	public List<Player> getPlayers(List<Integer> receiverIds, String apiKey) {
		Query query = em.createQuery("select p from Player p where p.belongsTo.apiKey=:apiKey and p.id in (:receiverIds)", Player.class);
		query.setParameter("apiKey", apiKey);
		query.setParameter("receiverIds", receiverIds);

		return query.getResultList();
	}

	/**
	 * Delete a player from database.
	 * 
	 * @param id
	 *            of the player which should be deleted
	 * @param apiKey
	 *            a valid api key
	 * @return {@link Player}
	 */
	public Player deletePlayer(int id, String apiKey) {
		Player player = getPlayer(id, apiKey);

		if (player != null) {
			em.remove(player);
		}

		return player;
	}

	/**
	 * Get a list of all player which belong to an api key
	 * 
	 * @param apiKey
	 *            the requested api key
	 * @return {@link List<Player>}
	 */
	public List<Player> getPlayers(String apiKey) {
		Query query = em.createQuery("select p from Player p where p.belongsTo.apiKey=:apiKey", Player.class);
		query.setParameter("apiKey", apiKey);

		return query.getResultList();
	}
}
