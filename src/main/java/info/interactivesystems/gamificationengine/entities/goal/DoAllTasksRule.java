package info.interactivesystems.gamificationengine.entities.goal;

import info.interactivesystems.gamificationengine.entities.task.FinishedTask;
import info.interactivesystems.gamificationengine.entities.task.Task;
import info.interactivesystems.gamificationengine.utils.Progress;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  A DoAllTasksRule defines a task rule by which all mentioned tasks have to be fulfilled. If all tasks are 
 *  finished the goal rule an so the associated goal is completed.
 *
 */
@Entity
@Inheritance
@DiscriminatorValue("TRULEALL")
public class DoAllTasksRule extends TaskRule {

	private static final Logger log = LoggerFactory.getLogger(DoAllTasksRule.class);

	/**
	 * Gets the tasks of a DoAllTasksRule that are already finished. So the user gets a status which tasks she/he 
	 * dosen't have to complete any more. Therefore a list of finished tasks that were done by a specific 
	 * player is filtered if they already needed for this rule. If a task was done and is needed for fulfilling 
	 * the rule it is added to a list.  
	 * 
	 * @param finishedPlayerTasks
	 * 			The list of all tasks a specific player has already done.
	 * @param lastDate
	 * 			Optionally a date can be passed. If it isn't null all tasks after this date are checked. 
	 * @return The list of already completed tasks of a task rule.
	 */
	public List<Task> getCompletedTasks(List<FinishedTask> finishedPlayerTasks, LocalDateTime lastDate) {

		List<Task> completedTasks = new ArrayList<>();
		List<Task> finishedTasks;

		log.debug("Last Date: " + lastDate);
		log.debug("Temp Tasks List last item: " + finishedPlayerTasks.get((finishedPlayerTasks.size() - 1)).getFinishedDate());

		if (lastDate != null) {
			// grouping and counting finished tasks
			finishedTasks = finishedPlayerTasks.stream().filter(o -> tasks.contains(o.getTask()) && o.getFinishedDate().isAfter(lastDate))
					.map(FinishedTask::getTask).collect(Collectors.toList());
		} else {
			// grouping and counting finished tasks
			finishedTasks = finishedPlayerTasks.stream().filter(o -> tasks.contains(o.getTask())).map(FinishedTask::getTask)
					.collect(Collectors.toList());
		}

		for (Task task : tasks) {
			if (finishedTasks.remove(task)) {
				log.debug("Completed Task: " + task.getTaskName());
				completedTasks.add(task);
			}
		}

		return completedTasks;
	}

	
	/**
	 * Gets the tasks of a DoAllTasksRule that are not finished yet. So the user gets a status which tasks she/he 
	 * has to complete for fulfilling this rule. Therefore a list of finished tasks that were done by a specific 
	 * player is filtered if they needed for this rule. If a task wasn't already done and is needed for fulfilling 
	 * the rule it is added to a list.  
	 * 
	 * @param finishedPlayerTasks
	 * 			The list of all tasks a specific player has already done.
	 * @param lastDate
	 * 			The date after all tasks are checked if they were completed in this period of time. 
	 * @return A list of missing tasks which have to be completed until the task rule is fulfilled.
	 */
	public List<Task> getUncompletedTasks(List<FinishedTask> finishedPlayerTasks, LocalDateTime lastDate) {

		// grouping and counting unfinished tasks
		List<Task> uncompletedTasks = tasks.stream().filter(o -> !getCompletedTasks(finishedPlayerTasks, lastDate).contains(o))
				.collect(Collectors.toList());

		return uncompletedTasks;
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
	 * This method checks if a rule is fulfilled. Therefore the list of all already finished tasks is checked
	 * if all needed tasks are in this list. If they are, they are added to a list. In the end the count of 
	 * finished tasks and needed task are compared. It they match the rule is completed and true is returned 
	 * otherwise false is returned.
	 * 
	 * @param finishedPlayerTasks
	 * 			The list of already finished tasks a player has already completed.
	 * @param lastDate
	 * 			Optionally a date can be passed. If it isn't null all tasks after this date are checked. 
	 */
	@Override
	public boolean checkRule(List<FinishedTask> finishedPlayerTasks, LocalDateTime lastDate) {

		Map<String, Long> finishedTasks;

		// grouping and counting tasks by name
		Map<String, Long> tasksToComplete = tasks.stream().collect(Collectors.groupingBy(Task::getTaskName, Collectors.counting()));

		log.debug(" Rule = DoALLTasksRule! ");
		log.debug("Last Date: " + lastDate);
		log.debug("Temp Tasks List last item: " + finishedPlayerTasks.get((finishedPlayerTasks.size() - 1)).getFinishedDate());

		if (lastDate != null) {

			// grouping and counting finished tasks after last finishedDate
			finishedTasks = finishedPlayerTasks.stream().filter(o -> tasks.contains(o.getTask()) && o.getFinishedDate().isAfter(lastDate))
					.collect(Collectors.groupingBy(o -> o.getTask().getTaskName(), Collectors.counting()));

		} else {

			// grouping and counting finished tasks
			finishedTasks = finishedPlayerTasks.stream().filter(o -> tasks.contains(o.getTask()))
					.collect(Collectors.groupingBy(o -> o.getTask().getTaskName(), Collectors.counting()));

		}

		// matching number of tasks
		for (Map.Entry<String, Long> stringLongEntry : tasksToComplete.entrySet()) {
			if (finishedTasks.containsKey(stringLongEntry.getKey())) {
				if (finishedTasks.get(stringLongEntry.getKey()) < stringLongEntry.getValue()) {
					// not enough finished tasks of this type
					log.debug("not enough finished tasks of this type: " + stringLongEntry.getKey() + " -> "
							+ finishedTasks.get(stringLongEntry.getKey()) + "/" + stringLongEntry.getValue());
					return false;
				}
			} else {
				// task is missing in finished tasks
				log.debug("task is missing in finished tasks: " + stringLongEntry.getKey());
				return false;
			}
		}

		return true;

	}

	/**
	 * Returns the progress of the task rule. This progress is represented by the number of the already 
	 * finished tasks and the number of tasks which has to be completed for fulfilling this rule.
	 * 
	 * @param finishedPlayerTasks
	 * 			The number of finished tasks a player has already completed. This list is checked which 
	 * 			tasks are needed for the rule.
	 * @param lastDate
	 * 			Optionally a date can be passed. If it isn't null all tasks after this date are checked.
	 */
	@Override
	public Progress getProgress(List<FinishedTask> finishedPlayerTasks, LocalDateTime lastDate) {

		Progress progress = new Progress(getCompletedTasks(finishedPlayerTasks, lastDate).size(), getTasks().size());

		return progress;
	}
}
