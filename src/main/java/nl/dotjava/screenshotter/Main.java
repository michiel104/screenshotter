package nl.dotjava.screenshotter;

import nl.dotjava.screenshotter.browser.BrowserFactory;
import nl.dotjava.screenshotter.browser.CookieConsentHandler;
import nl.dotjava.screenshotter.capture.CaptureArea;
import nl.dotjava.screenshotter.capture.ScreenshotService;
import nl.dotjava.screenshotter.output.JpgWriter;
import nl.dotjava.screenshotter.output.PdfWriter;
import org.openqa.selenium.WebDriver;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static nl.dotjava.screenshotter.input.WebsiteReader.readWebsitesFromFile;

/**
 * Command line tool which reads a list of websites, takes screenshots and outputs this in a .pdf file with metadata
 */
public class Main {

    // default jpg: size and upper-left corner (x,y) measured from the upper-left corner of the viewport
    public static final String WIDTH   = "2016";
    public static final String HEIGHT  = "1500";
    public static final String START_X = "10";
    public static final String START_Y = "10";
    // browser window must be large enough to contain the capture area
    private static final String WINDOW_WIDTH  = "3072"; // 3840;
    private static final String WINDOW_HEIGHT = "1570"; // 2160;
    private static final Duration RENDER_DELAY = Duration.ofSeconds(4);

    // command line arguments
    private static String project;
    private static String title;
    private static String author;
    private static String creator;
    private static String keywords;
    private static int width;
    private static int height;
    private static int x;
    private static int y;
    private static int wx;
    private static int wy;
    private static List<String> websites;

    static void main(String[] args) throws Exception {
        // 1. get info from command line
        unravelArguments(args);
        Path currentDirectory = Path.of(System.getProperty("user.dir"));
        Path outputDirectory = Path.of(System.getProperty("output.directory", "raw"));
        System.out.println("Starting screenshotter for '" + project + "' with " +  (long) websites.size() + " websites");
        System.out.println("Screenshot size [" + width + "," + height + "] starting at [" + x + "," + y + "] on viewport [" + wx + "," + wy + "]");
        if ((height+y) > (wy-86)) { // 86 pixels is roughly the height of the browser's address bar
            System.out.println("Warning: capture might be out of boundaries (" + (86+height+y-wy) + ")");
        }

        // 2. capture all URL's present with rectangle cropping
        CaptureArea area = new CaptureArea(x, y, width, height);
        JpgWriter jpgWriter = new JpgWriter(outputDirectory, project + "_");
        WebDriver webDriver = BrowserFactory.headlessFirefox(wx, wy);
        try {
            ScreenshotService service = new ScreenshotService(webDriver, new CookieConsentHandler(), area, RENDER_DELAY);
            for (String url : websites) {
                Path file = jpgWriter.write(service.capture(url));
                System.out.println("temp.saved: " + file);
            }
        } finally {
            webDriver.quit();
        }

        // 3. put all jpg files from the raw directory together in one pdf, including the metadata
        boolean createLandscape = width > height;
        String pdfName = project.replaceAll("_+$", "");
        PdfWriter pdfWriter = new PdfWriter(createLandscape);
        pdfWriter.setMetadata(title, author, creator, keywords);
        Path pdfFile = pdfWriter.write(outputDirectory, currentDirectory.resolve(pdfName + ".pdf"));
        System.out.println("saved: " + pdfFile);
        jpgWriter.deleteFiles();
        System.exit(0);
    }

    /**
     * Unclutter the arguments
     * @param args command line arguments
     */
    private static void unravelArguments(String[] args) {
        title = null;
        author = null;
        creator = null;
        keywords = null;
        width = Integer.parseInt(WIDTH);
        height = Integer.parseInt(HEIGHT);
        x = Integer.parseInt(START_X);
        y = Integer.parseInt(START_Y);
        wx = Integer.parseInt(WINDOW_WIDTH);
        wy = Integer.parseInt(WINDOW_HEIGHT);
        List<String> positional = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "-t", "-a", "-c", "-k", "-w", "-h", "-x", "-y", "-wx", "-wy" -> {
                    if (++i >= args.length) {
                        System.err.println("Missing value for option " + a);
                        printUsage();
                        System.exit(1);
                    }
                    String value = args[i].isBlank() ? null : args[i];
                    switch (a) {
                        case "-t" -> title = value;
                        case "-a" -> author = value;
                        case "-c" -> creator = value;
                        case "-w" -> width = Integer.parseInt(value == null ? WIDTH : value);
                        case "-h" -> height = Integer.parseInt(value == null ? HEIGHT : value);
                        case "-x" ->  x = Integer.parseInt(value == null ? START_X : value);
                        case "-y" ->  y = Integer.parseInt(value == null ? START_Y : value);
                        case "-wx" -> wx = Integer.parseInt(value == null ? WINDOW_WIDTH : value);
                        case "-wy" -> wy = Integer.parseInt(value == null ? WINDOW_HEIGHT : value);
                        default -> keywords = value;
                    }
                }
                default -> {
                    if (a.length() > 1 && a.startsWith("-")) {
                        System.err.println("Unknown option " + a);
                        printUsage();
                        System.exit(1);
                    }
                    positional.add(a);
                }
            }
        }
        if (positional.size() != 2) {
            printUsage();
            System.exit(positional.isEmpty() ? 0 : 1);
        }
        project = positional.get(0);
        websites = readWebsitesFromFile(positional.get(1));
    }

    private static void printUsage() {
        System.out.println("""
                Usage: java -jar screenshotter.jar [options] <project> <websites>

                  <project>   name of the project (used for the output file names)
                  <websites>  file with list of websites

                Options (all optional, placed before the two arguments above):
                  -t <title>
                  -a <author>
                  -c <creator> (original author)
                  -k <keywords>
                  -wx <width>    windows viewport width (default 3072)
                  -wy <height>   windows viewport height (default 1570)
                  -w <width>     width of the screenshot
                  -h <height>    height of the screen
                  -x <x>         starting left edge, measured from the left of the viewport
                  -y <y>         starting top edge, measured from the top of the viewport""");
    }
}
