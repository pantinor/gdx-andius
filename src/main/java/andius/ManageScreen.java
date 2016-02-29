package andius;

import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ManageScreen implements Screen, Constants {

    Screen returnScreen;
    Stage stage;
    Batch batch;
    BitmapFont font;
    Texture bkgnd;

    List<RosterIndex> registry;
    List<PartyIndex> partyFormation;

    ImageButton apply;
    ImageButton clear;
    ImageButton add;
    ImageButton remove;

    ImageButton iconLeft, partyIconLeft;
    ImageButton iconRight, partyIconRight;

    TextButton save;
    TextButton cancel;

    TextField nameField;
    Slider strEdit;
    Slider intEdit;
    Slider dexEdit;
    Slider wisEdit;
    SelectBox<Profession> profSelect;
    SelectBox<ClassType> raceSelect;

    int pidx = 0;

    private static final String EMPTY = "<empty>";

    SaveGame saveGame = new SaveGame();

    public ManageScreen(Screen rs, Skin skin) {
        this.stage = new Stage();
        this.batch = new SpriteBatch();
        this.returnScreen = rs;

        font = Andius.font;

        bkgnd = new Texture(Gdx.files.classpath("assets/data/roster.png"));

        try {
            saveGame.read(SAVE_FILENAME);
        } catch (Exception e) {
        }

        PartyIndex[] mbrs = new PartyIndex[1];
        for (int i = 0; i < mbrs.length; i++) {
            CharacterRecord r = saveGame.players[i];
            if (r == null || r.name.length() < 1) {
                r = new CharacterRecord();
                r.name = EMPTY;
            }
            mbrs[i] = new PartyIndex(r, i + 1);
        }

        partyFormation = new List<>(skin);
        partyFormation.setItems(mbrs);

        RosterIndex[] recs = new RosterIndex[20];
        for (int i = 0; i < recs.length; i++) {
            recs[i] = new RosterIndex(new CharacterRecord(), i + 1);
            recs[i].character.name = EMPTY;
        }

        InputStream is;
        LittleEndianDataInputStream dis = null;
        try {
            is = new FileInputStream(Gdx.files.internal(ROSTER_FILENAME).file());
            dis = new LittleEndianDataInputStream(is);
            for (RosterIndex ri : recs) {
                try {
                    ri.character.read(dis);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }

        for (RosterIndex ri : recs) {
            if (ri.character.name.trim().length() < 1) {
                ri.character.name = EMPTY;
            }
        }

        registry = new List<>(skin);
        registry.setItems(recs);

        Skin imgBtnSkin = new Skin(Gdx.files.classpath("assets/skin/imgBtn.json"));

        apply = new ImageButton(imgBtnSkin, "left");
        clear = new ImageButton(imgBtnSkin, "clear");
        add = new ImageButton(imgBtnSkin, "right");
        remove = new ImageButton(imgBtnSkin, "left");
        cancel = new TextButton("Cancel", skin, "red");
        save = new TextButton("Save", skin, "red");
        iconLeft = new ImageButton(imgBtnSkin, "sm-arr-left");
        iconRight = new ImageButton(imgBtnSkin, "sm-arr-right");
        partyIconLeft = new ImageButton(imgBtnSkin, "sm-arr-left");
        partyIconRight = new ImageButton(imgBtnSkin, "sm-arr-right");

        apply.setX(326);
        apply.setY(Andius.SCREEN_HEIGHT - 200);

        clear.setX(326);
        clear.setY(Andius.SCREEN_HEIGHT - 292);

        add.setX(326);
        add.setY(Andius.SCREEN_HEIGHT - 396 - 50);

        remove.setX(326);
        remove.setY(Andius.SCREEN_HEIGHT - 472 - 50);

        save.setX(512);
        save.setY(Andius.SCREEN_HEIGHT - 42);

        cancel.setX(712);
        cancel.setY(Andius.SCREEN_HEIGHT - 42);

        iconLeft.setX(750);
        iconLeft.setY(Andius.SCREEN_HEIGHT - 165);
        iconRight.setX(825);
        iconRight.setY(Andius.SCREEN_HEIGHT - 165);

        partyIconLeft.setX(770);
        partyIconLeft.setY(Andius.SCREEN_HEIGHT - 684);
        partyIconRight.setX(770 + 75);
        partyIconRight.setY(Andius.SCREEN_HEIGHT - 684);

        apply.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                int st = (int) strEdit.getValue();
                int dx = (int) dexEdit.getValue();
                int in = (int) intEdit.getValue();
                int wi = (int) wisEdit.getValue();

                int total = st + dx + in + wi;
                if (total > 50 || nameField.getText().length() < 1) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }

                CharacterRecord sel = registry.getSelected().character;
                sel.name = nameField.getText();
                sel.race = raceSelect.getSelected();
                sel.profession = profSelect.getSelected();
                sel.str = (int) strEdit.getValue();
                sel.dex = (int) dexEdit.getValue();
                sel.intell = (int) intEdit.getValue();
                sel.wis = (int) wisEdit.getValue();
                sel.health = 150;
                sel.gold = 150;
                sel.portaitIndex = pidx;

                Sounds.play(Sound.TRIGGER);
            }
        });

        clear.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                registry.getSelected().character = new CharacterRecord();
                registry.getSelected().character.name = EMPTY;
                Sounds.play(Sound.TRIGGER);
            }
        });

        add.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                CharacterRecord rsel = registry.getSelected().character;
                CharacterRecord psel = partyFormation.getSelected().character;
                if (!psel.name.equals(EMPTY)) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }
                for (PartyIndex pi : partyFormation.getItems()) {
                    if (pi.character.name.equals(rsel.name)) {
                        Sounds.play(Sound.NEGATIVE_EFFECT);
                        return;
                    }
                }
                partyFormation.getSelected().character = rsel;
                Sounds.play(Sound.TRIGGER);
            }
        });

        remove.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                CharacterRecord psel = partyFormation.getSelected().character;
                if (psel.name.equals(EMPTY)) {
                    return;
                }
                RosterIndex found = null;
                for (RosterIndex ri : registry.getItems()) {
                    if (ri.character.name.equals(psel.name)) {
                        found = ri;
                        found.character = psel;
                        break;
                    }
                }
                if (found == null) {
                    for (RosterIndex ri : registry.getItems()) {
                        if (ri.character.name.equals(EMPTY)) {
                            found = ri;
                            found.character = psel;
                            break;
                        }
                    }
                }
                if (found == null) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                } else {
                    partyFormation.getSelected().character = new CharacterRecord();
                    partyFormation.getSelected().character.name = EMPTY;
                    Sounds.play(Sound.TRIGGER);
                }
            }
        });

        save.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                try {
                    FileOutputStream fos = new FileOutputStream(ROSTER_FILENAME);
                    LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(fos);
                    for (RosterIndex ri : registry.getItems()) {
                        if (ri.character.name.equals(EMPTY)) {
                            ri.character.name = null;
                        }
                        ri.character.write(dos);
                    }
                    saveGame.players[0] = partyFormation.getItems().get(0).character;
                    for (CharacterRecord r : saveGame.players) {
                        if (r.name.equals(EMPTY)) {
                            r.name = null;
                        }
                    }
                    
                    saveGame.write(SAVE_FILENAME);
                } catch (Exception e) {
                }
                Sounds.play(Sound.TRIGGER);
                Andius.mainGame.setScreen(returnScreen);
            }
        });

        cancel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Andius.mainGame.setScreen(returnScreen);
            }
        });

        iconLeft.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                pidx--;
                if (pidx < 0) {
                    pidx = 0;
                }
            }
        });

        iconRight.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                pidx++;
                if (pidx > 6 * 6 - 1) {
                    pidx = 6 * 6 - 1;
                }
            }
        });

        partyIconLeft.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                partyFormation.getSelected().character.portaitIndex--;
                if (partyFormation.getSelected().character.portaitIndex < 0) {
                    partyFormation.getSelected().character.portaitIndex = 0;
                }
            }
        });

        partyIconRight.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                partyFormation.getSelected().character.portaitIndex++;
                if (partyFormation.getSelected().character.portaitIndex > 6 * 6 - 1) {
                    partyFormation.getSelected().character.portaitIndex = 6 * 6 - 1;
                }
            }
        });

        ScrollPane sp1 = new ScrollPane(partyFormation, skin);
        sp1.setX(496);
        sp1.setY(264);
        sp1.setWidth(224);
        sp1.setHeight(96);

        ScrollPane sp2 = new ScrollPane(registry, skin);
        sp2.setX(80);
        sp2.setY(Andius.SCREEN_HEIGHT - 528 - 16);
        sp2.setWidth(160);
        sp2.setHeight(464);

        nameField = new TextField("", skin);
        strEdit = new Slider(5, 25, 1, false, skin);
        dexEdit = new Slider(5, 25, 1, false, skin);
        intEdit = new Slider(5, 25, 1, false, skin);
        wisEdit = new Slider(5, 25, 1, false, skin);
        profSelect = new SelectBox<>(skin);
        profSelect.setItems(Profession.values());
        profSelect.setSelected(Profession.FIGHTER);
        raceSelect = new SelectBox<>(skin);
        raceSelect.setItems(ClassType.values());
        raceSelect.setSelected(ClassType.HUMAN);

        int x = 580;
        nameField.setX(x);
        strEdit.setX(x);
        dexEdit.setX(x);
        intEdit.setX(x);
        wisEdit.setX(x);
        profSelect.setX(x);
        raceSelect.setX(x);

        int y = Andius.SCREEN_HEIGHT - 112;
        nameField.setY(y);
        raceSelect.setY(y -= 28);
        profSelect.setY(y -= 28);
        strEdit.setY(y -= 28);
        dexEdit.setY(y -= 28);
        intEdit.setY(y -= 28);
        wisEdit.setY(y -= 28);

        nameField.setMaxLength(16);
        profSelect.setWidth(100);
        raceSelect.setWidth(100);

        stage.addActor(nameField);
        stage.addActor(strEdit);
        stage.addActor(intEdit);
        stage.addActor(dexEdit);
        stage.addActor(wisEdit);
        stage.addActor(profSelect);
        stage.addActor(raceSelect);

        stage.addActor(apply);
        stage.addActor(remove);
        stage.addActor(clear);
        stage.addActor(save);
        stage.addActor(cancel);
        stage.addActor(add);
        stage.addActor(iconLeft);
        stage.addActor(iconRight);
        stage.addActor(partyIconLeft);
        stage.addActor(partyIconRight);
        stage.addActor(sp1);
        stage.addActor(sp2);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

//        if (Exodus.playMusic) {
//            if (Exodus.music != null) {
//                Exodus.music.stop();
//            }
//            Sound snd = Sound.M2;
//            Exodus.music = Sounds.play(snd, Exodus.musicVolume);
//        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(bkgnd, 0, 0);

        int viewY = Andius.SCREEN_HEIGHT - 96;
        int x = 495;

        font.draw(batch, "Name: ", x, viewY);
        font.draw(batch, "Race: ", x, viewY -= 28);
        font.draw(batch, "Type: ", x, viewY -= 28);
        font.draw(batch, "Strength: ", x, viewY -= 28);
        font.draw(batch, "Dexterity: ", x, viewY -= 28);
        font.draw(batch, "Intelligence: ", x, viewY -= 28);
        font.draw(batch, "Wisdom: ", x, viewY -= 28);

        int st = (int) strEdit.getValue();
        int dx = (int) dexEdit.getValue();
        int in = (int) intEdit.getValue();
        int wi = (int) wisEdit.getValue();

        int total = st + dx + in + wi;
        if (total > 50) {
            font.setColor(Color.RED);
        }

        font.draw(batch, st + "", x + 235, viewY += 28 * 3);
        font.draw(batch, dx + "", x + 235, viewY -= 28);
        font.draw(batch, in + "", x + 235, viewY -= 28);
        font.draw(batch, wi + "", x + 235, viewY -= 28);

        font.setColor(Color.WHITE);

        batch.draw(Andius.faceTiles[pidx], 768, Andius.SCREEN_HEIGHT - 168);

        CharacterRecord sel = this.registry.getSelected().character;

        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 90;

        font.draw(batch, "Name: " + sel.name, x, viewY);
        font.draw(batch, "Race: " + sel.race.toString(), x, viewY -= 18);
        font.draw(batch, "Type: " + sel.profession.toString(), x, viewY -= 18);
        font.draw(batch, "Status: " + sel.status.toString(), x, viewY -= 18);
        font.draw(batch, "Weapon: " + sel.weapon.toString(), x, viewY -= 24);
        font.draw(batch, "Armour: " + sel.armor.toString(), x, viewY -= 18);

        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 90 + 135;

        font.draw(batch, "Strength: " + sel.str, x, viewY);
        font.draw(batch, "Dexterity: " + sel.dex, x, viewY -= 18);
        font.draw(batch, "Intelligence: " + sel.intell, x, viewY -= 18);
        font.draw(batch, "Wisdom: " + sel.wis, x, viewY -= 18);
        font.draw(batch, "Hit Points: " + sel.health, x, viewY -= 42);
        font.draw(batch, "Experience: " + sel.exp, x, viewY -= 18);

        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 90 + 250;

        font.draw(batch, "Gold: " + sel.gold, x, viewY -= 18);

        batch.draw(Andius.faceTiles[sel.portaitIndex], 384, Andius.SCREEN_HEIGHT - 720);

        sel = this.partyFormation.getSelected().character;

        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 504;

        font.draw(batch, "Name: " + sel.name, x, viewY);
        font.draw(batch, "Race: " + sel.race.toString(), x, viewY -= 18);
        font.draw(batch, "Type: " + sel.profession.toString(), x, viewY -= 18);
        font.draw(batch, "Status: " + sel.status.toString(), x, viewY -= 18);
        font.draw(batch, "Weapon: " + sel.weapon.toString(), x, viewY -= 24);
        font.draw(batch, "Armour: " + sel.armor.toString(), x, viewY -= 18);

        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 504 + 135;

        font.draw(batch, "Strength: " + sel.str, x, viewY);
        font.draw(batch, "Dexterity: " + sel.dex, x, viewY -= 18);
        font.draw(batch, "Intelligence: " + sel.intell, x, viewY -= 18);
        font.draw(batch, "Wisdom: " + sel.wis, x, viewY -= 18);
        font.draw(batch, "Hit Points: " + sel.health, x, viewY -= 42);
        font.draw(batch, "Experience: " + sel.exp, x, viewY -= 18);

        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 504 + 250;

        font.draw(batch, "Gold: " + sel.gold, x, viewY -= 18);
        batch.draw(Andius.faceTiles[sel.portaitIndex], 792, Andius.SCREEN_HEIGHT - 720);

        batch.end();

        stage.act();
        stage.draw();
    }

    private class RosterIndex {

        CharacterRecord character;
        int index;

        RosterIndex(CharacterRecord sp, int idx) {
            this.character = sp;
            this.index = idx;
        }

        @Override
        public String toString() {
            return " " + index + " - " + character.name;
        }
    }

    private class PartyIndex {

        CharacterRecord character;
        int index;

        PartyIndex(CharacterRecord sp, int idx) {
            this.character = sp;
            this.index = idx;
        }

        @Override
        public String toString() {
            return " Active Player " + index + " : " + character.name;
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
    public void hide() {
    }

    @Override
    public void dispose() {
    }

}
