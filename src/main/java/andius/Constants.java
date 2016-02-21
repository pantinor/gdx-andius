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
                        MovementBehavior movement = MovementBehavior.valueOf(obj.getProperties().get("movement", String.class));
                        Creature cr = new Creature(icon, role, surname, sx, m.baseMap.getHeight() - 1 - sy, x, y, movement);
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

    public enum Profession {

        RANGER(0x008),
        FIGHTER(0x010),
        WIZARD(0x020),
        THIEF(0x0040),
        CLERIC(0x100),
        WITCHER(0x200);

        private final int val;

        private Profession(int val) {
            this.val = val;
        }

        public int val() {
            return this.val;
        }

    }

    public enum ArmorType {
        NONE(128, 0xfff),
        CLOTH(136, 0xfff),
        LEATHER(145, Profession.RANGER.val() | Profession.FIGHTER.val() | Profession.THIEF.val() | Profession.CLERIC.val() | Profession.WITCHER.val()),
        CHAIN(160, Profession.RANGER.val() | Profession.FIGHTER.val() | Profession.CLERIC.val() | Profession.WITCHER.val()),
        PLATE(176, Profession.RANGER.val() | Profession.FIGHTER.val() | Profession.WITCHER.val()),
        CHAIN_P2(192, Profession.RANGER.val() | Profession.FIGHTER.val()),
        PLATE_P2(208, Profession.RANGER.val() | Profession.FIGHTER.val()),
        EXOTIC(224, 0xfff);

        private final int usableMask;
        private final int defense;
        private TextureRegion icon;

        private ArmorType(int def, int mask) {
            this.usableMask = mask;
            this.defense = def;
        }

        public static ArmorType get(int v) {
            for (ArmorType x : values()) {
                if (x.ordinal() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
        }

        public int getDefense() {
            return this.defense;
        }

        public boolean canUse(Profession prof) {
            return (prof.val() & this.usableMask) > 0;
        }

        public TextureRegion getIcon() {
            return icon;
        }

        public void setIcon(TextureRegion icon) {
            this.icon = icon;
        }

    }

    public enum WeaponType {
        NONE(4, 4, 0xfff),
        DAGGER(4, 8, 0xfff),
        STAFF(4, 9, 0xfff),
        MACE(4, 10, Profession.CLERIC.val() | Profession.THIEF.val() | Profession.RANGER.val() | Profession.FIGHTER.val() | Profession.WITCHER.val()),
        SLING(4, 13, Profession.THIEF.val() | Profession.RANGER.val() | Profession.FIGHTER.val() | Profession.WITCHER.val()),
        AXE(4, 16, Profession.THIEF.val() | Profession.RANGER.val() | Profession.FIGHTER.val() | Profession.WITCHER.val()),
        BOW(4, 19, Profession.THIEF.val() | Profession.RANGER.val() | Profession.FIGHTER.val() | Profession.WITCHER.val()),
        SWORD(4, 22, Profession.THIEF.val() | Profession.RANGER.val() | Profession.FIGHTER.val() | Profession.WITCHER.val()),
        SWORD_2H(4, 25, Profession.RANGER.val() | Profession.FIGHTER.val() | Profession.WITCHER.val()),
        AXE_P2(4, 28, Profession.RANGER.val() | Profession.FIGHTER.val() | Profession.WITCHER.val()),
        BOW_P2(4, 31, Profession.RANGER.val() | Profession.FIGHTER.val() | Profession.WITCHER.val()),
        SWORD_P2(4, 34, Profession.FIGHTER.val() | Profession.WITCHER.val()),
        AXE_P4(4, 40, Profession.FIGHTER.val() | Profession.WITCHER.val()),
        BOW_P4(4, 43, Profession.FIGHTER.val() | Profession.WITCHER.val()),
        SWORD_P4(4, 46, Profession.FIGHTER.val() | Profession.WITCHER.val()),
        EXOTIC(4, 49, 0xfff);

        private final int dmin;
        private final int dmax;
        private final int usableMask;
        private TextureRegion icon;

        private WeaponType(int dmin, int dmax, int mask) {
            this.usableMask = mask;
            this.dmin = dmin;
            this.dmax = dmax;
        }

        public static WeaponType get(int v) {
            for (WeaponType x : values()) {
                if (x.ordinal() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
        }

        public boolean canUse(Profession prof) {
            return (prof.val() & this.usableMask) > 0;
        }

        public TextureRegion getIcon() {
            return icon;
        }

        public void setIcon(TextureRegion icon) {
            this.icon = icon;
        }

        public int getDmin() {
            return dmin;
        }

        public int getDmax() {
            return dmax;
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
        HOBBIT(75, 50, 75, 99),
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
                hero.animation = new Animation(3f, atlas.findRegions(hero.toString()));
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
