package info.interactivesystems.gamificationengine.entities.goal;

import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.PlayerGroup;
import info.interactivesystems.gamificationengine.utils.Progress;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

@Entity
@Inheritance
@DiscriminatorValue("PRULE")
public class GetPointsRule extends GoalRule {

	private int points;

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public boolean checkRule(Player player) {

		if (player.getPoints() >= points) {
			return true;
		} else {
			return false;
		}

	}

	public boolean checkRule(PlayerGroup group) {

		if (group.getPoints() >= points) {
			return true;
		} else {
			return false;
		}

	}

	public Progress getProgress(Player player) {

		Progress progress = new Progress(player.getPoints(), getPoints());
		return progress;
	}

}
