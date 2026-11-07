import andius.objects.MonsterModels;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class ModelVisualizerV2 extends ApplicationAdapter {

    private static final float ROTATION_SPEED = 25f;
    private final MonsterModels[] models = MonsterModels.values();
    private final Set<Model> loadedModels = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Environment environment = new Environment();
    private final Vector3 modelCenter = new Vector3();

    private ModelBatch modelBatch;
    private PerspectiveCamera camera;
    private Stage stage;
    private Skin skin;
    private Label modelLabel;
    private ModelInstance instance;
    private int modelIndex;
    private float modelRadius = 1f;
    private float rotation;

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "ModelVisualizer V2";
        config.width = 1280;
        config.height = 768;
        config.useGL30 = false;
        config.samples = 4;
        new LwjglApplication(new ModelVisualizerV2(), config);
    }

    @Override
    public void create() {
        modelBatch = new ModelBatch();
        camera = new PerspectiveCamera(45f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.05f;
        camera.far = 30f;
        camera.position.set(0f, 0f, 4f);
        camera.lookAt(Vector3.Zero);
        camera.update();

        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.45f, 0.45f, 0.45f, 1f));
        environment.add(new DirectionalLight().set(0.75f, 0.75f, 0.75f, -1f, -1f, -1f));
        environment.add(new DirectionalLight().set(0.25f, 0.28f, 0.32f, 1f, -0.3f, 0.5f));

        stage = new Stage(new ScreenViewport());
        skin = createSkin();
        createControls();
        Gdx.input.setInputProcessor(stage);
        showModel(0);
    }

    private Skin createSkin() {
        Skin result = new Skin();
        result.add("default-font", new BitmapFont());
        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        result.add("white", new Texture(pixel));
        pixel.dispose();

        TextButton.TextButtonStyle button = new TextButton.TextButtonStyle();
        button.font = result.getFont("default-font");
        button.fontColor = Color.WHITE;
        button.up = result.newDrawable("white", new Color(0.19f, 0.23f, 0.29f, 1f));
        button.over = result.newDrawable("white", new Color(0.28f, 0.34f, 0.42f, 1f));
        button.down = result.newDrawable("white", new Color(0.12f, 0.16f, 0.21f, 1f));
        result.add("default", button);
        result.add("default", new Label.LabelStyle(result.getFont("default-font"), Color.WHITE));
        return result;
    }

    private void createControls() {
        Table layout = new Table();
        layout.setFillParent(true);
        layout.top().pad(20f);
        modelLabel = new Label("", skin);
        modelLabel.setAlignment(Align.center);
        layout.add(modelLabel).expandX().fillX().colspan(3).height(40f);
        layout.row();
        layout.add().expandY().colspan(3);
        layout.row();

        TextButton previous = new TextButton("< Previous", skin);
        TextButton next = new TextButton("Next >", skin);
        previous.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showModel(modelIndex - 1);
            }
        });
        next.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showModel(modelIndex + 1);
            }
        });
        layout.add(previous).width(160f).height(48f);
        layout.add(new Label("Left / Right arrows to browse", skin)).expandX();
        layout.add(next).width(160f).height(48f);
        stage.addActor(layout);
    }

    private void showModel(int index) {
        modelIndex = Math.floorMod(index, models.length);
        MonsterModels selected = models[modelIndex];
        instance = null;
        rotation = 0f;
        modelLabel.setText((modelIndex + 1) + " / " + models.length + "   " + selected.name());
        try {
            Model model = selected.model();
            if (model == null) {
                modelLabel.setText(modelLabel.getText() + "   (failed to load)");
                return;
            }
            loadedModels.add(model);
            instance = new ModelInstance(model);
            BoundingBox bounds = instance.calculateBoundingBox(new BoundingBox());
            bounds.getCenter(modelCenter);
            // A bounding sphere keeps the whole model in view at every rotation.
            modelRadius = Math.max(bounds.getDimensions(new Vector3()).len() * 0.5f, 0.001f);
            updateModelTransform();
        } catch (RuntimeException exception) {
            instance = null;
            Gdx.app.error("ModelVisualizerV2", "Could not load " + selected.name(), exception);
            modelLabel.setText(modelLabel.getText() + "   (failed to load)");
        }
    }

    private void updateModelTransform() {
        float aspect = camera.viewportWidth / Math.max(camera.viewportHeight, 1f);
        float scale = Math.min(1f, aspect) / modelRadius;
        instance.transform.idt()
                .rotate(Vector3.Y, rotation)
                .scale(scale, scale, scale)
                .translate(-modelCenter.x, -modelCenter.y, -modelCenter.z);
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            showModel(modelIndex - 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            showModel(modelIndex + 1);
        }
        if (instance != null) {
            rotation = (rotation + ROTATION_SPEED * Gdx.graphics.getDeltaTime()) % 360f;
            updateModelTransform();
        }

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.09f, 0.11f, 0.14f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        modelBatch.begin(camera);
        if (instance != null) {
            modelBatch.render(instance, environment);
        }
        modelBatch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0 || camera == null) {
            return;
        }
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);
        stage.dispose();
        skin.dispose();
        modelBatch.dispose();
        for (Model model : loadedModels) {
            model.dispose();
        }
    }
}
