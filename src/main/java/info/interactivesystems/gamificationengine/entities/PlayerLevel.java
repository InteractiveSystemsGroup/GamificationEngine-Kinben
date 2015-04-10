package info.interactivesystems.gamificationengine.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * The PlayerLevel shows the status of the Player. After the Player completed a
 * task his level can advance if the conditions are fulfilled.
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
	 * Get the id of the PlayerLevel.
	 * 
	 * @return int of the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the id of a PlayerLevel
	 * 
	 * @param id
	 *            the id of the PlayerLevel.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get the organisation which the player level belongs to and in which a
	 * player can award it.
	 * 
	 * @return an organisation object.
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Set the organisation which the level belongs to and in which a player can
	 * award it.
	 * 
	 * @param belongsTo
	 *            the organisation object.
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Get the level's index.
	 * 
	 * @return the levelIndex as an int
	 */
	public int getLevelIndex() {
		return levelIndex;
	}

	/**
	 * Set the levelIndex for the level.
	 * 
	 * @param levelIndex
	 *            as int
	 */
	public void setLevelIndex(int levelIndex) {
		this.levelIndex = levelIndex;
	}

	/**
	 * Get the name of the level. This could be for example a number or a status
	 * like a title.
	 * 
	 * @return a level name
	 */
	public String getLevelName() {
		return levelName;
	}

	/**
	 * Set the name of the level. This could be for example a number or a status
	 * like a title.
	 * 
	 * @param levelName
	 *            the for the level
	 */
	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}

}
