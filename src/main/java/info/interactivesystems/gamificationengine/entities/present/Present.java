package info.interactivesystems.gamificationengine.entities.present;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * A present is a little positive message which one player can send to one or
 * more other players. A present can be a short text or an image.
 * <ul>
 * <li>sender - player who sends a present to another player.</li>
 * <li>receiver - one or more players who should receive the present.</li>
 * </ul>
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "PRESENT_TYPE", discriminatorType = DiscriminatorType.STRING)
public abstract class Present {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@ManyToOne
	private Organisation belongsTo;

	@ManyToOne
	private Player sender;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<Player> receiver;

	public Present() {
		receiver = new ArrayList<>();
	}

	/**
	 * Get the if of the created present.
	 * 
	 * @return id of the present as int
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the id of a present.
	 * 
	 * @param id
	 *            of the present
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * The organisation the present belongs to.
	 * 
	 * @return a not nullable organisation
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * An organisation to which this group belongs, may not be null.
	 * 
	 * @param belongsTo
	 *            an organisation
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Get the sender who send the present to one or more other players.
	 * 
	 * @return the sender of a present
	 */
	public Player getSender() {
		return sender;
	}

	/**
	 * Set the sender of a present.
	 * 
	 * @param sender
	 *            who send the present
	 */
	public void setSender(Player sender) {
		this.sender = sender;
	}

	/**
	 * Get all receivers of a present.
	 * 
	 * @return List of all players who should receive the present
	 */
	public List<Player> getReceiver() {
		return receiver;
	}

	/**
	 * Set all player who should receive the present.
	 * 
	 * @param List
	 *            of players who receive the present
	 */
	public void setReceiver(List<Player> receiver) {
		this.receiver = receiver;
	}

	/**
	 * Test if a group belongs to a specific organisation.
	 */
	public boolean belongsTo(Organisation organisation) {
		return getBelongsTo().getApiKey().equals(organisation.getApiKey());
	}
}
