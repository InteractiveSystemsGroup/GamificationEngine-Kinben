package info.interactivesystems.gamificationengine.entities.goal;

import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.PlayerGroup;
import info.interactivesystems.gamificationengine.utils.Progress;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

/**
 * A PointsRule is a sub-class of the GoalRule. It specifies to reach a certain amount of points. If the player collected
 * all needed points for example by executing tasks, the rule is completed. So every time the player receives points it 
 * is checked if one PointsRule is fulfilled. 
 */
@Entity
@Inheritance
@DiscriminatorValue("PRULE")
public class GetPointsRule extends GoalRule {

	private int points;

	/**
	 * Gets the amount of points which is needed to complete the rule.
	 * 
	 * @return The amount of needed points as int. 
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * Sets the amount of points which is needed to complete the rule.
	 * 
	 * @param points
	 * 			The amount of points that is needed as int.
	 */
	public void setPoints(int points) {
		this.points = points;
	}

	/**
	 * This method checks if the passed player has enough or more points than needed.
	 * If it does, true is returned and the rule is fulfilled, otherwise false is returned.
	 * 
	 * @param player
	 * 			The player whose points are checked.
	 * @return Boolean value if a player has enough points (true) or not (false).
	 */
	public boolean checkRule(Player player) {

		if (player.getPoints() >= points) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * This method checks if the passed group of players has enough or more points than needed.
	 * If it does, true is returned and the rule is fulfilled, otherwise false is returned.
	 * 
	 * @param group
	 * 			The group whose points are checked.
	 * @return Boolean value if a player has enough points (true) or not (false).
	 */
	public boolean checkRule(PlayerGroup group) {

		if (group.getPoints() >= points) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Gets the progress of the PointsRule. The current points of the player and the needed points are returned.
	 * 
	 * @param player
	 * 			The player whose points are compared.
	 * @return The progress of the current and needed points of a player to fulfil the points rule.
	 */
	public Progress getProgress(Player player) {

		Progress progress = new Progress(player.getPoints(), this.getPoints());
		return progress;
	}

}
