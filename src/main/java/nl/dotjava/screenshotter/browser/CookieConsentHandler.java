package nl.dotjava.screenshotter.browser;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/** best-effort click on a cookie banner's accept button, does nothing when no banner is found */
public class CookieConsentHandler {

    private static final List<By> ACCEPT_BUTTONS = List.of(
            By.cssSelector("#onetrust-accept-btn-handler"),
            By.cssSelector("button[id*='accept' i]"),
            By.cssSelector("a[id*='accept' i]"),
            By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accepteer')]"),
            By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept')]"),
            By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'akkoord')]")
    );

    public boolean acceptIfPresent(WebDriver driver) {
        for (By locator : ACCEPT_BUTTONS) {
            for (WebElement element : driver.findElements(locator)) {
                try {
                    if (element.isDisplayed()) {
                        element.click();
                        return true;
                    }
                } catch (RuntimeException ignored) {
                    // stale or not clickable; try the next candidate
                }
            }
        }
        return false;
    }
}
