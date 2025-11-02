
import andius.objects.TibianSprite;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import utils.AutoFocusScrollPane;

public class MonsterAtlasTool extends InputAdapter implements ApplicationListener {

    static int screenWidth = 1920;
    static int screenHeight = 1008;

    Batch batch;
    BitmapFont font;
    Stage stage;
    Skin skin;
    String selectedIcon;

    @Override
    public void create() {

        font = new BitmapFont();
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
        stage = new Stage();

        TibianSprite.init();

        Table animTable = new Table(skin);
        animTable.defaults().pad(2);
        int count = 0;
        for (String name : TibianSprite.names()) {

            Image im = new Image(TibianSprite.icon(name));
            im.setName(name);
            im.addListener(new ClickListener(-1) {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (event.getButton() == 0) {
                        selectedIcon = event.getTarget().getName();
                    }
                }
            });

            animTable.add(im);
            count++;
            if (count > 20) {
                count = 0;
                animTable.row();
            }
        }

        ScrollPane imageScrollPane = new AutoFocusScrollPane(animTable, skin);
        imageScrollPane.setScrollingDisabled(true, false);
        imageScrollPane.setBounds(0, screenHeight, screenWidth - 300, screenHeight);
        imageScrollPane.setPosition(0, 0);

        stage.addActor(imageScrollPane);

        Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));

    }

    @Override

    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "icon: " + selectedIcon, screenWidth - 350, screenHeight - 830);
        batch.end();

        stage.act();
        stage.draw();
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Monster Atlas Tool";
        cfg.width = screenWidth;
        cfg.height = screenHeight;
        new LwjglApplication(new MonsterAtlasTool(), cfg);

    }

    public class AnimationActor extends Actor {

        private final Animation<TextureRegion> animation;
        private float stateTime = 0f;
        private boolean looping = true;

        public AnimationActor(Animation<TextureRegion> animation) {
            this.animation = animation;
            TextureRegion first = animation.getKeyFrame(0f);
            setSize(first.getRegionWidth(), first.getRegionHeight());
            setOrigin(getWidth() / 2f, getHeight() / 2f);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {

            TextureRegion frame = animation.getKeyFrame(stateTime, looping);
            batch.draw(frame,
                    getX(), getY(),
                    getOriginX(), getOriginY(),
                    getWidth(), getHeight(),
                    getScaleX(), getScaleY(),
                    getRotation());
        }

    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public void resume() {
    }

}
