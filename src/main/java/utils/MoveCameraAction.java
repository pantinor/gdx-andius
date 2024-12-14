package utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;

public class MoveCameraAction extends TemporalAction {

    private final Camera camera;
    private final float startX;
    private final float startY;
    private final float endX;
    private final float endY;

    public MoveCameraAction(Camera camera, float duration, float endX, float endY) {
        super(duration);
        this.camera = camera;
        this.endX = endX;
        this.endY = endY;
        this.startX = camera.position.x;
        this.startY = camera.position.z;
    }

    @Override
    protected void update(float percent) {
        float x = startX + (endX - startX) * percent;
        float y = startY + (endY - startY) * percent;
        this.camera.position.set(x, .5f, y);
    }

}
