package andius;

import andius.objects.Actor;
import andius.objects.BaseMap;
import andius.objects.Monster;
import andius.objects.MutableMonster;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public interface Constants {

    public static int WORLD_TILE_DIM = 24;
    public static int TILE_DIM = 48;

    public static final String SAVE_FILENAME = "party.json";
    public static final String ROSTER_FILENAME = "roster.json";
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
        WORLD("Andius", "sosaria.tmx", WORLD_TILE_DIM),
        CAVE("Cave", "cave.tmx", TILE_DIM),
        BOLTAC("Boltac's Trading Post", "boltac.tmx", TILE_DIM),
        LLECHY("Llechy", "llechy.tmx", TILE_DIM),
        ALIBABA("Ali Baba Shahriar", "ali-baba.tmx", TILE_DIM),
        CANT("Radiant Temple of Cant", "templeCant.tmx", TILE_DIM),
        BARAD_ENELETH("Barad Eneleth", "barad_eneleth.tmx", TILE_DIM),
        WIWOLD("Wiwold", "wiwold.tmx", TILE_DIM),
        WIWOLD_LVL_2("Wiwold Level 2", "wiwold_lvl_2.tmx", TILE_DIM),
        WIZARDRY1("Proving Grounds of the Mad Overlord", WizardryData.Scenario.PMO),
        WIZARDRY2("Knight of Diamonds", WizardryData.Scenario.KOD),
        WIZARDRY3("Legacy of Llylgamyn", WizardryData.Scenario.LEG),
        BLACK_STONE("Black Stone", WizardryData.Scenario.BS),
        WIZARDRY4("Return of Werdna", WizardryData.Scenario.WER);

        private final String label;
        private final String tmxFile;
        private final int dim;
        private final WizardryData.Scenario wizScenario;

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
            this.wizScenario = WizardryData.Scenario.PMO;
        }

        private Map(String label, WizardryData.Scenario scenario) {
            this.label = label;
            this.tmxFile = null;
            this.dim = 0;
            this.wizScenario = scenario;
        }

        public String getLabel() {
            return label;
        }

        public String getTmxFile() {
            return tmxFile;
        }

        public TiledMap getTiledMap() {
            if (this.tiledMap == null) {
                init();
            }
            return this.tiledMap;
        }

        public BaseMap getBaseMap() {
            return baseMap;
        }

        public int getDim() {
            return dim;
        }

        public WizardryData.Scenario scenario() {
            return wizScenario;
        }

        public boolean isLoaded() {
            return this.screen != null;
        }

        public BaseScreen getScreen() {
            if (this.screen == null) {
                init();
            }
            return this.screen;
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

        public void init() {

            if (this.tmxFile != null) {

                TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
                this.tiledMap = loader.load("assets/data/" + this.tmxFile);
                this.baseMap = new BaseMap();

                MapProperties prop = this.tiledMap.getProperties();
                this.baseMap.setWidth(prop.get("width", Integer.class));
                this.baseMap.setHeight(prop.get("height", Integer.class));
                this.startX = Integer.parseInt(prop.get("startX", String.class));
                this.startY = Integer.parseInt(prop.get("startY", String.class));

                MapLayer portalsLayer = this.tiledMap.getLayers().get("portals");
                if (portalsLayer != null) {
                    Iterator<MapObject> iter = portalsLayer.getObjects().iterator();
                    while (iter.hasNext()) {
                        MapObject obj = iter.next();
                        Map pm;
                        try {
                            pm = Map.valueOf(obj.getName());
                        } catch (Exception e) {
                            pm = this;
                        }
                        float x = obj.getProperties().get("x", Float.class);
                        float y = obj.getProperties().get("y", Float.class);
                        int sx = (int) (x / this.dim);
                        int sy = this.baseMap.getHeight() - 1 - (int) (y / this.dim);

                        if ("ELEVATOR".equals(obj.getName())) {
                            Object down = obj.getProperties().get("DOWN");
                            if (down != null) {
                                try {
                                    pm = Map.valueOf((String) down);
                                    Object dx = obj.getProperties().get("dx");
                                    Object dy = obj.getProperties().get("dy");
                                    this.baseMap.addPortal(pm, sx, sy,
                                            dx != null ? Integer.parseInt((String) dx) : -1,
                                            dy != null ? Integer.parseInt((String) dy) : -1,
                                            null, true, false);
                                } catch (Exception e) {
                                    //ignore
                                }
                            }
                            Object up = obj.getProperties().get("UP");
                            if (up != null) {
                                try {
                                    pm = Map.valueOf((String) up);
                                    Object ux = obj.getProperties().get("ux");
                                    Object uy = obj.getProperties().get("uy");
                                    this.baseMap.addPortal(pm, sx, sy,
                                            ux != null ? Integer.parseInt((String) ux) : -1,
                                            uy != null ? Integer.parseInt((String) uy) : -1,
                                            null, true, true);
                                } catch (Exception e) {
                                    //ignore
                                }
                            }
                        } else {
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
                            this.baseMap.addPortal(pm, sx, sy,
                                    dx != null ? Integer.parseInt((String) dx) : -1,
                                    dy != null ? Integer.parseInt((String) dy) : -1,
                                    randoms.size() > 0 ? randoms : null, false, false);
                        }
                    }
                }

                MapLayer peopleLayer = this.tiledMap.getLayers().get("people");
                if (peopleLayer != null) {
                    loadPeopleLayer(peopleLayer, this.wizScenario.monsterMap());
                }

                MapLayer roomsLayer = this.tiledMap.getLayers().get("rooms");
                if (roomsLayer != null) {
                    this.roomIds = new int[this.baseMap.getWidth()][this.baseMap.getHeight()][3];
                    Iterator<MapObject> iter = roomsLayer.getObjects().iterator();
                    while (iter.hasNext()) {
                        MapObject obj = iter.next();
                        int id = obj.getProperties().get("id", Integer.class);
                        PolygonMapObject rmo = (PolygonMapObject) obj;
                        for (int y = 0; y < this.baseMap.getHeight(); y++) {
                            for (int x = 0; x < this.baseMap.getWidth(); x++) {
                                if (rmo.getPolygon().contains(x * TILE_DIM + TILE_DIM / 2, this.baseMap.getHeight() * TILE_DIM - y * TILE_DIM - TILE_DIM / 2)) {
                                    if (this.roomIds[x][y][0] == 0) {
                                        this.roomIds[x][y][0] = id;
                                    } else if (this.roomIds[x][y][1] == 0) {
                                        this.roomIds[x][y][1] = id;
                                    } else if (this.roomIds[x][y][2] == 0) {
                                        this.roomIds[x][y][2] = id;
                                    } else {
                                        throw new RuntimeException("Too many overlaps on roomids");
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (this.dim == 0) {
                this.screen = new WizardryDungeonScreen(this);
            } else if (this.dim == TILE_DIM) {
                this.screen = new GameScreen(this);
            } else {
                this.screen = new WorldScreen(this);
                //this.screen = new VoronoiAzgaarMapScreen();
            }

        }

        private void loadPeopleLayer(MapLayer peopleLayer, java.util.Map<String, Monster> monsters) {

            Iterator<MapObject> iter = peopleLayer.getObjects().iterator();
            while (iter.hasNext()) {
                MapObject obj = iter.next();
                String name = obj.getName();
                int id = obj.getProperties().get("id", Integer.class);
                float x = obj.getProperties().get("x", Float.class);
                float y = obj.getProperties().get("y", Float.class);
                int sx = (int) (x / TILE_DIM);
                int sy = (int) (y / TILE_DIM);

                String icon = obj.getProperties().get("icon", String.class);
                if (icon == null) {
                    icon = "Knight_Arena_Champion_Male";
                }

                String rl = obj.getProperties().get("type", String.class);
                Role role = Role.valueOf(rl != null ? rl : "FRIENDLY");

                String mv = obj.getProperties().get("movement", String.class);
                MovementBehavior movement = MovementBehavior.valueOf(mv != null ? mv : "FIXED");

                Actor actor = new Actor(name, null);
                if (role == Role.MONSTER) {
                    try {
                        String mid = obj.getProperties().get("monsterID", String.class);
                        Monster monster = monsters.get(mid != null ? mid : name);
                        if (monster != null) {
                            MutableMonster mm = new MutableMonster(monster);
                            actor.set(mm, role, sx, this.baseMap.getHeight() - 1 - sy, x, y, movement);
                        } else {
                            System.err.printf("Cannot load actor: %s %s %s on map %s with creature [%s] icon id [%s]\n",
                                    name, role, movement, this, mid, icon);
                        }
                    } catch (Exception e) {
                        System.err.printf("Cannot find monster: %s on map %s.\n", name, this);
                    }
                } else {
                    MutableMonster mm = null;
                    actor.set(mm, role, sx, this.baseMap.getHeight() - 1 - sy, x, y, movement);
                }

                this.baseMap.actors.add(actor);

            }
        }

    }

    public enum MovementBehavior {

        FIXED,
        WANDER,
        FOLLOW_AVATAR,
        ATTACK_AVATAR;
    }

    public static final int[] THACO_PRIEST = new int[]{20, 20, 20, 18, 18, 18, 16, 16, 16, 14, 14, 14, 12, 12, 12, 10, 10, 10, 8, 8};
    public static final int[] THACO_THIEF = new int[]{20, 20, 19, 19, 18, 18, 17, 17, 16, 16, 15, 15, 14, 14, 13, 13, 12, 12, 11, 11};
    public static final int[] THACO_FIGHTER = new int[]{20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
    public static final int[] THACO_MAGE = new int[]{20, 20, 20, 19, 19, 19, 18, 18, 18, 17, 17, 17, 16, 16, 16, 15, 15, 15, 14, 14};
    public static final int[] THAC0_MONSTER = new int[]{20, 19, 19, 17, 17, 15, 15, 13, 13, 11, 11, 9, 9, 7, 7, 5, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10};

    public static final int[][] LEVEL_PROGRESSION_TABLE = new int[][]{
        {289709, 318529, 304132, 260639, 438479, 456601, 475008, 529756},
        {1000, 1100, 1050, 900, 1200, 1250, 1300, 1450},
        {1724, 1896, 1810, 1551, 2105, 2192, 2280, 2543},
        {2972, 3268, 3120, 2674, 3692, 3845, 4000, 4461},
        {5124, 5634, 5379, 4610, 6477, 6745, 7017, 7826},
        {8834, 9713, 9274, 7948, 11363, 11833, 12310, 13729},
        {15231, 16746, 15989, 13703, 19935, 20759, 21596, 24085},
        {26260, 28872, 27567, 23625, 34973, 36419, 37887, 42254},
        {45275, 49779, 47529, 40732, 61356, 63892, 66468, 74129},
        {78060, 85825, 81946, 70227, 107642, 112091, 116610, 130050},
        {134586, 147974, 141286, 121081, 188845, 196650, 204578, 228157},
        {232044, 255127, 243596, 208760, 331307, 345000, 358908, 400275},
        {400075, 439874, 419993, 359931, 581240, 605263, 629663, 702236}
    };

    public static final String[] HITMSGS = new String[]{
        "whacks",
        "smites",
        "jabs",
        "pokes",
        "wallops",
        "bashes",
        "pounds",
        "smashes",
        "lambasts",
        "whomps",
        "smacks",
        "clouts",};

    public static final String[] DEATHMSGS = new String[]{
        "shuffles off this mortal coil",
        "turns his toes up to the daises",
        "pays an obolus to Charon",
        "kicks the proverbial bucket",
        "departs the land of the living",
        "moans OH MA, I THINK ITS MY TIME"};

    public enum CharacterType {
        FIGHTER, MAGE, PRIEST, THIEF, MIDGET, GIANT, MYTHICAL, DRAGON, ANIMAL,
        WERE, UNDEAD, DEMON, INSECT, ENCHANTED;
    }

    public enum Breath {
        NONE,
        FLAME,
        COLD,
        POISON,
        DRAIN_BREATH,
        STONE,
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
            int mapWidth = Map.WORLD.getTiledMap().getProperties().get("width", Integer.class);
            int tileWidth = Map.WORLD.getTiledMap().getProperties().get("tilewidth", Integer.class);
            MapLayer moongatesLayer = Map.WORLD.getTiledMap().getLayers().get("moongates");
            Iterator<MapObject> iter = moongatesLayer.getObjects().iterator();
            while (iter.hasNext()) {
                MapObject obj = iter.next();
                Moongate mg = Moongate.valueOf(obj.getName());
                mg.x = obj.getProperties().get("x", Float.class);
                mg.y = obj.getProperties().get("y", Float.class);
                mg.mapX = mg.x / tileWidth;
                mg.mapY = mapWidth - (mg.y / tileWidth) - 1;
                int c = 0;
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
        AFRAID(Color.YELLOW, "AF"),
        SILENCED(Color.ORANGE, "SI"),
        ASLEEP(Color.MAGENTA, "SL"),
        POISONED(Color.GREEN, "PO"),
        PARALYZED(Color.CYAN, "PZ"),
        STONED(Color.DARK_GRAY, "ST"),
        ASHES(Color.LIGHT_GRAY, "AS");

        private final Color color;
        private final String display;

        private Status(Color color, String display) {
            this.color = color;
            this.display = display;
        }

        public Color getColor() {
            return color;
        }

        public String getDisplay() {
            return display;
        }

    }

    public enum Role {
        NONE,
        FRIENDLY,
        TEMPLE,
        MONSTER,
        INNKEEPER,
        MERCHANT,
        MERCHANT1,
        MERCHANT2;
    }

    public static final FileHandleResolver CLASSPTH_RSLVR = new FileHandleResolver() {
        @Override
        public FileHandle resolve(String fileName) {
            return Gdx.files.classpath(fileName);
        }
    };

    public enum SpellTarget {
        PERSON, PARTY, MONSTER, GROUP, VARIABLE, NONE, CASTER
    };

    public enum SpellArea {
        COMBAT, ANY_TIME, LOOTING, CAMP, COMBAT_OR_CAMP
    };

    public enum AuraType {

        NONE,
        HORN,
        JINX,
        NEGATE,
        PROTECTION,
        QUICKNESS;
    }

    public enum Resistance {
        NONE,
        MAGIC,
        LVLDRAIN,
        STONING,
        POISON,
        COLD,
        FIRE;
    }

    public enum Ability {
        NONE,
        LVLDRAIN,
        RUN,
        SLEEP,
        AUTOKILL,
        PARALYZE,
        POISON,
        STONE;
    }

    public enum AttackResult {

        HIT,
        MISS;
    }

    public enum CombatAction {
        ATTACK,
        ADVANCE,
        CAST,
        BREATH,
        FLEE;
    }

    public static class AttackVector {

        public final int x;
        public final int y;
        public int distance;
        public AttackResult result;
        public Actor victim;

        public AttackVector(int x, int y, int distance) {
            this.x = x;
            this.y = y;
            this.distance = distance;
        }
    }

    public static class AddActorAction implements Runnable {

        private final com.badlogic.gdx.scenes.scene2d.Actor actor;
        private final Stage stage;

        public AddActorAction(Stage stage, com.badlogic.gdx.scenes.scene2d.Actor actor) {
            this.actor = actor;
            this.stage = stage;
        }

        @Override
        public void run() {
            stage.addActor(actor);
        }
    }

    public static class PlaySoundAction implements Runnable {

        private Sound s;

        public PlaySoundAction(Sound s) {
            this.s = s;
        }

        @Override
        public void run() {
            Sounds.play(s);
        }
    }

    public static class LogAction implements Runnable {

        private BaseScreen screen;
        private String text;

        public LogAction(BaseScreen screen, String text) {
            this.screen = screen;
            this.text = text;
        }

        @Override
        public void run() {
            this.screen.log(this.text);
        }
    }

}
