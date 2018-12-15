package andius.voronoi.nodes;

public final class Circle extends Object {

    public Point center;
    public float radius;

    public Circle(float centerX, float centerY, float radius) {
        super();
        this.center = new Point(centerX, centerY);
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "Circle (center: " + center + "; radius: " + radius + ")";
    }
}
