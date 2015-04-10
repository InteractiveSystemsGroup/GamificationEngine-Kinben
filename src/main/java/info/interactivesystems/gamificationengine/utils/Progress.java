package info.interactivesystems.gamificationengine.utils;

public class Progress {

	private int current;
	private int full;

	public Progress() {
		// TODO Auto-generated constructor stub
	}

	public Progress(int current, int full) {
		this.current = current;
		this.full = full;
	}

	/**
	 * Gets the current progress.
	 * 
	 * @return int as a current Step
	 */
	public int getCurrent() {
		return current;
	}

	/**
	 * Sets the current progress.
	 *
	 * @param current
	 *            the current progress
	 */
	public void setCurrent(int current) {
		this.current = current;
	}

	/**
	 * Gets the full progress.
	 * 
	 * @return int as a value for the full progress
	 */
	public int getFull() {
		return full;
	}

	/**
	 * Sets the value for a full progress.
	 * 
	 * @param full
	 *            the value for a the full progress
	 */
	public void setFull(int full) {
		this.full = full;
	}

}
