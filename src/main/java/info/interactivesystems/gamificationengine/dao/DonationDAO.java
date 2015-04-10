package info.interactivesystems.gamificationengine.dao;

import info.interactivesystems.gamificationengine.entities.DonationCall;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Data access for a Donation.
 */
@Named
@Stateless
public class DonationDAO {

	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	/**
	 * Store a new donation call in the database.
	 * 
	 * @param dCall
	 *            the {@link DonationCall} that should be stored in the database
	 * @return the id of the created database entry
	 */
	public int insertDonationCall(DonationCall dCall) {
		em.persist(dCall);
		em.flush();
		return dCall.getId();
	}

	/**
	 * Get a donation call from the database.
	 * 
	 * @param donationCallId
	 *            the id of the donation call
	 * @return the found {@link DonationCall} or null
	 */
	public DonationCall getDonationCall(int donationCallId) {
		return em.find(DonationCall.class, donationCallId);
	}

	/**
	 * Get a list of all donation calls
	 * 
	 * @param apiKey
	 *            the api key of the organisation
	 * @return {@link List<DonationCall>}
	 */
	public List<DonationCall> getDonationCalls(String apiKey) {
		Query query = em.createQuery("select dc from DonationCall dc where dc.belongsTo.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}

	/**
	 * Delete a donation call from the database
	 * 
	 * @param apikey
	 *            the api key of the organisation
	 * @param dCId
	 *            the id of the donation call
	 * @return {@link DonationCall}
	 */
	public DonationCall deleteDonationCall(String apikey, int dCId) {
		DonationCall donationCall = getDonationCall(dCId);
		em.remove(donationCall);
		return donationCall;
	}

}
