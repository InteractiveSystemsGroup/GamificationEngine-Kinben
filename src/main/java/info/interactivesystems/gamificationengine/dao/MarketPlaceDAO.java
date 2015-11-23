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
	 * Stores a new marketplace in the data base.
	 * 
	 * @param market
	 *            The {@link MarketPlace} that should be stored in the data base.
	 * @return The id of the created data base entry.
	 */
	public int insertMarketPlace(MarketPlace market) {
		em.persist(market);
		em.flush();
		return market.getId();
	}

	/**
	 * Stores a new offer in the data base.
	 * 
	 * @param market
	 *            The {@link Offer} that should be stored in the data base.
	 * @return The id of the created data base entry.
	 */
	public int insertOffer(Offer offer) {
		em.persist(offer);
		em.flush();
		return offer.getId();
	}

	/**
	 * Stores a new bid in the data base.
	 * 
	 * @param market
	 *            The {@link Bid} that should be stored in the data base.
	 * @return The id of the created data base entry.
	 */
	public int insertBid(Bid bid) {
		em.persist(bid);
		em.flush();
		return bid.getId();
	}


	/**
	 * Gets a list of bids for a specific player and offer
	 * 
	 * @param player
	 *            The player who has made the bids on the offer.
	 * @param offer
	 *            The offer that is associated with the bids.
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
	 * Gets a list of bids for a specific offer.
	 * 
	 * @param offer
	 *            The offer whose bids are requested.
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
	 * Gets a list of all offers which were created by a specific player.
	 * 
	 * @param player
	 *            The player who has created offers. 
	 * @return A {@link List<Offer>} with all offers a player has created.
	 */
	public List<Offer> getOffersByPlayer(Player player) {
		Query query = em.createQuery("select o from Offer o where o.player=:player");
		query.setParameter("player", player);
		return query.getResultList();
	}

	/**
	 * Gets a specific offer from the data base by its id.
	 * 
	 * @param offerId
	 *            The id of the requested offer.
	 * @return The {@link Offer} that is associated with the passed id.
	 */
	public Offer getOffer(int offerId) {
		Offer offer = em.find(Offer.class, offerId);
		return offer;
	}

	/**
	 * Gets a specific marketplace from the data base that is associated with the id.
	 * 
	 * @param marketId
	 *            The id of the requested marketplace.
	 * @return The {@link MarketPlace} that is associated with the passed id.
	 */
	public MarketPlace getMarketPl(int marketId) {
		MarketPlace marketPl = em.find(MarketPlace.class, marketId);
		return marketPl;
	}

	/**
	 * Gets a list of all marketplaces which are associated with the passed API key.
	 * 
	 * @param apiKey
	 *            The API key of the organisation to which the marketplaces belong to.
	 * @return The {@link List<MarketPlace>} with all marketplaces that belong to the
	 * 			passed API key.
	 */
	public List<MarketPlace> getAllMarketPlaceForApiKey(String apiKey) {
		Query query = em.createQuery("select m from MarketPlace m where m.belongsTo.apiKey=:apiKey", MarketPlace.class);
		query.setParameter("apiKey", apiKey);

		return query.getResultList();
	}

	/**
	 * Removes a marketplace from the data base.
	 * 
	 * @param id
	 *          The id of the marketplace that should be removed.
	 * @return The {@link MarketPlace} that is removed from the database.
	 */
	public MarketPlace deleteMarketPlace(int id) {
		// TODO with organisation-check
		MarketPlace market = em.find(MarketPlace.class, id);
		em.remove(market);
		return market;
	}

	/**
	 * Deletes an offer from the data base.
	 * 
	 * @param id
	 *           The id of the offer that should be removed from the data base.
	 * @return {@link Offer}
	 */
	public Offer deleteOffer(int offerId) {
		Offer offer = getOffer(offerId);
		em.remove(offer);
		return offer;
	}

}
