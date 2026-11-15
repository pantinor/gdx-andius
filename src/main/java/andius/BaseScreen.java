package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
import andius.dialogs.ReorderPartyDialog;
import static andius.Andius.CTX;
import static andius.Andius.mainGame;
import static andius.Andius.startScreen;
import andius.objects.SaveGame;
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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

public abstract class BaseScreen implements Screen, InputProcessor, Constants {

    protected Stage stage;

    protected float time = 0;
    protected Random rand = new Random();

    protected int mapPixelHeight;
    public final Vector3 newMapPixelCoords = new Vector3();

    protected final Viewport viewport = new com.badlogic.gdx.utils.viewport.FitViewport(Andius.SCREEN_WIDTH, Andius.SCREEN_HEIGHT);

    protected Camera camera;

    protected final Vector2 currentMousePos = new Vector2();

    public abstract void setMapPixelCoords(Vector3 v, int x, int y, int z);

    public abstract void getCurrentMapCoords(Vector3 v);

    public abstract void save(SaveGame saveGame);

    public abstract void load(SaveGame saveGame);

    public abstract void log(String t);

    @Override
    public void dispose() {

    }

    public Stage getStage() {
        return stage;
    }

    protected Skin getUiSkin() {
        return Andius.HUD.getSkin();
    }

    protected BitmapFont getMessageFont() {
        return getUiSkin().getFont("body");
    }

    public abstract void finishTurn(int currentX, int currentY);

    public abstract void teleport(int level, int north, int east);

    public void endCombat(boolean isWon, Object opponent) {

    }

    public final void addButtons(Map map) {
        TextButton reorder = new TextButton("Formation", getUiSkin());
        reorder.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                Sounds.play(Sound.TRIGGER);
                new ReorderPartyDialog(CTX, BaseScreen.this).show(stage);
            }
        });
        reorder.setBounds(224, 24, 112, 36);

        TextButton inventory = new TextButton("Camp", getUiSkin());
        inventory.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                Sounds.play(Sound.TRIGGER);
                CampScreen es = new CampScreen(CTX, map);
                mainGame.setScreen(es);
            }
        });
        inventory.setBounds(124, 24, 92, 36);

        TextButton save = new TextButton("Save", getUiSkin());
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

                    CTX.saveGame.write();

                    Sounds.play(Sound.TRIGGER);

                    log("Party Saved");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        save.setBounds(24, 24, 92, 36);

        stage.addActor(inventory);
        stage.addActor(reorder);
        stage.addActor(save);
    }

    protected void showExplorationHelp(Map map) {
        boolean dungeon = this instanceof WizardryDungeonScreen || this instanceof TmxDungeonScreen;
        Dialog help = new Dialog("Exploration controls", getUiSkin());
        help.pad(36, 24, 20, 24);
        String controls = dungeon
                ? "Up / Down    Move forward / back\nLeft / Right    Turn\nDrag the scene    Look around\nP    Show dungeon map\nI    Toggle torch\nK    Use stairs\nPage Up / Down    Change floor\nSpace    Wait a turn"
                : "Arrow keys    Move\nE    Enter a location or use stairs"
                    + (this instanceof GameScreen ? "\nT    Talk to someone nearby\nG    Search for treasure" : "");
        Label label = new Label(controls, getUiSkin());
        help.getContentTable().add(label).width(360).padBottom(16);
        help.getButtonTable().defaults().width(110).height(36);
        help.button("Close");
        help.key(com.badlogic.gdx.Input.Keys.ESCAPE, true);
        help.show(stage);
    }

    public void animateText(String text, Color color) {

        if (text == null) {
            return;
        }

        float sx = 100;
        float sy = -100;
        float dx = 100;
        float dy = 400;
        float delay = 5;

        log(text);

        Label label = new Label(text, new Label.LabelStyle(getMessageFont(), Color.WHITE));
        label.setWrap(true);
        label.setWidth(800);
        label.setPosition(sx, sy);
        label.setAlignment(Align.center);
        label.setColor(color);
        stage.addActor(label);

        label.addAction(sequence(Actions.moveTo(dx, dy, delay), Actions.fadeOut(1f), Actions.removeActor(label)));
    }

    public void partyDeath(Throwable t) {
        for (SaveGame.CharacterRecord player : CTX.saveGame.players) {
            System.out.println(player.toDebugString());
        }
        t.printStackTrace();
        Map.releaseAllMaps();
        mainGame.setScreen(startScreen);
    }

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
        viewport.update(width, height, true);
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
