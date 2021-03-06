package utils;

import main.ImageCalibrator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

// loads and writes the image scales & offsets during calibration
public class DemoToImageMapper {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final static HashMap<StoredProperties, String> propertyAsStringMapper;
    private final Properties properties;
    private final String xmlPath;
    private final String imgPath;
    private float screenX0, screenY0;
    private float xRatio, yRatio;
    public ImageCalibrator.ViewType viewType;

    static {
        propertyAsStringMapper = new HashMap<>() {{
            put(StoredProperties.SCREEN_X0, " -- screenX0");
            put(StoredProperties.SCREEN_Y0, " -- screenY0");
            put(StoredProperties.X_RATIO, " -- x pixels per game unit");
            put(StoredProperties.Y_RATIO, " -- y pixels per game unit");
            put(StoredProperties.VIEW_TYPE, " -- view type");
        }};
    }


    public DemoToImageMapper(String xmlPath, String imgPath) {
        properties = new Properties();
        this.xmlPath = xmlPath;
        this.imgPath = imgPath;
    }


    // Throws exception if the file doesn't exists or properties don't exist in file,
    // the catcher then generates default values for the positions of the game locations (the two balls).
    public void loadProperties() throws NullPointerException {
        try {
            properties.loadFromXML(new FileInputStream(xmlPath));
        } catch (IOException e) {
            System.out.println("No xml file found at " + xmlPath + ", will generate a new file if properties are be saved.");
        }
        screenX0 = Float.parseFloat(properties.getProperty(imgPath + propertyAsStringMapper.get(StoredProperties.SCREEN_X0)));
        screenY0 = Float.parseFloat(properties.getProperty(imgPath + propertyAsStringMapper.get(StoredProperties.SCREEN_Y0)));
        xRatio = Float.parseFloat(properties.getProperty(imgPath + propertyAsStringMapper.get(StoredProperties.X_RATIO)));
        yRatio = Float.parseFloat(properties.getProperty(imgPath + propertyAsStringMapper.get(StoredProperties.Y_RATIO)));
        viewType = ImageCalibrator.ViewType.valueOf(properties.getProperty(imgPath + propertyAsStringMapper.get(StoredProperties.VIEW_TYPE)));
    }


    public void setProperties(float screenX0, float screenY0, float xRatio, float yRatio, ImageCalibrator.ViewType viewType) {
        this.screenX0 = screenX0;
        this.screenY0 = screenY0;
        this.xRatio = xRatio;
        this.yRatio = yRatio;
        this.viewType = viewType;
        properties.setProperty(imgPath + propertyAsStringMapper.get(StoredProperties.SCREEN_X0), String.valueOf(screenX0));
        properties.setProperty(imgPath + propertyAsStringMapper.get(StoredProperties.SCREEN_Y0), String.valueOf(screenY0));
        properties.setProperty(imgPath + propertyAsStringMapper.get(StoredProperties.X_RATIO), String.valueOf(xRatio));
        properties.setProperty(imgPath + propertyAsStringMapper.get(StoredProperties.Y_RATIO), String.valueOf(yRatio));
        properties.setProperty(imgPath + propertyAsStringMapper.get(StoredProperties.VIEW_TYPE), viewType.toString());
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void writeProperties() {
        try {
            File f = new File(xmlPath);
            f.getParentFile().mkdirs();
            f.createNewFile();
            properties.storeToXML(new FileOutputStream(xmlPath), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // redundant to have this in here and the internal class
    public float getScreenX(float x, float y, float z) {
        switch (viewType) {
            case Z_CONST_XX:
            case Y_CONST:
                return screenX0 + xRatio * x;
            case Z_CONST_XY:
            case X_CONST:
                return screenX0 + xRatio * y;
            default:
                throw new IllegalArgumentException("unknown view type");
        }
    }


    public float getScreenY(float x, float y, float z) {
        switch (viewType) {
            case Z_CONST_XX:
                return screenY0 + yRatio * y;
            case Z_CONST_XY:
                return screenY0 + yRatio * x;
            case X_CONST:
            case Y_CONST:
                return screenY0 + yRatio * z;
            default:
                throw new IllegalArgumentException("unknown view type");
        }
    }


    // Ensures that x game units per pixel = y game units per pixel.
    // This is done by scaling one of those values to match the other;
    // no stretching is done, only shrinking.
    public class ScaledMapper {

        public final float screenX0, screenY0;
        public final float xRatio, yRatio;
        public final float shrinkRatio;
        public final boolean shrinkX;

        public ScaledMapper() {
            DemoToImageMapper baseMapper = DemoToImageMapper.this; // outer class instance
            float absXRatio = Math.abs(baseMapper.xRatio);
            float absYRatio = Math.abs(baseMapper.yRatio);
            shrinkRatio = Math.min(absXRatio, absYRatio)
                    / Math.max(absXRatio, absYRatio);
            
            shrinkX = absXRatio > absYRatio;
            if (shrinkX) {
                screenX0 = baseMapper.screenX0 * shrinkRatio;
                screenY0 = baseMapper.screenY0;
                xRatio = baseMapper.xRatio * shrinkRatio;
                yRatio = baseMapper.yRatio;
            } else {
                screenX0 = baseMapper.screenX0;
                screenY0 = baseMapper.screenY0 * shrinkRatio;
                xRatio = baseMapper.xRatio;
                yRatio = baseMapper.yRatio * shrinkRatio;
            }
        }


        public double getScreenX(double[] arr) { // x, y, z
            switch (DemoToImageMapper.this.viewType) {
                case Z_CONST_XX:
                case Y_CONST:
                    return screenX0 + xRatio * arr[0];
                case Z_CONST_XY:
                case X_CONST:
                    return screenX0 + xRatio * arr[1];
                default:
                    throw new IllegalArgumentException("unknown view type");
            }
        }


        public double getScreenY(double[] arr) { // x, y, z
            switch (DemoToImageMapper.this.viewType) {
                case Z_CONST_XX:
                    return screenY0 + yRatio * arr[1];
                case Z_CONST_XY:
                    return screenY0 + yRatio * arr[0];
                case X_CONST:
                case Y_CONST:
                    return screenY0 + yRatio * arr[2];
                default:
                    throw new IllegalArgumentException("unknown view type");
            }
        }


        // get screen angle from in-game angles; visually 'y' is flipped but the math is the same
        public double getScreenYaw(double[] arr) { // (in-game) pitch, yaw, roll
            double out;
            switch (DemoToImageMapper.this.viewType) {
                case Z_CONST_XX:
                    out = arr[1] * Math.signum(yRatio);
                    if (xRatio < 0)
                        out = (180 - Math.abs(out)) * Math.signum(out); // reflect across x-axis
                    break;
                case Z_CONST_XY:
                    // same as above, but now ang = (-yaw + 90), and x & y axes are switched
                    out = (-arr[1] + 90) * Math.signum(xRatio);
                    if (yRatio < 0)
                        out = (180 - Math.abs(out)) * Math.signum(out);
                    break;
                case X_CONST:
                case Y_CONST:
                    double scaleX = Math.cos(Math.toRadians(arr[1]));
                    out = Math.toDegrees(Math.acos(Math.cos(Math.toRadians(arr[0])) * scaleX));
                    if (arr[0] < 0 == yRatio < 0)
                        out *= -1;
                    if (xRatio < 0)
                        out = (180 - Math.abs(out)) * Math.signum(out);
                    break;
                default:
                    throw new IllegalArgumentException("unknown view type");
            }
            return Math.toRadians(out);
        }
    }


    private enum StoredProperties {
        SCREEN_X0,
        SCREEN_Y0,
        X_RATIO,
        Y_RATIO,
        VIEW_TYPE
    }
}
