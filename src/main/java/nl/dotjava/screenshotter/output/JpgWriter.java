package nl.dotjava.screenshotter.output;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/** writes images as {@code <prefix><counter>.jpg} to a folder */
public class JpgWriter {

    private final Path directory;
    private final String prefix;
    private int counter;

    public JpgWriter(Path directory, String prefix) {
        this.directory = directory;
        this.prefix = prefix;
        this.counter = 1;
    }

    public Path write(BufferedImage image) throws IOException {
        Files.createDirectories(directory);
        BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        rgb.createGraphics().drawImage(image, 0, 0, null);
        Path file = directory.resolve(prefix + String.format("%04d", counter++) + ".jpg");
        if (!ImageIO.write(rgb, "jpg", file.toFile())) {
            throw new IOException("No JPG writer available");
        }
        return file;
    }

    public void deleteFiles() throws IOException {
        if (!Files.isDirectory(directory)) {
            return;
        }
        List<Path> jpgs;
        try (Stream<Path> files = Files.list(directory)) {
            jpgs = files
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().toLowerCase().endsWith(".jpg"))
                    .toList();
        }
        for (Path jpg : jpgs) {
            Files.delete(jpg);
        }
    }
}
