package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class PositionManager {

    public static SmallDemoFormat[] demosFromPaths(String[] demoPaths, boolean interpolate) {
        List<SmallDemoFormat> smallDemos = new ArrayList<>();
        for (String demoPath : demoPaths) {
            try {
                smallDemos.add(new SmallDemoFormat(demoPath));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-2);
            } catch (NoSuchElementException e) {
                e.printStackTrace();
                System.out.println("ignoring demo \"" + new File(demoPath).getName() + "\"\n");
            }
        }
        return smallDemos.toArray(new SmallDemoFormat[0]);
        // todo add the interpolation
    }


    public static class DemoToImageMapper {

        public final float xPixels;
        public final float yPixels;
        public final float gameX0;
        public final float gameY0;


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
