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
	 * @param goalId
	 *            The id of the goal.
	 * @return The {@link Goal} object or null if it wasn't found.
	 */
	public Goal getGoal(int goalId) {
		return em.find(Goal.class, goalId);
	}

	/**
	 * This method finds a goal by its id and and returns it. The method also 
	 * checks if it belongs to the passed organisation.
	 * 
	 * @param goalId
	 *            The id of the goal.
	 * @param organisation
	 *            The organisation of the goal.
	 * @return The {@link Goal} or null if the goal dosen't belong to the passed
	 *        organisation.
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
	 * Deletes a goal by its id and checks if it belongs to the passed organisation.
	 * 
	 * @param id
	 *            The id of the requested goal.
	 * @param organisation
	 *            The organisation the goal belongs to.
	 * @return {@link Goal}
	 */
	public Goal deleteGoalByIdAndOrganisation(int id, Organisation organisation) {
		Goal goal = getGoalByIdAndOrganisation(id, organisation);
		em.remove(goal);
		return goal;
	}

	/**
	 * Gets all goals which are associated to a specific rule.
	 * 
	 * @param rule
	 *           The rule to which the goals are associated. 
	 * @return A {@link List} of all {@link Goal}s which are associated to the specific rule.
	 */
	public List<Goal> getGoalsByRule(GoalRule rule) {

		Query query = em.createQuery("select g from Goal g where g.rule.id =:ruleId");
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
}
