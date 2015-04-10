package info.interactivesystems.gamificationengine.entities.rewards;

import info.interactivesystems.gamificationengine.dao.GoalDAO;
import info.interactivesystems.gamificationengine.dao.RuleDAO;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.PlayerGroup;

import java.net.URL;

import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;

/**
 * The achievement class contains an image and a description for a documentary
 * use to track significant results or milestones the player was able to achieve
 * during his play. It's a more elaborate way to record the players achievements
 * than the badge class. An achievement is a permanent reward, so a player can
 * get a specific achievement only one.
 */
@Entity
@DiscriminatorValue("RewAchieve")
public class Achievement extends PermanentReward {

	@NotNull
	private String name;

	private String description;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	private byte[] imageIcon;

	private URL icon;

	public Achievement() {
	}

	/**
	 * Get the achievement's icon as byte[].
	 * 
	 * @return byte[] of the achievement's icon
	 */
	public byte[] getImageIcon() {
		return imageIcon;
	}

	/**
	 * Set the byte[] as an icon of an achievement.
	 * 
	 * @param the
	 *            icon that should be connected with the achievement
	 */
	public void setImageIcon(byte[] iconImage) {
		this.imageIcon = iconImage;
	}

	/**
	 * Get the description of an achievement. This could contain for example the
	 * tasks which have to be completed to get this achievement.
	 * 
	 * @return the achievement's description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description of an achievement. This contains further information
	 * how the achievement can be earned, like all requirements to get the
	 * achievement or the process to award the achievement.
	 * 
	 * @param description
	 *            of the achievement
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the name of an achievement, which can describe the success in a short
	 * way and can be displayed in the application.
	 * 
	 * @return the achievement's name as String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of an achievement, which can describe the success in a short
	 * way and can be displayed in the application.
	 * 
	 * @param name
	 *            of the achievement as a String
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the URL of the achievement's icon, when it was created.
	 * 
	 * @return the URL of the icon
	 */
	public URL getIcon() {
		return icon;
	}

	/**
	 * Set the URL value of the achievement's icon, when it was created.
	 * 
	 * @param the
	 *            URL of the icon
	 */
	public void setIcon(URL icon) {
		this.icon = icon;
	}

	/**
	 * Awards the player an achievement and adds it to his list with permanent
	 * rewards.
	 */
	@Override
	public void addReward(Player player, GoalDAO goalDao, RuleDAO ruleDao) {
		player.addPermanentReward(this);
	}

	/**
	 * Awards a group an achievement and adds it to the list with permanent
	 * rewards.
	 */
	@Override
	public void addReward(PlayerGroup group, GoalDAO goalDao, RuleDAO ruleDao) {
		group.addPermanentReward(this);
	}
}
