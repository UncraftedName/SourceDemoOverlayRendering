package utils;

import java.io.*;

public class PositionManager {

    public static SmallDemoFormat demoFromPath(String demoPath, boolean interpolate) {
        SmallDemoFormat smallDemo = null;
        try {
            smallDemo = new SmallDemoFormat(demoPath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-2);
        }
        return smallDemo;
        // todo add the interpolation
    }
}
