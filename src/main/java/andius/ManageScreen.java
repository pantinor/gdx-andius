package andius;

import static andius.Andius.ITEMS;
import andius.objects.Race;
import andius.objects.ClassType;
import static andius.Constants.ROSTER_FILENAME;
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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;
import utils.Utils;

public class ManageScreen implements Screen, Constants {

    Screen returnScreen;
    Stage stage;
    Batch batch;
    BitmapFont font;
    Texture bkgnd;

    Label rosText;
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
    TextButton reset;

    TextField nameField;

    ImageButton strMinus, strPlus;
    ImageButton intMinus, intPlus;
    ImageButton piMinus, piPlus;
    ImageButton vitMinus, vitPlus;
    ImageButton agMinus, agPlus;
    ImageButton luMinus, luPlus;

    List<ClassType> profSelect;
    SelectBox<Race> raceSelect;

    int stVal, inVal, piVal, viVal, agVal, luVal;
    int stExt, inExt, piExt, viExt, agExt, luExt;

    ExtraPoints extraPoints = new ExtraPoints(Utils.getRandomBetween(5, 20));
    int pidx = 0;

    private static final String EMPTY = "<empty>";

    SaveGame saveGame;

    public ManageScreen(Screen rs, Skin skin) {
        this.stage = new Stage();
        this.batch = new SpriteBatch();
        this.returnScreen = rs;

        font = Andius.font;

        bkgnd = new Texture(Gdx.files.classpath("assets/data/roster.png"));

        try {
            saveGame = SaveGame.read(SAVE_FILENAME);
        } catch (Exception e) {
            saveGame = new SaveGame();
        }

        PartyIndex[] mbrs = new PartyIndex[6];
        for (int i = 0; i < mbrs.length; i++) {
            CharacterRecord r = null;
            try {
                r = saveGame.players[i];
            } catch (Exception e) {
            }
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

        try {
            GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(Gdx.files.internal(ROSTER_FILENAME).file()));
            String b64 = IOUtils.toString(gzis, StandardCharsets.UTF_8);
            gzis.close();
            String json = Base64Coder.decodeString(b64);
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            RosterIndex[] r = gson.fromJson(json, RosterIndex[].class);
            for (int i = 0; i < 20; i++) {
                recs[i] = r[i];
            }
        } catch (Exception e) {
            //nothing
        }

        for (RosterIndex ri : recs) {
            if (ri.character.name == null) {
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
        reset = new TextButton("Reset", skin, "default");
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

        reset.setX(740);
        reset.setY(Andius.SCREEN_HEIGHT - 340);

        iconLeft.setX(769);
        iconLeft.setY(Andius.SCREEN_HEIGHT - 125);
        iconRight.setX(840);
        iconRight.setY(Andius.SCREEN_HEIGHT - 125);

        partyIconLeft.setX(776);
        partyIconLeft.setY(Andius.SCREEN_HEIGHT - 710);
        partyIconRight.setX(770 + 77);
        partyIconRight.setY(Andius.SCREEN_HEIGHT - 710);

        apply.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                if (profSelect.getSelected() == null || nameField.getText().length() < 1) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }

                CharacterRecord sel = registry.getSelected().character;
                sel.name = nameField.getText();
                sel.race = raceSelect.getSelected();
                sel.classType = profSelect.getSelected();
                sel.str = stVal + stExt;
                sel.intell = inVal + inExt;
                sel.piety = piVal + piExt;
                sel.vitality = viVal + viExt;
                sel.agility = agVal + agExt;
                sel.luck = luVal + luExt;

                sel.hp = sel.getMoreHP();
                sel.maxhp = sel.hp;
                sel.gold = Utils.getRandomBetween(100, 200);
                sel.portaitIndex = pidx;
                sel.weapon = ITEMS.get(101).clone();
                
                sel.magePoints = sel.getMaxMageSpellPoints();
                sel.magePoints = sel.getMaxClericSpellPoints();
                
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
                    for (RosterIndex ri : registry.getItems()) {
                        if (ri.character.name.equals(EMPTY)) {
                            ri.character.name = null;
                        }
                    }
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    String json = gson.toJson(registry.getItems().toArray());
                    String b64 = Base64Coder.encodeString(json);
                    FileOutputStream fos = new FileOutputStream(ROSTER_FILENAME);
                    GZIPOutputStream gzos = new GZIPOutputStream(fos);
                    gzos.write(b64.getBytes("UTF-8"));
                    gzos.close();

                    Array<CharacterRecord> sgchars = new Array<>();
                    for (PartyIndex pi : partyFormation.getItems()) {
                        if (!pi.character.name.equals(EMPTY)) {
                            sgchars.add(pi.character);
                        }
                    }
                    saveGame.players = sgchars.toArray(CharacterRecord.class);
                    saveGame.write(SAVE_FILENAME);
                } catch (Exception e) {
                    e.printStackTrace();
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

        reset.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                stExt = 0;
                inExt = 0;
                piExt = 0;
                viExt = 0;
                agExt = 0;
                luExt = 0;
                extraPoints = new ExtraPoints(Utils.getRandomBetween(5, 20));
                checkClasses();
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

        rosText = new Label("Roster", skin);
        rosText.setX(80);
        rosText.setY(Andius.SCREEN_HEIGHT - 55);

        ScrollPane sp1 = new ScrollPane(partyFormation, skin);
        sp1.setX(490);
        sp1.setY(232);
        sp1.setWidth(224);
        sp1.setHeight(143);

        ScrollPane sp2 = new ScrollPane(registry, skin);
        sp2.setX(80);
        sp2.setY(Andius.SCREEN_HEIGHT - 528 - 16);
        sp2.setWidth(160);
        sp2.setHeight(464);

        nameField = new TextField("", skin);
        strMinus = new ImageButton(imgBtnSkin, "minus");
        strPlus = new ImageButton(imgBtnSkin, "plus");
        intMinus = new ImageButton(imgBtnSkin, "minus");
        intPlus = new ImageButton(imgBtnSkin, "plus");
        piMinus = new ImageButton(imgBtnSkin, "minus");
        piPlus = new ImageButton(imgBtnSkin, "plus");
        vitMinus = new ImageButton(imgBtnSkin, "minus");
        vitPlus = new ImageButton(imgBtnSkin, "plus");
        agMinus = new ImageButton(imgBtnSkin, "minus");
        agPlus = new ImageButton(imgBtnSkin, "plus");
        luMinus = new ImageButton(imgBtnSkin, "minus");
        luPlus = new ImageButton(imgBtnSkin, "plus");

        profSelect = new List<>(skin);
        ScrollPane classPane = new ScrollPane(profSelect, skin);

        raceSelect = new SelectBox<>(skin);
        raceSelect.setItems(Race.values());
        raceSelect.setSelected(Race.HUMAN);

        stVal = Race.HUMAN.getInitialStrength();
        inVal = Race.HUMAN.getInitialIntell();
        piVal = Race.HUMAN.getInitialPiety();
        viVal = Race.HUMAN.getInitialVitality();
        agVal = Race.HUMAN.getInitialAgility();
        luVal = Race.HUMAN.getInitialLuck();

        raceSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Race race = raceSelect.getSelected();
                stVal = race.getInitialStrength();
                inVal = race.getInitialIntell();
                piVal = race.getInitialPiety();
                viVal = race.getInitialVitality();
                agVal = race.getInitialAgility();
                luVal = race.getInitialLuck();
                stExt = 0;
                inExt = 0;
                piExt = 0;
                viExt = 0;
                agExt = 0;
                luExt = 0;
                extraPoints = new ExtraPoints(Utils.getRandomBetween(5, 20));
                checkClasses();
            }
        });

        strMinus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (extraPoints.val < extraPoints.max && stExt > 0) {
                    extraPoints.incr();
                    stExt--;
                }
                checkClasses();
            }
        });
        strPlus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (extraPoints.val > 0) {
                    extraPoints.decr();
                    stExt++;
                }
                checkClasses();
            }
        });
        intMinus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (extraPoints.val < extraPoints.max && inExt > 0) {
                    extraPoints.incr();
                    inExt--;
                }
                checkClasses();
            }
        });
        intPlus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (extraPoints.val > 0) {
                    extraPoints.decr();
                    inExt++;
                }
                checkClasses();
            }
        });
        piMinus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (extraPoints.val < extraPoints.max && piExt > 0) {
                    extraPoints.incr();
                    piExt--;
                }
                checkClasses();
            }
        });
        piPlus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (extraPoints.val > 0) {
                    extraPoints.decr();
                    piExt++;
                }
                checkClasses();
            }
        });
        vitMinus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (extraPoints.val < extraPoints.max && viExt > 0) {
                    extraPoints.incr();
                    viExt--;
                }
                checkClasses();
            }
        });
        vitPlus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (extraPoints.val > 0) {
                    extraPoints.decr();
                    viExt++;
                }
                checkClasses();
            }
        });
        agMinus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (extraPoints.val < extraPoints.max && agExt > 0) {
                    extraPoints.incr();
                    agExt--;
                }
                checkClasses();
            }
        });
        agPlus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (extraPoints.val > 0) {
                    extraPoints.decr();
                    agExt++;
                }
                checkClasses();
            }
        });
        luMinus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (extraPoints.val < extraPoints.max && luExt > 0) {
                    extraPoints.incr();
                    luExt--;
                }
                checkClasses();
            }
        });
        luPlus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (extraPoints.val > 0) {
                    extraPoints.decr();
                    luExt++;
                }
                checkClasses();
            }
        });

        int x = 580;
        nameField.setX(x);
        strMinus.setX(x);
        intMinus.setX(x);
        piMinus.setX(x);
        vitMinus.setX(x);
        agMinus.setX(x);
        luMinus.setX(x);

        strPlus.setX(x + 35);
        intPlus.setX(x + 35);
        piPlus.setX(x + 35);
        vitPlus.setX(x + 35);
        agPlus.setX(x + 35);
        luPlus.setX(x + 35);

        classPane.setX(715);
        classPane.setY(Andius.SCREEN_HEIGHT - 300);

        raceSelect.setX(x);

        int y = Andius.SCREEN_HEIGHT - 112;
        nameField.setY(y);
        raceSelect.setY(y -= 28);
        strMinus.setY(y -= 28);
        intMinus.setY(y -= 28);
        piMinus.setY(y -= 28);
        vitMinus.setY(y -= 28);
        agMinus.setY(y -= 28);
        luMinus.setY(y -= 28);

        y = Andius.SCREEN_HEIGHT - 112;
        strPlus.setY(y -= 28 * 2);
        intPlus.setY(y -= 28);
        piPlus.setY(y -= 28);
        vitPlus.setY(y -= 28);
        agPlus.setY(y -= 28);
        luPlus.setY(y -= 28);

        nameField.setMaxLength(16);
        classPane.setWidth(100);
        raceSelect.setWidth(100);

        stage.addActor(nameField);
        stage.addActor(strMinus);
        stage.addActor(intMinus);
        stage.addActor(piMinus);
        stage.addActor(vitMinus);
        stage.addActor(agMinus);
        stage.addActor(luMinus);

        stage.addActor(strPlus);
        stage.addActor(intPlus);
        stage.addActor(piPlus);
        stage.addActor(vitPlus);
        stage.addActor(agPlus);
        stage.addActor(luPlus);

        stage.addActor(classPane);
        stage.addActor(raceSelect);

        stage.addActor(apply);
        stage.addActor(remove);
        stage.addActor(clear);
        stage.addActor(save);
        stage.addActor(cancel);
        stage.addActor(reset);
        stage.addActor(add);
        stage.addActor(iconLeft);
        stage.addActor(iconRight);
        stage.addActor(partyIconLeft);
        stage.addActor(partyIconRight);
        stage.addActor(sp1);
        stage.addActor(sp2);
        stage.addActor(rosText);

    }

    private void checkClasses() {
        Array<ClassType> items = new Array<>();
        for (ClassType ct : ClassType.values()) {
            boolean s = stVal + stExt >= ct.getMinStr();
            boolean i = inVal + inExt >= ct.getMinIntell();
            boolean p = piVal + piExt >= ct.getMinPiety();
            boolean v = viVal + viExt >= ct.getMinVitality();
            boolean a = agVal + agExt >= ct.getMinAgility();
            boolean l = stVal + stExt >= ct.getMinLuck();
            if (s && i && p && v && a && l) {
                items.add(ct);
            }
            profSelect.setItems(items);
        }
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
        font.draw(batch, "Strength: ", x, viewY -= 28);
        font.draw(batch, "Intelligence: ", x, viewY -= 28);
        font.draw(batch, "Piety: ", x, viewY -= 28);
        font.draw(batch, "Vitality: ", x, viewY -= 28);
        font.draw(batch, "Agility: ", x, viewY -= 28);
        font.draw(batch, "Luck: ", x, viewY -= 28);
        font.draw(batch, "Extra Points: ", x, viewY -= 28);

        viewY = Andius.SCREEN_HEIGHT - 96;

        font.draw(batch, stVal + stExt + "", 600, viewY -= 28 * 2);
        font.draw(batch, inVal + inExt + "", 600, viewY -= 28);
        font.draw(batch, piVal + piExt + "", 600, viewY -= 28);
        font.draw(batch, viVal + viExt + "", 600, viewY -= 28);
        font.draw(batch, agVal + agExt + "", 600, viewY -= 28);
        font.draw(batch, luVal + luExt + "", 600, viewY -= 28);
        font.draw(batch, extraPoints.val + "", 600, viewY -= 28);

        font.setColor(Color.WHITE);

        batch.draw(Andius.faceTiles[pidx], 785, Andius.SCREEN_HEIGHT - 131);

        CharacterRecord sel = this.registry.getSelected().character;

        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 90;

        font.draw(batch, "Name: " + sel.name, x, viewY);
        font.draw(batch, "Race: " + sel.race.toString(), x, viewY -= 18);
        font.draw(batch, "Type: " + sel.classType.toString(), x, viewY -= 18);
        font.draw(batch, "Status: " + sel.status.toString(), x, viewY -= 18);
        if (sel.weapon != null) {
            font.draw(batch, "Weapon: " + sel.weapon.getName(), x, viewY -= 24);
        }
        if (sel.armor != null) {
            font.draw(batch, "Armour: " + sel.armor.getName(), x, viewY -= 18);
        }

        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 90 + 135;

        font.draw(batch, "Strength: " + sel.str, x, viewY);
        font.draw(batch, "Intelligence: " + sel.intell, x, viewY -= 18);
        font.draw(batch, "Piety: " + sel.piety, x, viewY -= 18);
        font.draw(batch, "Vitality: " + sel.vitality, x, viewY -= 18);
        font.draw(batch, "Agility: " + sel.agility, x, viewY -= 18);
        font.draw(batch, "Luck: " + sel.luck, x, viewY -= 18);

        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 90 + 250;

        font.draw(batch, "Gold: " + sel.gold, x, viewY -= 18);
        font.draw(batch, "Hit Points: " + sel.hp, x, viewY -= 18);
        font.draw(batch, "Experience: " + sel.exp, x, viewY -= 18);

        batch.draw(Andius.faceTiles[sel.portaitIndex], 383, Andius.SCREEN_HEIGHT - 721);

        sel = this.partyFormation.getSelected().character;

        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 504;

        font.draw(batch, "Name: " + sel.name, x, viewY);
        font.draw(batch, "Race: " + sel.race.toString(), x, viewY -= 18);
        font.draw(batch, "Type: " + sel.classType.toString(), x, viewY -= 18);
        font.draw(batch, "Status: " + sel.status.toString(), x, viewY -= 18);
        if (sel.weapon != null) {
            font.draw(batch, "Weapon: " + sel.weapon.getName(), x, viewY -= 24);
        }
        if (sel.armor != null) {
            font.draw(batch, "Armour: " + sel.armor.getName(), x, viewY -= 18);
        }
        
        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 504 + 135;

        font.draw(batch, "Strength: " + sel.str, x, viewY);
        font.draw(batch, "Intelligence: " + sel.intell, x, viewY -= 18);
        font.draw(batch, "Piety: " + sel.piety, x, viewY -= 18);
        font.draw(batch, "Vitality: " + sel.vitality, x, viewY -= 18);
        font.draw(batch, "Agility: " + sel.agility, x, viewY -= 18);
        font.draw(batch, "Luck: " + sel.luck, x, viewY -= 18);

        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 504 + 250;

        font.draw(batch, "Gold: " + sel.gold, x, viewY -= 18);
        font.draw(batch, "Hit Points: " + sel.hp, x, viewY -= 18);
        font.draw(batch, "Experience: " + sel.exp, x, viewY -= 18);

        batch.draw(Andius.faceTiles[sel.portaitIndex], 792, Andius.SCREEN_HEIGHT - 719);

        batch.end();

        stage.act();
        stage.draw();
    }

    private class ExtraPoints {

        int val;
        final int max;

        ExtraPoints(int max) {
            this.val = this.max = max;
        }

        void incr() {
            val++;
            if (val > max) {
                val = max;
            }
        }

        void decr() {
            val--;
            if (val < 0) {
                val = 0;
            }
        }
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
