package andius;

import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import andius.objects.Sound;
import andius.objects.Sounds;
import andius.objects.Race;
import andius.objects.Gender;
import andius.objects.Portraits;
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
import com.badlogic.gdx.utils.Scaling;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import utils.Utils;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import static utils.SkinUtils.GOLD;
import static utils.SkinUtils.INK;
import static utils.SkinUtils.MUTED;
import static utils.SkinUtils.createSkin;

public class ManageScreen implements Screen, Constants {

    Screen returnScreen;
    Stage stage;
    private final Skin uiSkin;
    private final Texture uiPixel;
    private Label rosterSummary, partySummary, partySpells, pointsLabel, footerHint, classHint;
    private Image rosterPortrait;
    private final Label[] statValues = new Label[6];
    List<RosterIndex> registry;
    List<PartyIndex> partyFormation;

    TextButton apply;
    TextButton clear;
    TextButton add;
    TextButton remove;

    private Image newPortrait, partyPortrait;
    private SelectBox<Gender> genderSelection, rosterGender, partyGender;

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

    private static final String EMPTY = "(empty)";

    private static final float PORTRAIT_W = 76;
    private static final float PORTRAIT_H = 76;

    public ManageScreen(Screen rs, Skin skin, SaveGame saveGame) {

        this.stage = new Stage(new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.uiSkin = createSkin(skin);
        this.uiPixel = Utils.fillRectangle(1, 1, Color.WHITE, 1);
        this.returnScreen = rs;

        configureStyles(skin);

        PartyIndex[] mbrs = new PartyIndex[6];
        for (int i = 0; i < mbrs.length; i++) {
            CharacterRecord r = null;
            try {
                r = saveGame.players[i];
            } catch (Exception e) {
                // ignore and substitute empty slot below
            }
            sanitizeCharacter(r);
            if (r == null || r.name == null || r.name.length() < 1) {
                r = new CharacterRecord();
                r.name = EMPTY;
            }

            mbrs[i] = new PartyIndex(r, i + 1);
        }

        partyFormation = new List<>(uiSkin, "default-12-padded-clear");
        partyFormation.setItems(mbrs);

        RosterIndex[] recs = new RosterIndex[20];
        for (int i = 0; i < recs.length; i++) {
            recs[i] = new RosterIndex(new CharacterRecord(), i + 1);
            recs[i].character.name = EMPTY;
        }

        try (FileInputStream fis = new FileInputStream(Gdx.files.internal(ROSTER_FILENAME).file())) {
            //GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(Gdx.files.internal(ROSTER_FILENAME).file()));
            //String b64 = IOUtils.toString(gzis, StandardCharsets.UTF_8);
            //gzis.close();
            //String json = Base64Coder.decodeString(b64);
            String json = IOUtils.toString(fis, StandardCharsets.UTF_8);
            Gson gson = new GsonBuilder().create();
            RosterIndex[] loaded = gson.fromJson(json, RosterIndex[].class);

            if (loaded != null) {
                for (int i = 0; i < Math.min(recs.length, loaded.length); i++) {
                    if (loaded[i] != null) {
                        recs[i] = loaded[i];
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (RosterIndex ri : recs) {
            if (ri.character == null) {
                ri.character = new CharacterRecord();
            }
            sanitizeCharacter(ri.character);
            if (ri.character.name == null) {
                ri.character.name = EMPTY;
            }

        }

        registry = new List<>(uiSkin, "default-12-padded-clear");
        registry.setItems(recs);


        apply = new TextButton("Create in selected slot", uiSkin);
        clear = new TextButton("Delete character", uiSkin);
        add = new TextButton("Assign to party", uiSkin);
        remove = new TextButton("Return to roster", uiSkin);

        cancel = new TextButton("Cancel", uiSkin);
        reset = new TextButton("Reset", uiSkin);
        save = new TextButton("Save & return", uiSkin);




        apply.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                if (classTypeSelection.getSelected() == null || nameField.getText().length() < 1) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }

                CharacterRecord sel = registry.getSelected().character;
                sanitizeCharacter(sel);

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
                sel.exp = 0;
                sel.gold = Utils.getRandomBetween(100, 200);
                sel.gender = genderSelection.getSelected();

                sel.knownSpells.clear();
                for (int i = 0; i < sel.magePoints.length; i++) {
                    sel.magePoints[i] = 0;
                }
                for (int i = 0; i < sel.clericPoints.length; i++) {
                    sel.clericPoints[i] = 0;
                }

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

                if (rsel == null || EMPTY.equals(rsel.name)) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }

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
                RosterIndex found = findEmptyRosterSlot();
                if (found == null) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                } else {
                    found.character = psel;
                    partyFormation.getSelected().character = new CharacterRecord();
                    partyFormation.getSelected().character.name = EMPTY;
                    updateChangeClassOptions();
                    Sounds.play(Sound.TRIGGER);
                }
            }
        });

        save.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                try {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();

                    RosterIndex[] out = buildRosterForSave();
                    String json = gson.toJson(out);

                    try (FileOutputStream fos = new FileOutputStream(ROSTER_FILENAME)) {
                        fos.write(json.getBytes(StandardCharsets.UTF_8));
                    }

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
                    footerHint.setText("Save failed. Your changes are still here; try again.");
                    return;
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
                nameField.setText("");
                genderSelection.setSelected(Gender.MALE);
                raceGroup.getButtons().first().setChecked(true);
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

        nameField = new UppercaseTextField("", uiSkin, "default");
        nameField.setMaxLength(16);

        strMinus = new ImageButton(new ImageButton.ImageButtonStyle());
        strPlus = new ImageButton(new ImageButton.ImageButtonStyle());
        intMinus = new ImageButton(new ImageButton.ImageButtonStyle());
        intPlus = new ImageButton(new ImageButton.ImageButtonStyle());
        piMinus = new ImageButton(new ImageButton.ImageButtonStyle());
        piPlus = new ImageButton(new ImageButton.ImageButtonStyle());
        vitMinus = new ImageButton(new ImageButton.ImageButtonStyle());
        vitPlus = new ImageButton(new ImageButton.ImageButtonStyle());
        agMinus = new ImageButton(new ImageButton.ImageButtonStyle());
        agPlus = new ImageButton(new ImageButton.ImageButtonStyle());
        luMinus = new ImageButton(new ImageButton.ImageButtonStyle());
        luPlus = new ImageButton(new ImageButton.ImageButtonStyle());

        classTypeSelection = new List<>(uiSkin, "default-12-padded-clear");

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
            cbRace[r.ordinal()] = new CheckBox(r.name(), uiSkin);
            cbRace[r.ordinal()].addListener(onRaceChanged);
            cbRace[r.ordinal()].setUserObject(r);
            raceGroup.add(cbRace[r.ordinal()]);
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

        changeClassBox = new SelectBox<>(uiSkin);
        changeClassBtn = new TextButton("Change class", uiSkin);


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

        newPortrait = new Image();
        partyPortrait = new Image();
        for (Image image : new Image[] {newPortrait, partyPortrait}) {
            image.setScaling(Scaling.fit);
            image.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        }
        genderSelection = genderBox();
        rosterGender = genderBox();
        partyGender = genderBox();
        rosterGender.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (!EMPTY.equals(registry.getSelected().character.name)) registry.getSelected().character.gender = rosterGender.getSelected();
            }
        });
        partyGender.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (!EMPTY.equals(partyFormation.getSelected().character.name)) partyFormation.getSelected().character.gender = partyGender.getSelected();
            }
        });

        updateChangeClassOptions();

        buildLayout();
        checkClasses();
        refreshUI();
    }

    private Drawable surface(String color) {
        return new TextureRegionDrawable(new TextureRegion(uiPixel)).tint(Color.valueOf(color));
    }

    private void configureStyles(Skin shared) {
        TextButton.TextButtonStyle button = new TextButton.TextButtonStyle(uiSkin.get(TextButton.TextButtonStyle.class));
        button.up = surface("223443");
        button.over = surface("344c5d");
        button.down = surface("475d68");
        button.disabled = surface("15222d");
        button.fontColor = Color.valueOf("e7e6df");
        button.disabledFontColor = MUTED;
        uiSkin.add("default", button);

        List.ListStyle list = new List.ListStyle(uiSkin.get("default-12-padded-clear", List.ListStyle.class));
        com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable selection = (com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable) surface("344b59");
        selection.setTopHeight(5);
        selection.setBottomHeight(5);
        selection.setLeftWidth(8);
        selection.setRightWidth(8);
        list.selection = selection;
        list.fontColorSelected = GOLD;
        list.fontColorUnselected = Color.valueOf("dce3e6");
        list.background = surface("101d28");
        uiSkin.add("default-12-padded-clear", list);

        TextField.TextFieldStyle field = new TextField.TextFieldStyle(shared.get("default-16", TextField.TextFieldStyle.class));
        field.font = uiSkin.getFont("emphasis");
        field.background = surface("0c1822");
        field.focusedBackground = surface("263e4c");
        field.fontColor = Color.WHITE;
        uiSkin.add("default", field);
        CheckBox.CheckBoxStyle check = new CheckBox.CheckBoxStyle(shared.get("default-16", CheckBox.CheckBoxStyle.class));
        check.font = uiSkin.getFont("body");
        check.fontColor = Color.valueOf("dce3e6");
        com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable off = (com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable) surface("344b59");
        off.setMinWidth(12);
        off.setMinHeight(12);
        com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable on = (com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable) surface("dbbb7c");
        on.setMinWidth(12);
        on.setMinHeight(12);
        check.checkboxOff = off;
        check.checkboxOn = on;
        uiSkin.add("default", check);
        SelectBox.SelectBoxStyle select = new SelectBox.SelectBoxStyle(shared.get("default-16-dark-background", SelectBox.SelectBoxStyle.class));
        select.font = uiSkin.getFont("emphasis");
        select.listStyle = new List.ListStyle(list);
        select.background = surface("223443");
        uiSkin.add("default", select);
        ScrollPane.ScrollPaneStyle scrolling = new ScrollPane.ScrollPaneStyle();
        com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable track = (com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable) surface("0c1822");
        track.setMinWidth(6);
        com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable knob = (com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable) surface("7f755f");
        knob.setMinWidth(6);
        knob.setMinHeight(24);
        scrolling.vScroll = track;
        scrolling.vScrollKnob = knob;
        uiSkin.add("default", scrolling);
        select.scrollStyle = scrolling;
        com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle window =
                new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle(uiSkin.get(com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle.class));
        window.background = surface("15232f");
        window.titleFontColor = GOLD;
        uiSkin.add("default", window);
    }

    private Label text(String value, String style, Color color) {
        Label label = new Label(value, uiSkin, style);
        label.setColor(color);
        return label;
    }

    private Table panel(String title, String subtitle) {
        Table panel = new Table();
        panel.background(surface("15232f"));
        panel.pad(12).top();
        panel.add(text(title, "default-16", GOLD)).growX().left().row();
        panel.add(text(subtitle, "default", MUTED)).growX().padTop(3).padBottom(10).row();
        Image rule = new Image(surface("66583f"));
        panel.add(rule).growX().height(1).padBottom(12).row();
        return panel;
    }

    private ScrollPane scroll(Actor actor) {
        ScrollPane scroll = new ScrollPane(actor, uiSkin);
        scroll.setScrollingDisabled(true, false);
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);
        scroll.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override public void enter(InputEvent event, float x, float y, int pointer, Actor from) {
                if (pointer == -1) stage.setScrollFocus(scroll);
            }
        });
        return scroll;
    }

    private SelectBox<Gender> genderBox() {
        SelectBox<Gender> box = new SelectBox<>(uiSkin);
        box.setItems(Gender.values());
        return box;
    }

    private void portrait(Image image, TextureRegion region) {
        if (image.getDrawable() instanceof TextureRegionDrawable current && current.getRegion() == region) return;
        image.setDrawable(new TextureRegionDrawable(region));
    }

    private void primary(TextButton button) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(button.getStyle());
        style.up = surface("b59b67");
        style.over = surface("dbc28c");
        style.down = surface("947c50");
        style.fontColor = INK;
        button.setStyle(style);
    }

    private void buildLayout() {
        Table root = new Table();
        root.setFillParent(true);
        root.pad(24);
        stage.addActor(root);
        Table header = new Table();
        header.add(text("Adventurers' Guild", "default-24", GOLD)).left().expandX();
        header.add(text("PARTY MANAGEMENT", "default-16", MUTED)).right();
        root.add(header).growX().height(38).row();
        root.add(text("Recruit adventurers, shape their abilities, and assemble your expedition.", "default", MUTED))
                .growX().padBottom(18).row();

        Table roster = panel("01  /  Roster", "20 reserve slots");
        roster.add(scroll(registry)).growX().height(218).row();
        roster.add(text("SELECTED ADVENTURER", "default", GOLD)).left().padTop(12).padBottom(8).row();
        Table reserveCard = new Table();
        rosterPortrait = new Image();
        rosterPortrait.setScaling(Scaling.fit);
        Table reserveIdentity = new Table();
        reserveIdentity.add(rosterPortrait).size(PORTRAIT_W, PORTRAIT_H).row();
        reserveIdentity.add(rosterGender).width(PORTRAIT_W).height(26).padTop(6);
        reserveCard.add(reserveIdentity).top().padRight(10);
        rosterSummary = text("", "default", Color.WHITE);
        rosterSummary.setWrap(true);
        reserveCard.add(rosterSummary).width(178).top();
        roster.add(reserveCard).growX().height(166).top().row();
        roster.add(add).growX().height(36).padTop(8).row();
        roster.add(clear).growX().height(32).padTop(6).row();
        roster.add().expandY();

        Table recruit = panel("02  /  Create character", "Create in the selected roster slot");
        Table identity = new Table();
        identity.add(newPortrait).size(PORTRAIT_W, PORTRAIT_H).padRight(12);
        Table name = new Table();
        name.add(text("NAME", "default", GOLD)).left().growX().row();
        name.add(nameField).growX().height(32).padTop(2).row();
        Table gender = new Table();
        gender.add(text("GENDER", "default", GOLD)).left().padRight(8);
        gender.add(genderSelection).growX().height(26);
        name.add(gender).growX().padTop(4);
        identity.add(name).growX();
        recruit.add(identity).growX().row();
        recruit.add(text("ANCESTRY", "default", GOLD)).left().padTop(12).row();
        Table ancestry = new Table();
        for (int i = 0; i < cbRace.length; i++) {
            cbRace[i].getLabel().setText(Race.values()[i].name().substring(0, 1)
                    + Race.values()[i].name().substring(1).toLowerCase(java.util.Locale.ROOT));
            cbRace[i].getImageCell().padRight(5);
            ancestry.add(cbRace[i]).width(112).height(26).left();
            if (i == 2) ancestry.row();
        }
        recruit.add(ancestry).growX().padBottom(6).row();
        pointsLabel = text("", "default", GOLD);
        recruit.add(pointsLabel).growX().padBottom(4).row();
        Table attributes = new Table();
        String[] names = {"Strength", "Intellect", "Piety", "Vitality", "Agility", "Luck"};
        ImageButton[] minus = {strMinus, intMinus, piMinus, vitMinus, agMinus, luMinus};
        ImageButton[] plus = {strPlus, intPlus, piPlus, vitPlus, agPlus, luPlus};
        for (int i = 0; i < names.length; i++) {
            styleAttributeButton(minus[i], "-");
            styleAttributeButton(plus[i], "+");
            attributes.add(text(names[i], "default", Color.WHITE)).growX().left();
            attributes.add(minus[i]).size(28, 26).padRight(8);
            statValues[i] = text("", "default", GOLD);
            statValues[i].setAlignment(Align.center);
            attributes.add(statValues[i]).width(24);
            attributes.add(plus[i]).size(28, 26).padLeft(8);
            attributes.row();
        }
        recruit.add(attributes).growX().row();
        recruit.add(text("ELIGIBLE CLASS", "default", GOLD)).left().padTop(10).padBottom(4).row();
        com.badlogic.gdx.scenes.scene2d.ui.Stack classes = new com.badlogic.gdx.scenes.scene2d.ui.Stack();
        classes.add(scroll(classTypeSelection));
        classHint = text("Spend attribute points to unlock classes.", "default", MUTED);
        classHint.setAlignment(Align.center);
        classHint.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        classes.add(classHint);
        recruit.add(classes).growX().height(66).row();
        Table createActions = new Table();
        createActions.add(reset).width(68).height(36).padRight(8);
        createActions.add(apply).growX().height(36);
        recruit.add(createActions).growX().padTop(10).row();
        recruit.add().expandY();

        Table party = panel("03  /  Active party", "6 formation slots  /  select a destination");
        party.add(partyFormation).growX().height((float) Math.ceil(partyFormation.getPrefHeight())).row();
        party.add(text("SELECTED PARTY MEMBER", "default", GOLD)).left().padTop(12).padBottom(8).row();
        Table memberCard = new Table();
        Table memberIdentity = new Table();
        memberIdentity.add(partyPortrait).size(PORTRAIT_W, PORTRAIT_H).row();
        memberIdentity.add(partyGender).width(PORTRAIT_W).height(26).padTop(6);
        memberCard.add(memberIdentity).top().padRight(10);
        partySummary = text("", "default", Color.WHITE);
        partySummary.setWrap(true);
        memberCard.add(partySummary).width(170).top();
        party.add(memberCard).height(164).growX().top().row();
        partySpells = text("", "default", MUTED);
        party.add(partySpells).growX().height(40).row();
        party.add(text("CLASS TRAINING  /  resets level to 1", "default", GOLD)).left().padTop(8).padBottom(6).row();
        Table training = new Table();
        training.add(changeClassBox).width(126).height(32).padRight(8);
        training.add(changeClassBtn).growX().height(32);
        party.add(training).growX().row();
        party.add(remove).growX().height(32).padTop(8).row();
        party.add().expandY();

        Table columns = new Table();
        columns.add(roster).width(288).growY().padRight(16);
        columns.add(recruit).width(368).growY().padRight(16);
        columns.add(party).width(288).growY();
        root.add(columns).grow().row();
        Table footer = new Table();
        footerHint = text("Select a roster slot to create or assign an adventurer.", "default", MUTED);
        footer.add(footerHint).expandX().left();
        footer.add(cancel).width(92).height(38).padRight(10);
        footer.add(save).width(150).height(38);
        root.add(footer).growX().height(62).bottom();
        primary(apply);
        primary(save);
        clear.getLabel().setColor(Color.valueOf("d89b91"));
    }

    private String summary(CharacterRecord c) {
        if (EMPTY.equals(c.name)) return "Empty slot\n\nReady for an adventurer.";
        return c.name + "\nLevel " + c.level + " " + c.race + "\n" + c.classType + "  /  " + c.status
                + "\nHP  " + c.hp + " / " + c.maxhp + "\nGold  " + c.gold + "\nExperience  " + c.exp
                + "\nItems  " + c.inventory.size()
                + "\nSTR " + c.str + "   INT " + c.intell + "   PIE " + c.piety
                + "\nVIT " + c.vitality + "   AGI " + c.agility + "   LCK " + c.luck;
    }

    private void styleAttributeButton(ImageButton button, String glyph) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = surface("223443");
        style.over = surface("344c5d");
        style.down = surface("66583f");
        style.disabled = surface("101d28");
        button.setStyle(style);
        button.clearChildren();
        button.add(text(glyph, "default", GOLD)).center();
    }

    private void refreshUI() {
        CharacterRecord reserve = registry.getSelected().character;
        CharacterRecord member = partyFormation.getSelected().character;
        sanitizeCharacter(reserve);
        sanitizeCharacter(member);
        rosterSummary.setText(summary(reserve));
        partySummary.setText(summary(member));
        portrait(rosterPortrait, Portraits.get(reserve));
        rosterPortrait.setVisible(!EMPTY.equals(reserve.name));
        portrait(newPortrait, Portraits.get(genderSelection.getSelected(), (Race) raceGroup.getChecked().getUserObject(), classTypeSelection.getSelected()));
        portrait(partyPortrait, Portraits.get(member));
        partyPortrait.setVisible(!EMPTY.equals(member.name));
        rosterGender.setSelected(reserve.gender);
        rosterGender.setDisabled(EMPTY.equals(reserve.name));
        partyGender.setSelected(member.gender);
        partyGender.setDisabled(EMPTY.equals(member.name));
        int[] base = {stVal, inVal, piVal, viVal, agVal, luVal};
        int[] extra = {stExt, inExt, piExt, viExt, agExt, luExt};
        ImageButton[] minus = {strMinus, intMinus, piMinus, vitMinus, agMinus, luMinus};
        ImageButton[] plus = {strPlus, intPlus, piPlus, vitPlus, agPlus, luPlus};
        for (int i = 0; i < 6; i++) {
            statValues[i].setText(Integer.toString(base[i] + extra[i]));
            minus[i].setDisabled(extra[i] == 0);
            plus[i].setDisabled(extraPoints.val == 0 || base[i] + extra[i] >= 18);
            minus[i].setColor(extra[i] == 0 ? MUTED : Color.WHITE);
            plus[i].setColor(plus[i].isDisabled() ? MUTED : Color.WHITE);
        }
        pointsLabel.setText("ATTRIBUTES   /   " + extraPoints.val + " points remaining");
        classHint.setVisible(classTypeSelection.getItems().isEmpty());
        apply.setDisabled(nameField.getText().trim().isEmpty() || classTypeSelection.getSelected() == null);
        clear.setDisabled(EMPTY.equals(reserve.name));
        boolean inParty = false;
        for (PartyIndex slot : partyFormation.getItems()) {
            if (!EMPTY.equals(reserve.name) && slot.character.name.equals(reserve.name)) inParty = true;
        }
        add.setDisabled(EMPTY.equals(reserve.name) || !EMPTY.equals(member.name) || inParty);
        remove.setDisabled(EMPTY.equals(member.name) || findEmptyRosterSlot() == null);
        changeClassBtn.setDisabled(EMPTY.equals(member.name) || changeClassBox.getSelected() == null
                || changeClassBox.getSelected() == member.classType);
        changeClassBox.setDisabled(EMPTY.equals(member.name));
        partySpells.setText(EMPTY.equals(member.name) ? "Choose an empty slot, then Assign to party."
                : "Mage  " + spellPoints(member.magePoints) + "\nPriest  " + spellPoints(member.clericPoints));
    }

    private RosterIndex findEmptyRosterSlot() {
        for (RosterIndex slot : registry.getItems()) {
            if (EMPTY.equals(slot.character.name)) return slot;
        }
        return null;
    }

    private String spellPoints(int[] points) {
        return java.util.Arrays.toString(points).replace("[", "").replace("]", "").replace(", ", " / ");
    }

    private void checkClasses() {
        ClassType selected = classTypeSelection.getSelected();
        Array<ClassType> items = new Array<>();
        for (ClassType ct : ClassType.values()) {
            boolean s = stVal + stExt >= ct.getMinStr();
            boolean i = inVal + inExt >= ct.getMinIntell();
            boolean p = piVal + piExt >= ct.getMinPiety();
            boolean v = viVal + viExt >= ct.getMinVitality();
            boolean a = agVal + agExt >= ct.getMinAgility();
            boolean l = luVal + luExt >= ct.getMinLuck();
            if (s && i && p && v && a && l) {
                items.add(ct);
            }
        }
        classTypeSelection.setItems(items);
        if (selected != null && items.contains(selected, true)) classTypeSelection.setSelected(selected);
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

    private void sanitizeCharacter(CharacterRecord c) {
        if (c == null) {
            return;
        }
        if (c.name == null) {
            c.name = EMPTY;
        }
        if (c.gender == null) c.gender = Gender.MALE;
        if (c.knownSpells == null) {
            c.knownSpells = new java.util.ArrayList<>();
        }
        if (c.inventory == null) {
            c.inventory = new java.util.ArrayList<>();
        }
        if (c.magePoints == null || c.magePoints.length != 7) {
            c.magePoints = new int[7];
        }
        if (c.clericPoints == null || c.clericPoints.length != 7) {
            c.clericPoints = new int[7];
        }
    }

    private CharacterRecord copyCharacterForSave(CharacterRecord src) {
        CharacterRecord dst = new CharacterRecord();

        dst.name = EMPTY.equals(src.name) ? null : src.name;
        dst.gender = src.gender;
        dst.str = src.str;
        dst.intell = src.intell;
        dst.piety = src.piety;
        dst.vitality = src.vitality;
        dst.agility = src.agility;
        dst.luck = src.luck;
        dst.race = src.race;
        dst.classType = src.classType;
        dst.hp = src.hp;
        dst.maxhp = src.maxhp;
        dst.exp = src.exp;
        dst.gold = src.gold;
        dst.level = src.level;

        dst.armor = src.armor;
        dst.weapon = src.weapon;
        dst.helm = src.helm;
        dst.shield = src.shield;
        dst.glove = src.glove;
        dst.item1 = src.item1;
        dst.item2 = src.item2;

        dst.knownSpells.clear();
        if (src.knownSpells != null) {
            dst.knownSpells.addAll(src.knownSpells);
        }

        if (src.magePoints != null) {
            System.arraycopy(src.magePoints, 0, dst.magePoints, 0, Math.min(src.magePoints.length, dst.magePoints.length));
        }
        if (src.clericPoints != null) {
            System.arraycopy(src.clericPoints, 0, dst.clericPoints, 0, Math.min(src.clericPoints.length, dst.clericPoints.length));
        }

        dst.inventory.clear();
        if (src.inventory != null) {
            dst.inventory.addAll(src.inventory);
        }

        dst.summoningCircles.clear();
        if (src.summoningCircles != null) {
            dst.summoningCircles.addAll(src.summoningCircles);
        }

        dst.summonedMonsters.clear();
        if (src.summonedMonsters != null) {
            dst.summonedMonsters.addAll(src.summonedMonsters);
        }

        dst.submorsels = src.submorsels;
        dst.acmodifier1 = src.acmodifier1;
        dst.acmodifier2 = src.acmodifier2;

        return dst;
    }

    private RosterIndex[] buildRosterForSave() {
        RosterIndex[] out = new RosterIndex[registry.getItems().size];
        for (int i = 0; i < registry.getItems().size; i++) {
            RosterIndex src = registry.getItems().get(i);
            RosterIndex dst = new RosterIndex(copyCharacterForSave(src.character), src.index);
            out[i] = dst;
        }
        return out;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(INK.r, INK.g, INK.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        refreshUI();
        stage.getViewport().apply();
        stage.act(Math.min(delta, .1f));
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
        stage.getViewport().update(width, height, true);
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
        stage.dispose();
        uiSkin.dispose();
        uiPixel.dispose();
    }

}
