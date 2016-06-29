package info.interactivesystems.gamificationengine.entities.marketPlace;

import info.interactivesystems.gamificationengine.dao.MarketPlaceDAO;
import info.interactivesystems.gamificationengine.dao.PlayerDAO;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.Role;
import info.interactivesystems.gamificationengine.entities.task.Task;
import info.interactivesystems.gamificationengine.utils.OfferMarketPlace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Players can create an offer with a task for the marketplace so another player can
 * bid to do this task and get its rewards. Via Bids an initial bid by the creator can be
 * raised. To be able to create offers, a marketplace for the organisation is needed. 
 * If none exists yet, it first has to be created.
 */
@Entity
@JsonIgnoreProperties({ "belongsTo" })
public class MarketPlace {

	private static final Logger LOGGER = LoggerFactory.getLogger(MarketPlace.class);
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@ManyToOne
	private Organisation belongsTo;

	/**
	 * All offers in one marketplace, which a player can bid for
	 */
	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<Offer> offers;

	public MarketPlace() {
		offers = new ArrayList<>();
	}

	/**
	 * Gets the id of a marketplace.
	 * 
	 * @return The markeptlace's id as int.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of a marketplace.
	 * 
	 * @param id
	 * 			The new id of the marketplace.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the organisation a marketplace belongs to.
	 * 
	 * @return The organisation of the marketplace as an object.
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Sets the organisation a marketplace belongs to.
	 * 
	 * @param belongsTo
	 * 			The marketplace's organisation.
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Gets all offers of one marketplace, which a player can bid for.
	 * 
	 * @return A list of all existing offers a player can bid for. 
	 */
	public List<Offer> getOffers() {
		return offers;
	}

	/**
	 * Sets the list of offers of a marketplace. 
	 *   
	 * @param offers
	 * 			The new list of offers of a marketplace.
	 */
	public void setOffers(List<Offer> offers) {
		this.offers = offers;
	}

	/**
	 * Adds one new offer to the list of offers.
	 * 
	 * @param offer 
	 * 			The offer that is added to the list of offers.
	 */
	public void addOffer(Offer offer) {
		if(!offers.contains(offer)){
			this.offers.add(offer);
		}
	}

	/**
	 * Removes an offer from a marketplace's list of offers.
	 * 
	 * @param offer
	 * 			The offer that should be removed.
	 */
	public void removeOffer(Offer offer) {
		if(offers.contains(offer)){
			this.offers.remove(offer);
		}
	}
	
	/**
	 * This method filters all existing offers in a marketplace by a passed role of a player.
	 * So a list is returned which contains only offers which at least match one role of 
	 * the player.
	 *   
	 * @param roles
	 * 			The player whose roles are checked if they match an offer.
	 * 			The list of Roles which are the hint for filtering.
	 * @return The list of all offers which are in the marketplace and a player is allowed
	 * 			to complete.
	 */
	public List<Offer> filterOfferByRole(List<Role> roles) {
		List<Offer> matchingOffers = new ArrayList<>();
		for (Offer offer : this.getOffers()) {
			for (Role r : roles) {
					if (offer.getTask().getAllowedFor().contains(r)) {
					matchingOffers.add(offer);
					break;
				}
			}
		}
		return matchingOffers;
	}

	/**
	 * This method filters all matching offers to the player's roles and filters it by the
	 * date so the latest offers are presented.  
	 * A list with a specific count of offers are returned that are the latest and match at least one role of
	 * the player. 
	 * 
	 * @param matchingOffers
	 * 			All offers which match at least one role of the player.
	 * @param count
	 * 			The number of offers that are returned with the list.
	 * @return The list with the number of the second parameter and matching offers to the 
	 * player's roles is returned.
	 */
	public List<Offer> filterOfferByDate(List<Offer> matchingOffers, int count) {
		Comparator<Offer> byOfferDate = (o1, o2) -> o1.getOfferDate().compareTo(o2.getOfferDate());
		return filterOfferByParam(matchingOffers, count, byOfferDate, true);
	}

	/**
	 * This method filters all matching offers to the player's roles and filters it by the 
	 * prize that can be earned. 
	 * A list with x offers are returned that have the highest prize and match at least one 
	 * role of the player. 
	 * 
	 * @param matchingOffers
	 * 			All offers which match at least one role of the player.
	 * @param count
	 * 			The number of offers that are returned with the list.
	 * @return The list with the number of the second parameter and matching offers to the 
	 * player's roles is returned.
	 */
	public List<Offer> filterOffersByPrize(List<Offer> matchingOffers, int count) {
		Comparator<Offer> byOfferPrize = (o1, o2) -> Integer.compare(o1.getPrize(), o2.getPrize());
		return filterOfferByParam(matchingOffers, count, byOfferPrize, true);
	}

	/**
	 * This method filters the offers thate are matching at least one role of the player
	 * by the passed conditions.
	 * 
	 * @param matchingOffers
	 * 			All offers which match at least one role of the player.
	 * @param count
	 * 			The number of offers that are returned with the list.
	 * @param comp
	 * 			The comparator which defines the filter method. This can be for example
	 * 			compare the offers' prize or their dates of creation.
	 * @param reverse
	 * 			Boolean i the order of the result list should be reversed (true) or not 
	 * 			(false).
	 * @return The list of filtered offers.
	 */
	private List<Offer> filterOfferByParam(List<Offer> matchingOffers, int count, Comparator<Offer> comp, boolean reverse) {
		int toIndex;

		if (reverse) {
			Collections.sort(matchingOffers, Collections.reverseOrder(comp));
		} else {
			Collections.sort(matchingOffers, comp);
		}

		if (count < matchingOffers.size() && count >= 0) {
			toIndex = count;
		} else {
			toIndex = matchingOffers.size();
		}

		return matchingOffers.stream().limit(toIndex).collect(Collectors.toList());
	}
	
	
	/**
	 * Every prize of every offer in the passed list is added and obtained to the
	 * player who has finished the task. Then every offer in the passed list is 
	 * removed from the appropriated marketplace and deleted from the database.
	 * 
	 * @param taskOffers
	 * 			A list of offers that contain the same task. Additionally the list 
	 * 			contains for each offer the id of the marketplace is  
	 * @param player
	 * 			The player that has fulfilled the task.
	 * @param marketPlDao
	 * 			DAO to get the marketplace of the database.
	 * @param playerDao
	 * 			DAO to update the player.
	 * @param apiKey 
	 * 			 The valid query parameter API key affiliated to one specific organisation, 
	 *           to which this task belongs to.
	 */
	public static void completeAssociatedOffers(List<OfferMarketPlace> taskOffers, Player player, 
			MarketPlaceDAO marketPlDao, PlayerDAO playerDao, String apiKey){
		
		int prizeReward = 0;
		
		HashSet<MarketPlace> mPls = new HashSet<>();
		List<Offer> offersToComplete = new ArrayList<>();
		List<Offer> offersToDelete = new ArrayList<>();
		
		for (OfferMarketPlace offerMarketPlace : taskOffers) {
			offersToComplete.add(offerMarketPlace.getOffer());
			mPls.add(marketPlDao.getMarketplace(offerMarketPlace.getMarketPlaceId(), apiKey));	
		}	
		
		for (MarketPlace places : mPls) {
			List<Offer> removeOffers = new ArrayList<>();
			
			//Bids are deleted by cascading.
			for (Offer offer2 : offersToComplete) {
				if(places.offers.contains(offer2)){
					prizeReward += offer2.getPrize();
					removeOffers.add(offer2);
					LOGGER.debug("Offer removed and prize = " + prizeReward);
				}
			}
			offersToComplete.removeAll(removeOffers);
			offersToDelete.addAll(removeOffers);
			places.offers.removeAll(removeOffers);
			marketPlDao.insertMarketPlace(places);
			LOGGER.debug("MarketPlace updated " + places.getId());
		}
			
		marketPlDao.deleteOffers(offersToDelete, apiKey);
		
		player.setCoins(player.getCoins() + prizeReward);
			
		LOGGER.debug("reward awarded: " + prizeReward + " coins" );
		playerDao.insert(player);
				
	}
	
	/**
	 * This methods creates an ArrayList of all offers that contains a specific 
	 * task. Additonally to every offer the id of the marektplace is added where 
	 * the offer can be find.
	 *   
	 * @param marketPlDao
	 * 			The DAO to get the marketplace of the database.
	 * @param task
	 * 			All offers of the marketplaces are returned in the list that 
	 * 			contains this task.
	 * @param apiKey
	 * 			The valid query parameter API key affiliated to one specific organisation, 
	 *          to which the offer belongs to.
	 * @return The List of all offers and their marketplace ids which contain the passed task.
	 */
	public static ArrayList<OfferMarketPlace> getAllOfferMarketPlaces(MarketPlaceDAO marketPlDao, Task task, String apiKey){
		List<Offer> offers = marketPlDao.getOffersByTask(task, apiKey); 
		
		ArrayList<OfferMarketPlace> offList = new ArrayList<>();
		
		List<MarketPlace> markets = marketPlDao.getAllMarketPlaces(apiKey);
		for (MarketPlace marketPlace : markets) {
			List<Offer> marketOffers = marketPlace.getOffers();
			
			for (Offer offer : offers) {
				if(marketOffers.contains(offer)){
					OfferMarketPlace offMarP = new OfferMarketPlace(offer, marketPlace.getId());
					offList.add(offMarP);
				}
			}
		}
		return offList;
	}
	
}
