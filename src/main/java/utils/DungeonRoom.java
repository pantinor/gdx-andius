package utils;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the combat room from shared models without taking ownership of them.
 */
public final class DungeonRoom {

    private DungeonRoom() {
    }

    public record Layout(List<ModelInstance> instances, List<ModelInstance> floors, BoundingBox bounds,
            float left, float right, float back, float front, float doorX) {

    }

    /**
     * The floor's upper surface is placed at {@code floorY}.
     */
    public static Layout build(Model wallModel, Model cornerModel, Model floorModel, Model doorModel,
            BoundingBox sceneBounds, Vector3 cameraPosition, float floorY) {
        List<ModelInstance> instances = new ArrayList<>();
        List<ModelInstance> floors = new ArrayList<>();
        BoundingBox floorBounds = new ModelInstance(floorModel).calculateBoundingBox(new BoundingBox());
        float tileWidth = floorBounds.getWidth();
        float tileDepth = floorBounds.getDepth();
        float left = (float) Math.floor((sceneBounds.min.x - .4f) / tileWidth) * tileWidth;
        float right = Math.max(left + tileWidth * 3,
                (float) Math.ceil((sceneBounds.max.x + .4f) / tileWidth) * tileWidth);
        float back = (float) Math.floor(Math.min(0, sceneBounds.min.z - .4f) / tileDepth) * tileDepth;
        float front = (float) Math.ceil(Math.max(sceneBounds.max.z + .5f, cameraPosition.z + .6f) / tileDepth) * tileDepth;
        for (float x = left; x < right - .001f; x += tileWidth) {
            for (float z = back; z < front - .001f; z += tileDepth) {
                ModelInstance floor = new ModelInstance(floorModel);
                floor.transform.setToTranslation(x - floorBounds.min.x, floorY - floorBounds.max.y, z - floorBounds.min.z);
                instances.add(floor);
                floors.add(floor);
            }
        }
        BoundingBox wallFaces = wallFaceBounds(wallModel);
        BoundingBox backWall = new BoundingBox(wallFaces)
                .mul(new Matrix4().setToRotation(Vector3.Y, 90));
        float thickness = Math.min(wallFaces.getWidth(), wallFaces.getDepth());
        BoundingBox cornerFaces = wallFaceBounds(cornerModel);
        float cornerRun = Math.max(cornerFaces.getWidth(), cornerFaces.getDepth()) - thickness;
        for (int side = 0; side < 2; side++) {
            ModelInstance corner = new ModelInstance(cornerModel);
            corner.transform.setToRotation(Vector3.Y, side == 0 ? 180 : 90);
            BoundingBox cornerBounds = new BoundingBox(cornerFaces).mul(corner.transform);
            float x = side == 0 ? left - cornerBounds.min.x - thickness : right - cornerBounds.max.x + thickness;
            corner.transform.setTranslation(x, floorY - cornerBounds.min.y, back - cornerBounds.min.z - thickness);
            instances.add(corner);
        }
        float start = left + cornerRun;
        int doorTile = Math.max(0, Math.round(((left + right) / 2 - start) / backWall.getWidth() - .5f));
        float doorX = start + (doorTile + .5f) * backWall.getWidth();
        for (float x = start; x < right - cornerRun - .001f; x += backWall.getWidth()) {
            addRoomWall(instances, wallModel, 90, x - backWall.min.x, floorY - backWall.min.y, back - backWall.max.z);
        }
        for (int side = 0; side < 2; side++) {
            float angle = side == 0 ? 0 : 180;
            BoundingBox sideWall = new BoundingBox(wallFaces)
                    .mul(new Matrix4().setToRotation(Vector3.Y, angle));
            for (float z = back + cornerRun; z < front - .001f; z += sideWall.getDepth()) {
                float x = side == 0 ? left - sideWall.max.x : right - sideWall.min.x;
                addRoomWall(instances, wallModel, angle, x, floorY - sideWall.min.y, z - sideWall.min.z);
            }
        }
        ModelInstance door = new ModelInstance(doorModel);
        door.transform.setToRotation(Vector3.Y, 180);
        BoundingBox doorBounds = door.calculateBoundingBox(new BoundingBox()).mul(door.transform);
        BoundingBox wood = new BoundingBox().inf();
        for (var node : door.nodes) {
            for (var part : node.parts) {
                if (part.meshPart.id.contains("wood")) {
                    part.meshPart.mesh.extendBoundingBox(wood, part.meshPart.offset, part.meshPart.size, node.globalTransform);
                }
            }
        }
        wood.mul(door.transform);
        door.transform.setTranslation(doorX - (doorBounds.min.x + doorBounds.max.x) / 2,
                floorY - doorBounds.min.y, back + .04f - wood.min.z);
        instances.add(door);

        float wallHeight = Math.max(wallFaces.getHeight(), cornerFaces.getHeight());
        BoundingBox bounds = new BoundingBox(new Vector3(left, floorY, back),
                new Vector3(right, floorY + wallHeight, front));
        return new Layout(List.copyOf(instances), List.copyOf(floors), bounds, left, right, back, front, doorX);
    }

    private static BoundingBox wallFaceBounds(Model model) {
        ModelInstance template = new ModelInstance(model);
        BoundingBox whole = template.calculateBoundingBox(new BoundingBox());
        BoundingBox faces = new BoundingBox().inf();
        Vector3 point = new Vector3();
        for (Mesh mesh : model.meshes) {
            int stride = mesh.getVertexSize() / Float.BYTES;
            int offset = mesh.getVertexAttribute(VertexAttributes.Usage.Position).offset / Float.BYTES;
            float[] vertices = new float[mesh.getNumVertices() * stride];
            mesh.getVertices(vertices);
            for (int i = offset; i < vertices.length; i += stride) {
                point.set(vertices[i], vertices[i + 1], vertices[i + 2]).mul(template.nodes.first().globalTransform);
                if (point.y > whole.min.y + whole.getHeight() * .5f) {
                    faces.ext(point);
                }
            }
        }
        // Keep the original grounding while measuring the footprint from the upper wall.
        faces.min.y = whole.min.y;
        return faces;
    }

    private static void addRoomWall(List<ModelInstance> instances, Model model, float angle, float x, float y, float z) {
        ModelInstance wall = new ModelInstance(model);
        wall.transform.setToRotation(Vector3.Y, angle).setTranslation(x, y, z);
        instances.add(wall);
    }
}
