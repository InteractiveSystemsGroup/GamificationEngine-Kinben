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
	 * 
	 * @param value
	 *            The value for validation.
	 * @return If valid the identity is returned.
	 */
	public static @NotNull String validateAsListOfDigits(@NotNull @ValidListOfDigits String value) {
		return value;
	}
}