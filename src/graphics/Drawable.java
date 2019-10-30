package graphics;

import main.ImageCalibrator;
import main.Main;
import processing.core.PApplet;

public interface Drawable {


    default void draw(ImageCalibrator canvas, float scaleFactor, float transX, float transY) {}

    default void draw(Main canvas) {}

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
