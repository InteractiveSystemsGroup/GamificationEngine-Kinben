package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
import info.interactivesystems.gamificationengine.api.validation.ValidListOfDigits;
import info.interactivesystems.gamificationengine.api.validation.ValidListOfDigitsOrNull;
import info.interactivesystems.gamificationengine.api.validation.ValidPositiveDigit;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.dao.PlayerDAO;
import info.interactivesystems.gamificationengine.dao.PlayerGroupDAO;
import info.interactivesystems.gamificationengine.dao.RoleDAO;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.PlayerGroup;
import info.interactivesystems.gamificationengine.entities.Role;
import info.interactivesystems.gamificationengine.entities.goal.FinishedGoal;
import info.interactivesystems.gamificationengine.entities.rewards.Achievement;
import info.interactivesystems.gamificationengine.entities.rewards.Badge;
import info.interactivesystems.gamificationengine.entities.rewards.PermanentReward;
import info.interactivesystems.gamificationengine.entities.task.FinishedTask;
import info.interactivesystems.gamificationengine.utils.ImageUtils;
import info.interactivesystems.gamificationengine.utils.SecurityTools;
import info.interactivesystems.gamificationengine.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
 * A player represents a user in the gamification application, eg. an employee of an organisation or a customer. 
 * By the creation, each player is assigned a nickname and certain roles. Each player has a list for his earned 
 * rewards, already finished Goals and finished Tasks. The initial value of possible points, coins and index of 
 * a level is set to "0". These can be raised by fulfilling tasks in the gamification application. Furthermore 
 * a player can have an avatar, by specifying the path of an image previously uploaded to a server. 
 * A player can be set active or can be deactivated so that she/he cannot complete tasks. In addition to create 
 * and to delete a player it is possible to get one particular player of one specific organisation by her/his 
 * associated id or all players of the organisation. The avatar of one player can also be requested. To display 
 * the status of a player ancillary the already finished goals and finished tasks it can be requested all earned 
 * permanent rewards. If only one status element is needed, the current points, coins, badges or achievements 
 * can be gotten instead. 
 * Each player can also have a list of contacts which represent other players in the same organisation, so players 
 * can send them little presents. 
 * At a later point of time it is possible to change the password, nickname, avatar and the roles or contacts a 
 * player has.
 * In the responses the player's password and avatar isn't returned because of security reasons respectively overload.
 * To get the avatar of a player a specific get request can be sent ("/player/{id}/avatar").
 */
@Path("/player")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class PlayerApi {
	private static final Logger LOGGER = LoggerFactory.getLogger(PlayerApi.class);

	@Inject
	OrganisationDAO organisationDao;
	@Inject
	PlayerDAO playerDao;
	@Inject
	RoleDAO roleDao;
	@Inject
	PlayerGroupDAO groupDao;
	

	/**
	 * Creates a new player and so the method generates the player-id. The organisation's API key 
	 * is mandatory otherwise a warning with the hint for a non valid API key is returned.
	 * The player can choose a password for her/his account. By the creation some initial
	 * roles can be set which can also be changed at a later point of time. By default every 
	 * created player is active until she/he is deactivated. It is checked, if the id of the 
	 * roles are positive numbers otherwise a message for the invalid number is returned.
	 * In the response the player's password and avatar isn't returned because of security 
	 * reasons respectively overhead.
	 * 
	 * @param nickname
	 *            The query parameter of the player's nickname. This field must not be null.
	 * @param password
	 *            The query parameter for the player's password. This field must not be null.
	 * @param playerRoleIds
	 *            Optionally a list of role ids can be passed that a player has. These ids are 
	 *            separated by commas.
	 * @param reference
	 *            Optionally the player's reference as string map to a customers user
	 *            identifier.
	 * @param avatar           
	 * 			  The url of the avatar. The image's size can be up to 3 MB.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this player should belong to.
	 * @return A Response of Player in JSON.
	 */
	@POST
	@Path("/")
	@TypeHint(Player.class)
	public Response create(@QueryParam("nickname") @NotNull String nickname, @QueryParam("password") @NotNull String password,
			@QueryParam("reference") String reference, 
			@QueryParam("roleIds") @DefaultValue("null") @ValidListOfDigitsOrNull String playerRoleIds,
			@QueryParam("avatar") String avatar,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		LOGGER.debug("createplayer requested (Params: apiKey = {}, nickname = {}, password = {}", apiKey, nickname, password);

		List<Role> roles = new ArrayList<>();
		if(!playerRoleIds.equals("null")){
			List<Integer> roleIds = StringUtils.stringArrayToIntegerList(playerRoleIds);
			roles = roleDao.getRoles(roleIds, apiKey);
	
			List<Integer> roleIds1 = new ArrayList<>(roleIds);
			roleIds1.removeAll(roles.stream().map(Role::getId).collect(Collectors.toList()));
			if (!roleIds1.isEmpty()) {
				throw new ApiError(Response.Status.FORBIDDEN, "Creation failed, role ids don't exist " + roleIds1);
			}
		}
			
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		
		Player player = new Player();
		
		List<Player> existingPlayers = playerDao.getPlayers(apiKey);
		if(player.checkNickname(nickname, existingPlayers)){
			throw new ApiError(Response.Status.FORBIDDEN, "The nickname is used by another player. Please choose another one.");
		}
		
		player.setBelongsTo(organisation);
		player.setPassword(SecurityTools.encryptWithSHA512(password));
		player.setReference(reference);
		player.setNickname(nickname);
		player.setBelongsToRoles(roles);
		
		if (avatar != null) {
			try {
				player.setAvatar(ImageUtils.imageToByte(avatar));
			} catch (Exception e) {
				throw new ApiError(Response.Status.FORBIDDEN, "Failed to store the avatar in the database.");
			}
		}

		playerDao.insert(player);
		return ResponseSurrogate.created(player);
	}

	/**
	 * This method collects all players associated with the given API key and so all players who 
	 * belong to the associated organisation. If the API key is not valid an analogous message 
	 * is returned.
	 * In the response the players' password and avatar isn't returned because of security 
	 * reasons respectively overhead.
	 * 
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation.
	 * @return A Response as List of Players in JSON.
	 */
	@GET
	@Path("/*")
	@TypeHint(Player[].class)
	public Response getAll(@QueryParam("apiKey") @ValidApiKey String apiKey) {

		List<Player> players = playerDao.getPlayers(apiKey);
		return ResponseSurrogate.of(players);
	}

	/**
	 * This method gets one specific player who is identified by the given id and the API key.
	 * If the API key is not valid an analogous message is returned. It is also checked, if the 
	 * id is a positive number otherwise a message for an invalid number is returned.
	 * In the response the player's password and avatar isn't returned because of security 
	 * reasons respectively overhead.
	 *
	 * @param id
	 *           Required integer as path parameter which uniquely identify the {@link Player}.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this player belongs to.
	 * @return Response of Player in JSON.
	 */
	@GET
	@Path("/{id}")
	@TypeHint(Player.class)
	public Response get(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		int playerId = ValidateUtils.requireGreaterThanZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		ValidateUtils.requireNotNull(playerId, player);
		
		return ResponseSurrogate.of(player);
	}

	/**
	 * Removes a specific player from the data base who is identified by the given id and the 
	 * API key. If the API key is not valid an analogous message is returned. It is also checked,
	 * if the id is a positive number otherwise a message for an invalid number is returned. 
	 * In the response the player's password and avatar isn't returned because of security 
	 * reasons respectively overhead.
	 * 
	 * @param id
	 *           Required integer as path parameter which uniquely identify the {@link Player}.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this role belongs to.
	 * @return Response as List of Players in JSON.
	 */
	@DELETE
	@Path("/{id}")
	@TypeHint(Player.class)
	public Response delete(@PathParam("id") @NotNull @ValidPositiveDigit String id, 
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		int playerId = ValidateUtils.requireGreaterThanZero(id);
		Player player = playerDao.deletePlayer(playerId, apiKey);
		ValidateUtils.requireNotNull(playerId, player);

		return ResponseSurrogate.deleted(player);
	}

	/**
	 * With this method the fields of one specific player can be changed. For 
	 * this the player id, the API key of the specific organisation, the 
	 * name of the field and the new field's value are needed. 
	 * To modify the password, the reference and the nickname the new string has 
	 * to be transfered with the attribute field. For a new avatar the path of
	 * new image is needed in the attribute parameter. The format of the image
	 * has to be .jpg or .png. A new list of roles and contacts can be transfered 
	 * when their ids are separated by commas. 
	 * If the API key is not valid an analogous message is returned. It is 
	 * also checked, if the id is a positive number otherwise a message for 
	 * an invalid number is returned.
	 * In the response the player's password and avatar isn't returned because of security 
	 * reasons respectively overhead.
	 * 
	 * @param id
	 *            Required integer which uniquely identify the {@link Player}.
	 * @param attribute
	 *            The name of the attribute which should be modified. This 
	 *            parameter is required. The following names of attributes can 
	 *            be used to change the associated field:
	 *            "password", "reference", "nickname", "playerRoles", "contact" 
	 *            and "avatar".
	 * @param value
	 *            The new value of the attribute. This parameter is required.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this player belongs to.
	 * @return Response of Player in JSON.
	 */
	@PUT
	@Path("/{id}/attributes")
	@TypeHint(Player.class)
	public Response changeAttributes(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("attribute") String attribute,
			@QueryParam("value") String value, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		LOGGER.debug("change Attribute of Player");

		int playerId = ValidateUtils.requireGreaterThanZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		ValidateUtils.requireNotNull(playerId, player);

		// not: id -> generated & belongsTo -> fixed
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
			try {
				player.setAvatar(ImageUtils.imageToByte(value));
			} catch (Exception e) {
				throw new ApiError(Response.Status.FORBIDDEN, "Failed to store the avatar in the database.");
			} 
			break;

		default:
			break;
		}

		playerDao.insert(player);
		return ResponseSurrogate.updated(player);
	}

	/**
	 * This method converts the string of role ids which are transfered to a list of roles.
	 * These roles are then set as the new list of roles a player has. 
	 * 
	 * @param value
	 * 		   	The new values of player roles as string separated by commas. This parameter is 
	 * 		   	required.
	 * @param player
	 * 		  	The player which field of roles will be modified. This parameter should be not 
	 * 		  	null because this method is called by a method which checks the given id if a player
	 * 		  	exists. 
	 * @param apiKey
	 *   	   	The valid query parameter API key affiliated to one specific organisation, 
	 *        	to which this player belongs to.
	 * 	
	 */
	private void changePlayerRoles(@NotNull String value, Player player, String apiKey) {
		String commaSeparatedList = StringUtils.validateAsListOfDigits(value);
		List<Integer> ids = StringUtils.stringArrayToIntegerList(commaSeparatedList);
		List<Role> roles = roleDao.getRoles(ids, apiKey);
		player.setBelongsToRoles(roles);
	}

	/**
	 * This method converts the string of contact ids which are transfered to a list of players.
	 * These players are then set as the new list of contacts a player has. 
	 * 
	 * @param value
	 * 			The new values of player contacts as string separated by commas. This parameter is 
	 * 		   	required.
	 * @param player
	 * 			The player which field of roles will be modified. This parameter should be not 
	 * 		  	null because this method is called by a method which checks the given id if a player
	 * 		  	exists. 
	 * @param apiKey
	 * 			The valid query parameter API key affiliated to one specific organisation, 
	 *        	to which this player belongs to.
	 */
	private void changeContacts(@NotNull String value, Player player, String apiKey) {
		String commaSeparatedList = StringUtils.validateAsListOfDigits(value);
		List<Integer> ids = StringUtils.stringArrayToIntegerList(commaSeparatedList);
		List<Player> contacts = playerDao.getPlayers(ids, apiKey);
		player.setContactList(contacts);
	}

	/**
	 * Adds one or more contacts to the current player's contact list. A contact represents another
	 * player in the gamification application. All ids are checked, if they are positive numbers 
	 * otherwise a message for an invalid number is returned. If the API key is not valid an analogous
	 * message is returned.
	 * In the response the player's password and avatar isn't returned because of security 
	 * reasons respectively overhead.
	 * 
	 * @param id
	 *           Required path parameter as integer which uniquely identify the {@link Player}.
	 * @param contactIds
	 *           The list of player ids which should be added to the contact list. These ids are 
	 *           separated by commas.
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *           to which this player belongs to.
	 * @return Response of Player in JSON.
	 */
	@PUT
	@Path("/{id}/contacts")
	@TypeHint(Player.class)
	public Response addContacts(@PathParam("id") @ValidPositiveDigit String id,
			@QueryParam("contactIds") @NotNull @ValidListOfDigits String contactIds, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		LOGGER.debug("adding contacts to player");

		List<Integer> list = StringUtils.stringArrayToIntegerList(contactIds);
		List<Player> contactsToAdd = playerDao.getPlayers(list, apiKey);

		int playerId = ValidateUtils.requireGreaterThanZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		ValidateUtils.requireNotNull(playerId, player);
		
		player.addContacts(contactsToAdd);
		playerDao.insert(player);

		return ResponseSurrogate.updated(player);
	}

	
	/**
	 * Adds one or more roles to the current player's list of roles. All ids are checked, if they are 
	 * positive numbers otherwise a message for an invalid number is returned. If the API key is not 
	 * valid an analogous message is returned.
	 * In the response the player's password and avatar isn't returned because of security 
	 * reasons respectively overhead.
	 * 
	 * @param id
	 *           Required path parameter as integer which uniquely identify the {@link Player}.
	 * @param roleIds
	 *           The list of role ids which should be added to the contact list. These ids are 
	 *           separated by commas.
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *           to which this player belongs to.
	 * @return Response of Player in JSON.
	 */
	@PUT
	@Path("/{id}/roles")
	@TypeHint(Player.class)
	public Response addRoles(@PathParam("id") @NotNull @ValidPositiveDigit String id,
			@QueryParam("roleIds") @NotNull @ValidListOfDigits String roleIds, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		LOGGER.debug("adding roles to player");

		List<Integer> list = StringUtils.stringArrayToIntegerList(roleIds);
		List<Role> rolesToAdd = roleDao.getRoles(list, apiKey); 

		int playerId = ValidateUtils.requireGreaterThanZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		ValidateUtils.requireNotNull(playerId, player);

		player.addRoles(rolesToAdd); 
		playerDao.insert(player);
		
		return ResponseSurrogate.updated(player);
	}
	
	
	
	/**
	 * Removes one or more contacts from the currents player's contact list. A contact represents another
	 * player in the gamification application. All ids are checked, if they are positive numbers 
	 * otherwise a message for an invalid number is returned. If the API key is not valid an analogous
	 * message is returned.
	 * In the response the player's password and avatar isn't returned because of security 
	 * reasons respectively overhead.
	 * 
	 * @param id
	 *           Required path parameter as integer which uniquely identify the {@link Player}.
	 * @param contactIds
	 *           The list of player ids which should be added to the contact list. These ids are 
	 *           separated by commas and must not be null.
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *           to which this player belongs to.
	 * @return Response of Player in JSON.
	 */
	@DELETE
	@Path("/{id}/contacts")
	@TypeHint(Player.class)
	public Response deleteContact(@PathParam("id") @NotNull @ValidPositiveDigit String id,
			@QueryParam("contactIds") @NotNull @ValidListOfDigits String contactIds, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		LOGGER.debug("deleting contacts for of a player");

		List<Integer> list = StringUtils.stringArrayToIntegerList(contactIds);
		List<Player> contactsToDelete = playerDao.getPlayers(list, apiKey);

		int playerId = ValidateUtils.requireGreaterThanZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		ValidateUtils.requireNotNull(playerId, player);
		
		player.removeContacts(contactsToDelete);
		playerDao.insert(player);
		
		return ResponseSurrogate.deleted(player);
	}

	/**
	 * Returns the avatar which is associated with a player. To identify the player her/his id and 
	 * the API key is needed to which the player belongs to.
	 * The byte array of the avatar image is Base64-encoded to ensure that the data is transmitted 
	 * correctly as String.  
	 * If the API key is not valid an analogous message is returned. It is also checked, if the id 
	 * is a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param id
	 *          Required path parameter as integer which uniquely identify the {@link Player}.
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *           to which this player belongs to.
	 * @return Response of Object with an byte[] in JSON.
	 */
	@GET
	@Path("{id}/avatar")
	@TypeHint(byte[].class)
	public Response getAvatar(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		LOGGER.debug("get player's avatar image");

		int playerId = ValidateUtils.requireGreaterThanZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		ValidateUtils.requireNotNull(playerId, player);
		
		String b64= ImageUtils.encodeByteArrayToBase64(player.getAvatar());
		
		return ResponseSurrogate.of(b64);
	}

	/**
	 * Deactivates a player with the associated id and API key. So this player cannot complete a task
	 * until she/he is set active again.
	 * If the API key is not valid an analogous message is returned. It is also checked, if the id 
	 * is a positive number otherwise a message for an invalid number is returned.
	 * In the response the player's password and avatar isn't returned because of security 
	 * reasons respectively overhead.
	 * 
	 * @param id
	 *         Required path parameter as integer which uniquely identify the {@link Player}.
	 * @param apiKey
	 *         The valid query parameter API key affiliated to one specific organisation, 
	 *         to which this player belongs to.
	 * @return Response of Player in JSON.
	 */
	@PUT
	@Path("{id}/deactivate")
	@TypeHint(Player.class)
	public Response deactivate(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		int playerId = ValidateUtils.requireGreaterThanZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		ValidateUtils.requireNotNull(playerId, player);

		player.setActive(false);

		return ResponseSurrogate.of(player);
	}
	
	/**
	 * Activates a player with the associated id and API key so this player is allowed to complete
	 * tasks.
	 * If the API key is not valid an analogous message is returned. It is also checked, if the id 
	 * is a positive number otherwise a message for an invalid number is returned.
	 * In the response the player's password and avatar isn't returned because of security 
	 * reasons respectively overhead.
	 * 
	 * @param id
	 *         Required path parameter as integer which uniquely identify the {@link Player}.
	 * @param apiKey
	 *         The valid query parameter API key affiliated to one specific organisation, 
	 *         to which this player belongs to.
	 * @return Response of Player in JSON.
	 */
	@PUT
	@Path("{id}/activate")
	@TypeHint(Player.class)
	public Response activate(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		int playerId = ValidateUtils.requireGreaterThanZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		ValidateUtils.requireNotNull(playerId, player);

		player.setActive(true);

		return ResponseSurrogate.of(player);
	}
	
	

	/**
	 * Returns a list of all already finished goals of a specific player. 
	 * If the API key is not valid an analogous message is returned. It is also checked, if the 
	 * player id is a positive number otherwise a message for an invalid number is returned.
	 *
	 * @param id
	 *         Required path parameter as integer which uniquely identify the {@link Player}.
	 * @param apiKey
	 *         The valid query parameter API key affiliated to one specific organisation, 
	 *         to which this player belongs to.
	 * @return Response as List of FinishedGoals in JSON.
	 */
	@GET
	@Path("/{id}/goals")
	@TypeHint(FinishedGoal[].class)
	public Response getPlayerFinishedGoals(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		LOGGER.debug("getFinishedGoals requested");
		List<FinishedGoal> goals = playerDao.getPlayer(ValidateUtils.requireGreaterThanZero(id), apiKey).getFinishedGoals();

		return ResponseSurrogate.of(goals);
	}

	/**
	 * Returns a list of all already awarded rewards associated with the player of the given id.
	 * If the API key is not valid an analogous message is returned. It is also checked, if the 
	 * player id is a positive number otherwise a message for an invalid number is returned.
	 *
	 * @param id
	 *          Required path parameter as integer which uniquely identify the {@link Player}.
	 * @param apiKey
	 *         The valid query parameter API key affiliated to one specific organisation, 
	 *         to which this player belongs to.
	 * @return Response as List of PermanentRewards in JSON.
	 */
	@GET
	@Path("/{id}/rewards")
	@TypeHint(PermanentReward[].class)
	public Response getRewards(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		LOGGER.debug("getPlayerPermanentRewards requested");
		int playerId = ValidateUtils.requireGreaterThanZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		ValidateUtils.requireNotNull(playerId, player);
		
		List<PermanentReward> pRewards = player.getRewards();

		return ResponseSurrogate.of(pRewards);
	}

	/**
	 * Returns a list of all already finished tasks associated with the player of the passed 
	 * id. If the API key is not valid an analogous message is returned. It is also checked, 
	 * if the player id is a positive number otherwise a message for an invalid number is 
	 * returned.
	 * 
	 * @param id
	 *          Required path parameter as integer which uniquely identify the {@link Player}.
	 * @param apiKey
	 *         The valid query parameter API key affiliated to one specific organisation, 
	 *         to which this player belongs to.
	 * @return Response as List of FinishedTasks in JSON.
	 */
	@GET
	@Path("/{id}/tasks")
	@TypeHint(FinishedTask[].class)
	public Response getPlayerFinishedTasks(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		LOGGER.debug("getFinishedTasks requested");
		List<FinishedTask> fTasks = playerDao.getPlayer(ValidateUtils.requireGreaterThanZero(id), apiKey).getFinishedTasks();

		return ResponseSurrogate.of(fTasks);
	}

	/**
	 * Returns a list of all awarded badges associated with the player of the passed id.
	 * If the API key is not valid an analogous message is returned. It is also checked, 
	 * if the player id is a positive number otherwise a message for an invalid number is 
	 * returned.
	 *
	 * @param id
	 *          Required path parameter as integer which uniquely identify the {@link Player}.
	 * @param apiKey
	 *          The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this player belongs to.
	 * @return Response as List of Badges in JSON.
	 */
	@GET
	@Path("/{id}/badges")
	@TypeHint(Badge[].class)
	public Response getPlayerBadges(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		LOGGER.debug("get earned Badges from Player requested");
		
		int playerId = ValidateUtils.requireGreaterThanZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		ValidateUtils.requireNotNull(playerId, player);
		
		List<Badge> badges = player.getOnlyBadges();

		return ResponseSurrogate.of(badges);
	}

	/**
	 * Returns a list of all awarded achievements associated with the player of the passed 
	 * id. If the API key is not valid an analogous message is returned. It is also checked, 
	 * if the player id is a positive number otherwise a message for an invalid number is 
	 * returned.
	 *
	 * @param id
	 *          Required path parameter as integer which uniquely identify the {@link Player}.
	 * @param apiKey
	 *          The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this player belongs to.
	 * @return Response as List of Achievements in JSON
	 */
	@GET
	@Path("/{id}/achievements")
	@TypeHint(Achievement[].class)
	public Response getPlayerAchievements(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		LOGGER.debug("get earned Achievements from Player requested");
		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThanZero(id), apiKey);
		List<Achievement> achievements = player.getOnlyAchievement();

		return ResponseSurrogate.of(achievements);
	}

	/**
	 * Returns the current amount of points associated with the player of the passed id. If 
	 * the API key is not valid an analogous message is returned. It is also checked, if
	 * the player id is a positive number otherwise a message for an invalid number is 
	 * returned.
	 *
	 * @param id
	 *          Required path parameter as integer which uniquely identify the {@link Player}.
	 * @param apiKey
	 *          The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this player belongs to.
	 * @return Response of int in JSON.
	 */
	@GET
	@Path("/{id}/points")
	@TypeHint(int.class)
	public Response getPlayerPoints(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		LOGGER.debug("get earned points from Player requested");
		
		int playerId = ValidateUtils.requireGreaterThanZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		ValidateUtils.requireNotNull(playerId, player);
		
		int points = player.getPoints();

		return ResponseSurrogate.of(points);
	}

	/**
	 * Returns the current amount of coins associated with the player of the passed id. If 
	 * the API key is not valid an analogous message is returned. It is also checked, if
	 * the player id is a positive number otherwise a message for an invalid number is 
	 * returned.
	 * 
	 * @param id
	 *          Required path parameter as integer which uniquely identify the {@link Player}.
	 * @param apiKey
	 *          The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this player belongs to.
	 * @return Response of int in JSON.
	 */
	@GET
	@Path("/{id}/coins")
	@TypeHint(int.class)
	public Response getPlayerCoins(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		LOGGER.debug("get earned coins from Player requested");
		int playerId = ValidateUtils.requireGreaterThanZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		ValidateUtils.requireNotNull(playerId, player);
		
		int coins = player.getCoins();

		return ResponseSurrogate.of(coins);
	}
	
	/**
	 * Returns the field of the reference attribute of the player. 
	 * This can be used, when the player's id is known and the player should be 
	 * matched with a user of the application.
	 * If the API key is not valid an analogous message is returned. It is also checked, 
	 * if the player id is a positive number otherwise a message for an invalid number 
	 * is returned.
	 * 
	 * @param id
	 * 			The id of the player, whose reference field is returned.
	 * @param apiKey
	 * 			The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this player belongs to.
	 * @return Response of String in JSON.
	 */
	@GET
	@Path("/{id}/reference")
	@TypeHint(String.class)
	public Response getPlayerReference(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		int playerId = ValidateUtils.requireGreaterThanZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		ValidateUtils.requireNotNull(playerId, player);
		
		String reference = player.getReference();
		
		return ResponseSurrogate.of(reference);
	}
	
	/**
	 * Returns the player that matched to a specific reference field in a specific organisation.
	 * If the reference field dosen't match to a player an API Error is returned with the
	 * message that such a player dosen't exist
	 * If the API key is not valid an analogous message is returned.
	 * 
	 * @param reference
	 * 			The reference to which the player should be returned.
	 * @param apiKey
	 * 			The valid query parameter API key affiliated to one specific organisation, 
	 *          to which the player belongs to.
	 * @return Response of the player as JSON.
	 */
	@GET
	@Path("/reference")
	@TypeHint(Player.class)
	public Response getPlayerByReference(@QueryParam("reference") @NotNull String reference, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		if(!reference.equals("null")){
			try {
				Player player = playerDao.getPlayerByReference(reference, apiKey);
				return ResponseSurrogate.of(player);
			} catch (Exception e) {
				throw new ApiError(Response.Status.FORBIDDEN, "No Player with this reference exist.");
			}
		}
		throw new ApiError(Response.Status.FORBIDDEN, "No Player with this reference exist.");
	}
	
	
	/**
	 * This method can be used to get a player by reference, but only if the hashed password matches.
	 * 
	 * @param reference
	 * 			he reference to which the player should be returned.
	 * @param password
	 * 			The hashed password which can be compared with the player's one.
	 * @param apiKey
	 * 			The valid query parameter API key affiliated to one specific organisation, 
	 *          to which the player belongs to.
	 * @return Response of the player as JSON.
	 */
	@GET
	@Path("/referencePW")
	@TypeHint(Player.class)
	public Response getPlayerByReferencePW(@QueryParam("reference") @NotNull String reference, 
			@HeaderParam("pw") @NotNull String password, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		if(!reference.equals("null") && !password.equals("null")){
			try {
				Player player = playerDao.getPlayerByReference(reference, apiKey);
				if(player.getPassword().equals(password)){
					return ResponseSurrogate.of(player);
				}else{
					throw new ApiError(Response.Status.FORBIDDEN, "Credentials are wrong.");
				}
				
				
			} catch (Exception e) {
				throw new ApiError(Response.Status.FORBIDDEN, "Credentials are wrong.");
			}
		}
		throw new ApiError(Response.Status.FORBIDDEN, "Credentials are wrong.");
	}
	
	
	
	/**
	 * Gets a list of all contacts a player has.
	 * In the response the players' password and avatar isn't returned because of security 
	 * reasons respectively overhead.
	 * 
	 * @param id
	 * 			Required path parameter as integer which uniquely identify the Player.
	 * @param apiKey
	 * 			  The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this player belongs to.
	 * @return Response of all player contacts in JSON.
	 */
	@GET
	@Path("/{id}/contacts")
	@TypeHint(Player[].class)
	public Response contacts(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		LOGGER.debug("get contacts of a player");
		int playerId = ValidateUtils.requireGreaterThanZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		ValidateUtils.requireNotNull(playerId, player);
		
		List<Player> contacts = player.getContactList();

		return ResponseSurrogate.of(contacts);
	}
	
	/**
	 * Gets a list of all groups in which a player is a member. 
	 * 
	 * @param id 
	 * 			Required path parameter as integer which uniquely identify the Player.
	 * @param apiKey
	 * 			The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this player belongs to.
	 * @return Response of all groups of a player in JSON.
	 */
	@GET
	@Path("/{id}/groups")
	@TypeHint(Player[].class)
	public Response playerGroups(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		LOGGER.debug("get groups of a player");
		int playerId = ValidateUtils.requireGreaterThanZero(id);
		Player player = playerDao.getPlayer(playerId, apiKey);
		ValidateUtils.requireNotNull(playerId, player);
		
		List<PlayerGroup> allGroups = groupDao.getAllGroups(apiKey);
		List<PlayerGroup> groups = new ArrayList<>();

		for (PlayerGroup g : allGroups) {
			if (g.getPlayers().contains(player)) {
				groups.add(g);
			}
		}

		return ResponseSurrogate.of(groups);
	}
}
