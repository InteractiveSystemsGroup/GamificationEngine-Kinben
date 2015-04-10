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
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Super Class for different types of Task.
 * 
 */
@Entity
public class Task implements Serializable {

	private static final long serialVersionUID = 8925734998433033594L;

	private static final Logger log = LoggerFactory.getLogger(Task.class);

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

	private boolean tradeable;

	/**
	 * Get the id of the task.
	 * 
	 * @return int value of the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the id of the Task
	 * 
	 * @param id
	 *            the id of the task
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get the organisation a task belongs to and can completed by its
	 * employees.
	 * 
	 * @return the organisation object
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Set the organisation a task belongs to and can completed by its
	 * employees.
	 * 
	 * @param belongsTo
	 *            the organisation a task belongs to
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Get the name of a task.
	 * 
	 * @return the name of the task as String
	 */
	public String getTaskName() {
		return taskName;
	}

	/**
	 * Set the name of the task.
	 * 
	 * @param taskName
	 *            the name of the task as String.
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	/**
	 * Get the description of a task.
	 * 
	 * @return the task's description as String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description of a task.
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get all roles for which the task is allowed. A player need only one role
	 * to complete the task.
	 * 
	 * @return List of roles which are allowed to complete the task.
	 */
	public List<Role> getAllowedFor() {
		return allowedFor;
	}

	/**
	 * Set all roles for which the task is allowed. A player need only one role
	 * to complete the task.
	 * 
	 * @param allowedFor
	 *            List of roles which are allowed to complete the task.
	 */
	public void setAllowedFor(List<Role> allowedFor) {
		this.allowedFor = allowedFor;
	}

	/**
	 * Check if a task is tradeable. This means if a player is allowed to offer
	 * it on the market place.
	 * 
	 * @return value of task is tradeable as boolean
	 */
	public boolean isTradeable() {
		return tradeable;
	}

	/**
	 * Set a task as tradeable, so that it can be offered on the market place.
	 * 
	 * @param tradable
	 *            value if the task is tradeable as boolean.
	 */
	public void setTradeable(boolean tradable) {
		this.tradeable = tradable;
	}

	/**
	 * Check if a task belongs to a specific organisation.
	 * 
	 * @param organisation
	 *            the organisation which is tested
	 * @return value if a task belongs to the specific organisation (true) or
	 *         not
	 */
	public boolean belongsTo(Organisation organisation) {
		return getBelongsTo().getApiKey().equals(organisation.getApiKey());
	}

	/**
	 * 
	 * This method adds the completed task to the finished tasks list if the
	 * player is allowed to complete this task
	 * 
	 * @param organisation
	 *            The owner of the task
	 * @param player
	 *            The player who completed the task
	 * @param ruleDao
	 *            The rule DAO is required to access rules
	 * @param goalDao
	 *            The goal DAO is required to access goals
	 * @param groupDao
	 *            The group DAO is required to access groups
	 * @param finishedDate
	 *            DateTime when the task has been finished. If null the date
	 *            will be now.
	 */
	public void completeTask(Organisation organisation, Player player, RuleDAO ruleDao, GoalDAO goalDao, PlayerGroupDAO groupDao,
			LocalDateTime finishedDate) {

		if (!player.isActive()) {
			log.debug("Player is inactive!");
			throw new ApiError(Response.Status.FORBIDDEN, "Player is inactive!");
		}

		Task task = this;

		// set tempFinishedGoals list to add this to the player at the end
		// --> avoid transaction errors
		List<FinishedGoal> finishedPlayerGoalsList = new ArrayList<>();
		// List<FinishedGoal> finishedGroupGoalsList = new ArrayList<>();
		List<Reward> recievedRewards = new ArrayList<>();
		List<Role> matchingRoles;

		// set Timestamp
		if (finishedDate == null) {
			finishedDate = LocalDateTime.now();
		}
		// LocalDateTime lastDate = null;
		boolean pointsRecieved = false;

		FinishedTask fTask = new FinishedTask();
		fTask.setTask(task);
		fTask.setFinishedDate(finishedDate);

		log.debug("Player Name: " + player.getNickname());
		log.debug("Player Points: " + player.getPoints());
		log.debug("Player Currency: " + player.getCoins());
		log.debug("Player Tasks: " + player.getFinishedTasks().size());
		log.debug("Player Goals: " + player.getFinishedGoals().size());

		// check if task can be completed by player
		log.debug("Player Roles:");
		for (Role r : player.getBelongsToRoles()) {
			log.debug("- " + r.getName());
		}

		log.debug("Task Roles:");
		for (Role r : task.getAllowedFor()) {
			log.debug("- " + r.getName());
		}

		if (task.getAllowedFor().size() > 0) {
			log.debug("Task is restricted by roles");
			matchingRoles = task.getAllowedFor().stream().filter(r -> {
				if (player.getBelongsToRoles().contains(r)) {
					log.debug("Player has required Role to Complete Task: " + r.getName());
					return true;
				} else {
					return false;
				}
			}).collect(Collectors.toList());

			if (matchingRoles.size() > 0) {
				log.debug("Roles match -> proceed");
			} else {
				log.debug("Roles don't match -> error");
				throw new ApiError(Response.Status.FORBIDDEN, "Roles don't match!");
			}
		} else {
			log.debug("Task is not restricted by roles");
		}

		List<FinishedTask> playerFinishedTasksList = player.getFinishedTasks();
		playerFinishedTasksList.add(fTask);

		log.debug("Player Tasks: " + player.getFinishedTasks().size());
		log.debug("Player Tasks last item: " + player.getFinishedTasks().get(player.getFinishedTasks().size() - 1).getFinishedDate());
		log.debug("Temp Tasks List: " + playerFinishedTasksList.size());
		log.debug("Temp Tasks List last item: " + playerFinishedTasksList.get((playerFinishedTasksList.size() - 1)).getFinishedDate());

		// search all rules which contain this task
		List<TaskRule> rules = ruleDao.getRulesByTask(task, organisation);

		log.debug("Rule count: " + rules.size());

		// for each rule...
		for (TaskRule rule : rules) {

			log.debug("Rule: " + rule.getName());

			// get goals which contain this rule
			// for (Goal goal : rule.getGoals()) {
			for (Goal goal : goalDao.getGoalsByRule(rule)) {

				log.debug("Goal: " + goal.getName());

				// check if goal can be completed by player
				log.debug("Player Roles:");
				for (Role r : player.getBelongsToRoles()) {
					log.debug("- " + r.getName());
				}

				log.debug("Goal Roles:");
				for (Role r : goal.getCanCompletedBy()) {
					log.debug("- " + r.getName());
				}

				if (goal.getCanCompletedBy().size() > 0) {
					log.debug("Goal is restricted by roles");
					matchingRoles = goal.getCanCompletedBy().stream().filter(r -> {
						if (player.getBelongsToRoles().contains(r)) {
							log.debug("Player has required Role to Complete Goal: " + r.getName());
							return true;
						} else {
							return false;
						}
					}).collect(Collectors.toList());

					if (matchingRoles.size() > 0) {
						log.debug("Roles match -> proceed");
					} else {
						log.debug("Roles don't match -> goal can not be completed");
						continue;
					}
				} else {
					log.debug("Goal is not restricted by roles");
				}

				List<FinishedGoal> oldFinishedGoals = new ArrayList<>();

				// check if goal is groupGoal
				if (!goal.isPlayerGroupGoal()) {

					oldFinishedGoals.addAll(player.getFinishedGoalsByGoal(goal));

					// check if goal is completed
					FinishedGoal tempFinishedGoal = goal.checkGoal(oldFinishedGoals, playerFinishedTasksList, rule);
					if (tempFinishedGoal != null) {
						finishedPlayerGoalsList.add(tempFinishedGoal);
					}
				} else {

					// get all groups from player
					List<PlayerGroup> allGroups = groupDao.getAllGroupsByOrganisation(organisation);
					List<PlayerGroup> playerGroups = new ArrayList<>();

					for (PlayerGroup g : allGroups) {
						if (g.getPlayers().contains(player)) {
							playerGroups.add(g);
						}
					}

					// foreach group
					for (PlayerGroup g : playerGroups) {
						// set finishedGoals
						List<FinishedGoal> groupFinishedGoals = g.getFinishedGoals();

						// get fininshed tasks from all players
						List<FinishedTask> groupFinishedTasksList = new ArrayList<>();

						for (Player p : g.getPlayers()) {
							groupFinishedTasksList.addAll(p.getFinishedTasks());
						}

						// check if goal is completed and add it to
						// finishedGoals of group
						FinishedGoal tempFinishedGoal = goal.checkGoal(groupFinishedGoals, groupFinishedTasksList, rule);
						if (tempFinishedGoal != null) {
							// add goal to finishedGoals list
							groupFinishedGoals.add(tempFinishedGoal);

							// add rewards to group
							for (Reward r : goal.getRewards()) {
								log.debug("Add Reward to group");
								r.addReward(g, goalDao, ruleDao);
							}

						}

					}

				}
			}
		}

		// proceed with fGoalsList
		log.debug("procedd with fGoalsList");
		for (FinishedGoal fGoal : finishedPlayerGoalsList) {

			// for each reward -> addReward
			for (Reward reward : fGoal.getGoal().getRewards()) {

				log.debug("Reward: " + reward.getId());

				// only add point rewards
				if (reward instanceof Points) {
					log.debug("Reward: instanceof Points -> get points");
					Points r = (Points) reward;
					log.debug("Reward Points: " + r.getAmount());
					reward.addReward(player, goalDao, ruleDao);
					pointsRecieved = true;
				} else {
					// other awards will be added afterwards
					log.debug("Reward: NOT instanceof Points -> add to recievedRewards");
					recievedRewards.add(reward);
				}
			}
		}

		log.debug("add finishedGoals to player");
		// add Goals to finishedGaolsList
		player.addFinishedGoal(finishedPlayerGoalsList);

		log.debug("add Rewards to player");
		// add Rewards to rewardList
		for (Reward reward : recievedRewards) {
			reward.addReward(player, goalDao, ruleDao);
		}

		log.debug("Player Points: " + player.getPoints());
		log.debug("Player Currency: " + player.getCoins());

	}

}
