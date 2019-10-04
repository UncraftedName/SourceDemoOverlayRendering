import graphics.Drawable;
import graphics.Player;
import processing.core.PApplet;
import processing.core.PImage;
import utils.PositionManager;
import utils.SmallDemoFormat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Main extends PApplet {

    private String[] demoPaths = new String[] {"demos/cm/16/16_1560.dem", "demos/cm/16/ch16_26_010.dem",
            "demos/cm/16/cm-16-inbounds-1583.dem", "demos/cm/16/fnzzy_16il_22,500.dem"};
    private String imgPath = "img/levels/top/chmb16.png";

    private List<Drawable> drawables = new ArrayList<>();
    private SmallDemoFormat[] smallDemoFormats;
    private PImage img;
    private PositionManager.DemoToImageMapper mapper;


    public static void main(String[] args) {
        PApplet.main(Main.class);
    }


    public void settings() {
        BufferedImage bImg = null;
        try {
            bImg = ImageIO.read(new File(imgPath));
            size(bImg.getWidth(), bImg.getHeight());
            Properties props = new Properties();
            props.loadFromXML(new FileInputStream(ImageCalibrator.offsetsFilePath));
            mapper = new PositionManager.DemoToImageMapper(
                    Float.parseFloat(props.getProperty(imgPath + " - x pixels per game units")),
                    Float.parseFloat(props.getProperty(imgPath + " - y pixels per game units")),
                    Float.parseFloat(props.getProperty(imgPath + " - game x0 on screen")),
                    Float.parseFloat(props.getProperty(imgPath + " - game y0 on screen"))
            );
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-2);
        }
        //size(1000, 1000);
    }


    public void setup() {
        smallDemoFormats = PositionManager.demosFromPaths(demoPaths, false);
        img = loadImage(imgPath);
        Arrays.stream(smallDemoFormats).forEach(smallDemoFormat -> drawables.add(
                new Player(this, smallDemoFormat, 100, false, mapper)));

    }


    public void draw() {
        clear();
        image(img, 0, 0);
        drawables.forEach(drawable -> drawable.draw(this.g, 1, 0, 0));
        drawables.stream().filter(drawable -> drawable instanceof Player).forEach(drawable -> {
            ((Player) drawable).setCoords(frameCount);
        });
    }
}
