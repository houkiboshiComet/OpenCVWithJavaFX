package image.effect;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

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

	 public static Mat toPng(BufferedImage image) {
		 Mat mat = toMat(image);
		 Mat transpalent = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8U, new Scalar(255));
		 Mat result = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC4, new Scalar(255));

		 for(int y = 0; y < mat.height(); y++) {
				for(int x = 0; x < mat.width(); x++) {
					double rgb[] = mat.get(y, x);
					if(rgb[0] >= 253 && rgb[1] >= 253 && rgb[2] >= 253) {
						transpalent.put(y, x, 0);
					}
				}
		 }
		 List<Mat> list = new ArrayList<Mat>();
		 Core.split(mat, list);
		 list.add(transpalent);
		 Core.merge(list, result);
		 return result;
	 }

	 public static void detect(Mat src, Mat dst, String xml, Scalar color, int thickness ) {

		 src.copyTo(dst);
		 CascadeClassifier faceDetector = new CascadeClassifier(xml);
		 MatOfRect faces = new MatOfRect();
		 faceDetector.detectMultiScale(dst, faces);
	        for (Rect rect : faces.toArray()) {
	            Imgproc.rectangle(
	            		dst,
	                    new Point(rect.x, rect.y),
	                    new Point(rect.x + rect.width, rect.y + rect.height),
	                    color,thickness);
	        }
	 }

	 public static void toEdgeImage(Mat src, Mat dst, boolean blackEdge, int threah) {
		 Imgproc.cvtColor(src, dst, Imgproc.COLOR_RGB2GRAY);
		 Imgproc.Canny(dst, dst, threah, threah * 1.5);
		 if( blackEdge ) { Core.bitwise_not(dst,dst); }
		 Imgproc.cvtColor(dst, dst, Imgproc.COLOR_GRAY2RGB);
	 }

	 public static void toEdgeColorImage(Mat src, Mat dst, int threah) {
		if(src == dst) {
			src = src.clone();
		}
		 Imgproc.cvtColor(src, dst, Imgproc.COLOR_RGB2GRAY);
		 Imgproc.Canny(dst, dst, threah, threah * 1.5);
		 Imgproc.cvtColor(dst, dst, Imgproc.COLOR_GRAY2RGB);
		 Core.bitwise_not(dst,dst);

		 Core.add(src, dst, dst);
	 }

	 public static void gammaCorrection(Mat src, Mat dst, double gamma ) {
		 Mat lut = new Mat(1, 256, CvType.CV_8UC1);

		 for (int i = 0; i < 256; i++) {
		    	lut.put(0, i, Math.pow((double)(1.0 * i/255), 1/gamma) * 255);
		 }
		 Core.LUT(src, lut, dst);
	 }

	 public static void warm(Mat src, Mat dst, double ratio) {

		double warmWeight = ratio;
		double srcWeight = (1.0 - ratio);

		 for(int y = 0; y < src.height(); y++) {
				for(int x = 0; x < src.width(); x++) {
					double rgb[] = src.get(y, x);
					double gray = rgb[0] * 0.1 + rgb[1] * 0.6 + rgb[2] * 0.3;
					rgb[0] = rgb[0] * srcWeight + gray * warmWeight * 0.7;
					rgb[1] = rgb[1] * srcWeight + gray * warmWeight * 0.8;
					rgb[2] = rgb[2] * srcWeight + gray * warmWeight * 1.4;
					dst.put(y, x, rgb);
				}
		 }
	 }

	 public static void glow(Mat src, Mat dst, int rgb, int intensity) {
		 Mat blur = new Mat(src.height(),src.width(), CvType.CV_8UC3);

		 int size = Math.max(1, intensity);
		 size = size * 2 + 1;

		 Imgproc.GaussianBlur(src, blur, new Size(size, size), intensity * 10);

		 for(int y = 0; y < src.height(); y++) {
				for(int x = 0; x < src.width(); x++) {
					double rgb_src[] = src.get(y, x);
					double rgb_blur[] = blur.get(y, x);
					if( rgb_src[rgb] < rgb_blur[rgb] ) {
						rgb_src[rgb] = rgb_blur[rgb];
					}
					dst.put(y, x, rgb_src);
				}
		 }
	 }

	 public static void glow(Mat src, Mat dst, int intensity) {
		 Mat blur = new Mat(src.height(),src.width(), CvType.CV_8UC3);

		 double gamma = Math.pow(4.0, 1.0 / (double) intensity);

		 gammaCorrection(src, blur, gamma);

		 int size = Math.max(1, intensity);
		 size = size * 2 + 1;
		 Imgproc.GaussianBlur(src, blur, new Size(size, size), intensity * 10);

		 for(int y = 0; y < src.height(); y++) {
				for(int x = 0; x < src.width(); x++) {
					double rgb_src[] = src.get(y, x);
					double rgb_blur[] = blur.get(y, x);
					if( rgb_src[0] < rgb_blur[0] ) {
						rgb_src[0] = rgb_blur[0];
					}
					if( rgb_src[1] < rgb_blur[1] ) {
						rgb_src[1] = rgb_blur[1];
					}
					if( rgb_src[2] < rgb_blur[2] ) {
						rgb_src[2] = rgb_blur[2];
					}
					dst.put(y, x, rgb_src);
				}
			}
	 }

	 public static void toCartoon(Mat src, Mat dst, int value) {
		 Imgproc.pyrMeanShiftFiltering(src, dst, value, value * 2);
		 Mat edge = dst.clone();
		 ImageProcessor.toEdgeImage(edge, edge, false, 80);
		 Core.subtract(dst, edge, dst);
	 }

	 public static Mat extend(Mat img, Size newSize, int x, int y ) {

		 Mat matrix = new Mat(2, 3,CvType.CV_32F);
		 matrix.put(0, 0, 1.0);
		 matrix.put(1, 1, 1.0);
		 matrix.put(0, 2, x);
		 matrix.put(1, 2, y);

		 Mat result = new Mat(newSize, img.type(), new Scalar(0,0,0,0));
		 Imgproc.warpAffine(img, result, matrix, newSize, Imgproc.INTER_LINEAR, Core.BORDER_TRANSPARENT, new Scalar(0,0,0,0) );
		 return result;
	 }

	 public static void synthesize(Mat src, Mat dst, Mat stamp, Mat filter, int transparent ) {
		 Core.multiply(stamp, filter, stamp, 1.0 /255);
		 Mat not = new Mat();
		 Core.bitwise_not(filter, not);
		 Core.add(not, new Scalar(transparent,transparent,transparent), not);
		 Core.multiply(src, not, dst, 1.0 /255);
		 Core.add(dst,stamp,dst);
	 }

	 public static void split(Mat png, Mat rgb, Mat transsparent) {
		 List<Mat> rgba = new ArrayList<Mat>();
		 List<Mat> list= new ArrayList<Mat>();
		 Core.split(png, rgba);
		 list.add(rgba.get(3));
		 list.add(rgba.get(3));
		 list.add(rgba.get(3));
		 Core.merge(list, transsparent);

		 list.clear();
		 list.add(rgba.get(0));
		 list.add(rgba.get(1));
		 list.add(rgba.get(2));
		 Core.merge(list, rgb);
	 }

	 public static void addStars(Mat src, Mat dst, Mat shape, int count ) {
		 Mat matrix = new Mat(2,3,CvType.CV_32F);
		 src.copyTo(dst);

		 for(int i  = 0; i < count; i++ ) {
			Mat star = shape.clone();
			Mat starImage = new Mat(src.size(),CvType.CV_8UC3);
			double size = 2.0 * Math.random();
			double angle = Math.PI * Math.random();
			matrix.put(0, 0, size * Math.cos(angle));
			matrix.put(0, 1, 0  * - Math.sin(angle));
			matrix.put(1, 0, 0  * Math.sin(angle));
			matrix.put(1, 1, size * Math.cos(angle));
			matrix.put(0, 2, Math.random() * src.cols());
			matrix.put(1, 2, Math.random() * src.rows());
			ImageProcessor.convert(star, star, -150 *  Math.random(), -150 * Math.random(),-150 * Math.random());
			Imgproc.GaussianBlur(star, star, new Size(9,9), 10);
			Imgproc.warpAffine(star,starImage, matrix, src.size(), Imgproc.INTER_LINEAR, Core.BORDER_TRANSPARENT, new Scalar(0,0,0,0) );
			Core.add(dst, starImage, dst);
		 }

	 }

	 public static void addSnow(Mat src, Mat dst, int count ) {
		 Mat matrix = new Mat(2,3,CvType.CV_32F);
		 src.copyTo(dst);
		 Mat circle = new Mat(40, 40,CvType.CV_8UC3, new Scalar(0,0,0));
		 Imgproc.circle(circle, new Point(20,20), 15, new Scalar(255,255,255),-1,Imgproc.LINE_AA,0);

		 for(int i  = 0; i < count; i++ ) {
			Mat bubble = circle.clone();
			Mat bubbleImage = new Mat(src.size(),CvType.CV_8UC3);
			double size = Math.random();
			double angle = Math.PI * (Math.random() / 4 );
			matrix.put(0, 0, size * Math.cos(angle) * 2);
			matrix.put(0, 1, size  * - Math.sin(angle));
			matrix.put(1, 0, size  * Math.sin(angle) * 2);
			matrix.put(1, 1, size * Math.cos(angle));
			matrix.put(0, 2, Math.random() * src.cols());
			matrix.put(1, 2, Math.random() * src.rows());
			ImageProcessor.convert(bubble, bubble, - 50 * Math.random(), -50 * Math.random(), -30 * Math.random());
			Imgproc.GaussianBlur(bubble, bubble, new Size(9, 9), 10);
			Imgproc.warpAffine(bubble,bubbleImage, matrix, src.size(), Imgproc.INTER_LINEAR, Core.BORDER_TRANSPARENT, new Scalar(0,0,0,0) );
			Core.add(dst, bubbleImage, dst);
		 }

	 }


	 public static void addBubble (Mat src, Mat dst, int count ) {
		 Mat matrix = new Mat(2,3,CvType.CV_32F);
		 src.copyTo(dst);
		 Mat circle = new Mat(50, 50,CvType.CV_8UC3, new Scalar(0,0,0));
		 Imgproc.circle(circle, new Point(25,25), 20, new Scalar(150,100,100),-1,Imgproc.LINE_AA,0);

		 for(int i  = 0; i < count; i++ ) {
			Mat bubble = circle.clone();
			Mat bubbleImage = new Mat(src.size(),CvType.CV_8UC3);
			double size = 2.0 * Math.random();
			matrix.put(0, 0, size);
			matrix.put(1, 1, size);
			matrix.put(0, 2, Math.random() * src.cols());
			matrix.put(1, 2, Math.random() * src.rows());
			ImageProcessor.convert(bubble, bubble, -100 * Math.random(), -100 * Math.random(),-100 * Math.random());
			Imgproc.GaussianBlur(bubble, bubble, new Size(15, 15), 10);
			Imgproc.warpAffine(bubble,bubbleImage, matrix, src.size(), Imgproc.INTER_LINEAR, Core.BORDER_TRANSPARENT, new Scalar(0,0,0,0) );
			Core.add(dst, bubbleImage, dst);
		 }

	 }

	 public static void filter(Mat src, Mat dst, int bulerLevel) {
		 Mat kernel = new Mat(7,7,CvType.CV_32F);

		 double centerWeight = bulerLevel > 0 ? 1.0 / (1.0 + bulerLevel) : 1.0 - bulerLevel * 0.5;
		 double weghtAdjust = (1.0 - centerWeight) / 64.0;

		 for(int i = 0;  i <  7; i ++) {
			 for(int j = 0;  j <  7; j++) {
				 if( i == 3 && j == 3 ) {
					 kernel.put(i, j, centerWeight);
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
