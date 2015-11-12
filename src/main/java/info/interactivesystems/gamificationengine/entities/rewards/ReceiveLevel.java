package info.interactivesystems.gamificationengine.entities.rewards;

import info.interactivesystems.gamificationengine.dao.GoalDAO;
import info.interactivesystems.gamificationengine.dao.RuleDAO;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.PlayerGroup;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * The ReceiveLevel class is a Reward-subclass that allocates a specific level
 * to a player which can serve as a status.
 */
@Entity
@DiscriminatorValue("RewRLevel")
public class ReceiveLevel extends VolatileReward {

	@NotNull
	private int levelIndex;
	private String levelLabel;

	/**
	 * Gets the index of the level.
	 * 
	 * @return the level's index as an int.
	 */
	public int getLevelIndex() {
		return levelIndex;
	}

	/**
	 * Sets the level's index which should be awarded.
	 * 
	 * @param levelIndex
	 *            The index of the level which is awarded as int.
	 */
	public void setLevelIndex(int levelIndex) {
		this.levelIndex = levelIndex;
	}

	/**
	 * Gets the level's label which should be awarded.
	 * 
	 * @return The value of the level's label as String. 
	 */
	public String getLevelLabel() {
		return levelLabel;
	}

	/**
	 * Sets the String label of a level.
	 * 
	 * @param levelLabel
	 *            Label of the level as String.
	 */
	public void setLevelLabel(String levelLabel) {
		this.levelLabel = levelLabel;
	}

	/**
	 * This method awards the player a specfic lavel.
	 * 
	 * @param player
	 *            The player who should award the level. This parameter must
	 *            not be null.
	 * @param goalDao
	 *            The goal DAO is required to access created goals. 
	 * @param ruleDao
	 *            The rule DAO is required to access the created rules. 
	 */
	@Override
	public void addReward(Player player, GoalDAO goalDao, RuleDAO ruleDao) {
		player.setLevelIndex(levelIndex);
		player.setLevelLabel(levelLabel);
	}

	/**
	 * Awards a group of player a specific level.
	 * 
	 * @param group
	 *            The group of players which should award the level. This parameter 
	 *            must not be null.
	 * @param goalDao
	 *            The goal DAO is required to access created goals. 
	 * @param ruleDao
	 *            The rule DAO is required to access the created rules. 
	 * 
	 */
	@Override
	public void addReward(PlayerGroup group, GoalDAO goalDao, RuleDAO ruleDao) {
		group.setLevelIndex(levelIndex);
		group.setLevelLabel(levelLabel);
	}

}
