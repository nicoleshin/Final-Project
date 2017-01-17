import java.awt.image.RenderedImage;
import java.io.*;
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
import javafx.scene.effect.*;

public class Main extends Application {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 800;
    private static ArrayList<Double> mouseLog;
    private static ArrayList<EventType<MouseEvent>> mouseEventLog;
    public static HashMap<String, Canvas> layers;
    public static ArrayList<String> layerStrings;
    private static ChoiceBox<String> layerSelector;
    private static ListView<String> toolListDisplay;
    private static final String[] drawingTools = {"Brush","Eraser"};
    public static Pane pane;
    private static Canvas cursorCanvas;
    private static final ColorPicker colorPicker = new ColorPicker();
    private static final Slider lineWidth = new Slider(0,100,15);
    private static ChoiceBox<BlendMode> blendMode;
    public static ArrayList<Image> toUndos;
    public static ArrayList<Image> toRedos;
    public static ArrayList<Canvas> undoCanvases;
    public static ArrayList<Canvas> redoCanvases;
    private static final Slider hue = new Slider (0, 360, 60);
    private static final Slider saturation = new Slider (0, 100, 10);
    private static final Slider brightness = new Slider (0, 100, 10);
    private static final Slider opacity = new Slider (0, 100, 10);
    private String colorUpdater;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
        public void start(Stage stage) {
            stage.setTitle("Minimalistic Art Rendering System");
            Group root = new Group();

            //Undo Redo Holding Arrays
            toUndos = new ArrayList<Image>(50);
            toRedos = new ArrayList<Image>(50);
            undoCanvases = new ArrayList<Canvas>(50);
            redoCanvases = new ArrayList<Canvas>(50);

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

            // Color Creation Nodes Setup
            colorPicker.setValue(Color.BLACK);
            hue.setShowTickLabels(true);
            hue.setShowTickMarks(true);
            hue.setMajorTickUnit(60);
            hue.setMinorTickCount(30);
            hue.setBlockIncrement(1);
            hue.setValue(0.0);
            hue.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    colorUpdater = "ColorPicker";
                }
            });
            saturation.setShowTickLabels(true);
            saturation.setShowTickMarks(true);
            saturation.setMajorTickUnit(10);
            saturation.setMinorTickCount(5);
            saturation.setBlockIncrement(1);
            saturation.setValue(100.0);
            saturation.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    colorUpdater = "ColorPicker";
                }
            });
            brightness.setShowTickLabels(true);
            brightness.setShowTickMarks(true);
            brightness.setMajorTickUnit(10);
            brightness.setMinorTickCount(5);
            brightness.setBlockIncrement(1);
            brightness.setValue(100.0);
            brightness.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    colorUpdater = "ColorPicker";
                }
            });
            opacity.setShowTickLabels(true);
            opacity.setShowTickMarks(true);
            opacity.setMajorTickUnit(10);
            opacity.setMinorTickCount(5);
            opacity.setBlockIncrement(1);
            opacity.setValue(100.0);
            opacity.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    colorUpdater = "ColorPicker";
                }
            });

            // Setup mouse-action log
            mouseLog = new ArrayList<Double>(20);
            mouseEventLog = new ArrayList<EventType<MouseEvent>>(10);

            // Setup slider for brush size
            lineWidth.setShowTickLabels(true);
            lineWidth.setShowTickMarks(true);
            lineWidth.setMajorTickUnit(10);
            lineWidth.setMinorTickCount(5);
            lineWidth.setBlockIncrement(1);

            // Setup button for making new layer
            Button newLayer = new Button("Add new Layer");
            // Opens pop up prompt for new layer creation
            // Only works in java 8:
            // newLayer.setOnAction(l -> makeNewLayer(AddLayerPopup.display()));
            newLayer.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent a) {
                    String name = AddLayerPopup.display();
                    if (!name.isEmpty()) {
                        makeNewLayer(name);
                    }
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
                            // Removes cursor canvas from the pane so the cursor
                            // does not show up in the picture
                            pane.getChildren().remove(layerStrings.size());
                            pane.snapshot(null, writableImage);
                            // The cursor canvas is added back and brought to the front
                            pane.getChildren().add(cursorCanvas);
                            cursorCanvas.toFront();
                            RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                            ImageIO.write(renderedImage, "png", file);
                        } catch (IOException ex) {
                            //Compile error;
                            Logger.getLogger("Save Error").log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });

            Button buttonOpen = new Button("Open");
            Label currentFile = new Label();
            buttonOpen.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    FileChooser fileChooser = new FileChooser();

                    //Set extension filter
                    FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
                    FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
                    fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

                    //Show open file dialog
                    File file = fileChooser.showOpenDialog(null);

                    String path = file.getAbsolutePath();
                    currentFile.setText(path);

                    try {
                        InputStream inputStream = new FileInputStream(path);
                        Image image = new Image(inputStream);
                        makeNewLayer(path);
                        getCurrentLayer().getGraphicsContext2D().drawImage(image, 0.0, 0.0);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger("Save Error").log(Level.SEVERE, null, ex);
                    }
                }
            });


            // Setup list for drawing tools
            toolListDisplay = new ListView<String>();
            ObservableList<String> observableToolList = FXCollections.observableArrayList(drawingTools);
            toolListDisplay.setPrefWidth(200);
            toolListDisplay.setPrefHeight(50);
            toolListDisplay.setItems(observableToolList);
            toolListDisplay.getSelectionModel().select(0);

            //Choose a blendmode for the layer you're on
            blendMode = new ChoiceBox<BlendMode>();
            blendMode.setTooltip(new Tooltip("Select a Layer Blending Mode!"));
            blendMode.getItems().addAll(BlendMode.ADD, BlendMode.BLUE, BlendMode.COLOR_BURN, BlendMode.COLOR_DODGE, BlendMode.DARKEN, BlendMode.DIFFERENCE, BlendMode.EXCLUSION, BlendMode.GREEN, BlendMode.HARD_LIGHT, BlendMode.LIGHTEN, BlendMode.MULTIPLY, BlendMode.OVERLAY, BlendMode.RED, BlendMode.SCREEN, BlendMode.SOFT_LIGHT, BlendMode.SRC_ATOP, BlendMode.SRC_OVER);
            blendMode.setValue(BlendMode.SRC_OVER);

            // Labels
            final Label colorPickerLabel = new Label("Color Selection");
            colorPickerLabel.setPrefHeight(5);
            final Label layerSelectionLabel = new Label("Layer Selector");
            layerSelectionLabel.setPrefHeight(5);
            final Label lineWidthLabel = new Label("Current Tool Width");
            lineWidthLabel.setPrefHeight(5);
            final Label toolSelectionLabel = new Label("Tool Selection");
            toolSelectionLabel.setPrefHeight(5);
            final Label hueLabel = new Label("Hue");
            hueLabel.setPrefHeight(5);
            final Label saturationLabel = new Label("Saturation");
            saturationLabel.setPrefHeight(5);
            final Label brightnessLabel = new Label("Brightness");
            brightnessLabel.setPrefHeight(5);
            final Label opacityLabel = new Label("Density");
            opacityLabel.setPrefHeight(5);

            colorUpdater = "Sliders";

            //The group "root" now has previously added items in it
            //ADD
            leftToolbar.getChildren().addAll(
                    colorPickerLabel,colorPicker,
                    hueLabel,hue,
                    saturationLabel,saturation,
                    brightnessLabel,brightness,
                    opacityLabel,opacity,
                    layerSelectionLabel,layerSelector,
                    editLayers,
                    newLayer,
                    lineWidthLabel,lineWidth,
                    toolSelectionLabel,toolListDisplay,
                    buttonSave,
                    blendMode,
                    buttonOpen
                    );
            // leftToolbar.setOnMouseEntered(new EventHandler<MouseEvent>() {
            // 	    @Override
            // 	    public void handle(MouseEvent t) {
            // 		leftToolbar.setOpacity(1.0);
            // 	    }
            // 	});
            leftToolbar.setOnMouseMoved(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    colorUpdate();
                }
            });
            // leftToolbar.setOnMouseExited(new EventHandler<MouseEvent>() {
            // 	    @Override
            // 	    public void handle(MouseEvent t) {
            // 		leftToolbar.setOpacity(0.0);
            // 	    }
            // 	});
            root.getChildren().addAll(borderPane);
            // The stage's scene is not the group root
            stage.setScene(new Scene(root));
            stage.show();
            saveCurrent();
            logMouseEvent(MouseEvent.MOUSE_MOVED);
        }

    //for the case of needing to clear? By smothering everything with a new thing on top?
    private void reset(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
    }

    //had to write this, since there was no 'erase-in-a-line' funcion, like there is for stroke-a-line
    private void eraseLine(GraphicsContext gc, double fromX, double fromY, double toX, double toY, double eraserSize){
        double Xincrement = (toX - fromX) / 40;
        double Yincrement = (toY - fromY) / 40;
        while (!(closeEnough(toX, fromX, toY, fromY, 1))){
            gc.clearRect(fromX - (eraserSize / 2), fromY - (eraserSize / 2), eraserSize, eraserSize);
            fromX += Xincrement;
            fromY += Yincrement;
        }
    }

    // Helping the erase-a-line
    private boolean closeEnough(double X1, double X2, double Y1, double Y2, double Closeness){
        return ((((X1 - X2) < (Closeness)) && ((X1 - X2) > (0.0 - Closeness))) && (((Y1 - Y2) < (Closeness)) && ((Y1 - Y2) > (0.0 - Closeness))));
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
            System.out.println(layerStrings);
            System.out.println(pane.getChildren());
            cursorCanvas.toFront();
        }
    }

    private Canvas getCurrentLayer() {
        return layers.get(layerSelector.getValue());
    }

    private void saveCurrent(){
        toRedos.clear();
        redoCanvases.clear();
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        WritableImage currentCanvasState = getCurrentLayer().snapshot(parameters, new WritableImage(WIDTH, HEIGHT));
        toUndos.add(0, currentCanvasState);
        if (toUndos.size() > 50){
            toUndos.remove(50);
        }
        undoCanvases.add(0, getCurrentLayer());
        if (undoCanvases.size() > 50){
            undoCanvases.remove(50);
        }
        //System.out.println(toUndos.toString());
        //System.out.println(mouseEventLog.toString());
    }

    private void cursorUpdate(MouseEvent e) {
        GraphicsContext cursorGC = cursorCanvas.getGraphicsContext2D();
        double radius = lineWidth.getValue();
        reset(cursorGC);
        cursorGC.setLineCap(StrokeLineCap.ROUND);
        cursorGC.setStroke(Color.DARKGRAY);
        cursorGC.setLineWidth(1);
        if (toolListDisplay.getSelectionModel().getSelectedItem().equals("Eraser")) {
            cursorGC.strokeRect(e.getX()-radius/2, e.getY()-radius/2, radius, radius);
        } else {
            cursorGC.strokeOval(e.getX()-radius/2, e.getY()-radius/2, radius, radius);
        }
    }

    private void logMouseEventCoordinates(MouseEvent e){
        mouseLog.add(0, e.getY());
        mouseLog.add(0, e.getX());
        if (mouseLog.size() > 10) {
            mouseLog.remove(10);
            mouseLog.remove(10);
        }
    }

    private void logMouseEvent(EventType<MouseEvent> type){
        mouseEventLog.add(0, type);
        if (mouseEventLog.size() > 10) {
            mouseEventLog.remove(10);
        }
    }

    private void colorUpdate(){
        if (colorUpdater.equals("Sliders")){
            hue.setValue(colorPicker.getValue().getHue());
            saturation.setValue(colorPicker.getValue().getSaturation() * 100);
            brightness.setValue(colorPicker.getValue().getBrightness() * 100);
            opacity.setValue(colorPicker.getValue().getOpacity() * 100);
        }
        if (colorUpdater.equals("ColorPicker")){
            colorPicker.setValue(Color.hsb(hue.getValue(), saturation.getValue() / 100, brightness.getValue() / 100, opacity.getValue() / 100));
            colorUpdater = "Sliders";
        }
    }

    private void logMouseMovement() {
        cursorCanvas.addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                GraphicsContext gc = getCurrentLayer().getGraphicsContext2D();
                cursorUpdate(e);
                colorUpdate();
                if (mouseEventLog.get(0) == MouseEvent.MOUSE_CLICKED){
                    saveCurrent();
                }
                logMouseEventCoordinates(e);
                logMouseEvent(MouseEvent.MOUSE_MOVED);
                //System.out.println(mouseLog.toString());
                if (e.isControlDown() && !e.isAltDown() && (e.getX() < mouseLog.get(2))){
                    if (toUndos.size() > 0 && undoCanvases.size() > 0){
                        gc = undoCanvases.get(0).getGraphicsContext2D();
                        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
                        reset(gc);
                        gc.drawImage(toUndos.get(0),0.0,0.0);
                        toRedos.add(0, toUndos.get(0));
                        redoCanvases.add(0, undoCanvases.get(0));
                        toUndos.remove(0);
                        undoCanvases.remove(0);
                    }
                    //System.out.println(toUndos.toString());
                    //System.out.println(toRedos.toString());
                    //System.out.println(mouseEventLog.toString());
                }
                if (e.isControlDown() && !e.isAltDown() && (e.getX() > mouseLog.get(2))){
                    if (toRedos.size() > 0 && redoCanvases.size() > 0){
                        gc = redoCanvases.get(0).getGraphicsContext2D();
                        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
                        reset(gc);
                        gc.drawImage(toRedos.get(0),0.0,0.0);
                        toUndos.add(0, toRedos.get(0));
                        undoCanvases.add(0, redoCanvases.get(0));
                        toRedos.remove(0);
                        redoCanvases.remove(0);
                    }
                    //System.out.println(toUndos.toString());
                    //System.out.println(toUndos.toString());
                    //System.out.println(mouseEventLog.toString());
                }
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
                        if ((opacity.getValue() == 100.0)
                            && ((blendMode.getValue() == BlendMode.SRC_OVER)
                                || (blendMode.getValue() == BlendMode.SRC_ATOP)
                                || (blendMode.getValue() == BlendMode.RED)
                                || (blendMode.getValue() == BlendMode.BLUE)
                                || (blendMode.getValue() == BlendMode.GREEN)
                                || (blendMode.getValue() == BlendMode.LIGHTEN)
                                || (blendMode.getValue() == BlendMode.DARKEN))){
                            gc.setLineCap(StrokeLineCap.ROUND);
                        } else {
                            gc.setLineCap(StrokeLineCap.BUTT);
                        }
                        gc.setGlobalBlendMode(blendMode.getValue());
                        // Makes stroke based on selected tool
                        createStroke(gc, e);
                    }
                    if (mouseEventLog.get(0) == MouseEvent.MOUSE_MOVED){
                        saveCurrent();
                    }
                }
                if (e.isSecondaryButtonDown()){
                    gc.setGlobalBlendMode(BlendMode.SRC_OVER);
                }
                logMouseEventCoordinates(e);
                logMouseEvent(MouseEvent.MOUSE_DRAGGED);
                //System.out.println(mouseLog.toString());
            }
        });
    }

    private void createStroke(GraphicsContext gc, MouseEvent e) {
        // More tools to be added later
        double width = lineWidth.getValue();
        gc.setLineWidth(width);
        String selectedTool = toolListDisplay.getSelectionModel().getSelectedItem();
        if (selectedTool.equals("Brush")) {
            gc.setStroke(colorPicker.getValue());
            // Able to draw continuous lines instead of separated squares
            gc.strokeLine(mouseLog.get(0),mouseLog.get(1),e.getX(),e.getY());
        } else if (selectedTool.equals("Eraser")) {
            gc.setGlobalBlendMode(BlendMode.SRC_OVER);
            eraseLine(gc, mouseLog.get(0),mouseLog.get(1),e.getX(),e.getY(), width);
        } else {
            // for debugging
            System.out.println(selectedTool);
        }
    }

    private void logMouseClicking() {
        cursorCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                cursorUpdate(e);
                GraphicsContext gc = getCurrentLayer().getGraphicsContext2D();
                //if ((e.getClickCount() >1) && (e.getButton() == MouseButton.SECONDARY)) {
                //    Method "reset" clears the screen after a double-right-click
                //    reset(gc);
                //}
                if ((e.getButton() == MouseButton.SECONDARY) && !e.isControlDown() && !e.isAltDown()){
                    if (toolListDisplay.getSelectionModel().getSelectedItem().equals("Brush")){
                        toolListDisplay.getSelectionModel().select("Eraser");
                    } else if (toolListDisplay.getSelectionModel().getSelectedItem().equals("Eraser")){
                        toolListDisplay.getSelectionModel().select("Brush");
                    }
                }
                if (e.isAltDown() && !e.isControlDown()) {
                    WritableImage canvasSnapshot = pane.snapshot(new SnapshotParameters(), new WritableImage(WIDTH, HEIGHT));
                    // Chooses color from screen
                    Color newColor = canvasSnapshot.getPixelReader().getColor((int)(e.getX()), (int)(e.getY()));
                    colorPicker.setValue(newColor);
                    hue.setValue(newColor.getHue());
                    saturation.setValue(newColor.getSaturation() * 100);
                    brightness.setValue(newColor.getBrightness() * 100);
                    opacity.setValue(100);
                }
                logMouseEventCoordinates(e);
                logMouseEvent(MouseEvent.MOUSE_CLICKED);
                //System.out.println(toUndos.toString());
                //System.out.println(mouseEventLog.toString());
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
