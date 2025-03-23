package andius;

import static andius.Andius.CTX;
import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import static andius.Andius.startScreen;
import andius.WizardryData.MazeCell;
import static andius.WizardryData.WER4_CHARS;
import static andius.WizardryData.WER_ITEMS;
import andius.objects.ClassType;
import andius.objects.Dice;
import andius.objects.DoGooder;
import andius.objects.HealthCursor;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.Mutable;
import andius.objects.MutableCharacter;
import andius.objects.MutableMonster;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Spells;
import static andius.objects.Spells.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import utils.AutoFocusScrollPane;
import utils.FrameMaker;
import utils.LogScrollPane;
import utils.Utils;

public class Wiz4CombatScreen implements Screen, Constants {

    public final DoGooder opponent;
    private final Stage stage;
    private final Batch batch;

    private final com.badlogic.gdx.scenes.scene2d.ui.List<SpellLabel> spells;
    private final Table monstersTable;
    private final Table enemiesTable;
    private final AutoFocusScrollPane monstersScroll;
    private final AutoFocusScrollPane enemiesScroll;
    private final AutoFocusScrollPane spellsScroll;

    public final List<MutableCharacter> enemies = new ArrayList<>();
    public final List<MutableMonster> monsters;
    public final CharacterRecord player;

    private final LogScrollPane logs;
    private final TextButton cast;
    private final TextButton fight;
    private final TextButton flee;
    private final TextButton exit;

    private static final int TABLE_HEIGHT = 740;
    private static final int LISTING_WIDTH = 300;
    private static final int LINE_HEIGHT = 17;
    private static final int LOG_WIDTH = 370;
    private static final int LOG_HEIGHT = 400;

    private final Image selected = new Image(Utils.fillRectangle(LISTING_WIDTH, LINE_HEIGHT * 3, Color.RED, .25f));
    private static final Texture GREEN = Utils.fillRectangle(LISTING_WIDTH, LINE_HEIGHT * 3, Color.GREEN, .2f);

    private final Texture background;
    private final TextureAtlas iconAtlas;

    private int round = 1;
    private int suprised = 0;
    MazeCell destCell, fromCell;

    public Wiz4CombatScreen(CharacterRecord player, List<MutableMonster> monsters, DoGooder opponent, MazeCell destCell, MazeCell fromCell) {

        this.opponent = opponent;
        this.player = player;
        this.monsters = monsters;
        this.destCell = destCell;
        this.fromCell = fromCell;

        this.batch = new SpriteBatch();

        FrameMaker fm = new FrameMaker(SCREEN_WIDTH, SCREEN_HEIGHT);

        this.iconAtlas = new TextureAtlas(Gdx.files.classpath("assets/json/wiz4ibm.atlas"));

        Table logTable = new Table(Andius.skin);
        logTable.setBackground("log-background");
        this.logs = new LogScrollPane(Andius.skin, logTable, LOG_WIDTH);

        this.stage = new Stage();
        //this.stage.setDebugAll(true);

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

        this.spells = new com.badlogic.gdx.scenes.scene2d.ui.List(Andius.skin, "default-16");
        this.spellsScroll = new AutoFocusScrollPane(this.spells, Andius.skin);
        this.spellsScroll.setScrollingDisabled(true, false);

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

        int x = 365;
        this.fight = new TextButton("FIGHT", Andius.skin, "default-16");
        this.fight.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                fight();
            }
        });
        this.fight.setBounds(x, 426, 80, 40);

        this.cast = new TextButton("CAST", Andius.skin, "default-16");
        this.cast.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                cast();
            }
        });
        this.cast.setBounds(x += 100, 426, 80, 40);

        this.flee = new TextButton("FLEE", Andius.skin, "default-16");
        this.flee.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                end(true);
            }
        });
        this.flee.setBounds(x += 100, 426, 80, 40);

        this.exit = new TextButton("EXIT", Andius.skin, "default-16");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (player.isDead()) {
                    mainGame.setScreen(startScreen);
                } else {
                    if (pickEnemy() == null) {
                        Map.WIZARDRY4.getScreen().endCombat(true, opponent);
                        mainGame.setScreen(new Wiz4RewardScreen(player, opponent));
                    } else {
                        mainGame.setScreen(Map.WIZARDRY4.getScreen());
                    }
                }
            }
        });
        this.exit.setBounds(x, 426, 80, 40);

        fm.setBounds(this.monstersScroll, 10, 14, LISTING_WIDTH, TABLE_HEIGHT);
        fm.setBounds(this.spellsScroll, 340, 530, 300, 220);
        fm.setBounds(this.logs, 326, 14, LOG_WIDTH, LOG_HEIGHT);
        fm.setBounds(this.enemiesScroll, 712, 14, LISTING_WIDTH, TABLE_HEIGHT - 35);

        this.stage.addActor(this.fight);
        this.stage.addActor(this.flee);
        this.stage.addActor(this.cast);
        this.stage.addActor(this.logs);

        this.stage.addActor(this.monstersScroll);
        this.stage.addActor(this.enemiesScroll);
        this.stage.addActor(this.spellsScroll);

        if (!this.opponent.slogan.isEmpty()) {
            Label l = new Label(this.opponent.slogan.split("[|]")[0], Andius.skin, "default-16");
            l.setPosition(715, 730);
            this.stage.addActor(l);
        } else {
            Label l = new Label(this.opponent.name, Andius.skin, "default-16");
            l.setPosition(715, 730);
            this.stage.addActor(l);
        }

        this.background = fm.build();

        String disp = !this.opponent.slogan.isEmpty() ? this.opponent.slogan.replace("|", "  ") : this.opponent.name;

        if (Utils.percentChance(20)) {
            this.suprised = 1;
            log("You suprised " + disp, Color.YELLOW);
        } else if (Utils.percentChance(20)) {
            this.suprised = 2;
            log("You were suprised by " + disp, Color.YELLOW);
        } else {
            this.suprised = 0;
            log("You encounter " + disp, Color.YELLOW);
        }

    }

    private void cast() {

        List<Object> shuffled = new ArrayList();

        if (this.suprised == 1) {
            shuffled.add(player);
            shuffled.addAll(this.monsters);
            shuffled.addAll(this.enemies);
        } else if (this.suprised == 2) {
            shuffled.addAll(this.enemies);
            shuffled.addAll(this.monsters);
            shuffled.add(player);
        } else {
            shuffled.add(player);
            shuffled.addAll(this.monsters);
            shuffled.addAll(this.enemies);
            Collections.shuffle(shuffled);
        }

        this.suprised = 0;

        for (Object m : shuffled) {
            if (m instanceof CharacterRecord) {
                MutableCharacter defender = pickEnemy();
                if (defender != null) {
                    SpellLabel sp = this.spells.getSelected();
                    if (sp != null) {
                        Spells spell = sp.spell != null ? sp.spell : sp.item.spell;
                        DoGooderListing dgl = (DoGooderListing) selected.getParent();
                        if (dgl != null) {
                            spellCast(spell, player, dgl.mm, sp.item != null);
                        } else {
                            spellCast(spell, player, null, sp.item != null);
                        }
                        if (sp.item != null && sp.item.changeChance > 0) {
                            boolean decayed = Utils.percentChance(sp.item.changeChance);
                            if (decayed) {
                                Item changeTo = WER_ITEMS.get(sp.item.changeTo);
                                player.removeItem(sp.item.id, sp.item.scenarioID);
                                if (changeTo.id != 0) {
                                    player.inventory.add(changeTo);
                                }
                                this.spells.getItems().removeValue(sp, false);
                                this.spells.getSelection().clear();
                            }
                        }
                    }
                }
            } else if (m instanceof MutableMonster) {
                monsterFight((MutableMonster) m);
            } else {
                enemyFight((MutableCharacter) m);
            }
        }

        CTX.endTurn();

        boolean alive = false;
        for (MutableCharacter c : this.enemies) {
            if (!c.isDead()) {
                alive = true;
            }
        }

        if (!alive) {
            end(false);
        } else if (player.isDead()) {
            end(false);
        }

        log("------------ end of round " + round, Color.YELLOW);
        round++;
    }

    private void fight() {

        List<Object> shuffled = new ArrayList();

        if (this.suprised == 1) {
            shuffled.add(player);
            shuffled.addAll(this.monsters);
            shuffled.addAll(this.enemies);
        } else if (this.suprised == 2) {
            shuffled.addAll(this.enemies);
            shuffled.addAll(this.monsters);
            shuffled.add(player);
        } else {
            shuffled.add(player);
            shuffled.addAll(this.monsters);
            shuffled.addAll(this.enemies);
            Collections.shuffle(shuffled);
        }

        this.suprised = 0;

        for (Object m : shuffled) {
            if (m instanceof CharacterRecord) {
                MutableCharacter defender = pickEnemy();
                if (defender != null) {
                    if (!player.isDisabled()) {
                        boolean hit = Utils.attackHit(player, defender);
                        if (hit) {
                            Item weapon = player.weapon == null ? Item.HANDS : player.weapon;
                            int damage = Utils.dealDamage(weapon, defender);
                            log(defender.getDamageDescription(player.name, damage), Color.SCARLET);
                        } else {
                            log(String.format("%s misses %s", player.name.toUpperCase(), defender.name()), Color.WHITE);
                        }
                    }
                }
            } else if (m instanceof MutableMonster) {
                monsterFight((MutableMonster) m);
            } else {
                enemyFight((MutableCharacter) m);
            }
        }

        CTX.endTurn();

        boolean alive = false;
        for (MutableCharacter c : this.enemies) {
            if (!c.isDead()) {
                alive = true;
            }
        }

        if (!alive) {
            end(false);
        } else if (player.isDead()) {
            end(false);
        }

        log("------------ end of round " + round, Color.YELLOW);
        round++;

    }

    public void end(boolean fled) {
        player.acmodifier1 = 0;

        this.fight.remove();
        this.cast.remove();
        this.flee.remove();

        if (fled) {
            WizardryDungeonScreen sc = (WizardryDungeonScreen) Map.WIZARDRY4.getScreen();
            sc.teleport(this.fromCell.address, false);
        }

        this.stage.addActor(this.exit);
    }

    private void monsterFight(MutableMonster attacker) {

        if (attacker.isDead()) {
            return;
        }

        attacker.processStatusAffects();

        if (attacker.status().isDisabled()) {
            log(attacker.name().toUpperCase() + " is " + attacker.status().toLongString(), attacker.status().color());
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

        if (attacker.monster().ability.contains(Ability.CALLFORHELP)
                && attacker.getPercentDamaged() < 0.50
                && Utils.RANDOM.nextInt(100) < 75
                && Utils.percentChance(attacker.getLevel() * 5)) {
            action = CombatAction.CALL_FOR_HELP;
        }

        switch (action) {
            case BREATH:
                log(String.format("%s breathes %s", attacker.name(), attacker.breath()), Color.BROWN);
                for (MutableCharacter defender : enemies) {
                    int dmg = attacker.getCurrentHitPoints() / 2;
                    DoGooder dg = (DoGooder) defender.baseType();
                    if (dg.savingThrowBreath()) {
                        log(String.format("%s made a saving throwing throw against %s", defender.name(), attacker.breath()));
                        dmg = dmg / 2;
                    }
                    damage(attacker, defender, dmg);
                }
                break;
            case ATTACK:
                MutableCharacter defender = pickEnemy();
                if (defender != null) {
                    boolean hit = Utils.attackHit(attacker, defender);
                    if (hit) {
                        for (Dice dice : attacker.getDamage()) {
                            int dmg = dice.roll();
                            damage(attacker, defender, dmg);
                        }
                    } else {
                        log(String.format("%s misses %s", attacker.name(), defender.name()));
                    }
                }
                break;
            case CAST: {
                MutableCharacter target = pickEnemy();
                if (target != null) {
                    log(String.format("%s casts %s", attacker.name(), spell), Color.SKY);
                    spellCast(spell, attacker, target, false);
                }
                break;
            }
            case CALL_FOR_HELP: {
                log(String.format("%s called for help!", attacker.name()), Color.GOLDENROD);
                MutableMonster clone = new MutableMonster(attacker.monster());
                this.monsters.add(clone);
                break;
            }
        }

    }

    private void enemyFight(MutableCharacter attacker) {

        if (attacker.isDead()) {
            return;
        }

        attacker.processStatusAffects();

        if (attacker.status().isDisabled()) {
            log(attacker.name().toUpperCase() + " is " + attacker.status().toLongString(), attacker.status().color());
            return;
        }

        CombatAction action = CombatAction.ATTACK;
        Spells spell = null;

        if (attacker.getCurrentMageSpellLevel() > 0 && !attacker.status().has(Status.SILENCED) && Utils.RANDOM.nextInt(100) < 75) {
            spell = attacker.castMageSpell();
            action = spell != null ? CombatAction.CAST : CombatAction.ATTACK;
        }

        if (action != CombatAction.CAST) {
            if (attacker.getCurrentPriestSpellLevel() > 0 && !attacker.status().has(Status.SILENCED) && Utils.RANDOM.nextInt(100) < 75) {
                spell = attacker.castPriestSpell();
                action = spell != null ? CombatAction.CAST : CombatAction.ATTACK;
            }
        }

        switch (action) {
            case ATTACK:
                Object def = pickMonster();
                if (def != null) {
                    if (def instanceof MutableMonster defender) {
                        boolean hit = Utils.attackHit(attacker, defender);
                        if (hit) {
                            for (Dice dice : attacker.getDamage()) {
                                int dmg = dice.roll();
                                if (attacker.getType() == ClassType.NINJA && Utils.RANDOM.nextInt(100) < 15) {
                                    dmg = defender.getMaxHitPoints();
                                }
                                damage(attacker, defender, dmg);
                            }
                        } else {
                            log(String.format("%s misses %s", attacker.name(), defender.name()));
                        }
                    } else {
                        boolean hit = Utils.attackHit(attacker, player);
                        if (hit) {
                            for (Dice dice : attacker.getDamage()) {
                                int dmg = dice.roll();
                                if (attacker.getType() == ClassType.NINJA && Utils.RANDOM.nextInt(100) < 15) {
                                    dmg = player.maxhp;
                                }
                                damage(attacker, player, dmg);
                            }
                        } else {
                            log(String.format("%s misses %s", attacker.name(), player.name.toUpperCase()));
                        }
                    }
                }
                break;
            case CAST: {
                log(String.format("%s casts %s", attacker.name(), spell), Color.SKY);
                Object def2 = pickMonster();
                if (def2 != null) {
                    if (def2 instanceof MutableMonster defender) {
                        spellCast(spell, attacker, defender, false);
                    } else {
                        spellCast(spell, attacker, player, false);
                    }
                }
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

    private Object pickMonster() {

        List<Object> shuffled = new ArrayList();
        for (MutableMonster m : monsters) {
            if (m.getCurrentHitPoints() > 0) {
                shuffled.add(m);
            }
        }
        shuffled.add(player);
        Collections.shuffle(shuffled);

        if (shuffled.isEmpty()) {
            return null;
        }

        return shuffled.get(0);
    }

    private void damage(Object attacker, Object defender, int damage) {

        if (damage <= 0) {
            return;
        }

        String attName = null;
        if (attacker instanceof Mutable) {
            Mutable a = (Mutable) attacker;
            attName = a.name();
        } else {
            attName = player.name.toUpperCase();
        }

        if (defender instanceof Mutable) {
            Mutable m = (Mutable) defender;
            m.adjustHitPoints(-damage);
            m.getHealthCursor().adjust(m.getCurrentHitPoints(), m.getMaxHitPoints());
            log(m.getDamageDescription(attName, damage), Color.SCARLET);
        } else {
            player.adjustHP(-damage);
            log(String.format("%s strikes %s who was hit for %d damage!", attName, player.name.toUpperCase(), damage), Color.RED);
        }
    }

    private void spellCast(Spells spell, Object caster, Object target, boolean item) {

        if (caster instanceof CharacterRecord) {
            if (player.isDisabled()) {
                log(player.name.toUpperCase() + " cannot cast spell in current state!");
                return;
            }

            if (!item) {
                if (!player.canCast(spell)) {
                    log(player.name.toUpperCase() + " does not have enough magic points!");
                    return;
                }
                player.decrMagicPts(spell);
            }

            log(player.name.toUpperCase() + " casts " + spell, Color.SKY);

        } else {
            Mutable m = (Mutable) caster;
            if (m.status().isDisabled()) {
                log(m.name() + " cannot cast spell in current state!");
                return;
            }
            log(m.name() + " casts " + spell, Color.SKY);

            m.decrementSpellPoints(spell);
        }

        switch (spell) {
            case MAKANITO:
            case LAKANITO:
            case LITOKAN:
            case LORTO:
            case MALIKTO:
            case MAHALITO:
            case MOLITO:
            case DALTO:
            case LAHALITO:
            case TILTOWAIT:
            case MADALTO:
                spellGroupDamage(caster, spell);
                break;
            case HALITO:
            case BADIAL:
            case BADIALMA:
            case ZILWAN:
            case BADIOS:
            case BADI:
                spellDamage(caster, spell, target);
                break;
            case MABADI:
                spellGroupMabadi(caster, spell);
                break;
            case KATINO:
                spellGroupAffect(caster, spell, Status.ASLEEP);
                break;
            case MANIFO:
                spellGroupAffect(caster, spell, Status.PARALYZED);
                break;
            case MONTINO:
                spellGroupAffect(caster, spell, Status.SILENCED);
                break;
            case DIOS:
            case DIAL:
            case DIALMA:
            case MADI:
                spellGroupHeal(caster, spell);
                break;
            case LATUMAPIC:
                //nothing - supposed to identify monsters
                break;
            case MOGREF:
            case SOPIC:
            case PORFIC:
                if (caster instanceof CharacterRecord) {
                    if (player.acmodifier1 == 0) {
                        player.acmodifier1 = spell.getHitBonus();
                    }
                } else {
                    Mutable m = (Mutable) caster;
                    m.setACModifier(m.getACModifier() + spell.getHitBonus());
                }
                break;
            case KALKI:
            case MATU:
            case BAMATU:
            case MASOPIC:
            case MAPORFIC:
                spellGroupACModify(caster, spell.getHitBonus());
                break;
            case DILTO:
            case MORLIS:
            case MAMORLIS:
                spellGroupEnemyACModify(caster, spell.getHitBonus());
                break;
            case DIALKO:
                player.status.set(Status.PARALYZED, 0);
                player.status.set(Status.ASLEEP, 0);
                for (MutableMonster m : player.summonedMonsters) {
                    m.status().set(Status.PARALYZED, 0);
                    m.status().set(Status.ASLEEP, 0);
                }
                break;
        }

    }

    private void spellDamage(Object caster, Spells spell, Object target) {
        if (target == null) {
            return;
        }
        if (caster instanceof MutableMonster || caster instanceof CharacterRecord) {
            MutableCharacter mc = (MutableCharacter) target;
            DoGooder dg = (DoGooder) mc.baseType();
            if (!mc.status().isDisabled() && dg.savingThrowSpell()) {
                log(dg.name + " made a saving throw versus " + spell + " and is unaffected!");
            } else {
                int dmg = spell.damage();
                damage(caster, mc, dmg);
            }
        }
        if (caster instanceof MutableCharacter) {
            if (target instanceof MutableMonster) {
                MutableMonster mm = (MutableMonster) target;
                boolean unaffected = mm.isUnaffected(spell, ((MutableCharacter) caster).getMonsterType());
                int dmg = spell.damage();
                if (unaffected) {
                    dmg = dmg / 2;
                }
                if (spell.equals(Spells.ZILWAN) && mm.getMonsterType() != CharacterType.UNDEAD) {
                    dmg = 0;
                }
                damage(caster, mm, dmg);

            } else {
                if (player.savingThrowSpell()) {
                    log(player.name.toUpperCase() + " made a saving throw versus " + spell + " and is unaffected!");
                } else {
                    int dmg = spell.damage();
                    damage(caster, player, dmg);
                }
            }
        }
    }

    private void spellGroupDamage(Object caster, Spells spell) {
        int groupDamage = spell.damage();
        if (caster instanceof MutableMonster || caster instanceof CharacterRecord) {
            java.util.Map<MutableCharacter, Integer> grpDamageMap = this.enemies.stream().collect(Collectors.toMap(key -> key, key -> 0));
            while (groupDamage > 0) {
                for (MutableCharacter mc : this.enemies) {
                    DoGooder dg = (DoGooder) mc.baseType();
                    CharacterType ct = caster instanceof MutableMonster ? ((MutableMonster) caster).getMonsterType() : CharacterType.valueOf(player.classType.toString());
                    boolean unaffected = mc.isUnaffected(spell, ct);
                    if (!mc.status().isDisabled() && (dg.savingThrowSpell() || unaffected)) {
                        log(dg.name + " made a saving throw versus " + spell + " and is unaffected!");
                    } else {
                        int dmg = grpDamageMap.get(mc);
                        grpDamageMap.put(mc, dmg + 1);
                    }
                    groupDamage--;
                }
            }
            for (MutableCharacter mc : grpDamageMap.keySet()) {
                int dmg = grpDamageMap.get(mc);
                if (dmg > 0) {
                    damage(caster, mc, dmg);
                }
            }
        }
        if (caster instanceof MutableCharacter) {
            java.util.Map<MutableMonster, Integer> grpDamageMap = monsters.stream().collect(Collectors.toMap(key -> key, key -> 0));
            int playerDmg = 0;
            while (groupDamage > 0) {
                for (MutableMonster mm : this.monsters) {
                    boolean unaffected = mm.isUnaffected(spell, ((MutableCharacter) caster).getMonsterType());
                    if (unaffected) {
                        log(mm.name() + " made a saving throw versus " + spell + " and is unaffected!");
                    } else {
                        int dmg = grpDamageMap.get(mm);
                        grpDamageMap.put(mm, dmg + 1);
                    }
                    groupDamage--;
                }
                if (player.armor != null && player.armor.id == 89 && (spell.equals(Spells.KATINO) || spell.equals(Spells.LAKANITO) || spell.equals(Spells.MAKANITO))) {
                    log(player.name.toUpperCase() + " was saved by oxygen mask versus " + spell + " and is unaffected!");
                } else if (player.savingThrowSpell()) {
                    log(player.name.toUpperCase() + " made a saving throw versus " + spell + " and is unaffected!");
                } else {
                    playerDmg++;
                }
                groupDamage--;
            }
            for (MutableMonster mm : grpDamageMap.keySet()) {
                int dmg = grpDamageMap.get(mm);
                if (dmg > 0) {
                    damage(caster, mm, dmg);
                }
            }
            if (playerDmg > 0) {
                damage(caster, player, playerDmg);
            }
        }
    }

    private void spellGroupHeal(Object caster, Spells spell) {
        if (caster instanceof MutableMonster || caster instanceof CharacterRecord) {
            for (MutableMonster mm : this.monsters) {
                if (!mm.isDead()) {
                    int dmg = spell.damage();
                    mm.adjustHitPoints(dmg);
                    mm.getHealthCursor().adjust(mm.getCurrentHitPoints(), mm.getMaxHitPoints());
                }
            }
            player.adjustHP(spell.damage());
        }
        if (caster instanceof MutableCharacter) {
            for (MutableCharacter mm : this.enemies) {
                if (!mm.isDead()) {
                    int dmg = spell.damage();
                    mm.adjustHitPoints(dmg);
                    mm.getHealthCursor().adjust(mm.getCurrentHitPoints(), mm.getMaxHitPoints());
                }
            }
        }
    }

    private void spellGroupMabadi(Object caster, Spells spell) {
        if (caster instanceof MutableMonster || caster instanceof CharacterRecord) {
            for (MutableCharacter mc : this.enemies) {
                DoGooder dg = (DoGooder) mc.baseType();
                CharacterType ct = caster instanceof MutableMonster ? ((MutableMonster) caster).getMonsterType() : CharacterType.valueOf(player.classType.toString());
                boolean unaffected = mc.isUnaffected(spell, ct);
                if (!mc.status().isDisabled() && (dg.savingThrowSpell() || unaffected)) {
                    log(dg.name + " made a saving throw versus " + spell + " and is unaffected!");
                } else {
                    int pointsLeft = Utils.getRandomBetween(1, 8);
                    mc.adjustHitPoints(mc.getCurrentHitPoints() - pointsLeft);
                    mc.getHealthCursor().adjust(mc.getCurrentHitPoints(), mc.getMaxHitPoints());
                }
            }
        }
        if (caster instanceof MutableCharacter) {
            for (MutableMonster mm : this.monsters) {
                boolean unaffected = mm.isUnaffected(spell, ((MutableCharacter) caster).getMonsterType());
                if (unaffected) {
                    log(mm.name() + " made a saving throw versus " + spell + " and is unaffected!");
                } else {
                    int pointsLeft = Utils.getRandomBetween(1, 8);
                    mm.adjustHitPoints(mm.getCurrentHitPoints() - pointsLeft);
                    mm.getHealthCursor().adjust(mm.getCurrentHitPoints(), mm.getMaxHitPoints());
                }
            }
            if (player.savingThrowSpell()) {
                log(player.name.toUpperCase() + " made a saving throw versus " + spell + " and is unaffected!");
            } else {
                int pointsLeft = Utils.getRandomBetween(1, 8);
                player.hp = pointsLeft;
                player.healthCursor.adjust(player.hp, player.maxhp);
            }
        }
    }

    private void spellGroupAffect(Object caster, Spells spell, Status effect) {
        if (caster instanceof MutableMonster || caster instanceof CharacterRecord) {
            for (MutableCharacter mc : this.enemies) {
                DoGooder dg = (DoGooder) mc.baseType();
                if (!mc.status().isDisabled() && dg.savingThrowSpell()) {
                    log(dg.name + " made a saving throw versus " + spell + " and is unaffected!");
                } else {
                    mc.status().set(effect, 1);
                }
            }
        }
        if (caster instanceof MutableCharacter) {
            for (MutableMonster mm : this.monsters) {
                boolean unaffected = mm.isUnaffected(spell, ((MutableCharacter) caster).getMonsterType());
                if (unaffected) {
                    log(mm.name() + " made a saving throw versus " + spell + " and is unaffected!");
                } else {
                    mm.status().set(effect, 1);
                }
            }
            if (player.savingThrowSpell()) {
                log(player.name.toUpperCase() + " made a saving throw versus " + spell + " and is unaffected!");
            } else {
                player.status.set(effect, 3);
            }
        }
    }

    private void spellGroupACModify(Object caster, int modifier) {
        if (caster instanceof MutableMonster || caster instanceof CharacterRecord) {
            for (MutableMonster mm : this.monsters) {
                mm.setACModifier(modifier);
            }
            player.acmodifier1 = modifier;
        }
        if (caster instanceof MutableCharacter) {
            for (MutableCharacter mm : this.enemies) {
                mm.setACModifier(modifier);
            }
        }
    }

    private void spellGroupEnemyACModify(Object caster, int modifier) {
        if (caster instanceof MutableMonster || caster instanceof CharacterRecord) {
            for (MutableCharacter mm : this.enemies) {
                mm.setACModifier(modifier);
            }
        }
        if (caster instanceof MutableCharacter) {
            for (MutableMonster mm : this.monsters) {
                mm.setACModifier(modifier);
            }
            player.acmodifier1 = modifier;
        }
    }

    private class SpellLabel extends Label {

        final Spells spell;
        final Item item;

        public SpellLabel(Spells spell) {
            super(spell.toString(), Andius.skin, "default-16");
            this.item = null;
            this.spell = spell;
        }

        public SpellLabel(Item it) {
            super(it.name, Andius.skin, "default-16");
            this.item = it;
            this.spell = null;
        }

        @Override
        public String toString() {
            return this.spell != null ? this.spell.label() : this.item.name + " - " + this.item.spell;
        }

    }

    private void addSpell(Spells s) {
        if (s.getArea() == SpellArea.COMBAT || s.getArea() == SpellArea.ANY_TIME) {
            SpellLabel label = new SpellLabel(s);
            spells.getItems().add(label);
        }
    }

    private void addSpell(Item i) {
        if (i.spell.getArea() == SpellArea.COMBAT || i.spell.getArea() == SpellArea.ANY_TIME) {
            SpellLabel label = new SpellLabel(i);
            spells.getItems().add(label);
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

    private void log(String s) {
        this.logs.add(s);
    }

    private void log(String s, Color c) {
        System.out.println(s);
        this.logs.add(s, c);
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
        public com.badlogic.gdx.utils.StringBuilder getText() {
            int[] ms = rec.magePoints;
            int[] cs = rec.clericPoints;
            return new com.badlogic.gdx.utils.StringBuilder(
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

        final Label l1;
        final PlayerStatusLabel l2;
        final PlayerMagicPointsLabel l3;
        final ListingBackground bckgrnd;
        final SaveGame.CharacterRecord c;

        PlayerListing(SaveGame.CharacterRecord rec) {
            this.c = rec;
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
        public com.badlogic.gdx.utils.StringBuilder getText() {
            return new com.badlogic.gdx.utils.StringBuilder(String.format("MG: %d  PR: %d ", mm.getCurrentMageSpellLevel(), mm.getCurrentPriestSpellLevel()));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

    }

    private class MonsterListing extends Group {

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

            this.l1 = new Label("", Andius.skin, "default-16");
            this.l2 = new MonsterStatusLabel(mm);
            this.l3 = new MonsterMagicPointsLabel(mm);

            String d1 = String.format("%s  LVL %d", m.name.toUpperCase(), mm.getLevel());
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

        }

    }

    private class DoGooderStatusLabel extends Label {

        private final MutableCharacter mm;

        public DoGooderStatusLabel(MutableCharacter mm) {
            super("", Andius.skin, "default-16");
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
            return mm.getCurrentHitPoints() <= 0 ? Color.SCARLET : mm.status().color();
        }

    }

    private class DoGooderMagicPointsLabel extends Label {

        private final MutableCharacter mm;

        public DoGooderMagicPointsLabel(MutableCharacter mm) {
            super("", Andius.skin, "default-16");
            this.mm = mm;
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.StringBuilder getText() {
            int[] ms = mm.getMageSpellLevels();
            int[] cs = mm.getPriestSpellLevels();
            return new com.badlogic.gdx.utils.StringBuilder(
                    String.format("M: %d %d %d %d %d %d %d  P: %d %d %d %d %d %d %d",
                            ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6], cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

    }

    private class DoGooderListing extends Group {

        final Image icon;
        final Label l1;
        final DoGooderStatusLabel l2;
        final DoGooderMagicPointsLabel l3;
        final ListingBackground bckgrnd;

        final DoGooder m;
        final MutableCharacter mm;

        DoGooderListing(MutableCharacter mm) {
            this.mm = mm;
            this.m = (DoGooder) mm.baseType();

            this.bckgrnd = new ListingBackground();
            mm.setHealthCursor(this.bckgrnd);
            this.bckgrnd.adjust(mm.getCurrentHitPoints(), mm.getMaxHitPoints());

            this.icon = new Image(iconAtlas.findRegion(this.m.iconID));

            this.l1 = new Label("", Andius.skin, "default-16");
            this.l2 = new DoGooderStatusLabel(mm);
            this.l3 = new DoGooderMagicPointsLabel(mm);

            String d1 = String.format("%s  %s  LVL %d  %s", m.name.toUpperCase(), m.race, mm.getLevel(), m.characterClass);
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
            this.icon.setPosition(247, 2);

            this.setSize(LISTING_WIDTH, LINE_HEIGHT * 3f);

            this.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selected.remove();
                    DoGooderListing.this.addActor(selected);
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

}
