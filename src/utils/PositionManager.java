package utils;

import java.io.*;
import java.util.*;

public class PositionManager {

    public static Demo[] demosFromPaths(String[] demoPaths) {
        List<Demo> demos = new ArrayList<>();
        // parallel increases speed by ~5x for large amounts of demos; TODO use thread pool executor
        Arrays.stream(demoPaths).parallel().forEach(demoPath -> {
            try {
                demos.add(new Demo(demoPath));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-2);
            } catch (NoSuchElementException e) {
                e.printStackTrace();
                System.out.println("ignoring demo \"" + new File(demoPath).getName() + "\"\n");
            }
        });
        // sort the demos based on length so the faster ones are displayed first
        demos.sort(Comparator.comparingInt(o -> o.maxTick));
        Collections.reverse(demos);
        return demos.toArray(new Demo[0]);
    }
}
