package andius;

import andius.WizardryData.MazeAddress;
import andius.WizardryData.MazeCell;
import andius.WizardryData.MazeLevel;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static utils.Utils.CLASSPTH_RSLVR;

public class TmxWizardryLevel extends MazeLevel {

    public TmxWizardryLevel(String tmxFile, int level) {

        TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
        TiledMap tiledMap = loader.load("assets/data/" + tmxFile);

        this.level = level;
        this.dimension = tiledMap.getProperties().get("width", Integer.class);
        this.cells = new MazeCell[dimension][dimension];

        for (int column = 0; column < dimension; column++) {
            for (int row = 0; row < dimension; row++) {
                this.cells[row][column] = new MazeCell(new MazeAddress(level, row, column));
            }
        }

        TiledMapTileLayer walls = (TiledMapTileLayer) tiledMap.getLayers().get("walls");
        for (int column = 0; column < dimension; column++) {
            for (int row = 0; row < dimension; row++) {
                //TMX is X and Y-down and wizardry is north and east with origin bottom left
                MazeCell mc = this.cells[column][row];
                TiledMapTileLayer.Cell cell = walls.getCell(row, column);
                if (cell != null && cell.getTile() != null) {
                    int tid = cell.getTile().getId() - 1;
                    switch (tid) {
                        case 0:
                            mc.northWall = true;
                            mc.southWall = true;
                            mc.eastWall = true;
                            mc.westWall = true;
                            break;
                        case 1:
                            mc.southWall = true;
                            mc.eastWall = true;
                            mc.westWall = true;
                            break;
                        case 2:
                            mc.northWall = true;
                            mc.southWall = true;
                            mc.westWall = true;
                            break;
                        case 3:
                            mc.northWall = true;
                            mc.eastWall = true;
                            mc.westWall = true;
                            break;
                        case 4:
                            mc.northWall = true;
                            mc.southWall = true;
                            mc.eastWall = true;
                            break;
                        case 16:
                            mc.westWall = true;
                            break;
                        case 17:
                            mc.eastWall = true;
                            break;
                        case 18:
                            mc.northWall = true;
                            break;
                        case 19:
                            mc.southWall = true;
                            break;
                        case 20:
                            mc.eastWall = true;
                            mc.westWall = true;
                            break;
                        case 32:
                            mc.northWall = true;
                            mc.southWall = true;
                            break;
                        case 33:
                            mc.westWall = true;
                            mc.southWall = true;
                            break;
                        case 34:
                            mc.northWall = true;
                            mc.eastWall = true;
                            break;
                        case 35:
                            mc.northWall = true;
                            mc.westWall = true;
                            break;
                        case 36:
                            mc.southWall = true;
                            mc.eastWall = true;
                            break;
                        case 53:
                            mc.rock = true;
                            break;
                    }
                }
            }
        }

        TiledMapTileLayer doors = (TiledMapTileLayer) tiledMap.getLayers().get("doors");
        for (int column = 0; column < dimension; column++) {
            for (int row = 0; row < dimension; row++) {
                MazeCell mc = this.cells[column][row];
                TiledMapTileLayer.Cell cell = doors.getCell(row, column);
                if (cell != null && cell.getTile() != null) {
                    int tid = cell.getTile().getId() - 1;
                    switch (tid) {
                        case 5:
                            mc.northDoor = true;
                            mc.southDoor = true;
                            mc.eastDoor = true;
                            mc.westDoor = true;
                            break;
                        case 6:
                            mc.southDoor = true;
                            mc.eastDoor = true;
                            mc.westDoor = true;
                            break;
                        case 7:
                            mc.northDoor = true;
                            mc.southDoor = true;
                            mc.westDoor = true;
                            break;
                        case 8:
                            mc.northDoor = true;
                            mc.eastDoor = true;
                            mc.westDoor = true;
                            break;
                        case 9:
                            mc.northDoor = true;
                            mc.southDoor = true;
                            mc.eastDoor = true;
                            break;
                        case 21:
                            mc.westDoor = true;
                            break;
                        case 22:
                            mc.eastDoor = true;
                            break;
                        case 23:
                            mc.northDoor = true;
                            break;
                        case 24:
                            mc.southDoor = true;
                            break;
                        case 25:
                            mc.eastDoor = true;
                            mc.westDoor = true;
                            break;
                        case 37:
                            mc.northDoor = true;
                            mc.southDoor = true;
                            break;
                        case 38:
                            mc.westDoor = true;
                            mc.southDoor = true;
                            break;
                        case 39:
                            mc.northDoor = true;
                            mc.eastDoor = true;
                            break;
                        case 40:
                            mc.northDoor = true;
                            mc.westDoor = true;
                            break;
                        case 41:
                            mc.southDoor = true;
                            mc.eastDoor = true;
                            break;
                    }
                }
            }
        }

        TiledMapTileLayer hdoors = (TiledMapTileLayer) tiledMap.getLayers().get("hidden-doors");
        for (int column = 0; column < dimension; column++) {
            for (int row = 0; row < dimension; row++) {
                MazeCell mc = this.cells[column][row];
                TiledMapTileLayer.Cell cell = hdoors.getCell(row, column);
                if (cell != null && cell.getTile() != null) {
                    int tid = cell.getTile().getId() - 1;
                    switch (tid) {
                        case 48:
                            mc.hiddenNorthDoor = true;
                            mc.hiddenSouthDoor = true;
                            mc.hiddenEastDoor = true;
                            mc.hiddenWestDoor = true;
                            break;
                        case 49:
                            mc.hiddenSouthDoor = true;
                            mc.hiddenEastDoor = true;
                            mc.hiddenWestDoor = true;
                            break;
                        case 50:
                            mc.hiddenNorthDoor = true;
                            mc.hiddenSouthDoor = true;
                            mc.hiddenWestDoor = true;
                            break;
                        case 51:
                            mc.hiddenNorthDoor = true;
                            mc.hiddenEastDoor = true;
                            mc.hiddenWestDoor = true;
                            break;
                        case 52:
                            mc.hiddenNorthDoor = true;
                            mc.hiddenSouthDoor = true;
                            mc.hiddenEastDoor = true;
                            break;
                        case 64:
                            mc.hiddenWestDoor = true;
                            break;
                        case 65:
                            mc.hiddenEastDoor = true;
                            break;
                        case 66:
                            mc.hiddenNorthDoor = true;
                            break;
                        case 67:
                            mc.hiddenSouthDoor = true;
                            break;
                        case 68:
                            mc.hiddenEastDoor = true;
                            mc.hiddenWestDoor = true;
                            break;
                        case 80:
                            mc.hiddenNorthDoor = true;
                            mc.hiddenSouthDoor = true;
                            break;
                        case 81:
                            mc.hiddenWestDoor = true;
                            mc.hiddenSouthDoor = true;
                            break;
                        case 82:
                            mc.hiddenNorthDoor = true;
                            mc.hiddenEastDoor = true;
                            break;
                        case 83:
                            mc.hiddenNorthDoor = true;
                            mc.hiddenWestDoor = true;
                            break;
                        case 84:
                            mc.hiddenSouthDoor = true;
                            mc.hiddenEastDoor = true;
                            break;
                    }
                }
            }
        }

        MapLayer portals = tiledMap.getLayers().get("portals");
        Iterator<MapObject> iter = portals.getObjects().iterator();
        while (iter.hasNext()) {
            MapObject obj = iter.next();
            RectangleMapObject rmo = (RectangleMapObject) obj;
            Rectangle rect = rmo.getRectangle();
            int px = (int) (rect.x / 16f);
            int py = (int) (rect.y / 16f);
            MapProperties props = obj.getProperties();
            int lvl = Integer.parseInt((String) props.get("level"));
            int dx = Integer.parseInt((String) props.get("x"));
            int dy = Integer.parseInt((String) props.get("y"));
            this.cells[py][px].stairs = true;
            this.cells[py][px].addressTo = new MazeAddress(lvl, dimension - 1 - dy, dx);
        }

        MapLayer encounters = tiledMap.getLayers().get("encounters");
        Iterator<MapObject> iter2 = encounters.getObjects().iterator();
        while (iter2.hasNext()) {
            MapObject obj = iter2.next();
            RectangleMapObject rmo = (RectangleMapObject) obj;
            Rectangle rect = rmo.getRectangle();
            int px = (int) (rect.x / 16f);
            int py = (int) (rect.y / 16f);
            MapProperties props = obj.getProperties();
            int eid = Integer.parseInt((String) props.get("id"));
            this.cells[py][px].encounterID = eid;
            this.cells[py][px].hasTreasureChest = true;
        }

        TiledMapTileLayer lights = (TiledMapTileLayer) tiledMap.getLayers().get("lights");
        for (int column = 0; column < dimension; column++) {
            for (int row = 0; row < dimension; row++) {
                MazeCell mc = this.cells[column][row];
                TiledMapTileLayer.Cell cell = lights.getCell(row, column);
                if (cell != null && cell.getTile() != null) {
                    mc.spotLight = true;
                }
            }
        }

        TiledMapTileLayer wandering = (TiledMapTileLayer) tiledMap.getLayers().get("wandering");
        for (int column = 0; column < dimension; column++) {
            for (int row = 0; row < dimension; row++) {
                MazeCell mc = this.cells[column][row];
                TiledMapTileLayer.Cell cell = wandering.getCell(row, column);
                if (cell != null && cell.getTile() != null) {
                    mc.wanderingEncounterID = 1;
                }
            }
        }

    }

    @Override
    public int getRandomMonster() {
        return 0;
    }

    @Override
    public List<Integer> getEncounterIds() {
        return null;
    }

}
