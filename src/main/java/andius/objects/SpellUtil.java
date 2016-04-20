package andius.objects;

import andius.Andius;
import andius.BaseScreen;
import andius.CombatScreen;
import andius.Constants;
import andius.Constants.AddActorAction;
import andius.Constants.PlaySoundAction;
import static andius.Constants.TILE_DIM;
import andius.Context;
import andius.Direction;
import andius.GameScreen;
import andius.Sound;
import andius.Sounds;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import utils.Utils;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.Color;
import java.util.Random;
import utils.XORShiftRandom;

public class SpellUtil {

    private static final Random rand = new XORShiftRandom();

    public static boolean spellCast(final BaseScreen screen, final Context context, final Spells spell,
            final andius.objects.Actor caster, final andius.objects.Actor subject, final Direction dir) {

        if (caster == null || spell == null || screen == null) {
            return false;
        }

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
                    case HALITO:
                        break;
                    case MOGREF:
                        break;
                    case KATINO:
                        break;
                    case DUMAPIC:
                        break;
                    case DILTO:
                        break;
                    case SOPIC:
                        break;
                    case MAHALITO:
                        break;
                    case MOLITO:
                        break;
                    case MORLIS:
                        break;
                    case DALTO:
                        break;
                    case LAHALITO:
                        break;
                    case MAMORLIS:
                        break;
                    case MAKANITO:
                        break;
                    case MADALTO:
                        break;
                    case LAKANITO:
                        break;
                    case ZILWAN:
                        break;
                    case MASOPIC:
                        break;
                    case HAMAN:
                        break;
                    case MALOR:
                        break;
                    case MAHAMAN:
                        break;
                    case TILTOWAIT:
                        break;
                    case KALKI:
                        break;
                    case DIOS:
                        break;
                    case BADIOS:
                        ((CombatScreen)screen).animateMagicAttack(caster, dir, spell);
                        break;
                    case MILWA:
                        break;
                    case PORFIC:
                        break;
                    case MATU:
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
                    case BAMATU:
                        break;
                    case DIAL:
                        break;
                    case BADIAL:
                        break;
                    case LATUMOFIS:
                        break;
                    case MAPORFIC:
                        break;
                    case DIALMA:
                        break;
                    case BADIALMA:
                        break;
                    case LITOKAN:
                        break;
                    case KANDI:
                        break;
                    case DI:
                        break;
                    case BADI:
                        break;
                    case LORTO:
                        break;
                    case MADI:
                        break;
                    case MABADI:
                        break;
                    case LOKTOFEIT:
                        break;
                    case MALIKTO:
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

    public static void spellGroupDamage(BaseScreen screen, CharacterRecord caster, int minDamage, int maxDamage) {

//        final CombatScreen combatScreen = (CombatScreen) screen;
//
//        SequenceAction seq = Actions.action(SequenceAction.class);
//
//        for (Creature cr : combatScreen.combatMap.getCreatures()) {
//
//            Utils.dealDamage(caster, cr, Utils.getRandomBetween(minDamage, maxDamage));
//            Tile tile = Exodus.baseTileSet.getTileByName("hit_flash");
//            Drawable d = new Drawable(combatScreen.combatMap, cr.currentX, cr.currentY, tile, Exodus.standardAtlas);
//            d.setX(cr.currentPos.x);
//            d.setY(cr.currentPos.y);
//            d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));
//
//            seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
//            seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
//
//            if (cr.getDamageStatus() == CreatureStatus.DEAD) {
//                seq.addAction(Actions.run(combatScreen.new RemoveCreatureAction(cr)));
//            }
//
//        }
//
//        seq.addAction(Actions.run(new Runnable() {
//            @Override
//            public void run() {
//                combatScreen.finishPlayerTurn();
//            }
//        }));
//
//        combatScreen.getStage().addAction(seq);
    }

    public static void spellUndead(BaseScreen screen, CharacterRecord caster) {

//        SequenceAction seq = Actions.action(SequenceAction.class);
//
//        final CombatScreen combatScreen = (CombatScreen) screen;
//
//        int level = caster.getPlayer().getLevel();
//
//        for (Creature cr : combatScreen.combatMap.getCreatures()) {
//
//            boolean turn = Utils.rand.nextInt(100) >= 50;
//
//            if (level > 5) {
//                turn = Utils.rand.nextInt(100) >= 35;
//            }
//            if (level > 10) {
//                turn = Utils.rand.nextInt(100) >= 20;
//            }
//            if (level > 15) {
//                turn = true;
//            }
//
//            if (cr.getUndead() && turn) {
//                Tile tile = Exodus.baseTileSet.getTileByName("hit_flash");
//                Drawable d = new Drawable(combatScreen.combatMap, cr.currentX, cr.currentY, tile, Exodus.standardAtlas);
//                d.setX(cr.currentPos.x);
//                d.setY(cr.currentPos.y);
//
//                d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));
//
//                seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
//                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
//
//                Utils.dealDamage(caster, cr, cr.getBasehp());
//            }
//
//            if (cr.getDamageStatus() == CreatureStatus.DEAD) {
//                seq.addAction(Actions.run(combatScreen.new RemoveCreatureAction(cr)));
//            }
//
//        }
//
//        seq.addAction(Actions.run(new Runnable() {
//            @Override
//            public void run() {
//                combatScreen.finishPlayerTurn();
//            }
//        }));
//
//        combatScreen.getStage().addAction(seq);
    }

    public static void spellRepond(BaseScreen screen, CharacterRecord caster) {

//        SequenceAction seq = Actions.action(SequenceAction.class);
//
//        final CombatScreen combatScreen = (CombatScreen) screen;
//
//        int level = caster.getPlayer().getLevel();
//
//        for (Creature cr : combatScreen.combatMap.getCreatures()) {
//
//            boolean turn = Utils.rand.nextInt(100) >= 50;
//
//            if (level > 5) {
//                turn = Utils.rand.nextInt(100) >= 35;
//            }
//            if (level > 10) {
//                turn = Utils.rand.nextInt(100) >= 20;
//            }
//            if (level > 15) {
//                turn = true;
//            }
//
//            if (turn && (cr.getTile() == CreatureType.troll || cr.getTile() == CreatureType.orc || cr.getTile() == CreatureType.gremlin)) {
//
//                Tile tile = Exodus.baseTileSet.getTileByName("hit_flash");
//                Drawable d = new Drawable(combatScreen.combatMap, cr.currentX, cr.currentY, tile, Exodus.standardAtlas);
//                d.setX(cr.currentPos.x);
//                d.setY(cr.currentPos.y);
//
//                d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));
//
//                seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
//                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
//
//                Utils.dealDamage(caster, cr, cr.getBasehp());
//            }
//
//            if (cr.getDamageStatus() == CreatureStatus.DEAD) {
//                seq.addAction(Actions.run(combatScreen.new RemoveCreatureAction(cr)));
//            }
//
//        }
//        
//        seq.addAction(Actions.run(new Runnable() {
//            @Override
//            public void run() {
//                combatScreen.finishPlayerTurn();
//            }
//        }));
//
//        combatScreen.getStage().addAction(seq);
    }

    public static void spellView(BaseScreen screen, CharacterRecord caster) {
//        InputProcessor ip = screen.getPeerGemInputProcessor();
//        if (ip != null) {
//            Gdx.input.setInputProcessor(ip);
//        }
    }

    public static void spellXit(BaseScreen screen, CharacterRecord caster) {
//        if (screen.scType == ScreenType.DUNGEON) {
//            DungeonScreen dngScreen = (DungeonScreen) screen;
//            screen.log("Leaving " + dngScreen.dngMap.getLabel());
//            if (Exodus.mainGame != null) {
//                Exodus.mainGame.setScreen(dngScreen.gameScreen);
//            }
//        }
    }

    public static void spellYup(BaseScreen screen, CharacterRecord caster) {
//        DungeonScreen dngScreen = (DungeonScreen) screen;
//
//        dngScreen.currentLevel--;
//
//        if (dngScreen.currentLevel < 0) {
//            dngScreen.currentLevel = 0;
//            Exodus.mainGame.setScreen(dngScreen.gameScreen);
//        } else {
//
//            for (int i = 0; i < 32; i++) {
//                int x = Utils.rand.nextInt(16);
//                int y = Utils.rand.nextInt(16);
//                if (dngScreen.validTeleportLocation(x, y, dngScreen.currentLevel)) {
//                    dngScreen.currentPos = new Vector3(x + .5f, .5f, y + .5f);
//                    dngScreen.camera.position.set(dngScreen.currentPos);
//                    if (dngScreen.currentDir == Direction.EAST) {
//                        dngScreen.camera.lookAt(dngScreen.currentPos.x + 1, dngScreen.currentPos.y, dngScreen.currentPos.z);
//                    } else if (dngScreen.currentDir == Direction.WEST) {
//                        dngScreen.camera.lookAt(dngScreen.currentPos.x - 1, dngScreen.currentPos.y, dngScreen.currentPos.z);
//                    } else if (dngScreen.currentDir == Direction.NORTH) {
//                        dngScreen.camera.lookAt(dngScreen.currentPos.x, dngScreen.currentPos.y, dngScreen.currentPos.z - 1);
//                    } else if (dngScreen.currentDir == Direction.SOUTH) {
//                        dngScreen.camera.lookAt(dngScreen.currentPos.x, dngScreen.currentPos.y, dngScreen.currentPos.z + 1);
//                    }
//                    dngScreen.moveMiniMapIcon();
//                    break;
//                }
//            }
//
//            dngScreen.createMiniMap();
//        }

    }

    public static void spellZdown(BaseScreen screen, CharacterRecord caster) {

//        DungeonScreen dngScreen = (DungeonScreen) screen;
//
//        dngScreen.currentLevel++;
//
//        if (dngScreen.currentLevel > DungeonScreen.DUNGEON_LVLS) {
//
//            dngScreen.currentLevel = DungeonScreen.DUNGEON_LVLS;
//
//        } else {
//
//            for (int i = 0; i < 32; i++) {
//                int x = Utils.rand.nextInt(16);
//                int y = Utils.rand.nextInt(16);
//                if (dngScreen.validTeleportLocation(x, y, dngScreen.currentLevel)) {
//                    dngScreen.currentPos = new Vector3(x + .5f, .5f, y + .5f);
//                    dngScreen.camera.position.set(dngScreen.currentPos);
//                    if (dngScreen.currentDir == Direction.EAST) {
//                        dngScreen.camera.lookAt(dngScreen.currentPos.x + 1, dngScreen.currentPos.y, dngScreen.currentPos.z);
//                    } else if (dngScreen.currentDir == Direction.WEST) {
//                        dngScreen.camera.lookAt(dngScreen.currentPos.x - 1, dngScreen.currentPos.y, dngScreen.currentPos.z);
//                    } else if (dngScreen.currentDir == Direction.NORTH) {
//                        dngScreen.camera.lookAt(dngScreen.currentPos.x, dngScreen.currentPos.y, dngScreen.currentPos.z - 1);
//                    } else if (dngScreen.currentDir == Direction.SOUTH) {
//                        dngScreen.camera.lookAt(dngScreen.currentPos.x, dngScreen.currentPos.y, dngScreen.currentPos.z + 1);
//                    }
//                    dngScreen.moveMiniMapIcon();
//                    break;
//                }
//            }
//
//            dngScreen.createMiniMap();
//        }
    }

    public static void spellLibRec(BaseScreen screen, CharacterRecord caster) {

//        DungeonScreen dngScreen = (DungeonScreen) screen;
//
//        for (int i = 0; i < 32; i++) {
//            int x = Utils.rand.nextInt(16);
//            int y = Utils.rand.nextInt(16);
//            if (dngScreen.validTeleportLocation(x, y, dngScreen.currentLevel)) {
//                dngScreen.currentPos = new Vector3(x + .5f, .5f, y + .5f);
//                dngScreen.camera.position.set(dngScreen.currentPos);
//                if (dngScreen.currentDir == Direction.EAST) {
//                    dngScreen.camera.lookAt(dngScreen.currentPos.x + 1, dngScreen.currentPos.y, dngScreen.currentPos.z);
//                } else if (dngScreen.currentDir == Direction.WEST) {
//                    dngScreen.camera.lookAt(dngScreen.currentPos.x - 1, dngScreen.currentPos.y, dngScreen.currentPos.z);
//                } else if (dngScreen.currentDir == Direction.NORTH) {
//                    dngScreen.camera.lookAt(dngScreen.currentPos.x, dngScreen.currentPos.y, dngScreen.currentPos.z - 1);
//                } else if (dngScreen.currentDir == Direction.SOUTH) {
//                    dngScreen.camera.lookAt(dngScreen.currentPos.x, dngScreen.currentPos.y, dngScreen.currentPos.z + 1);
//                }
//                dngScreen.moveMiniMapIcon();
//                break;
//            }
//        }
//
//        dngScreen.createMiniMap();
    }

    public static void destoryAllCreatures(BaseScreen screen, CharacterRecord caster) {

//        if (screen.scType == ScreenType.MAIN) {
//
//            final GameScreen gameScreen = (GameScreen) screen;
//
//            SequenceAction seq = Actions.action(SequenceAction.class);
//
//            for (final Creature cr : screen.context.getCurrentMap().getCreatures()) {
//
//                Utils.dealDamage(caster, cr, 255);
//
//                Tile tile = Exodus.baseTileSet.getTileByName("hit_flash");
//                Drawable d = new Drawable(screen.context.getCurrentMap(), cr.currentX, cr.currentY, tile, Exodus.standardAtlas);
//                d.setX(cr.currentPos.x);
//                d.setY(cr.currentPos.y);
//                d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));
//
//                seq.addAction(Actions.run(new AddActorAction(gameScreen.getStage(), d)));
//                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
//                seq.addAction(Actions.run(new Runnable() {
//                    @Override
//                    public void run() {
//                        screen.context.getCurrentMap().getCreatures().remove(cr);
//                    }
//                }));
//
//            }
//
//            gameScreen.getStage().addAction(seq);
//
//        }
    }

    public static void useMaskOfMinax(BaseScreen screen, CharacterRecord caster) {

//        final CombatScreen combatScreen = (CombatScreen) screen;
//
//        final SequenceAction seq = Actions.action(SequenceAction.class);
//
//        for (Creature cr : combatScreen.combatMap.getCreatures()) {
//
//            if (Utils.rand.nextInt(3) == 0) {
//                Utils.dealDamage(caster, cr, 255);
//            } else {
//                if (cr.getHP() > 23) {
//                    Utils.dealDamage(caster, cr, cr.getHP() * (3 / 4));
//                } else {
//                    Utils.dealDamage(caster, cr, 15);
//                }
//            }
//
//            Actor d = new CloudDrawable();
//            d.setX(cr.currentPos.x - 16);
//            d.setY(cr.currentPos.y - 16);
//            d.addAction(Actions.sequence(Actions.delay(2f), Actions.removeActor()));
//
//            seq.addAction(Actions.run(new PlaySoundAction(Sound.SPIRITS)));
//            seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
//            seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
//
//            if (cr.getDamageStatus() == CreatureStatus.DEAD) {
//                seq.addAction(Actions.run(combatScreen.new RemoveCreatureAction(cr)));
//            }
//
//        }
//        
//        seq.addAction(Actions.run(new Runnable() {
//            @Override
//            public void run() {
//                combatScreen.finishPlayerTurn();
//            }
//        }));
//        
//        combatScreen.getStage().addAction(seq);
    }

    public static void useRageOfGod(BaseScreen screen, CharacterRecord caster) {

//        final CombatScreen combatScreen = (CombatScreen) screen;
//
//        final SequenceAction seq = Actions.action(SequenceAction.class);
//
//        for (Creature cr : combatScreen.combatMap.getCreatures()) {
//
//            if (Utils.rand.nextInt(2) == 0) {
//                Utils.dealDamage(caster, cr, 255);
//            } else if (Utils.rand.nextInt(2) == 0) {
//                if (cr.getHP() > 23) {
//                    Utils.dealDamage(caster, cr, cr.getHP() - 23);
//                }
//            } else {
//                Utils.dealDamage(caster, cr, cr.getHP() / 2);
//            }
//
//            Actor d = new ExplosionLargeDrawable();
//            d.setX(cr.currentPos.x - 32 * 3 + 16);
//            d.setY(cr.currentPos.y - 32 * 3 + 16);
//            d.addAction(Actions.sequence(Actions.delay(2f), Actions.removeActor()));
//
//            seq.addAction(Actions.run(new PlaySoundAction(Sound.RAGE)));
//            seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
//            seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
//
//            if (cr.getDamageStatus() == CreatureStatus.DEAD) {
//                seq.addAction(Actions.run(combatScreen.new RemoveCreatureAction(cr)));
//            }
//
//        }
//
//        seq.addAction(Actions.run(new Runnable() {
//            @Override
//            public void run() {
//                combatScreen.finishPlayerTurn();
//            }
//        }));
//        
//        combatScreen.getStage().addAction(seq);
    }

    private static int modSpellAC(CharacterRecord rec, int acmod, int low, int high) {
        int ac = rec.calculateAC();
        for (int i = low; i < high; i++) {
            ac += acmod;
        }
        return ac;
    }

    private static void doSpellHeal(BaseScreen sc, CharacterRecord rec, Spells spell) {
        int points = Utils.calcPoints(spell.getHitCount(), spell.getHitRange(), 0);
        sc.log(rec.name + " is healed.");
        rec.adjustHP(points);
    }

}
