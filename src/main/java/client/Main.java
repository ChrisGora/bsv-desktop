package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends Application {

    private Uploader uploader;
    private Pane rootGroup;

    private GridPane progressGrid;
    private Map<String, Integer> progressGridMap;
    private int filesSubmitted = 1;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        this.uploader = new Uploader();
        FileChooser fileChooser = new FileChooser();

        primaryStage.setTitle("Database client");

        Button singleFileButton = getSingleFileButton(primaryStage, fileChooser);
        Button multipleFilesButton = getMultipleFilesButton(primaryStage, fileChooser);
        Button photoUploadButton = get360PhotoUploadButton(primaryStage, fileChooser);

        GridPane mainGrid = new GridPane();

        progressGrid = new GridPane();
        progressGrid.setVgap(10);
        progressGrid.setHgap(10);
        progressGridMap = new HashMap<>();

        // TODO: 20/07/18 Add progress grid labels: "Progress" , "S3 Upload Status", "RDS Update Status"

        ScrollPane scrollPane = new ScrollPane();
//        scrollPane.setVmin(300);
        scrollPane.setContent(progressGrid);

        mainGrid.setHgap(6);
        mainGrid.setVgap(6);
//        mainGrid.getChildren().addAll(singleFileButton, multipleFilesButton);
        mainGrid.getChildren().addAll(singleFileButton, multipleFilesButton, photoUploadButton);

        this.rootGroup = new VBox(12);
        rootGroup.getChildren().addAll(mainGrid, scrollPane);
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
            handleFile(primaryStage, file);
        });
        return singleFileButton;
    }

    private void handleFile(Stage primaryStage, File file) {
        if (file != null) {
            UploadHolder upload = uploader.newUploadHolder(file);
            if (upload != null) {
                showUploadProgress(primaryStage, upload);
                uploader.upload(upload);
            }
        }
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
                    handleFile(primaryStage, file);
                }
            }
        });
        return multipleFilesButton;
    }

    private Button get360PhotoUploadButton(Stage primaryStage, FileChooser fileChooser) {
        Button photoUploadButton = new Button();
        GridPane.setConstraints(photoUploadButton, 2, 0);

        photoUploadButton.setText("Upload 360 photos");
        photoUploadButton.setOnAction((event) -> {
            fileChooser.setTitle("Upload 360 photos");
            List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
            if (files != null) {
                for (File file : files) {
                    if (file.getPath().contains("_E.jpg")) {
                        handleFile(primaryStage, file);
                    }
                }
            }
        });
        return photoUploadButton;
    }


    private void showUploadProgress(Stage primaryStage, UploadHolder uploadStatus) {
        filesSubmitted++;

        String filenameString = uploadStatus.getFile().getName();
        progressGridMap.put(filenameString, filesSubmitted);

        ProgressBar progressBar = new ProgressBar();
        Text filename = new Text(filenameString);

        Text copyStatus = new Text("UPLOAD NOT STARTED");
        Text dbStatus = new Text("DATABASE UPDATE NOT STARTED");

        GridPane.setConstraints(progressBar, 0, filesSubmitted);
        GridPane.setConstraints(filename, 1, filesSubmitted);
        GridPane.setConstraints(copyStatus, 2, filesSubmitted);
        GridPane.setConstraints(dbStatus, 3, filesSubmitted);

//        Button cancelButton

        uploadStatus.setProgressListener((progress) -> {
            Platform.runLater(() -> {
                progressBar.setProgress(progress);
//                progressBar.setStyle("-fx-accent: yellow;");
                copyStatus.setText("UPLOAD IN PROGRESS...");
            });
        });

        uploadStatus.setCompletionListener((upload) -> {
            Platform.runLater(() -> {
                progressBar.setStyle("-fx-accent: green;");
                copyStatus.setText("UPLOAD SUCCESSFUL");
//                progressGrid.getChildren().remove(progressBar);
            });

        });

        uploadStatus.setFailureListener((error) -> {
            Platform.runLater(() -> {
                progressBar.setStyle("-fx-accent: red;");
                copyStatus.setText("UPLOAD ERROR >>> " + error);
            });
        });

        progressGrid.getChildren().addAll(progressBar, filename, copyStatus, dbStatus);
    }

}
