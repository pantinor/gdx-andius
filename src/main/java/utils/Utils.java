package utils;

import andius.Constants;
import andius.Direction;
import andius.objects.Item;
import andius.objects.Mutable;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import java.util.Random;

public class Utils {

    public static final Random RANDOM = new Random();

    //This gives you a random number in between low (inclusive) and high (exclusive)
    public static int getRandomBetween(int low, int high) {
        if (low == high) {
            return low;
        }
        return RANDOM.nextInt(high - low) + low;
    }

    public static boolean randomBoolean() {
        return RANDOM.nextInt(100) < 50;
    }
    
    public static boolean percentChance(int percent) {
        return RANDOM.nextInt(100 + 1) > percent;
    }

    public static int intValue(byte b1) {
        return b1 & 0xFF;
    }

    public static int intValue(byte b1, byte b2) {
        return intValue(b1) + intValue(b2) * 256;
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

    public static boolean distanceLessThan(float val, float x, float y) {
        return Math.abs(x - y) <= val;
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

    public static boolean attackHit(Mutable attacker, Mutable defender) {
        int roll = RANDOM.nextInt(20) + 1;

        if (roll == 20) {
            return true;
        }

        if (roll == 1) {
            return false;
        }

        int THAC0 = 0;
        if (attacker.getLevel() > Constants.THAC0_MONSTER.length) {
            THAC0 = Constants.THAC0_MONSTER[Constants.THAC0_MONSTER.length - 1];
        } else {
            THAC0 = Constants.THAC0_MONSTER[attacker.getLevel() - 1];
        }

        int chanceToHit = THAC0 - defender.getArmourClass();

        chanceToHit += (defender.status().isDisabled() ? 3 : 0);

        chanceToHit += attacker.hitModifier();

        return roll >= chanceToHit;
    }

    public static boolean attackHit(Mutable attacker, CharacterRecord defender) {
        int roll = RANDOM.nextInt(20) + 1;

        if (roll == 20) {
            return true;
        }

        if (roll == 1) {
            return false;
        }

        int THAC0 = 0;
        if (attacker.getLevel() > Constants.THAC0_MONSTER.length) {
            THAC0 = Constants.THAC0_MONSTER[Constants.THAC0_MONSTER.length - 1];
        } else {
            THAC0 = Constants.THAC0_MONSTER[attacker.getLevel() - 1];
        }

        int chanceToHit = THAC0 - defender.weapon.armourClass;

        chanceToHit -= defender.calculateAC();

        chanceToHit += (defender.status.isDisabled() ? 3 : 0);

        chanceToHit += attacker.hitModifier();

        return roll >= chanceToHit;
    }

    public static boolean attackHit(CharacterRecord attacker, Mutable defender) {

        if (defender == null) {
            return false;
        }

        int roll = (RANDOM.nextInt(20) + 1);

        if (roll == 20) {
            return true;
        }

        if (roll == 1) {
            return false;
        }

        int THAC0 = 0;
        switch (attacker.classType) {
            case FIGHTER:
            case SAMURAI:
            case LORD:
            case NINJA:
                THAC0 = Constants.THACO_FIGHTER[attacker.level];
                break;
            case MAGE:
            case BISHOP:
                THAC0 = Constants.THACO_MAGE[attacker.level];
                break;
            case PRIEST:
                THAC0 = Constants.THACO_PRIEST[attacker.level];
                break;
            case THIEF:
                THAC0 = Constants.THACO_THIEF[attacker.level];
                break;
        }

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

        THAC0 -= strMod;

        THAC0 -= attacker.weapon.wephitmd;

        int chanceToHit = THAC0 - defender.getArmourClass();

        chanceToHit -= defender.getACModifier();

        chanceToHit += (defender.status().isDisabled() ? 3 : 0);

        return roll >= chanceToHit;
    }

    public static int dealDamage(Item weapon, Mutable defender) {
        int damage = weapon.damage.roll() + (defender.status().isDisabled() ? 5 : 0); //add 5 points to the damage if the defender is not in OK status
        defender.setCurrentHitPoints(defender.getCurrentHitPoints() - damage);
        defender.adjustHealthCursor();
        return damage;
    }

    public static Vector2 centerOfMass(TextureRegion tr) {

        int sx = tr.getRegionX();
        int sy = tr.getRegionY();
        int w = tr.getRegionWidth();
        int h = tr.getRegionHeight();

        if (!tr.getTexture().getTextureData().isPrepared()) {
            tr.getTexture().getTextureData().prepare();
        }
        Pixmap p = tr.getTexture().getTextureData().consumePixmap();

        int x1 = 0, x2 = 0, y1 = 0, y2 = 0;

        for (int x = sx; x < sx + h; x++) {
            for (int y = sy; y < sy + h; y++) {
                if (p.getPixel(x, y) != 0) {
                    x1 = x;
                    break;
                }
            }
        }
        for (int x = sx + w - 1; x >= sx; x--) {
            for (int y = sy; y < sy + h; y++) {
                if (p.getPixel(x, y) != 0) {
                    x2 = x;
                    break;
                }
            }
        }
        for (int y = sy; y < sy + h; y++) {
            for (int x = sx; x < sx + w; x++) {
                if (p.getPixel(x, y) != 0) {
                    y1 = y;
                    break;
                }
            }
        }
        for (int y = sy + h - 1; y >= sy; y--) {
            for (int x = sx; x < sx + w; x++) {
                if (p.getPixel(x, y) != 0) {
                    y2 = y;
                    break;
                }
            }
        }

        p.dispose();

        int cx = x1 + (x2 - x1) / 2 - sx;
        int cy = y1 + (y2 - y1) / 2 - sy;

        return new Vector2(cx, h - cy);
    }

    public static Texture rotate90(Texture t) {
        if (!t.getTextureData().isPrepared()) {
            t.getTextureData().prepare();
        }
        Pixmap p = t.getTextureData().consumePixmap();
        int width = p.getWidth();
        int height = p.getHeight();
        Pixmap rotatedPix = new Pixmap(height, width, p.getFormat());

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                rotatedPix.drawPixel(x, y, p.getPixel(y, x));
            }
        }

        Texture rotated = new Texture(rotatedPix);

        p.dispose();
        return rotated;

    }

    public static Texture reverse(Texture t) {
        if (!t.getTextureData().isPrepared()) {
            t.getTextureData().prepare();
        }
        Pixmap p = t.getTextureData().consumePixmap();
        Pixmap newp = new Pixmap(p.getWidth(), p.getHeight(), p.getFormat());

        for (int x = 0; x < p.getWidth(); x++) {
            for (int y = 0; y < p.getHeight(); y++) {
                newp.drawPixel(x, y, p.getPixel(p.getWidth() - 1 - x, p.getHeight() - 1 - y));
            }
        }

        Texture newt = new Texture(newp);

        p.dispose();
        return newt;
    }

}
