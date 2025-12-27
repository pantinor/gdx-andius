
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.utils.Align;
import java.util.HashMap;

public class TextureAtlasViewer extends InputAdapter implements ApplicationListener {

    static int screenWidth = 1024;
    static int screenHeight = 768;

    Batch batch;
    BitmapFont font;
    Stage stage;
    Skin skin;

    // Increase this to make tiny regions (e.g. 16x16) easier to see in the ScrollPane.
    private static final float PREVIEW_SCALE = 4f;
    private static final float PREVIEW_PAD = 10f;
    private static final float LABEL_WIDTH = 220f;

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Texture Atlas Viewer";
        cfg.width = screenWidth;
        cfg.height = screenHeight;
        new LwjglApplication(new TextureAtlasViewer(), cfg);

    }

    private class AnimatedLabelGroup extends Group {

        
        private final Label label;
        private final Animation<TextureRegion> animation;
        private float stateTime = 0f;

        private final float regionW;
        private final float regionH;
        private final float previewW;
        private final float previewH;

        public AnimatedLabelGroup(Label label, Animation<TextureRegion> animation) {
            this.label = label;
            this.animation = animation;

            TextureRegion first = animation.getKeyFrames()[0];
            this.regionW = first.getRegionWidth();
            this.regionH = first.getRegionHeight();
            this.previewW = regionW * PREVIEW_SCALE;
            this.previewH = regionH * PREVIEW_SCALE;

            // Lay out the label + preview so the Table/ScrollPane can size rows correctly.
            this.label.setAlignment(Align.left);
            this.label.setBounds(0, 0, LABEL_WIDTH, previewH);
            addActor(this.label);

            setBounds(0, 0, LABEL_WIDTH + PREVIEW_PAD + previewW, previewH);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);

            TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);

            float previewX = getX() + LABEL_WIDTH + PREVIEW_PAD;
            float previewY = getY() + (getHeight() - previewH) * 0.5f;

            batch.draw(currentFrame, previewX, previewY, previewW, previewH);
        }


    }

    @Override
    public void create() {

        font = new BitmapFont();
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
        stage = new Stage();

        TextureAtlas atlas = new TextureAtlas(new FileHandle("src/main/resources/assets/tibian/tileset16.atlas"));

        // Keep scaled-up 16x16 (pixel art) regions crisp instead of blurry.
        for (Texture t : atlas.getTextures()) {
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }


        Table animTable = new Table(skin);
        animTable.top().left();
        animTable.defaults().left().pad(4);

        java.util.Map<String, Animation> animations = new HashMap<>();
        Array<String> processedNames = new Array<>();
        for (AtlasRegion region : atlas.getRegions()) {
            String regionName = region.name;
            if (!processedNames.contains(regionName, false)) {
                Array<TextureAtlas.AtlasRegion> frames = atlas.findRegions(regionName);
                if (frames.size > 0) {
                    Animation<TextureRegion> animation = new Animation(0.3f, frames, Animation.PlayMode.LOOP);
                    animations.put(regionName, animation);
                    processedNames.add(regionName);
                }
            }
        }

        for (String r : animations.keySet()) {
            AnimatedLabelGroup a = new AnimatedLabelGroup(new Label(r, skin, "default-16"), animations.get(r));
            animTable.add(a).expandX().fillX().left();
            animTable.row();
        }

        ScrollPane sp1 = new ScrollPane(animTable, skin, "default");
        sp1.setScrollingDisabled(false, false);
        sp1.setBounds(50, 50, 700, 700);

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
