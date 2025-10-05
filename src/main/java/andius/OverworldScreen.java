package andius;

import andius.objects.Direction;
import static andius.Andius.CTX;
import andius.WizardryData.Scenario;
import andius.objects.ClassType;
import andius.objects.Item;
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
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import utils.SpreadFOV;
import utils.WrappingTileMapRenderer;

public class OverworldScreen extends BaseScreen {

    private static final int DIM = 16;
    private static final int VIEWPORT_DIM = DIM * 31;

    private final Map map;
    private final WrappingTileMapRenderer renderer;
    private final Batch batch;
    private final Viewport mapViewPort;
    private final Texture frame;
    private final int width, height;

    public OverworldScreen(Map map) {

        this.map = map;

        this.frame = new Texture(Gdx.files.classpath("assets/data/world_frame.png"));

        this.width = this.map.getBaseMap().getWidth();
        this.height = this.map.getBaseMap().getHeight();
        this.mapPixelHeight = height * DIM;

        this.batch = new SpriteBatch();

        this.stage = new Stage(viewport);

        this.camera = new OrthographicCamera();
        this.mapViewPort = new ScreenViewport(camera);

        addButtons(this.map);

        float[][] shadowMap = new float[width][height];
        TiledMapTileLayer mapLayer = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("map");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                TiledMapTileLayer.Cell cell = mapLayer.getCell(x, height - 1 - y);
                int val = cell.getTile().getId();
                shadowMap[x][y] = (val == 4 ? 1 : 0);
            }
        }

        SpreadFOV fov = new SpreadFOV(shadowMap);
        this.renderer = new WrappingTileMapRenderer(this.map, this.map.getTiledMap(), fov, 1f);
    }

    @Override
    public void save(SaveGame saveGame) {
        Vector3 v = new Vector3();
        getCurrentMapCoords(v);
        CTX.saveGame.map = Map.OVERWORLD;
        CTX.saveGame.level = 0;
        CTX.saveGame.direction = Direction.NORTH;
    }

    @Override
    public void load(SaveGame saveGame) {

    }

    @Override
    public void show() {
        Andius.HUD.addActor(this.stage);
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));

        setMapPixelCoords(newMapPixelCoords, CTX.saveGame.wx, CTX.saveGame.wy, 0);
        renderer.getFOV().calculateFOV(CTX.saveGame.wx, CTX.saveGame.wy, 72);
    }

    @Override
    public void hide() {
    }

    //private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    @Override
    public void render(float delta) {

        this.time += delta;

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (this.renderer == null) {
            return;
        }

        this.camera.position.x = newMapPixelCoords.x + DIM * 11;
        this.camera.position.y = newMapPixelCoords.y + DIM * 0;

        this.camera.update();

        this.renderer.setView(camera.combined,
                camera.position.x - DIM * 26,
                camera.position.y - DIM * 15,
                VIEWPORT_DIM,
                VIEWPORT_DIM);

        this.renderer.render();

        batch.begin();

        batch.draw(this.frame, 0, 0);
        batch.draw(Andius.world_scr_avatar.getKeyFrame(time, true), DIM * 21, DIM * 24);

        Andius.HUD.render(batch, Andius.CTX);

        Vector3 v = new Vector3();
        getCurrentMapCoords(v);
        Andius.font14.draw(batch, String.format("East [%s], North [%s]\n", (int) v.x, (int) v.y), 300, Andius.SCREEN_HEIGHT - 45);
        batch.end();

        stage.act();
        stage.draw();

        //Rectangle vb = this.renderer.getViewBounds();
        //Gdx.gl.glLineWidth(1);
        //shapeRenderer.setProjectionMatrix(camera.combined);
        //shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        //shapeRenderer.setColor(255, 255, 0, .50f);//yellow
        //shapeRenderer.box(vb.x, vb.y, 0, vb.width, vb.height, 0);
        //shapeRenderer.box(camera.position.x, camera.position.y, 0, 16, 16, 0);
        //shapeRenderer.end();
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
        v.set(x * DIM, mapPixelHeight - DIM - y * DIM, 0);
    }

    @Override
    public void getCurrentMapCoords(Vector3 v) {
        Vector3 tmp = camera.unproject(new Vector3(336, 368, 0), 96, 624, VIEWPORT_DIM, VIEWPORT_DIM);
        float y = Math.round((mapPixelHeight - tmp.y) / DIM) - 47;
        float x = Math.round(tmp.x / DIM) - 10;
        v.set(x, y, 0);
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
                if (p.getMap() == Map.WIZARDRY4) {
                    if (Andius.CTX.players().length == 1 && Andius.CTX.players()[0].classType == ClassType.MAGE) {
                        Andius.mainGame.setScreen(p.getMap().getScreen());
                    } else {
                        log("An impenetrable force of energy bars your party's passage in the entrance! A solitary mage might have more luck...");
                    }
                } else {
                    Andius.mainGame.setScreen(p.getMap().getScreen());
                }
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

        TiledMapTileLayer mapLayer = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("map");
        TiledMapTileLayer.Cell cell = mapLayer.getCell(nx, this.map.getBaseMap().getHeight() - 1 - ny);
        int tileid = cell.getTile().getId();

        Item wingedboots = CTX.partyHasItem(5, Scenario.WER.ordinal());
        Item markSnake = CTX.partyHasItem(104, Scenario.PMO.ordinal());
        Item markFire = CTX.partyHasItem(105, Scenario.PMO.ordinal());
        Item markForce = CTX.partyHasItem(103, Scenario.PMO.ordinal());

        boolean canFly = (wingedboots != null || markSnake != null || markFire != null || markForce != null);

        if ((tileid == 1 || tileid == 4) && !canFly) {
            return null;
        }

        if (dir == Direction.NORTH) {
            if (newMapPixelCoords.y + DIM >= this.map.getBaseMap().getHeight() * DIM) {
                newMapPixelCoords.y = 0;
            } else {
                newMapPixelCoords.y += DIM;
            }
        } else if (dir == Direction.SOUTH) {
            if (newMapPixelCoords.y - DIM < 0) {
                newMapPixelCoords.y = (this.map.getBaseMap().getHeight() - 1) * DIM;
            } else {
                newMapPixelCoords.y -= DIM;
            }
        } else if (dir == Direction.EAST) {
            if (newMapPixelCoords.x + DIM >= this.map.getBaseMap().getWidth() * DIM) {
                newMapPixelCoords.x = 0;
            } else {
                newMapPixelCoords.x += DIM;
            }
        } else if (dir == Direction.WEST) {
            if (newMapPixelCoords.x - DIM < 0) {
                newMapPixelCoords.x = (this.map.getBaseMap().getWidth() - 1) * DIM;
            } else {
                newMapPixelCoords.x -= DIM;
            }
        }

        renderer.getFOV().calculateFOV(nx, ny, 20f);

        return new Vector3(nx, ny, 0);
    }

    private void postMove(Vector3 position) {
        CTX.saveGame.wx = (int) position.x;
        CTX.saveGame.wy = (int) position.y;
        CTX.endTurn(this.map);
    }

    @Override
    public void finishTurn(int currentX, int currentY) {

    }

    @Override
    public void teleport(int level, int north, int east) {
        setMapPixelCoords(newMapPixelCoords, east, north, 0);
        this.renderer.getFOV().calculateFOV(east, north, 20f);
    }

    @Override
    public void partyDeath() {
    }

}
