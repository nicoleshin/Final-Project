# MARS (Minimalistic Art Rendering System)
Our project is a drawing program that immitates software such as Paint Tool Sai
and Photoshop.

## Working Features
- Color selection
- Hue/Saturation/Brightness sliders
- Layer creation, selection, deletion, renaming, reordering
- Tool selection and controlling brush size
- Saving work as png and opening png files
- Blend modes
- Numerous shortcuts (listed below)

## Unresolved Bugs
- The different blendmodes and changing density from 100 use a different brush type. Instead of the round/circular one, we use the butt cursor shape which does not overlap. If we use the round one, whenever the left mouse button is clicked or dragged, the program will continuously change the color of the area of the cursor, overlapping the color many times.
- The eraser is the shape of a square because there is no built in function for clearing a circular area, only rectangles. Trying to paint with a transparent color also does not work.

## How to Compile and Run
Compile and run Main.java to use the program.

## How to Use the Program
### Features
##### Color Selection
This dropdown tab allows you to pick the color you'd like to draw with and allows the creation/saving of custom color.
#####  Hue/Saturation/Brightness sliders
These sliders allow you to adjust the HSV values of the current selected color. Changing the HSV silders will select a new color with the corresponding HSV values. Note that the color black cannot be affected by the hue or saturation sliders.
##### Layers
Note that when a new layer is made, it is put below all existing layers. To reorder, rename, or delete a layer, press the "Edit Layers" button.
##### Blend Modes
The different blend modes determine how the current selected color is blended into the current layer. More information on all the different blend modes can be found [here](https://docs.oracle.com/javafx/2/api/javafx/scene/effect/BlendMode.html).
##### Saving and Opening Files
You can save your current project as a png. You can open any png jpg images. It will appear in a new layer that has the name of the file's path.
### Controls
- Left-click-drag: draw with selected tool
- Right-click: switches selected tool
- Alt + Left-click: selects color underneath cursor in current layer
- Ctrl + Alt + Left-click-drag: Increases (right) and decreases (left) cursor size
- Ctrl + Left-click-drag: Undo (left) and redo (right); very fast
