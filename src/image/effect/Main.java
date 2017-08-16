package image.effect;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;



public class Main extends Application {

	@Override
	public void start(Stage primaryStage) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		try {
			SplitPane root = FXMLLoader.load(getClass().getResource("Sample.fxml"));
			Scene mainScene = new Scene(root);
			mainScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			primaryStage.setScene(mainScene);
			primaryStage.show();
			primaryStage.setOnCloseRequest(e -> System.exit(0));

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
