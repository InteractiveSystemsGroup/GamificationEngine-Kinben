package info.interactivesystems.gamificationengine.api;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.interactivesystems.gamificationengine.api.exeption.CredentialException;
import info.interactivesystems.gamificationengine.dao.AccountDAO;
import info.interactivesystems.gamificationengine.entities.Account;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableValidator;
import javax.ws.rs.core.Response;

import org.hibernate.validator.constraints.Email;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AccountApiTest {

	private AccountDAO accountDao;
	private AccountApi accountApi;

	private static ExecutableValidator executableValidator;

	@BeforeClass
	public static void setUpClass() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		executableValidator = factory.getValidator().forExecutables();
	}

	@Before
	public void setUp() throws Exception {
		accountApi = new AccountApi();
		accountDao = mock(AccountDAO.class);

		accountApi.accountDao = accountDao;
	}

	@Test
	public void testGetAccountCredentialsOk() {
		String email = "test@example.com";
		String password = "123456";

		when(accountDao.checkCredentials(anyString(), anyString())).then(invocation -> true);
		mockGetAccount(email, password);

		Response response = accountApi.get(email, password);
		ResponseSurrogate<Account> entity = (ResponseSurrogate<Account>) response.getEntity();

		assertThat(entity).isNotNull();
		assertThat(Response.Status.OK.getStatusCode()).isEqualTo(response.getStatus());

		assertThat(entity.content.getEmail()).isEqualTo(email);
		assertThat(entity.content.getPassword()).isEqualTo(password);
	}

	private void mockGetAccount(String email, String password) {
		when(accountDao.getAccount(anyString())).then(invocation -> {
			Account account = new Account();
			account.setEmail(email);
			account.setPassword(password);
			return account;
		});
	}

	@Test(expected = CredentialException.class)
	public void testGetAccountCredentialsWrong() {
		when(accountDao.checkCredentials(anyString(), anyString())).then(invocation -> false);
		mockGetAccount(null, null);

		Response response = accountApi.get(null, null);
		ResponseSurrogate<Account> entity = (ResponseSurrogate<Account>) response.getEntity();
		assertThat(entity).isNull();
		assertThat(entity.info.size()).isAtLeast(1);
	}

	@Test
	public void testCreateAccount() {
		String email = "test@example.com";
		String password = "123456";

		Response response = accountApi.create(email, password, null, null);
		ResponseSurrogate<Account> entity = (ResponseSurrogate<Account>) response.getEntity();

		assertThat(entity.content.getEmail()).isEqualTo(email);
		assertThat(entity.content.getPassword()).isEqualTo(password);
	}

	@Test
	public void testCreateAccountNotAnEmail() throws NoSuchMethodException {
		String wrongEmail = "not an email";
		String password = "123456";

		Method method = AccountApi.class.getMethod("create", String.class, String.class, String.class, String.class);

		Object[] parameterValues = { wrongEmail, password, null, null };
		Set<ConstraintViolation<AccountApi>> violations = executableValidator.validateParameters(accountApi, method, parameterValues);
		assertThat(violations.size()).is(1);

		Class<? extends Annotation> constraintType = violations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType();
		assertThat(constraintType).isEqualTo(Email.class);
	}

	@Test
	public void testCreateAccountNoPassword() throws NoSuchMethodException {
		String email = "test@example.com";
		String password = null;

		Method method = AccountApi.class.getMethod("create", String.class, String.class, String.class, String.class);

		Object[] parameterValues = { email, password, null, null };
		Set<ConstraintViolation<AccountApi>> violations = executableValidator.validateParameters(accountApi, method, parameterValues);
		assertThat(violations.size()).is(1);

		Class<? extends Annotation> constraintType = violations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType();
		assertThat(constraintType).isEqualTo(NotNull.class);
	}
}