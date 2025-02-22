
import static andius.WizardryData.DUNGEON_DIM;
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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import java.util.ArrayList;
import java.util.List;

public class GridWithAxes implements ApplicationListener, InputProcessor {

    private ModelBuilder builder = new ModelBuilder();
    private ModelBatch modelBatch;
    private ModelBatch shadowBatch;
    private SpriteBatch spriteBatch;
    private CameraInputController inputController;
    private PerspectiveCamera cam;
    private Environment environment;
    private final List<ModelInstance> modelInstances = new ArrayList<>();

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "GridWithAxes";
        cfg.useGL30 = false;
        cfg.width = 1280;
        cfg.height = 768;
        new LwjglApplication(new GridWithAxes(), cfg);
    }

    @Override
    public void create() {

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.near = 0.1f;
        cam.far = 10;
        cam.update();

        inputController = new CameraInputController(cam);
        inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
        inputController.translateUnits = 30f;

        Gdx.input.setInputProcessor(new InputMultiplexer(inputController));

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        modelBatch = new ModelBatch();

        createAxes();

        cam.position.set(-1, 1, 0);
        cam.lookAt(0, 0, 0);

        Material gr = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        Material bl = new Material(ColorAttribute.createDiffuse(Color.BLUE));
        Material yl = new Material(ColorAttribute.createDiffuse(Color.YELLOW));
        Material red = new Material(ColorAttribute.createDiffuse(Color.RED));

        Model floorModel = builder.createBox(1.1f, 0.1f, 1.1f, yl, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.Normal);
        Model boxModel = builder.createBox(1.0f, 1.0f, 1.0f, bl, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.Normal);
        Model ceilingModel = builder.createBox(1.2f, 0.1f, 1.2f, red, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.Normal);

        this.modelInstances.add(new ModelInstance(floorModel, 0 + .5f, -2.25f, 0 + .5f));
        this.modelInstances.add(new ModelInstance(boxModel, 0 + .5f, -1.75f, 0 + .5f));
        this.modelInstances.add(new ModelInstance(ceilingModel, 0 + .5f, -1.25f, 0 + .5f));

        this.modelInstances.add(new ModelInstance(floorModel, 0 + .5f, -1.15f, 0 + .5f));
        this.modelInstances.add(new ModelInstance(boxModel, 0 + .5f, -.65f, 0 + .5f));
        this.modelInstances.add(new ModelInstance(ceilingModel, 0 + .5f, -.15f, 0 + .5f));

        this.modelInstances.add(new ModelInstance(floorModel, 0 + .5f, -.05f, 0 + .5f));
        this.modelInstances.add(new ModelInstance(boxModel, 0 + .5f, .5f, 0 + .5f));
        this.modelInstances.add(new ModelInstance(ceilingModel, 0 + .5f, 1.05f, 0 + .5f));

        this.modelInstances.add(new ModelInstance(floorModel, 0 + .5f, 1.15f, 0 + .5f));
        this.modelInstances.add(new ModelInstance(boxModel, 0 + .5f, 1.7f, 0 + .5f));
        this.modelInstances.add(new ModelInstance(ceilingModel, 0 + .5f, 2.25f, 0 + .5f));

        this.modelInstances.add(new ModelInstance(floorModel, 0 + .5f, 2.35f, 0 + .5f));
        this.modelInstances.add(new ModelInstance(boxModel, 0 + .5f, 2.85f, 0 + .5f));
        this.modelInstances.add(new ModelInstance(ceilingModel, 0 + .5f, 3.35f, 0 + .5f));

    }

    @Override
    public void render() {
        cam.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);

        modelBatch.render(axesInstance);

        for (ModelInstance i : modelInstances) {
            modelBatch.render(i, environment);
        }

        modelBatch.end();
    }

    final float GRID_MIN = -3.5f;
    final float GRID_MAX = 3.5f;
    final float GRID_STEP = .1f;
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
