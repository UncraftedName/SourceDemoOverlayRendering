import graphics.Drawable;
import graphics.Player;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import utils.PositionManager;
import utils.SmallDemoFormat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Main extends PApplet {

    private final String demoPath = "demos/cm/08"; // can be file or folder; if folder then search is recursive
    private final String imgPath = "img/levels/top/08.png";
    private final boolean playInRealTime = true; // true -> skips over ticks as necessary; false -> ensures every tick in the demo is displayed (kinda like host_framerate)
    private final Player.TextSetting textSetting = Player.TextSetting.NONE;
    private final float playerDiameter = 75;

    // don't modify
    private List<Drawable> drawables = new ArrayList<>();
    private SmallDemoFormat[] smallDemoFormats;
    private PImage img;
    public PositionManager.DemoToImageMapper mapper;
    private String[] demoPaths;
    private long timeAtBeginDraw;
    private boolean scaleX; // if the x scale > y scale
    private float ratio; // either x scale / y scale or the opposite depending on scaleX


    public static void main(String[] args) {
        PApplet.main(Main.class);
    }


    public void settings() {
        BufferedImage bImg = null;
        try {
            bImg = ImageIO.read(new File(imgPath));
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
        scaleX = mapper.yPixels < mapper.xPixels;
        if (scaleX) {
            ratio =  Math.abs(mapper.yPixels / mapper.xPixels);
            size((int) (bImg.getWidth() * ratio), bImg.getHeight());
            mapper = new PositionManager.DemoToImageMapper(
                    mapper.xPixels * ratio, mapper.yPixels, mapper.gameX0 * ratio, mapper.gameY0);
        } else {
            ratio = Math.abs(mapper.xPixels / mapper.yPixels);
            size(bImg.getWidth(), (int) (bImg.getHeight() * ratio));
            mapper = new PositionManager.DemoToImageMapper(
                    mapper.xPixels, mapper.yPixels * ratio, mapper.gameX0, mapper.gameY0 * ratio);
        }
    }


    public void setup() {
        if (new File(demoPath).isFile()) {
            demoPaths = new String[] {demoPath};
        } else {
            try {
                demoPaths = Files.walk(Paths.get(demoPath)).filter(Files::isRegularFile).map(Path::toString).filter(s -> s.endsWith(".dem")).toArray(String[]::new);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        smallDemoFormats = PositionManager.demosFromPaths(demoPaths, false);
        Arrays.stream(smallDemoFormats).forEach(smallDemoFormat -> drawables.add(
                new Player(this, smallDemoFormat, playerDiameter, textSetting, mapper)));
        img = loadImage(imgPath);
        if (scaleX) // resize the image so I don't have to constantly resize it in draw
            img.resize((int) (img.width * ratio), img.height);
        else
            img.resize(img.width, (int) (img.height * ratio));
    }


    public void draw() {
        if (frameCount == 1)
            timeAtBeginDraw = System.currentTimeMillis();
        clear();
        image(img, 0, 0);
        drawables.forEach(drawable -> drawable.draw(this.g, 1, 0, 0));
        drawables.stream().filter(drawable -> drawable instanceof Player).forEach(drawable -> ((Player) drawable).setCoords(playInRealTime ? (int)((System.currentTimeMillis() - timeAtBeginDraw) / 1000f * 66) : frameCount));
    }
}
