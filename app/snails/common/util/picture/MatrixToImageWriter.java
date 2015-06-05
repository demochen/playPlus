package snails.common.util.picture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * @ClassName: BitMatrix
 * @Description: 生成二维码
 * @author chenlinlin
 * @date 2015-2-20 上午12:00:49
 */

public class MatrixToImageWriter {

	private static final int BLACK = 0xFF000000;
	private static final int WHITE = 0xFFFFFFFF;
	private static Logger logger = LoggerFactory.getLogger(MatrixToImageWriter.class);

	private MatrixToImageWriter(){
	}

	public static BufferedImage toBufferedImage(BitMatrix matrix) {
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
			}
		}
		return image;
	}

	public static void writeToFile(BitMatrix matrix, String format, File file) throws IOException {
		BufferedImage image = toBufferedImage(matrix);
		if (!ImageIO.write(image, format, file)) {
			throw new IOException("Could not write an image of format " + format + " to " + file);
		}
	}

	public static void writeToStream(BitMatrix matrix, String format, OutputStream stream) throws IOException {
		BufferedImage image = toBufferedImage(matrix);
		if (!ImageIO.write(image, format, stream)) {
			throw new IOException("Could not write an image of format " + format);
		}
	}

	public static boolean generateCode(String url, String filePath) {
		boolean flag = true;
		QRCodeWriter writer = new QRCodeWriter();
		try {
			BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, 400, 400);
			File file = new File(filePath);

			MatrixToImageWriter.writeToFile(matrix, "png", file);
		} catch (WriterException e) {
			flag = false;
		} catch (IOException e) {
			flag = false;
		}
		return flag;
	}

	public static void main(String args[]) {
		QRCodeWriter writer = new QRCodeWriter();
		try {
			BitMatrix matrix = writer.encode("www.baidu.com", BarcodeFormat.QR_CODE, 400, 400);
			File file = new File("E://cll.png");
			MatrixToImageWriter.writeToFile(matrix, "png", file);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
