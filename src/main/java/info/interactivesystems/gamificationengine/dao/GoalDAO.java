package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.goal.FinishedGoal;
import info.interactivesystems.gamificationengine.entities.goal.Goal;
import info.interactivesystems.gamificationengine.entities.goal.GoalRule;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Named
@Stateless
public class GoalDAO {
	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	/**
	 * Stores a new goal in the data base.
	 * 
	 * @param goal
	 *            The goal which should be stored in the data base.
	 * @return The id of the {@link Goal}.
	 */
	public int insertGoal(Goal goal) {
		em.persist(goal);
		em.flush();
		return goal.getId();
	}

	/**
	 * Stores a new finished goal in the data base.
	 * 
	 * @param goal
	 *            The finished goal which should be stored in the data base.
	 * @return The id of the {@link FinishedGoal}.
	 */
	public int insertFinishedGoal(FinishedGoal goal) {
		em.persist(goal);
		em.flush();
		return goal.getId();
	}

	/**
	 * Gets an goal from the data base.
	 * 
	 * @param id
	 *            The id of the goal.
	 * @param apiKey
	 *           The API key of the organisation to which the goal belongs to. 
	 * @return The {@link Goal} object or null if it wasn't found.
	 */
	public Goal getGoal(int id, String apiKey) {
		Query query = em.createQuery("select g from Goal g where g.belongsTo.apiKey=:apiKey and g.id=:id", Goal.class);
		List list = QueryUtils.configureQuery(query, id, apiKey);
		if (list.isEmpty()) {
			return null;
		}
		return (Goal) list.get(0);
	}


	/**
	 * Gets all goals which are associated to a specific rule.
	 * 
	 * @param rule
	 *           The rule to which the goals are associated. 
	 * @param apiKey
	 *           The API key of the organisation to which the goal belongs to. 
	 * @return A {@link List} of all {@link Goal}s which are associated to the specific rule.
	 */
	public List<Goal> getGoalsByRule(GoalRule rule, String apiKey) {

		Query query = em.createQuery("select g from Goal g where g.rule.id =:ruleId and g.belongsTo.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);
		query.setParameter("ruleId", rule.getId());

		return query.getResultList();
	}
	


	/**
	 * Gets all goals which belong to the specific passed API key.
	 * 
	 * @param apiKey
	 *            The API key affiliated to one specific organisation, to which 
	 *            the goals belongs to.
	 * @return A {@link List} of all {@link Goal}s which are associated to the specific API key.
	 */
	public List<Goal> getGoals(String apiKey) {
		Query query = em.createQuery("select g from Goal g join g.belongsTo a where a.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}
	
	/**
	 * Deletes a goal by its id and checks if it belongs to the passed organisation.
	 * 
	 * @param id
	 *            The id of the requested goal.
	 * @param apiKey
	 *           The API key of the organisation to which the goal belongs to. 
	 * @return The {@link Goal} that should be deleted.
	 */
	public Goal deleteGoal(Goal goal, String apiKey) {
		
		if(goal!= null){
			em.remove(goal);
		}
		return goal;
	}
}
