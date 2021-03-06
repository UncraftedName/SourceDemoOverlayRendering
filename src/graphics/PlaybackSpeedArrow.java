package graphics;

import main.Main;
import processing.core.PImage;
import utils.helperClasses.HelperFuncs;

public class PlaybackSpeedArrow implements Drawable {

    private final static String fastForwardImgDir = "img/icons/fast-forward.png";
    private final static String pauseImgDir = "img/icons/pause.png";
    private final static int diameter = 100;
    private static PImage pauseImg, forwardsPlaybackImg, backwardsPlaybackImg;


    public PlaybackSpeedArrow(Main applet) {
        if (forwardsPlaybackImg == null) {
            forwardsPlaybackImg = applet.loadImage(fastForwardImgDir);
            forwardsPlaybackImg.resize(diameter, diameter);
            pauseImg = applet.loadImage(pauseImgDir);
            pauseImg.resize(diameter, diameter);
            backwardsPlaybackImg = new PImage(diameter, diameter);
            forwardsPlaybackImg.loadPixels();
            backwardsPlaybackImg.loadPixels();
            for (int x = 0; x < diameter; x++)
                for (int y = 0; y < diameter; y++)
                    backwardsPlaybackImg.pixels[y * diameter + (diameter - x - 1)] = forwardsPlaybackImg.pixels[y * diameter + x];
            backwardsPlaybackImg.updatePixels();
        }
    }


    @Override
    public void draw(Main canvas) {
        if (canvas.paused)
            canvas.image(pauseImg, 0, canvas.height - diameter);
        else if (canvas.hostTimeScale > 0)
            canvas.image(forwardsPlaybackImg, 0, canvas.height - diameter);
        else
            canvas.image(backwardsPlaybackImg, 0, canvas.height - diameter);
        canvas.textSize(30);
        canvas.text("Speed: " + HelperFuncs.roundToPlaces(canvas.hostTimeScale, 3), 0, canvas.height - diameter);
        if (Main.interpType == Player.InterpType.NONE)
            canvas.text("Tick: " + (int)canvas.currentTick, 0, canvas.height - diameter - 30);
        else
            canvas.text("Tick: " + HelperFuncs.roundToPlaces(canvas.currentTick, 1), 0, canvas.height - diameter - 30);
    }
}
