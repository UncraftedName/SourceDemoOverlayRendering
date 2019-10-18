package utils.helperClasses;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;

public class ImageSaverRunnable implements Runnable {

    // for the priority queue to prioritize threads w/ lower frame numbers. Attempts to write the frames in order.
    public final static Comparator<Runnable> frameNumComparator = Comparator.comparingInt(o -> ((ImageSaverRunnable)o).frameNumber);
    public final static int padCount = 5; // output is padded with this many 0's
    private String outDir;
    private int frameNumber;
    private BufferedImage image;


    public ImageSaverRunnable(String outDir, int frameNumber, BufferedImage image) {
        this.outDir = outDir;
        this.frameNumber = frameNumber;
        this.image = image;
    }


    @Override
    public void run() {
        try {
            ImageIO.write(image, "png",  new File(outDir + "/" +
                    String.format("%1$" + padCount + "s", Integer.toString(frameNumber)).replace(' ', '0') + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("couldn't save image with frame number: " + frameNumber + " :(");
        }
    }
}
