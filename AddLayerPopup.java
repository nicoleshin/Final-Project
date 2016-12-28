import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.input.*;
import javafx.event.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class AddLayerPopup{

    private static String newLayerName = "";

    public static String display(){
        Stage popupwindow = new Stage();
        popupwindow.initModality(Modality.APPLICATION_MODAL);
        popupwindow.setTitle("Create a new layer.");

        //layerName text field
        final TextField layerName = new TextField();
        layerName.setPromptText("Enter the layer name.");
        layerName.setPrefColumnCount(10);
        layerName.setLayoutY(50);

        //submit button for Layer name submitting
        Button submit = new Button("Make New Layer");
        submit.setLayoutY(75);

        // Warning that layer names cannot be empty or repeat
        final Text warning = new Text("Layer names cannot be blank or repeated");
        warning.setFill(Color.RED);

        // Submit closes window
        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String name = layerName.getText().trim();
                if (!name.isEmpty() && !Main.layerStrings.contains(name)) {
                    newLayerName = name;
                    popupwindow.close();
                }
            }
        });

        VBox layout= new VBox(10);
        layout.getChildren().addAll(layerName, submit, warning);
        layout.setAlignment(Pos.CENTER);
        Scene scene= new Scene(layout, 300, 250);
        popupwindow.setScene(scene);
        popupwindow.showAndWait();
        return newLayerName;
    }

}

