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

import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * A donation stands for a real world purpose. This could be for example a real donation for a 
 * charitable purpose or an event for the organisation’s employees like the arrangement for the 
 * company party or purchasing a new coffee machine. When a donation is created, players can pool 
 * coins for a certain amount and the connected purpose if she/he has enough coins. If the required 
 * amount is reached, the goal is reached and the purpose can be implemented by the responsible 
 * manager.
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
	 * Creates a new call for donations and generates the DonationCall-id. The organisation's API key is
	 * mandatory otherwise a warning with the hint for a non valid API key is returned. 
	 * By the creation the name and description are passed who should be assigned to this call for donation.
	 * The goal of the call for donations also has be specified. This goal repesents the amount of coins that 
	 * should be reached.
	 * 
	 * @param name
	 *           The short name of the call for donation.
	 * @param description
	 *            The longer description of the call for donation. This can contain the its purpose.
	 * @param goal
	 *            The amount of coins that should be reached to fulfil this donation.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this call for donations belongs to.
	 * @return {@link Response} of {@link DonationCall} in JSON.
	 */
	@POST
	@Path("/")
	@TypeHint(DonationCall.class)
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
	 * With this method a player donates a specific amount of coins if she/he has enough coins. 
	 * These coins are subtracted from the player's current account and will be added to the Donation
	 * Call's current amount. If the API key is not valid an analogous message is returned.
	 * It is also checked, if the id is a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param dId
	 *            The id of the call for donations to which a player donates. This path parameter is required.
	 * @param playerId
	 *            The id of the player who donates. This path parameter is required.
	 * @param amount
	 *            The amount of coins which the player donates. 
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this call for donations belongs to.
	 * @return {@link Response} of {@link DonationCall} in JSON.
	 */
	@POST
	@Path("/{id}/donate/{playerId}")
	@TypeHint(DonationCall.class)
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
	 * Returns the call for donation which is associated with the passed id. If the API key is not 
	 * valid an analogous message is returned. It is also checked, if the id is a positive number 
	 * otherwise a message for an invalid number is returned.
	 * 
	 * @param dId
	 * 			Required path parameter as integer which uniquely identify the {@link DonationCall}.
	 * @param apiKey
	 * 			The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this call for donations belongs to.
	 * @return {@link Response} of {@link DonationCall} in JSON.
	 */
	@GET
	@Path("/{id}")
	@TypeHint(DonationCall.class)
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
	 * Removes the call for donations with the assigned id from data base. It is checked, if the passed id is a 
	 * positive number otherwise a message for an invalid number is returned. If the API key is not 
	 * valid an analogous message is returned.
	 * 
	 * @param id
	 *          Required path parameter as integer which uniquely identify the {@link DonationCall} that
	 *          should be deleted.
	 * @param apiKey
	 *          The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this call for donations belongs to.
	 * @return {@link Response} of {@link DonationCall} in JSON.
	 */
	@DELETE
	@Path("/{id}")
	@TypeHint(DonationCall.class)
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
