package info.interactivesystems.gamificationengine.entities.present;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * A present can be a short positive text message, which is sent to one or more
 * receivers. It can contain a little thank or a praise.
 */
@Entity
@DiscriminatorValue("PreTextM")
public class TextMessage extends Present {

	private String content;

	/**
	 * Gets the content of the created text message.
	 * 
	 * @return String of the message's content.
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Sets the content of a text message as a String.
	 * 
	 * @param content
	 *            The content of the text message.
	 */
	public void setContent(String content) {
		this.content = content;
	}

}
