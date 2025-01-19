package andius;

import static andius.Andius.mainGame;
import static andius.Andius.startScreen;
import static andius.WizardryData.WER4_CHARS;
import andius.objects.Dice;
import andius.objects.DoGooder;
import andius.objects.HealthCursor;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.MutableCharacter;
import andius.objects.MutableMonster;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Spells;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import java.util.ArrayList;
import java.util.List;
import utils.AutoFocusScrollPane;
import utils.LogScrollPane;
import utils.Utils;

public class Wiz4CombatScreen implements Screen, InputProcessor, Constants {

    public final DoGooder opponent;
    private final Stage stage;

    private final Table monstersTable;
    private final Table enemiesTable;
    private final AutoFocusScrollPane monstersScroll;
    private final AutoFocusScrollPane enemiesScroll;

    public final List<MutableCharacter> enemies = new ArrayList<>();
    public final List<MutableMonster> monsters;
    public final CharacterRecord player;

    private final LogScrollPane logs;

    private static final int TABLE_HEIGHT = 750;
    private static final int LISTING_WIDTH = 300;
    private static final int LINE_HEIGHT = 17;
    private static final int LOG_WIDTH = 370;
    private static final int LOG_HEIGHT = 400;

    public Wiz4CombatScreen(CharacterRecord player, List<MutableMonster> monsters, DoGooder opponent) {

        this.opponent = opponent;
        this.player = player;
        this.monsters = monsters;

        Table logTable = new Table(Andius.skin);
        logTable.setBackground("log-background");
        this.logs = new LogScrollPane(Andius.skin, logTable, LOG_WIDTH);
        this.logs.setBounds(320, 10, LOG_WIDTH, LOG_HEIGHT);

        this.stage = new Stage();
        //this.stage.setDebugAll(true);
        this.stage.addActor(this.logs);

        this.monstersTable = new Table(Andius.skin);
        this.monstersTable.top().left();
        this.monstersScroll = new AutoFocusScrollPane(this.monstersTable, Andius.skin);
        this.monstersScroll.setScrollingDisabled(true, false);

        this.enemiesTable = new Table(Andius.skin);
        this.enemiesTable.top().left();
        this.enemiesScroll = new AutoFocusScrollPane(enemiesTable, Andius.skin);
        this.enemiesScroll.setScrollingDisabled(true, false);

        this.monstersTable.add(new PlayerListing(player)).pad(3);
        this.monstersTable.row();

        for (MutableMonster mm : monsters) {
            this.monstersTable.add(new MonsterListing(mm)).pad(3);
            this.monstersTable.row();
        }

        for (int id : opponent.partyMembers) {
            DoGooder dg = WER4_CHARS.get(id);
            MutableCharacter mutc = new MutableCharacter(dg);
            this.enemies.add(mutc);
            this.enemiesTable.add(new DoGooderListing(mutc)).pad(3);
            this.enemiesTable.row();
        }

        this.monstersScroll.setBounds(10, 10, LISTING_WIDTH, TABLE_HEIGHT);
        this.enemiesScroll.setBounds(700, 10, LISTING_WIDTH, TABLE_HEIGHT);

        this.stage.addActor(this.monstersScroll);
        this.stage.addActor(this.enemiesScroll);

        if (!this.opponent.slogan.isEmpty()) {
            log("You are about to battle with " + this.opponent.slogan.replace("|", " ... "));
        }

    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.F) {
            fight();
        }
        return false;
    }

    private void fight() {

        for (MutableMonster mm : monsters) {
            monsterFight(mm);
        }

        MutableCharacter defender = pickEnemy();
        if (defender != null) {
            if (!player.isDisabled()) {
                boolean hit = Utils.attackHit(player, defender);
                if (hit) {
                    Item weapon = player.weapon == null ? Item.HANDS : player.weapon;
                    int damage = Utils.dealDamage(weapon, defender);
                    log(String.format("%s %s %s, who %s after %d damage.",
                            player.name,
                            HITMSGS[Utils.RANDOM.nextInt(HITMSGS.length)],
                            defender.name(),
                            defender.getDamageTag(),
                            damage));
                } else {
                    log(String.format("%s misses %s", player.name, defender.name()));
                }
            }
            if (player.isDead()) {
                end();
            }
        } else {
            end();
        }

        for (MutableCharacter mm : enemies) {
            enemyFight(mm);
        }

    }

    public void end() {
        if (player.isDead()) {
            mainGame.setScreen(startScreen);
        } else {
            Map.WIZARDRY4.getScreen().endCombat(true, null);
            if (pickEnemy() == null) {
                mainGame.setScreen(new Wiz4RewardScreen(player, opponent));
            } else {
                mainGame.setScreen(Map.WIZARDRY4.getScreen());
            }
        }
    }

    private void monsterFight(MutableMonster attacker) {

        if (attacker.isDead() || attacker.status().isDisabled()) {
            return;
        }

        CombatAction action = CombatAction.ATTACK;
        Spells spell = null;

        if (attacker.breath() != Breath.NONE && Utils.RANDOM.nextInt(100) < 60) {
            action = CombatAction.BREATH;
        }

        if (attacker.getCurrentMageSpellLevel() > 0 && !attacker.status().has(Status.SILENCED) && Utils.RANDOM.nextInt(100) < 75) {
            spell = attacker.castMageSpell();
            action = CombatAction.CAST;
        }

        if (action != CombatAction.CAST) {
            if (attacker.getCurrentPriestSpellLevel() > 0 && !attacker.status().has(Status.SILENCED) && Utils.RANDOM.nextInt(100) < 75) {
                spell = attacker.castPriestSpell();
                action = CombatAction.CAST;
            }
        }

        switch (action) {
            case BREATH:
                log(String.format("%s breathes %s", attacker.name(), attacker.breath()));
                for (MutableCharacter defender : enemies) {
                    int dmg = attacker.getCurrentHitPoints() / 2;
                    DoGooder dg = (DoGooder) defender.baseType();
                    if (dg.savingThrowBreath()) {
                        log(String.format("%s made a saving throwing throw against %s", defender.name(), attacker.breath()));
                        dmg = dmg / 2;
                    }
                    defender.setCurrentHitPoints(defender.getCurrentHitPoints() - dmg);
                    defender.getHealthCursor().adjust(defender.getCurrentHitPoints(), defender.getMaxHitPoints());
                }
                break;
            case ATTACK:
                MutableCharacter defender = pickEnemy();
                if (defender != null) {
                    boolean hit = Utils.attackHit(attacker, defender);
                    if (hit) {
                        for (Dice dice : attacker.getDamage()) {
                            int dmg = dice.roll();
                            log(String.format("%s hit %s for %d damage!", attacker.name(), defender.name(), dmg));
                            defender.setCurrentHitPoints(defender.getCurrentHitPoints() - dmg);
                            defender.getHealthCursor().adjust(defender.getCurrentHitPoints(), defender.getMaxHitPoints());
                        }
                    } else {
                        log(String.format("%s misses %s", attacker.name(), defender.name()));
                    }
                }
                break;
            case CAST: {
                log(String.format("%s casts %s", attacker.name(), spell));
                //SpellUtil.spellMonsterCast(this, seq, spell, creature, target);
                break;
            }
        }

    }

    private void enemyFight(MutableCharacter attacker) {

        if (attacker.isDead() || attacker.status().isDisabled()) {
            return;
        }

        CombatAction action = CombatAction.ATTACK;
        Spells spell = null;

        if (attacker.getCurrentMageSpellLevel() > 0 && !attacker.status().has(Status.SILENCED) && Utils.RANDOM.nextInt(100) < 75) {
            spell = attacker.castMageSpell();
            action = CombatAction.CAST;
        }

        if (action != CombatAction.CAST) {
            if (attacker.getCurrentPriestSpellLevel() > 0 && !attacker.status().has(Status.SILENCED) && Utils.RANDOM.nextInt(100) < 75) {
                spell = attacker.castPriestSpell();
                action = CombatAction.CAST;
            }
        }

        switch (action) {
            case ATTACK:
                MutableMonster defender = pickMonster();
                int roll = Utils.RANDOM.nextInt(100);
                if (roll > 15 && defender != null) {
                    boolean hit = Utils.attackHit(attacker, defender);
                    if (hit) {
                        for (Dice dice : attacker.getDamage()) {
                            int dmg = dice.roll();
                            log(String.format("%s hit %s for %d damage!", attacker.name(), defender.name(), dmg));
                            defender.setCurrentHitPoints(defender.getCurrentHitPoints() - dmg);
                            defender.getHealthCursor().adjust(defender.getCurrentHitPoints(), defender.getMaxHitPoints());
                        }
                    } else {
                        log(String.format("%s misses %s", attacker.name(), defender.name()));
                    }
                } else {
                    boolean hit = Utils.attackHit(attacker, player);
                    if (hit) {
                        for (Dice dice : attacker.getDamage()) {
                            int dmg = dice.roll();
                            log(String.format("%s hit %s for %d damage!", attacker.name(), player.name, dmg));
                            player.adjustHP(-dmg);
                            player.healthCursor.adjust(player.hp, player.maxhp);
                        }
                    } else {
                        log(String.format("%s misses %s", attacker.name(), player.name));
                    }
                }
                break;
            case CAST: {
                log(String.format("%s casts %s", attacker.name(), spell));
                //SpellUtil.spellMonsterCast(this, seq, spell, creature, target);
                break;
            }
        }

    }

    private MutableCharacter pickEnemy() {
        List<MutableCharacter> notDead = new ArrayList<>();
        for (MutableCharacter m : enemies) {
            if (m.getCurrentHitPoints() > 0) {
                notDead.add(m);
            }
        }
        if (notDead.isEmpty()) {
            return null;
        }
        return notDead.get(Utils.RANDOM.nextInt(notDead.size()));
    }

    private MutableMonster pickMonster() {
        List<MutableMonster> notDead = new ArrayList<>();
        for (MutableMonster m : monsters) {
            if (m.getCurrentHitPoints() > 0) {
                notDead.add(m);
            }
        }
        if (notDead.isEmpty()) {
            return null;
        }
        return notDead.get(Utils.RANDOM.nextInt(notDead.size()));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(this, this.stage));
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

    public void log(String s) {
        this.logs.add(s);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();

    }

    private class PlayerStatusLabel extends Label {

        private final CharacterRecord rec;

        public PlayerStatusLabel(CharacterRecord rec) {
            super("", Andius.skin, "default");
            this.rec = rec;
            setColor(rec.status.color());
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.StringBuilder getText() {
            return new com.badlogic.gdx.utils.StringBuilder(
                    String.format("HP: %d /%d  AC: %d  ST: %s", rec.hp, rec.maxhp, rec.calculateAC(), rec.isDead() ? "DEAD" : rec.status));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

        @Override
        public Color getColor() {
            return rec.isDead() ? Color.RED : rec.status.color();
        }

    }

    private class PlayerListing extends Group {

        final Label l1;
        final PlayerStatusLabel l2;
        final Label l3;
        final ListingBackground bckgrnd;
        final SaveGame.CharacterRecord c;

        PlayerListing(SaveGame.CharacterRecord rec) {
            this.c = rec;
            this.bckgrnd = new ListingBackground();
            rec.healthCursor = this.bckgrnd;

            this.l1 = new Label("", Andius.skin, "default");
            this.l2 = new PlayerStatusLabel(rec);
            this.l3 = new Label("", Andius.skin, "default");

            String d1 = String.format("%s  %s  LVL %d  %s", rec.name.toUpperCase(), rec.race.toString(), rec.level, rec.classType.toString());
            int[] ms = rec.magePoints;
            int[] cs = rec.clericPoints;
            String d3 = String.format("M: %d %d %d %d %d %d %d  P: %d %d %d %d %d %d %d",
                    ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6], cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]);
            this.l1.setText(d1);
            this.l3.setText(d3);

            addActor(this.bckgrnd);
            addActor(this.l1);
            addActor(this.l2);
            addActor(this.l3);

            float x = 3;
            this.l1.setBounds(x, LINE_HEIGHT * 2, LISTING_WIDTH, LINE_HEIGHT);
            this.l2.setBounds(x, LINE_HEIGHT * 1, LISTING_WIDTH, LINE_HEIGHT);
            this.l3.setBounds(x, LINE_HEIGHT * 0, LISTING_WIDTH, LINE_HEIGHT);

            this.setSize(LISTING_WIDTH, LINE_HEIGHT * 3f);
        }

    }

    private class MonsterStatusLabel extends Label {

        private final MutableMonster mm;

        public MonsterStatusLabel(MutableMonster mm) {
            super("", Andius.skin, "default");
            this.mm = mm;
            setColor(mm.status().color());
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.StringBuilder getText() {
            return new com.badlogic.gdx.utils.StringBuilder(
                    String.format("HP: %d / %d AC: %d  ST: %s", mm.getCurrentHitPoints(), mm.getMaxHitPoints(), mm.getArmourClass(), mm.getCurrentHitPoints() <= 0 ? "DEAD" : mm.status()));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

        @Override
        public Color getColor() {
            return mm.getCurrentHitPoints() <= 0 ? Color.RED : mm.status().color();
        }

    }

    private class MonsterListing extends Group {

        final Label l1;
        final MonsterStatusLabel l2;
        final Label l3;
        final ListingBackground bckgrnd;

        final Monster m;
        final MutableMonster mm;

        MonsterListing(MutableMonster mm) {
            this.mm = mm;
            this.m = (Monster) mm.baseType();
            this.bckgrnd = new ListingBackground();
            mm.setHealthCursor(this.bckgrnd);

            this.l1 = new Label("", Andius.skin, "default");
            this.l2 = new MonsterStatusLabel(mm);
            this.l3 = new Label("", Andius.skin, "default");

            String d1 = String.format("%s  LVL %d", m.name.toUpperCase(), mm.getLevel());
            String d3 = String.format("MG: %d  PR: %d ", mm.getCurrentMageSpellLevel(), mm.getCurrentPriestSpellLevel());
            this.l1.setText(d1);
            this.l3.setText(d3);

            addActor(this.bckgrnd);
            addActor(this.l1);
            addActor(this.l2);
            addActor(this.l3);

            float x = 3;
            this.l1.setBounds(x, LINE_HEIGHT * 2, LISTING_WIDTH, LINE_HEIGHT);
            this.l2.setBounds(x, LINE_HEIGHT * 1, LISTING_WIDTH, LINE_HEIGHT);
            this.l3.setBounds(x, LINE_HEIGHT * 0, LISTING_WIDTH, LINE_HEIGHT);

            this.setSize(LISTING_WIDTH, LINE_HEIGHT * 3f);

        }

    }

    private class DoGooderStatusLabel extends Label {

        private final MutableCharacter mm;

        public DoGooderStatusLabel(MutableCharacter mm) {
            super("", Andius.skin, "default");
            this.mm = mm;
            setColor(mm.status().color());
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.StringBuilder getText() {
            return new com.badlogic.gdx.utils.StringBuilder(
                    String.format("HP: %d / %d AC: %d  ST: %s", mm.getCurrentHitPoints(), mm.getMaxHitPoints(), mm.getArmourClass(), mm.getCurrentHitPoints() <= 0 ? "DEAD" : mm.status()));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

        @Override
        public Color getColor() {
            return mm.getCurrentHitPoints() <= 0 ? Color.RED : mm.status().color();
        }

    }

    private class DoGooderListing extends Group {

        final Label l1;
        final DoGooderStatusLabel l2;
        final Label l3;
        final ListingBackground bckgrnd;

        final DoGooder m;
        final MutableCharacter mm;

        DoGooderListing(MutableCharacter mm) {
            this.mm = mm;
            this.m = (DoGooder) mm.baseType();
            this.bckgrnd = new ListingBackground();
            mm.setHealthCursor(this.bckgrnd);

            this.l1 = new Label("", Andius.skin, "default");
            this.l2 = new DoGooderStatusLabel(mm);
            this.l3 = new Label("", Andius.skin, "default");

            String d1 = String.format("%s  %s  LVL %d  %s", m.name.toUpperCase(), m.race, mm.getLevel(), m.characterClass);
            int[] ms = mm.getMageSpellLevels();
            int[] cs = mm.getPriestSpellLevels();
            String d3 = String.format("M: %d %d %d %d %d %d %d  P: %d %d %d %d %d %d %d",
                    ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6], cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]);

            this.l1.setText(d1);
            this.l3.setText(d3);

            addActor(this.bckgrnd);
            addActor(this.l1);
            addActor(this.l2);
            addActor(this.l3);

            float x = 3;
            this.l1.setBounds(x, LINE_HEIGHT * 2, LISTING_WIDTH, LINE_HEIGHT);
            this.l2.setBounds(x, LINE_HEIGHT * 1, LISTING_WIDTH, LINE_HEIGHT);
            this.l3.setBounds(x, LINE_HEIGHT * 0, LISTING_WIDTH, LINE_HEIGHT);

            this.setSize(LISTING_WIDTH, LINE_HEIGHT * 3f);

        }

    }

    private static final Texture GREEN = Utils.fillRectangle(LISTING_WIDTH, LINE_HEIGHT * 3, Color.GREEN, .2f);

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
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

}
