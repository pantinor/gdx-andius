package andius.objects;

import andius.Andius;
import andius.BaseScreen;
import andius.CombatScreen;
import andius.CombatScreen.RemoveCreatureAction;
import andius.Constants.AddActorAction;
import andius.Constants.CharacterType;
import andius.Constants.PlaySoundAction;
import andius.Constants.LogAction;
import andius.Constants.Status;
import andius.Context;
import andius.Sound;
import andius.Sounds;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import utils.Utils;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import java.util.Random;

public class SpellUtil {

    private static final Random rand = new Random();

    public static void spellCast(CombatScreen screen, Context context, Spells spell, andius.objects.Actor caster, andius.objects.Actor target, boolean isItem) {

        SequenceAction seq = Actions.action(SequenceAction.class);

        try {

            if (!isItem) {
                if (!caster.getPlayer().canCast(spell)) {
                    screen.log("Thou dost not have enough magic points!");
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }

                if (caster.getPlayer().status.has(Status.SILENCED)) {
                    screen.log("Silenced!");
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }

                caster.getPlayer().decrMagicPts(spell);
            }

            seq.addAction(Actions.run(new PlaySoundAction(spell.getSound())));
            seq.addAction(Actions.delay(0.5f));

            switch (spell) {
                case KATINO:
                    for (andius.objects.Actor m : screen.enemies) {
                        seq.addAction(Actions.delay(.60f));
                        if (rand.nextInt(100) < m.getEnemy().getUnaffected()) {
                            seq.addAction(Actions.run(new LogAction(screen, m.getEnemy().name() + " is unaffected.")));
                            seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
                        } else {
                            m.getEnemy().status().set(Status.ASLEEP, 4);
                            seq.addAction(Actions.run(new LogAction(screen, m.getEnemy().name() + " is asleep.")));
                            seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
                        }
                    }
                    break;
                case MAKANITO:
                    spellGroupDamage(screen, seq, spell, 35, 40);
                    break;
                case LAKANITO:
                    spellGroupDamage(screen, seq, spell, 45, 65);
                    break;
                case LITOKAN:
                case LORTO:
                case MALIKTO:
                case MAHALITO:
                case MOLITO:
                case DALTO:
                case LAHALITO:
                case TILTOWAIT:
                case MADALTO:
                    spellGroupDamage(screen, seq, spell);
                    break;
                case HALITO:
                case BADIAL:
                case BADIALMA:
                case ZILWAN:
                case BADIOS:
                case BADI:
                case MABADI:
                    projectileMagicAttack(screen, seq, caster, spell, target);
                    break;
                case MANIFO:
                    for (andius.objects.Actor m : screen.enemies) {
                        seq.addAction(Actions.delay(.60f));
                        if (rand.nextInt(100) < m.getEnemy().getUnaffected()) {
                            seq.addAction(Actions.run(new LogAction(screen, m.getEnemy().name() + " is unaffected.")));
                            seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
                        } else {
                            m.getEnemy().status().set(Status.PARALYZED, 4);
                            seq.addAction(Actions.run(new LogAction(screen, m.getEnemy().name() + " is paralyzed.")));
                            seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
                        }
                    }
                    break;
                case MONTINO:
                    for (andius.objects.Actor m : screen.enemies) {
                        seq.addAction(Actions.delay(.60f));
                        if (rand.nextInt(100) < m.getEnemy().getUnaffected()) {
                            seq.addAction(Actions.run(new LogAction(screen, m.getEnemy().name() + " is unaffected.")));
                            seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
                        } else {
                            m.getEnemy().status().set(Status.SILENCED, 4);
                            seq.addAction(Actions.run(new LogAction(screen, m.getEnemy().name() + " is silenced.")));
                            seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
                        }
                    }
                    break;
                case LATUMAPIC://dispel
                    for (andius.objects.Actor m : screen.partyMembers) {
                        target.getPlayer().status.set(Status.ASLEEP, 0);
                        target.getPlayer().status.set(Status.PARALYZED, 0);
                        target.getPlayer().status.set(Status.SILENCED, 0);
                    }
                    break;
                case DIOS:
                case DIAL:
                case DIALMA:
                case MADI:
                    doSpellHeal(screen, seq, target, spell);
                    break;
                case LATUMOFIS:
                    target.getPlayer().status.set(Status.POISONED, 0);
                    break;
                case DIALKO:
                    target.getPlayer().status.increment(Status.POISONED);
                    break;
                case MOGREF:
                case PORFIC:
                case SOPIC:
                    caster.getPlayer().acmodifier1 = spell.getHitBonus();
                    break;
                case DILTO:
                case MORLIS:
                case MAMORLIS:
                    modMonsterAC(screen, spell.getHitBonus());
                    break;
                case KALKI:
                case MATU:
                case BAMATU:
                case MASOPIC:
                    modPartyAC1(screen, spell.getHitBonus());
                    break;
                case MAPORFIC:
                    modPartyAC2(screen, spell.getHitBonus());
                    break;
                case MALOR:
                    screen.castCombatMalor();
                    break;
                case LOMILWA:
                case CALFO:
                case MILWA:
                case KANDI:
                case DI:
                case LOKTOFEIT:
                case KADORTO:
                case HAMAN:
                case MAHAMAN:
                case DUMAPIC:
                    seq.addAction(Actions.run(new LogAction(screen, "Cannot be cast during combat.")));
                    seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
                    break;
            }

        } finally {

            seq.addAction(Actions.run(new Runnable() {
                @Override
                public void run() {
                    screen.finishPlayerTurn();
                }
            }));

            screen.getStage().addAction(seq);
        }

    }

    public static boolean useItem(String item, CharacterRecord rec) {

        Spells spell = null;
        for (Spells sp : Spells.values()) {
            if (item.toUpperCase().contains(sp.toString())) {
                spell = sp;
                break;
            }
        }

        if (spell == null || rec.status.has(Status.SILENCED)) {
            Sounds.play(Sound.NEGATIVE_EFFECT);
            return false;
        }

        Sounds.play(spell.getSound());

        switch (spell) {
            case DIOS:
            case DIAL:
            case MADI:
                doSpellHeal(rec, spell);
                break;
            case LATUMOFIS:
                rec.status.set(Status.POISONED, 0);
                break;
            case SOPIC:
                rec.acmodifier1 = spell.getHitBonus();
                break;
        }

        return true;

    }

    public static void campCast(BaseScreen screen, Context context, CharacterRecord caster, Spells spell) {

        try {

            if (!caster.canCast(spell)) {
                screen.log("Thou dost not have enough magic points!");
                Sounds.play(Sound.EVADE);
                return;
            }

            if (caster.status.has(Status.SILENCED)) {
                screen.log("Silenced!");
                Sounds.play(Sound.EVADE);
                return;
            }

            caster.decrMagicPts(spell);

            switch (spell) {
                case DUMAPIC:
                    break;
                case DIOS:
                    break;
                case MILWA:
                    break;
                case LOMILWA:
                    break;
                case DIALKO:
                    break;
                case LATUMAPIC:
                    break;
                case DIAL:
                    break;
                case LATUMOFIS:
                    break;
                case MAPORFIC:
                    break;
                case DIALMA:
                    break;
                case KANDI:
                    break;
                case DI:
                    break;
                case MADI:
                    break;
                case KADORTO:
                    break;
            }

        } finally {

        }

    }

    private static void projectileMagicAttack(CombatScreen screen, SequenceAction seq, andius.objects.Actor attacker, Spells spell, andius.objects.Actor target) {

        int targetX = target.getWx();
        int targetY = target.getWy();

        int a = Math.abs(attacker.getWx() - targetX);
        int b = Math.abs(attacker.getWy() - targetY);

        ProjectileActor p = new ProjectileActor(spell.getColor(), attacker.getX(), attacker.getY());

        if (rand.nextInt(100) < target.getEnemy().getUnaffected()
                || (spell == Spells.ZILWAN && !target.getEnemy().getMonsterType().equals(CharacterType.UNDEAD))) {
            seq.addAction(Actions.run(new LogAction(screen, target.getEnemy().name() + " is unaffected.")));
        } else {
            spellDamage(spell, target.getEnemy());
            seq.addAction(Actions.run(new LogAction(screen, String.format("%s %s", target.getEnemy().name(), target.getEnemy().getDamageTag()))));
        }

        Actor expl = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(spell.getColor()));
        expl.setX(target.getX() + 12);
        expl.setY(target.getY() + 12);
        expl.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

        seq.addAction(Actions.run(new AddActorAction(screen.getStage(), expl)));

        if (target.getEnemy().getCurrentHitPoints() <= 0) {
            seq.addAction(Actions.run(new RemoveCreatureAction(screen, target)));
        }

        Action after = new Action() {
            @Override
            public boolean act(float delta) {
                p.remove();
                screen.getStage().addAction(seq);
                return true;
            }
        };

        p.addAction(Actions.sequence(Actions.moveTo(target.getX(), target.getY(), .3f, Interpolation.sineIn), after));

        screen.getStage().addActor(p);
    }

    private static void spellDamage(Spells spell, Mutable m) {
        int damage = spell.damage();
        m.setCurrentHitPoints(m.getCurrentHitPoints() - damage);
        m.adjustHealthCursor();
    }

    private static void spellGroupDamage(CombatScreen screen, SequenceAction seq, Spells spell) {

        for (andius.objects.Actor m : screen.enemies) {

            seq.addAction(Actions.delay(.60f));

            if (rand.nextInt(100) < m.getEnemy().getUnaffected()) {
                seq.addAction(Actions.run(new LogAction(screen, m.getEnemy().name() + " is unaffected.")));

                final Actor expl = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(Color.GRAY));
                expl.setX(m.getX() + 12);
                expl.setY(m.getY() + 12);
                expl.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

                seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
                seq.addAction(Actions.run(new AddActorAction(screen.getStage(), expl)));

            } else {

                int damage = spell.damage();
                m.getEnemy().setCurrentHitPoints(m.getEnemy().getCurrentHitPoints() - damage);
                m.getEnemy().adjustHealthCursor();

                seq.addAction(Actions.run(new LogAction(screen, String.format("%s affects %s %s", spell, m.getEnemy().name(), m.getEnemy().getDamageTag()))));

                final Actor expl = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(spell.getColor()));
                expl.setX(m.getX() + 12);
                expl.setY(m.getY() + 12);
                expl.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
                seq.addAction(Actions.run(new AddActorAction(screen.getStage(), expl)));

                if (m.getEnemy().getCurrentHitPoints() <= 0) {
                    seq.addAction(Actions.run(new RemoveCreatureAction(screen, m)));
                }

            }
        }

    }

    private static void spellGroupDamage(CombatScreen screen, SequenceAction seq, Spells spell, int minDamage, int maxDamage) {

        for (andius.objects.Actor m : screen.enemies) {

            seq.addAction(Actions.delay(.60f));

            int damage = Utils.getRandomBetween(minDamage, maxDamage);
            m.getEnemy().setCurrentHitPoints(m.getEnemy().getCurrentHitPoints() - damage);
            m.getEnemy().adjustHealthCursor();

            seq.addAction(Actions.run(new LogAction(screen, String.format("%s affects %s %s", spell, m.getEnemy().name(), m.getEnemy().getDamageTag()))));

            final Actor expl = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(spell.getColor()));
            expl.setX(m.getX() + 12);
            expl.setY(m.getY() + 12);
            expl.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

            seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
            seq.addAction(Actions.run(new AddActorAction(screen.getStage(), expl)));

            if (m.getEnemy().getCurrentHitPoints() <= 0) {
                seq.addAction(Actions.run(new RemoveCreatureAction(screen, m)));
            }

        }

    }

    private static void modPartyAC1(CombatScreen screen, int acmod) {
        for (andius.objects.Actor player : screen.partyMembers) {
            player.getPlayer().acmodifier1 = acmod;
        }
    }

    private static void modPartyAC2(CombatScreen screen, int acmod) {
        for (andius.objects.Actor player : screen.partyMembers) {
            player.getPlayer().acmodifier2 = acmod;
        }
    }

    private static void modMonsterAC(CombatScreen screen, int acmod) {
        for (andius.objects.Actor m : screen.enemies) {
            m.getEnemy().setACModifier(-acmod);
        }
    }

    private static void doSpellHeal(CombatScreen screen, SequenceAction seq, andius.objects.Actor target, Spells spell) {
        CharacterRecord rec = target.getPlayer();
        if (rec != null && !rec.isDead()) {
            seq.addAction(Actions.run(new LogAction(screen, rec.name + " is healed.")));
            if (spell == Spells.MADI) {
                target.adjustHP(rec.maxhp);
            } else {
                int points = spell.damage();
                target.adjustHP(points);
            }
        } else {
            seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
        }
    }

    private static void doSpellHeal(CharacterRecord rec, Spells spell) {
        if (rec != null && !rec.isDead()) {
            if (spell == Spells.MADI) {
                rec.adjustHP(rec.maxhp);
            } else {
                int points = spell.damage();
                rec.adjustHP(points);
            }
        }
    }

    public static void spellMonsterCast(CombatScreen screen, SequenceAction seq, Spells spell, andius.objects.Actor caster, andius.objects.Actor target) {

        if (caster.getEnemy().status().isDisabled()) {
            screen.log(caster.getEnemy().name() + " is disabled!");
            Sounds.play(Sound.NEGATIVE_EFFECT);
            return;
        }

        seq.addAction(Actions.run(new PlaySoundAction(spell.getSound())));
        seq.addAction(Actions.delay(0.5f));

        switch (spell) {
            case MAKANITO:
                monstersGroupDamage(screen, caster, seq, spell, 35, 40);
                break;
            case LAKANITO:
                monstersGroupDamage(screen, caster, seq, spell, 45, 65);
                break;
            case LITOKAN:
            case LORTO:
            case MALIKTO:
            case MAHALITO:
            case MOLITO:
            case DALTO:
            case LAHALITO:
            case TILTOWAIT:
            case MADALTO:
                monstersGroupDamage(screen, caster, seq, spell);
                break;
            case HALITO:
            case BADIAL:
            case BADIALMA:
            case ZILWAN:
            case BADIOS:
            case BADI:
            case MABADI:
                monstersMagicAttack(screen, seq, caster, spell, target);
                break;
            case KATINO:
                for (andius.objects.Actor m : screen.partyMembers) {
                    monsterCastEffect(screen, seq, caster.getEnemy(), m.getPlayer(), Status.ASLEEP);
                }
                break;
            case MANIFO:
                for (andius.objects.Actor m : screen.partyMembers) {
                    monsterCastEffect(screen, seq, caster.getEnemy(), m.getPlayer(), Status.PARALYZED);
                }
                break;
            case MONTINO:
                for (andius.objects.Actor m : screen.partyMembers) {
                    monsterCastEffect(screen, seq, caster.getEnemy(), m.getPlayer(), Status.SILENCED);
                }
                break;
            case DIOS:
            case DIAL:
            case DIALMA:
            case MADI:
                doMonsterHeal(screen, seq, caster.getEnemy(), spell);
                break;
            case LATUMAPIC:
                for (andius.objects.Actor m : screen.enemies) {
                    m.getEnemy().status().set(Status.ASLEEP, 0);
                    m.getEnemy().status().set(Status.PARALYZED, 0);
                    m.getEnemy().status().set(Status.SILENCED, 0);
                }
                break;
            case MOGREF:
            case SOPIC:
            case PORFIC:
                caster.getEnemy().setACModifier(caster.getEnemy().getACModifier() + spell.getHitBonus());
                break;
            case DILTO:
            case MORLIS:
            case MAMORLIS:
            case KALKI:
            case MATU:
            case BAMATU:
            case MASOPIC:
                modPartyAC1(screen, -spell.getHitBonus());
                break;
            case MAPORFIC:
                modPartyAC1(screen, -spell.getHitBonus());
                break;

        }

    }

    private static void monsterCastEffect(CombatScreen screen, SequenceAction seq, Mutable monster, CharacterRecord player, Status effect) {
        seq.addAction(Actions.delay(.60f));
        if (player.savingThrowSpell()) {
            seq.addAction(Actions.run(new LogAction(screen, player.name + " made a saving throw and is unaffected!")));
            seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
        } else {
            player.status.set(effect, 4);
            seq.addAction(Actions.run(new LogAction(screen, player.name + " is " + effect + ".")));
            seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
        }
    }

    private static void doMonsterHeal(CombatScreen screen, SequenceAction seq, Mutable target, Spells spell) {
        if (target.getCurrentHitPoints() > 0) {
            seq.addAction(Actions.run(new LogAction(screen, target.name() + " is healed.")));
            if (spell == Spells.MADI) {
                target.setCurrentHitPoints(target.getMaxHitPoints());
            } else {
                int healAmt = spell.damage();
                int current = target.getCurrentHitPoints() + healAmt;
                target.setCurrentHitPoints(current > target.getMaxHitPoints() ? target.getMaxHitPoints() : current);
            }
            target.adjustHealthCursor();
        } else {
            seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
        }
    }

    private static void monstersMagicAttack(CombatScreen screen, SequenceAction seq, andius.objects.Actor attacker, Spells spell, andius.objects.Actor player) {

        ProjectileActor p = new ProjectileActor(spell.getColor(), attacker.getX(), attacker.getY());

        if (player.getPlayer().savingThrowSpell()) {
            seq.addAction(Actions.run(new LogAction(screen, player.getPlayer().name + " made a saving throw and is unaffected!")));
        } else {
            int damage = spell.damage();
            if (player.getPlayer().savingThrowSpell()) {
                damage = damage / 2;
            }
            player.adjustHP(-damage);
            seq.addAction(Actions.run(new LogAction(screen, String.format("%s takes %d damage.", player.getPlayer().name, damage))));
        }

        Actor expl = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(spell.getColor()));
        expl.setX(player.getX() + 12);
        expl.setY(player.getY() + 12);
        expl.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

        seq.addAction(Actions.run(new AddActorAction(screen.getStage(), expl)));

        Action after = new Action() {
            @Override
            public boolean act(float delta) {
                p.remove();
                screen.getStage().addAction(seq);
                return true;
            }
        };

        p.addAction(Actions.sequence(Actions.moveTo(player.getX(), player.getY(), .3f, Interpolation.sineIn), after));

        screen.getStage().addActor(p);
    }

    private static void monstersGroupDamage(CombatScreen screen, andius.objects.Actor attacker, SequenceAction seq, Spells spell) {

        for (andius.objects.Actor player : screen.partyMembers) {

            seq.addAction(Actions.delay(.60f));

            if (player.getPlayer().savingThrowSpell()) {
                seq.addAction(Actions.run(new LogAction(screen, player.getPlayer().name + " made a saving throw and is unaffected!")));

                final Actor expl = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(Color.GRAY));
                expl.setX(player.getX() + 12);
                expl.setY(player.getY() + 12);
                expl.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

                seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
                seq.addAction(Actions.run(new AddActorAction(screen.getStage(), expl)));

            } else {
                int damage = spell.damage();
                if (player.getPlayer().savingThrowSpell()) {
                    damage = damage / 2;
                }
                player.adjustHP(-damage);

                seq.addAction(Actions.run(new LogAction(screen, String.format("%s deals %d damage to %s", spell, damage, player.getPlayer().name))));

                final Actor expl = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(spell.getColor()));
                expl.setX(player.getX() + 12);
                expl.setY(player.getY() + 12);
                expl.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
                seq.addAction(Actions.run(new AddActorAction(screen.getStage(), expl)));

            }
        }

    }

    private static void monstersGroupDamage(CombatScreen screen, andius.objects.Actor attacker, SequenceAction seq, Spells spell, int minDamage, int maxDamage) {

        for (andius.objects.Actor player : screen.partyMembers) {

            if (!player.getPlayer().savingThrowSpell()) {

                seq.addAction(Actions.delay(.60f));

                int damage = Utils.getRandomBetween(minDamage, maxDamage);
                player.adjustHP(-damage);

                seq.addAction(Actions.run(new LogAction(screen, String.format("%s deals %d damage to %s", spell, damage, player.getPlayer().name))));

                final Actor expl = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(spell.getColor()));
                expl.setX(player.getX() + 12);
                expl.setY(player.getY() + 12);
                expl.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
                seq.addAction(Actions.run(new AddActorAction(screen.getStage(), expl)));
            } else {
                seq.addAction(Actions.run(new LogAction(screen, player.getPlayer().name + " made a saving throw and is unaffected!")));
            }

        }

    }

}
