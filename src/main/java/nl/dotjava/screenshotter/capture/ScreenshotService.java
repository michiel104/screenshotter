package nl.dotjava.screenshotter.capture;

import nl.dotjava.screenshotter.browser.CookieConsentHandler;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;

import static java.lang.Math.round;

/** navigates to an URL, accepts cookies, waits for rendering and returns the cropped area */
public class ScreenshotService {

    private final WebDriver driver;
    private final CookieConsentHandler cookies;
    private final CaptureArea area;
    private final Duration renderDelay;

    public ScreenshotService(WebDriver driver, CookieConsentHandler cookies, CaptureArea area, Duration renderDelay) {
        this.driver = driver;
        this.cookies = cookies;
        this.area = area;
        this.renderDelay = renderDelay;
    }

    public BufferedImage capture(String url) {
        // URLs that differ only in the #fragment would not reload the page; go through a blank page first.
        driver.get("about:blank");
        driver.get(url);
        sleep(renderDelay);
        if (cookies.acceptIfPresent(driver)) {
            sleep(Duration.ofSeconds(1));
        }
        return crop(fullViewport());
    }

    private BufferedImage fullViewport() {
        byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        try {
            return ImageIO.read(new ByteArrayInputStream(png));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private BufferedImage crop(BufferedImage image) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        double viewportWidth = ((Number) js.executeScript("return window.innerWidth;")).doubleValue();
        double scale = image.getWidth() / viewportWidth; // device pixel ratio
        int x = (int) round(area.x() * scale);
        int y = (int) round(area.y() * scale);
        int w = (int) round(area.width() * scale);
        int h = (int) round(area.height() * scale);

        if (x + w > image.getWidth()) {
            w = image.getWidth() - x;
        }
        if (y + h > image.getHeight()) {
            h = image.getHeight() - y;
        }
        // because of the above, this will not occur (something to think about later for nicer handling)
        if (x + w > image.getWidth() || y + h > image.getHeight()) {
            throw new IllegalStateException(("Capture area of %dx%d starting at (%d,%d) does not fit in the %dx%d viewport; "
                    + "x + width must be at most %d and y + height at most %d.")
                    .formatted(w, h, x, y, image.getWidth(), image.getHeight(),
                            (int) (image.getWidth() / scale), (int) (image.getHeight() / scale)));
        }
        return image.getSubimage(x, y, w, h);
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
