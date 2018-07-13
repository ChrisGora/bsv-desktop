package client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    private Uploader uploader;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        this.uploader = new Uploader();
        primaryStage.setTitle("Database client");

        Button button = new Button();
        button.setText("Upload file");
        button.setOnAction((event) -> uploader.test());

        StackPane root = new StackPane();
        root.getChildren().add(button);

        primaryStage.setScene(new Scene(root, 300, 300));
        primaryStage.show();
    }
}
