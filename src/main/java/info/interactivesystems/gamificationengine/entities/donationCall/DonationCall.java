package info.interactivesystems.gamificationengine.entities.donationCall;

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
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;

/**
 * A DonationCall represents a call for donations. This could be a real world purpose like a real donation for a
 * charitable purpose or an event for the organisation's employee. Players can donate obtained coins to reach a 
 * particular amount of coins. If the required amount is reached, the goal is reached and the purpose can be 
 * implemented by the responsible manager.
 */
@Entity
@JsonIgnoreProperties({ "belongsTo", "donations" })
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

	@NotNull
	private int goalAmount;

	private int currentAmount;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<Player> donors;

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.EAGER, mappedBy="donationCall")
	private List<Donation> donations;
	
	private boolean goalReached;

	public DonationCall() {
		goalReached = false;
		currentAmount = 0;
		
		donations = new ArrayList<>();
		donors = new ArrayList<>();
	}

	/**
	 * Gets id of the DonationCall object.
	 * 
	 * @return int of the id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of the DonationCall object
	 * 
	 * @param id
	 * 		The id of the DonationCall.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the organisation which the DonationCall belongs to. 
	 * 
	 * @return an organisation object.
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Sets the organisation which the DonationCall belongs to. 
	 * 
	 * @param belongsTo
	 *            The organisation of the Call for donations.
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Gets the name of a DonationCall.
	 * 
	 * @return tThe name of the DonationCall as String.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of a DonationCall.
	 * 
	 * @param name
	 *         The new name of a DonationCall henceforth. 
	 * 
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the description of the DonationCall.
	 * 
	 * @return The description of DonationCall as String.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of the DonationCall.
	 * 
	 * @param description
	 *            The description of the DonationCall as String.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the goal of an DonationCall. This is represented by an amount of coins that should be reached. 
	 * 
	 * @return The value of the goal as int.
	 */
	public int getGoalAmount() {
		return goalAmount;
	}

	/**
	 * Sets the value of coins for the goal, that should be reached with donations.
	 * 
	 * @param goalAmount
	 *            The amount of coins that should be reached.
	 */
	public void setGoalAmount(int goalAmount) {
		this.goalAmount = goalAmount;
	}

	/**
	 * Gets the current value of the donated and collected coins. 
	 * 
	 * @return The value of the current amount of coins as int. 
	 */
	public int getCurrentAmount() {
		return currentAmount;
	}

	/**
	 * Sets the new current value of donated and collected coins.
	 * 
	 * @param currentAmount
	 *            The new current value of donated coins as int.
	 */
	public void setCurrentAmount(int currentAmount) {
		this.currentAmount = currentAmount;
	}

	/**
	 * Gets all players who have done a donation to this call for donations.
	 * 
	 * @return List of player who donated to this call for donations.
	 */
	public List<Player> getDonors() {
		return donors;
	}

	/**
	 * Sets the list of players who have done a donation.
	 * 
	 * @param donors
	 *            All players who has donated to the call for donations as a list.
	 */
	public void setDonors(List<Player> donors) {
		this.donors = donors;
	}

	/**
	 * Gets the value if the DonationCall is reached.
	 * 
	 * @return boolean
	 * 			If the predetermined amount of coins is reached (true) or not (false). 
	 */
	public boolean isGoalReached() {
		return goalReached;
	}

	/**
	 * Sets the value, if a DonationCall is reached.
	 * 
	 * @param goalReached
	 * 			If the predetermined amount of coins is reached (true) or not (false). 
	 */
	public void setGoalReached(boolean goalReached) {
		this.goalReached = goalReached;
	}

	/**
	 * Tests if the current amount of coins reaches the predetermined goal.
	 * 
	 * @return Boolean value if the predetermined goal is reached (true) or not (false).
	 */
	public boolean checkIsReached() {
		if (currentAmount >= goalAmount) {
			goalReached = true;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * A Player can donate an amount of his obtained coins to the call for donations. If a player
	 * donate she/he is added to a list of donors and the donation is added to the DonationCall's 
	 * current amount.
	 * 
	 * @param amount
	 *            The amount of coins which the player donates.
	 * @param player
	 *            The player who donates.
	 * @return Boolean value if with the latest donation the predetermined goal is reached (true) 
	 * 		   or not (false).
	 */
	public boolean donate(int amount, Player player) {
		this.currentAmount += amount;
		this.donors.add(player);

		return this.checkIsReached();
	}
}
