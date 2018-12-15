package andius.voronoi.nodes;

import java.util.List;

public final class Polygon {

    private final List<Point> verticesList;
    final float[] vertices;

    public Polygon(List<Point> v) {
        this.verticesList = v;
        this.vertices = new float[verticesList.size() * 2];
        int idx = 0;
        for (Point p : v) {
            this.vertices[idx] = p.x;
            this.vertices[idx + 1] = p.y;
            idx += 2;
        }
    }

    public float area() {
        return (float) Math.abs(signedDoubleArea() * 0.5);
    }

    public boolean contains(float x, float y) {
        final int numFloats = vertices.length;
        int intersects = 0;

        for (int i = 0; i < numFloats; i += 2) {
            float x1 = vertices[i];
            float y1 = vertices[i + 1];
            float x2 = vertices[(i + 2) % numFloats];
            float y2 = vertices[(i + 3) % numFloats];
            if (((y1 <= y && y < y2) || (y2 <= y && y < y1)) && x < ((x2 - x1) / (y2 - y1) * (y - y1) + x1)) {
                intersects++;
            }
        }
        return (intersects & 1) == 1;
    }
    
    public float[] vertices() {
        return this.vertices;
    }

    public Winding winding() {
        double signedDoubleArea = signedDoubleArea();
        if (signedDoubleArea < 0) {
            return Winding.CLOCKWISE;
        }
        if (signedDoubleArea > 0) {
            return Winding.COUNTERCLOCKWISE;
        }
        return Winding.NONE;
    }

    private float signedDoubleArea() {
        int index, nextIndex;
        int n = verticesList.size();
        Point point, next;
        float signedDoubleArea = 0;
        for (index = 0; index < n; ++index) {
            nextIndex = (index + 1) % n;
            point = verticesList.get(index);
            next = verticesList.get(nextIndex);
            signedDoubleArea += point.x * next.y - next.x * point.y;
        }
        return signedDoubleArea;
    }
}
