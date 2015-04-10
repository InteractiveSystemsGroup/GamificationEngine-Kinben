package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.marketPlace.Bid;
import info.interactivesystems.gamificationengine.entities.marketPlace.MarketPlace;
import info.interactivesystems.gamificationengine.entities.marketPlace.Offer;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Named
@Stateless
public class MarketPlaceDAO {

	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	/**
	 * Store a new market place in the database.
	 * 
	 * @param market
	 *            the {@link MarketPlace} that should be stored in the database
	 * @return the id of the created database entry
	 */
	public int insertMarketPlace(MarketPlace market) {
		em.persist(market);
		em.flush();
		return market.getId();
	}

	/**
	 * Store a new offer in the database.
	 * 
	 * @param market
	 *            the {@link Offer} that should be stored in the database
	 * @return the id of the created database entry
	 */
	public int insertOffer(Offer offer) {
		em.persist(offer);
		em.flush();
		return offer.getId();
	}

	/**
	 * Store a new bid in the database.
	 * 
	 * @param market
	 *            the {@link Bid} that should be stored in the database
	 * @return the id of the created database entry
	 */
	public int insertBid(Bid bid) {
		em.persist(bid);
		em.flush();
		return bid.getId();
	}

	// public int insertBd(Offer offer, Bid bid) {
	// em.persist(offer);
	// em.persist(bid);
	// em.flush();
	// return bid.getId();
	// }

	/**
	 * Get a list of bids for a specific player and offer
	 * 
	 * @param player
	 *            the player of the bids
	 * @param offer
	 *            the offer of the bids
	 * @return {@link List<Bid>}
	 */
	public List<Bid> getBidsForPlayerAndOffer(Player player, Offer offer) {
		Query query = em.createQuery("select b from Bid b where b.player=:player and b.offer=:offer");
		query.setParameter("player", player);
		query.setParameter("offer", offer);
		// List<Bid> result = new ArrayList<Bid>();
		// return result = (List<Bid>)query.getResultList();
		return query.getResultList();
	}

	/**
	 * Get a list of bids for a specific offer
	 * 
	 * @param offer
	 *            the requested offer
	 * @return {@link List<Bid>}
	 */
	public List<Bid> getBidsForOffer(Offer offer) {
		Query query = em.createQuery("select b from Bid b where b.offer=:offer");
		query.setParameter("offer", offer);

		// List<Bid> result = new ArrayList<Bid>();
		// return result = (List<Bid>)query.getResultList();
		return query.getResultList();
	}

	/**
	 * Get a list of all offers of a specifice player
	 * 
	 * @param player
	 *            the requested player
	 * @return {@link List<Offer>}
	 */
	public List<Offer> getOffersByPlayer(Player player) {
		Query query = em.createQuery("select o from Offer o where o.player=:player");
		query.setParameter("player", player);
		return query.getResultList();
	}

	/**
	 * Get the offer from the database
	 * 
	 * @param offerId
	 *            the requested id
	 * @return {@link Offer}
	 */
	public Offer getOffer(int offerId) {
		Offer offer = em.find(Offer.class, offerId);
		return offer;
	}

	/**
	 * Get the market place from the database
	 * 
	 * @param marketId
	 *            the requested id
	 * @return {@link MarketPlace}
	 */
	public MarketPlace getMarketPl(int marketId) {
		MarketPlace marketPl = em.find(MarketPlace.class, marketId);
		return marketPl;
	}

	/**
	 * Get a list of all market places
	 * 
	 * @param apiKey
	 *            the apiKey of the organisation
	 * @return {@link List<MarketPlace>}
	 */
	public List<MarketPlace> getAllMarketPlaceForApiKey(String apiKey) {
		Query query = em.createQuery("select m from MarketPlace m where m.belongsTo.apiKey=:apiKey", MarketPlace.class);
		query.setParameter("apiKey", apiKey);

		return query.getResultList();
	}

	/**
	 * Delete a market place from the database
	 * 
	 * @param id
	 *            the id of the market place
	 * @return {@link MarketPlace}
	 */
	public MarketPlace deleteMarketPlace(int id) {
		// TODO with organisation-check
		MarketPlace market = em.find(MarketPlace.class, id);
		em.remove(market);
		return market;
	}

	/**
	 * Delete an offer from the database
	 * 
	 * @param id
	 *            the id of the offer
	 * @return {@link Offer}
	 */
	public Offer deleteOffer(int offerId) {
		Offer offer = getOffer(offerId);
		em.remove(offer);
		return offer;
	}

}
