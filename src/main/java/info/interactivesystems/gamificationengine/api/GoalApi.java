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

/**
 * API for goal related services.
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
	 * Creates a new Goal.
	 * 
	 * @param name
	 *            required name of the goal
	 * @param repeatable
	 *            optional switch. Is the goal repeatable? "1" or "0", "true" or
	 *            "false". Default "true"
	 * @param ruleId
	 *            required rule which completes your goal
	 * @param rewardIds
	 *            required rewards that are awarded to the player
	 * @param roleIds
	 *            optional role ids which can complete this goal
	 * @param isGroupGoal
	 *            optional switch. Can this goal be completed by a group? "1" or
	 *            "0", "true" or "false". Default "false"
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Goal} in JSON
	 */
	@POST
	@Path("/")
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
	 * Returns all goals which can be tried to complete. These goals are specific for the organisation which has the same api key.
	 * 
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link javax.ws.rs.core.Response} of {@link List<Goal>} in JSON
	 */
	@GET
	@Path("/*")
	public Response getGoals(@QueryParam("apiKey") @ValidApiKey String apiKey) {

		List<Goal> goals = goalDao.getGoals(apiKey);
		return ResponseSurrogate.of(goals);
	}

	/**
	 * Gets the {@link GoalRule} object by id.
	 * 
	 * @param id
	 *            required goal id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Goal} in JSON
	 */
	@GET
	@Path("/{id}")
	public Response getGoal(@PathParam("id") @ValidPositiveDigit @NotNull String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		int goalId = ValidateUtils.requireGreaterThenZero(id);
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Goal goal = goalDao.getGoalByIdAndOrganisation(goalId, organisation);

		ValidateUtils.requireNotNull(goalId, goal);
		return ResponseSurrogate.of(goal);
	}

	/**
	 * Changes attributes of the goal.
	 * 
	 * @param goalId
	 *            required id of the goal which should be modified
	 * @param attribute
	 *            required attribute which should be modified
	 * @param value
	 *            required new value of the attribute
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Goal} in JSON
	 */
	@PUT
	@Path("/{id}/attributes")
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

	private void changeRewardIds(@NotNull String value, Organisation organisation, Goal goal, String apiKey) {
		String commaSeparatedList = StringUtils.validateAsListOfDigits(value);
		List<Integer> ids = StringUtils.stringArrayToIntegerList(commaSeparatedList);
		List<Reward> rewards = rewardDao.getRewards(ids, apiKey);
		goal.setRewards(rewards);
	}

	private void changeRoles(@NotNull String value, Goal goal, @NotNull String apiKey) {
		String commaSeparatedList = StringUtils.validateAsListOfDigits(value);
		List<Integer> ids = StringUtils.stringArrayToIntegerList(commaSeparatedList);
		List<Role> roles = roleDao.getRoles(ids, apiKey);
		goal.setCanCompletedBy(roles);
	}

	/**
	 * Deletes a Goal.
	 * 
	 * @param id
	 *            required id of the goal
	 * @param apiKey
	 *            your api key
	 * @return {@link Response} of {@link Goal} in JSON
	 */
	@DELETE
	@Path("/{id}")
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
