package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.present.Board;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Data access for a board.
 */
@Named
@Stateless
public class BoardDAO extends AbstractDAO<Board> {

	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	/**
	 * Returns the Board of a specific player of the organisation which the API
	 * key belongs to.
	 * 
	 * @param playerId
	 *            The player who owns the board.
	 * @param apiKey
	 *            Credential which identifies the board in one organisation.
	 * @return The board.object of a player.
	 */
	public Board getBoard(int playerId, String apiKey) {
		Query query = em.createQuery("select entity from Board entity where entity.owner.id = :playerId and entity.belongsTo.apiKey = :apiKey",
				Board.class);
		query.setParameter("playerId", playerId);
		query.setParameter("apiKey", apiKey);
		List list = query.setMaxResults(1).getResultList();
		if (list.isEmpty()) {
			return null;
		}

		return ((Board) list.get(0));
	}

	/**
	 * Returns the Boards of all players who are supposed to receive a present
	 * as a list. The API key is needed to identify their boards.
	 * 
	 * @param receivers
	 *            The owners of the boards who sould reveice a present.
	 * @param apiKey
	 *            Credential which identifies the board in one organisation.
	 * @return A {@link List} of {@link Board}s that represent all boards of the players who get a present.
	 */
	public List<Board> getBoards(List<Player> receivers, String apiKey) {
		Query query = em.createQuery("select entity from Board entity where entity.owner in (:owners) and entity.belongsTo.apiKey = :apiKey",
				Board.class);
		query.setParameter("owners", receivers);
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}
}
