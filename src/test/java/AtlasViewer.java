
import andius.TibianSprite;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.List;

public class AtlasViewer extends Game implements InputProcessor {

    public static final int SCREEN_WIDTH = 1200;
    public static final int SCREEN_HEIGHT = 768;

    OrthographicCamera camera;
    SpriteBatch batch;

    TextureAtlas atlas;
    List<String> names;
    float frameCounter = 0;
    BitmapFont font;
    int section = 0;

    int[][] ranges = new int[200][2];

    public static int currentPrefixIndex = 0;

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "AnimationSelection";
        cfg.width = SCREEN_WIDTH;
        cfg.height = SCREEN_HEIGHT;
        new LwjglApplication(new AtlasViewer(), cfg);
    }

    @Override
    public void create() {

        font = new BitmapFont();

        TibianSprite.init();

        this.names = TibianSprite.names();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);

        batch = new SpriteBatch();

        Gdx.input.setInputProcessor(this);

        for (int i = 0; i < ranges.length; i++) {
            ranges[i][0] = (i) * 40;
            ranges[i][1] = (i + 1) * 40;
        }
    }

    @Override
    public void render() {

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        frameCounter += Gdx.graphics.getDeltaTime();

        int y = 1;
        int count = 0;

        for (int index = 0; index < this.names.size(); index++) {

            if (index < ranges[section][0] || index >= ranges[section][1]) {
                continue;
            }

            Animation anim = TibianSprite.animation(this.names.get(index));

            if (anim.getKeyFrames().length <= 0) {
                continue;
            }

            count++;

            int DIM = 128;

            int rX = (count * DIM) - (DIM / 2) + 20;
            int rY = (y * DIM) - (DIM / 2) + 20;
            int centerRectX = rX + (DIM / 2);
            int centerRectY = rY + (DIM / 2);

            //centerize the image on the rectangle
            TextureRegion frame = (TextureRegion) anim.getKeyFrame(frameCounter);
            int width = frame.getRegionWidth();
            int height = frame.getRegionHeight();
            batch.draw(frame, centerRectX - width / 2, centerRectY - height / 2, width, height);

            if (index % 2 == 0) {
                font.draw(batch, this.names.get(index), rX + 20, rY + 20);
            } else {
                font.draw(batch, this.names.get(index), rX + 20, rY + 2);
            }

            if (count > 7) {
                y++;
                count = 0;
            }

        }

        batch.end();

    }

    @Override
    public boolean keyDown(int key) {
        if (key == Keys.UP) {
            section++;
        }
        if (key == Keys.DOWN) {
            section--;
        }
        if (section >= ranges.length) {
            section = 0;
        }
        if (section < 0) {
            section = 0;
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

}
