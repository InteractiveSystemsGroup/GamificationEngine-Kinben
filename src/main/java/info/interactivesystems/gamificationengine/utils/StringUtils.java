package info.interactivesystems.gamificationengine.utils;

import info.interactivesystems.gamificationengine.api.validation.ValidListOfDigits;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

public class StringUtils {

	/**
	 * Converts a string of digits to list of integers.
	 * <p></p>
	 * Splits a comma separated list by a comma and trims each string. Collects to
	 * a list of Integers.
	 * 
	 * @param commaSeparatedList
	 *            The comma separated list.
	 * @return The list of single integers that were separated. 
	 */
	public static @NotNull List<Integer> stringArrayToIntegerList(@NotNull String commaSeparatedList) {
		String[] arrayList = commaSeparatedList.split(",");
		return Stream.of(arrayList).map(String::trim).collect(Collectors.mapping(Integer::valueOf, Collectors.toList()));
	}

	/**
	 * Checks if the passed value is a list of digists.
	 * 
	 * @param value
	 *            The value for validation.
	 * @return If valid the identity is returned.
	 */
	public static @NotNull String validateAsListOfDigits(@NotNull @ValidListOfDigits String value) {
		return value;
	}
	
	/**
	 * Checks if the passed value is "true", "t", "yes", "y", "sure", "aye", "ja" or "1". If this is the case it is 
	 * accepted as a value for true and so true is returned else false.
	 * 
	 * @param value
	 * 		The value that should be checked.
	 * @return
	 * 		Boolean value if the passed String is 
	 */
	public static boolean checkBoolean(String value){
		boolean result = "true".equalsIgnoreCase(value) || "t".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)
		|| "y".equalsIgnoreCase(value) || "sure".equalsIgnoreCase(value) || "aye".equalsIgnoreCase(value)
		|| "ja".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value);
		return result;
	}
}