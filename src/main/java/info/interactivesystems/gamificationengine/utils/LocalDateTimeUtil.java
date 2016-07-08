package info.interactivesystems.gamificationengine.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class LocalDateTimeUtil {

	/**
	 * Parses a String to a LocalDateTime object like for example to '2015-12-15 12:30';
	 * 
	 * @param dateAndTime
	 * 			The String that should be formatted and parsed to a LocalDateTime object.
	 * @return The formatted LocalDateTime Object of the passed String.
	 */
	public static LocalDateTime formatDateAndTime(String dateAndTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime formatedDate = LocalDateTime.parse(dateAndTime, formatter);
		return formatedDate;
	}
	

	
	
}
