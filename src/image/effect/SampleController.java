package image.effect;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

public class SampleController implements Initializable {


	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}

	@FXML
	private ImageView imageView;

	@FXML
	private Label clockLabel;

	@FXML
	private CheckBox checkBox;

	@FXML
	private Slider Rbar, Gbar, Bbar, blurBar, effectBar;

	@FXML
	private ColorPicker colorPicker;

	@FXML
	private ComboBox<String> comboBox;

	@FXML
	private ComboBox<String> stampTypes;

	private Mat mat = new Mat();
	private Mat original = new Mat();
	private Mat base = new Mat();

	private Mat goodStamp, twinckeStamp, smileStamp;


	final FileChooser fileChooser = new FileChooser();

	 private void apllyBaseProcess(Mat src, Mat dst) {
		 ImageProcessor.convert(src, dst, Rbar.getValue() - 128,  Gbar.getValue() - 128, Bbar.getValue() - 128);
		 ImageProcessor.filter(dst, dst, (int) blurBar.getValue());
	  	 imageView.setImage(ImageProcessor.toImage(mat));
	 }

	 @FXML
	 public void saveImage(ActionEvent event) {
		 Imgcodecs.imwrite("C:\\image.bmp", mat);


	 }

	 @FXML
	 public void onReset(ActionEvent event) {
		 reset();
	 }

	 private void reset() {
		 Rbar.setValue(128);
		 Bbar.setValue(128);
		 Gbar.setValue(128);
		 blurBar.setValue(0);

		 //comboBox.setValue("None");
		 effectBar.setValue(0);
		 base = original.clone();
		 apllyBaseProcess(base, mat);
	 }

	 @FXML
	 public void onImageClicked(MouseEvent event) {
		 Mat png = null;
		 switch (stampTypes.getValue()) {
		 case "Smile":
			 png = ImageProcessor.extend(smileStamp, mat.size(),
						(int) (( event.getX() / imageView.getFitWidth()) * mat.width() - (smileStamp.cols() / 2)),
						(int) (( event.getY() / imageView.getFitHeight()) * mat.height()- (smileStamp.rows() / 2)));
			 break;
		 case "Twinkle":
			 png = ImageProcessor.extend(twinckeStamp, mat.size(),
						(int) (( event.getX() / imageView.getFitWidth()) * mat.width() - (twinckeStamp.cols() / 2)),
						(int) (( event.getY() / imageView.getFitHeight()) * mat.height()- (twinckeStamp.rows() / 2)));
			 break;
		 case "Good":
			 png = ImageProcessor.extend(goodStamp, mat.size(),
						(int) (( event.getX() / imageView.getFitWidth()) * mat.width() - (goodStamp.cols() / 2)),
						(int) (( event.getY() / imageView.getFitHeight()) * mat.height()- (goodStamp.rows() / 2)));
			 break;
		 case "None":
		 default:
			 return;
		 }

		 Mat splitedStamp = new Mat();
		 Mat back = new Mat();
		 ImageProcessor.split(png, splitedStamp, back);
		 ImageProcessor.synthesize(mat, mat, splitedStamp, back);
		 imageView.setImage(ImageProcessor.toImage(mat));
	 }

	 @FXML
	 public void effectBarChanged(MouseEvent event) {
		 apllyBaseProcess(original, mat); /* 基本効果だけ適用した画像を再作成 */
		 double value = effectBar.getValue();

		 if( 0 < value ) {
			 String effect = comboBox.getValue();
			 System.out.println(effect);

			switch (effect) {
			case "Oil paint":
				 Imgproc.pyrMeanShiftFiltering(mat, mat, 10,30);
				break;

			case "Line drawing":
				ImageProcessor.toEdgeImage(mat, mat, true);
				break;

			case "Cartoon":
				ImageProcessor.toCartoon(mat, mat,10);
				break;

			case "Glow":
				ImageProcessor.glow(mat, mat);
				break;

			case "Glow Red":
				ImageProcessor.glow(mat, mat, 2);
				break;

			case "Glow Green":
				ImageProcessor.glow(mat, mat, 1);
				break;

			case "Glow Blue":
				ImageProcessor.glow(mat, mat, 0);
				break;

			case "Sepia":
				ImageProcessor.warm(mat,mat);
				break;

			case "None":
			default:
				break;
			}
		 }
		 base = mat.clone(); /* 以降画像効果を引き継げるようbaseにコピー */
		 imageView.setImage(ImageProcessor.toImage(mat));
	 }

	 @FXML
	 public void baseBarsChanged(MouseEvent event) {
		 apllyBaseProcess(base, mat);
	 }

	 @FXML
	 public void test(ActionEvent event) {
		 apllyBaseProcess(original, mat); /* 基本効果だけ適用した画像を再作成 */
		 Mat bmp = Imgcodecs.imread("C:\\star.bmp");
		 Mat matrix = new Mat(2,3,CvType.CV_32F);

		 for(int i  = 0; i < 10; i++ ) {
			Mat star = bmp.clone();
			Mat stars = new Mat(mat.size(),CvType.CV_32FC3);
			double size = 2.0 * Math.random();
			double angle = Math.PI * Math.random();
			matrix.put(0, 0, size * Math.cos(angle));
			matrix.put(0, 1, 0  * - Math.sin(angle));
			matrix.put(1, 0, 0  * Math.sin(angle));
			matrix.put(1, 1, size * Math.cos(angle));
			matrix.put(0, 2, Math.random() * mat.cols());
			matrix.put(1, 2, Math.random() * mat.rows());
			ImageProcessor.convert(star, star, -150 *  Math.random(), -150 * Math.random(),-150 * Math.random());
			Imgproc.GaussianBlur(star, star, new Size(9,9), 10);
			Imgproc.warpAffine(star,stars, matrix, stars.size(), Imgproc.INTER_LINEAR, Core.BORDER_TRANSPARENT, new Scalar(0,0,0,0) );
			Core.add(mat, stars, mat);
		 }
		 /* snow
		 for(int i  = 0; i < 10; i++ ) {
			matrix.put(0, 0, Math.random() + 0.5);
			matrix.put(0, 1, Math.random());
			matrix.put(1, 0, Math.random() );
			matrix.put(1, 1, Math.random() + 0.5);

			matrix.put(0, 2, Math.random() * mat.cols());
		 	matrix.put(1, 2, Math.random() * mat.rows());
		 	ImageProcessor.convert(star, star, -10, -10,-10);
		 	Imgproc.warpAffine(star,stars, matrix, stars.size(), Imgproc.INTER_LINEAR, Core.BORDER_TRANSPARENT, new Scalar(0,0,0,0) );
		 }
		 Imgproc.GaussianBlur(stars, stars, new Size(9,9), 10);
		 Core.add(mat, stars, mat);
		 */
		 imageView.setImage(ImageProcessor.toImage(mat));

		 System.out.println("test");
	 }

	 @FXML
	 public void imageSelect(ActionEvent event) {

		 File file = fileChooser.showOpenDialog(null);
         if (file != null) {
        	try {
        		/* Imgcodecs.imreadを用いて読み込むと、日本語のパスを扱えないため */
        		mat = ImageProcessor.toMat(ImageIO.read(file));
				//mat = Imgcodecs.imread(new String(file.getPath().getBytes("UTF-16"),"UTF-16"));
			} catch ( IOException e) {
				e.printStackTrace();
			}
        	original = mat.clone();
        	reset();
         }

	 }

	 @FXML
	 public void onChecked(ActionEvent event) {
		clockLabel.setVisible(checkBox.isSelected());
		colorPicker.setVisible(checkBox.isSelected());
	 }
	 @FXML
	 public void onClockColorChanged(ActionEvent event) {
		 clockLabel.setTextFill(colorPicker.getValue());
	 }


	 final ExecutorService exec = Executors.newCachedThreadPool();
	 public void launchClock() {
		 exec.submit(() -> {
        	 while(true) {
        		 Platform.runLater(() -> clockLabel.setText(LocalDateTime.now().
        				 format(DateTimeFormatter.ofPattern("MM/dd HH:mm:ss"))));
        		 try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
        	 }
		 });
	 }



	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		//clockLabel.setText(LocalDateTime.now().toString());
		launchClock();
		colorPicker.setValue(Color.BLACK);
		comboBox.getItems().addAll(
			    "None",
			    "Oil paint",
			    "Line drawing",
			    "Cartoon",
			    "Glow",
			    "Glow Red",
			    "Glow Green",
			    "Glow Blue",
			    "Sepia"
			);
		comboBox.setValue("None");

		stampTypes.getItems().addAll(
			    "None",
			    "Smile",
			    "Twinkle",
			    "Good"
			);
		stampTypes.setValue("None");
		try {
			/*
			 * jarで読み込むためurlを使用。
			 * またimreadを用いるとjarから読み込むことができない。
			 */
			URL url=  getClass().getClassLoader().getResource("good.bmp");
			goodStamp = ImageProcessor.toPng(ImageIO.read(url));

			url=  getClass().getClassLoader().getResource("kirakira.bmp");
			twinckeStamp = ImageProcessor.toPng(ImageIO.read(url));

			url=  getClass().getClassLoader().getResource("smile.png");
			smileStamp = ImageProcessor.toPng(ImageIO.read(url));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
