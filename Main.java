import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.input.*;
import javafx.scene.shape.*;
import javafx.scene.image.*;
import javafx.event.*;
import javafx.beans.*;
import javafx.collections.*;
import java.util.*;

public class Main extends Application{

    private static final double WIDTH = 1000.0;
    private static final double HEIGHT = 700.0;
    private static ArrayList<Double> mouseLog;
    private static HashMap<String, Canvas> layers;
    public static ArrayList<String> layerStrings;
    private static ChoiceBox<String> layerSelector;
    private static Pane pane;
    private static final ColorPicker colorPicker = new ColorPicker();
    private static final Slider lineWidth = new Slider(0,100,15);
    private static final Slider eraserLineWidth = new Slider(0,100,15);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Basic Paint Program");
        Group root = new Group();

        // Choice selector for layers
        layerSelector = new ChoiceBox<String>();
        layerSelector.setTooltip(new Tooltip("Select a Layer"));
        layerSelector.setLayoutY(75);

        layerStrings = new ArrayList<String>();
        layers = new HashMap<String, Canvas>();


        BorderPane borderPane = new BorderPane();
        pane = new Pane();
        makeNewLayer("Layer1");
        borderPane.setCenter(pane);

        // Add Color picker
        colorPicker.setValue(Color.BLACK);

        // Setup mouse-action log
        mouseLog = new ArrayList<Double>(20);

        // Setup slider for brush size
        lineWidth.setShowTickLabels(true);
        lineWidth.setShowTickMarks(true);
        lineWidth.setMajorTickUnit(10);
        lineWidth.setMinorTickCount(5);
        lineWidth.setBlockIncrement(1);
        lineWidth.setLayoutY(150);
        final Label lineWidthLabel = new Label("Brush Width");
        lineWidthLabel.setLayoutY(125);

        // Setup for eraser size
        eraserLineWidth.setShowTickLabels(true);
        eraserLineWidth.setShowTickMarks(true);
        eraserLineWidth.setMajorTickUnit(10);
        eraserLineWidth.setMinorTickCount(5);
        eraserLineWidth.setBlockIncrement(1);
        eraserLineWidth.setLayoutY(250);
        final Label eraserLineWidthLabel = new Label("Eraser Width");
        eraserLineWidthLabel.setLayoutY(225);

        // Setup button for making new layer
        Button newLayer = new Button("Add new Layer");
        newLayer.setLayoutY(50);
        // Opens pop up prompt for new layer creation
        newLayer.setOnAction(l -> makeNewLayer(AddLayerPopup.display()));

        Button reorderLayers = new Button("Reorder Layers");
        reorderLayers.setLayoutY(350);
        reorderLayers.setOnAction(l -> setLayerStrings(ReorderLayersPopup.display()));

        // The group "root" now has previously added items in it
        root.getChildren().addAll(borderPane,
                colorPicker,
                newLayer,
                lineWidth,
                lineWidthLabel,
                eraserLineWidth,
                eraserLineWidthLabel,
                layerSelector,
                reorderLayers);
        // The stage's scene is not the grouop root
        stage.setScene(new Scene(root));
        stage.show();
    }

    //for the case of needing to clear? By smothering everything with a new thing on top?
    private void reset(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
    }

    //had to write this, since there was no 'erase-in-a-line' funcion, like there is for stroke-a-line
    private void eraseLine(GraphicsContext gc, double fromX, double fromY, double toX, double toY, double eraserSize){
        double Xincrement = (toX - fromX) / 40;
        double Yincrement = (toY - fromY) / 40;
        while (!(closeEnough(toX, fromX, 1))){
            gc.clearRect(fromX - (eraserSize / 2), fromY - (eraserSize / 2), eraserSize, eraserSize);
            fromX += Xincrement;
            fromY += Yincrement;
        }
    }

    // Helping the erase-a-line
    private boolean closeEnough(double X1, double X2, double Closeness){
        return (((X1 - X2) < (Closeness)) && ((X1 - X2) > (0.0 - Closeness)));
    }

    private void makeNewLayer(String layerName){
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        layerStrings.add(layerName);
        layers.put(layerName, canvas);

        layerSelector.getItems().add(layerName);
        layerSelector.setValue(layerName);
        pane.getChildren().add(0,canvas);
        logMouseMovement();
        logMouseDragging();
        logMouseClicking();
        //System.out.println(layerStrings);
    }

    private Canvas getCurrentLayer() {
        return layers.get(layerSelector.getValue());
    }

    private void logMouseMovement() {
        getCurrentLayer().addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent a) {
                mouseLog.add(0, a.getY());
                mouseLog.add(0, a.getX());
                if (mouseLog.size() > 10) {
                    mouseLog.remove(10);
                    mouseLog.remove(10);
                }
                //System.out.println(mouseLog.toString());
            }
        });
    }

    private void logMouseDragging() {
        getCurrentLayer().addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                GraphicsContext gc = getCurrentLayer().getGraphicsContext2D();
                //System.out.println(layerStrings.indexOf(layerSelector.getValue()));
                if (e.isPrimaryButtonDown()) {
                    gc.setLineCap(StrokeLineCap.ROUND);
                    gc.setStroke(colorPicker.getValue());
                    gc.setLineWidth(lineWidth.getValue());
                    //gc.fillRect(e.getX() - 2, e.getY() - 2, 4, 4);     //Draw a rectangle at place of left-mouse-drag
                    // Able to draw continuous lines instead of separated squares
                    gc.strokeLine(mouseLog.get(0),mouseLog.get(1),e.getX(),e.getY());
                }
                if (e.isSecondaryButtonDown()){
                    //gc.clearRect(e.getX() - 2, e.getY() - 2, 4, 4);    //Erase a rectangle at place of right-mouse-drag
                    // Able to erase in continuous lines instead of separted squares
                    eraseLine(gc, mouseLog.get(0),mouseLog.get(1),e.getX(),e.getY(),eraserLineWidth.getValue());
                }
                mouseLog.add(0, e.getY());
                mouseLog.add(0, e.getX());
                if (mouseLog.size() > 10) {
                    mouseLog.remove(10);
                    mouseLog.remove(10);
                }
                //System.out.println(mouseLog.toString());
            }
        });
    }

    private void logMouseClicking() {
        getCurrentLayer().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                GraphicsContext gc = getCurrentLayer().getGraphicsContext2D();
                if ((t.getClickCount() >1) && (t.getButton() == MouseButton.SECONDARY)) {
                    // Method "reset" clears the screen after a double-right-click
                    reset(gc);
                }
                if (t.isAltDown()) {
                    WritableImage canvasSnapshot = getCurrentLayer().snapshot(new SnapshotParameters(), new WritableImage((int)(WIDTH), (int)(HEIGHT)));
                    // Chooses color from screen
                    colorPicker.setValue(canvasSnapshot.getPixelReader().getColor((int)(t.getX()), (int)(t.getY())));
                }
                mouseLog.add(0, t.getY());
                mouseLog.add(0, t.getX());
                if (mouseLog.size() > 10) {
                    mouseLog.remove(10);
                    mouseLog.remove(10);
                }
                //System.out.println(mouseLog.toString());
            }
        });
    }

    private void setLayerStrings(ObservableList<String> n) {
        for (int i = n.size()-1; i > -1; i--) {
            layerStrings.set(i, n.get(i));
            layers.get(n.get(i)).toFront();
        }
        String selected = layerSelector.getSelectionModel().getSelectedItem();
        layerSelector.setItems(n);
        layerSelector.getSelectionModel().select(selected);
        //System.out.println(layerStrings);
    }
}
