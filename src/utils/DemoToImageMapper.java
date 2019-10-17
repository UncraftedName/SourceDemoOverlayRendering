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
    public class WarpedMapper {

        public final float screenX0, screenY0;
        public final float xRatio, yRatio;
        public final float shrinkRatio;
        public final boolean shrinkX;

        public WarpedMapper() {
            DemoToImageMapper baseMapper = DemoToImageMapper.this; // outer class instance
            shrinkRatio = Math.abs(Math.min(baseMapper.xRatio, baseMapper.yRatio)
                    / Math.max(baseMapper.xRatio, baseMapper.yRatio));
            
            shrinkX = baseMapper.xRatio > baseMapper.yRatio;
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


        public float getScreenX(float x, float y, float z) {
            switch (DemoToImageMapper.this.viewType) {
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
            switch (DemoToImageMapper.this.viewType) {
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
    }


    private enum StoredProperties {
        SCREEN_X0,
        SCREEN_Y0,
        X_RATIO,
        Y_RATIO,
        VIEW_TYPE
    }
}
