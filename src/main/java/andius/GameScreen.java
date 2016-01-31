package andius;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
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
import utils.TmxMapRenderer;
import utils.Utils;

public class GameScreen extends BaseScreen {

    private final Map map;
    private final TmxMapRenderer renderer;
    private final Batch mapBatch, batch;
    private final Viewport mapViewPort;

    private Texture t1;

    public GameScreen(Map map) {

        this.map = map;

        batch = new SpriteBatch();

        stage = new Stage(viewport);

        camera = new OrthographicCamera(Andius.MAP_VIEWPORT_DIM, Andius.MAP_VIEWPORT_DIM);

        mapViewPort = new ScreenViewport(camera);

        addButtons();

        t1 = Utils.fillRectangle(TILE_DIM, TILE_DIM, Color.RED, .3f);
        
        renderer = new TmxMapRenderer(Andius.CONTEXT, null, this.map, this.map.getTiledMap(), 1f);
        mapBatch = renderer.getBatch();

        mapPixelHeight = this.map.getMap().getHeight() * TILE_DIM;

        newMapPixelCoords = getMapPixelCoords(this.map.getStartX(), this.map.getStartY());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
    }
    
    @Override
    public void hide() {
        
    }

    @Override
    public void render(float delta) {

        time += delta;

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (renderer == null) {
            return;
        }

        camera.position.set(newMapPixelCoords.x + 3 * TILE_DIM + 24 + 8, newMapPixelCoords.y - 1 * TILE_DIM, 0);

        camera.update();

        renderer.setView(camera.combined,
                camera.position.x - TILE_DIM * 10,
                camera.position.y - TILE_DIM * 6,
                Andius.MAP_VIEWPORT_DIM,
                Andius.MAP_VIEWPORT_DIM);

        renderer.render();

        batch.begin();

        batch.draw(Andius.backGround, 0, 0);
        batch.draw(t1, TILE_DIM * 7, TILE_DIM * 8);

        //Vector3 v = getCurrentMapCoords();
        //Andius.font.draw(batch, String.format("%s, %s\n",v.x,v.y), 200, Andius.SCREEN_HEIGHT - 32);
        batch.end();

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        mapViewPort.update(width, height, false);
    }

    @Override
    public Vector3 getMapPixelCoords(int x, int y) {
        Vector3 v = new Vector3(x * TILE_DIM, mapPixelHeight - y * TILE_DIM, 0);
        return v;
    }

    @Override
    public Vector3 getCurrentMapCoords() {
        Vector3 v = camera.unproject(new Vector3(TILE_DIM * 7, TILE_DIM * 8, 0), 48, 96, Andius.MAP_VIEWPORT_DIM, Andius.MAP_VIEWPORT_DIM);
        return new Vector3(Math.round(v.x / TILE_DIM) - 3, ((mapPixelHeight - Math.round(v.y) - TILE_DIM) / TILE_DIM) - 0, 0);
    }

    @Override
    public boolean keyUp(int keycode) {
        Vector3 v = getCurrentMapCoords();

        if (keycode == Keys.UP) {
            if (!preMove(v, Direction.NORTH)) {
                return false;
            }
            if (newMapPixelCoords.y + TILE_DIM >= this.map.getMap().getHeight() * TILE_DIM) {
                newMapPixelCoords.y = 0;
            } else {
                newMapPixelCoords.y = newMapPixelCoords.y + TILE_DIM;
            }
        } else if (keycode == Keys.DOWN) {
            if (!preMove(v, Direction.SOUTH)) {
                return false;
            }
            if (newMapPixelCoords.y - TILE_DIM < 0) {
                newMapPixelCoords.y = (this.map.getMap().getHeight() - 1) * TILE_DIM;
            } else {
                newMapPixelCoords.y = newMapPixelCoords.y - TILE_DIM;
            }
        } else if (keycode == Keys.RIGHT) {
            if (!preMove(v, Direction.EAST)) {
                return false;
            }
            if (newMapPixelCoords.x + TILE_DIM >= this.map.getMap().getWidth() * TILE_DIM) {
                newMapPixelCoords.x = 0;
            } else {
                newMapPixelCoords.x = newMapPixelCoords.x + TILE_DIM;
            }
        } else if (keycode == Keys.LEFT) {
            if (!preMove(v, Direction.WEST)) {
                return false;
            }
            if (newMapPixelCoords.x - TILE_DIM < 0) {
                newMapPixelCoords.x = (this.map.getMap().getWidth() - 1) * TILE_DIM;
            } else {
                newMapPixelCoords.x = newMapPixelCoords.x - TILE_DIM;
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

        if (this.map.getBorderType() == MapBorderBehavior.EXIT) {
            if (nx > this.map.getMap().getWidth() - 1 || nx < 0 || ny > this.map.getMap().getHeight() - 1 || ny < 0) {

                //remove any city/town actors (chests) from the map we are leaving
//                for (Actor a : mapObjectsStage.getActors()) {
//                    if (a instanceof Drawable) {
//                        Drawable d = (Drawable) a;
//                        if (d.getMapId() != Maps.WORLD.getId() && d.getMapId() == bm.getId()) {
//                            d.remove();
//                        }
//                    }
//                }

                Andius.mainGame.setScreen(Map.WORLD.getScreen());
                
                return false;

            }
        }
        
        TiledMapTileLayer layer = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("floor");
        TiledMapTileLayer.Cell cell = layer.getCell(nx, this.map.getMap().getWidth() - 1 - ny);
        if (cell == null) {
            //Sounds.play(Sound.BLOCKED);
            return false;
        }

        return true;
    }

    @Override
    public void finishTurn(int currentX, int currentY) {

    }

    @Override
    public void partyDeath() {
    }

}
