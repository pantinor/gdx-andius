package andius;

import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import static andius.Andius.startScreen;
import static andius.WizardryData.WER4_CHARS;
import static andius.WizardryData.WER_ITEMS;
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
import com.badlogic.gdx.Input;
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
    private boolean suprised = false;

    public Wiz4CombatScreen(CharacterRecord player, List<MutableMonster> monsters, DoGooder opponent) {

        this.opponent = opponent;
        this.player = player;
        this.monsters = monsters;
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
                end();
            }
        });
        this.flee.setBounds(x += 100, 426, 80, 40);

        fm.setBounds(this.monstersScroll, 10, 14, LISTING_WIDTH, TABLE_HEIGHT);
        fm.setBounds(this.spellsScroll, 340, 530, 200, 220);
        fm.setBounds(this.logs, 326, 14, LOG_WIDTH, LOG_HEIGHT);
        fm.setBounds(this.enemiesScroll, 712, 14, LISTING_WIDTH, TABLE_HEIGHT);

        this.stage.addActor(this.fight);
        this.stage.addActor(this.flee);
        this.stage.addActor(this.cast);
        this.stage.addActor(this.logs);

        this.stage.addActor(this.monstersScroll);
        this.stage.addActor(this.enemiesScroll);
        this.stage.addActor(this.spellsScroll);

        this.background = fm.build();

        this.suprised = !Utils.percentChance(16);

        if (this.suprised) {
            if (!this.opponent.slogan.isEmpty()) {
                log("You were suprised by " + this.opponent.slogan.replace("|", " ... "));
            } else {
                log("You were suprised by " + this.opponent.name);
            }
            for (MutableCharacter mm : enemies) {
                enemyFight(mm);
            }
        } else {
            if (!this.opponent.slogan.isEmpty()) {
                log("You are about to battle " + this.opponent.slogan.replace("|", " ... "));
            } else {
                log("You are about to battle " + this.opponent.name);
            }
        }

    }

    private void cast() {

        List<Mutable> shuffled = new ArrayList();
        shuffled.addAll(this.monsters);
        shuffled.addAll(this.enemies);
        Collections.shuffle(shuffled);

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
                if (sp.item != null && sp.item.genericName.equals("POTION")) {
                    this.spells.getItems().removeValue(sp, false);
                    player.removeItem(sp.item.id, sp.item.scenarioID);
                } else if (sp.item != null && sp.item.changeChance > 0) {
                    boolean decayed = Utils.percentChance(sp.item.changeChance);
                    if (decayed) {
                        Item changeTo = WER_ITEMS.get(sp.item.changeTo);
                        player.removeItem(sp.item.id, sp.item.scenarioID);
                        player.inventory.add(changeTo);
                        this.spells.getItems().removeValue(sp, false);
                    }
                }
            }
            if (player.isDead()) {
                end();
            }
        } else {
            end();
        }

        for (Mutable m : shuffled) {
            if (m instanceof MutableMonster) {
                monsterFight((MutableMonster) m);
            } else {
                enemyFight((MutableCharacter) m);
            }
        }

        log("------------ end of round " + round);
        round++;
    }

    private void fight() {

        List<Mutable> shuffled = new ArrayList();
        shuffled.addAll(this.monsters);
        shuffled.addAll(this.enemies);
        Collections.shuffle(shuffled);

        MutableCharacter defender = pickEnemy();
        if (defender != null) {
            if (!player.isDisabled()) {
                boolean hit = Utils.attackHit(player, defender);
                if (hit) {
                    Item weapon = player.weapon == null ? Item.HANDS : player.weapon;
                    int damage = Utils.dealDamage(weapon, defender);
                    log(String.format("%s %s %s, who %s after %d damage.",
                            player.name.toUpperCase(),
                            HITMSGS[Utils.RANDOM.nextInt(HITMSGS.length)],
                            defender.name(),
                            defender.getDamageTag(),
                            damage));
                } else {
                    log(String.format("%s misses %s", player.name.toUpperCase(), defender.name()));
                }
            }
            if (player.isDead()) {
                end();
            }
        } else {
            end();
        }

        for (Mutable m : shuffled) {
            if (m instanceof MutableMonster) {
                monsterFight((MutableMonster) m);
            } else {
                enemyFight((MutableCharacter) m);
            }
        }

        log("------------ end of round " + round);
        round++;

    }

    public void end() {
        player.acmodifier1 = 0;

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

    private void monsterFight(MutableMonster attacker) {

        attacker.processStatusAffects();

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
                    log(String.format("%s casts %s", attacker.name(), spell));
                    spellCast(spell, attacker, target, false);
                }
                break;
            }
        }

    }

    private void enemyFight(MutableCharacter attacker) {

        attacker.processStatusAffects();

        if (attacker.isDead() || attacker.status().isDisabled()) {
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

        int roll = Utils.RANDOM.nextInt(100);

        switch (action) {
            case ATTACK:
                log(String.format("%s is attacking!", attacker.name()));
                MutableMonster defender = pickMonster();
                if (roll > 15 && defender != null) {
                    boolean hit = Utils.attackHit(attacker, defender);
                    if (hit) {
                        for (Dice dice : attacker.getDamage()) {
                            int dmg = dice.roll();
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
                            damage(attacker, player, dmg);
                        }
                    } else {
                        log(String.format("%s misses %s", attacker.name(), player.name.toUpperCase()));
                    }
                }
                break;
            case CAST: {
                MutableMonster target = pickMonster();
                log(String.format("%s casts %s", attacker.name(), spell));
                if (roll > 15 && target != null) {
                    spellCast(spell, attacker, target, false);
                } else {
                    spellCast(spell, attacker, player, false);
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
            log(String.format("%s strikes %s who was hit for %d damage!", attName, m.name(), damage));
        } else {
            player.adjustHP(-damage);
            player.healthCursor.adjust(player.hp, player.maxhp);
            log(String.format("%s strikes %s who was hit for %d damage!", attName, player.name.toUpperCase(), damage));
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
                    log(player.name.toUpperCase() + " dost not have enough magic points!");
                    return;
                }
                player.decrMagicPts(spell);
            }

            log(player.name.toUpperCase() + " casts " + spell);

        } else {
            Mutable m = (Mutable) caster;
            if (m.status().isDisabled()) {
                log(m.name() + " cannot cast spell in current state!");
                return;
            }
            log(m.name() + " casts " + spell);

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
            case DILTO:
            case MORLIS:
            case MAMORLIS:
            case KALKI:
            case MATU:
            case BAMATU:
            case MASOPIC:
            case MAPORFIC:
                spellGroupACModify(caster, spell);
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
            if (dg.savingThrowSpell()) {
                log(dg.name + " made a saving throw versus " + spell + " and is unaffected!");
            } else {
                int dmg = spell.damage();
                damage(caster, mc, dmg);
            }
        }
        if (caster instanceof MutableCharacter) {
            if (target instanceof MutableMonster) {
                MutableMonster mm = (MutableMonster) target;
                if (Utils.RANDOM.nextInt(100) < mm.getUnaffected()) {
                    log(mm.name() + " made a saving throw versus " + spell + " and is unaffected!");
                } else {
                    int dmg = spell.damage();
                    damage(caster, mm, dmg);
                }
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
            while (groupDamage > 0) {
                for (MutableCharacter mc : this.enemies) {
                    DoGooder dg = (DoGooder) mc.baseType();
                    if (dg.savingThrowSpell()) {
                        log(dg.name + " made a saving throw versus " + spell + " and is unaffected!");
                    } else {
                        damage(caster, mc, 1);
                    }
                    groupDamage--;
                }
            }
        }
        if (caster instanceof MutableCharacter) {
            while (groupDamage > 0) {
                for (MutableMonster mm : this.monsters) {
                    if (Utils.RANDOM.nextInt(100) < mm.getUnaffected()) {
                        log(mm.name() + " made a saving throw versus " + spell + " and is unaffected!");
                    } else {
                        damage(caster, mm, 1);
                    }
                    groupDamage--;
                }
                if (player.savingThrowSpell()) {
                    log(player.name.toUpperCase() + " made a saving throw versus " + spell + " and is unaffected!");
                } else {
                    damage(caster, player, 1);
                }
                groupDamage--;
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
            player.healthCursor.adjust(player.hp, player.maxhp);
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
                if (dg.savingThrowSpell()) {
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
                if (Utils.RANDOM.nextInt(100) < mm.getUnaffected()) {
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
                if (dg.savingThrowSpell()) {
                    log(dg.name + " made a saving throw versus " + spell + " and is unaffected!");
                } else {
                    log(dg.name + " is " + effect);
                    mc.status().set(effect, 4);
                }
            }
        }
        if (caster instanceof MutableCharacter) {
            for (MutableMonster mm : this.monsters) {
                if (Utils.RANDOM.nextInt(100) < mm.getUnaffected()) {
                    log(mm.name() + " made a saving throw versus " + spell + " and is unaffected!");
                } else {
                    log(mm.name() + " is " + effect);
                    mm.status().set(effect, 4);
                }
            }
            if (player.savingThrowSpell()) {
                log(player.name.toUpperCase() + " made a saving throw versus " + spell + " and is unaffected!");
            } else {
                log(player.name.toUpperCase() + " is " + effect);
                player.status.set(effect, 4);
            }
        }
    }

    private void spellGroupACModify(Object caster, Spells spell) {
        if (caster instanceof MutableMonster || caster instanceof CharacterRecord) {
            for (MutableMonster mm : this.monsters) {
                if (mm.getACModifier() == 0) {
                    mm.setACModifier(spell.getHitBonus());
                }
            }
            if (player.acmodifier1 == 0) {
                player.acmodifier1 = spell.getHitBonus();
            }
        }
        if (caster instanceof MutableCharacter) {
            for (MutableCharacter mm : this.enemies) {
                if (mm.getACModifier() == 0) {
                    mm.setACModifier(spell.getHitBonus());
                }
            }
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
            return this.spell != null ? this.spell.toString() : this.item.name + " - " + this.item.spell;
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

    public final void log(String s) {
        this.logs.add(s);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(this.background, 0, 0);
        batch.end();

        stage.act();
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            fight();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            end();
        }

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
            return rec.isDead() ? Color.RED : rec.status.color();
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
            return mm.getCurrentHitPoints() <= 0 ? Color.RED : mm.status().color();
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
            return mm.getCurrentHitPoints() <= 0 ? Color.RED : mm.status().color();
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
