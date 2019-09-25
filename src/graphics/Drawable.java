package graphics;

import processing.core.PApplet;
import processing.core.PGraphics;

public interface Drawable {


    void draw(PGraphics canvas, float scaleFactor, float transX, float transY);

    default void mousePressed(PApplet applet, float scaleFactor, float transX, float transY) {}

    default void mouseDragged(PApplet applet, float scaleFactor, float transX, float transY) {}

    default void mouseReleased(PApplet applet, float scaleFactor, float transX, float transY) {}

    default float zoomedMouseX(PApplet applet, float scaleFactor, float transX, float transY) {
        return -transX + applet.mouseX / scaleFactor;
    }

    default float zoomedMouseY(PApplet applet, float scaleFactor, float transX, float transY) {
        return -transY + applet.mouseY / scaleFactor;
    }
}
