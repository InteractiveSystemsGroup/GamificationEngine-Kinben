package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.marketPlace.Bid;
import info.interactivesystems.gamificationengine.entities.marketPlace.MarketPlace;
import info.interactivesystems.gamificationengine.entities.marketPlace.Offer;
import info.interactivesystems.gamificationengine.entities.task.Task;

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
	 * @param offer
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
	 * @param bid
	 *            The {@link Bid} that should be stored in the data base.
	 * @return The id of the created data base entry.
	 */
	public int insertBid(Bid bid) {
		em.persist(bid);
		em.flush();
		return bid.getId();
	}


//	/**
//	 * Gets a list of bids for a specific player and offer.
//	 * 
//	 * @param player
//	 *            The player who has made the bids on the offer.
//	 * @param offer
//	 *            The offer that is associated with the bids.
//	 * @return A {@link List} of all {@link Bid}s that are associated to a specific player and offer. 
//	 */
//	public List<Bid> getBidsForPlayerAndOffer(Player player, Offer offer) {
//		Query query = em.createQuery("select b from Bid b where b.player=:player and b.offer=:offer");
//		query.setParameter("player", player);
//		query.setParameter("offer", offer);
//		// List<Bid> result = new ArrayList<Bid>();
//		// return result = (List<Bid>)query.getResultList();
//		return query.getResultList();
//	}

	/**
	 * Gets a list of bids for a specific offer.
	 * 
	 * @param offer
	 *            The offer whose bids are requested.
	 * @param apiKey
	 *           The API key of the organisation to which the bids belong to. 
	 * @return A {@link List} of {@link Bid}s. that are associated to a specific offer and its APi key.
	 */
	public List<Bid> getBidsForOffer(Offer offer, String apiKey) {
		Query query = em.createQuery("select b from Bid b where b.offer=:offer and b.belongsTo.apiKey=:apiKey");
		query.setParameter("offer", offer);
		query.setParameter("apiKey", apiKey);
		return (List<Bid>)query.getResultList();
	}
	

	/**
	 * Gets a list of all offers which were created by a specific player.
	 * 
	 * @param player
	 *            The player who has created offers. 
	 * @param apiKey
	 *           The API key of the organisation to which the offers belong to. 
	 * @return A {@link List} of {@link Offer}s with all offers a player has created.
	 */
	public List<Offer> getOffersByPlayer(Player player, String apiKey) {
		Query query = em.createQuery("select o from Offer o where o.player=:player and o.belongsTo.apiKey=:apiKey");
		query.setParameter("player", player);
		query.setParameter("apiKey", apiKey);
		return (List<Offer>)query.getResultList();
	}

	// TODO test
	public List<Offer> getOffersByTask(Task task, String apiKey) {
		Query query = em.createQuery("select o from Offer o where o.task=:task and o.belongsTo.apiKey=:apiKey");
		query.setParameter("task", task);
		query.setParameter("apiKey", apiKey);
		return (List<Offer>)query.getResultList();
	}
	
	/**
	 * Gets a specific offer from the data base by its id.
	 * 
	 * @param offerId
	 *            The id of the requested offer.
	 * @param apiKey 
	 * 			The API key of the organisation to which the offer belongs to.
	 * @return The {@link Offer} that is associated with the passed id.
	 */
	public Offer getOffer(int offerId, String apiKey) {
		Query query = em.createQuery("select o from Offer o where o.belongsTo.apiKey=:apiKey and o.id =:id", Offer.class);
		List list = QueryUtils.configureQuery(query, offerId, apiKey);
		if (list.isEmpty()) {
			return null;
		}
		return ((Offer) list.get(0));
	}

	/**
	 * Gets all Offers which can be found on the marketplaces of an organisation.
	 * 
	 * @param apiKey
	 * 			The API key of the organisation to which the offers belong to.
	 * @return
	 * 		A List of all Offers, which belongs to the organisation with the associated apiKey.
	 */
	public List<Offer> getAllOffers(String apiKey) {
		Query query = em.createQuery("select o from Offer o where o.belongsTo.apiKey=:apiKey", Offer.class);
		query.setParameter("apiKey", apiKey);

		return query.getResultList();
	}
	
	
	
	/**
	 * Gets a list of all marketplaces of an organisation which is associated with the passed API key.
	 * 
	 * @param apiKey
	 *            The API key of the organisation to which the marketplaces belongs to.
	 * @return The {@link List} of {@link MarketPlace}s which belong to the passed API key.
	 */
	public List<MarketPlace> getAllMarketPlaces(String apiKey) {
		Query query = em.createQuery("select m from MarketPlace m where m.belongsTo.apiKey=:apiKey", MarketPlace.class);
		query.setParameter("apiKey", apiKey);

		return query.getResultList();
	}
	
	/**
	 * Gets a specific marketplace from the data base that is associated with the id and the API key.
	 * 
	 * @param id
	 * 			The id of the requested marketplace.
	 * @param apiKey
	 * 			The API key of the organisation to which the marketplaces belongs to.
	 * @return The requested {@link MarketPlace} that belongs to the passed id and API key.
	 */
	public MarketPlace getMarketplace(int id, String apiKey) {
		Query query = em.createQuery("select m from MarketPlace m where m.belongsTo.apiKey=:apiKey and m.id = :id", MarketPlace.class);
		List list = QueryUtils.configureQuery(query, id, apiKey);
		if (list.isEmpty()) {
			return null;
		}
		return ((MarketPlace) list.get(0));
	}

	/**
	 * Removes a marketplace from the data base.
	 * 
	 * @param id
	 *          The id of the marketplace that should be removed.
	 * @param apiKey
	 *           The API key of the organisation to which the marketPlace belong to. 
	 * @return The {@link MarketPlace} that is removed from the database.
	 */
	public MarketPlace deleteMarketPlace(int id, String apiKey) {
		MarketPlace market = getMarketplace(id, apiKey);

		if (market != null && market.getOffers().isEmpty()) {
			em.remove(market);
		}
		return market;
	}

	/**
	 * Removes an offer from the data base.
	 * 
	 * @param offerId
	 *           The id of the offer that should be removed from the data base.
	 * @param apiKey
	 *           The API key of the organisation to which the offer belongs to. 
	 * @return The deleted {@link Offer}.
	 */
	public Offer deleteOffer(int offerId, String apiKey) {
		
		Offer offer = getOffer(offerId, apiKey);
		
		if (offer != null) {
			em.remove(offer);
		}
		return offer;
	}
	
	/**
	 * Removes several offers from the data base.
	 * 
	 * @param offers
	 * 			The list of offers that should be removes from the data base.
	 * @param apiKey
	 * 			The API key of the organisation to which the offer belongs to. 
	 */
	public void deleteOffers(List<Offer> offers, String apiKey) {
		
		for (Offer offer : offers) {
			Offer offer2 = getOffer(offer.getId(), apiKey);
			
			if (offer2 != null) {
				em.remove(offer2);
			}
		}
	}

	/**
	 * Removes a specific bid from the data base.
	 * 
	 * @param bid
	 * 		 The bid that should be removed from the database.
	 * @return The deleted {@link Bid}.
	 */
	public Bid deleteBid(Bid bid) {
		if(bid!=null){
			em.remove(bid);
		}
		return bid;
	}
}
