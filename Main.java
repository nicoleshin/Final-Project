import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.input.*;
import javafx.event.*;

public class Main extends Application {

    private final double WIDTH = 1000.0;
    private final double HEIGHT = 700.0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Canvas Creation Test");
        Group root = new Group();                                         //makes a group thing
        Canvas canvas = new Canvas(WIDTH,HEIGHT);                         //there is now a canvas of size WIDTH HEIGHT 
	GraphicsContext gc = canvas.getGraphicsContext2D();               //canvas has graphical abilities and such
	ChoiceBox cb = new ChoiceBox();
	final ColorPicker colorPicker = new ColorPicker();
	colorPicker.setValue(Color.BLACK);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (e.isPrimaryButtonDown()) {
		    gc.setFill(colorPicker.getValue());
		    gc.setStroke(colorPicker.getValue());
                    gc.fillRect(e.getX() - 2, e.getY() - 2, 4, 4);     //Draw a rectangle at place of left-mouse-drag
                }
                if (e.isSecondaryButtonDown()){
                    gc.clearRect(e.getX() - 2, e.getY() - 2, 4, 4);    //Erase a rectangle at place of right-mouse-drag
                }
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if ((t.getClickCount() >1) && (t.getButton() == MouseButton.SECONDARY)) {
                    reset(canvas, Color.WHITE);                        //Method "reset" at place of double-right-click (see "reset" below)
                }
            }
        });

	root.getChildren().addAll(canvas, colorPicker);                                   //the group "root" now has the previously created canvas in it
        stage.setScene(new Scene(root));                                  //the stage's scene is now the group "root" (consisting of "canvas")
        stage.show();                                                     //the stage is now shown
    }

    //for the case of needing to clear? By smothering everything with a new thing on top?
    private void reset(Canvas canvas, Color color) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(color);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    
}
