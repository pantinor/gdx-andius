package andius.objects;

import static andius.Andius.CTX;
import andius.Constants.Status;
import utils.Utils;
import andius.objects.SaveGame.CharacterRecord;
import static andius.objects.Spells.DIOS;

public class SpellUtil {

    public static boolean useItem(Item item, CharacterRecord rec) {

        Spells spell = item.spell;

        switch (spell) {
            case DIOS:
            case DIAL:
            case MADI:
                Sounds.play(spell.getSound());
                doSpellHeal(rec, spell);
                break;
            case LATUMOFIS:
                Sounds.play(spell.getSound());
                rec.status.set(Status.POISONED, 0);
                break;
            case DIALKO:
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
                rec.acmodifier1 = spell.getHitBonus();
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

        caster.decrMagicPts(spell);

        switch (spell) {
            case DUMAPIC:
            case MILWA:
            case LOMILWA:
            case KANDI:
                Sounds.play(Sound.EVADE);
                break;
            case DIOS://heal
            case DIALMA://greatly heal
            case MADI://healing
            case DIAL://more heal
                doSpellHeal(target, spell);
                break;
            case DIALKO://dispel affects
                target.status.set(Status.ASLEEP, 0);
                target.status.set(Status.PARALYZED, 0);
            case LATUMAPIC://dispel affects
                target.status.set(Status.SILENCED, 0);
                target.status.set(Status.AFRAID, 0);
                target.status.set(Status.PARALYZED, 0);
                break;
            case LATUMOFIS://cure poison
                target.status.set(Status.POISONED, 0);
                break;
            case MAPORFIC://big shield
                target.acmodifier2 = spell.getHitBonus();
                break;
            case DI://life
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
            case KADORTO://resurect
                if (target.isDead()) {
                    target.adjustHP(target.maxhp);
                    target.status.reset();
                } else {
                    Sounds.play(Sound.EVADE);
                }
                break;
        }
    }

    private static void doSpellHeal(CharacterRecord target, Spells spell) {
        if (target != null && !target.isDead()) {
            if (spell == Spells.MADI) {
                target.adjustHP(target.maxhp);
            } else {
                int points = spell.damage();
                target.adjustHP(points);
            }
        }
    }

}
