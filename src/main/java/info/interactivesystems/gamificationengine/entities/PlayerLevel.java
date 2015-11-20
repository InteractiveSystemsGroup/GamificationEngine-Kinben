package info.interactivesystems.gamificationengine.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * A player level shows the status of the player. This can be a number or a status like a titel.
 * After the Player completed a task her/his level can advance if the conditions are fulfilled.
 */
@Entity
public class PlayerLevel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@ManyToOne
	private Organisation belongsTo;

	private int levelIndex;

	@NotNull
	private String levelName;

	/**
	 * Gets the id of the player level. 
	 * 
	 * @return The player level's id as int.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of a player level. 
	 * 
	 * @param id
	 *         The id of the player level henceforth.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the organisation which the player level belongs to.
	 * 
	 * @return The organisations object the player level belongs to.
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Sets the organisation which the level belongs to and in which a player can
	 * award it.
	 * 
	 * @param belongsTo
	 *            The organisation to which the group belongs to henceforth. 
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Gets the level's index.
	 * 
	 * @return The index of the level as an int.
	 */
	public int getLevelIndex() {
		return levelIndex;
	}

	/**
	 * Sets the index of the level.
	 * 
	 * @param levelIndex
	 *            The index of the level as int.
	 */
	public void setLevelIndex(int levelIndex) {
		this.levelIndex = levelIndex;
	}

	/**
	 * Gets the name of the level. This could be for example a number or a status
	 * like a title.
	 * 
	 * @return The name of the level as String. 
	 */
	public String getLevelName() {
		return levelName;
	}

	/**
	 * Sets the name of the level. This could be for example a number or a status
	 * like a title.
	 * 
	 * @param levelName
	 *            The name of the level henceforth. 
	 */
	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}

}
