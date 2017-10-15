package andius;

import static andius.Andius.CTX;
import static andius.Andius.mainGame;
import static andius.Constants.SAVE_FILENAME;
import com.badlogic.gdx.Gdx;
import java.util.Random;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import utils.XORShiftRandom;

public abstract class BaseScreen implements Screen, InputProcessor, Constants {

    protected Stage stage;

    protected float time = 0;
    protected Random rand = new XORShiftRandom();

    protected int mapPixelHeight;
    public final Vector3 newMapPixelCoords = new Vector3();

    protected final Viewport viewport = new ScreenViewport();

    protected Camera camera;

    protected final Vector2 currentMousePos = new Vector2();

    protected int currentRoomId = 0;
    protected String roomName = null;

    /**
     * translate map tile coords to world pixel coords
     * @param v
     * @param x
     * @param y
     */
    public abstract void setMapPixelCoords(Vector3 v, int x, int y);

    /**
     * get the map coords at the camera center
     * @param v
     */
    public abstract void setCurrentMapCoords(Vector3 v);
    
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

    public void endCombat(boolean isWon, andius.objects.Actor opponent) {

    }

    public final void addButtons(final Map map) {
        Skin imgBtnSkin = new Skin(Gdx.files.classpath("assets/skin/imgBtn.json"));

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
        
        ImageButton reorder = new ImageButton(imgBtnSkin, "inventory");
        reorder.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                Sounds.play(Sound.TRIGGER);
                new ReorderPartyDialog(CTX, BaseScreen.this).show(stage);
            }
        });
        reorder.setX(104);
        reorder.setY(5);

        ImageButton save = new ImageButton(imgBtnSkin, "save");
        save.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                try {
                    Sounds.play(Sound.TRIGGER);
                    log("Progress Saved");
                    Vector3 v = new Vector3();
                    Map.WORLD.getScreen().setCurrentMapCoords(v);
                    CTX.saveGame.map = Map.WORLD.ordinal();
                    CTX.saveGame.wx = (int)v.x;
                    CTX.saveGame.wy = (int)v.y;
                    CTX.saveGame.write(SAVE_FILENAME);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mainGame.setScreen(map.getScreen());
            }
        });
        save.setX(5);
        save.setY(5);

        stage.addActor(inventory);
        stage.addActor(reorder);
        stage.addActor(save);

    }
    
    public void animateText(String text, Color color, float sx, float sy, float dx, float dy, float delay) {
        Label label = new Label(text.replace(". ", ".\n"), Andius.skin, "larger");
        label.setPosition(sx, sy);
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
    public boolean scrolled(int amount) {
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

}
