package andius.voronoi.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public final class Site implements ICoord {

    private final static Stack<Site> _pool = new Stack();

    private int _siteIndex;
    public ArrayList<Edge> _edges;
    private ArrayList<LR> _edgeOrientations;
    public Polygon region;
    private Point _coord;
    public int pointIndex;

    final private static float EPSILON = .005f;
    final private static int TOP = 1;
    final private static int BOTTOM = 2;
    final private static int LEFT = 4;
    final private static int RIGHT = 8;

    public Site(Point p, int index) {
        init(p, index);
    }

    private Site init(Point p, int index) {
        _coord = p;
        _siteIndex = index;
        _edges = new ArrayList();
        return this;
    }

    public static Site create(Point p, int index) {
        if (_pool.size() > 0) {
            return _pool.pop().init(p, index);
        } else {
            return new Site(p, index);
        }
    }

    public static void sortSites(ArrayList<Site> sites) {
        //sites.sort(Site.compare);
        Collections.sort(sites, new Comparator<Site>() {
            @Override
            public int compare(Site o1, Site o2) {
                return (int) Site.compare(o1, o2);
            }
        });
    }

    /**
     * sort sites on y, then x, coord also change each site's _siteIndex to
     * match its new position in the list so the _siteIndex can be used to
     * identify the site for nearest-neighbor queries
     */
    private static double compare(Site s1, Site s2) {
        int returnValue = Voronoi.compareByYThenX(s1, s2);

        // swap _siteIndex values if necessary to match new ordering:
        int tempIndex;
        if (returnValue == -1) {
            if (s1._siteIndex > s2._siteIndex) {
                tempIndex = s1._siteIndex;
                s1._siteIndex = s2._siteIndex;
                s2._siteIndex = tempIndex;
            }
        } else if (returnValue == 1) {
            if (s2._siteIndex > s1._siteIndex) {
                tempIndex = s2._siteIndex;
                s2._siteIndex = s1._siteIndex;
                s1._siteIndex = tempIndex;
            }

        }

        return returnValue;
    }

    private static boolean closeEnough(Point p0, Point p1) {
        return Point.distance(p0, p1) < EPSILON;
    }

    @Override
    public Point get_coord() {
        return _coord;
    }

    @Override
    public String toString() {
        return "Site " + _siteIndex + ": " + get_coord();
    }

    public void dispose() {
        _coord = null;
        clear();
        _pool.push(this);
    }

    private void clear() {
        if (_edges != null) {
            _edges.clear();
            _edges = null;
        }
        if (_edgeOrientations != null) {
            _edgeOrientations.clear();
            _edgeOrientations = null;
        }
        region = null;
    }

    void addEdge(Edge edge) {
        _edges.add(edge);
    }

    public Edge nearestEdge() {
        Collections.sort(_edges, new Comparator<Edge>() {
            @Override
            public int compare(Edge o1, Edge o2) {
                return (int) Edge.compareSitesDistances(o1, o2);
            }
        });
        return _edges.get(0);
    }

    ArrayList<Site> neighborSites() {
        if (_edges == null || _edges.isEmpty()) {
            return new ArrayList();
        }
        if (_edgeOrientations == null) {
            reorderEdges();
        }
        ArrayList<Site> list = new ArrayList();
        for (Edge edge : _edges) {
            list.add(neighborSite(edge));
        }
        return list;
    }

    private Site neighborSite(Edge edge) {
        if (this == edge.get_leftSite()) {
            return edge.get_rightSite();
        }
        if (this == edge.get_rightSite()) {
            return edge.get_leftSite();
        }
        return null;
    }

    List<Point> region(Rectangle clippingBounds) {
        if (_edges == null || _edges.isEmpty()) {
            return null;
        }
        if (_edgeOrientations == null) {
            reorderEdges();
            List<Point> pts = clipToBounds(clippingBounds);
            region = new Polygon(pts);
            return pts;
        } else {
            return null;
        }
    }

    private void reorderEdges() {
        EdgeReorderer reorderer = new EdgeReorderer(_edges, Vertex.class);
        _edges = reorderer.get_edges();
        _edgeOrientations = reorderer.get_edgeOrientations();
        reorderer.dispose();
    }

    private ArrayList<Point> clipToBounds(Rectangle bounds) {
        ArrayList<Point> points = new ArrayList();
        int n = _edges.size();
        int i = 0;
        Edge edge;
        while (i < n && (_edges.get(i).get_visible() == false)) {
            ++i;
        }

        if (i == n) {
            // no edges visible
            return new ArrayList();
        }
        edge = _edges.get(i);
        LR orientation = _edgeOrientations.get(i);
        points.add(edge.get_clippedEnds().get(orientation));
        points.add(edge.get_clippedEnds().get((LR.other(orientation))));

        for (int j = i + 1; j < n; ++j) {
            edge = _edges.get(j);
            if (edge.get_visible() == false) {
                continue;
            }
            connect(points, j, bounds, false);
        }
        // close up the polygon by adding another corner point of the bounds if needed:
        connect(points, i, bounds, true);

        return points;
    }

    private void connect(ArrayList<Point> points, int j, Rectangle bounds, boolean closingUp) {
        Point rightPoint = points.get(points.size() - 1);
        Edge newEdge = _edges.get(j);
        LR newOrientation = _edgeOrientations.get(j);
        // the point that  must be connected to rightPoint:
        Point newPoint = newEdge.get_clippedEnds().get(newOrientation);
        if (!closeEnough(rightPoint, newPoint)) {
            // The points do not coincide, so they must have been clipped at the bounds;
            // see if they are on the same border of the bounds:
            if (rightPoint.x != newPoint.x
                    && rightPoint.y != newPoint.y) {
                // They are on different borders of the bounds;
                // insert one or two corners of bounds as needed to hook them up:
                // (NOTE this will not be correct if the region should take up more than
                // half of the bounds rect, for then we will have gone the wrong way
                // around the bounds and included the smaller part rather than the larger)
                int rightCheck = boundsCheck(rightPoint, bounds);
                int newCheck = boundsCheck(newPoint, bounds);
                float px, py;
                if ((rightCheck & RIGHT) != 0) {
                    px = bounds.right;
                    if ((newCheck & BOTTOM) != 0) {
                        py = bounds.bottom;
                        points.add(new Point(px, py));
                    } else if ((newCheck & TOP) != 0) {
                        py = bounds.top;
                        points.add(new Point(px, py));
                    } else if ((newCheck & LEFT) != 0) {
                        if (rightPoint.y - bounds.y + newPoint.y - bounds.y < bounds.height) {
                            py = bounds.top;
                        } else {
                            py = bounds.bottom;
                        }
                        points.add(new Point(px, py));
                        points.add(new Point(bounds.left, py));
                    }
                } else if ((rightCheck & LEFT) != 0) {
                    px = bounds.left;
                    if ((newCheck & BOTTOM) != 0) {
                        py = bounds.bottom;
                        points.add(new Point(px, py));
                    } else if ((newCheck & TOP) != 0) {
                        py = bounds.top;
                        points.add(new Point(px, py));
                    } else if ((newCheck & RIGHT) != 0) {
                        if (rightPoint.y - bounds.y + newPoint.y - bounds.y < bounds.height) {
                            py = bounds.top;
                        } else {
                            py = bounds.bottom;
                        }
                        points.add(new Point(px, py));
                        points.add(new Point(bounds.right, py));
                    }
                } else if ((rightCheck & TOP) != 0) {
                    py = bounds.top;
                    if ((newCheck & RIGHT) != 0) {
                        px = bounds.right;
                        points.add(new Point(px, py));
                    } else if ((newCheck & LEFT) != 0) {
                        px = bounds.left;
                        points.add(new Point(px, py));
                    } else if ((newCheck & BOTTOM) != 0) {
                        if (rightPoint.x - bounds.x + newPoint.x - bounds.x < bounds.width) {
                            px = bounds.left;
                        } else {
                            px = bounds.right;
                        }
                        points.add(new Point(px, py));
                        points.add(new Point(px, bounds.bottom));
                    }
                } else if ((rightCheck & BOTTOM) != 0) {
                    py = bounds.bottom;
                    if ((newCheck & RIGHT) != 0) {
                        px = bounds.right;
                        points.add(new Point(px, py));
                    } else if ((newCheck & LEFT) != 0) {
                        px = bounds.left;
                        points.add(new Point(px, py));
                    } else if ((newCheck & TOP) != 0) {
                        if (rightPoint.x - bounds.x + newPoint.x - bounds.x < bounds.width) {
                            px = bounds.left;
                        } else {
                            px = bounds.right;
                        }
                        points.add(new Point(px, py));
                        points.add(new Point(px, bounds.top));
                    }
                }
            }
            if (closingUp) {
                // newEdge's ends have already been added
                return;
            }
            points.add(newPoint);
        }
        Point newRightPoint = newEdge.get_clippedEnds().get(LR.other(newOrientation));
        if (!closeEnough(points.get(0), newRightPoint)) {
            points.add(newRightPoint);
        }
    }

    public float get_x() {
        return _coord.x;
    }

    public float get_y() {
        return _coord.y;
    }

    public float dist(ICoord p) {
        return Point.distance(p.get_coord(), this._coord);
    }

    public static int boundsCheck(Point point, Rectangle bounds) {
        int value = 0;
        if (point.x == bounds.left) {
            value |= LEFT;
        }
        if (point.x == bounds.right) {
            value |= RIGHT;
        }
        if (point.y == bounds.top) {
            value |= TOP;
        }
        if (point.y == bounds.bottom) {
            value |= BOTTOM;
        }
        return value;
    }
}
