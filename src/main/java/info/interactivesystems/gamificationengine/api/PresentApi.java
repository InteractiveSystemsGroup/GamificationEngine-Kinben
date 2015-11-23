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
 * Players in a gamification application can send little presents to each other, whereby 
 * one or more players can be a recipient. These presents can be an image or a short text
 * message which contains for example a little praise. A Board serves a player to send and 
 * to store little presents in terms of a short text message or an image. The difference 
 * between these two messages is as the name suggests, that the text message contains a 
 * short text and the image message an image. To archive the presents they can be moved to
 * an additional list. It is possible to get for one player all her/his text messages or all
 * messages with a little image that were created. Furthermore all new presents of player 
 * can be requested as well as the accepted and archived presents. All denies presents were
 * removed from the in-box.
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
	 * Creates a new text message as a present in a gamificated application, so the method 
	 * generates the Present-id. The organisation's API key is mandatory otherwise a warning
	 * with the hint for a non valid API key is returned. 
	 * By the creation the player-id of the sender and a list of the receiver ids are needed to 
	 * be passed. Additionally the content of the text message has to be passed. 
	 * It is checked, if the ids of the players are positive numbers otherwise a message for the
	 * invalid number is returned.
	 * 
	 * @param senderId
	 *            The player who sends the text message as a present to other players. This 
	 *            field must not be null.
	 * @param receiverIds
	 *            The player ids of the present's receivers. This field must not be null.
	 * @param content
	 *            The content of the text message.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this text message belongs to.
	 * @return {@link Response} of {@link TextMessage} in JSON.
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
		
		// TODO move to Player
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
	 * Creates a new image message as a present in a gamificated application, so the method 
	 * generates the Present-id. The organisation's API key is mandatory otherwise a warning
	 * with the hint for a non valid API key is returned. 
	 * By the creation the player-id of the sender and a list of the receiver ids are needed to 
	 * be passed. These id have to be separated by commas. Additionally the image path of the 
	 * image has to be passed. The format of the image has to be .jpg or .png.
	 * It is checked, if the ids of the players are positive numbers otherwise a message for the
	 * invalid number is returned.
	 * 
	 * @param senderId
	 *            The player who sends the image message as a present to other players. This 
	 *            field must not be null.
	 * @param receiverIds
	 *            The player ids of the present's receivers. This field must not be null.
	 * @param imagePath
	 *             The path of the image. This field must not be null and the format of the image
	 *             has to be .jpg or .png.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this image message belongs to.
	 * @return {@link Response} of {@link ImageMessage} in JSON.
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
		// TODO move to Player
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
	 * Removes the specific present with the assigned id from data base. It is checked, if the passed 
	 * id is a positive number otherwise a message for an invalid number is returned. If the API key 
	 * is not valid an analogous message is returned.
	 * 
	 * @param presentId
	 *            Required path parameter as integer which uniquely identify the {@link Present}.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this present belongs to.
	 * @return {@link Response} of {@link Present} in JSON.
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
	 * This method returns all text messages of a specific player's current presents and which are
	 * associated with the given API key and so all text messages which belong to the associated
	 * organisation. If the API key is not valid an analogous message is returned.
	 * 
	 * @param playerId
	 *            The id of the player who owns the board with the current presents. This field 
	 *            must not be null.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the text messages belongs to.
	 * @return {@link Response} of {@link List<TextMessage>} in JSON.
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
	 * This method returns all image messages of a specific player's current presents and which are
	 * associated with the given API key and so all text messages which belong to the associated
	 * organisation. If the API key is not valid an analogous message is returned.
	 * 
	 * @param playerId
	 *            The id of the player who owns the board with the current presents. This field 
	 *            must not be null.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the image messages belongs to.
	 * @return {@link Response} of {@link List<TextMessage>} in JSON.
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
	 * With this method one present is sent to all specified receivers. So the presents is stored 
	 * in the each inbox of the receivers.
	 * 
	 * @param presentId
	 *            The path parameter of the present's id that should be sent to the receivers. 
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this present belongs to.
	 * @return {@link Response} of {@link Present} in JSON.
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
	 * With this method a player accepts a present. So the present will be moved from her/his inbox 
	 * to the list of the player's current presents.
	 * 
	 * @param presentId
	 *            The present that is accepted. This field must not be null.
	 * @param playerId
	 *            The id of the player who accepts the present. This field must not be null.
	 * @param apiKey
	 *             The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this present belongs to.
	 * @return {@link Response} of {@link Present} in JSON.
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
	 * With this method a player denies a present. So the present will be deleted from her/his 
	 * inbox of the board. 
	 * 
	 * @param presentId
	 *            The present that is denied. This field must not be null.
	 * @param playerId
	 *            The id of the player who denies the present. This field must not be null.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this present belongs to.
	 * @return {@link Response} of {@link Present} in JSON.
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
	 * With this method on present is archived. So the present is moved from the player's list of 
	 * current presents to an list of archived presents on the board.
	 * 
	 * @param presentId
	 *            The present that is archived. This field must not be null.
	 * @param playerId
	 *            The id of the player who archived the present. This field must not be null.
	 * @param apiKey
	 *           The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this present belongs to.
	 * @return {@link Response} of {@link Present} in JSON.
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
	 * This method returns all presents of a player's inbox associated with the given API key and so 
	 * all presents who belong to the associated organisation. If the API key is not valid an analogous
	 * message is returned.
	 * 
	 * @param playerId
	 *            The id of the player whose presents are returned. This field must not be null.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this present belongs to.
	 * @return {@link Response} of {@link List<Present>} in JSON.
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
	 * Returns all presents from player's current presents as list of present-objects.
	 * These presents were accepted by the player so they were moved from the inbox to the 
	 * list of current presents.
	 * 
	 * @param playerId
	 *            The id of the player whose current presents are returned. This field must not be null.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this present belongs to.
	 * @return {@link Response} of {@link List<Present>} in JSON.
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
	 * Returns all presents from player's list of archived presents as list of present-objects.
	 * These presents are accepted presents which the player wanted to archive.
	 * 
	 * @param playerId
	 *            The id of the player whose current presents are returned. This field must not be null.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this present belongs to.
	 * @return {@link Response} of {@link List<Present>} in JSON.
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
