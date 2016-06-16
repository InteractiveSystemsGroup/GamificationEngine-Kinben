package info.interactivesystems.gamificationengine.api;

import java.io.ByteArrayOutputStream;

import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/qr")
@Stateless
@Produces("image/png")
// matching response type, image instead of jason
public class QrApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(QrApi.class);

	/**
	 * Creates a QR code from any content.
	 * 
	 * @param content
	 *            The content that should be presented as a QR code. This field must 
	 *            not be null.
	 * @return QrCode as download.
	 */
	@GET
	@Path("/{content}")
	public Response getQrCode(@PathParam("content") String content) {
		LOGGER.debug("GetQrCode called");

		// create QRCode from "content"
		ByteArrayOutputStream out = QRCode.from(content).to(ImageType.PNG).stream();

		// return QRCode as image
		ResponseBuilder response = Response.ok(out.toByteArray()); // convert to
																	// byte
																	// array
		response.header("Content-Disposition", "attachment; filename=test.png"); // file
																					// name
																					// of
																					// image
		return response.build();
	}

}
