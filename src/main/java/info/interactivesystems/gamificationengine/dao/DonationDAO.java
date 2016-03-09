package info.interactivesystems.gamificationengine.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import info.interactivesystems.gamificationengine.entities.donationCall.Donation;
import info.interactivesystems.gamificationengine.entities.donationCall.DonationCall;

/**
 * Data access for a Donation.
 */
@Named
@Stateless
public class DonationDAO {

	@PersistenceContext(unitName = PersistenceUnit.PROJECT)
	private EntityManager em;

	/**
	 * Stores a new call for donations in the data base.
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
	 * Stores a new  donation in the data base.
	 * 
	 * @param donation
	 * 			The new donation that should be stores in the data base.
	 */
	public void insertDonation(Donation donation) {
		em.persist(donation);
	}
	
	/**
	 * Gets a specific call for donations from the data base.
	 * 
	 * @param donationCallId
	 *            The id of the call for donations.
	 * @param apiKey
	 *           The API key of the organisation to which the call for donations belongs to. 
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
	 * Gets a list of all calls for donation of one specific organisaiton in the data base.
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
	 * @param dCId
	 *            The id of the specific call for donations.
	 * @param apiKey
	 *            The API key affiliated to one specific organisation, to which
	 *            the call for donations belongs to.
	 * @return {@link DonationCall}.
	 */
	public DonationCall deleteDonationCall(int dCId, String apiKey) {
		DonationCall donationCall = getDonationCall(dCId, apiKey);
		
		if (donationCall != null) {
			em.remove(donationCall);
		}
		
		return donationCall;
	}

	
	/**
	 * Gets a list of donations for a specific call for donations.
	 * 
	 * @param dCall
	 *            The call for donations whose donations are requested.
	 * @param apiKey
	 *           The API key of the organisation to which the donations belong to. 
	 * @return A {@link List} of {@link Donation}s. that are associated to a specific call for donation and its APi key.
	 */
	public List<Donation> getDonationsForDonationCall(DonationCall dCall, String apiKey) {
		Query query = em.createQuery("select d from Donation d where d.donationCall=:donationCall and d.belongsTo.apiKey=:apiKey");
		query.setParameter("donationCall", dCall);
		query.setParameter("apiKey", apiKey);
		return (List<Donation>)query.getResultList();
	}

	/**
	 * Removes a donation from the data base.
	 * 
	 *@param donation
	 *         The donation that is removed.
	 * 
	 */
	public void deleteDonation(Donation donation) {
		if(donation!=null){
			em.remove(donation);
		}
	}
}
