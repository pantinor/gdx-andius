
import andius.Andius;
import andius.objects.MonsterModels;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;

public class ModelVisualizer implements ApplicationListener, InputProcessor {

    private ModelBatch modelBatch;
    private CameraInputController inputController;
    private PerspectiveCamera cam;

    private final Environment outside = new Environment();

    private Vector3 lightTarget = new Vector3();
    private java.util.List<DirectionalLight> directionalLightsDown = new ArrayList<>();
    private java.util.List<DirectionalLight> directionalLightsUp = new ArrayList<>();

    public BitmapFont font;
    private SpriteBatch batch;

    private Color flame = new Color(0xf59414ff);

    private java.util.List<ModelInstance> modelInstances = new ArrayList<>();

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "ModelVisualizer";
        cfg.useGL30 = false;
        cfg.width = 1280;
        cfg.height = 768;
        new LwjglApplication(new ModelVisualizer(), cfg);
    }

    @Override
    public void create() {

        Andius a = new Andius();
        a.create();

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.near = 0.1f;
        cam.far = 1000;
        cam.update();

        inputController = new CameraInputController(cam) {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                return super.scrolled(amountX, amountY * 0.10f);
            }
        };
        inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
        inputController.translateUnits = 30f;

        font = new BitmapFont();
        batch = new SpriteBatch();

        Gdx.input.setInputProcessor(new InputMultiplexer(inputController));

        this.outside.set(ColorAttribute.createAmbient(0.8f, 0.8f, 0.8f, 1f));

        DefaultShader.Config config = new DefaultShader.Config();
        config.vertexShader = Gdx.files.internal("assets/dungeon.vertex.glsl").readString();
        config.fragmentShader = Gdx.files.internal("assets/dungeon.fragment.glsl").readString();
        config.numDirectionalLights = 12;

        this.modelBatch = new ModelBatch(new DefaultShaderProvider(config));

        //this.models[25] = ObjLoader.loadModel("assets/graphics/door.obj", "door", .1f);
        //this.models[26] = ObjLoader.loadModel("assets/graphics/letter-m.obj", "letter-m", .1f);
        //this.models[27] = ObjLoader.loadModel("assets/graphics/fountain.obj", "fountain", .1f);
        //this.models[28] = ObjLoader.loadModel("assets/graphics/pentagram.obj", "pentagram", .1f);
        //this.models[29] = ObjLoader.loadModel("assets/graphics/elevator.obj", "elevator-booth", .1f);
        final int MODELS_PER_ROW = 5;
        final float SPACING = 1.0f;

        for (int i = 0; i < MonsterModels.values().length; i++) {
            MonsterModels mm = MonsterModels.values()[i];
            System.out.println(mm);
            ModelInstance mi = new ModelInstance(mm.model());
            this.modelInstances.add(mi);

            int row = i / MODELS_PER_ROW;
            int col = i % MODELS_PER_ROW;

            float x = 0.5f + col * SPACING;
            float z = 0.5f + row * SPACING;

            mi.transform.setToTranslation(x, 0f, z);
        }

        computeModelsCenter(this.lightTarget);

        float LIGHT_RING_RADIUS = 6.0f;
        float LIGHT_HEIGHT = 8.0f;
        float LIGHT_INTENSITY = 0.34f;

        addHexRingDirectionalLights(this.outside, this.lightTarget, LIGHT_RING_RADIUS, LIGHT_HEIGHT,
                true, LIGHT_INTENSITY, this.directionalLightsDown); // from above, aiming down

        addHexRingDirectionalLights(this.outside, this.lightTarget, LIGHT_RING_RADIUS, LIGHT_HEIGHT,
                false, LIGHT_INTENSITY, this.directionalLightsUp);   // from below, aiming up

        createAxes();

        cam.position.set(1, 0.5f, 6);
        cam.lookAt(1, 0.5f, 0);

    }

    private void computeModelsCenter(Vector3 out) {
        out.set(0, 0, 0);
        if (this.modelInstances.isEmpty()) {
            return;
        }

        Vector3 tmp = new Vector3();
        for (ModelInstance mi : this.modelInstances) {
            mi.transform.getTranslation(tmp);
            out.add(tmp);
        }
        out.scl(1f / this.modelInstances.size());

        out.y = 0.5f;
    }

    private void addHexRingDirectionalLights(Environment env,
            Vector3 target,
            float ringRadius,
            float height,
            boolean fromAbove,
            float intensity,
            java.util.List<DirectionalLight> outList) {

        outList.clear();

        Vector3 dir = new Vector3();
        for (int i = 0; i < 6; i++) {
            float angleDeg = i * 60f;
            float lx = target.x + MathUtils.cosDeg(angleDeg) * ringRadius;
            float lz = target.z + MathUtils.sinDeg(angleDeg) * ringRadius;
            float ly = target.y + (fromAbove ? height : -height);

            dir.set(target.x - lx, target.y - ly, target.z - lz).nor();

            DirectionalLight light = new DirectionalLight().set(intensity, intensity, intensity, dir.x, dir.y, dir.z);

            env.add(light);
            outList.add(light);
        }
    }

    @Override
    public void render() {
        cam.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);

        modelBatch.render(axesInstance);

        for (ModelInstance m : this.modelInstances) {
            if (m != null) {
                modelBatch.render(m, outside);
            }
        }

        modelBatch.end();

        batch.begin();
        font.draw(batch, "" + cam.position, 280, 700);
        batch.end();
    }

    final float GRID_MIN = 0f;
    final float GRID_MAX = 20f;
    final float GRID_STEP = .5f;
    public Model axesModel;
    public ModelInstance axesInstance;

    private void createAxes() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        // grid
        MeshPartBuilder builder = modelBuilder.part("grid", GL30.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, new Material());
        builder.setColor(Color.LIGHT_GRAY);
        for (float t = GRID_MIN; t <= GRID_MAX; t += GRID_STEP) {
            builder.line(t, 0, GRID_MIN, t, 0, GRID_MAX);
            builder.line(GRID_MIN, 0, t, GRID_MAX, 0, t);
        }

        // axes
        builder = modelBuilder.part("axes", GL30.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, new Material());
        builder.setColor(Color.RED);
        builder.line(0, 0, 0, 500, 0, 0);
        builder.setColor(Color.GREEN);
        builder.line(0, 0, 0, 0, 500, 0);
        builder.setColor(Color.BLUE);
        builder.line(0, 0, 0, 0, 0, 500);

        axesModel = modelBuilder.end();
        axesInstance = new ModelInstance(axesModel);
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
    public boolean touchDown(int x, int y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean scrolled(float f, float f1) {
        return false;
    }

}
