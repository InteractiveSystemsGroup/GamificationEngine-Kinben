package info.interactivesystems.gamificationengine.entities;

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

/**
 * DonationCall represents a call for donations. Players can donate obtained
 * coins to reach a particular amount of coins.
 */
@Entity
public class DonationCall {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@ManyToOne
	private Organisation belongsTo;

	@NotNull
	private String name;

	private String description;

	private int goal;

	private int currentAmount;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<Player> donors;

	private boolean goalReached;

	public DonationCall() {
		goalReached = false;
	}

	/**
	 * Get id of the DonationCall object.
	 * 
	 * @return int of the id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set id of the DonationCall object
	 * 
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get the organisation which the role belongs to and in which a player can
	 * have this role.
	 * 
	 * @return an organisation object.
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Set the organisation which the role belongs to and in which a player can
	 * have this role.
	 * 
	 * @param belongsTo
	 *            the organisation object.
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Get the name of a DonationCall.
	 * 
	 * @return the name of the DonationCall as String.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of a DonationCall.
	 * 
	 * @param name
	 *            the name for a DonationCall.
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the description of the DonationCall.
	 * 
	 * @return description of DonationCall as String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description of the DonationCall.
	 * 
	 * @param description
	 *            the description of the DonationCall
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the goal of an DonationCall that should be reached.
	 * 
	 * @return int value of the goal
	 */
	public int getGoal() {
		return goal;
	}

	/**
	 * Set the value for the goal, that should be reached with donations.
	 * 
	 * @param goal
	 *            the amount of coins that should be reached
	 */
	public void setGoal(int goal) {
		this.goal = goal;
	}

	/**
	 * Gets the current value of donated and collected coins
	 * 
	 * @return int value for the collected coins.
	 */
	public int getCurrentAmount() {
		return currentAmount;
	}

	/**
	 * Set the current value of donated and collected coins.
	 * 
	 * @param currentAmount
	 *            the current value of donated coins.
	 */
	public void setCurrentAmount(int currentAmount) {
		this.currentAmount = currentAmount;
	}

	/**
	 * Get all players who have done a donation.
	 * 
	 * @return List of player who donated
	 */
	public List<Player> getDonors() {
		return donors;
	}

	/**
	 * Set players who have done a donation to a list.
	 * 
	 * @param donors
	 *            players who has donated
	 */
	public void setDonors(List<Player> donors) {
		this.donors = donors;
	}

	/**
	 * Get the value if the DonationCall is reached.
	 * 
	 * @return boolean
	 */
	public boolean isGoalReached() {
		return goalReached;
	}

	/**
	 * Set the value, if a DonationCall is reached.
	 * 
	 * @param goalReached
	 */
	public void setGoalReached(boolean goalReached) {
		this.goalReached = goalReached;
	}

	/**
	 * Test if the current amount of coins reaches the specified goal.
	 * 
	 * @return boolean if the specified goal is reached
	 */
	public boolean isReached() {
		if (currentAmount >= goal) {
			goalReached = true;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * A Player can donate an amount of his obtained coins for the call for
	 * donations. If a player donate he is added to a list of donors and the
	 * donation is added to the DonationCall's current amount.
	 * 
	 * @param amount
	 *            the amount of coins which is donated by the player
	 * @param player
	 *            who donate
	 * @return
	 */
	public boolean donate(int amount, Player player) {
		this.currentAmount += amount;
		this.donors.add(player);

		return this.isReached();
	}
}
