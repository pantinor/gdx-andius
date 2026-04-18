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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import utils.AutoFocusScrollPane;
import utils.LogScrollPane;
import utils.Utils;

public class TempleScreen implements Screen, Constants {

    private final Context context;
    private final Constants.Map contextMap;

    private final Texture hud;
    private final SpriteBatch batch;
    private final Stage stage;

    private final Label playerSelectionLabel;
    private final List<String> titherSelection;
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

    private static final int LOG_AREA_WIDTH = 600;
    private static final int X_ALIGN = 280;
    private static final int PAT_SCR_WIDTH = 650;
    private static final int PAT_ITEM_HGT = 25;

    public TempleScreen(Context context, final Map contextMap) {
        this.context = context;
        this.contextMap = contextMap;

        this.hud = new Texture(Gdx.files.classpath("assets/data/treasure.png"));
        this.icon = new TextureRegion(new Texture(Gdx.files.classpath("assets/data/rewards.png")), 135, 132, 128, 128);

        this.batch = new SpriteBatch();
        this.stage = new Stage();

        this.playerSelectionLabel = new Label("WHO WILL TITHE ?", Andius.skin, "default-16");

        this.focusIndicator = new Image(Utils.fillRectangle(PAT_SCR_WIDTH, PAT_ITEM_HGT, Color.YELLOW, .45f));
        this.focusIndicator.setWidth(PAT_SCR_WIDTH);
        this.focusIndicator.setHeight(PAT_ITEM_HGT);

        this.titherSelection = new List<>(Andius.skin, "default-16");
        rebuildTitherList();

        this.titherSelection.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                updateTitherFromSelection();
            }
        });

        this.offer = new TextButton("TITHE", Andius.skin, "default-16-red");
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

        this.pool = new TextButton("POOL", Andius.skin, "default-16-red");
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

        this.exit = new TextButton("EXIT", Andius.skin, "default-16-red");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(contextMap.getScreen());
            }
        });

        AutoFocusScrollPane sp1 = new AutoFocusScrollPane(this.titherSelection, Andius.skin);
        sp1.setScrollingDisabled(true, false);

        this.logTable = new Table(Andius.skin);
        this.logTable.bottom().left();
        this.logs = new LogScrollPane(Andius.skin, logTable, LOG_AREA_WIDTH);
        this.logs.setBounds(X_ALIGN, Andius.SCREEN_HEIGHT - 200, LOG_AREA_WIDTH, 150);

        this.patientTable = new Table(Andius.skin);
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
                    } else if (target != null && target.getParent() instanceof PatientListing pl) {
                        selectedPatient = pl;
                        selectedPatient.addActor(focusIndicator);
                    }
                }
                return false;
            }
        });

        this.patientScroll = new AutoFocusScrollPane(patientTable, Andius.skin);
        this.patientScroll.setScrollingDisabled(true, false);

        for (CharacterRecord p : this.context.players()) {
            PatientListing listing = new PatientListing(p);
            patientTable.add(listing);
            patientTable.row();

            if (selectedPatient == null) {
                selectedPatient = listing;
                selectedPatient.addActor(focusIndicator);
            }
        }

        this.patientScroll.setBounds(X_ALIGN, 55, PAT_SCR_WIDTH, 150);
        this.playerSelectionLabel.setBounds(X_ALIGN, 480, 400, 50);
        this.offer.setBounds(525, 310, 65, 40);
        this.pool.setBounds(600, 310, 65, 40);
        this.exit.setBounds(675, 310, 65, 40);
        sp1.setBounds(X_ALIGN, 310, 175, 175);

        stage.addActor(sp1);
        stage.addActor(playerSelectionLabel);
        stage.addActor(exit);
        stage.addActor(offer);
        stage.addActor(pool);
        stage.addActor(logs);
        stage.addActor(patientScroll);

        log("WELCOME TO THE " + contextMap.getLabel().toUpperCase());
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
        Array<String> names = new Array<>();
        CharacterRecord firstValid = null;

        for (CharacterRecord p : this.context.players()) {
            if (!p.isDisabled()) {
                names.add(p.name.toUpperCase());
                if (firstValid == null) {
                    firstValid = p;
                }
            }
        }

        this.titherSelection.setItems(names);

        if (tither == null || tither.isDisabled()) {
            tither = firstValid;
        }

        if (tither != null) {
            this.titherSelection.setSelected(tither.name.toUpperCase());
        }
    }

    private void updateTitherFromSelection() {
        String selected = titherSelection.getSelected();
        if (selected == null) {
            tither = null;
            return;
        }

        for (CharacterRecord p : this.context.players()) {
            if (!p.isDisabled() && selected.equals(p.name.toUpperCase())) {
                tither = p;
                return;
            }
        }
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
            this.style = new LabelStyle(Andius.font16, rec.isDead() ? Color.RED : rec.status.color());
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

            float x = getX();
            this.name.setBounds(x, getY(), 150, PAT_ITEM_HGT);
            this.lvlracetype.setBounds(x += 150, getY(), 200, PAT_ITEM_HGT);
            this.status.setBounds(x += 200, getY(), 120, PAT_ITEM_HGT);
            this.hp.setBounds(x += 120, getY(), 110, PAT_ITEM_HGT);
            this.gold.setBounds(x += 110, getY(), 100, PAT_ITEM_HGT);

            this.setBounds(getX(), getY(), PAT_SCR_WIDTH, PAT_ITEM_HGT);
            refresh();
        }

        void refresh() {
            Color color = c.isDead() ? Color.RED : c.status.color();
            this.style.fontColor = color;

            this.name.setText(c.name.toUpperCase());
            this.lvlracetype.setText("LVL " + c.level + " " + c.race + " " + c.classType);
            this.status.setText(c.isDead() ? "DEAD" : c.status.toString());
            this.hp.setText(c.hp + " / " + c.maxhp);
            this.gold.setText(String.valueOf(c.gold));

            this.name.setColor(color);
            this.lvlracetype.setColor(color);
            this.status.setColor(color);
            this.hp.setColor(color);
            this.gold.setColor(color);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float f) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(this.hud, 0, 0);
        batch.draw(this.icon, 80, Andius.SCREEN_HEIGHT - 425);
        Andius.font16.draw(batch, "WHO ARE YOU HELPING ?", X_ALIGN, 245);
        batch.end();
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int i, int i1) {
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
