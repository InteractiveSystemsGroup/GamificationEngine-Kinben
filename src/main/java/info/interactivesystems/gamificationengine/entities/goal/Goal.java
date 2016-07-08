package info.interactivesystems.gamificationengine.entities.goal;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Role;
import info.interactivesystems.gamificationengine.entities.rewards.Reward;
import info.interactivesystems.gamificationengine.entities.task.FinishedTask;
import info.interactivesystems.gamificationengine.utils.StringUtils;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
/**
 * A Goal comprises one or more tasks and is associated with a goal rule. If the player wants to earn the 
 * connected awards the rule has to be fulfilled. To create a goal some already created components are needed.
 * So the condition when a goal is completed is defined in the goal rule and the connected tasks. Who can 
 * complete a goal is defined by the role of a player and whether it can be done by a group. It is also 
 * possible to define whether a goal is repeatable so that the player can complete the tasks and obtains its 
 * coins and points as rewards again. All goals that are associated with the organisation can be requested 
 * or like the elements before only one specific goal, if the correspondent id is used. The name, the 
 * associated rewards and also the rule for completion can be changed as well as the indication if the goal is
 * repeatable or a goal that can be reached by a group. Is is also possible to change the roles so different 
 * people can complete the goal. 
 *
 */
@Entity
@JsonIgnoreProperties({ "belongsTo" })
public class Goal {

	private static final Logger LOGGER = LoggerFactory.getLogger(Goal.class);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@ManyToOne
	private Organisation belongsTo;

	private String name;

	@NotNull
	@ManyToOne
	private GoalRule rule;

	private boolean repeatable;
	private boolean playerGroupGoal;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	// @JoinTable(name = "Goal_Reward", joinColumns = @JoinColumn(name =
	// "Goal_id"), inverseJoinColumns = @JoinColumn(name = "rewars_id"))
	@JsonBackReference
	private List<Reward> rewards;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<Role> canCompletedBy;

	public Goal() {
		rewards = new ArrayList<>();
	}

	/**
	 Gets the id of the goal.
	 * 
	 * @return The goal's id as int.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of the goal.
	 * 
	 * @param id
	 *          The id of the goal henceforth.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * The organisation the goal belongs to. This parameter must 
	 * not be null
	 * 
	 * @return The organisations object the goal belongs to.
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Sets the organisation to which this goal belongs. The parameter
	 * must not be null. 
	 * 
	 * @param belongsTo
	 *            The organisation to which the goal belongs to henceforth.  
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Gets the name of the goal.
	 * 
	 * @return The name of the goal as String.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the goal.
	 * 
	 * @param name
	 * 			The new name of the goal as String.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the rule that defines which tasks have to be completed how to fulfil the goal.
	 * 
	 * @return The goal rule of this goal.
	 */
	public GoalRule getRule() {
		return rule;
	}

	/**
	 * Sets the rule of this goal which defines the needed tasks for fulfilling the goal.
	 * 
	 * @param rule
	 * 			The rule for this goal.
	 */
	public void setRule(GoalRule rule) {
		this.rule = rule;
	}

	/**
	 * Gets the value if the goal is repeatable. If it is this method returns true otherwise false.
	 * 
	 * @return The value if the goal is repeatable (true) or not (false).
	 */
	public boolean isRepeatable() {
		return repeatable;
	}

	/**
	 * Sets the value if the goal is repeatable (true) or not (false). 
	 * 
	 * @param repeatable
	 * 			Value of the goal's repeatability. True if it is, otherwise false.
	 */
	public void setRepeatable(boolean repeatable) {
		this.repeatable = repeatable;
	}

	/**
	 * Gets the value if the goal if a group goal. If it is this method returns true otherwise false.
	 * 
	 * @return The value if the goal is also a group goal (true) or not (false).
	 */
	public boolean isPlayerGroupGoal() {
		return playerGroupGoal;
	}

	/**
	 * Sets the value if the goal is also a group goal (true) or not (false). 
	 * 
	 * @param playerGroupGoal
	 * 				Value if the goal is a group goal. True if it is, otherwise false.
	 */
	public void setPlayerGroupGoal(boolean playerGroupGoal) {
		this.playerGroupGoal = playerGroupGoal;
	}

	/**
	 * Gets the List of rewards that are associated with the goal. All rewards can be earned
	 * by the player who completes the goal.
	 * 
	 * @return All rewards a player can earn by fulfilling the goal as a List.
	 */
	public List<Reward> getRewards() {
		return rewards;
	}

	/**
	 * Sets the List of that are associated with the goal. All rewards can be earned
	 * by the player who completes the goal.
	 * 
	 * @param rewards List of rewards a player can earn by fulfilling the goal as List.
	 */
	public void setRewards(List<Reward> rewards) {
		this.rewards = rewards;
	}

	/**
	 * Adds one or more rewards  to the list of rewards which can be earned by a player.
	 * 
	 * @param reward The rewards that should be added to the list of rewards.
	 */
	public void addRewards(List<Reward> reward) {
		rewards.addAll(rewards);
	}

	/**
	 * Adds one reward  to the list of rewards which can be earned by a player.
	 * 
	 * @param reward The reward that should be added to the list of rewards.
	 */
	public void addReward(Reward reward) {
		rewards.add(reward);
	}

	/**
	 * This method checks if a goal belongs to a specific organisation. Therefore
	 * it is tested if the organisation's API key matchs the group's API key. 
	 * 
	 * @param organisation
	 * 			The organisation object a goal may belongs to.
	 * @return Boolean value if the API key of the goal is the same 
	 * 			of the tested organisation (true) or not (false).
	 */
	public boolean belongsTo(Organisation organisation) {
		return getBelongsTo().getApiKey().equals(organisation.getApiKey());
	}

	/**
	 * Gets all a roles to check if a player is allowed to fulfil a goal. Therefore a player has to own 
	 * at least one of these roles.
	 *  
	 * @return All roles who are allowed to complete the goal as list.
	 */
	public List<Role> getCanCompletedBy() {
		return canCompletedBy;
	}

	/**
	 * Sets all roles which are needed to test if a player is allowed to fulfil a goal. Therefore a player has 
	 * to own at least one of these roles.
	 *  
	 * @param canCompletedBy All roles who are allowed to complete the goal as list. 
	 */
	public void setCanCompletedBy(List<Role> canCompletedBy) {
		this.canCompletedBy = canCompletedBy;
	}

	/**
	 * This method checks if a goal is completed after a task is finished. Therefore it is also checked if the
	 * goal is repeatable. If it is is can be fulfilled one more time otherwise the method stops.
	 *  
	 * @param oldFinishedGoals 
	 * 				The list of all goal a player has completed, yet.
	 * @param finishedTasksList 
	 * 				The list of all already finished tasks of a player.
	 * @param rule 
	 * 				The goal rule which is associated with the goal and indicates when the goal is completed.
	 * @return The just finished goal when the player hasn't finished it yet or if the goal can be finished one 
	 * 		more time otherwise null is returned. 
	 */
	public FinishedGoal checkGoal(List<FinishedGoal> oldFinishedGoals, List<FinishedTask> finishedTasksList, 
			TaskRule rule) {

		Goal goal = this;

		LocalDateTime finishedDate = LocalDateTime.now();
		LocalDateTime lastDate = null;

		// checks if goal is already finished
		if (oldFinishedGoals.size() > 0) {
			// goal is already finished
			LOGGER.debug("Goal: is on finishedGoals list");
			// check if goal is repeatable
			if (goal.isRepeatable()) {
				// get finishedDate of last goal
				LOGGER.debug("Goal: is repeatable");
				lastDate = oldFinishedGoals.get(oldFinishedGoals.size() - 1).getFinishedDate();
				LOGGER.debug("Goal: last finished: " + lastDate);
			} else {
				LOGGER.debug("Goal: is not repeatable -> break");
				return null;
			}

			// checks if goal/rule is completed after lastDate
			if (rule.checkRule(finishedTasksList, lastDate)) {
				// add goal to tempFinishedGoals list
				LOGGER.debug("Goal: Rule is completed! -> add to fGoalsList (temp)");
				FinishedGoal fGoal = new FinishedGoal();
				fGoal.setGoal(goal);
				fGoal.setFinishedDate(finishedDate);
				return fGoal;
			}
		} else {
			// goal has not yet been finished
			LOGGER.debug("Goal: is NOT on finished Goals list");
			// checks if goal/rule is completed after lastDate
			if (rule.checkRule(finishedTasksList, lastDate)) {
				// add goal to tempFinishedGoals list
				LOGGER.debug("Goal: Rule is completed! -> add to fGoalsList (temp)");
				FinishedGoal fGoal = new FinishedGoal();
				fGoal.setGoal(goal);
				fGoal.setFinishedDate(finishedDate);
				return fGoal;
			}
		}

		return null;
	}

	public static void logGoalDetails(String name, String repeatable, String ruleId, String rewardIds, String roleIds, String isGroupGoal, String apiKey) {
		LOGGER.debug("createNewGoal apiKey");
		LOGGER.debug("apiKey: " + apiKey);
		LOGGER.debug("name: " + name);
		LOGGER.debug("repeatable: " + repeatable);
		LOGGER.debug("ruleId: " + ruleId);
		LOGGER.debug("rewardIds: " + rewardIds);
		LOGGER.debug("rewardIds: " + roleIds);
	}

	
	/**
	 * This method gets the ids of goals that have to be deleted before a specific
	 * object like a rule or reward can be deleted. These ids are then passed to create 
	 * a message in the response to give the user a hint.
	 * 
	 * @param goals
	 * 			List of goals that are associated with an object that should be deleted.
	 */
	public static void checkGoalsForDeletion(List<Goal> goals, String objectToDelete, String type) {
		List<String> ids = getGoalIds(goals);
		StringUtils.printIdsForDeletion(ids, objectToDelete , type);
	}
	
	
	/**
	 * Gets the id each goal that is in the passed List.
	 * 
	 * @param goals
	 * 			List of goals of which the ids are returned.
	 * @return A list of Integers of the passed goals. 
	 */
	public static List<String> getGoalIds(List<Goal> goals){
		List<String> ids = new ArrayList<>();
		for (Goal goal : goals) {
			ids.add(Integer.toString(goal.getId()));
		}
		return ids;
	}
}
