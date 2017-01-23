package info.interactivesystems.gamificationengine.dao;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@Stateless
public class RuleDAO {
	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RuleDAO.class);

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
	 * @param id
	 * 			The id of the requested rule.
	 * @param apiKey
	 *           The API key of the organisation to which the rule belongs to. 
	 * @return The {@link GoalRule} which is associated with the passed id and API key.
	 */
	public GoalRule getRule(int id, String apiKey) {
		Query query = em.createQuery("select r from GoalRule r where r.belongsTo.apiKey=:apiKey and r.id=:id)", GoalRule.class);
		List list = QueryUtils.configureQuery(query, id, apiKey);
		if (list.isEmpty()) {
			return null;
		}
		return ((GoalRule) list.get(0));
	}
	
	/**
	 * Gets all rules which are associated with the passed API key.
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
	 * Gets all rules of the type TaskRule which contains the passed task and are associated 
	 * with the passed organisaion.
	 * 
	 * @param task
	 * 			It is checked if the task rules of an organisation contain the task.			
	 * @param apiKey
	 *           The API key of the organisation to which the rules belong to.
	 * @return A {@link List} of {@link TaskRule}s which contain the passed task and 
	 * 			are associated with the passed organisation.
	 */
	public List<TaskRule> getRulesByTask(Task task, String apiKey) {
		List<TaskRule> result = new ArrayList<>();
		Query query = em.createQuery("select r from GoalRule r where RULE_TYPE LIKE :ruleType AND r.belongsTo.apiKey=:apiKey");
		query.setParameter("ruleType", "TRULE%");
		query.setParameter("apiKey", apiKey);
		List<TaskRule> temp = query.getResultList();
		for (TaskRule rule : temp) {
			if (rule.getTasks().contains(task)) {
				result.add(rule);
			}
		}
		
		return result;
	}
	
	
	/**
	 * Gets all rules of the type PointsRule of an specific organisation.
	 * 
	 * @param apiKey
	 *           The API key of the organisation to which the point rules belong to. 
	 * @return A {@link List} of {@link GoalRule}s with all points rules.
	 */
	public List<GoalRule> getAllPointsRules(String apiKey) {
		Query query = em.createQuery("select r from GoalRule r where RULE_TYPE =:ruleType and r.belongsTo.apiKey=:apiKey");
		query.setParameter("ruleType", "PRULE");
		query.setParameter("apiKey", apiKey);
		
		return query.getResultList();
	}
	
	/**
	 * Removes a rule from the data base.
	 * 
	 * @param id
	 * 			The id of the rule which should be deleted.
	 * @param apiKey
	 *           The API key of the organisation to which the rule belongs to. 
	 * @return The {@link GoalRule} that is associated with the passed id and APi key.
	 */
	public GoalRule deleteRule(int id, String apiKey) {
		GoalRule rule = getRule(id, apiKey);
		
		if(rule!=null){
			em.remove(rule);
		}
		return rule;
	}
}
