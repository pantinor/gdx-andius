package andius;

import static andius.Andius.CTX;
import static andius.Andius.mainGame;
import andius.objects.Portal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
import utils.TmxMapRenderer;

public class WorldScreen extends BaseScreen {

    private final Map map;
    private final TmxMapRenderer renderer;
    private final Batch mapBatch, batch;
    private final Viewport mapViewPort;
    private final TextureAtlas moonPhaseAtlas;
    private static int phaseIndex = 0, phaseCount = 0, trammelphase = 0, trammelSubphase = 0, feluccaphase = 0;
    public GameTimer gameTimer = new GameTimer();

    public WorldScreen(Map map) {

        this.map = map;

        batch = new SpriteBatch();

        stage = new Stage(viewport);

        camera = new OrthographicCamera(Andius.MAP_VIEWPORT_DIM, Andius.MAP_VIEWPORT_DIM);

        mapViewPort = new ScreenViewport(camera);

        addButtons(this.map);

        moonPhaseAtlas = new TextureAtlas(Gdx.files.classpath("assets/data/moon-atlas.txt"));

        SequenceAction seq1 = Actions.action(SequenceAction.class);
        seq1.addAction(Actions.delay(.25f));
        seq1.addAction(Actions.run(gameTimer));
        stage.addAction(Actions.forever(seq1));

        renderer = new TmxMapRenderer(this.map, this.map.getTiledMap(), 1f);
        mapBatch = renderer.getBatch();

        mapPixelHeight = this.map.getMap().getHeight() * WORLD_TILE_DIM;

        setMapPixelCoords(newMapPixelCoords, this.map.getStartX(), this.map.getStartY());

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

        camera.position.set(newMapPixelCoords.x + 7 * WORLD_TILE_DIM + 9, newMapPixelCoords.y - 2 * WORLD_TILE_DIM, 0);

        camera.update();

        renderer.setView(camera.combined,
                camera.position.x - WORLD_TILE_DIM * 20,
                camera.position.y - WORLD_TILE_DIM * 12,
                Andius.MAP_VIEWPORT_DIM,
                Andius.MAP_VIEWPORT_DIM);

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

        batch.draw(Andius.backGround, 0, 0);
        batch.draw(Andius.world_scr_avatar.getKeyFrame(time, true), WORLD_TILE_DIM * 14, WORLD_TILE_DIM * 17);

        batch.draw(moonPhaseAtlas.findRegion("PHASE_" + trammelphase), 348, Andius.SCREEN_HEIGHT - 32, 20, 20);
        batch.draw(moonPhaseAtlas.findRegion("PHASE_" + feluccaphase), 372, Andius.SCREEN_HEIGHT - 32, 20, 20);

        Andius.HUD.render(batch, Andius.CTX);

        //Vector3 v = new Vector3();
        //setCurrentMapCoords(v);        
        //Andius.font.draw(batch, String.format("%s, %s\n", v.x, v.y), 200, Andius.SCREEN_HEIGHT - 32);
        
        batch.end();

        stage.act();
        stage.draw();

    }

    @Override
    public void log(String s) {
        Andius.HUD.add(s);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        mapViewPort.update(width, height, false);
    }

    @Override
    public void setMapPixelCoords(Vector3 v, int x, int y) {
        v.set(x * WORLD_TILE_DIM, mapPixelHeight - y * WORLD_TILE_DIM, 0);
    }

    @Override
    public void setCurrentMapCoords(Vector3 v) {
        Vector3 tmp = camera.unproject(new Vector3(WORLD_TILE_DIM * 14, WORLD_TILE_DIM * 17, 0), 48, 96, Andius.MAP_VIEWPORT_DIM, Andius.MAP_VIEWPORT_DIM);
        v.set(Math.round(tmp.x / WORLD_TILE_DIM) - 6, ((mapPixelHeight - Math.round(tmp.y) - WORLD_TILE_DIM) / WORLD_TILE_DIM) - 3, 0);
    }

    public class GameTimer implements Runnable {

        public boolean active = true;

        @Override
        public void run() {
            if (active) {

                updateMoons();

//                if (System.currentTimeMillis() - context.getLastCommandTime() > 20 * 1000) {
//                    keyUp(Keys.SPACE);
//                }
            }
        }
    }

    @Override
    public boolean keyUp(int keycode) {
        Vector3 v = new Vector3();
        setCurrentMapCoords(v);

        if (keycode == Keys.UP) {
            if (!preMove(v, Direction.NORTH)) {
                return false;
            }
            newMapPixelCoords.y = newMapPixelCoords.y + WORLD_TILE_DIM;
            postMove(Direction.NORTH, v.x, v.y - 1);
        } else if (keycode == Keys.DOWN) {
            if (!preMove(v, Direction.SOUTH)) {
                return false;
            }
            newMapPixelCoords.y = newMapPixelCoords.y - WORLD_TILE_DIM;
            postMove(Direction.SOUTH, v.x, v.y + 1);
        } else if (keycode == Keys.RIGHT) {
            if (!preMove(v, Direction.EAST)) {
                return false;
            }
            newMapPixelCoords.x = newMapPixelCoords.x + WORLD_TILE_DIM;
            postMove(Direction.EAST, v.x + 1, v.y);
        } else if (keycode == Keys.LEFT) {
            if (!preMove(v, Direction.WEST)) {
                return false;
            }
            newMapPixelCoords.x = newMapPixelCoords.x - WORLD_TILE_DIM;
            postMove(Direction.WEST, v.x - 1, v.y);
        } else if (keycode == Keys.E) {
            Portal p = this.map.getMap().getPortal((int) v.x, (int) v.y);
            if (p != null) {
                Andius.mainGame.setScreen(p.getMap().getScreen());
            }
        }

        return false;
    }

    private boolean preMove(Vector3 current, Direction dir) {

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

        if (nx > this.map.getMap().getWidth() - 1 || nx < 0 || ny > this.map.getMap().getHeight() - 1 || ny < 0) {
            Andius.mainGame.setScreen(Map.WORLD.getScreen());
            return false;
        }

        TiledMapTileLayer grass = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("grass");
        TiledMapTileLayer.Cell c1 = grass.getCell(nx, this.map.getMap().getHeight() - 1 - ny);
        if (c1 == null) {
            return false;
        }

        return true;
    }

    private void postMove(Direction dir, float newx, float newy) {

        //check for active moongate portal
        for (Moongate g : Moongate.values()) {
            if (g.getCurrentTexture() != null && newx == g.getMapX() && newy == g.getMapY()) {
                Sounds.play(Sound.WAVE);
                Moongate d = getDestinationForMoongate(g);
                newMapPixelCoords.x = d.getX();
                newMapPixelCoords.y = d.getY();
            }
        }
        
        CTX.endTurn(this.map);
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
