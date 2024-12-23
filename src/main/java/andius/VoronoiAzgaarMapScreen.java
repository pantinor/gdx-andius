package andius;

import static andius.Andius.CTX;
import static andius.Andius.mapAtlas;
import static andius.Constants.MOON_PHASES;
import static andius.Constants.MOON_SECONDS_PER_PHASE;
import static andius.Constants.WORLD_TILE_DIM;
import andius.objects.Portal;
import andius.objects.SaveGame;
import andius.voronoi.graph.Center;
import andius.voronoi.graph.Corner;
import andius.voronoi.graph.Edge;
import andius.voronoi.nodes.LineSegment;
import andius.voronoi.nodes.Point;
import andius.voronoi.nodes.Rectangle;
import andius.voronoi.nodes.Site;
import andius.voronoi.nodes.Voronoi;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class VoronoiAzgaarMapScreen extends BaseScreen {

    private Grid grid;
    private JsonElement burgs;
    private JsonElement states;
    private JsonElement cultures;
    private JsonElement features;

    private JsonElement religions;
    private JsonElement provinces;
    private JsonElement rivers;
    private JsonElement markers;
    private JsonElement routes;
    private JsonElement zones;

    private final float factor = 5.5f;

    private final Viewport mapViewPort;
    private final Batch mapBatch, batch;
    private Center currentLocation;
    private final VoronoiAzgaarRenderer renderer;

    public static Animation<TextureRegion> castle;
    private final TextureAtlas moonPhaseAtlas;
    private static int phaseIndex = 0, phaseCount = 0, trammelphase = 0, trammelSubphase = 0, feluccaphase = 0;
    public GameTimer gameTimer = new GameTimer();

    public java.util.Map<Constants.Moongate, Center> moongateMap = new HashMap<>();

    public VoronoiAzgaarMapScreen() {

        try {
            //https://azgaar.github.io/Fantasy-Map-Generator/
            InputStream fstream = Gdx.files.classpath("assets/azgaarMaps/Movia 2024-11-03-13-51.map").read();
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String sparams = br.readLine();
            String ssettings = br.readLine();
            String smapCoordinates = br.readLine();
            String sbiomes = br.readLine();
            String snotes = br.readLine();

            while (!br.readLine().contains("</svg>")) {
                //next
            }

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();

            String sgrid = br.readLine();
            this.grid = gson.fromJson(sgrid, new TypeToken<Grid>() {
            }.getType());

            grid.cellsHeight = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
            grid.cellsPrecipitation = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
            grid.cellsFeature = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
            grid.cellsType = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
            grid.cellsTemperature = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
            grid.cellsColor = new Color[grid.cellsHeight.length];

            features = gson.fromJson(br.readLine(), new TypeToken<JsonArray>() {
            }.getType());
            cultures = gson.fromJson(br.readLine(), new TypeToken<JsonArray>() {
            }.getType());
            states = gson.fromJson(br.readLine(), new TypeToken<JsonArray>() {
            }.getType());
            burgs = gson.fromJson(br.readLine(), new TypeToken<JsonArray>() {
            }.getType());

            //https://github.com/Azgaar/Fantasy-Map-Generator/blob/master/modules/io/load.js#L384C1-L407C64
            grid.biome = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
            grid.burg = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
            grid.conf = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
            grid.culture = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
            grid.fl = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
            grid.pop = Arrays.stream(br.readLine().split(",")).mapToDouble(Double::parseDouble).toArray();
            grid.r = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
            br.readLine();//deprecated cells.road
            grid.s = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
            grid.state = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
            grid.religion = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
            grid.province = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
            br.readLine();//deprecated cells.crossroad

            religions = gson.fromJson(br.readLine(), new TypeToken<JsonArray>() {
            }.getType());
            provinces = gson.fromJson(br.readLine(), new TypeToken<JsonArray>() {
            }.getType());
            br.readLine();//name bases split with /
            rivers = gson.fromJson(br.readLine(), new TypeToken<JsonArray>() {
            }.getType());
            br.readLine();//
            br.readLine();//
            markers = gson.fromJson(br.readLine(), new TypeToken<JsonArray>() {
            }.getType());
            br.readLine();//
            routes = gson.fromJson(br.readLine(), new TypeToken<JsonArray>() {
            }.getType());
            zones = gson.fromJson(br.readLine(), new TypeToken<JsonArray>() {
            }.getType());

        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < grid.points.size(); i++) {
            grid.points.get(i)[0] = grid.points.get(i)[0] * factor;
            grid.points.get(i)[1] = grid.points.get(i)[1] * factor;
        }

        renderer = new VoronoiAzgaarRenderer(this.grid.points);
        mapBatch = renderer.getBatch();

        castle = new Animation(.4f, mapAtlas.findRegions("castle_gray"));

        batch = new SpriteBatch();
        stage = new Stage(viewport);
        Andius.HUD.addActor(stage);
        moonPhaseAtlas = new TextureAtlas(Gdx.files.classpath("assets/data/moon-atlas.txt"));

        SequenceAction seq1 = Actions.action(SequenceAction.class);
        seq1.addAction(Actions.delay(.25f));
        seq1.addAction(Actions.run(gameTimer));
        stage.addAction(Actions.forever(seq1));

        camera = new OrthographicCamera(Andius.MAP_VIEWPORT_DIM + 128, Andius.MAP_VIEWPORT_DIM + 128);
        ((OrthographicCamera) camera).setToOrtho(true);

        mapViewPort = new ScreenViewport(camera);

        JsonArray e = burgs.getAsJsonArray();
        currentLocation = getCenter(e.get(5).getAsJsonObject());

        newMapPixelCoords.set((float) currentLocation.loc.x, (float) currentLocation.loc.y, 0f);

        e = markers.getAsJsonArray();
        List<JsonObject> mks = new ArrayList<>();
        for (int i = 0; i < e.size(); i++) {
            JsonObject jo = e.get(i).getAsJsonObject();
            String type = jo.getAsJsonPrimitive("type").getAsString();
            if (type.equals("portals") || type.equals("ruins")) {
                mks.add(jo);
            }
        }
        for (int i = 0; i < Moongate.values().length; i++) {
            moongateMap.put(Moongate.values()[i], getCenter(mks.get(i).getAsJsonObject()));
        }

    }

    private Center getCenter(JsonObject jo) {
        float x = jo.getAsJsonPrimitive("x").getAsFloat();
        float y = jo.getAsJsonPrimitive("y").getAsFloat();
        return renderer.getNearestCenter(x * factor, y * factor);
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
    public void setMapPixelCoords(Vector3 v, int x, int y, int z) {
    }

    @Override
    public void getCurrentMapCoords(Vector3 v) {
    }

    @Override
    public void save(SaveGame saveGame) {
    }

    @Override
    public void load(SaveGame saveGame) {
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
                Center c = moongateMap.get(g);
                mapBatch.begin();
                mapBatch.draw(t, grid.points.get(c.index)[0], grid.points.get(c.index)[1], 11, 2, 24f, 24f, 1.0f, 1.0f, 90, false);
                mapBatch.end();
            }
        }
        batch.begin();

        batch.draw(Andius.backGround, 0, 0);
        batch.draw(Andius.world_scr_avatar.getKeyFrame(time, true), WORLD_TILE_DIM * 14, WORLD_TILE_DIM * 17);
        batch.draw(moonPhaseAtlas.findRegion("PHASE_" + trammelphase), 348, Andius.SCREEN_HEIGHT - 32, 20, 20);
        batch.draw(moonPhaseAtlas.findRegion("PHASE_" + feluccaphase), 372, Andius.SCREEN_HEIGHT - 32, 20, 20);

        Andius.HUD.render(batch, Andius.CTX);

        //Andius.font.draw(batch, String.format("%s\n", this.currentLocation), 200, Andius.SCREEN_HEIGHT - 32);
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

        if (keycode == Input.Keys.UP) {
            Center next = preMove(current, Direction.NORTH);
            if (next == null) {
                return false;
            }
            this.currentLocation = next;
            postMove(Direction.NORTH, next);
        } else if (keycode == Input.Keys.DOWN) {
            Center next = preMove(current, Direction.SOUTH);
            if (next == null) {
                return false;
            }
            this.currentLocation = next;
            postMove(Direction.SOUTH, next);
        } else if (keycode == Input.Keys.RIGHT) {
            Center next = preMove(current, Direction.EAST);
            if (next == null) {
                return false;
            }
            this.currentLocation = next;
            postMove(Direction.EAST, next);
        } else if (keycode == Input.Keys.LEFT) {
            Center next = preMove(current, Direction.WEST);
            if (next == null) {
                return false;
            }
            this.currentLocation = next;
            postMove(Direction.WEST, next);
        } else if (keycode == Input.Keys.E) {
            if (this.currentLocation.portal != null) {
                Portal p = (Portal) this.currentLocation.portal;
                Andius.mainGame.setScreen(p.getMap().getScreen());
            }
        }

        return false;
    }

    private Center preMove(Center current, Direction dir) {

        Center next = null;

        if (dir == Direction.NORTH) {
            next = current.getClosestNeighbor(current.loc.x, current.loc.y - 48);
        }
        if (dir == Direction.SOUTH) {
            next = current.getClosestNeighbor(current.loc.x, current.loc.y + 48);
        }
        if (dir == Direction.WEST) {
            next = current.getClosestNeighbor(current.loc.x - 48, current.loc.y);
        }
        if (dir == Direction.EAST) {
            next = current.getClosestNeighbor(current.loc.x + 48, current.loc.y);
        }

        if (next != null) {
            if (grid.cellsType[next.pointIndex] < 0 || grid.cellsHeight[next.pointIndex] >= 70) {
                if (dir == Direction.NORTH) {
                    next = current.getClosestNeighbor(current.loc.x, current.loc.y - 48, next);
                }
                if (dir == Direction.SOUTH) {
                    next = current.getClosestNeighbor(current.loc.x, current.loc.y + 48, next);
                }
                if (dir == Direction.WEST) {
                    next = current.getClosestNeighbor(current.loc.x - 48, current.loc.y, next);
                }
                if (dir == Direction.EAST) {
                    next = current.getClosestNeighbor(current.loc.x + 48, current.loc.y, next);
                }
            }
        }

        if (next != null) {
            if (grid.cellsType[next.pointIndex] < 0 || grid.cellsHeight[next.pointIndex] >= 70) {
                next = null;
            }
        }

        if (next == null) {
            Sounds.play(Sound.BLOCKED);
        }

        return next;
    }

    private void postMove(Direction dir, Center next) {

        newMapPixelCoords.set((float) next.loc.x, (float) (next.loc.y), 0f);

        //check for active moongate portal
        for (Moongate g : Moongate.values()) {
            Center c = moongateMap.get(g);
            if (c != null && c.equals(next) && g.getCurrentTexture() != null) {
                Sounds.play(Sound.WAVE);
                Moongate d = getDestinationForMoongate(g);
                Center tmp = moongateMap.get(d);
                this.currentLocation = tmp;
                newMapPixelCoords.set(tmp.loc.x, tmp.loc.y, 0f);
                return;
            }
        }
    }

    @Override
    public void log(String t) {
    }

    @Override
    public void teleport(int level, int stepsX, int stepsY) {

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

    public class Grid {

        Float spacing;
        Integer cellsX;
        Integer cellsY;
        List<Integer[]> boundary;
        List<Float[]> points;
        JsonArray features;

        int[] cellsHeight;
        int[] cellsPrecipitation;
        int[] cellsFeature;
        int[] cellsType;// cell type: 1 = land coast; -1 = water near coast
        int[] cellsTemperature;
        Color[] cellsColor;

        int[] biome;
        int[] burg;
        int[] conf;
        int[] culture;
        int[] fl;
        double[] pop;
        int[] r;
        int[] s;
        int[] state;
        int[] religion;
        int[] province;

    }

    private Color color(int idx) {
        if (this.grid.cellsColor[idx] == null) {

            int height = this.grid.cellsHeight[idx];

            if (height >= 70) {
                this.grid.cellsColor[idx] = new Color(0xf99456ff);
            } else if (height > 60) {
                this.grid.cellsColor[idx] = new Color(0xfdb76aff);
            } else if (height > 50) {
                this.grid.cellsColor[idx] = new Color(0xfed483ff);
            } else if (height > 40) {
                this.grid.cellsColor[idx] = new Color(0xfeeb9fff);
            } else if (height > 35) {
                this.grid.cellsColor[idx] = new Color(0xfbf8b0ff);
            } else if (height > 30) {
                this.grid.cellsColor[idx] = new Color(0xd7ef9fff);
            } else if (height > 25) {
                this.grid.cellsColor[idx] = new Color(0xb6e2a1ff);
            } else if (height > 21) {
                this.grid.cellsColor[idx] = new Color(0x8fd2a4ff);
            } else if (height == 21) {
                this.grid.cellsColor[idx] = new Color(0x69bda9ff);
            } else if (height == 20) {
                this.grid.cellsColor[idx] = new Color(0x69bda9ff);
            } else {
                this.grid.cellsColor[idx] = Color.NAVY;
            }

            switch (this.grid.cellsType[idx]) {
                case -2:
                    this.grid.cellsColor[idx] = new Color(0, 0, 0.6f, 1);
                    break;
                case -1:
                    this.grid.cellsColor[idx] = new Color(0, 0, 0.9f, 1);
                    break;
                case 1:
                    this.grid.cellsColor[idx] = new Color(0x69bda9ff);
                    break;
                case 2:
                    this.grid.cellsColor[idx] = new Color(0x69bda9ff);
                    break;
            }

            if (this.grid.cellsColor[idx] == null) {
                this.grid.cellsColor[idx] = Color.PURPLE;
            }
        }

        return this.grid.cellsColor[idx];
    }

    public class VoronoiAzgaarRenderer implements MapRenderer {

        final Voronoi voro;
        private final List<Edge> edges = new ArrayList();
        private final List<Corner> corners = new ArrayList();
        private final List<Center> centers = new ArrayList();
        private final float[] triangleVertices = new float[6];

        private final ShapeRenderer shape;
        private final com.badlogic.gdx.math.Rectangle viewBounds;
        private final Rectangle bounds;
        private final Batch batch;

        public VoronoiAzgaarRenderer(List<Float[]> pts) {

            this.shape = new ShapeRenderer();
            this.batch = new SpriteBatch();
            this.viewBounds = new com.badlogic.gdx.math.Rectangle();
            this.voro = new Voronoi(pts, 1f);
            this.bounds = this.voro.getBounds();

            buildGraph();
            improveCorners();

            for (int i = 0; i < grid.points.size(); i++) {
                Iterator<Site> iter = this.voro.sites();
                while (iter.hasNext()) {
                    Site s = iter.next();
                    if (s.region != null && s.region.contains(pts.get(i)[0], pts.get(i)[1])) {
                        s.pointIndex = i;
                        break;
                    }
                }
            }

            for (int i = 0; i < centers.size(); i++) {
                Center center = centers.get(i);
                Site s = this.voro.site(center.loc);
                if (s != null) {
                    center.pointIndex = s.pointIndex;
                }
            }

            JsonArray e = burgs.getAsJsonArray();
            for (int i = 1; i < e.size(); i++) {
                JsonObject burg = e.get(i).getAsJsonObject();
                float x = burg.getAsJsonPrimitive("x").getAsFloat();
                float y = burg.getAsJsonPrimitive("y").getAsFloat();
                System.out.println(burg);
                Center c = getNearestCenter(x * factor, y * factor);
                if (c != null) {
                    c.portal = new Portal(Constants.Map.CAVE, 0, 0, 15, 31, null, false, false);
                }
            }

            JsonArray mks = markers.getAsJsonArray();
            for (int i = 1; i < mks.size(); i++) {
                JsonObject mk = mks.get(i).getAsJsonObject();
                float x = mk.getAsJsonPrimitive("x").getAsFloat();
                float y = mk.getAsJsonPrimitive("y").getAsFloat();
                System.out.println(mk);
                Center c = getNearestCenter(x * factor, y * factor);
                if (c != null) {
                    c.object = mk;
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

        public Center getCenterAtPoint(int idx) {
            for (int i = 0; i < centers.size(); i++) {
                Center center = this.centers.get(i);
                if (center.pointIndex == idx) {
                    return center;
                }
            }
            return null;
        }

        private Center getNearestCenter(float x, float y) {
            Center found = null;
            float dist = Float.MAX_VALUE;
            for (int i = 0; i < centers.size(); i++) {
                Center c = this.centers.get(i);
                float d = Point.distance(c.loc, x, y);
                if (d < dist && grid.cellsType[c.pointIndex] >= 0 && grid.cellsHeight[c.pointIndex] < 70) {
                    dist = d;
                    found = c;
                }
            }
            return found;
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

        public void render(float delta, Center current) {

            for (int i = 0; i < centers.size(); i++) {
                Center c = centers.get(i);
                if (viewBounds.contains((float) c.loc.x, (float) c.loc.y)) {
                    drawPolygon(c, color(c.pointIndex));
                }
            }

            for (Edge e : edges) {
                if (e.v0 != null && e.v1 != null && viewBounds.contains(e.v0.loc.x, e.v0.loc.y)) {
                    if (grid.cellsHeight[e.d0.pointIndex] >= 20) {
                        this.shape.begin(ShapeRenderer.ShapeType.Line);
                        this.shape.setColor(Color.GRAY);
                        this.shape.line(e.v0.loc.x, e.v0.loc.y, e.v1.loc.x, e.v1.loc.y);
                        this.shape.end();
                    }
                }
            }

            for (int i = 0; i < centers.size(); i++) {
                Center c = centers.get(i);
                if (c.portal != null && viewBounds.contains((float) c.loc.x, (float) c.loc.y)) {
                    batch.begin();
                    batch.draw(castle.getKeyFrame(delta, true), (float) c.loc.x, (float) (c.loc.y), 11, 2, 24f, 24f, 1.0f, 1.0f, 90, false);
                    batch.end();
                }
            }

        }

        @Override
        public void render() {
        }

        @Override
        public void render(int[] layers) {
        }

        private void drawPolygon(Center c, Color color) {

            Gdx.gl.glLineWidth(1);
            this.shape.setColor(c.object != null ? Color.RED : color);

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

}
