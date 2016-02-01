package utils;

import andius.Constants.Direction;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class Utils {

    public static Texture fillRectangle(int width, int height, Color color, float alpha) {
        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pix.setColor(color.r, color.g, color.b, alpha);
        pix.fillRectangle(0, 0, width, height);
        Texture t = new Texture(pix);
        pix.dispose();
        return t;
    }
    
    public static Direction getPath(int width, int height, int toX, int toY, int validMovesMask, boolean towards, int fromX, int fromY) {
        // find the directions that lead [to/away from] our target 
        int directionsToObject = towards ? getRelativeDirection(width, height, toX, toY, fromX, fromY) : ~getRelativeDirection(width, height, toX, toY, fromX, fromY);

        // make sure we eliminate impossible options 
        directionsToObject &= validMovesMask;

        // get the new direction to move 
        if (directionsToObject > 0) {
            return Direction.getRandomValidDirection(directionsToObject);
        } else {
            // there are no valid directions that lead to our target            
            return null;
        }
    }

    /**
     * Finds the movement distance (not using diagonals) from point a to point b
     * on a map, taking into account map boundaries and such.
     */
    public static int movementDistance(int width, int height, int fromX, int fromY, int toX, int toY) {
        int dirmask = 0;;
        int dist = 0;

        /* get the direction(s) to the coordinates */
        dirmask = getRelativeDirection(width, height, toX, toY, fromX, fromY);

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

    /**
     * Returns a mask of directions that indicate where one point is relative to
     * another. For instance, if the object at (x, y) is northeast of (c.x,
     * c.y), then this function returns (MASK_DIR(DIR_NORTH) |
     * MASK_DIR(DIR_EAST)) This function also takes into account map boundaries
     * and adjusts itself accordingly.
     */
    public static int getRelativeDirection(int width, int height, int toX, int toY, int fromX, int fromY) {
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

}
