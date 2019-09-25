package graphics;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

public class GameLocation extends Selectable {

    private static final float IMAGE_RADIUS = 50;
    private static final String IMG_PATH = "img/icons/crosshair.png";
    public float x, y;
    private float xOffset, yOffset; // used so that object center doesn't teleport to mouse position
    private PImage img;


    public GameLocation(PApplet applet, float x, float y) {
        this.x = x;
        this.y = y;
        img = applet.loadImage(IMG_PATH);
    }


    @Override
    public boolean mouseSelects(float zoomedMouseX, float zoomedMouseY) {
        return (zoomedMouseX - x) * (zoomedMouseX - x) + (zoomedMouseY - y) * (zoomedMouseY - y) < IMAGE_RADIUS * IMAGE_RADIUS / 4;
    }


    @Override
    public void mousePressed(PApplet applet, float scaleFactor, float transX, float transY) {
        super.mousePressed(applet, scaleFactor, transX, transY);
        xOffset = x - zoomedMouseX(applet, scaleFactor, transX, transY);
        yOffset = y - zoomedMouseY(applet, scaleFactor, transX, transY);
    }

    @Override
    public void draw(PGraphics canvas, float scaleFactor, float transX, float transY) {
        canvas.pushStyle();
        canvas.colorMode(PConstants.RGB, 255);
        canvas.imageMode(PConstants.CENTER);
        canvas.noStroke();
        canvas.fill(0, 100, 255, 65);
        canvas.circle(x, y, IMAGE_RADIUS * 5);
        canvas.fill(255, isSelected ? 150 : 100);
        canvas.circle(x, y, IMAGE_RADIUS);
        canvas.image(img, x, y, IMAGE_RADIUS, IMAGE_RADIUS);
        canvas.popStyle();
    }

    @Override
    public void mouseDragged(PApplet applet, float scaleFactor, float transX, float transY) {
        if (isSelected) {
            x = zoomedMouseX(applet, scaleFactor, transX, transY) + xOffset;
            y = zoomedMouseY(applet, scaleFactor, transX, transY) + yOffset;
        }
    }
}
