import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;
import javafx.application.Application;
import javafx.stage.*;
import javafx.geometry.*;
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
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;

public class Main extends Application{

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;
    private static ArrayList<Double> mouseLog;
    public static HashMap<String, Canvas> layers;
    public static ArrayList<String> layerStrings;
    private static ChoiceBox<String> layerSelector;
    public static Pane pane;
    private static Canvas cursorCanvas;
    private static final ColorPicker colorPicker = new ColorPicker();
    private static final Slider lineWidth = new Slider(0,100,15);
    private static final Slider eraserLineWidth = new Slider(0,100,15);
    private String tool;

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

        layerStrings = new ArrayList<String>();
        layers = new HashMap<String, Canvas>();

        VBox leftToolbar = new VBox(10);
        leftToolbar.setAlignment(Pos.TOP_LEFT);
        leftToolbar.setPrefWidth(175);
        pane = new Pane();

        // Make uninteractable layer for cursor
        // Add to layers and layerStrings, make sure no conflicts, cursor layer
        // should always be first in layerStrings
        cursorCanvas = new Canvas(WIDTH, HEIGHT);
        pane.getChildren().add(cursorCanvas);
        logMouseMovement();
        logMouseDragging();
        logMouseClicking();

        makeNewLayer("Layer1");
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(pane);
        borderPane.setLeft(leftToolbar);
        borderPane.setMargin(leftToolbar, new Insets(10));

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
        final Label lineWidthLabel = new Label("Brush Width");

        // Setup for eraser size
        eraserLineWidth.setShowTickLabels(true);
        eraserLineWidth.setShowTickMarks(true);
        eraserLineWidth.setMajorTickUnit(10);
        eraserLineWidth.setMinorTickCount(5);
        eraserLineWidth.setBlockIncrement(1);
        final Label eraserLineWidthLabel = new Label("Eraser Width");

        // Setup button for making new layer
        Button newLayer = new Button("Add new Layer");
        // Opens pop up prompt for new layer creation
        // Only works in java 8:
        // newLayer.setOnAction(l -> makeNewLayer(AddLayerPopup.display()));
        newLayer.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent a) {
                makeNewLayer(AddLayerPopup.display());
            }
        });

        // Opens pop up prompt with options to edit layers
        // When the popup is exited and selected layer is renamed, no layer will be selected
        Button editLayers = new Button("Edit Layers");
        // Only works in java 8:
        // editLayers.setOnAction(l -> setLayerStrings(EditLayersPopup.display()));
        editLayers.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent a) {
                setLayerStrings(EditLayersPopup.display());
            }
        });

        Button buttonSave = new Button("Save");
        buttonSave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                FileChooser fileChooser = new FileChooser();
                // Set extension filter
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
                fileChooser.getExtensionFilters().add(extFilter);
                //Show save file dialog
                File file = fileChooser.showSaveDialog(stage);

                if(file != null){
                    try {
                        WritableImage writableImage = new WritableImage(WIDTH, HEIGHT);
                        // Right now, only saves the current layer
                        pane.snapshot(null, writableImage);
                        RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                        ImageIO.write(renderedImage, "png", file);
                    } catch (IOException ex) {
                        //Compile error:
                        //Logger.getLogger(JavaFX_DrawOnCanvas.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        // //button to set tool to "brush"
        // Button brushButton = new Button("Brush");
        // brushButton.setLayoutY(400);
        // brushButton.setOnAction(new EventHandler<ActionEvent>() {

        // 	@Override
        // 	    public void handle(ActionEvent t) {
        // 	    tool = "brush";
        // 	}
        //     });

        // //Button to set tool to "eraser"
        // Button eraserButton = new Button("Eraser");
        // eraserButton.setLayoutY(400);
        // eraserButton.setLayoutX(50);
        // eraserButton.setOnAction(new EventHandler<ActionEvent>() {

        // 	@Override
        // 	    public void handle(ActionEvent t) {
        // 	    tool = "eraser";
        // 	}
        //     });

        //The group "root" now has previously added items in it
        //ADD
        leftToolbar.getChildren().addAll(
                colorPicker,
                layerSelector,
                editLayers,
                newLayer,
                lineWidthLabel,
                lineWidth,
                eraserLineWidthLabel,
                eraserLineWidth,
                buttonSave
        );
                // brushButton,
                // eraserButton
        root.getChildren().addAll(borderPane);
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
        if (!layerName.isEmpty()) {
            Canvas canvas = new Canvas(WIDTH, HEIGHT);
            layerStrings.add(layerName);
            layers.put(layerName, canvas);

            layerSelector.getItems().add(layerName);
            // set selected value
            layerSelector.setValue(layerName);
            pane.getChildren().add(0,canvas);
            logMouseMovement();
            logMouseDragging();
            logMouseClicking();
            System.out.println(layerStrings);
            System.out.println(pane.getChildren());

            cursorCanvas.toFront();
        }
    }

    private Canvas getCurrentLayer() {
        return layers.get(layerSelector.getValue());
    }

    private void cursorUpdate(MouseEvent e) {
        GraphicsContext cursorGC = cursorCanvas.getGraphicsContext2D();
        double radius = lineWidth.getValue();
        reset(cursorGC);
        cursorGC.setLineCap(StrokeLineCap.ROUND);
        cursorGC.setStroke(Color.DARKGRAY);
        //cursorGC.setLineWidth(lineWidth.getValue());
        cursorGC.setLineWidth(1);
        cursorGC.strokeOval(e.getX()-radius/2, e.getY()-radius/2, radius, radius);
    }

    private void logMouseMovement() {
        cursorCanvas.addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                cursorUpdate(e);
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

    private void logMouseDragging() {
        cursorCanvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                cursorUpdate(e);
                GraphicsContext gc = getCurrentLayer().getGraphicsContext2D();
                if (e.isAltDown() && e.isControlDown()){
                    lineWidth.setValue(lineWidth.getValue() + (e.getX() - mouseLog.get(0)));
                } else {
                    //System.out.println(layerStrings.indexOf(layerSelector.getValue()));
                    if (e.isPrimaryButtonDown()) {
                        gc.setLineCap(StrokeLineCap.ROUND);
                        gc.setStroke(colorPicker.getValue());
                        gc.setLineWidth(lineWidth.getValue());
                        // Able to draw continuous lines instead of separated squares
                        gc.strokeLine(mouseLog.get(0),mouseLog.get(1),e.getX(),e.getY());
                    }
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
        cursorCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                cursorUpdate(e);
                GraphicsContext gc = getCurrentLayer().getGraphicsContext2D();
                if ((e.getClickCount() >1) && (e.getButton() == MouseButton.SECONDARY)) {
                    // Method "reset" clears the screen after a double-right-click
                    reset(gc);
                }
                if (e.isAltDown()) {
                    WritableImage canvasSnapshot = getCurrentLayer().snapshot(new SnapshotParameters(), new WritableImage(WIDTH, HEIGHT));
                    // Chooses color from screen
                    colorPicker.setValue(canvasSnapshot.getPixelReader().getColor((int)(e.getX()), (int)(e.getY())));
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

    private void setLayerStrings(ObservableList<String> n) {
        layerStrings.clear();
        for (int i = n.size()-1; i > -1; i--) {
            layerStrings.add(n.get(n.size()-i-1));
            layers.get(n.get(i)).toFront();
        }
        String selected = layerSelector.getSelectionModel().getSelectedItem();
        int selectedIndex = layerSelector.getSelectionModel().getSelectedIndex();
        layerSelector.setItems(n);
        if (n.contains(selected)) {
            layerSelector.getSelectionModel().select(selected);
        } else if (selectedIndex < n.size()) {
            layerSelector.getSelectionModel().select(selectedIndex);
        } else {
            layerSelector.getSelectionModel().select(0);
        }
        cursorCanvas.toFront();
    }
}
