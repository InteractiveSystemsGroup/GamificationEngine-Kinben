package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
import info.interactivesystems.gamificationengine.api.validation.ValidPositiveDigit;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.dao.PlayerLevelDAO;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.PlayerLevel;

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
 * API for player level related services.
 */
@Path("/playerLevel")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class PlayerLevelApi {

	private static final Logger log = LoggerFactory.getLogger(TaskApi.class);

	@Inject
	OrganisationDAO organisationDao;
	@Inject
	PlayerLevelDAO playerLevelDao;

	/**
	 * Creates a new player level.
	 * 
	 * @param name
	 *            required level name
	 * @param index
	 *            required level number
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link PlayerLevel} in JSON
	 */
	@POST
	@Path("/")
	public Response createNewPlayerLevel(@QueryParam("levelName") @NotNull String name,
			@QueryParam("levelIndex") @NotNull @ValidPositiveDigit String index, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("createNewPlayerLevelcalled");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		PlayerLevel pL = new PlayerLevel();
		pL.setLevelName(name);
		pL.setLevelIndex(ValidateUtils.requireGreaterThenZero(index));
		pL.setBelongsTo(organisation);

		playerLevelDao.insertPlayerLevel(pL);

		return ResponseSurrogate.created(pL);
	}

	/**
	 * Returns a player level for assigned id.
	 * 
	 * @param id
	 *            required id of the level
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link PlayerLevel} in JSON
	 */
	@GET
	@Path("/{id}")
	public Response getPlayerLevel(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		PlayerLevel pL = playerLevelDao.getPlayerLevel(apiKey, ValidateUtils.requireGreaterThenZero(id));
		return ResponseSurrogate.of(pL);
	}

	/**
	 * Changes the value for a corresponding attribute key.
	 * 
	 * @param id
	 *            required level id
	 * @param attribute
	 *            required attribute key
	 * @param value
	 *            required value for associated attribute
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link PlayerLevel} in JSON
	 */
	@PUT
	@Path("/{id}/attributes")
	public Response changePlayerLevelAttributes(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("attribute") @NotNull String attribute,
			@QueryParam("value") @NotNull String value, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("change Attribute of PlayerLevel");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		int levelId = ValidateUtils.requireGreaterThenZero(id);
		PlayerLevel playerLevel = playerLevelDao.getPlayerLevel(apiKey, levelId);

		if ("null".equals(value)) {
			value = null;
		}

		// not changeable: id -> generated & belongsTo;
		switch (attribute) {
		case "levelName":
			playerLevel.setLevelName(value);
			break;

		case "levelIndex":
			playerLevel.setLevelIndex(levelId);
			break;

		default:
			break;
		}

		playerLevelDao.insertPlayerLevel(playerLevel);

		return ResponseSurrogate.updated(playerLevel);
	}

	/**
	 * Removes player level with assigned id from data base.
	 * 
	 * @param id
	 *            required level id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link PlayerLevel} in JSON
	 */
	@DELETE
	@Path("/{id}")
	public Response deletePlayerLevel(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		PlayerLevel playerLevel = playerLevelDao.deletePlayerLevel(apiKey, ValidateUtils.requireGreaterThenZero(id));
		return ResponseSurrogate.deleted(playerLevel);
	}
}
