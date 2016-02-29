package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.goal.GoalRule;
import info.interactivesystems.gamificationengine.entities.goal.TaskRule;
import info.interactivesystems.gamificationengine.entities.task.Task;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Named
@Stateless
public class RuleDAO {
	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	/**
	 * Stores a new rule in the data base.
	 * 
	 * @param rule
	 * 			The rule which should be stored in the data base.
	 * @return The generated id of the rule. 
	 */
	public int insertRule(GoalRule rule) {
		em.persist(rule);
		em.flush();
		return rule.getId();
	}

	/**
	 * Gets the rule by its id.
	 * 
	 * @param ruleId
	 * 			The id of the requested rule.
	 * @return The {@link GoalRule} which is associated with the passed id.
	 */
	public GoalRule getRule(int ruleId) {
		return em.find(GoalRule.class, ruleId);
	}
	
	/**
	 * Gets all rewards which are associated with the passed API key.
	 * 
	 * @param apiKey
	 * 			The API key of the organisation to which the rules belong to. 
	 * @return A {@link List} of {@link GoalRule}s with all rules which are associated with 
	 * 			the passed API key.
	 */
	public List<GoalRule> getRules(String apiKey) {
		Query query = em.createQuery("select g from GoalRule g join g.belongsTo a where a.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}

	/**
	 * Gets a rule by its id and organisation.
	 * 
	 * @param ruleId
	 * 			The id of the requested rule.
	 * @param organisation
	 * 			The organisaiton the rule is associated with.
	 * @return The {@link GoalRule} which is associated with the passed id and organisation.
	 */
	public GoalRule getRuleByIdAndOrganisation(int ruleId, Organisation organisation) {
		GoalRule rule = em.find(GoalRule.class, ruleId);
		if (rule != null) {
			if (rule.belongsTo(organisation)) {
				return rule;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Removes a rule from the data base.
	 * 
	 * @param id
	 * 			The id of the rule which should be deleted.
	 * @param organisation
	 * 			The organisaiton the rule is associated with.
	 * @return The {@link GoalRule} that is associated with the passed id and organisation.
	 */
	public GoalRule deleteRuleByIdAndOrganisation(int id, Organisation organisation) {
		GoalRule rule = getRuleByIdAndOrganisation(id, organisation);
		
		if(rule!=null){
			em.remove(rule);
		}
		return rule;
	}

	/**
	 * Gets all rules of the type TaskRule which contains the passed task and are associated 
	 * with the passed organisaion.
	 * 
	 * @param task
	 * 			It is checked if the task rules of an organisation contain the task.			
	 * @param organisation
	 * 			The organisaiton the rule is associated with.
	 * @return A {@link List} of {@link TaskRule}s which contain the passed task and 
	 * 			are associated with the passed organisation.
	 */
	public List<TaskRule> getRulesByTask(Task task, Organisation organisation) {
		List<TaskRule> result = new ArrayList<>();
		Query query = em.createQuery("select r from GoalRule r where RULE_TYPE LIKE :ruleType AND r.belongsTo = :cB");
		query.setParameter("ruleType", "TRULE%");
		query.setParameter("cB", organisation);
		List<TaskRule> temp = query.getResultList();
		for (TaskRule rule : temp) {
			if (rule.getTasks().contains(task)) {
				result.add(rule);
			}
		}
		return result;
	}

	/**
	 * Gets all rules of the type PointsRule.
	 * 
	 * @return A {@link List} of {@link GoalRule}s with all points rules.
	 */
	public List<GoalRule> getAllPointsRules() {
		Query query = em.createQuery("select r from GoalRule r where RULE_TYPE =:ruleType");
		query.setParameter("ruleType", "PRULE");
		return query.getResultList();
	}
}
