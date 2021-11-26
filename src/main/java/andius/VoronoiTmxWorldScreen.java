package andius;

import static andius.Andius.mapAtlas;
import static andius.Constants.MOON_PHASES;
import static andius.Constants.MOON_SECONDS_PER_PHASE;
import static andius.Constants.WORLD_TILE_DIM;
import andius.objects.Portal;
import andius.voronoi.graph.Center;
import andius.voronoi.graph.Corner;
import andius.voronoi.graph.Edge;
import andius.voronoi.nodes.LineSegment;
import andius.voronoi.nodes.Point;
import andius.voronoi.nodes.Rectangle;
import andius.voronoi.nodes.Voronoi;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class VoronoiTmxWorldScreen extends BaseScreen {

    private final List<Cell> cells;
    private final java.util.Map<Constants.Moongate, Cell> moongateMap = new HashMap<>();

    private final float factor = 1.00f;

    private final Viewport mapViewPort;
    private final Batch mapBatch, batch;
    private Center currentLocation;
    private final VoronoiRenderer renderer;

    public static Animation castle;
    private final TextureAtlas moonPhaseAtlas;
    private static int phaseIndex = 0, phaseCount = 0, trammelphase = 0, trammelSubphase = 0, feluccaphase = 0;
    public GameTimer gameTimer = new GameTimer();
    private static final Random RANDOM = new Random();

    public VoronoiTmxWorldScreen() {

        String spoints = "";
        String scells = null;
        String sportals = null;
        String smoongates = null;

        try {
            InputStream fstream = Gdx.files.classpath("assets/azgaarMaps/u4-voronoi-world.data").read();
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String line = br.readLine();
            while (!line.equals("CELLS")) {
                line = br.readLine();
                if (!line.equals("CELLS")) {
                    spoints += line;
                }
            }
            scells = br.readLine();
            line = br.readLine();
            sportals = br.readLine();
            line = br.readLine();
            smoongates = br.readLine();

        } catch (Exception e) {
            e.printStackTrace();
        }

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        List<Float[]> pts = gson.fromJson(spoints, new TypeToken<java.util.List<Float[]>>() {
        }.getType());
        cells = gson.fromJson(scells, new TypeToken<java.util.List<Cell>>() {
        }.getType());

        List<PortalBean> portals = gson.fromJson(sportals, new TypeToken<java.util.List<PortalBean>>() {
        }.getType());
        List<MGBean> moongates = gson.fromJson(smoongates, new TypeToken<java.util.List<MGBean>>() {
        }.getType());

        Rectangle rect = new Rectangle(1, 1, 1, 1);
        for (PortalBean p : portals) {
            for (Cell cell : cells) {
                rect.set(cell.x - 1, cell.y - 1, 3, 3);
                if (rect.inBounds(p.x, p.y)) {
                    Constants.Map map = Constants.Map.LLECHY;
                    try {
                        map = Constants.Map.valueOf(p.name);
                    } catch (Exception e) {
                        map = Constants.Map.MENAGERIE;
                    }
                    cell.portal = new Portal(map, 0, 0, 15, 31, null, false, false);
                    break;
                }
            }
        }

        for (MGBean m : moongates) {
            for (Cell cell : cells) {
                rect.set(cell.x - 1, cell.y - 1, 3, 3);
                if (rect.inBounds(m.x, m.y)) {
                    moongateMap.put(Moongate.values()[m.id], cell);
                    break;
                }
            }
        }

        for (Cell c : cells) {
            c.x *= factor;
            c.y *= factor;
        }

        renderer = new VoronoiRenderer(pts);
        mapBatch = renderer.getBatch();

        castle = new Animation(.4f, mapAtlas.findRegions("castle_gray"));

        batch = new SpriteBatch();
        stage = new Stage(viewport);

        moonPhaseAtlas = new TextureAtlas(Gdx.files.classpath("assets/data/moon-atlas.txt"));

        SequenceAction seq1 = Actions.action(SequenceAction.class);
        seq1.addAction(Actions.delay(.25f));
        seq1.addAction(Actions.run(gameTimer));
        stage.addAction(Actions.forever(seq1));

        camera = new OrthographicCamera(Andius.MAP_VIEWPORT_DIM + 128, Andius.MAP_VIEWPORT_DIM + 128);
        camera.setToOrtho(true);

        mapViewPort = new ScreenViewport(camera);

        currentLocation = renderer.getCenter(37191);
        newMapPixelCoords.set((float) currentLocation.loc.x, (float) currentLocation.loc.y, 0f);

    }

    @Override
    public void show() {
        gameTimer.active = true;
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
    }

    @Override
    public void hide() {
        gameTimer.active = false;
    }

    @Override
    public void setMapPixelCoords(Vector3 v, int x, int y) {
    }

    @Override
    public void setCurrentMapCoords(Vector3 v) {
    }

    public class GameTimer implements Runnable {

        public boolean active = true;

        @Override
        public void run() {
            if (active) {

                updateMoons();

            }
        }
    }

    @Override
    public void render(float delta) {

        time += delta;

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glLineWidth(1);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        if (renderer == null) {
            return;
        }

        camera.position.set(
                (float) this.newMapPixelCoords.x + 167,
                (float) (this.newMapPixelCoords.y + 38),
                0);

        camera.update();

        renderer.setView(camera.combined,
                camera.position.x - 536,
                camera.position.y - 396,
                Andius.MAP_VIEWPORT_DIM + 128,
                Andius.MAP_VIEWPORT_DIM + 128);

        renderer.render(time, this.currentLocation);

        for (Constants.Moongate g : Constants.Moongate.values()) {
            TextureRegion t = g.getCurrentTexture();
            if (t != null) {
                Cell c = moongateMap.get(g);
                mapBatch.begin();
                mapBatch.draw(t, c.x, c.y, 11, 2, 24f, 24f, 1.0f, 1.0f, 90, false);
                mapBatch.end();
            }
        }
        batch.begin();

        batch.draw(Andius.backGround, 0, 0);
        batch.draw(Andius.world_scr_avatar.getKeyFrame(time, true), WORLD_TILE_DIM * 14, WORLD_TILE_DIM * 17);
        batch.draw(moonPhaseAtlas.findRegion("PHASE_" + trammelphase), 348, Andius.SCREEN_HEIGHT - 32, 20, 20);
        batch.draw(moonPhaseAtlas.findRegion("PHASE_" + feluccaphase), 372, Andius.SCREEN_HEIGHT - 32, 20, 20);

        Andius.HUD.render(batch, Andius.CTX);

        Andius.font.draw(batch, String.format("%s\n", this.currentLocation), 200, Andius.SCREEN_HEIGHT - 32);

        batch.end();

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        mapViewPort.update(width, height, false);
    }

    @Override
    public boolean keyUp(int keycode) {
        Center current = this.currentLocation;

        if (keycode == Input.Keys.UP || keycode == Input.Keys.NUMPAD_8) {
            move(current, Direction.NORTH);
        } else if (keycode == Input.Keys.DOWN || keycode == Input.Keys.NUMPAD_2) {
            move(current, Direction.SOUTH);
        } else if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.NUMPAD_6) {
            move(current, Direction.EAST);
        } else if (keycode == Input.Keys.LEFT || keycode == Input.Keys.NUMPAD_4) {
            move(current, Direction.WEST);
        } else if (keycode == Input.Keys.NUMPAD_7) {
            move(current, Direction.NORTH_WEST);
        } else if (keycode == Input.Keys.NUMPAD_9) {
            move(current, Direction.NORTH_EAST);
        } else if (keycode == Input.Keys.NUMPAD_1) {
            move(current, Direction.SOUTH_WEST);
        } else if (keycode == Input.Keys.NUMPAD_3) {
            move(current, Direction.SOUTH_EAST);
        } else if (keycode == Input.Keys.E) {
            Portal p = this.currentLocation.cell != null ? ((Cell) this.currentLocation.cell).portal : null;
            if (p != null) {
                Andius.mainGame.setScreen(p.getMap().getScreen());
            }
        }

        return false;
    }

    private void move(Center current, Direction dir) {
        Center next = preMove(current, dir);
        if (next == null) {
            return;
        }
        this.currentLocation = next;
        postMove(dir, next);
    }

    private Center preMove(Center current, Direction dir) {

        Center next = null;

        int dist = 24;

        if (dir == Direction.NORTH) {
            next = current.getClosestNeighbor(current.loc.x, current.loc.y - dist);
        }
        if (dir == Direction.SOUTH) {
            next = current.getClosestNeighbor(current.loc.x, current.loc.y + dist);
        }
        if (dir == Direction.WEST) {
            next = current.getClosestNeighbor(current.loc.x - dist, current.loc.y);
        }
        if (dir == Direction.EAST) {
            next = current.getClosestNeighbor(current.loc.x + dist, current.loc.y);
        }

        if (dir == Direction.NORTH_EAST) {
            next = current.getClosestNeighbor(current.loc.x + dist, current.loc.y - dist);
        }
        if (dir == Direction.NORTH_WEST) {
            next = current.getClosestNeighbor(current.loc.x - dist, current.loc.y - dist);
        }

        if (dir == Direction.SOUTH_EAST) {
            next = current.getClosestNeighbor(current.loc.x + dist, current.loc.y + dist);
        }
        if (dir == Direction.SOUTH_WEST) {
            next = current.getClosestNeighbor(current.loc.x - dist, current.loc.y + dist);
        }

        if (next != null) {
            Cell cell = (Cell) next.cell;
            if (cell == null || (cell.height != null && cell.height < 0) || cell.height >= 70) {
                //Sounds.play(Sound.BLOCKED);
                //return null;
            }
        }
        return next;
    }

    private void postMove(Direction dir, Center next) {

        newMapPixelCoords.set((float) next.loc.x, (float) (next.loc.y), 0f);

        //check for active moongate portal
        for (Moongate g : Moongate.values()) {
            Cell c = moongateMap.get(g);
            if (c != null && c.center != null && c.center.equals(next) && g.getCurrentTexture() != null) {
                Sounds.play(Sound.WAVE);
                Moongate d = getDestinationForMoongate(g);
                Cell tmp = moongateMap.get(d);
                this.currentLocation = tmp.center;
                newMapPixelCoords.set(tmp.center.loc.x, tmp.center.loc.y, 0f);
                return;
            }
        }
    }

    @Override
    public void log(String t) {
    }

    @Override
    public void finishTurn(int currentX, int currentY) {
    }

    @Override
    public void partyDeath() {
    }

    private void updateMoons() {

        phaseIndex++;
        if (phaseIndex >= MOON_PHASES * MOON_SECONDS_PER_PHASE * 4) {
            phaseIndex = 0;
        }

        phaseCount = (phaseIndex / (4 * MOON_SECONDS_PER_PHASE));
        feluccaphase = phaseCount % 8;
        trammelphase = phaseCount / 3;
        if (trammelphase > 7) {
            trammelphase = 7;
        }
        trammelSubphase = phaseIndex % (MOON_SECONDS_PER_PHASE * 4 * 3);

        for (Constants.Moongate g : Constants.Moongate.values()) {
            g.setCurrentTexture(null);
        }

        Constants.Moongate gate = Constants.Moongate.values()[trammelphase];
        TextureAtlas.AtlasRegion texture = null;
        if (trammelSubphase == 0) {
            texture = Andius.moongateTextures.get(0);
        } else if (trammelSubphase == 1) {
            texture = Andius.moongateTextures.get(1);
        } else if (trammelSubphase == 2) {
            texture = Andius.moongateTextures.get(2);
        } else if (trammelSubphase == 3) {
            texture = Andius.moongateTextures.get(3);
        } else if ((trammelSubphase > 3) && (trammelSubphase < (MOON_SECONDS_PER_PHASE * 4 * 3) - 3)) {
            texture = Andius.moongateTextures.get(3);
        } else if (trammelSubphase == (MOON_SECONDS_PER_PHASE * 4 * 3) - 3) {
            texture = Andius.moongateTextures.get(2);
        } else if (trammelSubphase == (MOON_SECONDS_PER_PHASE * 4 * 3) - 2) {
            texture = Andius.moongateTextures.get(1);
        } else if (trammelSubphase == (MOON_SECONDS_PER_PHASE * 4 * 3) - 1) {
            texture = Andius.moongateTextures.get(0);
        }
        gate.setCurrentTexture(texture);

    }

    private Constants.Moongate getDestinationForMoongate(Constants.Moongate m) {

        int destGate = m.ordinal();

        if (feluccaphase == m.getD1()) {
            destGate = m.getD1();
        } else if (feluccaphase == m.getD2()) {
            destGate = m.getD2();
        } else if (feluccaphase == m.getD3()) {
            destGate = m.getD3();
        }

        return Constants.Moongate.values()[destGate];
    }

    class Cell {

        String name;
        Float x;
        Float y;
        Integer height;

        Center center;
        Portal portal;
        Color color;

        public Color color() {
            if (this.color == null) {
                if (name != null) {
                    switch (name) {
                        case "shallows":
                            this.color = new Color(0, 0, 0.95f, 1);
                            break;
                        case "water":
                            this.color = new Color(0, 0, 0.75f, 1);
                            break;
                        case "sea":
                            this.color = new Color(0, 0, 0.5f, 1);
                            break;
                        case "hills":
                            this.color = Color.BROWN;
                            break;
                        case "mountains":
                            float gray = ((RANDOM.nextInt(21) + 60)) * .01f;
                            this.color = new Color(gray, gray, gray, 1f);
                            break;
                        case "forest":
                            this.color = Color.FOREST;
                            break;
                        case "brush":
                            this.color = new Color(0, 0.7f, 0, 1);
                            break;
                        case "lava":
                            this.color = Color.FIREBRICK;
                            break;
                        case "fire_field":
                            this.color = Color.SCARLET;
                            break;
                        case "swamp":
                            this.color = new Color(0xcccc00ff);
                            break;
                        case "shrine":
                        case "dungeon":
                        case "city":
                        case "bridge":
                        case "castle":
                        case "town":
                        case "ankh":
                        case "ruins":
                        case "lcb_west":
                        case "lcb_entrance":
                        case "lcb_east":
                            this.color = Color.PINK;
                            break;
                        default:
                            this.color = new Color(0, (float) ((100 - height - 0) * .01f), 0, 1);
                    }
                } else {
                    this.color = new Color(0, (float) ((100 - height - 0) * .01f), 0, 1);
                }

            }

            return this.color;
        }

        public boolean enterable() {
            switch (this.name) {
                case "shrine":
                case "dungeon":
                case "city":
                case "castle":
                case "town":
                case "ankh":
                case "ruins":
                case "lcb_entrance":
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public String toString() {
            return "Cell{" + "name=" + name + ", x=" + x + ", y=" + y + ", portal=" + portal + '}';
        }

    }

    class PortalBean {

        String name;
        Float x;
        Float y;
    }

    class MGBean {

        Integer id;
        Float x;
        Float y;
    }

    public class VoronoiRenderer {

        private final Voronoi voro;
        private final List<Edge> edges = new ArrayList();
        private final List<Corner> corners = new ArrayList();
        private final List<Center> centers = new ArrayList();
        private final float[] triangleVertices = new float[6];

        private final ShapeRenderer shape;
        private final com.badlogic.gdx.math.Rectangle viewBounds;
        private final Rectangle bounds;
        private final Batch batch;

        public VoronoiRenderer(List<Float[]> pts) {

            this.shape = new ShapeRenderer();
            this.batch = new SpriteBatch();
            this.viewBounds = new com.badlogic.gdx.math.Rectangle();
            this.voro = new Voronoi(pts, factor);
            this.bounds = this.voro.getBounds();

            buildGraph();
            improveCorners();

            for (Cell cell : cells) {
                for (int i = 0; i < centers.size(); i++) {
                    Center center = centers.get(i);
                    if (Math.round(cell.x * 100.0) == Math.round(center.loc.x * 100.0)
                            && Math.round(cell.y * 100.0) == Math.round(center.loc.y * 100.0)) {
                        center.cell = cell;
                        cell.center = center;
                        break;
                    }
                }
            }

        }

        public Batch getBatch() {
            return batch;
        }

        public com.badlogic.gdx.math.Rectangle getViewBounds() {
            return viewBounds;
        }

        public Center getCenter(int i) {
            return this.centers.get(i);
        }

        public Cell getCenterCell(int i) {
            return (Cell) this.centers.get(i).cell;
        }

        public void setView(OrthographicCamera camera) {
            batch.setProjectionMatrix(camera.combined);
            shape.setProjectionMatrix(camera.combined);
            float width = camera.viewportWidth * camera.zoom;
            float height = camera.viewportHeight * camera.zoom;
            float w = width * Math.abs(camera.up.y) + height * Math.abs(camera.up.x);
            float h = height * Math.abs(camera.up.y) + width * Math.abs(camera.up.x);
            viewBounds.set(camera.position.x - w / 2, camera.position.y - h / 2, w, h);
        }

        public void setView(Matrix4 projection, float x, float y, float width, float height) {
            batch.setProjectionMatrix(projection);
            shape.setProjectionMatrix(projection);
            viewBounds.set(x, y, width, height);
        }

        public void render(float delta, Center current) {

            for (int i = 0; i < centers.size(); i++) {
                Center c = centers.get(i);
                if (viewBounds.contains((float) c.loc.x, (float) c.loc.y)) {
                    if (c.cell != null) {
                        Cell cell = (Cell) c.cell;
                        drawPolygon(c, cell.color());
                    } else {
                        drawPolygon(c, Color.NAVY);
                    }
                }
            }

            for (Edge e : edges) {
                if (e.v0 != null && e.v1 != null && viewBounds.contains(e.v0.loc.x, e.v0.loc.y)) {
                    if (e.d0.cell != null && ((Cell) e.d0.cell).height != null && ((Cell) e.d0.cell).height > 0) {
                        this.shape.begin(ShapeRenderer.ShapeType.Line);
                        this.shape.setColor(Color.GRAY);
                        this.shape.line(e.v0.loc.x, e.v0.loc.y, e.v1.loc.x, e.v1.loc.y);
                        this.shape.end();
                    }
                }
            }

            for (int i = 0; i < centers.size(); i++) {
                Center c = centers.get(i);
                Cell cell = (Cell) c.cell;
                if (c.cell != null && cell.portal != null && viewBounds.contains((float) c.loc.x, (float) c.loc.y)) {
                    batch.begin();
                    batch.draw(castle.getKeyFrame(delta, true),
                            (float) c.loc.x, (float) (c.loc.y), 11, 2, 24f, 24f, 1.0f, 1.0f, 90, false);
                    batch.end();
                }
            }

            if (current != null) {
//                if (viewBounds.contains(current.loc.x, current.loc.y)) {
//                    shape.begin(ShapeRenderer.ShapeType.Line);
//                    this.shape.setColor(Color.RED);
//                    this.shape.ellipse(current.loc.x, current.loc.y, 6, 6);
//                    shape.end();
//                }
            }

        }

        private void drawPolygon(Center c, Color color) {

            Gdx.gl.glLineWidth(1);
            this.shape.setColor(color);

            for (int i = 0; i < c.neighbors.size(); i++) {
                Center n = c.neighbors.get(i);
                Edge e = edgeWithCenters(c, n);
                if (e.v0 == null) {
                    continue;
                }
                drawTriangle(e.v0, e.v1, c);
            }
        }

        private void drawTriangle(Corner c1, Corner c2, Center center) {

            triangleVertices[0] = center.loc.x;
            triangleVertices[1] = center.loc.y;
            triangleVertices[2] = c1.loc.x;
            triangleVertices[3] = c1.loc.y;
            triangleVertices[4] = c2.loc.x;
            triangleVertices[5] = c2.loc.y;

            this.shape.begin(ShapeRenderer.ShapeType.Filled);
            this.shape.triangle(triangleVertices[0], triangleVertices[1], triangleVertices[2], triangleVertices[3], triangleVertices[4], triangleVertices[5]);
            this.shape.end();

        }

        private void buildGraph() {

            final HashMap<Point, Center> pointCenterMap = new HashMap();
            final ArrayList<Point> points = this.voro.siteCoords();

            for (Point p : points) {
                Center c = new Center();
                c.loc = p;
                c.index = centers.size();
                centers.add(c);
                pointCenterMap.put(p, c);
            }

            for (Center c : centers) {
                this.voro.region(c.loc);
            }

            final ArrayList<andius.voronoi.nodes.Edge> libedges = this.voro.edges();
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

        private Edge edgeWithCenters(Center c1, Center c2) {
            for (int i = 0; i < c1.borders.size(); i++) {
                Edge e = c1.borders.get(i);
                if (e.d0 == c2 || e.d1 == c2) {
                    return e;
                }
            }
            return null;
        }

        private void improveCorners() {
            Point[] newP = new Point[corners.size()];
            for (Corner c : corners) {
                if (c.border) {
                    newP[c.index] = c.loc;
                } else {
                    float x = 0;
                    float y = 0;
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
                e.setVoronoi(e.v0, e.v1);
            });
        }

    }

    class VoroPolygonRegion extends PolygonRegion {

        final float[] textureCoords;
        final float[] vertices;
        final short[] triangles;
        final TextureRegion region;

        public VoroPolygonRegion(TextureRegion region, float[] vertices, short[] triangles) {
            super(region, vertices, triangles);
            this.region = region;
            this.vertices = vertices;
            this.triangles = triangles;
            this.textureCoords = new float[vertices.length];

            float u = region.getU(), v = region.getV();
            float uvWidth = region.getU2() - u;
            float uvHeight = region.getV2() - v;
            int width = region.getRegionWidth();
            int height = region.getRegionHeight();

            for (int i = 0, n = vertices.length; i < n; i++) {
                textureCoords[i] = u + uvWidth * (vertices[i] / width);
                i++;
                textureCoords[i] = v + uvHeight * (1 - (vertices[i] / height));
            }
        }

        @Override
        public float[] getVertices() {
            return vertices;
        }

        @Override
        public short[] getTriangles() {
            return triangles;
        }

        @Override
        public float[] getTextureCoords() {
            return textureCoords;
        }

        @Override
        public TextureRegion getRegion() {
            return region;
        }
    }

}
