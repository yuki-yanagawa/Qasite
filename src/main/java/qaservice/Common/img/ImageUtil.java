package qaservice.Common.img;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStreamImpl;

import qaservice.Common.charcterutil.CharUtil;

public class ImageUtil {
	public static enum FileType {
		PNG,
		JPEG
	}

	public static String reCreateBase64ImgData(byte[] base64ImgData, int squareSize) throws IOException {
		String imgRawStr = new String(base64ImgData, CharUtil.getCharset());
		String[] imgRawStrLine = imgRawStr.split(",");
		String tmpHead = imgRawStrLine[0];
		String tmpBase64ImgData = imgRawStrLine[1];
		byte[] decodeData = Base64.getDecoder().decode(tmpBase64ImgData);
		byte[] resizeBinData = resizeSquare(decodeData, squareSize, ImageUtil.getFileTypeFromImgHeader(tmpHead));
		String retBase64 = Base64.getEncoder().encodeToString(resizeBinData);
		return tmpHead + "," + retBase64;
	}

	public static byte[] resizeSquare(byte[] imgBindata, int squareSize, FileType fileType) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(imgBindata);
		BufferedImage buf = ImageIO.read(bis);
		if(buf.getWidth() <= squareSize && buf.getHeight() <= squareSize) {
			return imgBindata;
		}

		int newWidth = -1;
		int newHeight = -1;
		if(buf.getWidth() == buf.getHeight()) {
			newWidth = squareSize;
			newHeight = squareSize;
		} else if(buf.getWidth() <= squareSize) {
			newHeight = squareSize;
			int tmp = squareSize * buf.getWidth();
			double tmpWidth = tmp / buf.getHeight();
			newWidth = (int)Math.round(tmpWidth);
		} else if(buf.getHeight() <= squareSize) {
			newWidth = squareSize;
			int tmp = squareSize * buf.getHeight();
			double tmpHeight = tmp / buf.getWidth();
			newHeight = (int)Math.round(tmpHeight);
		} else {
			if(buf.getHeight() < buf.getWidth()) {
				newWidth = squareSize;
				int tmp = squareSize * buf.getHeight();
				double tmpHeight = tmp / buf.getWidth();
				newHeight = (int)Math.round(tmpHeight);
			} else {
				newWidth = squareSize;
				int tmp = squareSize * buf.getHeight();
				double tmpHeight = tmp / buf.getWidth();
				newHeight = (int)Math.round(tmpHeight);
			}
		}
		Image resizeImage = buf.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
		BufferedImage newImage = new BufferedImage(newWidth, newHeight, buf.getType());
		Graphics2D g = newImage.createGraphics();
		g.drawImage(resizeImage, 0, 0, null);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(newImage, createFileExtension(fileType), bos);
		return bos.toByteArray();
	}

	public static void resize(byte[] imgBindata, int height, int width) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(imgBindata);
		BufferedImage buf = ImageIO.read(bis);
		
	}

	private static String createFileExtension(FileType fileType) {
		switch(fileType) {
		case PNG:
			return "png";
		case JPEG:
			return "jpeg";
		default:
			return null;
		}
	}

	public static FileType getFileTypeFromImgHeader(String imgHeader) {
		String extFile = imgHeader.replaceAll("data:", "").split(";")[0].trim();
		for(FileType f : FileType.values()) {
			if(extFile.toUpperCase().equals("IMAGE/" + f.name())) {
				return f;
			}
		}
		return null;
	}
}
