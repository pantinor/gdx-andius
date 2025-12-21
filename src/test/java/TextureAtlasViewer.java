
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;

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

    private class AnimatedLabelGroup extends Group {

        private Label label;
        private Animation<TextureRegion> animation;
        private float stateTime;
        float w, h;

        public AnimatedLabelGroup(Label label, Animation<TextureRegion> animation) {
            this.label = label;
            this.animation = animation;
            this.stateTime = 0f;
            this.w = animation.getKeyFrames()[0].getRegionWidth();
            this.h = animation.getKeyFrames()[0].getRegionHeight();

            addActor(label);

            setBounds(0, 0, w, h);

            this.label.setBounds(0, 0, w, h);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
            stateTime += parentAlpha;
            TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
            batch.draw(currentFrame, getX() + 200, getY(), w, h);
        }

    }

    @Override
    public void create() {

        font = new BitmapFont();
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
        stage = new Stage();

        TextureAtlas atlas = new TextureAtlas(new FileHandle("src/main/resources/assets/tibian/tibian.atlas"));

        Table animTable = new Table(skin);
        animTable.left().setFillParent(true);

        java.util.Map<String, Animation> animations = new HashMap<>();
        Array<String> processedNames = new Array<>();
        for (AtlasRegion region : atlas.getRegions()) {
            String regionName = region.name;
            if (!processedNames.contains(regionName, false)) {
                Array<TextureAtlas.AtlasRegion> frames = atlas.findRegions(regionName);
                if (frames.size > 0) {
                    Animation<TextureRegion> animation = new Animation(8.0f, frames, Animation.PlayMode.LOOP);
                    animations.put(regionName, animation);
                    processedNames.add(regionName);
                }
            }
        }

        for (String r : animations.keySet()) {
            AnimatedLabelGroup a = new AnimatedLabelGroup(new Label(r, skin, "default-16"), animations.get(r));
            animTable.add(a);
            animTable.row();
        }

        ScrollPane sp1 = new ScrollPane(animTable, skin, "default");
        sp1.setBounds(50, 50, 300, 700);

        stage.addActor(sp1);

        Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
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
