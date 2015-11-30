package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.Player;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Data-access for user of an organisation. All dependent objects (i.e. badges of a user)
 * are implicitly loaded.
 *
 */
@Named
@Stateless
public class PlayerDAO {

	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	/**
	 * Stores a new player in the data base.
	 * 
	 * @param player
	 *            The player who should be stored in the data base.
	 */
	public void insert(Player player) {
		em.persist(player);
	}

	/**
	 * Stores a list of new players in the data base.
	 * 
	 * @param playerList
	 *            The list of players who should be stored in the data base.
	 */
	public void insert(List<Player> playerList) {
		for (Player player : playerList) {
			em.persist(player);
		}
	}

	/**
	 * Gets a player by her/his id and API key.
	 * 
	 * @param id
	 *          The id of the requested player.
	 * @param apiKey
	 *            The API key of the organisation to which the player belongs to.
	 * @return The {@link Player} that is associated with the passed id and APi key.
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
	 * Gets a list of players by their ids and the API key.
	 * 
	 * @param receiverIds
	 *           A list of ids which represent the requested players.
	 * @param apiKey
	 *           The API key of the organisation to which the players belong to. 
	 * @return The {@link List} of {@link Player}s who are associated with the passed ids and API key.
	 */
	public List<Player> getPlayers(List<Integer> receiverIds, String apiKey) {
		Query query = em.createQuery("select p from Player p where p.belongsTo.apiKey=:apiKey and p.id in (:receiverIds)", Player.class);
		query.setParameter("apiKey", apiKey);
		query.setParameter("receiverIds", receiverIds);

		return query.getResultList();
	}

	/**
	 * Removes a player from the data base.
	 * 
	 * @param id
	 *           The id of the player who should be deleted.
	 * @param apiKey
	 *           The API key of the organisation to which the player belongs to. 
	 * @return The {@link Player} that is associated with the passed id and APi key.
	 */
	public Player deletePlayer(int id, String apiKey) {
		Player player = getPlayer(id, apiKey);

		if (player != null) {
			em.remove(player);
		}

		return player;
	}

	/**
	 * Gets a list of all players who are associated with the passed API key.
	 * 
	 * @param apiKey
	 *           The API key of the organisation to which the players belong to. 
	 * @return A {@link List} of {@link Player}s who are associated with the passed API key.
	 */
	public List<Player> getPlayers(String apiKey) {
		Query query = em.createQuery("select p from Player p where p.belongsTo.apiKey=:apiKey", Player.class);
		query.setParameter("apiKey", apiKey);

		return query.getResultList();
	}
}
