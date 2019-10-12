package main;

import graphics.Drawable;
import graphics.Player;
import processing.core.PApplet;
import processing.core.PImage;
import utils.DemoToImageMapper;
import utils.HelperFuncs;
import utils.PositionManager;
import utils.SmallDemoFormat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main extends PApplet {

    private final String demoPath = "demos/cm/18"; // can be file or folder; if folder then search is recursive
    private final String imgPath = "img/levels/side/18-x-const.png";
    private final float hostFramerate = 0;
    private final boolean render = false;
    private final Player.TextSetting textSetting = Player.TextSetting.NONE;
    private final float playerDiameter = 75; // pixels
    private final String renderOutputFolder = "img/render output";

    // don't modify
    private List<Drawable> drawables = new ArrayList<>();
    private SmallDemoFormat[] smallDemoFormats;
    private PImage img;
    public DemoToImageMapper.WarpedMapper mapper;
    private String[] demoPaths;
    private long timeAtBeginDraw;


    public static void main(String[] args) {
        PApplet.main(Main.class);
    }


    public void settings() {
        BufferedImage bImg = null;
        try {
            bImg = ImageIO.read(new File(imgPath));
            DemoToImageMapper tmpMapper = new DemoToImageMapper(ImageCalibrator.offsetsFilePath, imgPath);
            tmpMapper.loadProperties();
            mapper = tmpMapper.new WarpedMapper();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-2);
        }
        if (mapper.shrinkX)
            size((int) (bImg.getWidth() * mapper.shrinkRatio), bImg.getHeight());
        else
            size(bImg.getWidth(), (int) (bImg.getHeight() * mapper.shrinkRatio));

        if (render) {
            File f = new File(renderOutputFolder);
            //noinspection ResultOfMethodCallIgnored
            f.mkdirs();
            Arrays.stream(Objects.requireNonNull(f.listFiles())).filter(file -> file.getName().endsWith(".png")).forEach(file -> {
                try {
                    Files.delete(file.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }


    public void setup() {
        if (new File(demoPath).isFile()) {
            demoPaths = new String[] {demoPath};
        } else {
            try {
                System.out.println("Getting demos...");
                demoPaths = Files.walk(Paths.get(demoPath))
                        .filter(Files::isRegularFile)
                        .map(Path::toString)
                        .filter(s -> s.endsWith(".dem"))
                        .toArray(String[]::new);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        System.out.println("Parsing demos...");
        smallDemoFormats = PositionManager.demosFromPaths(demoPaths);
        Arrays.stream(smallDemoFormats).forEach(smallDemoFormat -> drawables.add(
                new Player(this, smallDemoFormat, playerDiameter, textSetting, mapper)));
        img = loadImage(imgPath);
        if (mapper.shrinkX) // resize the image so I don't have to constantly resize it in draw
            img.resize((int) (img.width * mapper.shrinkRatio), img.height);
        else
            img.resize(img.width, (int) (img.height * mapper.shrinkRatio));
    }


    @SuppressWarnings({"divzero", "ConstantConditions"})
    public void draw() {
        if (frameCount == 1)
            timeAtBeginDraw = System.currentTimeMillis();
        clear();
        image(img, 0, 0);
        drawables.forEach(drawable -> drawable.draw(this.g, 1, 0, 0));
        int tick = (int) (hostFramerate <= 0 ?
                ((System.currentTimeMillis() - timeAtBeginDraw) / 1000f * 66)
                : (frameCount * 66f / hostFramerate));
        HelperFuncs.filterForType(drawables.stream(), Player.class).forEach(player -> player.setCoords(tick));
        if (render) {
            if (drawables.stream().filter(drawable -> drawable instanceof Player).allMatch(drawable -> ((Player)drawable).invisible))
                exit();
            saveFrame("img/render output/#####.png");
        }
    }
}
