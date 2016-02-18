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
import info.interactivesystems.gamificationengine.entities.present.PresentAccepted;
import info.interactivesystems.gamificationengine.entities.present.PresentArchived;
import info.interactivesystems.gamificationengine.entities.present.TextMessage;
import info.interactivesystems.gamificationengine.utils.ImageUtils;
import info.interactivesystems.gamificationengine.utils.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * Players in a gamification application can send presents to each other, whereby 
 * one or more players can be a recipient. These presents can be a small image or a short text
 * message which contains for example a little praise. A Board serves a player to send and 
 * to store presents in terms of a short text message or an image. The difference 
 * between these two messages is as the name suggests, that the text message contains a 
 * short text and the image message an image. To archive the presents they can be moved to
 * an additional list. It is possible to get for one player all her/his text messages or all
 * messages with a small image that were created. Furthermore all new presents of player 
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
	 * @return Response of TextMessage in JSON.
	 */
	@POST
	@Path("/textMessage")
	@TypeHint(TextMessage.class)
	public Response createTextMessage(
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The sender id must be a valid number") String senderId,
			@QueryParam("receiverIds") @NotNull @ValidListOfDigits String receiverIds, 
			@QueryParam("content") String content,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("create New TestMessage called");
		
		int sendId = ValidateUtils.requireGreaterThanZero(senderId);
		
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Player sender = playerDao.getPlayer(ValidateUtils.requireGreaterThanZero(senderId), apiKey);
		ValidateUtils.requireNotNull(sendId, sender);
		
		List<Player> receivers = new ArrayList<>();
		receivers = receiverList(receiverIds, apiKey);
		
		for (Player player : receivers) {
			log.debug("Receivers: " + player.getId());
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
	 * @param textMessage
	 * 			A short text comment that belong to the image.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this image message belongs to.
	 * @return Response of ImageMessage in JSON.
	 */
	@POST
	@Path("/imageMessage")
	@TypeHint(ImageMessage.class)
	public Response createImageMessage(
			@QueryParam("playerId") @NotNull @ValidPositiveDigit(message = "The sender id must be a valid number") String senderId,
			@QueryParam("receiverIds") @NotNull @ValidListOfDigits String receiverIds, 
			@QueryParam("imagePath") @NotNull String imagePath,
			@QueryParam("text") @NotNull String textMessage,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("createNew ImageMessage called");

		int sendId = ValidateUtils.requireGreaterThanZero(senderId);
		
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Player sender = playerDao.getPlayer(ValidateUtils.requireGreaterThanZero(senderId), apiKey);
		ValidateUtils.requireNotNull(sendId, sender);
		
		List<Player> receivers = new ArrayList<>();
		
		receivers = receiverList(receiverIds, apiKey);
		for (Player player : receivers) {
			log.debug("Receivers: " + player.getId());
		} 
		
		ImageMessage iMessage = new ImageMessage();
		iMessage.setBelongsTo(organisation);
		iMessage.setReceiver(receivers);
		iMessage.setSender(sender);
		iMessage.setMessage(textMessage);
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
	 * This method converts the string of receiver ids which are transfered to a list of players.
	 * These players are then set as the list of receivers a present has. 
	 * 
	 * @param commaSeparatedList
	 * 			The list of receivers as string separated by commas. This parameter is 
	 * 		   	required.
	 * @param apiKey
	 * 			The valid query parameter API key affiliated to one specific organisation, 
	 *        	to which this present belongs to.
	 * @return Returns a list of Players.
	 */
	private List<Player> receiverList(String commaSeparatedList, String apiKey) {
		StringUtils.validateAsListOfDigits(commaSeparatedList);
		List<Integer> ids = StringUtils.stringArrayToIntegerList(commaSeparatedList);
		List<Player> receivers = playerDao.getPlayers(ids, apiKey);
		return receivers;
	}
	
	

	/**
	 * This method returns all already accepted messages of a specific player's current presents.  
	 * If the API key is not valid an analogous message is returned.
	 * 
	 * @param playerId
	 *            The id of the player who owns the board with the current presents. This field 
	 *            must not be null.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the messages belongs to.
	 * @return Response as List of Presents in JSON.
	 * 
	 */
	@GET
	@Path("/{playerId}/boardMessages")
	@TypeHint(PresentAccepted[].class)
	public Response getCurrentBoardMessages(
			@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("getboardMessages called");

		int playId = ValidateUtils.requireGreaterThanZero(playerId);
		
		Player player = playerDao.getPlayer(playId, apiKey);
		ValidateUtils.requireNotNull(playId, player);
		
		Board board = boardDao.getBoard(Integer.valueOf(playerId), apiKey);
		if (board == null) {
			board = new Board();
			board.setOwner(player);
			board.setBelongsTo(player.getBelongsTo());
			boardDao.persist(board);
		}

		List<PresentAccepted> presents = board.getCurrentPresents();
		
		return ResponseSurrogate.of(presents);
	}
	
	
	/**
	 * This method returns all text messages of a specific player's current presents. 
	 * If the API key is not valid an analogous message is returned.
	 * 
	 * @param playerId
	 *            The id of the player who owns the board with the current presents. This field 
	 *            must not be null.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the text messages belongs to.
	 * @return Response as List of TextMessages in JSON.
	 */
	@GET
	@Path("/{playerId}/textMessages")
	@TypeHint(TextMessage[].class)
	public Response getCurrentTextMessage(
			@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("getMessages called");

		int playId = ValidateUtils.requireGreaterThanZero(playerId);
		Player player = playerDao.getPlayer(playId, apiKey);
		ValidateUtils.requireNotNull(playId, player);
		
		Board board = boardDao.getBoard(Integer.valueOf(playerId), apiKey);
		if (board == null) {
			board = new Board();
			board.setOwner(player);
			board.setBelongsTo(player.getBelongsTo());
			boardDao.persist(board);
		}

		List<PresentAccepted> presents = board.getCurrentPresents();
		List<TextMessage> currentTextMessages = board.filterTextMessages(presents); 

		return ResponseSurrogate.of(currentTextMessages);
	}

	/**
	 * This method returns all image messages of a specific player's current presents.
	 * If the API key is not valid an analogous message is returned.
	 * 
	 * @param playerId
	 *            The id of the player who owns the board with the current presents. This field 
	 *            must not be null.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the image messages belongs to.
	 * @return Response as List of ImageMessages in JSON.
	 */
	@GET
	@Path("/{playerId}/imageMessages")
	@TypeHint(ImageMessage[].class)
	public Response getCurrentImageMessages(
			@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("getImageMessages called");

		int playId = ValidateUtils.requireGreaterThanZero(playerId);
		Player player = playerDao.getPlayer(playId, apiKey);
		ValidateUtils.requireNotNull(playId, player);
		
		Board board = boardDao.getBoard(Integer.valueOf(playerId), apiKey);
		if (board == null) {
			board = new Board();
			board.setOwner(player);
			board.setBelongsTo(player.getBelongsTo());
			boardDao.persist(board);
		}

		List<PresentAccepted> presents = board.getCurrentPresents();
		List<ImageMessage> currentImageMessages = board.filterImageMessages(presents); 

		return ResponseSurrogate.of(currentImageMessages);
	}
	
	
	/**
	 * With this method one present is sent to all specified receivers. So the present is stored 
	 * in each inbox of the receivers.
	 * 
	 * @param presentId
	 *            The path parameter of the present's id that should be sent to the receivers. 
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this present belongs to.
	 * @return Response of Present in JSON.
	 */
	@POST
	@Path("/{presentId}/send")
	@TypeHint(Present.class)
	public Response send(@PathParam("presentId") @NotNull @ValidPositiveDigit(message = "The present id must be a valid number") String presentId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("send a Present called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		int presId = ValidateUtils.requireGreaterThanZero(presentId);
		Present present = presentDao.getPresentByIdAndOrganisation(presId, organisation);
		ValidateUtils.requireNotNull(presId, present);
		
		log.debug("Receivers player: " + present.getId());
		
		Set<Player> receiverSet = new HashSet<Player>(present.getReceiver());
		List<Player> receivers = new ArrayList<>();
		receivers.addAll(receiverSet);
		
		List<Board> boards = boardDao.getBoards(receivers, apiKey);

		log.debug("List Boards Receivers: ");
		for (Board b : boards) {
			log.debug("Copy Receivers: " + b.getOwner().getId());
		} 
		
		List<Player> copyRecievers = new ArrayList<Player>(receivers);
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
			log.debug("receivers are: " + player.getId());
		}

		for (Player player : receivers) {
			log.debug("receivers are: " + player.getId());
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
	 * @return Response of Present in JSON.
	 */
	@POST
	@Path("/{presentId}/accept/{playerId}")
	@TypeHint(Present.class)
	public Response acceptPresent(
			@PathParam("presentId") @NotNull @ValidPositiveDigit(message = "The present id must be a valid number") String presentId,
			@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("receive a Present called");

		int presId = ValidateUtils.requireGreaterThanZero(presentId);
		int playId = ValidateUtils.requireGreaterThanZero(playerId);
		
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Present present = presentDao.getPresentByIdAndOrganisation(presId, organisation);
		ValidateUtils.requireNotNull(presId, present);
		
		log.debug("present id: " + present.getId());

		Player player = playerDao.getPlayer(playId, apiKey);
		ValidateUtils.requireNotNull(playId, player);
		
		Board board = boardDao.getBoard(Integer.valueOf(playerId), apiKey);
		board.checkBoardExists(board);
		
		log.debug("Board " + board.getId());

		board.acceptAndCreateAcceptedPresent(present);

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
	 * @return Response of Present in JSON.
	 */
	@POST
	@Path("/{presentId}/deny/{playerId}")
	@TypeHint(Present.class)
	public Response denyPresent(
			@PathParam("presentId") @NotNull @ValidPositiveDigit(message = "The present id must be a valid number") String presentId,
			@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("deny a Present called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Present present = presentDao.getPresentByIdAndOrganisation(ValidateUtils.requireGreaterThanZero(presentId), organisation);
		if (present == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No present to deny.");
		}

		log.debug("present id: " + present.getId());

		Player player = playerDao.getPlayer(Integer.valueOf(playerId), apiKey);
		if(player == null){
			throw new ApiError(Response.Status.FORBIDDEN, "a player with this id doesn't exist");
		}
		
		Board board = boardDao.getBoard(Integer.valueOf(playerId), apiKey);
		if(board == null){
			throw new ApiError(Response.Status.NOT_FOUND, "Player hasn't a board with presents that can be accepted.");
		}
		
		log.debug("Board " + board.getId());

		board.denyPresent(present);

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
	 * @return Response of Present in JSON.
	 */
	@POST
	@Path("/{presentId}/archive/{playerId}")
	@TypeHint(Present.class)
	public Response archivePresent(
			@PathParam("presentId") @NotNull @ValidPositiveDigit(message = "The present id must be a valid number") String presentId,
			@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("archive a Present called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		PresentAccepted accPresent = presentDao.getPresentAcceptedByIdAndOrganisation(ValidateUtils.requireGreaterThanZero(presentId), organisation);
		if (accPresent == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "No present to archive.");
		}

		log.debug("present id: " + accPresent.getId());

		Player player = playerDao.getPlayer(Integer.valueOf(playerId), apiKey);
		if(player == null){
			throw new ApiError(Response.Status.FORBIDDEN, "a player with this id doesn't exist");
		}
		
		Board board = boardDao.getBoard(Integer.valueOf(playerId), apiKey);
		board.checkBoardExists(board);
		
		board.archive(accPresent);
		boardDao.persist(board);
		
		return ResponseSurrogate.created(accPresent);
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
	 * @return Response as List of Presents in JSON.
	 */
	@GET
	@Path("/{playerId}/inbox")
	@TypeHint(Present[].class)
	public Response getInbox(@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("get inbox");

		Player player = playerDao.getPlayer(Integer.valueOf(playerId), apiKey);
		
		if(player == null){
			throw new ApiError(Response.Status.FORBIDDEN, "a player with this id doesn't exist");
		}
		
		Board board = boardDao.getBoard(Integer.valueOf(playerId), apiKey);
		if (board == null) {
			board = new Board();
			board.setOwner(player);
			board.setBelongsTo(player.getBelongsTo());
//			boardDao.persist(board);
		}
		log.debug("Board " + board.getId());

		List<Present> presents = board.getInBox();
		
		boardDao.persist(board);
		return ResponseSurrogate.of(presents);
	}


	/**
	 * This method returns all already archived messages of a specific player's presents.  
	 * If the API key is not valid an analogous message is returned.
	 * 
	 * @param playerId
	 *            The id of the player who owns the board. This field must not be null.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which the messages belongs to.
	 * @return Response as List of PresentArchiveds in JSON.
	 */
	@GET
	@Path("/{playerId}/archive")
	@TypeHint(PresentArchived[].class)
	public Response getArchiveMessages(
			@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("get archived Messages called");

		Player player = playerDao.getPlayer(Integer.valueOf(playerId), apiKey);
		
		if(player == null){
			throw new ApiError(Response.Status.FORBIDDEN, "a player with this id doesn't exist");
		}
		
		Board board = boardDao.getBoard(Integer.valueOf(playerId), apiKey);
		if (board == null) {
			board = new Board();
			board.setOwner(player);
			board.setBelongsTo(player.getBelongsTo());
			boardDao.persist(board);
		}

		List<PresentArchived> presents = board.getArchive();
		
		return ResponseSurrogate.of(presents);
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
	 * @return Response of Present in JSON.
	 */
	@DELETE
	@Path("/{id}")
	@TypeHint(Present.class)
	public Response deletePresent(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The present id must be a valid number") String presentId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("delete Present called");

		int id = ValidateUtils.requireGreaterThanZero(presentId);
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		
		Present present = presentDao.getPresentByIdAndOrganisation(id, organisation);
		ValidateUtils.requireNotNull(id, present);

		presentDao.deletePresent(present);

		return ResponseSurrogate.deleted(present);
	}
	
	/**
	 * Removes the specific present with the assigned id from data base. It is checked, if the passed 
	 * id is a positive number otherwise a message for an invalid number is returned. If the API key 
	 * is not valid an analogous message is returned.
	 * 
	 * @param presentId
	 *            Required path parameter as integer which uniquely identify the {@link Present}.
	 * @param playerId
	 * 			The present is removed from the player's board.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this present belongs to.
	 * @return Response of Present in JSON.
	 */
	@DELETE
	@Path("/{id}/deleteCurrent/{playerId}")
	@TypeHint(PresentAccepted.class)
	public Response deleteCurrentPresent(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The present id must be a valid number") String presentId,
			@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("delete Present called");

		int presId = ValidateUtils.requireGreaterThanZero(presentId);
		int playId = ValidateUtils.requireGreaterThanZero(playerId);
		
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		
		PresentAccepted accPresent = presentDao.getPresentAcceptedByIdAndOrganisation(presId, organisation);
		ValidateUtils.requireNotNull(presId, accPresent);
		
		Player player = playerDao.getPlayer(Integer.valueOf(playerId), apiKey);
		ValidateUtils.requireNotNull(playId, player);
		
		Board board = boardDao.getBoard(Integer.valueOf(playId), apiKey);
		board.checkBoardExists(board);
		
		//deletes first the present from the list of current presents and than from the database 
		board.removeAcceptedPresent(accPresent);
		presentDao.deletePresent(accPresent);
		boardDao.persist(board);
		
		return ResponseSurrogate.deleted(accPresent);
	}
	
	/**
	 * Removes the specific present with the assigned id from data base and the from the board's list of archived 
	 * presents. It is checked, if the passed id is a positive number otherwise a message for an invalid number 
	 * is returned. If the API key is not valid an analogous message is returned.
	 * 
	 * @param presentId
	 *            Required path parameter as integer which uniquely identify the {@link Present}.
	 * @param playerId
	 * 			The present is removed from the player's board.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this present belongs to.
	 * @return Response of Present in JSON.
	 */
	@DELETE
	@Path("/{id}/deleteArchived/{playerId}")
	@TypeHint(PresentArchived.class)
	public Response deleteArchivedPresent(@PathParam("id") @NotNull @ValidPositiveDigit(message = "The present id must be a valid number") String presentId,
			@PathParam("playerId") @NotNull @ValidPositiveDigit(message = "The player id must be a valid number") String playerId,
			@QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("delete Present called");

		int presId = ValidateUtils.requireGreaterThanZero(presentId);
		int playId = ValidateUtils.requireGreaterThanZero(playerId);
		
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		
		PresentArchived archPresent = presentDao.getPresentArchivedByIdAndOrganisation(presId, organisation);
		ValidateUtils.requireNotNull(presId, archPresent);
		
		Player player = playerDao.getPlayer(Integer.valueOf(playerId), apiKey);
		ValidateUtils.requireNotNull(playId, player);
		
		Board board = boardDao.getBoard(Integer.valueOf(playId), apiKey);
		board.checkBoardExists(board);
		
		//deletes first the present from the list of archived presents and than from the database 
		board.removeArchivedPresent(archPresent);
		presentDao.deletePresent(archPresent);
		boardDao.persist(board);
		
		return ResponseSurrogate.deleted(archPresent);
	}
}
