package client;

import client.connections.StorageType;
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

    private BucketHandler bucketHandler;
    private Pane rootGroup;

    private GridPane progressGrid;
    private Map<String, Integer> progressGridMap;
    private int filesSubmitted = 1;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        this.bucketHandler = new BucketHandler("bristol-streetview-photos", StorageType.LOCAL);
        FileChooser fileChooser = new FileChooser();

        primaryStage.setTitle("Database client");

        Button singleFileButton = getSingleFileButton(0, 0, primaryStage, fileChooser);
        Button multipleFilesButton = getMultipleFilesButton(1, 0, primaryStage, fileChooser);
        Button photoUploadButton = get360PhotoUploadButton(2, 0, primaryStage, fileChooser);
        Button newRouteButton = getNewRouteButton(3, 0, primaryStage);

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
        mainGrid.getChildren().addAll(singleFileButton, multipleFilesButton, photoUploadButton, newRouteButton);

        this.rootGroup = new VBox(12);
        rootGroup.getChildren().addAll(mainGrid, scrollPane);
        rootGroup.setPadding(new Insets(12, 12, 12, 12));

        primaryStage.setScene(new Scene(rootGroup));
        primaryStage.show();
    }

    private Button getSingleFileButton(int col, int row, Stage primaryStage, FileChooser fileChooser) {
        Button singleFileButton = new Button();
        GridPane.setConstraints(singleFileButton, col, row);

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
            FileHolder upload = bucketHandler.newFileHolder(file);
            if (upload != null) {
                showUploadProgress(primaryStage, upload);
                bucketHandler.upload(upload);
            }
        }
    }

    private Button getMultipleFilesButton(int col, int row, Stage primaryStage, FileChooser fileChooser) {
        Button multipleFilesButton = new Button();
        GridPane.setConstraints(multipleFilesButton, col, row);

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

    private Button get360PhotoUploadButton(int col, int row, Stage primaryStage, FileChooser fileChooser) {
        Button photoUploadButton = new Button();
        GridPane.setConstraints(photoUploadButton, col, row);

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

    private Button getNewRouteButton(int col, int row, Stage primaryStage) {
        Button newRouteButton = new Button();
        GridPane.setConstraints(newRouteButton, col, row);

        newRouteButton.setText("Save as new route");
        newRouteButton.setOnAction((event) -> bucketHandler.saveJustUploadedAsNewRoute(2));
        return newRouteButton;
    }


    private void showUploadProgress(Stage primaryStage, FileHolder uploadStatus) {
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

        uploadStatus.setProgressListener((progress) -> Platform.runLater(() -> {
            progressBar.setProgress(progress);
            copyStatus.setText("UPLOAD IN PROGRESS...");
        }));

        uploadStatus.setUploadCompletionListener((upload) -> Platform.runLater(() -> {
            progressBar.setStyle("-fx-accent: green;");
            copyStatus.setText("UPLOAD SUCCESSFUL");
        }));

        uploadStatus.setUploadFailureListener((error) -> Platform.runLater(() -> {
            progressBar.setStyle("-fx-accent: red;");
            copyStatus.setText("UPLOAD ERROR >>> " + error);
        }));

        uploadStatus.setDbUpdateCompletionListener((uploadHolder) -> Platform.runLater(() -> {
            dbStatus.setText("DATABASE UPDATE OK");
        }));

        uploadStatus.setDbFailureListener((error) -> Platform.runLater(() -> {
            progressBar.setStyle("-fx-accent: red;");
            dbStatus.setText("DATABASE ERROR " + error);
        }));

        progressGrid.getChildren().addAll(progressBar, filename, copyStatus, dbStatus);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        bucketHandler.close();



    }
}
