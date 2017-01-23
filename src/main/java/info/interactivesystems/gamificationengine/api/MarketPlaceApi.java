package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
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
import info.interactivesystems.gamificationengine.utils.LocalDateTimeUtil;
import info.interactivesystems.gamificationengine.utils.OfferMarketPlace;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(MarketPlaceApi.class);

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
	 * @return Response of MarketPlace in JSON.
	 */
	@POST
	@Path("/market")
	@TypeHint(MarketPlace.class)
	public Response createMarketPlace(@QueryParam("apiKey") @ValidApiKey String apiKey) {

		LOGGER.debug("create new MarketPlace");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

//		if (marketPlDao.getAllMarketPlaceForApiKey(apiKey).size() > 0) {
//			throw new ApiError(Response.Status.FORBIDDEN, "Marketplace does already exists for your organization.");
//		}

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
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this goal belongs to.
	 * @return Response of MarketPlace in JSON.
	 */
	@DELETE
	@Path("/{id}/market")
	@TypeHint(MarketPlace.class)
	public Response deleteMarketPlace(
			@PathParam("id") @NotNull @ValidPositiveDigit(message = "The market id must be a valid number") String marketId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		LOGGER.debug("delete Market");
//		if (marketId == null) {
//			throw new ApiError(Response.Status.FORBIDDEN, "no marketPlaceId transferred");
//		}

		int id = ValidateUtils.requireGreaterThanZero(marketId);
		MarketPlace market = marketPlDao.deleteMarketPlace(id, apiKey);
		ValidateUtils.requireNotNull(id, market);

		return ResponseSurrogate.deleted(market);
	}

	/**
	 * Gets all marketplaces of an organisation with all current offers.
	 * 
	 * @param apiKey
	 * 			 The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this goal belongs to.
	 * @return Return of all MarketPlaces as JSON.
	 */
	@GET
	@Path("/markets/*")
	@TypeHint(MarketPlace[].class)
	public Response getAllMarketPlaces(@QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		List<MarketPlace> markets = marketPlDao.getAllMarketPlaces(apiKey);
		
		for (MarketPlace m : markets) {
			LOGGER.debug("| MarketPlace:" + m.getId());
		}

		return ResponseSurrogate.of(markets);
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
	 * @return Response of Offer in JSON.
	 */
	@POST
	@Path("/offer")
	@TypeHint(Offer.class)
	public Response createOffer(@QueryParam("name") @NotNull String name, 
			@QueryParam("endDate") String endDate,
			@QueryParam("prize") @NotNull @ValidPositiveDigit(message = "The prize must be a valid number") String prize,
			@QueryParam("taskId") @NotNull @ValidPositiveDigit(message = "The task id must be a valid number") String taskId,
			@QueryParam("deadLine") String deadLine,
			@QueryParam("marketId") @NotNull @ValidPositiveDigit(message = "The market id must be a valid number") String marketId,
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		LOGGER.debug("create new Offer called");

		Task task = taskDao.getTask(ValidateUtils.requireGreaterThanZero(taskId), apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(taskId), task);
		
		if (!task.isTradeable()) {
			throw new ApiError(Response.Status.FORBIDDEN, "Task is not tradeable.");
		}
		
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		
		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThanZero(playerId), apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(playerId), player);

		if (!player.enoughPrize(ValidateUtils.requireGreaterThanZero(prize))) {
			throw new ApiError(Response.Status.FORBIDDEN, "Not enough coins for such an offer.");
		}
		
		if (ValidateUtils.requireGreaterThanZero(prize) <= 0) {
			throw new ApiError(Response.Status.FORBIDDEN, "Please, give a bid greater than 0.");
		}

		MarketPlace marketPlace = marketPlDao.getMarketplace(ValidateUtils.requireGreaterThanZero(marketId), apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(marketId), marketPlace);
		
		List<Offer> oldOffers = marketPlace.getOffers();
		for(Offer oldOffer : oldOffers){
			if(oldOffer.getTask().equals(task)){
				throw new ApiError(Response.Status.FORBIDDEN, "This task is already an offer on the marketplace.");
			}
		}
		
		Offer offer = new Offer();
		offer.setName(name);
		offer.setBelongsTo(organisation);
		offer.setOfferDate(LocalDateTime.now());
		offer.setPrize(ValidateUtils.requireGreaterThanZero(prize));
		offer.setTask(task);
		offer.setPlayer(player);
		if(endDate!=null){
			LocalDateTime endTime = LocalDateTimeUtil.formatDateAndTime(endDate);
			if(endTime.isAfter(LocalDateTime.now())){
				offer.setEndDate(endTime);
			}else {
				throw new ApiError(Response.Status.FORBIDDEN, "The endTime has to be in the future.");
			}
		}
		if(deadLine!=null){
			LocalDateTime deadLineTime = LocalDateTimeUtil.formatDateAndTime(deadLine);
			if(deadLineTime.isAfter(LocalDateTime.now())){
				offer.setDeadLine(deadLineTime);
			} else {
				throw new ApiError(Response.Status.FORBIDDEN, "The deadline has to be in the future.");
			}
		}

		LOGGER.debug("Offer created  ");
		player.setCoins(player.getCoins() - ValidateUtils.requireGreaterThanZero(prize));

		LOGGER.debug("Prize: " + player.getCoins());
		
		marketPlace.addOffer(offer);
		marketPlDao.insertOffer(offer);
		
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
	 * @return Response of Bid in JSON.
	 */
	@POST
	@Path("/{playerId}/bid/{offerId}")
	@TypeHint(Bid.class)
	public Response giveABid(@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@PathParam("offerId") @NotNull @ValidPositiveDigit(message = "The offer id must be a valid number") String offerId,
			@QueryParam("prize") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String prize,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		LOGGER.debug("create new Bid called");

		if (ValidateUtils.requireGreaterThanZero(prize) <= 0) {
			throw new ApiError(Response.Status.FORBIDDEN, "Please, give a bid greater than 0.");
		}

		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThanZero(playerId), apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(playerId), player);

		if (!player.enoughPrize(ValidateUtils.requireGreaterThanZero(prize))) {
			throw new ApiError(Response.Status.FORBIDDEN, "Not enough coins for such a bid.");
		}

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Offer offer = marketPlDao.getOffer(ValidateUtils.requireGreaterThanZero(offerId), apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(offerId), offer);
		
		LOGGER.debug("Bids:");
		for (Bid b : marketPlDao.getBidsForOffer(offer, apiKey)) {
			LOGGER.debug("-" + b.getId());
		}

		Bid bid = new Bid();
		bid.setPrize(ValidateUtils.requireGreaterThanZero(prize));
		bid.setBelongsTo(organisation);
		bid.setCreationDate(LocalDateTime.now());		//Set current date and time for bid
		bid.setPlayer(player);

		marketPlDao.insertBid(bid);
		bid.setOffer(offer);

		LOGGER.debug("Offerprize before: " + offer.getPrize());
		offer.addPrize(prize);
		LOGGER.debug("Offerprize after: " + offer.getPrize());

		LOGGER.debug("Player coins before: " + player.getCoins());
		player.spent(Integer.valueOf(prize));
		LOGGER.debug("Player coins after: " + player.getCoins());
		
		LOGGER.debug("Bids:");
		for (Bid b : marketPlDao.getBidsForOffer(offer, apiKey)) {
			LOGGER.debug("-" + b.getId());
		}

		return ResponseSurrogate.created(bid);
	}

	
	/**
	 * With this method one specific offer can be requested. If the API key is not valid 
	 * an analogous message is returned. It is also checked, if the id is a positive number otherwise a message 
	 * for an invalid number is returned.
	 * 
	 * @param offerId
	 * 			 The id of the offer that should be changed. This parameter is required.
	 * @param apiKey
	 * 			The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the offer belongs to.
	 * @return Response of Offer in JSON.
	 */
	@GET
	@Path("/offer/{offerId}")
	@TypeHint(Offer.class)
	public Response getOffer(
			@PathParam("offerId") @NotNull @ValidPositiveDigit(message = "The offer id must be a valid number") String offerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		int offId = ValidateUtils.requireGreaterThanZero(offerId);
		Offer offer = marketPlDao.getOffer(offId, apiKey);
		ValidateUtils.requireNotNull(offId, offer);

		return ResponseSurrogate.of(offer);
	}
	
	
	/**
	 * Gets all offers of an organisation (independent of the marketplace). If the API key is not valid 
	 * an analogous message is returned. It is also checked, if the id is a positive number otherwise a message 
	 * for an invalid number is returned.
	 * 
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the offers belongs to.
	 * @return A Response as List of Offers in JSON.
	 */
	@GET
	@Path("/offers/*")
	@TypeHint(Offer[].class)
	public Response getAllOffers(@QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		List<Offer> offers = marketPlDao.getAllOffers(apiKey);
		
		for (Offer offer : offers) {
			LOGGER.debug("| Offer:" + offer.getId());
		}

		return ResponseSurrogate.of(offers);
	}
	

	/**
	 * Gets all offers of an organisation (depending of the marketplace). If the API key is not valid 
	 * an analogous message is returned. It is also checked, if the id is a positive number otherwise a message 
	 * for an invalid number is returned.
	 * 
	 * @param marketPlId
	 * 			The marketplace whose offers should be considered.
	 * @param count
	 *           Optionally the count of offers that should be returned can be passed. If no value is passed
	 *           all offers of the marketplace are returned.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the offers belongs to.
	 * @return A Response as List of Offers in JSON.
	 */
	@GET
	@Path("/offers/market/{marketPlaceId}/*")
	@TypeHint(Offer[].class)
	public Response getAllOffersOfMarketPlace(
			@PathParam("marketPlaceId") @NotNull @ValidPositiveDigit(message = "The market id must be a valid number") String marketPlId,
			@QueryParam("count") @ValidPositiveDigit(message = "Count must be a valid number") String count,  
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		MarketPlace market = marketPlDao.getMarketplace(ValidateUtils.requireGreaterThanZero(marketPlId),apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(marketPlId), market);
		
		List<Offer> offers = new ArrayList<>();
		if(count != null){
			offers = market.getOffers().stream()
	            .limit(Integer.valueOf(count)).collect(Collectors.toList());
		}else{
			offers = market.getOffers();
		}
		
		
		for (Offer offer : offers) {
			LOGGER.debug("| Offer:" + offer.getId());
		}

		return ResponseSurrogate.of(offers);
	}
	
	
	/**
	 * Gets all offers which are amongst others for a specific role (depending of the marketplace). If the API key is not valid 
	 * an analogous message is returned. It is also checked, if the id is a positive number otherwise a message 
	 * for an invalid number is returned.
	 * 
	 * @param roleId
	 * 			The roles id for which the offers are searched. 
	 * @param marketPlId
	 * 			The marketplace whose offers should be considered.
	 * @param count
	 * 			 Optionally the count of offers that should be returned can be passed. If no value is passed
	 *           all offers of the marketplace are returned.
	 * @param apiKey
	 * 			The valid query parameter API key affiliated to one specific organisation, 
	 *          to which the offers belongs to.
	 * @return A Response as List of Offers in JSON.
	 */
	@GET
	@Path("/offers/role")
	@TypeHint(Offer[].class)
	public Response getAllOffersForRole(
			@QueryParam("roleId") @NotNull @ValidPositiveDigit(message = "The role id must be a valid number") String roleId,
			@QueryParam("marketPlaceId") @NotNull @ValidPositiveDigit(message = "The market id must be a valid number") String marketPlId,
			@QueryParam("count") @ValidPositiveDigit(message = "Count must be a valid number") String count,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		int id = Integer.parseInt(roleId);
		Role role = roleDao.getRole(id, apiKey);
		ValidateUtils.requireNotNull(id, role);
		
		MarketPlace market = marketPlDao.getMarketplace(ValidateUtils.requireGreaterThanZero(marketPlId), apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(marketPlId), market);
		
		List<Offer> offers = new ArrayList<>();
		if(count != null){
			offers = market.filterOfferByRole(Arrays.asList(role)).stream()
	            .limit(Integer.valueOf(count)).collect(Collectors.toList());
		}else{
			offers = market.filterOfferByRole(Arrays.asList(role));
		}
		
		for (Offer offer : offers) {
			LOGGER.debug("| Offer:" + offer.getId());
		}

		return ResponseSurrogate.of(offers);
	}
		
	
	/**
	 * Gets all offers a specific player has created (independent of the marketplace). If the API key is not valid 
	 * an analogous message is returned. It is also checked, if the id is a positive number otherwise a message 
	 * for an invalid number is returned.
	 * 
	 * @param playerId
	 *            The player whose created offers are returned.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the player belongs to.
	 * @return A Response as List of Offers in JSON.
	 */
	@GET
	@Path("/{playerId}/getOffers")
	@TypeHint(Offer[].class)
	public Response getPlayerOffers(
			@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThanZero(playerId), apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(playerId), player);

		List<Offer> offers;
		offers = marketPlDao.getOffersByPlayer(player, apiKey);

		for (Offer offer : offers) {
			LOGGER.debug("Player: " + player.getId() + "| Offer:" + offer.getId());
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
	 * @param count
	 * 			 Optionally the count of offers that should be returned can be passed. 
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this player belongs to.
	 * @return A Response as List of Offers in JSON.
	 */
	@GET
	@Path("/{playerId}/getOfferRole")
	@TypeHint(Offer[].class)
	public Response getOffersByPlayerRole(
			@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("marketPlaceId") @NotNull @ValidPositiveDigit(message = "The market id must be a valid number") String marketPlId,
			@QueryParam("count") @ValidPositiveDigit(message = "The count must be a valid number") @DefaultValue("10") String count,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThanZero(playerId), apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(playerId), player);

		MarketPlace market = marketPlDao.getMarketplace(ValidateUtils.requireGreaterThanZero(marketPlId),apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(marketPlId), market);
		
		List<Offer> matchingOffers = new ArrayList<>();
		if (market.getOffers().size() > 0) {
			LOGGER.debug("Marketplace: " + market.getId());
			matchingOffers = market.filterOfferByRole(player.getBelongsToRoles());

			if(count != null){
				matchingOffers = matchingOffers.stream().limit(Integer.valueOf(count)).collect(Collectors.toList());
			}
			
			for (Offer offer : matchingOffers) {
				LOGGER.debug("Result Offer : " + offer.getId());
			}
			
			return ResponseSurrogate.of(matchingOffers);
		}

		return ResponseSurrogate.of(matchingOffers);
	}

	/**
	 * Gets all offers of the marketplace ordered by date, recent first.
	 * If the API key is not valid an analogous message is returned. It is also checked, if the player id is
	 * a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param marketPlId
	 *            The marketplace whose offers are filtered.
	 * @param count
	 *            Optionally the count of offers that should be returned can be passed. The default value is 10. 
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the player belongs to.
	 * @return Response as List of Offers in JSON.
	 */
	@GET
	@Path("/recentOffers")
	@TypeHint(Offer[].class)
	public Response getRecentOffers(
			@QueryParam("marketPlaceId") @NotNull @ValidPositiveDigit(message = "The market id must be a valid number") String marketPlId,
			@QueryParam("count") @ValidPositiveDigit(message = "The count must be a valid number") @DefaultValue("10") String count,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		MarketPlace market = marketPlDao.getMarketplace(ValidateUtils.requireGreaterThanZero(marketPlId), apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(marketPlId), market);

		List<Offer> recentOffers = market.filterOfferByDate(market.getOffers(), ValidateUtils.requireGreaterThanZero(count));

		for (Offer offer : recentOffers) {
			LOGGER.debug("| Offer:" + offer.getId());
		}
		return ResponseSurrogate.of(recentOffers);
	}
	
	/**
	 * Gets all available offers for a player ordered by date, recent first. This can be used if a player wants to see 
	 * all recent offers she/he can complete. 
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
	 * @return Response as List of Offers in JSON.
	 */
	@GET
	@Path("/{playerId}/recentOffersRoleFiltered")
	@TypeHint(Offer[].class)
	public Response getRecentOffersRoleFiltered(@PathParam("playerId") @NotNull @ValidPositiveDigit String playerId,
			@QueryParam("marketPlaceId") @NotNull @ValidPositiveDigit(message = "The market id must be a valid number") String marketPlId,
			@QueryParam("count") @ValidPositiveDigit(message = "The count must be a valid number") @DefaultValue("10") String count,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThanZero(playerId), apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(playerId), player);
		
		MarketPlace market = marketPlDao.getMarketplace(ValidateUtils.requireGreaterThanZero(marketPlId), apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(marketPlId), market);
		
		List<Offer> matchingOffers;
			matchingOffers = market.filterOfferByRole(player.getBelongsToRoles());

			if (matchingOffers.isEmpty()) {
				return ResponseSurrogate.of(matchingOffers);
			}

			List<Offer> recentOffers = market.filterOfferByDate(matchingOffers, ValidateUtils.requireGreaterThanZero(count));

			for (Offer offer : recentOffers) {
				LOGGER.debug("| Offer:" + offer.getId());
			}
			return ResponseSurrogate.of(recentOffers);
	}

	/**
	 * Gets all offers of a marketplace ordered by prize, highest prize first.
	 * If the API key is not valid an analogous message is returned. It is also checked, if the player id is 
	 * a positive number otherwise a message for an invalid number is returned.
	 * 
	 * @param marketPlId
	 *            The marketplace whose offers are filtered.
	 * @param count
	 *            Optionally the count of offers that should be returned can be passed. The default value is 10. 
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the player belongs to.
	 * @return Response as List of Offers in JSON.
	 */
	@GET
	@Path("/highestOffers")
	@TypeHint(Offer[].class)
	public Response getHighestOffers(
			@QueryParam("marketPlaceId") @NotNull @ValidPositiveDigit(message = "The market id must be a valid number") String marketPlId,
			@QueryParam("count") @ValidPositiveDigit(message = "The count must be a valid number") @DefaultValue("10") String count,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		MarketPlace market = marketPlDao.getMarketplace(ValidateUtils.requireGreaterThanZero(marketPlId), apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(marketPlId), market);
		
		List<Offer> highestOffers = market.filterOffersByPrize(market.getOffers(), ValidateUtils.requireGreaterThanZero(count));

		for (Offer offer : highestOffers) {
			LOGGER.debug("| Offer:" + offer.getId());
		}
			
		return ResponseSurrogate.of(highestOffers);
	}
	
	/**
	 * Gets all available offers for a player ordered by prize, highest prize first. This can be used if a player wants to see 
	 * all offers she/he can complete and get the highest prize.
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
	 * @return Response as List of Offers in JSON.
	 */
	@GET
	@Path("/{playerId}/highestOffersRoleFiltered")
	@TypeHint(Offer[].class)
	public Response getHighestOffersRoleFiltered(
			@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("marketPlaceId") @NotNull @ValidPositiveDigit(message = "The market id must be a valid number") String marketPlId,
			@QueryParam("count") @ValidPositiveDigit(message = "The count must be a valid number") @DefaultValue("10") String count,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThanZero(playerId), apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(playerId), player);
		
		MarketPlace market = marketPlDao.getMarketplace(ValidateUtils.requireGreaterThanZero(marketPlId), apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(marketPlId), market);
		
		List<Offer> matchingOffers;
			matchingOffers = market.filterOfferByRole(player.getBelongsToRoles());

			if (matchingOffers.isEmpty()) {
				return ResponseSurrogate.of(matchingOffers);
			}
			List<Offer> highestOffers = market.filterOffersByPrize(matchingOffers, ValidateUtils.requireGreaterThanZero(count));

			for (Offer offer : highestOffers) {
				LOGGER.debug("| Offer:" + offer.getId());
			}
			
			return ResponseSurrogate.of(highestOffers);
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
	 * @return Response as List of Bids in JSON.
	 */
	@GET
	@Path("/{id}/bids")
	@TypeHint(Bid[].class)
	public Response getBids(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The id must be a valid number") String offerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
	
		Offer offer = marketPlDao.getOffer(ValidateUtils.requireGreaterThanZero(offerId), apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(offerId), offer);
		
		List<Bid> bidsForOffer = marketPlDao.getBidsForOffer(offer, apiKey);

		for (Bid bid : bidsForOffer) {
			LOGGER.debug("Bid " + bid.getId() + " with "+ bid.getPrize() + "coins for Offer: " + offer.getId());
		}

		return ResponseSurrogate.of(bidsForOffer);
	}

	/**
	 * Removes the offer with the assigned id from the marketPlace and with it all associated bids.
	 * Every player who has mad a bid to this offer gets her/his amount of coins back.
	 * It is checked, if the passed id is a positive number otherwise a message for an invalid number 
	 * is returned. If the API key is not 
	 * valid an analogous message is returned.
	 * 
	 * @param offerId
	 *            The offer which should be removed. This parameter is required. 
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the offer belongs to.
	 * @return Response of Offer in JSON.
	 */
	@DELETE
	@Path("/{id}/offer")
	@TypeHint(Offer.class)
	public Response deleteOffer(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The id must be a valid number") String offerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		int offId = ValidateUtils.requireGreaterThanZero(offerId);
		Offer offer = marketPlDao.getOffer(offId, apiKey);
		ValidateUtils.requireNotNull(offId, offer);
		
		int prize = offer.getPrize();
		int sum = 0;
		
		List<Bid> bids = marketPlDao.getBidsForOffer(offer, apiKey);
		if (!bids.isEmpty()) {
			for (Bid bid : bids) {
				Player player = bid.getPlayer();
				player.setCoins(player.getCoins() + bid.getPrize());
				LOGGER.debug("give a bid" + player.getId());
				sum = sum + bid.getPrize();
				LOGGER.debug(" Sum = " + sum);
				marketPlDao.deleteBid(bid);
			}
		}
		
		List<MarketPlace> markets = marketPlDao.getAllMarketPlaces(apiKey);
		if (!markets.isEmpty()) {
			for (MarketPlace market : markets) {
				market.removeOffer(offer);
				LOGGER.debug("Removed from market place  " + market.getId());
			}
		}

		Player owner = offer.getPlayer();
		LOGGER.debug("Owners Coins " + owner.getCoins());
		owner.setCoins(owner.getCoins() + (prize-sum));
		LOGGER.debug("Owners Coins " + owner.getCoins());
		
		Offer deletedOffer = marketPlDao.deleteOffer(offId, apiKey);

		return ResponseSurrogate.deleted(deletedOffer);
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
	 *            "name", "deadline" and "enddate".
	 * @param value
	 *            The new value of the attribute. This parameter is required.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the offer belongs to.
	 * @return Response of Offer in JSON.
	 */
	@PUT
	@Path("/{id}/attributes")
	@TypeHint(Offer.class)
	public Response changeOfferAttributes(
			@PathParam("id") @NotNull @ValidPositiveDigit(message = "The offer id must be a valid number") String offerId,
			@QueryParam("attribute") @NotNull String attribute, @QueryParam("value") @NotNull String value,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		Offer offer = marketPlDao.getOffer(ValidateUtils.requireGreaterThanZero(offerId), apiKey);
		ValidateUtils.requireNotNull(Integer.valueOf(offerId), offer);

		if ("null".equals(value)) {
			value = null;
		}

		switch (attribute) {
		case "name":
			offer.setName(value);
			break;

		case "deadline":
			offer.setDeadLine(LocalDateTimeUtil.formatDateAndTime(value));
			break;

		case "enddate":
			offer.setEndDate(LocalDateTimeUtil.formatDateAndTime(value));
			break;

		default:
			break;
		}

		marketPlDao.insertOffer(offer);

		return ResponseSurrogate.updated(offer);
	}

	/**
	 * This method returns a list with all ids of offers that contain a specific task. This is irrespective 
	 * of the marketplace. 
	 *   
	 * @param taskId
	 * 			The id of the task, which all returned offers contain.
	 * @param apiKey
	 * 			The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the offer belongs to.
	 * @return A list of all offers' ids that contain the task with the passed id as a list. 
	 */
	@GET
	@Path("/offers/{taskId}/*")
	@TypeHint(Integer[].class)
	@JsonIgnoreProperties({ "player" })
	public Response getAllOffersByTask(
			@PathParam("taskId") @NotNull @ValidPositiveDigit(message = "The task id must be a valid number") String taskId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		int idTask = ValidateUtils.requireGreaterThanZero(taskId);
		Task task = taskDao.getTask(idTask, apiKey);
		
		List<Offer> offers = marketPlDao.getOffersByTask(task, apiKey); 
		List<Integer> matchingOffers = new ArrayList<>();
		
		for (Offer offer : offers) {
			LOGGER.debug("| Offer:" + offer.getId());
			matchingOffers.add(offer.getId());
		}

		return ResponseSurrogate.of(matchingOffers);
	}
	
	
	/**
	 * This method returns a list with all offers which contain a specific task and the id of the 
	 * offer's marketplace. 
	 * 
	 * @param taskId
	 * 			The id of the task, which all returned offers contain.
	 * @param apiKey
	 * 			The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the offer belongs to.
	 * @return A list of all offers and their marketplaces which contain the task with the 
	 * 			passed id as a list. 
	 */
	@GET
	@Path("/offers/{taskId}/market/*")
	@TypeHint(Offer[].class)
	@JsonIgnoreProperties({ "player" })
	public Response getAllMarketPlaceOffersByTask(
			@PathParam("taskId") @NotNull @ValidPositiveDigit(message = "The task id must be a valid number") String taskId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {
		
		int idTask = ValidateUtils.requireGreaterThanZero(taskId);
		Task task = taskDao.getTask(idTask, apiKey);
		
		ArrayList<OfferMarketPlace> offList = MarketPlace.getAllOfferMarketPlaces(marketPlDao, task, apiKey);
		
		return ResponseSurrogate.of(offList);
	}
	
	
}
