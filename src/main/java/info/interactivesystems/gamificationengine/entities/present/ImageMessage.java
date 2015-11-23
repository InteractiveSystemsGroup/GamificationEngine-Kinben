package info.interactivesystems.gamificationengine.entities.present;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

/**
 * A present can be an imageMessage in the form of an image icon with a positive message for
 * the receiver.
 */
@Entity
@DiscriminatorValue("PreIconM")
public class ImageMessage extends Present {

	@Lob
	@Column(columnDefinition = "BLOB", length = 100000)
	private byte[] imageIcon;

	/**
	 * Gets the image icon which is sent as a present to a player as byte[].
	 * 
	 * @return The byte[] of the sent image.
	 */
	public byte[] getImageIcon() {
		return imageIcon;
	}

	/**
	 * Sets the image icon which is sent as a present to a player as byte[].
	 * 
	 * @param The byte[] of the imageIcon, which should be sent.
	 */
	public void setImageIcon(byte[] imageIcon) {
		this.imageIcon = imageIcon;
	}

}
