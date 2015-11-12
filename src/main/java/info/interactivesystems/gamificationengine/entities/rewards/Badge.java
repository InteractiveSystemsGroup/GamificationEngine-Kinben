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
 * award a specific badge only once.
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
	 * Gets the name of the badge. The name should be meaningful and connected to
	 * the completed task(s).
	 * 
	 * @return The badge's name as String.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of a created badge. The name should be meaningful and
	 * connected to the completed task(s).
	 * 
	 * @return The badge's name as a String.
	 * @param name
	 *            The new name of the badge as a String. 
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the description of a created badge. This could contain for example
	 * which tasks werde completed to get this badge.
	 * 
	 * @return The descirtopion of the badge. 
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of a badge. This can contain further information how the
	 * badge can be earned, like the requirements to get the badge or the
	 * process to award the badge.
	 *
	 * @param description
	 *            The description of the badge as String.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the URL of the badge's icon, when it was created.
	 * 
	 * @return The URL of the associated icon.
	 */
	public URL getIcon() {
		return icon;
	}

	/**
	 * Sets the URL value of the badge's icon, when it was created.
	 * 
	 * @param icon
	 *            The URL of the associated icon.
	 */
	public void setIcon(URL icon) {
		this.icon = icon;
	}

	/**
	 * Gets the badge's icon as byte[].
	 * 
	 * @return Byte[] of the badge's icon that is stored in the database.
	 */
	public byte[] getImageIcon() {
		return imageIcon;
	}

	/**
	 * Sets the byte[] as an icon of a badge that is stored in the database.
	 * 
	 * @param icon
	 *            The icon that should be connected with the badge as byte[].
	 */
	public void setImageIcon(byte[] icon) {
		imageIcon = icon;
	}

	/**
	 * With this method the player awards an badge. Therefore the badge is added
	 * to her/his list of permanent rewards.
	 * 
	 * @param player
	 *            The player who should award the badge. This parameter must
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
	 * Awards a group a badge and adds it to the list with permanent rewards.
	 * 
	 * @param group
	 *            The group of players which should award the badge. This 
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
