package client;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
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
    private Pane rootGroup;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        this.uploader = new Uploader();
        FileChooser fileChooser = new FileChooser();

        primaryStage.setTitle("Database client");

        Button singleFileButton = getSingleFileButton(primaryStage, fileChooser);
        Button multipleFilesButton = getMultipleFilesButton(primaryStage, fileChooser);

        GridPane gridPane = new GridPane();

        gridPane.setHgap(6);
        gridPane.setVgap(6);
//        gridPane.getChildren().addAll(singleFileButton, multipleFilesButton);
        gridPane.getChildren().addAll(singleFileButton, multipleFilesButton);

        this.rootGroup = new VBox(12);
        rootGroup.getChildren().addAll(gridPane);
        rootGroup.setPadding(new Insets(12, 12, 12, 12));

        primaryStage.setScene(new Scene(rootGroup));
        primaryStage.show();
    }

    private Button getSingleFileButton(Stage primaryStage, FileChooser fileChooser) {
        Button singleFileButton = new Button();
        GridPane.setConstraints(singleFileButton, 0, 0);

        singleFileButton.setText("Upload file");
        singleFileButton.setOnAction((event) -> {
            fileChooser.setTitle("Upload file");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                UploadHolder uploadStatus = uploader.upload(file);
                addProgressBar(primaryStage, uploadStatus);
            }
        });
        return singleFileButton;
    }

    private Button getMultipleFilesButton(Stage primaryStage, FileChooser fileChooser) {
        Button multipleFilesButton = new Button();
        GridPane.setConstraints(multipleFilesButton, 1, 0);

        multipleFilesButton.setText("Upload multiple files");
        multipleFilesButton.setOnAction((event) -> {
            fileChooser.setTitle("Upload multiple files");
            List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
            if (files != null) {
                for (File file : files) {
                    if (file != null) {
                        UploadHolder uploadStatus = uploader.upload(file);
                        addProgressBar(primaryStage, uploadStatus);
                    }
                }
            }
        });
        return multipleFilesButton;
    }


    private void addProgressBar(Stage primaryStage, UploadHolder uploadStatus) {
        ProgressBar progressBar = new ProgressBar();
        uploadStatus.setProgressListener(progressBar::setProgress);
        rootGroup.getChildren().add(progressBar);
        uploadStatus.setCompletionListener((upload) -> rootGroup.getChildren().remove(progressBar));
    }

}
