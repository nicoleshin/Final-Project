import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.input.*;
import javafx.event.*;
import javafx.collections.*;
import javafx.scene.input.*;

public class EditLayersPopup {

    private static final int WINDOW_LENGTH = 500;
    private static final int LIST_WIDTH = 200;
    private static final int MARGIN_WIDTH = 20;
    private static ListView<String> list;
    private static ObservableList<String> names;
    private static final TextField newName = new TextField();

    public static ObservableList<String> display() {
        // Setup popup window
        Stage popupWindow = new Stage();
        popupWindow.initModality(Modality.APPLICATION_MODAL);
        popupWindow.setTitle("Edit Layers");

        Group root = new Group();

        // Setup list of layers
        list = new ListView<String>();
        names =FXCollections.observableArrayList(Main.layerStrings);
        list.setPrefWidth(LIST_WIDTH);
        list.setPrefHeight(WINDOW_LENGTH);
        list.setItems(names);

        Pane pane = new Pane();
        pane.getChildren().add(list);

        // Setup buttons and labels to move layers up or down
        Button up = new Button("Move Up");
        up.setOnAction(l -> moveUp());
        Button down = new Button("Move Down");
        down.setOnAction(l -> moveDown());
        Label reorderLabel = new Label("Reorder layers");

        // Rename text field
        newName.setPromptText("New Name");
        newName.setPrefColumnCount(10);
        Label renameLabel = new Label("Enter a new name for the selected layer");

        // Setup buttons and labels to rename layers
        Button submitName = new Button("Rename");
        submitName.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                rename();
            }
       });

        VBox rightLayout = new VBox(10);
        rightLayout.getChildren().addAll(reorderLabel,
                up,
                down,
                renameLabel,
                newName,
                submitName);
        rightLayout.setAlignment(Pos.CENTER);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(pane);
        borderPane.setRight(rightLayout);
        borderPane.setMargin(rightLayout, new Insets(MARGIN_WIDTH));
        borderPane.setPrefSize(WINDOW_LENGTH, WINDOW_LENGTH);

        root.getChildren().addAll(borderPane);
        popupWindow.setScene(new Scene(root));
        popupWindow.showAndWait();
        return names;
    }

    // If the layer is not first on the list, then it will move it up
    private static void moveUp() {
        int selectedIndex = list.getSelectionModel().getSelectedIndex();
        if (selectedIndex != 0) {
            swap(selectedIndex-1, selectedIndex);
            list.getSelectionModel().select(selectedIndex-1);
        }
    }

    // If the layer is not last on the list, then it will move it down
    private static void moveDown() {
        int selectedIndex = list.getSelectionModel().getSelectedIndex();
        if (selectedIndex != Main.layerStrings.size()) {
            swap(selectedIndex, selectedIndex+1);
            list.getSelectionModel().select(selectedIndex+1);
        }
    }

    private static void swap(int index1, int index2) {
        String temp = names.get(index1);
        names.set(index1, names.get(index2));
        names.set(index2, temp);
    }

    private static void rename() {
        int selectedIndex = list.getSelectionModel().getSelectedIndex();
        String newLayerName = newName.getText();
        String oldLayerName = names.get(selectedIndex);
        names.set(selectedIndex, newLayerName);
        Main.layers.put(newLayerName, Main.layers.get(oldLayerName));
        Main.layers.remove(oldLayerName);
        // reselects the layer
        list.getSelectionModel().select(selectedIndex);
        System.out.println(Main.layerStrings);
    }
}
