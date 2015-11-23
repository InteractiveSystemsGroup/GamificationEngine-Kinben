package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.task.Task;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Named
@Stateless
public class TaskDAO {
	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	/**
	 * Stores a new task in the data base.
	 * 
	 * @param task
	 * 			The task which should be stored in the data base.
	 * @return The generated id of the task. 
	 */
	public int insertTask(Task task) {
		em.persist(task);
		em.flush();
		return task.getId();
	}

	/**
	 * Gets the task by its id.
	 * 
	 * @param taskId
	 * 			The id of the requested task.
	 * @return The {@link Task} which is associated with the passed id. 
	 */
	public Task getTask(int taskId) {
		return em.find(Task.class, taskId);
	}

	/**
	 * Gets a task by its id and organisation.
	 * 
	 * @param taskId
	 * 			The id of the requested task.
	 * @param organisation
	 * 			The organisaiton the task is associated with.
	 * @return The {@link Task} which is associated with the passed id and organisation.
	 */
	public Task getTaskByIdAndOrganisation(int taskId, Organisation organisation) {
		Task task = em.find(Task.class, taskId);
		if (task != null)
			if (task.belongsTo(organisation)) {
				return task;
			} else {
				return null;
			}
		else {
			return null;
		}
	}

	/**
	 * Removes a task from the data base.
	 * 
	 * @param id
	 * 		 The id of the task which should be deleted.	
	 * @param organisation
	 * 		  The organisaiton the task is associated with.	
	 * @return The {@link Task} that is associated with the passed id and organisation.
	 */
	public Task deleteTaskByIdAndOrganisation(int id, Organisation organisation) {
		Task task = getTaskByIdAndOrganisation(id, organisation);
		em.remove(task);
		return task;
	}

	/**
	 * Gets all tasks which are associated with the passed API key.
	 * 
	 * @param apiKey
	 * 			The API key of the organisation to which the tasks belong to. 
	 * @return A {@link List<Task>} with all tasks which are associated with the passed 
	 * 			API key.
	 */
	public List<Task> getTasks(String apiKey) {
		Query query = em.createQuery("select t from Task t join t.belongsTo a where a.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}

	/**
	 * Gets all tasks with the passed ids which match the also passed API key.
	 * 
	 * @param taskIds
	 * 			A list of task ids. 
	 * @param apiKey
	 * 			The API key of the organisation to which the tasks belong to. 
	 * @return A {@link List<Task>} with all tasks which are associated with the passed 
	 * 			API key.
	 */
	public List<Task> getTasks(List<Integer> taskIds, String apiKey) {
		Query query = em.createQuery("select r from Task r where r.belongsTo.apiKey=:apiKey and r.id in (:taskIds)", Task.class);
		query.setParameter("apiKey", apiKey);
		query.setParameter("taskIds", taskIds);

		return query.getResultList();
	}
}
