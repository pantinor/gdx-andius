package andius;

import andius.objects.BaseMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import java.util.Iterator;

public interface Constants {

    public static int WORLD_TILE_DIM = 24;
    public static int TILE_DIM = 48;

    public static final String PARTY_SAV_BASE_FILENAME = "party.sav";
    public static final int STATS_NONE = 0;
    public static final int STATS_PLAYER1 = 1;
    public static final int STATS_PLAYER2 = 2;
    public static final int STATS_PLAYER3 = 3;
    public static final int STATS_PLAYER4 = 4;
    public static int MOON_PHASES = 24;
    public static int MOON_SECONDS_PER_PHASE = 4;
    public static int MOON_CHAR = 20;
    public static final int MAX_CREATURES_ON_MAP = 10;
    public static final int MAX_WANDERING_CREATURES_IN_DUNGEON = 2;
    public static final int MAX_CREATURE_DISTANCE = 24;

    public enum Map {

        WORLD("Andius", "world.tmx", MapBorderBehavior.WRAP, WORLD_TILE_DIM),
        LLECHY("Llechy", "llechy.tmx", MapBorderBehavior.EXIT, TILE_DIM),
        BARAD_ENELETH("Barad Eneleth", "llechy.tmx", MapBorderBehavior.EXIT, TILE_DIM),;

        private final String label;
        private final String tmxFile;
        private final MapBorderBehavior borderType;
        private final int dim;
        private BaseMap baseMap;
        private TiledMap tiledMap;
        private BaseScreen screen;

        private Map(String label, String tmx, MapBorderBehavior borderType, int dim) {
            this.label = label;
            this.tmxFile = tmx;
            this.borderType = borderType;
            this.dim = dim;
        }

        public String getLabel() {
            return label;
        }

        public String getTmxFile() {
            return tmxFile;
        }

        public MapBorderBehavior getBorderType() {
            return borderType;
        }

        public TiledMap getTiledMap() {
            return tiledMap;
        }

        public BaseMap getMap() {
            return baseMap;
        }

        public int getDim() {
            return dim;
        }

        public BaseScreen getScreen() {
            return screen;
        }

        public static void init() {
            FileHandleResolver resolver = new Constants.ClasspathResolver();
            TmxMapLoader loader = new TmxMapLoader(resolver);
            for (Map m : Map.values()) {
                m.tiledMap = loader.load("assets/data/" + m.tmxFile);
                m.baseMap = new BaseMap();
                m.screen = (m.dim == TILE_DIM ? new GameScreen(m) : new WorldScreen(m));

                MapProperties prop = m.tiledMap.getProperties();
                m.baseMap.setWidth(prop.get("width", Integer.class));
                m.baseMap.setHeight(prop.get("height", Integer.class));

                MapLayer portalsLayer = m.tiledMap.getLayers().get("portals");
                if (portalsLayer != null) {
                    Iterator<MapObject> iter = portalsLayer.getObjects().iterator();
                    while (iter.hasNext()) {
                        MapObject obj = iter.next();
                        Map pm = Map.valueOf(obj.getName());
                        String x = obj.getProperties().get("x", String.class);
                        String y = obj.getProperties().get("y", String.class);
                        m.baseMap.addPortal(pm, Integer.parseInt(x), Integer.parseInt(y));
                    }

                }

//                float[][] shadowMap = new float[m.baseMap.getWidth()][m.baseMap.getHeight()];
//                for (int y = 0; y < m.baseMap.getHeight(); y++) {
//                    for (int x = 0; x < m.baseMap.getWidth(); x++) {
//                        TiledMapTileLayer layer = (TiledMapTileLayer) m.tiledMap.getLayers().get("hills_forest");
//                        TiledMapTileLayer.Cell cell = layer.getCell(x, m.baseMap.getWidth() - 1 - y);
//                        shadowMap[x][y] = (cell != null ? 1 : 0);
//                    }
//                }
//
//                m.baseMap.setShadownMap(shadowMap);
            }

        }

    }

    public enum MapBorderBehavior {

        WRAP,
        EXIT,
        FIXED;
    }

    public enum Profession {

        BARBARIAN("fighter", 0x001),
        DRUID("shepherd", 0x002),
        ALCHEMIST("tinker", 0x004),
        RANGER("ranger", 0x008),
        FIGHTER("fighter", 0x010),
        WIZARD("mage", 0x020),
        THIEF("rogue", 0x0040),
        ILLUSIONIST("mage", 0x080),
        CLERIC("cleric", 0x100),
        PALADIN("paladin", 0x200);

        private final String tile;
        private final int val;

        private Profession(String tile, int val) {
            this.tile = tile;
            this.val = val;
        }

        public String getTile() {
            return tile;
        }

        public int val() {
            return this.val;
        }

    }

    public enum ClassType {

        HUMAN(75, 75, 75, 75),
        ELF(75, 99, 75, 50),
        DWARF(99, 75, 50, 75),
        BOBIT(75, 50, 75, 99),
        FUZZY(25, 99, 99, 75);

        private final int maxStr, maxDex, maxInt, maxWis;

        private ClassType(int mxSt, int mxDx, int mxIn, int mxWi) {
            this.maxStr = mxSt;
            this.maxDex = mxDx;
            this.maxInt = mxIn;
            this.maxWis = mxWi;
        }

        public int getMaxStr() {
            return maxStr;
        }

        public int getMaxDex() {
            return maxDex;
        }

        public int getMaxInt() {
            return maxInt;
        }

        public int getMaxWis() {
            return maxWis;
        }

    }

    public enum Status {

        GOOD,
        POISONED,
        ASH,
        DEAD;
    }

    public enum Tile {

        WIZARD,
        CLERIC,
        PALADIN,
        RANGER,
        BARBARIAN,
        THIEF,
        DRUID,
        TORTURER,
        FIGHTER,
        SWASHBUCKLER,
        KNIGHT,
        WITCH,
        BAT_MAJOR,
        BAT_MINOR,
        SPIDER_MAJOR,
        SPIDER_MINOR,
        BLACK_WIDOW_MAJOR,
        BLACK_WIDOW_MINOR,
        DWARF_FIGHTER,
        SKELETON,
        SKELETON_SWORDSMAN,
        LICHE,
        SKELETON_ARCHER,
        ORC,
        ORC_SHIELDSMAN,
        TROLL,
        OGRE_SHAMAN,
        OGRE,
        ORC_SHAMAN,
        RAT_MAJOR,
        RAT_MINOR,
        ZOMBIE_GREEN,
        ZOMBIE_BLUE,
        WRAITH,
        DWARF_CLERIC,
        DWARF_LORD,
        MINOTAUR,
        VAMPIRE_RED,
        VAMPIRE_BLUE,
        SORCERER,
        SORCERER_EVIL,
        WOLF_BLACK,
        WOLF_BROWN,
        MERMAN_SWORDSMAN,
        MERMAN_PIKE,
        MERMAN_SHAMAN,
        MERMAN_SWORDSMAN_BLUE,
        MERMAN_PIKE_BLUE,
        MERMAN_SHAMAN_BLUE,
        GAZER,
        GAZER_BLUE,
        PHANTOM_BLUE,
        PHANTOM_RED,
        PHANTOM_GREY,
        PIXIE,
        PIXIE_RED,
        DEMON_RED,
        DEMON_BLUE,
        DEMON_GREEN,
        ANGEL,
        DARK_ANGEL,
        HALFLING,
        HALFLING_RANGER,
        HALFLING_SHIELDSMAN,
        HALFLING_WIZARD,
        WISP_MAJOR,
        WISP_MINOR,
        DRAGON_BLACK,
        DRAGON_RED,
        DRAGON_BLUE,
        DRAGON_GREEN,
        HAWK_WHITE,
        HAWK_BROWN,
        CROW,
        MUMMY,
        MUMMY_KING,
        GOLEM_STONE,
        GOLEM_FIRE,
        GOLEM_EARTH,
        GOLEM_ICE,
        GOLEM_MUD,
        COBRA_MAJOR,
        COBRA_MINOR,
        KING_RED,
        QUEEN_RED,
        KING_BLUE,
        QUEEN_BLUE,
        BEETLE_BLACK,
        BEETLE_RED,
        BEETLE_BLACK_MINOR,
        BEETLE_RED_MINOR,
        GHOST_MINOR,
        GHOST_MAJOR,
        SLIME_GREEN,
        SLIME_RED,
        SLIME_PURPLE,
        GRUB_MINOR,
        GRUB_MAJOR,
        ELEMENTAL_PURPLE,
        ELEMENTAL_BLUE,
        ELEMENTAL_ORANGE,
        ELEMENTAL_CYAN,
        ELEMENTAL_BROWN,
        BUTTERFLY_WHITE,
        BUTTERFLY_RED,
        BUTTERFLY_BLACK,
        FROG_GREEN,
        FROG_BLUE,
        FROG_BROWN,
        INSECT_SWARM,
        MIMIC,
        SHOPKEEPER_BROWN,
        SHOPKEEPER_BLOND,
        BLOOD_PRIEST,
        BARBARIAN_AXE,
        DEMON_LORD,
        DARK_WIZARD,
        FIGHTER_RED,
        HOLY_AVENGER,
        SWASHBUCKLER_BLUE,
        DEATH_KNIGHT,
        BRAWLER,
        BRAWLER_DARK,
        BRAWLER_BLOND,
        ELVEN_SWORDSMAN_GREEN,
        ELVEN_WIZARD_GREEN,
        ELVEN_ARCHER_GREEN,
        ELVEN_SWORDSMAN_BLUE,
        ELVEN_WIZARD_BLUE,
        ELVEN_ARCHER_BLUE,;

    }

    public class ClasspathResolver implements FileHandleResolver {

        @Override
        public FileHandle resolve(String fileName) {
            return Gdx.files.classpath(fileName);
        }

    }

}
