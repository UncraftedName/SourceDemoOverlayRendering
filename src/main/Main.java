package main;

import graphics.DraggableSelection;
import graphics.Drawable;
import graphics.Player;
import graphics.PlaybackSpeedArrow;
import processing.core.PApplet;
import processing.core.PImage;
import utils.DemoToImageMapper;
import utils.helperClasses.FFMPEGWriter;
import utils.helperClasses.threading.BlockingThreadPoolExecutor;
import utils.helperClasses.HelperFuncs;
import utils.PositionManager;
import utils.Demo;
import utils.helperClasses.threading.ImageSaver;

import javax.imageio.ImageIO;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("WeakerAccess")
public class Main extends PApplet {

    public static final String demoPath = "demos/cm/e02"; // can be file or folder; if folder then search is recursive
    public static final String imgPath = "img/levels/side/e02-y.png";
    public static final float hostFramerate = 0; // if you want to convert to gif use multiples of 100 (100,50,25,etc.)
    public static final int tickrate = 66; // specify tickrate for each game / p2 uses 60 - p1 uses 66
    public static final boolean render = false; // if set to true, change hostFramerate value other to something other than 0
    public static final Player.TextSetting textSetting = Player.TextSetting.PLAYER_NAME_IF_NO_AVATAR;
    public static final Player.InterpType interpType = Player.InterpType.LINEAR_THRESHOLD;
    public static final float playerDiameter = 60; // pixels
    public static final String renderOutputFolder = "img/render output";
    public static final boolean createFFMPEGBatch = true; // will create a basic ffmpeg file in the render output directory to convert the images to a video
    public static boolean drawArrow = false;
    public float hostTimeScale = 1;
    public float startTick = 0f;
    public float endTick = Float.MAX_VALUE; // only applies when rendering, will stop when this tick has been exceeded

    // don't modify
    public List<Drawable> drawables = new ArrayList<>();
    public Demo[] demos;
    public PImage img;
    public DemoToImageMapper.ScaledMapper mapper;
    public String[] demoPaths;
    public long timeAtListKeyFrame;
    public float tickAtLastKeyFrame = 0;
    public float currentTick = startTick;
    public boolean paused = false;
    public BlockingThreadPoolExecutor executor;
    public int maxLength;
    public boolean stopNextFrame = false; // ensures the last frame w/o anybody in it is drawn
    public boolean setNextFrameAsKeyFrame = true;
    public Timer timer; // prints stats while rendering/deleting
    public TimerTask renderTimerTask = new TimerTask() {
        @Override
        public void run() {
            System.out.println(String.format("frames processed: %d/%d, queue size: %d/%d, threads active: %d/%d",
                    ImageSaver.framesFinishedInDir.get(renderOutputFolder).get(), (long)Math.ceil((Math.min(maxLength, endTick) - startTick) / tickrate * hostFramerate),
                    (BlockingThreadPoolExecutor.queueCapacity - executor.getQueue().remainingCapacity()), BlockingThreadPoolExecutor.queueCapacity,
                    executor.getActiveCount(), executor.getMaximumPoolSize()));
        }
    };


    public static void main(String[] args) {
        if (render && hostFramerate <= 0) {
            System.out.println("host framerate should be positive value");
            System.exit(1);
        }
        PApplet.main(Main.class);
    }


    @Override
    public void settings() {
        BufferedImage bImg = null;
        try {
            bImg = ImageIO.read(new File(imgPath));
            DemoToImageMapper tmpMapper = new DemoToImageMapper(ImageCalibrator.offsetsFilePath, imgPath);
            tmpMapper.loadProperties();
            mapper = tmpMapper.new ScaledMapper();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-2);
        }
        int sizeX = mapper.shrinkX ? (int) (bImg.getWidth() * mapper.shrinkRatio) : bImg.getWidth();
        int sizeY = mapper.shrinkX ? bImg.getHeight() : (int) (bImg.getHeight() * mapper.shrinkRatio);
        if (render)
            size(sizeX, sizeY);
        else
            size(sizeX, sizeY, P2D); // P2D seems to be much more reliable for key presses, but less pretty

        if (render) {
            File f = new File(renderOutputFolder);
            //noinspection ResultOfMethodCallIgnored
            f.mkdirs();
            System.out.println("deleting old files...");
            final File[] filesToDelete = Arrays.stream(Objects.requireNonNull(f.listFiles()))
                    .filter(file -> file.getName().endsWith(".png"))
                    .toArray(File[]::new);
            final AtomicLong deletedCount = new AtomicLong();
            timer = new Timer(true);
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    System.out.println(String.format("deleted %d/%d files", deletedCount.get(), filesToDelete.length));
                }
            };
            timer.scheduleAtFixedRate(timerTask, 0, 1000);
            Arrays.stream(filesToDelete).forEach(file -> {
                try {
                    Files.delete(file.toPath());
                    deletedCount.incrementAndGet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            timer.cancel();
            timerTask.run();
        }
    }


    @Override
    public void setup() {
        if (new File(demoPath).isFile()) {
            demoPaths = new String[] {demoPath};
        } else {
            try {
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
        demos = PositionManager.demosFromPaths(demoPaths);
        Arrays.stream(demos).forEach(demo -> drawables.add(
                new Player(this, demo, playerDiameter, textSetting, interpType, mapper)));
        if (!render) {
            drawables.add(new PlaybackSpeedArrow(this));
            drawables.add(new DraggableSelection(this)); // must be added after players
        }
        img = loadImage(imgPath);
        if (mapper.shrinkX) // resize the image so I don't have to constantly resize it in draw
            img.resize((int) (img.width * mapper.shrinkRatio), img.height);
        else
            img.resize(img.width, (int) (img.height * mapper.shrinkRatio));

        maxLength = Arrays.stream(demos).mapToInt(demo -> demo.maxTick).max().orElse(1);
        System.out.println("max demo length in ticks: " + maxLength);

        if (render) {
            executor = new BlockingThreadPoolExecutor();
            timer = new Timer(true);
            timer.scheduleAtFixedRate(renderTimerTask, 500, 1000);
            executor.prestartAllCoreThreads();
            if (createFFMPEGBatch)
                FFMPEGWriter.writeBatch(renderOutputFolder, hostFramerate);
            System.out.println("rendering enabled");
            System.out.println("images will be " + width + "x" + height);
        } else {
            System.out.println("rendering disabled");
        }
    }


    @SuppressWarnings({"divzero", "ConstantConditions", "RedundantSuppression"})
    @Override
    public void draw() {
        if (setNextFrameAsKeyFrame) {
            timeAtListKeyFrame = System.currentTimeMillis();
            tickAtLastKeyFrame = currentTick;
            setNextFrameAsKeyFrame = false;
        }
        image(img, 0, 0);
        if (!paused) {
            currentTick = (hostFramerate <= 0 ?
                    tickAtLastKeyFrame + ((System.currentTimeMillis() - timeAtListKeyFrame) / 1000f * tickrate * hostTimeScale)
                    : (frameCount * tickrate / hostFramerate + startTick));
        }

        drawables.forEach(drawable -> drawable.draw(this));

        if (render) {
            if (stopNextFrame) {
                System.out.println("finished rendering, awaiting image processing...");
                executor.shutdown();
                try {
                    executor.awaitTermination(5, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                executor.shutdownNow();
                timer.cancel();
                renderTimerTask.run(); // just for the ocd :p
                exit();
            } else {
                // add a new image to the queue
                executor.execute(new ImageSaver(renderOutputFolder, frameCount, HelperFuncs.deepImageCopy((BufferedImage)g.image)));
                // if all players are invisible, the next frame will not be rendering (image is only displayed when draw is finished)
                stopNextFrame = HelperFuncs.filterForType(drawables.stream(), Player.class).allMatch(player -> player.invisible)
                        || currentTick > endTick;
            }
        }
    }


    @Override
    public void keyPressed() {
        //noinspection ConstantConditions
        if (hostFramerate == 0) {
            switch (keyCode) {
                case KeyEvent.VK_SPACE:
                case KeyEvent.VK_NUMPAD5:
                    paused ^= true;
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_KP_RIGHT:
                case KeyEvent.VK_D:
                    if (Math.abs(hostTimeScale) < 10000f)
                        hostTimeScale *= 1.2f;
                    break;
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_KP_LEFT:
                case KeyEvent.VK_A:
                    if (Math.abs(hostTimeScale) > 0.01f)
                        hostTimeScale /= 1.2f;
                    break;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_KP_UP:
                case KeyEvent.VK_W:
                    hostTimeScale = Math.abs(hostTimeScale);
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_KP_DOWN:
                case KeyEvent.VK_S:
                    hostTimeScale = -Math.abs(hostTimeScale);
                    break;
            }
            setNextFrameAsKeyFrame = true;
        }
    }


    @Override
    public void mousePressed() {
        drawables.forEach(drawable -> drawable.mousePressed(this, 1, 0, 0));
    }


    @Override
    public void mouseReleased() {
        drawables.forEach(drawable -> drawable.mouseReleased(this, 1, 0, 0));
    }


    @Override
    public void mouseDragged() {
        drawables.forEach(drawable -> drawable.mouseDragged(this, 1, 0, 0));
    }


    @Override
    public void dispose() {
        super.dispose();
        if (executor != null && !executor.isTerminated()) {
            executor.getQueue().clear();
            timer.cancel();
            System.out.println("waiting for " + executor.getActiveCount() + " images to finish before shutting down...");
            try {
                executor.shutdown();
                executor.awaitTermination(25, TimeUnit.SECONDS);
                System.out.println("finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
                executor.shutdownNow();
            }
        }
    }
}
