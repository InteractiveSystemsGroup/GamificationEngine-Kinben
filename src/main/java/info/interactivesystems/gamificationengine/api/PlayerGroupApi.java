package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
import info.interactivesystems.gamificationengine.api.validation.ValidListOfDigits;
import info.interactivesystems.gamificationengine.api.validation.ValidPositiveDigit;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.dao.PlayerDAO;
import info.interactivesystems.gamificationengine.dao.PlayerGroupDAO;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.PlayerGroup;
import info.interactivesystems.gamificationengine.utils.ImageUtils;
import info.interactivesystems.gamificationengine.utils.StringUtils;

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

/**
 * API for player group related services.
 */
@Path("/playerGroup")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class PlayerGroupApi {

	private static final Logger log = LoggerFactory.getLogger(GoalApi.class);

	@Inject
	OrganisationDAO organisationDao;
	@Inject
	PlayerGroupDAO groupDao;
	@Inject
	PlayerDAO playerDao;

	/**
	 * Creates a new group of players.
	 *
	 * @param playerIds
	 *            required list of player ids
	 * @param groupName
	 *            required name of the group
	 * @param logoPath
	 *            optional a group logo as a HTTP reference
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link PlayerGroup} in JSON
	 */
	@POST
	@Path("/")
	public Response createNewGroup(@QueryParam("playerIds") @NotNull @ValidListOfDigits String playerIds,
			@QueryParam("name") @NotNull String groupName, @QueryParam("logoPath") String logoPath, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("created new Group");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		PlayerGroup group = new PlayerGroup();

		group.setName(groupName);

		// Find all Players by Id
		String[] playerIdList = playerIds.split(",");
		List<Player> players = new ArrayList<>();

		for (String playerIdString : playerIdList) {
			log.debug("Player To Add: " + playerIdString);
			Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(playerIdString), apiKey);
			if (player != null) {
				log.debug("Player added: " + player.getId());
				players.add(player);
			}
		}

		group.setPlayers(players);
		group.setBelongsTo(organisation);
		if (logoPath != null) {
			group.setGroupLogo(ImageUtils.imageToByte(logoPath));
		}

		// persist Group
		groupDao.insertGroup(group);

		return ResponseSurrogate.created(group);
	}

	/**
	 * Returns a group for assigned id.
	 *
	 * @param id
	 *            required group id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link PlayerGroup} in JSON
	 */
	@GET
	@Path("/{id}")
	public Response getPlayerGroup(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		PlayerGroup group = groupDao.getPlayerGroupByIdAndOrganisation(ValidateUtils.requireGreaterThenZero(id), organisation);

		if (group == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No such PlayerGroup: " + id);
		}

		return ResponseSurrogate.of(group);
	}

	/**
	 * Changes values for assogned attribute keys.
	 *
	 * @param id
	 *            required group id
	 * @param attribute
	 *            required attribute key
	 * @param value
	 *            required value for the associated attribute
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link PlayerGroup} in JSON
	 */
	@PUT
	@Path("/{id}/attributes")
	public Response changePlayerGroupAttributes(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("attribute") @NotNull String attribute,
			@QueryParam("value") @NotNull String value, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("change Attribute of PlayerGroup");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		PlayerGroup plGroup = groupDao.getPlayerGroupByIdAndOrganisation(ValidateUtils.requireGreaterThenZero(id), organisation);

		if ("null".equals(value)) {
			value = null;
		}

		// not changeable: id -> generated & belongsTo;
		switch (attribute) {

		case "name":
			plGroup.setName(value);
			break;

		case "playerIds":
			changePlayerIds(value, plGroup, apiKey);
			break;

		case "logo":
			plGroup.setGroupLogo(ImageUtils.imageToByte(value));
			break;

		case "points":
			plGroup.setPoints(ValidateUtils.requireGreaterThenZero(value));
			break;
		}

		groupDao.insertGroup(plGroup);

		return ResponseSurrogate.updated(plGroup);
	}

	private void changePlayerIds(@NotNull String value, PlayerGroup plGroup, String apiKey) {
		String commaSeparatedList = StringUtils.validateAsListOfDigits(value);
		List<Integer> ids = StringUtils.stringArrayToIntegerList(commaSeparatedList);
		List<Player> players = playerDao.getPlayers(ids, apiKey);
		plGroup.setPlayers(players);
	}

	/**
	 * Removes a group with assigned id from data base.
	 *
	 * @param id
	 *            required group id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link PlayerGroup} in JSON
	 */
	@DELETE
	@Path("/{id}")
	public Response deletePlayerGroup(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		if (id == null) {
			throw new ApiError(Response.Status.FORBIDDEN, "no GroupId transferred");
		}

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		PlayerGroup plGroup = groupDao.deletePlayerGroupByIdAndOrganisation(ValidateUtils.requireGreaterThenZero(id), organisation);

		if (plGroup == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No such PlayerGroup: " + plGroup);
		}

		return ResponseSurrogate.deleted(plGroup);
	}

}
