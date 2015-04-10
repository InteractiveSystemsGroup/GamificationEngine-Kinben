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
 * A player can give one or more bids for an offer so its total prize gets
 * higher and in order to increase the incentive of fulfilling the task.
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

	// GETTER & SETTER
	/**
	 * Get the id of a bid.
	 * 
	 * @return id of a bid
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the id of a bid.
	 * 
	 * @param id
	 *            a unique integer, usually will be generated automatically
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get the organisation a bid belongs to.
	 * 
	 * @return the organisation the bid belongs to
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Set the organisation a bid belongs to.
	 * 
	 * @param belongsTo
	 *            a none null organisation
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Get the amount of coins, by which the prize of coins is raised.
	 * 
	 * @return prize the concrete bid
	 */
	public int getPrize() {
		return prize;
	}

	/**
	 * Set the amount of coins, by which the prize is raised.
	 * 
	 * @param prize
	 *            an amount of coins
	 */
	public void setPrize(int prize) {
		this.prize = prize;
	}

	/**
	 * Get the exact date and time, when the bid was given.
	 * 
	 * @return creationDate
	 */
	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	/**
	 * Set the exact date and time, when the bid was given.
	 * 
	 * @param creationDate
	 *            local date time, usually now
	 */
	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Get the player, who gives a bid for an offer.
	 * 
	 * @return player who has bid
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Set the player, who gives a bid for an offer.
	 * 
	 * @param player
	 * 
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * Get the offer for which the bid was given.
	 * 
	 * @return offer
	 */
	public Offer getOffer() {
		return offer;
	}

	/**
	 * Set the offer for which a bid was given.
	 * 
	 * @param offer
	 *            an instance of offer
	 */
	public void setOffer(Offer offer) {
		this.offer = offer;
	}

}
