import graphics.Drawable;
import graphics.GameLocation;
import graphics.Origin;
import graphics.Selectable;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/*
1. get screenshot of level
2. find two places in the game (which you can see in the image) which have a very well defined location and are relatively far apart and get their coords
    e.g. a corner, a texture lineup on a button, etc.
3. provide the image path and the two locations to this class'
4. run the main here, and drag the two balls over the two locations in the image for which you got the coordinates,
    using the 0,0 ball as a reference to not accidentally flip the two locations.
5. when done, press enter, the two reference locations will be saved
*/
public class ImageCalibrator extends PApplet {

    // all of these are editable
    private final String imgPath = "img/levels/top/chmb16.png";
    private final String pos1 = "setpos -691.875000 1031.750000 68.187500;"; // this will be yellow
    private final String pos2 = "setpos 1896.312500 -744.406250 803.312500;"; // this will be purple
    private static final float ZOOM_FACTOR = 1.2f;
    public static final String offsetsFilePath = "img/levels/scales and offsets.xml";

    // all of these are set automatically
    private float gameX1, gameY1, gameX2, gameY2;
    private PImage img;
    private float displayOffsetX, displayOffsetY; // storing these manually cuz matrix doesn't give easy direct access to them
    private float currentZoom;
    private float screenX1, screenY1, screenX2, screenY2; // where the game locations are
    private ArrayList<Drawable> drawables = new ArrayList<>();
    private Properties properties = new Properties();
    private long lastFrameTime;
    private int saveTextMillis = 0; // how many milliseconds to show the saved text for


    public static void main(String[] args) {
        PApplet.main(ImageCalibrator.class);
    }


    public void settings() {
        size(1000, 1000);
    }


    public void setup() {
        // as of right now, only supports a top-down view
        gameX1 = Float.parseFloat(pos1.split(" ")[1]);
        gameY1 = Float.parseFloat(pos1.split(" ")[2]);
        gameX2 = Float.parseFloat(pos2.split(" ")[1]);
        gameY2 = Float.parseFloat(pos2.split(" ")[2]);
        try {
            properties.loadFromXML(new FileInputStream(offsetsFilePath));
        } catch (IOException e) {
            properties = new Properties();
        }
        img = loadImage(imgPath);
        currentZoom = 1;
        try {
            float screenX0 = Float.parseFloat(properties.getProperty(imgPath + " - game x0 on screen"));
            float screenY0 = Float.parseFloat(properties.getProperty(imgPath + " - game y0 on screen"));
            float xRatio = Float.parseFloat(properties.getProperty(imgPath + " - x pixels per game units"));
            float yRatio = Float.parseFloat(properties.getProperty(imgPath + " - y pixels per game units"));
            screenX1 = screenX0 + gameX1 * xRatio;
            screenX2 = screenX0 + gameX2 * xRatio;
            screenY1 = screenY0 + gameY1 * yRatio;
            screenY2 = screenY0 + gameY2 * yRatio;
        } catch (NullPointerException e) {
            screenX1 = screenY1 = 100;
            screenX2 = width - 100;
            screenY2 = height - 100;
        }
        drawables.add(new GameLocation(this, screenX1, screenY1, color(200, 200, 0)));
        drawables.add(new GameLocation(this, screenX2, screenY2, color(255, 0, 255)));
        new GameLocation(this, 100, 100);
        drawables.add(new Origin(this, -5000, -5000)); // coordinates are updated in draw anyway
    }


    public void draw() {
        clear();
        scale(currentZoom);
        translate(displayOffsetX, displayOffsetY);
        image(img, 0, 0);
        screenX1 = ((GameLocation)drawables.get(0)).x;
        screenY1 = ((GameLocation)drawables.get(0)).y;
        screenX2 = ((GameLocation)drawables.get(1)).x;
        screenY2 = ((GameLocation)drawables.get(1)).y;
        ((Origin)drawables.get(2)).x = screenX1 - gameX1 * (screenX2 - screenX1) / (gameX2 - gameX1);
        ((Origin)drawables.get(2)).y = screenY1 - gameY1 * (screenY2 - screenY1) / (gameY2 - gameY1);
        drawables.forEach(drawable -> drawable.draw(this.g, currentZoom, displayOffsetX, displayOffsetY));
        if (saveTextMillis > 0) {
            translate(-displayOffsetX, -displayOffsetY);
            scale(1 / currentZoom);
            fill(255, 255, 0, min(255, saveTextMillis));
            textSize(70);
            text("saved...", 20, 70);
            saveTextMillis -= System.currentTimeMillis() - lastFrameTime;
        }
        lastFrameTime = System.currentTimeMillis();
    }


    public void mousePressed() {
        drawables.forEach(drawable -> drawable.mousePressed(this, currentZoom, displayOffsetX, displayOffsetY));
        boolean hasSelectedItem = false;
        for (Drawable drawable : drawables) { // ensure that there is only 1 selected item at a time
            if (drawable instanceof Selectable) {
                Selectable selectable = (Selectable) drawable;
                if (hasSelectedItem)
                    selectable.isSelected = false;
                else if (selectable.isSelected)
                    hasSelectedItem = true;
            }
        }
    }


    public void mouseReleased() {
        drawables.forEach(drawable -> drawable.mouseReleased(this, currentZoom, displayOffsetX, displayOffsetY));
    }


    public void mouseDragged() {
        drawables.forEach(drawable -> drawable.mouseDragged(this, currentZoom, displayOffsetX, displayOffsetY));
        if (anySelectedObject())
            return;
        displayOffsetX += (mouseX - pmouseX) / currentZoom;
        displayOffsetY += (mouseY - pmouseY) / currentZoom;
    }


    public void mouseWheel(MouseEvent event) {
        if (anySelectedObject())
            return;
        // event.getCount == -1 for scroll up, 1 for scroll down
        if (event.getCount() != 0) {
            float thisScale = (event.getCount() < 0 ? ZOOM_FACTOR : 1 / ZOOM_FACTOR) * Math.abs(event.getCount());
            float pZoom = currentZoom;
            currentZoom *= thisScale;
            displayOffsetX += -mouseX * (1 / pZoom - 1 / currentZoom); // applying the additional delta from the mouse movement
            displayOffsetY += -mouseY * (1 / pZoom - 1 / currentZoom);
        }
    }


    public void keyPressed() {
        if (key == ENTER)
            setAndWriteProperties();
        saveTextMillis = 2000;
    }


    private boolean anySelectedObject() {
        return drawables.stream().filter(drawable -> drawable instanceof Selectable).anyMatch(drawable -> ((Selectable)drawable).isSelected);
    }


    private void setAndWriteProperties() {
        float xRatio = (screenX2 - screenX1) / (gameX2 - gameX1);
        float yRatio = (screenY2 - screenY1) / (gameY2 - gameY1);
        properties.setProperty(imgPath + " - game x0 on screen", String.valueOf(screenX1 - gameX1 * xRatio));
        properties.setProperty(imgPath + " - game y0 on screen", String.valueOf(screenY1 - gameY1 * yRatio));
        properties.setProperty(imgPath + " - x pixels per game units", String.valueOf(xRatio));
        properties.setProperty(imgPath + " - y pixels per game units", String.valueOf(yRatio));
        try {
            properties.storeToXML(new FileOutputStream(offsetsFilePath), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
