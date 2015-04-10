package info.interactivesystems.gamificationengine.entities;

import info.interactivesystems.gamificationengine.entities.goal.FinishedGoal;
import info.interactivesystems.gamificationengine.entities.goal.Goal;
import info.interactivesystems.gamificationengine.entities.rewards.PermanentReward;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

/**
 * A player may belong to a group. Some tasks are only allowed to be completed
 * by a concrete group.
 */
@Entity
public class PlayerGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	private String name;

	@NotNull
	@ManyToOne
	private Organisation belongsTo;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<Player> players;

	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<FinishedGoal> finishedGoals;

	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<PermanentReward> rewards;

	private int coins;
	private int points;

	private int levelIndex;
	private String levelLabel;

	public int getLevelIndex() {
		return levelIndex;
	}

	public void setLevelIndex(int levelIndex) {
		this.levelIndex = levelIndex;
	}

	public String getLevelLabel() {
		return levelLabel;
	}

	public void setLevelLabel(String levelLabel) {
		this.levelLabel = levelLabel;
	}

	@Lob
	@Column(columnDefinition = "BLOB")
	private byte[] groupLogo;

	public PlayerGroup() {
		players = new ArrayList<>();
		finishedGoals = new ArrayList<>();
	}

	// GETTER & SETTER

	/**
	 * The name of the group.
	 * 
	 * @return a name of the group
	 */
	public String getName() {
		return name;
	}

	/**
	 * Name used to name a group.
	 * 
	 * @param name
	 *            May not be null
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Current list of players belonging to this group.
	 * 
	 * @return a list of players
	 */
	public List<Player> getPlayers() {
		return players;
	}

	/**
	 * List of players who belongt to the group.
	 * 
	 * @param players
	 *            a list of players
	 */
	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	/**
	 * Get all Goals a group has completed.
	 * 
	 * @return list of finished goals
	 */
	public List<FinishedGoal> getFinishedGoals() {
		return finishedGoals;
	}

	/**
	 * Set the goals a group has finished.
	 * 
	 * @param finishedGoals
	 *            goals a group has finished
	 */
	public void setFinishedGoals(List<FinishedGoal> finishedGoals) {
		this.finishedGoals = finishedGoals;
	}

	/**
	 * Get the current amount of poins a group has.
	 * 
	 * @return Amount of points this group has
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * Set the points a group has collected.
	 * 
	 * @param points
	 *            amount this group should get
	 */
	public void setPoints(int points) {
		this.points = points;
	}

	/**
	 * The organisation the group belongs to.
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
	 *            a organisation
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Get the id of the group.
	 * 
	 * @return id as int
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the id of the group.
	 * 
	 * @param id
	 *            id of the group
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get the logo of a group as a byte[].
	 * 
	 * @return byte[] of the image content
	 */
	public byte[] getGroupLogo() {
		return groupLogo;
	}

	/**
	 * Set the logo of an group.
	 * 
	 * @param groupLogo
	 *            byte[] of the image content
	 */
	public void setGroupLogo(byte[] groupLogo) {
		this.groupLogo = groupLogo;
	}

	/**
	 * Test if a group belongs to a specific organisation.
	 */
	public boolean belongsTo(Organisation organisation) {
		return getBelongsTo().getApiKey().equals(organisation.getApiKey());
	}

	/**
	 * Add a permanent reward like a Badge or Achievement to all obtained
	 * rewards.
	 * 
	 * @param reward
	 *            the permanent reward that was just obtained.
	 */
	public void addPermanentReward(PermanentReward reward) {
		rewards.add(reward);
	}

	/**
	 * Set the current amount of coins a group has obtained.
	 * 
	 * @param coins
	 *            the amount of current coins
	 */
	public void awardCoins(int amount) {
		setCoins(getCoins() + amount);
	}

	/**
	 * The group obtained an amount of points which is added to the current
	 * points.
	 * 
	 * @param amount
	 *            amount of obtained points
	 */
	public void awardPoints(int amount) {
		this.points = this.points + amount;
	}

	/**
	 * Test if a goal was already finished and get all finished goals of it.
	 * 
	 * @param goal
	 *            the goal that should be compared
	 * @return List of all finished goals of the goal
	 */
	public List<FinishedGoal> getFinishedGoalsByGoal(Goal goal) {
		List<FinishedGoal> returnList = new ArrayList<>();
		for (FinishedGoal fGoal : finishedGoals) {
			if (fGoal.getGoal().equals(goal)) {
				returnList.add(fGoal);
			}
		}
		return returnList;
	}

	public int getCoins() {
		return coins;
	}

	public void setCoins(int coins) {
		this.coins = coins;
	}

	public List<PermanentReward> getRewards() {
		return rewards;
	}

	public void setRewards(List<PermanentReward> rewards) {
		this.rewards = rewards;
	}
}
