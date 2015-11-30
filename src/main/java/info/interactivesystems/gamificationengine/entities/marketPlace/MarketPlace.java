package info.interactivesystems.gamificationengine.entities.marketPlace;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

/**
 * Players can create an offer with a task for the marketplace so another player can
 * bid to do this task and get its rewards. Via Bids an initial bid by the creator can be
 * raised. To be able to create offers, a marketplace for the organisation is needed. 
 * If none exists yet, it first has to be created.
 */
@Entity
public class MarketPlace {

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
	 * @param offer The offer that is added to the list of offers.
	 * @return The new list of offers of the marketplace.
	 */
	public List<Offer> addOffer(Offer offer) {
		offers.add(offer);
		return offers;
	}

	/**
	 * This method filters all existing offers in a marketplace by a passed role of a player.
	 * So a list is returned which contains only offers which at least match one role of 
	 * the player.
	 *   
	 * @param player
	 * 			The player whose roles are checked if they match an offer.
	 * @param list
	 * 			The list of Roles which are the hint for filtering.
	 * @return The list of all offers which are in the marketplace and a player is allowed
	 * 			to complete.
	 */
	public List<Offer> filterOfferByRole(Player player, List<Role> list) {
		List<Offer> matchingOffers = new ArrayList<>();
		for (Offer offer : this.getOffers()) {
			for (Role r : player.getBelongsToRoles()) {
				if (offer.getAllowedForRole().contains(r)) {
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
	 * A list with x offers are returned that are the latest and match at least one role of
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
	public List<Offer> filterOfferByPrize(List<Offer> matchingOffers, int count) {
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
	private List<Offer> filterOfferByParam(List<Offer> matchingOffers, int count, Comparator comp, boolean reverse) {
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
}
