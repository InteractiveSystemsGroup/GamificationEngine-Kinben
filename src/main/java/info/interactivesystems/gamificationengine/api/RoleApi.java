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

import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * A role describes which members of an organisation is allowed to do or see
 * particular elements of the engine such as to fulfil a particular task and get
 * its rewards. Each Player can have many different roles such as one for his
 * occupation or the department in which she/he works. But the roles can also be a 
 * part of an invented role system that isn’t oriented towards the work context. All 
 * roles are specific to the respective created organisation. 
 * Ancillary creating and deleting, either all roles of a specific organisation or with 
 * a given id the associated role can be gotten. 
 * The name of one role can also be changed at a later point of time.
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
	 * Creates a new role for the organisation to which the API key belongs to. Because 
	 * of this API key the created role is specific to this organisation. 
	 * If the API key is not valid an analogous message is returned.
	 * 
	 * @param roleName
	 *            The required name of the new role. This field must not be null.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this role belongs to.
	 * @return {@link Response} of {@link Role} in JSON.
	 */
	@POST
	@Path("/")
	@TypeHint(Role.class)
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
	 * Gets all roles of a specific organisation that have been created. If the API key is not 
	 * valid an analogous message is returned.
	 * 
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this role belongs to. This field must not be null.
	 * @return {@link Response} as {@link List} of {@link Role}s in JSON.
	 */
	@GET
	@Path("/*")
	@TypeHint(Role[].class)
	public Response getAll(@QueryParam("apiKey") @ValidApiKey String apiKey) {

		List<Role> roles = roleDao.getRoles(apiKey);
		return ResponseSurrogate.of(roles);
	}

	/**
	 * Gets a specific role of an organisation so the id of the organisation and its API key are needed.
	 * If the API key is not valid an analogous message is returned. It is also checked, if the id is a positive
	 * number otherwise a message for an invalid number is returned.
	 * 
	 * @param id
	 *            Required path parameter id of the role that should be gotten. This parameter is required.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this role belongs to.
	 * @return {@link Response} of {@link Role} in JSON.
	 */
	@GET
	@Path("/{id}")
	@TypeHint(Role.class)
	public Response get(@PathParam("id") @NotNull @ValidPositiveDigit String id, @QueryParam("apiKey") @ValidApiKey String apiKey) {

		int roleId = ValidateUtils.requireGreaterThenZero(id);
		Role role = roleDao.getRole(roleId, apiKey);

		ValidateUtils.requireNotNull(roleId, role);
		return ResponseSurrogate.of(role);
	}

	/**
	 * With this method the name field of one role can be changed. For this the id of the role, the API key of the
	 * specific organisation, the name of the field and the new value are needed.
	 * If the API key is not valid an analogous message is returned. 
	 * 
	 * 
	 * @param id
	 *            The id of the role that should be changed. This parameter is required.
	 * @param attribute
	 *            The name of the attribute which should be changed.  This parameter is required. 
	 * @param value
	 *            The new value of the attribute. This parameter is required.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this role belongs to.
	 * @return {@link Response} of {@link Role} in JSON.
	 */
	@PUT
	@Path("/{id}/attributes")
	@TypeHint(Role.class)
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
	 * Deletes a specific role of an organisation so the id of the organisation and its API key are needed.
	 * If the API key is not valid an analogous message is returned. It is also checked, if the id is a positive
	 * number otherwise a message for an invalid number is returned.
	 * 
	 * @param id
	 *            Required path parameter id of the role that should be gotten. This parameter is required.
	 * @param apiKey
	 *            The valid query parameter API key affiliated to one specific organisation, 
	 *            to which this role belongs to.
	 * @return {@link Response} of {@link Role} in JSON.
	 */
	@DELETE
	@Path("/{id}")
	@TypeHint(Role.class)
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
