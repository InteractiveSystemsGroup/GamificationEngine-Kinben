package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.present.Board;
import info.interactivesystems.gamificationengine.entities.present.Present;
import info.interactivesystems.gamificationengine.entities.present.PresentAccepted;
import info.interactivesystems.gamificationengine.entities.present.PresentArchived;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Named
@Stateless
public class PresentDAO {

	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	/**
	 * Stores a new present in the data base.
	 * 
	 * @param present
	 * 			The present which should be stores in the data base.
	 * @return The generated id of the present. 
	 */
	public int insertPresent(Present present) {
		em.persist(present);
		em.flush();
		return present.getId();
	}

	/**
	 * Stores a list of boards in the data base.
	 * 
	 * @param boardList
	 * 			The list of boards that should be stored in the data base.
	 */
	public void insert(List<Board> boardList) {
		for (Board b : boardList) {
			em.persist(b);
			em.flush();
		}
	}

	/**
	 * Gets a present by its id and API key.
	 * 
	 * @param presentId
	 * 			The id of the requested present.
	 * @param apiKey
	 * 			The API key of the organisation to which the present belongs to.
	 * @return The {@link Present} which is associated with the passed id and API key.
	 */
	public Present getPresent(int presentId, String apiKey) {
		Query query = em.createQuery("select p from Present p where p.belongsTo.apiKey=:apiKey and p.id = :id", Present.class);
		List list = QueryUtils.configureQuery(query, presentId, apiKey);
		if (list.isEmpty()) {
			return null;
		}
		return  (Present) list.get(0);
	}
	
	
	/**
	 * Gets an accepted present by its id and API key.
	 * 
	 * @param presentId
	 * 			The id of the requested present.
	 * @param apiKey
	 * 			The API key of the organisation to which the present belongs to.
	 * @return The {@link Present} which is associated with the passed id and API key.
	 */
	public PresentArchived getArchivedPresent(int presentId,  String apiKey) {
		Query query = em.createQuery("select p from PresentArchived p where p.belongsTo.apiKey=:apiKey and p.id = :id", PresentArchived.class);
		List list = QueryUtils.configureQuery(query, presentId, apiKey);
		if (list.isEmpty()) {
			return null;
		}
		return  (PresentArchived) list.get(0);
	}

	
	/**
	 * Gets an archived present by its id and API key.
	 * 
	 * @param presentId
	 * 			The id of the requested present.
	 *@param apiKey
	 * 			The API key of the organisation to which the present belongs to.
	 * @return The {@link Present} which is associated with the passed id and API key.
	 */
	public PresentAccepted getAcceptedPresent(int presentId, String apiKey) {
		Query query = em.createQuery("select p from PresentAccepted p where p.belongsTo.apiKey=:apiKey and p.id = :id", PresentAccepted.class);
		List list = QueryUtils.configureQuery(query, presentId, apiKey);
		if (list.isEmpty()) {
			return null;
		}
		return  (PresentAccepted) list.get(0);
	}

	/**
	 * Generic method to remove a present, an accepted or an archived present from
	 * the database.
	 * 
	 * @param present
	 * 			The present that should be removed from the data base.
	 * @return The Present which is removed.
	 */
	public <T> T deletePresent(T present) {
		
		if (present != null) {
			em.remove(present);
		}
		return present;
	}

}
