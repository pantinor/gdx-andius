package andius;

import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import andius.objects.Sound;
import andius.objects.Sounds;
import andius.objects.Race;
import andius.objects.ClassType;
import static andius.Constants.ROSTER_FILENAME;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Spells;
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
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import utils.FrameMaker;
import utils.Utils;

public class ManageScreen implements Screen, Constants {

    Screen returnScreen;
    Stage stage;
    Batch batch;
    BitmapFont font;
    Texture bkgnd;

    Label rosText, activePartyText, roleCharacterText;
    List<RosterIndex> registry;
    List<PartyIndex> partyFormation;

    TextButton apply;
    TextButton clear;
    TextButton add;
    TextButton remove;

    ImageButton iconLeft, partyIconLeft;
    ImageButton iconRight, partyIconRight;

    TextButton save;
    TextButton cancel;
    TextButton reset;

    UppercaseTextField nameField;

    ImageButton strMinus, strPlus;
    ImageButton intMinus, intPlus;
    ImageButton piMinus, piPlus;
    ImageButton vitMinus, vitPlus;
    ImageButton agMinus, agPlus;
    ImageButton luMinus, luPlus;

    List<ClassType> classTypeSelection;
    ButtonGroup<CheckBox> raceGroup = new ButtonGroup<>();
    CheckBox[] cbRace = new CheckBox[Race.values().length];

    SelectBox<ClassType> changeClassBox;
    TextButton changeClassBtn;

    int stVal, inVal, piVal, viVal, agVal, luVal;
    int stExt, inExt, piExt, viExt, agExt, luExt;

    ExtraPoints extraPoints = new ExtraPoints();
    int pidx = 0;

    private static final String EMPTY = "(empty)";

    public ManageScreen(Screen rs, Skin skin, SaveGame saveGame) {

        this.stage = new Stage();
        this.batch = new SpriteBatch();
        this.returnScreen = rs;

        font = Andius.font16;

        FrameMaker fm = new FrameMaker(SCREEN_WIDTH, SCREEN_HEIGHT);

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

        partyFormation = new List<>(skin, "default-16-padded-clear");
        partyFormation.setItems(mbrs);

        RosterIndex[] recs = new RosterIndex[20];
        for (int i = 0; i < recs.length; i++) {
            recs[i] = new RosterIndex(new CharacterRecord(), i + 1);
            recs[i].character.name = EMPTY;
        }

        try {
            //GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(Gdx.files.internal(ROSTER_FILENAME).file()));
            //String b64 = IOUtils.toString(gzis, StandardCharsets.UTF_8);
            //gzis.close();
            //String json = Base64Coder.decodeString(b64);
            FileInputStream fis = new FileInputStream(Gdx.files.internal(ROSTER_FILENAME).file());
            String json = IOUtils.toString(fis, StandardCharsets.UTF_8);
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

        registry = new List<>(skin, "default-16-padded-clear");
        registry.setItems(recs);

        Skin imgBtnSkin = new Skin(Gdx.files.classpath("assets/skin/imgBtn.json"));

        apply = new TextButton("APPLY NEW TO ROSTER", skin, "default-16-green");
        clear = new TextButton("CLEAR FROM ROSTER", skin, "default-16-red");
        add = new TextButton("ROSTER TO PARTY", skin, "default-16-green");
        remove = new TextButton("REMOVE FROM PARTY", skin, "default-16-red");

        cancel = new TextButton("CANCEL", skin, "default-16");
        reset = new TextButton("RESET", skin, "default-16");
        save = new TextButton("SAVE", skin, "default-16");

        iconLeft = new ImageButton(imgBtnSkin, "sm-arr-left");
        iconRight = new ImageButton(imgBtnSkin, "sm-arr-right");
        partyIconLeft = new ImageButton(imgBtnSkin, "sm-arr-left");
        partyIconRight = new ImageButton(imgBtnSkin, "sm-arr-right");

        apply.setBounds(245, Andius.SCREEN_HEIGHT - 170 - 40, 215, 40);
        clear.setBounds(245, Andius.SCREEN_HEIGHT - 220 - 40, 215, 40);
        add.setBounds(245, Andius.SCREEN_HEIGHT - 420 - 40, 215, 40);
        remove.setBounds(245, Andius.SCREEN_HEIGHT - 470 - 40, 215, 40);

        save.setBounds(260, Andius.SCREEN_HEIGHT - 120, 80, 40);
        cancel.setBounds(370, Andius.SCREEN_HEIGHT - 120, 80, 40);

        reset.setBounds(774, Andius.SCREEN_HEIGHT - 302 - 40, 80, 40);

        iconLeft.setX(764);
        iconLeft.setY(Andius.SCREEN_HEIGHT - 125);
        iconRight.setX(764 + 80);
        iconRight.setY(Andius.SCREEN_HEIGHT - 125);

        partyIconLeft.setX(771);
        partyIconLeft.setY(Andius.SCREEN_HEIGHT - 710);
        partyIconRight.setX(771 + 80);
        partyIconRight.setY(Andius.SCREEN_HEIGHT - 710);

        apply.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                if (classTypeSelection.getSelected() == null || nameField.getText().length() < 1) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }

                CharacterRecord sel = registry.getSelected().character;
                sel.name = nameField.getText();
                sel.race = (Race) raceGroup.getChecked().getUserObject();
                sel.classType = classTypeSelection.getSelected();
                sel.str = stVal + stExt;
                sel.intell = inVal + inExt;
                sel.piety = piVal + piExt;
                sel.vitality = viVal + viExt;
                sel.agility = agVal + agExt;
                sel.luck = luVal + luExt;

                sel.hp = sel.getMoreHP();
                sel.maxhp = sel.hp;
                sel.level = 1;
                sel.gold = Utils.getRandomBetween(100, 200);
                sel.portaitIndex = pidx;

                if (sel.classType == ClassType.MAGE || sel.classType == ClassType.BISHOP) {
                    sel.knownSpells.add(Spells.values()[1]);
                    sel.knownSpells.add(Spells.values()[3]);
                    sel.magePoints[0] = 2;
                }
                if (sel.classType == ClassType.PRIEST) {
                    sel.knownSpells.add(Spells.values()[23]);
                    sel.knownSpells.add(Spells.values()[24]);
                    sel.clericPoints[0] = 2;
                }

                SaveGame.setSpellPoints(sel);

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
                updateChangeClassOptions();
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
                    Gson gson = builder.setPrettyPrinting().create();
                    String json = gson.toJson(registry.getItems().toArray());
                    //String b64 = Base64Coder.encodeString(json);
                    FileOutputStream fos = new FileOutputStream(ROSTER_FILENAME);
                    //GZIPOutputStream gzos = new GZIPOutputStream(fos);
                    //gzos.write(b64.getBytes("UTF-8"));
                    //gzos.close();
                    fos.write(json.getBytes("UTF-8"));
                    fos.close();

                    Array<CharacterRecord> sgchars = new Array<>();
                    for (PartyIndex pi : partyFormation.getItems()) {
                        if (!pi.character.name.equals(EMPTY)) {
                            sgchars.add(pi.character);
                        }
                    }
                    saveGame.players = sgchars.toArray(CharacterRecord.class);
                    saveGame.write();
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
                extraPoints = new ExtraPoints();
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

        rosText = new Label("ROSTER", skin, "default-16");
        rosText.setX(64);
        rosText.setY(Andius.SCREEN_HEIGHT - 54);

        ScrollPane sp2 = new ScrollPane(registry, skin);
        fm.setBoundsFancy(sp2, 64, Andius.SCREEN_HEIGHT - 64 - 464, 160, 464, new Color(0x326c8eff));
        fm.setBoundsFancy(null, 64, Andius.SCREEN_HEIGHT - 576 - 160, 384, 160, new Color(0x326c8eff));
        fm.setBoundsFancy(null, 383, Andius.SCREEN_HEIGHT - 673 - 48, 48, 48, new Color(0x2e2e2eff));

        activePartyText = new Label("ACTIVE PARTY - SAVED GAME FILE", skin, "default-16");
        activePartyText.setX(480);
        activePartyText.setY(Andius.SCREEN_HEIGHT - 395);

        ScrollPane sp1 = new ScrollPane(partyFormation, skin);
        fm.setBoundsFancy(sp1, 480, 215, 384, 143, new Color(0x0f6905ff));
        fm.setBoundsFancy(null, 480, Andius.SCREEN_HEIGHT - 576 - 160, 384, 160, new Color(0x0f6905ff));
        fm.setBoundsFancy(null, 792, Andius.SCREEN_HEIGHT - 673 - 48, 48, 48, new Color(0x2e2e2eff));

        nameField = new UppercaseTextField("", skin, "default-16");
        nameField.setMaxLength(16);

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

        roleCharacterText = new Label("ROLE NEW CHARACTER", skin, "default-16");
        roleCharacterText.setX(480);
        roleCharacterText.setY(Andius.SCREEN_HEIGHT - 54);

        classTypeSelection = new List<>(skin, "default-16-padded-clear");
        ScrollPane classTypeScrollPane = new ScrollPane(classTypeSelection, skin);

        fm.setBoundsFancy(null, 480, Andius.SCREEN_HEIGHT - 64 - 288, 384, 288, new Color(0x7a5d98ff));
        fm.setBoundsFancy(null, 785, Andius.SCREEN_HEIGHT - 83 - 48, 48, 48, new Color(0x2e2e2eff));

        stVal = Race.HUMAN.getInitialStrength();
        inVal = Race.HUMAN.getInitialIntell();
        piVal = Race.HUMAN.getInitialPiety();
        viVal = Race.HUMAN.getInitialVitality();
        agVal = Race.HUMAN.getInitialAgility();
        luVal = Race.HUMAN.getInitialLuck();

        raceGroup = new ButtonGroup<>();
        raceGroup.setMinCheckCount(1);
        raceGroup.setMaxCheckCount(1);

        ChangeListener onRaceChanged = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                CheckBox cb = (CheckBox) actor;
                if (!cb.isChecked()) {
                    return;
                }
                Race race = (Race) cb.getUserObject();
                stVal = race.getInitialStrength();
                inVal = race.getInitialIntell();
                piVal = race.getInitialPiety();
                viVal = race.getInitialVitality();
                agVal = race.getInitialAgility();
                luVal = race.getInitialLuck();
                stExt = inExt = piExt = viExt = agExt = luExt = 0;
                extraPoints = new ExtraPoints();
                checkClasses();
            }
        };

        for (Race r : Race.values()) {
            cbRace[r.ordinal()] = new CheckBox(r.name(), Andius.skin, "default-16");
            cbRace[r.ordinal()].addListener(onRaceChanged);
            cbRace[r.ordinal()].setUserObject(r);
            raceGroup.add(cbRace[r.ordinal()]);
            stage.addActor(cbRace[r.ordinal()]);
        }

        raceGroup.getButtons().first().setChecked(true);

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
                if (extraPoints.val > 0 && stExt + stVal < 18) {
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
                if (extraPoints.val > 0 && inExt + inVal < 18) {
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
                if (extraPoints.val > 0 && piExt + piVal < 18) {
                    extraPoints.decr();
                    piExt++;
                }
                checkClasses();
            }
        });
        vitMinus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (extraPoints.val < extraPoints.max && viExt > 0 && viExt + viVal < 19) {
                    extraPoints.incr();
                    viExt--;
                }
                checkClasses();
            }
        });
        vitPlus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (extraPoints.val > 0 && viExt + viVal < 18) {
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
                if (extraPoints.val > 0 && agExt + agVal < 18) {
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
                if (extraPoints.val > 0 && luExt + luVal < 18) {
                    extraPoints.decr();
                    luExt++;
                }
                checkClasses();
            }
        });

        int x = 490;
        int y = SCREEN_HEIGHT - 90;
        nameField.setPosition(x + 55, y);
        cbRace[0].setPosition(x, y -= 28);
        cbRace[1].setPosition(x, y -= 28);
        cbRace[2].setPosition(x, y -= 28);
        cbRace[3].setPosition(x, y -= 28);
        cbRace[4].setPosition(x, y -= 28);

        x = 637;
        y = SCREEN_HEIGHT - 115;
        strMinus.setPosition(x, y);
        intMinus.setPosition(x, y -= 28);
        piMinus.setPosition(x, y -= 28);
        vitMinus.setPosition(x, y -= 28);
        agMinus.setPosition(x, y -= 28);
        luMinus.setPosition(x, y -= 28);

        y = SCREEN_HEIGHT - 115;
        strPlus.setPosition(x + 46, y);
        intPlus.setPosition(x + 46, y -= 28);
        piPlus.setPosition(x + 46, y -= 28);
        vitPlus.setPosition(x + 46, y -= 28);
        agPlus.setPosition(x + 46, y -= 28);
        luPlus.setPosition(x + 46, y -= 28);

        changeClassBox = new SelectBox<>(skin, "default-16-dark-background");
        changeClassBtn = new TextButton("CHANGE CLASS", skin, "default-16-green");

        changeClassBox.setBounds(728, Andius.SCREEN_HEIGHT - 35 - 460, 130, 35);
        changeClassBtn.setBounds(728, Andius.SCREEN_HEIGHT - 35 - 510, 130, 35);

        partyFormation.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateChangeClassOptions();
            }
        });
        changeClassBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                CharacterRecord sel = partyFormation.getSelected().character;
                if (sel == null || sel.name.equals(EMPTY)) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }
                ClassType target = changeClassBox.getSelected();
                if (target == null || target == sel.classType) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }
                applyClassChange(sel, target);
                updateChangeClassOptions();
                Sounds.play(Sound.TRIGGER);
            }
        });

        updateChangeClassOptions();

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

        classTypeScrollPane.setPosition(715, SCREEN_HEIGHT - 300);
        stage.addActor(classTypeScrollPane);

        stage.addActor(apply);
        stage.addActor(remove);
        stage.addActor(clear);
        stage.addActor(add);

        stage.addActor(save);
        stage.addActor(cancel);
        stage.addActor(reset);

        stage.addActor(iconLeft);
        stage.addActor(iconRight);
        stage.addActor(partyIconLeft);
        stage.addActor(partyIconRight);

        stage.addActor(sp1);
        stage.addActor(sp2);
        stage.addActor(rosText);
        stage.addActor(activePartyText);
        stage.addActor(roleCharacterText);

        stage.addActor(changeClassBox);
        stage.addActor(changeClassBtn);

        //stage.setDebugAll(true);
        bkgnd = fm.build();

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
            classTypeSelection.setItems(items);
        }
    }

    private void updateChangeClassOptions() {
        Array<ClassType> items = new Array<>();
        CharacterRecord sel = partyFormation.getSelected() != null ? partyFormation.getSelected().character : null;
        if (sel != null && !sel.name.equals(EMPTY)) {
            for (ClassType ct : ClassType.values()) {
                boolean s = sel.str >= ct.getMinStr();
                boolean i = sel.intell >= ct.getMinIntell();
                boolean p = sel.piety >= ct.getMinPiety();
                boolean v = sel.vitality >= ct.getMinVitality();
                boolean a = sel.agility >= ct.getMinAgility();
                boolean l = sel.luck >= ct.getMinLuck();
                if (s && i && p && v && a && l) {
                    items.add(ct);
                }
            }
        }
        changeClassBox.setItems(items);
        if (sel != null && items.contains(sel.classType, true)) {
            changeClassBox.setSelected(sel.classType);
        }
    }

    private void applyClassChange(SaveGame.CharacterRecord c, ClassType newClass) {
        c.classType = newClass;
        c.level = 1;
        c.exp = 0;

        c.hp = c.getMoreHP();
        c.maxhp = c.hp;

        c.knownSpells.clear();
        for (int i = 0; i < c.magePoints.length; i++) {
            c.magePoints[i] = 0;
        }
        for (int i = 0; i < c.clericPoints.length; i++) {
            c.clericPoints[i] = 0;
        }

        if (newClass == ClassType.MAGE || newClass == ClassType.BISHOP) {
            c.knownSpells.add(Spells.values()[1]);
            c.knownSpells.add(Spells.values()[3]);
            c.magePoints[0] = 2;
        }
        if (newClass == ClassType.PRIEST) {
            c.knownSpells.add(Spells.values()[23]);
            c.knownSpells.add(Spells.values()[24]);
            c.clericPoints[0] = 2;
        }

        SaveGame.setSpellPoints(c);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0x18 / 255f, 0x18 / 255f, 0x18 / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(bkgnd, 0, 0);

        int x = 490;
        int viewY = Andius.SCREEN_HEIGHT - 75;

        font.draw(batch, "NAME", x, viewY);

        x = 600;
        viewY = Andius.SCREEN_HEIGHT - 75;

        font.draw(batch, "STR", x, viewY -= 28);
        font.draw(batch, "INT", x, viewY -= 28);
        font.draw(batch, "PIE", x, viewY -= 28);
        font.draw(batch, "VIT", x, viewY -= 28);
        font.draw(batch, "AGI", x, viewY -= 28);
        font.draw(batch, "LCK", x, viewY -= 28);

        x = 658;
        viewY = Andius.SCREEN_HEIGHT - 75;

        font.draw(batch, stVal + stExt + "", x, viewY -= 28);
        font.draw(batch, inVal + inExt + "", x, viewY -= 28);
        font.draw(batch, piVal + piExt + "", x, viewY -= 28);
        font.draw(batch, viVal + viExt + "", x, viewY -= 28);
        font.draw(batch, agVal + agExt + "", x, viewY -= 28);
        font.draw(batch, luVal + luExt + "", x, viewY -= 28);
        font.draw(batch, extraPoints.val + "", x, viewY -= 28);

        font.setColor(Color.WHITE);

        batch.draw(Andius.faceTiles[pidx], 785, Andius.SCREEN_HEIGHT - 83 - 48);

        CharacterRecord sel = this.registry.getSelected().character;

        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 90;

        font.draw(batch, "NAME: " + sel.name.toUpperCase(), x, viewY);
        font.draw(batch, "LVL: " + sel.level, x, viewY -= 18);
        font.draw(batch, "RACE: " + sel.race.toString().toUpperCase(), x, viewY -= 18);
        font.draw(batch, "CLASS: " + sel.classType.toString().toUpperCase(), x, viewY -= 18);
        font.draw(batch, "STAT: " + sel.status.toString(), x, viewY -= 18);

        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 90 + 145;

        font.draw(batch, "GLD: " + sel.gold, x, viewY);
        font.draw(batch, "HP: " + sel.hp, x, viewY -= 18);
        font.draw(batch, "EXP: " + sel.exp, x, viewY -= 18);
        font.draw(batch, "INV: " + sel.inventory.size(), x, viewY -= 18);

        batch.draw(Andius.faceTiles[sel.portaitIndex], 383, Andius.SCREEN_HEIGHT - 673 - 48);

        sel = this.partyFormation.getSelected().character;

        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 504;

        font.draw(batch, "NAME: " + sel.name.toUpperCase(), x, viewY);
        font.draw(batch, "LVL: " + sel.level, x, viewY -= 18);
        font.draw(batch, "RACE: " + sel.race.toString().toUpperCase(), x, viewY -= 18);
        font.draw(batch, "CLASS: " + sel.classType.toString().toUpperCase(), x, viewY -= 18);
        font.draw(batch, "STAT: " + sel.status.toString().toUpperCase(), x, viewY -= 18);

        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 504 + 145;

        font.draw(batch, "STR: " + sel.str, x, viewY);
        font.draw(batch, "INT: " + sel.intell, x, viewY -= 18);
        font.draw(batch, "PTY: " + sel.piety, x, viewY -= 18);
        font.draw(batch, "VIT: " + sel.vitality, x, viewY -= 18);
        font.draw(batch, "AGI: " + sel.agility, x, viewY -= 18);
        font.draw(batch, "LCK: " + sel.luck, x, viewY -= 18);

        viewY = Andius.SCREEN_HEIGHT - 590;
        x = 504 + 220;

        font.draw(batch, "GLD: " + sel.gold, x, viewY);
        font.draw(batch, "HP: " + sel.hp, x, viewY -= 18);
        font.draw(batch, "EXP: " + sel.exp, x, viewY -= 18);

        batch.draw(Andius.faceTiles[sel.portaitIndex], 792, Andius.SCREEN_HEIGHT - 673 - 48);

        int[] ms = sel.magePoints;
        int[] cs = sel.clericPoints;
        String d = String.format("M: %d %d %d %d %d %d %d  P: %d %d %d %d %d %d %d",
                ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6], cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]);
        font.draw(batch, d, 504, 60);

        batch.end();

        stage.act();
        stage.draw();
    }

    private class ExtraPoints {

        int val;
        final int max;

        ExtraPoints() {
            this.val = this.max = 10;
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
            return " " + index + " - " + character.name.toUpperCase();
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
            return " " + index + " - " + character.name.toUpperCase();
        }
    }

    public class UppercaseTextField extends TextField {

        public UppercaseTextField(String text, Skin skin) {
            super(text, skin);
            setTextFieldListener((field, c) -> enforceUpper());
        }

        public UppercaseTextField(String text, Skin skin, String styleName) {
            super(text, skin, styleName);
            setTextFieldListener((field, c) -> enforceUpper());
        }

        private void enforceUpper() {
            String t = getText();
            String up = t.toUpperCase(java.util.Locale.ROOT);
            if (!t.equals(up)) {
                int cursor = getCursorPosition();
                setText(up);
                setCursorPosition(Math.min(cursor, up.length()));
            }
        }

        @Override
        public void setText(String text) {
            super.setText(text == null ? null : text.toUpperCase(java.util.Locale.ROOT));
        }

        @Override
        public void appendText(String text) {
            super.appendText(text == null ? "" : text.toUpperCase(java.util.Locale.ROOT));
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
