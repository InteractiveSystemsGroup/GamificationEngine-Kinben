package info.interactivesystems.gamificationengine.entities.rewards;

import info.interactivesystems.gamificationengine.dao.GoalDAO;
import info.interactivesystems.gamificationengine.dao.RuleDAO;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.PlayerGroup;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Coins class serves as a Reward-subclass, that allocates coins to a player.
 * Coins are a volatile reward which can be earned more than one time. The
 * awarded coins are added to the current ones of a player.
 */
@Entity
@DiscriminatorValue("RewCoins")
public class Coins extends VolatileReward {

	// @NotNull
	private int amount;

	/**
	 * Get the concrete amount of coins which can be earned as a reward.
	 * 
	 * @return the current amount as int.
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * Set a specific amount of coins which can be earned as a reward.
	 * 
	 * @param amount
	 *            of coins which can be earned.
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * Awards the player the concrete amount of coins and adds it to the
	 * player's current coins.
	 */
	@Override
	public void addReward(Player player, GoalDAO goalDao, RuleDAO ruleDao) {
		player.awardCoins(amount);
	}

	/**
	 * Awards the group the concrete amount of coins and adds it to the current
	 * coins.
	 */
	@Override
	public void addReward(PlayerGroup group, GoalDAO goalDao, RuleDAO ruleDao) {
		group.awardCoins(amount);
	}
}
