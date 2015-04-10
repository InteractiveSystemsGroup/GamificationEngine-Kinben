package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
import info.interactivesystems.gamificationengine.api.validation.ValidListOfDigits;
import info.interactivesystems.gamificationengine.api.validation.ValidPositiveDigit;
import info.interactivesystems.gamificationengine.dao.GoalDAO;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.dao.PlayerDAO;
import info.interactivesystems.gamificationengine.dao.PlayerGroupDAO;
import info.interactivesystems.gamificationengine.dao.RoleDAO;
import info.interactivesystems.gamificationengine.dao.RuleDAO;
import info.interactivesystems.gamificationengine.dao.TaskDAO;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.Role;
import info.interactivesystems.gamificationengine.entities.task.Task;
import info.interactivesystems.gamificationengine.utils.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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
 * API for task related services.
 */
@Path("/task")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class TaskApi {

	private static final Logger log = LoggerFactory.getLogger(TaskApi.class);

	@Inject
	OrganisationDAO organisationDao;
	@Inject
	TaskDAO taskDao;
	@Inject
	PlayerDAO playerDao;
	@Inject
	PlayerGroupDAO groupDao;
	@Inject
	RuleDAO ruleDao;
	@Inject
	GoalDAO goalDao;
	@Inject
	RoleDAO roleDao;

	/**
	 * Creates a new task.
	 * 
	 * @param name
	 *            required task name
	 * @param description
	 *            optional short description
	 * @param tradeable
	 *            specifies whether the task is tradeable or not, default is not
	 *            tradeable
	 * @param roleIds
	 *            optional a list of role ids
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Task} in JSON
	 */
	@POST
	@Path("/")
	public Response createNewTask(@QueryParam("name") @NotNull String name, @QueryParam("description") String description,
			@QueryParam("tradeable") @DefaultValue("false") String tradeable,
			@QueryParam("roleIds") @NotNull @ValidListOfDigits(message = "The role ids must be a valid list of numbers") String roleIds,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("createNewTask called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		String[] roleIdList = roleIds.split(",");
		List<Role> roles = new ArrayList<>();

		for (String roleIdString : roleIdList) {
			Role role = roleDao.getRole(ValidateUtils.requireGreaterThenZero(roleIdString), apiKey);
			if (role != null) {
				roles.add(role);
			}
		}

		Task task = new Task();
		task.setTaskName(name);
		task.setDescription(description);
		task.setBelongsTo(organisation);
		task.setAllowedFor(roles);
		task.setTradeable(Boolean.parseBoolean(tradeable));
		taskDao.insertTask(task);

		return ResponseSurrogate.created(task);
	}

	/**
	 * Returns a list of all tasks associated with assigned api key.
	 * 
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Task} in JSON
	 */
	@GET
	@Path("/*")
	public Response getTasks(@QueryParam("apiKey") @ValidApiKey String apiKey) {

		List<Task> tasks = taskDao.getTasks(apiKey);

		for (Task t : tasks) {
			System.out.println("Task: " + t.getTaskName());
			for (Role r : t.getAllowedFor()) {
				System.out.println("Role: " + r.getId());
			}
		}
		return ResponseSurrogate.of(tasks);
	}

	/**
	 * Returns a task for assigned id.
	 * 
	 * @param id
	 *            required task id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Task} in JSON
	 */
	@GET
	@Path("/{id}")
	public Response getTask(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The task id must be a valid number") String id,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		int taskId = ValidateUtils.requireGreaterThenZero(id);
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Task task = taskDao.getTaskByIdAndOrganisation(taskId, organisation);

		ValidateUtils.requireNotNull(taskId, task);
		return ResponseSurrogate.of(task);
	}

	/**
	 * Removes a task from data base.
	 * 
	 * @param id
	 *            required task id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Task} in JSON
	 */
	@DELETE
	@Path("/{id}")
	public Response deleteTask(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The task id must be a valid number") String id,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		int taskId = ValidateUtils.requireGreaterThenZero(id);
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Task task = taskDao.deleteTaskByIdAndOrganisation(taskId, organisation);

		ValidateUtils.requireNotNull(taskId, task);
		return ResponseSurrogate.deleted(task);
	}

	/**
	 * Completes a task with an assigned id for a associated player.
	 * 
	 * @param id
	 *            required task id
	 * @param playerId
	 *            required player id
	 * @param finishedDate
	 *            optional date time when the task were finished
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Task} in JSON
	 */
	@POST
	@Path("/{id}/complete/{playerId}")
	public Response completeTask(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The task id must be a valid number") String id,
			@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("finishedDate") String finishedDate, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("completeTask called");
		log.debug("TaskId: " + id);

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		// find player by id and organisation
		log.debug("Get Player");
		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(playerId), apiKey);

		// find task by id and organisation
		int taskId = ValidateUtils.requireGreaterThenZero(id);
		Task task = taskDao.getTaskByIdAndOrganisation(taskId, organisation);
		ValidateUtils.requireNotNull(taskId, task);
		log.debug("TaskName: " + task.getTaskName());

		if (finishedDate == null || "".equals(finishedDate)) {
			log.debug("Kein Datum übergeben");
			task.completeTask(organisation, player, ruleDao, goalDao, groupDao, null);
		} else {
			log.debug("Datum übergeben: " + finishedDate);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			LocalDateTime dateTime = LocalDateTime.parse(finishedDate, formatter);
			task.completeTask(organisation, player, ruleDao, goalDao, groupDao, dateTime);
		}

		return ResponseSurrogate.created(task);
	}

	/**
	 * Changes task attribute value for assigned id.
	 * 
	 * @param id
	 *            required task id
	 * @param attribute
	 *            required attribute key
	 * @param value
	 *            required content corresponding to the attribute
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Task} in JSON
	 */
	@PUT
	@Path("/{id}/attributes")
	public Response changeTaskAttributes(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("attribute") @NotNull String attribute,
			@QueryParam("value") @NotNull String value, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("change Attribute of Task");

		Task task = taskDao.getTask(ValidateUtils.requireGreaterThenZero(id));

		if ("null".equals(value)) {
			value = null;
		}

		// not changeable: id -> generated & belongsTo;
		switch (attribute) {
		case "taskName":
			task.setTaskName(value);
			break;

		case "description":
			task.setDescription(value);
			break;

		case "tradeable":
			task.setTradeable(Boolean.parseBoolean(value));
			break;

		case "roles":
			changeRoles(value, task, apiKey);
			break;

		default:
			break;
		}

		taskDao.insertTask(task);

		return ResponseSurrogate.updated(task);
	}

	private void changeRoles(@NotNull String value, Task task, String apiKey) {
		String commaSeparatedList = StringUtils.validateAsListOfDigits(value);
		List<Integer> ids = StringUtils.stringArrayToIntegerList(commaSeparatedList);
		List<Role> roles = roleDao.getRoles(ids, apiKey);
		task.setAllowedFor(roles);
	}

}
