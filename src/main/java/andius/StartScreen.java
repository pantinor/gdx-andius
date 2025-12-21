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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import java.io.File;
import utils.AutoFocusScrollPane;
import utils.FrameMaker;

public class StartScreen implements Screen, Constants {

    private final Batch batch;
    private final TextButton manage;
    private final TextButton journey;
    private final BitmapFont titleFont;
    private final Stage stage;
    private final Texture background;
    private final List saveGameSelection;

    public StartScreen() {

        titleFont = new BitmapFont(Gdx.files.classpath("assets/fonts/exodus.fnt"));

        batch = new SpriteBatch();

        saveGameSelection = new List<>(Andius.skin, "default-16");

        AutoFocusScrollPane savedGamePane = new AutoFocusScrollPane(saveGameSelection, Andius.skin);

        FrameMaker fm = new FrameMaker(SCREEN_WIDTH, SCREEN_HEIGHT);
        fm.setBounds(savedGamePane, 360, 185, 220, 100);

        manage = new TextButton("Manage", Andius.skin, "default-24");
        manage.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Sounds.play(Sound.TRIGGER);
                if (CTX == null) {
                    CTX = new Context();
                    try {
                        
                        String fname = (String) saveGameSelection.getSelected();
                        SaveGame saveGame = SaveGame.read(fname);
                        saveGame.saveGameFileName = fname;
                    
                        CTX.setSaveGame(saveGame);
                        mainGame.setScreen(new ManageScreen(StartScreen.this, Andius.skin, saveGame));
                    } catch (Exception e) {
                        CTX.setSaveGame(new SaveGame());
                        mainGame.setScreen(new ManageScreen(StartScreen.this, Andius.skin, CTX.saveGame));
                    }
                } else {
                    mainGame.setScreen(new ManageScreen(StartScreen.this, Andius.skin, CTX.saveGame));
                }
            }
        });
        manage.setBounds(360, Andius.SCREEN_HEIGHT - 400, 220, 40);

        journey = new TextButton("Journey Onward", Andius.skin, "default-24");
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
        journey.setBounds(360, Andius.SCREEN_HEIGHT - 450, 220, 40);

        stage = new Stage();
        stage.addActor(manage);
        stage.addActor(journey);
        stage.addActor(savedGamePane);

        this.background = fm.build();

    }

    @Override
    public void show() {

        Object[] jsonFiles = new File(".").list((File dir, String name) -> name.startsWith("party") && name.endsWith(".json"));
        saveGameSelection.setItems(jsonFiles);

        Object selected = saveGameSelection.getSelected();
        if (selected == null) {
            if (jsonFiles.length > 0) {
                saveGameSelection.setSelected(jsonFiles[0]);
            }
        }

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(this.background, 0, 0);
        titleFont.draw(batch, "ANDIUS", 320, Andius.SCREEN_HEIGHT - 140);
        Andius.font72.draw(batch, "Bridgeburners", 250, Andius.SCREEN_HEIGHT - 240);
        batch.end();

        stage.act();
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            this.manage.toggle();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            this.journey.toggle();
        }

    }

    @Override
    public void resize(int width, int height) {
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
