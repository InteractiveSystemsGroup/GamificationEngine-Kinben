package info.interactivesystems.gamificationengine.entities.task;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * When a player has completed a Task, it will be added to the player’s list of finished tasks. 
 * At the same time the date is also stored when this request was sent and the task was 
 * officially be done. If the task is the last one to fulfill a goal, the goal is also added 
 * to the player’s list of finished goals and the player will obtain all its associated 
 * rewards.
 */
@Entity
public class FinishedTask {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	private LocalDateTime finishedDate;

	@NotNull
	@ManyToOne
	private Task task;

	/**
	 * Gets the id of the finished task.
	 * 
	 * @return The int value of finished task's id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of the finished task.
	 * 
	 * @param id
	 *            Sets the generated id of the finished task.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * The date and time and when a task was finished.
	 * 
	 * @return The localDateTime when the task was finished.
	 */
	public LocalDateTime getFinishedDate() {
		return finishedDate;
	}

	/**
	 * Sets the date and time when a task was finished.
	 * 
	 * @param finishedDate
	 *            The date and time when a task was finished as LocalDateTime.
	 */
	public void setFinishedDate(LocalDateTime finishedDate) {
		this.finishedDate = finishedDate;
	}

	/**
	 * Gets the task which was finished.
	 * 
	 * @return The task object of the finished task.
	 */
	public Task getTask() {
		return task;
	}

	/**
	 * Sets the task that was finished.
	 * 
	 * @param task
	 *            The task object which was finished.
	 */
	public void setTask(Task task) {
		this.task = task;
	}

}
