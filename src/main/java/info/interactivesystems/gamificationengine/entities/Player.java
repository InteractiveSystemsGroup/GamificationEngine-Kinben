package info.interactivesystems.gamificationengine.entities;

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

/**
 * A user in a gamificated app.
 *
 */
@Entity
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

	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
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

	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
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

	// das (wiederholt) abgeschlossene finishedGoal wird übergeben
	// public void grantReward(FinishedGoal fGoal) {
	// List<Reward> rewards = fGoal.getGoal().getRewards();
	//
	// for (Reward r : rewards) {
	// if (r instanceof Points) {
	// awardPoints(((Points) r).getAmount());
	// } else if (r instanceof Coins) {
	// awardCoins(((Coins) r).getAmount());
	// } else if (r instanceof ReceiveLevel) {
	// // awardLevel(((ReceiveLevel) r).getLevelAmount());
	// awardLevel(((ReceiveLevel) r).getId());
	// } else if (r instanceof Achievement) {
	// awardAchievement((Achievement) r);
	// } else if (r instanceof Badge) {
	// awardBadge((Badge) r);
	// }
	//
	// }
	//
	// }

	/**
	 * The amount of points will be added to the player's current points and
	 * raise them.
	 * 
	 * @param the
	 *            points which are added to the player's points.
	 */
	public void awardPoints(int points) {
		this.points += points;

	}

	/**
	 * The amount of coins will be added to the player's current coins and raise
	 * them.
	 * 
	 * @param the
	 *            amount which is added to the player's coins.
	 */
	public void awardCoins(int amount) {
		coins += amount;
	}

	/**
	 * Get the nickname of a player.
	 * 
	 * @return nickname as String
	 */
	public String getNickname() {
		return nickname;
	}

	/**
	 * Set the nickname of a player, that is displayer for the other players.
	 * 
	 * @param nickname
	 *            the nickname of a user.
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
	 * Add a new Achivement to the player's permanent rewards.
	 * 
	 * @param the
	 *            achievement that will be granted
	 */
	private void awardAchievement(Achievement r) {
		rewards.add(r);
	}

	/**
	 * Add a new Badge to the player's permanent rewards.
	 * 
	 * @param the
	 *            badge that will be granted
	 */
	private void awardBadge(Badge r) {
		rewards.add(r);
	}

	/**
	 * Get the real name of a player.
	 * 
	 * @return player's real name as String
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * Set the real name of a player.
	 * 
	 * @param reference
	 *            the real name of a player
	 */
	public void setReference(String reference) {
		this.reference = reference;
	}

	/**
	 * Get the current amount of coins a player has obtained.
	 * 
	 * @return the amount of obtained coins as int.
	 */
	public int getCoins() {
		return coins;
	}

	/**
	 * Set the current amount of coins a player has obtained.
	 * 
	 * @param coins
	 *            the amount of current coins
	 */
	public void setCoins(int coins) {
		this.coins = coins;
	}

	// public PlayerLevel getLevel() {
	// return level;
	// }
	//
	// public void setLevel(PlayerLevel level) {
	// this.level = level;
	// }

	/**
	 * Get the current level index of a player.
	 * 
	 * @return level index as int
	 */
	public int getLevelIndex() {
		return levelIndex;
	}

	/**
	 * Set the current level index a player has obtained.
	 * 
	 * @param levelIndex
	 *            player's current level
	 */
	public void setLevelIndex(int levelIndex) {
		this.levelIndex = levelIndex;
	}

	/**
	 * Get the label of a player's current level.
	 * 
	 * @return name of the current level as String.
	 */
	public String getLevelLabel() {
		return levelLabel;
	}

	/**
	 * Set the label of a player's current level.
	 * 
	 * @param levelLabel
	 *            the name of the current level
	 */
	public void setLevelLabel(String levelLabel) {
		this.levelLabel = levelLabel;
	}

	/**
	 * Get the id of a player.
	 * 
	 * @return player's id as int
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the id of a player.
	 * 
	 * @param id
	 *            of the player
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get the organisaiton a player belongs to.
	 * 
	 * @return the organisation of the player
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Set the organisation a player belongs to.
	 * 
	 * @param belongsTo
	 *            player's organisation
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Get the password of a player.
	 * 
	 * @return password as String
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the password of a player.
	 * 
	 * @param password
	 *            the new password of the player
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Get player's current amount of points.
	 * 
	 * @return current amount of points.
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * Set the player's current amount of points.
	 * 
	 * @param points
	 *            the player's current amount of points
	 */
	public void setPoints(int points) {
		this.points = points;
	}

	/**
	 * Get the avatar of a player as byte[].
	 * 
	 * @return the avatar as byte[]
	 */
	public byte[] getAvatar() {
		return avatar;
	}

	/**
	 * Set player's avatar.
	 * 
	 * @param avatar
	 *            the new avatar of the player
	 */
	public void setAvatar(byte[] avatar) {
		this.avatar = avatar;
	}

	/**
	 * Get all Tasks a player has finished.
	 * 
	 * @return List of all finished Tasks
	 */
	public List<FinishedTask> getFinishedTasks() {
		return finishedTasks;
	}

	/**
	 * Set the tasks a player has finished.
	 * 
	 * @param finishedTasks
	 *            all tasks a player has finished
	 */
	public void setFinishedTasks(List<FinishedTask> finishedTasks) {
		this.finishedTasks = finishedTasks;
	}

	/**
	 * Get all Goals a player has finished.
	 * 
	 * @return List of all completed Goals
	 */
	public List<FinishedGoal> getFinishedGoals() {
		return finishedGoals;
	}

	/**
	 * Set the goals a player has completed.
	 * 
	 * @param finishedGoals
	 *            all goals a player has completed
	 */
	public void setFinishedGoals(List<FinishedGoal> finishedGoals) {
		this.finishedGoals = finishedGoals;
	}

	/**
	 * The roles a player has.
	 * 
	 * @return List of all roles a player has.
	 */
	public List<Role> getBelongsToRoles() {
		return belongsToRoles;
	}

	/**
	 * Set the roles of a player.
	 * 
	 * @param belongsToRoles
	 *            all roles a player has
	 */
	public void setBelongsToRoles(List<Role> belongsToRoles) {
		this.belongsToRoles = belongsToRoles;
	}

	/**
	 * Get all permanent rewards a player has obtained. These are for example a
	 * badge or an achievement.
	 * 
	 * @return List of obtained permanent rewards
	 */
	public List<PermanentReward> getRewards() {
		return rewards;
	}

	/**
	 * Set the rewards a player has obtained.
	 * 
	 * @param rewards
	 *            permanent rewards a player has obtained
	 */
	public void setRewards(List<PermanentReward> rewards) {
		this.rewards = rewards;
	}

	/**
	 * Get only all Badges a player has obtained.
	 * 
	 * @return obtained Badges as List.
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
	 * Get only all Achievements a player has obtained.
	 * 
	 * @return obtained Achievement as List.
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
	 * Add a just completed task to the List of finished tasks.
	 * 
	 * @param task
	 *            the just finished task
	 */
	public void addFinishedTask(FinishedTask task) {
		finishedTasks.add(task);
	}

	/**
	 * Add a just completed goal to the List of finished tasks.
	 * 
	 * @param goal
	 *            the just finished goal
	 */
	public void addFinishedGoal(FinishedGoal goal) {
		finishedGoals.add(goal);
	}

	/**
	 * Add more than one finished goal to the List of finished goals.
	 * 
	 * @param fGoalsList
	 *            List of finished goals
	 */
	public void addFinishedGoal(List<FinishedGoal> fGoalsList) {
		finishedGoals.addAll(fGoalsList);
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

	/**
	 * If a player donates an amount of obtained coins to a specified
	 * DonationCall, the amount will be subtracted of his current amount of
	 * coins. Before that step it is checked if the player has enough coins for
	 * the donation.
	 * 
	 * @param dCall
	 *            the DonationCall a player donate for
	 * @param amount
	 *            the specified amount of donated coins
	 * @return boolean if a player has enough coins for a donation
	 */
	public boolean donate(DonationCall dCall, int amount) {
		if (enoughPrize(amount)) {
			this.coins -= amount;
			return dCall.donate(amount, this);
		}
		return false;
	}

	/**
	 * Get the List of a player's contacts.
	 * 
	 * @return List of other players
	 */
	public List<Player> getContactList() {
		return contactList;
	}

	/**
	 * Set the list of contacts a player has.
	 * 
	 * @param contactList
	 *            all contacts of a player
	 */
	public void setContactList(List<Player> contactList) {
		this.contactList = contactList;
	}

	/**
	 * Test if a player has enough coins for a specified prize.
	 * 
	 * @param prize
	 *            the prize that should be spent or donated
	 * @return boolean if a player has enough coins
	 */
	public boolean enoughPrize(int prize) {
		if (this.coins >= prize) {
			return true;
		} else
			return false;
	}

	/**
	 * Add player to a player's list of contacts.
	 * 
	 * @param contacts
	 *            players who should be added
	 * @return boolean value
	 */
	public boolean addContacts(Collection<? extends Player> contacts) {
		// do not allow self as a contact
		contacts.remove(this);
		return getContactList().addAll(contacts);
	}

	/**
	 * Remove one or more contacts of the player's list of contacts
	 * 
	 * @param contacts
	 *            players that should be removed
	 * @return boolean value
	 */
	public boolean removeContacts(Collection<Player> contacts) {
		return getContactList().removeAll(contacts);
	}

	/**
	 * Check if a player is active for the gamificated application.
	 * 
	 * @return value if a player is active.
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * Set the value if a player is active.
	 * 
	 * @param isActive
	 *            current value of a player is active
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}
