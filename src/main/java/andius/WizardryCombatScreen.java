package andius;

import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import static andius.Andius.startScreen;
import andius.WizardryData.MazeCell;
import andius.objects.ClassType;
import andius.objects.HealthCursor;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.MutableMonster;
import andius.objects.Reward;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Sound;
import andius.objects.Sounds;
import andius.objects.Spells;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import java.util.ArrayList;
import java.util.List;
import utils.AutoFocusScrollPane;
import utils.FrameMaker;
import utils.LogScrollPane;
import utils.Utils;

public class WizardryCombatScreen extends Combat implements Screen, Constants {

    private final Stage stage;
    private final Batch batch;
    private final com.badlogic.gdx.scenes.scene2d.ui.List<SpellLabel> spellsList;
    private final com.badlogic.gdx.scenes.scene2d.ui.List<ActionLabel> actionsList;
    private final Table monstersTable;
    private final Table playersTable;
    private final AutoFocusScrollPane playersScroll;
    private final ScrollPane actionsScroll;
    private final AutoFocusScrollPane monstersScroll;
    private final AutoFocusScrollPane spellsScroll;
    private final LogScrollPane logs;
    private final TextButton fight;
    private final TextButton reset;
    private final TextButton flee;
    private final TextButton exit;

    private static final int TABLE_HEIGHT = 740;
    private static final int LISTING_WIDTH = 300;
    private static final int LINE_HEIGHT = 17;
    private static final int LOG_WIDTH = 370;
    private static final int LOG_HEIGHT = 455;

    private final Image selectedMonster = new Image(Utils.fillRectangle(LISTING_WIDTH, LINE_HEIGHT * 3, Color.RED, .25f));
    private final Image selectedPlayer = new Image(Utils.fillRectangle(LISTING_WIDTH, LINE_HEIGHT * 3, Color.YELLOW, .25f));
    private static final Texture GREEN = Utils.fillRectangle(LISTING_WIDTH, LINE_HEIGHT * 3, Color.GREEN, .2f);

    private final Texture background;
    private final TextureAtlas iconAtlas;

    private MazeCell destCell, fromCell;
    private final boolean hasTreasure;

    public WizardryCombatScreen(Context context, Map contextMap, String mname, Monster opponent, int level, boolean hasTreasure, MazeCell destCell, MazeCell fromCell) {
        super(context, contextMap, opponent, level);

        this.destCell = destCell;
        this.fromCell = fromCell;
        this.hasTreasure = hasTreasure;

        this.batch = new SpriteBatch();

        FrameMaker fm = new FrameMaker(SCREEN_WIDTH, SCREEN_HEIGHT);

        this.iconAtlas = new TextureAtlas(Gdx.files.classpath("assets/json/wizIcons.atlas"));

        Table logTable = new Table(Andius.skin);
        logTable.setBackground("log-background");

        this.logs = new LogScrollPane(Andius.skin, logTable, LOG_WIDTH);
        setLogs(this.logs);

        this.stage = new Stage();
        //this.stage.setDebugAll(true);

        this.monstersTable = new Table(Andius.skin);
        this.monstersTable.top().left();
        this.monstersScroll = new AutoFocusScrollPane(this.monstersTable, Andius.skin);
        this.monstersScroll.setScrollingDisabled(true, false);

        this.playersTable = new Table(Andius.skin);
        this.playersTable.top().left();
        this.playersScroll = new AutoFocusScrollPane(playersTable, Andius.skin);
        this.playersScroll.setScrollingDisabled(true, false);

        for (int i = 0; i < context.players().length; i++) {
            CharacterRecord p = this.ctx.players()[i];
            this.playersTable.add(new PlayerListing(i, p)).pad(3);
            this.playersTable.row();
        }

        this.actionsList = new com.badlogic.gdx.scenes.scene2d.ui.List(Andius.skin, "default-16-padded");
        this.actionsList.getSelection().setDisabled(true);
        this.actionsScroll = new ScrollPane(this.actionsList, Andius.skin);
        for (Action action : this.actions) {
            this.actionsList.getItems().add(new ActionLabel(action));
        }

        for (MutableMonster mm : monsters) {
            this.monstersTable.add(new MonsterListing(mm)).pad(3);
            this.monstersTable.row();
        }

        this.spellsList = new com.badlogic.gdx.scenes.scene2d.ui.List(Andius.skin, "default-16-padded");
        this.spellsScroll = new AutoFocusScrollPane(this.spellsList, Andius.skin);
        this.spellsScroll.setScrollingDisabled(true, false);

        CharacterRecord player = this.ctx.players()[0];

        for (Spells s : player.knownSpells) {
            addSpell(s);
        }
        if (player.weapon != null && player.weapon.spell != null) {
            addSpell(player.weapon);
        }
        if (player.helm != null && player.helm.spell != null) {
            addSpell(player.helm);
        }
        if (player.armor != null && player.armor.spell != null) {
            addSpell(player.armor);
        }
        if (player.item1 != null && player.item1.spell != null) {
            addSpell(player.item1);
        }
        if (player.item2 != null && player.item2.spell != null) {
            addSpell(player.item2);
        }
        for (Item i : player.inventory) {
            if (i.spell != null) {
                addSpell(i);
            }
        }

        this.spellsList.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                PlayerListing pl = (PlayerListing) selectedPlayer.getParent();
                SpellLabel sl = spellsList.getSelected();
                if (pl != null && sl != null) {
                    if (sl.spell != null) {
                        setAction(pl.index, sl.spell);
                    }
                    if (sl.item != null) {
                        setAction(pl.index, sl.item);
                    }
                    if (sl.dispel) {
                        setAction(pl.index, true);
                    }
                }
            }
        });

        int x = 345;
        this.fight = new TextButton("FIGHT", Andius.skin, "default-16");
        this.fight.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                fight();
            }
        });
        this.fight.setBounds(x, 480, 80, 40);

        this.reset = new TextButton("RESET", Andius.skin, "default-16");
        this.reset.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                actionsList.getItems().clear();
                actions.clear();
                for (CharacterRecord p : ctx.players()) {
                    actionsList.getItems().add(new ActionLabel(addAction(p)));
                }
            }
        });
        this.reset.setBounds(x += 100, 480, 80, 40);

        this.flee = new TextButton("FLEE", Andius.skin, "default-16");
        this.flee.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                end(true);
            }
        });
        this.flee.setBounds(x += 100, 480, 80, 40);

        this.exit = new TextButton("EXIT", Andius.skin, "default-16");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (ctx.pickRandomEnabledPlayer() == null) {
                    mainGame.setScreen(startScreen);
                } else {
                    if (pickMonster() == null) {

                        List<CharacterRecord> lastMenStanding = new ArrayList<>();
                        for (CharacterRecord p : players) {
                            if (!p.isDisabled()) {
                                lastMenStanding.add(p);
                            }
                        }

                        int goldRewardId = 0;
                        int chestRewardId = 0;
                        int exp = 0;

                        for (MutableMonster mm : monsters) {
                            if (mm != null) {
                                Monster m = (Monster) mm.baseType();
                                if (m.getExp() > exp) {
                                    exp = m.getExp();
                                }
                                if (m.getGoldReward() > goldRewardId) {
                                    goldRewardId = m.getGoldReward();
                                }
                                if (m.getChestReward() > chestRewardId) {
                                    chestRewardId = m.getChestReward();
                                }
                            }
                        }

                        WizardryCombatScreen.this.contextMap.getScreen().endCombat(true, WizardryCombatScreen.this.opponent);

                        for (CharacterRecord p : lastMenStanding) {
                            if (!p.isDead()) {
                                p.awardXP(exp / lastMenStanding.size());
                                WizardryCombatScreen.this.contextMap.getScreen().log(String.format("%s gained %d experience points.", p.name.toUpperCase(), exp / lastMenStanding.size()));
                            }
                        }

                        if (WizardryCombatScreen.this.hasTreasure) {
                            mainGame.setScreen(new RewardScreen(WizardryCombatScreen.this.ctx, WizardryCombatScreen.this.contextMap, chestRewardId));
                        } else {
                            Reward reward = contextMap.scenario().rewards().get(goldRewardId);
                            int goldAmt = reward.goldAmount();
                            for (SaveGame.CharacterRecord c : lastMenStanding) {
                                c.adjustGold(goldAmt / lastMenStanding.size());
                                WizardryCombatScreen.this.contextMap.getScreen().log(String.format("%s found %d gold.", c.name.toUpperCase(), goldAmt));
                            }
                            mainGame.setScreen(WizardryCombatScreen.this.contextMap.getScreen());
                        }

                    } else {
                        mainGame.setScreen(WizardryCombatScreen.this.contextMap.getScreen());
                    }
                }
            }
        });
        this.exit.setBounds(x, 480, 80, 40);

        fm.setBounds(this.playersScroll, 10, 412, LISTING_WIDTH, 345);
        fm.setBounds(this.spellsScroll, 10, 14, LISTING_WIDTH, 355);
        fm.setBounds(this.actionsScroll, 326, 584, LOG_WIDTH, 136);
        fm.setBounds(this.logs, 326, 14, LOG_WIDTH, LOG_HEIGHT);
        fm.setBounds(this.monstersScroll, 712, 14, LISTING_WIDTH, TABLE_HEIGHT - 35);

        this.stage.addActor(this.fight);
        this.stage.addActor(this.flee);
        this.stage.addActor(this.reset);
        this.stage.addActor(this.logs);

        this.stage.addActor(this.monstersScroll);
        this.stage.addActor(this.playersScroll);
        this.stage.addActor(this.actionsScroll);
        this.stage.addActor(this.spellsScroll);

        Label actionLabel = new Label("Action", Andius.skin, "default-16");
        actionLabel.setPosition(326, 730);
        this.stage.addActor(actionLabel);

        Label monLabel = new Label(mname, Andius.skin, "default-16");
        monLabel.setPosition(715, 730);
        this.stage.addActor(monLabel);

        Label spellLabel = new Label("Spells", Andius.skin, "default-16");
        spellLabel.setPosition(10, 380);
        this.stage.addActor(spellLabel);

        this.background = fm.build();
    }

    @Override
    public void end(boolean fled) {
        super.end(fled);

        this.fight.remove();
        this.reset.remove();
        this.flee.remove();

        if (fled && this.fromCell != null) {
            Screen dungeon = this.contextMap.getScreen();
            mainGame.setScreen(dungeon);
            if (dungeon instanceof TmxDungeonScreen) {
                ((TmxDungeonScreen) dungeon).teleport(this.fromCell.address, false);
            } else if (dungeon instanceof WizardryDungeonScreen) {
                ((WizardryDungeonScreen) dungeon).teleport(this.fromCell.address, false);
            } else {
                throw new IllegalStateException("Context map screen is not a dungeon screen: " + dungeon);
            }
        }

        this.stage.addActor(this.exit);
    }

    private class SpellLabel extends Label {

        final Spells spell;
        final Item item;
        final boolean dispel;

        public SpellLabel(Spells spell) {
            super(spell.toString(), Andius.skin, "default-16");
            this.item = null;
            this.spell = spell;
            this.dispel = false;
        }

        public SpellLabel(Item it) {
            super(it.name, Andius.skin, "default-16");
            this.item = it;
            this.spell = null;
            this.dispel = false;
        }

        public SpellLabel() {
            super("Dispel Undead", Andius.skin, "default-16");
            this.item = null;
            this.spell = null;
            this.dispel = true;
        }

        @Override
        public String toString() {
            if (this.dispel) {
                return "Dispel Undead";
            }
            return this.spell != null ? this.spell.label() : this.item.name + " - " + this.item.spell;
        }

    }

    private void addSpell(Spells s) {
        if (s.getArea() == SpellArea.COMBAT || s.getArea() == SpellArea.ANY_TIME) {
            SpellLabel label = new SpellLabel(s);
            spellsList.getItems().add(label);
        }
    }

    private void addSpell(Item i) {
        if (i.spell.getArea() == SpellArea.COMBAT || i.spell.getArea() == SpellArea.ANY_TIME) {
            SpellLabel label = new SpellLabel(i);
            spellsList.getItems().add(label);
        }
    }

    private class ActionLabel extends Label {

        final Action action;

        public ActionLabel(Action action) {
            super("", Andius.skin, "default-16");
            this.action = action;
        }

        @Override
        public String toString() {
            return this.action.toString();
        }

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(this.stage));
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public void log(String s) {
        this.logs.add(s);
    }

    @Override
    public void log(String s, Color c) {
        this.logs.add(s, c);
    }

    @Override
    public void playSound(Sound sound) {
        Sounds.play(sound);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(this.background, 0, 0);
        batch.end();

        stage.act();
        stage.draw();

    }

    private class PlayerStatusLabel extends Label {

        private final CharacterRecord rec;

        public PlayerStatusLabel(CharacterRecord rec) {
            super("", Andius.skin, "default-16");
            this.rec = rec;
            setColor(rec.status.color());
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.CharArray getText() {
            return new com.badlogic.gdx.utils.CharArray(
                    String.format("HP: %d /%d  AC: %d  ST: %s", rec.hp, rec.maxhp, rec.calculateAC(), rec.isDead() ? "DEAD" : rec.status));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

        @Override
        public Color getColor() {
            return rec.isDead() ? Color.SCARLET : rec.status.color();
        }

    }

    private class PlayerMagicPointsLabel extends Label {

        private final CharacterRecord rec;

        public PlayerMagicPointsLabel(CharacterRecord rec) {
            super("", Andius.skin, "default-16");
            this.rec = rec;
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.CharArray getText() {
            int[] ms = rec.magePoints;
            int[] cs = rec.clericPoints;
            return new com.badlogic.gdx.utils.CharArray(
                    String.format("M: %d %d %d %d %d %d %d  P: %d %d %d %d %d %d %d",
                            ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6], cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

    }

    private class PlayerListing extends Group {

        final int index;
        final Label l1;
        final PlayerStatusLabel l2;
        final PlayerMagicPointsLabel l3;
        final ListingBackground bckgrnd;
        final CharacterRecord player;

        PlayerListing(int index, CharacterRecord rec) {
            this.index = index;
            this.player = rec;
            this.bckgrnd = new ListingBackground();
            rec.healthCursor = this.bckgrnd;
            rec.healthCursor.adjust(rec.hp, rec.maxhp);

            this.l1 = new Label("", Andius.skin, "default-16");
            this.l2 = new PlayerStatusLabel(rec);
            this.l3 = new PlayerMagicPointsLabel(rec);

            String d1 = String.format("%s  %s  LVL %d  %s", rec.name.toUpperCase(), rec.race.toString(), rec.level, rec.classType.toString());
            this.l1.setText(d1);

            addActor(this.bckgrnd);
            addActor(this.l1);
            addActor(this.l2);
            addActor(this.l3);

            float x = 3;
            this.l1.setBounds(x, LINE_HEIGHT * 2, LISTING_WIDTH, LINE_HEIGHT);
            this.l2.setBounds(x, LINE_HEIGHT * 1, LISTING_WIDTH, LINE_HEIGHT);
            this.l3.setBounds(x, LINE_HEIGHT * 0, LISTING_WIDTH, LINE_HEIGHT);

            this.setSize(LISTING_WIDTH, LINE_HEIGHT * 3f);

            this.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectedPlayer.remove();
                    PlayerListing.this.addActor(selectedPlayer);

                    spellsList.getItems().clear();

                    for (Spells s : player.knownSpells) {
                        addSpell(s);
                    }
                    if (player.classType == ClassType.PRIEST
                            || (player.classType == ClassType.LORD && player.level >= 3)
                            || (player.classType == ClassType.BISHOP && player.level >= 8)) {
                        spellsList.getItems().add(new SpellLabel());
                    }
                    if (player.weapon != null && player.weapon.spell != null) {
                        addSpell(player.weapon);
                    }
                    if (player.helm != null && player.helm.spell != null) {
                        addSpell(player.helm);
                    }
                    if (player.armor != null && player.armor.spell != null) {
                        addSpell(player.armor);
                    }
                    if (player.item1 != null && player.item1.spell != null) {
                        addSpell(player.item1);
                    }
                    if (player.item2 != null && player.item2.spell != null) {
                        addSpell(player.item2);
                    }
                    for (Item i : player.inventory) {
                        if (i.spell != null) {
                            addSpell(i);
                        }
                    }

                }
            });
        }

    }

    private class MonsterStatusLabel extends Label {

        private final MutableMonster mm;

        public MonsterStatusLabel(MutableMonster mm) {
            super("", Andius.skin, "default-16");
            this.mm = mm;
            setColor(mm.status().color());
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.CharArray getText() {
            return new com.badlogic.gdx.utils.CharArray(
                    String.format("HP: %d / %d AC: %d  ST: %s", mm.getCurrentHitPoints(), mm.getMaxHitPoints(), mm.getArmourClass(), mm.getCurrentHitPoints() <= 0 ? "DEAD" : mm.status()));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

        @Override
        public Color getColor() {
            return mm.getCurrentHitPoints() <= 0 ? Color.SCARLET : mm.status().color();
        }

    }

    private class MonsterMagicPointsLabel extends Label {

        private final MutableMonster mm;

        public MonsterMagicPointsLabel(MutableMonster mm) {
            super("", Andius.skin, "default-16");
            this.mm = mm;
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.CharArray getText() {
            return new com.badlogic.gdx.utils.CharArray(String.format("MG: %d  PR: %d ", mm.getCurrentMageSpellLevel(), mm.getCurrentPriestSpellLevel()));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

    }

    private class MonsterListing extends Group {

        final Image icon;
        final Label l1;
        final MonsterStatusLabel l2;
        final MonsterMagicPointsLabel l3;
        final ListingBackground bckgrnd;

        final Monster m;
        final MutableMonster mm;

        MonsterListing(MutableMonster mm) {
            this.mm = mm;
            this.m = (Monster) mm.baseType();
            this.bckgrnd = new ListingBackground();
            mm.setHealthCursor(this.bckgrnd);
            this.bckgrnd.adjust(mm.getCurrentHitPoints(), mm.getMaxHitPoints());

            AtlasRegion ar = iconAtlas.findRegion("" + this.m.getIconId());

            if (ar == null) {
                ar = iconAtlas.findRegion("0");
            }

            this.icon = new Image(ar);

            this.l1 = new Label("", Andius.skin, "default-16");
            this.l2 = new MonsterStatusLabel(mm);
            this.l3 = new MonsterMagicPointsLabel(mm);
            this.icon.setPosition(220, 2);

            String d1 = String.format("%s  LVL %d", m.name.toUpperCase(), mm.getLevel());
            this.l1.setText(d1);

            addActor(this.bckgrnd);
            addActor(this.icon);
            addActor(this.l1);
            addActor(this.l2);
            addActor(this.l3);

            float x = 3;
            this.l1.setBounds(x, LINE_HEIGHT * 2, LISTING_WIDTH, LINE_HEIGHT);
            this.l2.setBounds(x, LINE_HEIGHT * 1, LISTING_WIDTH, LINE_HEIGHT);
            this.l3.setBounds(x, LINE_HEIGHT * 0, LISTING_WIDTH, LINE_HEIGHT);

            this.setSize(LISTING_WIDTH, LINE_HEIGHT * 3f);

            this.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectedMonster.remove();
                    MonsterListing.this.addActor(selectedMonster);

                    PlayerListing pl = (PlayerListing) selectedPlayer.getParent();
                    if (pl != null) {
                        setAction(pl.index, MonsterListing.this.mm);
                    }
                }
            });

        }

    }

    private class ListingBackground extends HealthCursor {

        TextureRegion healthGreen;

        public ListingBackground() {
            this.healthGreen = new TextureRegion(GREEN, 0, 0, LISTING_WIDTH, LINE_HEIGHT * 3);
        }

        @Override
        public void adjust(int hp, int maxhp) {
            double percent = (double) hp / maxhp;
            double bar = percent * (double) LISTING_WIDTH;
            if (hp < 0) {
                bar = 0;
            }
            if (bar > LISTING_WIDTH) {
                bar = LISTING_WIDTH;
            }
            healthGreen.setRegion(0, 0, (int) bar, LINE_HEIGHT * 3);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            batch.draw(healthGreen, getX(), getY());
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
    public void addMonster(MutableMonster mm) {
        super.addMonster(mm);

        this.monstersTable.add(new MonsterListing(mm)).pad(3);
        this.monstersTable.row();
    }

    @Override
    public void removeMonster(MutableMonster mm) {
        super.removeMonster(mm);

        Cell found = null;
        for (Cell cell : this.monstersTable.getCells()) {
            Actor actor = cell.getActor();
            if (actor instanceof MonsterListing ml) {
                if (ml.mm == mm) {
                    found = cell;
                    break;
                }
            }
        }

        if (found != null) {

            found.getActor().remove();
            monstersTable.getCells().removeValue(found, true);

            List<Actor> cells = new ArrayList<>();
            for (Cell c : monstersTable.getCells().toArray(Cell.class)) {
                cells.add(c.getActor());
            }

            monstersTable.reset();

            for (Actor a : cells) {
                monstersTable.add(a).pad(3);
                monstersTable.row();
            }

            monstersTable.layout();
        }
    }

}
