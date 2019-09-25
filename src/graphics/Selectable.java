package graphics;

import processing.core.PApplet;

public abstract class Selectable implements Drawable {

    public boolean isSelected;

    abstract boolean mouseSelects(float zoomedMouseX, float zoomedMouseY);


    @Override
    public void mousePressed(PApplet applet, float scaleFactor, float transX, float transY) {
        isSelected = mouseSelects(zoomedMouseX(applet, scaleFactor, transX, transY), zoomedMouseY(applet, scaleFactor, transX, transY));
    }


    @Override
    public void mouseReleased(PApplet applet, float scaleFactor, float transX, float transY) {
        isSelected = false;
    }
}
