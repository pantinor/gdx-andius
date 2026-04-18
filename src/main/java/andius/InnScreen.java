package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import utils.AutoFocusScrollPane;
import utils.LogScrollPane;
import utils.Utils;

public class InnScreen implements Screen, Constants {

    private final Context context;
    private final Constants.Map contextMap;

    private final Texture hud;
    private final SpriteBatch batch;
    private final Stage stage;

    private final Label playerSelectionLabel;
    private final List<String> roomSelection;
    private final Table playerTable;
    private final AutoFocusScrollPane playerScroll;
    private final Table logTable;
    private final LogScrollPane logScroll;
    private final TextButton go;
    private final TextButton pool;
    private final TextButton exit;
    private final Image focusIndicator;
    private PlayerListing selectedPlayer;

    private static final int LOG_AREA_WIDTH = 500;
    private static final int X_ALIGN = 200;
    private static final int PAT_SCR_WIDTH = 650;
    private static final int PAT_ITEM_HGT = 27;

    public InnScreen(Context context, final Constants.Map contextMap) {
        this.context = context;
        this.contextMap = contextMap;

        for (CharacterRecord player : Andius.CTX.players()) {
            player.acmodifier2 = 0;
        }

        this.hud = new Texture(Gdx.files.classpath("assets/data/inn.png"));

        this.batch = new SpriteBatch();
        this.stage = new Stage();

        this.playerSelectionLabel = new Label("WE HAVE :", Andius.skin, "default-16");

        this.focusIndicator = new Image(Utils.fillRectangle(PAT_SCR_WIDTH, PAT_ITEM_HGT, Color.YELLOW, .45f));
        this.focusIndicator.setWidth(PAT_SCR_WIDTH);
        this.focusIndicator.setHeight(PAT_ITEM_HGT);

        this.roomSelection = new List<>(Andius.skin, "default-16");
        Array<String> names = new Array<>();
        names.add("THE STABLES (FREE!)");
        names.add("COTS. 10 GP/WEEK.");
        names.add("ECONOMY ROOMS. 50 GP/WEEK.");
        names.add("MERCHANT SUITES. 200 GP/WEEK.");
        names.add("ROYAL SUITES. 500 GP/WEEK.");
        this.roomSelection.setItems(names);

        this.go = new TextButton("BUY", Andius.skin, "default-16-red");
        this.go.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedPlayer == null) {
                    log("NO PLAYER SELECTED.");
                    return;
                }

                if (selectedPlayer.c.isDead()) {
                    log(selectedPlayer.c.name.toUpperCase() + " IS DEAD.");
                    return;
                }

                String room = roomSelection.getSelected();
                if (room == null) {
                    log("NO ROOM SELECTED.");
                    return;
                }

                if (room.startsWith("THE STAB")) {
                    takeNap(0, 0, selectedPlayer);
                } else if (room.startsWith("COTS")) {
                    takeNap(1, 10, selectedPlayer);
                } else if (room.startsWith("ECON")) {
                    takeNap(3, 50, selectedPlayer);
                } else if (room.startsWith("MERCH")) {
                    takeNap(7, 200, selectedPlayer);
                } else if (room.startsWith("ROYAL")) {
                    takeNap(10, 500, selectedPlayer);
                }
            }
        });

        this.pool = new TextButton("POOL", Andius.skin, "default-16-red");
        this.pool.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedPlayer == null) {
                    log("NO PLAYER SELECTED.");
                    return;
                }

                for (Cell<?> cell : playerTable.getCells()) {
                    if (cell.getActor() instanceof PlayerListing pi) {
                        if (pi != selectedPlayer) {
                            int gold = pi.c.gold;
                            if (gold > 0) {
                                pi.c.adjustGold(-gold);
                                selectedPlayer.c.adjustGold(gold);
                                pi.gold.setText(String.valueOf(pi.c.gold));
                            }
                        }
                    }
                }

                selectedPlayer.gold.setText(String.valueOf(selectedPlayer.c.gold));
                log("ALL GOLD POOLED TO " + selectedPlayer.c.name.toUpperCase() + ".");
            }
        });

        this.exit = new TextButton("EXIT", Andius.skin, "default-16-red");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(contextMap.getScreen());
            }
        });

        AutoFocusScrollPane sp1 = new AutoFocusScrollPane(this.roomSelection, Andius.skin);
        sp1.setBounds(X_ALIGN, 340, 300, 145);

        this.playerTable = new Table(Andius.skin);
        this.playerTable.top().left();
        this.playerTable.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if ("touchDown".equals(String.valueOf(event))) {
                    if (focusIndicator.getParent() != null) {
                        focusIndicator.getParent().removeActor(focusIndicator);
                    }

                    Actor target = event.getTarget();
                    if (target instanceof PlayerListing listing) {
                        setSelectedPlayer(listing);
                    } else if (target != null && target.getParent() instanceof PlayerListing listing) {
                        setSelectedPlayer(listing);
                    }
                }
                return false;
            }
        });

        this.playerScroll = new AutoFocusScrollPane(playerTable, Andius.skin);
        this.playerScroll.setScrollingDisabled(true, false);

        for (int i = 0; i < context.players().length; i++) {
            CharacterRecord p = context.players()[i];
            PlayerListing pl = new PlayerListing(p);
            if (i == 0) {
                setSelectedPlayer(pl);
            }
            playerTable.add(pl);
            playerTable.row();
        }

        this.logTable = new Table(Andius.skin);
        this.logTable.bottom().left();
        this.logScroll = new LogScrollPane(Andius.skin, this.logTable, LOG_AREA_WIDTH);
        this.logScroll.setBounds(X_ALIGN + 300, 250, LOG_AREA_WIDTH, 395);

        this.playerScroll.setBounds(X_ALIGN, 25, PAT_SCR_WIDTH, 180);
        this.playerSelectionLabel.setBounds(X_ALIGN, 458, 20, 100);
        this.go.setBounds(X_ALIGN, 290, 60, 40);
        this.pool.setBounds(X_ALIGN + 70, 290, 60, 40);
        this.exit.setBounds(X_ALIGN + 140, 290, 60, 40);

        stage.addActor(sp1);
        stage.addActor(playerSelectionLabel);
        stage.addActor(exit);
        stage.addActor(go);
        stage.addActor(pool);
        stage.addActor(logScroll);
        stage.addActor(playerScroll);

        log("WELCOME TO THE " + contextMap.getLabel().toUpperCase());
    }

    private void setSelectedPlayer(PlayerListing listing) {
        this.selectedPlayer = listing;
        listing.addActor(focusIndicator);
    }

    private void takeNap(int hpAdd, int goldAmt, PlayerListing pi) {
        int expnextlvl = pi.c.checkAndSetLevel();
        while (expnextlvl >= 0) {
            pi.c.maxhp += pi.c.getMoreHP();

            log(pi.c.name.toUpperCase() + " IS LEVEL " + pi.c.level);
            pi.lvlracetype.setText("LVL " + pi.c.level + " " + pi.c.race + " " + pi.c.classType);

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
                pi.c.adjustHP(hpAdd);
                pi.c.adjustGold(-goldAmt);
                pi.gold.setText(String.valueOf(pi.c.gold));
                pi.hp.setText(pi.c.hp + " / " + pi.c.maxhp);
                healed++;
            }

            if (healed > 0) {
                log(pi.c.name.toUpperCase() + " HAS HEALED (" + healed + " WEEKS).");
                Sounds.play(Sound.HEALING);
            } else {
                if (pi.c.gold < goldAmt) {
                    log(pi.c.name.toUpperCase() + " DOES NOT HAVE ENOUGH GOLD.");
                } else if (pi.c.hp >= pi.c.maxhp) {
                    log(pi.c.name.toUpperCase() + " IS ALREADY FULLY HEALED.");
                }
            }
        } else {
            log(pi.c.name.toUpperCase() + " IS NAPPING.");
        }

        pi.status.setText(pi.c.isDead() ? "DEAD" : pi.c.status.toString());
        pi.hp.setText(pi.c.hp + " / " + pi.c.maxhp);
        pi.gold.setText(String.valueOf(pi.c.gold));
        pi.exp.setText(String.valueOf(pi.c.exp));

        log("");
    }

    private int gainOrLose(String desc, int attr, CharacterRecord c) {
        int newattrib = SaveGame.gainOrLose(attr);
        if (newattrib > attr) {
            log(c.name.toUpperCase() + " GAINED " + desc + ".");
        } else if (newattrib < attr) {
            log(c.name.toUpperCase() + " LOST " + desc + ".");
        }
        return newattrib;
    }

    private void log(String s) {
        this.logScroll.add(s);
    }

    private class PlayerListing extends Group {

        final Label name;
        final Label lvlracetype;
        final Label status;
        final Label hp;
        final Label gold;
        final Label exp;

        final LabelStyle style;
        final CharacterRecord c;

        PlayerListing(CharacterRecord rec) {
            this.c = rec;
            this.style = new LabelStyle(Andius.font16, rec.isDead() ? Color.RED : rec.status.color());

            this.name = new Label(rec.name.toUpperCase(), this.style);
            this.lvlracetype = new Label("LVL " + rec.level + " " + rec.race + " " + rec.classType, this.style);
            this.status = new Label(rec.isDead() ? "DEAD" : rec.status.toString(), this.style);
            this.hp = new Label(rec.hp + " / " + rec.maxhp, this.style);
            this.gold = new Label(String.valueOf(rec.gold), this.style);
            this.exp = new Label(String.valueOf(rec.exp), this.style);

            addActor(this.name);
            addActor(this.lvlracetype);
            addActor(this.status);
            addActor(this.hp);
            addActor(this.gold);
            addActor(this.exp);

            float x = getX();
            this.name.setBounds(x, getY(), 150, PAT_ITEM_HGT);
            this.lvlracetype.setBounds(x += 100, getY(), 200, PAT_ITEM_HGT);
            this.status.setBounds(x += 200, getY(), 120, PAT_ITEM_HGT);
            this.hp.setBounds(x += 100, getY(), 110, PAT_ITEM_HGT);
            this.gold.setBounds(x += 110, getY(), 100, PAT_ITEM_HGT);
            this.exp.setBounds(x += 75, getY(), 100, PAT_ITEM_HGT);

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
        Andius.font16.draw(batch, "WHO WILL STAY ?", X_ALIGN, 245);
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