package firstgui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class FirstGUI extends Application {
    
@Override
public void start(Stage primaryStage) {
        
primaryStage.setTitle("JavaFX GUI");
        

Button buttonmain= new Button("Click to go to pop up window");
buttonmain.setOnAction(e -> firstgui.Popup.display());

StackPane layout= new StackPane();

layout.getChildren().add(buttonmain);

        
Scene scene1= new Scene(layout, 300, 250);
primaryStage.setScene(scene1);
        
primaryStage.show();
}

  
public static void main(String[] args) {
launch(args);
}
    
}
