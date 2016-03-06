package andius.objects;

import andius.Constants.Map;
import andius.Direction;
import andius.GameScreen;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.List;
import utils.PartyDeathException;
import utils.Utils;

public class BaseMap {

    private int width;
    private int height;
    private final List<Portal> portals = new ArrayList<>();
    public final List<Actor> actors = new ArrayList<>();

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

    public void addPortal(Map map, int sx, int sy, int dx, int dy, List<Vector3> randoms) {
        portals.add(new Portal(map, sx, sy, dx, dy, randoms));
    }
    
    public Portal getPortal(int sx, int sy) {
        for (Portal p : portals) {
            if (p.getSx() == sx && p.getSy() == sy) {
                return p;
            }
        }
        return null;
    }

    public Actor getCreatureAt(int x, int y) {
        for (Actor cr : actors) {
            if (cr.getWx() == x && cr.getWy() == y) {
                return cr;
            }
        }
        return null;
    }

    public void removeCreature(Actor cr) {
        actors.remove(cr);
    }

    public void moveObjects(Map map, GameScreen screen, int avatarX, int avatarY) throws PartyDeathException {

        wanderFlag++;

        for (Actor p : actors) {

            Direction dir = null;

            switch (p.getMovement()) {
                case ATTACK_AVATAR: {
                    int dist = Utils.movementDistance(p.getWx(), p.getWy(), avatarX, avatarY);
                    if (dist <= 1) {
                        //combat
                        continue;
                    } else if (dist >= 4) {
                        //dont move until close enough
                        continue;
                    }
                    int mask = getValidMovesMask(map, p.getWx(), p.getWy(), p, avatarX, avatarY);
                    dir = Utils.getPath(avatarX, avatarY, mask, true, p.getWx(), p.getWy());
                }
                break;
                case FOLLOW_AVATAR: {
                    int mask = getValidMovesMask(map, p.getWx(), p.getWy(), p, avatarX, avatarY);
                    dir = Utils.getPath(avatarX, avatarY, mask, true, p.getWx(), p.getWy());
                }
                break;
                case FIXED:
                    break;
                case WANDER: {
                    if (wanderFlag % 2 == 0) {
                        continue;
                    }
//                    if (p.isTalking()) {
//                        continue;
//                    }

                    dir = Direction.getRandomValidDirection(getValidMovesMask(map, p.getWx(), p.getWy(), p, avatarX, avatarY));
                }
                default:
                    break;

            }

            if (dir == null) {
                continue;
            }

            switch (dir) {
                case NORTH:
                    p.setWy(p.getWy() - 1);
                    break;
                case SOUTH:
                    p.setWy(p.getWy() + 1);
                    break;
                case EAST:
                    p.setWx(p.getWx() + 1);
                    break;
                case WEST:
                    p.setWx(p.getWx() - 1);
                    break;
                default:
                    break;
            }

            Vector3 pixelPos = screen.getMapPixelCoords(p.getWx(), p.getWy() + 1);
            p.setX(pixelPos.x);
            p.setY(pixelPos.y);

        }
    }

    public int getValidMovesMask(Map map, int x, int y, Actor cr, int avatarX, int avatarY) {
        int mask = 0;

        TiledMapTileLayer layer = (TiledMapTileLayer) map.getTiledMap().getLayers().get("floor");
        TiledMapTileLayer.Cell north = layer.getCell(x, height - 1 - y + 1);
        TiledMapTileLayer.Cell south = layer.getCell(x, height - 1 - y - 1);
        TiledMapTileLayer.Cell west = layer.getCell(x - 1, height - 1 - y + 0);
        TiledMapTileLayer.Cell east = layer.getCell(x + 1, height - 1 - y + 0);

        mask = addToMask(Direction.NORTH, mask, north, x, y - 1, cr, avatarX, avatarY);
        mask = addToMask(Direction.SOUTH, mask, south, x, y + 1, cr, avatarX, avatarY);
        mask = addToMask(Direction.WEST, mask, west, x - 1, y, cr, avatarX, avatarY);
        mask = addToMask(Direction.EAST, mask, east, x + 1, y, cr, avatarX, avatarY);

        return mask;
    }

    private int addToMask(Direction dir, int mask, TiledMapTileLayer.Cell cell, int x, int y, Actor cr, int avatarX, int avatarY) {
        if (cell != null) {

            for (Actor c : this.actors) {
                if (c.getWx() == x && c.getWy() == y) {
                    return mask;
                }
            }

            if (avatarX == x && avatarY == y) {
                return mask;
            }

            mask = Direction.addToMask(dir, mask);
        }
        return mask;
    }

}
