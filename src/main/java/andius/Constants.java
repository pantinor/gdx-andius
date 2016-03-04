package andius;

import andius.objects.BaseMap;
import andius.objects.Creature;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import utils.Utils;
import utils.XORShiftRandom;

public interface Constants {

    public static int WORLD_TILE_DIM = 24;
    public static int TILE_DIM = 48;

    public static final String SAVE_FILENAME = "game.save";
    public static final String ROSTER_FILENAME = "roster.save";
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

        WORLD("Andius", "world.tmx", WORLD_TILE_DIM),
        LLECHY("Llechy", "llechy.tmx", TILE_DIM),
        ALIBABA("Shahriar", "ali-baba.tmx", TILE_DIM),
        BARAD_ENELETH("Barad Eneleth", "barad_eneleth.tmx", TILE_DIM),
        WIWOLD("Wiwold", "wiwold.tmx", TILE_DIM),
        WIWOLD_LVL_2("Wiwold Level 2", "wiwold_lvl_2.tmx", TILE_DIM),;

        private final String label;
        private final String tmxFile;
        private final int dim;
        private BaseMap baseMap;
        private TiledMap tiledMap;
        private BaseScreen screen;
        private int startX;
        private int startY;
        private int[][][] roomIds;

        private Map(String label, String tmx, int dim) {
            this.label = label;
            this.tmxFile = tmx;
            this.dim = dim;
        }

        public String getLabel() {
            return label;
        }

        public String getTmxFile() {
            return tmxFile;
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

        public int getStartX() {
            return startX;
        }

        public int getStartY() {
            return startY;
        }

        public int[][][] getRoomIds() {
            return roomIds;
        }

        public static void init() {

            FileHandle fh = Gdx.files.classpath("assets/data/heroes-atlas.txt");
            TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(fh, fh.parent(), false);
            String[] iconTileIds = new String[40 * 13];
            for (TextureAtlas.TextureAtlasData.Region r : atlas.getRegions()) {
                int x = r.left / r.width;
                int y = r.top / r.height;
                int i = x + (y * 40);
                iconTileIds[i] = r.name;
            }

            FileHandleResolver resolver = new Constants.ClasspathResolver();
            TmxMapLoader loader = new TmxMapLoader(resolver);
            for (Map m : Map.values()) {
                m.tiledMap = loader.load("assets/data/" + m.tmxFile);
                m.baseMap = new BaseMap();

                MapProperties prop = m.tiledMap.getProperties();
                m.baseMap.setWidth(prop.get("width", Integer.class));
                m.baseMap.setHeight(prop.get("height", Integer.class));
                m.startX = Integer.parseInt(prop.get("startX", String.class));
                m.startY = Integer.parseInt(prop.get("startY", String.class));

                MapLayer portalsLayer = m.tiledMap.getLayers().get("portals");
                if (portalsLayer != null) {
                    Iterator<MapObject> iter = portalsLayer.getObjects().iterator();
                    while (iter.hasNext()) {
                        MapObject obj = iter.next();
                        Map pm;
                        try {
                            pm = Map.valueOf(obj.getName());
                        } catch (Exception e) {
                            pm = m;
                        }
                        float x = obj.getProperties().get("x", Float.class);
                        float y = obj.getProperties().get("y", Float.class);
                        int sx = (int) (x / m.dim);
                        int sy = m.baseMap.getHeight() - 1 - (int) (y / m.dim);
                        Object dx = obj.getProperties().get("dx");
                        Object dy = obj.getProperties().get("dy");
                        List<Vector3> randoms = new ArrayList<>();
                        for (int i = 0; i < 6; i++) {
                            String temp = (String) obj.getProperties().get("random" + i);
                            if (temp != null) {
                                String[] s = temp.split(",");
                                randoms.add(new Vector3(Integer.parseInt(s[1]), Integer.parseInt(s[2]), 0));
                            }
                        }
                        m.baseMap.addPortal(pm, sx, sy,
                                dx != null ? Integer.parseInt((String) dx) : -1, dy != null ? Integer.parseInt((String) dy) : -1,
                                randoms.size() > 0 ? randoms : null);
                    }
                }

                MapLayer peopleLayer = m.tiledMap.getLayers().get("people");
                if (peopleLayer != null) {
                    TiledMapTileLayer iconLayer = (TiledMapTileLayer) m.tiledMap.getLayers().get("creature");
                    int firstgid = m.tiledMap.getTileSets().getTileSet("heroes").getProperties().get("firstgid", Integer.class);
                    Iterator<MapObject> iter = peopleLayer.getObjects().iterator();
                    while (iter.hasNext()) {
                        MapObject obj = iter.next();
                        String surname = obj.getName();
                        float x = obj.getProperties().get("x", Float.class);
                        float y = obj.getProperties().get("y", Float.class);
                        int sx = (int) (x / TILE_DIM);
                        int sy = (int) (y / TILE_DIM);
                        TiledMapTileLayer.Cell iconCell = iconLayer.getCell(sx, sy);
                        Icons icon = Icons.valueOf(iconTileIds[iconCell.getTile().getId() - firstgid]);
                        Role role = Role.valueOf(obj.getProperties().get("type", String.class));
                        Creatures monster = null;
                        try {
                            monster = Creatures.valueOf(obj.getProperties().get("creature", String.class));
                        } catch (Exception e) {
                            monster = Creatures.NONE;
                        }
                        MovementBehavior movement = MovementBehavior.valueOf(obj.getProperties().get("movement", String.class));
                        Creature cr = new Creature(icon);
                        cr.set(monster, role, surname, sx, m.baseMap.getHeight() - 1 - sy, x, y, movement);
                        m.baseMap.creatures.add(cr);
                    }
                }

                MapLayer roomsLayer = m.tiledMap.getLayers().get("rooms");
                if (roomsLayer != null) {
                    m.roomIds = new int[m.baseMap.getWidth()][m.baseMap.getHeight()][3];
                    Iterator<MapObject> iter = roomsLayer.getObjects().iterator();
                    while (iter.hasNext()) {
                        MapObject obj = iter.next();
                        int id = obj.getProperties().get("id", Integer.class);
                        PolygonMapObject rmo = (PolygonMapObject) obj;
                        for (int y = 0; y < m.baseMap.getHeight(); y++) {
                            for (int x = 0; x < m.baseMap.getWidth(); x++) {
                                if (rmo.getPolygon().contains(x * TILE_DIM + TILE_DIM / 2, m.baseMap.getHeight() * TILE_DIM - y * TILE_DIM - TILE_DIM / 2)) {
                                    if (m.roomIds[x][y][0] == 0) {
                                        m.roomIds[x][y][0] = id;
                                    } else if (m.roomIds[x][y][1] == 0) {
                                        m.roomIds[x][y][1] = id;
                                    } else if (m.roomIds[x][y][2] == 0) {
                                        m.roomIds[x][y][2] = id;
                                    } else {
                                        throw new RuntimeException("Too many overlaps on roomids");
                                    }
                                }
                            }
                        }
                    }
                }

                m.screen = (m.dim == TILE_DIM ? new GameScreen(m) : new WorldScreen(m));

            }

        }

    }

    enum Direction {

        WEST(1, 0x1),
        NORTH(2, 0x2),
        EAST(3, 0x4),
        SOUTH(4, 0x8);

        private int val;
        private int mask;

        private Direction(int v, int mask) {
            this.val = v;
            this.mask = mask;
        }

        public int getVal() {
            return val;
        }

        public int getMask() {
            return mask;
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
                mask &= ~dir.getMask();
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
            int rand = new XORShiftRandom().nextInt(n);
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
                if (val == d.getVal()) {
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

    public enum MovementBehavior {

        FIXED,
        WANDER,
        FOLLOW_AVATAR,
        ATTACK_AVATAR;
    }

    public enum Race {

        HUMAN(8, 8, 5, 8, 8, 9),
        ELF(7, 10, 10, 6, 9, 6),
        DWARF(10, 7, 10, 10, 5, 6),
        GNOME(7, 7, 10, 8, 10, 7),
        HOBBIT(5, 7, 7, 6, 10, 12);

        private final int initialStrength, initialIntell, initialPiety, initialVitality, initialAgility, initialLuck;

        private Race(int initialStrength, int initialIntell, int initialPiety, int initalVitality, int initialAgility, int initialLuck) {
            this.initialStrength = initialStrength;
            this.initialIntell = initialIntell;
            this.initialPiety = initialPiety;
            this.initialVitality = initalVitality;
            this.initialAgility = initialAgility;
            this.initialLuck = initialLuck;
        }

        public int getInitialStrength() {
            return initialStrength;
        }

        public int getInitialIntell() {
            return initialIntell;
        }

        public int getInitialPiety() {
            return initialPiety;
        }

        public int getInitialVitality() {
            return initialVitality;
        }

        public int getInitialAgility() {
            return initialAgility;
        }

        public int getInitialLuck() {
            return initialLuck;
        }

    }

    public enum ClassType {

        FIGHTER("F", 10, 11, 11, 0, 0, 0, 0, 0),
        MAGE("M", 5, 4, 0, 11, 0, 0, 0, 0),
        CLERIC("C", 8, 7, 0, 0, 11, 0, 0, 0),
        THIEF("T", 6, 7, 0, 0, 0, 0, 11, 0),
        WIZARD("B", 6, 5, 0, 12, 12, 0, 0, 0),
        SAMURAI("S", 14, 7, 15, 11, 10, 14, 10, 0),
        LORD("L", 18, 9, 15, 12, 12, 15, 14, 15),
        NINJA("N", 12, 5, 17, 17, 17, 17, 17, 17);

        private final int minStr, minIntell, minPiety, minVitality, minAgility, minLuck;
        private final String abbr;
        private final int startHP;
        private final int incrHP;

        private ClassType(String abbr, int startHP, int incrHP, int minStr, int minIntell, int minPiety, int minVitality, int minAgility, int minLuck) {
            this.abbr = abbr;
            this.minStr = minStr;
            this.minIntell = minIntell;
            this.minPiety = minPiety;
            this.minVitality = minVitality;
            this.minAgility = minAgility;
            this.minLuck = minLuck;
            this.startHP = startHP;
            this.incrHP = incrHP;
        }

        public String getAbbr() {
            return this.abbr;
        }

        public int getMinStr() {
            return minStr;
        }

        public int getMinIntell() {
            return minIntell;
        }

        public int getMinPiety() {
            return minPiety;
        }

        public int getMinVitality() {
            return minVitality;
        }

        public int getMinAgility() {
            return minAgility;
        }

        public int getMinLuck() {
            return minLuck;
        }

        public int getStartHP() {
            return startHP;
        }

        public int getIncrHP() {
            return incrHP;
        }

    }

    public static final int[][] LEVEL_PROGRESSION_TABLE = new int[][]{
        {1000, 1100, 1050, 900, 1200, 1200, 1300, 1450},
        {1724, 1896, 1810, 1551, 2105, 2105, 2280, 2543},
        {2972, 3268, 3120, 2674, 3677, 3677, 4000, 4461},
        {5124, 5634, 5379, 4610, 6477, 6477, 7017, 7826},
        {8834, 9713, 9274, 7948, 11363, 11363, 12310, 13729},
        {15231, 16746, 15989, 13703, 19935, 19935, 21596, 24085},
        {26260, 28872, 27567, 23625, 34973, 34973, 37887, 42254},
        {45275, 49779, 47529, 40732, 61356, 61356, 66468, 74129},
        {78060, 85825, 81946, 70227, 107642, 107642, 116610, 130050},
        {134586, 147974, 141286, 121081, 188845, 188845, 204578, 228157},
        {232044, 255127, 243596, 208760, 331307, 331307, 358908, 400275},
        {400075, 439874, 419993, 359931, 581240, 581240, 629663, 702236},
        {289709, 318529, 304132, 260639, 428479, 428479, 475008, 529756}
    };

    public enum ArmorType {
        NONE("None", 0, "FMTCBSLN", 0),
        ROBES("Robes", 15, "FMTCBSLN", 1),
        LEATHER("Leather Armor", 50, "FPTBSLN", 2),
        CHAIN_MAIL("Chain Mail", 90, "FPSLN", 3),
        BREAST_PLATE("Breast Plate", 200, "FPSLN", 4),
        PLATE("Plate Mail", 750, "FSLN", 5),
        CHAIN_P1("Chain Mail +1", 1500, "FPSLN", 4),
        LEATHER_P1("Leather +1", 1500, "FPTBSLN", 3),
        PLATE_P1("Plate Mail +1", 1500, "FSLN", 6),
        BREAST_PLATE_P1("Breast Plate +1", 1500, "FPSLN", 5),
        LEATHER_P2("Leather +2", 6000, "FPTBSLN", 4),
        CHAIN_P2("Chain +2", 6000, "FPSLN", 5),
        PLATE_P2("Plate Mail +2", 6000, "FPSLN", 7),
        EVIL_CHAIN_P2("Evil Chain +2", 8000, "FPSLN", 5),
        BR_PLATE_P2("Breast Plate +2", 10000, "FPSLN", 6),
        BR_PLATE_P3("Breast Plate +3", 100000, "FPSLN", 7),
        CHAIN_FIRE("Chain Pro Fire", 150000, "FPSLN", 6),
        EVIL_PLATE_P3("Evil Plate +3", 150000, "FPSLN", 9),
        LORDS_GARB("Lords Garb", 1000000, "L", 10);

        private final String name;
        private final int cost;
        private final String usableMask;
        private final int ac;
        private TextureRegion icon;

        private ArmorType(String name, int cost, String usableMask, int ac) {
            this.name = name;
            this.cost = cost;
            this.usableMask = usableMask;
            this.ac = ac;
        }

        public String getName() {
            return name;
        }

        public int getCost() {
            return cost;
        }

        public static ArmorType get(int v) {
            for (ArmorType x : values()) {
                if (x.ordinal() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
        }

        public int getAC() {
            return this.ac;
        }

        public boolean canUse(ClassType ct) {
            return this.usableMask.contains(ct.getAbbr());
        }

        public TextureRegion getIcon() {
            return icon;
        }

        public void setIcon(TextureRegion icon) {
            this.icon = icon;
        }

    }

    public enum WeaponType {
        NONE("None", 0, "FMTCBSLN", 1, 3),
        DAGGER("Dagger", 5, "FMTSLN", 1, 4),
        STAFF("Staff", 10, "FMTCBSLN", 1, 5),
        SHORT_SWD("Short Sword", 15, "FTSLN", 1, 6),
        LONG_SWD("Long Sword", 25, "FSLN", 1, 8),
        ANOINT_MACE("Anointed Mace", 30, "FPBSLN", 2, 6),
        ANOINT_FLAIL("Anointed Flail", 150, "FPSLN", 1, 7),
        STAFF_P2("Staff +2", 2500, "FMTCBSLN", 3, 6),
        STAFF_MOG("Staff of Mogref", 3000, "MB", 1, 6),
        MACE_P2("Mace +2", 4000, "FPBSLN", 3, 10),
        SHORT_SWD_P2("Short Sword +2", 4000, "FTSLN", 3, 8),
        LONG_SWD_P2("Long Sword +2", 4000, "FSLN", 3, 12),
        DAGGER_P2("Dagger +2", 8000, "FMTSLN", 3, 6),
        SHORT_SWD_M2("Short Sword -2", 8000, "FTSLN", 1, 6),
        LONG_SWD_P1("Long Sword +1", 10000, "FSLN", 2, 9),
        DRAGON_SLAYER("Dragon Slayer", 10000, "FSLN", 2, 11),
        WERE_SLAYER("Were Slayer", 10000, "FSLN", 2, 11),
        MAGE_MASHER("Mage Masher", 10000, "FTSLN", 2, 7),
        MACE_PRO_POISON("Mace Pro Poison", 10000, "FPBSLN", 1, 8),
        MACE_P1("Mace +1", 12500, "FPBSLN", 3, 9),
        SHORT_SWD_P1("Short Sword +1", 15000, "FTSLN", 2, 7),
        STAFF_MONTINO("Staff of Montino", 15000, "FMTCBSLN", 2, 6),
        BLADE_CUSINART("Blade Cusinart", 15000, "FSLN", 10, 12),
        DAGGER_SPEED("Dagger of Speed", 30000, "MN", 1, 4),
        EVIL_SWD_P3("Evil Sword +3", 50000, "FSLN", 4, 13),
        THIEVES_DAGGER("Thieves Dagger", 50000, "TN", 1, 6),
        SHURIKEN("Shuriken", 50000, "N", 11, 16),
        MURASAMA_BLADE("Murasama Blade", 1000000, "S", 10, 50);

        private final String name;
        private final int cost;
        private final String usableMask;
        private final int dmin;
        private final int dmax;
        private TextureRegion icon;

        private WeaponType(String name, int cost, String usableMask, int dmin, int dmax) {
            this.name = name;
            this.cost = cost;
            this.usableMask = usableMask;
            this.dmin = dmin;
            this.dmax = dmax;
        }

        public String getName() {
            return name;
        }

        public int getCost() {
            return cost;
        }

        public static WeaponType get(int v) {
            for (WeaponType x : values()) {
                if (x.ordinal() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
        }

        public boolean canUse(ClassType ct) {
            return this.usableMask.contains(ct.getAbbr());
        }

        public int getDmin() {
            return dmin;
        }

        public int getDmax() {
            return dmax;
        }

        public TextureRegion getIcon() {
            return icon;
        }

        public void setIcon(TextureRegion icon) {
            this.icon = icon;
        }
    }

    public enum CreatureStatus {

        FINE,
        DEAD,
        FLEEING,
        CRITICAL,
        HEAVILYWOUNDED,
        LIGHTLYWOUNDED,
        BARELYWOUNDED;
    }

    public enum Moongate {
        GATE_0(0, 1, 2),
        GATE_1(3, 4, 5),
        GATE_2(6, 7, 0),
        GATE_3(1, 2, 3),
        GATE_4(4, 5, 6),
        GATE_5(7, 0, 1),
        GATE_6(2, 3, 4),
        GATE_7(5, 6, 7),;

        private float x;
        private float y;
        private float mapX;
        private float mapY;
        private final int d1;
        private final int d2;
        private final int d3;

        private TextureAtlas.AtlasRegion currentTexture;

        private Moongate(int d1, int d2, int d3) {
            this.d1 = d1;
            this.d2 = d2;
            this.d3 = d3;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getMapX() {
            return mapX;
        }

        public float getMapY() {
            return mapY;
        }

        public int getD1() {
            return d1;
        }

        public int getD2() {
            return d2;
        }

        public int getD3() {
            return d3;
        }

        public TextureAtlas.AtlasRegion getCurrentTexture() {
            return currentTexture;
        }

        public void setCurrentTexture(TextureAtlas.AtlasRegion currentTexture) {
            this.currentTexture = currentTexture;
        }

        public static void init() {
            MapLayer moongatesLayer = Map.WORLD.getTiledMap().getLayers().get("moongates");
            Iterator<MapObject> iter = moongatesLayer.getObjects().iterator();
            while (iter.hasNext()) {
                MapObject obj = iter.next();
                Moongate mg = Moongate.valueOf(obj.getName());
                mg.x = obj.getProperties().get("x", Float.class);
                mg.y = obj.getProperties().get("y", Float.class);
                mg.mapX = Float.parseFloat(obj.getProperties().get("wx", String.class));
                mg.mapY = Float.parseFloat(obj.getProperties().get("wy", String.class));
            }
        }

    }

    public enum HealType {

        NONE,
        CURE,
        FULLHEAL,
        RESURRECT,
        HEAL;
    }

    public enum Status {

        GOOD,
        POISONED,
        ASH,
        DEAD;
    }

    public enum Role {
        NONE,
        FRIENDLY,
        MONSTER,
        MERCHANT;
    }

    public enum Icons {

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

        private Animation animation;

        public Animation getAnimation() {
            return animation;
        }

        public static void init(TextureAtlas atlas) {
            for (Icons hero : Icons.values()) {
                int frameRate = Utils.getRandomBetween(3, 5);
                hero.animation = new Animation(frameRate, atlas.findRegions(hero.toString()));
            }
        }

    }

    public class ClasspathResolver implements FileHandleResolver {

        @Override
        public FileHandle resolve(String fileName) {
            return Gdx.files.classpath(fileName);
        }

    }

    public enum AuraType {

        NONE,
        HORN,
        JINX,
        NEGATE,
        PROTECTION,
        QUICKNESS;
    }

    public enum AttackResult {

        NONE,
        HIT,
        MISS;
    }

    public class AttackVector {

        public int x;
        public int y;
        public int distance;

        public AttackResult result;

        public Creature impactedCreature;

        public AttackVector(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public enum Creatures {
        NONE("", 0, 0, false, 0),
        ZOMBIE("Zombie", 48, 3, false, 50),;

        private final String name;
        private final int basehp;
        private final int exp;
        private final boolean ranged;
        private final int gold;

        private Creatures(String name, int basehp, int exp, boolean ranged, int gold) {
            this.name = name;
            this.basehp = basehp;
            this.exp = exp;
            this.ranged = ranged;
            this.gold = gold;
        }

        public String getName() {
            return name;
        }

        public int getBasehp() {
            return basehp;
        }

        public int getExp() {
            return exp;
        }

        public boolean isRanged() {
            return ranged;
        }

        public int getGold() {
            return gold;
        }

    }

}
