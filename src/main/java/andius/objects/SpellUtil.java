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
import utils.XORShiftRandom;

public class SpellUtil {

    private static final Random rand = new XORShiftRandom();

    public static void spellCast(CombatScreen screen, Context context, Spells spell,
            andius.objects.Actor caster, andius.objects.Actor target) {

        SequenceAction seq = Actions.action(SequenceAction.class);
        
        try {

            if (!caster.getPlayer().canCast(spell)) {
                screen.log("Thou dost not have enough magic points!");
                Sounds.play(Sound.NEGATIVE_EFFECT);
                return;
            }
            
            caster.getPlayer().decrMagicPts(spell);

            seq.addAction(Actions.run(new PlaySoundAction(spell.getSound())));
            seq.addAction(Actions.delay(0.5f));

            switch (spell) {
                case MOGREF:
                case SOPIC:
                    caster.getPlayer().acmodifier1 = spell.getHitBonus();
                    break;
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
                case DILTO:
                case MORLIS:
                case MAMORLIS:
                    modMonsterAC(screen, seq, spell.getHitBonus());
                    break;
                case MAKANITO:
                    spellGroupDamage(screen, seq, spell, 35, 40);
                    break;
                case LAKANITO:
                    spellGroupDamage(screen, seq, spell, 45, 65);
                    break;
                case KALKI:
                case MATU:
                case BAMATU:
                case MASOPIC:
                    modPartyAC1(screen, seq, spell.getHitBonus());
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
                case PORFIC:
                    caster.getPlayer().acmodifier1 = spell.getHitBonus();
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

                case DIOS:
                case DIAL:
                case DIALMA:
                case MADI:
                    doSpellHeal(screen, seq, target.getPlayer(), spell);
                    break;
                case LATUMOFIS:
                case DIALKO:
                    if (target.getPlayer().status == Status.POISONED) {
                        target.getPlayer().status = Status.OK;
                    }
                    break;
                case MAPORFIC:
                    modPartyAC2(screen, seq, spell.getHitBonus());
                    break;

                //TODO
                case LOMILWA:
                case CALFO:
                case MILWA:
                case LATUMAPIC:
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

    private static void modPartyAC1(CombatScreen screen, SequenceAction seq, int acmod) {
        for (andius.objects.Actor player : screen.partyMembers) {
            if (!player.getPlayer().isDisabled()) {
                player.getPlayer().acmodifier1 = acmod;
            }
        }
    }

    private static void modPartyAC2(CombatScreen screen, SequenceAction seq, int acmod) {
        for (andius.objects.Actor player : screen.partyMembers) {
            if (!player.getPlayer().isDisabled()) {
                player.getPlayer().acmodifier2 = acmod;
            }
        }
    }

    private static void modMonsterAC(CombatScreen screen, SequenceAction seq, int acmod) {
        for (andius.objects.Actor m : screen.enemies) {
            m.getMonster().setACModifier(acmod);
        }
    }

    private static void doSpellHeal(CombatScreen screen, SequenceAction seq, CharacterRecord target, Spells spell) {
        if (target.status != Status.DEAD) {

            seq.addAction(Actions.run(new LogAction(screen, target.name + " is healed.")));

            if (spell == Spells.MADI) {
                target.status = Status.OK;
                target.adjustHP(target.maxhp);
            } else {
                int points = Utils.dealSpellDamage(spell.getHitCount(), spell.getHitRange(), 0);
                target.adjustHP(points);
            }
        } else {
            seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
        }
    }

}
