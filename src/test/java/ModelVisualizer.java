
import andius.Andius;
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
import utils.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

public class ModelVisualizer implements ApplicationListener, InputProcessor {

    private ModelBatch modelBatch;
    private CameraInputController inputController;
    private PerspectiveCamera cam;
    private ShapeRenderer debugRenderer;

    private final Environment outside = new Environment();

    private DirectionalLight directionalLightDown = new DirectionalLight();
    private DirectionalLight directionalLightUp = new DirectionalLight();

    public BitmapFont font;
    private SpriteBatch batch;

    private Color flame = new Color(0xf59414ff);

    private Model[] models = new Model[30];
    private ModelInstance[] modelInstances = new ModelInstance[30];

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

        inputController = new CameraInputController(cam);
        inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
        inputController.translateUnits = 30f;

        font = new BitmapFont();
        batch = new SpriteBatch();

        Gdx.input.setInputProcessor(new InputMultiplexer(inputController));

        this.outside.set(ColorAttribute.createAmbient(0.8f, 0.8f, 0.8f, 1f));
        this.directionalLightDown = new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0.2274f, -0.8961f, 0.3811f);
        this.directionalLightUp = new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.1865f, 0.9098f, -0.3709f);
        this.outside.add(this.directionalLightDown);
        this.outside.add(this.directionalLightUp);

        DefaultShader.Config config = new DefaultShader.Config();
        config.vertexShader = Gdx.files.internal("assets/dungeon.vertex.glsl").readString();
        config.fragmentShader = Gdx.files.internal("assets/dungeon.fragment.glsl").readString();
        config.numSpotLights = 16;

        this.modelBatch = new ModelBatch(new DefaultShaderProvider(config));
        this.debugRenderer = new ShapeRenderer();

        this.models[0] = ObjLoader.loadModel("assets/graphics/monsters/demon.obj", "demon", .1f);
        this.models[1] = ObjLoader.loadModel("assets/graphics/monsters/bear.obj", "bear", .1f);
        this.models[2] = ObjLoader.loadModel("assets/graphics/monsters/blob.obj", "blob", .1f);
        this.models[3] = ObjLoader.loadModel("assets/graphics/monsters/celtic-warrior.obj", "CelticWarrior", .1f);
        this.models[4] = ObjLoader.loadModel("assets/graphics/chest.obj", "chest", .1f);
        this.models[5] = ObjLoader.loadModel("assets/graphics/monsters/dragon.obj", "dragon", .1f);
        this.models[6] = ObjLoader.loadModel("assets/graphics/monsters/elemental.obj", "elemental", .1f);
        this.models[7] = ObjLoader.loadModel("assets/graphics/monsters/humanoid.obj", "humanoid", .1f);
        this.models[8] = ObjLoader.loadModel("assets/graphics/monsters/kobold.obj", "kobold", .1f);
        this.models[9] = ObjLoader.loadModel("assets/graphics/monsters/ninja.obj", "ninja", .1f);
        this.models[10] = ObjLoader.loadModel("assets/graphics/monsters/ogre.obj", "ogre", .1f);
        this.models[11] = ObjLoader.loadModel("assets/graphics/monsters/orc.obj", "orc", .1f);
        this.models[12] = ObjLoader.loadModel("assets/graphics/monsters/plant.obj", "plant", .1f);
        this.models[13] = ObjLoader.loadModel("assets/graphics/monsters/priest.obj", "priest", .1f);
        this.models[14] = ObjLoader.loadModel("assets/graphics/monsters/rat.obj", "rat", .1f);
        this.models[15] = ObjLoader.loadModel("assets/graphics/monsters/skeleton.obj", "skeleton", .1f);
        this.models[16] = ObjLoader.loadModel("assets/graphics/monsters/toad.obj", "toad", .1f);
        this.models[17] = ObjLoader.loadModel("assets/graphics/monsters/spider.obj", "spider", .1f);
        this.models[18] = ObjLoader.loadModel("assets/graphics/monsters/viking.obj", "viking", .1f);
        this.models[19] = ObjLoader.loadModel("assets/graphics/monsters/viking2.obj", "viking2", .1f);
        this.models[20] = ObjLoader.loadModel("assets/graphics/monsters/warlock1.obj", "warlock1", .1f);
        this.models[21] = ObjLoader.loadModel("assets/graphics/monsters/warrior.obj", "warrior", .1f);
        this.models[22] = ObjLoader.loadModel("assets/graphics/monsters/wasp.obj", "wasp", .1f);
        this.models[23] = ObjLoader.loadModel("assets/graphics/monsters/wizard.obj", "wizard", .1f);
        this.models[24] = ObjLoader.loadModel("assets/graphics/monsters/dark-wizard.obj", "dark-wizard", .1f);
        this.models[25] = ObjLoader.loadModel("assets/graphics/door.obj", "door", .1f);
        this.models[26] = ObjLoader.loadModel("assets/graphics/letter-m.obj", "letter-m", .1f);
        this.models[27] = ObjLoader.loadModel("assets/graphics/fountain.obj", "fountain", .1f);
        this.models[28] = ObjLoader.loadModel("assets/graphics/pentagram.obj", "pentagram", .1f);
        this.models[29] = ObjLoader.loadModel("assets/graphics/elevator.obj", "elevator-booth", .1f);

        final int MODELS_PER_ROW = 5;
        final float SPACING = 1.0f;  // distance between models, tweak if you want them farther apart

        for (int i = 0; i < this.models.length; i++) {
            if (this.models[i] != null) {
                this.modelInstances[i] = new ModelInstance(this.models[i]);

                int row = i / MODELS_PER_ROW;  // integer division
                int col = i % MODELS_PER_ROW;  // remainder

                float x = 0.5f + col * SPACING;
                float z = 0.5f + row * SPACING;

                this.modelInstances[i].transform.setToTranslation(x, 0f, z);
            }
        }

        createAxes();

        cam.position.set(1, 0.5f, 6);
        cam.lookAt(1, 0.5f, 0);

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

        debugRenderer.setProjectionMatrix(cam.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);

        float len = 50f;                 // length of the line
        Vector3 origin = Vector3.Zero;   // start at world origin (0,0,0)

        // UP light (red)
        debugRenderer.setColor(Color.RED);
        debugRenderer.line(
                origin.x, origin.y, origin.z,
                origin.x - directionalLightUp.direction.x * len,
                origin.y - directionalLightUp.direction.y * len,
                origin.z - directionalLightUp.direction.z * len
        );

        // DOWN light (blue)
        debugRenderer.setColor(Color.BLUE);
        debugRenderer.line(
                origin.x, origin.y, origin.z,
                origin.x - directionalLightDown.direction.x * len,
                origin.y - directionalLightDown.direction.y * len,
                origin.z - directionalLightDown.direction.z * len
        );

        debugRenderer.end();

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
