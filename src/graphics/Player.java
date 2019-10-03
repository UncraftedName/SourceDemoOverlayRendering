package graphics;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.HashMap;

public class Player implements Drawable {

    private String playerDemoName;
    private int diameter;
    private boolean drawPlayerName;
    private PImage img;
    public int x, y; // updated by applet


    public Player(PApplet applet, String playerDemoName, int diameter, boolean drawPlayerName) {
        this.playerDemoName = playerDemoName;
        this.diameter = diameter;
        this.drawPlayerName = drawPlayerName;
        String path = "img/icons/players/" + playerDemoName + ".png";
        img = applet.loadImage(path);
        if (img == null)
            img = applet.loadImage("img/icons/sanic.png");
    }


    @Override // scale & translation are ignored
    public void draw(PGraphics canvas, float scaleFactor, float transX, float transY) {
        canvas.pushStyle();
        canvas.imageMode(PConstants.CENTER);
        canvas.image(img, x, y, diameter, diameter);
        if (drawPlayerName) {
            canvas.textSize(20);

        }
        canvas.popStyle();
    }
}
