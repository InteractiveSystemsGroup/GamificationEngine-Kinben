package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
import info.interactivesystems.gamificationengine.api.validation.ValidListOfDigits;
import info.interactivesystems.gamificationengine.api.validation.ValidPositiveDigit;
import info.interactivesystems.gamificationengine.dao.GoalDAO;
import info.interactivesystems.gamificationengine.dao.MarketPlaceDAO;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.dao.PlayerDAO;
import info.interactivesystems.gamificationengine.dao.PlayerGroupDAO;
import info.interactivesystems.gamificationengine.dao.RoleDAO;
import info.interactivesystems.gamificationengine.dao.RuleDAO;
import info.interactivesystems.gamificationengine.dao.TaskDAO;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.Role;
import info.interactivesystems.gamificationengine.entities.marketPlace.Bid;
import info.interactivesystems.gamificationengine.entities.marketPlace.MarketPlace;
import info.interactivesystems.gamificationengine.entities.marketPlace.Offer;
import info.interactivesystems.gamificationengine.entities.task.Task;
import info.interactivesystems.gamificationengine.utils.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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
 * API for market place related services.
 */
@Path("/marketPlace")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class MarketPlaceApi {

	private static final Logger log = LoggerFactory.getLogger(MarketPlaceApi.class);

	@Inject
	OrganisationDAO organisationDao;
	@Inject
	PlayerDAO playerDao;
	@Inject
	RoleDAO roleDao;
	@Inject
	PlayerGroupDAO groupDao;
	@Inject
	RuleDAO ruleDao;
	@Inject
	GoalDAO goalDao;
	@Inject
	TaskDAO taskDao;
	@Inject
	MarketPlaceDAO marketPlDao;

	/**
	 * Create a new market place.
	 * 
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {MarketPlace} in JSON
	 */
	@POST
	@Path("/market")
	public Response createMarketPlace(@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("create new MarketPlace");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		if (marketPlDao.getAllMarketPlaceForApiKey(apiKey).size() > 0) {
			throw new ApiError(Response.Status.FORBIDDEN, "Marketplace does already exists for your organization.");
		}

		MarketPlace marketplace = new MarketPlace();
		marketplace.setBelongsTo(organisation);

		marketPlDao.insertMarketPlace(marketplace);

		return ResponseSurrogate.created(marketplace);
	}

	/**
	 * Delete a market place.
	 * 
	 * @param marketId
	 *            required id of the market place
	 * @param apikey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link MarketPlace} in JSON
	 */
	@DELETE
	@Path("/{id}/market")
	public Response deleteMarketPlace(
			@PathParam("id") @NotNull @ValidPositiveDigit(message = "The market id must be a valid number") String marketId,
			@QueryParam("apiKey") @ValidApiKey String apikey) {

		log.debug("delete Market");
		if (marketId == null) {
			throw new ApiError(Response.Status.FORBIDDEN, "no marketPlaceId transferred");
		}

		int id = ValidateUtils.requireGreaterThenZero(marketId);

		MarketPlace market = marketPlDao.deleteMarketPlace(id);

		if (market == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No such MarketPlace: " + marketId);
		}

		return ResponseSurrogate.deleted(market);
	}

	/**
	 * Creates a new offer.
	 * 
	 * @param name
	 *            required name of the offer
	 * @param endDate
	 *            how long is the offer available on the market
	 * @param prize
	 *            required prize of the offer
	 * @param taskId
	 *            required task that should be completed
	 * @param allowedRoles
	 *            optional roles that are allowed to receive the price
	 * @param deadLine
	 *            how long is the offer valid
	 * @param marketId
	 *            required id of the market where the offer should be available
	 * @param playerId
	 *            required id of the player who created the offer
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {Offer} in JSON
	 */
	@POST
	@Path("/offer")
	public Response createNewOffer(@QueryParam("name") String name, @QueryParam("endDate") String endDate,
			@QueryParam("prize") @ValidPositiveDigit(message = "The prize must be a valid number") String prize,
			@QueryParam("taskId") @NotNull @ValidPositiveDigit(message = "The task id must be a valid number") String taskId,
			@QueryParam("roles") @DefaultValue("null") @ValidListOfDigits String allowedRoles, @QueryParam("deadLine") String deadLine,
			@QueryParam("marketId") @NotNull @ValidPositiveDigit(message = "The market id must be a valid number") String marketId,
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("create new Offer called");

		Task task = taskDao.getTask(ValidateUtils.requireGreaterThenZero(taskId));
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(playerId), apiKey);

		if (!task.isTradeable()) {
			throw new ApiError(Response.Status.FORBIDDEN, "task is not tradeable");
		}

		if (ValidateUtils.requireGreaterThenZero(prize) <= 0) {
			throw new ApiError(Response.Status.FORBIDDEN, "Please, give a real bid!");
		}

		if (!player.enoughPrize(ValidateUtils.requireGreaterThenZero(prize))) {
			throw new ApiError(Response.Status.FORBIDDEN, "Not enough coins for such an offer");
		}

		// set Timestamp for Offer
		LocalDateTime offerDate = LocalDateTime.now();

		// Parse String in LocalDateTime -> Format:"2014-12-15 12:30";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime endDateTime = LocalDateTime.parse(endDate, formatter);
		LocalDateTime deadLineTime = LocalDateTime.parse(deadLine, formatter);

		Offer offer = new Offer();
		offer.setName(name);
		offer.setBelongsTo(organisation);
		offer.setOfferDate(offerDate);
		offer.setEndDate(endDateTime);
		offer.setPrize(ValidateUtils.requireGreaterThenZero(prize));
		offer.setDeadLine(deadLineTime);
		offer.setTask(task);
		offer.setPlayer(player);

		player.setCoins(player.getCoins() - ValidateUtils.requireGreaterThenZero(prize));

		// Find all roles by Id
		String[] roleIdList = allowedRoles.split(",");

		for (String roleIdString : roleIdList) {
			log.debug("Role To Add: " + roleIdString);
			Role role = roleDao.getRoleById(ValidateUtils.requireGreaterThenZero(roleIdString));
			if (role != null) {
				log.debug("Role Added: " + role.getId());
				offer.addRole(role);
			}
		}

		offer.setBelongsTo(organisation);
		MarketPlace marketPlace = marketPlDao.getMarketPl(ValidateUtils.requireGreaterThenZero(marketId));
		marketPlace.setOffers(marketPlace.addOffer(offer));

		int marketPId = marketPlDao.insertMarketPlace(marketPlace);

		return ResponseSurrogate.created(offer);
	}

	/**
	 * Place a bid on an offer.
	 * 
	 * @param playerId
	 *            required player who adds the bid
	 * @param offerId
	 *            required offer where the bid should be placed
	 * @param prize
	 *            required prize of the bid
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {Bid} in JSON
	 */
	@POST
	@Path("/bid")
	public Response giveABid(@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("offerId") @NotNull @ValidPositiveDigit(message = "The offer id must be a valid number") String offerId,
			@QueryParam("prize") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String prize,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("create New Bid called");

		if (ValidateUtils.requireGreaterThenZero(prize) <= 0) {
			throw new ApiError(Response.Status.FORBIDDEN, "Please, give a real bid!");
		}

		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(playerId), apiKey);

		if (!player.enoughPrize(ValidateUtils.requireGreaterThenZero(prize))) {
			throw new ApiError(Response.Status.FORBIDDEN, "Not enough coins for such a bid.");
		}

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Offer offer = marketPlDao.getOffer(ValidateUtils.requireGreaterThenZero(offerId));
		List<Role> roles = offer.getAllowedForRole();

		log.debug("Offer: " + offer.getId() + " " + roles);

		log.debug("Bids:");
		for (Bid b : marketPlDao.getBidsForOffer(offer)) {
			log.debug("-" + b.getId());
		}

		List<Role> matchingRoles;

		if (offer.getAllowedForRole().size() > 0) {
			log.debug("Offer is restricted by roles");
			matchingRoles = offer.getAllowedForRole().stream().filter(r -> {
				if (player.getBelongsToRoles().contains(r)) {
					log.debug("Player has required Role to create Bid: " + r.getName());
					return true;
				} else {
					return false;
				}
			}).collect(Collectors.toList());

			if (matchingRoles.size() > 0) {
				log.debug("Roles match -> proceed");
			} else {
				log.debug("Roles don't match -> error");
				throw new ApiError(Response.Status.FORBIDDEN, "Roles don't match!");
			}
		} else {
			log.debug("Offer is not restricted by roles");
		}

		// set current Time for Bid
		LocalDateTime creationDate = LocalDateTime.now();

		Bid bid = new Bid();
		bid.setPrize(ValidateUtils.requireGreaterThenZero(prize));
		bid.setBelongsTo(organisation);
		bid.setCreationDate(creationDate);
		bid.setPlayer(player);

		marketPlDao.insertBid(bid);

		bid.setOffer(offer);

		offer.setPrize(offer.getPrize() + ValidateUtils.requireGreaterThenZero(prize));

		player.setCoins(player.getCoins() - ValidateUtils.requireGreaterThenZero(prize));

		log.debug("Bids:");
		for (Bid b : marketPlDao.getBidsForOffer(offer)) {
			log.debug("-" + b.getId());
		}

		return ResponseSurrogate.created(bid);
	}

	/**
	 * Get all offers of from a specific player.
	 * 
	 * @param playerId
	 *            required player id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {List<Offer>} in JSON
	 */
	@GET
	@Path("/getOffers")
	public Response getPlayerOffers(
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(playerId), apiKey);

		List<Offer> offers;
		offers = marketPlDao.getOffersByPlayer(player);

		for (Offer offer : offers) {
			log.debug("Player: " + player.getId() + "| Offer:" + offer.getId());
		}

		if (offers.isEmpty()) {
			throw new ApiError(Response.Status.NOT_FOUND, "There are no offers");
		}

		return ResponseSurrogate.of(offers);
	}

	/**
	 * Get the allowed offers for a player.
	 * 
	 * @param playerId
	 *            required player id
	 * @param marketPlId
	 *            required market place id
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {List<Offer>} in JSON
	 */
	@GET
	@Path("/getOfferRole")
	public Response getOffersByRole(
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("marketPlaceId") @NotNull @ValidPositiveDigit(message = "The market id must be a valid number") String marketPlId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(playerId), apiKey);

		MarketPlace market = marketPlDao.getMarketPl(ValidateUtils.requireGreaterThenZero(marketPlId));
		List<Offer> matchingOffers;

		if (market.getOffers().size() > 0) {
			matchingOffers = market.filterOfferByRole(player, player.getBelongsToRoles());

			if (matchingOffers.size() <= 0) {
				throw new ApiError(Response.Status.NOT_FOUND, "There are no offers fot this role");
			}
			return ResponseSurrogate.of(matchingOffers);
		}

		throw new ApiError(Response.Status.NOT_FOUND, "There are no offers");
	}

	/**
	 * Get all available offers for a player ordered by date, newest first.
	 * 
	 * @param playerId
	 *            required id of the player
	 * @param marketPlId
	 *            required id of the market place
	 * @param count
	 *            optional count of offers that should be returned. Default "10"
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {List<Offer>} in JSON
	 */
	@GET
	@Path("/getNewestOffer")
	public Response getNewestOffers(@QueryParam("playerId") @NotNull @ValidPositiveDigit String playerId,
			@QueryParam("marketPlaceId") @NotNull @ValidPositiveDigit(message = "The market id must be a valid number") String marketPlId,
			@QueryParam("count") @ValidPositiveDigit(message = "The count must be a valid number") @DefaultValue("10") String count,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(playerId), apiKey);

		MarketPlace market = marketPlDao.getMarketPl(ValidateUtils.requireGreaterThenZero(marketPlId));
		List<Offer> matchingOffers;

		if (market.getOffers().size() > 0) {
			matchingOffers = market.filterOfferByRole(player, player.getBelongsToRoles());

			if (matchingOffers.size() <= 0) {
				throw new ApiError(Response.Status.NOT_FOUND, "There are no offers for the player roles");
			}

			List<Offer> newestOffers = market.filterOfferByDate(matchingOffers, ValidateUtils.requireGreaterThenZero(count));

			return ResponseSurrogate.of(newestOffers);
		}

		throw new ApiError(Response.Status.NOT_FOUND, "There are no offers");
	}

	/**
	 * Get all available offers for a player ordered by prize, highest prize
	 * first.
	 * 
	 * @param playerId
	 *            required id of the player
	 * @param marketPlId
	 *            required id of the market place
	 * @param count
	 *            optional count of offers that should be returned. Default "10"
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {List<Offer>} in JSON
	 */
	@GET
	@Path("/getHighestO")
	public Response getHighestOffers(
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("marketPlaceId") @NotNull @ValidPositiveDigit(message = "The market id must be a valid number") String marketPlId,
			@QueryParam("count") @ValidPositiveDigit(message = "The count must be a valid number") @DefaultValue("10") String count,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(playerId), apiKey);

		MarketPlace market = marketPlDao.getMarketPl(ValidateUtils.requireGreaterThenZero(marketPlId));
		List<Offer> matchingOffers;

		if (market.getOffers().size() > 0) {
			matchingOffers = market.filterOfferByRole(player, player.getBelongsToRoles());

			if (matchingOffers.size() <= 0) {
				throw new ApiError(Response.Status.NOT_FOUND, "There are no offers for this role");
			}
			List<Offer> highestOffers = market.filterOfferByPrize(matchingOffers, ValidateUtils.requireGreaterThenZero(count));

			return ResponseSurrogate.of(highestOffers);
		}

		throw new ApiError(Response.Status.NOT_FOUND, "There are no offers");
	}

	/**
	 * Get a list of all bids placed on an offer.
	 * 
	 * @param offerId
	 *            required id of the offer
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link List<Bid>} in JSON
	 */
	@GET
	@Path("/{id}/bids")
	public Response getBids(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The id must be a valid number") String offerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
		// TODO apiKey setzen
		Offer offer = marketPlDao.getOffer(ValidateUtils.requireGreaterThenZero(offerId));
		List<Bid> bidsForOffer = marketPlDao.getBidsForOffer(offer);

		for (Bid bid : bidsForOffer) {
			log.debug("Bid " + bid.getId() + " für Offer: " + offer.getId());
		}

		return ResponseSurrogate.of(bidsForOffer);
	}

	// TODO testen
	/**
	 * Delete an offer.
	 * 
	 * @param offerId
	 *            required id of the offer
	 * @param apikey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Offer} in JSON
	 */
	@DELETE
	@Path("/{id}/offer")
	public Response deleteOffer(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The id must be a valid number") String offerId,
			@QueryParam("apiKey") @ValidApiKey String apikey) {

		if (offerId == null) {
			throw new ApiError(Response.Status.FORBIDDEN, "no offerId transferred");
		}

		log.debug("offerId aufgerufen");
		int id = ValidateUtils.requireGreaterThenZero(offerId);

		Offer offer = marketPlDao.getOffer(id);
		log.debug("hole Offer");

		if (!marketPlDao.getBidsForOffer(offer).isEmpty()) {
			for (Bid bid : marketPlDao.getBidsForOffer(offer)) {
				Player player = bid.getPlayer();
				player.setCoins(player.getCoins() + bid.getPrize());
				log.debug("give a bid" + player.getId());
			}
		}

		log.debug("here Offer");

		Offer deletedOffer = marketPlDao.deleteOffer(id);

		log.debug("deleted Offer");
		if (deletedOffer == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No such Offer: " + offerId);
		}

		log.debug("ende Offer");
		return ResponseSurrogate.deleted(deletedOffer);
	}

	/**
	 * Complete offer.
	 * 
	 * @param offerId
	 *            required id of the offer
	 * @param playerId
	 *            required id of the player
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Task#getId} in JSON
	 */
	@POST
	@Path("/{id}/compOffer")
	public Response completedOffer(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The id must be a valid number") String offerId,
			@QueryParam("player") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		if (offerId == null) {
			throw new ApiError(Response.Status.FORBIDDEN, "no offerId transferred");
		}

		if (playerId == null) {
			throw new ApiError(Response.Status.FORBIDDEN, "no playerId transferred");
		}

		log.debug("offerId aufgerufen");
		int id = ValidateUtils.requireGreaterThenZero(offerId);
		int idPlayer = ValidateUtils.requireGreaterThenZero(playerId);

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Player player = playerDao.getPlayer(idPlayer, apiKey);
		Offer offer = marketPlDao.getOffer(id);
		log.debug("hole Offer");

		if (offer == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No such Offer: " + offerId);
		}

		Task task = offer.getTask();
		// TODO testen
		log.debug("task" + task.getId());
		task.completeTask(organisation, player, ruleDao, goalDao, groupDao, LocalDateTime.now());
		log.debug("Task completed");

		int prizeReward = offer.getPrize();
		player.setCoins(player.getCoins() + prizeReward);

		log.debug("Preisvergabe");
		playerDao.insert(player);

		return ResponseSurrogate.created("Task " + task.getId() + "abgeschlossen.");
	}

	/**
	 * Changes the value of a given attribute.
	 * 
	 * @param offerId
	 *            required id of an offer
	 * @param attribute
	 *            required attribute name to be changed
	 * @param value
	 *            required new value
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Offer} in JSON
	 */
	@PUT
	@Path("/{id}/attributes")
	public Response changeOfferAttributes(
			@PathParam("id") @NotNull @ValidPositiveDigit(message = "The offer id must be a valid number") String offerId,
			@QueryParam("attribute") @NotNull String attribute, @QueryParam("value") @NotNull String value,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
		log.debug("change Attribute of Offer");

		Offer offer = marketPlDao.getOffer(ValidateUtils.requireGreaterThenZero(offerId));

		if ("null".equals(value)) {
			value = null;
		}

		switch (attribute) {
		case "name":
			offer.setName(value);
			break;

		case "deadline":
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			offer.setDeadLine(LocalDateTime.parse(value, formatter));
			break;

		case "enddate":
			offer.setEndDate(LocalDateTime.parse(value));
			break;

		case "playerRoles":
			changePlayerRoles(value, offer, apiKey);
			break;

		default:
			break;
		}

		marketPlDao.insertOffer(offer);

		return ResponseSurrogate.updated(offer);
	}

	private void changePlayerRoles(@NotNull String value, Offer offer, String apiKey) {
		String commaSeparatedList = StringUtils.validateAsListOfDigits(value);
		List<Integer> ids = StringUtils.stringArrayToIntegerList(commaSeparatedList);
		List<Role> roles = roleDao.getRoles(ids, apiKey);
		offer.setAllowedForRole(roles);
	}

	/**
	 * Removes a bid from the database.
	 * 
	 * @param bidId
	 *            required id of a bid
	 * @param playerId
	 *            required id of the player
	 * @param offerId
	 *            required id of the offer
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Offer} in JSON
	 */
	@DELETE
	@Path("/{id}/bid")
	public Response deleteBid(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The id must be a valid number") String bidId,
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("offerId") @NotNull @ValidPositiveDigit(message = "The offer id must be a valid number") String offerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
		if (bidId == null) {
			throw new ApiError(Response.Status.FORBIDDEN, "no bidId transferred");
		}

		// TODO testen
		int id = ValidateUtils.requireGreaterThenZero(bidId);
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(playerId), apiKey);
		Offer offer = marketPlDao.getOffer(ValidateUtils.requireGreaterThenZero(offerId));

		List<Bid> bid = marketPlDao.getBidsForPlayerAndOffer(player, offer);

		if (bid == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No such Offer: " + offerId);
		}

		return ResponseSurrogate.deleted(offer);
	}

}
