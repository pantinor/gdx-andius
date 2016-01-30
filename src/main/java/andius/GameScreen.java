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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import utils.TmxMapRenderer;
import utils.Utils;

public class GameScreen extends BaseScreen {

    private final Map map;
    private TmxMapRenderer renderer;
    private Batch mapBatch, batch;
    private final Viewport mapViewPort;

    private Texture t1, t2;

    public GameScreen(Map map) {
        
        this.map = map;

        batch = new SpriteBatch();

        stage = new Stage(viewport);

        camera = new OrthographicCamera(Andius.MAP_VIEWPORT_DIM, Andius.MAP_VIEWPORT_DIM);

        mapViewPort = new ScreenViewport(camera);

        addButtons();

        t1 = Utils.fillRectangle(TILE_DIM, TILE_DIM, Color.RED, .3f);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
        loadMap(11, 10);
    }

    private void loadMap(int x, int y) {

        if (renderer != null) {
            renderer.dispose();
        }

        renderer = new TmxMapRenderer(Andius.CONTEXT, null, this.map, this.map.getTiledMap(), 1f);
        mapBatch = renderer.getBatch();

        mapPixelHeight = this.map.getMap().getHeight() * TILE_DIM;

//        renderer.getFOV().calculateFOV(this.map.getMap().getShadownMap(), x, y, 25f);
        
        newMapPixelCoords = getMapPixelCoords(x, y);
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

        if (keycode == Keys.UP) {
            if (newMapPixelCoords.y + TILE_DIM >= this.map.getMap().getHeight() * TILE_DIM) {
                newMapPixelCoords.y = 0;
            } else {
                newMapPixelCoords.y = newMapPixelCoords.y + TILE_DIM;
            }
        } else if (keycode == Keys.DOWN) {
            if (newMapPixelCoords.y - TILE_DIM < 0) {
                newMapPixelCoords.y = (this.map.getMap().getHeight() - 1) * TILE_DIM;
            } else {
                newMapPixelCoords.y = newMapPixelCoords.y - TILE_DIM;
            }
        } else if (keycode == Keys.RIGHT) {
            if (newMapPixelCoords.x + TILE_DIM >= this.map.getMap().getWidth() * TILE_DIM) {
                newMapPixelCoords.x = 0;
            } else {
                newMapPixelCoords.x = newMapPixelCoords.x + TILE_DIM;
            }
        } else if (keycode == Keys.LEFT) {
            if (newMapPixelCoords.x - TILE_DIM < 0) {
                newMapPixelCoords.x = (this.map.getMap().getWidth() - 1) * TILE_DIM;
            } else {
                newMapPixelCoords.x = newMapPixelCoords.x - TILE_DIM;
            }
        }
        
//        Vector3 v = getCurrentMapCoords();
//        renderer.getFOV().calculateFOV(this.map.getMap().getShadownMap(), (int)v.x, (int)v.y, 25f);

        return false;
    }

    @Override
    public void finishTurn(int currentX, int currentY) {

    }

    @Override
    public void partyDeath() {
    }

}
