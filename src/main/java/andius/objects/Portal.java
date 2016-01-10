package andius.objects;

import andius.Constants.Map;

public class Portal {

    private final Map map;
    private final int x;
    private final int y;

    public Portal(Map map, int x, int y) {
        this.map = map;
        this.x = x;
        this.y = y;
    }

    public Map getMap() {
        return map;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}
