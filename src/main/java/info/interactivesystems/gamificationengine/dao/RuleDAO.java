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

	public int insertRule(GoalRule rule) {
		em.persist(rule);
		em.flush();
		return rule.getId();
	}

	public GoalRule getRule(int ruleId) {
		return em.find(GoalRule.class, ruleId);
	}

	public List<GoalRule> getRules(String apiKey) {
		Query query = em.createQuery("select g from GoalRule g join g.belongsTo a where a.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}

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

	public GoalRule deleteRuleByIdAndOrganisation(int id, Organisation organisation) {
		GoalRule rule = getRuleByIdAndOrganisation(id, organisation);
		em.remove(rule);
		return rule;
	}

	public List<TaskRule> getRulesByTask(Task task, Organisation organisation) {
		// return task.getRules();
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

	public List<GoalRule> getAllPointsRules() {
		Query query = em.createQuery("select r from GoalRule r where RULE_TYPE =:ruleType");
		query.setParameter("ruleType", "PRULE");
		return query.getResultList();
	}
}
