package andius;

import andius.objects.BaseMap;
import andius.objects.Creature;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import java.util.Iterator;
import utils.XORShiftRandom;

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

        WORLD("Andius", "world.tmx", WORLD_TILE_DIM),
        LLECHY("Llechy", "llechy.tmx", TILE_DIM),
        BARAD_ENELETH("Barad Eneleth", "barad_eneleth.tmx", TILE_DIM),;

        private final String label;
        private final String tmxFile;
        private final int dim;
        private BaseMap baseMap;
        private TiledMap tiledMap;
        private BaseScreen screen;
        private int startX;
        private int startY;

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
                        Map pm = Map.valueOf(obj.getName());
                        String x = obj.getProperties().get("wx", String.class);
                        String y = obj.getProperties().get("wy", String.class);
                        m.baseMap.addPortal(pm, Integer.parseInt(x), Integer.parseInt(y));
                    }
                }

                MapLayer peopleLayer = m.tiledMap.getLayers().get("people");
                if (peopleLayer != null) {
                    TiledMapTileLayer iconLayer = (TiledMapTileLayer) m.tiledMap.getLayers().get("creature");
                    int firstgid = m.tiledMap.getTileSets().getTileSet("uf_heroes").getProperties().get("firstgid", Integer.class);
                    Iterator<MapObject> iter = peopleLayer.getObjects().iterator();
                    while (iter.hasNext()) {
                        MapObject obj = iter.next();
                        int sx = Integer.parseInt(obj.getProperties().get("startX", String.class));
                        int sy = Integer.parseInt(obj.getProperties().get("startY", String.class));
                        float x = obj.getProperties().get("x", Float.class);
                        float y = obj.getProperties().get("y", Float.class);
                        TiledMapTileLayer.Cell iconCell = iconLayer.getCell(sx, m.baseMap.getHeight() - 1 - sy);
                        Heroes icon = Heroes.valueOf(iconTileIds[iconCell.getTile().getId() - firstgid]);
                        String surname = obj.getName();
                        PersonRole role = PersonRole.valueOf(obj.getProperties().get("type", String.class));
                        MovementBehavior movement = MovementBehavior.valueOf(obj.getProperties().get("movement", String.class));
                        Creature cr = new Creature(icon, role, surname, sx, sy, x, y, movement);
                        m.baseMap.creatures.add(cr);
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

    public enum PersonRole {
        NONE,
        MERCHANT;
    }

    public enum Heroes {

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
            for (Heroes hero : Heroes.values()) {
                hero.animation = new Animation(1.2f, atlas.findRegions(hero.toString()));
            }
        }

    }

    public class ClasspathResolver implements FileHandleResolver {

        @Override
        public FileHandle resolve(String fileName) {
            return Gdx.files.classpath(fileName);
        }

    }

}
