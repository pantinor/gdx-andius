package andius.voronoi.nodes;

public class Point {

    public static float distance(Point _coord, Point _coord0) {
        return (float) Math.sqrt((_coord.x - _coord0.x) * (_coord.x - _coord0.x) + (_coord.y - _coord0.y) * (_coord.y - _coord0.y));
    }
    
    public static float distance(Point _coord, float x, float y) {
        return (float) Math.sqrt((_coord.x - x) * (_coord.x - x) + (_coord.y - y) * (_coord.y - y));
    }
    
    public float x, y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return x + ", " + y;
    }

    public float l2() {
        return x * x + y * y;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }
}
