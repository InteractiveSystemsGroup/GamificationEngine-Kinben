package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
import info.interactivesystems.gamificationengine.api.validation.ValidPositiveDigit;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.dao.RewardDAO;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.rewards.Achievement;
import info.interactivesystems.gamificationengine.entities.rewards.Badge;
import info.interactivesystems.gamificationengine.entities.rewards.Coins;
import info.interactivesystems.gamificationengine.entities.rewards.Points;
import info.interactivesystems.gamificationengine.entities.rewards.ReceiveLevel;
import info.interactivesystems.gamificationengine.entities.rewards.Reward;
import info.interactivesystems.gamificationengine.utils.ImageUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
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

import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * A Reward will be awarded in dependent of the goal for a group or one person after 
 * a person fulfilled a task. A reward can be a permanent reward like a badge or achievement 
 * which can be obtained only once or a volatile reward such as coins, points or a 
 * particular level which can be earned several times and so can be changed by getting for 
 * example more coins or decrease the coins by giving a bid for an offer in the marketplace. 
 * Ancillary all possible types for creating a reward, all already created rewards for one 
 * particular organisation or with the associated id only one specific reward can be 
 * requested. 
 * For all rewards the name and description can be changed after they have been created. 
 * Dependent on the reward has an image or an amount of points respectively coins these 
 * attributes also can be changed. 
 */
@Path("/reward")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class RewardApi {

	private static final Logger log = LoggerFactory.getLogger(RewardApi.class);

	@Inject
	OrganisationDAO organisationDao;
	@Inject
	RewardDAO rewardDao;

	/**
	 * Returns a list of all rewards associated with the passed API key and so all rewards 
	 * which belong to a specific organisation. If the API key is not valid an analogous 
	 * message is returned. 
	 * 
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *           to which this reward belongs to.
	 * @return {@link Response} as {@link List} of {@link Reward}s in JSON.
	 */
	@GET
	@Path("/*")
	@TypeHint(Reward[].class)
	public Response getRewards(@QueryParam("apiKey") @ValidApiKey String apiKey) {

		List<Reward> reward = rewardDao.getRewards(apiKey);
		return ResponseSurrogate.of(reward);
	}

	/**
	 * This method returns one specific player who is identified by the passed id and the 
	 * API key. If the API key is not valid an analogous message is returned. It is also 
	 * checked, if the id is a positive number otherwise a message for an invalid number 
	 * is returned.
	 * 
	 * @param id
	 *           Required integer which uniquely identify the {@link Reward}.
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *           to which this reward belongs to.
	 * @return {@link Response} of {@link Reward} in JSON.
	 */
	@GET
	@Path("/{id}")
	@TypeHint(Reward.class)
	public Response getReward(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		int rewardId = ValidateUtils.requireGreaterThanZero(id);
		Reward reward = rewardDao.getRewardByIdAndOrganisation(rewardId, organisation);

		ValidateUtils.requireNotNull(rewardId, reward);
		return ResponseSurrogate.of(reward);
	}

	/**
	 * Creates a new reward and so the method generates the reward-id. The organisation's API
	 * key is mandatory otherwise a warning with the hint for a non valid API key is returned.
	 * It has to be defined which type of reward should be created, its name, description and
	 * respectively it is a permanent or a volatile reward an URL for the icon or the amount 
	 * of coins or points.
	 * 
	 * @param type
	 *          The required type of the reward. A list of available reward types
	 *            can be received by {@link RewardApi#getRewardTypes}.
	 * @param name
	 *           A string that represents the name of the reward.
	 * @param amount
	 *           If the type is a volatile reward like points or coins this parameter is used
	 *           to represent their amount. 	 
	 * @param url
	 *           If the type is a permanent reward like a badge or achievement this parameter
	 *           represents the URL of the associated image. 
	 * @param description
	 *           Optionally the description of the reward as String.
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *           to which this reward belongs to.
	 * @return {@link Response} of {@link Reward} in JSON.
	 */
	@POST
	@Path("/")
	@TypeHint(Reward.class)
	public Response createNewReward(@QueryParam("type") @NotNull String type, @QueryParam("name") @NotNull String name,
			@QueryParam("amount") @ValidPositiveDigit String amount, @QueryParam("icon") String url, @QueryParam("description") String description,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		log.debug("createNewReward called");

		log.debug("apiKey: " + apiKey);
		log.debug("type: " + type);
		log.debug("name: " + name);
		log.debug("amount: " + amount);
		log.debug("icon: " + url);
		log.debug("description: " + description);

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Reward reward;

		switch (type) {
		case "Achievement":
			return createAchievement(name, description, url, apiKey);
		case "Badge":
			return createBadge(name, description, url, apiKey);
		case "Coins":
			return createCoinsReward(name, description, amount, apiKey);
		case "Points":
			return createPointReward(name, description, amount, apiKey);
		case "ReceiveLevel":
			return createReceiveLevel(name, description, amount, apiKey);

		default:
			reward = new Achievement();
			break;
		}

		reward.setBelongsTo(organisation);
		rewardDao.insertReward(reward);

		return ResponseSurrogate.created(reward);
	}

	/**
	 * Removes a specific reward from the data base which is identified by the passed id and 
	 * the API key. If the API key is not valid an analogous message is returned. It is also 
	 * checked, if the id is a positive number otherwise a message for an invalid number is 
	 * returned. 
	 * 
	 * @param id
	 *          Required integer which uniquely identify the {@link Reward} which sould be
	 *          deleted.
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *           to which this reward belongs to.
	 * @return {@link Response} of {@link Reward} with 200 OK and JSON as
	 *         response type.
	 */
	@DELETE
	@Path("/{id}")
	@TypeHint(Reward.class)
	public Response deleteReward(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		if (id == null) {
			throw new ApiError(Response.Status.FORBIDDEN, "no rewardId transferred");
		}

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		int rewardId = ValidateUtils.requireGreaterThanZero(id);
		Reward reward = rewardDao.deleteRewardByIdAndOrganisation(rewardId, organisation);

		ValidateUtils.requireNotNull(rewardId, reward);
		return ResponseSurrogate.deleted(reward);
	}

	/**
	 * Returns a list of all available reward types associated with an API key that can
	 * can created. If the API key is not valid an analogous message is returned. 
	 * 
	 * @param apiKey
	 *          The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this reward belongs to.
	 * @return {@link Response} of {@link Reward} with 200 OK and JSON as
	 *         response type.
	 */
	@GET
	@Path("/types")
	@TypeHint(String[].class)
	public Response getRewardTypes(@QueryParam("apiKey") @ValidApiKey String apiKey) {
		List<String> rewards = Arrays.asList("Achievement", "Badge", "Coins", "Points", "ReceiveLevel");
		return ResponseSurrogate.of(rewards);
	}

	/**
	 * Creates a new reward of type achievement so the method generates its reward-id. The
	 * organisation's API key is mandatory otherwise a warning with the hint for a non valid 
	 * API key is returned. Optionally the URL for an icon can be passed and a description for 
	 * the achievement.
	 * 
	 * @param name
	 *           The required name of the achievement.
	 * @param description
	 *            Optionally a short text can be set to describe the achievement.
	 * @param url
	 *            Optionally the URL of an image can be set that is associated with the 
	 *            achievement.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this reward belongs to.
	 * @return {@link Response} of {@link Achievement} in JSON.
	 */
	@POST
	@Path("/achievement")
	@TypeHint(Achievement.class)
	public Response createAchievement(@QueryParam("name") @NotNull String name, @QueryParam("description") String description,
			@QueryParam("url") String url, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		log.debug("create Achievement called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Achievement reward = new Achievement();
		reward.setName(name);
		reward.setDescription(description);

		if (url != null) {
			try {
				URL icon = new URL(url);
				reward.setIcon(icon);
				reward.setImageIcon(ImageUtils.imageToByte(url));
			} catch (MalformedURLException e) {
				throw new ApiError(Response.Status.FORBIDDEN, "no valid url was transferred");
			}
		}

		reward.setBelongsTo(organisation);
		rewardDao.insertReward(reward);

		return ResponseSurrogate.created(reward);
	}

	/**
	 * Creates a new reward of type badge so the method generates its reward-id. The
	 * organisation's API key is mandatory otherwise a warning with the hint for a non valid 
	 * API key is returned. Optionally the URL for an icon can be passed and a description for
	 * the badge.
	 * 
	 * @param name
	 *            The required name of the badge.
	 * @param description
	 *           Optionally a short text can be set to describe the badge.
	 * @param url
	 *           Optionally the URL of an image can be set that is associated with the badge.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this reward belongs to.
	 * @return {@link Response} of {@link Badge} in JSON.
	 */
	@POST
	@Path("/badge")
	@TypeHint(Badge.class)
	public Response createBadge(@QueryParam("name") @NotNull String name, @QueryParam("description") String description,
			@QueryParam("url") String url, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		log.debug("create Badge called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Badge reward = new Badge();
		reward.setName(name);
		reward.setDescription(description);

		if (url != null) {
			try {
				URL icon = new URL(url);
				reward.setIcon(icon);
				reward.setImageIcon(ImageUtils.imageToByte(url));
			} catch (MalformedURLException e) {
				throw new ApiError(Response.Status.FORBIDDEN, "no valid url was transferred");
			}
		}

		reward.setBelongsTo(organisation);
		rewardDao.insertReward(reward);

		return ResponseSurrogate.created(reward);
	}

	/**
	 * Creates a new reward of type coins so the method generates its reward-id. The
	 * organisation's API key is mandatory otherwise a warning with the hint for a non valid 
	 * API key is returned.  
	 * 
	 * @param name
	 *           The required name of the coins reward.
	 * @param description
	 *           Optionally a short text can be set to describe the reward.
	 * @param amount
	 *          The required amount of coins greater then zero that can be earned.
	 * @param apiKey
	 *          The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this reward belongs to.
	 * @return {@link Response} of {@link Coins} in JSON.
	 */
	@POST
	@Path("/coins")
	@TypeHint(Coins.class)
	public Response createCoinsReward(@QueryParam("name") @NotNull String name, 
			@QueryParam("description") String description,
			@QueryParam("amount") @NotNull @ValidPositiveDigit String amount,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		log.debug("create Coins Reward called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Coins reward = new Coins();
		reward.setName(name);
		reward.setDescription(description);
		reward.setAmount(ValidateUtils.requireGreaterThanZero(amount));
		
		reward.setBelongsTo(organisation);
		rewardDao.insertReward(reward);

		return ResponseSurrogate.created(reward);
	}

	/**
	 * Creates a new reward of type points so the method generates its reward-id. The
	 * organisation's API key is mandatory otherwise a warning with the hint for a non valid 
	 * API key is returned. 
	 * 
	 * @param name
	 *           The required name of the points reward.
	 * @param description
	 *           Optionally a short text can be set to describe the reward.
	 * @param amount
	 *           The required amount of points greater then zero that can be earned.
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *           to which this reward belongs to.
	 * @return {@link Response} of {@link Reward} in JSON.
	 */
	@POST
	@Path("/points")
	@TypeHint(Points.class)
	public Response createPointReward(@QueryParam("name") @NotNull String name, 
			@QueryParam("description") String description,
			@QueryParam("amount") @NotNull @ValidPositiveDigit String amount,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("create Point Reward called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Points reward = new Points();
		reward.setName(name);
		reward.setDescription(description);
		reward.setAmount(ValidateUtils.requireGreaterThanZero(amount));

		reward.setBelongsTo(organisation);
		rewardDao.insertReward(reward);

		return ResponseSurrogate.created(reward);
	}

	/**
	 * Creates a new reward of type level so the method generates its reward-id. A level can 
	 * be a number or a status like novice or expert in the area of specific tasks. The
	 * organisation's API key is mandatory otherwise a warning with the hint for a non valid 
	 * API key is returned. 
	 * 
	 *  @param name
	 *           The required name of the level can be set.
	 * @param description
	 *           Optionally a short text can be set to describe the level.
	 * @param index
	 *            The required level index greater then zero.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this reward belongs to.
	 * @return {@link Response} of {@link ReceiveLevel} in JSON.
	 */
	@POST
	@Path("/level")
	@TypeHint(ReceiveLevel.class)
	public Response createReceiveLevel(@QueryParam("name") @NotNull String name, @QueryParam("description") String description,
			@QueryParam("amount") @NotNull @ValidPositiveDigit String index,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("create ReceiveLevel Reward called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		ReceiveLevel reward = new ReceiveLevel();
		reward.setLevelIndex((ValidateUtils.requireGreaterThanZero(index)));
		reward.setLevelLabel(name);
		reward.setDescription(description);

		reward.setBelongsTo(organisation);
		rewardDao.insertReward(reward);

		return ResponseSurrogate.created(reward);
	}

	/**
	 * This method returns the icon of an specific achievement for example to show it the
	 * player who has just earned it. If the API key is not valid an analogous message is 
	 * returned. It is also checked, if the id is a positive number otherwise a message for 
	 * an invalid number is returned. 
	 * 
	 * @param rewardId
	 *            The required reward id.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this reward belongs to.
	 * @return {@link Response} of {@link Object} in JSON.
	 */
	@GET
	@Path("/achievement/{id}")
	@TypeHint(byte[].class)
	public Response getAchievementIcon(@PathParam("id") @NotNull @ValidPositiveDigit String rewardId, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("getAchievement called");

		Reward reward = rewardDao.getReward(ValidateUtils.requireGreaterThanZero(rewardId));

		if (!(reward instanceof Achievement)) {
			throw new ApiError(Response.Status.NOT_FOUND, "No such Achievement: " + reward);
		}
		byte[] image = ((Achievement) reward).getImageIcon();
		// Image img;
		// ImageIcon icon2 = new ImageIcon(image);
		// img = icon2.getImage();

		return ResponseSurrogate.of(new Object() {
			public byte[] bits = image;
			// public Image image = img;
		});

	}

	/**
	 * This method returns the icon of an specific badge for example to show it the player 
	 * who has just earned it. If the API key is not valid an analogous message is returned. 
	 * It is also checked, if the id is a positive number otherwise a message for an invalid
	 * number is returned. 
	 * 
	 * @param rewardId
	 *            The required reward id.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this reward belongs to.
	 * @return {@link Response} of {@link Reward} in JSON.
	 */
	@GET
	@Path("/badge/{id}")
	@TypeHint(byte[].class)
	public Response getBadgeIcon(@PathParam("id") @NotNull @ValidPositiveDigit String rewardId, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("getBadge called");

		Reward reward = rewardDao.getReward(ValidateUtils.requireGreaterThanZero(rewardId));

		if (!(reward instanceof Badge)) {
			throw new ApiError(Response.Status.NOT_FOUND, "No such Badge: " + reward);
		}
		byte[] image = ((Badge) reward).getImageIcon();
		// Image img;
		// ImageIcon icon2 = new ImageIcon(image);
		// img = icon2.getImage();

		return ResponseSurrogate.of(new Object() {
			public byte[] bits = image;
			// public Image image = img;
		});

	}

	/**
	 * With this method the fields of one specific achievement can be changed. For this the 
	 * reward id, the API key of the specific organisation, the name of the field and the 
	 * new field's value are needed.  
	 * To modify the name and description the new string has to be transfered with the attribute 
	 * field. For a new icon the path of the new image is needed in the attribute parameter. 
	 * The format of the image has to be .jpg or .png. 
	 * If the API key is not valid an analogous message is returned. It is also checked, if 
	 * the id is a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param rewardId
	 *            Required integer which uniquely identify the {@link Reward}.
	 * @param attribute
	 *            The name of the attribute which should be modified. This parameter is required. 
	 *            The following names of attributes can be used to change the associated field:
	 *            "name", "description" and "icon".
	 * @param value
	 *            The new value of the attribute. This parameter is required.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this reward belongs to.
	 * @return {@link Response} of {@link Reward} in JSON.
	 */
	@PUT
	@Path("/{id}/changeAchievement")
	@TypeHint(Reward.class)
	public Response changeAchievement(@PathParam("id") @NotNull @ValidPositiveDigit String rewardId,
			@QueryParam("attribute") @NotNull String attribute, @QueryParam("value") @NotNull String value,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("change " + attribute + "of Achivement in " + value);

		if (!organisationDao.checkApiKey(apiKey)) {
			return Response.status(Response.Status.FORBIDDEN).entity("No such apiKey: " + apiKey).build();
		}

		Reward reward = rewardDao.getReward(ValidateUtils.requireGreaterThanZero(rewardId));

		if (value.isEmpty()) {
			value = null;
		}
		if (reward instanceof Achievement) {
			switch (attribute) {
			case "name":
				((Achievement) reward).setName(value);
				break;

			case "description":
				((Achievement) reward).setDescription(value);
				break;

			case "icon":
				((Achievement) reward).setImageIcon(ImageUtils.imageToByte(value));
				break;
			}
		} else {
			throw new ApiError(Response.Status.BAD_REQUEST, "The transfered id does not belong to an achievement");
		}

		return ResponseSurrogate.updated(reward);
	}

	
	
	/**
	 * With this method the fields of one specific badge can be changed. For this the 
	 * reward id, the API key of the specific organisation, the name of the field and the 
	 * new field's value are needed.  
	 * To modify the name and description the new string has to be transfered with the attribute 
	 * field. For a new icon the path of the new image is needed in the attribute parameter. 
	 * The format of the image has to be .jpg or .png. 
	 * If the API key is not valid an analogous message is returned. It is also checked, if 
	 * the id is a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param rewardId
	 *            Required integer which uniquely identify the {@link Reward}.
	 * @param attribute
	 *            The name of the attribute which should be modified. This parameter is required.
	 *            The following names of attributes can be used to change the associated field:
	 *            "name", "description" and "icon".
	 * @param value
	 *            The new value of the attribute. This parameter is required.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this reward belongs to.
	 * @return {@link Response} of {@link Reward} in JSON.
	 */
	@PUT
	@Path("/{id}/changeBadge")
	@TypeHint(Reward.class)
	public Response changeBadge(@PathParam("id") @NotNull @ValidPositiveDigit String rewardId, @QueryParam("attribute") @NotNull String attribute,
			@QueryParam("value") @NotNull String value, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("change " + attribute + "of Badge in " + value);

		if (!organisationDao.checkApiKey(apiKey)) {
			return Response.status(Response.Status.FORBIDDEN).entity("No such apiKey: " + apiKey).build();
		}

		Reward reward = rewardDao.getReward(ValidateUtils.requireGreaterThanZero(rewardId));

		if ("null".equals(value) || value != null && value.isEmpty()) {
			value = null;
		}
		if (reward instanceof Badge) {
			switch (attribute) {
			case "name":
				((Badge) reward).setName(value);
				break;

			case "description":
				((Badge) reward).setDescription(value);
				break;

			case "icon":
				((Badge) reward).setImageIcon(ImageUtils.imageToByte(value));
				break;
			}
		} else {
			throw new ApiError(Response.Status.BAD_REQUEST, "The transfered id does not belong to a badge.");
		}

		return ResponseSurrogate.updated(reward);
	}

	/**
	 * With this method the fields of one specific point reward can be changed. For this the 
	 * reward id, the API key of the specific organisation, the name of the field and the 
	 * new field's value are needed.  
	 * To modify the amount of points the new amount has to be transfered with the attribute
	 * field. 
	 * If the API key is not valid an analogous message is returned. It is also checked, if 
	 * the id is a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param rewardId
	 *            Required integer which uniquely identify the {@link Reward}.
	 * @param attribute
	 *            The name of the attribute which should be modified. This parameter is required. 
	 *            The following names of attributes can be used to change the associated field:
	 *            "amount".
	 * @param value
	 *            The new value of the attribute. This parameter is required.
	 * @param apiKey
	 *             The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this reward belongs to.
	 * @return {@link Response} of {@link Reward} in JSON.
	 */
	@PUT
	@Path("/{id}/Points")
	@TypeHint(Reward.class)
	public Response changePointReward(@PathParam("id") @NotNull @ValidPositiveDigit String rewardId,
			@QueryParam("attribute") @NotNull String attribute, @QueryParam("value") @NotNull String value,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("change " + attribute + "of points in " + value);

		if (!organisationDao.checkApiKey(apiKey)) {
			return Response.status(Response.Status.FORBIDDEN).entity("No such apiKey: " + apiKey).build();
		}

		Reward reward = rewardDao.getReward(ValidateUtils.requireGreaterThanZero(rewardId));

		if ("null".equals(value) || value != null && value.isEmpty()) {
			value = null;
		}
		if (reward instanceof Points) {
			switch (attribute) {
			case "amount":
				((Points) reward).setAmount(ValidateUtils.requireGreaterThanZero(value));

			}
		} else {
			throw new ApiError(Response.Status.BAD_REQUEST, "The transfered id does not belong to a point");
		}

		return ResponseSurrogate.updated(reward);
	}
	
	
	/**
	 * With this method the fields of one specific coin reward can be changed. For this the 
	 * reward id, the API key of the specific organisation, the name of the field and the 
	 * new field's value are needed.  
	 * To modify the amount of coins the new amount has to be transfered with the attribute
	 * field. 
	 * If the API key is not valid an analogous message is returned. It is also checked, if 
	 * the id is a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param rewardId
	 *            Required integer which uniquely identify the {@link Reward}.
	 * @param attribute
	 *            The name of the attribute which should be modified. This parameter is required. 
	 *            The following names of attributes can be used to change the associated field:
	 *            "amount".
	 * @param value
	 *            The new value of the attribute. This parameter is required.
	 * @param apiKey
	 *             The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this reward belongs to.
	 * @return {@link Response} of {@link Reward} in JSON.
	 */
	@PUT
	@Path("/{id}/Coins")
	@TypeHint(Reward.class)
	public Response changeCoinsReward(@PathParam("id") @NotNull @ValidPositiveDigit String rewardId,
			@QueryParam("attribute") @NotNull String attribute, @QueryParam("value") @NotNull String value,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("change " + attribute + "of coins in " + value);

		if (!organisationDao.checkApiKey(apiKey)) {
			return Response.status(Response.Status.FORBIDDEN).entity("No such apiKey: " + apiKey).build();
		}

		Reward reward = rewardDao.getReward(ValidateUtils.requireGreaterThanZero(rewardId));

		if ("null".equals(value) || value != null && value.isEmpty()) {
			value = null;
		}
		if (reward instanceof Coins) {
			switch (attribute) {
			case "amount":
				((Coins) reward).setAmount(ValidateUtils.requireGreaterThanZero(value));

			}
		} else {
			throw new ApiError(Response.Status.BAD_REQUEST, "The transfered id does not belong to a coins reward");
		}

		return ResponseSurrogate.updated(reward);
	}
	

	/**
	 * With this method the fields of one specific level reward can be changed. For this the 
	 * reward id, the API key of the specific organisation, the name of the field and the 
	 * new field's value are needed.  
	 * To modify the name and amount of the level the new name respectively amount has to be 
	 * transfered with the attribute field. 
	 * If the API key is not valid an analogous message is returned. It is also checked, if 
	 * the id is a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param rewardId
	 *            Required integer which uniquely identify the {@link Reward}.
	 * @param attribute
	 *            The name of the attribute which should be modified. This parameter is required.
	 *            The following names of attributes can be used to change the associated field:
	 *            "amount" and "name". 
	 * @param value
	 *            The new value of the attribute. This parameter is required.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this reward belongs to.
	 * @return {@link Response} of {@link Reward} in JSON.
	 */
	@PUT
	@Path("/{id}/ReceiveLevel")
	@TypeHint(Reward.class)
	public Response changeLevel(@PathParam("id") @NotNull @ValidPositiveDigit String rewardId, @QueryParam("attribute") String attribute,
			@QueryParam("value") String value, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("change " + attribute + "of ReceiveLevel in " + value);

		if (!organisationDao.checkApiKey(apiKey)) {
			return Response.status(Response.Status.FORBIDDEN).entity("No such apiKey: " + apiKey).build();
		}

		Reward reward = rewardDao.getReward(ValidateUtils.requireGreaterThanZero(rewardId));

		if ("null".equals(value) || value != null && value.isEmpty()) {
			value = null;
		}
		if (reward instanceof ReceiveLevel) {
			switch (attribute) {
			case "amount":
				((ReceiveLevel) reward).setLevelIndex(ValidateUtils.requireGreaterThanZero(value));

			case "name":
				((ReceiveLevel) reward).setLevelLabel(value);

			}
		} else {
			throw new ApiError(Response.Status.BAD_REQUEST, "The transfered id does not belong to a level");
		}

		return ResponseSurrogate.updated(reward);
	}

}
