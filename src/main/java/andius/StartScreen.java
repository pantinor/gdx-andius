package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
import static andius.Andius.CTX;
import static andius.Andius.mainGame;
import static andius.Constants.SAVE_FILENAME;
import andius.objects.SaveGame;
import andius.objects.Spells;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class StartScreen implements Screen, Constants {

    float time = 0;
    Batch batch;
    TextButton manual;
    TextButton manage;
    TextButton journey;
    BitmapFont titleFont;
    Stage stage;

    public StartScreen() {

        titleFont = new BitmapFont(Gdx.files.classpath("assets/fonts/exodus.fnt"));

        batch = new SpriteBatch();

        manage = new TextButton("Manage", Andius.skin, "default-24");
        manage.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Sounds.play(Sound.TRIGGER);
                if (CTX == null) {
                    CTX = new Context();
                    try {
                        SaveGame saveGame = SaveGame.read(SAVE_FILENAME);
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
                    SaveGame saveGame = SaveGame.read(SAVE_FILENAME);
                    CTX.setSaveGame(saveGame);

                    if (CTX.pickRandomEnabledPlayer() == null) {
                        Sounds.play(Sound.NEGATIVE_EFFECT);
                        return;
                    }

                    Sounds.play(Sound.TRIGGER);

                    if (CTX.saveGame.map == null) {
                        CTX.saveGame.map = Map.WORLD;
                        CTX.saveGame.wx = Map.WORLD.getStartX();
                        CTX.saveGame.wy = Map.WORLD.getStartY();
                    }

//                    CTX.saveGame.players[5].level = 10;
//                    CTX.saveGame.players[5].magePoints = new int[]{5, 5, 5, 5, 5, 5, 5};
//                    for (Spells s : Spells.values()) {
//                        CTX.saveGame.players[5].knownSpells.add(s);
//                    }

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

//        manual = new TextButton("Notes", Andius.skin, "default-24");
//        manual.addListener(new ChangeListener() {
//            @Override
//            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
//                Sounds.play(Sound.TRIGGER);
//                Andius.mainGame.setScreen(new BookScreen(StartScreen.this, Andius.skin));
//            }
//        });
//        manual.setBounds(360, Andius.SCREEN_HEIGHT - 500, 220, 40);
        stage = new Stage();
        //stage.addActor(manual);
        stage.addActor(manage);
        stage.addActor(journey);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
    }

    @Override
    public void render(float delta) {
        time += Gdx.graphics.getDeltaTime();

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        titleFont.draw(batch, "ANDIUS", 320, Andius.SCREEN_HEIGHT - 140);
        Andius.font72.draw(batch, "Bridgeburners", 250, Andius.SCREEN_HEIGHT - 240);
        batch.end();

        stage.act();
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            this.manual.toggle();
        }

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
