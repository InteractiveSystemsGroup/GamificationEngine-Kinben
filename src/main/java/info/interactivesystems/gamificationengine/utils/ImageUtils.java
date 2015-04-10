package info.interactivesystems.gamificationengine.utils;

import info.interactivesystems.gamificationengine.api.exeption.ApiError;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.ws.rs.core.Response;

public class ImageUtils {

	// method to create byte[] from image file
	// public static byte[] imageToByte(String fileLocation) {
	// File fileImage = new File(fileLocation);
	// BufferedImage originalImage;
	// byte[] byteImage = null;
	//
	// String format = fileLocation.substring(fileLocation.lastIndexOf(".") +
	// 1);
	//
	// if (format.equals("png") || format.equals("jpg")) {
	// try {
	// originalImage = ImageIO.read(fileImage);
	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// ImageIO.write(originalImage, format, baos);
	// byteImage = baos.toByteArray();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// return byteImage;
	// }

	/**
	 * Create a byte[] from image file by an URL
	 * 
	 * @param fileLocation
	 * @return byte[] of the image content
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
				throw new ApiError(Response.Status.FORBIDDEN, "no valid url was transferred");
			}
		}
		return byteImage;
	}

}