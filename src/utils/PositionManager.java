package utils;

import java.io.*;
import java.util.Arrays;

public class PositionManager {

    public static SmallDemoFormat[] demosFromPaths(String[] demoPaths, boolean interpolate) {
        SmallDemoFormat[] smallDemos = new SmallDemoFormat[demoPaths.length];
        try {
            for (int i = 0; i < demoPaths.length; i++)
                smallDemos[i] = new SmallDemoFormat(demoPaths[i]);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-2);
        }
        return smallDemos;
        // todo add the interpolation
    }


    public static class DemoToImageMapper {

        private final float xPixels;
        private final float yPixels;
        private final float gameX0;
        private final float gameY0;


        public DemoToImageMapper(float xPixels, float yPixels, float gameX0, float gameY0) {
            this.xPixels = xPixels;
            this.yPixels = yPixels;
            this.gameX0 = gameX0;
            this.gameY0 = gameY0;
        }


        public double getImgX(double gameX) {
            return gameX0 + xPixels * gameX;
        }


        public double getImgY(double gameY) {
            return gameY0 + yPixels * gameY;
        }
    }
}
