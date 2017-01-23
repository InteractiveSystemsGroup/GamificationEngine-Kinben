package info.interactivesystems.gamificationengine.dao;

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
	 * @param id
	 * 			The id of the requested task.
	 * @param apiKey
	 *           The API key of the organisation to which the task belongs to. 
	 * @return The {@link Task} which is associated with the passed id and API key. 
	 */
	public Task getTask(int id, String apiKey) {
		Query query = em.createQuery("select t from Task t where t.belongsTo.apiKey=:apiKey and t.id=:id", Task.class);
		List list = QueryUtils.configureQuery(query, id, apiKey);
		if (list.isEmpty()) {
			return null;
		}
		return ((Task) list.get(0));
	}


	/**
	 * Gets all tasks which are associated with the passed API key.
	 * 
	 * @param apiKey
	 * 			The API key of the organisation to which the tasks belong to. 
	 * @return A {@link List} of {@link Task}s with all tasks which are associated with the passed 
	 * 			API key.
	 */
	public List<Task> getTasks(String apiKey) {
		Query query = em.createQuery("select t from Task t join t.belongsTo a where a.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}
	
	/**
	 * Get all tasks whose ids are passed. 
	 * 
	 * @param ids
	 * 			The ids of the tasks that should be returned.  
	 * @param apiKey
	 * 			The API key of the organisation to which the tasks belong to. 
	 * @return A List of Tasks with all tasks that are associated with the passed ids and API key.
	 */
	public List<Task> getTasksWithId(List<Integer> ids, String apiKey) {
		Query query = em.createQuery("select t from Task t where t.belongsTo.apiKey=:apiKey and t.id in (:ids)", Task.class);
		query.setParameter("apiKey", apiKey);
		query.setParameter("ids", ids);
		return query.getResultList();
	}
	
	/**
	 * Removes a task from the data base.
	 * 
	 * @param id
	 * 		 The id of the task which should be deleted.	
	 * @param apiKey
	 *           The API key of the organisation to which the task belongs to. 
	 * @return The {@link Task} that is associated with the passed id and API key.
	 */
	public Task deleteTask(int id, String apiKey) {
		Task task = getTask(id, apiKey);
		
		if(task != null){
			em.remove(task);
		}
		return task;
	}

	/**
	 * Returns all tasks that haven't been done for at least one time, yet.
	 * 
	 * @param apiKey
	 * 			The API key of the organisation to which these tasks belong to.
	 * @return A List of Tasks which are not done and associated with the passed API key.
	 */
	public List<Task> getTasksToDo(String apiKey) {
		Query query = em.createQuery("select t from Task t where t.belongsTo.apiKey=:apiKey and not exists (select fT from FinishedTask fT where t.id=fT.task.id)", Task.class);
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}
	
	/**
	 * Returns all tasks that are tradeable and haven't been done for at least one time, yet.
	 * 
	 * @param apiKey
	 * 			The API key of the organisation to which these tasks belong to.
	 * @return A List of Tasks which are tradeable, not done and associated with the passed API key.
	 */
	public List<Task> getTradeableTasksToDo(String apiKey) {
		Query query = em.createQuery("select t from Task t where t.belongsTo.apiKey=:apiKey and t.tradeable=true and not exists (select fT from FinishedTask fT where t.id=fT.task.id)", Task.class);
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}
}
