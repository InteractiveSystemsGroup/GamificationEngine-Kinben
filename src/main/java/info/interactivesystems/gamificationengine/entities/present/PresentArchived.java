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
 * Presents that are accepted a player can archived. If the player archived a present 
 * an PresentArchived object is created. 
 *
 */
@Entity
public class PresentArchived {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@ManyToOne
	private Organisation belongsTo;

	private LocalDateTime date;

	@NotNull
	@ManyToOne
	private PresentAccepted acceptedPresent;


	/**
	 * Gets the id of the archived present.
	 * 
	 * @return The id of the archived present.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of the archived present.
	 * 
	 * @param id
	 * 		The new id of the archived present.	
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the organisation an archived present belongs to.
	 * 
	 * @return The organisation of the archived present as an object.
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Sets the organisation an archived present belongs to.
	 * 
	 * @param belongsTo
	 * 			The organisation of the archived present.
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Gets the date and time a present was archived by a player.
	 * 
	 * @return The date and time when the present was archived.
	 */
	public LocalDateTime getDate() {
		return date;
	}

	/**
	 * Sets the date and time when the present was archived.
	 * 
	 * @param date
	 * 			The date and time when the present was archived as LocalDateTime.
	 */
	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	/**
	 * Gets the present object of the present that is archived.
	 * 
	 * @return The archived present as present object.
	 */
	public PresentAccepted getAcceptedPresent() {
		return acceptedPresent;
	}

	/**
	 * Sets the present that was archived.
	 * 
	 * @param present
	 * 			The archived present as a present object.
	 */
	public void setAcceptedPresent(PresentAccepted present) {
		this.acceptedPresent = present;
	}
	
}
