package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
import info.interactivesystems.gamificationengine.api.validation.ValidPositiveDigit;
import info.interactivesystems.gamificationengine.dao.DonationDAO;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.dao.PlayerDAO;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.donationCall.Donation;
import info.interactivesystems.gamificationengine.entities.donationCall.DonationCall;
import info.interactivesystems.gamificationengine.utils.Progress;

import java.time.LocalDateTime;
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
	 * @param goalAmount
	 *            The amount of coins that should be reached to fulfil this donation.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this call for donations belongs to.
	 * @return Response of DonationCall in JSON.
	 */
	@POST
	@Path("/")
	@TypeHint(DonationCall.class)
	public Response createDonationCall(@QueryParam("name") @NotNull String name, @QueryParam("description") String description,
			@QueryParam("goalAmount") @NotNull @ValidPositiveDigit String goalAmount,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("create New Donation Call ");
		
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		DonationCall dCall = new DonationCall();
		dCall.setName(name);
		dCall.setBelongsTo(organisation);
		dCall.setDescription(description);
		dCall.setGoalAmount(ValidateUtils.requireGreaterThanZero(goalAmount));

		donationDao.insertDonationCall(dCall);

		return ResponseSurrogate.created(dCall);
	}

	/**
	 * With this method a player donates a specific amount of coins if she/he has enough coins and the given
	 * amount hasn't been already reached.  
	 * These coins are subtracted from the player's current account and will be added to the DonationCall's 
	 * current amount. If the API key is not valid an analogous message is returned.
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
	 * @return A Response of DonationCall in JSON.
	 */
	@POST
	@Path("/{id}/donate/{playerId}")
	@TypeHint(DonationCall.class)
	public Response donate(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The donation id must be a valid number") String dId,
			@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("amount") @NotNull @ValidPositiveDigit(message = "The amount must be a valid number") String amount,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		int id = ValidateUtils.requireGreaterThanZero(dId);
		DonationCall dCall = donationDao.getDonationCall(id, apiKey);
		ValidateUtils.requireNotNull(id, dCall);

		if(!dCall.checkIsReached()){
			int pId = ValidateUtils.requireGreaterThanZero(playerId);
			Player player = playerDao.getPlayer(pId, apiKey);
			ValidateUtils.requireNotNull(pId, player);
	
			int donationAmount = ValidateUtils.requireGreaterThanZero(Integer.valueOf(amount));
			
			if (!player.enoughPrize(donationAmount)) {
				throw new ApiError(Response.Status.FORBIDDEN, "Not enough coins for such a donation.");
			}
			player.donate(dCall, donationAmount);
			
			Donation donation = new Donation();
			donation.setBelongsTo(dCall.getBelongsTo());
			donation.setAmount(donationAmount);
			donation.setPlayer(player);
			donation.setCreationDate(LocalDateTime.now());
			donation.setDonationCall(dCall);
			
			donationDao.insertDonation(donation);
			
		}else{
			return ResponseSurrogate.of("Call for Donation is already completed");
		}
		return ResponseSurrogate.created(dCall);
	}

	
	/**
	 * Returns the call for donation which is associated with the passed id. If the API key is not 
	 * valid an analogous message is returned. It is also checked, if the id is a positive number 
	 * otherwise a message for an invalid number is returned.
	 * 
	 * @param dId
	 * 			Required path parameter as integer which uniquely identify the DonationCall.
	 * @param apiKey
	 * 			The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this call for donations belongs to.
	 * @return Response of DonationCall in JSON.
	 */
	@GET
	@Path("/{id}")
	@TypeHint(DonationCall.class)
	public Response getDonationCall(@PathParam("id") @NotNull @ValidPositiveDigit String dId, 
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		int id = ValidateUtils.requireGreaterThanZero(dId);
		DonationCall dCall = donationDao.getDonationCall(id, apiKey);
		ValidateUtils.requireNotNull(id, dCall);

		return ResponseSurrogate.of(dCall);
	}
	
	/**
	 * Returns the call for donation which are associated with the organisation. If the API key is not 
	 * valid an analogous message is returned. 
	 * 
	 * @param apiKey
	 * 			The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this call for donations belongs to.
	 * @return Response of List with all DonationCalls of one organisaiton in JSON.
	 */
	@GET
	@Path("/*")
	@TypeHint(DonationCall[].class)
	public Response getDonationCalls(@QueryParam("apiKey") @ValidApiKey String apiKey) {

		List<DonationCall> dCalls = donationDao.getDonationCalls(apiKey);
		return ResponseSurrogate.of(dCalls);
	}
		
	/**
	 * Returns the progress of an call for donations: the current amount and the amount that should be
	 * reached. If the API key is not valid an analogous message is returned. It is also checked, if the 
	 * id is a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param dId
	 * 			Required path parameter as integer which uniquely identify the DonationCall.
	 * @param apiKey
	 * 			 The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this call for donations belongs to.
	 * @return  Returns the current amount and the amount that should be reached in JSON.
	 */
	@GET
	@Path("/{id}/progress")
	@TypeHint(DonationCall[].class)
	public Response getProgess(@PathParam("id") @NotNull @ValidPositiveDigit String dId, 
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("get progress");
		
		int id = ValidateUtils.requireGreaterThanZero(dId);
		DonationCall dCall = donationDao.getDonationCall(id, apiKey);
		ValidateUtils.requireNotNull(id, dCall);

		Progress progress = new Progress(dCall.getCurrentAmount(), dCall.getGoalAmount());
		
		return ResponseSurrogate.of(progress);
	}
	
	/**
	 * Returns a list of all donors that have donated to a specific call for donoation. If the API key is 
	 * not valid an analogous message is returned. It is also checked, if the id is a positive number 
	 * otherwise a message for an invalid number is returned.
	 * 
	 * @param dId
	 * 			 The id of the call for donations to which all donors should be returned.
	 * @param apiKey
	 * 			The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this call for donations belongs to.
	 * @return Returns a list of all donors of a specific call for donations in JSON.
	 */
	@GET
	@Path("/{id}/donors")
	@TypeHint(DonationCall[].class)
	public Response getDonors(@PathParam("id") @NotNull @ValidPositiveDigit String dId, 
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		int id = ValidateUtils.requireGreaterThanZero(dId);
		DonationCall dCall = donationDao.getDonationCall(id, apiKey);
		ValidateUtils.requireNotNull(id, dCall);

		List<Player> donors = dCall.getDonors();
		
		return ResponseSurrogate.of(donors);
	}
	
	/**
	 * Gets a list of all donations which were made for a specific call for donations. 
	 * If the API key is not valid an analogous message is returned. It is also checked, if the offer id is a 
	 * positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param donationCallId
	 *           The call for Donation whose donations are returned. This parameter is required.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the call for donations belongs to.
	 * @return Response as List of Donations in JSON.
	 */
	@GET
	@Path("/{id}/donations")
	@TypeHint(Donation[].class)
	public Response getDonations(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The id must be a valid number") String donationCallId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
	
		int dId = ValidateUtils.requireGreaterThanZero(donationCallId);
		DonationCall dCall = donationDao.getDonationCall(dId, apiKey); 
		ValidateUtils.requireNotNull(dId, dCall);
		
		List<Donation> donations = donationDao.getDonationsForDonationCall(dCall, apiKey); 

		for (Donation donation : donations) {
			log.debug("Donation " + donation.getId() + " with "+ donation.getAmount() + "coins for: " + dCall.getId());
		}

		return ResponseSurrogate.of(donations);
	}
	
	/**
	 * With this method the fields of one specific call for donations can be changed. 
	 * For this the associated id, the API key of the specific organisation, the 
	 * name of the field and the new field's value are needed. 
	 * To modify the name or the description of a call for donation, the new value 
	 * can be passed.  
	 * If the API key is not valid an analogous message is returned. It is 
	 * also checked, if the id is a positive number otherwise a message for 
	 * an invalid number is returned.
	 * 
	 * @param id
	 *            Required integer which uniquely identify the DonationCall.
	 * @param attribute
	 *            The name of the attribute which should be modified. This 
	 *            parameter is required. The following names of attributes can 
	 *            be used to change the associated field:
	 *            "name" and "description"
	 * @param value
	 *            The new value of the attribute. This parameter is required.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this call for donations belongs to.
	 * @return Response of DonationCall in JSON.
	 */
	@PUT
	@Path("/{id}/attributes")
	@TypeHint(DonationCall.class)
	public Response changeAttributes(@PathParam("id") @NotNull @ValidPositiveDigit String id, 
			@QueryParam("attribute") String attribute,
			@QueryParam("value") String value, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		log.debug("change Attribute of Player");

		int dCallId = ValidateUtils.requireGreaterThanZero(id);
		DonationCall dCall = donationDao.getDonationCall(dCallId, apiKey); 
		ValidateUtils.requireNotNull(dCallId, dCall);

		// not: id -> generated & belongsTo -> fixed
		switch (attribute) {
		case "name":
			dCall.setName(value);
			break;

		case "description":
			dCall.setDescription(value);
			break;


		default:
			break;
		}

		donationDao.insertDonationCall(dCall);
		return ResponseSurrogate.updated(dCall);
	}

	/**
	 * Removes the call for donations with the assigned id from data base. If the goal of call for donations isn't
	 * reached, first all donors get their donations back and then in a second step the call for donation itself is 
	 * removed from the database. Else if the goal is already reached, the call for donation is deleted.
	 * It is checked, if the passed id is a positive number otherwise a message for an invalid number is returned.
	 * If the API key is not valid an analogous message is returned.
	 * 
	 * @param id
	 *          Required path parameter as integer which uniquely identify the {@link DonationCall} that
	 *          should be deleted.
	 * @param apiKey
	 *          The valid query parameter API key affiliated to one specific organisation, 
	 *          to which this call for donations belongs to.
	 * @return  Response of DonationCall in JSON.
	 */
	@DELETE
	@Path("/{id}")
	@TypeHint(DonationCall.class)
	public Response deleteDonationCall(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		int dId = ValidateUtils.requireGreaterThanZero(id);
		DonationCall dCall = donationDao.getDonationCall(dId, apiKey);
		ValidateUtils.requireNotNull(dId, dCall);
		
		if(!dCall.isGoalReached()){
			List<Donation> donations = donationDao.getDonationsForDonationCall(dCall, apiKey);
			if(!donations.isEmpty()){
				for (Donation donation : donations) {
					Player player = donation.getPlayer();
					player.setCoins(player.getCoins() + donation.getAmount());
					donationDao.deleteDonation(donation);
				}
			}
		}

		dCall = donationDao.deleteDonationCall(dId, apiKey);
		
		return ResponseSurrogate.deleted(dCall);
	}

}
