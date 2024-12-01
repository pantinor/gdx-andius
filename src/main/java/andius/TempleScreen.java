package andius;

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
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
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
    private final ScrollPane patientScroll;
    private final Table logTable;
    private final ScrollPane logScroll;
    private final TextButton offer;
    private final TextButton exit, pool;
    private final Image focusIndicator;
    private final TextureRegion icon;
    private PatientListing selectedPatient;
    private CharacterRecord tither;

    private static final int LOG_AREA_WIDTH = 400;
    private static final int X_ALIGN = 280;
    private static final int PAT_SCR_WIDTH = 650;
    private static final int PAT_ITEM_HGT = 25;

    public TempleScreen(Context context, final Map contextMap) {
        this.context = context;
        this.contextMap = contextMap;

        this.hud = new Texture(Gdx.files.classpath("assets/data/treasure.png"));
        icon = new TextureRegion(new Texture(Gdx.files.classpath("assets/data/rewards.png")), 135, 132, 128, 128);

        this.batch = new SpriteBatch();
        this.stage = new Stage();

        this.playerSelectionLabel = new Label("WHO WILL TITHE ?", Andius.skin, "larger");

        focusIndicator = new Image(Utils.fillRectangle(PAT_SCR_WIDTH, PAT_ITEM_HGT, Color.YELLOW, .45f));
        focusIndicator.setWidth(PAT_SCR_WIDTH);
        focusIndicator.setHeight(PAT_ITEM_HGT);

        this.titherSelection = new List<>(Andius.skin, "larger");
        Array<String> names = new Array<>();
        for (int i = 0; i < this.context.players().length; i++) {
            if (!this.context.players()[i].isDisabled()) {
                names.add(this.context.players()[i].name.toUpperCase());
            }
        }
        this.titherSelection.setItems(names);
        this.titherSelection.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                for (CharacterRecord p : TempleScreen.this.context.players()) {
                    if (titherSelection.getSelected().startsWith(p.name.toUpperCase())) {
                        tither = p;
                        break;
                    }
                }
            }
        });

        this.offer = new TextButton("TITHE", Andius.skin, "brown-larger");
        this.offer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedPatient == null) {
                    log("WHO IS IN NEED ?");
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }
                if (!selectedPatient.c.isDisabled() && selectedPatient.c.hp == selectedPatient.c.maxhp) {
                    log(selectedPatient.c.name + " IS WELL.");
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }
                int amt = 50;
                if (selectedPatient.c.status.has(Status.ASHES)) {
                    amt = 500;
                } else if (selectedPatient.c.status.has(Status.PARALYZED)) {
                    amt = 100;
                } else if (selectedPatient.c.status.has(Status.STONED)) {
                    amt = 200;
                } else if (selectedPatient.c.isDead()) {
                    amt = 250;
                }

                if (tither == null) {
                    log("Who will Tithe?");
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }

                if (tither.gold < amt) {
                    log("CHEAP APOSTATES! OUT!");
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }

                log("THE DONATION WILL BE " + amt);

                if (selectedPatient.c.isDead()) {
                    selectedPatient.c.hp = 1;
                }

                if (selectedPatient.c.status.has(Status.ASHES)) {
                    selectedPatient.c.hp = selectedPatient.c.maxhp;
                }

                selectedPatient.c.adjustHP(25);
                
                selectedPatient.c.status.reset();

                tither.adjustGold(-amt);

                Sounds.play(Sound.HEALING);

                log(selectedPatient.c.name + " IS WELL.");

                //update available tithers and update their status in the table listing
                Array<String> tithers = new Array<>();
                for (Cell cell : patientTable.getCells()) {
                    if (cell.getActor() instanceof PatientListing) {
                        PatientListing pi = (PatientListing) cell.getActor();
                        pi.style.fontColor = pi.c.status.color();
                        pi.status.setText(pi.c.status.toString());
                        pi.hp.setText(pi.c.hp + " / " + pi.c.maxhp);
                        pi.gold.setText("" + pi.c.gold);
                        if (!pi.c.isDisabled()) {
                            tithers.add(pi.c.name.toUpperCase());
                        }
                    }
                }
                titherSelection.setItems(tithers);
            }
        });

        this.pool = new TextButton("POOL", Andius.skin, "brown-larger");
        this.pool.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (tither == null) {
                    return;
                }
                for (CharacterRecord cr : TempleScreen.this.context.players()) {
                    if (cr != tither) {
                        int gold = cr.gold;
                        cr.adjustGold(-gold);
                        tither.adjustGold(gold);
                    }
                }
                for (Cell cell : patientTable.getCells()) {
                    if (cell.getActor() instanceof PatientListing) {
                        PatientListing pi = (PatientListing) cell.getActor();
                        pi.gold.setText("" + pi.c.gold);
                    }
                }
            }
        });

        this.exit = new TextButton("EXIT", Andius.skin, "brown-larger");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(contextMap.getScreen());
            }
        });

        ScrollPane sp1 = new ScrollPane(this.titherSelection, Andius.skin);

        patientTable = new Table(Andius.skin);
        patientTable.defaults().align(Align.left);
        patientTable.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event.toString().equals("touchDown")) {
                    if (focusIndicator.getParent() != null) {
                        focusIndicator.getParent().removeActor(focusIndicator);
                    }
                    if (event.getTarget() instanceof PatientListing) {
                        selectedPatient = (PatientListing) event.getTarget();
                        selectedPatient.addActor(focusIndicator);
                    } else if (event.getTarget().getParent() instanceof PatientListing) {
                        selectedPatient = (PatientListing) event.getTarget().getParent();
                        selectedPatient.addActor(focusIndicator);
                    }
                }
                return false;
            }
        }
        );
        this.patientScroll = new ScrollPane(patientTable, Andius.skin);
        for (CharacterRecord p : this.context.players()) {
            patientTable.add(new PatientListing(p));
            patientTable.row();
        }

        this.logTable = new Table(Andius.skin);
        this.logTable.defaults().align(Align.left);
        this.logScroll = new ScrollPane(this.logTable, Andius.skin);

        this.logScroll.setBounds(X_ALIGN, Andius.SCREEN_HEIGHT - 200, LOG_AREA_WIDTH, 150);
        this.patientScroll.setBounds(X_ALIGN, 35, PAT_SCR_WIDTH, 200);
        this.playerSelectionLabel.setBounds(X_ALIGN, 458, 20, 100);
        this.offer.setBounds(525, 310, 65, 40);
        this.pool.setBounds(600, 310, 65, 40);
        this.exit.setBounds(675, 310, 65, 40);
        sp1.setBounds(X_ALIGN, 310, 175, 175);

        stage.addActor(sp1);
        stage.addActor(playerSelectionLabel);
        stage.addActor(exit);
        stage.addActor(offer);
        stage.addActor(pool);
        stage.addActor(logScroll);
        stage.addActor(patientScroll);

        log("WELCOME TO THE " + contextMap.getLabel().toUpperCase());

    }

    private void log(String s) {
        logTable.add(new Label(s, Andius.skin, "larger"));
        logTable.row();
        logScroll.layout();
        logScroll.setScrollPercentY(100);
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
            this.style = new LabelStyle(Andius.largeFont, rec.status.color());
            this.name = new Label(rec.name, this.style);
            this.lvlracetype = new Label("LVL " + rec.level + " " + rec.race.toString() + " " + rec.classType.toString(), this.style);
            this.status = new Label(rec.status.toString(), this.style);
            this.hp = new Label(rec.hp + " / " + rec.maxhp, this.style);
            this.gold = new Label("" + rec.gold, this.style);

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
        Andius.largeFont.draw(batch, "WHO ARE YOU HELPING ?", X_ALIGN, 245);
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
