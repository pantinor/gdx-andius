
import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

/**
 * 5 spot lights, 5 blue cubes and red walls, using the dungeon screen's shaders.
 * Y is up.
 */
public class SpotLightShadowDemo extends ApplicationAdapter {

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Test";
        cfg.width = SCREEN_WIDTH;
        cfg.height = SCREEN_HEIGHT;
        new LwjglApplication(new SpotLightShadowDemo(), cfg);
    }
    private PerspectiveCamera cam;
    private CameraInputController camController;

    // Geometry
    private Model boxModel;
    private Model wallModel;
    private Model groundModel;
    private final Array<ModelInstance> instances = new Array<>();
    private final Array<Vector3> cubeCenters = new Array<>();

    // Lighting
    private final Array<SpotLight> spotLights = new Array<>();
    private final Environment environment = new Environment();

    // Rendering
    private ModelBatch modelBatch;

    @Override
    public void create() {
        // Camera: Y is up
        cam = new PerspectiveCamera(67f,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
        cam.position.set(0f, 0.5f, 0f);
        cam.lookAt(1f, 0.5f, 1f);
        cam.near = 0.1f;
        cam.far = 50f;
        cam.update();

        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);

        // Models & materials
        ModelBuilder mb = new ModelBuilder();
        Material blue = new Material(ColorAttribute.createDiffuse(Color.BLUE));
        Material red = new Material(ColorAttribute.createDiffuse(Color.RED));
        Material gray = new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY));

        boxModel = mb.createBox(1f, 1f, 1f, blue, Usage.Position | Usage.Normal);
        wallModel = mb.createBox(3f, 4f, 0.1f, red, Usage.Position | Usage.Normal);
        groundModel = mb.createBox(20f, 0.1f, 20f, gray, Usage.Position | Usage.Normal);

        // Ground
        ModelInstance ground = new ModelInstance(groundModel);
        ground.transform.setToTranslation(0f, -0.05f, 0f);
        instances.add(ground);

        // Cubes + red walls
        createCubesAndWalls();

        // 5 spot lights, one over each cube (positive x,y,z)
        createSpotLights();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, .12f, .12f, .12f, 1));
        for (SpotLight light : spotLights) {
            environment.add(light);
        }

        DefaultShader.Config config = new DefaultShader.Config();
        config.vertexShader = Gdx.files.internal("assets/dungeon.vertex.glsl").readString();
        config.fragmentShader = Gdx.files.internal("assets/dungeon.fragment.glsl").readString();
        config.numDirectionalLights = 0;
        config.numPointLights = 0;
        config.numSpotLights = spotLights.size;
        modelBatch = new ModelBatch(new DefaultShaderProvider(config));
    }

    private void createCubesAndWalls() {
        // 5 cubes in positive X/Z, at y = 0.5 (Y up)
        // One directly in front of camera look-at
        Vector3[] centers = new Vector3[]{
            new Vector3(1f, 0.5f, 1f),
            new Vector3(1f, 0.5f, 6f),
            new Vector3(5f, 0.5f, 1f),
            new Vector3(5f, 0.5f, 6f),
            new Vector3(9f, 0.5f, 1f)
        };

        float wallHalfHeight = 2f; // total 4 high
        float fenceRadius = 1.5f;  // distance from cube center to wall center

        for (Vector3 c : centers) {
            cubeCenters.add(new Vector3(c));

            // Blue cube
            ModelInstance cube = new ModelInstance(boxModel);
            cube.transform.setToTranslation(c);
            instances.add(cube);

            // 4 red walls around cube, height 4
            // North / South (no rotation)
            ModelInstance wallN = new ModelInstance(wallModel);
            wallN.transform.setToTranslation(c.x, wallHalfHeight, c.z + fenceRadius);
            instances.add(wallN);

            ModelInstance wallS = new ModelInstance(wallModel);
            wallS.transform.setToTranslation(c.x, wallHalfHeight, c.z - fenceRadius);
            instances.add(wallS);

            // East / West (rotated 90 degrees around Y)
            ModelInstance wallE = new ModelInstance(wallModel);
            wallE.transform.idt()
                    .setToRotation(Vector3.Y, 90f)
                    .trn(c.x + fenceRadius, wallHalfHeight, c.z);
            instances.add(wallE);

            ModelInstance wallW = new ModelInstance(wallModel);
            wallW.transform.idt()
                    .setToRotation(Vector3.Y, 90f)
                    .trn(c.x - fenceRadius, wallHalfHeight, c.z);
            instances.add(wallW);
        }
    }

    private void createSpotLights() {
        // 5 spot lights above each cube, pointing straight down (-Y)
        for (Vector3 center : cubeCenters) {
            SpotLight sl = new SpotLight();

            Vector3 pos = new Vector3(center.x, center.y + 3f, center.z);
            Vector3 dir = new Vector3(0f, -1f, 0f);

            // Color, position, direction, intensity, cutoffAngle(deg), exponent
            // Match the dungeon shader's inverse-square falloff at this demo's larger scale.
            sl.set(Color.WHITE, pos, dir, 25f, 45f, 10f);

            spotLights.add(sl);
        }
    }

    @Override
    public void render() {
        camController.update();

        Gdx.gl.glViewport(0, 0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);
        for (ModelInstance inst : instances) {
            modelBatch.render(inst, environment);
        }
        modelBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.update();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        boxModel.dispose();
        wallModel.dispose();
        groundModel.dispose();
    }

}
