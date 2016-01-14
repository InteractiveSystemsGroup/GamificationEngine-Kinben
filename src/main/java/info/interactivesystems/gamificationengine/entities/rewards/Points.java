package info.interactivesystems.gamificationengine.entities.rewards;

import info.interactivesystems.gamificationengine.dao.GoalDAO;
import info.interactivesystems.gamificationengine.dao.RuleDAO;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.PlayerGroup;
import info.interactivesystems.gamificationengine.entities.Role;
import info.interactivesystems.gamificationengine.entities.goal.FinishedGoal;
import info.interactivesystems.gamificationengine.entities.goal.GetPointsRule;
import info.interactivesystems.gamificationengine.entities.goal.Goal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Points class serves as a Reward-subclass, that allocates points to a player.
 * Points are a volatile reward which can be earned more than one time. The
 * awarded points are added to the current ones of a player.
 */
@Entity
@DiscriminatorValue("RewPoints")
public class Points extends VolatileReward {

	private static final Logger log = LoggerFactory.getLogger(Points.class);

	@NotNull
	private int amount;

	/**
	 * Gets the concrete amount of points which a player can earn as a
	 * reward.
	 * 
	 * @return The concrete amount as int.
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * Sets a specific amount of points which a player can earn as a reward.
	 * 
	 * @param amount
	 *            The amount of points which a player can earn as int.
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * Awards the player the concrete amount of points and add it to the
	 * player's current points. After that it's checked if a PointsRule is
	 * fulfilled so that the player can also earn another reward.
	 * 
	 * @param player
	 *            The player who should award the points. This parameter must
	 *            not be null.
	 * @param goalDao
	 *            The goal DAO is required to access created goals. 
	 * @param ruleDao
	 *            The rule DAO is required to access the created rules. 
	 */
	@Override
	public void addReward(Player player, GoalDAO goalDao, RuleDAO ruleDao) {

		LocalDateTime lastDate = null;
		LocalDateTime finishedDate = LocalDateTime.now();
		List<FinishedGoal> fGoalsList = new ArrayList<>();
		List<Reward> recievedRewards = new ArrayList<>();
		//New: Test for matching roles
		List<Role> matchingRoles;

		log.debug("Add points to player: " + amount);

		player.awardPoints(amount);

		log.debug("Points recieved -> check all points rules");

		List<GetPointsRule> completedPointsRules = ruleDao.getAllPointsRules().stream().map(r -> (GetPointsRule) r).filter(r -> r.checkRule(player))
				.collect(Collectors.toList());

		// for each completed rule
		for (GetPointsRule rule : completedPointsRules) {

			log.debug("PointsRule: " + rule.getName());

			// get goals which contain this rule
			for (Goal goal : goalDao.getGoalsByRule(rule)) {
				
				//New: Test, if player role match with role of the goal 
				if (goal.getCanCompletedBy().size() > 0) {
					log.debug("Pointsgoal is restricted by roles");
					matchingRoles = goal.getCanCompletedBy().stream().filter(r -> {
						if (player.getBelongsToRoles().contains(r)) {
							log.debug("Player has required Role to Complete Pointgoal: " + r.getName());
							return true;
						} else {
							return false;
						}
					}).collect(Collectors.toList());

					if (matchingRoles.size() > 0) {
						log.debug("Roles match for PointGoal -> proceed");
					} else {
						log.debug("Roles don't match for Pointgoal -> Pointgoal can not be completed");
						continue;
					}
				} else {
					log.debug("Pointgoal is not restricted by roles");
				}//Until here
				
				List<FinishedGoal> oldFinishedGoals = player.getFinishedGoalsByGoal(goal);
				
				// check if goal is already finished
				if (oldFinishedGoals.size() > 0) {
					// goal is already finished
					log.debug("Points Goal: is on finishedGoals list");
					// check if goal is repeatable
					if (goal.isRepeatable()) {
						// get finishedDate of last goal
						log.debug("Points Goal: is repeatable");
						lastDate = oldFinishedGoals.get(oldFinishedGoals.size() - 1).getFinishedDate();
					} else {
						log.debug("Points Goal: is not repeatable -> break");
						break;
					}

					// check if goal/rule is completed after lastDate
					if (rule.checkRule(player)) {
						// add goal to tempFinishedGoals list
						log.debug("Points Goal: Rule is completed! -> add to fGoalsList (temp)");
						FinishedGoal fGoal = new FinishedGoal();
						fGoal.setGoal(goal);
						fGoal.setFinishedDate(finishedDate);
						fGoalsList.add(fGoal);
					}
				} else {
					// goal has not yet been finished
					log.debug("Points Goal: is NOT on finished Goals list");
					// check if goal/rule is completed after lastDate
					if (rule.checkRule(player)) {
						// add goal to tempFinishedGoals list
						log.debug("Points Goal: Rule is completed! -> add to fGoalsList (temp)");
						FinishedGoal fGoal = new FinishedGoal();
						fGoal.setGoal(goal);
						fGoal.setFinishedDate(finishedDate);
						fGoalsList.add(fGoal);
						// for each reward -> addReward
						for (Reward reward : goal.getRewards()) {
							recievedRewards.add(reward);
						}
					}
				}
			}
		}

		log.debug("add finishedGoals to player");
		// add Goals to finishedGaolsList
		if(fGoalsList.size() > 0){
			player.addFinishedGoal(fGoalsList);
		}

		log.debug("add Rewards to player");
		// add Rewards to rewardList
		for (Reward reward : recievedRewards) {
			reward.addReward(player, goalDao, ruleDao);
		}

	}

	/**
	 * Awards the group of players the concrete amount of points and add them to
	 * the group's current points. After that it is checked if a PointsRule is
	 * fulfilled so that another reward can also be earned.
	 * 
	 * @param group
	 *            The group of players which should award the points. This parameter 
	 *            must not be null.
	 * @param goalDao
	 *            The goal DAO is required to access created goals. 
	 * @param ruleDao
	 *            The rule DAO is required to access the created rules. 
	 */
	@Override
	public void addReward(PlayerGroup group, GoalDAO goalDao, RuleDAO ruleDao) {

		LocalDateTime lastDate = null;
		LocalDateTime finishedDate = LocalDateTime.now();
		List<FinishedGoal> fGoalsList = new ArrayList<>();
		List<Reward> recievedRewards = new ArrayList<>();
		//New: Test for matching roles
		List<Role> matchingRoles;
		
		log.debug("Add points to group: " + amount);

		group.awardPoints(amount);

		log.debug("Group: Points recieved -> check all points rules");

		// TODO check for organisation and check for group goal
		List<GetPointsRule> completedPointsRules = ruleDao.getAllPointsRules().stream().map(r -> (GetPointsRule) r).filter(r -> r.checkRule(group))
				.collect(Collectors.toList());

		// for each completed rule
		for (GetPointsRule rule : completedPointsRules) {

			log.debug("Group: PointsRule: " + rule.getName());

			// get goals which contain this rule
			for (Goal goal : goalDao.getGoalsByRule(rule)) {

							
				
				List<FinishedGoal> oldFinishedGoals = group.getFinishedGoalsByGoal(goal);

				// check if goal is already finished
				if (oldFinishedGoals.size() > 0) {
					// goal is already finished
					log.debug("Group: Points Goal: is on finishedGoals list");
					// check if goal is repeatable
					if (goal.isRepeatable()) {
						// get finishedDate of last goal
						log.debug("Group: Points Goal: is repeatable");
						lastDate = oldFinishedGoals.get(oldFinishedGoals.size() - 1).getFinishedDate();
					} else {
						log.debug("Group: Points Goal: is not repeatable -> break");
						break;
					}

					// check if goal/rule is completed after lastDate
					if (rule.checkRule(group)) {
						// add goal to tempFinishedGoals list
						log.debug("Group: Points Goal: Rule is completed! -> add to fGoalsList (temp)");
						FinishedGoal fGoal = new FinishedGoal();
						fGoal.setGoal(goal);
						fGoal.setFinishedDate(finishedDate);
						fGoalsList.add(fGoal);
					}
				} else {
					// goal has not yet been finished
					log.debug("Group: Points Goal: is NOT on finished Goals list");
					// check if goal/rule is completed after lastDate
					if (rule.checkRule(group)) {
						// add goal to tempFinishedGoals list
						log.debug("Group: Points Goal: Rule is completed! -> add to fGoalsList (temp)");
						FinishedGoal fGoal = new FinishedGoal();
						fGoal.setGoal(goal);
						fGoal.setFinishedDate(finishedDate);
						fGoalsList.add(fGoal);
						// for each reward -> addReward
						for (Reward reward : goal.getRewards()) {
							recievedRewards.add(reward);
						}
					}
				}
			}
		}

		log.debug("Group: add finishedGoals to group");
		// add Goals to finishedGaolsList
		group.getFinishedGoals().addAll(fGoalsList);

		log.debug("Group: add Rewards to group");
		// add Rewards to rewardList
		for (Reward reward : recievedRewards) {
			reward.addReward(group, goalDao, ruleDao);
		}

	}
}
