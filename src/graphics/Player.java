package graphics;

import playerInfo.PlayerIcon;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import utils.DemoToImageMapper;
import utils.SmallDemoFormat;

import java.io.File;
import java.util.HashMap;

public class Player implements Drawable {

    private final static float textScale = .3f;
    // these maps are stored to not have to load the same player from multiple demos
    private static HashMap<String, PImage> playerToImgMap = new HashMap<>();
    private static HashMap<String, Integer> playerToTextColorMapper = new HashMap<>();
    private static HashMap<String, Boolean> playerUsesDefaultAvatar = new HashMap<>(); // bleh

    private final SmallDemoFormat demo;
    private final float diameter;
    private final TextSetting textSetting;
    private final PImage img;
    public float x, y;
    private final DemoToImageMapper.WarpedMapper mapper;
    public boolean invisible;
    private final boolean defaultAvatar;
    private final float textSize;
    private final int textColor;


    public Player(PApplet applet, SmallDemoFormat demo, float diameter, TextSetting setting, DemoToImageMapper.WarpedMapper mapper) {
        this.demo = demo;
        this.diameter = diameter;
        textSize = diameter * textScale;
        textSetting = setting;
        this.mapper = mapper;
        invisible = false;
        if (playerToImgMap.containsKey(demo.playerNameInDemo)) {
            System.out.println("copied avatar for \"" + demo.playerNameInDemo + "\" in demo \"" + demo.demoName + ".dem\"");
            img = playerToImgMap.get(demo.playerNameInDemo);
            textColor = playerToTextColorMapper.get(demo.playerNameInDemo);
            defaultAvatar = playerUsesDefaultAvatar.get(demo.playerNameInDemo);
        } else {
            String path = "img/icons/players/" + demo.playerNameInDemo + ".png";
            PImage loadedImage = null;
            if (new File(path).exists()) // to prevent flooding the console w/ errors
                loadedImage = applet.loadImage(path);
            if (loadedImage == null) {
                try {
                    PlayerIcon icon = PlayerIcon.valueOf(demo.playerNameInDemo.replace(' ', '_')); // enum name can't have spaces
                    loadedImage = applet.loadImage(icon.uri);
                } catch (IllegalArgumentException ignored) {}
            }
            if (loadedImage == null) {
                System.out.println("no avatar found for \"" + demo.playerNameInDemo + "\" in demo \"" + demo.demoName + ".dem\"");
                defaultAvatar = true;
                img = applet.loadImage("img/icons/sanic.png");
                textColor = applet.color(50, 80, 255); // default text color
            } else {
                System.out.println("set avatar for \"" + demo.playerNameInDemo + "\" in demo \"" + demo.demoName + "\"");
                defaultAvatar = false;
                int minDim = Math.min(loadedImage.width, loadedImage.height);
                img = new PImage(minDim, minDim, PConstants.ARGB);
                loadedImage.loadPixels();
                img.loadPixels();
                double r = 0, g = 0, b = 0;
                for (int i = 0; i < minDim * minDim; i++) { // manually get a circular selection of the image
                    int x = i % minDim;
                    int y = i / minDim;
                    if (Math.sqrt((x - (minDim >> 1)) * (x - (minDim >> 1)) + (y - (minDim >> 1)) * (y - (minDim >> 1))) < minDim >> 1) // if pixel is in circle
                        img.pixels[i] = loadedImage.pixels[i];
                    else
                        img.pixels[i] = applet.color(0, 0);
                    // kind of sort of attempt to fix some dark profile pictures; will improve upon this
                    // f(x) = exp(-x/50 + 1.5) + 1
                    // correction(x) = (x+10)f(x)
                    r += Math.min(255, (Math.exp(applet.red(loadedImage.pixels[i]) / -50f + 1.5f) + 1.0f)) * (8 + applet.red(loadedImage.pixels[i]));
                    g += Math.min(255, (Math.exp(applet.green(loadedImage.pixels[i]) / -50f + 1.5f) + 1.0f)) * (8 + applet.green(loadedImage.pixels[i]));
                    b += Math.min(255, (Math.exp(applet.blue(loadedImage.pixels[i]) / -50f + 1.5f) + 1.0f)) * (8 + applet.blue(loadedImage.pixels[i]));
                }
                // set to the average of the image
                textColor = applet.color((float) r / img.pixels.length, (float) g / img.pixels.length, (float) b / img.pixels.length);
                img.updatePixels();
            }
            playerToImgMap.put(demo.playerNameInDemo, img);
            playerToTextColorMapper.put(demo.playerNameInDemo, textColor);
            playerUsesDefaultAvatar.put(demo.playerNameInDemo, defaultAvatar);
        }
    }


    @SuppressWarnings("DuplicateBranchesInSwitch")
    @Override // scale & translation are ignored
    public void draw(PGraphics canvas, float scaleFactor, float transX, float transY) {
        if (!invisible) {
            canvas.pushStyle();
            canvas.imageMode(PConstants.CENTER);
            canvas.image(img, x, y, diameter, diameter);
            canvas.textSize(textSize);
            boolean showPlayerName = false, showDemoName = false;
            switch (textSetting) {
                case DEMO_NAME_IF_NO_AVATAR:
                    if (!defaultAvatar)
                        break;
                case DEMO_NAME:
                    showDemoName = true;
                    break;
                case PLAYER_NAME_IF_NO_AVATAR:
                    if (!defaultAvatar)
                        break;
                case PLAYER_NAME:
                    showPlayerName = true;
                    break;
                case DEMO_AND_PLAYER_NAME_IF_NO_AVATAR:
                    if (!defaultAvatar)
                        break;
                case DEMO_AND_PLAYER_NAME:
                    showDemoName = showPlayerName = true;
                    break;
                case NONE:
                default:
                    break;
            }
            canvas.fill(textColor);
            canvas.textAlign(PConstants.CENTER);
            if (showPlayerName && showDemoName) {
                canvas.text(demo.playerNameInDemo, x, y + diameter * 0.7f);
                canvas.text(demo.demoName, x, y + diameter * .7f + textSize);
            } else if (showPlayerName) {
                canvas.text(demo.playerNameInDemo, x, y + diameter * 0.7f);
            } else if (showDemoName) {
                canvas.text(demo.demoName, x, y + diameter * 0.7f);
            }
            canvas.popStyle();
        }
    }


    public void setCoords(float tick) {
        int intTick = (int)tick; // for now, will do interp later
        if (intTick > demo.maxTick) {
            invisible = true;
            return;
        }
        SmallDemoFormat.Position pos = null;
        for (SmallDemoFormat.Position position : demo.positions) {
            if (position.tick == intTick) {
                pos = position;
                break;
            }
        }
        if (pos != null) {
            float demoX = (float) pos.locations[0][0];
            float demoY = (float) pos.locations[0][1];
            float demoZ = (float) pos.locations[0][2];
            x = mapper.getScreenX(demoX, demoY, demoZ);
            y = mapper.getScreenY(demoX, demoY, demoZ);
        }
    }


    public enum TextSetting {
        NONE,
        PLAYER_NAME,
        DEMO_NAME,
        DEMO_AND_PLAYER_NAME,
        PLAYER_NAME_IF_NO_AVATAR,
        DEMO_NAME_IF_NO_AVATAR,
        DEMO_AND_PLAYER_NAME_IF_NO_AVATAR
    }
}
