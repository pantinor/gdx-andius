package utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;

public class PanCameraAction extends TemporalAction {

    private final Camera camera;
    private final float endAngle;
    private final float startAngle;

    public PanCameraAction(Camera camera, float duration, float startAngle, float endAngle) {
        super(duration);
        this.camera = camera;
        this.endAngle = endAngle;
        this.startAngle = startAngle;
    }

    @Override
    protected void update(float percent) {
        float angle = startAngle + (endAngle - startAngle) * percent;
        this.camera.direction.set((float) Math.sin(Math.toRadians(angle)), 0, (float) -Math.cos(Math.toRadians(angle))).nor();
    }

}
