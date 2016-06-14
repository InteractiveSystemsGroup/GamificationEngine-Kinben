package info.interactivesystems.gamificationengine.api;

import info.interactivesystems.gamificationengine.api.exeption.ErrorMessage;
import info.interactivesystems.gamificationengine.api.exeption.Notification;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This surrogate is used to add to each returned object an error field. The
 * error field may contain a list of errors. The deputized object is unwrapped,
 * so it seems the error field exists in any response object.
 * 
 * @param <T>
 */
public class ResponseSurrogate<T> {

	@SuppressWarnings("FieldCanBeLocal")
	// @JsonUnwrapped
	@JsonProperty
	final T content;

	@JsonProperty
	final Response.Status contentResponseType;

	@SuppressWarnings("FieldCanBeLocal")
	@JsonProperty
	final List<ErrorMessage> info;

	private ResponseSurrogate(T content, Response.Status contentResponseType, Notification info) {
		this.content = content;
		this.contentResponseType = contentResponseType;
		this.info = info.getErrors();
	}

	public static <T> Response of(Response.Status status, MediaType mediaType, T content, Response.Status contentType, Notification notification) {
		status = Optional.ofNullable(status).orElse(Response.Status.OK);
		mediaType = Optional.ofNullable(mediaType).orElse(MediaType.APPLICATION_JSON_TYPE);

		notification = Optional.ofNullable(notification).orElse(new Notification());

		if (content == null) {
			contentType = Response.Status.BAD_REQUEST;
		} else {
			contentType = Optional.ofNullable(contentType).orElse(Response.Status.OK);
		}

		ResponseSurrogate<T> surrogate = new ResponseSurrogate<>(content, contentType, notification);
		return Response.status(status).type(mediaType).entity(surrogate).build();
	}

	public static <T> Response of(T content) {
		return of(null, null, content, null, null);
	}

	public static <T> Response of(T content, Notification notification) {
		return of(null, null, content, null, notification);
	}

	public static <T> Response of(Response.Status status, T content, Notification notification) {
		return of(status, null, content, null, notification);
	}

	public static <T> Response of(T content, MediaType mediaType) {
		return of(null, mediaType, content, null, null);
	}

	public static <T> Response of(Response.Status status, T content, MediaType mediaType) {
		return of(status, mediaType, content, null, null);
	}

	public static <T> Response created(T content) {
		return created(content, null);
	}

	public static <T> Response created(T content, Notification notification) {
		return of(Response.Status.CREATED, null, content, null, notification);
	}

	public static <T> Response updated(T content) {
		return updated(content, null);
	}

	public static <T> Response updated(T content, Notification notification) {
		return of(Response.Status.OK, null, content, null, notification);
	}

	public static <T> Response deleted(T content) {
		return deleted(content, null);
	}

	public static <T> Response deleted(T content, Notification notification) {
		return of(Response.Status.OK, null, content, null, notification);
	}

}
