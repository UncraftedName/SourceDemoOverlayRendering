package utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class SmallDemoFormat {

    public final String demoName;
    public final String playerNameInDemo;
    public final ArrayList<Position> positions; // positions for every player


    public SmallDemoFormat(String demoPath) throws IOException, IllegalArgumentException {
        demoName = new File(demoPath).getName().replaceFirst("[.][^.]+$", ""); // file name without extension
        FileInputStream demoFileStream = new FileInputStream(demoPath);
        demoFileStream.skipNBytes(276);
        byte[] playerNameAsBytes = new byte[260];
        if (demoFileStream.read(playerNameAsBytes, 276, 260) == -1)
            throw new IllegalArgumentException();
        demoFileStream.close();
        int indexOfNullChar = 0;
        for (; indexOfNullChar < 260; indexOfNullChar++)
            if (playerNameAsBytes[indexOfNullChar] == 0)
                break;
        playerNameInDemo = new String(playerNameAsBytes, 0, indexOfNullChar, StandardCharsets.US_ASCII);
        InputStream parserOutput = Runtime.getRuntime().exec(
                new String[]{"resource/UncraftedDemoParser.exe", demoPath, "-p"}).getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(parserOutput));
        positions = new ArrayList<>();

        reader.lines().filter(s -> s.charAt(0) == '|').forEach(s -> { // |tick|-|x1,y1,z1|p1,y1,r1|-|x2,y2,z2|p2,y2,r2|...
            String[] tickAndPlayers = s.split("~");
            int tick = Integer.parseInt(tickAndPlayers[0].substring(1, tickAndPlayers[0].length() - 1)); // |tick|
            if (tick >= 0) { // ignore negative ticks
                // {{x1,y1,z1}, {p1,y1,r1}}, {{x2,y2,z2}, {p2,y2,r2}}...
                String[][] locationsAndAngles = (String[][]) Arrays.stream(tickAndPlayers).skip(1).map(player -> player.split("\\|")).toArray();
                double[][] locations = new double[locationsAndAngles.length][3];
                double[][] angles = new double[locationsAndAngles.length][3];
                for (int i = 0; i < locationsAndAngles.length; i++) {
                    locations[i] = Arrays.stream(locationsAndAngles[i][0].split(",")).mapToDouble(Double::parseDouble).toArray();
                    angles[i] = Arrays.stream(locationsAndAngles[i][1].split(",")).mapToDouble(Double::parseDouble).toArray();
                }
                positions.add(new Position(tick, locations, angles));
            }
        });
    }


    public class Position {

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