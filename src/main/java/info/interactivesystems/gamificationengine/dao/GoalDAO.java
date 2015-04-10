package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.Organisation;
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
	 * Store a new goal in the database.
	 * 
	 * @param goal
	 *            the goal which should be stored in the database
	 * @return the id of the {@link Goal}
	 */
	public int insertGoal(Goal goal) {
		em.persist(goal);
		em.flush();
		return goal.getId();
	}

	/**
	 * Store a new finished goal in the database.
	 * 
	 * @param goal
	 *            the finished goal which should be stored in the database
	 * @return the id of the {@link FinishedGoal}
	 */
	public int insertFinishedGoal(FinishedGoal goal) {
		em.persist(goal);
		em.flush();
		return goal.getId();
	}

	/**
	 * Get an goal from the database.
	 * 
	 * @param goalId
	 *            the id of the goal
	 * @return {@link Goal} or null
	 */
	public Goal getGoal(int goalId) {
		return em.find(Goal.class, goalId);
	}

	/**
	 * Find a Goal by id and check if it belongs to the organisation.
	 * 
	 * @param goalId
	 *            the id of the goal
	 * @param organisation
	 *            the organisation of the goal
	 * @return {@link Goal} or null
	 */
	public Goal getGoalByIdAndOrganisation(int goalId, Organisation organisation) {
		Goal goal = em.find(Goal.class, goalId);
		if (goal != null) {
			if (goal.belongsTo(organisation)) {
				return goal;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * delete a goal by id and check if it belongs to the organisation.
	 * 
	 * @param goalId
	 *            the id of the goal
	 * @param organisation
	 *            the organisation of the goal
	 * @return {@link Goal}
	 */
	public Goal deleteGoalByIdAndOrganisation(int id, Organisation organisation) {
		Goal goal = getGoalByIdAndOrganisation(id, organisation);
		em.remove(goal);
		return goal;
	}

	/**
	 * Get all goals which belong to a rule.
	 * 
	 * @param rule
	 *            the rule
	 * @return {@link List<Goal>}
	 */
	public List<Goal> getGoalsByRule(GoalRule rule) {

		Query query = em.createQuery("select g from Goal g where g.rule.id =:ruleId");
		query.setParameter("ruleId", rule.getId());

		return query.getResultList();
	}

	/**
	 * Get all goals which belong to an apiKey
	 * 
	 * @param apiKey
	 *            the api key
	 * @return {@link List<Goal>}
	 */
	public List<Goal> getGoals(String apiKey) {
		Query query = em.createQuery("select g from Goal g join g.belongsTo a where a.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}
}
