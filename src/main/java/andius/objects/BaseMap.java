package andius.objects;

import andius.Constants.Direction;
import andius.Constants.Map;
import andius.GameScreen;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import utils.PartyDeathException;
import utils.Utils;

public class BaseMap {

    private int width;
    private int height;
    private final java.util.Map<Map, Portal> portals = new HashMap<>();
    public final List<Creature> creatures = new ArrayList<>();

    //used to keep the pace of wandering to every 2 moves instead of every move, 
    //otherwise cannot catch up and talk to the character
    private long wanderFlag = 0;

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

    public Creature getCreatureAt(int x, int y) {
        for (Creature cr : creatures) {
            if (cr.getWx() == x && cr.getWy() == y) {
                return cr;
            }
        }
        return null;
    }

    public void moveObjects(Map map, GameScreen screen, int avatarX, int avatarY) throws PartyDeathException {

        wanderFlag++;

        for (Creature p : creatures) {

            Vector3 pos = null;
            Vector3 pixelPos = null;
            Direction dir = null;

            switch (p.getMovement()) {
                case ATTACK_AVATAR: {
                    int dist = Utils.movementDistance(width, height, p.getWx(), p.getWy(), avatarX, avatarY);
                    if (dist <= 1) {
                        //combat
                        continue;
                    }
                    int mask = getValidMovesMask(map, p.getWx(), p.getWy(), p, avatarX, avatarY);
                    dir = Utils.getPath(width, height, avatarX, avatarY, mask, true, p.getWx(), p.getWy());
                }
                break;
                case FOLLOW_AVATAR: {
                    int mask = getValidMovesMask(map, p.getWx(), p.getWy(), p, avatarX, avatarY);
                    dir = Utils.getPath(width, height, avatarX, avatarY, mask, true, p.getWx(), p.getWy());
                }
                break;
                case FIXED:
                    break;
                case WANDER: {
                    if (wanderFlag % 2 == 0) {
                        //continue;
                    }
//                    if (p.isTalking()) {
//                        continue;
//                    }

                    dir = Direction.getRandomValidDirection(getValidMovesMask(map, p.getWx(), p.getWy(), p, avatarX, avatarY));
                }
                break;
                default:
                    break;

            }

            if (dir == null) {
                continue;
            }
            if (dir == Direction.NORTH) {
                pos = new Vector3(p.getWx(), p.getWy() - 1, 0);
            }
            if (dir == Direction.SOUTH) {
                pos = new Vector3(p.getWx(), p.getWy() + 1, 0);
            }
            if (dir == Direction.EAST) {
                pos = new Vector3(p.getWx() + 1, p.getWy(), 0);
            }
            if (dir == Direction.WEST) {
                pos = new Vector3(p.getWx() - 1, p.getWy(), 0);
            }
            pixelPos = screen.getMapPixelCoords((int) pos.x, (int) pos.y);
            p.setX(pixelPos.x);
            p.setY(pixelPos.y);
            p.setWx((int) pos.x);
            p.setWy((int) pos.y);

        }
    }

    public int getValidMovesMask(Map map, int x, int y, Creature cr, int avatarX, int avatarY) {

        int mask = 0;
        
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getTiledMap().getLayers().get("floor");
        TiledMapTileLayer.Cell north = layer.getCell(x, map.getMap().getHeight() - 1 - y - 1);
        TiledMapTileLayer.Cell south = layer.getCell(x, map.getMap().getHeight() - 1 - y + 1);
        TiledMapTileLayer.Cell east = layer.getCell(x + 1, map.getMap().getHeight() - 1 - y);
        TiledMapTileLayer.Cell west = layer.getCell(x - 1, map.getMap().getHeight() - 1 - y);

        mask = addToMask(Direction.NORTH, mask, north, x, y - 1, cr, avatarX, avatarY);
        mask = addToMask(Direction.SOUTH, mask, south, x, y + 1, cr, avatarX, avatarY);
        mask = addToMask(Direction.EAST, mask, east, x + 1, y, cr, avatarX, avatarY);
        mask = addToMask(Direction.WEST, mask, west, x - 1, y, cr, avatarX, avatarY);

        return mask;

    }
    
    private int addToMask(Direction dir, int mask, TiledMapTileLayer.Cell cell, int x, int y, Creature cr, int avatarX, int avatarY) {
        System.out.printf("addToMask: %s %s\n", dir, cell);
        if (cell != null) {
            mask = Direction.addToMask(dir, mask);
        }
        return mask;
    }


}
