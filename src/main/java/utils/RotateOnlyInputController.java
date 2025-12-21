package utils;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public class RotateOnlyInputController extends InputAdapter {

    private final Camera camera;

    public int rotateButton = Input.Buttons.LEFT;
    public float rotateSpeed = 0.5f;

    private int lastX, lastY;
    private int activeButton = -1;

    private final Vector3 tmpRight = new Vector3();

    public RotateOnlyInputController(Camera camera) {
        this.camera = camera;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == rotateButton && activeButton == -1) {
            activeButton = button;
            lastX = screenX;
            lastY = screenY;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (activeButton != rotateButton) return false;

        int deltaX = screenX - lastX;
        int deltaY = screenY - lastY;
        lastX = screenX;
        lastY = screenY;

        rotateCamera(deltaX, deltaY);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == activeButton) {
            activeButton = -1;
            return true;
        }
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        // Disable zoom
        return false;
    }

    private void rotateCamera(float deltaX, float deltaY) {
        float yaw   = -deltaX * rotateSpeed; // left/right
        float pitch = -deltaY * rotateSpeed; // up/down

        camera.rotate(camera.up, yaw);

        tmpRight.set(camera.direction).crs(camera.up).nor();
        camera.rotate(tmpRight, pitch);

        camera.normalizeUp();
        camera.update();
    }
}
