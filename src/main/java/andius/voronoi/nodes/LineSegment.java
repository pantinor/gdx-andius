package andius.voronoi.nodes;

public final class LineSegment extends Object {

    public static float compareLengths_MAX(LineSegment segment0, LineSegment segment1) {
        float length0 = Point.distance(segment0.p0, segment0.p1);
        float length1 = Point.distance(segment1.p0, segment1.p1);
        if (length0 < length1) {
            return 1;
        }
        if (length0 > length1) {
            return -1;
        }
        return 0;
    }

    public static float compareLengths(LineSegment edge0, LineSegment edge1) {
        return -compareLengths_MAX(edge0, edge1);
    }
    public Point p0, p1;

    public LineSegment(Point p0, Point p1) {
        super();
        this.p0 = p0;
        this.p1 = p1;
    }
}
