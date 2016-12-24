import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import javafx.event.*;

public class Main extends Application {

    private final double WIDTH = 1000.0;
    private final double HEIGHT = 700.0;

    public static void main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Temporary Title");
        Group root = new Group();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        //final ColorPicker colorPicker = new ColorPicker();
        //colorPicker.setValue(Color.BLACK);

        stage.setScene(scene);
        stage.show();
    }
}
