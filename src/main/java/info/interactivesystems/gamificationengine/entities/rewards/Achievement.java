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


	private URL icon;
	
	@Lob
	@Basic(fetch = FetchType.LAZY)
	private byte[] imageIcon;


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
	 * With this method the player awards an achievement if she/he hasn't awarded it, yet. 
	 * Therefore the achievement is added to her/his list of permanent rewards.
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
		if(!player.getRewards().contains(this)){
			player.addPermanentReward(this);
		}
	}

	/**
	 * With this method a group awards an achievement and adds it to the list with permanent
	 * rewards, but only if this achievement isn't already in the list of permanent rewards.
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
		if(!group.getRewards().contains(this)){
			group.addPermanentReward(this);
		}
	}
}
