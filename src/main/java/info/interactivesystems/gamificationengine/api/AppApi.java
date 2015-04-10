package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.PlayerGroup;
import info.interactivesystems.gamificationengine.entities.Role;
import info.interactivesystems.gamificationengine.entities.goal.Goal;
import info.interactivesystems.gamificationengine.entities.goal.TaskRule;
import info.interactivesystems.gamificationengine.entities.rewards.Reward;
import info.interactivesystems.gamificationengine.entities.task.Task;

import java.util.HashMap;
import java.util.function.Function;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * API class for application information
 */
@Path("/")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class AppApi {

	@Inject
	AccountApi accountApi;
	@Inject
	OrganisationApi organisationApi;
	@Inject
	TaskApi taskApi;
	@Inject
	RoleApi roleApi;
	@Inject
	PlayerApi playerApi;
	@Inject
	PlayerGroupApi playerGroupApi;
	@Inject
	RewardApi rewardApi;
	@Inject
	RuleApi ruleApi;
	@Inject
	GoalApi goalApi;

	String email = "test@example.com";
	String password = "123456";

	@POST
	@Path("/createAppData")
	public Response createData() {

		HashMap<String, Object> map = new HashMap<>();
		accountApi.create(email, password, "Paul", "Parenko");
		String apiKey = function(organisationApi.create("Interactive Systems Inc.", email, password), Organisation::getApiKey);
		map.put("apiKey", apiKey);

		String villainRoleId = asString(roleApi.create("villain", apiKey), Role::getId);
		map.put("villainRole", villainRoleId);

		Player player0 = content(playerApi.create("Bad Mood", "villain", null, villainRoleId, apiKey));
		map.put("player0", player0);

		String playerRoleId = asString(roleApi.create("player", apiKey), Role::getId);
		map.put("roleId", playerRoleId);

		Player player1 = content(playerApi.create("PaulP", "paul", null, playerRoleId, apiKey));
		map.put("player1", player1);
		Player player2 = content(playerApi.create("MichaelS", "michael", null, playerRoleId, apiKey));
		map.put("player2", player2);
		Player player3 = content(playerApi.create("KathrinB", "kathrin", null, playerRoleId, apiKey));
		map.put("player3", player3);

		// group

		String player2Id = String.valueOf(player2.getId());
		String player3Id = String.valueOf(player3.getId());
		String komediaGroup = String.join(",", player2Id, player3Id);
		String komediaGroupId = asString(playerGroupApi.createNewGroup(komediaGroup, "Komedia", null, apiKey), PlayerGroup::getId);

		/**
		 * Laugh
		 */

		String laughTaskId = asString(taskApi.createNewTask("Laugh", "A player has laughed once", "false", playerRoleId, apiKey), Task::getId);
		map.put("laughTaskId", laughTaskId);
		String pointRewardId = asString(rewardApi.createPointReward("10", apiKey), Reward::getId);
		map.put("pointRewardId", pointRewardId);
		String coinRewardId = asString(rewardApi.createCoinsReward("1", apiKey), Reward::getId);
		map.put("coinRewardId", coinRewardId);

		String laughRuleId = asString(ruleApi.createNewTaskRule("DoAllTasksRule", "Laugh rule", "rule for laughing once", laughTaskId, apiKey),
				TaskRule::getId);
		map.put("laughRuleId", laughRuleId);

		String laughRewards = String.join(",", pointRewardId, coinRewardId);
		String laughGoalId = asString(goalApi.createNewGoal("Laugh goal", "true", laughRuleId, pointRewardId, playerRoleId, "false", apiKey),
				Goal::getId);
		map.put("laughGoalId", laughGoalId);

		/**
		 * Kitchen hero ((Bring cake + Bring cookies) * Cook coffee); single
		 * goal, not repeatable
		 */
		String cookCoffeeTaskId = asString(taskApi.createNewTask("Cook coffee", "A player has cooked coffee", "false", playerRoleId, apiKey),
				Task::getId);
		map.put("cookCoffeeTaskId", cookCoffeeTaskId);
		String bringCakeTaskId = asString(taskApi.createNewTask("Bring cake", "A player has cooked coffee", "false", playerRoleId, apiKey),
				Task::getId);
		map.put("bringCakeTaskId", bringCakeTaskId);
		String bringCookiesTaskId = asString(taskApi.createNewTask("Bring cookies", "A player has cooked coffee", "false", playerRoleId, apiKey),
				Task::getId);
		map.put("bringCookiesTaskId", bringCookiesTaskId);

		String kitchenBadgeId = asString(rewardApi.createBadge("Kitchen hero badge", "A Kitchen hero badge", null, apiKey), Reward::getId);
		map.put("kitchenBadgeId", kitchenBadgeId);

		String kitchenTaskIds = String.join(",", cookCoffeeTaskId, bringCakeTaskId, bringCookiesTaskId);
		String kitchenRuleId = asString(ruleApi.createNewTaskRule("DoAllTasksRule", "Kitchen hero rule", "rule for kitchen", kitchenTaskIds, apiKey),
				TaskRule::getId);
		map.put("kitchenRuleId", kitchenRuleId);

		String kitchenGoalId = asString(
				goalApi.createNewGoal("Kitchen hero goal", "false", kitchenRuleId, kitchenBadgeId, playerRoleId, "false", apiKey), Goal::getId);
		map.put("kitchenGoalId", kitchenGoalId);

		/**
		 * Kryptonite remover / Kryptonit-Entferner / Clean-up hero (Bring
		 * garbage out * Water flowers * Dispel dishwasher); group goal, not
		 * repeatable
		 */
		String garbageTaskId = asString(taskApi.createNewTask("Bring garbage out", "A player has cooked coffee", "false", playerRoleId, apiKey),
				Task::getId);
		map.put("garbageTaskId", garbageTaskId);
		String flowersTaskId = asString(taskApi.createNewTask("Water flowers", "A player has cooked coffee", "false", playerRoleId, apiKey),
				Task::getId);
		map.put("flowersTaskId", flowersTaskId);
		String dishwasherTaskId = asString(taskApi.createNewTask("Dispel dishwasher", "A player has cooked coffee", "false", playerRoleId, apiKey),
				Task::getId);
		map.put("dishwasherTaskId", dishwasherTaskId);

		String cleanUpBadgeId = asString(rewardApi.createBadge("Clean-up hero badge", "A clean-up hero badge badge", null, apiKey), Reward::getId);
		map.put("cleanUpBadgeId", cleanUpBadgeId);

		String cleanUpTaskIds = String.join(",", garbageTaskId, flowersTaskId, dishwasherTaskId);
		String cleanUpRuleId = asString(
				ruleApi.createNewTaskRule("DoAllTasksRule", "Clean-up hero rule", "rule for clean-up", cleanUpTaskIds, apiKey), TaskRule::getId);
		map.put("cleanUpRuleId", cleanUpRuleId);

		String cleanUpGoalId = asString(
				goalApi.createNewGoal("Clean-up hero goal", "false", cleanUpRuleId, cleanUpBadgeId, playerRoleId, "true", apiKey), Goal::getId);
		map.put("cleanUpGoalId", cleanUpGoalId);

		/**
		 * Bage + Punkte; einamal badge, ansonsten punkte (irgendwie durch goal)
		 * -- evtl. Coins
		 */

		/**
		 * Level up
		 */

		/**
		 * getOfferByRole;; Create marktplatz ;createNewOffer;
		 * giveABid;compliteOffer
		 */

		//

		// build
		return Response.ok(map).build();
	}

	@POST
	@Path("/updateAppData")
	public Response updateData() {
		HashMap<String, Object> map = new HashMap<>();

		/**
		 * update 2 badge icons
		 */
		/*
		 * String apiKey = function(organisationApi.get("1",email, password),
		 * Organisation::getApiKey);
		 * 
		 * String cleanUpBadgeId = asString(rewardApi.changeBadge("id", "icon",
		 * "http://", apiKey), Reward::getId); map.put("cleanUpBadgeId",
		 * cleanUpBadgeId);
		 */

		// build
		return Response.ok(map).build();
	}

	@SuppressWarnings("unchecked")
	private static <T> T content(Response response) {
		ResponseSurrogate<T> surrogate = (ResponseSurrogate<T>) response.getEntity();
		return surrogate.content;
	}

	private static <T> String asString(Response response, Function<T, Integer> function) {
		return String.valueOf(function(response, function));
	}

	private static <T, R> R function(Response response, Function<T, R> function) {
		return function.apply(content(response));
	}
}
