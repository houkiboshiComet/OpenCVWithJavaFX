package image.effect;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javafx.scene.image.Image;

public class ImageProcessor {
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	 public static void convert(Mat src, Mat dst, double r, double g, double b) {
		for(int y = 0; y < src.height(); y++) {
			for(int x = 0; x < src.width(); x++) {
				double rgb[] = src.get(y, x);
				rgb[0] += b;
				rgb[1] += g;
				rgb[2] += r;
				dst.put(y, x, rgb);
			}
		}
	 }

	 public static Image toImage(Mat mat) {
		MatOfByte byteMat = new MatOfByte();
	  	Imgcodecs.imencode(".bmp", mat, byteMat);
	  	return new Image(new ByteArrayInputStream(byteMat.toArray()));
	 }
	 public static Mat toMat(BufferedImage image) {
		 Mat mat = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC3);
		 byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		 mat.put(0,0,pixels);
		 return mat;
	 }

	 public static void toEdgeImage(Mat src, Mat dst, boolean blackEdge) {
		 Imgproc.cvtColor(src, dst, Imgproc.COLOR_RGB2GRAY);
		 Imgproc.Canny(dst, dst, 80, 100);
		 if( blackEdge ) { Core.bitwise_not(dst,dst); }
		 Imgproc.cvtColor(dst, dst, Imgproc.COLOR_GRAY2RGB);
	 }

	 public static void filter(Mat src, Mat dst, int bulerLevel) {
		 Mat kernel = new Mat(7,7,CvType.CV_32F);

		 double centerWeight = bulerLevel > 0 ? 1.0 / (1.0 + bulerLevel) : 1.0 - bulerLevel * 0.5;
		 double weghtAdjust = (1.0 - centerWeight) / 64.0;

		 for(int i = 0;  i <  7; i ++) {
			 for(int j = 0;  j <  7; j++) {
				 if( i == 3 && j == 3 ) {
					 kernel.put(i, j, centerWeight);
					 System.out.println(centerWeight);
				 } else if(
					 i >= 2 && i <= 4 &&
					 j >= 2 && j <= 4 ) {
					 /* 2.0 x 8 + 1.25 x 40 = 64 */
					 kernel.put(i, j, weghtAdjust * 2.0);

				 } else {
					 kernel.put(i, j, weghtAdjust * 1.25);
				 }
			 }
		 }
		 Imgproc.filter2D(src, dst, -1, kernel);
	 }

}
