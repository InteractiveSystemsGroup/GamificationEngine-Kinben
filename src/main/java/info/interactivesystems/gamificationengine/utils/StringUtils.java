package info.interactivesystems.gamificationengine.utils;

import info.interactivesystems.gamificationengine.api.validation.ValidListOfDigits;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

public class StringUtils {

	/**
	 * Converts a string of digits to list of integers.
	 * <p/>
	 * Splits a comma separated list by a comma. Trims each string. Collects to
	 * a list of Integers.
	 * 
	 * @param commaSeparatedList
	 *            a comma separated list
	 * @return list of integers
	 */
	public static @NotNull List<Integer> stringArrayToIntegerList(@NotNull String commaSeparatedList) {
		String[] rolesList = commaSeparatedList.split(",");
		return Stream.of(rolesList).map(String::trim).collect(Collectors.mapping(Integer::valueOf, Collectors.toList()));
	}

	/**
	 * @param value
	 *            for validation
	 * @return if valid identity is returned
	 */
	public static @NotNull String validateAsListOfDigits(@NotNull @ValidListOfDigits String value) {
		return value;
	}
}