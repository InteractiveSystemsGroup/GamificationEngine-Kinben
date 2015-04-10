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

	public int insertTask(Task task) {
		em.persist(task);
		em.flush();
		return task.getId();
	}

	public Task getTask(int taskId) {
		return em.find(Task.class, taskId);
	}

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

	public Task deleteTaskByIdAndOrganisation(int id, Organisation organisation) {
		Task task = getTaskByIdAndOrganisation(id, organisation);
		em.remove(task);
		return task;
	}

	public List<Task> getTasks(String apiKey) {
		Query query = em.createQuery("select t from Task t join t.belongsTo a where a.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}

	public List<Task> getTasks(List<Integer> taskIds, String apiKey) {
		Query query = em.createQuery("select r from Task r where r.belongsTo.apiKey=:apiKey and r.id in (:taskIds)", Task.class);
		query.setParameter("apiKey", apiKey);
		query.setParameter("taskIds", taskIds);

		return query.getResultList();
	}
}
