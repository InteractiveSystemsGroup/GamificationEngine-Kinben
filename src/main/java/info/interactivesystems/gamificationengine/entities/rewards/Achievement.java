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
 * during her/his play. It's a more elaborate way to record the players achievements
 * than the badge class. An achievement is a permanent reward, so a player can
 * get a specific achievement only once.
 */
@Entity
@DiscriminatorValue("RewAchieve")
public class Achievement extends PermanentReward {

	@NotNull
	private String name;

	private String description;

	private URL icon;
	
	@Lob
	@Basic(fetch = FetchType.LAZY)
	private byte[] imageIcon;

	public Achievement() {
	}

	/**
	 * Gets the achievement's icon as byte[].
	 * 
	 * @return byte[] of the achievement's icon
	 */
	public byte[] getImageIcon() {
		return imageIcon;
	}

	/**
	 * Sets the byte[] as an icon of an achievement.
	 * 
	 * @param iconImage
	 *            The icon that should be connected with the achievement.
	 */
	public void setImageIcon(byte[] iconImage) {
		this.imageIcon = iconImage;
	}

	/**
	 * Gets the description of an achievement. This could contains for example the 
	 * different tasks which have to be completed to get this achievement.
	 * 
	 * @return the achievement's description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of an achievement. This contains for example further 
	 * information about how the achievement can be earned, like all requirements to 
	 * get the achievement or the process to award the achievement.
	 * 
	 * @param description
	 *            of the achievement
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the name of an achievement which can describe the success in a short
	 * way and can be displayed in the application.
	 * 
	 * @return The achievement's name as String.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of an achievement, which can describe the success in a short
	 * way and can be displayed in the application.
	 * 
	 * @param name
	 *            The name of the achievement as a String.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the URL of the achievement's icon, but only when it was created.
	 * 
	 * @return The URL of the icon.
	 */
	public URL getIcon() {
		return icon;
	}

	/**
	 * Sets the URL value of the achievement's icon, when it was created.
	 * 
	 * @param icon 
	 *            The URL of the icon that should be set.
	 */
	public void setIcon(URL icon) {
		this.icon = icon;
	}

	/**
	 * With this method the player awards an achievement. Therefore the achievement
	 * is added to her/his list of permanent rewards.
	 * 
	 * @param player
	 *            The player who should award the achievement. This parameter must
	 *            not be null.
	 * @param goalDao
	 *            The goal DAO is required to access created goals. 
	 * @param ruleDao
	 *            The rule DAO is required to access the created rules. 
	 */
	@Override
	public void addReward(Player player, GoalDAO goalDao, RuleDAO ruleDao) {
		player.addPermanentReward(this);
	}

	/**
	 * Awards a group an achievement and adds it to the list with permanent
	 * rewards.
	 * 
	 * @param group
	 *            The group of players which should award the achievement. This 
	 *            parameter must not be null.
	 * @param goalDao
	 *            The goal DAO is required to access created goals. 
	 * @param ruleDao
	 *            The rule DAO is required to access the created rules. 
	 */
	@Override
	public void addReward(PlayerGroup group, GoalDAO goalDao, RuleDAO ruleDao) {
		group.addPermanentReward(this);
	}
}
