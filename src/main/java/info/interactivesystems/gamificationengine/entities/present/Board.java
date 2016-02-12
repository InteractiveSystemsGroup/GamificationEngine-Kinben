package info.interactivesystems.gamificationengine.entities.present;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
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
 * A Board serves a player to store presents in three lists: When a present is
 * sent to a player firstly it is added to the inBox-list. The player decides if
 * she/he accepts the present. If the player does it is added to the list which holds
 * the current presents else if it is denied it is removed from the inbox. When the player
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

	/**
	 * Received presents are stored in the inBox-list. The player can decide if she/he wants to accept or deny each present.
	 */
	@ManyToMany(cascade = CascadeType.PERSIST)
	@JoinTable(name = "board_inBox")
//	@JsonIgnore
	private List<Present> inBox;

	/**
	 * If the player decides to accept a present it is stored in the list of current presents.
	 */
	@ManyToMany(cascade = CascadeType.PERSIST)
	@JoinTable(name = "board_current")
	private List<PresentAccepted> currentPresents;

	/**
	 * If the player wants to archive a present it is stored in the list of archived presents.
	 */
	@ManyToMany(cascade = CascadeType.PERSIST)
	@JoinTable(name = "board_archive")
	private List<PresentArchived> archive;

	@OneToOne
	private Player owner;

	public Board() {
		currentPresents = new ArrayList<>();
		archive = new ArrayList<>();
		inBox = new ArrayList<>();
	}

	/**
	 * Gets the id of a created board.
	 * 
	 * @return The id of the board as int.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of a board.
	 * 
	 * @param id
	 * 			The id of the board as int.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * The organisation the board belongs to.
	 * 
	 * @return The organisation of the board as an object. This parameter is required.
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Sets the organisation the board belongs to. This parameter field must not be null.
	 * 
	 * @param belongsTo
	 *           The organisation of the board.
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Gets all current presents of a player. These are presents which were
	 * accepted before.
	 * 
	 * @return All accepted presents as {@link List} of {@link Present}s. 
	 */
	public List<PresentAccepted> getCurrentPresents() {
		return currentPresents;
	}

	/**
	 * Sets a list of current presents of a player.
	 * 
	 * @param presents
	 * 			The current presents a player has accepted.
	 */
	public void setCurrentPresents(List<PresentAccepted> presents) {
		this.currentPresents = presents;
	}

	/**
	 * Gets the owner of the board, so the owner belongs to.
	 * 
	 * @return The player who is the owner of the board.
	 */
	public Player getOwner() {
		return owner;
	}

	/**
	 * Sets the passed player as the owner of the board.
	 * 
	 * @param owner
	 * 			The new owner of the board.
	 */
	public void setOwner(Player owner) {
		this.owner = owner;
	}

	/**
	 * Gets a list of all archived presents which belongs to one player.
	 * 
	 * @return All archives presents as {@link List} of {@link PresentArchived}.
	 */
	public List<PresentArchived> getArchive() {
		return archive;
	}

	/**
	 * Sets a list of all player's archived presents.
	 * 
	 * @param history
	 * 			Sets the list of archived presents.
	 */
	public void setArchive(List<PresentArchived> history) {
		this.archive = history;
	}

	/**
	 * Gets all presents which were sent to a player and which the player hasn't neither
	 * accepted nor denied, yet.
	 * 
	 * @return All just sent presents as {@link List} of {@link Present}s.
	 */
	public List<Present> getInBox() {
		return inBox;
	}

	/**
	 * Sets a list of presents which are sent to a player's board.
	 * 
	 * @param inBox
	 * 			All just sent presents as the list named inBox.
	 */
	public void setInBox(List<Present> inBox) {
		this.inBox = inBox;
	}

	/**
	 * If a player accepts a specific present of her/his in-Box it will be added to
	 * the list of current presents.
	 * 
	 * @param present
	 * 			The present a player has accepted. 
	 * @return The accepted present as object of Present.
	 */
	public Present acceptAndCreateAcceptedPresent(Present present) {
		if (this.inBox.contains(present)) {
			this.inBox.remove(present);
			
			PresentAccepted accPresent = new PresentAccepted();
			accPresent.setDate(LocalDateTime.now());
			accPresent.setPresent(present);
//			accPresent.setBoard(this);
			accPresent.setBelongsTo(present.getBelongsTo());
			accPresent.setStatus();
			
			this.currentPresents.add(accPresent);
//			this.setInBox(getInBox());
		} else {
			throw new ApiError(Response.Status.FORBIDDEN, "No such present to accept");
		}
		return present;
	}

	/**
	 * If a player denies a specific present of her/his in-Box it will be removed of
	 * the list of the in-box.
	 * 
	 * @param present
	 * 			The present which the player has denied. 
	 * @return The denied present as object of Present.
	 */
	public Present denyPresent(Present present) {
		if (this.inBox.contains(present)) {
			this.inBox.remove(present);
		} else {
			throw new ApiError(Response.Status.FORBIDDEN, "no such present to accept");
		}
		return present;
	}

	/**
	 * If a player archives a specific present of current ones it will be added
	 * to her/his list of archived presents.
	 * 
	 * @param present
	 *         The present which the player wants to archive.
	 * @return The archived present.
	 */
	public PresentAccepted archive(PresentAccepted present) {
		if (this.currentPresents.contains(present)) {
			this.currentPresents.remove(present);
			
			PresentArchived archPresent = new PresentArchived();
			archPresent.setDate(LocalDateTime.now());
			archPresent.setAcceptedPresent(present);
//			aPresent.setBoard(this);
			archPresent.setBelongsTo(present.getBelongsTo());
			
			this.archive.add(archPresent);
		} else {
			throw new ApiError(Response.Status.FORBIDDEN, "no such present to archive");
		}
		return present;
	}

	/**
	 * Returns the list of all presents which are archived.
	 * 
	 * @return The list of archived presents.
	 */
	public List<PresentArchived> showArchive() {
		return this.archive;
	}

	/**
	 * This method checks if a board belongs to a specific organisation. Therefore
	 * it is tested if the organisation's API key matchs the board's API key. 
	 *
	 * @param organisation
	 * 			The organisation object a board may belong to.
	 * @return Boolean value if the API key of the board is the same 
	 * 			of the tested organisation (true) or not (false).
	 */
	public boolean belongsTo(Organisation organisation) {
		return getBelongsTo().getApiKey().equals(organisation.getApiKey());
	}

	/**
	 * The sent present is added to the in-box of the board.
	 * 
	 * @param present
	 *           The present that is just sent and added to the inBox of the board.
	 */
	public void add(Present present) {
		getInBox().add(present);
	}
	
	
	/**
	 * This method checks, if a passed board exists in the database. Otherwise an error message is sent.
	 * 
	 * @param board
	 * 			The board that has to be checked.
	 */
	public void checkBoardExists(Board board){
		if(board == null){
			throw new ApiError(Response.Status.NOT_FOUND, "Player hasn't a board with presents taht can be accepted.");
		}
	}
	
	/**
	 * This method gets all message of the type TextMessage which were accepted by the player.
	 *  
	 * @param presents
	 * 				All currently accepted presents of a player.
	 * @return A list with all text messages of the list of accepted presents.
	 */
	public List<TextMessage> filterTextMessages(List<PresentAccepted> presents){
		List<TextMessage> textMList = new ArrayList<>();
		for (PresentAccepted present : presents) {
			if (present.getPresent() instanceof TextMessage) {
				textMList.add((TextMessage) present.getPresent());
			}
		}
		return textMList;
	}

	/**
	 * This method gets all message of the type ImageMessage which were accepted by the player.
	 *  
	 * @param presents
	 * 				All currently accepted presents of a player.
	 * @return A list with all text messages of the list of accepted presents.
	 */
	public List<ImageMessage> filterImageMessages(List<PresentAccepted> presents) {
		List<ImageMessage> imMessageList = new ArrayList<>();
		for (PresentAccepted present : presents) {
			if (present.getPresent() instanceof ImageMessage) {
				imMessageList.add((ImageMessage) present.getPresent());
			}
		}
		return imMessageList;
	}
	
}
