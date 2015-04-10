package info.interactivesystems.gamificationengine.entities.goal;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Role;
import info.interactivesystems.gamificationengine.entities.rewards.Reward;
import info.interactivesystems.gamificationengine.entities.task.FinishedTask;

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

@Entity
public class Goal {

	private static final Logger log = LoggerFactory.getLogger(Goal.class);

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
	private List<Reward> rewards;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<Role> canCompletedBy;

	public Goal() {
		rewards = new ArrayList<>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Organisation getBelongsTo() {
		return belongsTo;
	}

	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GoalRule getRule() {
		return rule;
	}

	public void setRule(GoalRule rule) {
		this.rule = rule;
	}

	public boolean isRepeatable() {
		return repeatable;
	}

	public void setRepeatable(boolean repeatable) {
		this.repeatable = repeatable;
	}

	public boolean isPlayerGroupGoal() {
		return playerGroupGoal;
	}

	public void setPlayerGroupGoal(boolean playerGroupGoal) {
		this.playerGroupGoal = playerGroupGoal;
	}

	public List<Reward> getRewards() {
		return rewards;
	}

	public void setRewards(List<Reward> rewards) {
		this.rewards = rewards;
	}

	public void addRewards(List<Reward> reward) {
		rewards.addAll(rewards);
	}

	public void addReward(Reward reward) {
		rewards.add(reward);
	}

	public boolean belongsTo(Organisation organisation) {
		return getBelongsTo().getApiKey().equals(organisation.getApiKey());
	}

	public List<Role> getCanCompletedBy() {
		return canCompletedBy;
	}

	public void setCanCompletedBy(List<Role> canCompletedBy) {
		this.canCompletedBy = canCompletedBy;
	}

	public FinishedGoal checkGoal(List<FinishedGoal> oldFinishedGoals, List<FinishedTask> finishedTasksList, TaskRule rule) {

		Goal goal = this;

		LocalDateTime finishedDate = LocalDateTime.now();
		LocalDateTime lastDate = null;

		// check if goal is already finished
		if (oldFinishedGoals.size() > 0) {
			// goal is already finished
			log.debug("Goal: is on finishedGoals list");
			// check if goal is repeatable
			if (goal.isRepeatable()) {
				// get finishedDate of last goal
				log.debug("Goal: is repeatable");
				lastDate = oldFinishedGoals.get(oldFinishedGoals.size() - 1).getFinishedDate();
				log.debug("Goal: last finish: " + lastDate);
			} else {
				log.debug("Goal: is not repeatable -> break");
				return null;
			}

			// check if goal/rule is completed after lastDate
			if (rule.checkRule(finishedTasksList, lastDate)) {
				// add goal to tempFinishedGoals list
				log.debug("Goal: Rule is completed! -> add to fGoalsList (temp)");
				FinishedGoal fGoal = new FinishedGoal();
				fGoal.setGoal(goal);
				fGoal.setFinishedDate(finishedDate);
				return fGoal;
			}
		} else {
			// goal has not yet been finished
			log.debug("Goal: is NOT on finished Goals list");
			// check if goal/rule is completed after lastDate
			if (rule.checkRule(finishedTasksList, lastDate)) {
				// add goal to tempFinishedGoals list
				log.debug("Goal: Rule is completed! -> add to fGoalsList (temp)");
				FinishedGoal fGoal = new FinishedGoal();
				fGoal.setGoal(goal);
				fGoal.setFinishedDate(finishedDate);
				return fGoal;
			}
		}

		return null;
	}

}
