package andius.objects;

import andius.Andius;
import andius.BaseScreen;
import andius.CombatScreen;
import andius.Constants.AddActorAction;
import andius.Constants.PlaySoundAction;
import andius.Context;
import andius.Direction;
import andius.Sound;
import andius.Sounds;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import utils.Utils;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import java.util.Random;
import utils.XORShiftRandom;

public class SpellUtil {

    private static final Random rand = new XORShiftRandom();

    public static boolean spellCast(final BaseScreen screen, final Context context, final Spells spell,
            final andius.objects.Actor caster, final andius.objects.Actor subject, final Direction dir) {

        if (!caster.getPlayer().canCast(spell)) {
            screen.log("Thou dost not have enough magic points!");
            Sounds.play(Sound.NEGATIVE_EFFECT);
            return false;
        }

//        if (context.getAura().getType() == AuraType.NEGATE) {
//            Exodus.hud.add("Spell is negated!");
//            return false;
//        }
        caster.getPlayer().decrMagicPts(spell);

        SequenceAction seq = Actions.action(SequenceAction.class);
        seq.addAction(Actions.run(new PlaySoundAction(spell.getSound())));
        seq.addAction(Actions.delay(0.5f));
        seq.addAction(Actions.run(new Runnable() {
            @Override
            public void run() {
                switch (spell) {
                    case MOGREF:
                    case SOPIC:
                        caster.getPlayer().acmodifier1 = spell.getHitBonus();
                        break;
                    case KATINO:
                        break;
                    case DUMAPIC:
                        break;
                    case DILTO:
                    case MORLIS:
                    case MAMORLIS:
                        modMonsterAC((CombatScreen) screen, spell.getHitBonus());
                        break;
                    case MAKANITO:
                        break;
                    case LAKANITO:
                        break;
                    case HAMAN:
                        break;
                    case MALOR:
                        break;
                    case MAHAMAN:
                        break;
                    case KALKI:
                    case MATU:
                    case BAMATU:
                    case MASOPIC:
                        modPartyAC1((CombatScreen) screen, spell.getHitBonus());
                        break;
                    case DIOS:
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
                        spellGroupDamage((CombatScreen) screen, caster, spell);
                        break;
                    case HALITO:
                    case BADIAL:
                    case BADIALMA:
                    case ZILWAN:
                    case BADIOS:
                        ((CombatScreen) screen).animateMagicAttack(caster, dir, spell);
                        break;
                    case MILWA:
                        break;
                    case PORFIC:
                        caster.getPlayer().acmodifier1 = spell.getHitBonus();
                        break;
                    case CALFO:
                        break;
                    case MANIFO:
                        break;
                    case MONTINO:
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
                        modPartyAC2((CombatScreen) screen, spell.getHitBonus());
                        break;
                    case DIALMA:
                        break;
                    case KANDI:
                        break;
                    case DI:
                        break;
                    case BADI:
                        break;
                    case MADI:
                        break;
                    case MABADI:
                        break;
                    case LOKTOFEIT:
                        break;
                    case KADORTO:
                        break;

                }
            }
        }));

        screen.getStage().addAction(seq);

        return true;
    }

    public static void spellBlink(BaseScreen screen, Direction dir) {

//        if (screen.scType == ScreenType.MAIN) {
//
//            GameScreen gameScreen = (GameScreen) screen;
//            BaseMap bm = screen.context.getCurrentMap();
//
//            Vector3 v = gameScreen.getCurrentMapCoords();
//            int x = (int) v.x;
//            int y = (int) v.y;
//
//            if (bm.getId() != Maps.SOSARIA.getId()) {
//                return;
//            }
//
//            int distance = 0;
//            int diff = 0;
//            Direction reverseDir = Direction.reverse(dir);
//
//            int var = (dir.getMask() & (Direction.WEST.getMask() | Direction.EAST.getMask())) > 0 ? x : y;
//
//            /* find the distance we are going to move */
//            distance = (var) % 0x10;
//            if (dir == Direction.EAST || dir == Direction.SOUTH) {
//                distance = 0x10 - distance;
//            }
//
//            /* see if we move another 16 spaces over */
//            diff = 0x10 - distance;
//            if ((diff > 0) && (Utils.rand.nextInt(diff * diff) > distance)) {
//                distance += 0x10;
//            }
//
//            /* test our distance, and see if it works */
//            for (int i = 0; i < distance; i++) {
//                if (dir == Direction.NORTH) {
//                    y--;
//                }
//                if (dir == Direction.SOUTH) {
//                    y++;
//                }
//                if (dir == Direction.WEST) {
//                    x--;
//                }
//                if (dir == Direction.EAST) {
//                    x++;
//                }
//            }
//
//            int i = distance;
//            /* begin walking backward until you find a valid spot */
//            while ((i-- > 0) && bm.getTile(x, y) != null && bm.getTile(x, y).getRule().has(TileAttrib.unwalkable)) {
//                if (reverseDir == Direction.NORTH) {
//                    y--;
//                }
//                if (reverseDir == Direction.SOUTH) {
//                    y++;
//                }
//                if (reverseDir == Direction.WEST) {
//                    x--;
//                }
//                if (reverseDir == Direction.EAST) {
//                    x++;
//                }
//            }
//
//            if (bm.getTile(x, y) != null && !bm.getTile(x, y).getRule().has(TileAttrib.unwalkable)) {
//
//                /* we didn't move! */
//                if (x == (int) v.x && y == (int) v.y) {
//                    screen.log("Failed to teleport!");
//                }
//
//                gameScreen.newMapPixelCoords = gameScreen.getMapPixelCoords(x, y);
//                gameScreen.recalcFOV(bm, x, y);
//
//            } else {
//                screen.log("Failed to teleport!");
//            }
//        } else {
//            screen.log("Outdoors only!");
//        }
    }

    public static void spellGroupDamage(final CombatScreen screen, andius.objects.Actor caster, Spells spell) {
        SequenceAction seq = Actions.action(SequenceAction.class);

        for (andius.objects.Actor m : screen.enemies) {

            if (rand.nextInt(100) < m.getMonster().getUnaffected()) {
                screen.log(m.getMonster().name + " is unaffected.");
                seq.addAction(Actions.sequence(Actions.delay(.5f), Actions.run(new PlaySoundAction(Sound.EVADE))));
            } else {
                int damage = Utils.dealSpellDamage(spell.getHitCount(), spell.getHitRange(), spell.getHitBonus());
                m.getMonster().setCurrentHitPoints(m.getMonster().getCurrentHitPoints() - damage);
                m.getMonster().adjustHealthBar();
                screen.log(String.format("%s affects %s %s", spell, m.getMonster().name, m.getMonster().getDamageTag()));

                final Actor d = new Andius.ExplosionDrawable(Andius.EXPLMAP.get(spell.getColor()));
                d.setX(m.getX() + 12);
                d.setY(m.getY() + 12);
                d.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
                seq.addAction(Actions.run(new AddActorAction(screen.getStage(), d)));
                if (m.getMonster().getCurrentHitPoints() <= 0) {
                    seq.addAction(Actions.run(screen.new RemoveCreatureAction(m)));
                }

            }
        }

        screen.getStage().addAction(seq);

        screen.log("Any key to continue...");
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyUp(int keycode) {
                Gdx.input.setInputProcessor(new InputMultiplexer(screen, screen.hudStage));
                screen.finishPlayerTurn();
                return false;
            }
        });

    }

    private static void modPartyAC1(CombatScreen screen, int acmod) {
        for (andius.objects.Actor player : screen.partyMembers) {
            if (!player.getPlayer().isDisabled()) {
                player.getPlayer().acmodifier1 = acmod;
            }
        }
    }

    private static void modPartyAC2(CombatScreen screen, int acmod) {
        for (andius.objects.Actor player : screen.partyMembers) {
            if (!player.getPlayer().isDisabled()) {
                player.getPlayer().acmodifier2 = acmod;
            }
        }
    }

    private static void modMonsterAC(CombatScreen screen, int acmod) {
        for (andius.objects.Actor m : screen.enemies) {
            m.getMonster().setACModifier(acmod);
        }
    }

    private static void doSpellHeal(BaseScreen sc, CharacterRecord rec, Spells spell) {
        int points = Utils.dealSpellDamage(spell.getHitCount(), spell.getHitRange(), 0);
        sc.log(rec.name + " is healed.");
        rec.adjustHP(points);
    }

}
