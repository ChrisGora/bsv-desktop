package client;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class Main extends Application {

    private Uploader uploader;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        this.uploader = new Uploader();
        FileChooser fileChooser = new FileChooser();

        primaryStage.setTitle("Database client");

        Button singleFileButton = new Button();
        singleFileButton.setText("Upload file");
        singleFileButton.setOnAction((event) -> {
            fileChooser.setTitle("Upload file");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                UploadHolder uploadStatus = uploader.upload(file);
            }
        });

        Button multipleFilesButton = new Button();
        multipleFilesButton.setText("Upload multiple files");
        multipleFilesButton.setOnAction((event) -> {
            fileChooser.setTitle("Upload multiple files");
            List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
            if (files != null) {
                for (File file : files) {
                    if (file != null) {
                        UploadHolder uploadStatus = uploader.upload(file);
                    }
                }
            }
        });

        GridPane gridPane = new GridPane();
        GridPane.setConstraints(singleFileButton, 0, 0);
        GridPane.setConstraints(multipleFilesButton, 1, 0);

        gridPane.setHgap(6);
        gridPane.setVgap(6);
        gridPane.getChildren().addAll(singleFileButton, multipleFilesButton);

        Pane rootGroup = new VBox(12);
        rootGroup.getChildren().addAll(gridPane);
        rootGroup.setPadding(new Insets(12, 12, 12, 12));

//        StackPane root = new StackPane();
//        root.getChildren().add(singleFileButton);
//        root.getChildren().add(multipleFilesButton);

        primaryStage.setScene(new Scene(rootGroup));
        primaryStage.show();
    }
}
