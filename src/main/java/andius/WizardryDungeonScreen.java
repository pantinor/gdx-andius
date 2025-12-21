package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
import andius.objects.Direction;
import andius.dialogs.RiddleDialog;
import static andius.Andius.CTX;
import static andius.Andius.mainGame;
import static andius.Andius.startScreen;
import andius.WizardryData.MazeAddress;
import andius.WizardryData.MazeCell;
import andius.WizardryData.MazeLevel;
import static andius.WizardryData.WER_MESSAGES;
import static andius.WizardryData.getMessage;
import static andius.objects.Direction.*;
import andius.objects.DoGooder;
import andius.objects.Item;
import andius.objects.Monster;
import utils.MoveCameraAction;
import utils.PanCameraAction;
import andius.objects.SaveGame;
import andius.objects.SaveGame.AnsweredRiddle;
import andius.objects.SaveGame.CharacterRecord;
import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import java.util.HashMap;
import java.util.Iterator;
import utils.ObjLoader;
import utils.RotateOnlyInputController;
import utils.Utils;
import static utils.Utils.CLASSPTH_RSLVR;

public class WizardryDungeonScreen extends BaseScreen {

    private static final int MAP_WIDTH = 672;
    private static final int MAP_HEIGHT = 672;

    private final ModelBuilder builder = new ModelBuilder();
    private final ModelBatch modelBatch;
    private final SpriteBatch batch;

    private RotateOnlyInputController cameraPan;
    private final AssetManager assets;

    private Model ladderUp, ladderDown, elevatorModel, pentagram, topHole, bottomHole, letterM, fountainModel, chestModel;
    private Model[] markModels = new Model[4];
    private Model[] walls = new Model[4];
    private Model[] doors = new Model[4];
    public DungeonTileModelInstance floorPlane;
    public DungeonTileModelInstance ceilingPlane;

    private final Environment environment = new Environment();
    private final Environment outside = new Environment();

    private final List<SpotLightInfo> spotLights = new ArrayList<>();

    private final Color darkness = Color.DARK_GRAY;
    private final Color flame = new Color(0xf59414ff);
    private final Color wallColor = new Color(0x646778ff);

    private SpotLight torch;
    boolean isTorchOn = true;
    private DirectionalLight directionalLightDown;
    private DirectionalLight directionalLightUp;

    public final List<DungeonTileModelInstance> modelInstances = new ArrayList<>();
    private final List<DungeonTileModelInstance> floor = new ArrayList<>();
    private final List<DungeonTileModelInstance> ceiling = new ArrayList<>();

    private final List<DungeonTileModelInstance> wiz4CastleLevel0ModelInstances = new ArrayList<>();
    private final List<DungeonTileModelInstance> wiz4CastleLevel12ModelInstances = new ArrayList<>();
    private final List<DungeonTileModelInstance> wiz4CastleLevel13ModelInstances = new ArrayList<>();

    private final TextureRegion[][] arrows = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/arrows.png")), 15, 15);

    public int currentLevel = 0;
    private final Vector3 currentPos = new Vector3(), workV = new Vector3();
    private Direction currentDir = NORTH;
    private boolean loadedMazeData;

    private boolean showMiniMap = false;
    private Texture miniMap;
    private final MiniMapIcon miniMapIcon;
    private final Pixmap miniMapIconsPixmap;
    private final int miniMapBackgroundDimension;
    private Texture miniMapBackground;
    private final int dim;//dimension of the map
    private final int xalignMM;
    private final int yalignMM;

    private static final int MINI_DIM = 24;

    public final Constants.Map map;

    public WizardryDungeonScreen(Constants.Map map) {
        this.map = map;
        this.stage = new Stage();
        //this.stage.setDebugAll(true);
        this.assets = new AssetManager(CLASSPTH_RSLVR);

        this.dim = map.scenario().dim();
        this.miniMapBackgroundDimension = MINI_DIM * this.dim + 8;
        this.xalignMM = Andius.SCREEN_WIDTH - this.miniMapBackgroundDimension - 10;
        this.yalignMM = Andius.SCREEN_HEIGHT - this.miniMapBackgroundDimension - 10;
        this.miniMapBackground = Utils.fillRectangle(miniMapBackgroundDimension + 7, miniMapBackgroundDimension + 7, new Color(0x009900ff), 1);

        arrows[3][2].getTexture().getTextureData().prepare();
        this.miniMapIconsPixmap = arrows[3][2].getTexture().getTextureData().consumePixmap();

        DefaultShader.Config config = new DefaultShader.Config();
        config.vertexShader = Gdx.files.internal("assets/dungeon.vertex.glsl").readString();
        config.fragmentShader = Gdx.files.internal("assets/dungeon.fragment.glsl").readString();
        config.numSpotLights = 16;

        this.modelBatch = new ModelBatch(new DefaultShaderProvider(config));
        this.batch = new SpriteBatch();

        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.12f, 0.12f, 0.12f, 1f));
        this.torch = new SpotLight();
        this.environment.add(this.torch);

        this.outside.set(ColorAttribute.createAmbient(0.8f, 0.8f, 0.8f, 1f));
        this.directionalLightDown = new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0.2274f, -0.8961f, 0.3811f);
        this.directionalLightUp = new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.1865f, 0.9098f, -0.3709f);
        this.outside.add(this.directionalLightDown);
        this.outside.add(this.directionalLightUp);

        this.camera = new PerspectiveCamera(67f, MAP_WIDTH, MAP_HEIGHT);
        this.camera.near = 0.1f;
        this.camera.far = 10f;

        init();

        this.stage.addActor(new MiniMapActor());
        this.miniMapIcon = new MiniMapIcon();
        this.stage.addActor(miniMapIcon);

        addButtons(this.map);

        setMapPixelCoords(null, this.map.scenario().getStartX(), this.map.scenario().getStartY(), this.map.scenario().getStartLevel());

        camera.position.set(currentPos);
        camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
        //currentLevel = 2;
        //camera.position.set(-1, 5, 3);
        //camera.lookAt(3, 0, 3);
        //this.isTorchOn = true;
        //this.showMiniMap = false;

        cameraPan = new RotateOnlyInputController(camera);
    }

    private void init() {
        assets.load("assets/graphics/floor.png", Texture.class);
        assets.load("assets/graphics/dirt.png", Texture.class);
        assets.load("assets/graphics/wall1.png", Texture.class);
        assets.load("assets/graphics/wall2.png", Texture.class);
        assets.load("assets/graphics/wall3.png", Texture.class);
        assets.load("assets/graphics/wall4.png", Texture.class);
        assets.load("assets/graphics/rock.png", Texture.class);
        assets.load("assets/graphics/grass.png", Texture.class);
        assets.load("assets/graphics/roof.png", Texture.class);
        assets.update(2000);

        Texture floorTex = assets.get("assets/graphics/rock.png", Texture.class);
        floorTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        Texture dirtTex = assets.get("assets/graphics/dirt.png", Texture.class);
        dirtTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        Texture roofTex = assets.get("assets/graphics/roof.png", Texture.class);
        roofTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        Material mwall1 = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/wall1.png", Texture.class)), IntAttribute.createCullFace(GL20.GL_NONE));
        Material mwall2 = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/wall2.png", Texture.class)), IntAttribute.createCullFace(GL20.GL_NONE));
        Material mwall3 = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/wall3.png", Texture.class)), IntAttribute.createCullFace(GL20.GL_NONE));
        Material mwall4 = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/wall4.png", Texture.class)), IntAttribute.createCullFace(GL20.GL_NONE));
        Material mdirt = new Material(TextureAttribute.createDiffuse(dirtTex), IntAttribute.createCullFace(GL20.GL_NONE));
        Material mroof = new Material(TextureAttribute.createDiffuse(roofTex), IntAttribute.createCullFace(GL20.GL_NONE));
        Material mfloor = new Material(TextureAttribute.createDiffuse(floorTex), IntAttribute.createCullFace(GL20.GL_NONE));
        Material mgreen = new Material(ColorAttribute.createDiffuse(Color.FOREST), IntAttribute.createCullFace(GL20.GL_NONE));
        Material medges = new Material(ColorAttribute.createDiffuse(wallColor), IntAttribute.createCullFace(GL20.GL_NONE));
        Texture grz = assets.get("assets/graphics/grass.png", Texture.class);
        grz.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        Material mgrass = new Material(TextureAttribute.createDiffuse(grz), IntAttribute.createCullFace(GL20.GL_NONE));

        ladderUp = ObjLoader.loadModel("assets/graphics/ladder.obj", "Ladder", 0.1f);
        ladderDown = ObjLoader.loadModel("assets/graphics/ladder.obj", "LadderDown", 0.1f);
        topHole = ObjLoader.loadModel("assets/graphics/ladder.obj", "TopHole", 0.1f);
        bottomHole = ObjLoader.loadModel("assets/graphics/ladder.obj", "BottomHole", 0.1f);
        pentagram = ObjLoader.loadModel("assets/graphics/pentagram.obj", "pentagram", 0.1f);
        letterM = ObjLoader.loadModel("assets/graphics/letter-m.obj", "letter-m", 0.1f);
        fountainModel = ObjLoader.loadModel("assets/graphics/fountain.obj", "fountain", 0.1f);
        chestModel = ObjLoader.loadModel("assets/graphics/chest.obj", "chest", 0.1f);
        elevatorModel = ObjLoader.loadModel("assets/graphics/elevator.obj", "elevator-booth", 0.1f);
        markModels[0] = Utils.getMark(builder, ObjLoader.loadModel("assets/graphics/mark-king.obj", "mark-king", 0.1f));
        markModels[1] = Utils.getMark(builder, ObjLoader.loadModel("assets/graphics/mark-fire.obj", "mark-fire", 0.1f));
        markModels[2] = Utils.getMark(builder, ObjLoader.loadModel("assets/graphics/mark-force.obj", "mark-force", 0.1f));
        markModels[3] = Utils.getMark(builder, ObjLoader.loadModel("assets/graphics/mark-snake.obj", "mark-snake", 0.1f));

        walls[0] = Utils.createWall(builder, mwall1, medges);
        walls[1] = Utils.createWall(builder, mwall2, medges);
        walls[2] = Utils.createWall(builder, mwall3, medges);
        walls[3] = Utils.createWall(builder, mwall4, medges);

        doors[0] = Utils.getDoor(builder, walls[0]);
        doors[1] = Utils.getDoor(builder, walls[1]);
        doors[2] = Utils.getDoor(builder, walls[2]);
        doors[3] = Utils.getDoor(builder, walls[3]);

        floorPlane = new DungeonTileModelInstance(Utils.createPlaneModel(builder, dim, mfloor, false), 0, 0, 0, -2f, 0f, -2f);
        ceilingPlane = new DungeonTileModelInstance(Utils.createPlaneModel(builder, dim, mdirt, true), 0, 0, 0, -2f, 1f, -2f);

        if (this.map == Map.WIZARDRY4) {

            Model floorModel = Utils.createThinBox(builder, mfloor, medges);
            Model ceilingModel = Utils.createThinBox(builder, mroof, mdirt, medges);
            Model sky = Utils.createSky(builder);
            sky.nodes.get(0).translation.set(10, 0, 10);

            DungeonTileModelInstance sk = new DungeonTileModelInstance(sky, 0, 0, 0);
            this.wiz4CastleLevel0ModelInstances.add(sk);
            this.wiz4CastleLevel12ModelInstances.add(sk);
            this.wiz4CastleLevel13ModelInstances.add(sk);

            Model grassPlaneModel = Utils.createPlaneModel(builder, this.dim, mgrass, false);
            this.wiz4CastleLevel0ModelInstances.add(new DungeonTileModelInstance(grassPlaneModel, 0, 0, 0, 0, -.06f, 0));
            this.wiz4CastleLevel12ModelInstances.add(new DungeonTileModelInstance(grassPlaneModel, 0, 0, 0, 0, -1.16f, 0));
            this.wiz4CastleLevel13ModelInstances.add(new DungeonTileModelInstance(grassPlaneModel, 0, 0, 0, 0, -2.26f, 0));

            TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
            TiledMap wiz4CastleFloorCeilingMap = loader.load("assets/data/wiz4-castle-floor-ceiling-map.tmx");
            TiledMapTileLayer floor1 = (TiledMapTileLayer) wiz4CastleFloorCeilingMap.getLayers().get("floor1");
            TiledMapTileLayer floor2 = (TiledMapTileLayer) wiz4CastleFloorCeilingMap.getLayers().get("floor2");
            TiledMapTileLayer floor3 = (TiledMapTileLayer) wiz4CastleFloorCeilingMap.getLayers().get("floor3");
            TiledMapTileLayer ceiling1 = (TiledMapTileLayer) wiz4CastleFloorCeilingMap.getLayers().get("ceiling1");
            TiledMapTileLayer ceiling2 = (TiledMapTileLayer) wiz4CastleFloorCeilingMap.getLayers().get("ceiling2");
            TiledMapTileLayer ceiling3 = (TiledMapTileLayer) wiz4CastleFloorCeilingMap.getLayers().get("ceiling3");

            castleFloorAndCeiling(wiz4CastleLevel0ModelInstances, floor1, floorModel, -.05f);
            castleFloorAndCeiling(wiz4CastleLevel0ModelInstances, ceiling1, ceilingModel, 1.05f);
            castleFloorAndCeiling(wiz4CastleLevel0ModelInstances, floor2, floorModel, 1.15f);
            castleFloorAndCeiling(wiz4CastleLevel0ModelInstances, ceiling2, ceilingModel, 2.25f);
            castleFloorAndCeiling(wiz4CastleLevel0ModelInstances, floor3, floorModel, 2.35f);
            castleFloorAndCeiling(wiz4CastleLevel0ModelInstances, ceiling3, ceilingModel, 3.35f);

            castleFloorAndCeiling(wiz4CastleLevel12ModelInstances, ceiling1, ceilingModel, -.15f);
            castleFloorAndCeiling(wiz4CastleLevel12ModelInstances, floor2, floorModel, -.05f);
            castleFloorAndCeiling(wiz4CastleLevel12ModelInstances, ceiling2, ceilingModel, 1.05f);
            castleFloorAndCeiling(wiz4CastleLevel12ModelInstances, floor3, floorModel, 1.15f);
            castleFloorAndCeiling(wiz4CastleLevel12ModelInstances, ceiling3, ceilingModel, 2.25f);

            castleFloorAndCeiling(wiz4CastleLevel13ModelInstances, ceiling1, ceilingModel, -1.25f);
            castleFloorAndCeiling(wiz4CastleLevel13ModelInstances, floor2, floorModel, -1.15f);
            castleFloorAndCeiling(wiz4CastleLevel13ModelInstances, ceiling2, ceilingModel, -.15f);
            castleFloorAndCeiling(wiz4CastleLevel13ModelInstances, floor3, floorModel, -.05f);
            castleFloorAndCeiling(wiz4CastleLevel13ModelInstances, ceiling3, ceilingModel, 1.05f);

            for (int x = 0; x < this.dim; x++) {
                for (int y = 0; y < this.dim; y++) {
                    addCastleCell(this.wiz4CastleLevel0ModelInstances, 0, this.map.scenario().levels()[0].cells[x][y], x, y, .5f);
                    addCastleCell(this.wiz4CastleLevel0ModelInstances, 12, this.map.scenario().levels()[12].cells[x][y], x, y, 1.7f);
                    addCastleCell(this.wiz4CastleLevel0ModelInstances, 13, this.map.scenario().levels()[13].cells[x][y], x, y, 2.85f);

                    addCastleCell(this.wiz4CastleLevel12ModelInstances, 0, this.map.scenario().levels()[0].cells[x][y], x, y, -.65f);
                    addCastleCell(this.wiz4CastleLevel12ModelInstances, 12, this.map.scenario().levels()[12].cells[x][y], x, y, .5f);
                    addCastleCell(this.wiz4CastleLevel12ModelInstances, 13, this.map.scenario().levels()[13].cells[x][y], x, y, 1.7f);

                    addCastleCell(this.wiz4CastleLevel13ModelInstances, 0, this.map.scenario().levels()[0].cells[x][y], x, y, -1.75f);
                    addCastleCell(this.wiz4CastleLevel13ModelInstances, 12, this.map.scenario().levels()[12].cells[x][y], x, y, -.65f);
                    addCastleCell(this.wiz4CastleLevel13ModelInstances, 13, this.map.scenario().levels()[13].cells[x][y], x, y, .5f);
                }
            }
        }

        for (int level = 0; level < this.map.scenario().levels().length; level++) {
            for (int e = 0; e < this.dim; e++) {
                for (int n = 0; n < this.dim; n++) {
                    MazeCell cell = this.map.scenario().levels()[level].cells[n][e];
                    addBlock(level, cell, n, e);
                    if (this.map != Map.WIZARDRY4 || (level == 4 || level == 6)) {
                        //duplicated for wrapping
                        addBlock(level, cell, n + this.dim, e);
                        addBlock(level, cell, n - this.dim, e);
                        addBlock(level, cell, n, e + this.dim);
                        addBlock(level, cell, n, e - this.dim);
                        addBlock(level, cell, n + this.dim, e + this.dim);
                        addBlock(level, cell, n - this.dim, e - this.dim);
                        addBlock(level, cell, n + this.dim, e - this.dim);
                        addBlock(level, cell, n - this.dim, e + this.dim);
                    }
                    if (cell.markType >= 0 || cell.summoningCircle != null || cell.fountainType >= 0
                            || cell.message != null || cell.function != null) {
                        SpotLight orbLight = new SpotLight().set(
                                flame.r, flame.g, flame.b,
                                n + .5f, 1f, e + .5f,
                                0f, -1f, 0f, // direction: down
                                2.7f, 95f, 10f
                        );
                        spotLights.add(new SpotLightInfo(level, n, e, orbLight));
                    }
                }
            }
        }

        pruneWrappedInstances();

    }

    @Override
    public void setMapPixelCoords(Vector3 v, int x, int y, int z) {
        currentPos.x = x + .5f;
        currentPos.y = .5f;
        currentPos.z = y + .5f;
        currentLevel = z - 1;
    }

    @Override
    public void getCurrentMapCoords(Vector3 v) {
        int x = (Math.round(currentPos.x) - 1);
        int y = (Math.round(currentPos.z) - 1);
        v.set(x, y, currentLevel);
    }

    public MazeCell currentCell() {
        int x = (Math.round(currentPos.x) - 1);
        int y = (Math.round(currentPos.z) - 1);
        return this.map.scenario().levels()[currentLevel].cells[x][y];
    }

    public MazeCell cell(int x, int y, int z) {
        return this.map.scenario().levels()[z].cells[x][y];
    }

    @Override
    public void save(SaveGame saveGame) {
        CTX.saveGame.map = this.map;
        CTX.saveGame.x = (Math.round(currentPos.x) - 1);
        CTX.saveGame.y = (Math.round(currentPos.z) - 1);
        CTX.saveGame.level = currentLevel + 1;
        CTX.saveGame.direction = this.currentDir;

        for (int level = 0; level < this.map.scenario().levels().length; level++) {
            for (int x = 0; x < this.dim; x++) {
                for (int y = 0; y < this.dim; y++) {
                    MazeCell cell = this.map.scenario().levels()[level].cells[x][y];
                    List<AnsweredRiddle> riddles = CTX.saveGame.riddles.get(this.map);
                    if (riddles == null) {
                        riddles = new ArrayList<>();
                        CTX.saveGame.riddles.put(this.map, riddles);
                    }
                    if (cell.riddleAnswers != null && cell.riddleAnswers.isEmpty()) {
                        AnsweredRiddle ar = new AnsweredRiddle(level, x, y);
                        if (!riddles.contains(ar)) {
                            riddles.add(ar);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void load(SaveGame saveGame) {

        currentPos.x = saveGame.x + .5f;
        currentPos.y = .5f;
        currentPos.z = saveGame.y + .5f;
        currentLevel = saveGame.level - 1;
        currentDir = saveGame.direction;

        if (currentDir == null) {
            currentDir = NORTH;
        }

        camera.position.set(currentPos);

        switch (currentDir) {
            case EAST:
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
                break;
            case WEST:
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
                break;
            case NORTH:
                camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
                break;
            case SOUTH:
                camera.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
                break;
            default:
                camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
                break;
        }

        camera.up.set(Vector3.Y);
        camera.update();

        loadMazeData(saveGame);
    }

    private void loadMazeData(SaveGame saveGame) {

        if (this.loadedMazeData) {
            return;
        }

        List<SaveGame.AnsweredRiddle> riddles = saveGame.riddles.get(this.map);
        if (riddles != null) {
            for (SaveGame.AnsweredRiddle ar : riddles) {
                MazeCell cell = this.map.scenario().levels()[ar.level].cells[ar.x][ar.y];
                cell.riddleAnswers.clear();
            }
        }

        this.loadedMazeData = true;
    }

    @Override
    public void log(String s) {
        Andius.HUD.log(s);
    }

    @Override
    public void show() {
        Andius.HUD.addActor(this.stage);
        setInputProcessor();
        loadMazeData(CTX.saveGame);
        createMiniMap();
        moveMiniMapIcon();
    }

    public void setInputProcessor() {
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage, cameraPan));
    }

    @Override
    public void hide() {
    }

    private void addBlock(int level, MazeCell cell, float x, float y) {
        addBlock(level, cell, x, y, false);
    }

    void addBlock(int level, MazeCell cell, float x, float y, boolean clear) {

        if (clear) {
            Iterator<DungeonTileModelInstance> iter = modelInstances.iterator();
            while (iter.hasNext()) {
                DungeonTileModelInstance dmi = iter.next();
                if (dmi.getLevel() == level && dmi.getCx() == x && dmi.getCy() == y) {
                    iter.remove();
                }
            }
        }

        int index = level % walls.length;
        Model wallModel = walls[index];
        Model doorModel = doors[index];

        float z = 0.5f;
        if (cell.northWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wallModel, level, x, y);
            instance.transform.setFromEulerAngles(270, 0, 0).trn(x + 1 - .025f, z, y + .5f);
            modelInstances.add(instance);
        }
        if (cell.southWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wallModel, level, x, y);
            instance.transform.setFromEulerAngles(90, 0, 0).trn(x + .025f, z, y + .5f);
            modelInstances.add(instance);
        }
        if (cell.eastWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wallModel, level, x, y);
            instance.transform.setFromEulerAngles(180, 0, 0).trn(x + .5f, z, y - .025f + 1);
            modelInstances.add(instance);
        }
        if (cell.westWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wallModel, level, x, y, x + .5f, z, y + .025f);
            modelInstances.add(instance);
        }

        if (cell.hiddenNorthDoor) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wallModel, level, x, y);
            instance.transform.setFromEulerAngles(270, 0, 0).trn(x + 1 - .025f, z, y + .5f);
            modelInstances.add(instance);
        } else if (cell.northDoor) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y);
            instance.transform.setFromEulerAngles(90, 0, 0).trn(1 + x - .025f, z, 1 + y - .5f);
            modelInstances.add(instance);
        }

        if (cell.hiddenSouthDoor) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wallModel, level, x, y);
            instance.transform.setFromEulerAngles(90, 0, 0).trn(x + .025f, z, y + .5f);
            modelInstances.add(instance);
        } else if (cell.southDoor) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y);
            instance.transform.setFromEulerAngles(270, 0, 0).trn(x + .025f, z, y + .5f);
            modelInstances.add(instance);
        }

        if (cell.hiddenEastDoor) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wallModel, level, x, y);
            instance.transform.setFromEulerAngles(180, 0, 0).trn(x + .5f, z, y - .025f + 1);
            modelInstances.add(instance);
        } else if (cell.eastDoor) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y, x - .5f + 1, z, y - .025f + 1);
            modelInstances.add(instance);
        }

        if (cell.hiddenWestDoor) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wallModel, level, x, y);
            instance.transform.setToTranslation(x + .5f, z, y + .025f);
            modelInstances.add(instance);
        } else if (cell.westDoor) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y);
            instance.transform.setFromEulerAngles(0, 180, 180).trn(x + .5f, z, y + .025f);
            modelInstances.add(instance);
        }

        if (cell.stairs) {
            if (cell.address.level < cell.addressTo.level) {//down
                modelInstances.add(new DungeonTileModelInstance(ladderDown, level, x, y, x + .5f, 0, y + .5f));
                modelInstances.add(new DungeonTileModelInstance(bottomHole, level, x, y, x + .5f, 0, y + .5f));
            } else {//up
                modelInstances.add(new DungeonTileModelInstance(ladderUp, level, x, y, x + .5f, 0, y + .5f));
                modelInstances.add(new DungeonTileModelInstance(topHole, level, x, y, x + .5f, .95f, y + .5f));
            }
        }
        if (cell.elevator) {
            modelInstances.add(new DungeonTileModelInstance(elevatorModel, level, x, y, x + .5f, 0, y + .5f));
        }
        if (cell.summoningCircle != null) {
            modelInstances.add(new DungeonTileModelInstance(pentagram, level, x, y, x + .5f, 0, y + .5f));
        }
        if (cell.chestType >= 0) {
            modelInstances.add(new DungeonTileModelInstance(chestModel, level, x, y, x + .5f, 0, y + .5f));
        }
        if (cell.markType >= 0) {
            modelInstances.add(new DungeonTileModelInstance(markModels[cell.markType - 1], level, x, y, x + .5f, 0, y + .5f));
        } else if (cell.fountainType >= 0) {
            modelInstances.add(new DungeonTileModelInstance(fountainModel, level, x, y, x + .5f, 0, y + .5f));
        } else if (cell.message != null || cell.function != null) {
            modelInstances.add(new DungeonTileModelInstance(letterM, level, x, y, x + .5f, .5f, y + .5f, true));
        }

    }

    private void rotateBlock(int level, MazeCell cell, float x, float y) {
        if (this.map.scenario().levels()[level].rotate(cell)) {
            addBlock(level, cell, x, y, true);
        }
    }

    private void castleFloorAndCeiling(List<DungeonTileModelInstance> list, TiledMapTileLayer layer, Model model, float z) {
        for (int x = 0; x < this.dim; x++) {
            for (int y = 0; y < this.dim; y++) {
                TiledMapTileLayer.Cell c = layer.getCell(x, this.dim - 1 - y);
                if (c != null) {
                    list.add(new DungeonTileModelInstance(model, 0, 0, 0, this.dim - 1 - y + .5f, z, x + .5f));
                }
            }
        }
    }

    private void addCastleCell(List<DungeonTileModelInstance> list, int level, MazeCell cell, float x, float y, float z) {

        Model wallModel = walls[2];
        Model doorModel = doors[2];

        if (cell.northWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wallModel, level, x, y);
            instance.transform.setFromEulerAngles(270, 0, 0).trn(1 + x - .025f, z, 1 + y - .5f);
            list.add(instance);
        }
        if (cell.southWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wallModel, level, x, y);
            instance.transform.setFromEulerAngles(90, 0, 0).trn(x + .025f, z, y + .5f);
            list.add(instance);
        }
        if (cell.eastWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wallModel, level, x, y);
            instance.transform.setFromEulerAngles(0, 0, 180).trn(x + .5f, z, 1 + y - .025f);
            list.add(instance);
        }
        if (cell.westWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wallModel, level, x, y, x + .5f, z, y + .025f);
            list.add(instance);
        }

        if (cell.hiddenNorthDoor) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wallModel, level, x, y);
            instance.transform.setFromEulerAngles(270, 0, 0).trn(1 + x - .025f, z, 1 + y - .5f);
            list.add(instance);
        } else if (cell.northDoor) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y);
            instance.transform.setFromEulerAngles(90, 0, 0).trn(1 + x - .025f, z, 1 + y - .5f);
            list.add(instance);
        }

        if (cell.hiddenSouthDoor) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wallModel, level, x, y);
            instance.transform.setFromEulerAngles(90, 0, 0).trn(x + .025f, z, y + .5f);
            list.add(instance);
        } else if (cell.southDoor) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y);
            instance.transform.setFromEulerAngles(270, 0, 0).trn(x + .025f, z, y + .5f);
            list.add(instance);
        }

        if (cell.hiddenEastDoor) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wallModel, level, x, y);
            instance.transform.setFromEulerAngles(0, 0, 180).trn(x + .5f, z, 1 + y - .025f);
            list.add(instance);
        } else if (cell.eastDoor) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y, x - .5f + 1, z, y - .025f + 1);
            list.add(instance);
        }

        if (cell.hiddenWestDoor) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wallModel, level, x, y, x + .5f, z, y + .025f);
            list.add(instance);
        } else if (cell.westDoor) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y);
            instance.transform.setFromEulerAngles(0, 180, 180).trn(x + .5f, z, y + .025f);
            list.add(instance);
        }

        if (cell.stairs) {
            if (level == 13 || cell.address.level > cell.addressTo.level) {//down
                list.add(new DungeonTileModelInstance(ladderDown, level, x, y, x + .5f, z - .5f, y + .5f));
                list.add(new DungeonTileModelInstance(bottomHole, level, x, y, x + .5f, z - .5f, y + .5f));
            } else {//up
                list.add(new DungeonTileModelInstance(ladderUp, level, x, y, x + .5f, z - .5f, y + .5f));
                list.add(new DungeonTileModelInstance(topHole, level, x, y, x + .5f, z + .45f, y + .5f));
            }
        }
        if (cell.elevator) {
            modelInstances.add(new DungeonTileModelInstance(elevatorModel, level, x, y, x + .5f, z, y + .5f));
        }
        if (cell.message != null || cell.function != null) {
            list.add(new DungeonTileModelInstance(letterM, level, x, y, x + .5f, z, y + .5f, true));
        }
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        if (this.map != Map.WIZARDRY4 || (this.currentLevel >= 1 && this.currentLevel <= 11)) {
            if (isTorchOn) {
                this.torch.set(flame, camera.position, camera.direction, 2.5f, 65, 5);
            } else {
                this.torch.set(darkness, camera.position, camera.direction, 0.5f, 65, 5);
            }
            camera.far = 10f;
        } else {
            camera.far = 100f;
        }

        Gdx.gl.glViewport(32, 64, MAP_WIDTH, MAP_HEIGHT);

        stage.act();

        camera.update();

        modelBatch.begin(camera);

        if (this.map != Map.WIZARDRY4 || (this.currentLevel >= 1 && this.currentLevel <= 11)) {

            floorPlane.render(modelBatch, environment);
            ceilingPlane.render(modelBatch, environment);

            //render only things inside a circle of radius 12 around the camera
            final float radius = 12f;
            final float r2 = radius * radius;
            for (DungeonTileModelInstance i : modelInstances) {
                if (i.getLevel() != currentLevel) {
                    continue;
                }
                i.transform.getTranslation(workV);
                float dx = workV.x - camera.position.x;
                float dz = workV.z - camera.position.z;
                if (dx * dx + dz * dz > r2) {
                    continue;
                }
                i.render(modelBatch, environment);
            }
        } else {
            if (this.currentLevel == 0) {
                for (DungeonTileModelInstance i : wiz4CastleLevel0ModelInstances) {
                    i.render(modelBatch, outside);
                }
            } else if (this.currentLevel == 12) {
                for (DungeonTileModelInstance i : this.wiz4CastleLevel12ModelInstances) {
                    i.render(modelBatch, outside);
                }
            } else if (this.currentLevel == 13) {
                for (DungeonTileModelInstance i : this.wiz4CastleLevel13ModelInstances) {
                    i.render(modelBatch, outside);
                }
            }
        }

        modelBatch.end();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.begin();

        batch.draw(Andius.backGround, 0, 0);

        Andius.HUD.render(batch, Andius.CTX);
        if (this.map == Map.WIZARDRY4) {
            Andius.HUD.renderSummonedMonsters(batch, Andius.CTX);
        }

        int x = (Math.round(currentPos.x) - 1);
        int y = (Math.round(currentPos.z) - 1);
        if (this.map.scenario().getLevelDescriptions() != null) {
            String lbl = String.format(this.map.scenario().getLevelDescriptions()[currentLevel] + " - Level %d [%d, %d]", currentLevel + 1, x, y).toUpperCase();
            Andius.font18.draw(batch, lbl, 280, Andius.SCREEN_HEIGHT - 12);
        } else {
            String lbl = String.format(this.map.getLabel() + " - Level %d [%d, %d]", currentLevel + 1, x, y).toUpperCase();
            Andius.font18.draw(batch, lbl, 280, Andius.SCREEN_HEIGHT - 12);
        }

        batch.end();

        stage.draw();

    }

    private void createMiniMap() {

        if (miniMap != null) {
            miniMap.dispose();
        }

        int yup = miniMapBackgroundDimension - 4;
        int wt = 3;

        Pixmap pixmap = new Pixmap(miniMapBackgroundDimension, miniMapBackgroundDimension, Format.RGBA8888);

        for (int e = 0; e < this.dim; e++) {
            for (int n = 0; n < this.dim; n++) {
                MazeCell cell = this.map.scenario().levels()[this.currentLevel].cells[n][e];

                int x = 4 + e * MINI_DIM;
                int y = n * MINI_DIM + MINI_DIM;

                if (cell.darkness) {
                    pixmap.setColor(Color.PURPLE);
                    pixmap.fillRectangle(x, yup - y, MINI_DIM, MINI_DIM);
                }
                if (cell.rock) {
                    pixmap.setColor(Color.BROWN);
                    pixmap.fillRectangle(x, yup - y, MINI_DIM, MINI_DIM);
                }
                if (cell.spellsBlocked) {
                    pixmap.setColor(Color.VIOLET);
                    pixmap.fillRectangle(x, yup - y, MINI_DIM, MINI_DIM);
                }
                if (cell.chute) {
                    pixmap.drawPixmap(
                            this.miniMapIconsPixmap,
                            x + 4,
                            yup - y + 4,
                            arrows[2][3].getRegionX(),
                            arrows[2][3].getRegionY(),
                            arrows[2][3].getRegionWidth(),
                            arrows[2][3].getRegionHeight()
                    );
                }
                if (cell.encounterID >= 0) {
                    pixmap.setColor(Color.RED);
                    pixmap.fillCircle(x + MINI_DIM / 2, yup - y + MINI_DIM / 2, 5);
                }
                if (cell.wanderingEncounterID != -1) {
                    pixmap.setColor(cell.hasTreasureChest ? Color.MAGENTA : Color.PINK);
                    pixmap.fillCircle(x + MINI_DIM / 2, yup - y + MINI_DIM / 2, 3);
                }

                if (cell.itemRequired > 0) {
                    pixmap.setColor(Color.FIREBRICK);
                    pixmap.fillRectangle(x, yup - y, MINI_DIM, MINI_DIM);
                }

                if (cell.itemObtained > 0) {
                    pixmap.setColor(Color.CHARTREUSE);
                    pixmap.fillRectangle(x, yup - y, MINI_DIM, MINI_DIM);
                }

                if (cell.chestType >= 0) {
                    pixmap.drawPixmap(
                            this.miniMapIconsPixmap,
                            x + 4,
                            yup - y + 4,
                            arrows[3][3].getRegionX(),
                            arrows[3][3].getRegionY(),
                            arrows[3][3].getRegionWidth(),
                            arrows[3][3].getRegionHeight()
                    );
                }

                if (cell.fountainType >= 0) {
                    pixmap.setColor(Color.SKY);
                    pixmap.fillCircle(x + MINI_DIM / 2, yup - y + MINI_DIM / 2, 5);
                } else if (cell.markType >= 0) {
                    pixmap.setColor(Color.YELLOW);
                    pixmap.fillCircle(x + MINI_DIM / 2, yup - y + MINI_DIM / 2, 5);
                } else if (cell.message != null || cell.function != null) {
                    pixmap.drawPixmap(
                            this.miniMapIconsPixmap,
                            x + 4,
                            yup - y + 4,
                            arrows[0][0].getRegionX(),
                            arrows[0][0].getRegionY(),
                            arrows[0][0].getRegionWidth(),
                            arrows[0][0].getRegionHeight()
                    );
                }

                if (cell.pit) {
                    pixmap.setColor(Color.BROWN);
                    pixmap.fillCircle(x + MINI_DIM / 2, yup - y + MINI_DIM / 2, 5);
                    pixmap.setColor(Color.WHITE);
                    pixmap.fillCircle(x + MINI_DIM / 2, yup - y + MINI_DIM / 2, 2);
                } else if (cell.damage != null) {
                    pixmap.drawPixmap(
                            this.miniMapIconsPixmap,
                            x + 4,
                            yup - y + 4,
                            arrows[1][0].getRegionX(),
                            arrows[1][0].getRegionY(),
                            arrows[1][0].getRegionWidth(),
                            arrows[1][0].getRegionHeight()
                    );
                }
                if (cell.cage) {
                    pixmap.drawPixmap(
                            this.miniMapIconsPixmap,
                            x + 4,
                            yup - y + 4,
                            arrows[1][1].getRegionX(),
                            arrows[1][1].getRegionY(),
                            arrows[1][1].getRegionWidth(),
                            arrows[1][1].getRegionHeight()
                    );
                }
                if (cell.spinner) {
                    pixmap.drawPixmap(
                            this.miniMapIconsPixmap,
                            x + 4,
                            yup - y + 4,
                            arrows[2][2].getRegionX(),
                            arrows[2][2].getRegionY(),
                            arrows[2][2].getRegionWidth(),
                            arrows[2][2].getRegionHeight()
                    );
                }

                if (cell.teleport) {
                    pixmap.setColor(new Color(0xcc9900ff));
                    pixmap.fillCircle(x + MINI_DIM / 2, yup - y + MINI_DIM / 2, 5);
                    pixmap.setColor(Color.WHITE);
                    pixmap.fillCircle(x + MINI_DIM / 2, yup - y + MINI_DIM / 2, 2);
                }

                pixmap.setColor(Color.WHITE);
                if (cell.northWall || cell.northDoor || cell.hiddenNorthDoor) {
                    pixmap.fillRectangle(x, yup - y, MINI_DIM, wt);
                }
                if (cell.southWall || cell.southDoor || cell.hiddenSouthDoor) {
                    pixmap.fillRectangle(x, yup - y + MINI_DIM - wt, MINI_DIM, wt);
                }
                if (cell.eastWall || cell.eastDoor || cell.hiddenEastDoor) {
                    pixmap.fillRectangle(x + MINI_DIM - wt, yup - y, wt, MINI_DIM);
                }
                if (cell.westWall || cell.westDoor || cell.hiddenWestDoor) {
                    pixmap.fillRectangle(x, yup - y, wt, MINI_DIM);
                }

                pixmap.setColor(new Color(0xcc9900ff));
                if (cell.hiddenNorthDoor) {
                    pixmap.fillRectangle(x + 2 * wt, yup - y, MINI_DIM - 4 * wt, wt);
                }
                if (cell.hiddenSouthDoor) {
                    pixmap.fillRectangle(x + 2 * wt, yup - y + MINI_DIM - wt, MINI_DIM - 4 * wt, wt);
                }
                if (cell.hiddenEastDoor) {
                    pixmap.fillRectangle(x + MINI_DIM - wt, yup - y + 2 * wt, wt, MINI_DIM - 4 * wt);
                }
                if (cell.hiddenWestDoor) {
                    pixmap.fillRectangle(x, yup - y + 2 * wt, wt, MINI_DIM - 4 * wt);
                }

                pixmap.setColor(Color.RED);
                if (cell.northDoor && !cell.hiddenNorthDoor) {
                    pixmap.fillRectangle(x + 2 * wt, yup - y, MINI_DIM - 4 * wt, wt);
                }
                if (cell.southDoor && !cell.hiddenSouthDoor) {
                    pixmap.fillRectangle(x + 2 * wt, yup - y + MINI_DIM - wt, MINI_DIM - 4 * wt, wt);
                }
                if (cell.eastDoor && !cell.hiddenEastDoor) {
                    pixmap.fillRectangle(x + MINI_DIM - wt, yup - y + 2 * wt, wt, MINI_DIM - 4 * wt);
                }
                if (cell.westDoor && !cell.hiddenWestDoor) {
                    pixmap.fillRectangle(x, yup - y + 2 * wt, wt, MINI_DIM - 4 * wt);
                }

                if (cell.stairs || cell.elevator) {
                    drawLadderTriangle(cell, pixmap, e, n);
                }

                if (cell.summoningCircle != null) {
                    pixmap.drawPixmap(
                            this.miniMapIconsPixmap,
                            x + 4,
                            yup - y + 4,
                            arrows[0][1].getRegionX(),
                            arrows[0][1].getRegionY(),
                            arrows[0][1].getRegionWidth(),
                            arrows[0][1].getRegionHeight()
                    );
                }

            }
        }

        miniMap = new Texture(pixmap);
        pixmap.dispose();

        int x = (Math.round(currentPos.x) - 1);
        int y = (Math.round(currentPos.z) - 1);
        MazeCell cell = this.map.scenario().levels()[this.currentLevel].cells[x][y];
        setLineOfSightLights(this.map.scenario().levels()[this.currentLevel], cell);
    }

    private void drawLadderTriangle(MazeCell cell, Pixmap pixmap, int e, int n) {
        int yup = miniMapBackgroundDimension - 4;
        int cx = 4 + e * MINI_DIM + 4;
        int cy = yup - n * MINI_DIM - MINI_DIM + 4;
        pixmap.setColor(Color.YELLOW);
        if (cell.elevator) {
            pixmap.drawPixmap(this.miniMapIconsPixmap, cx, cy, arrows[3][2].getRegionX(), arrows[3][2].getRegionY(), arrows[3][2].getRegionWidth(), arrows[3][2].getRegionHeight());
        } else if (cell.stairs) {
            if (this.map == Map.WIZARDRY4) {
                if (cell.address.level == 1 || cell.address.level >= 12) {
                    if (cell.address.level > cell.addressTo.level) {//down
                        pixmap.drawPixmap(this.miniMapIconsPixmap, cx, cy, arrows[3][1].getRegionX(), arrows[3][1].getRegionY(), arrows[3][1].getRegionWidth(), arrows[3][1].getRegionHeight());
                    } else {//up
                        pixmap.drawPixmap(this.miniMapIconsPixmap, cx, cy, arrows[3][0].getRegionX(), arrows[3][0].getRegionY(), arrows[3][0].getRegionWidth(), arrows[3][0].getRegionHeight());
                    }
                } else {
                    if (cell.address.level < cell.addressTo.level) {//down
                        pixmap.drawPixmap(this.miniMapIconsPixmap, cx, cy, arrows[3][1].getRegionX(), arrows[3][1].getRegionY(), arrows[3][1].getRegionWidth(), arrows[3][1].getRegionHeight());
                    } else {//up
                        pixmap.drawPixmap(this.miniMapIconsPixmap, cx, cy, arrows[3][0].getRegionX(), arrows[3][0].getRegionY(), arrows[3][0].getRegionWidth(), arrows[3][0].getRegionHeight());
                    }
                }
            } else {
                if (cell.address.level > cell.addressTo.level) {//up
                    pixmap.drawPixmap(this.miniMapIconsPixmap, cx, cy, arrows[3][0].getRegionX(), arrows[3][0].getRegionY(), arrows[3][0].getRegionWidth(), arrows[3][0].getRegionHeight());
                } else {//down
                    pixmap.drawPixmap(this.miniMapIconsPixmap, cx, cy, arrows[3][1].getRegionX(), arrows[3][1].getRegionY(), arrows[3][1].getRegionWidth(), arrows[3][1].getRegionHeight());
                }
            }
        }
    }

    private class MiniMapIcon extends Actor {

        TextureRegion north;
        TextureRegion south;
        TextureRegion east;
        TextureRegion west;

        public MiniMapIcon() {
            super();
            this.north = arrows[1][2];
            this.east = arrows[0][2];
            this.west = arrows[0][3];
            this.south = arrows[1][3];
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (!showMiniMap) {
                return;
            }
            TextureRegion t = north;
            if (currentDir == EAST) {
                t = east;
            }
            if (currentDir == WEST) {
                t = west;
            }
            if (currentDir == SOUTH) {
                t = south;
            }
            batch.draw(t, getX(), getY());
        }

    }

    private class MiniMapActor extends Actor {

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (!showMiniMap) {
                return;
            }
            batch.draw(miniMapBackground, xalignMM - 3, yalignMM - 3);
            batch.draw(miniMap, xalignMM, yalignMM);
        }

    }

    private void moveMiniMapIcon() {
        miniMapIcon.setX(xalignMM + (Math.round(currentPos.z) - 1) * MINI_DIM + 8);
        miniMapIcon.setY(yalignMM + (Math.round(currentPos.x)) * MINI_DIM - 16);
    }

    @Override
    public void partyDeath() {
        mainGame.setScreen(startScreen);
    }

    @Override
    public boolean keyUp(int keycode) {
        camera.up.set(Vector3.Y); // remove tilt/roll

        int x = (Math.round(currentPos.x) - 1);
        int y = (Math.round(currentPos.z) - 1);
        MazeCell cell = this.map.scenario().levels()[this.currentLevel].cells[x][y];

        float tdur = .7f;

        if (keycode == Keys.LEFT) {

            if (currentDir == EAST) {
                stage.addAction(new PanCameraAction(camera, tdur, EAST.degree(), NORTH.degree()));
                currentDir = NORTH;
            } else if (currentDir == WEST) {
                stage.addAction(new PanCameraAction(camera, tdur, WEST.degree(), -90));
                currentDir = SOUTH;
            } else if (currentDir == NORTH) {
                stage.addAction(new PanCameraAction(camera, tdur, NORTH.degree(), WEST.degree()));
                currentDir = WEST;
            } else if (currentDir == SOUTH) {
                stage.addAction(new PanCameraAction(camera, tdur, SOUTH.degree(), EAST.degree()));
                currentDir = EAST;
            }
            return false;

        } else if (keycode == Keys.RIGHT) {

            if (currentDir == EAST) {
                stage.addAction(new PanCameraAction(camera, tdur, EAST.degree(), SOUTH.degree()));
                currentDir = SOUTH;
            } else if (currentDir == WEST) {
                stage.addAction(new PanCameraAction(camera, tdur, WEST.degree(), NORTH.degree()));
                currentDir = NORTH;
            } else if (currentDir == NORTH) {
                stage.addAction(new PanCameraAction(camera, tdur, NORTH.degree(), EAST.degree()));
                currentDir = EAST;
            } else if (currentDir == SOUTH) {
                stage.addAction(new PanCameraAction(camera, tdur, SOUTH.degree(), 360));
                currentDir = WEST;
            }
            return false;

        } else if (keycode == Keys.UP) {

            boolean skipProgression = false;

            //forward
            if (currentDir == EAST) {
                y = y + 1;
                if (y > this.dim - 1) {
                    y = 0;
                    skipProgression = true;
                }
            } else if (currentDir == WEST) {
                y = y - 1;
                if (y < 0) {
                    y = this.dim - 1;
                    skipProgression = true;
                }
            } else if (currentDir == NORTH) {
                x = x + 1;
                if (x > this.dim - 1) {
                    x = 0;
                    skipProgression = true;
                }
            } else if (currentDir == SOUTH) {
                x = x - 1;
                if (x < 0) {
                    x = this.dim - 1;
                    skipProgression = true;
                }
            }

            try {
                move(cell, currentDir, x, y, skipProgression);
            } catch (Throwable e) {
                e.printStackTrace();
                partyDeath();
            }
            return false;

        } else if (keycode == Keys.DOWN) {
            boolean skipProgression = false;

            //backwards
            if (currentDir == EAST) {
                y = y - 1;
                if (y < 0) {
                    y = this.dim - 1;
                    skipProgression = true;
                }
            } else if (currentDir == WEST) {
                y = y + 1;
                if (y > this.dim - 1) {
                    y = 0;
                    skipProgression = true;
                }
            } else if (currentDir == NORTH) {
                x = x - 1;
                if (x < 0) {
                    x = this.dim - 1;
                    skipProgression = true;
                }
            } else if (currentDir == SOUTH) {
                x = x + 1;
                if (x > this.dim - 1) {
                    x = 0;
                    skipProgression = true;
                }
            }

            try {
                move(cell, Direction.reverse(currentDir), x, y, skipProgression);
            } catch (Throwable e) {
                e.printStackTrace();
                partyDeath();
            }
            return false;

        } else if (keycode == Keys.F1) {//CHEAT for DEV
            currentLevel--;
            if (currentLevel < 0) {
                currentLevel = 0;
                Andius.mainGame.setScreen(Map.WORLD.getScreen());
            } else {
                createMiniMap();
            }
            return false;
        } else if (keycode == Keys.F2) {//CHEAT for DEV
            currentLevel++;
            if (currentLevel >= this.map.scenario().levels().length) {
                currentLevel = this.map.scenario().levels().length - 1;
            } else {
                createMiniMap();
            }
            return false;
        } else if (keycode == Keys.K) {
            if (cell.stairs) {
                teleport(cell.addressTo, false);
            }
            return false;
        } else if (keycode == Keys.NUM_1 || keycode == Keys.PAGE_DOWN) {
            if (cell.elevator) {//up
                if (currentLevel == 0) {
                    Andius.mainGame.setScreen(Map.WORLD.getScreen());
                } else if (currentLevel + 1 - 1 >= cell.elevatorFrom && currentLevel + 1 - 1 <= cell.elevatorTo) {
                    currentLevel--;
                    createMiniMap();
                }
            }
            return false;
        } else if (keycode == Keys.NUM_2 || keycode == Keys.PAGE_UP) {
            if (cell.elevator) {//down
                if (currentLevel + 1 + 1 >= cell.elevatorFrom && currentLevel + 1 + 1 <= cell.elevatorTo) {
                    currentLevel++;
                    createMiniMap();
                }
            }
            return false;
        } else if (keycode == Keys.I) {
            if (!this.map.scenario().levels()[currentLevel].cells[x][y].darkness) {
                isTorchOn = !isTorchOn;
            } else {
                log("The torch fails to light and darkness remains!");
            }
        } else if (keycode == Keys.ESCAPE) {

            Utils.animateText(stage, Andius.skin, "default-16", Constants.HELP_KEYS, Color.YELLOW, 20, 100, 20, 200);

        } else if (keycode == Keys.P) {

            showMiniMap = !showMiniMap;

        } else if (keycode == Keys.S) {

            if (cell.summoningCircle != null) {
                SummoningCircleScreen ssc = new SummoningCircleScreen(CTX.saveGame.players[0], cell.summoningCircle);
                mainGame.setScreen(ssc);
            }

        } else if (keycode == Keys.SPACE) {

            log("Pass");

            try {
                pass(x, y);
            } catch (Throwable e) {
                e.printStackTrace();
                partyDeath();
            }

        }

        try {
            finishTurn(x, y);
        } catch (Throwable e) {
            e.printStackTrace();
            partyDeath();
        }

        return false;
    }

    private void move(MazeCell currentCell, Direction dir, int dx, int dy, boolean skipProgression) {

        boolean canMove = canMove(currentCell, dir);

        if (!canMove) {
            return;
        }

        WizardryData.MazeLevel[] levels = this.map.scenario().levels();
        MazeCell destinationCell = levels[currentLevel].cells[dx][dy];

        setLineOfSightLights(levels[currentLevel], destinationCell);

        if (destinationCell.teleport) {
            MazeAddress to = destinationCell.addressTo;
            teleport(to, true);
            return;
        }

        if (destinationCell.chute) {
            MazeAddress to = destinationCell.addressTo;
            teleport(to, true);
            return;
        }

        if (destinationCell.damage != null) {
            if (CTX.partyHasItem(5, 4) == null) {//winged boots
                CTX.damageGroup(destinationCell.damage);
                log(PIT_DAMAGE_MSGS[Utils.RANDOM.nextInt(PIT_DAMAGE_MSGS.length)]);
                Sounds.play(Sound.PC_STRUCK);
                if (this.map == Map.WIZARDRY4 && (this.currentLevel == 12 || this.currentLevel == 13)) {
                    if (dx == 9 && dy == 8 && this.currentLevel == 13) {
                        //on ledge
                    } else {
                        teleport(new MazeAddress(0, dx, dy), false);
                    }
                }
            }
        }

        if (destinationCell.itemRequired > 0) {
            Item item = this.map.scenario().items().get(destinationCell.itemRequired);
            CharacterRecord owner = Andius.CTX.getOwner(item);
            if (owner == null) {
                Andius.HUD.log(destinationCell.message.getText(), Color.YELLOW);
                Sounds.play(Sound.NEGATIVE_EFFECT);
                if (destinationCell.addressTo != null) {
                    MazeAddress to = destinationCell.addressTo;
                    teleport(to, true);
                }
                return;
            }
        } else {
            if (destinationCell.message != null && destinationCell.addressTo != null) {
                Andius.HUD.log(destinationCell.message.getText(), Color.YELLOW);
                MazeAddress to = destinationCell.addressTo;
                teleport(to, true);
                return;
            }
        }

        if (destinationCell.riddleAnswers != null && destinationCell.function == null && !destinationCell.riddleAnswers.isEmpty()) {
            new RiddleDialog(CTX, this, destinationCell).show(this.stage);
            return;
        }

        currentPos.x = dx + .5f;
        currentPos.z = dy + .5f;

        if (skipProgression) {
            this.camera.position.set(currentPos.x, .5f, currentPos.z);
        }

        stage.addAction(new MoveCameraAction(camera, .5f, dx + .5f, dy + .5f));

        switch (dir) {
            case EAST:
                if (dir == currentDir) {
                    camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
                }
                break;
            case WEST:
                if (dir == currentDir) {
                    camera.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
                }
                break;
            case NORTH:
                if (dir == currentDir) {
                    camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
                }
                break;
            case SOUTH:
                if (dir == currentDir) {
                    camera.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
                }
                break;
        }

        moveMiniMapIcon();

        boolean showMessage = true;

        if (destinationCell.tradeItem1 > 0) {
            Item item1 = this.map.scenario().items().get(destinationCell.tradeItem1);
            Item item2 = this.map.scenario().items().get(destinationCell.tradeItem2);
            CharacterRecord owner = Andius.CTX.getOwner(item1);
            if (owner != null) {
                Andius.HUD.log(destinationCell.message.getText(), Color.GREEN);
                log(String.format("%s traded %s for %s", owner.name, item1.genericName, item2.genericName));
                owner.inventory.remove(item1);
                owner.inventory.add(item2);
                Sounds.play(Sound.POSITIVE_EFFECT);
            } else {
                Sounds.play(Sound.NEGATIVE_EFFECT);
            }
            showMessage = false;
        }

        if (destinationCell.itemObtained > 0) {
            Item item = this.map.scenario().items().get(destinationCell.itemObtained);
            CharacterRecord owner = Andius.CTX.getOwner(item);
            if (owner == null) {
                Andius.HUD.log(destinationCell.message.getText(), Color.GREEN);
                CharacterRecord cr = Andius.CTX.pickRandomEnabledPlayer();
                log(String.format("%s found a %s", cr.name, item.genericName));
                cr.inventory.add(item);
                Sounds.play(Sound.POSITIVE_EFFECT);
            }
            showMessage = false;
        }

        if (destinationCell.riddleAnswers != null && destinationCell.riddleAnswers.isEmpty()) {
            showMessage = false;
        }

        if (destinationCell.itemRequired > 0 && Andius.CTX.getOwner(this.map.scenario().items().get(destinationCell.itemRequired)) != null) {
            showMessage = false;
        }

        if (destinationCell.darkness) {
            isTorchOn = false;
        }

        if (showMessage && destinationCell.message != null) {
            Andius.HUD.log(destinationCell.message.getText(), Color.GREEN);
        } else if (destinationCell.summoningCircle != null) {
            Andius.HUD.log(getMessage(WER_MESSAGES, 90).getText(), Color.GREEN);
        }

        if (destinationCell.function != null) {
            destinationCell.function.getDialog(CTX, this).show(this.stage);
        }

        if (destinationCell.chestType > 0) {
            mainGame.setScreen(new RewardScreen(Andius.CTX, this.map, Utils.RANDOM.nextInt(10, 20)));
            destinationCell.chestType = -1;
            addBlock(destinationCell.address.level - 1, destinationCell, destinationCell.address.row, destinationCell.address.column, true);
        }

        if (destinationCell.rotateDirection != -1) {
            rotateBlock(currentLevel, destinationCell, dx, dy);
            createMiniMap();
        }

        fight(destinationCell, currentCell, this.map.scenario().levels()[currentLevel].defeated);

        finishTurn(dx, dy);

    }

    private boolean canMove(MazeCell c, Direction dir) {
        switch (dir) {
            case NORTH:
                return c.hiddenNorthDoor || c.northDoor || !c.northWall;
            case SOUTH:
                return c.hiddenSouthDoor || c.southDoor || !c.southWall;
            case WEST:
                return c.hiddenWestDoor || c.westDoor || !c.westWall;
            case EAST:
                return c.hiddenEastDoor || c.eastDoor || !c.eastWall;
            default:
                return false;
        }
    }

    private void pass(int x, int y) {

        WizardryData.MazeLevel[] levels = this.map.scenario().levels();
        MazeCell currentCell = levels[currentLevel].cells[x][y];

        if (currentCell.teleport) {
            MazeAddress to = currentCell.addressTo;
            teleport(to, true);
            return;
        }

        if (currentCell.chute) {
            MazeAddress to = currentCell.addressTo;
            teleport(to, true);
            return;
        }

        if (currentCell.itemRequired > 0) {
            Item item = this.map.scenario().items().get(currentCell.itemRequired);
            CharacterRecord owner = Andius.CTX.getOwner(item);
            if (owner == null) {
                Andius.HUD.log(currentCell.message.getText(), Color.YELLOW);
                Sounds.play(Sound.NEGATIVE_EFFECT);
                if (currentCell.addressTo != null) {
                    MazeAddress to = currentCell.addressTo;
                    teleport(to, true);
                }
                return;
            }
        } else {
            if (currentCell.message != null && currentCell.addressTo != null) {
                Andius.HUD.log(currentCell.message.getText(), Color.YELLOW);
                MazeAddress to = currentCell.addressTo;
                teleport(to, true);
                return;
            }
        }

        if (currentCell.riddleAnswers != null && currentCell.function == null && !currentCell.riddleAnswers.isEmpty()) {
            new RiddleDialog(CTX, this, currentCell).show(this.stage);
            return;
        }

        boolean showMessage = true;

        if (currentCell.tradeItem1 > 0) {
            Item item1 = this.map.scenario().items().get(currentCell.tradeItem1);
            Item item2 = this.map.scenario().items().get(currentCell.tradeItem2);
            CharacterRecord owner = Andius.CTX.getOwner(item1);
            if (owner != null) {
                Andius.HUD.log(currentCell.message.getText(), Color.GREEN);
                Andius.HUD.log(String.format("%s traded %s for %s", owner.name, item1.genericName, item2.genericName));
                owner.inventory.remove(item1);
                owner.inventory.add(item2);
                Sounds.play(Sound.POSITIVE_EFFECT);
            } else {
                Sounds.play(Sound.NEGATIVE_EFFECT);
            }
            showMessage = false;
        }

        if (currentCell.itemObtained > 0) {
            Item item = this.map.scenario().items().get(currentCell.itemObtained);
            CharacterRecord owner = Andius.CTX.getOwner(item);
            if (owner == null) {
                Andius.HUD.log(currentCell.message.getText(), Color.GREEN);
                CharacterRecord cr = Andius.CTX.pickRandomEnabledPlayer();
                Andius.HUD.log(String.format("%s found a %s", cr.name, item.genericName));
                cr.inventory.add(item);
                Sounds.play(Sound.POSITIVE_EFFECT);
            }
            showMessage = false;
        }

        if (currentCell.riddleAnswers != null && currentCell.riddleAnswers.isEmpty()) {
            showMessage = false;
        }

        if (currentCell.itemRequired > 0 && Andius.CTX.getOwner(this.map.scenario().items().get(currentCell.itemRequired)) != null) {
            showMessage = false;
        }

        if (currentCell.darkness) {
            isTorchOn = false;
        }

        if (showMessage && currentCell.message != null) {
            Andius.HUD.log(currentCell.message.getText(), Color.GREEN);
        }

        if (currentCell.function != null) {
            currentCell.function.getDialog(CTX, this).show(this.stage);
        }

        if (currentCell.rotateDirection != -1) {
            rotateBlock(currentLevel, currentCell, x, y);
            createMiniMap();
        }

        fight(currentCell, currentCell, this.map.scenario().levels()[currentLevel].defeated);

        finishTurn(x, y);

    }

    private void fight(MazeCell destCell, MazeCell fromCell, List<Integer> defeated) {
        if (this.map == Map.WIZARDRY4) {

            if ((destCell.encounterID > 0 && !defeated.contains(destCell.encounterID))) {

                if (!destCell.fightIfDoNotOwnAnyOfItems.isEmpty() && CTX.partyHasAnyOfTheseItems(destCell.fightIfDoNotOwnAnyOfItems, 4) != null) {
                    //no fight
                    if (destCell.encounterGiveItem > 0) {
                        Item give = this.map.scenario().items().get(destCell.encounterGiveItem);
                        Andius.HUD.log(destCell.encounterTradeSuccessMessage.getText(), Color.YELLOW);
                        Andius.HUD.log(String.format("%s obtained a %s", CTX.saveGame.players[0].name, give.genericName));
                        CTX.saveGame.players[0].inventory.add(give);
                        destCell.message = null;
                        destCell.encounterID = -1;
                        Sounds.play(Sound.POSITIVE_EFFECT);
                    }
                } else if (destCell.encounterGiveItem > 0 && CTX.partyHasItem(destCell.encounterGiveItem, 4) != null) {
                    //no fight
                } else if (destCell.encounterTakeItem > 0 && CTX.partyHasItem(destCell.encounterTakeItem, 4) != null) {
                    //no fight
                    if (destCell.encounterGiveItem > 0) {
                        Item take = this.map.scenario().items().get(destCell.encounterTakeItem);
                        Item give = this.map.scenario().items().get(destCell.encounterGiveItem);
                        Andius.HUD.log(destCell.encounterTradeSuccessMessage.getText(), Color.YELLOW);
                        Andius.HUD.log(String.format("%s traded %s for %s", CTX.saveGame.players[0].name, take.genericName, give.genericName));
                        CTX.saveGame.players[0].inventory.remove(take);
                        CTX.saveGame.players[0].inventory.add(give);
                        destCell.message = null;
                        destCell.encounterID = -1;
                        Sounds.play(Sound.POSITIVE_EFFECT);
                    }
                } else {
                    DoGooder dogooder = this.map.scenario().characters().get(destCell.encounterID);
                    Wiz4CombatScreen cs = new Wiz4CombatScreen(CTX.saveGame.players[0], CTX.saveGame.players[0].summonedMonsters, dogooder, destCell, fromCell);
                    mainGame.setScreen(cs);
                }
            } else {
                if (Utils.percentChance(25)) {
                    int maxTries = 10;
                    for (int tries = 0; tries < maxTries; tries++) {
                        int id = this.map.scenario().levels()[currentLevel].getRandomMonster();
                        if (id <= 0) {
                            break;
                        }
                        if (!defeated.contains(id)) {
                            DoGooder d = this.map.scenario().characters().get(id);
                            mainGame.setScreen(new Wiz4CombatScreen(CTX.saveGame.players[0], CTX.saveGame.players[0].summonedMonsters, d, destCell, fromCell));
                            break;
                        }
                    }
                }
            }

        } else {
            boolean treasure = destCell.hasTreasureChest;
            boolean wandering = destCell.wanderingEncounterID != -1 && Utils.percentChance(33);
            if (destCell.encounterID != -1 || wandering) {
                int encounterID = destCell.encounterID != -1 ? destCell.encounterID : destCell.wanderingEncounterID;
                Monster monster = this.map.scenario().monsters().get(encounterID);
                mainGame.setScreen(new WizardryCombatScreen(CTX, this.map, monster.name, monster, currentLevel + 1, treasure, destCell, fromCell));
            }
        }
    }

    @Override
    public void teleport(int level, int x, int y) {
        if (x >= this.dim) {
            x = this.dim - x;
        }
        if (x < 0) {
            x = this.dim + x;
        }
        if (y >= this.dim) {
            y = this.dim - y;
        }
        if (y < 0) {
            y = this.dim + y;
        }
        if (x >= this.dim) {
            x = this.dim - 1;
        }
        if (y >= this.dim) {
            y = this.dim - 1;
        }
        if (level < 0) {
            level = 0;
        }
        if (this.map == Map.WIZARDRY4) {
            //Malor works differently in W4, where you are allowed to malor depends on how far you have progressed in the game.
            boolean failed = false;
            if (level == 9 && CTX.saveGame.players[0].level < 2) {
                failed = true;
            } else if (level == 8 && CTX.saveGame.players[0].level < 3) {
                failed = true;
            } else if (level == 7 && CTX.saveGame.players[0].level < 4) {
                failed = true;
            } else if (level == 6 && CTX.saveGame.players[0].level < 5) {
                failed = true;
            } else if (level == 5 && CTX.saveGame.players[0].level < 6) {
                failed = true;
            } else if (level == 4 && CTX.saveGame.players[0].level < 7) {
                failed = true;
            } else if (level == 3 && CTX.saveGame.players[0].level < 8) {
                failed = true;
            } else if (level == 2 && CTX.saveGame.players[0].level < 9) {
                failed = true;
            } else if (level == 1 && CTX.saveGame.players[0].level < 10) {
                failed = true;
            } else if ((level == 0 || level >= 11) && !CTX.saveGame.riddles.get(Map.WIZARDRY4).contains(new AnsweredRiddle(0, 1, 9))) {
                failed = true;//trebur sux riddle must be answered before malor can be used to the castle levels and beyond
            }
            if (failed) {
                Sounds.play(Sound.FLEE);
                return;
            }
        }
        teleport(new MazeAddress(level, x, y), true);
    }

    public void teleport(MazeAddress addr, boolean sound) {

        MazeAddress to;

        if (addr.level <= 0) {
            setMapPixelCoords(null, this.map.scenario().getStartX(), this.map.scenario().getStartY(), this.map.scenario().getStartLevel());
            Andius.mainGame.setScreen(Map.WORLD.getScreen());
            return;
        } else {
            if (this.map == Map.WIZARDRY4 && addr.level == 12) {//root of the world level 12
                if (currentLevel == 10 && CTX.partyHasItem(14, 4) != null) {//need void transducer
                    to = new MazeAddress(12, 0, 10);
                } else if (currentLevel == 11) {
                    //ok if already on 12
                    to = addr;
                } else {
                    Sounds.play(Sound.FLEE);
                    return;
                }
            } else {
                to = addr;
            }
        }

        int dx = to.row;
        int dy = to.column;

        if (to.level == currentLevel + 1) {//same level
            currentPos.x = dx + .5f;
            currentPos.z = dy + .5f;
            stage.addAction(new MoveCameraAction(camera, .5f, dx + .5f, dy + .5f));
        } else {//different level
            currentLevel = to.level - 1;
            if (currentLevel >= this.map.scenario().levels().length) {
                currentLevel = this.map.scenario().levels().length - 1;
            }
            if (currentLevel < 0) {
                currentLevel = 0;
            }
            currentPos.x = dx + .5f;
            currentPos.z = dy + .5f;
        }

        camera.position.set(currentPos.x, .5f, currentPos.z);
        camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);

        moveMiniMapIcon();
        createMiniMap();

        if (sound) {
            Sounds.play(Sound.WAVE);
        }
    }

    @Override
    public void endCombat(boolean isWon, Object opponent) {
        if (isWon) {
            int x = (Math.round(currentPos.x) - 1);
            int y = (Math.round(currentPos.z) - 1);

            this.map.scenario().levels()[currentLevel].cells[x][y].wanderingEncounterID = -1;

            if (this.map.scenario().levels()[currentLevel].cells[x][y].encounterID >= 0) {
                this.map.scenario().levels()[currentLevel].cells[x][y].encounterOccurences -= 1;
                if (this.map.scenario().levels()[currentLevel].cells[x][y].encounterOccurences <= 0) {
                    this.map.scenario().levels()[currentLevel].cells[x][y].encounterID = -1;
                }
                if (this.map == Map.WIZARDRY4) {
                    if (currentLevel == 6 && x == 2 && y == 13) {
                        teleport(new MazeAddress(7, 4, 17), true);
                    }
                    if (currentLevel == 6 && x == 2 && y == 8) {
                        teleport(new MazeAddress(7, 6, 2), true);
                    }
                }
            } else {
                if (this.map == Map.WIZARDRY4) {
                    DoGooder dg = (DoGooder) opponent;
                    for (int id : dg.partyMembers) {
                        if (!this.map.scenario().levels()[currentLevel].defeated.contains(id)) {
                            //this is not persisted to save file to be consistent with the real game
                            this.map.scenario().levels()[currentLevel].defeated.add(id);
                        }
                    }
                }
            }

        }
    }

    @Override
    public void finishTurn(int currentX, int currentY) {
        CTX.endTurn(this.map);
    }

    public static class DungeonTileModelInstance extends ModelInstance {

        private final int level;
        private final float cx;
        private final float cy;
        private boolean rotates;

        public DungeonTileModelInstance(Model model, int level, float cx, float cy) {
            super(model);
            this.level = level;
            this.cx = cx;
            this.cy = cy;
        }

        public DungeonTileModelInstance(Model model, int level, float cx, float cy, float x, float y, float z, boolean rotates) {
            this(model, level, cx, cy, x, y, z);
            this.rotates = rotates;
        }

        public DungeonTileModelInstance(Model model, int level, float cx, float cy, float x, float y, float z) {
            super(model);
            this.level = level;
            this.cx = cx;
            this.cy = cy;
            this.transform.setToTranslation(x, y, z);
        }

        public int getLevel() {
            return level;
        }

        public float getCx() {
            return cx;
        }

        public float getCy() {
            return cy;
        }

        public void render(ModelBatch batch, Environment env) {
            if (this.rotates) {
                this.transform.rotate(Vector3.Y, 45f * Gdx.graphics.getDeltaTime());
            }
            batch.render(this, env);
        }

    }

    private static class SpotLightInfo {

        final int level;
        final int cx, cy;
        final SpotLight light;

        SpotLightInfo(int level, int cx, int cy, SpotLight light) {
            this.level = level;
            this.cx = cx;
            this.cy = cy;
            this.light = light;
        }
    }

    private void setLineOfSightLights(MazeLevel lvl, MazeCell src) {

        SpotLightsAttribute sLights = ((SpotLightsAttribute) environment.get(SpotLightsAttribute.Type));
        if (sLights != null) {
            sLights.lights.clear();
        }

        environment.add(this.torch);

        for (SpotLightInfo info : this.spotLights) {
            if (info.level == currentLevel) {
                if (lvl.hasLineOfSight(src, info.cx, info.cy)) {
                    environment.add(info.light);
                } else {
                    environment.remove(info.light);
                }
            }
        }
    }

    private void pruneWrappedInstances() {

        int max = this.dim + 5;
        int min = -5;

        floor.removeIf(mi -> mi.transform.val[Matrix4.M03] > max
                || mi.transform.val[Matrix4.M23] < min
                || mi.transform.val[Matrix4.M03] < min
                || mi.transform.val[Matrix4.M23] > max);

        ceiling.removeIf(mi -> mi.transform.val[Matrix4.M03] > max
                || mi.transform.val[Matrix4.M23] < min
                || mi.transform.val[Matrix4.M03] < min
                || mi.transform.val[Matrix4.M23] > max);

        modelInstances.removeIf(mi -> mi.transform.val[Matrix4.M03] > max
                || mi.transform.val[Matrix4.M23] < min
                || mi.transform.val[Matrix4.M03] < min
                || mi.transform.val[Matrix4.M23] > max);

        MazeLevel[] levels = this.map.scenario().levels();

        java.util.Map<DungeonTileModelInstance, Boolean> visible = new HashMap<>();

        List<DungeonTileModelInstance>[] northByColumn = (List<DungeonTileModelInstance>[]) new ArrayList[this.dim];
        List<DungeonTileModelInstance>[] southByColumn = (List<DungeonTileModelInstance>[]) new ArrayList[this.dim];
        List<DungeonTileModelInstance>[] westByRow = (List<DungeonTileModelInstance>[]) new ArrayList[this.dim];
        List<DungeonTileModelInstance>[] eastByRow = (List<DungeonTileModelInstance>[]) new ArrayList[this.dim];

        for (int i = 0; i < this.dim; i++) {
            northByColumn[i] = new ArrayList<>();
            southByColumn[i] = new ArrayList<>();
            westByRow[i] = new ArrayList<>();
            eastByRow[i] = new ArrayList<>();
        }

        for (DungeonTileModelInstance mi : modelInstances) {
            int cx = Math.round(mi.getCx());
            int cy = Math.round(mi.getCy());

            if (cx < 0 || cx >= this.dim || cy < 0 || cy >= this.dim) {
                visible.put(mi, Boolean.TRUE);

                int row = Math.floorMod(cx, this.dim);
                int col = Math.floorMod(cy, this.dim);

                if (cx < 0) {
                    southByColumn[col].add(mi);
                } else if (cx >= this.dim) {
                    northByColumn[col].add(mi);
                }

                if (cy < 0) {
                    westByRow[row].add(mi);
                } else if (cy >= this.dim) {
                    eastByRow[row].add(mi);
                }
            }
        }

        for (int levelIndex = 0; levelIndex < levels.length; levelIndex++) {
            MazeLevel lvl = levels[levelIndex];

            int northRow = this.dim - 1;
            for (int col = 0; col < this.dim; col++) {
                MazeCell cell = lvl.cells[northRow][col];
                if (cell != null && (cell.northWall || cell.northDoor || cell.hiddenNorthDoor || cell.rock)) {
                    for (DungeonTileModelInstance mi : northByColumn[col]) {
                        if (mi.getLevel() == levelIndex) {
                            visible.put(mi, Boolean.FALSE);
                        }
                    }
                }
            }

            int southRow = 0;
            for (int col = 0; col < this.dim; col++) {
                MazeCell cell = lvl.cells[southRow][col];
                if (cell != null && (cell.southWall || cell.southDoor || cell.hiddenSouthDoor || cell.rock)) {
                    for (DungeonTileModelInstance mi : southByColumn[col]) {
                        if (mi.getLevel() == levelIndex) {
                            visible.put(mi, Boolean.FALSE);
                        }
                    }
                }
            }

            int westCol = 0;
            for (int row = 0; row < this.dim; row++) {
                MazeCell cell = lvl.cells[row][westCol];
                if (cell != null && (cell.westWall || cell.westDoor || cell.hiddenWestDoor || cell.rock)) {
                    for (DungeonTileModelInstance mi : westByRow[row]) {
                        if (mi.getLevel() == levelIndex) {
                            visible.put(mi, Boolean.FALSE);
                        }
                    }
                }
            }

            int eastCol = this.dim - 1;
            for (int row = 0; row < this.dim; row++) {
                MazeCell cell = lvl.cells[row][eastCol];
                if (cell != null && (cell.eastWall || cell.eastDoor || cell.hiddenEastDoor || cell.rock)) {
                    for (DungeonTileModelInstance mi : eastByRow[row]) {
                        if (mi.getLevel() == levelIndex) {
                            visible.put(mi, Boolean.FALSE);
                        }
                    }
                }
            }
        }

        modelInstances.removeIf(mi -> {
            Boolean v = visible.get(mi);
            return v != null && !v;
        });
    }

}
