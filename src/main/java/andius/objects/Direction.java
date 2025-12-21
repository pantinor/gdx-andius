package andius.objects;

import static utils.Utils.RANDOM;

public enum Direction {

    WEST(1, 0x1, 0),
    NORTH(2, 0x2, 90),
    EAST(3, 0x4, 180),
    SOUTH(4, 0x8, 270),
    NORTH_WEST(5, 0x10, 45),
    NORTH_EAST(6, 0x20, 135),
    SOUTH_WEST(7, 0x40, 315),
    SOUTH_EAST(8, 0x80, 225);

    private int val;
    private int mask;
    private int degree;

    private Direction(int v, int mask, int degree) {
        this.val = v;
        this.mask = mask;
        this.degree = degree;
    }
    
    public int value() {
        return val;
    }

    public int mask() {
        return mask;
    }
    
    public int degree() {
        return this.degree;
    }

    public static boolean isDirInMask(Direction dir, int mask) {
        int v = (mask & dir.mask);
        return (v > 0);
    }

    public static boolean isDirInMask(int dir, int mask) {
        int v = (mask & dir);
        return (v > 0);
    }

    public static int addToMask(Direction dir, int mask) {
        return (dir.mask | mask);
    }

    public static int removeFromMask(int mask, Direction... dirs) {
        for (Direction dir : dirs) {
            mask &= ~dir.mask();
        }
        return mask;
    }

    public static Direction getRandomValidDirection(int mask) {
        int n = 0;
        Direction d[] = new Direction[4];
        for (Direction dir : values()) {
            if (isDirInMask(dir, mask)) {
                d[n] = dir;
                n++;
            }
        }
        if (n == 0) {
            return null;
        }
        int rand = RANDOM.nextInt(n);
        return d[rand];
    }

    public static Direction reverse(Direction dir) {
        switch (dir) {
            case WEST:
                return EAST;
            case NORTH:
                return SOUTH;
            case EAST:
                return WEST;
            case SOUTH:
                return NORTH;
        }
        return null;
    }

    public static Direction getByValue(int val) {
        Direction ret = null;
        for (Direction d : Direction.values()) {
            if (val == d.value()) {
                ret = d;
                break;
            }
        }
        return ret;
    }

    public static Direction getByMask(int mask) {
        Direction ret = null;
        for (Direction d : Direction.values()) {
            if (mask == d.mask) {
                ret = d;
                break;
            }
        }
        return ret;
    }

};
