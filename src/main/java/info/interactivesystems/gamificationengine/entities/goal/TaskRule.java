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

@Entity
@Inheritance
// @DiscriminatorValue("TRULE")
// @MappedSuperclass
public abstract class TaskRule extends GoalRule {

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	// @JoinTable(name = "GoalRule_Task", joinColumns = @JoinColumn(name =
	// "GoalRule_id"), inverseJoinColumns = @JoinColumn(name = "tasks_id"))
	protected List<Task> tasks;

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public void addTasks(List<Task> tasks) {
		this.tasks.addAll(tasks);
	}

	public void addTask(Task task) {
		tasks.add(task);
	}

	public abstract Progress getProgress(List<FinishedTask> finishedPlayerTasks, LocalDateTime lastDate);

}
