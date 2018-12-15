package andius.voronoi.nodes;

public class Rectangle {

    final public float x, y, width, height, right, bottom, left, top;

    public Rectangle(float x, float y, float width, float height) {
        left = this.x = x;
        top = this.y = y;
        this.width = width;
        this.height = height;
        right = x + width;
        bottom = y + height;
    }

    public boolean liesOnAxes(Point p) {
        return closeEnough(p.x, x, 1) || closeEnough(p.y, y, 1) || closeEnough(p.x, right, 1) || closeEnough(p.y, bottom, 1);
    }

    public boolean inBounds(Point p) {
        return inBounds(p.x, p.y);
    }

    public boolean inBounds(float x0, float y0) {
        return !(x0 < x || x0 > right || y0 < y || y0 > bottom);
    }

    public boolean closeEnough(float d1, float d2, float diff) {
        return Math.abs(d1 - d2) <= diff;
    }
}
