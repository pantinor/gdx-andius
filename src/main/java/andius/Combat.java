package andius;

import andius.Constants.Map;
import andius.objects.Dice;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.Mutable;
import andius.objects.MutableMonster;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Sound;
import andius.objects.SpellUtil;
import andius.objects.Spells;
import static andius.objects.Spells.*;
import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import utils.Loggable;
import utils.Utils;
import static utils.Utils.RANDOM;

public abstract class Combat implements Constants {

    public final Monster opponent;
    public final Context ctx;
    public final Map contextMap;
    public final List<MutableMonster> monsters = new ArrayList<>();
    public final List<CharacterRecord> players = new ArrayList<>();
    public final List<Action> actions = new ArrayList<>();

    private int round = 1;
    private int suprised = 0;
    private Loggable logs;

    public Combat(Context context, Map contextMap, Monster opponent, int level) {

        this.opponent = opponent;
        this.ctx = context;
        this.contextMap = contextMap;

        for (int i = 0; i < this.ctx.players().length; i++) {
            CharacterRecord p = this.ctx.players()[i];
            this.players.add(p);
            addAction(p);
        }

        addMonsters(level, opponent);

        if (Utils.percentChance(20)) {
            this.suprised = 1;
        } else if (Utils.percentChance(20)) {
            this.suprised = 2;
        } else {
            this.suprised = 0;
        }
    }

    public void setLogs(Loggable logs) {
        this.logs = logs;
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
        boolean hasPartner = Utils.RANDOM.nextInt(100) + 1 <= monster.getPartnerOdds();
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

    public void fight() {

        List<Object> shuffled = new ArrayList<>();

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
                Action action = getAction(player);

                if (!player.isDisabled()) {

                    if (action.spell != null || action.item != null) {
                        Spells spell = action.spell != null ? action.spell : action.item.spell;
                        Object spellTarget = null;

                        switch (spell.getTarget()) {
                            case PERSON:
                                spellTarget = action.playerTarget != null ? action.playerTarget : player;
                                break;
                            case CASTER:
                                spellTarget = player;
                                break;
                            case MONSTER:
                                spellTarget = action.monsterTarget;
                                if (!(spellTarget instanceof MutableMonster mm) || mm.isDead()) {
                                    spellTarget = pickMonster();
                                }
                                break;
                            case PARTY:
                            case GROUP:
                            case VARIABLE:
                            case NONE:
                            default:
                                spellTarget = null;
                                break;
                        }

                        spellCast(spell, player, spellTarget, action.item != null);

                        if (action.item != null && action.item.changeChance > 0) {
                            boolean decayed = Utils.percentChance(action.item.changeChance);
                            if (decayed) {
                                Item changeTo = this.contextMap.scenario().items().get(action.item.changeTo);
                                player.removeItem(action.item.id, action.item.scenarioID);
                                if (changeTo.id != 0) {
                                    player.inventory.add(changeTo);
                                }
                            }
                        }

                    } else if (action.dispel) {
                        int penalty = dispelPenalty(player);
                        if (penalty != Integer.MAX_VALUE) {
                            List<MutableMonster> dispelled = new ArrayList<>();
                            for (MutableMonster mon : monsters) {
                                if (mon.getCurrentHitPoints() > 0 && mon.getMonsterType() == CharacterType.UNDEAD) {
                                    int chance = 50 + (player.level * 5) - (mon.getLevel() * 10) - penalty;
                                    chance = Math.max(0, Math.min(100, chance));
                                    int roll = RANDOM.nextInt(100) + 1;

                                    if (roll <= chance) {
                                        log(String.format("%s dispels %s", player.name.toUpperCase(), mon.name()), Color.SKY);
                                        dispelled.add(mon);
                                    } else {
                                        log(String.format("%s failed to dispel %s", player.name.toUpperCase(), mon.name()), Color.WHITE);
                                    }
                                }
                            }
                            for (MutableMonster mon : dispelled) {
                                removeMonster(mon);
                            }
                        } else {
                            log(String.format("%s cannot dispel undead", player.name.toUpperCase()), Color.WHITE);
                        }
                    } else {

                        int playerIndex = players.indexOf(player);
                        if (playerIndex < 0 || playerIndex >= 3) {
                            log(String.format("%s cannot reach the monsters", player.name.toUpperCase()), Color.WHITE);
                            continue;
                        }

                        MutableMonster defender = action.monsterTarget;

                        if (defender == null || defender.isDead()) {
                            defender = pickMonster();
                        }

                        if (defender != null) {
                            int hpDamage = 0;
                            int hitsCount = 0;
                            Item weapon = player.weapon == null ? Item.HANDS : player.weapon;

                            for (int i = 0; i < player.extraSwings(); i++) {
                                if (defender.isDead()) {
                                    break;
                                }
                                boolean hit = Utils.attackHit(player, defender);
                                if (hit) {
                                    int dealt = Utils.dealDamage(player, weapon, defender);
                                    if (dealt > 0) {
                                        hitsCount++;
                                        hpDamage += dealt;
                                    }
                                }
                            }

                            if (hpDamage > 0) {
                                log(String.format("%s hit %s [%d] %dx %s.",
                                        player.name.toUpperCase(),
                                        defender.name().toUpperCase(),
                                        hpDamage,
                                        hitsCount,
                                        weapon.name), Color.SCARLET);
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
        for (MutableMonster m : this.monsters) {
            if (!m.isDead()) {
                alive = true;
                break;
            }
        }

        if (!alive) {
            end(false);
            return;
        } else if (this.ctx.allDead()) {
            end(false);
            return;
        }

        log("------------ end of round " + round, Color.YELLOW);
        round++;
    }

    private int dispelPenalty(CharacterRecord player) {
        switch (player.classType) {
            case PRIEST:
                return 0;
            case BISHOP:
                return player.level >= 4 ? 20 : Integer.MAX_VALUE;
            case LORD:
                return player.level >= 9 ? 40 : Integer.MAX_VALUE;
            default:
                return Integer.MAX_VALUE;
        }
    }

    public void end(boolean fled) {
        for (CharacterRecord p : this.ctx.players()) {
            p.acmodifier1 = 0;
        }
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

        boolean silenced = attacker.status().has(Status.SILENCED);

        if (!silenced) {
            int mlvl = attacker.getCurrentMageSpellLevel();
            if (mlvl > 0 && Utils.RANDOM.nextInt(100) < 75) {
                action = CombatAction.CAST;
                spell = attacker.castMageSpell();
            }
        }

        if (action == CombatAction.ATTACK && !silenced) {
            int plvl = attacker.getCurrentPriestSpellLevel();
            if (plvl > 0 && Utils.RANDOM.nextInt(100) < 75) {
                action = CombatAction.CAST;
                spell = attacker.castPriestSpell();
            }
        }

        if (action == CombatAction.ATTACK
                && !silenced
                && attacker.breath() != Breath.NONE
                && Utils.RANDOM.nextInt(100) < 60) {
            action = CombatAction.BREATH;
        }

        if (action == CombatAction.ATTACK
                && !silenced
                && attacker.monster().ability.contains(Ability.CALLFORHELP)
                && Utils.shouldCallForHelp(attacker, this.monsters)) {
            action = CombatAction.CALL_FOR_HELP;
        } else if (action == CombatAction.ATTACK
                && attacker.monster().ability.contains(Ability.RUN)
                && Utils.shouldFlee(attacker)) {
            action = CombatAction.FLEE;
        }

        switch (action) {
            case BREATH:
                log(String.format("%s breathes %s", attacker.name(), attacker.breath()), Color.BROWN);
                int baseBreathDamage = attacker.getCurrentHitPoints();
                for (CharacterRecord defender : players) {
                    if (defender.isDisabled()) {
                        continue;
                    }
                    int dmg = baseBreathDamage;
                    if (defender.savingThrowBreath()) {
                        log(String.format("%s saves versus %s", defender.name.toUpperCase(), attacker.breath()));
                        dmg /= 2;
                    }
                    damage(attacker, defender, dmg, attacker.breath().toString());
                }
                break;

            case ATTACK:
                CharacterRecord defender = pickFrontPlayer();
                if (defender != null) {
                    boolean hit = Utils.attackHit(attacker, defender);
                    if (hit) {
                        for (Dice dice : attacker.getDamage()) {
                            int dmg = dice.roll();
                            damage(attacker, defender, dmg, Utils.getAttackName(attacker));
                            if (defender.isDead()) {
                                break;
                            }
                        }
                        if (!defender.isDead()) {
                            Utils.applyAttackSpecialEffects(attacker, defender, logs);
                        }
                    } else {
                        log(String.format("%s misses %s", attacker.name(), defender.name));
                    }
                }
                break;

            case CAST: {
                Object target = null;

                if (spell == Spells.DIOS || spell == Spells.DIAL || spell == Spells.DIALMA
                        || spell == Spells.MADI || spell == Spells.MOGREF || spell == Spells.SOPIC || spell == Spells.PORFIC) {
                    if (spell == Spells.DIOS || spell == Spells.DIAL || spell == Spells.DIALMA || spell == Spells.MADI) {
                        MutableMonster weakestAlly = null;
                        for (MutableMonster mm : this.monsters) {
                            if (!mm.isDead()) {
                                if (weakestAlly == null
                                        || mm.getCurrentHitPoints() < weakestAlly.getCurrentHitPoints()) {
                                    weakestAlly = mm;
                                }
                            }
                        }
                        if (weakestAlly != null
                                && weakestAlly.getCurrentHitPoints() < weakestAlly.getMaxHitPoints() / 2) {
                            target = weakestAlly;
                        } else {
                            target = pickPlayer();
                        }
                        playSound(Sound.HEALING);
                    } else {
                        target = attacker;
                    }
                } else {
                    target = pickPlayer();
                }

                if (target != null) {
                    spellCast(spell, attacker, target, false);
                }
                break;
            }

            case FLEE:
                log(String.format("%s fled!", attacker.name()), Color.SKY);
                removeMonster(attacker);
                playSound(Sound.FLEE);
                break;

            case CALL_FOR_HELP:
                log(String.format("%s called for help!", attacker.name()), Color.GOLDENROD);
                MutableMonster added = new MutableMonster(attacker.monster());
                addMonster(added);
                playSound(Sound.GIGGLE);
                break;
        }
    }

    public MutableMonster pickMonster() {
        MutableMonster weakestMonster = null;
        for (MutableMonster m : monsters) {
            if (m.getCurrentHitPoints() > 0) {
                if (weakestMonster == null || m.getCurrentHitPoints() < weakestMonster.getCurrentHitPoints()) {
                    weakestMonster = m;
                }
            }
        }
        return weakestMonster;
    }

    // classic wizardry melee is front-line limited
    public CharacterRecord pickFrontPlayer() {
        List<CharacterRecord> front = new ArrayList<>();

        for (int i = 0; i < Math.min(3, players.size()); i++) {
            CharacterRecord p = players.get(i);
            if (!p.isDead()) {
                front.add(p);
            }
        }

        if (!front.isEmpty()) {
            return front.get(RANDOM.nextInt(front.size()));
        }

        return pickPlayer();
    }

    // For spells or effects that may target anyone in the party.
    public CharacterRecord pickPlayer() {
        List<CharacterRecord> alive = new ArrayList<>();

        for (CharacterRecord p : players) {
            if (!p.isDead()) {
                alive.add(p);
            }
        }

        if (alive.isEmpty()) {
            return null;
        }

        return alive.get(RANDOM.nextInt(alive.size()));
    }

    private void damage(Object attacker, Object defender, int damage, String type) {

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
            if (!m.isDead()) {
                m.adjustHitPoints(-damage);
                if (m.getHealthCursor() != null) {
                    m.getHealthCursor().adjust(m.getCurrentHitPoints(), m.getMaxHitPoints());
                }
                log(String.format("%s %s %s [%d] %s.",
                        attName,
                        "hit",
                        m.name(),
                        damage, type), Color.SCARLET);
            }
        } else if (defender instanceof CharacterRecord p) {
            if (!p.isDead()) {
                p.adjustHP(-damage);
                log(String.format("%s hit %s [%d]", attName, p.name.toUpperCase(), damage), Color.RED);
            }
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
            case MABADI:
                spellDamage(caster, spell, target);
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
            case DILTO:
            case MORLIS:
            case MAMORLIS:
                spellGroupFearAndACModify(caster, spell);
                break;
            case DIALKO:
                for (CharacterRecord p : players) {
                    p.status.set(Status.PARALYZED, 0);
                    p.status.set(Status.ASLEEP, 0);
                }
                break;
            case DIOS:
            case DIAL:
            case DIALMA:
            case MADI:
                spellHeal(caster, spell, target);
                break;
            case HAMAN:
            case MAHAMAN:
                if (caster instanceof CharacterRecord p) {
                    SpellUtil.haman(p, spell, players, monsters, this::removeMonster, logs);
                }
                break;
            case LATUMAPIC:
                //nothing - supposed to identify monsters
                break;
            case MOGREF:
            case SOPIC:
            case PORFIC:
                //Stacks with repeat casts per Data Driven Gamer’s “The spellbooks of Wizardry”
                if (caster instanceof CharacterRecord p) {
                    p.acmodifier1 += spell.getHitBonus();
                } else if (caster instanceof Mutable m) {
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
        }
    }

    private void spellDamage(Object caster, Spells spell, Object target) {
        if (target == null) {
            return;
        }

        if (caster instanceof CharacterRecord p) {
            if (!(target instanceof MutableMonster mm)) {
                return;
            }

            boolean unaffected = mm.isUnaffected(spell, CharacterType.valueOf(p.classType.toString()));

            if (spell == Spells.MABADI) {
                if (unaffected) {
                    log(mm.name() + " saves versus " + spell + " unaffected!");
                    return;
                }
                int pointsLeft = Utils.getRandomBetween(1, 8);
                mm.adjustHitPoints(-(mm.getCurrentHitPoints() - pointsLeft));
                mm.adjustHealthCursor();
                return;
            }

            int dmg = spell.damage();
            if (unaffected) {
                dmg = dmg / 2;
            }
            if (spell.equals(Spells.ZILWAN) && mm.getMonsterType() != CharacterType.UNDEAD) {
                dmg = 0;
            }
            damage(caster, mm, dmg, spell.getName());
        }

        if (caster instanceof MutableMonster) {
            if (!(target instanceof CharacterRecord p)) {
                return;
            }

            if (p.savingThrowSpell()) {
                log(p.name.toUpperCase() + " saves versus " + spell + " unaffected!");
                return;
            }

            if (spell == Spells.MABADI) {
                int pointsLeft = Utils.getRandomBetween(1, 8);
                p.hp = pointsLeft;
                if (p.healthCursor != null) {
                    p.healthCursor.adjust(p.hp, p.maxhp);
                }
                return;
            }

            int dmg = spell.damage();
            damage(caster, p, dmg, spell.getName());
        }
    }

    private void spellGroupDamage(Object caster, Spells spell) {
        int dmg = spell.damage();
        log("for group damage of " + dmg, Color.SKY);

        if (caster instanceof CharacterRecord p) {
            CharacterType casterType = CharacterType.valueOf(p.classType.toString());

            for (MutableMonster mm : this.monsters) {
                if (mm.isDead()) {
                    continue;
                }

                boolean unaffected = mm.isUnaffected(spell, casterType);
                int actualDamage = unaffected ? dmg / 2 : dmg;

                if (actualDamage > 0) {
                    damage(caster, mm, actualDamage, spell.getName());
                }
            }

            return;
        }

        if (caster instanceof MutableMonster) {
            for (CharacterRecord p : this.players) {
                if (p.isDead()) {
                    continue;
                }

                int actualDamage = dmg;

                if (p.savingThrowSpell()) {
                    log(p.name.toUpperCase() + " saves versus " + spell + " for half damage!");
                    actualDamage /= 2;
                }

                if (actualDamage > 0) {
                    damage(caster, p, actualDamage, spell.getName());
                }
            }
        }
    }

    private void spellHeal(Object caster, Spells spell, Object target) {
        if (target == null) {
            return;
        }

        int heal = spell == Spells.MADI ? Integer.MAX_VALUE : spell.damage();

        if (caster instanceof CharacterRecord) {
            if (!(target instanceof CharacterRecord p)) {
                return;
            }

            if (!p.isDead()) {
                if (spell == Spells.MADI) {
                    p.adjustHP(p.maxhp);
                    p.status.set(Status.ASLEEP, 0);
                    p.status.set(Status.PARALYZED, 0);
                    p.status.set(Status.SILENCED, 0);
                    p.status.set(Status.AFRAID, 0);
                    p.status.set(Status.POISONED, 0);
                } else {
                    p.adjustHP(heal);
                }
            }
        }

        if (caster instanceof MutableMonster) {
            if (!(target instanceof MutableMonster mm)) {
                return;
            }

            if (!mm.isDead()) {
                if (spell == Spells.MADI) {
                    mm.adjustHitPoints(mm.getMaxHitPoints());
                    mm.status().set(Status.ASLEEP, 0);
                    mm.status().set(Status.PARALYZED, 0);
                    mm.status().set(Status.SILENCED, 0);
                    mm.status().set(Status.AFRAID, 0);
                    mm.status().set(Status.POISONED, 0);
                } else {
                    mm.adjustHitPoints(heal);
                }
                mm.adjustHealthCursor();
            }
        }
    }

    private void spellGroupAffect(Object caster, Spells spell, Status effect) {
        if (caster instanceof MutableMonster) {
            for (CharacterRecord p : this.players) {
                if (p.savingThrowSpell()) {
                    log(p.name.toUpperCase() + " saves versus " + spell + " unaffected!");
                } else {
                    p.status.set(effect, Math.abs(spell.getHitBonus()));
                }
            }
        }
        if (caster instanceof CharacterRecord p) {
            for (MutableMonster mm : this.monsters) {
                boolean unaffected = mm.isUnaffected(spell, CharacterType.valueOf(p.classType.toString()));
                if (unaffected) {
                    log(mm.name() + " saves versus " + spell + " unaffected!");
                } else {
                    mm.status().set(effect, Math.abs(spell.getHitBonus()));
                }
            }
        }
    }

    private void spellGroupACModify(Object caster, int modifier) {
        if (caster instanceof CharacterRecord) {
            for (CharacterRecord p : this.players) {
                p.acmodifier1 += modifier;
            }
        } else if (caster instanceof MutableMonster) {
            for (MutableMonster mm : this.monsters) {
                mm.setACModifier(mm.getACModifier() + modifier);
            }
        }
    }

    private void spellGroupFearAndACModify(Object caster, Spells spell) {
        int modifier = spell.getHitBonus();

        if (caster instanceof CharacterRecord p) {
            for (MutableMonster mm : this.monsters) {
                boolean unaffected = mm.isUnaffected(spell, CharacterType.valueOf(p.classType.toString()));

                if (unaffected) {
                    log(mm.name() + " saves versus " + spell + " unaffected!");
                } else {
                    mm.status().set(Status.AFRAID, Math.abs(modifier));
                    mm.setACModifier(mm.getACModifier() + modifier);
                }
            }
        }

        if (caster instanceof MutableMonster) {
            for (CharacterRecord p : this.players) {
                if (p.savingThrowSpell()) {
                    log(p.name.toUpperCase() + " saves versus " + spell + " unaffected!");
                } else {
                    p.status.set(Status.AFRAID, Math.abs(modifier));
                    p.acmodifier1 += modifier;
                }
            }
        }
    }

    public abstract void log(String s);

    public abstract void log(String s, Color c);

    public abstract void playSound(Sound sound);

    public class Action {

        public final CharacterRecord player;
        public Spells spell;
        public Item item;
        public boolean dispel;
        public MutableMonster monsterTarget;
        public CharacterRecord playerTarget;

        public Action(CharacterRecord player) {
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
            if (this.dispel) {
                sb.append("Dispel Undead").append(" - ");
            }
            if (this.playerTarget != null) {
                sb.append(this.playerTarget.name.toUpperCase());
            } else if (this.monsterTarget != null) {
                Monster m = (Monster) this.monsterTarget.baseType();
                sb.append(m.name.toUpperCase());
            } else {
                sb.append("ANY");
            }
            return sb.toString();
        }

    }

    public final Action addAction(CharacterRecord player) {
        Action a = new Action(player);
        actions.add(a);
        return a;
    }

    public final void setAction(int index, Spells s) {
        Action al = this.actions.get(index);
        if (al != null && (s.getArea() == SpellArea.COMBAT || s.getArea() == SpellArea.ANY_TIME)) {
            al.spell = s;
            al.item = null;
            al.dispel = false;
        }
    }

    public final void setAction(int index, Item i) {
        Action al = this.actions.get(index);
        if (al != null && (i.spell.getArea() == SpellArea.COMBAT || i.spell.getArea() == SpellArea.ANY_TIME)) {
            al.item = i;
            al.spell = null;
            al.dispel = false;
        }
    }

    public final void setAction(int index, boolean dispel) {
        Action al = this.actions.get(index);
        if (al != null) {
            al.dispel = dispel;
            if (dispel) {
                al.spell = null;
                al.item = null;
            }
        }
    }

    public final void setAction(int index, MutableMonster target) {
        Action al = this.actions.get(index);
        al.monsterTarget = target;
        al.playerTarget = null;
    }

    public final void setAction(int index, CharacterRecord target) {
        Action al = this.actions.get(index);
        al.playerTarget = target;
        al.monsterTarget = null;
    }

    public final Action getAction(CharacterRecord player) {
        for (int i = 0; i < this.actions.size(); i++) {
            Action al = this.actions.get(i);
            if (al.player == player) {
                return al;
            }
        }
        return null;
    }

    public void addMonster(MutableMonster added) {
        this.monsters.add(added);
    }

    public void removeMonster(MutableMonster removed) {
        this.monsters.remove(removed);
    }
}
