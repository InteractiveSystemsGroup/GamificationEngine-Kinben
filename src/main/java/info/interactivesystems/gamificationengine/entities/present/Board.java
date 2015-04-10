package info.interactivesystems.gamificationengine.entities.present;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

/**
 * A Board serves a player to store presents in three lists: If a present is
 * sent to a player it first is added to the inBox-list. The player decides if
 * he accepts the present, then it is added to the list which holds the current
 * presents else if it is denied it is removed from the inbox. When the player
 * wants to archive a present it is added to the archive list.
 */
@Entity
public class Board {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@ManyToOne
	private Organisation belongsTo;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	@JoinTable(name = "board_inBox")
	private List<Present> inBox;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	@JoinTable(name = "board_current")
	private List<Present> currentPresents;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	@JoinTable(name = "board_archive")
	private List<PresentArchived> archive;

	@OneToOne
	private Player owner;

	public Board() {
		currentPresents = new ArrayList<>();
		archive = new ArrayList<>();
		inBox = new ArrayList<>();
	}

	// GETTER & SETTER
	/**
	 * Get the id of a created board.
	 * 
	 * @return id of the board as an int
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the id of a board.
	 * 
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * The organisation the board belongs to.
	 * 
	 * @return a not nullable organisation
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * * An organisation to which this board belongs, may not be null.
	 * 
	 * @param belongsTo
	 *            an organisation
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	// public List<PresentAccepted> getCurrentPresents() {
	// return currentPresents;
	// }
	//
	// public void setCurrentPresents(List<PresentAccepted> presents) {
	// this.currentPresents = presents;
	// }

	/**
	 * Get all current presents of a player. These are presents which were
	 * accepted before.
	 * 
	 * @return List<Present>
	 */
	public List<Present> getCurrentPresents() {
		return currentPresents;
	}

	/**
	 * Set a List of current presents of a player.
	 * 
	 * @param presents
	 */
	public void setCurrentPresents(List<Present> presents) {
		this.currentPresents = presents;
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}

	/**
	 * Get a List of all archived presents which belongs to one player.
	 * 
	 * @return List<PresentArchived>
	 */
	public List<PresentArchived> getArchive() {
		return archive;
	}

	/**
	 * Set a List of player's archived presents.
	 * 
	 * @param history
	 */
	public void setArchive(List<PresentArchived> history) {
		this.archive = history;
	}

	/**
	 * Get all Presents which were sent to a player.
	 * 
	 * @return List<Present>
	 */
	public List<Present> getInBox() {
		return inBox;
	}

	/**
	 * Set List of Presents which are sent to a player's board.
	 * 
	 * @param inBox
	 */
	public void setInBox(List<Present> inBox) {
		this.inBox = inBox;
	}

	/**
	 * If a player accept a specific present of his in-box it will be added to
	 * the List of current presents.
	 * 
	 * @param the
	 *            present which should be accepted
	 * @return the accepted present
	 */
	public Present accept(Present p) {
		if (this.inBox.contains(p)) {
			// this.inBox.remove(p.getPresent());
			// this.currentPresents.add(p);
			this.inBox.remove(p);
			this.currentPresents.add(p);
		} else {
			throw new ApiError(Response.Status.FORBIDDEN, "no such present to accept");
		}
		return p;
	}

	/**
	 * If a player denies specific present of his in-box it will be removed of
	 * the List of the in-box.
	 * 
	 * @param the
	 *            present which should be denied
	 * @return the denied present
	 */
	public Present deny(Present p) {
		if (this.inBox.contains(p)) {
			this.inBox.remove(p);
		} else {
			throw new ApiError(Response.Status.FORBIDDEN, "no such present to accept");
		}
		return p;
	}

	/**
	 * If a player archives a specific present of current ones it will be added
	 * of the List of archived presents.
	 * 
	 * @param the
	 *            present which should be archived
	 * @return the archived present
	 */
	public PresentArchived archive(PresentArchived p) {
		if (this.currentPresents.contains(p)) {
			this.currentPresents.remove(p.getPresent());
			this.archive.add(p);
		} else {
			throw new ApiError(Response.Status.FORBIDDEN, "no such present to archive");
		}
		return p;
	}

	/**
	 * Returns the list of all presents which are archived.
	 * 
	 * @return List of archived presents
	 */
	public List<PresentArchived> showArchive() {
		return this.archive;
	}

	/**
	 * Test if a board belongs to a specific organisation.
	 */
	public boolean belongsTo(Organisation organisation) {
		return getBelongsTo().getApiKey().equals(organisation.getApiKey());
	}

	/**
	 * A sent present is added to the in-box of the board.
	 * 
	 * @param present
	 *            which is sent
	 */
	public void add(Present present) {
		getInBox().add(present);
	}
}
