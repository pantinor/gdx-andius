package andius;

import andius.objects.BaseMap;
import andius.objects.Creature;
import java.util.Random;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import utils.XORShiftRandom;

public abstract class BaseScreen implements Screen, InputProcessor, Constants {

    protected BaseScreen returnScreen;
    protected Stage stage;

    protected float time = 0;
    protected Random rand = new XORShiftRandom();

    protected int mapPixelHeight;
    public Vector3 newMapPixelCoords;

    protected Viewport viewport = new ScreenViewport();

    protected Camera camera;

    protected Vector2 currentMousePos;

    protected Creature currentEncounter;
    
    protected int currentRoomId = 0;

    /**
     * translate map tile coords to world pixel coords
     */
    public abstract Vector3 getMapPixelCoords(int x, int y);

    /**
     * get the map coords at the camera center
     */
    public abstract Vector3 getCurrentMapCoords();
    
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

    public void endCombat(boolean isWon, BaseMap combatMap, boolean wounded) {
    }

    public final void addButtons() {

//        TextButton bookButt = new TextButton("Book", Exodus.skin, "wood");
//        bookButt.addListener(new ChangeListener() {
//            @Override
//            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
//                Exodus.mainGame.setScreen(new BookScreen(BaseScreen.this, Exodus.skin));
//            }
//        });
//        bookButt.setX(625);
//        bookButt.setY(15);
//
//        stage.addActor(bookButt);

    }

    public abstract void partyDeath();

    @Override
    public void hide() {
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        currentMousePos = new Vector2(screenX, screenY);
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
