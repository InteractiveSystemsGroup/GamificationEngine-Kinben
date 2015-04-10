package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;
import info.interactivesystems.gamificationengine.api.validation.ValidApiKey;
import info.interactivesystems.gamificationengine.api.validation.ValidPositiveDigit;
import info.interactivesystems.gamificationengine.dao.OrganisationDAO;
import info.interactivesystems.gamificationengine.dao.RoleDAO;
import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.Role;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API for roles related services.
 */
@Path("/role")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class RoleApi {

	private static final Logger log = LoggerFactory.getLogger(GoalApi.class);

	@Inject
	OrganisationDAO organisationDao;
	@Inject
	RoleDAO roleDao;

	/**
	 * Creates a new role.
	 * 
	 * @param roleName
	 *            required name of the new role
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Role} in JSON
	 */
	@POST
	@Path("/")
	public Response create(@QueryParam("roleName") @NotNull String roleName, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("create New Role ");

		Organisation organisation = organisationDao.getOrganisationByApiKey(apiKey);

		Role role = new Role();
		role.setName(roleName);
		role.setBelongsTo(organisation);

		roleDao.insert(role);
		return ResponseSurrogate.created(role);
	}

	/**
	 * Get all roles.
	 * 
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link List<Role>} in JSON
	 */
	@GET
	@Path("/*")
	public Response getAll(@QueryParam("apiKey") @ValidApiKey String apiKey) {

		List<Role> roles = roleDao.getRoles(apiKey);
		return ResponseSurrogate.of(roles);
	}

	/**
	 * Get a specific role.
	 * 
	 * @param id
	 *            required id of the role
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Role} in JSON
	 */
	@GET
	@Path("/{id}")
	public Response get(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		int roleId = ValidateUtils.requireGreaterThenZero(id);
		Role role = roleDao.getRole(roleId, apiKey);

		ValidateUtils.requireNotNull(roleId, role);
		return ResponseSurrogate.of(role);
	}

	/**
	 * @param id
	 *            required id of the role
	 * @param attribute
	 *            required name of the attribute which should be changed
	 * @param value
	 *            required new value of the attribute
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Role} in JSON
	 */
	@PUT
	@Path("/{id}/attributes")
	public Response changeAttributes(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("attribute") @NotNull String attribute,
			@QueryParam("value") @NotNull String value, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		log.debug("change Attribute of Role");

		int roleId = ValidateUtils.requireGreaterThenZero(id);
		Role role = roleDao.getRole(roleId, apiKey);

		// not changeable: id -> generated & belongsTo;
		switch (attribute) {
		case "name":
			role.setName(value);
			break;
		}

		roleDao.insert(role);
		return ResponseSurrogate.updated(role);
	}

	/**
	 * @param id
	 *            required id of the role
	 * @param apiKey
	 *            a valid query param api key affiliated to an organisation
	 * @return {@link Response} of {@link Role} in JSON
	 */
	@DELETE
	@Path("/{id}")
	public Response delete(@PathParam("id") @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {
		if (id == null) {
			throw new ApiError(Response.Status.FORBIDDEN, "no goalId transferred");
		}

		int roleId = ValidateUtils.requireGreaterThenZero(id);
		Role role = roleDao.delete(roleId, apiKey);

		ValidateUtils.requireNotNull(roleId, role);
		return ResponseSurrogate.deleted(role);
	}

}
