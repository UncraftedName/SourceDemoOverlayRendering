package graphics;

import main.Main;
import playerInfo.PlayerIcon;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import utils.DemoToImageMapper;
import utils.Demo;

import java.io.File;
import java.util.HashMap;

public class Player implements Drawable {

    private final static float textScale = .3f;
    private final static float linearThreshold = 62.518f; // if two ticks are apart by this much or more, no interp is done
    private final static int backgroundCircleScale = 6; // how much larger the background circle is than the player (in pixels)
    // these maps are stored to not have to load the same player from multiple demos
    private static HashMap<String, PImage> playerToImgMap = new HashMap<>();
    private static HashMap<String, Integer> playerToTextColorMapper = new HashMap<>();
    private static HashMap<String, Boolean> playerUsesDefaultAvatar = new HashMap<>(); // bleh

    public final Demo demo;
    final float diameter;
    private final TextSetting textSetting;
    private final InterpType interpType;
    private final PImage playerImg;
    public float x, y;
    private final DemoToImageMapper.ScaledMapper mapper;
    public boolean invisible;
    private final boolean defaultAvatar;
    private final float textSize;
    final int textColor;
    int backgroundColor = 0; // starts completely transparent, this field is used exclusively by DraggableSelection right now
    private final PlayerArrow playerArrow;
    public Demo.PosAndRot currentPosAndRot;


    public Player(PApplet applet, Demo demo, float diameter, TextSetting setting, InterpType interpType, DemoToImageMapper.ScaledMapper mapper) {
        this.demo = demo;
        this.diameter = diameter;
        textSize = diameter * textScale;
        textSetting = setting;
        this.interpType = interpType;
        this.mapper = mapper;
        invisible = false;
        if (playerToImgMap.containsKey(demo.playerNameInDemo)) {
            System.out.println("copied avatar for \"" + demo.playerNameInDemo + "\" in demo \"" + demo.demoName + ".dem\"");
            playerImg = playerToImgMap.get(demo.playerNameInDemo);
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
                playerImg = applet.loadImage("img/icons/sanic.png");
                textColor = applet.color(50, 80, 255); // default text color
            } else {
                System.out.println("set avatar for \"" + demo.playerNameInDemo + "\" in demo \"" + demo.demoName + "\"");
                defaultAvatar = false;
                int minDim = Math.min(loadedImage.width, loadedImage.height);
                playerImg = new PImage(minDim, minDim, PConstants.ARGB);
                loadedImage.loadPixels();
                playerImg.loadPixels();
                double r = 0, g = 0, b = 0;
                for (int i = 0; i < minDim * minDim; i++) { // manually get a circular selection of the image
                    int x = i % minDim;
                    int y = i / minDim;
                    if (Math.sqrt((x - (minDim >> 1)) * (x - (minDim >> 1)) + (y - (minDim >> 1)) * (y - (minDim >> 1))) < minDim >> 1) // if pixel is in circle
                        playerImg.pixels[i] = loadedImage.pixels[i];
                    else
                        playerImg.pixels[i] = applet.color(0, 0);
                    // kind of sort of attempt to fix some dark profile pictures; will improve upon this
                    // f(x) = exp(-x/50 + 1.5) + 1
                    // correction(x) = (x+10)f(x)
                    r += Math.min(255, (Math.exp(applet.red(loadedImage.pixels[i]) / -50f + 1.5f) + 1.0f)) * (8 + applet.red(loadedImage.pixels[i]));
                    g += Math.min(255, (Math.exp(applet.green(loadedImage.pixels[i]) / -50f + 1.5f) + 1.0f)) * (8 + applet.green(loadedImage.pixels[i]));
                    b += Math.min(255, (Math.exp(applet.blue(loadedImage.pixels[i]) / -50f + 1.5f) + 1.0f)) * (8 + applet.blue(loadedImage.pixels[i]));
                }
                // set to the average of the image
                textColor = applet.color((float) r / playerImg.pixels.length, (float) g / playerImg.pixels.length, (float) b / playerImg.pixels.length);
                playerImg.updatePixels();
            }
            playerToImgMap.put(demo.playerNameInDemo, playerImg);
            playerToTextColorMapper.put(demo.playerNameInDemo, textColor);
            playerUsesDefaultAvatar.put(demo.playerNameInDemo, defaultAvatar);
        }
        // initialize this at the end since this uses the players text color
        playerArrow = new PlayerArrow(applet, this);
    }


    @SuppressWarnings("DuplicateBranchesInSwitch")
    @Override // scale & translation are ignored
    public void draw(Main canvas) {
        invisible = canvas.currentTick > demo.maxTick || canvas.currentTick < 0;
        if (!invisible) {
            setCoords(canvas.currentTick);
            //System.out.println(Arrays.toString(currentPosAndRot.viewAngles[0]));
            playerArrow.draw(canvas);
            canvas.pushStyle();
            if (backgroundColor != 0) {
                canvas.strokeWeight(0);
                canvas.fill(backgroundColor);
                canvas.circle(x, y, diameter + backgroundCircleScale);
            }
            canvas.imageMode(PConstants.CENTER);
            canvas.image(playerImg, x, y, diameter, diameter);
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


    private void setCoords(float tick) {
        currentPosAndRot = null;
        Demo.PosAndRot tmpPos = new Demo.PosAndRot(tick);
        Demo.PosAndRot floorTick = demo.posAndRots.lower(tmpPos);
        Demo.PosAndRot ceilingTick = demo.posAndRots.ceiling(tmpPos);

        if (floorTick == null && ceilingTick == null)
            throw new IllegalArgumentException("no ticks found in " + demo.demoName);
        else if (floorTick == null)
            currentPosAndRot = ceilingTick;
        else if (ceilingTick == null)
            currentPosAndRot = floorTick;

        if (currentPosAndRot == null) {
            switch (interpType) {
                case LINEAR_THRESHOLD:
                    // if the position is greater than some threshold, don't interp
                    if (Demo.PosAndRot.distance(floorTick, ceilingTick, 0) / (ceilingTick.tick - floorTick.tick) > linearThreshold) {
                        currentPosAndRot = floorTick;
                        break;
                    }
                case LINEAR:
                    currentPosAndRot = Demo.PosAndRot.lerp(floorTick, ceilingTick, tick);
                    break;
                case NONE:
                default:
                    currentPosAndRot = floorTick;
                    break;
            }
        }
        x = (float)mapper.getScreenX(currentPosAndRot.locations[0]);
        y = (float)mapper.getScreenY(currentPosAndRot.locations[0]);
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

    public enum InterpType {
        NONE,
        LINEAR,
        LINEAR_THRESHOLD // don't interp if the pos jumps by more than ~63 units, ideally should also use velocity
    }
}
