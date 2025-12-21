package andius;

import andius.Constants.Map;
import andius.objects.Dice;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.Mutable;
import andius.objects.MutableMonster;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Sound;
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
                Action action = getAction(player);

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
                                    Item changeTo = this.contextMap.scenario().items().get(action.item.changeTo);
                                    player.removeItem(action.item.id, action.item.scenarioID);
                                    if (changeTo.id != 0) {
                                        player.inventory.add(changeTo);
                                    }
                                }
                            }
                        } else if (action.dispel) {
                            for (MutableMonster mon : monsters) {
                                if (mon.getCurrentHitPoints() > 0 && mon.getMonsterType() == CharacterType.UNDEAD) {
                                    int roll = RANDOM.nextInt(100) + 1;
                                    if (roll < 50 + (player.level * 5) - (mon.getLevel() * 10)) {
                                        log(String.format("%s dispels %s and sends it back to the abyss!", player.name.toUpperCase(), defender.name()), Color.SKY);
                                    } else {
                                        log(String.format("%s failed to dispel %s", player.name.toUpperCase(), defender.name()), Color.WHITE);
                                    }
                                }
                            }
                        } else {
                            int hpDamage = 0;
                            int hitsCount = 0;
                            Item weapon = player.weapon == null ? Item.HANDS : player.weapon;

                            for (int i = 0; i < player.extraSwings(); i++) {
                                boolean hit = Utils.attackHit(player, defender);
                                if (hit) {
                                    hitsCount++;
                                    hpDamage += Utils.dealDamage(player, weapon, defender);
                                }
                            }

                            if (hpDamage > 0) {
                                log(String.format("%s %s %s %d times for %d damage with %s.",
                                        player.name.toUpperCase(),
                                        HITMSGS[Utils.RANDOM.nextInt(HITMSGS.length)],
                                        defender.name().toUpperCase(),
                                        hitsCount,
                                        hpDamage, weapon.name), Color.SCARLET);
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

        if (Utils.inflict(attacker, pickPlayer(), this.logs)) {
            return;
        }

        if (attacker.monster().ability.contains(Ability.CALLFORHELP)
                && attacker.getPercentDamaged() < 0.50
                && Utils.RANDOM.nextInt(100) < 75
                && Utils.percentChance(attacker.getLevel() * 5)) {
            action = CombatAction.CALL_FOR_HELP;
        } else if (attacker.monster().ability.contains(Ability.RUN)
                && attacker.getPercentDamaged() < 0.50
                && Utils.RANDOM.nextInt(100) < 75
                && Utils.percentChance(attacker.getLevel() * 5)) {
            action = CombatAction.FLEE;
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
                    damage(attacker, defender, dmg, attacker.breath().toString());
                }
                break;
            case ATTACK:
                CharacterRecord defender = pickPlayer();
                if (defender != null) {
                    boolean hit = Utils.attackHit(attacker, defender);
                    if (hit) {
                        for (Dice dice : attacker.getDamage()) {
                            int dmg = dice.roll();
                            damage(attacker, defender, dmg, "arms");
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
            case FLEE: {
                log(String.format("%s fled!", attacker.name()), Color.SKY);
                removeMonster(attacker);
                playSound(Sound.FLEE);
                break;
            }
            case CALL_FOR_HELP: {
                log(String.format("%s called for help!", attacker.name()), Color.GOLDENROD);
                MutableMonster added = new MutableMonster(attacker.monster());
                addMonster(added);
                playSound(Sound.GIGGLE);
                break;
            }
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

    public CharacterRecord pickPlayer() {

        List<CharacterRecord> alive = new ArrayList<>(players.size());
        for (CharacterRecord p : players) {
            if (!p.isDead()) {
                alive.add(p);
            }
        }

        int n = alive.size();
        if (n == 0) {
            return null;
        }
        if (n == 1) {
            return alive.get(0);
        }
        if (n <= 3) {
            return alive.get(RANDOM.nextInt(n));
        }

        int idx = (n <= 5) ? pickIndex2nPlus3(n) : pickIndexNPlus9(n);
        return alive.get(idx);
    }

    private int pickIndex2nPlus3(int n) {
        int r = RANDOM.nextInt(2 * n + 3);
        if (r < 9) {
            return r / 3;
        }
        return 3 + (r - 9) / 2;
    }

    private int pickIndexNPlus9(int n) {
        int r = RANDOM.nextInt(n + 9);
        if (r < 12) {
            return r / 4;
        }
        return 3 + (r - 12);
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
                log(String.format("%s %s %s for %d damage with %s.",
                        attName.toUpperCase(),
                        HITMSGS[Utils.RANDOM.nextInt(HITMSGS.length)],
                        m.name().toUpperCase(),
                        damage, type), Color.SCARLET);
            }
        } else if (defender instanceof CharacterRecord p) {
            if (!p.isDead()) {
                p.adjustHP(-damage);
                log(String.format("%s strikes %s who was hit for %d damage!", attName, p.name.toUpperCase(), damage), Color.RED);
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
            case DILTO:
            case MORLIS:
            case MAMORLIS:
                spellGroupAffect(caster, spell, Status.AFRAID);
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
            damage(caster, mm, dmg, spell.getName());
        }
        if (caster instanceof MutableMonster) {
            CharacterRecord p = (CharacterRecord) target;
            if (p.savingThrowSpell()) {
                log(p.name.toUpperCase() + " made a saving throw versus " + spell + " and is unaffected!");
            } else {
                int dmg = spell.damage();
                damage(caster, p, dmg, spell.getName());
            }
        }
    }

    private void spellGroupDamage(Object caster, Spells spell) {
        int groupDamage = spell.damage();
        if (caster instanceof CharacterRecord p) {
            java.util.Map<MutableMonster, Integer> grpDamageMap = monsters.stream().collect(Collectors.toMap(key -> key, key -> 0));
            while (groupDamage > 0) {
                for (MutableMonster mm : this.monsters) {
                    boolean unaffected = mm.isUnaffected(spell, CharacterType.valueOf(p.classType.toString()));
                    if (!mm.status().isDisabled() && unaffected) {

                    } else {
                        int dmg = grpDamageMap.get(mm);
                        grpDamageMap.put(mm, dmg + 1);
                    }
                    groupDamage--;
                }
            }
            for (MutableMonster mm : grpDamageMap.keySet()) {
                int dmg = grpDamageMap.get(mm);
                if (dmg > 0) {
                    damage(caster, mm, dmg, spell.getName());
                }
            }
        }
        if (caster instanceof MutableMonster) {
            java.util.Map<CharacterRecord, Integer> grpDamageMap = this.players.stream().collect(Collectors.toMap(key -> key, key -> 0));
            while (groupDamage > 0) {
                for (CharacterRecord p : this.players) {
                    if (p.armor != null && p.armor.id == 89 && (spell.equals(Spells.KATINO) || spell.equals(Spells.LAKANITO) || spell.equals(Spells.MAKANITO))) {
                        log(p.name.toUpperCase() + " was saved by oxygen mask versus " + spell + " and is unaffected!");
                    } else if (p.savingThrowSpell()) {
                        log(p.name.toUpperCase() + " made a saving throw versus " + spell + " and is unaffected!");
                    } else {
                        int dmg = grpDamageMap.get(p);
                        grpDamageMap.put(p, dmg + 1);
                    }
                    groupDamage--;
                }
            }
            for (CharacterRecord p : grpDamageMap.keySet()) {
                int dmg = grpDamageMap.get(p);
                if (dmg > 0) {
                    damage(caster, p, dmg, spell.getName());
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
                    if (mm.getHealthCursor() != null) {
                        mm.getHealthCursor().adjust(mm.getCurrentHitPoints(), mm.getMaxHitPoints());
                    }
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
                    if (p.healthCursor != null) {
                        p.healthCursor.adjust(p.hp, p.maxhp);
                    }
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
                    if (mm.getHealthCursor() != null) {
                        mm.getHealthCursor().adjust(mm.getCurrentHitPoints(), mm.getMaxHitPoints());
                    }
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
                    p.status.set(effect, Math.abs(spell.getHitBonus()));
                }
            }
        }
        if (caster instanceof CharacterRecord p) {
            for (MutableMonster mm : this.monsters) {
                boolean unaffected = mm.isUnaffected(spell, CharacterType.valueOf(p.classType.toString()));
                if (unaffected) {
                    log(mm.name() + " made a saving throw versus " + spell + " and is unaffected!");
                } else {
                    mm.status().set(effect, Math.abs(spell.getHitBonus()));
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

    public abstract void log(String s);

    public abstract void log(String s, Color c);

    public abstract void playSound(Sound sound);

    public class Action {

        public final CharacterRecord player;
        public Spells spell;
        public Item item;
        public boolean dispel;
        public MutableMonster target;

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
            if (this.target != null) {
                Monster m = (Monster) this.target.baseType();
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
        }
    }

    public final void setAction(int index, Item i) {
        Action al = this.actions.get(index);
        if (al != null && (i.spell.getArea() == SpellArea.COMBAT || i.spell.getArea() == SpellArea.ANY_TIME)) {
            al.item = i;
            al.spell = null;
        }
    }

    public final void setAction(int index, boolean dispel) {
        Action al = this.actions.get(index);
        if (al != null) {
            al.dispel = dispel;
        }
    }

    public final void setAction(int index, MutableMonster target) {
        Action al = this.actions.get(index);
        al.target = target;
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
