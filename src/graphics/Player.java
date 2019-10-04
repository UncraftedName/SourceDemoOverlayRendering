package graphics;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import utils.PositionManager;
import utils.SmallDemoFormat;

public class Player implements Drawable {

    private SmallDemoFormat demo;
    private int diameter;
    private boolean drawPlayerName;
    private PImage img;
    public float x, y; // updated by applet
    private PositionManager.DemoToImageMapper mapper;


    public Player(PApplet applet, SmallDemoFormat demo, int diameter, boolean drawPlayerName, PositionManager.DemoToImageMapper mapper) {
        this.demo = demo;
        this.diameter = diameter;
        this.drawPlayerName = drawPlayerName;
        this.mapper = mapper;
        String path = "img/icons/players/" + demo.playerNameInDemo + ".png";
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


    public void setCoords(int tick) {
        SmallDemoFormat.Position pos = null;
        for (SmallDemoFormat.Position position : demo.positions) {
            if (position.tick == tick) {
                pos = position;
                break;
            }
        }
        if (pos != null) {
            double demoX = pos.locations[0][0];
            double demoY = pos.locations[0][1];
            x = (float) mapper.getImgX(demoX);
            y = (float) mapper.getImgY(demoY);
        }
    }
}
