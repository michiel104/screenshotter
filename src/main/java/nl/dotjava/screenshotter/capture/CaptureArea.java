package nl.dotjava.screenshotter.capture;

/**
 * rectangle of the given width and height, whose upper-left corner (x,y) is measured from the  upper-left corner of the
 * viewport. With x=0 and y=0 the capture starts at the start of the viewport
 */
public record CaptureArea(int x, int y, int width, int height) {

    public CaptureArea {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("x and y must not be negative: " + this);
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive: " + this);
        }
    }
}
