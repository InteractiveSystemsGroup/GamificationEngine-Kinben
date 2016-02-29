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

import com.webcohesion.enunciate.metadata.rs.TypeHint;

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
	 * Creates a new player level and generates the PlayerLevel-id. The organisation's 
	 * API key is mandatory otherwise a warning with the hint for a non valid API key 
	 * is returned. 
	 * By the creation the name and index of the player have to be passed. It is checked, 
	 * if the index of the level is a positive number otherwise a message for the 
	 * invalid number is returned. 
	 * 
	 * @param name
	 *           The name of the player level. This parameter is required.
	 * @param index
	 *           The index of the level. This parameter is required.
	 * @param apiKey
	 *          The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this player level belongs to.
	 * @return Response of PlayerLevel in JSON.
	 */
	@POST
	@Path("/")
	@TypeHint(PlayerLevel.class)
	public Response createNewPlayerLevel(@QueryParam("levelName") @NotNull String name,
			@QueryParam("levelIndex") @NotNull @ValidPositiveDigit String index, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("createNewPlayerLevelcalled");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		PlayerLevel pL = new PlayerLevel();
		pL.setLevelName(name);
		pL.setLevelIndex(ValidateUtils.requireGreaterThanZero(index));
		pL.setBelongsTo(organisation);

		playerLevelDao.insertPlayerLevel(pL);

		return ResponseSurrogate.created(pL);
	}

	/**
	 * Returns the player level associated with the passed id. If the API key is not 
	 * valid an analogous message is returned. It is also checked, if the id is a 
	 * positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param id
	 *          Required path parameter as integer which uniquely identify the {@link PlayerLevel}.
	 * @param apiKey
	 *          The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this player level belongs to.
	 * @return Response of PlayerLevel in JSON.
	 */
	@GET
	@Path("/{id}")
	@TypeHint(PlayerLevel.class)
	public Response getPlayerLevel(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		PlayerLevel pL = playerLevelDao.getPlayerLevel(ValidateUtils.requireGreaterThanZero(id), apiKey);
		return ResponseSurrogate.of(pL);
	}

	/**
	 * With this method the fields of a PlayerLevel can be changed. For this the id of the player level, 
	 * the API key of the specific organisation, the name of the field and the new value are needed.
	 * To modify the name or the index of the level the new value has to be passed with the value field. 
	 * If the API key is not valid an analogous message is returned. It is also checked, if 
	 * the ids are a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param id
	 *           The id of the player level that should be changed. This parameter is required.
	 * @param attribute
	 *            The name of the attribute which should be modified. This parameter is required. 
	 *            The following names of attributes can be used to change the 
	 *            associated field:
	 *            "levelName" and "levelIndex". 
	 * @param value
	 *            The new value of the attribute. This parameter is required.
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *           to which the player level belongs to.
	 * @return Response of PlayerLevel in JSON.
	 */
	@PUT
	@Path("/{id}/attributes")
	@TypeHint(PlayerLevel.class)
	public Response changePlayerLevelAttributes(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("attribute") @NotNull String attribute,
			@QueryParam("value") @NotNull String value, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("change Attribute of PlayerLevel");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		int levelId = ValidateUtils.requireGreaterThanZero(id);
		PlayerLevel playerLevel = playerLevelDao.getPlayerLevel(levelId, apiKey);

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
	 * Removes the layer level with the assigned id from data base. It is checked, if the passed id is a 
	 * positive number otherwise a message for an invalid number is returned. If the API key is not 
	 * valid an analogous message is returned.
	 * 
	 * @param id
	 *           Required path parameter as integer which uniquely identify the {@link PlayerLevel}.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this player level belongs to.
	 * @return Response of PlayerLevel in JSON.
	 */
	@DELETE
	@Path("/{id}")
	@TypeHint(PlayerLevel.class)
	public Response deletePlayerLevel(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		PlayerLevel playerLevel = playerLevelDao.deletePlayerLevel(ValidateUtils.requireGreaterThanZero(id), apiKey);
		return ResponseSurrogate.deleted(playerLevel);
	}
}
