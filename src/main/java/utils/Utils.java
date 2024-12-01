package utils;

import andius.Constants.Status;
import andius.Direction;
import andius.objects.Item;
import andius.objects.MutableMonster;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import java.util.Random;

public class Utils {

    public static final Random RANDOM = new Random();

    //This gives you a random number in between low (inclusive) and high (exclusive)
    public static int getRandomBetween(int low, int high) {
        return RANDOM.nextInt(high - low) + low;
    }

    public static boolean randomBoolean() {
        return RANDOM.nextInt(2) == 1;
    }

    public static int adjustValueMax(int v, int amt, int max) {
        v += amt;
        if (v > max) {
            v = max;
        }
        return v;
    }

    public static int adjustValueMin(int v, int amt, int min) {
        v += amt;
        if (v < min) {
            v = min;
        }
        return v;
    }

    public static int adjustValue(int v, long amt, int max, int min) {
        v += amt;
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

    public static boolean attackHit(MutableMonster attacker, CharacterRecord defender) {
        int attackValue = RANDOM.nextInt(20) + 1;
        int defenseValue = 20 - defender.calculateAC() - attacker.getLevel() - (defender.status.isDisabled() ? 3 : 0);

        if (defenseValue < 1) {
            defenseValue = 1;
        }

        if (defenseValue > 19) {
            defenseValue = 19;
        }

        //System.out.printf("%s AC[%d] result: %s (%d/%d)\n", 
        //        attacker.name, defender.calculateAC(), attackValue >= defenseValue ? AttackResult.HIT : AttackResult.MISS, attackValue, defenseValue);
        return attackValue >= defenseValue;
    }

    public static boolean attackHit(CharacterRecord attacker, MutableMonster defender) {
        int strMod = 0;
        if (attacker.str <= 3) {
            strMod = -3;
        } else if (attacker.str == 4) {
            strMod = -2;
        } else if (attacker.str == 5) {
            strMod = -1;
        } else if (attacker.str == 16) {
            strMod = 1;
        } else if (attacker.str == 17) {
            strMod = 2;
        } else if (attacker.str >= 18) {
            strMod = 3;
        }
        int attackValue = (RANDOM.nextInt(20) + 1) + strMod;
        int defenseValue = 21 - defender.getArmourClass() + defender.getACModifier() - (defender.status().isDisabled() ? 3 : 0);

        if (attackValue < 1) {
            attackValue = 1;
        }

        if (attackValue > 19) {
            attackValue = 19;
        }

        if (defenseValue < 1) {
            defenseValue = 1;
        }

        if (defenseValue > 19) {
            defenseValue = 19;
        }

        //System.out.printf("%s AC[%d] result: %s (%d/%d)\n",
        //        attacker.name, defender.getArmourClass(),
        //        attackValue >= defenseValue ? AttackResult.HIT : AttackResult.MISS, attackValue, defenseValue);
        return attackValue >= defenseValue;
    }

    public static int dealDamage(Item weapon, MutableMonster defender) {
        int damage = weapon.damage.roll() + (defender.status().isDisabled() ? 5 : 0); //add 5 points to the damage if the defender is not in OK status
        defender.setCurrentHitPoints(defender.getCurrentHitPoints() - damage);
        defender.adjustHealthBar();
        return damage;
    }

    public static int dealSpellDamage(int hits, int range, int bonus) {
        int points = 0;
        while (hits > 0) {
            points += RANDOM.nextInt(range + 1);
            hits--;
        }
        points += bonus;
        return points;
    }
}
