import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.input.*;
import javafx.event.*;


public class AddLayerPopup{

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

        //submit does stuff
        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                popupwindow.close();
            }
        });

        VBox layout= new VBox(10);
        layout.getChildren().addAll(layerName, submit);
        layout.setAlignment(Pos.CENTER);
        Scene scene= new Scene(layout, 300, 250);
        popupwindow.setScene(scene);
        popupwindow.showAndWait();
        return layerName.getText();
    }

}

