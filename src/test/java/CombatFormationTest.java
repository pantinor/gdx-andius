import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.GdxNativesLoader;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import utils.CombatFormation;

public class CombatFormationTest {
    @Test public void compactFacingLayoutUsesLessSpaceThanCircularReservations() {
        GdxNativesLoader.load();
        BoundingBox dragon = new BoundingBox(new Vector3(-1.4f, 0, -.25f), new Vector3(1.4f, 1.2f, .25f));
        BoundingBox small = new BoundingBox(new Vector3(-.15f, 0, -.1f), new Vector3(.15f, .5f, .1f));
        List<BoundingBox> models = List.of(dragon, dragon, dragon, small, small);
        List<BoundingBox> circles = models.stream().map(CombatFormation::cameraFacingFootprint).toList();
        var reserved = CombatFormation.layout(circles, 794f / 520, 50, .1f);
        var compact = CombatFormation.layoutFacingCamera(models, 794f / 520, 50, .1f);
        assertTrue(compact.cameraPosition().dst(compact.cameraTarget())
                < reserved.cameraPosition().dst(reserved.cameraTarget()) * .95f, "Camera should move noticeably closer");
        for (float aspect : new float[] {.75f, 794f / 520, 2.4f}) verify(models, aspect, true);
    }

    @Test public void cameraFacingFootprintContainsEveryYaw() {
        GdxNativesLoader.load();
        BoundingBox model = new BoundingBox(new Vector3(-1.4f, -.1f, -.8f), new Vector3(1.8f, 1.2f, .9f));
        BoundingBox footprint = CombatFormation.cameraFacingFootprint(model);
        for (int angle = -180; angle <= 180; angle += 5) {
            BoundingBox rotated = new BoundingBox(model).mul(new com.badlogic.gdx.math.Matrix4().setToRotation(Vector3.Y, angle));
            assertTrue(footprint.contains(rotated), "Turning models must stay within their reserved footprint");
        }
    }

    @Test public void wideModelsAreNotStackedBehindOneAnother() {
        BoundingBox dragon = new BoundingBox(new Vector3(-1.4f, 0, -.8f), new Vector3(1.8f, 1.2f, .9f));
        CombatFormation.Layout layout = CombatFormation.layout(List.of(dragon, dragon, dragon), 794f / 520, 50, .1f);
        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 3; j++) {
                assertTrue(Math.abs(layout.positions().get(i).x - layout.positions().get(j).x) >= dragon.getWidth(),
                        "Wide creatures should be spread across the view, not hidden in single file");
            }
        }
    }

    @Test public void variedModelSizesDoNotOverlapAndStayInView() {
        GdxNativesLoader.load();
        for (int count : new int[] {1, 3, 7, 24, 48}) {
            for (float aspect : new float[] {.75f, 794f / 520, 2.4f}) {
                List<BoundingBox> models = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    // Wide dragon, small creature and tall asymmetric creature.
                    models.add(switch (i % 3) {
                        case 0 -> new BoundingBox(new Vector3(-1.4f, -.1f, -.8f), new Vector3(1.8f, 1.2f, .9f));
                        case 1 -> new BoundingBox(new Vector3(-.08f, 0, -.1f), new Vector3(.12f, .3f, .1f));
                        default -> new BoundingBox(new Vector3(-.4f, 0, -.3f), new Vector3(.1f, 3.5f, .7f));
                    });
                }
                verify(models, aspect);
            }
        }
        verify(List.of(), 794f / 520);
    }

    private void verify(List<BoundingBox> models, float aspect) {
        verify(models, aspect, false);
    }

    private void verify(List<BoundingBox> models, float aspect, boolean faceCamera) {
        CombatFormation.Layout layout = faceCamera ? CombatFormation.layoutFacingCamera(models, aspect, 50, .1f)
                : CombatFormation.layout(models, aspect, 50, .1f);
        assertEquals(layout.positions().size(), models.size());
        PerspectiveCamera camera = new PerspectiveCamera(50, aspect * 1000, 1000);
        camera.near = .1f;
        camera.far = 10000;
        camera.position.set(layout.cameraPosition());
        camera.lookAt(layout.cameraTarget());
        camera.update();
        List<BoundingBox> placed = new ArrayList<>();
        for (int i = 0; i < models.size(); i++) {
            Vector3 offset = layout.positions().get(i);
            BoundingBox model = new BoundingBox(models.get(i));
            if (faceCamera) model.mul(new com.badlogic.gdx.math.Matrix4().setToRotation(Vector3.Y,
                    CombatFormation.facingYaw(offset, layout.cameraPosition())));
            BoundingBox box = new BoundingBox(new Vector3(model.min).add(offset), new Vector3(model.max).add(offset));
            for (BoundingBox previous : placed) {
                boolean overlapX = box.min.x < previous.max.x && box.max.x > previous.min.x;
                boolean overlapZ = box.min.z < previous.max.z && box.max.z > previous.min.z;
                assertFalse(overlapX && overlapZ, "Model footprints must be separated");
            }
            placed.add(box);
            for (int corner = 0; corner < 8; corner++) {
                Vector3 point = new Vector3((corner & 1) == 0 ? box.min.x : box.max.x,
                        (corner & 2) == 0 ? box.min.y : box.max.y, (corner & 4) == 0 ? box.min.z : box.max.z);
                camera.project(point, 0, 0, aspect * 1000, 1000);
                assertTrue(point.x >= 0 && point.x <= aspect * 1000, "Horizontal camera fit");
                assertTrue(point.y >= 0 && point.y <= 1000, "Vertical camera fit");
                assertTrue(point.z >= 0 && point.z <= 1, "Near/far camera fit");
            }
        }
    }
}
