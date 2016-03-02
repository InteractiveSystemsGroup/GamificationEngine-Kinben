package info.interactivesystems.gamificationengine.entities.goal;

import info.interactivesystems.gamificationengine.entities.task.FinishedTask;
import info.interactivesystems.gamificationengine.entities.task.Task;
import info.interactivesystems.gamificationengine.utils.Progress;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.ManyToMany;

/**
 * A TaskRule defines the combination of tasks until a goal is fulfilled. So for a defined sample of tasks 
 * either all of them have to be fulfilled (type: DoAllTaskRule) which corresponds an AND-expression, or 
 * only one of a specific selection, which is like an OR-expression (type: DoAnyTaskRule).
 *
 */
@Entity
@Inheritance
// @DiscriminatorValue("TRULE")
// @MappedSuperclass
public abstract class TaskRule extends GoalRule {

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	// @JoinTable(name = "GoalRule_Task", joinColumns = @JoinColumn(name =
	// "GoalRule_id"), inverseJoinColumns = @JoinColumn(name = "tasks_id"))
	protected List<Task> tasks;

	/**
	 * Gets the List of tasks which are contained in the goal rule definition. The specific rule is defined by 
	 * the use DoALlTasksRule or DoAnyTaskRule.
	 * 
	 * @return A list of tasks which are needed in goal rule's deifnition.
	 */
	public List<Task> getTasks() {
		return tasks;
	}

	/**
	 * Sets the List of tasks which are contained in the goal rule's definition. The specific rule is defined by 
	 * the use DoALlTasksRule or DoAnyTaskRule.
	 * 
	 * @param tasks
	 * 			The list of tasks which are needed for the definition. 
	 */
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	/**
	 * Adds one or more tasks to the list of tasks of a rule definition.
	 * 
	 * @param tasks
	 * 			The list of tasks which are added to the rule's list of tasks.
	 */
	public void addTasks(List<Task> tasks) {
		this.tasks.addAll(tasks);
	}

	/**
	 * Adds only one task to the list of tasks of a rule definition.
	 * 
	 * @param task
	 * 			The task which is added to the rule's list of task.
	 */
	public void addTask(Task task) {
		tasks.add(task);
	}

	/**
	 * This method checks if a rule contains a specific task. If the task is defined in the rule it returns true
	 * otherwise false.
	 * 
	 * @param task
	 * 			The task which has to be checked.
	 * @return The boolean value if the task is contained in the task rule (true) or not (false).
	 */
	public boolean contains(Task task) {
		return tasks.contains(task);
	}

	/**
	 * Abstract method to get the progress of a TaskRule. Dependent on the type of rule another value is returned.
	 * 
	 * @param finishedPlayerTasks
	 * 			The list of finished tasks a player has already completed to check which are needed for the rule.
	 * @param lastDate
	 * 			The date after all tasks are checked if they were completed in this period of time. 
	 * @return The progress of the  rule dependent on it's type different values of the progress are returned.
	 */
	public abstract Progress getProgress(List<FinishedTask> finishedPlayerTasks, LocalDateTime lastDate);

	/**
	 * This rule checks if a rule is fulfilled. If it does true is returned otherwise false. Dependent on the
	 * type of rule this check is different.
	 *  
	 * @param finishedPlayerTasks
	 * 			The list of already finished tasks a player has already completed.
	 * @param lastDate
	 * 			The date a player has done a task. All dates after the passed date are checked.
	 * @return The boolean value if a rule is fulfilled (true) or not(false).
	 */
	public abstract boolean checkRule(List<FinishedTask> finishedPlayerTasks, LocalDateTime lastDate);
	
}
