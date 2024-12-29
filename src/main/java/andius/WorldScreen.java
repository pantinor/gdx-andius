package andius;

import static andius.Andius.CTX;
import andius.objects.Portal;
import andius.objects.SaveGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import utils.SpreadFOV;
import utils.WrappingTileMapRenderer;

public class WorldScreen extends BaseScreen {

    private static final int MAP_VIEWPORT_DIM = 504;

    private final Map map;
    private final WrappingTileMapRenderer renderer;
    private final Batch mapBatch, batch;
    private final Viewport mapViewPort;
    private final TextureAtlas moonPhaseAtlas;
    private static int phaseIndex = 0, phaseCount = 0, trammelphase = 0, trammelSubphase = 0, feluccaphase = 0;
    public GameTimer gameTimer = new GameTimer();
    private final Texture frame;
    private final int width, height;

    public WorldScreen(Map map) {

        this.map = map;

        this.frame = new Texture(Gdx.files.classpath("assets/data/world_frame.png"));

        this.width = this.map.getBaseMap().getWidth();
        this.height = this.map.getBaseMap().getHeight();
        this.mapPixelHeight = height * WORLD_TILE_DIM;

        batch = new SpriteBatch();

        stage = new Stage(viewport);
        Andius.HUD.addActor(stage);

        camera = new OrthographicCamera(MAP_VIEWPORT_DIM, MAP_VIEWPORT_DIM);
        mapViewPort = new ScreenViewport(camera);

        addButtons(this.map);

        moonPhaseAtlas = new TextureAtlas(Gdx.files.classpath("assets/data/moon-atlas.txt"));

        SequenceAction seq1 = Actions.action(SequenceAction.class);
        seq1.addAction(Actions.delay(.25f));
        seq1.addAction(Actions.run(gameTimer));
        stage.addAction(Actions.forever(seq1));

        float[][] shadowMap = new float[width][height];
        TiledMapTileLayer forest = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("forest");
        TiledMapTileLayer mountains = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("mountains");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                TiledMapTileLayer.Cell cellForest = forest.getCell(x, height - 1 - y);
                TiledMapTileLayer.Cell cellMountain = mountains.getCell(x, height - 1 - y);
                shadowMap[x][y] = (cellForest != null || cellMountain != null ? 1 : 0);
            }
        }

        SpreadFOV fov = new SpreadFOV(shadowMap);
        renderer = new WrappingTileMapRenderer(this.map, this.map.getTiledMap(), fov, 1f);
        mapBatch = renderer.getBatch();

        setMapPixelCoords(newMapPixelCoords, this.map.getStartX(), this.map.getStartY(), 0);

        renderer.getFOV().calculateFOV(this.map.getStartX(), this.map.getStartY(), 20f);
    }

    @Override
    public void save(SaveGame saveGame) {
        Vector3 v = new Vector3();
        getCurrentMapCoords(v);
        CTX.saveGame.map = Map.WORLD;
        CTX.saveGame.wx = (int) v.x;
        CTX.saveGame.wy = (int) v.y;
        CTX.saveGame.level = 0;
        CTX.saveGame.direction = Direction.NORTH;
    }

    @Override
    public void load(SaveGame saveGame) {
        setMapPixelCoords(newMapPixelCoords, saveGame.wx, saveGame.wy, 0);
        renderer.getFOV().calculateFOV(saveGame.wx, saveGame.wy, 20f);
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
    public void render(float delta) {

        time += delta;

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (renderer == null) {
            return;
        }

        camera.position.set(
                newMapPixelCoords.x + 7 * WORLD_TILE_DIM + 8,
                newMapPixelCoords.y - 0 * WORLD_TILE_DIM + 0,
                0);

        camera.update();

        renderer.setView(camera.combined,
                camera.position.x - WORLD_TILE_DIM * 18,
                camera.position.y - WORLD_TILE_DIM * 12,
                MAP_VIEWPORT_DIM,
                MAP_VIEWPORT_DIM);

        renderer.render();

        mapBatch.begin();

        for (Moongate g : Moongate.values()) {
            TextureRegion t = g.getCurrentTexture();
            if (t != null) {
                mapBatch.draw(t, g.getX(), g.getY());
            }
        }

        mapBatch.end();

        batch.begin();

        batch.draw(this.frame, 0, 0);
        batch.draw(Andius.world_scr_avatar.getKeyFrame(time, true), WORLD_TILE_DIM * 14, WORLD_TILE_DIM * 15);

        batch.draw(moonPhaseAtlas.findRegion("PHASE_" + trammelphase), 348, Andius.SCREEN_HEIGHT - 32, 20, 20);
        batch.draw(moonPhaseAtlas.findRegion("PHASE_" + feluccaphase), 372, Andius.SCREEN_HEIGHT - 32, 20, 20);

        Andius.HUD.render(batch, Andius.CTX);

        Vector3 v = new Vector3();
        getCurrentMapCoords(v);
        Andius.font.draw(batch, String.format("%s, %s\n", v.x, v.y), 100, Andius.SCREEN_HEIGHT - 32);
        batch.end();

        stage.act();
        stage.draw();

    }

    @Override
    public void log(String s) {
        Andius.HUD.log(s);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        mapViewPort.update(width, height, false);
    }

    @Override
    public void setMapPixelCoords(Vector3 v, int x, int y, int z) {
        v.set(x * WORLD_TILE_DIM, mapPixelHeight - y * WORLD_TILE_DIM, 0);
    }

    @Override
    public void getCurrentMapCoords(Vector3 v) {
        Vector3 tmp = camera.unproject(new Vector3(348, 384, 0), 96, 624, MAP_VIEWPORT_DIM, MAP_VIEWPORT_DIM);
        float x = Math.round(tmp.x / WORLD_TILE_DIM) - 7;
        if (Math.round(tmp.y) == -750) {//workaround for weird math at 0,0 location
            tmp.y = mapPixelHeight + WORLD_TILE_DIM;
            v.set(x, 0, 0);
        } else {
            float y = Math.round((mapPixelHeight - tmp.y) / WORLD_TILE_DIM) - 31;
            v.set(x, y, 0);
        }
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
    public boolean keyUp(int keycode) {
        Vector3 currentPosition = new Vector3();
        getCurrentMapCoords(currentPosition);

        Vector3 newPosition = null;

        if (keycode == Keys.UP) {
            newPosition = checkAndMove(currentPosition, Direction.NORTH);
        } else if (keycode == Keys.DOWN) {
            newPosition = checkAndMove(currentPosition, Direction.SOUTH);
        } else if (keycode == Keys.RIGHT) {
            newPosition = checkAndMove(currentPosition, Direction.EAST);
        } else if (keycode == Keys.LEFT) {
            newPosition = checkAndMove(currentPosition, Direction.WEST);
        } else if (keycode == Keys.E) {
            Portal p = this.map.getBaseMap().getPortal((int) currentPosition.x, (int) currentPosition.y);
            if (p != null) {
                Andius.mainGame.setScreen(p.getMap().getScreen());
            }
            return false;
        }

        if (newPosition != null) {
            postMove(newPosition);
        }

        return false;
    }

    private Vector3 checkAndMove(Vector3 current, Direction dir) {

        int nx = (int) current.x;
        int ny = (int) current.y;

        if (dir == Direction.NORTH) {
            ny = (int) current.y - 1;
        }
        if (dir == Direction.SOUTH) {
            ny = (int) current.y + 1;
        }
        if (dir == Direction.WEST) {
            nx = (int) current.x - 1;
        }
        if (dir == Direction.EAST) {
            nx = (int) current.x + 1;
        }

        if (nx > this.map.getBaseMap().getWidth() - 1) {
            nx = 0;
        }

        if (nx < 0) {
            nx = this.map.getBaseMap().getWidth() - 1;
        }

        if (ny > this.map.getBaseMap().getHeight() - 1) {
            ny = 0;
        }

        if (ny < 0) {
            ny = this.map.getBaseMap().getHeight() - 1;
        }

        TiledMapTileLayer grass = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("grass");
        TiledMapTileLayer.Cell c1 = grass.getCell(nx, this.map.getBaseMap().getHeight() - 1 - ny);

        TiledMapTileLayer meadow = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("meadow");
        TiledMapTileLayer.Cell c2 = meadow.getCell(nx, this.map.getBaseMap().getHeight() - 1 - ny);

        TiledMapTileLayer forest = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("forest");
        TiledMapTileLayer.Cell c3 = forest.getCell(nx, this.map.getBaseMap().getHeight() - 1 - ny);

        if (c1 == null && c2 == null && c3 == null) {
            return null;
        }

        if (dir == Direction.NORTH) {
            if (newMapPixelCoords.y + WORLD_TILE_DIM >= this.map.getBaseMap().getHeight() * WORLD_TILE_DIM) {
                newMapPixelCoords.y = 0;
            } else {
                newMapPixelCoords.y += WORLD_TILE_DIM;
            }
        } else if (dir == Direction.SOUTH) {
            if (newMapPixelCoords.y - WORLD_TILE_DIM < 0) {
                newMapPixelCoords.y = (this.map.getBaseMap().getHeight() - 1) * WORLD_TILE_DIM;
            } else {
                newMapPixelCoords.y -= WORLD_TILE_DIM;
            }
        } else if (dir == Direction.EAST) {
            if (newMapPixelCoords.x + WORLD_TILE_DIM >= this.map.getBaseMap().getWidth() * WORLD_TILE_DIM) {
                newMapPixelCoords.x = 0;
            } else {
                newMapPixelCoords.x += WORLD_TILE_DIM;
            }
        } else if (dir == Direction.WEST) {
            if (newMapPixelCoords.x - WORLD_TILE_DIM < 0) {
                newMapPixelCoords.x = (this.map.getBaseMap().getWidth() - 1) * WORLD_TILE_DIM;
            } else {
                newMapPixelCoords.x -= WORLD_TILE_DIM;
            }
        }

        renderer.getFOV().calculateFOV(nx, ny, 20f);

        return new Vector3(nx, ny, 0);
    }

    private void postMove(Vector3 position) {

        //check for active moongate portal
        for (Moongate g : Moongate.values()) {
            if (g.getCurrentTexture() != null && position.x == g.getMapX() && position.y == g.getMapY()) {
                Sounds.play(Sound.WAVE);
                Moongate d = getDestinationForMoongate(g);
                newMapPixelCoords.x = d.getX();
                newMapPixelCoords.y = d.getY();
                break;
            }
        }

        CTX.endTurn(this.map);
    }

    @Override
    public void finishTurn(int currentX, int currentY) {

    }

    @Override
    public void teleport(int level, int stepsX, int stepsY) {

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

        for (Moongate g : Moongate.values()) {
            g.setCurrentTexture(null);
        }

        Moongate gate = Moongate.values()[trammelphase];
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

    private Moongate getDestinationForMoongate(Moongate m) {

        int destGate = m.ordinal();

        if (feluccaphase == m.getD1()) {
            destGate = m.getD1();
        } else if (feluccaphase == m.getD2()) {
            destGate = m.getD2();
        } else if (feluccaphase == m.getD3()) {
            destGate = m.getD3();
        }

        return Moongate.values()[destGate];
    }

}
