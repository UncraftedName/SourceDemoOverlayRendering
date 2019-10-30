package utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public class SmallDemoFormat {

    public final String demoName;
    public final String playerNameInDemo;
    public final TreeSet<Position> positions = new TreeSet<>(Position.tickComparator); // positions for every player, sorted by tick
    public final int maxTick;


    public SmallDemoFormat(String demoPath) throws IOException, IllegalArgumentException {
        demoName = new File(demoPath).getName().replaceFirst("[.][^.]+$", ""); // file name without extension
        DataInputStream demoFileStream = new DataInputStream(new FileInputStream(new File(demoPath)));
        demoFileStream.skipNBytes(276); // skip over header, demo & network protocol, & server name
        byte[] playerNameAsBytes = new byte[260]; // null terminated (steam) client name, up to 260 chars long
        if (demoFileStream.read(playerNameAsBytes, 0, 260) == -1)
           throw new IllegalArgumentException("Not a demo file probably");
        demoFileStream.close();
        int indexOfNullChar = 0;
        for (; indexOfNullChar < 260; indexOfNullChar++) // loop until null char is found
            if (playerNameAsBytes[indexOfNullChar] == 0)
                break;
        playerNameInDemo = new String(playerNameAsBytes, 0, indexOfNullChar, StandardCharsets.UTF_8);
        // execute parser w/ "-p" to spew out the positions
        InputStream parserOutput = Runtime.getRuntime().exec(
                new String[]{"resource/UncraftedDemoParser.exe", demoPath, "-p"}).getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(parserOutput));
        // read the lines of the parser output and add the positions to the position list
        reader.lines().filter(s -> s.length() > 0 && s.charAt(0) == '|').forEach(s -> { // |tick|~|x1,y1,z1|p1,y1,r1|~|x2,y2,z2|p2,y2,r2|...
            String[] tickAndPlayers = s.split("~");
            int tick = Integer.parseInt(tickAndPlayers[0].substring(1, tickAndPlayers[0].length() - 1)); // |tick|
            if (tick >= 0) { // ignore negative ticks
                // {{x1,y1,z1}, {p1,y1,r1}}, {{x2,y2,z2}, {p2,y2,r2}}...
                String[][] locationsAndAngles = Arrays.stream(tickAndPlayers).skip(1).map(player -> player.split("\\|")).toArray(String[][]::new);
                double[][] locations = new double[locationsAndAngles.length][3];
                double[][] angles = new double[locationsAndAngles.length][3];
                for (int i = 0; i < locationsAndAngles.length; i++) {
                    locations[i] = Arrays.stream(locationsAndAngles[i][1].split(",")).mapToDouble(Double::parseDouble).toArray();
                    angles[i] = Arrays.stream(locationsAndAngles[i][2].split(",")).mapToDouble(Double::parseDouble).toArray();
                }
                // make sure there are no duplicate ticks, and remove the tick where pos = ang = 0
                if (positions.stream().noneMatch(position -> position.tick == tick) && !isNullTick(locations, angles))
                    positions.add(new Position(tick, locations, angles));
            }
        });
        //noinspection OptionalGetWithoutIsPresent
        maxTick = (int) positions.stream().mapToDouble(position -> position.tick).max().getAsDouble();
        populateVelocities();
    }


    private void populateVelocities() {
        Iterator<Position> positionIterator = positions.iterator();
        Position previous = null, current = positionIterator.next();
        while (positionIterator.hasNext()) {
            previous = current;
            current = positionIterator.next();
            for (int player = 0; player < previous.locations.length; player++)
                for (int component = 0; component < previous.locations[0].length; component++)
                    previous.velocities[player][component] = (current.locations[player][component] - previous.locations[player][component]) / (current.tick - previous.tick);
        }
        // last element is a  special case
        //noinspection ConstantConditions
        for (int player = 0; player < previous.locations.length; player++)
            for (int component = 0; component < previous.locations[0].length; component++)
                current.velocities[player][component] = (current.locations[player][component] - previous.locations[player][component]) / (current.tick - previous.tick);
    }


    // loc = ang = 0
    private static boolean isNullTick(double[][] locations, double[][] angles) {
        for (double[] location : locations)
            for (double locComp : location)
                if (locComp != 0)
                    return false;
        for (double[] angle : angles)
            for (double angComp : angle)
                if (angComp != 0)
                    return false;
        return true;
    }


    public static class Position {

        static final Comparator<Position> tickComparator = (o1, o2) -> Float.compare(o1.tick, o2.tick);

        public final float tick;
        // [player][loc/ang]
        // internally the demos will use floats, but java only has double streams
        public final double[][] locations; // x, y, z
        public final double[][] viewAngles; // pitch, yaw, roll
        public final double[][] velocities; // x, y, z


        public Position(int tick, double[][] locations, double[][] viewAngles) {
            this.tick = tick;
            this.locations = locations;
            this.viewAngles = viewAngles;
            // just initialize, populate once all the entire demo has been parsed and all the positions are known
            velocities = new double[locations.length][locations[0].length];
        }


        public Position(float tick) { // for searching the tree set
            this.tick = tick;
            locations = viewAngles = velocities = null;
        }


        public static double distance(Position p1, Position p2, int player) {
            double tmpSum = 0;
            for (int i = 0; i < 3; i++)
                tmpSum += (p1.locations[player][i] - p2.locations[player][i]) * (p1.locations[player][i] - p2.locations[player][i]);
            return Math.sqrt(tmpSum);
        }
    }
}