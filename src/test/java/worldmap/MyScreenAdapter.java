package worldmap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public abstract class MyScreenAdapter extends ScreenAdapter implements InputProcessor {

    public OrthographicCamera cam;
    public SpriteBatch batch;
    public ShapeRenderer shape;
    public ExtendViewport viewport;

    private final static float SCALE = 1f;
    private final static float INV_SCALE = 1.f / SCALE;
    private final static float VIEWPORT_WIDTH = 1280 * INV_SCALE;
    private final static float VIEWPORT_HEIGHT = 720 * INV_SCALE;

    private static int prevWindowWidth = (int) VIEWPORT_WIDTH;
    private static int prevWindowHeight = (int) VIEWPORT_HEIGHT;

    private static boolean vsync = true;

    private static float zoomTarget = 1;
    private static float zoomSpeed = 3;

    public MyScreenAdapter() {

        cam = new OrthographicCamera();
        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        viewport = new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, cam);
        viewport.apply();

        cam.zoom = 1;
        setZoomTarget(1);

        Gdx.input.setInputProcessor(this);

        toggleVsync();
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glLineWidth(1);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        cam.update();
        batch.setProjectionMatrix(cam.combined);
        shape.setProjectionMatrix(cam.combined);

        zoomCamera(delta);

        if (Gdx.input.isButtonPressed(Buttons.MIDDLE)) {
            cam.zoom = 1;
            setZoomTarget(1);
        }

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        shape.dispose();
        batch.dispose();
    }

    @Override
    public boolean scrolled(float sx, float sy) {
        setZoomTarget(cam.zoom += sx / 2f);
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    public static void toggleFullscreen() {
        if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(prevWindowWidth, prevWindowHeight);
        } else {
            prevWindowWidth = Gdx.graphics.getWidth();
            prevWindowHeight = Gdx.graphics.getHeight();
            if (Gdx.graphics.supportsDisplayModeChange()) {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }
    }

    public static void toggleVsync() {
        vsync = !vsync;
        Gdx.graphics.setVSync(vsync);
    }

    private void zoomCamera(float delta) {
        if (cam.zoom != zoomTarget) {
            float scaleSpeed = zoomSpeed * delta;
            cam.zoom += (cam.zoom < zoomTarget) ? scaleSpeed : -scaleSpeed;

            if (Math.abs(cam.zoom - zoomTarget) < 0.2) {
                cam.zoom = zoomTarget;
            }
        }
    }

    public static void setZoomTarget(float zoom) {
        zoomTarget = zoom;
    }
}
