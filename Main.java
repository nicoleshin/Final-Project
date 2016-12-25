import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.input.*;
import javafx.event.*;
import java.util.ArrayList;

public class Main extends Application {

    private final double WIDTH = 1000.0;
    private final double HEIGHT = 700.0;
    private ArrayList<Double> Mouse_Log;
    private double lineWidth;
    private double eraserLineWidth;
    
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
	Mouse_Log = new ArrayList<Double>(20);
	lineWidth = 1.0;
	eraserLineWidth = 4.0;
        stage.setTitle("Canvas Creation Test");
        Group root = new Group();                                         //makes a group thing
        Canvas canvas = new Canvas(WIDTH,HEIGHT);                         //there is now a canvas of size WIDTH HEIGHT 
	GraphicsContext gc = canvas.getGraphicsContext2D();               //canvas has graphical abilities and such
	ChoiceBox cb = new ChoiceBox();
	final ColorPicker colorPicker = new ColorPicker();
	colorPicker.setValue(Color.BLACK);

	//logs mouse movement
	canvas.addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent a) {
		    Mouse_Log.add(0, a.getY());
		    Mouse_Log.add(0, a.getX());
		    if (Mouse_Log.size() > 10) {
			Mouse_Log.remove(10);
			Mouse_Log.remove(10);
		    }
		    System.out.println(Mouse_Log.toString());
		}
	    });

	//logs mouse dragging
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
		    if (e.isPrimaryButtonDown()) {
			gc.setStroke(colorPicker.getValue());
			gc.setLineWidth(lineWidth);
			//gc.fillRect(e.getX() - 2, e.getY() - 2, 4, 4);     //Draw a rectangle at place of left-mouse-drag
		        gc.strokeLine(Mouse_Log.get(0),Mouse_Log.get(1),e.getX(),e.getY()); //YEEEEEEEEEEEAH!!! you now draw continuous lines instead of seperated squares
		    }
		    if (e.isSecondaryButtonDown()){
			//gc.clearRect(e.getX() - 2, e.getY() - 2, 4, 4);    //Erase a rectangle at place of right-mouse-drag
			eraseLine(canvas, Mouse_Log.get(0),Mouse_Log.get(1),e.getX(),e.getY()); //Erases in continuous lines instead of seperated squares!
		    }
		    Mouse_Log.add(0, e.getY());
		    Mouse_Log.add(0, e.getX());
		    if (Mouse_Log.size() > 10) {
			Mouse_Log.remove(10);
			Mouse_Log.remove(10);
		    }
		    System.out.println(Mouse_Log.toString());
		}
	    });

	//logs mouse clicking
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent t) {
		    if ((t.getClickCount() >1) && (t.getButton() == MouseButton.SECONDARY)) {
			reset(canvas);                        //Method "reset" at place of double-right-click (see "reset" below)
		    }
		    Mouse_Log.add(0, t.getY());
		    Mouse_Log.add(0, t.getX());
		    if (Mouse_Log.size() > 10) {
			Mouse_Log.remove(10);
			Mouse_Log.remove(10);
		    }
		    System.out.println(Mouse_Log.toString());
		}
	    });

	//submit button for Layer name submitting
	Button submit = new Button("Add new Layer");
	submit.setLayoutY(50);
	//submit does stuff
	submit.setOnAction(l -> AddLayerPopup.display());
	
	root.getChildren().addAll(canvas, colorPicker, submit);        //the group "root" now has the previously created items in it
        stage.setScene(new Scene(root));                                  //the stage's scene is now the group "root" (consisting of "canvas")
        stage.show();                                                     //the stage is now shown
    }
    
    //for the case of needing to clear? By smothering everything with a new thing on top?
    private void reset(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void eraseLine(Canvas canvas, double fromX, double fromY, double toX, double toY){
	GraphicsContext gc = canvas.getGraphicsContext2D();
	double Xincrement = (toX - fromX) / 40;
	double Yincrement = (toY - fromY) / 40;
	while (!(closeEnough(toX, fromX, 1))){
		gc.clearRect(fromX - (eraserLineWidth / 2), fromY - (eraserLineWidth / 2), eraserLineWidth, eraserLineWidth);
		fromX += Xincrement;
		fromY += Yincrement;
	}	    
    }
    
    private boolean closeEnough(double X1, double X2, double Closeness){
	return (((X1 - X2) < (Closeness)) && ((X1 - X2) > (0.0 - Closeness)));
    }
	
}
