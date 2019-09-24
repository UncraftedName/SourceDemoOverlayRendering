package graphics;

import processing.core.PApplet;
import processing.core.PGraphics;

public interface Drawable {

    void draw(PGraphics canvas);

    void mousePressed(PApplet applet);

    void mouseDragged(PApplet applet);
}
