import utils.TGAReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class ConvertTgaToPng {

    // run this to convert every tga in the img folder to a png w/ the same name.
    // allows you to preview it and use it with the default libraries
    public static void main(String[] args) throws IOException {
        Files.walkFileTree(Paths.get("img"), new FileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {byte[] buffer;
                if (file.toString().endsWith(".tga")) {
                    FileInputStream fis = new FileInputStream(file.toFile());
                    buffer = new byte[fis.available()];
                    //noinspection ResultOfMethodCallIgnored
                    fis.read(buffer);
                    fis.close();

                    int[] pixels = TGAReader.read(buffer, TGAReader.ARGB);
                    int width = TGAReader.getWidth(buffer);
                    int height = TGAReader.getHeight(buffer);
                    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    image.setRGB(0, 0, width, height, pixels, 0, width);
                    ImageIO.write(image, "png", new File(file.toString().replaceFirst("[.][^.]+$", "") + ".png"));
                    Files.deleteIfExists(file.toAbsolutePath());
                    System.out.println("Converted " + file.toAbsolutePath().toString());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
