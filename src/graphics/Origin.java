package graphics;

import main.ImageCalibrator;
import main.Main;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

public class Origin extends Selectable {

    private static final String imgFolder = "img/icons/";
    private static final float IMAGE_RADIUS = 75;
    private PImage img;
    public final ImageCalibrator.ViewType viewType;
    public float x, y;


    public Origin(PApplet applet, float x, float y, ImageCalibrator.ViewType viewType) {
        this.x = x;
        this.y = y;
        img = applet.loadImage(imgFolder + viewType.toString() + ".png");
        this.viewType = viewType;
    }


    @Override
    public void draw(Main canvas) {
        canvas.pushStyle();
        canvas.colorMode(PConstants.RGB, 255);
        canvas.imageMode(PConstants.CENTER);
        canvas.noStroke();
        canvas.fill(255, 0, 100, 50);
        canvas.circle(x, y, IMAGE_RADIUS * 4);
        if (isSelected) {
            canvas.fill(255, 150);
            canvas.circle(x, y, IMAGE_RADIUS);
        }
        canvas.image(img, x, y, IMAGE_RADIUS, IMAGE_RADIUS);
        canvas.popStyle();
    }


    @Override
    public void mouseReleased(PApplet applet, float scaleFactor, float transX, float transY) {}


    @Override
    public void mousePressed(PApplet applet, float scaleFactor, float transX, float transY) {
        if (!isSelected)
            super.mousePressed(applet, scaleFactor, transX, transY);
    }


    @Override
    public boolean mouseSelects(float zoomedMouseX, float zoomedMouseY) {
        return (x - zoomedMouseX) * (x - zoomedMouseX) + (y - zoomedMouseY) * (y - zoomedMouseY) < IMAGE_RADIUS * IMAGE_RADIUS / 4;
    }
}
