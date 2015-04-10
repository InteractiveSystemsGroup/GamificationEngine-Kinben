package info.interactivesystems.gamificationengine.entities.present;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * A present can be a short positive text message, which is sent to one or more
 * receivers.
 */
@Entity
@DiscriminatorValue("PreTextM")
public class TextMessage extends Present {

	private String content;

	/**
	 * Get the content of the created text message.
	 * 
	 * @return String of the messages content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Set the content of a text message as a relative short String.
	 * 
	 * @param content
	 *            of the text message
	 */
	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return content;
	}
}
