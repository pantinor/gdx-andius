
import andius.WizardryData;
import static andius.WizardryData.DUNGEON_DIM;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.UBJsonReader;
import java.util.ArrayList;
import java.util.List;

public class DungeonVisualizer implements ApplicationListener, InputProcessor {

    private ModelBuilder builder = new ModelBuilder();
    private ModelBatch modelBatch;
    private CameraInputController inputController;
    private PerspectiveCamera cam;
    private Environment environment;
    private final List<ModelInstance> modelInstances = new ArrayList<>();

    private Model doorModel;
    private Model wall, hiddenDoor;

    public BitmapFont font;
    private SpriteBatch batch;

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

        PointLight light1 = new PointLight().set(flame, 2.5f, .5f, 2.5f, 1f);
        environment.add(light1);

        PointLight light2 = new PointLight().set(flame, 16.5f, .5f, 16.5f, .5f);
        environment.add(light2);

        PointLight light3 = new PointLight().set(flame, 8.5f, .5f, 8.5f, .5f);
        environment.add(light3);

        modelBatch = new ModelBatch();

        createAxes();

        cam.position.set(-1f, 3f, 9f);
        cam.lookAt(1, 0, 9);

        Material mortar = new Material(TextureAttribute.createDiffuse(new Texture(Gdx.files.classpath("assets/graphics/mortar.png"))));

        TextureRegion flipped = new TextureRegion(new Texture(Gdx.files.classpath("assets/graphics/mortar.png")));
        flipped.flip(true, true);
        Material mortarRotated = new Material(TextureAttribute.createDiffuse(flipped));

        Material wood = new Material(TextureAttribute.createDiffuse(new Texture(Gdx.files.classpath("assets/graphics/wood-door-texture.png"))));
        Material dirt = new Material(TextureAttribute.createDiffuse(new Texture(Gdx.files.classpath("assets/graphics/dirt.png"))));
        Material gr = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        Material bl = new Material(ColorAttribute.createDiffuse(Color.BLUE));
        Material yl = new Material(ColorAttribute.createDiffuse(Color.YELLOW));
        Material red = new Material(ColorAttribute.createDiffuse(Color.RED));

        ModelLoader gloader = new G3dModelLoader(new UBJsonReader());

        Model floorModel = builder.createBox(1f, 1f, 1f, dirt, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.Normal);

        doorModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/door.g3db"));
        doorModel.nodes.get(0).scale.set(.2f, .2f, .2f);
        doorModel.nodes.get(0).translation.set(.06f, -.5f, .015f);
        doorModel.nodes.get(0).parts.first().material = wood;

        hiddenDoor = builder.createBox(1.090f, 1, 0.05f, red, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        wall = builder.createBox(1.090f, 1, 0.05f, mortar, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        Model doorWall = builder.createBox(1.090f, 1, 0.05f, mortarRotated, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        builder.begin();
        builder.node("door-wall", doorWall);
        builder.node("door-main", doorModel);
        doorModel = builder.end();

        WizardryData.Scenario sc = WizardryData.Scenario.PMO;

        for (int e = -1; e < DUNGEON_DIM + 1; e++) {
            for (int n = -1; n < DUNGEON_DIM + 1; n++) {
                modelInstances.add(new DungeonTileModelInstance(floorModel, 0, 0f, 0f, e + .5f, -.5f, n + .5f));
            }
        }

        for (int e = 0; e < DUNGEON_DIM; e++) {
            for (int n = 0; n < DUNGEON_DIM; n++) {
                WizardryData.MazeCell cell = sc.levels()[0].cells[n][e];
                addBlock(0, cell, n, e);
            }
        }

    }

    void addBlock(int level, WizardryData.MazeCell cell, float x, float y) {

        float z = 0.5f;
        if (cell.northWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y);
            instance.transform.setFromEulerAngles(270, 0, 0).trn(x + 1 - .025f, z, y + .5f);
            modelInstances.add(instance);
        }
        if (cell.southWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y);
            instance.transform.setFromEulerAngles(90, 0, 0).trn(x + .025f, z, y + .5f);
            modelInstances.add(instance);
        }
        if (cell.eastWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y);
            instance.transform.setFromEulerAngles(180, 0, 0).trn(x + .5f, z, y - .025f + 1);
            modelInstances.add(instance);
        }
        if (cell.westWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y, x + .5f, z, y + .025f);
            modelInstances.add(instance);
        }

        if (cell.northDoor) {
            if (cell.northWall) {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(hiddenDoor, level, x, y);
                instance.transform.setFromEulerAngles(270, 0, 0).trn(x + 1 - .025f, z, y + .5f);
                modelInstances.add(instance);
            } else {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y);
                instance.transform.setFromEulerAngles(90, 0, 0).trn(1 + x - .025f, z, 1 + y - .5f);
                modelInstances.add(instance);
            }
        }
        if (cell.southDoor) {
            if (cell.southWall) {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(hiddenDoor, level, x, y);
                instance.transform.setFromEulerAngles(90, 0, 0).trn(x + .025f, z, y + .5f);
                modelInstances.add(instance);
            } else {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y);
                instance.transform.setFromEulerAngles(270, 0, 0).trn(x + .025f, z, y + .5f);
                modelInstances.add(instance);
            }
        }
        if (cell.eastDoor) {
            if (cell.eastWall) {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(hiddenDoor, level, x, y);
                instance.transform.setFromEulerAngles(180, 0, 0).trn(x + .5f, z, y - .025f + 1);
                modelInstances.add(instance);
            } else {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y, x - .5f + 1, z, y - .025f + 1);
                modelInstances.add(instance);
            }
        }
        if (cell.westDoor) {
            if (cell.westWall) {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(hiddenDoor, level, x, y);
                instance.transform.setToTranslation(x + .5f, z, y + .025f);
                modelInstances.add(instance);
            } else {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y);
                instance.transform.setFromEulerAngles(0, 180, 180).trn(x + .5f, z, y + .025f);
                modelInstances.add(instance);
            }
        }

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

    private class DungeonTileModelInstance extends ModelInstance {

        private final int level;
        private final float cx;
        private final float cy;
        private boolean rotates;

        public DungeonTileModelInstance(Model model, int level, float cx, float cy) {
            super(model);
            this.level = level;
            this.cx = cx;
            this.cy = cy;
        }

        public DungeonTileModelInstance(Model model, int level, float cx, float cy, float x, float y, float z, boolean rotates) {
            this(model, level, cx, cy, x, y, z);
            this.rotates = rotates;
        }

        public DungeonTileModelInstance(Model model, int level, float cx, float cy, float x, float y, float z) {
            super(model);
            this.level = level;
            this.cx = cx;
            this.cy = cy;
            this.transform.setToTranslation(x, y, z);
        }

        public int getLevel() {
            return level;
        }

        public float getCx() {
            return cx;
        }

        public float getCy() {
            return cy;
        }

        public void render(ModelBatch batch, Environment env) {
            if (this.rotates) {
                this.transform.rotate(Vector3.Y, 45f * Gdx.graphics.getDeltaTime());
            }
            batch.render(this, env);
        }

    }

}
