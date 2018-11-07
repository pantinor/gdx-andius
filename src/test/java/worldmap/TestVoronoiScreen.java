/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldmap;

import com.badlogic.gdx.Game;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.ConvexHull;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

public class TestVoronoiScreen extends MyScreenAdapter {

    static class MyGame extends Game {

        @Override
        public void create() {
            setScreen(new TestVoronoiScreen());
        }

    }

    BitmapFont text = new BitmapFont();
    FloatArray points;
    DelaunayTriangulator delaunay = new DelaunayTriangulator();
    ShortArray triangles;
    ArrayList<DelaunayCell> dCells = new ArrayList<>();

    float[] hull;
    Polygon hullPoly;

    boolean drawCircumcircle = false,
            drawCircumcenter = false,
            drawPoints = true,
            drawDelaunay = true,
            drawVoronoi = true,
            drawMidpoints = false,
            drawHull = false;

    public TestVoronoiScreen() {

        cam.position.x = Gdx.graphics.getWidth() / 2;
        cam.position.y = Gdx.graphics.getHeight() / 2;

        generateNewPoints(100);
    }

    public static void main(String[] args) throws Exception {
        new LwjglApplication(new MyGame());
    }

    private void generateNewPoints(int numPoints) {
        int pad = 200;//distance away from edge of screen
        points = new FloatArray();
        for (int i = 0; i < numPoints * 2; i += 2) {
            float x = MathUtils.random(pad, Gdx.graphics.getWidth() - pad);
            float y = MathUtils.random(pad, Gdx.graphics.getHeight() - pad);
            points.add(x);
            points.add(y);
        }
        calculateDelaunay();
    }

    private void calculateDelaunay() {
        triangles = delaunay.computeTriangles(points, false);

        dCells.clear();
        for (int i = 0; i < triangles.size; i += 3) {
            int p1 = triangles.get(i) * 2;
            int p2 = triangles.get(i + 1) * 2;
            int p3 = triangles.get(i + 2) * 2;
            Vector2 a = new Vector2(points.get(p1), points.get(p1 + 1));
            Vector2 b = new Vector2(points.get(p2), points.get(p2 + 1));
            Vector2 c = new Vector2(points.get(p3), points.get(p3 + 1));

            DelaunayCell d = new DelaunayCell(a, b, c);
            dCells.add(d);
        }

        findNeighbors(dCells);

        ConvexHull convex = new ConvexHull();
        hull = convex.computePolygon(points, false).toArray();
        hullPoly = new Polygon(hull);

    }

    private boolean collideWithHull(Vector2 a, Vector2 b, Vector2 intersect) {
        float[] verticies = hullPoly.getTransformedVertices();
        for (int v = 0; v < verticies.length - 2; v += 2) {
            float xA = verticies[v];
            float yA = verticies[v + 1];
            float xB = verticies[v + 2];
            float yB = verticies[v + 3];

            Vector2 edgeA = new Vector2(xA, yA);
            Vector2 edgeB = new Vector2(xB, yB);

            if (Intersector.intersectSegments(edgeA, edgeB, a, b, intersect)) {
                return true;
            }

        }
        return false;
    }

    private void drawCellEdge(DelaunayCell cellA, DelaunayCell cellB) {
        if (hullPoly.contains(cellA.circumcenter)) {
            if (cellB != null) {
                shape.setColor(Color.ORANGE);
                if (hullPoly.contains(cellB.circumcenter)) {
                    shape.line(cellA.circumcenter, cellB.circumcenter);
                } else {
                    Vector2 intersect = new Vector2();
                    if (collideWithHull(cellA.circumcenter, cellB.circumcenter, intersect)) {
                        shape.line(cellA.circumcenter, intersect);
                        shape.circle(intersect.x, intersect.y, 8);//show point of intersection
                    }
                }

            } else {
                shape.setColor(Color.CYAN);
                shape.line(cellA.circumcenter, cellA.midAB);
                shape.line(cellA.circumcenter, cellA.midBC);
                shape.line(cellA.circumcenter, cellA.midCA);
                shape.circle(cellA.midAB.x, cellA.midAB.y, 8);
                shape.circle(cellA.midBC.x, cellA.midBC.y, 8);
                shape.circle(cellA.midCA.x, cellA.midCA.y, 8);
            }

        } else {

            float[] verticies = hullPoly.getTransformedVertices();
            for (int v = 0; v < verticies.length - 2; v += 2) {
                float xA = verticies[v];
                float yA = verticies[v + 1];
                float xB = verticies[v + 2];
                float yB = verticies[v + 3];

                Vector2 edgeA = new Vector2(xA, yA);
                Vector2 edgeB = new Vector2(xB, yB);

                drawIntersectingLines(cellA, cellA.midAB, edgeA, edgeB);
                drawIntersectingLines(cellA, cellA.midBC, edgeA, edgeB);
                drawIntersectingLines(cellA, cellA.midCA, edgeA, edgeB);
            }
        }
    }

    private void drawIntersectingLines(DelaunayCell cell, Vector2 mid, Vector2 edgeA, Vector2 edgeB) {
        Vector2 intersect = new Vector2();
        if (Intersector.intersectSegments(edgeA, edgeB, cell.circumcenter, mid, intersect)) {
            shape.setColor(Color.GREEN);
            shape.line(mid, intersect);
            shape.circle(intersect.x, intersect.y, 3);
        }
    }

    @Override
    public void render(float delta) {

        super.render(delta);

        Gdx.gl20.glClearColor(1, 1, 1, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawStuff();

        updateControls();

        drawMenu();
    }

    private void drawStuff() {
        shape.begin(ShapeType.Line);

        int pSize = 6;
        for (DelaunayCell d : dCells) {
            if (drawPoints) {
                shape.setColor(Color.BLACK);
                shape.circle(d.a.x, d.a.y, pSize);
                shape.circle(d.b.x, d.b.y, pSize);
                shape.circle(d.c.x, d.c.y, pSize);
            }

            if (drawDelaunay) {
                shape.setColor(Color.GRAY);
                shape.triangle(d.a.x, d.a.y, // A
                        d.b.x, d.b.y, // B
                        d.c.x, d.c.y);// C
            }

            if (drawMidpoints) {
                shape.setColor(Color.BLUE);
                shape.circle(d.midAB.x, d.midAB.y, 1);
                shape.circle(d.midCA.x, d.midCA.y, 2);
                shape.circle(d.midBC.x, d.midBC.y, 3);
            }

            if (drawVoronoi) {
                drawCellEdge(d, d.nAB);
                drawCellEdge(d, d.nBC);
                drawCellEdge(d, d.nBC);
            }

            if (!hullPoly.contains(d.circumcenter)) {
                shape.setColor(Color.MAGENTA);
            } else {
                shape.setColor(Color.GREEN);
            }
            if (drawCircumcircle) {
                shape.circle(d.circumcenter.x, d.circumcenter.y, d.circumradius);
            }
            if (drawCircumcenter) {
                shape.circle(d.circumcenter.x, d.circumcenter.y, pSize);
            }
        }

        if (drawHull) {
            shape.setColor(Color.RED);
            shape.polyline(hullPoly.getVertices());
        }

        shape.end();
    }

    private void drawMenu() {
        batch.begin();
        int y = Gdx.graphics.getHeight() - 10;
        float h = text.getLineHeight();
        text.setColor(drawCircumcenter ? Color.GREEN : Color.BLACK);
        text.draw(batch, "1: Circumcenter", 10, y);

        text.setColor(drawCircumcircle ? Color.GREEN : Color.BLACK);
        text.draw(batch, "2: Circumcircle", 10, y - h * 1);

        text.setColor(drawPoints ? Color.GREEN : Color.BLACK);
        text.draw(batch, "3: Points  ", 10, y - h * 2);

        text.setColor(drawMidpoints ? Color.GREEN : Color.BLACK);
        text.draw(batch, "4: Mid points  ", 10, y - h * 3);

        text.setColor(drawVoronoi ? Color.GREEN : Color.BLACK);
        text.draw(batch, "5: Voronoi     ", 10, y - h * 4);

        text.setColor(drawDelaunay ? Color.GREEN : Color.BLACK);
        text.draw(batch, "6: Delaunay    ", 10, y - h * 5);

        text.setColor(drawHull ? Color.GREEN : Color.BLACK);
        text.draw(batch, "7: Hull        ", 10, y - h * 6);
        batch.end();
    }

    private void updateControls() {
        if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            generateNewPoints(3);
        }

        if (Gdx.input.justTouched() && Gdx.input.isButtonPressed(Buttons.LEFT)) {
            points.add(Gdx.input.getX());
            points.add(Gdx.graphics.getHeight() - Gdx.input.getY());
            calculateDelaunay();
        }

        if (Gdx.input.isButtonPressed(Buttons.RIGHT)) {
            int x = Gdx.input.getX();
            int y = Gdx.graphics.getHeight() - Gdx.input.getY();
            boolean mod = false;

            for (int i = 0; i < points.size && !mod; i += 2) {
                float px = points.get(i);
                float py = points.get(i + 1);

                if (Vector2.dst(x, y, px, py) < 20) {
                    points.set(i, x);
                    points.set(i + 1, y);
                    mod = true;
                }
            }

            if (mod) {
                calculateDelaunay();
            }
        }

        if (Gdx.input.isKeyJustPressed(Keys.NUM_1)) {
            drawCircumcenter = !drawCircumcenter;
        }
        if (Gdx.input.isKeyJustPressed(Keys.NUM_2)) {
            drawCircumcircle = !drawCircumcircle;
        }
        if (Gdx.input.isKeyJustPressed(Keys.NUM_3)) {
            drawPoints = !drawPoints;
        }
        if (Gdx.input.isKeyJustPressed(Keys.NUM_4)) {
            drawMidpoints = !drawMidpoints;
        }
        if (Gdx.input.isKeyJustPressed(Keys.NUM_5)) {
            drawVoronoi = !drawVoronoi;
        }
        if (Gdx.input.isKeyJustPressed(Keys.NUM_6)) {
            drawDelaunay = !drawDelaunay;
        }
        if (Gdx.input.isKeyJustPressed(Keys.NUM_7)) {
            drawHull = !drawHull;
        }
    }

    class VoronoiCell {

        Polygon poly;
        FloatArray verticies = new FloatArray();

        public void addVertex(Vector2 point) {
            verticies.add(point.x);
            verticies.add(point.y);
        }

        public void setVerticies() {
            ConvexHull convex = new ConvexHull();
            FloatArray hull = convex.computePolygon(verticies, false);
            poly.setVertices(hull.toArray());
        }

    }

    class DelaunayCell {

        Vector2 a, b, c;//vertex that define triangle
        Vector2 midAB, midBC, midCA;//midpoints between vertex
        DelaunayCell nAB, nBC, nCA;//neighbors (TODO: reference for now, index later)
        Vector2 circumcenter;//center of circle that intersects each vertex a,b,c
        float circumradius;//radius of circle that intersects each vertex a,b,c

        public DelaunayCell(Vector2 a, Vector2 b, Vector2 c) {
            this.a = a;
            this.b = b;
            this.c = c;

            midAB = a.cpy().add(b).scl(0.5f);
            midBC = b.cpy().add(c).scl(0.5f);
            midCA = c.cpy().add(a).scl(0.5f);

            Vector3 circle = circumcircle2(a, b, c);
            circumcenter = new Vector2(circle.x, circle.y);
            circumradius = circle.z;
        }

    }

    private static boolean sharesMidpoint(Vector2 midpoint, DelaunayCell other) {
        float epsilon = 0.01f;//error margin
        return midpoint.epsilonEquals(other.midAB, epsilon)
                || midpoint.epsilonEquals(other.midBC, epsilon)
                || midpoint.epsilonEquals(other.midCA, epsilon);
    }

    private static boolean isNeighbor(DelaunayCell cellA, DelaunayCell cellB) {
        if (sharesMidpoint(cellA.midAB, cellB)) {
            cellA.nAB = cellB;
            return true;
        }

        if (sharesMidpoint(cellA.midBC, cellB)) {
            cellA.nBC = cellB;
            return true;
        }

        if (sharesMidpoint(cellA.midCA, cellB)) {
            cellA.nCA = cellB;
            return true;
        }

        return false;
    }

    private static void findNeighbors(ArrayList<DelaunayCell> dCells) {
        for (DelaunayCell cellA : dCells) {
            for (DelaunayCell cellB : dCells) {
                if (cellA.circumcenter.epsilonEquals(cellB.circumcenter, 0.01f)) {
                    continue;
                }
                isNeighbor(cellA, cellB);
            }
        }
    }

    private static Vector3 circumcircle2(Vector2 a, Vector2 b, Vector2 c) {
        float EPSILON = 1.0f / 1048576.0f;

        float fabsy1y2 = Math.abs(a.y - b.y),
                fabsy2y3 = Math.abs(b.y - c.y),
                xc, yc, m1, m2, mx1, mx2, my1, my2, dx, dy;

        if (fabsy1y2 < EPSILON) {
            m2 = -((c.x - b.x) / (c.y - b.y));
            mx2 = (b.x + c.x) / 2.0f;
            my2 = (b.y + c.y) / 2.0f;
            xc = (b.x + a.x) / 2.0f;
            yc = m2 * (xc - mx2) + my2;
        } else if (fabsy2y3 < EPSILON) {
            m1 = -((b.x - a.x) / (b.y - a.y));
            mx1 = (a.x + b.x) / 2.0f;
            my1 = (a.y + b.y) / 2.0f;
            xc = (c.x + b.x) / 2.0f;
            yc = m1 * (xc - mx1) + my1;
        } else {
            m1 = -((b.x - a.x) / (b.y - a.y));
            m2 = -((c.x - b.x) / (c.y - b.y));
            mx1 = (a.x + b.x) / 2.0f;
            mx2 = (b.x + c.x) / 2.0f;
            my1 = (a.y + b.y) / 2.0f;
            my2 = (b.y + c.y) / 2.0f;
            xc = (m1 * mx1 - m2 * mx2 + my2 - my1) / (m1 - m2);
            yc = (fabsy1y2 > fabsy2y3) ? m1 * (xc - mx1) + my1 : m2 * (xc - mx2) + my2;
        }

        dx = b.x - xc;
        dy = b.y - yc;
        float radius = (float) Math.sqrt(dx * dx + dy * dy);
        return new Vector3(xc, yc, radius);
    }

}
