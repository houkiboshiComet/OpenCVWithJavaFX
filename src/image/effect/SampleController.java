package image.effect;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

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
import javafx.stage.FileChooser.ExtensionFilter;

public class SampleController implements Initializable {


	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}

	@FXML
	private ImageView imageView, stampView;

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
	private Mat allStamp = null;
	private Mat allStampBack = null;

	private Mat goodStamp, twinckeStamp, smileStamp, heartStamp;
	private Mat starShape;

	final FileChooser fileChooser = new FileChooser();

	 private void apllyBaseProcess(Mat src, Mat dst) {
		 ImageProcessor.convert(src, dst, Rbar.getValue() - 128,  Gbar.getValue() - 128, Bbar.getValue() - 128);
		 ImageProcessor.filter(dst, dst, (int) blurBar.getValue());
		 updateImage();
	 }

	 @FXML
	 public void saveImage(ActionEvent event) {
		 fileChooser.getExtensionFilters().clear();
		 fileChooser.getExtensionFilters().addAll(
		         new ExtensionFilter("Bitmap Image", "*.bmp")
		         );
		 File file = fileChooser.showSaveDialog(null);
		 fileChooser.setInitialDirectory(file.getParentFile());
		 if(file != null) {
			 Mat saveImg = mat.clone();
			 ImageProcessor.synthesize(mat, saveImg, allStamp, allStampBack, 50);

			 /* Imgcodecs.imwriteを用いると、utf-16の日本語のパスを扱えないため */
 	 		try {
 	 			Imgcodecs.imwrite("temp.bmp",saveImg);
				Files.copy(Paths.get("temp.bmp"),file.toPath(),StandardCopyOption.REPLACE_EXISTING);
				Files.delete(Paths.get("temp.bmp"));
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		 }
	 }

	 @FXML
	 public void onReset(ActionEvent event) {
		 resetStamp();
		 reset();
	 }

	 @FXML
	 public void onResetStamp(ActionEvent event) {
		 resetStamp();
		 updateImage();
	 }

	 @FXML
	 public void stampSelected(ActionEvent event) {
		 switch (stampTypes.getValue()) {
		 case "Smile":
			 stampView.setImage(ImageProcessor.toImage(smileStamp));
			 stampView.setVisible(true);

			 break;
		 case "Twinkle":
			 stampView.setImage(ImageProcessor.toImage(twinckeStamp));
			 stampView.setVisible(true);
			 break;

		 case "Good":
			 stampView.setImage(ImageProcessor.toImage(goodStamp));
			 stampView.setVisible(true);
			 break;

		 case "Heart":
			 stampView.setImage(ImageProcessor.toImage(heartStamp));
			 stampView.setVisible(true);
			 break;

		 case "None":
		 default:
			 stampView.setVisible(false);
			 break;
		 }
	 }


	 public void resetStamp() {
		allStamp = new Mat(mat.size(),CvType.CV_8UC3, new Scalar(255,255,255));
     	allStampBack = new Mat(mat.size(),CvType.CV_8UC3, new Scalar(0,0,0));
	 }

	 private void reset() {
		 Rbar.setValue(128);
		 Bbar.setValue(128);
		 Gbar.setValue(128);
		 blurBar.setValue(0);

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

		 case "Heart":
			 png = ImageProcessor.extend(heartStamp, mat.size(),
						(int) (( event.getX() / imageView.getFitWidth()) * mat.width() - (heartStamp.cols() / 2)),
						(int) (( event.getY() / imageView.getFitHeight()) * mat.height()- (heartStamp.rows() / 2)));

			 break;
		 case "None":
		 default:
			 return;
		 }
		 Mat splitedStamp = new Mat();
		 Mat back = new Mat();
		 ImageProcessor.split(png, splitedStamp, back);
		 ImageProcessor.synthesize(allStamp, allStamp, splitedStamp, back, 0);
		 Core.add(allStampBack, back, allStampBack);

		 updateImage();
	 }

	 private void updateImage() {
		 Mat dispImage = mat.clone();
		 ImageProcessor.synthesize(mat, dispImage, allStamp, allStampBack, 50);
		 imageView.setImage(ImageProcessor.toImage(dispImage));
	 }
	 @FXML
	 public void effectBoxChanged(ActionEvent event) {
		 effectBarChanged(null);
	 }

	 @FXML
	 public void effectBarChanged(MouseEvent event) {
		 apllyBaseProcess(original, mat); /* 基本効果だけ適用した画像を再作成 */
		 double value = effectBar.getValue();

		 if( 0 < value ) {
			 String effect = comboBox.getValue();
			 System.out.println(effect);

			switch (effect) {
			case "Sketchi":
				Photo.pencilSketch(mat, mat, mat, (float) (0.7 *(50.0 + value)) , (float) (0.0007 *(50.0 + value)), (float) (0.001 * (150.0 - value)));
				break;

			case "Oil paint":
				Imgproc.pyrMeanShiftFiltering(mat, mat, (value / 4.0), (value / 2.0));
				break;

			case "Black Pencil":
				ImageProcessor.toEdgeImage(mat, mat, true,  200 - (int) value);
				break;//toEdgeColorImage

			case "Color Pencil":
				ImageProcessor.toEdgeColorImage(mat, mat,  200 - (int) value);
				break;

			case "Cartoon":
				ImageProcessor.toCartoon(mat, mat, (int) (value / 4.0));
				break;

			case "Glow":
				ImageProcessor.glow(mat, mat,(int)(value / 10.0));
				break;

			case "Sepia":
				ImageProcessor.warm(mat,mat, value / 100.0);
				break;

			case "Stars":
				ImageProcessor.addStars(mat,mat,starShape,(int) (value / 2.0));
				break;

			case "None":
			default:
				break;
			}
		 }
		 base = mat.clone(); /* 以降画像効果を引き継げるようbaseにコピー */
		 updateImage();
	 }

	 @FXML
	 public void baseBarsChanged(MouseEvent event) {
		 apllyBaseProcess(base, mat);
	 }

	 @FXML
	 public void imageSelect(ActionEvent event) {
		 fileChooser.getExtensionFilters().clear();
		 fileChooser.getExtensionFilters().addAll(
		         new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.bmp")
		         );

		 File file = fileChooser.showOpenDialog(null);
		 fileChooser.setInitialDirectory(file.getParentFile());
         if (file != null) {

        	 try {
        		/* Imgcodecs.imreadを用いて読み込むと、utf-16の日本語のパスを扱えないため */
        	 		Files.copy(file.toPath(), Paths.get("temp.bmp"),StandardCopyOption.REPLACE_EXISTING);
					mat = Imgcodecs.imread("temp.bmp",1);
					Files.delete(Paths.get("temp.bmp"));
				} catch ( IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}

        	original = mat.clone();
        	allStamp = new Mat(mat.size(),CvType.CV_8UC3, new Scalar(255,255,255));
        	allStampBack = new Mat(mat.size(),CvType.CV_8UC3, new Scalar(0,0,0));
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
			    "Sketchi",
			    "Oil paint",
			    "Black Pencil",
			    "Color Pencil",
			    "Cartoon",
			    "Glow",
			    //"Glow Red",
			    //"Glow Green",
			    //"Glow Blue",
			    "Sepia",
			    "Stars"
			);
		comboBox.setValue("None");

		stampTypes.getItems().addAll(
			    "None",
			    "Smile",
			    "Twinkle",
			    "Heart",
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

			url=  getClass().getClassLoader().getResource("star.bmp");
			starShape =  ImageProcessor.toMat(ImageIO.read(url));

			url=  getClass().getClassLoader().getResource("heart.bmp");
			heartStamp =  ImageProcessor.toPng(ImageIO.read(url));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}
