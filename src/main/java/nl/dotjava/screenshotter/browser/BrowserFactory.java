package nl.dotjava.screenshotter.browser;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.nio.file.Files;
import java.nio.file.Path;

/** creates a headless Firefox with a fixed window size (viewport is predictable) */
public final class BrowserFactory {

    // firefox location
    private static final Path SNAP_FIREFOX = Path.of("/snap/firefox/current/usr/lib/firefox/firefox");

    private BrowserFactory() {
        // default empty constructor
    }

    /**
     * selenium rejects /usr/bin/firefox on snap installs because it is only a wrapper script.
     * use -Dfirefox.binary if given, else the real snap binary when present, else let Selenium decide.
     */
    private static String firefoxBinary() {
        String configured = System.getProperty("firefox.binary");
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        return Files.isExecutable(SNAP_FIREFOX) ? SNAP_FIREFOX.toString() : null;
    }

    public static WebDriver headlessFirefox(int width, int height) {
        FirefoxOptions options = new FirefoxOptions();
        String binary = firefoxBinary();
        if (binary != null) {
            options.setBinary(binary);
        }
        options.addArguments("-headless", "--width=" + width, "--height=" + height);
        return new FirefoxDriver(options);
    }
}
