package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.present.Board;
import info.interactivesystems.gamificationengine.entities.present.Present;

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

	public int insertPresent(Present present) {
		em.persist(present);
		em.flush();
		return present.getId();
	}

	public void insert(List<Board> boardList) {
		for (Board b : boardList) {
			em.persist(b);
			em.flush();
		}
	}

	// public Board getBoard(int boardId) {
	// return em.find(Board.class, boardId);
	// }

	// public Present getPresent(int presentId) {
	// return em.find(Present.class, presentId);
	// }

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

	// public Board getBoardByIdAndOrganisation(int boardId, Organisation
	// organisation) {
	// Board board = em.find(Board.class, boardId);
	// if (board != null) {
	// if (board.belongsTo(organisation)) {
	// return board;
	// } else {
	// return null;
	// }
	// } else {
	// return null;
	// }
	// }

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

	// TODO get List of Presents
	// public List<Present> getPresentsOfPlayer(Player player, Organisation
	// organisation)
	// {
	// List<Present> presents = null;
	// return presents;
	// }

	// public Board deleteBoardByIdAndOrganisation(int boardId, Organisation
	// organisation) {
	// Board board = getBoardByIdAndOrganisation(boardId, organisation);
	// em.remove(board);
	// return board;
	// }

}
