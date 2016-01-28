package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
import info.interactivesystems.gamificationengine.api.validation.ValidListOfDigits;
import info.interactivesystems.gamificationengine.api.validation.ValidPositiveDigit;
import info.interactivesystems.gamificationengine.dao.GoalDAO;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.dao.RewardDAO;
import info.interactivesystems.gamificationengine.dao.RoleDAO;
import info.interactivesystems.gamificationengine.dao.RuleDAO;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Role;
import info.interactivesystems.gamificationengine.entities.goal.Goal;
import info.interactivesystems.gamificationengine.entities.goal.GoalRule;
import info.interactivesystems.gamificationengine.entities.rewards.Reward;
import info.interactivesystems.gamificationengine.utils.StringUtils;

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

import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * A Goal comprises one or more tasks and has to be completed if the player wants to earn the connected awards.
 * To create a goal some already created components are needed. So the condition when a goal is completed is 
 * defined in the goal rule and the connected tasks. Who can complete a goal is defined by the role of a player
 * and whether it can be done by a group. It is also possible to define whether a goal is repeatable so that the
 * player can complete the tasks and obtains its coins and points as rewards again. All goals that are 
 * associated with the organisation can be requested or like the elements before only one specific goal, if the
 * correspondent id is used. The name, the associated rewards and also the rule for completion can be changed 
 * as well as the indication if the goal is repeatable or a goal that can be reached by a group. It is also 
 * possible to change the roles so different people can complete the goal. 
 */
@Path("/goal")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class GoalApi {

	private static final Logger log = LoggerFactory.getLogger(GoalApi.class);

	@Inject
	OrganisationDAO organisationDao;
	@Inject
	GoalDAO goalDao;
	@Inject
	RuleDAO ruleDao;
	@Inject
	RewardDAO rewardDao;
	@Inject
	RoleDAO roleDao;

	/**
	 * Creates a new goal and so the method generates the goal-id.
	 * The organisation's API key is mandatory otherwise a warning with the hint for a  non valid API key is 
	 * returned. 
	 * By the creation the name and the id of the associated rule are needed. It can also be defined if 
	 * the goal is repeatable or if it can also be completed by a group. 
	 * Optionally the goal can be passed the ids of roles which are allowed to complete the goal. So if a player has at 
	 * least one of these roles she/he can complete the goal and earn its rewards. It is checked, if the ids of the 
	 * players are positive numbers otherwise a message for the invalid number is returned.
	 * Optionally the goal can be passed the id of rewards which can be earned. These ids are also checked if 
	 * they are positive numbers.
	 * If the API key is not valid an analogous message is returned. 
	 * 
	 * Note:  If a goal is associated with a points rule and is also repeatable the goal will be added once only to the
	 * player's or respectively group's list of already finished goals. The rewards of such a goal are also awarded
	 * only once. So a points rule can be fulfilled once only although the associated goal is repeatable.  
	 * 
	 * @param name
	 *            The name of the goal. This parameter is required. 
	 * @param repeatable
	 *            Optionally a goal can be set as repeatable by "1" or "0", "true" or
	 *            "false". The default value is "true".
	 * @param ruleId
	 *            The rule which define when a goal is completed. This parameter is required. 
	 * @param rewardIds
	 *            All rewards that are awarded to the player who completes the goal. These ids are 
	 *            separated by commas.
	 * @param roleIds
	 *            Optionally a list of role-ids can be passed which are separated by commas. These ids indicate
	 *            who is allowed to fulfil the goal. This parameter is required.
	 * @param isGroupGoal
	 *            Optionally a goal can also be done by a group. Possible values are "1" or "0", "true" or 
	 *            "false". The default value is "false". 
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this goal belongs to.
	 * @return A {@link Response} of {@link Goal} in JSON.
	 */
	@POST
	@Path("/")
	@TypeHint(Goal.class)
	public Response createNewGoal(@QueryParam("name") @NotNull String name, @QueryParam("repeatable") @DefaultValue("true") String repeatable,
			@QueryParam("ruleId") @NotNull @ValidPositiveDigit String ruleId, @QueryParam("rewardIds") @ValidListOfDigits String rewardIds,
			@QueryParam("roleIds") @DefaultValue("null") @ValidListOfDigits String roleIds,
			@QueryParam("groupGoal") @DefaultValue("false") String isGroupGoal, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("createNewGoal apiKey");
		log.debug("apiKey: " + apiKey);
		log.debug("name: " + name);
		log.debug("repeatable: " + repeatable);
		log.debug("ruleId: " + ruleId);
		log.debug("rewardIds: " + rewardIds);
		log.debug("rewardIds: " + roleIds);

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Goal goal = new Goal();
		goal.setName(name);
		goal.setBelongsTo(organisation);

		// Convert String to boolean
		boolean isRepeatable = "true".equalsIgnoreCase(repeatable) || "t".equalsIgnoreCase(repeatable) || "yes".equalsIgnoreCase(repeatable)
				|| "y".equalsIgnoreCase(repeatable) || "sure".equalsIgnoreCase(repeatable) || "aye".equalsIgnoreCase(repeatable)
				|| "ja".equalsIgnoreCase(repeatable) || "1".equalsIgnoreCase(repeatable);
		goal.setRepeatable(isRepeatable);

		boolean isPlayerGroupGoal = "true".equalsIgnoreCase(isGroupGoal) || "t".equalsIgnoreCase(isGroupGoal) || "yes".equalsIgnoreCase(isGroupGoal)
				|| "y".equalsIgnoreCase(isGroupGoal) || "sure".equalsIgnoreCase(isGroupGoal) || "aye".equalsIgnoreCase(isGroupGoal)
				|| "ja".equalsIgnoreCase(isGroupGoal) || "1".equalsIgnoreCase(isGroupGoal);
		goal.setPlayerGroupGoal(isPlayerGroupGoal);

		// Get rule object
		GoalRule rule = ruleDao.getRuleByIdAndOrganisation(ValidateUtils.requireGreaterThenZero(ruleId), organisation);
		goal.setRule(rule);

		// Find all rewards by Id
		String[] rewardIdList = rewardIds.split(",");

		for (String rewardIdString : rewardIdList) {
			log.debug("RewardToAdd: " + rewardIdString);
			Reward reward = rewardDao.getRewardByIdAndOrganisation(ValidateUtils.requireGreaterThenZero(rewardIdString), organisation);
			if (reward != null) {
				log.debug("RewardAdded: " + reward.getId());
				goal.addReward(reward);
			}
		}

		// Find all roles by Id and Organisation
		String[] rolesList = roleIds.split(",");
		List<Role> roles = new ArrayList<>();

		for (String roleIdString : rolesList) {
			Role role = roleDao.getRole(ValidateUtils.requireGreaterThenZero(roleIdString), apiKey);
			if (role != null) {
				roles.add(role);
			}
		}
		goal.setCanCompletedBy(roles);

		// persist Goal
		goalDao.insertGoal(goal);

		return ResponseSurrogate.created(goal);
	}

	/**
	 * Returns all goals which are associated with the given API key and so are belonging to the organisation.
	 * The players of one organisaiton can try to complete one these goals. 
	 * If the API key is not valid an analogous message is returned.
	 * 
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this goal belongs to.
	 * @return A {@link javax.ws.rs.core.Response} as {@link List} of {@link Goal}s in JSON.
	 */
	@GET
	@Path("/*")
	@TypeHint(Goal[].class)
	public Response getGoals(@QueryParam("apiKey") @ValidApiKey String apiKey) {

		List<Goal> goals = goalDao.getGoals(apiKey);
		return ResponseSurrogate.of(goals);
	}

	/**
	 * Gets the {@link GoalRule} object which is associated with the goal. It is identified by the passed id and 
	 * the API key. If the API key is not valid an analogous message is returned. It is also checked, if the 
	 * id is a positive number otherwise a message for an invalid number is returned..
	 * 
	 * @param id
	 *            Required integer which uniquely identify the {@link Goal}.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this goal belongs to.
	 * @return {@link Response} of {@link Goal} in JSON.
	 */
	@GET
	@Path("/{id}")
	@TypeHint(Goal.class)
	public Response getGoal(@PathParam("id") @ValidPositiveDigit @NotNull String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		int goalId = ValidateUtils.requireGreaterThenZero(id);
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Goal goal = goalDao.getGoalByIdAndOrganisation(goalId, organisation);

		ValidateUtils.requireNotNull(goalId, goal);
		return ResponseSurrogate.of(goal);
	}

	/**
	 * With this method the fields of one specific goal can be changed. For this the goal id, the API key of 
	 * the specific organisation, the name of the field and the new field's value are needed. 
	 * To modify the name of the goal the new string has to be transfered with the attribute field. 
	 * A list with role-ids separated by commas can be passed to define new roles which a player has to be allowed
	 * to complete the goal. By passing an id of another rule a new goal rule is associated with the goal.  
	 * To modify if a goal is repeatable or can be completed as a group the values "1" or "0" or alternatively 
	 * "true" and "false" can be passed. 
	 * It is also checked, if all ids are a positive number otherwise a message for an invalid number is returned.
	 * If the API key is not valid an analogous message is returned.
	 * 
	 * @param goalId
	 *            Required id of the goal which should be modified.
	 * @param attribute
	 *            The attribute which should be modified. This parameter is required.
	 *            The following names of attributes can be used to change the associated field:
	 *            "goalName", "isRepeateable", "isGroupGoal", "rewardId", "ruleId" and "roles".
	 * @param value
	 *            The new value of the attribute.
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *           to which this role belongs to.
	 * @return {@link Response} of {@link Goal} in JSON
	 */
	@PUT
	@Path("/{id}/attributes")
	@TypeHint(Goal.class)
	public Response changeGoalAttributes(@PathParam("id") @ValidPositiveDigit String goalId, @QueryParam("attribute") String attribute,
			@QueryParam("value") String value, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("change Attribute of Goal");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Goal goal = goalDao.getGoal(ValidateUtils.requireGreaterThenZero(goalId));

		if ("null".equals(value) || value != null && value.isEmpty()) {
			value = null;
		}

		// not changeable: id -> generated & belongsTo;
		switch (attribute) {
		case "goalName":
			goal.setName(value);
			break;

		case "isRepeateable":
			goal.setRepeatable(Boolean.parseBoolean(value));
			break;

		case "isGroupGoal":
			goal.setPlayerGroupGoal(Boolean.parseBoolean(value));
			break;

		case "rewardId":
			changeRewardIds(value, organisation, goal, apiKey);
			break;

		case "ruleId":
			GoalRule goalRule = ruleDao.getRule(ValidateUtils.requireGreaterThenZero(value));
			goal.setRule(goalRule);
			break;

		case "roles":
			changeRoles(value, goal, apiKey);
			break;
		}

		goalDao.insertGoal(goal);

		return ResponseSurrogate.created(goal);
	}

	/**
	 * This method converts the string of reward ids which are transfered to a list of rewards.
	 * These rewards are then set as the new list of rewards a player can earn by completing the goal. 
	 * 
	 * @param value
	 * 			The new values of rewards as string separated by commas. This parameter is required.
	 * @param organisation
	 * 			 The organisation the goal belongs to and which is represented by the API key.. 
	 * @param goal
	 * 			The goal whose field of rewards will be modified. This parameter should be not 
	 * 		  	null. 
	 * @param apiKey
	 * 			  The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this goal belongs to.
	 */
	private void changeRewardIds(@NotNull String value, Organisation organisation, Goal goal, String apiKey) {
		String commaSeparatedList = StringUtils.validateAsListOfDigits(value);
		List<Integer> ids = StringUtils.stringArrayToIntegerList(commaSeparatedList);
		List<Reward> rewards = rewardDao.getRewards(ids, apiKey);
		goal.setRewards(rewards);
	}

	/**
	 * This method converts the string of role-ids which are transfered to a list of roles.
	 * These roles are then set as the new list of roles a player can have to be allowed to complete a goal. 
	 * 
	 * @param value
	 * 			The new values of roles as string separated by commas. This parameter is required.
	 * @param goal
	 * 			The goal whose field of roles will be modified. This parameter should be not 
	 * 		  	null. 
	 * @param apiKey
	 * 			  The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this goal belongs to.
	 */
	private void changeRoles(@NotNull String value, Goal goal, @NotNull String apiKey) {
		String commaSeparatedList = StringUtils.validateAsListOfDigits(value);
		List<Integer> ids = StringUtils.stringArrayToIntegerList(commaSeparatedList);
		List<Role> roles = roleDao.getRoles(ids, apiKey);
		goal.setCanCompletedBy(roles);
	}

	/**
	 * Removes a specific goal from the data base which is identified by the given id and the 
	 * API key. If the API key is not valid an analogous message is returned. It is also checked,
	 * if the id is a positive number otherwise a message for an invalid number is returned. 
	 * 
	 * @param id
	 *          Required integer which uniquely identify the {@link Goal}.	
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this goal belongs to.
	 * @return {@link Response} of {@link Goal} in JSON.
	 */
	@DELETE
	@Path("/{id}")
	@TypeHint(Goal.class)
	public Response deleteGoal(@PathParam("id") @ValidPositiveDigit @NotNull String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		if (id == null) {
			throw new ApiError(Response.Status.FORBIDDEN, "no goalId transferred");
		}

		int goalId = ValidateUtils.requireGreaterThenZero(id);
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Goal goal = goalDao.deleteGoalByIdAndOrganisation(goalId, organisation);

		ValidateUtils.requireNotNull(goalId, goal);
		return ResponseSurrogate.deleted(goal);
	}
}
