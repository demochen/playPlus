package snails.common.util.picture;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.StandardStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class PictureDeal {

	private static final Logger log = LoggerFactory.getLogger(PictureDeal.class);
	private static PictureDeal instance = new PictureDeal();

	public static PictureDeal getInstance() {
		return instance;
	}

	private PictureDeal(){

	}

	public void picturesToZip(String tagPath, String[] sourcePath) throws Exception {

		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tagPath));
		File[] files = new File[sourcePath.length];
		for (int i = 0; i < files.length; i++) {
			files[i] = new File(sourcePath[i]);
		}
		byte[] b = new byte[1024];
		for (int j = 0; j < files.length; j++) {
			FileInputStream in = new FileInputStream(files[j]);
			out.putNextEntry(new ZipEntry(files[j].getName()));
			int len = 0;
			while ((len = in.read(b)) > -1) {
				out.write(b, 0, len);
			}
			out.closeEntry();
			in.close();
		}
		out.close();

	}

	public String cutImage(String sourcePath, String targetPath, int x1, int y1, int x2, int y2) throws IOException {
		File imageFile = new File(sourcePath);
		if (!imageFile.exists()) {
			throw new IOException("Not found the images:" + sourcePath);
		}
		if (targetPath == null || targetPath.isEmpty()) targetPath = sourcePath;
		String format = sourcePath.substring(sourcePath.lastIndexOf(".") + 1, sourcePath.length());
		BufferedImage image = ImageIO.read(imageFile);
		image = image.getSubimage(x1, y1, x2, y2);
		System.out.println(image);
		ImageIO.write(image, format, new File(targetPath));
		return targetPath;
	}

	public void scale(String srcImageFile, String result, boolean flag) throws IOException {

		BufferedImage src = ImageIO.read(new File(srcImageFile)); // 读入文件
		int width = src.getWidth(); // 得到源图宽
		int height = src.getHeight(); // 得到源图长
		int targetheight = (480 * height) / width;
		Image image = src.getScaledInstance(480, targetheight, Image.SCALE_SMOOTH);
		BufferedImage tag = new BufferedImage(480, targetheight, BufferedImage.TYPE_INT_RGB);
		Graphics g = tag.getGraphics();
		g.drawImage(image, 0, 0, null); // 绘制缩小后的图
		g.dispose();
		ImageIO.write(tag, "JPG", new File(result));// 输出到文件流

	}

	public void scale(String srcImageFile, String result) {
		long starttime = new Date().getTime();
		IMOperation op = new IMOperation();
		op.addImage();
		op.resize(480, 960);
		op.addImage();
		ConvertCmd cmd = new ConvertCmd(true);
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.indexOf("win") >= 0) {
			cmd.setSearchPath("D://GraphicsMagick-1.3.19-Q8");
		}
		cmd.setErrorConsumer(StandardStream.STDERR);
		try {
			cmd.run(op, srcImageFile, result);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IM4JavaException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		long endTime = new Date().getTime();
		log.error("++++++scale img cost time " + String.valueOf(endTime - starttime));
	}

	public void scale2(String srcImageFile, String result) throws IOException {
		IMOperation op = new IMOperation();
		op.addImage(srcImageFile);
		op.resize(480, 900);
		op.addImage(result);
		ConvertCmd cmd = new ConvertCmd(true);
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.indexOf("win") >= 0) {
			cmd.setSearchPath("E://GraphicsMagick-1.3.19-Q8");
		}
		cmd.setErrorConsumer(StandardStream.STDERR);
		try {
			cmd.run(op);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IM4JavaException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param imagePath 原路径
	 * @param newPath 新路径
	 * @param x 裁剪的横坐标
	 * @param y 裁剪纵坐标
	 * @param width 裁剪的宽度
	 * @param height 裁剪的高度
	 * @return
	 */
	public void cutImageByGm(String imagePath, String newPath, int x, int y, int width, int height) {
		try {
			IMOperation op = new IMOperation();
			op.addImage(imagePath);
			op.crop(width, height, x, y);
			op.addImage(newPath);
			ConvertCmd convert = new ConvertCmd(true);
			convert.run(op);
		} catch (Exception e) {
			log.error("PictureDeal", e);
		}
	}

	public static String generateQRCode(long brandsNo, String path, String content) {

		QRCodeWriter writer = new QRCodeWriter();
		try {
			BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 400, 400);
			File file = new File(path);
			MatrixToImageWriter.writeToFile(matrix, "png", file);
		} catch (WriterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}

}
