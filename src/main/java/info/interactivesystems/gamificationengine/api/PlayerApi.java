package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
import info.interactivesystems.gamificationengine.api.validation.ValidListOfDigits;
import info.interactivesystems.gamificationengine.api.validation.ValidPositiveDigit;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.dao.PlayerDAO;
import info.interactivesystems.gamificationengine.dao.RoleDAO;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.Role;
import info.interactivesystems.gamificationengine.entities.goal.FinishedGoal;
import info.interactivesystems.gamificationengine.entities.rewards.Achievement;
import info.interactivesystems.gamificationengine.entities.rewards.Badge;
import info.interactivesystems.gamificationengine.entities.rewards.PermanentReward;
import info.interactivesystems.gamificationengine.entities.task.FinishedTask;
import info.interactivesystems.gamificationengine.utils.ImageUtils;
import info.interactivesystems.gamificationengine.utils.StringUtils;

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
 * API for player related services.
 */
@Path("/player")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class PlayerApi {
	private static final Logger log = LoggerFactory.getLogger(PlayerApi.class);

	@Inject
	OrganisationDAO organisationDao;
	@Inject
	PlayerDAO playerDao;
	@Inject
	RoleDAO roleDao;

	/**
	 * Creates new player-id for gamificated app. Developer organisation
	 * credentials are mandatory.
	 * 
	 * @param nickname
	 *            required query param of the player
	 * @param password
	 *            required query param for access
	 * @param playerRoleIds
	 *            optional list of role ids
	 * @param reference
	 *            optional player reference string maping customers user
	 *            identifier
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Player} in JSON
	 */
	@POST
	@Path("/")
	public Response create(@QueryParam("nickname") @NotNull String nickname, @QueryParam("password") @NotNull String password,
			@QueryParam("reference") String reference, @QueryParam("roleIds") @ValidListOfDigits String playerRoleIds,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("createplayer requested (Params: apiKey = {}, nickname = {}, password = {}", apiKey, nickname, password);

		List<Integer> roleIds = StringUtils.stringArrayToIntegerList(playerRoleIds);
		List<Role> roles = roleDao.getRoles(roleIds, apiKey);

		List<Integer> roleIds1 = new ArrayList<>(roleIds);
		roleIds1.removeAll(roles.stream().map(Role::getId).collect(Collectors.toList()));
		if (!roleIds1.isEmpty()) {
			throw new ApiError(Response.Status.FORBIDDEN, "Creation failed, role ids don't exists " + roleIds1);
		}

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Player player = new Player();
		player.setBelongsTo(organisation);
		player.setPassword(password);
		player.setReference(reference);
		player.setNickname(nickname);
		player.setBelongsToRoles(roles);

		playerDao.insert(player);
		return ResponseSurrogate.created(player);
	}

	/**
	 * Collects all players associated with this api key.
	 * 
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link List<Player>} in JSON
	 */
	@GET
	@Path("/*")
	public Response getAll(@QueryParam("apiKey") @ValidApiKey String apiKey) {

		List<Player> players = playerDao.getPlayers(apiKey);
		return ResponseSurrogate.of(players);
	}

	/**
	 * Returns the player-organisation as json.
	 *
	 * @param id
	 *            required integer uniquely identifying the {@link Player}
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Player} in JSON
	 */
	@GET
	@Path("/{id}")
	public Response get(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("getplayer requested");

		int playerId = ValidateUtils.requireGreaterThenZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);

		ValidateUtils.requireNotNull(playerId, player);
		return ResponseSurrogate.of(player);
	}

	/**
	 * Removes a player from data base.
	 * 
	 * @param id
	 *            required player id path param
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link List<Player>} in JSON
	 */
	@DELETE
	@Path("/{id}")
	public Response delete(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		int playerId = ValidateUtils.requireGreaterThenZero(id);
		ValidateUtils.requireGreaterThenZero(playerId);

		log.debug("getplayer requested");

		Player player = playerDao.deletePlayer(playerId, apiKey);

		ValidateUtils.requireNotNull(playerId, player);
		return ResponseSurrogate.deleted(player);
	}

	/**
	 * Changes value of a given attribute key.
	 * 
	 * @param id
	 *            required valid player id
	 * @param attribute
	 *            required key which should be modified
	 * @param value
	 *            required content corresponding to the attribute
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Player} in JSON
	 */
	@PUT
	@Path("/{id}/attributes")
	public Response changeAttributes(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("attribute") String attribute,
			@QueryParam("value") String value, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("change Attribute of Player");

		int playerId = ValidateUtils.requireGreaterThenZero(id);
		log.debug("Player Id" + playerId);

		Player player = playerDao.getPlayer(playerId, apiKey);

		// not: id -> generated & belongsTo ->
		switch (attribute) {
		case "password":
			player.setPassword(value);
			break;

		case "reference":
			player.setReference(value);
			break;

		case "nickname":
			player.setNickname(value);
			break;

		case "playerRoles":
			changePlayerRoles(value, player, apiKey);
			break;

		case "contact":
			changeContacts(value, player, apiKey);
			break;

		case "avatar":
			player.setAvatar(ImageUtils.imageToByte(value));
			break;

		default:
			break;
		}

		playerDao.insert(player);
		return ResponseSurrogate.updated(player);
	}

	private void changePlayerRoles(@NotNull String value, Player player, String apiKey) {
		String commaSeparatedList = StringUtils.validateAsListOfDigits(value);
		List<Integer> ids = StringUtils.stringArrayToIntegerList(commaSeparatedList);
		List<Role> roles = roleDao.getRoles(ids, apiKey);
		player.setBelongsToRoles(roles);
	}

	private void changeContacts(@NotNull String value, Player player, String apiKey) {
		String commaSeparatedList = StringUtils.validateAsListOfDigits(value);
		List<Integer> ids = StringUtils.stringArrayToIntegerList(commaSeparatedList);
		List<Player> contacts = roleDao.getPlayers(ids, apiKey);
		player.setContactList(contacts);
	}

	/**
	 * Adds a contact to the current player's contact list.
	 * 
	 * @param id
	 *            required valid player id
	 * @param contactIds
	 *            required list of contact ids
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Player} in JSON
	 */
	@PUT
	@Path("/{id}/contacts")
	public Response addContacts(@PathParam("id") @ValidPositiveDigit String id,
			@QueryParam("contactIds") @NotNull @ValidListOfDigits String contactIds, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("adding contacts to player");

		List<Integer> list = StringUtils.stringArrayToIntegerList(contactIds);
		List<Player> contactsToAdd = roleDao.getPlayers(list, apiKey);

		int playerId = ValidateUtils.requireGreaterThenZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		player.addContacts(contactsToAdd);

		playerDao.insert(player);
		return ResponseSurrogate.updated(player);
	}

	/**
	 * Removes one or many contacts from players contact list.
	 * 
	 * @param id
	 *            required valid player id
	 * @param contactIds
	 *            required list of contact ids
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Player} in JSON
	 */
	@DELETE
	@Path("/{id}/contacts")
	public Response deleteContact(@PathParam("id") @ValidPositiveDigit String id,
			@QueryParam("contactIds") @NotNull @ValidListOfDigits String contactIds, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("deleting contacts for of a player");

		List<Integer> list = StringUtils.stringArrayToIntegerList(contactIds);
		List<Player> contactsToDelete = roleDao.getPlayers(list, apiKey);

		int playerId = ValidateUtils.requireGreaterThenZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		player.removeContacts(contactsToDelete);

		playerDao.insert(player);
		return ResponseSurrogate.deleted(player);
	}

	/**
	 * Returns an avatar associated with a player.
	 * 
	 * @param id
	 *            required valid player id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Object} with an image field in JOSN
	 */
	@GET
	@Path("{id}/avatar")
	public Response getAvatar(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("get player's avatar image");

		int playerId = ValidateUtils.requireGreaterThenZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);

		byte[] bytes = player.getAvatar();
		// Image img;
		// ImageIcon icon2 = new ImageIcon(image);
		// img = icon2.getImage();

		return ResponseSurrogate.of(new Object() {
			public byte[] image = bytes;
			// public Image image = img;
		});
	}

	/**
	 * Deactivates a player with associated id.
	 * 
	 * @param id
	 *            required valid player id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Player} in JSON
	 */
	@POST
	@Path("{id}/deactivate")
	public Response deactivate(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("deactivate player called");

		int playerId = ValidateUtils.requireGreaterThenZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);

		player.setActive(false);

		return ResponseSurrogate.of(player);
	}

	/**
	 *
	 * Returns a list of finished goals of a specific player.
	 *
	 * @param id
	 *            required player id
	 *
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link List<FinishedGoal>} in JSON
	 */
	@GET
	@Path("/{id}/goals")
	public Response getPlayerFinishedGoals(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("getFinishedGoals requested");
		List<FinishedGoal> goals = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(id), apiKey).getFinishedGoals();

		return ResponseSurrogate.of(goals);
	}

	/**
	 * Returns a list of all rewards associated for player with assigned id.
	 *
	 * @param id
	 *            required player id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link List<PermanentReward>} in JSON
	 */
	@GET
	@Path("/{id}/rewards")
	public Response getRewards(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("getPlayerPermanentRewards requested");
		List<PermanentReward> pRewards = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(id), apiKey).getRewards();

		return ResponseSurrogate.of(pRewards);
	}

	/**
	 * Returns a list of all finished tasks associated for player with assigned
	 * id.
	 *
	 * @param id
	 *            required player id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link List<FinishedTask>} in JSON
	 */
	@GET
	@Path("/{id}/tasks")
	public Response getPlayerFinishedTasks(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("getFinishedTasks requested");
		List<FinishedTask> fTasks = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(id), apiKey).getFinishedTasks();

		return ResponseSurrogate.of(fTasks);
	}

	/**
	 * Returns a list of all badges associated for player with assigned id.
	 *
	 * @param id
	 *            required player id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link List<Badge>} in JSON
	 */
	@GET
	@Path("/{id}/badges")
	public Response getPlayerBadges(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("get earned Badges from Player requested");
		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(id), apiKey);
		List<Badge> badges = player.getOnlyBadges();

		return ResponseSurrogate.of(badges);
	}

	/**
	 * Returns a list of all achievements associated for player with assigned
	 * id.
	 *
	 * @param id
	 *            required player id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link List<Achievement>} in JSON
	 */
	@GET
	@Path("/{id}/achievements")
	public Response getPlayerAchievements(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("get earned Achievements from Player requested");
		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(id), apiKey);
		List<Achievement> achievements = player.getOnlyAchievement();

		return ResponseSurrogate.of(achievements);
	}

	/**
	 * Returns all points associated for player with assigned id.
	 *
	 * @param id
	 *            required player id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link int} in JSON
	 */
	@GET
	@Path("/{id}/points")
	public Response getPlayerPoints(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("get earned Achievements from Player requested");
		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(id), apiKey);
		int points = player.getPoints();

		return ResponseSurrogate.of(points);
	}

	/**
	 * Returns all coins associated for player with assigned id.
	 *
	 * @param id
	 *            required player id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link int} in JSON
	 */
	@GET
	@Path("/{id}/coins")
	public Response getPlayerCoins(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("get earned Achievements from Player requested");
		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(id), apiKey);
		int coins = player.getCoins();

		return ResponseSurrogate.of(coins);
	}
}
