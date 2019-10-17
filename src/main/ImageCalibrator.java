package main;

import graphics.Drawable;
import graphics.GameLocation;
import graphics.Origin;
import graphics.Selectable;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;
import utils.HelperFuncs;
import utils.DemoToImageMapper;

import java.util.ArrayList;
import java.util.Optional;

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
    private final String imgPath = "img/levels/side/10-y.png";
    // make sure to stand for the z coordinate to be more accurate
    private final String pos1 = "etpos -1965.937500 128.500000 -503.218750;setang 89.000000 -87.714020 0.00"; // this will be yellow
    private final String pos2 = "tpos 1581.656250 127.718750 1032.750000"; // this will be purple
    private static final float ZOOM_FACTOR = 1.2f;
    static final String offsetsFilePath = "img/levels/scales and offsets.xml";
    private ViewType defaultView = ViewType.Z_CONST_XX; // can choose during the calibration

    // all of these are set automatically
    private float gameX1, gameY1, gameZ1, gameX2, gameY2, gameZ2;
    private PImage img;
    private float displayOffsetX, displayOffsetY; // storing these manually cuz matrix doesn't give easy direct access to them
    private float currentZoom;
    private float screenX1, screenY1, screenX2, screenY2; // where the game locations are
    private ArrayList<Drawable> drawables = new ArrayList<>();
    private DemoToImageMapper demoToImageMapper;
    private long lastFrameTime;
    private int saveTextMillis = 0; // how many milliseconds to show the saved text for


    public static void main(String[] args) {
        PApplet.main(ImageCalibrator.class);
    }


    public void settings() {
        size(1000, 1000);
    }


    public void setup() {
        // z coordinates in the demo are stored as the bottom of the hitbox; I assume the player is standing
        // when they provide the coordinates so I can subtract 64 for the z coordinate
        gameX1 = Float.parseFloat(pos1.split(" ")[1]);
        gameY1 = Float.parseFloat(pos1.split(" ")[2]);
        gameZ1 = Float.parseFloat(pos1.split(" ")[3].split(";")[0]) - 64.0f;
        gameX2 = Float.parseFloat(pos2.split(" ")[1]);
        gameY2 = Float.parseFloat(pos2.split(" ")[2]);
        gameZ2 = Float.parseFloat(pos2.split(" ")[3].split(";")[0]) - 64.0f;
        img = loadImage(imgPath);
        currentZoom = 1;
        try {
            demoToImageMapper = new DemoToImageMapper(offsetsFilePath, imgPath);
            demoToImageMapper.loadProperties();
            screenX1 = demoToImageMapper.getScreenX(gameX1, gameY1, gameZ1);
            screenY1 = demoToImageMapper.getScreenY(gameX1, gameY1, gameZ1);
            screenX2 = demoToImageMapper.getScreenX(gameX2, gameY2, gameZ2);
            screenY2 = demoToImageMapper.getScreenY(gameX2, gameY2, gameZ2);
            defaultView = demoToImageMapper.viewType;
        } catch (NullPointerException e) { // setting defaults if the image hasn't been calibrated before
            screenX1 = screenY1 = 100;
            screenX2 = width - 100;
            screenY2 = height - 100;
        }
        drawables.add(new GameLocation(this, screenX1, screenY1, color(200, 200, 0)));
        drawables.add(new GameLocation(this, screenX2, screenY2, color(255, 0, 255)));
        new GameLocation(this, 100, 100);
        for (ViewType viewType : ViewType.values()) {
            Origin origin = new Origin(this, -5000, -5000, viewType); // coordinates are updated in draw anyway
            if (viewType == defaultView)
                origin.isSelected = origin.pIsSelected = true;
            drawables.add(origin);
        }
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
        Origin[] origins = HelperFuncs.filterForType(drawables.stream(), Origin.class).toArray(Origin[]::new);
        /*
        * ex: for when gameX = screenX & gameY = screenY,
        * the x-loc on the screen where the gameX = 0 is calculated as
        * screenX1 - gameX1 * (pixels per game unit).
        * */
        for (Origin origin : origins) {
            switch (origin.viewType) {
                case Z_CONST_XX:
                    origin.x = screenX1 - gameX1 * (screenX2 - screenX1) / (gameX2 - gameX1);
                    origin.y = screenY1 - gameY1 * (screenY2 - screenY1) / (gameY2 - gameY1);
                    break;
                case Z_CONST_XY:
                    origin.x = screenX1 - gameY1 * (screenX2 - screenX1) / (gameY2 - gameY1);
                    origin.y = screenY1 - gameX1 * (screenY2 - screenY1) / (gameX2 - gameX1);
                    break;
                case X_CONST:
                    origin.x = screenX1 - gameY1 * (screenX2 - screenX1) / (gameY2 - gameY1);
                    origin.y = screenY1 - gameZ1 * (screenY2 - screenY1) / (gameZ2 - gameZ1);
                    break;
                case Y_CONST:
                    origin.x = screenX1 - gameX1 * (screenX2 - screenX1) / (gameX2 - gameX1);
                    origin.y = screenY1 - gameZ1 * (screenY2 - screenY1) / (gameZ2 - gameZ1);
                    break;
            }
        }
        HelperFuncs.reverseStream(drawables.stream())
                .forEach(drawable -> drawable.draw(this.g, currentZoom, displayOffsetX, displayOffsetY));
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
        for (Selectable selectable : HelperFuncs.filterForType(HelperFuncs.filterWithoutType(drawables.stream(), Origin.class), Selectable.class).toArray(Selectable[]::new)) {
            if (hasSelectedItem) // ensure that there is only 1 selected item at a time
                selectable.isSelected = false;
            else if (selectable.isSelected)
                hasSelectedItem = true;
        }
        Optional<Origin> origin = HelperFuncs.filterForType(drawables.stream(), Origin.class).filter(origin1 -> !origin1.pIsSelected && origin1.isSelected).findFirst();
        if (origin.isPresent()) {
            origin.get().pIsSelected = true;
            HelperFuncs.filterForType(drawables.stream(), Origin.class).filter(origin1 -> origin1 != origin.get()).forEach(origin1 -> origin1.isSelected = false);
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
        if (key == ENTER) {
            setAndWriteProperties();
            saveTextMillis = 2000;
        }
    }


    private boolean anySelectedObject() {
        return HelperFuncs.filterForType(HelperFuncs.filterWithoutType(drawables.stream(), Origin.class), Selectable.class)
                .anyMatch(selectable -> selectable.isSelected);
    }


    private void setAndWriteProperties() {
        ViewType viewType = HelperFuncs.filterForType(drawables.stream(), Origin.class)
                .filter(origin -> origin.isSelected)
                .map(origin -> origin.viewType)
                .findFirst()
                .orElse(ViewType.Z_CONST_XX);
        final float screenX0, screenY0, xRatio, yRatio;
        switch (viewType) {
            case Z_CONST_XX:
                xRatio = (screenX2 - screenX1) / (gameX2 - gameX1);
                yRatio = (screenY2 - screenY1) / (gameY2 - gameY1);
                screenX0 = screenX1 - gameX1 * xRatio;
                screenY0 = screenY1 - gameY1 * yRatio;
                break;
            case Z_CONST_XY:
                xRatio = (screenX2 - screenX1) / (gameY2 - gameY1);
                yRatio = (screenY2 - screenY1) / (gameX2 - gameX1);
                screenX0 = screenX1 - gameY1 * xRatio;
                screenY0 = screenY1 - gameX1 * yRatio;
                break;
            case X_CONST:
                xRatio = (screenX2 - screenX1) / (gameY2 - gameY1);
                yRatio = (screenY2 - screenY1) / (gameZ2 - gameZ1);
                screenX0 = screenX1 - gameY1 * xRatio;
                screenY0 = screenY1 - gameZ1 * yRatio;
                break;
            case Y_CONST:
                xRatio = (screenX2 - screenX1) / (gameX2 - gameX1);
                yRatio = (screenY2 - screenY1) / (gameZ2 - gameZ1);
                screenX0 = screenX1 - gameX1 * xRatio;
                screenY0 = screenY1 - gameZ1 * yRatio;
                break;
            default:
                throw new IllegalArgumentException("unknown view type");
        }
        demoToImageMapper.setProperties(screenX0, screenY0, xRatio, yRatio, viewType);
        demoToImageMapper.writeProperties();
    }


    public enum ViewType {
        Z_CONST_XX, // in game x & image x are parallel
        Z_CONST_XY, // in game x & image x are perpendicular
        X_CONST,
        Y_CONST
    }
}
