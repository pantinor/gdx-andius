package andius.voronoi.nodes;

import java.util.Stack;

final public class Vertex extends Object implements ICoord {

    public static final float NaN = 0.0f / 0.0f;
    final public static Vertex VERTEX_AT_INFINITY = new Vertex(NaN, NaN);
    final private static Stack<Vertex> _pool = new Stack();

    private static Vertex create(float x, float y) {

        if (Double.isNaN(x) || Double.isNaN(y)) {
            return VERTEX_AT_INFINITY;
        }
        if (_pool.size() > 0) {

            return _pool.pop().init(x, y);
        } else {
            return new Vertex(x, y);
        }
    }
    private static int _nvertices = 0;
    private Point _coord;

    @Override
    public Point get_coord() {
        return _coord;
    }
    private int _vertexIndex;

    public int get_vertexIndex() {
        return _vertexIndex;
    }

    public Vertex(float x, float y) {
        init(x, y);
    }

    private Vertex init(float x, float y) {
        _coord = new Point(x, y);
        return this;
    }

    public void dispose() {
        _coord = null;
        _pool.push(this);
    }

    public void setIndex() {
        _vertexIndex = _nvertices++;
    }

    @Override
    public String toString() {
        return "Vertex (" + _vertexIndex + ")";
    }

    /**
     * This is the only way to make a Vertex
     *
     * @param halfedge0
     * @param halfedge1
     * @return
     *
     */
    public static Vertex intersect(Halfedge halfedge0, Halfedge halfedge1) {
        Edge edge0, edge1, edge;
        Halfedge halfedge;
        float determinant, intersectionX, intersectionY;
        boolean rightOfSite;

        edge0 = halfedge0.edge;
        edge1 = halfedge1.edge;
        if (edge0 == null || edge1 == null) {
            return null;
        }
        if (edge0.get_rightSite() == edge1.get_rightSite()) {
            return null;
        }

        determinant = edge0.a * edge1.b - edge0.b * edge1.a;
        if (-1.0e-10 < determinant && determinant < 1.0e-10) {
            // the edges are parallel
            return null;
        }

        intersectionX = (edge0.c * edge1.b - edge1.c * edge0.b) / determinant;
        intersectionY = (edge1.c * edge0.a - edge0.c * edge1.a) / determinant;

        if (Voronoi.compareByYThenX(edge0.get_rightSite(), edge1.get_rightSite()) < 0) {
            halfedge = halfedge0;
            edge = edge0;
        } else {
            halfedge = halfedge1;
            edge = edge1;
        }
        rightOfSite = intersectionX >= edge.get_rightSite().get_x();
        if ((rightOfSite && halfedge.leftRight == LR.LEFT)
                || (!rightOfSite && halfedge.leftRight == LR.RIGHT)) {
            return null;
        }

        return Vertex.create(intersectionX, intersectionY);
    }

    public float get_x() {
        return _coord.x;
    }

    public float get_y() {
        return _coord.y;
    }
}
