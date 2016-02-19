package info.interactivesystems.gamificationengine.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class LocalDateTimeUtil {

//	Parse String in LocalDateTime -> for example to "2015-12-15 12:30";
	public static LocalDateTime formatDateAndTime(String dateAndTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime formatedDate = LocalDateTime.parse(dateAndTime, formatter);
		return formatedDate;
	}
	

	
	
}
