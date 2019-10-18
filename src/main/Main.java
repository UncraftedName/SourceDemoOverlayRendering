package main;

import graphics.Drawable;
import graphics.Player;
import processing.core.PApplet;
import processing.core.PImage;
import utils.DemoToImageMapper;
import utils.helperClasses.HelperFuncs;
import utils.PositionManager;
import utils.SmallDemoFormat;
import utils.helperClasses.ImageSaverRunnable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main extends PApplet {

    private final String demoPath = "demos/cm/08"; // can be file or folder; if folder then search is recursive
    private final String imgPath = "img/levels/top/08.png";
    private final float hostFramerate = 60;
    private final boolean render = true;
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
    private ThreadPoolExecutor executor;
    private int maxLength;
    private Timer imageFileCounter; // displays how many images are processed and how many remain (only if rendering)


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
            System.out.println("deleting old files...");
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
                System.out.println("getting demos...");
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
        System.out.println("parsing demos...");
        smallDemoFormats = PositionManager.demosFromPaths(demoPaths);
        Arrays.stream(smallDemoFormats).forEach(smallDemoFormat -> drawables.add(
                new Player(this, smallDemoFormat, playerDiameter, textSetting, mapper)));
        img = loadImage(imgPath);
        if (mapper.shrinkX) // resize the image so I don't have to constantly resize it in draw
            img.resize((int) (img.width * mapper.shrinkRatio), img.height);
        else
            img.resize(img.width, (int) (img.height * mapper.shrinkRatio));

        //noinspection OptionalGetWithoutIsPresent
        maxLength = Arrays.stream(smallDemoFormats).mapToInt(demo -> demo.maxTick).max().getAsInt();
        System.out.println("max demo length in ticks: " + maxLength);

        if (render) {
            executor = new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    Integer.MAX_VALUE,
                    10, TimeUnit.SECONDS,
                    new PriorityBlockingQueue<>(10, ImageSaverRunnable.frameNumComparator));
            imageFileCounter = new Timer();
            imageFileCounter.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("frames processed: " + executor.getCompletedTaskCount() + "/" + (long)Math.ceil(maxLength / 66f * hostFramerate));
                }
            }, 500, 1500);
        }
    }


    @SuppressWarnings({"divzero", "ConstantConditions", "RedundantSuppression"})
    public void draw() {
        if (frameCount == 1)
            timeAtBeginDraw = System.currentTimeMillis();
        clear();
        image(img, 0, 0);
        drawables.forEach(drawable -> drawable.draw(this.g, 1, 0, 0));
        float tick = (hostFramerate <= 0 ?
                ((System.currentTimeMillis() - timeAtBeginDraw) / 1000f * 66)
                : (frameCount * 66f / hostFramerate));
        HelperFuncs.filterForType(drawables.stream(), Player.class).forEach(player -> player.setCoords(tick));

        if (render) {
            if (HelperFuncs.filterForType(drawables.stream(), Player.class).allMatch(player -> player.invisible)) {
                System.out.println("finished rendering, awaiting image processing...");
                noLoop(); // i don't think this actually matters
                executor.shutdown();
                try {
                    executor.awaitTermination(5, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                executor.shutdownNow();
                imageFileCounter.cancel();
                System.out.println("finished image processing, shutting down");
                exit();
            } else {
                executor.execute(new ImageSaverRunnable(renderOutputFolder, frameCount, HelperFuncs.deepImageCopy((BufferedImage)g.image)));
            }
        }
    }


    @Override
    public void dispose() {
        if (executor != null && !executor.isTerminated())
            executor.shutdownNow();
        super.dispose();
    }
}
