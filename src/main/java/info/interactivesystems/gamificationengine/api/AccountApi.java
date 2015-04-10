package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.CredentialException;
import info.interactivesystems.gamificationengine.dao.AccountDAO;
import info.interactivesystems.gamificationengine.entities.Account;

import java.util.Optional;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.validator.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API for account related services.
 */
@Path("/account")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class AccountApi {

	private static final Logger log = LoggerFactory.getLogger(AccountApi.class);

	@Inject
	AccountDAO accountDao;

	/**
	 * Returns an account corresponding to an email. Requires valid credentials.
	 *
	 * @param email
	 *            A required valid email.
	 * @param password
	 *            Required query param.
	 *
	 * @return a {@link javax.ws.rs.core.Response} of {@link Account} in JSON
	 */
	@GET
	@Path("/")
	public Response get(@QueryParam("email") @NotNull @Email String email, @QueryParam("password") @NotNull String password) {

		log.debug("get account requested");

		if (!accountDao.checkCredentials(email, password)) {
			log.warn("Account with wrong credentials (email:\"%s\", pass:\"%s\") requested", email, password);
			throw new CredentialException(email);
		}

		Account account = accountDao.getAccount(email);
		log.debug("Account requested: %s", account);
		return ResponseSurrogate.of(account);
	}

	/**
	 * Create a new account. Email and password are mandatory.
	 *
	 * @param email
	 *            A required valid email.
	 * @param password
	 *            Required query param.
	 * 
	 * @param firstName
	 *            Optional first name.
	 * @param lastName
	 *            Optional last name.
	 * 
	 * @return a {@link javax.ws.rs.core.Response} of {@link Account} in JSON
	 */
	@POST
	@Path("/")
	public Response create(@QueryParam("email") @NotNull @Email String email, @QueryParam("password") @NotNull String password,
			@QueryParam("firstName") String firstName, @QueryParam("lastName") String lastName) {

		log.debug("create account requested");

		Account account = new Account(email);
		account.setPassword(password);
		account.setFirstName(firstName);
		account.setLastName(lastName);
		accountDao.persist(account);

		log.debug("Persisted account: %s", account);
		return ResponseSurrogate.created(account);
	}

	/**
	 * Updates an existing account. Email and password are mandatory.
	 *
	 * @param email
	 *            A required valid email.
	 * @param password
	 *            Required query param.
	 * 
	 * @param firstName
	 *            Optional first name.
	 * @param lastName
	 *            Optional last name.
	 * 
	 * @return a {@link javax.ws.rs.core.Response} of {@link Account} in JSON
	 */
	@PUT
	@Path("/")
	public Response update(@QueryParam("email") @NotNull @Email String email, @QueryParam("password") @NotNull String password,
			@QueryParam("firstName") String firstName, @QueryParam("lastName") String lastName) {

		log.debug("update account requested");

		if (!accountDao.checkCredentials(email, password)) {
			log.warn("Account with wrong credentials (email:\"%s\", pass:\"%s\") requested", email, password);
			throw new CredentialException(email);
		}

		Account account = accountDao.getAccount(email);
		Optional.ofNullable(password).ifPresent(account::setPassword);
		Optional.ofNullable(firstName).ifPresent(account::setFirstName);
		Optional.ofNullable(lastName).ifPresent(account::setLastName);
		accountDao.persist(account);

		log.debug("Updated account: %s", account);
		return ResponseSurrogate.updated(account);
	}
}
