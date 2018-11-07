package andius.voronoi.graph;

import andius.VoronoiWorldScreen;
import andius.voronoi.groundshapes.HeightAlgorithm;
import andius.voronoi.nodes.Point;
import andius.voronoi.nodes.Rectangle;
import andius.voronoi.nodes.LineSegment;
import andius.voronoi.nodes.Voronoi;
import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.math.Matrix4;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class VoronoiRenderer implements MapRenderer {

    private final List<Edge> edges = new ArrayList();
    private final List<Corner> corners = new ArrayList();
    private final List<Center> centers = new ArrayList();
    private final Random r;
    private final float[] polygonVertices = new float[8];
    private final float[] triangleVertices = new float[6];
    private final ShapeRenderer shape;
    private final Rectangle bounds;
    private final com.badlogic.gdx.math.Rectangle viewBounds;
    private final Batch batch;

    protected Color OCEAN, RIVER, LAKE, BEACH;

    public VoronoiRenderer(Voronoi v, int numLloydRelaxations, Random r, HeightAlgorithm algorithm) {
        this.r = r;
        this.bounds = v.get_plotBounds();
        this.shape = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.viewBounds = new com.badlogic.gdx.math.Rectangle();

        for (int i = 0; i < numLloydRelaxations; i++) {
            ArrayList<Point> points = v.siteCoords();
            for (Point p : points) {
                ArrayList<Point> region = v.region(p);
                double x = 0;
                double y = 0;
                for (Point c : region) {
                    x += c.x;
                    y += c.y;
                }
                x /= region.size();
                y /= region.size();
                p.x = x;
                p.y = y;
            }
            v = new Voronoi(points, v.get_plotBounds());
        }

        buildGraph(v);
        improveCorners();

        assignCornerElevations(algorithm);
        assignOceanCoastAndLand();
        redistributeElevations(landCorners());
        assignPolygonElevations();

        calculateDownslopes();
        createRivers();
        assignCornerMoisture();
        redistributeMoisture(landCorners());
        assignPolygonMoisture();
        assignBiomes();

    }

    public Batch getBatch() {
        return batch;
    }

    public com.badlogic.gdx.math.Rectangle getViewBounds() {
        return viewBounds;
    }

    public Corner getCorner(int i) {
        return this.corners.get(i);
    }

    @Override
    public void setView(OrthographicCamera camera) {
        batch.setProjectionMatrix(camera.combined);
        shape.setProjectionMatrix(camera.combined);
        float width = camera.viewportWidth * camera.zoom;
        float height = camera.viewportHeight * camera.zoom;
        float w = width * Math.abs(camera.up.y) + height * Math.abs(camera.up.x);
        float h = height * Math.abs(camera.up.y) + width * Math.abs(camera.up.x);
        viewBounds.set(camera.position.x - w / 2, camera.position.y - h / 2, w, h);
    }

    @Override
    public void setView(Matrix4 projection, float x, float y, float width, float height) {
        batch.setProjectionMatrix(projection);
        shape.setProjectionMatrix(projection);
        viewBounds.set(x, y, width, height);
    }

    public void render(float delta, Corner current) {

        for (int i = 0; i < centers.size(); i++) {
            Center c = centers.get(i);
            if (viewBounds.contains((float) c.loc.x, (float) (c.loc.y))) {
                drawPolygon(delta, c, getColor(c.biome));
            }
        }

        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            float e1 = (float) (e.d0.loc.x + e.d1.loc.x) / 2;
            float e2 = (float) ((e.d0.loc.y + e.d1.loc.y) / 2);
            if (viewBounds.contains(e1, e2)) {
                if (false) {
                    Gdx.gl.glLineWidth(1);
                    this.shape.setColor(Color.YELLOW);
                    this.shape.begin(ShapeType.Line);
                    this.shape.line(
                            (float) e.d0.loc.x,
                            (float) ((e.d0.loc.y)),
                            (float) e.d1.loc.x,
                            (float) ((e.d1.loc.y)));
                    this.shape.end();
                }
                if (true && e.river > 0) {
                    Gdx.gl.glLineWidth(1 + (int) Math.sqrt(e.river * 2));
                    this.shape.setColor(RIVER);
                    this.shape.begin(ShapeType.Line);
                    this.shape.line(
                            (float) e.v0.loc.x,
                            (float) ((e.v0.loc.y)),
                            (float) e.v1.loc.x,
                            (float) ((e.v1.loc.y)));
                    this.shape.end();
                }
            }
        }

        for (int i = 0; i < corners.size(); i++) {
            Corner c = corners.get(i);
            if (c.portal != null) {
                if (viewBounds.contains((float) c.loc.x, (float) (c.loc.y))) {
                    batch.begin();
                    batch.draw(VoronoiWorldScreen.castle.getKeyFrame(delta, true),
                            (float) c.loc.x, (float) (c.loc.y), 11, 2, 24f, 24f, 0.6f, 0.6f, 90, false);
                    batch.end();
                }
            }
        }

        if (current != null) {
            //if (viewBounds.contains((float) current.loc.x, (float) (current.loc.y))) {
            //this.shape.setColor(Color.PINK);
            //this.shape.begin(ShapeType.Line);
            //this.shape.ellipse((float) current.loc.x, (float) current.loc.y, 6, 6);
            //this.shape.end();
            //}
        }
    }

    @Override
    public void render() {
    }

    @Override
    public void render(int[] layers) {
    }

    abstract protected Enum getBiome(Center p);

    abstract protected Color getColor(Enum biome);

    private void drawPolygon(float delta, Center c, Color color) {

        Gdx.gl.glLineWidth(1);
        this.shape.setColor(color);

        for (int i = 0; i < c.neighbors.size(); i++) {
            Center n = c.neighbors.get(i);
            Edge e = edgeWithCenters(c, n);

            if (e.v0 == null) {
                continue;
            }

            drawTriangle(delta, e.v0, e.v1, c);

        }

    }

    private void drawTriangle(float delta, Corner c1, Corner c2, Center center) {

        triangleVertices[0] = (float) center.loc.x;
        triangleVertices[1] = (float) (center.loc.y);
        triangleVertices[2] = (float) c1.loc.x;
        triangleVertices[3] = (float) (c1.loc.y);
        triangleVertices[4] = (float) c2.loc.x;
        triangleVertices[5] = (float) (c2.loc.y);

        this.shape.begin(ShapeType.Filled);
        this.shape.triangle(triangleVertices[0], triangleVertices[1], triangleVertices[2], triangleVertices[3], triangleVertices[4], triangleVertices[5]);
        this.shape.end();

    }

    private void buildGraph(Voronoi v) {

        final HashMap<Point, Center> pointCenterMap = new HashMap();
        final ArrayList<Point> points = v.siteCoords();

        points.stream().forEach((p) -> {
            Center c = new Center();
            c.loc = p;
            c.index = centers.size();
            centers.add(c);
            pointCenterMap.put(p, c);
        });

        centers.stream().forEach((c) -> {
            v.region(c.loc);
        });

        final ArrayList<andius.voronoi.nodes.Edge> libedges = v.edges();
        final HashMap<Integer, Corner> pointCornerMap = new HashMap();

        for (andius.voronoi.nodes.Edge libedge : libedges) {

            final LineSegment vEdge = libedge.voronoiEdge();
            final LineSegment dEdge = libedge.delaunayLine();

            final Edge edge = new Edge();
            edge.index = edges.size();
            edges.add(edge);

            edge.v0 = makeCorner(pointCornerMap, vEdge.p0);
            edge.v1 = makeCorner(pointCornerMap, vEdge.p1);
            edge.d0 = pointCenterMap.get(dEdge.p0);
            edge.d1 = pointCenterMap.get(dEdge.p1);

            // Centers point to edges. Corners point to edges.
            if (edge.d0 != null) {
                edge.d0.borders.add(edge);
            }
            if (edge.d1 != null) {
                edge.d1.borders.add(edge);
            }
            if (edge.v0 != null) {
                edge.v0.protrudes.add(edge);
            }
            if (edge.v1 != null) {
                edge.v1.protrudes.add(edge);
            }

            // Centers point to centers.
            if (edge.d0 != null && edge.d1 != null) {
                addToCenterList(edge.d0.neighbors, edge.d1);
                addToCenterList(edge.d1.neighbors, edge.d0);
            }

            // Corners point to corners
            if (edge.v0 != null && edge.v1 != null) {
                addToCornerList(edge.v0.adjacent, edge.v1);
                addToCornerList(edge.v1.adjacent, edge.v0);
            }

            // Centers point to corners
            if (edge.d0 != null) {
                addToCornerList(edge.d0.corners, edge.v0);
                addToCornerList(edge.d0.corners, edge.v1);
            }
            if (edge.d1 != null) {
                addToCornerList(edge.d1.corners, edge.v0);
                addToCornerList(edge.d1.corners, edge.v1);
            }

            // Corners point to centers
            if (edge.v0 != null) {
                addToCenterList(edge.v0.touches, edge.d0);
                addToCenterList(edge.v0.touches, edge.d1);
            }
            if (edge.v1 != null) {
                addToCenterList(edge.v1.touches, edge.d0);
                addToCenterList(edge.v1.touches, edge.d1);
            }
        }
    }

    private void addToCornerList(ArrayList<Corner> list, Corner c) {
        if (c != null && !list.contains(c)) {
            list.add(c);
        }
    }

    private void addToCenterList(ArrayList<Center> list, Center c) {
        if (c != null && !list.contains(c)) {
            list.add(c);
        }
    }

    private Corner makeCorner(HashMap<Integer, Corner> pointCornerMap, Point p) {
        if (p == null) {
            return null;
        }
        int index = (int) ((int) p.x + (int) (p.y) * bounds.width * 2);
        Corner c = pointCornerMap.get(index);
        if (c == null) {
            c = new Corner();
            c.loc = p;
            c.border = bounds.liesOnAxes(p);
            c.index = corners.size();
            corners.add(c);
            pointCornerMap.put(index, c);
        }
        return c;
    }

    private void improveCorners() {
        Point[] newP = new Point[corners.size()];
        for (Corner c : corners) {
            if (c.border) {
                newP[c.index] = c.loc;
            } else {
                double x = 0;
                double y = 0;
                for (Center center : c.touches) {
                    x += center.loc.x;
                    y += center.loc.y;
                }
                newP[c.index] = new Point(x / c.touches.size(), y / c.touches.size());
            }
        }
        corners.stream().forEach((c) -> {
            c.loc = newP[c.index];
        });
        edges.stream().filter((e) -> (e.v0 != null && e.v1 != null)).forEach((e) -> {
            e.setVornoi(e.v0, e.v1);
        });
    }

    private Edge edgeWithCenters(Center c1, Center c2) {
        for (int i = 0; i < c1.borders.size(); i++) {
            Edge e = c1.borders.get(i);
            if (e.d0 == c2 || e.d1 == c2) {
                return e;
            }
        }
        return null;
    }

    private void assignCornerElevations(HeightAlgorithm algorithm) {
        LinkedList<Corner> queue = new LinkedList();
        for (Corner c : corners) {
            c.water = algorithm.isWater(c.loc, bounds, r);
            if (c.border) {
                c.elevation = 0;
                queue.add(c);
            } else {
                c.elevation = Double.MAX_VALUE;
            }
        }

        while (!queue.isEmpty()) {
            Corner c = queue.pop();
            for (Corner a : c.adjacent) {
                double newElevation = 0.01 + c.elevation;
                if (!c.water && !a.water) {
                    newElevation += 1;
                }
                if (newElevation < a.elevation) {
                    a.elevation = newElevation;
                    queue.add(a);
                }
            }
        }
    }

    private void assignOceanCoastAndLand() {
        LinkedList<Center> queue = new LinkedList();
        final double waterThreshold = .3;
        for (final Center center : centers) {
            int numWater = 0;
            for (final Corner c : center.corners) {
                if (c.border) {
                    center.border = center.water = center.ocean = true;
                    queue.add(center);
                }
                if (c.water) {
                    numWater++;
                }
            }
            center.water = center.ocean || ((double) numWater / center.corners.size() >= waterThreshold);
        }
        while (!queue.isEmpty()) {
            final Center center = queue.pop();
            for (final Center n : center.neighbors) {
                if (n.water && !n.ocean) {
                    n.ocean = true;
                    queue.add(n);
                }
            }
        }
        for (Center center : centers) {
            boolean oceanNeighbor = false;
            boolean landNeighbor = false;
            for (Center n : center.neighbors) {
                oceanNeighbor |= n.ocean;
                landNeighbor |= !n.water;
            }
            center.coast = oceanNeighbor && landNeighbor;
        }

        for (Corner c : corners) {
            int numOcean = 0;
            int numLand = 0;
            for (Center center : c.touches) {
                numOcean += center.ocean ? 1 : 0;
                numLand += !center.water ? 1 : 0;
            }
            c.ocean = numOcean == c.touches.size();
            c.coast = numOcean > 0 && numLand > 0;
            c.water = c.border || ((numLand != c.touches.size()) && !c.coast);
        }
    }

    private ArrayList<Corner> landCorners() {
        final ArrayList<Corner> list = new ArrayList();
        for (Corner c : corners) {
            if (!c.ocean && !c.coast) {
                list.add(c);
            }
        }
        return list;
    }

    private void redistributeElevations(ArrayList<Corner> landCorners) {
        Collections.sort(landCorners, new Comparator<Corner>() {
            @Override
            public int compare(Corner o1, Corner o2) {
                if (o1.elevation > o2.elevation) {
                    return 1;
                } else if (o1.elevation < o2.elevation) {
                    return -1;
                }
                return 0;
            }
        });

        final double SCALE_FACTOR = 1.1;
        for (int i = 0; i < landCorners.size(); i++) {
            double y = (double) i / landCorners.size();
            double x = Math.sqrt(SCALE_FACTOR) - Math.sqrt(SCALE_FACTOR * (1 - y));
            x = Math.min(x, 1);
            landCorners.get(i).elevation = x;
        }

        for (Corner c : corners) {
            if (c.ocean || c.coast) {
                c.elevation = 0.0;
            }
        }
    }

    private void assignPolygonElevations() {
        for (Center center : centers) {
            double total = 0;
            for (Corner c : center.corners) {
                total += c.elevation;
            }
            center.elevation = total / center.corners.size();
        }
    }

    private void calculateDownslopes() {
        for (Corner c : corners) {
            Corner down = c;
            //System.out.println("ME: " + c.elevation);
            for (Corner a : c.adjacent) {
                //System.out.println(a.elevation);
                if (a.elevation <= down.elevation) {
                    down = a;
                }
            }
            c.downslope = down;
        }
    }

    private void createRivers() {
        for (int i = 0; i < bounds.width / 2; i++) {
            Corner c = corners.get(r.nextInt(corners.size()));
            if (c.ocean || c.elevation < 0.3 || c.elevation > 0.9) {
                continue;
            }
            // Bias rivers to go west: if (q.downslope.x > q.x) continue;
            while (!c.coast) {
                if (c == c.downslope) {
                    break;
                }
                Edge edge = lookupEdgeFromCorner(c, c.downslope);
                if (!edge.v0.water || !edge.v1.water) {
                    edge.river++;
                    c.river++;
                    c.downslope.river++;  // TODO: fix double count
                }
                c = c.downslope;
            }
        }
    }

    private Edge lookupEdgeFromCorner(Corner c, Corner downslope) {
        for (Edge e : c.protrudes) {
            if (e.v0 == downslope || e.v1 == downslope) {
                return e;
            }
        }
        return null;
    }

    private void assignCornerMoisture() {
        LinkedList<Corner> queue = new LinkedList();
        for (Corner c : corners) {
            if ((c.water || c.river > 0) && !c.ocean) {
                c.moisture = c.river > 0 ? Math.min(3.0, (0.2 * c.river)) : 1.0;
                queue.push(c);
            } else {
                c.moisture = 0.0;
            }
        }

        while (!queue.isEmpty()) {
            Corner c = queue.pop();
            for (Corner a : c.adjacent) {
                double newM = .9 * c.moisture;
                if (newM > a.moisture) {
                    a.moisture = newM;
                    queue.add(a);
                }
            }
        }

        // Salt water
        for (Corner c : corners) {
            if (c.ocean || c.coast) {
                c.moisture = 1.0;
            }
        }
    }

    private void redistributeMoisture(ArrayList<Corner> landCorners) {
        Collections.sort(landCorners, new Comparator<Corner>() {
            @Override
            public int compare(Corner o1, Corner o2) {
                if (o1.moisture > o2.moisture) {
                    return 1;
                } else if (o1.moisture < o2.moisture) {
                    return -1;
                }
                return 0;
            }
        });
        for (int i = 0; i < landCorners.size(); i++) {
            landCorners.get(i).moisture = (double) i / landCorners.size();
        }
    }

    private void assignPolygonMoisture() {
        for (Center center : centers) {
            double total = 0;
            for (Corner c : center.corners) {
                total += c.moisture;
            }
            center.moisture = total / center.corners.size();
        }
    }

    private void assignBiomes() {
        for (Center center : centers) {
            center.biome = getBiome(center);
        }
    }

    private boolean closeEnough(double d1, double d2, double diff) {
        return Math.abs(d1 - d2) <= diff;
    }

}
