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
	 * Get the index of the level.
	 * 
	 * @return the level's index as an int.
	 */
	public int getLevelIndex() {
		return levelIndex;
	}

	/**
	 * Set the level's index which should be awarded.
	 * 
	 * @param levelIndex
	 *            of the level which should be awarded
	 */
	public void setLevelIndex(int levelIndex) {
		this.levelIndex = levelIndex;
	}

	/**
	 * Get the level's label which should be awarded.
	 * 
	 * @return String value of the level's label
	 */
	public String getLevelLabel() {
		return levelLabel;
	}

	/**
	 * Set the label of a level.
	 * 
	 * @param levelLabel
	 *            as String
	 */
	public void setLevelLabel(String levelLabel) {
		this.levelLabel = levelLabel;
	}

	/**
	 * Awards the player a specific level.
	 */
	@Override
	public void addReward(Player player, GoalDAO goalDao, RuleDAO ruleDao) {
		player.setLevelIndex(levelIndex);
		player.setLevelLabel(levelLabel);
	}

	/**
	 * Awards the group of players a specific level.
	 */
	@Override
	public void addReward(PlayerGroup group, GoalDAO goalDao, RuleDAO ruleDao) {
		// TODO Auto-generated method stub
		group.setLevelIndex(levelIndex);
		group.setLevelLabel(levelLabel);
	}

}
