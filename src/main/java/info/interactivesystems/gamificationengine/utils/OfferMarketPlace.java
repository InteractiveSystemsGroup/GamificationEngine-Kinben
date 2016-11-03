package info.interactivesystems.gamificationengine.utils;

import info.interactivesystems.gamificationengine.entities.marketPlace.Offer;

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
