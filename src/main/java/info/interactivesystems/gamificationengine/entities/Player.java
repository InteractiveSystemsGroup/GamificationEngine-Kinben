package info.interactivesystems.gamificationengine.entities;

import info.interactivesystems.gamificationengine.entities.donationCall.DonationCall;
import info.interactivesystems.gamificationengine.entities.goal.FinishedGoal;
import info.interactivesystems.gamificationengine.entities.goal.Goal;
import info.interactivesystems.gamificationengine.entities.rewards.Achievement;
import info.interactivesystems.gamificationengine.entities.rewards.Badge;
import info.interactivesystems.gamificationengine.entities.rewards.PermanentReward;
import info.interactivesystems.gamificationengine.entities.task.FinishedTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A player represents a user in the gamification application, eg. an employee of an organisation or a customer. 
 * By the creation, each player is assigned a nickname and certain roles. Each player has a list for his earned 
 * rewards, already finished Goals and finished Tasks. Points, coins and index of a level can be earned or raised 
 * by fulfilling tasks in the gamification application. Furthermore a player can have an avatar.
 * A player can be set active or can be deactivated so that she/he cannot complete tasks. By default every created 
 * player is active until she/he is deactivated. 
 * Each player can also have a list of contacts which represent other players in the same organisation to send 
 * little presents. 
 * At a later point of time it is possible to change the password, nickname, avatar and the roles or contacts a 
 * player has.
 */
@Entity
@JsonIgnoreProperties({ "belongsTo", "password", "avatar" })
public class Player {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@ManyToOne
	private Organisation belongsTo;

	@NotNull
	private String nickname;

	@NotNull
	private String password;

	/**
	 * Location for business Objects;
	 */
	private String reference;

	private boolean isActive;

	@Lob
	@Column(columnDefinition = "BLOB")
	private byte[] avatar;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<PermanentReward> rewards;

	private int points;
	private int coins;

	// @ManyToOne(cascade = CascadeType.PERSIST)
	// private PlayerLevel level;

	// Current Level
	private int levelIndex;
	private String levelLabel;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<FinishedGoal> finishedGoals;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<FinishedTask> finishedTasks;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<Role> belongsToRoles;

	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<Player> contactList;

	public Player() {
		rewards = new ArrayList<>();
		finishedTasks = new ArrayList<>();
		finishedGoals = new ArrayList<>();
		belongsToRoles = new ArrayList<>();
		contactList = new ArrayList<>();
		setActive(true);

	}


	/**
	 * Gets the nickname of a player.
	 * 
	 * @return The nickname of the player as a String.
	 */
	public String getNickname() {
		return nickname;
	}

	/**
	 * Sets the nickname of a player, that is displayed for the other players.
	 * 
	 * @param nickname
	 *            The nickname of the player as String.
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	// private void awardLevel(int levelid) {
	//
	// this.level.setId(levelid);
	//
	// }

	/**
	 * The amount of points the player has earned will be added to the player's current 
	 * points and raise them.
	 * 
	 * @param points
	 *            The amount of points which is transfered and so added to the player's 
	 *            current points.
	 */
	public void awardPoints(int points) {
		this.points += points;

	}

	/**
	 * The amount of coins the player has earned will be added to the player's current 
	 * coins and raise them.
	 * 
	 * @param amount
	 *            The Amount of coins which is transfered and so added to the player's 
	 *            current coins.
	 */
	public void awardCoins(int amount) {
		coins += amount;
	}
	

	/**
	 * Gets the real name of a player.
	 * 
	 * @return player's real name as String.
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * Sets the real name of a player.
	 * 
	 * @param reference
	 *            This field represents the real name of a player.
	 */
	public void setReference(String reference) {
		this.reference = reference;
	}

	/**
	 * Gets the current amount of coins a player has obtained.
	 * 
	 * @return The amount of obtained coins as int.
	 */
	public int getCoins() {
		return coins;
	}

	/**
	 * Sets the current amount of coins a player has obtained.
	 * 
	 * @param coins
	 *            The amount of current coins a player has.
	 */
	public void setCoins(int coins) {
		this.coins = coins;
	}

	/**
	 * Gets the current amount of points a player has obtained.
	 * 
	 * @return The amount of obtained points as int.
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * Sets the current amount of points a player has obtained.
	 * 
	 * @param points
	 *             The amount of current points a player has.
	 */
	public void setPoints(int points) {
		this.points = points;
	}
	
	
	// public PlayerLevel getLevel() {
	// return level;
	// }
	//
	// public void setLevel(PlayerLevel level) {
	// this.level = level;
	// }

	/**
	 * Gets the current level index of a player.
	 * 
	 * @return Level index returned as int.
	 */
	public int getLevelIndex() {
		return levelIndex;
	}

	/**
	 * Sets the current level index a player has obtained.
	 * 
	 * @param levelIndex
	 *            The index of player's current level.
	 */
	public void setLevelIndex(int levelIndex) {
		this.levelIndex = levelIndex;
	}

	/**
	 * Gets the label of a player's current level.
	 * 
	 * @return The name of the player's current level as String.
	 */
	public String getLevelLabel() {
		return levelLabel;
	}

	/**
	 * Sets the label of a player's current level.
	 * 
	 * @param levelLabel
	 *            The name of the player's current level.
	 */
	public void setLevelLabel(String levelLabel) {
		this.levelLabel = levelLabel;
	}

	/**
	 * Gets the id of a player.
	 * 
	 * @return The player's id as int.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of a player.
	 * 
	 * @param id
	 *            The id of the player.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the organisation a player belongs to.
	 * 
	 * @return The organisation of the player as an object.
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Sets the organisation a player belongs to.
	 * 
	 * @param belongsTo
	 *            The player's organisation.
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Gets the password of a player.
	 * 
	 * @return The player's password as String.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password of a player.
	 * 
	 * @param password
	 *            The new password of the player.
	 */
	public void setPassword(String password) {
		this.password = password;
	}


	/**
	 * Gets the avatar of a player as byte[].
	 * 
	 * @return The player's avatar as byte[].
	 */
	public byte[] getAvatar() {
		return avatar;
	}

	/**
	 * Sets player's transferred byte[] as the current avatar.
	 * 
	 * @param avatar
	 *            The new avatar of the player
	 */
	public void setAvatar(byte[] avatar) {
		this.avatar = avatar;
	}

	/**
	 * Gets all Tasks a player has already finished.
	 * 
	 * @return List of all finished Tasks a player has completed.
	 */
	public List<FinishedTask> getFinishedTasks() {
		return finishedTasks;
	}

	/**
	 * Sets the list of Tasks a player has already finished.
	 * 
	 * @param finishedTasks
	 *            All tasks a player has already finished.
	 */
	public void setFinishedTasks(List<FinishedTask> finishedTasks) {
		this.finishedTasks = finishedTasks;
	}

	/**
	 * Gets all Goals a player has already completed.
	 * 
	 * @return List of all completed Goals.
	 */
	public List<FinishedGoal> getFinishedGoals() {
		return finishedGoals;
	}

	/**
	 * Sets the List of Goals a player has completed.
	 * 
	 * @param finishedGoals
	 *            All Goals a player has already completed.
	 */
	public void setFinishedGoals(List<FinishedGoal> finishedGoals) {
		this.finishedGoals = finishedGoals;
	}

	/**
	 * Gets all roles a player has.
	 * 
	 * @return List of all roles a player has.
	 */
	public List<Role> getBelongsToRoles() {
		return belongsToRoles;
	}

	/**
	 * Sets the list of roles a player has.
	 * 
	 * @param belongsToRoles
	 *            All roles of a player.
	 */
	public void setBelongsToRoles(List<Role> belongsToRoles) {
		this.belongsToRoles = belongsToRoles;
	}

	/**
	 * Gets all permanent rewards a player has already obtained. These are for example 
	 * all badges and achievements.
	 * 
	 * @return List of all obtained permanent rewards.
	 */
	public List<PermanentReward> getRewards() {
		return rewards;
	}

	/**
	 * Sets the list of all permanent rewards a player has obtained.
	 * 
	 * @param rewards
	 *            All permanent rewards a player has obtained.
	 */
	public void setRewards(List<PermanentReward> rewards) {
		this.rewards = rewards;
	}

	/**
	 * Gets only all Badges a player has already obtained.
	 * If the player has no Badge, null is returned. 
	 * 
	 * @return A List of all obtained Badges as List.
	 */
	public List<Badge> getOnlyBadges() {
		// filter PermanentRewards for Badges
		if (rewards != null) {
			List<Badge> badges = rewards.stream().filter(r -> r instanceof Badge).map(r -> (Badge) r).collect(Collectors.toList());
			return badges;
		} else {
			return null;
		}
	}

	/**
	 * Gets only all Achievements a player has already obtained.
	 * If the player has no Achievement, null is returned. 
	 * 
	 * @return A List of all obtained Achievements as List.
	 */
	public List<Achievement> getOnlyAchievement() {
		// filter PermanantAchievements for Achievements
		if (rewards != null) {
			List<Achievement> achievements = rewards.stream().filter(r -> r instanceof Achievement).map(r -> (Achievement) r)
					.collect(Collectors.toList());

			return achievements;
		} else {
			return null;
		}
	}

	/**
	 * Adds the just completed task that is transfered to the player's list 
	 * of all finished tasks.
	 * 
	 * @param task
	 *           The just finished task that should be added to the list.
	 */
	public void addFinishedTask(FinishedTask task) {
		finishedTasks.add(task);
	}

	/**
	 * Adds the just completed goal that is transfered to the player's list 
	 * of all finished goals. 
	 * 
	 * @param goal
	 *           The just finished goal that should be added to the list.
	 */
	public void addFinishedGoal(FinishedGoal goal) {
		finishedGoals.add(goal);
	}

	/**
	 * Adds several finished goals to the player's list of finished goals.
	 * 
	 * @param fGoalsList
	 *           The list of all already finished goals a player has completed that is
	 *           added to the player's list of finished goals.
	 */
	public void addFinishedGoal(List<FinishedGoal> fGoalsList) {
		finishedGoals.addAll(fGoalsList);
	}

	/**
	 * Adds the transfered permanent reward like a Badge or Achievement to all 
	 * already obtained rewards.
	 * 
	 * @param reward
	 *            The permanent reward that was just obtained.
	 */
	public void addPermanentReward(PermanentReward reward) {
		rewards.add(reward);
	}

	/**
	 * This method checks if one specific goal was already finished and gets all 
	 * finished goals objects of this type in one list. If no goal of this type of
	 * was completed an empty list is returned.
	 * 
	 * @param goal
	 *            The goal that should be compared with all other already obtained
	 *            goals.
	 * @return The list of all finished goals that match the compared goal.
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

	
	public void spent(int amount) {
		if (enoughPrize(amount)) {
			this.coins -= amount;
		}
	}
	
	
	/**
	 * If a player donates an amount of obtained coins to a specified
	 * DonationCall, the amount will be subtracted of her/his current amount of
	 * coins. Before this step it is checked if the player has enough coins for
	 * the donation. It the player has enough coins true is returned otherwise
	 * false.
	 * 
	 * @param dCall
	 *            The DonationCall a player donates for.
	 * @param amount
	 *            The specified amount of donated coins.
	 * @return boolean that is true if a player has enough coins for a donation 
	 * otherwise false.
	 */
	public boolean donate(DonationCall dCall, int amount) {
		if (enoughPrize(amount)) {
			this.coins -= amount;
			return dCall.donate(amount, this);
		}
		return false;
	}

	/**
	 * Gets the List of all player's contacts.
	 * 
	 * @return A list of other players who are contacts of the player. 
	 */
	public List<Player> getContactList() {
		return contactList;
	}

	/**
	 * Sets the list of contacts a player has.
	 * 
	 * @param contactList
	 *            All contacts of a player who reprents other player in the 
	 *            gamification application.
	 */
	public void setContactList(List<Player> contactList) {
		this.contactList = contactList;
	}

	/**
	 * This method checks if a player has enough coins for example to make a bid 
	 * or donate.
	 * 
	 * @param prize
	 *            The prize that should be spent or donated.
	 * @return boolean that is true if a player has enough coins otherwise false.
	 */
	public boolean enoughPrize(int prize) {
		if (this.coins >= prize) {
			return true;
		} else
			return false;
	}

	/**
	 * Adds one or more players to a player's list of contacts. If the player who is 
	 * the owner of this list she/he is removed. 
	 * 
	 * @param contacts
	 *           List of one ore more player who should be added to the player's contact list.
	 * @return boolean value
	 */
	public boolean addContacts(Collection<? extends Player> contacts) {
		// do not allow self as a contact
		contacts.remove(this);
		return getContactList().addAll(contacts);
	}

	/**
	 * Removes one or more contacts of the player's list of contacts
	 * 
	 * @param contacts
	 *            List of one ore more player who should be removed from the player's contact list.
	 * @return boolean value
	 */
	public boolean removeContacts(Collection<Player> contacts) {
		return getContactList().removeAll(contacts);
	}

	/**
	 * Checks if a player is active in the gamificated application. This is needed for example 
	 * to complete a task.
	 * 
	 * @return True if the player is active and false if not.
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * Sets the value if a player is active or not.
	 * 
	 * @param isActive
	 *            The current value if a player is active (true) or not (false).
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	/**
	 * This method adds one or more roles to the player's list of roles, but only if they
	 * are not in this list already.
	 * 
	 * @param newRoles
	 * 			The roles that should be added to the player's current list of roles.
	 */
	public void addRoles(List<Role> newRoles){
		for(Role role : newRoles){
			if(!belongsToRoles.contains(role)){
				belongsToRoles.add(role);
			}
		}
	}
}
