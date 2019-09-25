package graphics;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

public class Origin implements Drawable {

    private static final String imgPath = "img/icons/origin.png";
    private static final float IMAGE_RADIUS = 75;
    private PImage img;
    public float x, y;


    public Origin(PApplet applet, float x, float y) {
        this.x = x;
        this.y = y;
        img = applet.loadImage(imgPath);
    }

    @Override
    public void draw(PGraphics canvas, float scaleFactor, float transX, float transY) {
        canvas.pushStyle();canvas.colorMode(PConstants.RGB, 255);
        canvas.imageMode(PConstants.CENTER);
        canvas.noStroke();
        canvas.fill(255, 0, 100, 50);
        canvas.circle(x, y, IMAGE_RADIUS * 4);
        canvas.image(img, x, y, IMAGE_RADIUS, IMAGE_RADIUS);
        canvas.popStyle();
    }
}
