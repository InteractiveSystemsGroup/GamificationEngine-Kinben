package info.interactivesystems.gamificationengine.entities.present;

import info.interactivesystems.gamificationengine.entities.Organisation;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * Presents that are in the in-box of the board a player can accept or deny. If the player
 * accept a present its status is set to accepted and an PresentAccepted object is created.
 */
@Entity
public class PresentAccepted {

	enum Status {
		ACCEPT, DENIED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@ManyToOne
	private Organisation belongsTo;

	private LocalDateTime date;

	@NotNull
	@ManyToOne
	private Board board;

	@NotNull
	@ManyToOne
	private Present present;

	private Status status;

	/**
	 * Gets the id of the accepted present.
	 * 
	 * @return The id of the accepted present.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of the accepted present.
	 * 
	 * @param id
	 * 			The new id of the accepted present.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the organisation an accepted present belongs to.
	 * 
	 * @return The organisation of the accepted present as an object.
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Sets the organisation an accepted present belongs to.
	 * 
	 * @param belongsTo
	 * 			 The organisation of the accepted present.
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Gets the date and time a present was accepted by a player.
	 *  
	 * @return The date and time a present was accepted.
	 */
	public LocalDateTime getDate() {
		return date;
	}

	/**
	 * The date and time a present was accepted by a player.
	 * 
	 * @param date
	 * 		   The date and time the ülayer has accepted the present. 
	 */
	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	/**
	 * Gets the board to which the present was accepted.
	 * 
	 * @return The board in which the present was accepted.
	 */
	public Board getBoard() {
		return board;
	}

	/**
	 * Sets the board the present belongs to. 
	 * 
	 * @param board
	 * 			The board the accepted presents belongs to henceforth.
	 */
	public void setBoard(Board board) {
		this.board = board;
	}

	/**
	 * Get the present that is accepted by the player.
	 * 
	 * @return The present that was accepted.
	 */
	public Present getPresent() {
		return present;
	}

	/**
	 * Sets the presents that was accepted by the player.
	 * 
	 * @param present
	 * 			The present that was accepted by the player.
	 */
	public void setPresent(Present present) {
		this.present = present;
	}

	/**
	 * Sets the status of the present if the player has accepted it or not.
	 * 
	 * @return The status of the present. If it was accepted by the player the field is ACCEPT
	 * otherwise DENIED.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Sets the status of the present to accepted.  
	 */
	public void setStatus() {
		this.status = Status.ACCEPT;
	}

	/**
	 * This method checks if a accepted present belongs to a specific organisation. Therefore
	 * it is tested if the organisation's API key matchs the present's API key. 
	 * 
	 * @param organisation
	 * 			The organisation object a present may belongs to.
	 * @return Boolean value if the API key of the accepted present is the same 
	 * 			of the tested organisation (true) or not (false).
	 */
	public boolean belongsTo(Organisation organisation) {
		return getBelongsTo().getApiKey().equals(organisation.getApiKey());
	}
}
