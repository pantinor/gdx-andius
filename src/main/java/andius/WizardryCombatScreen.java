package andius;

import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import static andius.Andius.startScreen;
import andius.WizardryData.MazeCell;
import static andius.WizardryData.WER_ITEMS;
import andius.objects.Dice;
import andius.objects.HealthCursor;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.Mutable;
import andius.objects.MutableMonster;
import andius.objects.Reward;
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
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
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

public class WizardryCombatScreen implements Screen, Constants {

    public final Monster opponent;
    private final Stage stage;
    private final Batch batch;

    private final com.badlogic.gdx.scenes.scene2d.ui.List<SpellLabel> spells;
    private final com.badlogic.gdx.scenes.scene2d.ui.List<ActionLabel> actions;
    private final Table monstersTable;
    private final Table playersTable;
    private final AutoFocusScrollPane playersScroll;
    private final AutoFocusScrollPane actionsScroll;
    private final AutoFocusScrollPane monstersScroll;
    private final AutoFocusScrollPane spellsScroll;

    public final Context ctx;
    public final Map contextMap;

    public final List<MutableMonster> monsters = new ArrayList<>();
    public final List<CharacterRecord> players = new ArrayList<>();

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

    private int round = 1;
    private int suprised = 0;
    private MazeCell destCell, fromCell;
    private final boolean hasTreasure;

    public WizardryCombatScreen(Context context, Map contextMap, Monster opponent, int level, boolean hasTreasure, MazeCell destCell, MazeCell fromCell) {

        this.opponent = opponent;
        this.ctx = context;
        this.contextMap = contextMap;
        this.destCell = destCell;
        this.fromCell = fromCell;
        this.hasTreasure = hasTreasure;

        this.batch = new SpriteBatch();

        FrameMaker fm = new FrameMaker(SCREEN_WIDTH, SCREEN_HEIGHT);

        this.iconAtlas = new TextureAtlas(Gdx.files.classpath("assets/json/wizIcons.atlas"));

        Table logTable = new Table(Andius.skin);
        logTable.setBackground("log-background");
        this.logs = new LogScrollPane(Andius.skin, logTable, LOG_WIDTH);

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

        this.actions = new com.badlogic.gdx.scenes.scene2d.ui.List(Andius.skin, "default-16");
        this.actionsScroll = new AutoFocusScrollPane(this.actions, Andius.skin);
        this.actionsScroll.setScrollingDisabled(true, false);

        for (int i = 0; i < this.ctx.players().length; i++) {
            CharacterRecord p = this.ctx.players()[i];
            this.players.add(p);
            this.playersTable.add(new PlayerListing(i, p)).pad(3);
            this.playersTable.row();

            addAction(p);
        }

        addMonsters(level, opponent);

        for (MutableMonster mm : monsters) {
            this.monstersTable.add(new MonsterListing(mm)).pad(3);
            this.monstersTable.row();
        }

        this.spells = new com.badlogic.gdx.scenes.scene2d.ui.List(Andius.skin, "default-16");
        this.spellsScroll = new AutoFocusScrollPane(this.spells, Andius.skin);
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

        this.spells.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                PlayerListing pl = (PlayerListing) selectedPlayer.getParent();
                SpellLabel sl = spells.getSelected();
                if (pl != null && sl != null) {
                    if (sl.spell != null) {
                        setAction(pl.index, sl.spell);
                    }
                    if (sl.item != null) {
                        setAction(pl.index, sl.item);
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
                actions.getItems().clear();
                actions.clear();

                for (CharacterRecord p : ctx.players()) {
                    addAction(p);
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
                                log(String.format("%s gained %d experience points.", p.name.toUpperCase(), exp / lastMenStanding.size()));
                            }
                        }

                        if (WizardryCombatScreen.this.hasTreasure) {
                            mainGame.setScreen(new RewardScreen(WizardryCombatScreen.this.ctx, WizardryCombatScreen.this.contextMap, 1, exp, chestRewardId));
                        } else {
                            Reward gold = contextMap.scenario().rewards().get(goldRewardId);
                            int goldAmt = gold.goldAmount();
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
        fm.setBounds(this.actionsScroll, 10, 14, LISTING_WIDTH, 370);
        fm.setBounds(this.spellsScroll, 340, 530, 300, 220);
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

        Label l = new Label(this.opponent.name, Andius.skin, "default-16");
        l.setPosition(715, 730);
        this.stage.addActor(l);

        this.background = fm.build();

        if (Utils.percentChance(20)) {
            this.suprised = 1;
            log("You suprised " + this.opponent.name, Color.YELLOW);
        } else if (Utils.percentChance(20)) {
            this.suprised = 2;
            log("You were suprised by " + this.opponent.name, Color.YELLOW);
        } else {
            this.suprised = 0;
            log("You encounter " + this.opponent.name, Color.YELLOW);
        }

    }

    private void addMonsters(int level, Monster monster) {
        int maxGroups = Math.min(level + 1, 4);
        int numCreatures = monster.getGroupSize().roll();
        for (int i = 0; i < numCreatures; i++) {
            this.monsters.add(new MutableMonster(monster));
        }
        addPartners(monster, 1, maxGroups);
    }

    private void addPartners(Monster monster, int groupCount, int maxGroups) {
        if (groupCount > maxGroups) {
            return;
        }

        if (monster.getPartnerOdds() == 0) {
            return;
        }

        Monster partner = this.contextMap.scenario().monsters().get(monster.getPartnerID());

        int numPartners = 0;
        boolean hasPartner = Utils.RANDOM.nextInt(100) + 1 < monster.getPartnerOdds();
        if (hasPartner) {
            numPartners = partner.getGroupSize().roll();
        } else {
            return;
        }

        for (int i = 0; i < numPartners; i++) {
            this.monsters.add(new MutableMonster(this.contextMap.scenario().monsters().get(monster.getPartnerID())));
        }

        addPartners(partner, groupCount + 1, maxGroups);
    }

    private void fight() {

        List<Object> shuffled = new ArrayList();

        if (this.suprised == 1) {
            shuffled.addAll(this.players);
            shuffled.addAll(this.monsters);
        } else if (this.suprised == 2) {
            shuffled.addAll(this.monsters);
            shuffled.addAll(this.players);
        } else {
            shuffled.addAll(this.monsters);
            shuffled.addAll(this.players);
            Collections.shuffle(shuffled);
        }

        this.suprised = 0;

        for (Object m : shuffled) {
            if (m instanceof CharacterRecord player) {
                ActionLabel action = getAction(player);

                MutableMonster defender = pickMonster();
                if (action.target != null) {
                    defender = action.target;
                }

                if (defender != null) {
                    if (!player.isDisabled()) {

                        if (action.spell != null || action.item != null) {
                            Spells spell = action.spell != null ? action.spell : action.item.spell;
                            spellCast(spell, player, defender, action.item != null);
                            if (action.item != null && action.item.changeChance > 0) {
                                boolean decayed = Utils.percentChance(action.item.changeChance);
                                if (decayed) {
                                    Item changeTo = WER_ITEMS.get(action.item.changeTo);
                                    player.removeItem(action.item.id, action.item.scenarioID);
                                    if (changeTo.id != 0) {
                                        player.inventory.add(changeTo);
                                    }
                                    //this.spells.getItems().removeValue(action, false);
                                    //this.spells.getSelection().clear();
                                }
                            }
                        } else {
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
                }
            } else if (m instanceof MutableMonster mm) {
                monsterFight(mm);
            }
        }
        
        this.ctx.endTurn();

        boolean alive = false;
        for (MutableMonster c : this.monsters) {
            if (!c.isDead()) {
                alive = true;
            }
        }

        if (!alive) {
            end(false);
        } else if (this.ctx.allDead()) {
            end(false);
        }

        log("------------ end of round " + round, Color.YELLOW);
        round++;

    }

    public void end(boolean fled) {

        for (CharacterRecord p : this.ctx.players()) {
            p.acmodifier1 = 0;
        }

        this.fight.remove();
        this.reset.remove();
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
                for (CharacterRecord defender : players) {
                    int dmg = attacker.getCurrentHitPoints() / 2;
                    if (defender.savingThrowBreath()) {
                        log(String.format("%s made a saving throwing throw against %s", defender.name, attacker.breath()));
                        dmg = dmg / 2;
                    }
                    damage(attacker, defender, dmg);
                }
                break;
            case ATTACK:
                CharacterRecord defender = pickPlayer();
                if (defender != null) {
                    boolean hit = Utils.attackHit(attacker, defender);
                    if (hit) {
                        for (Dice dice : attacker.getDamage()) {
                            int dmg = dice.roll();
                            damage(attacker, defender, dmg);
                        }
                    } else {
                        log(String.format("%s misses %s", attacker.name(), defender.name));
                    }
                }
                break;
            case CAST: {
                CharacterRecord target = pickPlayer();
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

    private CharacterRecord pickPlayer() {

        List<CharacterRecord> shuffled = new ArrayList();
        for (CharacterRecord p : players) {
            if (!p.isDead()) {
                shuffled.add(p);
            }
        }
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
        if (attacker instanceof MutableMonster a) {
            attName = a.name();
        } else if (attacker instanceof CharacterRecord p) {
            attName = p.name.toUpperCase();
        }

        if (defender instanceof MutableMonster m) {
            m.adjustHitPoints(-damage);
            m.getHealthCursor().adjust(m.getCurrentHitPoints(), m.getMaxHitPoints());
            log(m.getDamageDescription(attName, damage), Color.SCARLET);
        } else if (defender instanceof CharacterRecord p) {
            p.adjustHP(-damage);
            log(String.format("%s strikes %s who was hit for %d damage!", attName, p.name.toUpperCase(), damage), Color.RED);
        }
    }

    private void spellCast(Spells spell, Object caster, Object target, boolean item) {

        if (caster instanceof CharacterRecord p) {
            if (p.isDisabled()) {
                log(p.name.toUpperCase() + " cannot cast spell in current state!");
                return;
            }

            if (!item) {
                if (!p.canCast(spell)) {
                    log(p.name.toUpperCase() + " does not have enough magic points!");
                    return;
                }
                p.decrMagicPts(spell);
            }

            log(p.name.toUpperCase() + " casts " + spell, Color.SKY);

        } else if (caster instanceof MutableMonster m) {
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
            case HAMAN:
            case MAHAMAN:
                //TODO - randomized affects
                break;
            case LATUMAPIC:
                //nothing - supposed to identify monsters
                break;
            case MOGREF:
            case SOPIC:
            case PORFIC:
                if (caster instanceof CharacterRecord p) {
                    if (p.acmodifier1 == 0) {
                        p.acmodifier1 = spell.getHitBonus();
                    }
                } else {
                    Mutable m = (MutableMonster) caster;
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
                for (CharacterRecord p : players) {
                    p.status.set(Status.PARALYZED, 0);
                    p.status.set(Status.ASLEEP, 0);
                }
                for (MutableMonster m : monsters) {
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
        if (caster instanceof CharacterRecord p) {
            MutableMonster mm = (MutableMonster) target;
            boolean unaffected = mm.isUnaffected(spell, CharacterType.valueOf(p.classType.toString()));
            int dmg = spell.damage();
            if (unaffected) {
                dmg = dmg / 2;
            }
            if (spell.equals(Spells.ZILWAN) && mm.getMonsterType() != CharacterType.UNDEAD) {
                dmg = 0;
            }
            damage(caster, mm, dmg);
        }
        if (caster instanceof MutableMonster) {
            CharacterRecord p = (CharacterRecord) target;
            if (p.savingThrowSpell()) {
                log(p.name.toUpperCase() + " made a saving throw versus " + spell + " and is unaffected!");
            } else {
                int dmg = spell.damage();
                damage(caster, p, dmg);
            }
        }
    }

    private void spellGroupDamage(Object caster, Spells spell) {
        int groupDamage = spell.damage();
        if (caster instanceof CharacterRecord p) {
            while (groupDamage > 0) {
                for (MutableMonster mm : this.monsters) {
                    boolean unaffected = mm.isUnaffected(spell, CharacterType.valueOf(p.classType.toString()));
                    if (!mm.status().isDisabled() && unaffected) {
                        log(mm.name() + " made a saving throw versus " + spell + " and is unaffected!");
                    } else {
                        damage(caster, mm, 1);
                    }
                    groupDamage--;
                }
            }
        }
        if (caster instanceof MutableMonster) {
            while (groupDamage > 0) {
                for (CharacterRecord p : this.players) {
                    if (p.armor != null && p.armor.id == 89 && (spell.equals(Spells.KATINO) || spell.equals(Spells.LAKANITO) || spell.equals(Spells.MAKANITO))) {
                        log(p.name.toUpperCase() + " was saved by oxygen mask versus " + spell + " and is unaffected!");
                    } else if (p.savingThrowSpell()) {
                        log(p.name.toUpperCase() + " made a saving throw versus " + spell + " and is unaffected!");
                    } else {
                        damage(caster, p, 1);
                    }
                    groupDamage--;
                }
            }
        }
    }

    private void spellGroupHeal(Object caster, Spells spell) {
        if (caster instanceof MutableMonster) {
            for (MutableMonster mm : this.monsters) {
                if (!mm.isDead()) {
                    int dmg = spell.damage();
                    mm.adjustHitPoints(dmg);
                    mm.getHealthCursor().adjust(mm.getCurrentHitPoints(), mm.getMaxHitPoints());
                }
            }
        }
        if (caster instanceof CharacterRecord) {
            for (CharacterRecord p : this.players) {
                if (!p.isDead()) {
                    p.adjustHP(spell.damage());
                }
            }
        }
    }

    private void spellGroupMabadi(Object caster, Spells spell) {
        if (caster instanceof MutableMonster) {
            for (CharacterRecord p : this.players) {
                if (p.savingThrowSpell()) {
                    log(p.name.toUpperCase() + " made a saving throw versus " + spell + " and is unaffected!");
                } else {
                    int pointsLeft = Utils.getRandomBetween(1, 8);
                    p.hp = pointsLeft;
                    p.healthCursor.adjust(p.hp, p.maxhp);
                }
            }
        }
        if (caster instanceof CharacterRecord p) {
            for (MutableMonster mm : this.monsters) {
                boolean unaffected = mm.isUnaffected(spell, CharacterType.valueOf(p.classType.toString()));
                if (unaffected) {
                    log(mm.name() + " made a saving throw versus " + spell + " and is unaffected!");
                } else {
                    int pointsLeft = Utils.getRandomBetween(1, 8);
                    mm.adjustHitPoints(mm.getCurrentHitPoints() - pointsLeft);
                    mm.getHealthCursor().adjust(mm.getCurrentHitPoints(), mm.getMaxHitPoints());
                }
            }
        }
    }

    private void spellGroupAffect(Object caster, Spells spell, Status effect) {
        if (caster instanceof MutableMonster) {
            for (CharacterRecord p : this.players) {
                if (p.savingThrowSpell()) {
                    log(p.name.toUpperCase() + " made a saving throw versus " + spell + " and is unaffected!");
                } else {
                    p.status.set(effect, 3);
                }
            }
        }
        if (caster instanceof CharacterRecord p) {
            for (MutableMonster mm : this.monsters) {
                boolean unaffected = mm.isUnaffected(spell, CharacterType.valueOf(p.classType.toString()));
                if (unaffected) {
                    log(mm.name() + " made a saving throw versus " + spell + " and is unaffected!");
                } else {
                    mm.status().set(effect, 1);
                }
            }
        }
    }

    private void spellGroupACModify(Object caster, int modifier) {
        if (caster instanceof CharacterRecord) {
            for (CharacterRecord p : this.players) {
                p.acmodifier1 = modifier;
            }
        }
        if (caster instanceof MutableMonster) {
            for (MutableMonster mm : this.monsters) {
                mm.setACModifier(modifier);
            }
        }
    }

    private void spellGroupEnemyACModify(Object caster, int modifier) {
        if (caster instanceof MutableMonster) {
            for (CharacterRecord p : this.players) {
                p.acmodifier1 = modifier;
            }
        }
        if (caster instanceof CharacterRecord) {
            for (MutableMonster mm : this.monsters) {
                mm.setACModifier(modifier);
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

    private class ActionLabel extends Label {

        final CharacterRecord player;
        Spells spell;
        Item item;
        MutableMonster target;

        public ActionLabel(CharacterRecord player) {
            super("", Andius.skin, "default-16");
            this.player = player;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.player.name.toUpperCase()).append(" - ");
            if (this.spell != null) {
                sb.append(this.spell).append(" - ");
            }
            if (this.item != null) {
                sb.append(this.item.spell).append(" - ");
            }
            if (this.target != null) {
                Monster m = (Monster) this.target.baseType();
                sb.append(m.name.toUpperCase());
            } else {
                sb.append("ANY");
            }
            return sb.toString();
        }

    }

    private void addAction(CharacterRecord player) {
        ActionLabel label = new ActionLabel(player);
        actions.getItems().add(label);
    }

    private void setAction(int index, Spells s) {
        ActionLabel al = this.actions.getItems().get(index);
        if (al != null && (s.getArea() == SpellArea.COMBAT || s.getArea() == SpellArea.ANY_TIME)) {
            al.spell = s;
            al.item = null;
        }
    }

    private void setAction(int index, Item i) {
        ActionLabel al = this.actions.getItems().get(index);
        if (al != null && (i.spell.getArea() == SpellArea.COMBAT || i.spell.getArea() == SpellArea.ANY_TIME)) {
            al.item = i;
            al.spell = null;
        }
    }

    private void setAction(int index, MutableMonster target) {
        ActionLabel al = this.actions.getItems().get(index);
        al.target = target;
    }

    private ActionLabel getAction(CharacterRecord player) {
        for (int i = 0; i < this.actions.getItems().size; i++) {
            ActionLabel al = this.actions.getItems().get(i);
            if (al.player == player) {
                return al;
            }
        }
        return null;
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

                    spells.getItems().clear();

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
                iconAtlas.findRegion("0");
            }

            this.icon = new Image(ar);

            this.l1 = new Label("", Andius.skin, "default-16");
            this.l2 = new MonsterStatusLabel(mm);
            this.l3 = new MonsterMagicPointsLabel(mm);
            this.icon.setPosition(247, 2);

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

}
