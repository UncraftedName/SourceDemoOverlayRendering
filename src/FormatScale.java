import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix;
import processing.event.MouseEvent;

/*
(not yet implemented)

1. get screenshot of level
2. find two places in the game (which you can see in the image) which have a very well defined location and are relatively far apart and get their coords
    e.g. a corner, a texture lineup on a button, etc.
3. provide the image path and the two locations to this class
4. run the main here, and drag the two balls over the two locations in the image for which you got the coordinates,
    using the 0,0 ball as a reference to not accidentally flip the two locations.
5. when done, press enter, the two reference locations will be saved
*/
public class FormatScale extends PApplet {

    // all of these are editable
    private final String imgPath = "img/levels/top/chmb16.png";
    private final float gameX1 = 50.0f;
    private final float gameY1 = 50.0f;
    private final float gameX2 = 100.0f;
    private final float gameY2 = 100.0f;
    private final static float ZOOM_FACTOR = 1.2f;

    // all of these are set automatically
    private PImage img;
    private PMatrix matrix;
    private float screenX;
    private float screenY;
    private float matrixX, matrixY; // storing these manually cuz matrix doesn't give easy direct access to them
    private float currentZoom;


    public static void main(String[] args) {
        PApplet.main(FormatScale.class);
    }


    public void settings() {
        size(1000, 1000);
    }


    public void setup() {
        img = loadImage(imgPath);
        matrix = getMatrix();
        currentZoom = 1;
    }


    public void draw() {
        clear();
        applyMatrix(matrix);
        image(img, 0, 0);
    }


    public void mouseDragged() {
        float deltaX = (mouseX - pmouseX) / currentZoom;
        float deltaY = (mouseY - pmouseY) / currentZoom;
        matrix.translate(deltaX, deltaY);
        matrixX += deltaX;
        matrixY += deltaY;
    }


    public void mouseWheel(MouseEvent event) {
        // event.getCount == -1 for scroll up, 1 for scroll down
        if (event.getCount() != 0) {
            matrix.translate(-matrixX, -matrixY); // reset view back to 0,0
            float thisScale = (event.getCount() < 0 ? ZOOM_FACTOR : 1 / ZOOM_FACTOR) * Math.abs(event.getCount());
            float pZoom = currentZoom;
            matrix.scale(thisScale); // apply the zoomage
            currentZoom *= thisScale;
            float deltaX = -mouseX * (1 / pZoom - 1 / currentZoom); // the delta to add in order to zoom towards the mouse
            float deltaY = -mouseY * (1 / pZoom - 1 / currentZoom);
            matrix.translate(matrixX + deltaX, matrixY + deltaY); // move the view back to the original location, applying the additional delta as well
            matrixX += deltaX;
            matrixY += deltaY;
        }
    }
}
