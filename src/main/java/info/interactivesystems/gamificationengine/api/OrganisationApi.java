package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.api.exeption.CredentialException;
import info.interactivesystems.gamificationengine.api.exeption.Notification;
import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
import info.interactivesystems.gamificationengine.dao.AccountDAO;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.entities.Account;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.utils.SecurityTools;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.validator.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API for organisation related services.
 */
@Path("/organisation")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class OrganisationApi {

	private static final Logger log = LoggerFactory.getLogger(OrganisationApi.class);

	@Inject
	OrganisationDAO organisationDao;
	@Inject
	AccountDAO accountDao;

	/**
	 * Create a new developer organisation. Email and password are mandatory for
	 * authentication.
	 *
	 * @param email
	 *            required valid email
	 * @param password
	 *            required query param
	 * @return a {@link Response} of {@link Organisation} in JSON
	 */
	@POST
	@Path("/")
	public Response create(@QueryParam("name") String name, @QueryParam("email") @NotNull @Email String email,
			@QueryParam("password") @NotNull String password) {

		log.debug("create organisation requested");

		if (!accountDao.checkCredentials(email, password)) {
			throw new CredentialException(email);
		}

		Organisation organisation = new Organisation();
		organisation.setName(name);
		organisation.addManager(accountDao.getAccount(email));
		organisation.setApiKey(SecurityTools.generateApiKey());

		organisationDao.insertOrganisation(organisation);
		return ResponseSurrogate.created(organisation);
	}

	/**
	 * Adds a new developer to an organisation. Email and password are mandatory
	 * for authentication.
	 *
	 * @param manager
	 *            required valid email for new manager
	 * @param email
	 *            required valid email
	 * @param password
	 *            required query param
	 * @return a {@link Response} of {@link Organisation} in JSON
	 */
	@POST
	@Path("/addManager")
	public Response addManager(@QueryParam("manager") @NotNull @Email String manager, @QueryParam("email") @NotNull @Email String email,
			@QueryParam("password") @NotNull String password, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("add organisation requested");

		if (!accountDao.checkCredentials(email, password)) {
			throw new CredentialException(email);
		}

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);
		Account account = accountDao.getAccount(manager);

		if (account == null) {
			throw new ApiError(Response.Status.NOT_FOUND, "Account %s not found", email);
		}

		if (organisation.getManagers().contains(account)) {
			throw new ApiError(Response.Status.NOT_ACCEPTABLE, "Account %s already in managers list", email);
		}

		organisation.addManager(account);
		return ResponseSurrogate.created(organisation);
	}

	/**
	 * Returns all organisations which are assosiated with the query param.
	 * 
	 * @param email
	 *            required valid email
	 * @param password
	 *            required query param
	 * @return {@link Response} of {@link List<Organisation>} in JSON
	 */
	@GET
	@Path("/*")
	public Response get(@QueryParam("email") @Email String email, @QueryParam("password") @NotNull String password) {

		log.debug("get organisation requested");

		if (!accountDao.checkCredentials(email, password)) {
			throw new CredentialException(email);
		}

		List<Organisation> organisation = organisationDao.getAllOrganisations(email);
		return ResponseSurrogate.of(organisation);
	}

	/**
	 * Returns a specific organisation which id is equal to transfered query
	 * param.
	 * 
	 * @param id
	 *            path param of an organisation
	 * @param email
	 *            required valid email
	 * @param password
	 *            required query param
	 * @return {@link Response} of {@link Organisation} in JSON
	 */
	@GET
	@Path("/{id}")
	public Response get(@PathParam("id") String id, @QueryParam("email") @Email String email, @QueryParam("password") @NotNull String password) {

		log.debug("get organisation requested");

		if (!accountDao.checkCredentials(email, password)) {
			throw new CredentialException(email);
		}

		int intId = Integer.valueOf(id);

		Organisation organisation = organisationDao.getOrganisation(intId);
		return ResponseSurrogate.of(organisation);
	}

	/**
	 * Generate apiKey for given organisation. Resets api-key field if already
	 * set.
	 * 
	 * @param id
	 *            path param of an organisation
	 * @param email
	 *            a valid email
	 * @param password
	 *            required query param
	 * @return {@link Response} of {@link Organisation} in JSON
	 */
	@PUT
	@Path("/{id}/generateapikey")
	public Response generateApiKey(@PathParam("id") String id, @QueryParam("email") @Email String email,
			@QueryParam("password") @NotNull String password) {
		Notification notification = new Notification();

		log.debug("generate api key requested");
		if (!accountDao.checkCredentials(email, password)) {
			throw new CredentialException(email);
		}

		int intId = Integer.valueOf(id);

		Organisation organisation = organisationDao.getOrganisation(intId);
		organisation.setApiKey(SecurityTools.generateApiKey());

		return ResponseSurrogate.updated(organisation, notification);
	}
}
