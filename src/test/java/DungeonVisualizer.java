
import andius.Andius;
import andius.Constants;
import andius.TmxDungeonScreen;
import andius.WizardryData;
import andius.WizardryDungeonScreen;
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
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class DungeonVisualizer implements ApplicationListener, InputProcessor {

    private ModelBatch modelBatch;
    private CameraInputController inputController;
    private PerspectiveCamera cam;
    private Environment environment;

    public BitmapFont font;
    private SpriteBatch batch;

    private TmxDungeonScreen screen;

    private Color flame = new Color(0xf59414ff);

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "GridWithAxes";
        cfg.useGL30 = false;
        cfg.width = 1280;
        cfg.height = 768;
        new LwjglApplication(new DungeonVisualizer(), cfg);
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

        environment = new Environment();
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.05f, 0.05f, 0.05f, 1f));

        PointLight light1 = new PointLight().set(flame, 2, 2, 2, 5);
        environment.add(light1);

        PointLight light2 = new PointLight().set(flame, 2, 2, 18, 5);
        environment.add(light2);

        PointLight light3 = new PointLight().set(flame, 10, 2, 2, 5);
        environment.add(light3);

        PointLight light4 = new PointLight().set(flame, 10, 2, 18, 5);
        environment.add(light4);

        PointLight light5 = new PointLight().set(flame, 18, 2, 2, 5);
        environment.add(light5);

        PointLight light6 = new PointLight().set(flame, 18, 2, 18, 5);
        environment.add(light6);

        modelBatch = new ModelBatch();

        WizardryData.class.getClass();

        //this.screen = new WizardryDungeonScreen(Constants.Map.SLAVERS_PIT);
        this.screen = new TmxDungeonScreen(Constants.Map.SLAVERS_PIT);

        createAxes();

        cam.position.set(2.5f, 0.5f, 11.5f);
        cam.lookAt(9.5f, 0.5f, 9.5f);

    }

    @Override
    public void render() {
        cam.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);

        modelBatch.render(axesInstance);

        screen.floorPlane.render(modelBatch, environment);
        //screen.ceilingPlane.render(modelBatch, environment);

        for (TmxDungeonScreen.DungeonTileModelInstance i : this.screen.modelInstances) {
            if (i.getLevel() == 0) {
                modelBatch.render(i, environment);
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
