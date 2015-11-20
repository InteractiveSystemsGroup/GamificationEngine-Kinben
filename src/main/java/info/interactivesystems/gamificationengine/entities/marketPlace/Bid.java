package info.interactivesystems.gamificationengine.entities.marketPlace;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * A player can give one or more bids for an offer so its total prize gets higher and in order to increase 
 * the incentive of fulfilling the task. The bidden amount of coins will be subtracted from the bidder’s 
 * current account and will be added to the offer’s current prize. Each player can make several bids on 
 * condition that her/his coins are enough otherwise the bid cannot be done.
 */
@Entity
public class Bid {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@ManyToOne
	private Organisation belongsTo;

	private int prize;

	private LocalDateTime creationDate;

	@ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	// @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	private Player player;

	@ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	// @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	private Offer offer;

	/**
	 * Gets the id of a bid.
	 * 
	 * @return The id of a bid as int.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of a bid.
	 * 
	 * @param id
	 *            A unique integer, usually will be generated automatically.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the organisation a bid belongs to.
	 * 
	 * @return the organisation object the bid belongs to.
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Sets the organisation a bid belongs to.
	 * 
	 * @param belongsTo
	 *            The organisation a task belongs to.
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Gets the amount of coins, by which the prize of coins is raised.
	 * 
	 * @return The coins of the concrete bid.
	 */
	public int getPrize() {
		return prize;
	}

	/**
	 * Sets the amount of coins, by which the prize is raised.
	 * 
	 * @param prize
	 *            The amount of coins by which the bid is raised.
	 */
	public void setPrize(int prize) {
		this.prize = prize;
	}

	/**
	 * Gets the exact date and time, when the bid was made.
	 * 
	 * @return creationDate
	 * 			The date and time, when the bid was created as LocalDateTime.
	 */
	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	/**
	 * Sets the exact date and time, when the bid was given.
	 * 
	 * @param creationDate
	 *            Sets the date and time a bid was created as LocalDateTime. Usually it's now.
	 */
	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Gets the player, who gives a bid for an offer.
	 * 
	 * @return The player who has made the bid.
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Sets the player, who gives a bid for an offer.
	 * 
	 * @param player
	 * 			The player who has made the bid.
	 * 
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * Gets the offer for which the bid was given.
	 * 
	 * @return offer
	 * 			The offer the player had bid for.
	 */
	public Offer getOffer() {
		return offer;
	}

	/**
	 * Sets the offer for which a bid was given.
	 * 
	 * @param offer
	 *            The offer object a bid was made for.
	 */
	public void setOffer(Offer offer) {
		this.offer = offer;
	}

}
