package andius;

import static andius.Andius.CTX;
import static andius.Andius.mainGame;
import static andius.Constants.SAVE_FILENAME;
import andius.objects.SaveGame;
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
                    Sounds.play(Sound.TRIGGER);

                    if (CTX == null) {
                        CTX = new Context();
                        SaveGame saveGame = SaveGame.read(SAVE_FILENAME);
                        CTX.setSaveGame(saveGame);
                    }

                    if (CTX.saveGame.map == null) {
                        CTX.saveGame.map = Map.WORLD;
                        CTX.saveGame.wx = Map.WORLD.getStartX();
                        CTX.saveGame.wy = Map.WORLD.getStartY();
                    }

                    BaseScreen scr = (BaseScreen) CTX.saveGame.map.getScreen();
                    scr.load(CTX.saveGame);

                    mainGame.setScreen(scr);

                    //RewardScreen rs = new RewardScreen(CTX, Map.WIWOLD, 1, 230, Andius.REWARDS.get(20), Andius.REWARDS.get(20));
                    //mainGame.setScreen(rs);
//                    CTX.saveGame.players[0].inventory.add(ITEMS_MAP.get("CHAIN MAIL +1").clone());
//                    CTX.saveGame.players[0].inventory.add(ITEMS_MAP.get("MACE +1").clone());
//                    CTX.saveGame.players[0].inventory.add(ITEMS_MAP.get("STAFF").clone());
//                    CTX.saveGame.players[0].inventory.add(ITEMS_MAP.get("HELM").clone());
//                    CTX.saveGame.players[0].inventory.add(ITEMS_MAP.get("DAGGER OF SPEED").clone());
//                        Item tmp = ITEMS_MAP.get("DAGGER OF SPEED").clone();
//                        tmp.unidentified = true;
//                        CTX.saveGame.players[0].inventory.add(tmp);
//                    CTX.saveGame.players[0].inventory.add(ITEMS_MAP.get("SHORT SWORD").clone());
//                    CTX.saveGame.players[0].inventory.add(ITEMS_MAP.get("WERDNAS AMULET").clone());
//                    CTX.saveGame.players[0].inventory.add(ITEMS_MAP.get("SCROLL OF BADIOS").clone());
//                    CTX.saveGame.players[0].inventory.add(ITEMS_MAP.get("PLATE MAIL +1").clone());
//                    CTX.saveGame.players[0].inventory.add(ITEMS_MAP.get("LEATHER +1").clone());
                    //EquipmentScreen es = new EquipmentScreen(CTX, Map.WIWOLD);
                    //mainGame.setScreen(es);
//                        CTX.saveGame.players[0].hp = 5;
//                        CTX.saveGame.players[1].hp = 0;
//                        CTX.saveGame.players[1].status = Status.DEAD;
//                        CTX.saveGame.players[2].hp = 5;
//                        CTX.saveGame.players[2].status = Status.PARALYZED;
//                        CTX.saveGame.players[3].gold = 500;
//                        TempleScreen rs = new TempleScreen(CTX, Map.WIWOLD);
//                        mainGame.setScreen(rs);
//                        CTX.saveGame.players[0].exp = 1500;
//                        CTX.saveGame.players[2].exp = 2500;
//                        CTX.saveGame.players[1].exp = 3500;
                    //InnScreen es = new InnScreen(CTX, Map.WIWOLD);
                    //mainGame.setScreen(es);
                    //VendorScreen es = new VendorScreen(CTX, Role.MERCHANT1, Map.WIWOLD);
                    //mainGame.setScreen(es);
                    for (int j = 0; j < 6; j++) {
                        //CTX.saveGame.players[j].exp = 1500;

//                        CTX.saveGame.players[j].armor = ITEMS_MAP.get("CHAIN MAIL +1").clone();
//                        CTX.saveGame.players[j].weapon = ITEMS_MAP.get("MACE +1").clone();
//                        CTX.saveGame.players[j].helm = ITEMS_MAP.get("HELM").clone();
//                        CTX.saveGame.players[j].shield = ITEMS_MAP.get("LARGE SHIELD").clone();
//                        CTX.saveGame.players[j].glove = ITEMS_MAP.get("SILVER GLOVES").clone();
//                        CTX.saveGame.players[j].item1 = ITEMS_MAP.get("ROD OF FLAME").clone();
//                        CTX.saveGame.players[j].item2 = ITEMS_MAP.get("WERDNAS AMULET").clone();
//                        CTX.saveGame.players[j].inventory.add(ITEMS_MAP.get("DAGGER OF SPEED").clone());
//                        CTX.saveGame.players[j].inventory.add(ITEMS_MAP.get("LEATHER +1").clone());
//                        CTX.saveGame.players[j].magePoints = new int[]{5, 5, 5, 5, 5, 5, 5};
//                        CTX.saveGame.players[j].clericPoints = new int[]{5, 5, 5, 5, 5, 5, 5};
//                        CTX.saveGame.players[j].spellPresets[5] = Spells.KATINO;
//                        for (Spells s : Spells.values()) {
//                            CTX.saveGame.players[j].knownSpells.add(s);
//                        }
                    }

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
