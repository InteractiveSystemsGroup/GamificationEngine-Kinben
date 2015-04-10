package info.interactivesystems.gamificationengine.entities.task;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * After a Task is done it gets a finished Date.
 *
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
	 * Get the id of the finished task
	 * 
	 * @return int value of the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the id of the finished task
	 * 
	 * @param id
	 *            the id of the finished task
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * The date when a task was finished.
	 * 
	 * @return the localDateTime when the task was finished.
	 */
	public LocalDateTime getFinishedDate() {
		return finishedDate;
	}

	/**
	 * Set the date when a task was finished.
	 * 
	 * @param finishedDate
	 *            the date when a task was finished.
	 */
	public void setFinishedDate(LocalDateTime finishedDate) {
		this.finishedDate = finishedDate;
	}

	/**
	 * Get the task that was finished.
	 * 
	 * @return the task object of the finished task
	 */
	public Task getTask() {
		return task;
	}

	/**
	 * Set the task that was finished.
	 * 
	 * @param task
	 *            the task that was finished.
	 */
	public void setTask(Task task) {
		this.task = task;
	}

}
