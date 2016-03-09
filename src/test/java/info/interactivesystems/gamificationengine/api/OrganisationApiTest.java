package info.interactivesystems.gamificationengine.api;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.interactivesystems.gamificationengine.api.exeption.CredentialException;
import info.interactivesystems.gamificationengine.dao.AccountDAO;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.entities.Account;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.utils.SecurityTools;

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

public class OrganisationApiTest {

	private OrganisationDAO organisationDao;
	private OrganisationApi organisationApi;

	private static ExecutableValidator executableValidator;
	private static AccountDAO accountDao;

	@BeforeClass
	public static void setUpClass() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		executableValidator = factory.getValidator().forExecutables();
	}

	@Before
	public void setUp() throws Exception {
		organisationApi = new OrganisationApi();
		organisationDao = mock(OrganisationDAO.class);
		accountDao = mock(AccountDAO.class);

		organisationApi.organisationDao = organisationDao;
		organisationApi.accountDao = accountDao;
	}

	@Test
	public void testGetOrganisationCredentialsOk() {

		when(accountDao.checkCredentials(anyString(), SecurityTools.encryptWithSHA512(anyString()))).then(invocation -> true);
		mockGetOrganisation("Test Organisation");

		Response response = organisationApi.get("1", null, null);
		ResponseSurrogate<Organisation> entity = (ResponseSurrogate<Organisation>) response.getEntity();

		assertThat(entity).isNotNull();
		assertThat(Response.Status.OK.getStatusCode()).isEqualTo(response.getStatus());

		assertThat(entity.content.getApiKey()).isNotNull();
	}

	private void mockGetOrganisation(String name) {
		when(organisationDao.getOrganisation(anyInt())).then(invocation -> {
			Organisation organisation = new Organisation(name);
			organisation.setName(name);
			Account account = new Account();
			account.setEmail("test@example.com");
			account.setPassword("123456");
			organisation.addManager(account);
			organisation.setApiKey("some key");
			return organisation;
		});
	}

	@Test(expected = CredentialException.class)
	public void testGetOrganisationCredentialsWrong() {
		when(accountDao.checkCredentials(anyString(), SecurityTools.encryptWithSHA512(anyString()))).then(invocation -> false);
		mockGetOrganisation("Test Organisation");

		Response response = organisationApi.get("1", null, null);
		ResponseSurrogate<Organisation> entity = (ResponseSurrogate<Organisation>) response.getEntity();
		assertThat(entity).isNull();
		assertThat(entity.info.size()).isAtLeast(1);
	}

	@Test(expected = CredentialException.class)
	public void testGenerateApiKeyCredentialsWrong() {
		when(accountDao.checkCredentials(anyString(), SecurityTools.encryptWithSHA512(anyString()))).then(invocation -> false);
		mockGetOrganisation("Test Organisation");

		Response response = organisationApi.generateApiKey("1", null, null);
		ResponseSurrogate<Organisation> entity = (ResponseSurrogate<Organisation>) response.getEntity();
		assertThat(entity).isNull();
		assertThat(entity.info.size()).isAtLeast(1);
	}

	@Test
	public void testGenerateApiKey() {

		when(accountDao.checkCredentials(anyString(), SecurityTools.encryptWithSHA512(anyString()))).then(invocation -> true);
		mockGetOrganisation("Test Organisation");

		Response response = organisationApi.generateApiKey("1", null, null);
		ResponseSurrogate<Organisation> entity = (ResponseSurrogate<Organisation>) response.getEntity();

		assertThat(entity.content.getApiKey()).isNotNull();
	}

	@Test
	public void testCreateOrganisation() {
		String name = "Test Organisation";
		String email = "test@example.com";

		when(accountDao.checkCredentials(anyString(), SecurityTools.encryptWithSHA512(anyString()))).then(invocation -> true);
		Response response = organisationApi.create(name, email, null);
		ResponseSurrogate<Organisation> entity = (ResponseSurrogate<Organisation>) response.getEntity();

		assertThat(entity.content.getName()).isEqualTo(name);
		assertThat(entity.content.getManagers().size()).isAtLeast(1);
		assertThat(entity.content.getApiKey()).isNotNull();
	}

	@Test
	public void testCreateOrganisationNotAnEmail() throws NoSuchMethodException {
		String id = "1";
		String wrongEmail = "not an email";
		String password = "123456";

		Method method = OrganisationApi.class.getMethod("create", String.class, String.class, String.class);

		Object[] parameterValues = { id, wrongEmail, password };
		Set<ConstraintViolation<OrganisationApi>> violations = executableValidator.validateParameters(organisationApi, method, parameterValues);
		assertThat(violations.size()).is(1);

		Class<? extends Annotation> constraintType = violations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType();
		assertThat(constraintType).isEqualTo(Email.class);
	}

	@Test
	public void testCreateOrganisationNoPassword() throws NoSuchMethodException {
		String id = "1";
		String email = "test@example.com";
		String password = null;

		Method method = OrganisationApi.class.getMethod("create", String.class, String.class, String.class);

		Object[] parameterValues = { id, email, password };
		Set<ConstraintViolation<OrganisationApi>> violations = executableValidator.validateParameters(organisationApi, method, parameterValues);
		assertThat(violations.size()).is(1);

		Class<? extends Annotation> constraintType = violations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType();
		assertThat(constraintType).isEqualTo(NotNull.class);
	}
}