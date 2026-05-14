package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.Book;
import org.example.model.Publication;
import java.time.Year;
import java.io.IOException;
import java.util.ArrayList;

public class BookGUI extends Application {
    private Catalogue catalogue = new Catalogue();
    private TableView<Publication> table = new TableView<>();
    private ObservableList<Publication> data = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Library Catalogue Management System");

        TextField searchInput = new TextField();
        searchInput.setPromptText("Enter title to search...");
        Button searchBtn = new Button("Search");
        Button resetBtn = new Button("Reset");
        HBox searchBar = new HBox(10, searchInput, searchBtn, resetBtn);
        searchBar.setPadding(new Insets(10));

        setupTable();

        // Input Fields
        Label inputLabel = new Label("Add / Update Publication Details:");
        inputLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        TextField tIn = new TextField(); tIn.setPromptText("Title");
        TextField aIn = new TextField(); aIn.setPromptText("Author");
        TextField yIn = new TextField(); yIn.setPromptText("Year");
        TextField pIn = new TextField(); pIn.setPromptText("Publisher");
        TextField gIn = new TextField(); gIn.setPromptText("Genre");

        double fieldWidth = 140;
        tIn.setPrefWidth(fieldWidth); aIn.setPrefWidth(fieldWidth); yIn.setPrefWidth(fieldWidth);
        pIn.setPrefWidth(fieldWidth); gIn.setPrefWidth(fieldWidth);

        HBox fieldsRow = new HBox(10, tIn, aIn, yIn, pIn, gIn);
        fieldsRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox inputSection = new VBox(10, inputLabel, fieldsRow);
        inputSection.setPadding(new Insets(15));
        inputSection.setStyle("-fx-background-color: #eeeeee; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection instanceof Book) {
                Book b = (Book) newSelection;
                tIn.setText(b.getTitle());
                aIn.setText(b.getAuthor());
                yIn.setText(String.valueOf(b.getYear()));
                pIn.setText(b.getPublisher());
                gIn.setText(b.getGenre());
            }
        });

        // Controls
        VBox rightPane = new VBox(10);
        rightPane.setPadding(new Insets(10));
        Button addBtn = new Button("Add Book");
        Button updateBtn = new Button("Update Book");
        Button removeBtn = new Button("Remove Book");
        Button saveBtn = new Button("Save to File");
        Button loadBtn = new Button("Load from File");

        rightPane.getChildren().addAll(addBtn, updateBtn, removeBtn, saveBtn, loadBtn);
        rightPane.getChildren().forEach(n -> ((Button)n).setMaxWidth(Double.MAX_VALUE));

        // ADD
        addBtn.setOnAction(e -> {
            try {
                String title = tIn.getText().trim();
                if (title.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Input Error", "Title cannot be empty!");
                    return;
                }

                if (catalogue.findPublicationByTitle(title) != null) {
                    showAlert(Alert.AlertType.ERROR, "Duplicate Error",
                            "A publication with the title '" + title + "' already exists!");
                    return;
                }

                int inputYear = Integer.parseInt(yIn.getText());
                int currentYear = java.time.Year.now().getValue();

                if (inputYear > currentYear) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Year", "Max year is " + currentYear);
                    return;
                }

                Book b = new Book(title, inputYear, aIn.getText(), pIn.getText(), gIn.getText());
                catalogue.addPublication(b);
                data.add(b);

                clearInputs(tIn, aIn, yIn, pIn, gIn);

            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Year must be a number!");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Something went wrong: " + ex.getMessage());
            }
        });

        // UPDATE
        updateBtn.setOnAction(e -> {
            String title = tIn.getText().trim();

            Publication old = catalogue.findPublicationByTitle(title);

            if (old != null) {
                try {
                    int inputYear = Integer.parseInt(yIn.getText());
                    int currentYear = java.time.Year.now().getValue();

                    if (inputYear > currentYear) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Year",
                                "The year cannot be in the future (max: " + currentYear + ")");
                        return;
                    }

                    catalogue.removePublicationByTitle(title);
                    Book updated = new Book(title, inputYear, aIn.getText(), pIn.getText(), gIn.getText());
                    catalogue.addPublication(updated);

                    data.setAll(catalogue.getAllPublications());
                    showAlert(Alert.AlertType.INFORMATION, "Update", "Book '" + title + "' updated successfully!");
                    clearInputs(tIn, aIn, yIn, pIn, gIn);

                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "Year must be a number!");
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Update failed: " + ex.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Update", "Book with title '" + title + "' not found.\nMake sure the Title field is filled correctly.");
            }
        });

        // REMOVE
        removeBtn.setOnAction(e -> {
            Publication selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmation");
                confirm.setHeaderText("Delete Publication");
                confirm.setContentText("Are you sure you want to delete: " + selected.getTitle() + "?");

                if (confirm.showAndWait().get() == ButtonType.OK) {
                    try {
                        catalogue.removePublicationByTitle(selected.getTitle());
                        data.remove(selected);
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
                    }
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Selection", "Select a book in the table.");
            }
        });

        // SEARCH
        searchBtn.setOnAction(e -> {
            String term = searchInput.getText().trim();
            if (!term.isEmpty()) {
                ArrayList<Publication> results = catalogue.findAllByTitle(term);

                if (!results.isEmpty()) {
                    data.setAll(results);
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Search", "No results found for: " + term);
                }
            } else {
                data.setAll(catalogue.getAllPublications());
            }
        });

        // RESET
        resetBtn.setOnAction(e -> {
            searchInput.clear();
            data.setAll(catalogue.getAllPublications());
        });

        // SAVE
        saveBtn.setOnAction(e -> {
            try {
                catalogue.saveToFile("catalogue.dat");
                showAlert(Alert.AlertType.INFORMATION, "Success", "Saved to catalogue.dat");
            } catch (IOException ex) { showAlert(Alert.AlertType.ERROR, "IO Error", ex.getMessage()); }
        });

        // LOAD
        loadBtn.setOnAction(e -> {
            try {
                catalogue.loadFromFile("catalogue.dat");
                data.setAll(catalogue.getAllPublications());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data loaded.");
            } catch (Exception ex) { showAlert(Alert.AlertType.ERROR, "Load Error", "File not found or corrupted."); }
        });

        BorderPane root = new BorderPane();
        root.setTop(searchBar); root.setCenter(table); root.setRight(rightPane); root.setBottom(inputSection);
        primaryStage.setScene(new Scene(root, 1000, 600));
        primaryStage.show();

    }

    private void setupTable() {
        TableColumn<Publication, String> tCol = new TableColumn<>("Title");
        tCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        tCol.setPrefWidth(150);

        TableColumn<Publication, Integer> yCol = new TableColumn<>("Year");
        yCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        yCol.setPrefWidth(60);

        TableColumn<Publication, String> aCol = new TableColumn<>("Author");
        aCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        aCol.setPrefWidth(150);

        TableColumn<Publication, String> pCol = new TableColumn<>("Publisher");
        pCol.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        pCol.setPrefWidth(120);

        TableColumn<Publication, String> gCol = new TableColumn<>("Genre");
        gCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        gCol.setPrefWidth(100);

        table.getColumns().setAll(tCol, yCol, aCol, pCol, gCol);
        table.setItems(data);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }

    private void clearInputs(TextField... fields) {
        for (TextField f : fields) f.clear();
    }
}