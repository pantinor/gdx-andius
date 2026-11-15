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
import utils.ManagementUi;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import static utils.SkinUtils.GOLD;
import static utils.SkinUtils.INK;
import static utils.SkinUtils.MUTED;

public class InnScreen implements Screen, Constants {

    private final Context context;
    private final Constants.Map contextMap;

    private final ManagementUi ui = new ManagementUi();
    private final Texture hud;
    private Label roomDescription, bookingCaption;
    private final Stage stage;

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

    private static final int LOG_AREA_WIDTH = 280;
    private static final int PAT_SCR_WIDTH = 304;
    private static final int PAT_ITEM_HGT = 84;

    public InnScreen(Context context, final Constants.Map contextMap) {
        this.context = context;
        this.contextMap = contextMap;

        for (CharacterRecord player : context.players()) {
            player.acmodifier2 = 0;
        }

        this.hud = new Texture(Gdx.files.classpath("assets/data/inn.png"));

        this.stage = new Stage(new FitViewport(Andius.SCREEN_WIDTH, Andius.SCREEN_HEIGHT));

        this.focusIndicator = ui.selection(PAT_SCR_WIDTH, PAT_ITEM_HGT);
        this.focusIndicator.setWidth(PAT_SCR_WIDTH);
        this.focusIndicator.setHeight(PAT_ITEM_HGT);

        this.roomSelection = new List<>(ui.skin);
        Array<String> names = new Array<>();
        names.add("Stables  /  Free");
        names.add("Cots  /  10 gold per week");
        names.add("Economy room  /  50 gold per week");
        names.add("Merchant suite  /  200 gold per week");
        names.add("Royal suite  /  500 gold per week");
        this.roomSelection.setItems(names);

        this.go = new TextButton("BUY", ui.skin);
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

                int choice = roomSelection.getSelectedIndex();
                takeNap(new int[] {0, 1, 3, 7, 10}[choice], new int[] {0, 10, 50, 200, 500}[choice], selectedPlayer);
            }
        });

        this.pool = new TextButton("POOL", ui.skin);
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

        this.exit = new TextButton("EXIT", ui.skin);
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(contextMap.getScreen());
            }
        });

        this.playerTable = new Table(ui.skin);
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

        this.playerScroll = new AutoFocusScrollPane(playerTable, ui.skin);
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

        this.logTable = new Table(ui.skin);
        this.logTable.bottom().left();
        this.logScroll = new LogScrollPane(ui.skin, this.logTable, LOG_AREA_WIDTH, "default");

        buildLayout();

        log("Welcome to the inn. Select a guest and a room.");
    }

    private void setSelectedPlayer(PlayerListing listing) {
        this.selectedPlayer = listing;
        listing.addActor(focusIndicator);
        focusIndicator.toBack();
    }

    private void buildLayout() {
        Image artwork = new Image(hud);
        artwork.setBounds(0, 0, Andius.SCREEN_WIDTH, Andius.SCREEN_HEIGHT);
        artwork.setTouchable(Touchable.disabled);
        stage.addActor(artwork);
        Image headingShade = new Image(ui.surface("0c141ddd"));
        headingShade.setBounds(0, 696, Andius.SCREEN_WIDTH, 72);
        headingShade.setTouchable(Touchable.disabled);
        stage.addActor(headingShade);
        ui.label(stage, "The Wayfarers' Rest", "default-24", GOLD, 24, 714, 624, 34);
        ui.label(stage, contextMap.getLabel() + "  /  Inn & lodgings", "default", MUTED, 24, 696, 624, 20);
        ui.label(stage, "A light in the window.", "default-16", GOLD, 24, 642, 304, 28);
        Label welcome = ui.description(stage, 24, 560, 280, 72);
        welcome.setText("Lay down your burdens. A warm bed and a little time can ready your company for the road ahead.");
        ui.panel(stage, "Rooms & rest", "Choose your accommodation", 24, 96, 304, 390, "15232ff0");
        roomSelection.setBounds(36, 260, 280, (float) Math.ceil(roomSelection.getPrefHeight()));
        stage.addActor(roomSelection);
        roomDescription = ui.description(stage, 36, 112, 280, 132);
        ui.panel(stage, "Rest journal", "News of recovery and advancement", 344, 96, 304, 230, "15232ff0");
        logScroll.setBounds(356, 108, 280, 152);
        logScroll.setFadeScrollBars(false);
        stage.addActor(logScroll);
        ui.panel(stage, "The company", "Select the guest who will stay", 672, 96, 328, 600, "15232ff0");
        playerScroll.setBounds(684, 108, 304, 516);
        playerScroll.setFadeScrollBars(false);
        stage.addActor(playerScroll);
        bookingCaption = ui.label(stage, "", "default", GOLD, 24, 72, 624, 20);
        go.setText("Stay & recover");
        go.setBounds(24, 24, 172, 38);
        pool.setText("Pool gold");
        pool.setBounds(204, 24, 124, 38);
        exit.setText("Return to journey");
        exit.setBounds(850, 24, 150, 38);
        stage.addActor(go);
        stage.addActor(pool);
        stage.addActor(exit);
        ui.primary(go);
    }

    private void refreshUI() {
        for (Cell<?> cell : playerTable.getCells()) if (cell.getActor() instanceof PlayerListing listing) listing.refresh();
        int choice = roomSelection.getSelectedIndex();
        int cost = choice < 0 ? 0 : new int[] {0, 10, 50, 200, 500}[choice];
        int recovery = choice < 0 ? 0 : new int[] {0, 1, 3, 7, 10}[choice];
        go.setDisabled(selectedPlayer == null || selectedPlayer.c.isDead() || choice < 0 || selectedPlayer.c.gold < cost);
        pool.setDisabled(selectedPlayer == null || context.players().length < 2);
        if (selectedPlayer == null) return;
        bookingCaption.setText(selectedPlayer.c.name + "  /  " + selectedPlayer.c.gold + " gold available");
        roomDescription.setText(cost == 0
                ? "A place to sleep, free of charge.\nRestores spell uses and checks for advancement. No health recovery."
                : "+" + recovery + " HP per week  /  " + cost + " gold per week.\nRestores spell uses and checks for advancement.\nPaid stays repeat until health is restored or funds run out.");
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
            this.style = new LabelStyle(ui.skin.getFont("body"), rec.isDead() ? Color.RED : rec.status.color());

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

            Image portrait = new Image(andius.objects.Portraits.get(rec));
            portrait.setScaling(Scaling.fit);
            portrait.setBounds(0, 4, 76, 76);
            addActor(portrait);
            this.name.setBounds(88, 60, 216, 20);
            this.lvlracetype.setBounds(88, 42, 216, 18);
            this.hp.setBounds(88, 24, 134, 18);
            this.status.setBounds(226, 24, 78, 18);
            this.gold.setBounds(88, 6, 106, 18);
            this.exp.setBounds(198, 6, 106, 18);
            for (Actor actor : getChildren()) if (actor instanceof Label label) label.setEllipsis(true);
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
            exp.setText("XP " + c.exp);
            name.setColor(Color.WHITE);
            lvlracetype.setColor(MUTED);
            hp.setColor(color);
            status.setColor(color);
            gold.setColor(MUTED);
            exp.setColor(MUTED);
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
        hud.dispose();
        ui.dispose();
    }
}
