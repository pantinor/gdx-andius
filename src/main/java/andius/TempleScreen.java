/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius;

import static andius.Andius.backGround;
import static andius.Andius.mainGame;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import java.util.Random;
import utils.LabelStyles;
import utils.Utils;
import utils.XORShiftRandom;

/**
 *
 * @author Paul
 */
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
    private final TextButton exit;
    private final TextureRegionDrawable selectedDrawable = new TextureRegionDrawable(new TextureRegion(Utils.fillRectangle(200, 30, Color.YELLOW, .45f)));
    private final TextureRegion icon;
    private PlayerIndex selectedPatient;
    private CharacterRecord tither;
    
    private static final int LOG_AREA_WIDTH = 400;
    private static final int LOG_X = 300;
    private static final int PAT_SCR_WIDTH = 550;

    public TempleScreen(Context context, final Map contextMap) {
        this.context = context;
        this.contextMap = contextMap;

        this.hud = new Texture(Gdx.files.classpath("assets/data/treasure.png"));
        icon = new TextureRegion(new Texture(Gdx.files.classpath("assets/data/rewards.png")), 135, 132, 128, 128);

        this.batch = new SpriteBatch();
        this.stage = new Stage();

        this.playerSelectionLabel = new Label("WHO WILL TITHE ?", Andius.skin, "larger");

        this.titherSelection = new List<>(Andius.skin, "larger");
        String[] names = new String[this.context.players().length];
        for (int i = 0; i < this.context.players().length; i++) {
            names[i] = this.context.players()[i].name.toUpperCase();
        }
        this.titherSelection.setItems(names);
        this.titherSelection.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                for (CharacterRecord p : context.players()) {
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
                if (selectedPatient.c.status == Status.OK && selectedPatient.c.hp == selectedPatient.c.maxhp) {
                    log(selectedPatient.c.name + " IS WELL.");
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }
                int amt = 50;
                if (selectedPatient.c.status == Status.ASHES) {
                    amt = 500;
                } else if (selectedPatient.c.status == Status.PARALYZED) {
                    amt = 100;
                } else if (selectedPatient.c.status == Status.STONED) {
                    amt = 200;
                } else if (selectedPatient.c.status == Status.DEAD) {
                    amt = 250;
                }
                if (tither.gold < amt) {
                    log("CHEAP APOSTATES! OUT!");
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }
                log("THE DONATION WILL BE " + amt);
                if (selectedPatient.c.status == Status.DEAD) {
                    selectedPatient.c.hp = 1;
                }
                if (selectedPatient.c.status == Status.ASHES) {
                    selectedPatient.c.hp = selectedPatient.c.maxhp;
                }
                if (selectedPatient.c.status == Status.OK) {
                    selectedPatient.c.adjustHP(25);
                }
                selectedPatient.c.status = Status.OK;
                tither.adjustGold(-amt);
                Sounds.play(Sound.HEALING);
                log(selectedPatient.c.name + " IS WELL.");

                for (Cell cell : patientTable.getCells()) {
                    if (cell.getActor() instanceof PlayerIndex) {
                        PlayerIndex pi = (PlayerIndex) cell.getActor();
                        pi.update();
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
                    for (Cell cell : patientTable.getCells()) {
                        if (cell.getActor() instanceof PlayerIndex) {
                            PlayerIndex pi = (PlayerIndex) cell.getActor();
                            pi.selected = false;
                        }
                    }
                    if (event.getTarget() instanceof PlayerIndex) {
                        PlayerIndex pi = (PlayerIndex) event.getTarget();
                        pi.selected = true;
                        selectedPatient = pi;
                    }
                }

                return false;
            }
        }
        );
        this.patientScroll = new ScrollPane(patientTable, Andius.skin);
        for (CharacterRecord p : context.players()) {
            patientTable.add(new PlayerIndex(p, LabelStyles.get(p).getStyle())).minWidth(PAT_SCR_WIDTH - 25).maxWidth(PAT_SCR_WIDTH - 25);
            patientTable.row();
        }

        this.logTable = new Table(Andius.skin);
        this.logTable.defaults().align(Align.left);
        this.logScroll = new ScrollPane(this.logTable, Andius.skin);

        this.logScroll.setBounds(LOG_X, Andius.SCREEN_HEIGHT - 200, LOG_AREA_WIDTH, 150);
        this.patientScroll.setBounds(325, 35, PAT_SCR_WIDTH, 200);
        this.playerSelectionLabel.setBounds(325, 458, 20, 100);
        this.offer.setBounds(525, 310, 65, 40);
        this.exit.setBounds(600, 310, 65, 40);
        sp1.setBounds(325, 310, 175, 175);

        stage.addActor(sp1);
        stage.addActor(playerSelectionLabel);
        stage.addActor(exit);
        stage.addActor(offer);
        stage.addActor(logScroll);
        stage.addActor(patientScroll);

        log("WELCOME TO THE TEMPLE OF RADIANT CANT");

    }

    private class PlayerIndex extends Label {

        SaveGame.CharacterRecord c;
        boolean selected = false;

        PlayerIndex(SaveGame.CharacterRecord sp, LabelStyle style) {
            super("", style);
            this.c = sp;
            update();
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
            if (selected) {
                selectedDrawable.draw(batch, getX(), getY(), getWidth(), getHeight());
            }
        }

        private void update() {
            String d = String.format("%s LVL %d %s %s %s %d / %d GLD: %d", c.name, c.level, c.race.toString(), c.classType.toString(), c.status, c.hp, c.maxhp, c.gold);
            setText(d);
            setStyle(LabelStyles.get(c).getStyle());
        }
    }

    private void log(String s) {
        logTable.add(new Label(s, Andius.skin, "larger"));
        logTable.row();
        logScroll.setScrollPercentY(100);
        logScroll.layout();
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
        batch.draw(this.icon, 125, Andius.SCREEN_HEIGHT - 425);
        Andius.largeFont.draw(batch, "WHO ARE YOU HELPING ?", 325, 245);
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
