package info.interactivesystems.gamificationengine.entities.marketPlace;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.Role;
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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * With an offer a player can create a task for other players. The other players
 * can bid for this offer to be allowed to fulfil its task and to obtain coins
 * or other rewards.
 */
@Entity
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

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<Role> allowedForRole;

	// @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	// private List<Bid> bids;

	@ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	private Player player;

	public Offer() {
		allowedForRole = new ArrayList<>();
		// bids = new ArrayList<Bid>();
	}

	// GETTER & SETTER
	/**
	 * Gets the id of an offer.
	 * 
	 * @return int
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of an offer.
	 * 
	 * @param id
	 *            - the id of an offer.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the exact LocalDateTime when the offer was created.
	 * 
	 * @return LocalDateTime
	 */
	public LocalDateTime getOfferDate() {
		return offerDate;
	}

	/**
	 * Sets the LocalDateTime when the offer was created.
	 * 
	 * @param offerDate
	 *            - the exact Date, when the offer was created
	 */
	public void setOfferDate(LocalDateTime offerDate) {
		this.offerDate = offerDate;
	}

	/**
	 * Gets the LocalDateTime, when the offer end.
	 * 
	 * @return LocalDateTime
	 */
	public LocalDateTime getEndDate() {
		return endDate;
	}

	/**
	 * Sets the LocalDateTime, when the offer ends.
	 * 
	 * @param endDate
	 *            - the exact Date, when the offer ends
	 */
	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	/**
	 * Gets the exact Date and Time, when the task of an offer should be
	 * finished.
	 * 
	 * @return LocalDateTime
	 */
	public LocalDateTime getDeadLine() {
		return deadLine;
	}

	/**
	 * Sets the exact Date and Time, when the task of an offer should be
	 * finished.
	 * 
	 * @param deadLine
	 *            - the exact * Date, when the offer's task should be done
	 */
	public void setDeadLine(LocalDateTime deadLine) {
		this.deadLine = deadLine;
	}

	/**
	 * Gets the current prize of an offer, which can be awarded by fulfilling the
	 * task.
	 * 
	 * @return int
	 */
	public int getPrize() {
		return prize;
	}

	/**
	 * Sets the current prize of an offer, which can be awarded by fulfilling the
	 * task.
	 * 
	 * @param prize
	 *            - the amount of coins a player will get, after he finished
	 *            offer's task
	 */
	public void setPrize(int prize) {
		this.prize = prize;
	}

	/**
	 * Gets the organisation an offer belongs to.
	 * 
	 * @return organisation object
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Sets the organisation an offer belongs to.
	 * 
	 * @param belongsTo
	 *            - the organisation the offer belongs to.
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Gets the task of an offer, which can be fulfilled to award the prize.
	 * 
	 * @return Task object
	 */
	public Task getTask() {
		return task;
	}

	/**
	 * Sets the task which is connected with the offer.
	 * 
	 * @param task
	 *            - the task which must be finished to comply the offer
	 */
	public void setTask(Task task) {
		this.task = task;
	}

	/**
	 * Gets the roles to check which player is allowed to fulfil this offer.
	 * 
	 * @return List<Role>
	 */
	public List<Role> getAllowedForRole() {
		return allowedForRole;
	}

	/**
	 * Sets a list of roles for the task. Players who have at least one of these
	 * roles is allowed to fulfil the task and award the prize.
	 * 
	 * @param allowedForRole
	 *            - the roles of which a player must have at least one to
	 *            fulfill this task.
	 */
	public void setAllowedForRole(List<Role> allowedForRole) {
		this.allowedForRole = allowedForRole;
	}

	// public List<Bid> getBids() {
	// return bids;
	// }
	//
	// public void setBids(List<Bid> bids) {
	// this.bids = bids;
	// }

	/**
	 * Gets the name of the offer, which can describe the task in a short way.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of an offer, which can describe the task in a short way.
	 * 
	 * @param name
	 *            - the name of the offer.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * This method adds a further role to the list of roles. If a player has got
	 * one of these roles he is allowed to fulfil the task.
	 * 
	 * @param role
	 *            - the new role which is added to the role list.
	 */
	public void addRole(Role role) {
		this.allowedForRole.add(role);
	}

	// public void addBid(Bid bid) {
	// bids.add(bid);
	// }

	/**
	 * Gets the player who has created the offer.
	 * 
	 * @return Player object
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Sets a player as a creator of an offer.
	 * 
	 * @param player
	 *            - the player who has created the offer.
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * This method checks if the API key of a role is equal to the
	 * organisation's one, which means the role belongs to this organisation.
	 * 
	 * @param organisation
	 *            a none null organisation
	 * @return boolean
	 */
	public boolean belongsTo(Organisation organisation) {
		return getBelongsTo().getApiKey().equals(organisation.getApiKey());
	}
}
