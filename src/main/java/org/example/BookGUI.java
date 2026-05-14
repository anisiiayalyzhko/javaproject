package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.Publication;

public class BookGUI extends Application {
    private Catalogue catalogue = new Catalogue();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Library Catalogue Management System");

        BorderPane root = new BorderPane();

        TableView<Publication> table = new TableView<>();
        table.setPlaceholder(new Label("The catalogue is currently empty"));

        VBox rightPane = new VBox(10);
        rightPane.setStyle("-fx-padding: 10;");

        Button addButton = new Button("Add Publication");
        Button removeButton = new Button("Remove Publication");
        Button saveButton = new Button("Save to File");
        Button loadButton = new Button("Load from File");

        addButton.setMaxWidth(Double.MAX_VALUE);
        removeButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setMaxWidth(Double.MAX_VALUE);
        loadButton.setMaxWidth(Double.MAX_VALUE);

        rightPane.getChildren().addAll(addButton, removeButton, saveButton, loadButton);

        root.setCenter(table);
        root.setRight(rightPane);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}