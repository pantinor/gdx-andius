import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OverworldTestMain extends ApplicationAdapter {

    private static final String OBJ = "assets/graphics/overworld.obj";
    private static final float STEP = 1f;
    private static final float CAMERA_DISTANCE = 3f;
    private static final float CAMERA_HEIGHT = 3f;
    private static final float PLAYER_RADIUS = 0.06f;
    private static final double MIN_CLEARANCE = 0.30;
    private static final float MAX_HEIGHT = 0.5f;
    private static final float EPSILON = 0.0001f;
    // Y is up; the map's rows increase towards the south (+Z).
    private static final Vector3[] DIRECTIONS = {
        new Vector3(0, 0, -1), new Vector3(1, 0, 0),
        new Vector3(0, 0, 1), new Vector3(-1, 0, 0)
    };

    private final List<Model> models = new ArrayList<>();
    private final List<ModelInstance> instances = new ArrayList<>();
    private final Environment environment = new Environment();
    private final Vector3 position = new Vector3();
    private final Vector3 lookAt = new Vector3();
    private WorldData world;
    private PerspectiveCamera camera;
    private ModelBatch batch;
    private ModelInstance playerMarker;
    private int facing;

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Overworld - Up/Down: move, Left/Right: turn";
        cfg.width = SCREEN_WIDTH;
        cfg.height = SCREEN_HEIGHT;
        new LwjglApplication(new OverworldTestMain(), cfg);
    }

    @Override
    public void create() {
        FileHandle obj = Gdx.files.classpath(OBJ);
        try {
            world = WorldData.read(obj);
            Map<String, Material> materials = readMaterials(obj.sibling("overworld.mtl"));
            Map<String, Model> parts = new LinkedHashMap<>();
            for (var entry : world.objects.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    Model model = buildModel(entry.getKey(), entry.getValue(), materials);
                    models.add(model);
                    parts.put(entry.getKey(), model);
                }
            }
            instances.add(new ModelInstance(requiredPart(parts, "overworld")));
            for (Placement placement : world.placements) {
                String type = placement.type.equals("dungeon") ? "dungeon_entrance" : placement.type;
                ModelInstance instance = new ModelInstance(requiredPart(parts, type));
                instance.transform.setToTranslation(placement.position);
                instances.add(instance);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + OBJ, exception);
        }

        Model marker = new ModelBuilder().createSphere(PLAYER_RADIUS * 2, PLAYER_RADIUS * 2,
                PLAYER_RADIUS * 2, 16, 8,
                new Material(ColorAttribute.createDiffuse(Color.YELLOW), ColorAttribute.createEmissive(Color.YELLOW)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        models.add(marker);
        playerMarker = new ModelInstance(marker);
        instances.add(playerMarker);

        batch = new ModelBatch();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.6f, 0.6f, 0.6f, 1f));
        environment.add(new DirectionalLight().set(0.7f, 0.7f, 0.65f, -0.5f, -1f, -0.3f));
        camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.01f;
        camera.far = 150f;
        position.set(world.britain()).add(0, 0, 2 * STEP);
        facing = 0;
        updateCamera();
    }

    private static Model requiredPart(Map<String, Model> parts, String name) {
        Model model = parts.get(name);
        if (model == null) {
            throw new IllegalStateException("Missing OBJ object: " + name);
        }
        return model;
    }

    private void move(int sign) {
        Vector3 next = world.nextPosition(position, DIRECTIONS[facing], sign);
        if (next != null) {
            position.set(next);
        }
    }

    private void updateCamera() {
        float ground = world.heightAt(position.x, position.z);
        if (!Float.isFinite(ground)) {
            throw new IllegalStateException("Player is outside the terrain: " + position);
        }
        position.y = ground;
        playerMarker.transform.setToTranslation(position.x, position.y + PLAYER_RADIUS, position.z);
        camera.position.set(position).mulAdd(DIRECTIONS[facing], -CAMERA_DISTANCE).add(0, CAMERA_HEIGHT, 0);
        lookAt.set(position);
        camera.up.set(Vector3.Y);
        camera.lookAt(lookAt);
        camera.update();
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            facing = (facing + 3) % 4;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            facing = (facing + 1) % 4;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            move(1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            move(-1);
        }
        updateCamera();
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        Gdx.gl.glClearColor(0.55f, 0.72f, 0.88f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        batch.begin(camera);
        for (ModelInstance instance : instances) {
            batch.render(instance, environment);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (camera != null && width > 0 && height > 0) {
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
        }
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        for (Model model : models) {
            model.dispose();
        }
    }

    private static Model buildModel(String name, List<Face> faces, Map<String, Material> materials) {
        ModelBuilder builder = new ModelBuilder();
        builder.begin();
        Map<String, List<Face>> groups = new LinkedHashMap<>();
        for (Face face : faces) {
            groups.computeIfAbsent(face.material, ignored -> new ArrayList<>()).add(face);
        }
        Material fallback = new Material(ColorAttribute.createDiffuse(Color.WHITE));
        for (var entry : groups.entrySet()) {
            List<Face> group = entry.getValue();
            // Keep each part small enough for ModelBuilder's 16-bit mesh indices.
            for (int start = 0; start < group.size(); start += 2000) {
                MeshPartBuilder part = builder.part(name + "_" + entry.getKey() + "_" + start,
                        GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                        materials.getOrDefault(entry.getKey(), fallback));
                for (int i = start; i < Math.min(start + 2000, group.size()); i++) {
                    Face face = group.get(i);
                    Vector3 normal = new Vector3(face.b).sub(face.a)
                            .crs(new Vector3(face.c).sub(face.a)).nor();
                    part.triangle(new MeshPartBuilder.VertexInfo().setPos(face.a).setNor(normal),
                            new MeshPartBuilder.VertexInfo().setPos(face.b).setNor(normal),
                            new MeshPartBuilder.VertexInfo().setPos(face.c).setNor(normal));
                }
            }
        }
        return builder.end();
    }

    private static Map<String, Material> readMaterials(FileHandle file) throws IOException {
        Map<String, Material> result = new LinkedHashMap<>();
        try (BufferedReader reader = file.reader(8192)) {
            String name = null;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                if (tokens[0].equals("newmtl")) {
                    name = tokens[1];
                } else if (tokens[0].equals("Kd") && name != null) {
                    result.put(name, new Material(ColorAttribute.createDiffuse(
                            Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3]), 1f)));
                }
            }
        }
        return result;
    }

    private record Face(Vector3 a, Vector3 b, Vector3 c, String material) {
    }

    private record Placement(String name, String type, Vector3 position) {
    }

    static final class WorldData {
        final Map<String, List<Face>> objects = new LinkedHashMap<>();
        final List<Placement> placements = new ArrayList<>();
        final List<Face> blockedFaces = new ArrayList<>();

        static WorldData read(FileHandle file) throws IOException {
            WorldData data = new WorldData();
            List<Vector3> vertices = new ArrayList<>();
            String object = "";
            String group = "";
            String material = "";
            try (BufferedReader reader = file.reader(65536)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.trim().split("\\s+");
                    switch (tokens[0]) {
                        case "o" -> {
                            object = tokens[1];
                            group = "";
                            data.objects.computeIfAbsent(object, ignored -> new ArrayList<>());
                        }
                        case "g" -> group = tokens[1];
                        case "usemtl" -> material = tokens[1];
                        case "v" -> {
                            Vector3 vertex = new Vector3(Float.parseFloat(tokens[1]),
                                    Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
                            vertices.add(vertex);
                            if (object.equals("positions")) {
                                int separator = group.lastIndexOf('-');
                                if (separator <= 0 || separator == group.length() - 1) {
                                    throw new IOException("Expected a name-type position group: " + group);
                                }
                                data.placements.add(new Placement(group.substring(0, separator),
                                        group.substring(separator + 1), vertex));
                            }
                        }
                        case "f" -> {
                            Vector3 a = vertex(vertices, tokens[1]);
                            for (int i = 2; i < tokens.length - 1; i++) {
                                Face face = new Face(a, vertex(vertices, tokens[i]),
                                        vertex(vertices, tokens[i + 1]), material);
                                data.objects.get(object).add(face);
                                if (object.equals("overworld")
                                        && (material.equals("mountain") || material.equals("water"))) {
                                    data.blockedFaces.add(face);
                                }
                            }
                        }
                        default -> { /* Normals are computed per face; other OBJ directives are unused. */ }
                    }
                }
            }
            if (data.objects.getOrDefault("overworld", List.of()).isEmpty()) {
                throw new IOException("Missing overworld terrain faces");
            }
            return data;
        }

        private static Vector3 vertex(List<Vector3> vertices, String token) {
            int index = Integer.parseInt(token.split("/", 2)[0]);
            return vertices.get(index > 0 ? index - 1 : vertices.size() + index);
        }

        Vector3 britain() {
            return placements.stream().filter(p -> p.name.equalsIgnoreCase("BRITAIN"))
                    .findFirst().orElseThrow(() -> new IllegalStateException("Missing BRITAIN position")).position;
        }

        Vector3 nextPosition(Vector3 current, Vector3 direction, int sign) {
            Vector3 target = new Vector3(current).mulAdd(direction, sign * STEP);
            if (validMove(current, target)) {
                return target;
            }
            // Keep the usual one-unit step, shortening only the last approach to
            // an obstacle. Surface positions need not coincide with mesh vertices.
            float low = 0;
            float high = 1;
            Vector3 candidate = new Vector3();
            for (int i = 0; i < 24; i++) {
                float fraction = (low + high) * 0.5f;
                candidate.set(current).mulAdd(direction, sign * STEP * fraction);
                if (validMove(current, candidate)) {
                    low = fraction;
                } else {
                    high = fraction;
                }
            }
            if (low * STEP <= EPSILON) {
                return null;
            }
            candidate.set(current).mulAdd(direction, sign * STEP * low);
            candidate.y = heightAt(candidate.x, candidate.z);
            return candidate;
        }

        private boolean validMove(Vector3 current, Vector3 target) {
            target.y = heightAt(target.x, target.z);
            return Float.isFinite(target.y) && target.y <= MAX_HEIGHT && !blockedPath(current, target);
        }

        boolean blockedPath(Vector3 from, Vector3 to) {
            // Distances are measured across the map (XZ), independent of face
            // elevation, so low water and sloping mountains get the same margin.
            double clearanceSquared = MIN_CLEARANCE * MIN_CLEARANCE;
            for (Face face : blockedFaces) {
                if (intersectsFaceXZ(from, to, face)
                        || edgeDistanceSquaredXZ(from, to, face.a, face.b) < clearanceSquared
                        || edgeDistanceSquaredXZ(from, to, face.b, face.c) < clearanceSquared
                        || edgeDistanceSquaredXZ(from, to, face.c, face.a) < clearanceSquared) {
                    return true;
                }
            }
            return false;
        }

        private static double edgeDistanceSquaredXZ(Vector3 from, Vector3 to, Vector3 a, Vector3 b) {
            if (crossesEdgeXZ(from, to, a, b)) {
                return 0;
            }
            return Math.min(Math.min(pointDistanceSquaredXZ(from, a, b), pointDistanceSquaredXZ(to, a, b)),
                    Math.min(pointDistanceSquaredXZ(a, from, to), pointDistanceSquaredXZ(b, from, to)));
        }

        private static double pointDistanceSquaredXZ(Vector3 point, Vector3 a, Vector3 b) {
            double dx = (double) b.x - a.x;
            double dz = (double) b.z - a.z;
            double lengthSquared = dx * dx + dz * dz;
            double fraction = lengthSquared == 0 ? 0 : Math.max(0, Math.min(1,
                    (((double) point.x - a.x) * dx + ((double) point.z - a.z) * dz) / lengthSquared));
            double offsetX = point.x - (a.x + fraction * dx);
            double offsetZ = point.z - (a.z + fraction * dz);
            return offsetX * offsetX + offsetZ * offsetZ;
        }

        private static boolean intersectsFaceXZ(Vector3 from, Vector3 to, Face face) {
            float abX = face.b.x - face.a.x;
            float abZ = face.b.z - face.a.z;
            float acX = face.c.x - face.a.x;
            float acZ = face.c.z - face.a.z;
            float determinant = abX * acZ - acX * abZ;
            if (Math.abs(determinant) < EPSILON) {
                // A vertical face still blocks a path crossing its projected edge.
                return crossesEdgeXZ(from, to, face.a, face.b)
                        || crossesEdgeXZ(from, to, face.b, face.c)
                        || crossesEdgeXZ(from, to, face.c, face.a);
            }
            float fromX = from.x - face.a.x;
            float fromZ = from.z - face.a.z;
            float toX = to.x - face.a.x;
            float toZ = to.z - face.a.z;
            float startU = (fromX * acZ - acX * fromZ) / determinant;
            float startV = (abX * fromZ - fromX * abZ) / determinant;
            float endU = (toX * acZ - acX * toZ) / determinant;
            float endV = (abX * toZ - toX * abZ) / determinant;
            float[] start = {startU, startV, 1 - startU - startV};
            float[] end = {endU, endV, 1 - endU - endV};
            float enter = 0;
            float exit = 1;
            // Clip the entire movement segment against the triangle's three sides.
            // This catches thin faces between two otherwise walkable endpoints.
            for (int i = 0; i < 3; i++) {
                float delta = end[i] - start[i];
                if (Math.abs(delta) < EPSILON) {
                    if (start[i] < 0) {
                        return false;
                    }
                } else if (delta > 0) {
                    enter = Math.max(enter, -start[i] / delta);
                } else {
                    exit = Math.min(exit, -start[i] / delta);
                }
            }
            // Allow leaving a boundary at t=0; touching it later blocks the move.
            return enter <= exit && exit > EPSILON;
        }

        private static boolean crossesEdgeXZ(Vector3 from, Vector3 to, Vector3 a, Vector3 b) {
            float dx = to.x - from.x;
            float dz = to.z - from.z;
            float ex = b.x - a.x;
            float ez = b.z - a.z;
            float ax = a.x - from.x;
            float az = a.z - from.z;
            float cross = dx * ez - dz * ex;
            if (Math.abs(cross) > EPSILON) {
                float t = (ax * ez - az * ex) / cross;
                float u = (ax * dz - az * dx) / cross;
                return t > EPSILON && t <= 1 && u >= 0 && u <= 1;
            }
            float lengthSquared = dx * dx + dz * dz;
            if (lengthSquared < EPSILON || Math.abs(ax * dz - az * dx) > EPSILON) {
                return false;
            }
            float t0 = (ax * dx + az * dz) / lengthSquared;
            float t1 = ((b.x - from.x) * dx + (b.z - from.z) * dz) / lengthSquared;
            return Math.max(t0, t1) > EPSILON && Math.min(t0, t1) <= 1;
        }

        float heightAt(float x, float z) {
            float highest = Float.NEGATIVE_INFINITY;
            for (Face face : objects.get("overworld")) {
                float abX = face.b.x - face.a.x;
                float abZ = face.b.z - face.a.z;
                float acX = face.c.x - face.a.x;
                float acZ = face.c.z - face.a.z;
                float determinant = abX * acZ - acX * abZ;
                if (Math.abs(determinant) < EPSILON) {
                    continue; // Vertical/degenerate faces have no ground height.
                }
                float px = x - face.a.x;
                float pz = z - face.a.z;
                float u = (px * acZ - acX * pz) / determinant;
                float v = (abX * pz - px * abZ) / determinant;
                if (u >= -EPSILON && v >= -EPSILON && u + v <= 1 + EPSILON) {
                    highest = Math.max(highest,
                            face.a.y + u * (face.b.y - face.a.y) + v * (face.c.y - face.a.y));
                }
            }
            return highest;
        }
    }
}
