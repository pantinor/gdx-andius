package andius.objects;

import andius.Andius;
import andius.CombatScreen;
import andius.CombatScreen.RemoveCreatureAction;
import andius.Constants.AddActorAction;
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
import java.util.concurrent.atomic.AtomicInteger;
import utils.XORShiftRandom;

public class SpellUtil {

    private static final Random rand = new XORShiftRandom();

    public static void spellCast(CombatScreen screen, Context context, Spells spell, andius.objects.Actor caster, andius.objects.Actor target) {

        SequenceAction seq = Actions.action(SequenceAction.class);

        try {

            if (!caster.getPlayer().canCast(spell)) {
                screen.log("Thou dost not have enough magic points!");
                Sounds.play(Sound.NEGATIVE_EFFECT);
                return;
            }

            if (caster.getPlayer().silencedCountdown.get() > 0) {
                screen.log("Silenced!");
                Sounds.play(Sound.NEGATIVE_EFFECT);
                return;
            }

            caster.getPlayer().decrMagicPts(spell);

            seq.addAction(Actions.run(new PlaySoundAction(spell.getSound())));
            seq.addAction(Actions.delay(0.5f));

            switch (spell) {
                case KATINO:
                    for (andius.objects.Actor m : screen.enemies) {
                        seq.addAction(Actions.delay(.60f));
                        if (rand.nextInt(100) < m.getMonster().getUnaffected()) {
                            seq.addAction(Actions.run(new LogAction(screen, m.getMonster().name + " is unaffected.")));
                            seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
                        } else {
                            m.getMonster().setStatus(Status.ASLEEP);
                            seq.addAction(Actions.run(new LogAction(screen, m.getMonster().name + " is asleep.")));
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
                        if (rand.nextInt(100) < m.getMonster().getUnaffected()) {
                            seq.addAction(Actions.run(new LogAction(screen, m.getMonster().name + " is unaffected.")));
                            seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
                        } else {
                            m.getMonster().setStatus(Status.PARALYZED);
                            seq.addAction(Actions.run(new LogAction(screen, m.getMonster().name + " is paralyzed.")));
                            seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
                        }
                    }
                    break;
                case MONTINO:
                    for (andius.objects.Actor m : screen.enemies) {
                        seq.addAction(Actions.delay(.60f));
                        if (rand.nextInt(100) < m.getMonster().getUnaffected()) {
                            seq.addAction(Actions.run(new LogAction(screen, m.getMonster().name + " is unaffected.")));
                            seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
                        } else {
                            m.getMonster().setStatus(Status.SILENCED);
                            seq.addAction(Actions.run(new LogAction(screen, m.getMonster().name + " is silenced.")));
                            seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
                        }
                    }
                    break;
                case LATUMAPIC:
                    for (andius.objects.Actor m : screen.partyMembers) {
                        if (m.getPlayer().status == Status.ASLEEP || m.getPlayer().status == Status.PARALYZED || m.getPlayer().status == Status.SILENCED) {
                            m.getPlayer().status = Status.OK;
                        }
                    }
                    break;
                case DIOS:
                case DIAL:
                case DIALMA:
                case MADI:
                    doSpellHeal(screen, seq, target, spell);
                    break;
                case LATUMOFIS:
                case DIALKO:
                    if (target.getPlayer().status == Status.POISONED) {
                        target.getPlayer().status = Status.OK;
                    }
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
                //TODO
                case LOMILWA:
                case CALFO:
                case MILWA:
                case KANDI:
                case DI:
                case LOKTOFEIT:
                case KADORTO:
                case HAMAN:
                case MALOR:
                case MAHAMAN:
                case DUMAPIC:
                    seq.addAction(Actions.run(new LogAction(screen, "NOT IMPLEMENTED YET SORRY")));
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

    private static void projectileMagicAttack(CombatScreen screen, SequenceAction seq, andius.objects.Actor attacker, Spells spell, andius.objects.Actor target) {

        int targetX = target.getWx();
        int targetY = target.getWy();

        int a = Math.abs(attacker.getWx() - targetX);
        int b = Math.abs(attacker.getWy() - targetY);

        ProjectileActor p = new ProjectileActor(spell.getColor(), attacker.getX(), attacker.getY());

        if (rand.nextInt(100) < target.getMonster().getUnaffected()
                || (spell == Spells.ZILWAN && target.getMonster().getType() != Monster.Type.UNDEAD)) {
            seq.addAction(Actions.run(new LogAction(screen, target.getMonster().name + " is unaffected.")));
        } else {
            spellDamage(spell, target.getMonster());
            seq.addAction(Actions.run(new LogAction(screen, String.format("%s %s", target.getMonster().name, target.getMonster().getDamageTag()))));
        }

        Actor expl = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(spell.getColor()));
        expl.setX(target.getX() + 12);
        expl.setY(target.getY() + 12);
        expl.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

        seq.addAction(Actions.run(new AddActorAction(screen.getStage(), expl)));

        if (target.getMonster().getCurrentHitPoints() <= 0) {
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

    private static void spellDamage(Spells spell, MutableMonster m) {
        int damage = Utils.dealSpellDamage(spell.getHitCount(), spell.getHitRange(), spell.getHitBonus());
        m.setCurrentHitPoints(m.getCurrentHitPoints() - damage);
        m.adjustHealthBar();
    }

    private static void spellGroupDamage(CombatScreen screen, SequenceAction seq, Spells spell) {

        for (andius.objects.Actor m : screen.enemies) {

            seq.addAction(Actions.delay(.60f));

            if (rand.nextInt(100) < m.getMonster().getUnaffected()) {
                seq.addAction(Actions.run(new LogAction(screen, m.getMonster().name + " is unaffected.")));

                final Actor expl = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(Color.GRAY));
                expl.setX(m.getX() + 12);
                expl.setY(m.getY() + 12);
                expl.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

                seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
                seq.addAction(Actions.run(new AddActorAction(screen.getStage(), expl)));

            } else {

                int damage = Utils.dealSpellDamage(spell.getHitCount(), spell.getHitRange(), spell.getHitBonus());
                m.getMonster().setCurrentHitPoints(m.getMonster().getCurrentHitPoints() - damage);
                m.getMonster().adjustHealthBar();

                seq.addAction(Actions.run(new LogAction(screen, String.format("%s affects %s %s", spell, m.getMonster().name, m.getMonster().getDamageTag()))));

                final Actor expl = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(spell.getColor()));
                expl.setX(m.getX() + 12);
                expl.setY(m.getY() + 12);
                expl.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
                seq.addAction(Actions.run(new AddActorAction(screen.getStage(), expl)));

                if (m.getMonster().getCurrentHitPoints() <= 0) {
                    seq.addAction(Actions.run(new RemoveCreatureAction(screen, m)));
                }

            }
        }

    }

    private static void spellGroupDamage(CombatScreen screen, SequenceAction seq, Spells spell, int minDamage, int maxDamage) {

        for (andius.objects.Actor m : screen.enemies) {

            seq.addAction(Actions.delay(.60f));

            int damage = Utils.getRandomBetween(minDamage, maxDamage);
            m.getMonster().setCurrentHitPoints(m.getMonster().getCurrentHitPoints() - damage);
            m.getMonster().adjustHealthBar();

            seq.addAction(Actions.run(new LogAction(screen, String.format("%s affects %s %s", spell, m.getMonster().name, m.getMonster().getDamageTag()))));

            final Actor expl = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(spell.getColor()));
            expl.setX(m.getX() + 12);
            expl.setY(m.getY() + 12);
            expl.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

            seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
            seq.addAction(Actions.run(new AddActorAction(screen.getStage(), expl)));

            if (m.getMonster().getCurrentHitPoints() <= 0) {
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
            m.getMonster().setACModifier(-acmod);
        }
    }

    private static void doSpellHeal(CombatScreen screen, SequenceAction seq, andius.objects.Actor target, Spells spell) {
        CharacterRecord rec = target.getPlayer();
        if (rec.status != Status.DEAD) {

            seq.addAction(Actions.run(new LogAction(screen, rec.name + " is healed.")));

            if (spell == Spells.MADI) {
                rec.status = Status.OK;
                target.adjustHP(rec.maxhp);
            } else {
                int points = Utils.dealSpellDamage(spell.getHitCount(), spell.getHitRange(), 0);
                target.adjustHP(points);
            }
        } else {
            seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
        }
    }

    public static void spellMonsterCast(CombatScreen screen, SequenceAction seq, Spells spell,
            andius.objects.Actor caster, andius.objects.Actor target) {

        if (caster.getMonster().getStatus() != Status.OK) {
            screen.log(caster.getMonster().getStatus().toString().toLowerCase() + "!");
            Sounds.play(Sound.NEGATIVE_EFFECT);
            return;
        }

        if (!caster.getMonster().canCast(spell)) {
            screen.log(caster.getMonster().name + " does not have enough magic points!");
            Sounds.play(Sound.NEGATIVE_EFFECT);
            return;
        }

        caster.getMonster().decrMagicPts(spell);

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
                    monsterCastEffect(screen, seq, caster.getMonster(), m.getPlayer(), m.getPlayer().asleepCountdown, "asleep");
                }
                break;
            case MANIFO:
                for (andius.objects.Actor m : screen.partyMembers) {
                    monsterCastEffect(screen, seq, caster.getMonster(), m.getPlayer(), m.getPlayer().paralyzedCountdown, "paralyzed");
                }
                break;
            case MONTINO:
                for (andius.objects.Actor m : screen.partyMembers) {
                    monsterCastEffect(screen, seq, caster.getMonster(), m.getPlayer(), m.getPlayer().silencedCountdown, "silenced");
                }
                break;
            case DIOS:
            case DIAL:
            case DIALMA:
            case MADI:
                doMonsterHeal(screen, seq, caster.getMonster(), spell);
                break;
            case LATUMAPIC:
                for (andius.objects.Actor m : screen.enemies) {
                    if (m.getMonster().getStatus() == Status.ASLEEP || m.getMonster().getStatus() == Status.PARALYZED || m.getMonster().getStatus() == Status.SILENCED) {
                        m.getMonster().resetStatus();
                    }
                }
                break;
            case MOGREF:
            case SOPIC:
            case PORFIC:
                caster.getMonster().setACModifier(caster.getMonster().getACModifier() + spell.getHitBonus());
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

    private static void monsterCastEffect(CombatScreen screen, SequenceAction seq, MutableMonster monster, CharacterRecord target, AtomicInteger counter, String effect) {
        seq.addAction(Actions.delay(.60f));
        boolean hit = Utils.attackHit(monster, target);
        if (hit) {
            seq.addAction(Actions.run(new LogAction(screen, target.name + " is unaffected.")));
            seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
        } else {
            counter.set(4);
            seq.addAction(Actions.run(new LogAction(screen, target.name + " is " + effect + ".")));
            seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
        }
    }

    private static void doMonsterHeal(CombatScreen screen, SequenceAction seq, MutableMonster target, Spells spell) {
        if (target.getCurrentHitPoints() > 0) {
            seq.addAction(Actions.run(new LogAction(screen, target.name + " is healed.")));
            if (spell == Spells.MADI) {
                target.setCurrentHitPoints(target.getMaxHitPoints());
            } else {
                int healAmt = Utils.dealSpellDamage(spell.getHitCount(), spell.getHitRange(), 0);
                int current = target.getCurrentHitPoints() + healAmt;
                target.setCurrentHitPoints(current > target.getMaxHitPoints() ? target.getMaxHitPoints() : current);
            }
            target.adjustHealthBar();
        } else {
            seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
        }
    }

    private static void monstersMagicAttack(CombatScreen screen, SequenceAction seq, andius.objects.Actor attacker, Spells spell, andius.objects.Actor target) {

        int targetX = target.getWx();
        int targetY = target.getWy();

        int a = Math.abs(attacker.getWx() - targetX);
        int b = Math.abs(attacker.getWy() - targetY);

        ProjectileActor p = new ProjectileActor(spell.getColor(), attacker.getX(), attacker.getY());

        if (rand.nextInt(100) < attacker.getMonster().getUnaffected()) {
            seq.addAction(Actions.run(new LogAction(screen, target.getPlayer().name + " is unaffected.")));
        } else {
            int damage = Utils.dealSpellDamage(spell.getHitCount(), spell.getHitRange(), spell.getHitBonus());
            target.adjustHP(damage);
            seq.addAction(Actions.run(new LogAction(screen, String.format("%s takes %d damage.", target.getPlayer().name, damage))));
        }

        Actor expl = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(spell.getColor()));
        expl.setX(target.getX() + 12);
        expl.setY(target.getY() + 12);
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

        p.addAction(Actions.sequence(Actions.moveTo(target.getX(), target.getY(), .3f, Interpolation.sineIn), after));

        screen.getStage().addActor(p);
    }

    private static void monstersGroupDamage(CombatScreen screen, andius.objects.Actor attacker, SequenceAction seq, Spells spell) {

        for (andius.objects.Actor m : screen.partyMembers) {

            seq.addAction(Actions.delay(.60f));

            if (rand.nextInt(100) < attacker.getMonster().getUnaffected()) {
                seq.addAction(Actions.run(new LogAction(screen, m.getPlayer().name + " is unaffected.")));

                final Actor expl = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(Color.GRAY));
                expl.setX(m.getX() + 12);
                expl.setY(m.getY() + 12);
                expl.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

                seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
                seq.addAction(Actions.run(new AddActorAction(screen.getStage(), expl)));

            } else {

                int damage = Utils.dealSpellDamage(spell.getHitCount(), spell.getHitRange(), spell.getHitBonus());
                m.adjustHP(damage);

                seq.addAction(Actions.run(new LogAction(screen, String.format("%s deals %d damage to %s", spell, damage, m.getPlayer().name))));

                final Actor expl = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(spell.getColor()));
                expl.setX(m.getX() + 12);
                expl.setY(m.getY() + 12);
                expl.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
                seq.addAction(Actions.run(new AddActorAction(screen.getStage(), expl)));

            }
        }

    }

    private static void monstersGroupDamage(CombatScreen screen, andius.objects.Actor attacker, SequenceAction seq, Spells spell, int minDamage, int maxDamage) {

        for (andius.objects.Actor m : screen.partyMembers) {

            seq.addAction(Actions.delay(.60f));

            int damage = Utils.getRandomBetween(minDamage, maxDamage);
            m.adjustHP(damage);

            seq.addAction(Actions.run(new LogAction(screen, String.format("%s deals %d damage to %s", spell, damage, m.getPlayer().name))));

            final Actor expl = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(spell.getColor()));
            expl.setX(m.getX() + 12);
            expl.setY(m.getY() + 12);
            expl.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

            seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
            seq.addAction(Actions.run(new AddActorAction(screen.getStage(), expl)));

        }

    }

}
