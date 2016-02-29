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
	 * Stores a new call for donations in the database.
	 * 
	 * @param dCall
	 *            The new {@link DonationCall} that should be stored in the data base.
	 * @return The id of the created data base entry.
	 */
	public int insertDonationCall(DonationCall dCall) {
		em.persist(dCall);
//		em.flush();
		return dCall.getId();
	}

	/**
	 * Gets a call for donations from the data base.
	 * 
	 * @param donationCallId
	 *            The id of the call for donations.
	 * @return The found {@link DonationCall} or null.
	 */
	public DonationCall getDonationCall(int donationCallId, String apiKey) {
		
		Query query = em.createQuery("select dC from DonationCall dC where dC.belongsTo.apiKey=:apiKey and dC.id = :id", DonationCall.class);
		List list = QueryUtils.configureQuery(query, donationCallId, apiKey);
		if (list.isEmpty()) {
			return null;
		}
		return ((DonationCall) list.get(0));
	}

	/**
	 * Gets a list of all calls for donation in the data base.
	 * 
	 * @param apiKey
	 *            The API key affiliated to one specific organisation, to which
	 *            the call for donations belongs to.
	 * @return A {@link List} of {@link DonationCall}s.
	 */
	public List<DonationCall> getDonationCalls(String apiKey) {
		Query query = em.createQuery("select dc from DonationCall dc where dc.belongsTo.apiKey=:apiKey");
		query.setParameter("apiKey", apiKey);
		return query.getResultList();
	}

	
	/**
	 * Removes a call for donations from the data base.
	 * 
	 * @param apiKey
	 *            The API key affiliated to one specific organisation, to which
	 *            the call for donations belongs to.
	 * @param dCId
	 *            The id of the call for daonations.
	 * @return {@link DonationCall}.
	 */
	public DonationCall deleteDonationCall(String apiKey, int dCId) {
		DonationCall donationCall = getDonationCall(dCId, apiKey);
		
		if (donationCall != null && donationCall.isGoalReached()) {
			em.remove(donationCall);
		}
		
		return donationCall;
	}

}
