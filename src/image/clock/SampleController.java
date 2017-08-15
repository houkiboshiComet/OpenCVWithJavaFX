package image.clock;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

public class SampleController implements Initializable {


	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	@FXML
	private ImageView imageView;

	@FXML
	private Label clockLabel;

	@FXML
	private CheckBox checkBox;

	@FXML
	private Slider Rbar, Gbar, Bbar, blurBar;

	Mat mat = new Mat();
	Mat original = new Mat();
	Mat base = new Mat();

	final FileChooser fileChooser = new FileChooser();
	//final LocalDateTime dateTime = new LocalD;

	 private void apllyBaseProcess(Mat src, Mat dst) {
		 ImageProcessor.convert(src, dst, Rbar.getValue() - 128,  Gbar.getValue() - 128, Bbar.getValue() - 128);
		 ImageProcessor.filter(dst, dst, (int) blurBar.getValue());
	  	 imageView.setImage(ImageProcessor.toImage(mat));
	 }

	 @FXML
	 public void baseBarsChanged(MouseEvent event) {
		 apllyBaseProcess(base, mat);
	 }

	 @FXML
	 public void test(ActionEvent event) {
		 apllyBaseProcess(original, mat); /* 基本効果だけ適用した画像を再作成 */
		 Imgproc.pyrMeanShiftFiltering(mat, mat, 10,30);
		 base = mat.clone(); /* 以降画像効果を引き継げるようbaseにコピー */
		 imageView.setImage(ImageProcessor.toImage(mat));
		 System.out.println("test");
	 }

	 @FXML
	 public void imageSelect(ActionEvent event) {

		 File file = fileChooser.showOpenDialog(null);
         if (file != null) {
        	mat = Imgcodecs.imread(file.getAbsolutePath());
        	original = mat.clone();
        	base = mat.clone();
        	imageView.setImage(ImageProcessor.toImage(mat));
         }

	 }

	 @FXML
	 public void onChecked(ActionEvent event) {
		clockLabel.setVisible(checkBox.isSelected());
	 }

	 final ExecutorService exec = Executors.newCachedThreadPool();
	 public void launchClock() {
		 exec.submit(() -> {
        	 while(true) {
        		 Platform.runLater(() -> clockLabel.setText(LocalDateTime.now().
        				 format(DateTimeFormatter.ofPattern("MM/dd HH:mm:ss"))));
        		 try {
 					Thread.sleep(1000);
 					System.out.println("thread");
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
        	 }
		 });
		 /* java fx cannot use naitive thread
		 new Thread( () -> {
        	 while(true) {
        		 try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        		clockLabel.setText(LocalDateTime.now().toString());
        	 }
         } ).run();*/
	 }


	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		//clockLabel.setText(LocalDateTime.now().toString());
		launchClock();
	}


}