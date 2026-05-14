package andius.objects;

import static andius.Andius.CTX;
import andius.Constants.SpellTarget;
import andius.Constants.Status;
import utils.Utils;
import andius.objects.SaveGame.CharacterRecord;
import static andius.objects.SaveGame.RANDOM;
import static andius.objects.Spells.DIOS;
import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;
import java.util.List;
import utils.Loggable;

public class SpellUtil {

    public static boolean useItem(Item item, CharacterRecord rec) {

        Spells spell = item.spell;

        switch (spell) {
            case DIOS:
            case DIAL:
            case DIALMA:
            case MADI:
                Sounds.play(spell.getSound());
                doSpellHeal(rec, spell);
                break;

            case LATUMOFIS:
                Sounds.play(spell.getSound());
                rec.status.set(Status.POISONED, 0);
                break;

            case DIALKO:
                Sounds.play(spell.getSound());
                for (CharacterRecord p : CTX.players()) {
                    p.status.set(Status.PARALYZED, 0);
                    p.status.set(Status.ASLEEP, 0);
                    for (MutableMonster m : p.summonedMonsters) {
                        m.status().set(Status.PARALYZED, 0);
                        m.status().set(Status.ASLEEP, 0);
                    }
                }
                break;

            case SOPIC:
            case PORFIC:
                Sounds.play(spell.getSound());
                rec.acmodifier1 += spell.getHitBonus();
                break;

            default:
                Sounds.play(Sound.NEGATIVE_EFFECT);
                return false;
        }

        return true;
    }

    public static void campCast(CharacterRecord caster, Spells spell, CharacterRecord target) {

        if (caster.isDisabled()) {
            Sounds.play(Sound.EVADE);
            return;
        }

        if (!caster.canCast(spell)) {
            Sounds.play(Sound.EVADE);
            return;
        }

        if (caster.status.has(Status.SILENCED)) {
            Sounds.play(Sound.EVADE);
            return;
        }

        if (target == null && spell.getTarget() == SpellTarget.PERSON) {
            Sounds.play(Sound.EVADE);
            return;
        }

        caster.decrMagicPts(spell);

        switch (spell) {
            case DUMAPIC:
            case MILWA:
            case LOMILWA:
            case KANDI:
                Sounds.play(Sound.EVADE);
                break;

            case DIOS:
            case DIAL:
            case DIALMA:
            case MADI:
                Sounds.play(spell.getSound());
                doSpellHeal(target, spell);
                break;

            case DIALKO:
                Sounds.play(spell.getSound());
                target.status.set(Status.ASLEEP, 0);
                target.status.set(Status.PARALYZED, 0);
                break;

            case LATUMAPIC:
                Sounds.play(spell.getSound());
                target.status.set(Status.SILENCED, 0);
                target.status.set(Status.AFRAID, 0);
                target.status.set(Status.PARALYZED, 0);
                break;

            case LATUMOFIS:
                Sounds.play(spell.getSound());
                target.status.set(Status.POISONED, 0);
                break;

            case MAPORFIC:
                Sounds.play(spell.getSound());
                target.acmodifier2 += spell.getHitBonus();
                break;

            case DI:
                Sounds.play(spell.getSound());
                if (target.isDead()) {
                    if (Utils.RANDOM.nextInt(100) > 50 + target.vitality * 3) {
                        Sounds.play(Sound.EVADE);
                    } else {
                        target.hp = 1;
                    }
                } else {
                    Sounds.play(Sound.EVADE);
                }
                break;

            case KADORTO:
                Sounds.play(spell.getSound());
                if (target.isDead()) {
                    target.adjustHP(target.maxhp);
                    target.status.reset();
                } else {
                    Sounds.play(Sound.EVADE);
                }
                break;

            default:
                Sounds.play(Sound.EVADE);
                break;
        }
    }

    private static void doSpellHeal(CharacterRecord target, Spells spell) {
        if (target != null && !target.isDead()) {
            if (spell == Spells.MADI) {
                target.adjustHP(target.maxhp);
                target.status.set(Status.ASLEEP, 0);
                target.status.set(Status.PARALYZED, 0);
                target.status.set(Status.SILENCED, 0);
                target.status.set(Status.AFRAID, 0);
                target.status.set(Status.POISONED, 0);
            } else {
                int points = spell.damage();
                target.adjustHP(points);
            }
        }
    }

    public static void haman(CharacterRecord p, 
            Spells spell, 
            List<CharacterRecord> players,
            List<MutableMonster> monsters,
            java.util.function.Consumer<MutableMonster> removeMonster,
            Loggable logs) {

        if (p.level <= 1) {
            logs.add(p.name.toUpperCase() + " is too weak to cast " + spell, Color.WHITE);
            return;
        }

        p.level--;

        int hpLoss = Math.max(1, p.getMoreHP());
        p.maxhp = Math.max(1, p.maxhp - hpLoss);

        if (p.hp > p.maxhp) {
            p.hp = p.maxhp;
        }

        if (p.healthCursor != null) {
            p.healthCursor.adjust(p.hp, p.maxhp);
        }

        SaveGame.setSpellPoints(p);

        int effect = RANDOM.nextInt(spell == Spells.MAHAMAN ? 5 : 3);

        switch (effect) {
            case 0:
                // Heal all living party members.
                for (CharacterRecord ally : players) {
                    if (!ally.isDead()) {
                        int heal = new Dice(9, 8).roll();
                        ally.adjustHP(heal);
                    }
                }
                logs.add(spell + " restores the party!", Color.SKY);
                break;

            case 1:
                // Full heal and cure most combat statuses except poison/death.
                for (CharacterRecord ally : players) {
                    if (!ally.isDead()) {
                        ally.adjustHP(ally.maxhp);
                        ally.status.set(Status.SILENCED, 0);
                        ally.status.set(Status.AFRAID, 0);
                        ally.status.set(Status.ASLEEP, 0);
                        ally.status.set(Status.PARALYZED, 0);
                    }
                }
                logs.add(spell + " fully restores the party!", Color.SKY);
                break;

            case 2:
                // frighten them and lower their AC modifier for this combat
                for (MutableMonster mm : monsters) {
                    if (!mm.isDead()) {
                        mm.status().set(Status.AFRAID, 5);
                        mm.setACModifier(mm.getACModifier() - 4);
                    }
                }

                logs.add(spell + " weakens all the monsters resistance!", Color.SKY);
                break;

            case 3:
                // MAHAMAN monster banishment.
                List<MutableMonster> banished = new ArrayList<>();

                for (MutableMonster mm : monsters) {
                    if (!mm.isDead()) {
                        banished.add(mm);
                    }
                }

                for (MutableMonster mm : banished) {
                    logs.add(mm.name() + " is banished!", Color.SKY);
                    removeMonster.accept(mm);
                }
                break;

            case 4:
                // MAHAMAN mass silence / fear.
                for (MutableMonster mm : monsters) {
                    if (!mm.isDead()) {
                        mm.status().set(Status.SILENCED, 5);
                        mm.status().set(Status.AFRAID, 5);
                    }
                }
                logs.add(spell + " overwhelms the monsters!", Color.SKY);
                break;
        }
    }
}
