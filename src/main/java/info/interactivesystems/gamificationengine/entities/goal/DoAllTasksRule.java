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

@Entity
@Inheritance
@DiscriminatorValue("TRULEALL")
public class DoAllTasksRule extends TaskRule {

	private static final Logger log = LoggerFactory.getLogger(DoAllTasksRule.class);

	// return list of completed tasks
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

	// return the missing tasks
	public List<Task> getUncompletedTasks(List<FinishedTask> finishedPlayerTasks, LocalDateTime lastDate) {

		// grouping and counting unfinished tasks
		List<Task> uncompletedTasks = tasks.stream().filter(o -> !getCompletedTasks(finishedPlayerTasks, lastDate).contains(o))
				.collect(Collectors.toList());

		return uncompletedTasks;
	}

	// check if rule contains tasks
	public boolean contains(Task task) {

		return tasks.contains(task);

	}

	@Override
	public boolean checkRule(List<FinishedTask> finishedPlayerTasks, LocalDateTime lastDate) {

		Map<String, Long> finishedTasks;

		// grouping and counting tasks by name
		Map<String, Long> tasksToComplete = tasks.stream().collect(Collectors.groupingBy(Task::getTaskName, Collectors.counting()));

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

	@Override
	public Progress getProgress(List<FinishedTask> finishedPlayerTasks, LocalDateTime lastDate) {

		Progress progress = new Progress(getCompletedTasks(finishedPlayerTasks, lastDate).size(), getTasks().size());

		return progress;
	}
}
