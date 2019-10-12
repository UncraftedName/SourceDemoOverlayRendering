package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class PositionManager {

    public static SmallDemoFormat[] demosFromPaths(String[] demoPaths) {
        List<SmallDemoFormat> smallDemos = new ArrayList<>();
        // parallel increases speed by ~5x for large amounts of demos
        Arrays.stream(demoPaths).parallel().forEach(demoPath -> {
            try {
                smallDemos.add(new SmallDemoFormat(demoPath));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-2);
            } catch (NoSuchElementException e) {
                e.printStackTrace();
                System.out.println("ignoring demo \"" + new File(demoPath).getName() + "\"\n");
            }
        });
        return smallDemos.toArray(new SmallDemoFormat[0]);
    }
}
