package info.interactivesystems.gamificationengine.entities.rewards;

import info.interactivesystems.gamificationengine.dao.GoalDAO;
import info.interactivesystems.gamificationengine.dao.RuleDAO;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.PlayerGroup;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * The Coins class serves as a Reward-subclass, that allocates coins to a player.
 * Coins are a volatile reward which can be earned more than one time. These
 * awarded coins are added to the current amount of coins a player owns.
 */
@Entity
@DiscriminatorValue("RewCoins")
public class Coins extends VolatileReward {

	// @NotNull
	private int amount;

	/**
	 * Gets the concrete amount of coins which can be earned as a reward.
	 * 
	 * @return The current amount as int.
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * Sets a specific amount of coins which can be earned as a reward.
	 * 
	 * @param amount
	 *            The amount of coins which can be earned by the reward.
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * With this method the player awards the amount of coins. Therefore the coins
	 * are added to her/his current amount of coins.
	 * 
	 * @param player
	 *            The player who should award the coins. This parameter must
	 *            not be null.
	 * @param goalDao
	 *            The goal DAO is required to access created goals. 
	 * @param ruleDao
	 *            The rule DAO is required to access the created rules. 
	 */
	@Override
	public void addReward(Player player, GoalDAO goalDao, RuleDAO ruleDao) {
		player.awardCoins(amount);
	}

	/**
	 * Awards a group the amount of coins and adds it to group's current amount.
	 * 
	 * @param group
	 *            The group of players which should award the coins. This parameter 
	 *            must not be null.
	 * @param goalDao
	 *            The goal DAO is required to access created goals. 
	 * @param ruleDao
	 *            The rule DAO is required to access the created rules. 
	 */
	@Override
	public void addReward(PlayerGroup group, GoalDAO goalDao, RuleDAO ruleDao) {
		group.awardCoins(amount);
	}
}
