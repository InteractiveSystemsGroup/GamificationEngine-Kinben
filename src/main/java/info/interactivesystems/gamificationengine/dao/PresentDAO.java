package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.api.PlayerApi;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.present.Board;
import info.interactivesystems.gamificationengine.entities.present.Present;
import info.interactivesystems.gamificationengine.entities.present.PresentAccepted;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@Stateless
public class PresentDAO {

	private static final Logger log = LoggerFactory.getLogger(PlayerApi.class);
	
	
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
	 * Gets a present by its id and organisation.
	 * 
	 * @param presentId
	 * 			The id of the requested present.
	 * @param organisation
	 * 				The organisaiton the group of players is associated with.
	 * @return The {@link Present} which is associated with the passed id and organisation.
	 */
	public Present getPresentByIdAndOrganisation(int presentId, Organisation organisation) {
		Present present = em.find(Present.class, presentId);
		
		if (present != null) {
			if (present.belongsTo(organisation)) {
				return present;
			} else {
				return null;
			}
		} else {
			return null;
		}
		
	}
	
	
	/**
	 * Gets a present by its id and organisation.
	 * 
	 * @param presentId
	 * 			The id of the requested present.
	 * @param organisation
	 * 				The organisaiton the group of players is associated with.
	 * @return The {@link Present} which is associated with the passed id and organisation.
	 */
	public PresentAccepted getPresentAcceptedByIdAndOrganisation(int presentId, Organisation organisation) {
		PresentAccepted present = em.find(PresentAccepted.class, presentId);
		
		if (present != null) {
			if (present.belongsTo(organisation)) {
				return present;
			} else {
				return null;
			}
		} else {
			return null;
		}
		
	}

	
	/**
	 * Removes a present from the data base.
	 * 
	 * @param present
	 * 			The present that should be removed from the data base.
	 * @return The {@link Present} which is removed.
	 */
	public Present deleteP(Present present) {
		em.remove(present);
		return present;
	}

	public Present deletePresent(int presentId, Organisation organisation) {
		// Present present = getPresent(presentId);
		Present present = getPresentByIdAndOrganisation(presentId, organisation);
		em.remove(present);
		return present;
	}

	

}
