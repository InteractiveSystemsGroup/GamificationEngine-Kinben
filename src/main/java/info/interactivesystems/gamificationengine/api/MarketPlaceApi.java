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

import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * The marketplace gives players the opportunity to offer tasks that have to be completed by their colleagues 
 * so that they are able to fulfil those tasks and obtain the respective reward. Upon creation of a task, an 
 * initial bid in terms of coins is set, which will be obtained as additional reward. Via Bids this initial bid
 * can be raised. To be able to create offers, a marketplace for the organisation is needed. If none exists yet,
 * it first has to be created. 
 * 
 * If an offer is created an initial bid in terms of coins is set which is obtained by the person who completes 
 * it. The initial bid can be raised by other colleagues in order to increase the incentive of fulfilling the 
 * task. When a player has completed a Task that belongs to an offer, she/he will obtain all bids as a reward. 
 * The particular task is then also added to the player’s list of the finished tasks. All offers a player has 
 * put on the marketplace can be requested. The name of an offer can be changed at a later point of time as 
 * well as the optional date when an offer ends or the deadline when the associated task of an offer should be
 * done at the latest.
 * 
 * At the marketplace not all offers may are visible for each player because the offers can be filtered by the 
 * roles a player has. It can also additionally filtered by the date an offer was created or the prize which 
 * can be earned. By making a bid, the reward of coins for completing a task is raised. The bidden amount of 
 * coins will be subtracted from the bidder’s current account and will be added to the offer’s current prize. 
 * Each player can make several bids on condition that her/his coins are enough otherwise the bid cannot be 
 * made. It is also possible to get all bids that was made for an offer.
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
	 * Creates a new market place for an organisation which is identified by the API key. So the method generates the
	 * marketplace-id. But this is only possible if no marketplace exists yet. 
	 * The organisation's API key is mandatory otherwise a warning with the hint for a non valid API key is 
	 * returned. 
	 * 
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *           to which this marketplace belongs to.
	 * @return {@link Response} of {MarketPlace} in JSON.
	 */
	@POST
	@Path("/market")
	@TypeHint(MarketPlace.class)
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
	 * Removes the marketplace with the assigned id from data base. It is checked, if the passed id is a 
	 * positive number otherwise a message for an invalid number is returned. If the API key is not 
	 * valid an analogous message is returned.
	 * 
	 * @param marketId
	 *             Required path parameter as integer which uniquely identify the {@link MarketPlace}.
	 * @param apikey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this goal belongs to.
	 * @return {@link Response} of {@link MarketPlace} in JSON.
	 */
	@DELETE
	@Path("/{id}/market")
	@TypeHint(MarketPlace.class)
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
	 * Creates a new group of offer and so the method generates the offer-id. The organisation's API key is 
	 * mandatory otherwise a warning with the hint for a non valid API key is returned. 
	 * By the creation the name, the prize which represents the initial bid and id of the task the offer 
	 * is associated should be passed. 
	 * Additionally the id of the player who creates the offer and the id of the marketplace should be passed. 
	 * Optionally it can be defined when the offer ends and until the task should be done. 
	 *
	 * @param name
	 *            The name of the offer. This parameter is required.
	 * @param endDate
	 *            The date and time how long the offer is available on the market. The format of the values is
	 *            yyyy-MM-dd HH:mm.
	 * @param prize
	 *            The initial bid of the offer. This is the prize a player can earn.
	 * @param taskId
	 *            The id of the task the offer is associated with. This parameter is required.
	 * @param allowedRoles
	 *            Optionally the roles can be passed to indicate who is allowed to fulfil the task and earn the
	 *            prize. 
	 * @param deadLine
	 *            The point of time until the offer is valid. The format of the values is
	 *            yyyy-MM-dd HH:mm.
	 * @param marketId
	 *            The id of the marketplace where the offer should be available.
	 * @param playerId
	 *            The id of the player who created the offer. This parameter is required.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this offer belongs to.
	 * @return {@link Response} of {Offer} in JSON.
	 */
	@POST
	@Path("/offer")
	@TypeHint(Offer.class)
	public Response createNewOffer(@QueryParam("name") @NotNull String name, @QueryParam("endDate") String endDate,
			@QueryParam("prize") @ValidPositiveDigit(message = "The prize must be a valid number") String prize,
			@QueryParam("taskId") @NotNull @ValidPositiveDigit(message = "The task id must be a valid number") String taskId,
			@QueryParam("roleIds") @DefaultValue("null") @ValidListOfDigits String allowedRoles, @QueryParam("deadLine") String deadLine,
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
	 * With this method a player makes a bid to an offer. So a new bid is created and therefore an id is 
	 * generated. The id of the player is needed to indicate who has made the bid and id of the offer to identify 
	 * for which she/he has bidden. The prize of the bid is needed to add it to the current amount of coins so 
	 * the offer's prize is raised.
	 * 
	 * @param playerId
	 *            The player who has done the bid. This parameter is required.
	 * @param offerId
	 *            The offer the player has bidden for. This parameter is required.
	 * @param prize
	 *            The amount of the bid. This is added to the current prize. This parameter is required.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the player belongs to.
	 * @return {@link Response} of {Bid} in JSON.
	 */
	@POST
	@Path("/bid")
	@TypeHint(Bid.class)
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
	 * Gets all offers a specific player has created. If the API key is not valid an analogous message is 
	 * returned. It is also checked, if the id is a positive number otherwise a message for an invalid 
	 * number is returned.
	 * 
	 * @param playerId
	 *            The player whose offers are returned.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the player belongs to.
	 * @return A {@link Response} as {@link List} of {@link Offer}s in JSON.
	 */
	@GET
	@Path("/getOffers")
	@TypeHint(Offer[].class)
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
	 * Gets the allowed offers for a specific player. The player is identified by her/his passed id and API key.
	 * The offers are filtered by the roles a player has so only offers are in the returned list which are 
	 * associated with at least one role a player has. 
	 * If the API key is not valid an analogous message is returned. It is also checked, if the ids are a 
	 * positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param playerId
	 *            The player whose roles are checked. This parameter is required.
	 * @param marketPlId
	 *            The marketplace whose offers are filtered.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this player belongs to.
	 * @return A {@link Response} as {@link List} of {@link Offer}s in JSON.
	 */
	@GET
	@Path("/getOfferRole")
	@TypeHint(Offer[].class)
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
	 * Gets all available offers for a player ordered by date, recent first.
	 * If the API key is not valid an analogous message is returned. It is also checked, if the player id is
	 * a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param playerId
	 *            The player whose roles are checked. This parameter is required.
	 * @param marketPlId
	 *            The marketplace whose offers are filtered.
	 * @param count
	 *            Optionally the count of offers that should be returned can be passed. The default value is 10. 
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the player belongs to.
	 * @return {@link Response} as {@link List} of {@link Offer}s in JSON.
	 */
	@GET
	@Path("/getRecentOffers")
	@TypeHint(Offer[].class)
	public Response getRecentOffers(@QueryParam("playerId") @NotNull @ValidPositiveDigit String playerId,
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

			List<Offer> recentOffers = market.filterOfferByDate(matchingOffers, ValidateUtils.requireGreaterThenZero(count));

			return ResponseSurrogate.of(recentOffers);
		}

		throw new ApiError(Response.Status.NOT_FOUND, "There are no offers");
	}

	/**
	 * Gets all available offers for a player ordered by prize, highest prize first.
	 * If the API key is not valid an analogous message is returned. It is also checked, if the player id is 
	 * a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param playerId
	 *            The player whose roles are checked. This parameter is required.
	 * @param marketPlId
	 *            The marketplace whose offers are filtered.
	 * @param count
	 *            Optionally the count of offers that should be returned can be passed. The default value is 10. 
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the player belongs to.
	 * @return {@link Response} as {@link List} of {@link Offer}s in JSON.
	 */
	@GET
	@Path("/getHighestOffers")
	@TypeHint(Offer[].class)
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
			List<Offer> highestOffers = market.filterOffersByPrize(matchingOffers, ValidateUtils.requireGreaterThenZero(count));

			return ResponseSurrogate.of(highestOffers);
		}

		throw new ApiError(Response.Status.NOT_FOUND, "There are no offers");
	}

	/**
	 * Gets a list of all bids which was made for a specific offer. 
	 * If the API key is not valid an analogous message is returned. It is also checked, if the offer id is a 
	 * positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param offerId
	 *           The offer whose bids are returned. This parameter is required.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the offer belongs to.
	 * @return {@link Response} as {@link List} of {@link Bid}s in JSON.
	 */
	@GET
	@Path("/{id}/bids")
	@TypeHint(Bid[].class)
	public Response getBids(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The id must be a valid number") String offerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
	
		Offer offer = marketPlDao.getOffer(ValidateUtils.requireGreaterThenZero(offerId));
		List<Bid> bidsForOffer = marketPlDao.getBidsForOffer(offer);

		for (Bid bid : bidsForOffer) {
			log.debug("Bid " + bid.getId() + " für Offer: " + offer.getId());
		}

		return ResponseSurrogate.of(bidsForOffer);
	}

	/**
	 * Removes the offer with the assigned id from the data base. It is checked, if the passed id is a 
	 * positive number otherwise a message for an invalid number is returned. If the API key is not 
	 * valid an analogous message is returned.
	 * 
	 * @param offerId
	 *            The offer which should be removed. This parameter is required. 
	 * @param apikey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the offer belongs to.
	 * @return {@link Response} of {@link Offer} in JSON.
	 */
	@DELETE
	@Path("/{id}/offer")
	@TypeHint(Offer.class)
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
	 * If an offer is fulfilled by a player, this request can be used to complete the offer. With this request 
	 * the player with the passed id represents the player who fulfilled the task so she/he gets the prize as a 
	 * reward. 
	 * It is checked, if all passed ids are a positive number otherwise a message for an invalid number is 
	 * returned. If the API key is not valid an analogous message is returned.
	 * 
	 * @param offerId
	 *            The offer id which was finished. This parameter is required. 
	 * @param playerId
	 *            The id of the player who has completed the task that was associated with the offer. So 
	 *            she/he earns the prize as a reward. 
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this player belongs to.
	 * @return {@link Response} of {@link Task#getId} in JSON.
	 */
	@POST
	@Path("/{id}/compOffer")
	@TypeHint(Task.class)
	public Response completeOffer(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The id must be a valid number") String offerId,
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
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
	 * With this method the fields of an Offer can be changed. For this the id of the offer, the API key of 
	 * the specific organisation, the name of the field and the new value are needed.
	 * 
	 * To modify the name the new String has to be passed with the attribute field. A new date and time as 
	 * LocalDateTime for the deadline or enddate can also be passed. The format of these values is 
	 * yyyy-MM-dd HH:mm. A new list of players can be passed when their ids are separated by commas. 
	 * If the API key is not valid an analogous message is returned. It is also checked, if 
	 * the ids are a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param offerId
	 *            The id of the offer that should be changed. This parameter is required.
	 * @param attribute
	 *            The name of the attribute which should be modified. This parameter is required. 
	 *            The following names of attributes can be used to change the associated field:
	 *            "name", "deadline", "enddate" and "playerRoles".
	 * @param value
	 *            The new value of the attribute. This parameter is required.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the offer belongs to.
	 * @return {@link Response} of {@link Offer} in JSON.
	 */
	@PUT
	@Path("/{id}/attributes")
	@TypeHint(Offer.class)
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

	/**
	 * This method converts the string of role ids which are transfered to a list of roles.
	 * These roles are then set as the new list of an offer's roles to indicate who is allowed to complete
	 * the associated task. 
	 * 
	 * @param value
	 * 		   	The new values of roles as string separated by commas. This parameter is required.
	 * @param offer
	 * 		  	The offer which field of roles will be modified. This parameter should be not null. 
	 * @param apiKey
	 *   	   	The valid query parameter API key affiliated to one specific organisation, 
	 *        	to which this offer belongs to.
	 */
	private void changePlayerRoles(@NotNull String value, Offer offer, String apiKey) {
		String commaSeparatedList = StringUtils.validateAsListOfDigits(value);
		List<Integer> ids = StringUtils.stringArrayToIntegerList(commaSeparatedList);
		List<Role> roles = roleDao.getRoles(ids, apiKey);
		offer.setAllowedForRole(roles);
	}

	/**
	 * Removes the bid with the assigned id from data base. It is checked, if the passed id is a 
	 * positive number otherwise a message for an invalid number is returned. If the API key is not 
	 * valid an analogous message is returned.
	 * If the API key is not valid an analogous message is returned. It is also checked, if the ids are
	 * a positive number otherwise a message for an invalid number is returned.
	 * 
	 * 
	 * @param bidId
	 *            Required path parameter as integer to uniquely identify the bid. 
	 * @param playerId
	 *            The id of the player who has made the bid. This parameter is required.
	 * @param offerId
	 *            The id of the offer to which the bid is associated.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the player belongs to.
	 * @return {@link Response} of {@link Offer} in JSON.
	 */
	@DELETE
	@Path("/{id}/bid")
	@TypeHint(Offer.class)
	public Response deleteBid(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The id must be a valid number") String bidId,
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("offerId") @NotNull @ValidPositiveDigit(message = "The offer id must be a valid number") String offerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
		if (bidId == null) {
			throw new ApiError(Response.Status.FORBIDDEN, "no bidId transferred");
		}

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
