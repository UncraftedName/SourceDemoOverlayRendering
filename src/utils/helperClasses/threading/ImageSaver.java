package utils.helperClasses.threading;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// a class to asynchronously save the applet images as png's
public class ImageSaver implements Runnable {

    public final static ConcurrentHashMap<String, AtomicInteger> framesFinishedInDir = new ConcurrentHashMap<>();
    public final static int padCount = 5; // output is padded with this many 0's
    private String outDir;
    private int frameNumber;
    private BufferedImage image;
    // I have no idea why, but every time a thread is started w/ the executor, it calls run() twice.
    // So I prevent that by not doing anything if run() has already been called.
    private boolean alreadyRun;


    public ImageSaver(String outDir, int frameNumber, BufferedImage image) {
        this.outDir = outDir;
        this.frameNumber = frameNumber;
        this.image = image;
        alreadyRun = false;
        if (!framesFinishedInDir.containsKey(outDir))
            framesFinishedInDir.put(outDir, new AtomicInteger());
    }


    @Override
    public void run() {
        if (!alreadyRun) {
            // It seems that separate threads call run() - if 'alreadyRun = true' is moved to the bottom of this method
            // run() will still be called twice.
            alreadyRun = true;
            try {
                ImageIO.write(image, "png", new File(outDir + "/" +
                        String.format("%1$" + padCount + "s", Integer.toString(frameNumber)).replace(' ', '0') + ".png"));
                framesFinishedInDir.get(outDir).incrementAndGet();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("couldn't save image with frame number: " + frameNumber + " :(");
            }
        }
    }
}
