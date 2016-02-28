package andius;

import andius.objects.SaveGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 *
 * @author Paul
 */
public class StartScreen implements Screen, Constants {

    float time = 0;
    Batch batch;
    //Texture title;

    TextButton manual;
    TextButton manage;
    TextButton journey;

    BitmapFont titleFont;

    Stage stage;

    public StartScreen() {

        titleFont = new BitmapFont(Gdx.files.classpath("assets/fonts/exodus.fnt"));

        batch = new SpriteBatch();

        manual = new TextButton("Manual", Andius.skin, "default");
        manual.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                //Sounds.play(Sound.TRIGGER);
                //Andius.mainGame.setScreen(new BookScreen(StartScreen.this, Exodus.skin));
            }
        });
        manual.setX(200);
        manual.setY(Andius.SCREEN_HEIGHT - 410);
        manual.setWidth(150);
        manual.setHeight(25);

        manage = new TextButton("Manage", Andius.skin, "default");
        manage.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                //Sounds.play(Sound.TRIGGER);
                Andius.mainGame.setScreen(new ManageScreen(StartScreen.this, Andius.skin));
            }
        });
        manage.setX(400);
        manage.setY(Andius.SCREEN_HEIGHT - 410);
        manage.setWidth(150);
        manage.setHeight(25);

        journey = new TextButton("Journey Onward", Andius.skin, "default");
        journey.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                //Sounds.play(Sound.TRIGGER);
                if (!Gdx.files.internal(SAVE_FILENAME).file().exists()) {
                    Andius.mainGame.setScreen(new ManageScreen(StartScreen.this, Andius.skin));
                } else {
                    SaveGame saveGame = new SaveGame();
                    try {
                        saveGame.read(SAVE_FILENAME);
                    } catch (Exception e) {
                    }
                    SaveGame.CharacterRecord r = saveGame.players[0];
                    if (r == null || r.name.length() < 1) {
                        Andius.mainGame.setScreen(new ManageScreen(StartScreen.this, Andius.skin));
                    } else {
                        Andius.mainGame.setScreen(Map.values()[saveGame.map].getScreen());
                        stage.clear();
                    }
                }

            }
        });
        journey.setX(600);
        journey.setY(Andius.SCREEN_HEIGHT - 410);
        journey.setWidth(150);
        journey.setHeight(25);

        stage = new Stage();
        stage.addActor(manual);
        stage.addActor(manage);
        stage.addActor(journey);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

//        if (Exodus.playMusic) {
//            if (Exodus.music != null) {
//                Exodus.music.stop();
//            }
//            Sound snd = Sound.SPLASH;
//            Exodus.music = Sounds.play(snd, Exodus.musicVolume);
//        }
    }

    @Override
    public void hide() {
    }

    @Override
    public void render(float delta) {
        time += Gdx.graphics.getDeltaTime();

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//        camera.update();
//
//        splashRenderer.setView(camera.combined, 0, 0, 19 * tilePixelWidth, 8 * tilePixelHeight);
//        splashRenderer.render();
        batch.begin();
        //batch.draw(title, 0, 0);
        titleFont.draw(batch, "ANDIUS", 320, Andius.SCREEN_HEIGHT - 140);
        Andius.ultimaFont.draw(batch, "Bridgeburners", 250, Andius.SCREEN_HEIGHT - 240);
        //Andius.largeFont.draw(batch, "From the depths of shadow...he comes for VENGEANCE!", 300, Andius.SCREEN_HEIGHT - 342);
        //Andius.largeFont.draw(batch, "LIBGDX Conversion by Paul Antinori", 350, 84);
        //Andius.largeFont.draw(batch, "Copyright 2016 Paul Antinori", 375, 48);
        batch.end();

        stage.act();
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        //viewPort.update(width, height, false);
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
