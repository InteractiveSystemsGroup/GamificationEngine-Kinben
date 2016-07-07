package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.Notification;
import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
import info.interactivesystems.gamificationengine.api.validation.ValidListOfDigitsOrNull;
import info.interactivesystems.gamificationengine.api.validation.ValidPositiveDigit;
import info.interactivesystems.gamificationengine.dao.GoalDAO;
import info.interactivesystems.gamificationengine.dao.MarketPlaceDAO;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.dao.PlayerDAO;
import info.interactivesystems.gamificationengine.dao.PlayerGroupDAO;
import info.interactivesystems.gamificationengine.dao.RoleDAO;
import info.interactivesystems.gamificationengine.dao.RuleDAO;
import info.interactivesystems.gamificationengine.dao.TaskDAO;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.Role;
import info.interactivesystems.gamificationengine.entities.goal.GoalRule;
import info.interactivesystems.gamificationengine.entities.goal.TaskRule;
import info.interactivesystems.gamificationengine.entities.marketPlace.MarketPlace;
import info.interactivesystems.gamificationengine.entities.task.Task;
import info.interactivesystems.gamificationengine.utils.LocalDateTimeUtil;
import info.interactivesystems.gamificationengine.utils.OfferMarketPlace;
import info.interactivesystems.gamificationengine.utils.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * A Task is the basic module and represents for example a specific activity. For a creation 
 * of a task, the roles are needed which indicate who is allowed to fulfil this task. To 
 * complete the task only one of these roles is needed. One or more tasks can be assigned to 
 * a goal, so depending on the rule of the goal some additional tasks may also have to be 
 * completed to fulfill the goal so the player can earn the associated rewards. If the task 
 * is tradeable it can be offered in the marketplace, so that another player can do it and 
 * gets the reward of it.
 * 
 * When a player has completed a task, it will be added to the player’s list of finished 
 * tasks. At the same time the date and time is also stored when this request was sent and the
 * task was officially be done. If the task is the last one to fulfill a goal, the goal is also
 * added to the player’s list of finished goals and the player will obtain all its associated 
 * rewards.
 * It is possible to query all tasks which are associated with a particular organisation or with 
 * the help of the associated id one specific task. When a task was created it is possible to 
 * change the task’s name, description and roles of players who are allowed to fulfil this task. 
 * Furthermore a task can be set tradeable or not at a later point of time. 
 */
@Path("/task")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class TaskApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskApi.class);

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
	@Inject
	MarketPlaceDAO marketPlDao;

	/**
	 * Creates a new task and so the method generates the task-id. The organisation's API key 
	 * is mandatory otherwise a warning with the hint for a non valid API key is returned.
	 * By the creation values for its name, a short description what have to done and the roles
	 * who are allowed to complete the task.  
	 * It is checked, if the id of the roles are positive numbers otherwise a message for the 
	 * invalid number is returned.
	 * 
	 * @param name
	 *            The name of the task. This parameter is required.
	 * @param description
	 *            Optional a short description can be set. This can be for example explain what 
	 *            a player has to do to complete the task.
	 * @param tradeable
	 *            This field specifies whether the task is tradeable or not. The default value is
	 *            set to not tradeable (false).
	 * @param roleIds
	 *            Optionally a list of role ids separated by commas who are allowed to fulfil the 
	 *            task.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this task belongs to.
	 * @return Response of Task in JSON.
	 */
	@POST
	@Path("/")
	@TypeHint(Task.class)
	public Response createNewTask(@QueryParam("name") @NotNull String name, @QueryParam("description") String description,
			@QueryParam("tradeable") @DefaultValue("false") String tradeable,
			@QueryParam("roleIds")@DefaultValue("null")@ValidListOfDigitsOrNull(message = "The role ids must be null or a valid list of numbers") String roleIds, 
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		LOGGER.debug("createNewTask called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		List<Role> roles = new ArrayList<>();
		if(!roleIds.equals("null")){
			String[] roleIdList = roleIds.split(",");

			for (String roleIdString : roleIdList) {
				Role role = roleDao.getRole(ValidateUtils.requireGreaterThanZero(roleIdString), apiKey);
				if (role != null) {
					roles.add(role);
				}
			}
		}
		
		Task task = new Task();
		task.setTaskName(name);
		task.setDescription(description);
		task.setBelongsTo(organisation);
		task.setAllowedFor(roles);
		task.setTradeable(Boolean.parseBoolean(tradeable));
		taskDao.insertTask(task);

//		Notification note = new Notification().of("New Task created.");
//		note.addError("New Task created.");
		return ResponseSurrogate.created(task, new Notification().of("New Task created."));
	}

	/**
	 * Returns a list of all tasks associated with the passed API key. If the key is not 
	 * valid an analogous message is returned.
	 * 
	 * @param apiKey
	 *          The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this task belongs to.
	 * @return Response of Task in JSON.
	 */
	@GET
	@Path("/*")
	@TypeHint(Task[].class)
	public Response getTasks(@QueryParam("apiKey") @ValidApiKey String apiKey) {

		List<Task> tasks = taskDao.getTasks(apiKey);

		for (Task t : tasks) {
			LOGGER.debug("Task: " + t.getTaskName());
			for (Role r : t.getAllowedFor()) {
				LOGGER.debug("Role: " + r.getId());
			}
		}
		return ResponseSurrogate.of(tasks);
	}
	
	/**
	 * Returns a list of all tasks which are tradeable and associated with the passed API key. If the key is not 
	 * valid an analogous message is returned.
	 * 
	 * @param apiKey
	 * 		 	The valid query parameter API key affiliated to one specific organisation, 
	 *          to which these tasks belong to.
	 * @return Response of all tradeable tasks in JSON.
	 */
	@GET
	@Path("/tradeable/*")
	@TypeHint(Task[].class)
	public Response getTradeableTasks(@QueryParam("apiKey") @ValidApiKey String apiKey) {

		List<Task> tasks = taskDao.getTasks(apiKey).stream().filter(t -> t.isTradeable()==true).collect(Collectors.toList());
		return ResponseSurrogate.of(tasks);
	}
	

	/**
	 * Returns the task associated with the passed id and API key. If the API key is not 
	 * valid an analogous message is returned. It is also checked, if the player id is a 
	 * positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param id
	 *          Required path parameter as integer which uniquely identify the Task.
	 * @param apiKey
	 *          The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this task belongs to.
	 * @return Response of Task in JSON.
	 */
	@GET
	@Path("/{id}")
	@TypeHint(Task.class)
	public Response getTask(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The task id must be a valid number") String id,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		int taskId = ValidateUtils.requireGreaterThanZero(id);
		Task task = taskDao.getTask(taskId, apiKey);
		ValidateUtils.requireNotNull(taskId, task);

		return ResponseSurrogate.of(task);
	}


	/**
	 * This method completes a task with the assigned id and associated API key. The player-id
	 * represents the player who has completed the task. The task is added to the list of 
	 * finished tasks of this player. Thereby the task becomes a finished task object and the 
	 * time and date is also stored when the task was officially be done.
	 * 
	 * @param id
	 *          Required integer which uniquely identify the Task.
	 * @param playerId
	 *           The if ot the player who has completed the task. This parameter is required.
	 * @param finishedDate
	 *           Optionally the local tate time can be passed when the task was finished. If the 
	 *           value is null, the finshedDate is set to the time and date when the query was 
	 *           sent. 
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *           to which this task belongs to.
	 * @return Response of Task in JSON.
	 */
	@POST
	@Path("/{id}/complete/{playerId}")
	@TypeHint(Task.class)
	public Response completeTask(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The task id must be a valid number") String id,
			@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("finishedDate") String finishedDate, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		// find player by id and organisation
		LOGGER.debug("Get Player");
		int pId = ValidateUtils.requireGreaterThanZero(playerId);
		Player player = playerDao.getPlayer(pId, apiKey);
		ValidateUtils.requireNotNull(pId, player);
		
		// find task by id and organisation
		int taskId = ValidateUtils.requireGreaterThanZero(id);
		Task task = taskDao.getTask(taskId, apiKey);
		ValidateUtils.requireNotNull(taskId, task);
		
		LOGGER.debug("TaskName: " + task.getTaskName());

		if (finishedDate == null || "".equals(finishedDate)) {
			LOGGER.debug("No Date passed.");
			task.completeTask(player, ruleDao, goalDao, groupDao, null, apiKey);
		} else {
			LOGGER.debug("Date passed: " + finishedDate);
			LocalDateTime dateTime = LocalDateTimeUtil.formatDateAndTime(finishedDate);
			task.completeTask(player, ruleDao, goalDao, groupDao, dateTime, apiKey);
		}
		
		List<OfferMarketPlace> taskOffers = MarketPlace.getAllOfferMarketPlaces(marketPlDao, task, apiKey);
		if(!taskOffers.isEmpty()){
			MarketPlace.completeAssociatedOffers(taskOffers, player, marketPlDao, playerDao, apiKey);
		}
		
		return ResponseSurrogate.created(task);
	}

	/**
	 * With this method the fields of a Task can be changed. For this the id of the 
	 * task, the API key of the specific organisation, the name of the field and the new 
	 * value are needed.
	 * To modify the name or the description the new String has to be passed with the 
	 * attribute field. A new list of roles can be passed when their ids are separated by 
	 * commas. Also the task can be set tradeable or not by passing the value true or false. 
	 * If the API key is not valid an analogous message is returned. It is also checked, if 
	 * the ids are a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param id
	 *           The id of the task that should be changed. This parameter is required.
	 * @param attribute
	 *            The name of the attribute which should be modified. This parameter is required. 
	 *            The following names of attributes can be used to change the associated field:
	 *            "taskName", "description", "tradeable" and "roles".
	 * @param value
	 *            The new value of the attribute. This parameter is required.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this task belongs to.
	 * @return Response of Task in JSON.
	 */
	@PUT
	@Path("/{id}/attributes")
	@TypeHint(Task.class)
	public Response changeTaskAttributes(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("attribute") @NotNull String attribute,
			@QueryParam("value") @NotNull String value, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		LOGGER.debug("change Attribute of Task");

		int taskId = ValidateUtils.requireGreaterThanZero(id);
		Task task = taskDao.getTask(taskId, apiKey);
		ValidateUtils.requireNotNull(taskId, task);
		
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

	/**
	 * This method converts the string of role ids which are transfered to a list of roles.
	 * These roles are then set as the new list of roles a player can have to fulfil this task. 
	 * 
	 * @param value
	 * 			The new values of roles as String separated by commas. This parameter is 
	 * 		   	required.
	 * @param task
	 * 			The task whose field of roles will be modified. This parameter should be not 
	 * 		  	null because this method is called by a method which checks the given id if a 
	 * 			group exists. 
	 * @param apiKey
	 * 			The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this task belongs to.
	 */
	private void changeRoles(@NotNull String value, Task task, String apiKey) {
		String commaSeparatedList = StringUtils.validateAsListOfDigits(value);
		List<Integer> ids = StringUtils.stringArrayToIntegerList(commaSeparatedList);
		List<Role> roles = roleDao.getRoles(ids, apiKey);
		task.setAllowedFor(roles);
	}

	
	/**
	 * Removes the task with the assigned id and associated API key from data base. It is checked, 
	 * if the passed id is a positive number otherwise a message for an invalid number is returned. 
	 * If the API key is not valid an analogous message is returned.
	 * 
	 * @param id
	 *          Required integer which uniquely identify the {@link Task}.
	 * @param apiKey
	 *          The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this task belongs to.
	 * @return Response of Task in JSON.
	 */
	@DELETE
	@Path("/{id}")
	@TypeHint(Task.class)
	public Response deleteTask(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The task id must be a valid number") String id,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		int taskId = ValidateUtils.requireGreaterThanZero(id);
		Task task = taskDao.getTask(taskId, apiKey);
		ValidateUtils.requireNotNull(taskId, task);
		
 		List<TaskRule> rules = ruleDao.getRulesByTask(task, apiKey);
 		if(!rules.isEmpty()){
 			GoalRule.checkRulesForTask(rules);
 		}
		task = taskDao.deleteTask(taskId, apiKey);
		
		return ResponseSurrogate.deleted(task);
	}
}
