package utils;

import andius.Constants.AttackResult;
import andius.Constants.AttackVector;
import andius.Constants.Direction;
import andius.Constants.WeaponType;
import andius.objects.BaseMap;
import andius.objects.Creature;
import andius.objects.Player;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {

    public static final Random RAND = new XORShiftRandom();

    //This gives you a random number in between low (inclusive) and high (exclusive)
    public static int getRandomBetween(int low, int high) {
        return RAND.nextInt(high - low) + low;
    }

    public static int adjustValueMax(int v, int val, int max) {
        v += val;
        if (v > max) {
            v = max;
        }
        return v;
    }

    public static int adjustValueMin(int v, int val, int min) {
        v += val;
        if (v < min) {
            v = min;
        }
        return v;
    }

    public static int adjustValue(int v, int val, int max, int min) {
        v += val;
        if (v > max) {
            v = max;
        }
        if (v < min) {
            v = min;
        }
        return v;
    }

    public static Texture fillRectangle(int width, int height, Color color, float alpha) {
        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pix.setColor(color.r, color.g, color.b, alpha);
        pix.fillRectangle(0, 0, width, height);
        Texture t = new Texture(pix);
        pix.dispose();
        return t;
    }

    public static Direction getPath(int toX, int toY, int validMovesMask, boolean towards, int fromX, int fromY) {
        // find the directions that lead [to/away from] our target 
        int directionsToObject = towards ? getRelativeDirection(toX, toY, fromX, fromY) : ~getRelativeDirection(toX, toY, fromX, fromY);

        // make sure we eliminate impossible options 
        directionsToObject &= validMovesMask;

        // get the new direction to move 
        if (directionsToObject > 0) {
            return Direction.getRandomValidDirection(directionsToObject);
        } else {
            // there are no valid directions that lead to our target            
            return Direction.getRandomValidDirection(validMovesMask);
        }
    }

    public static int movementDistance(int fromX, int fromY, int toX, int toY) {
        int dirmask = 0;;
        int dist = 0;

        /* get the direction(s) to the coordinates */
        dirmask = getRelativeDirection(toX, toY, fromX, fromY);

        while (fromX != toX || fromY != toY) {

            if (fromX != toX) {
                if (Direction.isDirInMask(Direction.WEST, dirmask)) {
                    fromX--;
                } else {
                    fromX++;
                }
                dist++;
            }
            if (fromY != toY) {
                if (Direction.isDirInMask(Direction.NORTH, dirmask)) {
                    fromY--;
                } else {
                    fromY++;
                }
                dist++;
            }

        }

        return dist;
    }

    public static int getRelativeDirection(int toX, int toY, int fromX, int fromY) {
        int dx = 0, dy = 0;
        int dirmask = 0;

        /* adjust our coordinates to find the closest path */
        dx = fromX - toX;
        dy = fromY - toY;

        /* add x directions that lead towards to_x to the mask */
        if (dx < 0) {
            dirmask |= Direction.EAST.getMask();
        } else if (dx > 0) {
            dirmask |= Direction.WEST.getMask();
        }

        /* add y directions that lead towards to_y to the mask */
        if (dy < 0) {
            dirmask |= Direction.SOUTH.getMask();
        } else if (dy > 0) {
            dirmask |= Direction.NORTH.getMask();
        }

        /* return the result */
        return dirmask;
    }
    
    public static AttackResult attackHit(Creature attacker, Player defender) {
        int attackValue = RAND.nextInt(256);
        int defenseValue = defender.getDefense();
        return attackValue > defenseValue ? AttackResult.HIT : AttackResult.MISS;
    }

    private static AttackResult attackHit(Player attacker, Creature defender) {
        int attackValue = RAND.nextInt(256) + attacker.getAttackBonus();
        int defenseValue = defender.getDefense();
        return attackValue > defenseValue ? AttackResult.HIT : AttackResult.MISS;
    }

    public static boolean dealDamage(Player attacker, Creature defender, int damage) {
        int xp = defender.getExp();
        if (!damageCreature(defender, damage, true)) {
            attacker.getCharRec().awardXP(xp);
            return false;
        }
        return true;
    }

    public static boolean dealDamage(Creature attacker, Player defender) throws PartyDeathException {
        int damage = attacker.getDamage();
        return defender.applyDamage(damage, true);
    }
    
    private static boolean damageCreature(Creature cr, int damage, boolean byplayer) {

        cr.setHP(Utils.adjustValueMin(cr.getHP(), -damage, 0));

        switch (cr.getDamageStatus()) {

            case DEAD:
                if (byplayer) {
                    //Exodus.hud.add(String.format("%s Killed! Exp. %d", cr.getName(), cr.getExp()));
                } else {
                    //Exodus.hud.add(String.format("%s Killed!", cr.getName()));
                }
                return false;
            case FLEEING:
               //Exodus.hud.add(String.format("%s Fleeing!", cr.getName()));
                break;

            case CRITICAL:
               // Exodus.hud.add(String.format("%s Critical!", cr.getName()));
                break;

            case HEAVILYWOUNDED:
                //Exodus.hud.add(String.format("%s Heavily Wounded!", cr.getName()));
                break;

            case LIGHTLYWOUNDED:
                //Exodus.hud.add(String.format("%s Lightly Wounded!", cr.getName()));
                break;

            case BARELYWOUNDED:
                //Exodus.hud.add(String.format("%s Barely Wounded!", cr.getName()));
                break;
            case FINE:
                break;
            default:
                break;
        }

        return true;
    }

    private static AttackVector attack(BaseMap map, Player attacker, Direction dir, int x, int y) {

        WeaponType wt = attacker.getCharRec().weapon;

        List<AttackVector> path = getDirectionalActionPath(map, dir.getMask(), x, y, 1, wt.getRange());

        AttackVector target = null;
        boolean foundTarget = false;
        int distance = 1;
        for (AttackVector v : path) {
            AttackResult res = attackAt(map, v, attacker, distance);
            target = v;
            target.result = res;
            target.distance = distance;
            if (res != AttackResult.NONE) {
                foundTarget = true;
                break;
            }
            distance++;
        }

        return target;
    }

    private static AttackResult attackAt(BaseMap map, AttackVector target, Player attacker, int distance) {
        AttackResult res = AttackResult.NONE;
        
        Creature creature = map.getCreatureAt(target.x, target.y);

        WeaponType wt = attacker.getCharRec().weapon;
        boolean wrongRange = (distance > wt.getRange());

        if (creature == null || wrongRange) {
            return res;
        }
        
        res = attackHit(attacker, creature);

        if (res == AttackResult.HIT) {
            dealDamage(attacker, creature, attacker.getDamage());
        }

        return res;
    }

    public static List<AttackVector> getDirectionalActionPath(BaseMap combatMap, int dirmask, int x, int y, int minDistance, int maxDistance) {

        List<AttackVector> path = new ArrayList<>();

        /*
         * try every tile in the given direction, up to the given range.
         * Stop when the the range is exceeded, or the action is blocked.
         */
        int nx = x;
        int ny = y;

        for (int distance = minDistance; distance <= maxDistance; distance++) {

            /* make sure our action isn't taking us off the map */
            if (nx > combatMap.getWidth() - 1 || nx < 0 || ny > combatMap.getHeight() - 1 || ny < 0) {
                break;
            }

            boolean blocked = false;//combatMap.isTileBlockedForRangedAttack(nx, ny, checkForCreatures);

            if (!blocked) {
                path.add(new AttackVector(nx, ny));
            } else {
                path.add(new AttackVector(nx, ny));
                break;
            }

            if (Direction.isDirInMask(Direction.NORTH, dirmask)) {
                ny--;
            }
            if (Direction.isDirInMask(Direction.SOUTH, dirmask)) {
                ny++;
            }
            if (Direction.isDirInMask(Direction.EAST, dirmask)) {
                nx++;
            }
            if (Direction.isDirInMask(Direction.WEST, dirmask)) {
                nx--;
            }

        }

        return path;
    }

}
