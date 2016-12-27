import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.input.*;
import javafx.event.*;
import javafx.collections.*;
import javafx.scene.input.*;

public class ReorderLayersPopup {

    private static ListView<String> list;
    private static ObservableList<String> names;

    public static ObservableList<String> display() {
        // Setup popup window
        Stage popupWindow = new Stage();
        popupWindow.initModality(Modality.APPLICATION_MODAL);
        popupWindow.setTitle("Edit the order of the layers.");

        Group root = new Group();

        // Setup list of layers
        list = new ListView<String>();
        names =FXCollections.observableArrayList(Main.layerStrings);
        list.setPrefWidth(200);
        list.setPrefHeight(500);
        list.setItems(names);

        Pane pane = new Pane();
        pane.getChildren().add(list);

        // Setup buttons to move layers up or down
        Button up = new Button("Move Up");
        up.setLayoutX(300);
        up.setLayoutY(200);
        up.setOnAction(l -> moveUp());
        Button down = new Button("Move Down");
        down.setLayoutX(300);
        down.setLayoutY(250);
        down.setOnAction(l -> moveDown());

        root.getChildren().addAll(pane, up, down);
        popupWindow.setScene(new Scene(root, 500,500));
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
}
