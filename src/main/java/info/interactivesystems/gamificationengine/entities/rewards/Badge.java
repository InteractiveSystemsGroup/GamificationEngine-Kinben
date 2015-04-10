package info.interactivesystems.gamificationengine.entities.rewards;

import info.interactivesystems.gamificationengine.dao.GoalDAO;
import info.interactivesystems.gamificationengine.dao.RuleDAO;
import info.interactivesystems.gamificationengine.entities.Player;
import info.interactivesystems.gamificationengine.entities.PlayerGroup;

import java.net.URL;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;

/**
 * The badge class serves as a Reward-subclass that represents a distinct icon.
 * It should be used as a an instantly recognizable visual reference to an badge
 * a player was able to reach. A badge is a permanent reward, so a player can
 * get a specific badge only one.
 */
@Entity
@DiscriminatorValue("RewBadge")
public class Badge extends PermanentReward {

	@NotNull
	private String name;

	private String description;

	private URL icon;

	@Lob
	@Column(columnDefinition = "BLOB")
	private byte[] imageIcon;

	public Badge() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Get the name of the badge. The name should be meaningful and connected to
	 * the completed task(s).
	 * 
	 * @return the badge's name as String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of a created badge. The name should be meaningful and
	 * connected to the completed task(s).
	 * 
	 * @return the badge's name as String
	 * @param name
	 *            of the badge as a String
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the description of a created badge. This could contain for example
	 * the tasks which have to be completed to get this badge.
	 * 
	 * @return the badge's description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description of a badge. This contains further information how the
	 * badge can be earned, like the requirements to get the badge or the
	 * process to award the badge.
	 *
	 * @param description
	 *            of the badge
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the URL of the badge's icon, when it was created.
	 * 
	 * @return the URL of the icon
	 */
	public URL getIcon() {
		return icon;
	}

	/**
	 * Set the URL value of the badge's icon, when it was created.
	 * 
	 * @param the
	 *            URL of the icon
	 */
	public void setIcon(URL icon) {
		this.icon = icon;
	}

	/**
	 * Get the badge's icon as byte[].
	 * 
	 * @return byte[] of the badge's icon
	 */
	public byte[] getImageIcon() {
		return imageIcon;
	}

	/**
	 * Set the byte[] as an icon of a badge.
	 * 
	 * @param the
	 *            icon that should be connected with the badge
	 */
	public void setImageIcon(byte[] icon) {
		imageIcon = icon;
	}

	/**
	 * Awards the player a badge and adds it to his list with permanent rewards.
	 */
	@Override
	public void addReward(Player player, GoalDAO goalDao, RuleDAO ruleDao) {
		player.addPermanentReward(this);
	}

	@Override
	public void addReward(PlayerGroup group, GoalDAO goalDao, RuleDAO ruleDao) {
		group.addPermanentReward(this);
	}
}
