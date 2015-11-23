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
 * more other players. These presents can be an image or a short text message which 
 * contains for example a little praise. A Board serves a player to send and to store
 * little presents in terms of a short text message or an image. The difference between
 * these two messages is as the name suggests, that the text message contains a short 
 * text and the image message an image. To archive the presents they can be moved to 
 * an additional list. It is possible to get for one player all her/his text messages 
 * or all messages with a little image that were created. Furthermore all new presents 
 * of player can be requested as well as the accepted and archived presents. All denies
 * presents were removed from the in-box.
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
	 * Gets the id of the created present.
	 * 
	 * @return The id of the present as int.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of a present.
	 * 
	 * @param id
	 *         The id of the present.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * The organisation the present belongs to.
	 * 
	 * @return The organisation of the present as an object. This parameter must not be null. 
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Sets the organisation to which this group belongs to. 
	 * 
	 * @param belongsTo
	 *            The present's organisation.
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Gets the sender who sent the present to one or more other players.
	 * 
	 * @return The sender of a present.
	 */
	public Player getSender() {
		return sender;
	}

	/**
	 * Sets the sender of a present.
	 * 
	 * @param sender
	 *           The player who sent the present.
	 */
	public void setSender(Player sender) {
		this.sender = sender;
	}

	/**
	 * Gets all receivers of a specific present.
	 * 
	 * @return The list of all players who should receive the present.
	 */
	public List<Player> getReceiver() {
		return receiver;
	}

	/**
	 * Sets all players who should receive the present.
	 * 
	 * @param receiver
	 *            The list of players who receive the present.
	 */
	public void setReceiver(List<Player> receiver) {
		this.receiver = receiver;
	}

	/**
	 * This method checks if a present belongs to a specific organisation. Therefore
	 * it is tested if the organisation's API key matchs the present's API key. 
	 * 
	 * @param organisation
	 * 			The organisation object a present may belong to.
	 * @return Boolean value if the API key of the present is the same 
	 * 			of the tested organisation (true) or not (false).
	 */
	public boolean belongsTo(Organisation organisation) {
		return getBelongsTo().getApiKey().equals(organisation.getApiKey());
	}
}
