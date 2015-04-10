package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
import info.interactivesystems.gamificationengine.api.validation.ValidListOfDigits;
import info.interactivesystems.gamificationengine.api.validation.ValidPositiveDigit;
import info.interactivesystems.gamificationengine.dao.BoardDAO;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.dao.PlayerDAO;
import info.interactivesystems.gamificationengine.dao.PresentDAO;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.present.Board;
import info.interactivesystems.gamificationengine.entities.present.ImageMessage;
import info.interactivesystems.gamificationengine.entities.present.Present;
import info.interactivesystems.gamificationengine.entities.present.PresentArchived;
import info.interactivesystems.gamificationengine.entities.present.TextMessage;
import info.interactivesystems.gamificationengine.utils.ImageUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API for present related services.
 */
@Path("/present")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class PresentApi {

	private static final Logger log = LoggerFactory.getLogger(TaskApi.class);

	@Inject
	OrganisationDAO organisationDao;
	@Inject
	PlayerDAO playerDao;
	@Inject
	PresentDAO presentDao;
	@Inject
	BoardDAO boardDao;

	/**
	 * Creates a new text message as a present in a gamificated app.
	 * 
	 * @param senderId
	 *            required id of the sender of the present
	 * @param receiverIds
	 *            required list of ids which represents the receivers of the
	 *            present
	 * @param content
	 *            required content of the text message
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link TextMessage} in JSON
	 */
	@POST
	@Path("/textMessage")
	public Response createTextMessage(
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The sender id must be a valid number") String senderId,
			@QueryParam("receiverIds") @NotNull @ValidListOfDigits String receiverIds, @QueryParam("content") String content,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("create New TestMessage called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Player sender = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(senderId), apiKey);

		List<Player> receivers = new ArrayList<>();
		// TODO in Player verschieben
		if (receiverIds.contains(",")) {
			String[] receiverList = receiverIds.split(",");

			for (String receiverIdString : receiverList) {
				Player playerRec = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(receiverIdString), apiKey);
				if (playerRec != null) {
					receivers.add(playerRec);
					log.debug(playerRec.getId() + "player added as receiver.");
				}
			}
		} else {
			Player playerRec = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(receiverIds), apiKey);
			if (playerRec != null) {
				receivers.add(playerRec);
				log.debug("player added");
			}
		}

		TextMessage message = new TextMessage();
		message.setBelongsTo(organisation);
		message.setReceiver(receivers);
		message.setSender(sender);
		message.setContent(content);

		presentDao.insertPresent(message);
		return ResponseSurrogate.created(message);
	}

	/**
	 * Creates a new image message as a present in a gamificated app.
	 * 
	 * @param senderId
	 *            required id of the sender of the present
	 * @param receiverIds
	 *            required list of ids which represents the receivers of the
	 *            present
	 * @param imagePath
	 *            required path for the image present
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link ImageMessage} in JSON
	 */
	@POST
	@Path("/imageMessage")
	public Response createImageMessage(
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The sender id must be a valid number") String senderId,
			@QueryParam("receiverIds") @NotNull @ValidListOfDigits String receiverIds, @QueryParam("imagePath") @NotNull String imagePath,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("createNew ImageMessage called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Player sender = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(senderId), apiKey);

		List<Player> receivers = new ArrayList<>();
		// TODO in Player verschieben
		if (receiverIds.contains(",")) {
			String[] receiverList = receiverIds.split(",");

			for (String receiverIdString : receiverList) {
				Player playerRec = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(receiverIdString), apiKey);
				if (playerRec != null) {
					receivers.add(playerRec);
					log.debug(playerRec.getId() + " player added as receiver.");
				}
			}
		} else {
			Player playerRec = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(receiverIds), apiKey);
			if (playerRec != null) {
				receivers.add(playerRec);
				log.debug(playerRec.getId() + " player added");
			}
		}

		ImageMessage iMessage = new ImageMessage();
		iMessage.setBelongsTo(organisation);
		iMessage.setReceiver(receivers);
		iMessage.setSender(sender);
		try {
			URL icon = new URL(imagePath);
			iMessage.setImageIcon(ImageUtils.imageToByte(imagePath));
		} catch (MalformedURLException e) {
			throw new ApiError(Response.Status.FORBIDDEN, "no valid url was transferred");
		}

		presentDao.insertPresent(iMessage);

		return ResponseSurrogate.created(iMessage);
	}

	/**
	 * Deletes a specific present.
	 * 
	 * @param presentId
	 *            required id for the present which should be deleted
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Present} in JSON
	 */
	@DELETE
	@Path("/{id}")
	public Response deletePresent(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The present id must be a valid number") String presentId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("delete Present called");

		if (presentId == null) {
			throw new ApiError(Response.Status.FORBIDDEN, "no presentId transferred");
		}

		int id = ValidateUtils.requireGreaterThenZero(presentId);
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Present present = presentDao.getPresentByIdAndOrganisation(id, organisation);

		if (present == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No such Present: " + presentId);
		}

		presentDao.deleteP(present);

		return ResponseSurrogate.deleted(present);
	}

	/**
	 * Returns all text messages among a specific player's current presents.
	 * 
	 * @param playerId
	 *            required player id who is the owner of the board
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link List<TextMessage>} in JSON
	 */
	@GET
	@Path("/boardMessages")
	public Response getTextMessage(
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("getMessages called");

		Player player = playerDao.getPlayer(Integer.valueOf(playerId), apiKey);
		Board board = boardDao.getBoard(Integer.valueOf(playerId), apiKey);
		if (board == null) {
			board = new Board();
			board.setOwner(player);
			board.setBelongsTo(player.getBelongsTo());
			boardDao.persist(board);
		}

		List<Present> presents = board.getCurrentPresents();
		List<TextMessage> textMList = new ArrayList<>();

		// for (PresentAccepted present : presents) {
		// Present p = present.getPresent();
		// if (p instanceof TextMessage) {
		// textMList.add((TextMessage) p);
		// }
		// }

		for (Present present : presents) {
			if (present instanceof TextMessage) {
				textMList.add((TextMessage) present);
			}
		}

		for (TextMessage textMessage : textMList) {
			log.debug("TextId:" + textMessage.getId());
		}

		return ResponseSurrogate.of(presents);
	}

	/**
	 * Returns all image messages among a specific player's current presents.
	 * 
	 * @param playerId
	 *            required player id who is the owner of the board
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link List<ImageMessage>} in JSON
	 */
	@GET
	@Path("/imageMessages")
	public Response getImageMessages(
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("getImageMessages called");

		Player player = playerDao.getPlayer(Integer.valueOf(playerId), apiKey);
		Board board = boardDao.getBoard(Integer.valueOf(playerId), apiKey);
		if (board == null) {
			board = new Board();
			board.setOwner(player);
			board.setBelongsTo(player.getBelongsTo());
			boardDao.persist(board);
		}

		List<Present> presents = board.getCurrentPresents();
		List<ImageMessage> imMessageList = new ArrayList<>();

		// for (PresentAccepted present : presents) {
		// Present p = present.getPresent();
		// if (p instanceof ImageMessage) {
		// imMessageList.add((ImageMessage) p);
		// }
		// }
		for (Present present : presents) {
			if (present instanceof ImageMessage) {
				imMessageList.add((ImageMessage) present);
			}
		}

		return ResponseSurrogate.of(imMessageList);
	}

	/**
	 * Sends one present to all specified receivers.
	 * 
	 * @param presentId
	 *            required id of the present to send
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Present} in JSON
	 */
	@POST
	@Path("/send")
	public Response send(@QueryParam("presentId") @NotNull @ValidPositiveDigit(message = "The present id must be a valid number") String presentId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("send a Present called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Present present = presentDao.getPresentByIdAndOrganisation(ValidateUtils.requireGreaterThenZero(presentId), organisation);

		if (present == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No present to send.");
		}

		List<Player> receivers = present.getReceiver();
		List<Board> boards = boardDao.getBoards(receivers, apiKey);

		List<Player> copyRecievers = new ArrayList<>(receivers);
		copyRecievers.removeAll(boards.stream().map(Board::getOwner).collect(Collectors.toList()));
		if (!copyRecievers.isEmpty()) {
			for (Player player : copyRecievers) {
				Board board = new Board();
				board.setOwner(player);
				board.setBelongsTo(organisation);
				boardDao.persist(board);
				boards.add(board);
			}
		}

		log.debug("present id: " + present.getId());

		for (Player player : receivers) {
			log.debug("receivers sind: " + player.getId());
		}

		for (int i = 0; i < receivers.size() - 1; i++) {
			for (int j = i + 1; j < receivers.size(); j++) {
				if (receivers.get(i).equals(receivers.get(j))) {
					receivers.remove(j);
				}
			}
		}

		for (Player player : receivers) {
			log.debug("bereinigte receivers sind: " + player.getId());
		}

		for (Board board : boards) {
			log.debug("board: " + board.getId());
			board.add(present);
		}

		presentDao.insert(boards);
		return ResponseSurrogate.created(present);
	}

	/**
	 * Player accepts a present, so that it will be moved from his inbox to the
	 * current presents-list.
	 * 
	 * @param presentId
	 *            required id of the present which is accepted
	 * @param playerId
	 *            required player id who accepts the present
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Present} in JSON
	 */
	@POST
	@Path("/accept")
	public Response acceptPresent(
			@QueryParam("presentId") @NotNull @ValidPositiveDigit(message = "The present id must be a valid number") String presentId,
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("receive a Present called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Present present = presentDao.getPresentByIdAndOrganisation(ValidateUtils.requireGreaterThenZero(presentId), organisation);

		if (present == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No such present to accept.");
		}
		log.debug("present id: " + present.getId());

		Player player = playerDao.getPlayer(Integer.valueOf(playerId), apiKey);
		Board board = boardDao.getBoard(Integer.valueOf(playerId), apiKey);
		if (board == null) {
			board = new Board();
			board.setOwner(player);
			board.setBelongsTo(player.getBelongsTo());
			boardDao.persist(board);
		}
		log.debug("Board " + board.getId());

		// PresentAccepted accPresent = new PresentAccepted();
		// accPresent.setDate(LocalDateTime.now());
		// accPresent.setPresent(present);
		// accPresent.setBoard(board);
		// accPresent.setBelongsTo(organisation);
		// accPresent.setStatus();

		// board.accept(accPresent);
		board.accept(present);

		boardDao.persist(board);
		return ResponseSurrogate.created(present);
	}

	/**
	 * Player denies a present, so that it will be deleted from his inbox of the
	 * board.
	 * 
	 * @param presentId
	 *            required present id which is denied
	 * @param playerId
	 *            required player id who denies the present
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Present} in JSON
	 */
	@POST
	@Path("/deny")
	public Response denyPresent(
			@QueryParam("presentId") @NotNull @ValidPositiveDigit(message = "The present id must be a valid number") String presentId,
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("deny a Present called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Present present = presentDao.getPresentByIdAndOrganisation(ValidateUtils.requireGreaterThenZero(presentId), organisation);

		if (present == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No present to deny.");
		}

		log.debug("present id: " + present.getId());

		Player player = playerDao.getPlayer(Integer.valueOf(playerId), apiKey);
		Board board = boardDao.getBoard(Integer.valueOf(playerId), apiKey);
		if (board == null) {
			board = new Board();
			board.setOwner(player);
			board.setBelongsTo(player.getBelongsTo());
			boardDao.persist(board);
		}
		log.debug("Board " + board.getId());

		board.deny(present);

		boardDao.persist(board);
		return ResponseSurrogate.created(present);
	}

	/**
	 * Moves one present from the current present-List to an archive on the
	 * board.
	 * 
	 * @param presentId
	 *            required id of the present which is archived
	 * @param playerId
	 *            required player id who archives the present
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Present} in JSON
	 */
	@POST
	@Path("/archive")
	public Response archivePresent(
			@QueryParam("presentId") @NotNull @ValidPositiveDigit(message = "The present id must be a valid number") String presentId,
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("archive a Present called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Present present = presentDao.getPresentByIdAndOrganisation(ValidateUtils.requireGreaterThenZero(presentId), organisation);

		if (present == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No present to archive.");
		}

		log.debug("present id: " + present.getId());

		Player player = playerDao.getPlayer(Integer.valueOf(playerId), apiKey);
		Board board = boardDao.getBoard(Integer.valueOf(playerId), apiKey);
		if (board == null) {
			board = new Board();
			board.setOwner(player);
			board.setBelongsTo(player.getBelongsTo());
			boardDao.persist(board);
		}
		log.debug("Board " + board.getId());

		PresentArchived aPresent = new PresentArchived();
		aPresent.setDate(LocalDateTime.now());
		aPresent.setPresent(present);
		aPresent.setBoard(board);
		aPresent.setBelongsTo(organisation);

		board.archive(aPresent);

		boardDao.persist(board);
		return ResponseSurrogate.created(present);
	}

	/**
	 * Returns all presents from a player's inbox as a list of present-objects.
	 * 
	 * @param playerId
	 *            required player id whose presents are returned
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link List<Present>} in JSON
	 */
	@GET
	@Path("/inbox")
	public Response getInbox(@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("get inbox");

		Player player = playerDao.getPlayer(Integer.valueOf(playerId), apiKey);
		Board board = boardDao.getBoard(Integer.valueOf(playerId), apiKey);
		if (board == null) {
			board = new Board();
			board.setOwner(player);
			board.setBelongsTo(player.getBelongsTo());
			boardDao.persist(board);
		}
		log.debug("Board " + board.getId());

		boardDao.persist(board);
		return ResponseSurrogate.of(board.getInBox());
	}

	/**
	 * 
	 * Returns all presents from player's current presents as list of
	 * present-objects.
	 * 
	 * @param playerId
	 *            required player id whose current presents are returned
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link List<Present>} in JSON
	 */
	@GET
	@Path("/current")
	public Response getCurrent(
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("get current presents");

		Player player = playerDao.getPlayer(Integer.valueOf(playerId), apiKey);
		Board board = boardDao.getBoard(Integer.valueOf(playerId), apiKey);
		if (board == null) {
			board = new Board();
			board.setOwner(player);
			board.setBelongsTo(player.getBelongsTo());
			boardDao.persist(board);
		}
		log.debug("Board " + board.getId());

		boardDao.persist(board);
		return ResponseSurrogate.of(board.getCurrentPresents());
	}

	/**
	 * Returns all presents from player's archived list as list of
	 * present-objects.
	 * 
	 * @param playerId
	 *            required player id whose presents are returned
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link List<Present>} in JSON
	 */
	@GET
	@Path("/archive")
	public Response getArchive(
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("get archived presents");

		Player player = playerDao.getPlayer(Integer.valueOf(playerId), apiKey);
		Board board = boardDao.getBoard(Integer.valueOf(playerId), apiKey);
		if (board == null) {
			board = new Board();
			board.setOwner(player);
			board.setBelongsTo(player.getBelongsTo());
			boardDao.persist(board);
		}

		boardDao.persist(board);
		return ResponseSurrogate.of(board.getArchive());
	}
}
