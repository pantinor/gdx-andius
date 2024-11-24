
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;

public class TextureAtlasViewer extends InputAdapter implements ApplicationListener {

    static int screenWidth = 1024;
    static int screenHeight = 768;

    Batch batch;
    BitmapFont font;
    Stage stage;
    Skin skin;

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Texture Atlas Viewer";
        cfg.width = screenWidth;
        cfg.height = screenHeight;
        new LwjglApplication(new TextureAtlasViewer(), cfg);

    }

    @Override
    public void create() {

        font = new BitmapFont();
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
        stage = new Stage();

        TextureAtlas atlas = new TextureAtlas(new FileHandle("src/main/resources/assets/gifs/bosses.atlas"));
                
        List<Label> regionLabels = new List(skin);
        Array<Label> tmp = new Array<>();
        for (TextureAtlas.AtlasRegion r : atlas.getRegions()) {
            if (r.index > 0) {
                continue;
            }
            Label l = new Label(r.name, skin);
            l.setUserObject(r);
            tmp.add(l);
        }
        regionLabels.setItems(tmp);

        ScrollPane sp1 = new ScrollPane(regionLabels, skin);

        TextButton makeButton = new TextButton("Make XML", skin, "default");
        makeButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                try {

                } catch (Exception e) {

                }
                return false;
            }
        }
        );

        sp1.setBounds(50, 50, 300, 700);

        stage.addActor(sp1);

        stage.addActor(makeButton);

        Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        font.draw(batch, "temp", 600, 10);
        batch.end();

        stage.act();
        stage.draw();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

}
