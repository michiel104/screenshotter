package nl.dotjava.screenshotter.input;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.Files.readAllLines;

public class WebsiteReader {

    private WebsiteReader() {
        // default empty constructor
    }

    public static List<String> readWebsitesFromFile(String filename) {
        try {
            Path filePath = Paths.get(filename);
            return readAllLines(filePath, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }
        return List.of();
    }
}
