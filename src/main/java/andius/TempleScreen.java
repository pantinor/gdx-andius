package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
import static andius.Andius.mainGame;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import utils.AutoFocusScrollPane;
import utils.LogScrollPane;
import utils.Utils;
import utils.ManagementUi;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import static utils.SkinUtils.GOLD;
import static utils.SkinUtils.INK;
import static utils.SkinUtils.MUTED;

public class TempleScreen implements Screen, Constants {

    private final Context context;
    private final Constants.Map contextMap;

    private final ManagementUi ui = new ManagementUi();
    private Label patientName, careDescription, donationLabel, donorCaption, careHint;
    private final Stage stage;

    private final List<CharacterRecord> titherSelection;
    private final Table patientTable;
    private final AutoFocusScrollPane patientScroll;
    private final Table logTable;
    private final LogScrollPane logs;
    private final TextButton offer;
    private final TextButton exit, pool;
    private final Image focusIndicator;
    private final TextureRegion icon;
    private PatientListing selectedPatient;
    private CharacterRecord tither;

    private static final int LOG_AREA_WIDTH = 280;
    private static final int PAT_SCR_WIDTH = 304;
    private static final int PAT_ITEM_HGT = 84;

    public TempleScreen(Context context, final Map contextMap) {
        this.context = context;
        this.contextMap = contextMap;

        this.icon = new TextureRegion(new Texture(Gdx.files.classpath("assets/data/rewards.png")), 135, 132, 128, 128);

        this.stage = new Stage(new FitViewport(Andius.SCREEN_WIDTH, Andius.SCREEN_HEIGHT));

        this.focusIndicator = ui.selection(PAT_SCR_WIDTH, PAT_ITEM_HGT);
        this.focusIndicator.setWidth(PAT_SCR_WIDTH);
        this.focusIndicator.setHeight(PAT_ITEM_HGT);

        this.titherSelection = new List<>(ui.skin);
        rebuildTitherList();

        this.titherSelection.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateTitherFromSelection();
            }
        });

        this.offer = new TextButton("TITHE", ui.skin);
        this.offer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedPatient == null) {
                    log("WHO IS IN NEED ?");
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }

                if (selectedPatient.c.isAllWell() && selectedPatient.c.hp == selectedPatient.c.maxhp) {
                    log(selectedPatient.c.name.toUpperCase() + " IS FINE.");
                    return;
                }

                if (tither == null) {
                    log("WHO WILL TITHE?");
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }

                int amt = donationAmount(selectedPatient.c);
                log("THE DONATION WILL BE " + amt);

                if (tither.gold < amt) {
                    log("CHEAP APOSTATES! OUT!");
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }

                CharacterRecord patient = selectedPatient.c;

                if (patient.status.has(Status.ASHES)) {
                    if (Utils.RANDOM.nextInt(100) > 40 + patient.vitality * 3) {
                        tither.adjustGold(-amt);
                        refreshAllListings();
                        log("FAILED TO RETURN THE PATIENT FROM ASHES!");
                        Sounds.play(Sound.NEGATIVE_EFFECT);
                        return;
                    }

                    tither.adjustGold(-amt);
                    patient.hp = patient.maxhp;
                    patient.status.reset();
                    Sounds.play(Sound.HEALING);
                    log(patient.name.toUpperCase() + " HAS BEEN RESTORED FROM ASHES.");
                    refreshAllListings();
                    return;
                }

                if (patient.isDead()) {
                    if (Utils.RANDOM.nextInt(100) > 50 + patient.vitality * 3) {
                        tither.adjustGold(-amt);
                        refreshAllListings();
                        log("FAILED TO RESURRECT THE PATIENT!");
                        Sounds.play(Sound.NEGATIVE_EFFECT);
                        return;
                    }

                    tither.adjustGold(-amt);
                    patient.hp = 1;
                    patient.status.reset();
                    Sounds.play(Sound.HEALING);
                    log(patient.name.toUpperCase() + " HAS BEEN RESURRECTED.");
                    refreshAllListings();
                    return;
                }

                tither.adjustGold(-amt);

                if (patient.status.has(Status.STONED)
                        || patient.status.has(Status.PARALYZED)
                        || patient.status.has(Status.POISONED)) {
                    patient.status.reset();
                    patient.adjustHP(25);
                    Sounds.play(Sound.HEALING);
                    log(patient.name.toUpperCase() + " IS WELL.");
                    refreshAllListings();
                    return;
                }

                if (patient.hp < patient.maxhp) {
                    patient.adjustHP(25);
                    patient.status.reset();
                    Sounds.play(Sound.HEALING);
                    log(patient.name.toUpperCase() + " IS WELL.");
                    refreshAllListings();
                    return;
                }

                refreshAllListings();
            }
        });

        this.pool = new TextButton("POOL", ui.skin);
        this.pool.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (tither == null) {
                    log("WHO WILL TITHE?");
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }

                for (CharacterRecord cr : TempleScreen.this.context.players()) {
                    if (cr != tither) {
                        int gold = cr.gold;
                        cr.adjustGold(-gold);
                        tither.adjustGold(gold);
                    }
                }

                refreshAllListings();
                log("ALL GOLD HAS BEEN POOLED TO " + tither.name.toUpperCase() + ".");
            }
        });

        this.exit = new TextButton("EXIT", ui.skin);
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(contextMap.getScreen());
            }
        });

        this.logTable = new Table(ui.skin);
        this.logTable.bottom().left();
        this.logs = new LogScrollPane(ui.skin, logTable, LOG_AREA_WIDTH, "default");

        this.patientTable = new Table(ui.skin);
        this.patientTable.top().left();
        this.patientTable.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof InputEvent ie && ie.getType() == InputEvent.Type.touchDown) {
                    focusIndicator.remove();
                    Actor target = ie.getTarget();
                    if (target instanceof PatientListing pl) {
                        selectedPatient = pl;
                        selectedPatient.addActor(focusIndicator);
                        focusIndicator.toBack();
                    } else if (target != null && target.getParent() instanceof PatientListing pl) {
                        selectedPatient = pl;
                        selectedPatient.addActor(focusIndicator);
                        focusIndicator.toBack();
                    }
                }
                return false;
            }
        });

        this.patientScroll = new AutoFocusScrollPane(patientTable, ui.skin);
        this.patientScroll.setScrollingDisabled(true, false);

        for (CharacterRecord p : this.context.players()) {
            PatientListing listing = new PatientListing(p);
            patientTable.add(listing);
            patientTable.row();

            if (selectedPatient == null) {
                selectedPatient = listing;
                selectedPatient.addActor(focusIndicator);
                focusIndicator.toBack();
            }
        }

        buildLayout();

        log("Welcome to the Temple of Light. Choose a patient and a donor.");
    }

    private void buildLayout() {
        ui.label(stage, "Temple of Light", "default-24", GOLD, 24, 714, 700, 34);
        ui.label(stage, contextMap.getLabel() + "  /  Healing & restoration", "default", MUTED, 24, 696, 700, 20);
        ui.panel(stage, "01  /  Sanctuary", "A place of healing and hope", 24, 304);
        Image hands = new Image(icon);
        hands.setScaling(Scaling.none);
        hands.setBounds(112, 456, 128, 128);
        hands.setTouchable(Touchable.disabled);
        stage.addActor(hands);
        ui.label(stage, "The hands of light", "default-16", GOLD, 36, 404, 280, 30);
        patientName = ui.label(stage, "", "emphasis", Color.WHITE, 36, 372, 280, 24);
        careDescription = ui.description(stage, 36, 242, 280, 114);
        donationLabel = ui.label(stage, "", "default-16", GOLD, 36, 198, 280, 28);
        careHint = ui.description(stage, 36, 112, 280, 74);
        ui.panel(stage, "02  /  Your offering", "Choose who will make the donation", 344, 360, 304, 336, "15232f");
        titherSelection.setBounds(356, 424, 280, 196);
        stage.addActor(titherSelection);
        donorCaption = ui.label(stage, "", "default", GOLD, 356, 378, 280, 24);
        ui.panel(stage, "Temple journal", "Blessings and the outcome of your care", 344, 96, 304, 248, "15232f");
        logs.setBounds(356, 108, 280, 170);
        logs.setFadeScrollBars(false);
        stage.addActor(logs);
        ui.panel(stage, "03  /  The company", "Select the adventurer in need", 672, 328);
        patientScroll.setBounds(684, 108, 304, 516);
        patientScroll.setFadeScrollBars(false);
        stage.addActor(patientScroll);
        offer.setText("Make offering");
        offer.setBounds(24, 24, 172, 38);
        pool.setText("Pool gold");
        pool.setBounds(204, 24, 124, 38);
        exit.setText("Return to journey");
        exit.setBounds(850, 24, 150, 38);
        stage.addActor(offer);
        stage.addActor(pool);
        stage.addActor(exit);
        ui.primary(offer);
    }

    private void refreshUI() {
        for (Cell<?> cell : patientTable.getCells()) {
            if (cell.getActor() instanceof PatientListing listing) {
                listing.refresh();
            }
        }
        pool.setDisabled(tither == null || context.players().length < 2);
        if (selectedPatient == null) {
            offer.setDisabled(true);
            careHint.setText("Select an adventurer in need of care.");
            return;
        }
        CharacterRecord patient = selectedPatient.c;
        int cost = donationAmount(patient);
        boolean well = patient.isAllWell() && patient.hp == patient.maxhp;
        offer.setDisabled(well || tither == null || tither.gold < cost);
        patientName.setText(patient.name);
        careDescription.setText("Level " + patient.level + " " + patient.race + " " + patient.classType
                + "\nHealth  " + patient.hp + " / " + patient.maxhp + "\nCondition  " + (patient.isDead() ? "DEAD" : patient.status)
                + (patient.isDead() || patient.status.has(Status.ASHES) ? "\nRestoration is not guaranteed." : "\nAn offering restores up to 25 HP and treats afflictions."));
        donationLabel.setText(well ? "No offering needed" : "Donation  /  " + cost + " gold");
        donorCaption.setText(tither == null ? "No eligible donor" : tither.name + "  /  " + tither.gold + " gold");
        careHint.setText(well ? "This adventurer is in good health." : tither == null ? "An able companion must make the offering."
                : tither.gold < cost ? "The donor needs more gold. Pool the company's funds or choose another donor."
                        : "Your offering will be taken from " + tither.name + ".");
    }

    private int donationAmount(CharacterRecord patient) {
        if (patient.status.has(Status.ASHES)) {
            return 500 * patient.level;
        }
        if (patient.status.has(Status.STONED)) {
            return 200 * patient.level;
        }
        if (patient.status.has(Status.POISONED) || patient.status.has(Status.PARALYZED)) {
            return 100 * patient.level;
        }
        if (patient.isDead()) {
            return 250 * patient.level;
        }
        return 50;
    }

    private void rebuildTitherList() {
        Array<CharacterRecord> donors = new Array<>();
        for (CharacterRecord player : context.players()) {
            if (!player.isDisabled()) {
                donors.add(player);
            }
        }
        CharacterRecord previous = tither;
        titherSelection.setItems(donors);
        tither = previous != null && donors.contains(previous, true) ? previous : donors.size > 0 ? donors.first() : null;
        if (tither != null) {
            titherSelection.setSelected(tither);
        }
    }

    private void updateTitherFromSelection() {
        tither = titherSelection.getSelected();
    }

    private void refreshAllListings() {
        for (Cell<?> cell : patientTable.getCells()) {
            if (cell.getActor() instanceof PatientListing pi) {
                pi.refresh();
            }
        }
        rebuildTitherList();
    }

    private void log(String s) {
        this.logs.add(s);
    }

    private class PatientListing extends Group {

        final Label name;
        final Label lvlracetype;
        final Label status;
        final Label hp;
        final Label gold;
        final LabelStyle style;
        final CharacterRecord c;

        PatientListing(CharacterRecord rec) {
            this.c = rec;
            this.style = new LabelStyle(ui.skin.getFont("body"), rec.isDead() ? Color.RED : rec.status.color());
            this.name = new Label("", this.style);
            this.lvlracetype = new Label("", this.style);
            this.status = new Label("", this.style);
            this.hp = new Label("", this.style);
            this.gold = new Label("", this.style);

            addActor(this.name);
            addActor(this.lvlracetype);
            addActor(this.status);
            addActor(this.hp);
            addActor(this.gold);

            Image portrait = new Image(andius.objects.Portraits.get(rec));
            portrait.setScaling(Scaling.fit);
            portrait.setBounds(0, 4, 76, 76);
            addActor(portrait);
            this.name.setBounds(88, 60, 216, 20);
            this.lvlracetype.setBounds(88, 42, 216, 18);
            this.hp.setBounds(88, 24, 134, 18);
            this.status.setBounds(226, 24, 78, 18);
            this.gold.setBounds(88, 6, 216, 18);

            for (Actor actor : getChildren()) {
                if (actor instanceof Label label) {
                    label.setEllipsis(true);
                }
            }
            this.name.setStyle(new LabelStyle(ui.skin.getFont("emphasis"), Color.WHITE));
            this.setSize(PAT_SCR_WIDTH, PAT_ITEM_HGT);
            refresh();
        }

        void refresh() {
            Color color = c.isDead() ? Color.SALMON : c.status.color();
            name.setText(c.name);
            lvlracetype.setText("Lv " + c.level + " " + c.race + " " + c.classType);
            hp.setText("HP " + c.hp + "/" + c.maxhp);
            status.setText(c.isDead() ? "DEAD" : c.status.toString());
            gold.setText("Gold " + c.gold);

            name.setColor(Color.WHITE);
            lvlracetype.setColor(MUTED);
            hp.setColor(color);
            status.setColor(color);
            gold.setColor(MUTED);

        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float f) {
        Gdx.gl.glClearColor(INK.r, INK.g, INK.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        refreshUI();
        stage.getViewport().apply();
        stage.act(Math.min(f, .1f));
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
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        icon.getTexture().dispose();
        ui.dispose();
    }
}
