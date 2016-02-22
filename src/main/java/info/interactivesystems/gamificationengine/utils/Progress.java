package info.interactivesystems.gamificationengine.utils;

public class Progress {

	private int current;
	private int full;

	public Progress(int current, int full) {
		this.current = current;
		this.full = full;
	}

	/**
	 * Gets the current progress.
	 * 
	 * @return The current step as int. 
	 */
	public int getCurrent() {
		return current;
	}

	/**
	 * Sets the current progress.
	 *
	 * @param current
	 *            The current progress.
	 */
	public void setCurrent(int current) {
		this.current = current;
	}

	/**
	 * Gets the full progress.
	 * 
	 * @return The value for the full progress as int.
	 */
	public int getFull() {
		return full;
	}

	/**
	 * Sets the value for the full progress.
	 * 
	 * @param full
	 *            The value which represents the full progress.
	 */
	public void setFull(int full) {
		this.full = full;
	}

}
