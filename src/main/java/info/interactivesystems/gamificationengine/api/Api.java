package info.interactivesystems.gamificationengine.api;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webcohesion.enunciate.metadata.rs.TypeHint;


/**
 * With the API some application information can be queried like the current date and time.
 */
@Path("/")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class Api {

	private static final Logger log = LoggerFactory.getLogger(Api.class);

	/**
	 * A State is the default answer of the engine. It gives information about the current date 
	 * and time as well as the current version. It also shows the used path of the local host and the 
	 * protocols that are supported. 
	 */
	static class State {
		public String date = LocalDateTime.now().toString();
		public String version = "0.0.1-SNAPSHOT";
		public String helpUri = "http://localhost:8080/";

		public final Map<String, List<String>> support = new HashMap<>();
		{
			support.put("protocols", Arrays.asList("http", "https", "json"));
		}
 
		public List<String> authors = Arrays.asList();
	}

	/**
	 * Responses a state of the current status. 
	 * 
	 * @return Response as JSON with for example the current date and time.
	 */
	@GET
	@TypeHint(State.class)
	public Response status() {
		return Response.ok(new State()).build();
	}

}
