package info.interactivesystems.gamificationengine.utils;

import info.interactivesystems.gamificationengine.entities.marketPlace.Offer;

/**
 * Utility Class to have a temporal list with the offer of a task and the marketplace id where it is offered.
 * After completing a task this class is used to temporal save all offers that contains this task and the corresponding marketplace id. 
 *
 */
public class OfferMarketPlace {
	
	private Offer offer;
	private int marketPlaceId;
	
	public OfferMarketPlace(Offer o, int id) {
		this.offer = o;
		this.marketPlaceId = id;
	} 
	
	public Offer getOffer() {
		return offer;
	}

	public int getMarketPlaceId() {
		return marketPlaceId;
	}

	
}
