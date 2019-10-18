package utils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class SmallDemoFormat {

    public final String demoName;
    public final String playerNameInDemo;
    public final ArrayList<Position> positions; // positions for every player
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
        positions = new ArrayList<>();
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
        maxTick = positions.stream().mapToInt(position -> position.tick).max().getAsInt();
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

        public final int tick;
        // [player][loc/ang]
        // internally the demos will use floats, but java only has double streams
        public final double[][] locations; // x, y, z
        public final double[][] viewAngles; // pitch, yaw, roll

        public Position(int tick, double[][] locations, double[][] viewAngles) {
            this.tick = tick;
            this.locations = locations;
            this.viewAngles = viewAngles;
        }
    }
}