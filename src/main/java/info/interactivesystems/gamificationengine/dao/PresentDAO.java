package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.present.Board;
import info.interactivesystems.gamificationengine.entities.present.Present;
import info.interactivesystems.gamificationengine.entities.present.PresentAccepted;
import info.interactivesystems.gamificationengine.entities.present.PresentArchived;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
	 * Gets an accepted present by its id and organisation.
	 * 
	 * @param presentId
	 * 			The id of the requested present.
	 * @param organisation
	 * 				The organisaiton the group of players is associated with.
	 * @return The {@link Present} which is associated with the passed id and organisation.
	 */
	public PresentArchived getPresentArchivedByIdAndOrganisation(int presentId, Organisation organisation) {
		PresentArchived present = em.find(PresentArchived.class, presentId);
		
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
	 * Gets an archived present by its id and organisation.
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
	 * Generic method to remove a present, an accepted or an archived present from
	 * the database.
	 * 
	 * @param present
	 * 			The present that should be removed from the data base.
	 * @return The Present which is removed.
	 */
	public <T> T deletePresent(T present) {
		em.remove(present);
		return present;
	}

}
