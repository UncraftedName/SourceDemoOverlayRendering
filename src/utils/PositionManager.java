package utils;

import java.io.*;
import java.util.*;

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
        // sort the demos based on length so the faster ones are displayed first
        smallDemos.sort(Comparator.comparingInt(o -> o.maxTick));
        Collections.reverse(smallDemos);
        return smallDemos.toArray(new SmallDemoFormat[0]);
    }
}
