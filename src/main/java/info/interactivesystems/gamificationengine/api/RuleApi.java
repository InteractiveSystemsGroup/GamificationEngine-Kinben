package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
import info.interactivesystems.gamificationengine.api.validation.ValidListOfDigits;
import info.interactivesystems.gamificationengine.api.validation.ValidPositiveDigit;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.dao.RuleDAO;
import info.interactivesystems.gamificationengine.dao.TaskDAO;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.goal.DoAllTasksRule;
import info.interactivesystems.gamificationengine.entities.goal.DoAnyTaskRule;
import info.interactivesystems.gamificationengine.entities.goal.GetPointsRule;
import info.interactivesystems.gamificationengine.entities.goal.GoalRule;
import info.interactivesystems.gamificationengine.entities.goal.TaskRule;
import info.interactivesystems.gamificationengine.entities.rule.ExpressionNode;
import info.interactivesystems.gamificationengine.entities.rule.IdCollector;
import info.interactivesystems.gamificationengine.entities.rule.Parser;
import info.interactivesystems.gamificationengine.entities.rule.SetTask;
import info.interactivesystems.gamificationengine.entities.task.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

/**
 * API for rules related services.
 */
@Path("/rule")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class RuleApi {

	private static final Logger log = LoggerFactory.getLogger(RuleApi.class);

	@Inject
	OrganisationDAO organisationDao;
	@Inject
	RuleDAO ruleDao;
	@Inject
	TaskDAO taskDao;

	/**
	 * 
	 * Creates a new rule with an expression term.
	 * 
	 * @param name
	 *            required name of the new goal
	 * @param expression
	 *            required expression term
	 * @param description
	 *            optional description of the goal
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link GoalRule} in JSON
	 */
	@POST
	@Path("/")
	public Response createNewRule(@QueryParam("name") @NotNull String name, @QueryParam("expression") @NotNull String expression,
			@QueryParam("description") String description, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		if (expression == null) {
			throw new ApiError(Response.Status.PRECONDITION_FAILED, "expression is not provided");
		}

		Parser parser = new Parser();
		ExpressionNode exp = parser.parse(expression);
		configureExpressionTree(exp, apiKey);

		GoalRule goalRule = new GoalRule();
		goalRule.setName(name);
		goalRule.setDescription(description);
		goalRule.setBelongsTo(organisationDao.getOrganisationByApiKey(apiKey));
		goalRule.setExpressionTree(exp);
		ruleDao.insertRule(goalRule);

		return ResponseSurrogate.created(goalRule);
	}

	private void configureExpressionTree(ExpressionNode exp, String apiKey) {

		List<Integer> ids = new ArrayList<>();
		exp.accept(new IdCollector(ids));
		List<Task> tasks = taskDao.getTasks(ids, apiKey);

		ids.removeAll(tasks.stream().map(Task::getId).collect(Collectors.toList()));
		if (!ids.isEmpty()) {
			throw new ApiError(Response.Status.FORBIDDEN, "Creation failed, task ids don't exist " + ids);
		}

		for (Task task : tasks) {
			exp.accept(new SetTask(task));
		}
	}

	/**
	 * Creates a new task rule.
	 * 
	 * @param type
	 *            required type of the task rule. "DoAllTasksRule" or
	 *            "DoAnyTasksRule"
	 * @param name
	 *            required name of the task rule
	 * @param description
	 *            optional description of the rule
	 * @param taskIds
	 *            required list of task ids that need to be completed to
	 *            complete the goal
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link DoAllTasksRule} or
	 *         {@link DoAnyTaskRule} in JSON
	 */
	@POST
	@Path("/task")
	public Response createNewTaskRule(@QueryParam("type") @NotNull String type, @QueryParam("name") @NotNull String name,
			@QueryParam("description") String description, @QueryParam("tasks") @NotNull @ValidListOfDigits String taskIds,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("createNewTaskRule called");
		log.debug("Type: " + type);
		log.debug("ApiKey: " + apiKey);
		log.debug("Name: " + name);
		log.debug("Description: " + description);
		log.debug("TaskIds: " + taskIds);

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		log.debug("Organisation: " + organisation);

		TaskRule rule;

		// Find all tasks by Id
		String[] taskIdList = taskIds.split(",");
		List<Task> tasks = new ArrayList<>();

		for (String taskIdString : taskIdList) {
			Task task = taskDao.getTaskByIdAndOrganisation(ValidateUtils.requireGreaterThenZero(taskIdString), organisation);
			if (task != null) {
				tasks.add(task);
			}
		}

		log.debug("Tasks: " + tasks);

		switch (type) {
		case "DoAllTasksRule":
			rule = new DoAllTasksRule();
			break;

		case "DoAnyTasksRule":
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
	 * Creates a new point rule.
	 * 
	 * @param name
	 *            required name of the rule
	 * @param description
	 *            optional description of the rule
	 * @param points
	 *            required amount of points
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link GetPointsRule} in JSON
	 */
	@POST
	@Path("/point")
	public Response createNewPointRule(@QueryParam("name") @NotNull String name, @QueryParam("description") String description,
			@QueryParam("points") @NotNull @ValidPositiveDigit String points, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("createNewPointRule called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		GetPointsRule rule = new GetPointsRule();
		rule.setName(name);
		rule.setDescription(description);
		rule.setBelongsTo(organisation);
		rule.setPoints(ValidateUtils.requireGreaterThenZero(points));

		ruleDao.insertRule(rule);

		return ResponseSurrogate.created(rule);
	}

	/**
	 * Gets a list of all available rules.
	 * 
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link List<GoalRule>} in JSON
	 */
	@GET
	@Path("/*")
	public Response getRules(@QueryParam("apiKey") @ValidApiKey String apiKey) {
		List<GoalRule> tasks = ruleDao.getRules(apiKey);
		return ResponseSurrogate.of(tasks);
	}

	/**
	 * Returns a specific rule
	 * 
	 * @param id
	 *            required id of the requested rule
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link GoalRule} in JSON
	 */
	@GET
	@Path("/{id}")
	public Response getRule(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		int ruleId = ValidateUtils.requireGreaterThenZero(id);
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		GoalRule rule = ruleDao.getRuleByIdAndOrganisation(ruleId, organisation);
		ValidateUtils.requireNotNull(ruleId, rule);
		return ResponseSurrogate.of(rule);
	}

	/**
	 * Deletes a specific rule.
	 * 
	 * @param id
	 *            required id of the rule
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link GoalRule} in JSON
	 */
	@DELETE
	@Path("{id}")
	public Response deleteRule(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		if (id == null) {
			throw new ApiError(Response.Status.FORBIDDEN, "no ruleId transferred");
		}

		int ruleId = ValidateUtils.requireGreaterThenZero(id);
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		GoalRule rule = ruleDao.deleteRuleByIdAndOrganisation(ruleId, organisation);

		ValidateUtils.requireNotNull(ruleId, rule);
		return ResponseSurrogate.deleted(rule);
	}

	/**
	 * Changes the attribute of a specific rule
	 * 
	 * @param id
	 *            required id of the rule
	 * @param attribute
	 *            required name of the attribute
	 * @param value
	 *            requred new value of the attribute
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link GoalRule} in JSON
	 */
	@PUT
	@Path("/{id}/attributes")
	public Response changeRuleAttributes(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("attribute") @NotNull String attribute,
			@QueryParam("value") @NotNull String value, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("change Attribute of Rule");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		int ruleId = ValidateUtils.requireGreaterThenZero(id);
		GoalRule rule = ruleDao.getRule(ruleId);

		if ("null".equals(value)) {
			value = null;
		}

		// not changeable: id -> generated & belongsTo;
		switch (attribute) {
		case "description":
			rule.setDescription(value);
			break;

		case "name":
			rule.setName(value);
			break;

		case "points":
			((GetPointsRule) rule).setPoints(ValidateUtils.requireGreaterThenZero(value));
			break;

		default:

			break;
		}

		ruleDao.insertRule(rule);

		return ResponseSurrogate.updated(rule);
	}

}
