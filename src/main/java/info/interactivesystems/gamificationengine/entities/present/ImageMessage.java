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

	private String shortMessage;

	
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
	 * @param imageIcon 
	 * 		The byte[] of the imageIcon, which should be sent.
	 */
	public void setImageIcon(byte[] imageIcon) {
		this.imageIcon = imageIcon;
	}
	
	/**
	 * Gets the short message of the created image message.
	 * 
	 * @return String of the message's short message.
	 */
	public String getMessage() {
		return shortMessage;
	}

	/**
	 * Sets the content of a image message as a String.
	 * 
	 * @param shortMessage
	 *            The content of image's message short text.
	 */
	public void setMessage(String shortMessage) {
		this.shortMessage = shortMessage;
	}

}
