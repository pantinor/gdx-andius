package andius.objects;

import andius.Constants.Map;
import java.util.HashMap;

public class BaseMap {

    private int width;
    private int height;
    private float[][] shadownMap;
    private final java.util.Map<Map, Portal> portals = new HashMap<>();

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float[][] getShadownMap() {
        return shadownMap;
    }

    public void setShadownMap(float[][] shadownMap) {
        this.shadownMap = shadownMap;
    }

    public void addPortal(Map map, int x, int y) {
        portals.put(map, new Portal(map, x, y));
    }

    public Portal getPortal(Map map) {
        return portals.get(map);
    }

    public Portal getPortal(int x, int y) {
        for (Portal p : portals.values()) {
            if (p.getX() == x && p.getY() == y) {
                return p;
            }
        }
        return null;
    }

}
