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

import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * An Organisation represents for example a specific company or an association which 
 * represents a group of people belonging together and which are participating in the 
 * gamification process.
 * An Organisation possessed an generated API key which is needed for all further interactions
 * because all database entries are associated with this unique key and so with the respective 
 * organisation. The API key is uniquely in the whole application. It
 * may be changed, for this reason it has no primary key. 
 * When an Organisation is created it has to be connected with an account. Each organisation 
 * may be managed by many people, but at least by one who is added to the list of the manager 
 * of the respective organisation and so also the Account.  
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
	 * Creates a new organisation. The email address and password of one Account are used 
	 * to connect it with this organisation. So the email address and password are mandatory for
	 * authentication otherwise a warning with the hint for wrong credentials is returned.
	 * All further Accounts which should be associated to this organisation are added with the 
	 * method addManager. 
	 *
	 * @param name 
	 * 			The name of the developer or the manager of the account.
	 * @param email
	 *           The required valid email address.
	 * @param password
	 *           Required query param associated with the email address. 
	 * @return A Response of Organisation in JSON.
	 */
	@POST
	@Path("/")
	@TypeHint(Organisation.class)
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
	 * Adds a new developer to the organisation's list of manager. The email address and 
	 * password are mandatory for authentication otherwise a warning with the hint for 
	 * wrong credentials is returned. If the manager who should be added is already in the 
	 * list, a message is given with the hint that she/he is already added. 
	 *
	 * @param manager
	 *            The required valid email address for the new manager.
	 * @param email
	 *            The required valid email.
	 * @param password
	 *             Required query parameter associated with the email address. 
	 *@param apiKey
	 *			The API key of the organisation to which the manager belongs to.    
	 * @return A Response of Organisation in JSON.
	 */
	@POST
	@Path("/addManager")
	@TypeHint(Organisation.class)
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
	 * Returns all organisations which are associated with the combination of the two 
	 * query parameters. Otherwise an exception is sent that the given credentials are wrong.
	 * 
	 * @param email
	 *            A required valid email address. 
	 * @param password
	 *            Required query param to connect it with the given 
	 *            email address.
	 * @return A Response as List of Organisations in JSON.
	 */
	@GET
	@Path("/*")
	@TypeHint(Organisation[].class)
	public Response get(@QueryParam("email") @Email String email, @QueryParam("password") @NotNull String password) {

		log.debug("get organisation requested");

		if (!accountDao.checkCredentials(email, password)) {
			throw new CredentialException(email);
		}

		List<Organisation> organisation = organisationDao.getAllOrganisations(email);
		return ResponseSurrogate.of(organisation);
	}

	/**
	 * Returns a specific organisation which id is equal to the transfered path parameter. 
	 * Additionally the email address and the associated password are mandatory and have to be
	 * correct otherwise an exception is returned that the given credentials are wrong.
	 * 
	 * @param id
	 *            The path parameter of the organisation, that should be returned.
	 * @param email
	 *            The valid email address.
	 * @param password
	 *             Required query parameter to connect it with the given 
	 *             email address.
	 * @return A Response of Organisation in JSON.
	 */
	@GET
	@Path("/{id}")
	@TypeHint(Organisation.class)
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
	 * Generates an API key for the given organisation which matches the id, email address and the
	 * associated password. Otherwise an exception is returned that the given credentials are wrong.
	 * If the API key field is already set the method resets it and replaced it with the new generated
	 * API key.  
	 * 
	 * @param id
	 *            The path parameter of the organisation, for which the API key should be generated.
	 * @param email
	 *           The valid email address. 
	 * @param password
	 *            Required query parameter to connect it with the given 
	 *            email address.
	 * @return A Response of Organisation in JSON.
	 */
	@PUT
	@Path("/{id}/generateapikey")
	@TypeHint(Organisation.class)
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
