package andius;

import andius.objects.Direction;
import static andius.Andius.CTX;
import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import andius.WizardryData.Scenario;
import andius.objects.ClassType;
import andius.objects.Item;
import andius.objects.Portal;
import andius.objects.SaveGame;
import andius.objects.Spells;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import utils.FrameMaker;
import utils.SpreadFOV;
import utils.WrappingTileMapRenderer;

public class OverworldScreen extends BaseScreen {

    private static final int SCALED_DIM = 32;
    private static final int VIEWPORT_DIM = SCALED_DIM * 17;

    private final Map map;
    private final WrappingTileMapRenderer renderer;
    private final Batch batch;
    private final Viewport mapViewPort;
    private final Texture background;
    private final int width, height;

    public OverworldScreen(Map map) {

        this.map = map;

        FrameMaker fm = new FrameMaker(SCREEN_WIDTH, SCREEN_HEIGHT);
        Texture frame = new Texture(Gdx.files.classpath("assets/data/world_frame.png"));
        frame.getTextureData().prepare();
        fm.drawPixmap(frame.getTextureData().consumePixmap(), 0, 0);
        fm.emptyFrame(SCALED_DIM * 3, SCALED_DIM * 3, VIEWPORT_DIM, VIEWPORT_DIM);
        this.background = fm.build();

        this.width = this.map.getBaseMap().getWidth();
        this.height = this.map.getBaseMap().getHeight();
        this.mapPixelHeight = height * SCALED_DIM;

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
                shadowMap[x][y] = (val == 3 || val == 4 ? 1 : 0);//forest or mountains block visibility
            }
        }

        SpreadFOV fov = new SpreadFOV(shadowMap);
        this.renderer = new WrappingTileMapRenderer(this.map, this.map.getTiledMap(), fov, 2f);
    }

    @Override
    public void save(SaveGame saveGame) {
        Vector3 v = new Vector3();
        getCurrentMapCoords(v);
        CTX.saveGame.map = Map.WORLD;
        CTX.saveGame.level = 0;
        CTX.saveGame.direction = Direction.NORTH;
    }

    @Override
    public void load(SaveGame saveGame) {
        setMapPixelCoords(newMapPixelCoords, saveGame.wx, saveGame.wy, 0);
        renderer.getFOV().calculateFOV(saveGame.wx, saveGame.wy, 72);
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

        Gdx.gl.glClearColor(0x18 / 255f, 0x18 / 255f, 0x18 / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (this.renderer == null) {
            return;
        }

        this.camera.position.x = newMapPixelCoords.x + SCALED_DIM * 5;
        this.camera.position.y = newMapPixelCoords.y + SCALED_DIM * 1;

        this.camera.update();

        this.renderer.setView(camera.combined,
                camera.position.x - SCALED_DIM * 13,
                camera.position.y - SCALED_DIM * 9,
                VIEWPORT_DIM,
                VIEWPORT_DIM);

        this.renderer.render();

        batch.begin();

        batch.draw(this.background, 0, 0);
        batch.draw(Andius.world_scr_avatar.getKeyFrame(time, true), SCALED_DIM * 11, SCALED_DIM * 11, SCALED_DIM, SCALED_DIM);

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
        //shapeRenderer.box(camera.position.x, camera.position.y, 0, SCALED_DIM, SCALED_DIM, 0);
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
        v.set(x * SCALED_DIM, mapPixelHeight - SCALED_DIM - y * SCALED_DIM, 0);
    }

    @Override
    public void getCurrentMapCoords(Vector3 v) {
        Vector3 tmp = camera.unproject(new Vector3(SCALED_DIM * 11, SCALED_DIM * 11, 0), SCALED_DIM * 3, SCALED_DIM * 3, VIEWPORT_DIM, VIEWPORT_DIM);
        float y = Math.round((mapPixelHeight - tmp.y) / SCALED_DIM) + 2;
        float x = Math.round(tmp.x / SCALED_DIM) - 4;
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
                        showEnterWiz4Prompt(stage, () -> Andius.mainGame.setScreen(p.getMap().getScreen()));
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

    private void showEnterWiz4Prompt(Stage stage, Runnable onConfirm) {
        Dialog dialog = new Dialog("Enter Dungeon?", Andius.skin) {
            @Override
            protected void result(Object obj) {
                boolean yes = Boolean.TRUE.equals(obj);
                if (yes && onConfirm != null) {
                    SaveGame.CharacterRecord c = Andius.CTX.players()[0];
                    c.level = 1;
                    c.exp = 0;

                    c.hp = c.getMoreHP();
                    c.maxhp = c.hp;

                    c.knownSpells.clear();
                    for (int i = 0; i < c.magePoints.length; i++) {
                        c.magePoints[i] = 0;
                    }
                    for (int i = 0; i < c.clericPoints.length; i++) {
                        c.clericPoints[i] = 0;
                    }

                    c.knownSpells.add(Spells.values()[1]);
                    c.knownSpells.add(Spells.values()[3]);
                    c.magePoints[0] = 2;

                    SaveGame.setSpellPoints(c);

                    onConfirm.run();
                }
            }
        };
        dialog.text("Are you sure you want to proceed into Werdna's realm at this time?  Your experience level will start at 1.");
        dialog.button("Enter", true);
        dialog.button("Stay", false);
        dialog.key(Input.Keys.ENTER, true);
        dialog.key(Input.Keys.Y, true);
        dialog.key(Input.Keys.ESCAPE, false);
        dialog.key(Input.Keys.N, false);
        dialog.show(stage);
        dialog.setMovable(false);
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
            if (newMapPixelCoords.y + SCALED_DIM >= this.map.getBaseMap().getHeight() * SCALED_DIM) {
                newMapPixelCoords.y = 0;
            } else {
                newMapPixelCoords.y += SCALED_DIM;
            }
        } else if (dir == Direction.SOUTH) {
            if (newMapPixelCoords.y - SCALED_DIM < 0) {
                newMapPixelCoords.y = (this.map.getBaseMap().getHeight() - 1) * SCALED_DIM;
            } else {
                newMapPixelCoords.y -= SCALED_DIM;
            }
        } else if (dir == Direction.EAST) {
            if (newMapPixelCoords.x + SCALED_DIM >= this.map.getBaseMap().getWidth() * SCALED_DIM) {
                newMapPixelCoords.x = 0;
            } else {
                newMapPixelCoords.x += SCALED_DIM;
            }
        } else if (dir == Direction.WEST) {
            if (newMapPixelCoords.x - SCALED_DIM < 0) {
                newMapPixelCoords.x = (this.map.getBaseMap().getWidth() - 1) * SCALED_DIM;
            } else {
                newMapPixelCoords.x -= SCALED_DIM;
            }
        }

        renderer.getFOV().calculateFOV(nx, ny, 72);

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
        this.renderer.getFOV().calculateFOV(east, north, 72);
        CTX.saveGame.wx = east;
        CTX.saveGame.wy = north;
    }

    @Override
    public void partyDeath() {
    }

}
