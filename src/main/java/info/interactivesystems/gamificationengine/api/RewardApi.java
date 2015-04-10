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

/**
 * API for rewards related services.
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
	 * Returns a list of all reward associated with api key.
	 * 
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link List<Reward>} in JSON
	 */
	@GET
	@Path("/*")
	public Response getRewards(@QueryParam("apiKey") @ValidApiKey String apiKey) {

		List<Reward> reward = rewardDao.getRewards(apiKey);
		return ResponseSurrogate.of(reward);
	}

	/**
	 * Returns a reward for assigned id.
	 * 
	 * @param id
	 *            required reward id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Reward} in JSON
	 */
	@GET
	@Path("/{id}")
	public Response getReward(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		int rewardId = ValidateUtils.requireGreaterThenZero(id);
		Reward reward = rewardDao.getRewardByIdAndOrganisation(rewardId, organisation);

		ValidateUtils.requireNotNull(rewardId, reward);
		return ResponseSurrogate.of(reward);
	}

	/**
	 * Create a new reward.
	 * 
	 * @param type
	 *            required type of the reward. A list of available reward types
	 *            can be received by {@link RewardApi#getRewardTypes}
	 * @param name
	 *            a string that represents the name of the reward
	 * @param amount
	 *            if you use points or coins type add the amount of them
	 * @param url
	 *            the url to an image if you use any kind of badge or award
	 * @param description
	 *            a text to describe your reward
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Reward} in JSON
	 */
	@POST
	@Path("/")
	public Response createNewReward(@QueryParam("type") @NotNull String type, @QueryParam("name") String name,
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
			return createCoinsReward(amount, apiKey);
		case "Points":
			return createPointReward(amount, apiKey);
		case "ReceiveLevel":
			return createReceiveLevel(amount, name, apiKey);

		default:
			reward = new Achievement();
			break;
		}

		reward.setBelongsTo(organisation);
		rewardDao.insertReward(reward);

		return ResponseSurrogate.created(reward);
	}

	/**
	 * Delete a specific reward.
	 * 
	 * @param id
	 *            required reward id which should be deleted
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Reward} with 200 OK and JSON as
	 *         response type
	 */
	@DELETE
	@Path("/{id}")
	public Response deleteReward(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		if (id == null) {
			throw new ApiError(Response.Status.FORBIDDEN, "no rewardId transferred");
		}

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		int rewardId = ValidateUtils.requireGreaterThenZero(id);
		Reward reward = rewardDao.deleteRewardByIdAndOrganisation(rewardId, organisation);

		ValidateUtils.requireNotNull(rewardId, reward);
		return ResponseSurrogate.deleted(reward);
	}

	/**
	 * Retruns a list of all available reward types associated with an api key.
	 * 
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Reward} with 200 OK and JSON as
	 *         response type
	 */
	@GET
	@Path("/types")
	public Response getRewardTypes(@QueryParam("apiKey") @ValidApiKey String apiKey) {
		List<String> rewards = Arrays.asList("Achievement", "Badge", "Coins", "Points", "ReceiveLevel");
		return ResponseSurrogate.of(rewards);
	}

	/**
	 * Create a reward of type achievement.
	 * 
	 * @param name
	 *            rewuired name of the achievement
	 * @param description
	 *            a short text that describes the achievement
	 * @param url
	 *            the url to an image that represents the achievement
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Achievement} in JSON
	 */
	@POST
	@Path("/achievement")
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
	 * Create a reward of type badge.
	 * 
	 * @param name
	 *            required name of the badge
	 * @param description
	 *            a short text that describes the badge
	 * @param url
	 *            the url to an image that represents the badge
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Badge} in JSON
	 */
	@POST
	@Path("/badge")
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
	 * Creates a new coins reward.
	 * 
	 * @param amount
	 *            required amount of coins greater then zero
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Coins} in JSON
	 */
	@POST
	@Path("/coins")
	public Response createCoinsReward(@QueryParam("amount") @NotNull @ValidPositiveDigit String amount,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("create Coins Reward called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Coins reward = new Coins();
		reward.setAmount(ValidateUtils.requireGreaterThenZero(amount));

		reward.setBelongsTo(organisation);
		rewardDao.insertReward(reward);

		return ResponseSurrogate.created(reward);
	}

	/**
	 * Created a new point reward.
	 * 
	 * @param amount
	 *            required amount of points greater then zero
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Reward} in JSON
	 */
	@POST
	@Path("/points")
	public Response createPointReward(@QueryParam("amount") @NotNull @ValidPositiveDigit String amount,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("create Point Reward called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Points reward = new Points();
		reward.setAmount(ValidateUtils.requireGreaterThenZero(amount));

		reward.setBelongsTo(organisation);
		rewardDao.insertReward(reward);

		return ResponseSurrogate.created(reward);
	}

	/**
	 * Creates a new level reward.
	 * 
	 * @param index
	 *            required level index greater then zero
	 * @param name
	 *            optional name of the level
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link ReceiveLevel} in JSON
	 */
	@POST
	@Path("/level")
	public Response createReceiveLevel(@QueryParam("amount") @NotNull @ValidPositiveDigit String index, @QueryParam("name") String name,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("create ReceiveLevel Reward called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		ReceiveLevel reward = new ReceiveLevel();
		reward.setLevelIndex((ValidateUtils.requireGreaterThenZero(index)));
		reward.setLevelLabel(name);

		reward.setBelongsTo(organisation);
		rewardDao.insertReward(reward);

		return ResponseSurrogate.created(reward);
	}

	/**
	 * Returns an achievement icon.
	 * 
	 * @param rewardId
	 *            required reward id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Object} in JSON
	 */
	@GET
	@Path("/achievement/{id}")
	public Response getAchievementIcon(@PathParam("id") @NotNull @ValidPositiveDigit String rewardId, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("getAchievement called");

		Reward reward = rewardDao.getReward(ValidateUtils.requireGreaterThenZero(rewardId));

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
	 * Returns a badge icon.
	 * 
	 * @param rewardId
	 *            required reward id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Reward} in JSON
	 */
	@GET
	@Path("/badge/{id}")
	public Response getBadgeIcon(@PathParam("id") @NotNull @ValidPositiveDigit String rewardId, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("getBadge called");

		Reward reward = rewardDao.getReward(ValidateUtils.requireGreaterThenZero(rewardId));

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

	// PUTs

	/**
	 * Changes attribute values of an achievement. Changeable attributes are
	 * name, description, and icon.
	 * 
	 * @param rewardId
	 *            required reward id
	 * @param attribute
	 *            required key
	 * @param value
	 *            required value for the attribute
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Reward} in JSON
	 */
	@PUT
	@Path("/changeAchievement")
	public Response changeAchievement(@QueryParam("id") @NotNull @ValidPositiveDigit String rewardId,
			@QueryParam("attribute") @NotNull String attribute, @QueryParam("value") @NotNull String value,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("change " + attribute + "of Achivement in " + value);

		if (!organisationDao.checkApiKey(apiKey)) {
			return Response.status(Response.Status.FORBIDDEN).entity("No such apiKey: " + apiKey).build();
		}

		Reward reward = rewardDao.getReward(ValidateUtils.requireGreaterThenZero(rewardId));

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
			throw new ApiError(Response.Status.BAD_REQUEST, "The transfered id is not a achievement");
		}

		return ResponseSurrogate.updated(reward);
	}

	/**
	 * Changes badge attribute values. Changeable attributes are name,
	 * description, and icon.
	 * 
	 * @param rewardId
	 *            required reward id
	 * @param attribute
	 *            required attribute key
	 * @param value
	 *            required value for the associated attribute
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Reward} in JSON
	 */
	@PUT
	@Path("/changeBadge")
	public Response changeBadge(@QueryParam("id") @NotNull @ValidPositiveDigit String rewardId, @QueryParam("attribute") @NotNull String attribute,
			@QueryParam("value") @NotNull String value, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("change " + attribute + "of Badge in " + value);

		if (!organisationDao.checkApiKey(apiKey)) {
			return Response.status(Response.Status.FORBIDDEN).entity("No such apiKey: " + apiKey).build();
		}

		Reward reward = rewardDao.getReward(ValidateUtils.requireGreaterThenZero(rewardId));

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
			throw new ApiError(Response.Status.BAD_REQUEST, "The transfered id is not a badge");
		}

		return ResponseSurrogate.updated(reward);
	}

	/**
	 * Changes points attribute values. Changeable attribute is amount.
	 * 
	 * @param rewardId
	 *            required reward id
	 * @param attribute
	 *            required attribute key
	 * @param value
	 *            required value for the associated attribute
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Reward} in JSON
	 */
	@PUT
	@Path("/Points")
	public Response changePointReward(@QueryParam("id") @NotNull @ValidPositiveDigit String rewardId,
			@QueryParam("attribute") @NotNull String attribute, @QueryParam("value") @NotNull String value,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("change " + attribute + "of Points in " + value);

		if (!organisationDao.checkApiKey(apiKey)) {
			return Response.status(Response.Status.FORBIDDEN).entity("No such apiKey: " + apiKey).build();
		}

		Reward reward = rewardDao.getReward(ValidateUtils.requireGreaterThenZero(rewardId));

		if ("null".equals(value) || value != null && value.isEmpty()) {
			value = null;
		}
		if (reward instanceof Points) {
			switch (attribute) {
			case "amount":
				((Points) reward).setAmount(ValidateUtils.requireGreaterThenZero(value));

			}
		} else {
			throw new ApiError(Response.Status.BAD_REQUEST, "The transfered id is not a point");
		}

		return ResponseSurrogate.updated(reward);
	}

	/**
	 * Changes level attribute values. Changeable attributes are amount and
	 * name.
	 * 
	 * @param rewardId
	 *            required reward id
	 * @param attribute
	 *            required attribute key
	 * @param value
	 *            required value for the associated attribute
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Reward} in JSON
	 */
	@PUT
	@Path("/ReceiveLevel")
	public Response changeLevel(@QueryParam("id") @NotNull @ValidPositiveDigit String rewardId, @QueryParam("attribute") String attribute,
			@QueryParam("value") String value, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("change " + attribute + "of ReceiveLwvwl in " + value);

		if (!organisationDao.checkApiKey(apiKey)) {
			return Response.status(Response.Status.FORBIDDEN).entity("No such apiKey: " + apiKey).build();
		}

		Reward reward = rewardDao.getReward(ValidateUtils.requireGreaterThenZero(rewardId));

		if ("null".equals(value) || value != null && value.isEmpty()) {
			value = null;
		}
		if (reward instanceof ReceiveLevel) {
			switch (attribute) {
			case "amount":
				((ReceiveLevel) reward).setLevelIndex(ValidateUtils.requireGreaterThenZero(value));

			case "name":
				((ReceiveLevel) reward).setLevelLabel(value);

			}
		} else {
			throw new ApiError(Response.Status.BAD_REQUEST, "The transfered id is not a point");
		}

		return ResponseSurrogate.updated(reward);
	}

}
