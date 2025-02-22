package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
import andius.dialogs.ReorderPartyDialog;
import static andius.Andius.CTX;
import static andius.Andius.mainGame;
import static andius.Constants.SAVE_FILENAME;
import andius.objects.SaveGame;
import com.badlogic.gdx.Gdx;
import java.util.Random;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public abstract class BaseScreen implements Screen, InputProcessor, Constants {

    protected Stage stage;

    protected float time = 0;
    protected Random rand = new Random();

    protected int mapPixelHeight;
    public final Vector3 newMapPixelCoords = new Vector3();

    protected final Viewport viewport = new ScreenViewport();

    protected Camera camera;

    protected final Vector2 currentMousePos = new Vector2();

    protected int currentRoomId = 0;
    protected String roomName = null;

    /**
     * translate map tile coords to world pixel coords
     *
     * @param v the vector to set the values into
     * @param x
     * @param y
     * @param z
     */
    public abstract void setMapPixelCoords(Vector3 v, int x, int y, int z);

    /**
     * get the map coords at the camera center
     *
     * @param v
     */
    public abstract void getCurrentMapCoords(Vector3 v);

    public abstract void save(SaveGame saveGame);

    public abstract void load(SaveGame saveGame);

    public abstract void log(String t);

    public int currentRoomId() {
        return this.currentRoomId;
    }

    @Override
    public void dispose() {

    }

    public Stage getStage() {
        return stage;
    }

    public abstract void finishTurn(int currentX, int currentY);

    public abstract void teleport(int level, int north, int east);

    public void endCombat(boolean isWon, Object opponent) {
        
    }

    public final void addButtons(Map map) {
        Skin imgBtnSkin = new Skin(Gdx.files.classpath("assets/skin/imgBtn.json"));

        ImageButton reorder = new ImageButton(imgBtnSkin, "reorder");
        reorder.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                Sounds.play(Sound.TRIGGER);
                new ReorderPartyDialog(CTX, BaseScreen.this).show(stage);
            }
        });
        reorder.setX(104);
        reorder.setY(5);

        ImageButton inventory = new ImageButton(imgBtnSkin, "inventory");
        inventory.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                Sounds.play(Sound.TRIGGER);
                EquipmentScreen es = new EquipmentScreen(CTX, map);
                mainGame.setScreen(es);
            }
        });
        inventory.setX(52);
        inventory.setY(5);

        ImageButton save = new ImageButton(imgBtnSkin, "save");
        save.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                try {

                    for (Map m : Map.values()) {
                        if (m.isLoaded()) {
                            m.getScreen().save(CTX.saveGame);
                        }
                    }

                    map.getScreen().save(CTX.saveGame);

                    CTX.saveGame.write(SAVE_FILENAME);

                    Sounds.play(Sound.TRIGGER);

                    log("Party Saved");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        save.setX(5);
        save.setY(5);

        stage.addActor(inventory);
        stage.addActor(reorder);
        stage.addActor(save);

    }

    public void animateText(String text, Color color) {

        float sx = 100;
        float sy = -100;
        float dx = 100;
        float dy = 400;
        float delay = 5;

        log(text);

        Label.LabelStyle ls = new Label.LabelStyle(Andius.skin.get("small-ultima", BitmapFont.class), Color.WHITE);
        Label label = new Label(text, ls);
        label.setWrap(true);
        label.setWidth(800);
        label.setPosition(sx, sy);
        label.setAlignment(Align.center);
        label.setColor(color);
        stage.addActor(label);
        Sounds.play(Sound.POSITIVE_EFFECT);
        label.addAction(sequence(Actions.moveTo(dx, dy, delay), Actions.fadeOut(1f), Actions.removeActor(label)));
    }

    public abstract void partyDeath();

    @Override
    public void hide() {
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        currentMousePos.set(screenX, screenY);
        return false;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
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
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

}
