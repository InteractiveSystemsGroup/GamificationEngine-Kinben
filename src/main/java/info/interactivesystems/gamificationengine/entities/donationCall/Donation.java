package info.interactivesystems.gamificationengine.entities.donationCall;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;

@Entity
@JsonIgnoreProperties({ "belongsTo", "donationCall"})
public class Donation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@ManyToOne
	private Organisation belongsTo;

	private int amount;

	private LocalDateTime creationDate;

	@ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private Player player;

	@ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private DonationCall donationCall;

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

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public DonationCall getDonationCall() {
		return donationCall;
	}

	public void setDonationCall(DonationCall donationCall) {
		this.donationCall = donationCall;
	}
	
}
