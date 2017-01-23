package info.interactivesystems.gamificationengine.entities.task;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.dao.GoalDAO;
import info.interactivesystems.gamificationengine.dao.PlayerGroupDAO;
import info.interactivesystems.gamificationengine.dao.RuleDAO;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.PlayerGroup;
import info.interactivesystems.gamificationengine.entities.Role;
import info.interactivesystems.gamificationengine.entities.goal.FinishedGoal;
import info.interactivesystems.gamificationengine.entities.goal.Goal;
import info.interactivesystems.gamificationengine.entities.goal.TaskRule;
import info.interactivesystems.gamificationengine.entities.rewards.Points;
import info.interactivesystems.gamificationengine.entities.rewards.Reward;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The Super Class for different types of Task.
 * 
 * A Task is the basic module and represents for example a specific activity. By its creation
 * the roles were assigned which indicate who is allowed to fulfil this task. To complete the 
 * task only one of these roles is needed. One or more tasks can be assigned to a goal, so 
 * depending on the rule of the goal some additional tasks may also have to be completed to 
 * fulfill the goal so the player can earn the associated rewards. If the task is tradeable 
 * it can be offered in the marketplace, so that another player can do it and gets the reward
 * of it.
 */
@Entity
@JsonIgnoreProperties({ "belongsTo", "finishedTasks" })
public class Task implements Serializable {

	private static final long serialVersionUID = 8925734998433033594L;

	private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@ManyToOne
	private Organisation belongsTo;

	@NotNull
	private String taskName;

	private String description;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<Role> allowedFor;
	
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.EAGER, mappedBy="task")
	private List<FinishedTask> finishedTasks;

	private boolean tradeable;

	/**
	 * Gets the id of the task.
	 * 
	 * @return int value of the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of the task
	 * 
	 * @param id
	 *            the id of the task
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the organisation a task belongs to.
	 * 
	 * @return The organisation object the task belongs to.
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Sets the organisation a task belongs to so it can be completed 
	 * by its employees.
	 * 
	 * @param belongsTo
	 *            The organisation a task belongs to.
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Gets the name of a task.
	 * 
	 * @return The name of the task as String.
	 */
	public String getTaskName() {
		return taskName;
	}

	/**
	 * Sets the name of the task.
	 * 
	 * @param taskName
	 *            The name of the task as String.
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	/**
	 * Gets the description of a task.
	 * 
	 * @return The task's description as String.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of a task.
	 * 
	 * @param description
	 * 			The description of the task as a String.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets all roles for which the task is allowed. A player need only one role
	 * to complete the task.
	 * 
	 * @return List of roles which are allowed to complete the task.
	 */
	public List<Role> getAllowedFor() {
		return allowedFor;
	}

	/**
	 * Sets all roles for which the task is allowed. A player need only one role
	 * to complete the task.
	 * 
	 * @param allowedFor
	 *            List of roles which are allowed to complete the task.
	 */
	public void setAllowedFor(List<Role> allowedFor) {
		this.allowedFor = allowedFor;
	}

	/**
	 * Checks if a task is tradeable. This means if a player is allowed to offer
	 * it on the marketplace.
	 * 
	 * @return The value it the task is tradeable as boolean.
	 */
	public boolean isTradeable() {
		return tradeable;
	}

	/**
	 * If the tradeable field is set true a task as tradeable, so that it can be
	 * offered on the marketplace, if it is set to false the task cannot be traded.
	 * 
	 * @param tradable
	 *            The value (true/false) if the task is tradeable as boolean.
	 */
	public void setTradeable(boolean tradable) {
		this.tradeable = tradable;
	}

	/**
	 * Checks if a task belongs to a specific organisation. If the task has the 
	 * same API key like the organisation the method returns true and the task 
	 * belongs to this organisation otherwise false is returned.
	 * 
	 * @param organisation
	 *            The organisation which is tested.
	 * @return The value if a task belongs to the specific organisation (true) or
	 *         not (false).
	 */
	public boolean belongsTo(Organisation organisation) {
		return getBelongsTo().getApiKey().equals(organisation.getApiKey());
	}

	/**
	 * If a player completed a task this method adds the task to the list of finished 
	 * tasks if the player is allowed to complete this task. This is tested by the 
	 * roles a player has and the roles which are assigned to the task.  
	 * Also the method tests if the player isn't deactivated because then she/he isn't 
	 * allowed to complete tasks.
	 * 
	 * @param player
	 *            The player who completed the task. This parameter must not be null.
	 * @param ruleDao
	 *            The rule DAO is required to access the created rules.
	 * @param goalDao
	 *            The goal DAO is required to access created goals. 
	 * @param groupDao
	 *            The group DAO is required to access created groups.
	 * @param finishedDate
	 *            DateTime when the task has been finished the date time is stored. If 
	 *            the value is null the date is set to now.
	 * @param apiKey
	 *          The API key of the organisation. 
	 */
	public void completeTask(Player player, RuleDAO ruleDao, GoalDAO goalDao, PlayerGroupDAO groupDao,
			LocalDateTime finishedDate, String apiKey) {

		if (!player.isActive()) {
			throw new ApiError(Response.Status.FORBIDDEN, "Player is inactive!");
		}

		Task task = this;

		// set tempFinishedGoals list to add this to the player at the end --> avoid transaction errors
		List<FinishedGoal> finishedPlayerGoalsList = new ArrayList<>();
		List<Reward> recievedRewards = new ArrayList<>();
		List<Role> matchingRoles = new ArrayList<>();

		// set Timestamp
		if (finishedDate == null) {
			finishedDate = LocalDateTime.now();
		}
		
		boolean pointsRecieved = false;

		FinishedTask fTask = new FinishedTask();
		fTask.setTask(task);
		fTask.setFinishedDate(finishedDate);
		fTask.setPlayer(player);

		logPlayerDetails(player);

		// check if task can be completed by player
		playerIsAllowed(player, task, matchingRoles);
		
		List<FinishedTask> playerFinishedTasksList = player.getFinishedTasks();
		playerFinishedTasksList.add(fTask);

		logTasks(player, playerFinishedTasksList);

		// search all rules which contain this task
		List<TaskRule> rules = ruleDao.getRulesByTask(task, apiKey);

		LOGGER.debug("Rule count: " + rules.size());

		// for each rule...
		for (TaskRule rule : rules) {

			LOGGER.debug("Rule: " + rule.getName());

			// get goals which contain this rule
			for (Goal goal : goalDao.getGoalsByRule(rule, apiKey)) {

				logGoalandRoleNames(goal, player);

				if (goal.getCanCompletedBy().size() > 0) {
					LOGGER.debug("Goal is restricted by roles");
					matchingRoles = goal.getCanCompletedBy().stream().filter(r -> {
						if (player.getBelongsToRoles().contains(r)) {
							LOGGER.debug("Player has required Role to Complete Goal: " + r.getName());
							return true;
						} else {
							return false;
						}
					}).collect(Collectors.toList());

					if (matchingRoles.size() > 0) {
						LOGGER.debug("Roles match -> proceed");
					} else {
						LOGGER.debug("Roles don't match -> goal can not be completed");
						continue;
					}
				} else {
					LOGGER.debug("Goal is not restricted by roles");
				}

				List<FinishedGoal> oldFinishedGoals = new ArrayList<>();

				// check if goal is groupGoal
				if (!goal.isPlayerGroupGoal()) {

					oldFinishedGoals.addAll(player.getFinishedGoalsByGoal(goal));

					// check if goal is completed
					FinishedGoal tempFinishedGoal = goal.checkGoal(player, null, oldFinishedGoals, playerFinishedTasksList, rule);
					if (tempFinishedGoal != null) {
						finishedPlayerGoalsList.add(tempFinishedGoal);
					}
				} else {

					// get all groups from player
					List<PlayerGroup> allGroups = groupDao.getAllGroups(apiKey);
					List<PlayerGroup> playerGroups = new ArrayList<>();
					List<Role> matchingGroupRoles = new ArrayList<>();

					for (PlayerGroup g : allGroups) {
						if (g.getPlayers().contains(player)) {
							playerGroups.add(g);
						}
					}

					// for each group
					for (PlayerGroup group : playerGroups) {
						// get finishedGoals
						List<FinishedGoal> groupFinishedGoals = group.getFinishedGoals();

						// get finished tasks from all players
						List<FinishedTask> groupFinishedTasksList = new ArrayList<>();
						for (Player p : group.getPlayers()) {
							groupFinishedTasksList.addAll(p.getFinishedTasks());
						}
						
						//Test, if one player role of the group match with role of the goal 
						if (goal.getCanCompletedBy().size() > 0) {
							LOGGER.debug("Pointsgoal is restricted by roles");
							
							for(Player everyGroupPlayer : group.getPlayers()){
									matchingGroupRoles.addAll(goal.getCanCompletedBy().stream().filter(r -> {
										if (everyGroupPlayer.getBelongsToRoles().contains(r)) {
											LOGGER.debug("Player has required Role to Complete Pointgoal: " + r.getName());
											return true;
										} else {
											return false;
										}
								}).collect(Collectors.toList()));
							}
								
							if (matchingGroupRoles.size() > 0) {
								LOGGER.debug("Roles match for PointGoal -> proceed");
							} else {
								LOGGER.debug("Roles don't match for Pointgoal -> Pointgoal can not be completed");
								continue;
							}
						} else {
							LOGGER.debug("Pointgoal is not restricted by roles");
						}			
						
						
						// check if goal is completed and add it to finishedGoals of group
						FinishedGoal tempFinishedGoal = goal.checkGoal(null, group, groupFinishedGoals, groupFinishedTasksList, rule);
						if (tempFinishedGoal != null) {
							// add goal to finishedGoals list
							groupFinishedGoals.add(tempFinishedGoal);
							
							// add rewards to group
							for (Reward r : goal.getRewards()) {
								LOGGER.debug("Add Reward to group");
								if(r instanceof Points){
									((Points) r).addReward(group, goalDao, ruleDao);
								} else {
									r.addReward(group, goalDao, ruleDao);
								}
							}

							//Control
							for (PlayerGroup gr : playerGroups) {
								LOGGER.debug("Group points are: " + gr.getPoints());
							}
							
						}

					}

				}
			}
		}

		// proceed with fGoalsList
		LOGGER.debug("proceed with fGoalsList");
		for (FinishedGoal fGoal : finishedPlayerGoalsList) {

			if(!fGoal.getGoal().isPlayerGroupGoal()){
				fGoal.setPlayer(player);
			}
			
			// for each reward -> addReward
			for (Reward reward : fGoal.getGoal().getRewards()) {

				LOGGER.debug("Reward: " + reward.getId());

				// only add point rewards
				if (reward instanceof Points) {
					LOGGER.debug("Reward: instanceof Points -> get points");
					Points r = (Points) reward;
					LOGGER.debug("Reward Points: " + r.getAmount());
					reward.addReward(player, goalDao, ruleDao);
					pointsRecieved = true;
				} else {
					// other awards will be added afterwards
					LOGGER.debug("Reward: NOT instanceof Points -> add to recievedRewards");
					recievedRewards.add(reward);
				}
			}
		}

		LOGGER.debug("add finishedGoals to player");
		// add Goals to finishedGaolsList
		player.addFinishedGoal(finishedPlayerGoalsList);

		LOGGER.debug("add Rewards to player");
		// add Rewards to rewardList
		for (Reward reward : recievedRewards) {
			LOGGER.debug("Reward id : " + reward.getId());
			reward.addReward(player, goalDao, ruleDao);
		}

		logPlayerDetails(player);
	}

	
	private void logPlayerDetails(Player player) {
		LOGGER.debug("Player Name: " + player.getNickname());
		LOGGER.debug("Player Points: " + player.getPoints());
		LOGGER.debug("Player Currency: " + player.getCoins());
		LOGGER.debug("Player Tasks: " + player.getFinishedTasks().size());
		LOGGER.debug("Player Goals: " + player.getFinishedGoals().size());
	}

	private void logGoalandRoleNames(Goal goal, Player player) {
		LOGGER.debug("Goal: " + goal.getName());

		// check if goal can be completed by player
		LOGGER.debug("Player Roles:");
		for (Role r : player.getBelongsToRoles()) {
			LOGGER.debug("- " + r.getName());
		}

		LOGGER.debug("Goal Roles:");
		for (Role r : goal.getCanCompletedBy()) {
			LOGGER.debug("- " + r.getName());
		}
		
	}

	private void logTasks(Player player, List<FinishedTask> playerFinishedTasksList) {
		LOGGER.debug("Player Tasks: " + player.getFinishedTasks().size());
		LOGGER.debug("Player Tasks last item: " + player.getFinishedTasks().get(player.getFinishedTasks().size() - 1).getFinishedDate());
		LOGGER.debug("Temp Tasks List: " + playerFinishedTasksList.size());
		LOGGER.debug("Temp Tasks List last item: " + playerFinishedTasksList.get((playerFinishedTasksList.size() - 1)).getFinishedDate());
	}

	public void playerIsAllowed(Player player, Task task, List<Role> matchingRoles){
		
		LOGGER.debug("Player Roles:");
		for (Role r : player.getBelongsToRoles()) {
			LOGGER.debug("- " + r.getName());
		}

		LOGGER.debug("Task Roles:");
		for (Role r : task.getAllowedFor()) {
			LOGGER.debug("- " + r.getName());
		}

		if (task.getAllowedFor().size() > 0) {
			LOGGER.debug("Task is restricted by roles");
			matchingRoles = task.getAllowedFor().stream().filter(r -> {
				if (player.getBelongsToRoles().contains(r)) {
					LOGGER.debug("Player has required Role to Complete Task: " + r.getName());
					return true;
				} else {
					return false;
				}
			}).collect(Collectors.toList());

			if (matchingRoles.size() > 0) {
				LOGGER.debug("Roles match -> proceed");
			} else {
				LOGGER.debug("Roles don't match -> error");
				throw new ApiError(Response.Status.FORBIDDEN, "Roles don't match!");
			}
		} else {
			LOGGER.debug("Task is not restricted by roles");
		}
	}
	
}
