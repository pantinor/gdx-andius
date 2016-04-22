/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius;

import static andius.Andius.mainGame;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import utils.Utils;

/**
 *
 * @author Paul
 */
public class InnScreen implements Screen, Constants {

    private final Context context;
    private final Constants.Map contextMap;

    private final Texture hud;
    private final SpriteBatch batch;
    private final Stage stage;

    private final Label playerSelectionLabel;
    private final List<String> roomSelection;
    private final Table playerTable;
    private final ScrollPane playerScroll;
    private final Table logTable;
    private final ScrollPane logScroll;
    private final TextButton go;
    private final TextButton pool;
    private final TextButton exit;
    private final Image focusIndicator;
    private PlayerListing selectedPlayer;

    private static final int LOG_AREA_WIDTH = 400;
    private static final int X_ALIGN = 200;
    private static final int PAT_SCR_WIDTH = 650;
    private static final int PAT_ITEM_HGT = 27;

    public InnScreen(Context context, final Constants.Map contextMap) {
        this.context = context;
        this.contextMap = contextMap;

        this.hud = new Texture(Gdx.files.classpath("assets/data/inn.png"));

        this.batch = new SpriteBatch();
        this.stage = new Stage();

        this.playerSelectionLabel = new Label("WE HAVE :", Andius.skin, "larger");

        focusIndicator = new Image(Utils.fillRectangle(PAT_SCR_WIDTH, PAT_ITEM_HGT, Color.YELLOW, .45f));
        focusIndicator.setWidth(PAT_SCR_WIDTH);
        focusIndicator.setHeight(PAT_ITEM_HGT);

        this.roomSelection = new List<>(Andius.skin, "larger");
        Array<String> names = new Array<>();
        names.add("THE STABLES (FREE!)");
        names.add("COTS. 10 GP/WEEK.");
        names.add("ECONOMY ROOMS. 50 GP/WEEK.");
        names.add("MERCHANT SUITES. 200 GP/WEEK.");
        names.add("ROYAL SUITES. 500 GP/WEEK.");

        this.roomSelection.setItems(names);

        this.go = new TextButton("BUY", Andius.skin, "brown-larger");
        this.go.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedPlayer == null || selectedPlayer.c.isDisabled()) {
                    return;
                }
                if (roomSelection.getSelected().startsWith("THE STAB")) {
                    takeNap(0, 0, selectedPlayer);
                } else if (roomSelection.getSelected().startsWith("COTS")) {
                    takeNap(1, 10, selectedPlayer);
                } else if (roomSelection.getSelected().startsWith("ECON")) {
                    takeNap(3, 50, selectedPlayer);
                } else if (roomSelection.getSelected().startsWith("MERCH")) {
                    takeNap(7, 200, selectedPlayer);
                } else if (roomSelection.getSelected().startsWith("ROYAL")) {
                    takeNap(10, 500, selectedPlayer);
                }
            }
        });

        this.pool = new TextButton("POOL", Andius.skin, "brown-larger");
        this.pool.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedPlayer == null) {
                    return;
                }
                for (Cell cell : playerTable.getCells()) {
                    if (cell.getActor() instanceof PlayerListing) {
                        PlayerListing pi = (PlayerListing) cell.getActor();
                        if (pi != selectedPlayer) {
                            int gold = pi.c.gold;
                            pi.c.adjustGold(-gold);
                            pi.gold.setText("0");
                            selectedPlayer.c.adjustGold(gold);
                            pi.gold.setText("" + pi.c.gold);
                        }
                    }
                }
                selectedPlayer.gold.setText("" + selectedPlayer.c.gold);
            }
        });

        this.exit = new TextButton("EXIT", Andius.skin, "brown-larger");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(contextMap.getScreen());
            }
        });

        ScrollPane sp1 = new ScrollPane(this.roomSelection, Andius.skin);

        playerTable = new Table(Andius.skin);
        playerTable.defaults().align(Align.left);
        playerTable.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event.toString().equals("touchDown")) {
                    if (focusIndicator.getParent() != null) {
                        focusIndicator.getParent().removeActor(focusIndicator);
                    }
                    if (event.getTarget() instanceof PlayerListing) {
                        selectedPlayer = (PlayerListing) event.getTarget();
                        selectedPlayer.addActor(focusIndicator);
                    } else if (event.getTarget().getParent() instanceof PlayerListing) {
                        selectedPlayer = (PlayerListing) event.getTarget().getParent();
                        selectedPlayer.addActor(focusIndicator);
                    }
                }
                return false;
            }
        }
        );
        this.playerScroll = new ScrollPane(playerTable, Andius.skin);
        for (CharacterRecord p : context.players()) {
            playerTable.add(new PlayerListing(p));
            playerTable.row();
        }

        this.logTable = new Table(Andius.skin);
        this.logTable.defaults().align(Align.left);
        this.logScroll = new ScrollPane(this.logTable, Andius.skin);

        this.playerScroll.setBounds(X_ALIGN, 35, PAT_SCR_WIDTH, 200);
        this.playerSelectionLabel.setBounds(X_ALIGN, 458, 20, 100);
        this.go.setBounds(X_ALIGN, 290, 60, 40);
        this.pool.setBounds(X_ALIGN + 70, 290, 60, 40);
        this.exit.setBounds(X_ALIGN + 140, 290, 60, 40);
        sp1.setBounds(X_ALIGN, 340, 300, 145);
        this.logScroll.setBounds(X_ALIGN + 300, 250, LOG_AREA_WIDTH, 395);

        stage.addActor(sp1);
        stage.addActor(playerSelectionLabel);
        stage.addActor(exit);
        stage.addActor(go);
        stage.addActor(pool);
        stage.addActor(logScroll);
        stage.addActor(playerScroll);

        log("WELCOME TO THE " + contextMap.getLabel().toUpperCase());

    }

    private void takeNap(int hpAdd, int goldAmt, PlayerListing pi) {

        int expnextlvl = pi.c.checkAndSetLevel();
        while (expnextlvl >= 0) {
            pi.c.maxhp += pi.c.getMoreHP();

            log(pi.c.name.toUpperCase() + " IS LEVEL " + pi.c.level);
            pi.lvlracetype.setText("LVL " + pi.c.level + " " + pi.c.race.toString() + " " + pi.c.classType.toString());

            pi.c.str = gainOrLose("STRENGTH", pi.c.str, pi.c);
            pi.c.intell = gainOrLose("INTELLIGENCE", pi.c.intell, pi.c);
            pi.c.piety = gainOrLose("PIETY", pi.c.piety, pi.c);
            pi.c.vitality = gainOrLose("VITALITY", pi.c.vitality, pi.c);
            pi.c.agility = gainOrLose("AGILITY", pi.c.agility, pi.c);
            pi.c.luck = gainOrLose("LUCK", pi.c.luck, pi.c);

            SaveGame.setSpellPoints(pi.c);
            if (SaveGame.tryLearn(pi.c)) {
                log(pi.c.name.toUpperCase() + " LEARNED NEW SPELLS!");
            }
            
            expnextlvl = pi.c.checkAndSetLevel();
        }

        log(pi.c.name.toUpperCase() + " NEEDS " + Math.abs(expnextlvl) + " EXPERIENCE TO THE NEXT LEVEL.");

        SaveGame.setSpellPoints(pi.c);

        if (goldAmt > 0) {
            int healed = 0;
            while (pi.c.gold >= goldAmt && pi.c.hp < pi.c.maxhp) {
                pi.c.hp += hpAdd;
                if (pi.c.hp > pi.c.maxhp) {
                    pi.c.hp = pi.c.maxhp;
                }
                pi.c.adjustGold(-goldAmt);
                pi.gold.setText("" + pi.c.gold);
                pi.hp.setText(pi.c.hp + " / " + pi.c.maxhp);
                healed++;
            }
            if (healed > 0) {
                log(pi.c.name.toUpperCase() + " HAS HEALED (" + healed + " WEEKS).");
                Sounds.play(Sound.HEALING);
            }
        } else {
            log(pi.c.name.toUpperCase() + " IS NAPPING.");
        }

        log("");
    }

    private int gainOrLose(String desc, int attr, CharacterRecord c) {
        int newattrib = SaveGame.gainOrLose(attr);
        if (newattrib > attr) {
            log(c.name.toUpperCase() + " GAINED " + desc + ".");
        } else if (newattrib < attr) {
            log(c.name.toUpperCase() + " LOST " + desc + ".");
        } else {
            //nothing
        }
        return newattrib;
    }

    private void log(String s) {
        logTable.add(new Label(s, Andius.skin, "larger"));
        logTable.row();
        logScroll.setScrollPercentY(100);
        logScroll.layout();
    }

    private class PlayerListing extends Group {

        final Label name;
        final Label lvlracetype;
        final Label status;
        final Label hp;
        final Label gold;
        final LabelStyle style;
        final CharacterRecord c;

        PlayerListing(CharacterRecord rec) {
            this.c = rec;
            this.style = new LabelStyle(Andius.largeFont, rec.status.getColor());
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
        Andius.largeFont.draw(batch, "WHO WILL STAY ?", X_ALIGN, 245);
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
