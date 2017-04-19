package info.interactivesystems.gamificationengine.utils;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);
	
	/**
	 * The passed String represents an URL. With this URL a byte[] is created from the image file that was passed as a String that
	 * represents an URL. The format of the image has to be .jpg or .png. Otherwise 
	 * an exception is thrown with the hint, that the URL was not valid.
	 * 
	 * @param fileLocation
	 * 			The path an image can be located. This is an URL.
	 * @return byte[] of the image content.
	 */
	public static byte[] imageToByte(String fileLocation) {
		BufferedImage originalImage;
		byte[] byteImage = null;

		String format = fileLocation.substring(fileLocation.lastIndexOf(".") + 1);

		if (format.equals("png") || format.equals("jpg")) {
			try {
				URL fileImage = new URL(fileLocation);
				originalImage = ImageIO.read(fileImage);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(originalImage, format, baos);
				byteImage = baos.toByteArray();
				 
			} catch (IOException e) {
				throw new ApiError(Response.Status.FORBIDDEN, "No valid url was transferred");
			}
		}else{
			throw new ApiError(Response.Status.FORBIDDEN, "The image format has to be .png or .jpg");
		}
		return byteImage;
	}
	
	/**
	 * The passed byte array is Base64-encoded to ensure that the data is transmitted 
	 * correctly as String.  
	 * 
	 * @param bytes
	 * 			The byte array that should be encoded.
	 * @return The Base64 encoded String of the byte array.
	 */
	public static String encodeByteArrayToBase64(byte[] bytes){
		String b64 = Base64.encodeBase64String(bytes);
		return b64;
	}
}
