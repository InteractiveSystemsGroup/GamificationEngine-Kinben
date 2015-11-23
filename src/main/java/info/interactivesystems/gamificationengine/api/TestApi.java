package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.dao.AccountDAO;
import info.interactivesystems.gamificationengine.dao.BoardDAO;
import info.interactivesystems.gamificationengine.dao.GoalDAO;
import info.interactivesystems.gamificationengine.dao.MarketPlaceDAO;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.dao.PlayerDAO;
import info.interactivesystems.gamificationengine.dao.PlayerGroupDAO;
import info.interactivesystems.gamificationengine.dao.PresentDAO;
import info.interactivesystems.gamificationengine.dao.RewardDAO;
import info.interactivesystems.gamificationengine.dao.RoleDAO;
import info.interactivesystems.gamificationengine.dao.RuleDAO;
import info.interactivesystems.gamificationengine.dao.TaskDAO;
import info.interactivesystems.gamificationengine.entities.Account;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.PlayerGroup;
import info.interactivesystems.gamificationengine.entities.Role;
import info.interactivesystems.gamificationengine.entities.goal.DoAllTasksRule;
import info.interactivesystems.gamificationengine.entities.goal.DoAnyTaskRule;
import info.interactivesystems.gamificationengine.entities.goal.Goal;
import info.interactivesystems.gamificationengine.entities.goal.GoalRule;
import info.interactivesystems.gamificationengine.entities.goal.TaskRule;
import info.interactivesystems.gamificationengine.entities.marketPlace.MarketPlace;
import info.interactivesystems.gamificationengine.entities.marketPlace.Offer;
import info.interactivesystems.gamificationengine.entities.present.Board;
import info.interactivesystems.gamificationengine.entities.rewards.Achievement;
import info.interactivesystems.gamificationengine.entities.rewards.Badge;
import info.interactivesystems.gamificationengine.entities.rewards.Coins;
import info.interactivesystems.gamificationengine.entities.rewards.Points;
import info.interactivesystems.gamificationengine.entities.rewards.ReceiveLevel;
import info.interactivesystems.gamificationengine.entities.rewards.Reward;
import info.interactivesystems.gamificationengine.entities.task.Task;
import info.interactivesystems.gamificationengine.utils.ImageUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API class for application information. This class is used while developing to 
 * generate test data.
 */
@Path("/")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class TestApi {

	private static final Logger log = LoggerFactory.getLogger(TestApi.class);

	@Inject
	AccountDAO accountDao;
	@Inject
	OrganisationDAO organisationDao;
	@Inject
	GoalDAO goalDao;
	@Inject
	PlayerDAO playerDao;
	@Inject
	RewardDAO rewardDao;
	@Inject
	RuleDAO ruleDao;
	@Inject
	TaskDAO taskDao;
	@Inject
	RoleDAO roleDao;
	@Inject
	PlayerGroupDAO groupDao;
	@Inject
	MarketPlaceDAO marketPlaceDao;
	@Inject
	PresentDAO presentDao;
	@Inject
	BoardDAO boardDAO;

	@POST
	@Path("/createTestData")
	public Response createData() {

		Account newAcc = createAccount("test@example.com", "123456");
		Organisation newOrg = createOrganisation(newAcc);

		Role role1 = createNewRole(newOrg, "Users");
		Role role2 = createNewRole(newOrg, "Teamleader");
		Role role3 = createNewRole(newOrg, "Superuser");
		List<Role> roleList1 = new ArrayList<>();
		roleList1.add(role2);
		roleList1.add(role3);
		List<Role> roleList2 = new ArrayList<>();
		// roleList.add(role1);
		roleList2.add(role3);

		Player player1 = createPlayer(newOrg, "123456", "Player1");
		Player player2 = createPlayer(newOrg, "123456", "Player2");
		Player player3 = createPlayer(newOrg, "123456", "Player3");

		player1.getBelongsToRoles().add(role2);
		player1.getBelongsToRoles().add(role1);
		player2.getBelongsToRoles().add(role1);
		player3.getBelongsToRoles().add(role1);

		List<Player> pl1 = new ArrayList<>();
		pl1.add(player1);
		pl1.add(player2);

		PlayerGroup group1 = createNewGroup(newOrg, pl1, "Gruppe 1", "");

		Reward points20 = createReward(newOrg, "20Punkte", "20", "", "20 Punkte Reward", "Points");
		Reward points50 = createReward(newOrg, "50Punkte", "50", "", "50 Punkte Reward", "Points");

		Reward coins10 = createReward(newOrg, "10Geld", "10", "", "10 Geldeinheiten", "Currency");
		Reward badge1 = createReward(newOrg, "1 schöner Badge", "1", "http://lorempixel.com/400/200", "Ein Sternchen", "Badge");
		Reward level1 = createReward(newOrg, "1 Level", "1", " ", "Ein Sternchen", "ReceiveLevel");

		List<Reward> rl1 = new ArrayList<>();
		rl1.add(points20);

		List<Reward> rl2 = new ArrayList<>();
		rl2.add(points20);
		rl2.add(coins10);

		List<Reward> rl3 = new ArrayList<>();
		rl3.add(points50);

		List<Reward> rl4 = new ArrayList<>();
		rl4.add(points50);
		rl4.add(level1);

		Task task1 = createTask(newOrg, "Task1", "Der erste Task", true);
		Task task2 = createTask(newOrg, "Task2", "Der zweite Task", false);
		Task task3 = createTask(newOrg, "Task3", "Der dritte Task", true);
		Task task4 = createTask(newOrg, "Task4", "Der vierte Task", true);

		task1.setAllowedFor(roleList1);

		List<Task> tl1 = new ArrayList<>();
		List<Task> tl2 = new ArrayList<>();
		List<Task> tl3 = new ArrayList<>();
		List<Task> tl4 = new ArrayList<>();

		tl1.add(task1);

		tl2.add(task1);
		tl2.add(task2);

		tl3.add(task3);
		tl3.add(task4);

		tl4.add(task1);
		tl4.add(task2);
		tl4.add(task3);

		GoalRule rule1 = createNewTaskRule(newOrg, "DoAllTasksRule", "1 Task", "hier muss nur 1 Task erledigt werden", tl1);
		GoalRule rule2 = createNewTaskRule(newOrg, "DoAllTasksRule", "2 Task", "hier müssen 2 Tasks erledigt werden", tl2);
		GoalRule rule3 = createNewTaskRule(newOrg, "DoAnyTasksRule", "OneOfTwo", "hier muss 1 Task von zweien erledigt werden", tl3);
		GoalRule rule4 = createNewTaskRule(newOrg, "DoAllTasksRule", "3 Task", "hier müssen 3 Tasks erledigt werden", tl4);

		Goal goal1 = createNewGoal(newOrg, "Ziel 1", "true", rule1, rl1, "false");
		Goal goal2 = createNewGoal(newOrg, "Ziel 2", "true", rule2, rl2, "false");
		Goal goal3 = createNewGoal(newOrg, "Ziel 3", "true", rule3, rl3, "false");
		Goal goal4 = createNewGoal(newOrg, "Ziel 4", "true", rule4, rl1, "true");
		Goal goal5 = createNewGoal(newOrg, "Ziel 1_2", "true", rule1, rl1, "false");
		Goal goal6 = createNewGoal(newOrg, "Ziel 1_2", "true", rule1, rl4, "false");

		// goal1.getCanCompletedBy().add(role3);
		// goal5.getCanCompletedBy().add(role2);
		goal1.setCanCompletedBy(roleList1);
		goal5.setCanCompletedBy(roleList2);

		// Marketplace
		MarketPlace markt1 = createNewMarketPlace(newOrg);

		player1.setCoins(1000);
		player2.setCoins(2000);
		player3.setCoins(3000);

		Offer offer1 = createNewOffer("offer1", "2014-03-22 18:00", "20", String.valueOf(task3.getId()), "1,2", "2015-03-22 18:00",
				String.valueOf(markt1.getId()), String.valueOf(player1.getId()), newOrg.getApiKey());
		Offer offer2 = createNewOffer("offer2", "2015-06-22 17:00", "50", String.valueOf(task4.getId()), "1,2", "2017-03-22 19:00",
				String.valueOf(markt1.getId()), String.valueOf(player1.getId()), newOrg.getApiKey());
		Offer offer3 = createNewOffer("offer3", "2013-07-30 12:00", "40", String.valueOf(task1.getId()), "2,3", "2017-03-22 19:00",
				String.valueOf(markt1.getId()), String.valueOf(player1.getId()), newOrg.getApiKey());
		Offer offer4 = createNewOffer("offer4", "2013-07-30 12:00", "50", String.valueOf(task1.getId()), "2,3", "2017-03-22 19:00",
				String.valueOf(markt1.getId()), String.valueOf(player2.getId()), newOrg.getApiKey());

		// Present
		Board board1 = createNewBoard(player1.getId(), newOrg.getApiKey());
		Board board2 = createNewBoard(player2.getId(), newOrg.getApiKey());
		Board board3 = createNewBoard(player3.getId(), newOrg.getApiKey());

		return Response.ok("{\"testdaten\":\"angelegt\"}").build();
	}

	private Account createAccount(String email, String password) {

		log.debug(password);
		log.debug(email);

		Account account = new Account();
		account.setEmail(email);
		account.setPassword(password);

		accountDao.persist(account);
		return account;
	}

	private Organisation createOrganisation(Account account) {

		Organisation devOrganisation = new Organisation();
		devOrganisation.setName("Test Organization");
		devOrganisation.addManager(account);
		String apiKey = "2ea56402-e460-47c1-9a65-e17bb815475b";
		devOrganisation.setApiKey(apiKey);

		log.debug(devOrganisation.getApiKey());
		organisationDao.insertOrganisation(devOrganisation);

		return devOrganisation;
	}

	private Player createPlayer(Organisation organisation, String password, String nickname) {

		log.debug("Create Player: " + nickname);

		Player player = new Player();
		player.setBelongsTo(organisation);
		player.setPassword(password);
		player.setNickname(nickname);
		playerDao.insert(player);

		return player;

	}

	private Reward createReward(Organisation organisation, String name, String amount, String url, String description, String type) {

		log.debug("Create Reward: " + name);

		String rName = name;
		String rAmount = amount;
		String rDescription = description;

		Reward reward;

		switch (type) {
		case "Achievement":
			reward = new Achievement();
			((Achievement) reward).setName(rName);
			((Achievement) reward).setDescription(rDescription);
			// ((Achievement) reward).setIcon(rUri);
			if (!"null".equals(url)) {
				((Achievement) reward).setImageIcon(ImageUtils.imageToByte(url));
			}
			break;
		case "Badge":
			reward = new Badge();
			((Badge) reward).setName(rName);
			((Badge) reward).setDescription(rDescription);
			// ((Badge) reward).setIcon(rUri);
			if (!"null".equals(url)) {
				((Badge) reward).setImageIcon(ImageUtils.imageToByte(url));
			}
			break;
		case "Currency":
			reward = new Coins();
			if (!"null".equals(rAmount)) {
				((Coins) reward).setAmount(ValidateUtils.requireGreaterThenZero(rAmount));
			}
			break;
		case "Points":
			reward = new Points();
			if (!"null".equals(rAmount)) {
				((Points) reward).setAmount(ValidateUtils.requireGreaterThenZero(rAmount));
			}
			break;
		case "ReceiveLevel":
			reward = new ReceiveLevel();
			if (!"null".equals(rAmount)) {
				((ReceiveLevel) reward).setLevelIndex(ValidateUtils.requireGreaterThenZero(rAmount));
			}
			break;

		default:
			reward = new Achievement();
			break;
		}

		reward.setBelongsTo(organisation);
		rewardDao.insertReward(reward);

		return reward;

	}

	private Task createTask(Organisation organisation, String name, String description, boolean tradeable) {

		log.debug("Create Task: " + name);

		Task task = new Task();
		task.setTaskName(name);
		task.setDescription(description);
		task.setBelongsTo(organisation);
		task.setTradeable(tradeable);
		taskDao.insertTask(task);

		return task;
	}

	private GoalRule createNewTaskRule(Organisation organisation, String type, String name, String description, List<Task> tasks) {

		log.debug("Create TaskRule: " + name);

		TaskRule rule;

		switch (type) {
		case "DoAllTasksRule":
			rule = new DoAllTasksRule();
			break;

		case "DoAnyTasksRule":
			rule = new DoAnyTaskRule();
			break;

		default:
			rule = new DoAllTasksRule();
		}

		rule.setName(name);
		rule.setDescription(description);
		rule.setBelongsTo(organisation);

		int id = ruleDao.insertRule(rule);

		rule.setTasks(tasks);

		return rule;
	}

	private Goal createNewGoal(Organisation organisation, String name, String repeatable, GoalRule rule, List<Reward> rewards, String groupGoal) {

		log.debug("Create Goal: " + name);

		Goal goal = new Goal();
		goal.setName(name);
		goal.setBelongsTo(organisation);

		// Convert String to boolean
		boolean isRepeatable = stringToBoolean(repeatable);
		goal.setRepeatable(isRepeatable);

		boolean isGroupGoal = stringToBoolean(groupGoal);
		goal.setPlayerGroupGoal(isGroupGoal);

		// Get rule object
		goal.setRule(rule);

		for (Reward reward : rewards) {
			if (reward != null) {
				goal.addReward(reward);
			}
		}

		// persist Goal
		goalDao.insertGoal(goal);

		return goal;
	}

	private boolean stringToBoolean(String string) {
		return "true".equalsIgnoreCase(string) || "t".equalsIgnoreCase(string) || "yes".equalsIgnoreCase(string) || "y".equalsIgnoreCase(string)
				|| "sure".equalsIgnoreCase(string) || "aye".equalsIgnoreCase(string) || "ja".equalsIgnoreCase(string) || "1".equalsIgnoreCase(string);
	}

	public Role createNewRole(Organisation organisation, String roleName) {

		log.debug("Create Role: " + roleName);

		Role role = new Role();
		role.setName(roleName);
		role.setBelongsTo(organisation);

		roleDao.insert(role);

		return role;

	}

	public PlayerGroup createNewGroup(Organisation organisation, List<Player> players, String name, String logoPath) {

		log.debug("Create Group: " + name);

		PlayerGroup group = new PlayerGroup();

		for (Player player : players) {
			if (player != null) {
				log.debug("Player hinzufügen: " + player.getNickname());
				group.getPlayers().add(player);
			}
		}

		group.setName(name);
		group.setPlayers(players);
		group.setBelongsTo(organisation);
		if (logoPath != null && !logoPath.isEmpty()) {
			group.setGroupLogo(ImageUtils.imageToByte(logoPath));
		}
		groupDao.insertGroup(group);

		return group;

	}

	public MarketPlace createNewMarketPlace(Organisation organisation) {

		MarketPlace marketplace = new MarketPlace();
		marketplace.setBelongsTo(organisation);

		marketPlaceDao.insertMarketPlace(marketplace);

		log.debug("Create Marketplace: " + marketplace.getId());

		return marketplace;
	}

	public Offer createNewOffer(String name, String endDate, String prize, String taskId, String allowedRoles, String deadLine, String marketId,
			String playerId, String apiKey) {

		log.debug("create new Offer called");

		Task task = taskDao.getTask(ValidateUtils.requireGreaterThenZero(taskId));
		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Player player = playerDao.getPlayer(ValidateUtils.requireGreaterThenZero(playerId), apiKey);

		if (!task.isTradeable()) {
			throw new ApiError(Response.Status.FORBIDDEN, "task is not tradeable");
		}

		if (ValidateUtils.requireGreaterThenZero(prize) <= 0) {
			throw new ApiError(Response.Status.FORBIDDEN, "Please, give a real bid!");
		}

		if (!player.enoughPrize(ValidateUtils.requireGreaterThenZero(prize))) {
			throw new ApiError(Response.Status.FORBIDDEN, "Not enough coins for such an offer");
		}

		// set Timestamp for Offer
		LocalDateTime offerDate = LocalDateTime.now();

		// Parse String in LocalDateTime -> Format:"2014-12-15 12:30";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime endDateTime = LocalDateTime.parse(endDate, formatter);
		LocalDateTime deadLineTime = LocalDateTime.parse(deadLine, formatter);

		Offer offer = new Offer();
		offer.setName(name);
		offer.setBelongsTo(organisation);
		offer.setOfferDate(offerDate);
		offer.setEndDate(endDateTime);
		offer.setPrize(ValidateUtils.requireGreaterThenZero(prize));
		offer.setDeadLine(deadLineTime);
		offer.setTask(task);
		offer.setPlayer(player);

		player.setCoins(player.getCoins() - ValidateUtils.requireGreaterThenZero(prize));

		// Find all roles by Id
		String[] roleIdList = allowedRoles.split(",");

		for (String roleIdString : roleIdList) {
			log.debug("Role To Add: " + roleIdString);
			Role role = roleDao.getRoleById(ValidateUtils.requireGreaterThenZero(roleIdString));
			if (role != null) {
				log.debug("Role Added: " + role.getId());
				offer.addRole(role);
			}
		}

		offer.setBelongsTo(organisation);
		MarketPlace marketPlace = marketPlaceDao.getMarketPl(ValidateUtils.requireGreaterThenZero(marketId));
		marketPlace.addOffer(offer);

		marketPlaceDao.insertOffer(offer);

		return offer;

	}

	public Board createNewBoard(int playerId, String apiKey) {

		log.debug("Create board called");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Player player = playerDao.getPlayer(playerId, apiKey);

		Board board = new Board();
		board.setOwner(player);
		board.setBelongsTo(organisation);

		boardDAO.persist(board);

		return board;
	}
}
