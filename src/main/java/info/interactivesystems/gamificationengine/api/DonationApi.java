package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
import info.interactivesystems.gamificationengine.api.validation.ValidPositiveDigit;
import info.interactivesystems.gamificationengine.dao.DonationDAO;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.dao.PlayerDAO;
import info.interactivesystems.gamificationengine.entities.DonationCall;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API for donation related services.
 */
@Path("/donation")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class DonationApi {

	private static final Logger log = LoggerFactory.getLogger(DonationApi.class);

	@Inject
	OrganisationDAO organisationDao;

	@Inject
	DonationDAO donationDao;

	@Inject
	PlayerDAO playerDao;

	/**
	 * Creates a new call for a donation.
	 * 
	 * @param name
	 *            the short name of the donation call
	 * @param description
	 *            a longer description of the donation call describing its usage
	 * @param goal
	 *            an id of the goal, which should be done for this donation
	 * @param apiKey
	 *            your api key
	 * @return {@link Response} of {@link DonationCall} in JOSN
	 */
	@POST
	@Path("/")
	public Response createDonationCall(@QueryParam("name") @NotNull String name, @QueryParam("description") String description,
			@QueryParam("goal") @ValidPositiveDigit String goal, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("create New Donation Call ");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		DonationCall dCall = new DonationCall();
		dCall.setName(name);
		dCall.setBelongsTo(organisation);
		dCall.setDescription(description);
		dCall.setGoal(ValidateUtils.requireGreaterThenZero(goal));

		donationDao.insertDonationCall(dCall);

		return ResponseSurrogate.created(dCall);
	}

	/**
	 * With this method a player donates a specific amount of coins if she/he has enough. 
	 * These coins are subtracted from the player’s current account and will be added 
	 * to the Donation Call's current amount. 
	 * 
	 * @param id
	 *            the id of the call for donation
	 * @param playerId
	 *            the player who donates
	 * @param amount
	 *            the amount of coins which should be donated
	 * @param apiKey
	 *            your api key
	 * @return {@link Response} of {@link DonationCall} in JOSN
	 */

	@POST
	@Path("/{id}/donate/{playerId}")
	public Response donate(@PathParam("id") @ValidPositiveDigit(message = "The donation id must be a valid number") String dId,
			@PathParam("playerId") @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("amount") @ValidPositiveDigit(message = "The amount must be a valid number") String amount,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		int id = ValidateUtils.requireGreaterThenZero(dId);
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		DonationCall dCall = donationDao.getDonationCall(id);

		if (dCall == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No such DonationCallId: " + id);
		}

		int pId = ValidateUtils.requireGreaterThenZero(playerId);
		Player player = playerDao.getPlayer(pId, organisation.getApiKey());

		ValidateUtils.requireNotNull(pId, player);

		if (!player.enoughPrize(ValidateUtils.requireGreaterThenZero(amount))) {
			throw new ApiError(Response.Status.FORBIDDEN, "Not enough coins for such a donation.");
		}
		player.donate(dCall, ValidateUtils.requireGreaterThenZero(amount));

		return ResponseSurrogate.created(dCall);
	}

	/**
	 * Gets the {@link DonationCall} object by id.
	 * 
	 * @param dId
	 *            the id of the call for donation
	 * @param apiKey
	 *            your api key
	 * @return {@link Response} of {@link DonationCall} in JOSN
	 */
	@GET
	@Path("/{id}")
	public Response getDonationCall(@PathParam("id") @ValidPositiveDigit String dId, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		int id = ValidateUtils.requireGreaterThenZero(dId);
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		DonationCall dCall = donationDao.getDonationCall(id);

		if (dCall == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No such DonationCallId: " + id);
		}

		return ResponseSurrogate.of(dCall);
	}

	/**
	 * Deletes a call for donation.
	 * 
	 * @param id
	 *            id of the donation call that should be deleted
	 * @param apiKey
	 *            your api key
	 * @return {@link Response} of {@link DonationCall} in JOSN
	 */
	@DELETE
	@Path("/{id}")
	public Response deleteDonationCall(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		if (id == null) {
			throw new ApiError(Response.Status.FORBIDDEN, "no id transferred");
		}

		int dId = ValidateUtils.requireGreaterThenZero(id);
		DonationCall dCall = donationDao.deleteDonationCall(apiKey, dId);

		ValidateUtils.requireNotNull(dId, dCall);
		return ResponseSurrogate.deleted(dCall);
	}

}
