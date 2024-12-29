
import static andius.Constants.CLASSPTH_RSLVR;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class WorldTileConvertScript implements ApplicationListener {

    public final int GRASS1 = 146 + 1;
    public final int GRASS2 = 147 + 1;
    public final int FOREST = 185 + 1;
    public final int MEADOW = 178 + 1;
    public final int MOUNTAIN = 407 + 1;
    public final int WATER = 92 + 1;
    public final int CASTLE = 265 + 1;
    public final int DUNGEON = 393 + 1;
    public final int LAVA = 67 + 1;

    public static void main(String[] args) throws Exception {
        new LwjglApplication(new WorldTileConvertScript());
    }

    @Override
    public void create() {

        TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
        TiledMap tiledMap = loader.load("assets/data/sosaria.tmx");

        TiledMapTileLayer layer = (TiledMapTileLayer) tiledMap.getLayers().get("Map Layer");
        TiledMapTileLayer forest = (TiledMapTileLayer) tiledMap.getLayers().get("forest");
        TiledMapTileLayer meadow = (TiledMapTileLayer) tiledMap.getLayers().get("meadow");
        TiledMapTileLayer mountains = (TiledMapTileLayer) tiledMap.getLayers().get("mountains");
        TiledMapTileLayer grass = (TiledMapTileLayer) tiledMap.getLayers().get("grass");
        TiledMapTileLayer water = (TiledMapTileLayer) tiledMap.getLayers().get("water");
        TiledMapTileLayer lava = (TiledMapTileLayer) tiledMap.getLayers().get("lava");

        int dim = 64;
        for (int x = 0; x < dim; x++) {
            for (int y = 0; y < dim; y++) {
                grass.setCell(x, y, null);
                water.setCell(x, y, null);
                lava.setCell(x, y, null);
                mountains.setCell(x, y, null);
                forest.setCell(x, y, null);
                meadow.setCell(x, y, null);
            }
        }

        for (int x = 0; x < dim; x++) {
            for (int y = 0; y < dim; y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                switch (cell.getTile().getId()) {
                    case GRASS1:
                    case GRASS2:
                        grass.setCell(x, y, cell);
                        break;
                    case FOREST:
                        forest.setCell(x, y, cell);
                        break;
                    case MEADOW:
                        meadow.setCell(x, y, cell);
                        break;
                    case MOUNTAIN:
                        mountains.setCell(x, y, cell);
                        break;
                    case WATER:
                        water.setCell(x, y, cell);
                        break;
                    case LAVA:
                        lava.setCell(x, y, cell);
                        break;
                }
            }
        }

        System.out.println("grass");
        for (int y = 0; y < dim; y++) {
            for (int x = 0; x < dim; x++) {
                TiledMapTileLayer.Cell cell = grass.getCell(x, dim - 1 - y);
                System.out.print(cell == null ? "0," : cell.getTile().getId() + ",");
            }
            System.out.println("");
        }
        System.out.println("forest");
        for (int y = 0; y < dim; y++) {
            for (int x = 0; x < dim; x++) {
                TiledMapTileLayer.Cell cell = forest.getCell(x, dim - 1 - y);
                System.out.print(cell == null ? "0," : cell.getTile().getId() + ",");
            }
            System.out.println("");
        }
        System.out.println("meadow");
        for (int y = 0; y < dim; y++) {
            for (int x = 0; x < dim; x++) {
                TiledMapTileLayer.Cell cell = meadow.getCell(x, dim - 1 - y);
                System.out.print(cell == null ? "0," : cell.getTile().getId() + ",");
            }
            System.out.println("");
        }
        System.out.println("mountains");
        for (int y = 0; y < dim; y++) {
            for (int x = 0; x < dim; x++) {
                TiledMapTileLayer.Cell cell = mountains.getCell(x, dim - 1 - y);
                System.out.print(cell == null ? "0," : cell.getTile().getId() + ",");
            }
            System.out.println("");
        }
        System.out.println("water");
        for (int y = 0; y < dim; y++) {
            for (int x = 0; x < dim; x++) {
                TiledMapTileLayer.Cell cell = water.getCell(x, dim - 1 - y);
                System.out.print(cell == null ? "0," : cell.getTile().getId() + ",");
            }
            System.out.println("");
        }
        System.out.println("lava");
        for (int y = 0; y < dim; y++) {
            for (int x = 0; x < dim; x++) {
                TiledMapTileLayer.Cell cell = lava.getCell(x, dim - 1 - y);
                System.out.print(cell == null ? "0," : cell.getTile().getId() + ",");
            }
            System.out.println("");
        }
        System.out.println("Map Layer");
        for (int y = 0; y < dim; y++) {
            for (int x = 0; x < dim; x++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, dim - 1 - y);
                System.out.print(cell == null ? "0," : cell.getTile().getId() + ",");
            }
            System.out.println("");
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

}
