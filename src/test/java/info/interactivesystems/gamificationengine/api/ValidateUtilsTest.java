package info.interactivesystems.gamificationengine.api;

import static com.google.common.truth.Truth.assertThat;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ValidateUtilsTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testRequireNotNull() {
		Object object = new Object();

		assertThat(ValidateUtils.requireNotNull(0, object)).isNotNull();
	}

	@Test
	public void testRequireNotNullException() {
		thrown.expect(ApiError.class);
		ValidateUtils.requireNotNull(0, null);
	}

	@Test
	public void testRequireGreaterThenZero() {
		assertThat(ValidateUtils.requireGreaterThanZero(1)).isEqualTo(1);
		assertThat(ValidateUtils.requireGreaterThanZero(23)).isEqualTo(23);
	}

	@Test
	public void testRequireGreaterThenZeroException() {
		thrown.expect(ApiError.class);
		ValidateUtils.requireGreaterThanZero(0);
		ValidateUtils.requireGreaterThanZero(-1);
	}

	@Test
	public void testRequireGreaterThenZeroString() {
		assertThat(ValidateUtils.requireGreaterThanZero("1")).isEqualTo(1);
		assertThat(ValidateUtils.requireGreaterThanZero("23")).isEqualTo(23);
	}

	@Test
	public void testRequireGreaterThenZeroStringException() {
		thrown.expect(ApiError.class);
		ValidateUtils.requireGreaterThanZero("0");
		ValidateUtils.requireGreaterThanZero("-1");
	}
}