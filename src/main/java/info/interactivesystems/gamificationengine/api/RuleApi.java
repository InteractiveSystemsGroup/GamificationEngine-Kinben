package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
import info.interactivesystems.gamificationengine.api.validation.ValidListOfDigits;
import info.interactivesystems.gamificationengine.api.validation.ValidPositiveDigit;
import info.interactivesystems.gamificationengine.dao.GoalDAO;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.dao.RuleDAO;
import info.interactivesystems.gamificationengine.dao.TaskDAO;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.goal.DoAllTasksRule;
import info.interactivesystems.gamificationengine.entities.goal.DoAnyTaskRule;
import info.interactivesystems.gamificationengine.entities.goal.GetPointsRule;
import info.interactivesystems.gamificationengine.entities.goal.Goal;
import info.interactivesystems.gamificationengine.entities.goal.GoalRule;
import info.interactivesystems.gamificationengine.entities.goal.TaskRule;
import info.interactivesystems.gamificationengine.entities.task.Task;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * With a Goalrule can be defined which tasks and if all or only one task have to be fulfilled to reach a goal.
 * When a goal rule is fulfilled the goal is added to the player’s list of finished goals. If the goal can also
 * be done by a group it is also added to its list of finished goals. There are two types of rules that can be 
 * defined: a TaskRule or a PointsRule.
 * All created rules which are created in the context of one specific organisation can be requested with the 
 * appendant API key. With the given id also a particular rule can be requested, also for example to change some
 * attributes like the name, the description or, if it is a PointsRule, the amount of points which has to be 
 * reached. 
 */
@Path("/rule")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class RuleApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleApi.class);

	@Inject
	OrganisationDAO organisationDao;
	@Inject
	RuleDAO ruleDao;
	@Inject
	TaskDAO taskDao;
	@Inject
	GoalDAO goalDao;

	/**
	 * Creates a new task rule. By the creation the type of rule (DoAllTasksRule or DoAnyTaskRule) has to be defined, the rule's name, 
	 * description and the ids which should be associated with this rule.
	 * If the API key is not valid an analogous message is returned. It is also checked, if the id is a positive
	 * number otherwise a message for an invalid number is returned.
	 * 
	 * @param type
	 *            The type of the task rule, this can be "DoAllTasksRule" or "DoAnyTaskRule". 
	 *            This field must not be null.
	 * @param name
	 *            The name of the task rule. This parameter is required.
	 * @param description
	 *            Optionally the description of the rule can be passed. This can help the player to understand 
	 *            which tasks she/he to fulfil.
	 * @param taskIds
	 *            The list of task ids that are have to be respective can be fulfilled to complete the goal. 
	 *            These ids are separated by commas.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this rule belongs to.
	 * @return Response of DoAllTasksRule or DoAnyTaskRule in JSON.
	 */
	@POST
	@Path("/task")
	@TypeHint(TaskRule.class)
	public Response createNewTaskRule(@QueryParam("type") @NotNull String type, @QueryParam("name") @NotNull String name,
			@QueryParam("description") String description, @QueryParam("tasks") @NotNull @ValidListOfDigits String taskIds,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		TaskRule.logTaskRuleDetails(type, apiKey, name, description, taskIds);

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		TaskRule rule;

		// Find all tasks by Id
		String[] taskIdList = taskIds.split(",");
		List<Task> tasks = new ArrayList<>();

		for (String taskIdString : taskIdList) {
			Task task = taskDao.getTask(ValidateUtils.requireGreaterThanZero(taskIdString), apiKey);
			if (task != null) {
				tasks.add(task);
			}
		}

		LOGGER.debug("Tasks: " + tasks);

		switch (type) {
		case "DoAllTasksRule":
			rule = new DoAllTasksRule();
			break;

		case "DoAnyTaskRule":
			rule = new DoAnyTaskRule();
			break;

		default:
			rule = new DoAllTasksRule();
		}

		rule.setName(name);
		rule.setDescription(description);
		rule.setBelongsTo(organisation);

		ruleDao.insertRule(rule);

		rule.setTasks(tasks);

		return ResponseSurrogate.created(rule);
	}


	/**
	 * Creates a new points rule. By the creation the amount of points which has to be reached to fulfil the 
	 * goal and also its name are needed. A description can also be made. 
	 * If a goal is associated with a points rule and is also repeatable the goal will be added once only to the
	 * player's or respectively group's list of already finished goals. The rewards of such a goal are also awarded
	 * only once. So a points rule can be fulfilled once only although the associated goal is repeatable.  
	 * If the API key is not valid an analogous message is returned. It is also checked, if the id is a positive
	 * number otherwise a message for an invalid number is returned.
	 * 
	 * @param name
	 *            The name of the task rule. This parameter is required.
	 * @param description
	 *            Optionally the description of the rule can be passed. This can help the player to understand 
	 *            which tasks she/he to fulfil.
	 * @param points
	 *            The amount of points which should be reached.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this rule belongs to.
	 * @return Response of GetPointsRule in JSON.
	 */
	@POST
	@Path("/point")
	@TypeHint(GetPointsRule.class)
	public Response createNewPointRule(@QueryParam("name") @NotNull String name, @QueryParam("description") String description,
			@QueryParam("points") @NotNull @ValidPositiveDigit String points, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		LOGGER.debug("createNewPointRule called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		GetPointsRule rule = new GetPointsRule();
		rule.setName(name);
		rule.setDescription(description);
		rule.setBelongsTo(organisation);
		rule.setPoints(ValidateUtils.requireGreaterThanZero(points));

		ruleDao.insertRule(rule);

		return ResponseSurrogate.created(rule);
	}

	/**
	 * This method collects all available rules associated with the given API key and so all goal rules which 
	 * belong to the associated organisation. If the API key is not valid an analogous message 
	 * is returned.
	 * 
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this rule belongs to.
	 * @return Response as List of GoalRules in JSON.
	 */
	@GET
	@Path("/*")
	@TypeHint(GoalRule[].class)
	public Response getRules(@QueryParam("apiKey") @ValidApiKey String apiKey) {
		List<GoalRule> tasks = ruleDao.getRules(apiKey);
		return ResponseSurrogate.of(tasks);
	}

	/**
	 * This method gets one specific goal rule which is identified by the given id and the API key.
	 * If the API key is not valid an analogous message is returned. It is also checked, if the 
	 * id is a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param id
	 *           Required integer as path parameter which uniquely identify the goal rule.
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this rule belongs to.
	 * @return Response of GoalRule in JSON.
	 */
	@GET
	@Path("/{id}")
	@TypeHint(GoalRule.class)
	public Response getRule(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		int ruleId = ValidateUtils.requireGreaterThanZero(id);
		GoalRule rule = ruleDao.getRule(ruleId, apiKey);
		ValidateUtils.requireNotNull(ruleId, rule);
		
		return ResponseSurrogate.of(rule);
	}

	/**
	 * Removes a specific goal rule from the data base which is identified by the given id and the 
	 * API key. But only if the goal rule is not associated to a goal. Then first the goal has to deleted.
	 * If the API key is not valid an analogous message is returned. It is also checked, if the id 
	 * is a positive number otherwise a message for an invalid number is returned. 
	 * 
	 * @param id
	 *           Required integer as path parameter which uniquely identify the goal rule.
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *           to which this rule belongs to.
	 * @return Response of GoalRule in JSON.
	 */
	@DELETE
	@Path("{id}")
	@TypeHint(GoalRule.class)
	public Response deleteRule(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		int ruleId = ValidateUtils.requireGreaterThanZero(id);
		GoalRule rule = ruleDao.getRule(ruleId, apiKey);
		ValidateUtils.requireNotNull(ruleId, rule);

		List<Goal> goals = goalDao.getGoalsByRule(rule, apiKey);
		if(!goals.isEmpty()){
 			Goal.checkGoalsForDeletion(goals, "goal rule" , "goal");
 		}
		
		rule = ruleDao.deleteRule(ruleId, apiKey);
		
		return ResponseSurrogate.deleted(rule);
	}

	/**
	 * With this method the fields of one specific goal rule can be changed. For this the 
	 * goal rule id, the API key of the specific organisation, the name of the field and 
	 * the new field's value are needed. 
	 * To modify the name or description of the goal rule the new string has to be passed 
	 * with the attribute field. If the 
	 * If the API key is not valid an analogous message is returned. It is also checked, 
	 * if the id is a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param id
	 *          The id of the goal rule that should be changed. This parameter is required.
	 * @param attribute
	 *           The name of the attribute which should be modified. This parameter is required.
	 *           The following names of attributes can be used to change the associated field:
	 *           "description" and "name". The tasks that have to be completed or the amount of 
	 *           points that have to be reached, can't be changed later. 
	 * @param value
	 *           The new value of the attribute. This parameter is required.
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this goal rule belongs to.
	 * @return Response of GoalRule in JSON.
	 */
	@PUT
	@Path("/{id}/attributes")
	@TypeHint(GoalRule.class)
	public Response changeRuleAttributes(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("attribute") @NotNull String attribute,
			@QueryParam("value") @NotNull String value, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		LOGGER.debug("change attribute of rule");

		int ruleId = ValidateUtils.requireGreaterThanZero(id);
		GoalRule rule = ruleDao.getRule(ruleId, apiKey);
		ValidateUtils.requireNotNull(ruleId, rule);

		if ("null".equals(value)) {
			value = null;
		}

		switch (attribute) {
		case "description":
			rule.setDescription(value);
			break;

		case "name":
			rule.setName(value);
			break;

		default:
			break;
		}

		ruleDao.insertRule(rule);

		return ResponseSurrogate.updated(rule);
	}

}
