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

/**
 * API class for application information
 */
@Path("/")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class Api {

	private static final Logger log = LoggerFactory.getLogger(Api.class);

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
	 * Responses a state of current status. 
	 * 
	 * @return Response as JSON with for example the current date and time.
	 */
	@GET
	public Response status() {
		return Response.ok(new State()).build();
	}

}
