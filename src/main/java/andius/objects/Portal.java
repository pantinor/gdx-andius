package andius.objects;

import andius.Constants.Map;

public class Portal {

    private final Map map;
    private final int sx;
    private final int sy;
    private final int dx;
    private final int dy;

    public Portal(Map map, int sx, int sy, int dx, int dy) {
        this.map = map;
        this.sx = sx;
        this.sy = sy;
        this.dx = dx;
        this.dy = dy;
    }

    public Map getMap() {
        return this.map;
    }

    public int getSx() {
        return sx;
    }

    public int getSy() {
        return sy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }
    
    

}
