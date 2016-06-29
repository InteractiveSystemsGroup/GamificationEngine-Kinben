package info.interactivesystems.gamificationengine.entities.marketPlace;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.task.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * With an offer a player can create a task for other players. At this point of time an initial bid in terms 
 * of coins is set which is obtained by the person who completes it. The initial bid can be raised by other 
 * colleagues in order to increase the incentive of fulfilling the task. When a player has completed a Task 
 * that belongs to an offer, she/he will obtain all bids as a reward. 
 * The particular task is then also added to the player’s list of the finished tasks. 
 */
@Entity
@JsonIgnoreProperties({ "belongsTo", "bids" })
public class Offer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@ManyToOne
	private Organisation belongsTo;

	private String name;

	private LocalDateTime offerDate;
	private LocalDateTime endDate;
	private LocalDateTime deadLine;

	private int prize;

	@ManyToOne
	private Task task;

	//orphanRemoval = true,
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.EAGER, mappedBy="offer")
	private List<Bid> bids;

	@ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	private Player player;

	public Offer() {
		bids = new ArrayList<Bid>();
	}

	/**
	 * Gets the id of an offer.
	 * 
	 * @return The offer's id as int.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of an offer.
	 * 
	 * @param id
	 *            The id of an offer.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the exact LocalDateTime when the offer was created.
	 * 
	 * @return The date and time when an offer was created as LocalDateTime.
	 */
	public LocalDateTime getOfferDate() {
		return offerDate;
	}

	/**
	 * Sets the LocalDateTime when the offer was created.
	 * 
	 * @param offerDate
	 *            The exact date and time, when the offer was created.
	 */
	public void setOfferDate(LocalDateTime offerDate) {
		this.offerDate = offerDate;
	}

	/**
	 * Gets the LocalDateTime, when the offer ends.
	 * 
	 * @return LocalDateTime
	 * 			The exact date and time, when the offer ends.
	 */
	public LocalDateTime getEndDate() {
		return endDate;
	}

	/**
	 * Sets the LocalDateTime, when the offer ends.
	 * 
	 * @param endDate
	 *            The exact date and time, when the offer ends.
	 */
	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	/**
	 * Gets the exact date and time, when the task of an offer should be
	 * finished.
	 * 
	 * @return LocalDateTime
	 * 				The exact date and time, when the offer should be finished at least.
	 */
	public LocalDateTime getDeadLine() {
		return deadLine;
	}

	/**
	 * Sets the exact date and time, when the task of an offer should be
	 * finished.
	 * 
	 * @param deadLine
	 *           The exact date and time, when the offer sould be done.
	 */
	public void setDeadLine(LocalDateTime deadLine) {
		this.deadLine = deadLine;
	}

	/**
	 * Gets the current prize of an offer, which can be awarded by fulfilling the
	 * task.
	 * 
	 * @return int
	 * 			The offer's current prize as int.
	 */
	public int getPrize() {
		return prize;
	}

	/**
	 * Sets the current prize of an offer, which can be awarded by fulfilling the
	 * task.
	 * 
	 * @param prize
	 *           The amount of coins a player will get, after she/he has finished the
	 *            offer's task.
	 */
	public void setPrize(int prize) {
		this.prize = prize;
	}

	/**
	 * Gets the organisation an offer belongs to.
	 * 
	 * @return organisation 
	 * 			  The organisation of the player as an object.
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Sets the organisation an offer belongs to.
	 * 
	 * @param belongsTo
	 *            The player's organisation.
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Gets the task of an offer, which can be fulfilled to award the prize.
	 * 
	 * @return Task 
	 * 			The task which was associated with the offer.
	 */
	public Task getTask() {
		return task;
	}

	/**
	 * Sets the task which is connected with the offer.
	 * 
	 * @param task
	 *            The task which have to be finished to finish the offer.
	 */
	public void setTask(Task task) {
		this.task = task;
	}

	 public List<Bid> getBids() {
	 return bids;
	 }
	
	 public void setBids(List<Bid> bids) {
	 this.bids = bids;
	 }

	/**
	 * Gets the name of the offer, which can describe the task in a short way.
	 * 
	 * @return The offer's name as String.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of an offer, which can describe the task in a short way.
	 * 
	 * @param name
	 *          The name of the offer as String.
	 */
	public void setName(String name) {
		this.name = name;
	}

	
	// public void addBid(Bid bid) {
	// bids.add(bid);
	// }

	/**
	 * Gets the player who has created the offer.
	 * 
	 * @return Player 
	 * 			The player object of the player who has created the offer.
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Sets a player as a creator of an offer.
	 * 
	 * @param player
	 *            The player who has created the offer.
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * This method checks if the API key of a role is equal to the organisation's one. So it checks if the role
	 * also belongs to this organisation.
	 * 
	 * @param organisation
	 *            The organisation which is tested. This parameter is required.
	 * @return boolean
	 * 			The value if a offer belongs to the specific organisation (true) or
	 *          not (false).
	 */
	public boolean belongsTo(Organisation organisation) {
		return getBelongsTo().getApiKey().equals(organisation.getApiKey());
	}
	
	/**
	 * When a player does give a bid, the coins of this bid is added to the current prize.
	 * 
	 * @param addPrize
	 * 			Amount by which the current prize of the offer increase.
	 * @return int
	 * 			The new prize of the offer.
	 * 		
	 */
	public int addPrize(String addPrize) {
		this.prize = this.getPrize() + Integer.valueOf(addPrize);
		return prize;
	}
}



