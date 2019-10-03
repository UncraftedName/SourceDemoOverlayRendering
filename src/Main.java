import graphics.Drawable;
import processing.core.PApplet;
import utils.PositionManager;
import utils.SmallDemoFormat;

import java.util.Arrays;
import java.util.List;

public class Main extends PApplet {

    private String[] demoPaths = new String[] {"demos/cm-16-inbounds-1583.dem"};
    private String imgPath = "";

    private List<Drawable> drawables;
    private SmallDemoFormat[] smallDemoFormats;


    public static void main(String[] args) {
        PApplet.main(Main.class);
    }


    public void settings() {
        size(1000, 1000);
    }


    public void setup() {
        smallDemoFormats = (SmallDemoFormat[]) Arrays.stream(demoPaths).map(demoPath -> PositionManager.demoFromPath(demoPath, false)).toArray();
    }


    public void draw() {
        clear();
        circle(50, 50, 60);
    }
}
