package andius.voronoi.groundshapes;


import andius.voronoi.nodes.Point;
import andius.voronoi.nodes.Rectangle;
import java.util.Random;

/**
 * A blob with eyes.
 */
public class Blob implements HeightAlgorithm {

    @Override
    public boolean isWater(Point point, Rectangle bounds, Random random) {
        Point p = new Point(2 * (point.x / bounds.width - 0.5f), 2f * (point.y / bounds.height - 0.5f));
        boolean eye1 = new Point(p.x - 0.2f, p.y / 2 + 0.2f).length() < 0.05f;
        boolean eye2 = new Point(p.x + 0.2f, p.y / 2 + 0.2f).length() < 0.05f;
        boolean body = p.length() < 0.8 - 0.18 * Math.sin(5 * Math.atan2(p.y, p.x));
        return !body || eye1 || eye2;
    }
}
