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
 * bid to do this task and get its rewards.
 * 
 * <ul>
 * <li>offers - all offers in one marketplace, which a player can bid for</li>
 * </ul>
 */
@Entity
public class MarketPlace {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@ManyToOne
	private Organisation belongsTo;

	/**
	 * All offers in one marketplace, which a player can bid for</li>
	 */
	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<Offer> offers;

	public MarketPlace() {
		offers = new ArrayList<>();
	}

	// GETTER & SETTER
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Organisation getBelongsTo() {
		return belongsTo;
	}

	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	public List<Offer> getOffers() {
		return offers;
	}

	public void setOffers(List<Offer> offers) {
		this.offers = offers;
	}

	public List<Offer> addOffer(Offer offer) {
		offers.add(offer);
		// this.offers = offers;
		return offers;
	}

	public List<Offer> viewOffers() {

		return offers;
	}

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

	public List<Offer> filterOfferByDate(List<Offer> matchingOffers, int count) {
		Comparator<Offer> byOfferDate = (o1, o2) -> o1.getOfferDate().compareTo(o2.getOfferDate());
		return filterOfferByParam(matchingOffers, count, byOfferDate, true);
	}

	public List<Offer> filterOfferByPrize(List<Offer> matchingOffers, int count) {
		Comparator<Offer> byOfferPrize = (o1, o2) -> Integer.compare(o1.getPrize(), o2.getPrize());
		return filterOfferByParam(matchingOffers, count, byOfferPrize, true);
	}

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
