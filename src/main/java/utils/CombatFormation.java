package utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import java.util.ArrayList;
import java.util.List;

public final class CombatFormation {

    private static final float VIEW_FILL = .96f;
    private static final float DEFAULT_PITCH_DEGREES = 8;

    private CombatFormation() {
    }

    public record Layout(List<Vector3> positions, BoundingBox bounds, Vector3 cameraPosition, Vector3 cameraTarget) {

    }

    public static BoundingBox cameraFacingFootprint(BoundingBox bounds) {
        float x = Math.max(Math.abs(bounds.min.x), Math.abs(bounds.max.x));
        float z = Math.max(Math.abs(bounds.min.z), Math.abs(bounds.max.z));
        float radius = (float) Math.sqrt(x * x + z * z);
        return new BoundingBox(new Vector3(-radius, bounds.min.y, -radius),
                new Vector3(radius, bounds.max.y, radius));
    }

    public static float facingYaw(Vector3 position, Vector3 camera) {
        return MathUtils.atan2(camera.x - position.x, camera.z - position.z) * MathUtils.radiansToDegrees;
    }

    public static Layout fitBounds(BoundingBox bounds, float aspect, float verticalFov, float near) {
        return fitBounds(bounds, aspect, verticalFov, near, DEFAULT_PITCH_DEGREES);
    }

    public static Layout fitBounds(BoundingBox bounds, float aspect, float verticalFov, float near, float pitchDegrees) {
        float tanY = MathUtils.tan(verticalFov * MathUtils.degreesToRadians / 2) * VIEW_FILL;
        Vector3 back = cameraBack(pitchDegrees);
        return fit(List.of(), bounds, tanY * aspect, tanY, near, back, cameraUp(back));
    }

    public static Layout layoutFacingCamera(List<BoundingBox> models, float aspect, float verticalFov, float near) {
        return layoutFacingCamera(models, aspect, verticalFov, near, DEFAULT_PITCH_DEGREES);
    }

    public static Layout layoutFacingCamera(List<BoundingBox> models, float aspect, float verticalFov, float near, float pitchDegrees) {
        if (models.isEmpty()) {
            return layout(models, aspect, verticalFov, near, pitchDegrees);
        }
        List<BoundingBox> footprints = new ArrayList<>();
        for (BoundingBox model : models) {
            footprints.add(cameraFacingFootprint(model));
        }
        Layout best = null;
        float bestDistance = Float.MAX_VALUE;
        for (int trial = 0; trial < 12; trial++) {
            Layout candidate = layout(footprints, aspect, verticalFov, near, pitchDegrees);
            List<Vector3> positions = candidate.positions;
            List<BoundingBox> turned = new ArrayList<>();
            // Turning and camera fitting depend on one another. Settle them before repacking.
            for (int pass = 0; pass < 12; pass++) {
                turned.clear();
                BoundingBox scene = new BoundingBox().inf();
                for (int i = 0; i < models.size(); i++) {
                    float yaw = facingYaw(positions.get(i), candidate.cameraPosition);
                    BoundingBox bounds = new BoundingBox(models.get(i))
                            .mul(new com.badlogic.gdx.math.Matrix4().setToRotation(Vector3.Y, yaw));
                    turned.add(bounds);
                    scene.ext(new Vector3(bounds.min).add(positions.get(i)));
                    scene.ext(new Vector3(bounds.max).add(positions.get(i)));
                }
                Layout fitted = fitBounds(scene, aspect, verticalFov, near, pitchDegrees);
                if (models.size() == 1) {
                    // A final survivor should still have breathing room around it.
                    Vector3 away = new Vector3(fitted.cameraPosition).sub(fitted.cameraTarget);
                    away.setLength(Math.max(1.2f, away.len() / .70f));
                    fitted = new Layout(List.of(), scene, away.add(fitted.cameraTarget), fitted.cameraTarget);
                }
                candidate = new Layout(positions, scene, fitted.cameraPosition, fitted.cameraTarget);
            }
            // Use the final camera direction for the separation check as well.
            for (int i = 0; i < models.size(); i++) {
                turned.set(i, new BoundingBox(models.get(i)).mul(new com.badlogic.gdx.math.Matrix4()
                        .setToRotation(Vector3.Y, facingYaw(positions.get(i), candidate.cameraPosition))));
            }
            boolean separated = true;
            for (int i = 0; i < models.size(); i++) {
                BoundingBox a = turned.get(i);
                Vector3 pa = positions.get(i);
                for (int j = 0; j < i; j++) {
                    BoundingBox b = turned.get(j);
                    Vector3 pb = positions.get(j);
                    boolean overlapX = a.min.x + pa.x < b.max.x + pb.x && a.max.x + pa.x > b.min.x + pb.x;
                    boolean overlapZ = a.min.z + pa.z < b.max.z + pb.z && a.max.z + pa.z > b.min.z + pb.z;
                    if (overlapX && overlapZ) {
                        separated = false;
                    }
                }
            }
            float distance = candidate.cameraPosition.dst(candidate.cameraTarget);
            if (separated && distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
            for (int i = 0; i < footprints.size(); i++) {
                BoundingBox next = new BoundingBox(turned.get(i));
                if (!separated) {
                    next.ext(footprints.get(i));
                }
                // Small turning tolerance instead of a full 360-degree radius.
                next.min.sub(.015f, 0, .015f);
                next.max.add(.015f, 0, .015f);
                footprints.set(i, next);
            }
        }
        return best;
    }

    public static Layout layout(List<BoundingBox> models, float aspect, float verticalFov, float near) {
        return layout(models, aspect, verticalFov, near, DEFAULT_PITCH_DEGREES);
    }

    public static Layout layout(List<BoundingBox> models, float aspect, float verticalFov, float near, float pitchDegrees) {
        float tanY = MathUtils.tan(verticalFov * MathUtils.degreesToRadians / 2) * VIEW_FILL;
        float tanX = tanY * aspect;
        Vector3 back = cameraBack(pitchDegrees);
        Vector3 up = cameraUp(back);
        if (models.isEmpty()) {
            BoundingBox empty = new BoundingBox(new Vector3(.8f, 0, .3f), new Vector3(1.6f, .6f, 1.1f));
            return fit(List.of(), empty, tanX, tanY, near, back, up);
        }
        float averageSize = 0;
        for (BoundingBox bounds : models) {
            averageSize += Math.max(bounds.getWidth(), bounds.getDepth());
        }
        float gap = Math.max(.04f, averageSize / models.size() * .08f);
        Layout best = null;
        float bestScore = Float.MAX_VALUE;
        for (int columns = 1; columns <= Math.min(6, models.size()); columns++) {
            List<Vector3> positions = new ArrayList<>();
            BoundingBox scene = new BoundingBox().inf();
            float front = .3f;
            // Lay rows out from back to front; encounter order stays front to back.
            int lastRow = (models.size() - 1) / columns;
            Vector3[] offsets = new Vector3[models.size()];
            for (int row = lastRow; row >= 0; row--) {
                int start = row * columns;
                int end = Math.min(start + columns, models.size());
                float width = gap * (end - start - 1);
                float depth = 0;
                for (int i = start; i < end; i++) {
                    width += models.get(i).getWidth();
                    depth = Math.max(depth, models.get(i).getDepth());
                }
                front += depth;
                float left = 1.2f - width / 2;
                for (int i = start; i < end; i++) {
                    BoundingBox bounds = models.get(i);
                    Vector3 offset = new Vector3(left - bounds.min.x, 0, front - bounds.max.z);
                    offsets[i] = offset;
                    scene.ext(new Vector3(bounds.min).add(offset));
                    scene.ext(new Vector3(bounds.max).add(offset));
                    left += bounds.getWidth() + gap;
                }
                front += gap;
            }
            java.util.Collections.addAll(positions, offsets);
            Layout candidate = fit(positions, scene, tanX, tanY, near, back, up);
            float distance = candidate.cameraPosition.dst(candidate.cameraTarget);
            // A close camera is not useful if a front model hides its whole row.
            float score = distance * (1 + 3 * screenOverlap(models, candidate, tanX, tanY, back, up) / models.size());
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    private static Vector3 cameraBack(float pitchDegrees) {
        float pitch = pitchDegrees * MathUtils.degreesToRadians;
        return new Vector3(0, MathUtils.sin(pitch), MathUtils.cos(pitch));
    }

    private static Vector3 cameraUp(Vector3 back) {
        return new Vector3(0, back.z, -back.y);
    }

    private static float screenOverlap(List<BoundingBox> models, Layout layout, float tanX, float tanY, Vector3 back, Vector3 up) {
        List<float[]> rectangles = new ArrayList<>();
        float overlap = 0;
        for (int i = 0; i < models.size(); i++) {
            BoundingBox bounds = models.get(i);
            Vector3 offset = layout.positions.get(i);
            float[] rect = {Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
            for (int corner = 0; corner < 8; corner++) {
                Vector3 relative = new Vector3((corner & 1) == 0 ? bounds.min.x : bounds.max.x,
                        (corner & 2) == 0 ? bounds.min.y : bounds.max.y,
                        (corner & 4) == 0 ? bounds.min.z : bounds.max.z).add(offset).sub(layout.cameraPosition);
                float depth = -relative.dot(back);
                float x = relative.x / (depth * tanX);
                float y = relative.dot(up) / (depth * tanY);
                rect[0] = Math.min(rect[0], x);
                rect[1] = Math.min(rect[1], y);
                rect[2] = Math.max(rect[2], x);
                rect[3] = Math.max(rect[3], y);
            }
            float area = Math.max(.0001f, (rect[2] - rect[0]) * (rect[3] - rect[1]));
            for (float[] previous : rectangles) {
                float width = Math.max(0, Math.min(rect[2], previous[2]) - Math.max(rect[0], previous[0]));
                float height = Math.max(0, Math.min(rect[3], previous[3]) - Math.max(rect[1], previous[1]));
                float otherArea = Math.max(.0001f, (previous[2] - previous[0]) * (previous[3] - previous[1]));
                overlap += width * height / Math.min(area, otherArea);
            }
            rectangles.add(rect);
        }
        return overlap;
    }

    private static Layout fit(List<Vector3> positions, BoundingBox scene, float tanX, float tanY, float near, Vector3 back, Vector3 up) {
        Vector3 center = scene.getCenter(new Vector3());
        float distance = 0;
        for (int corner = 0; corner < 8; corner++) {
            Vector3 relative = new Vector3(
                    (corner & 1) == 0 ? scene.min.x : scene.max.x,
                    (corner & 2) == 0 ? scene.min.y : scene.max.y,
                    (corner & 4) == 0 ? scene.min.z : scene.max.z).sub(center);
            float towardsCamera = relative.dot(back);
            distance = Math.max(distance, towardsCamera + Math.max(near * 2,
                    Math.max(Math.abs(relative.x) / tanX, Math.abs(relative.dot(up)) / tanY)));
        }
        Vector3 camera = new Vector3(back).scl(Math.max(near * 3, distance)).add(center);
        return new Layout(positions, scene, camera, center);
    }
}
