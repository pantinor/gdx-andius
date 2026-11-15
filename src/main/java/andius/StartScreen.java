package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
import static andius.Andius.CTX;
import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import andius.objects.SaveGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import java.io.File;
import utils.AutoFocusScrollPane;
import utils.ManagementUi;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.FitViewport;
import static utils.SkinUtils.GOLD;
import static utils.SkinUtils.INK;
import static utils.SkinUtils.MUTED;

public class StartScreen implements Screen, Constants {

    private final ManagementUi ui = new ManagementUi();
    private final Texture splashBackground = new Texture(Gdx.files.classpath("assets/data/splash-background.png"));
    private Label saveSummary, status;
    private Label emptySaves;
    private final TextButton manage;
    private final TextButton journey;
    private final Stage stage;
    private final List<String> saveGameSelection;

    public StartScreen() {
        ui.skin.add("start-title", new BitmapFont(Gdx.files.classpath("assets/fonts/exodus.fnt")));
        ui.skin.add("start-title", new Label.LabelStyle(ui.skin.getFont("start-title"), GOLD));
        stage = new Stage(new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT));
        saveGameSelection = new List<>(ui.skin);

        AutoFocusScrollPane savedGamePane = new AutoFocusScrollPane(saveGameSelection, ui.skin);
        savedGamePane.setScrollingDisabled(true, false);
        savedGamePane.setFadeScrollBars(false);

        manage = new TextButton("Manage company", ui.skin);
        manage.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Sounds.play(Sound.TRIGGER);

                String fname = saveGameSelection.getSelected();
                SaveGame saveGame;
                try {
                    saveGame = (fname != null) ? SaveGame.read(fname) : new SaveGame();
                    if (fname != null) {
                        saveGame.saveGameFileName = fname;
                    }
                } catch (Exception e) {
                    saveGame = new SaveGame();
                }

                CTX = new Context();
                CTX.setSaveGame(saveGame);
                mainGame.setScreen(new ManageScreen(StartScreen.this, Andius.skin, saveGame));
            }
        });

        journey = new TextButton("Journey onward", ui.skin);
        journey.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                try {

                    CTX = new Context();

                    String fname = (String) saveGameSelection.getSelected();
                    SaveGame saveGame = SaveGame.read(fname);
                    saveGame.saveGameFileName = fname;
                    CTX.setSaveGame(saveGame);

                    if (CTX.pickRandomEnabledPlayer() == null) {
                        Sounds.play(Sound.NEGATIVE_EFFECT);
                        status.setText("No able adventurers. Manage your company first.");
                        return;
                    }

                    Sounds.play(Sound.TRIGGER);

                    if (CTX.saveGame.map == null) {
                        Map.WORLD.init();
                        CTX.saveGame.map = Map.WORLD;
                        CTX.saveGame.wx = Map.WORLD.getStartX();
                        CTX.saveGame.wy = Map.WORLD.getStartY();
                    }

                    BaseScreen scr = (BaseScreen) CTX.saveGame.map.getScreen();
                    scr.load(CTX.saveGame);

                    mainGame.setScreen(scr);

                } catch (Exception e) {
                    e.printStackTrace();
                    CTX.setSaveGame(new SaveGame());
                    mainGame.setScreen(new ManageScreen(StartScreen.this, Andius.skin, CTX.saveGame));
                }

            }
        });

        buildLayout(savedGamePane);
        saveGameSelection.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshSaveSummary();
            }
        });
        stage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean keyDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, int keycode) {
                if (event.isHandled()) {
                    return false;
                }
                TextButton selected = keycode == Input.Keys.M ? manage : keycode == Input.Keys.J ? journey : null;
                if (selected == null || selected.isDisabled()) {
                    return false;
                }
                selected.fire(new ChangeListener.ChangeEvent());
                return true;
            }
        });
    }

    private void buildLayout(AutoFocusScrollPane savedGamePane) {
        Image titlePanel = new Image(splashBackground);
        titlePanel.setBounds(40, 64, 552, 632);
        titlePanel.setTouchable(Touchable.disabled);
        stage.addActor(titlePanel);

        ui.label(stage, "AN ADVENTURER'S CHRONICLE", "default", MUTED, 64, 650, 496, 24);
        ui.label(stage, "ANDIUS", "start-title", GOLD, 60, 564, 500, 88);
        ui.label(stage, "Bridgeburners", "default-24", Color.WHITE, 64, 524, 496, 42);

        Label introduction = ui.description(stage, 64, 418, 440, 70);
        introduction.setText("Gather your company. Venture beyond the familiar.\nThe next chapter waits in the dark.");
        ui.label(stage, "Assemble your companions. Continue your journey.", "default", MUTED, 64, 100, 496, 34);

        ui.panel(stage, "Your expeditions", "Select a saved company", 624, 112, 352, 544, "15232f");
        savedGamePane.setBounds(640, 368, 320, 208);
        stage.addActor(savedGamePane);

        emptySaves = ui.label(stage, "No saved expeditions yet.", "default", MUTED, 648, 480, 300, 30);
        saveSummary = ui.label(stage, "", "default", MUTED, 640, 280, 320, 78);
        saveSummary.setWrap(true);
        saveSummary.setEllipsis(false);

        journey.setBounds(640, 220, 320, 44);
        manage.setBounds(640, 168, 320, 40);

        ui.primary(journey);
        stage.addActor(journey);
        stage.addActor(manage);

        status = ui.label(stage, "", "default", GOLD, 624, 60, 352, 44);
        status.setWrap(true);
        status.setEllipsis(false);
    }

    private void refreshSaveSummary() {
        status.setText("");
        String filename = saveGameSelection.getSelected();
        journey.setDisabled(filename == null);
        emptySaves.setVisible(saveGameSelection.getItems().isEmpty());
        if (filename == null) {
            saveSummary.setText("Begin with Manage company to recruit your first adventurers.");
            return;
        }
        try {
            SaveGame save = SaveGame.read(filename);
            int count = 0;
            if (save.players != null) {
                for (SaveGame.CharacterRecord player : save.players) {
                    if (player == null || player.name == null || player.name.isBlank()) {
                        continue;
                    }
                    count++;
                }
            }
            saveSummary.setText(filename + "\n" + count + (count == 1 ? " adventurer  /  " : " adventurers  /  ")
                    + (save.map == null ? "A new journey" : save.map.getLabel()));
        } catch (Exception failure) {
            saveSummary.setText(filename + "\nThis save could not be read. Manage company can help you begin again.");
        }
    }

    @Override
    public void show() {

        String[] jsonFiles = new File(".").list((dir, name) -> name.startsWith("party") && name.endsWith(".json"));
        if (jsonFiles == null) {
            jsonFiles = new String[0];
        }
        java.util.Arrays.sort(jsonFiles, String.CASE_INSENSITIVE_ORDER);
        String previous = saveGameSelection.getSelected();
        saveGameSelection.setItems(jsonFiles);
        if (previous != null && java.util.Arrays.asList(jsonFiles).contains(previous)) {
            saveGameSelection.setSelected(previous);
        }
        refreshSaveSummary();

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(INK.r, INK.g, INK.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.getViewport().apply();
        stage.act(Math.min(delta, .1f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        splashBackground.dispose();
        ui.dispose();
    }

}
