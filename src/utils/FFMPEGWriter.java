package utils;

import utils.helperClasses.ImageSaver;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

// very simple class, it's just nice to automatically have the file account for fps and padding differences
@SuppressWarnings("SpellCheckingInspection")
public class FFMPEGWriter {

    private final static String fileName = "_ffmpeg batch.bat";

    public static void writeBatch(String renderDir, float fps) {
        try {
            FileWriter writer = new FileWriter(Path.of(renderDir, fileName).toFile(), false);
            writer.write("ffmpeg.exe -r " + fps + " -i %%0" + ImageSaver.padCount + "d.png " +
                    "-vcodec libx264 -preset veryfast -crf 15 -r " + fps + " -y _output.mp4");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
