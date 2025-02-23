package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
import andius.objects.Direction;
import andius.dialogs.RiddleDialog;
import static andius.Andius.CTX;
import static andius.Andius.mainGame;
import static andius.Andius.startScreen;
import static andius.Constants.CLASSPTH_RSLVR;
import static andius.WizardryData.DUNGEON_DIM;
import andius.WizardryData.MazeAddress;
import andius.WizardryData.MazeCell;
import static andius.WizardryData.WER_LEVEL_DESC;
import static andius.WizardryData.WER_MESSAGES;
import static andius.WizardryData.getMessage;
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
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.UBJsonReader;
import java.util.Iterator;
import utils.Utils;

public class WizardryDungeonScreen extends BaseScreen {

    private static final int MAP_WIDTH = 672;
    private static final int MAP_HEIGHT = 672;

    private final ModelBuilder builder = new ModelBuilder();
    private final ModelBatch modelBatch;
    private final SpriteBatch batch;

    private CameraInputController inputController;
    private final AssetManager assets;

    private Model ladderModel;
    private Model doorModel, pentagram;
    private Model wall, manhole, letterM;

    private final Environment environment = new Environment();
    private final Environment outside = new Environment();

    private final Color darkness = Color.DARK_GRAY;
    private final Color flame = new Color(0xf59414ff);

    private PointLight torch;
    boolean isTorchOn = false;
    private DirectionalLight directionalLightDown;
    private DirectionalLight directionalLightUp;

    private final List<DungeonTileModelInstance> modelInstances = new ArrayList<>();
    private final List<DungeonTileModelInstance> floor = new ArrayList<>();
    private final List<DungeonTileModelInstance> ceiling = new ArrayList<>();

    private final List<DungeonTileModelInstance> wiz4CastleLevel0ModelInstances = new ArrayList<>();
    private final List<DungeonTileModelInstance> wiz4CastleLevel12ModelInstances = new ArrayList<>();
    private final List<DungeonTileModelInstance> wiz4CastleLevel13ModelInstances = new ArrayList<>();

    private final TextureRegion[][] arrows = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/arrows.png")), 15, 15);

    public int currentLevel = 0;
    private final Vector3 currentPos = new Vector3();
    private Direction currentDir = Direction.EAST;
    private boolean loadedMazeData;

    private boolean showMiniMap = true;
    private Texture miniMap;
    private static Texture MINI_MAP_BACKGROUND;
    private final MiniMapIcon miniMapIcon;
    private final Pixmap miniMapIconsPixmap;
    private static final int MINI_DIM = 15;
    private static final int MM_BKGRND_DIM = MINI_DIM * DUNGEON_DIM + MINI_DIM / 2;
    private static final int XALIGNMM = 705;
    private static final int YALIGNMM = 348;

    public final Constants.Map map;

    public WizardryDungeonScreen(Constants.Map map) {
        this.map = map;
        this.stage = new Stage();
        //this.stage.setDebugAll(true);
        this.assets = new AssetManager(CLASSPTH_RSLVR);

        MINI_MAP_BACKGROUND = Utils.fillRectangle(MM_BKGRND_DIM, MM_BKGRND_DIM, Color.LIGHT_GRAY, 1);

        arrows[3][2].getTexture().getTextureData().prepare();
        this.miniMapIconsPixmap = arrows[3][2].getTexture().getTextureData().consumePixmap();

        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.05f, 0.05f, 0.05f, 1f));
        this.torch = new PointLight();
        this.environment.add(this.torch);

        this.outside.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.6f, 0.6f, 0.6f, 1.f));
        this.directionalLightDown = new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.5f);
        this.directionalLightUp = new DirectionalLight().set(0.8f, 0.8f, 0.8f, 1f, 0.8f, 0.5f);
        this.outside.add(this.directionalLightDown);
        this.outside.add(this.directionalLightUp);

        this.modelBatch = new ModelBatch();
        this.batch = new SpriteBatch();

        this.camera = new PerspectiveCamera(67f, MAP_WIDTH, MAP_HEIGHT);
        this.camera.near = 0.1f;
        this.camera.far = 10f;

        init();

        this.miniMapIcon = new MiniMapIcon();
        this.miniMapIcon.setOrigin(4, 4);

        stage.addActor(miniMapIcon);
        Andius.HUD.addActor(stage);

        addButtons(this.map);

        setMapPixelCoords(null, this.map.scenario().getStartX(), this.map.scenario().getStartY(), this.map.scenario().getStartLevel());

        camera.position.set(currentPos);
        camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
//        currentLevel = 2;
//        camera.position.set(10, 2, 10);
//        camera.lookAt(10, 0, 10);
//        this.isTorchOn = true;
//        this.showMiniMap = false;
//        inputController = new CameraInputController(camera);
//        inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
//        inputController.translateUnits = 10f;
//        Gdx.input.setInputProcessor(inputController);
//        createAxes();
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
        CTX.saveGame.wx = 75;//TODO
        CTX.saveGame.wy = 95;//TODO
        CTX.saveGame.x = (Math.round(currentPos.x) - 1);
        CTX.saveGame.y = (Math.round(currentPos.z) - 1);
        CTX.saveGame.level = currentLevel + 1;
        CTX.saveGame.direction = this.currentDir;

        for (int level = 0; level < this.map.scenario().levels().length; level++) {
            for (int x = 0; x < DUNGEON_DIM; x++) {
                for (int y = 0; y < DUNGEON_DIM; y++) {
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

        camera.position.set(currentPos);

        if (currentDir == Direction.EAST) {
            camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
        } else if (currentDir == Direction.WEST) {
            camera.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
        } else if (currentDir == Direction.NORTH) {
            camera.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
        } else if (currentDir == Direction.SOUTH) {
            camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
        }

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

        List<String> removedMonsters = saveGame.removedActors.get(this.map);
        if (removedMonsters == null) {
            removedMonsters = new ArrayList<>();
            saveGame.removedActors.put(this.map, removedMonsters);
        }

        for (int level = 0; level < this.map.scenario().levels().length; level++) {
            for (int x = 0; x < DUNGEON_DIM; x++) {
                for (int y = 0; y < DUNGEON_DIM; y++) {
                    MazeCell cell = this.map.scenario().levels()[level].cells[x][y];
                    if (removedMonsters.contains(level + ":M:" + x + ":" + y)) {
                        cell.wanderingEncounterID = -1;
                    }
                }
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
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
        loadMazeData(CTX.saveGame);
        createMiniMap();
        moveMiniMapIcon();
    }

    @Override
    public void hide() {
    }

    private void init() {

        assets.load("assets/graphics/dirt.png", Texture.class);
        assets.load("assets/graphics/mortar.png", Texture.class);
        assets.load("assets/graphics/rock.png", Texture.class);
        assets.load("assets/graphics/grass.png", Texture.class);
        assets.load("assets/graphics/wood-door-texture.png", Texture.class);
        assets.update(2000);

        Material mortar = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/mortar.png", Texture.class)));
        Material mortar2 = new Material(TextureAttribute.createDiffuse(Utils.reverse(assets.get("assets/graphics/mortar.png", Texture.class))));
        Material wood = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/wood-door-texture.png", Texture.class)));
        Material dirt = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/dirt.png", Texture.class)));
        Material rock = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/rock.png", Texture.class)));
        Material grazz = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/grass.png", Texture.class)));
        Material gr = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        Material bl = new Material(ColorAttribute.createDiffuse(Color.BLUE));
        Material yl = new Material(ColorAttribute.createDiffuse(Color.YELLOW));
        Material red = new Material(ColorAttribute.createDiffuse(Color.RED));

        //export from blender to fbx format, then convert fbx to the g3db like below
        //fbx-conv.exe -o G3DB ./Chess/pawn.fbx ./pawn.g3db
        ModelLoader gloader = new G3dModelLoader(new UBJsonReader());
        ladderModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/ladder.g3db"));
        ladderModel.nodes.get(0).scale.set(.260f, .260f, .260f);
        ladderModel.nodes.get(0).translation.set(.10f, .93f, .06f);
        doorModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/door.g3db"));
        doorModel.nodes.get(0).scale.set(.2f, .2f, .2f);
        doorModel.nodes.get(0).translation.set(.06f, -.5f, .015f);
        doorModel.nodes.get(0).parts.first().material = wood;
        pentagram = gloader.loadModel(Gdx.files.classpath("assets/graphics/pentagram.g3db"));
        pentagram.nodes.get(0).scale.set(.2f, .2f, .2f);
        pentagram.nodes.get(0).rotation.set(0, 0, 0, 1);
        pentagram.nodes.get(0).translation.set(0, -.17f, 0);
        pentagram.nodes.get(0).parts.first().material = red;
        letterM = gloader.loadModel(Gdx.files.classpath("assets/graphics/letter-m.g3db"));
        letterM.nodes.get(0).scale.set(.3f, .3f, .3f);
        letterM.nodes.get(0).translation.set(0, 0, 0);
        letterM.nodes.get(0).parts.first().material = gr;

        wall = builder.createBox(1.090f, 1, 0.05f, mortar, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        Model walldoor = builder.createBox(1.090f, 1, 0.05f, mortar2, Usage.Position | Usage.Normal | Usage.TextureCoordinates);

        manhole = builder.createCylinder(.5f, .02f, .5f, 32, new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)), Usage.Position | Usage.Normal);

        builder.begin();
        builder.node("door-wall", walldoor);
        builder.node("door-main", doorModel);
        doorModel = builder.end();

        Model floorModel = builder.createBox(1.1f, 0.1f, 1.1f, rock, Usage.Position | Usage.TextureCoordinates | Usage.Normal);
        Model grassModel = builder.createBox(1.1f, 0.1f, 1.1f, grazz, Usage.Position | Usage.TextureCoordinates | Usage.Normal);
        Model ceilingModel = builder.createBox(1.1f, 0.1f, 1.1f, dirt, Usage.Position | Usage.TextureCoordinates | Usage.Normal);

        Model sky = gloader.loadModel(Gdx.files.classpath("assets/graphics/skydome.g3db"), (String fileName) -> Utils.fillRectangle(5, 5, Color.SKY, 1));
        sky.nodes.get(0).scale.set(.14f, .14f, .14f);
        sky.nodes.get(0).translation.set(10, -3, 10);

        for (int x = -DUNGEON_DIM * 2; x < DUNGEON_DIM * 2; x++) {
            for (int y = -DUNGEON_DIM * 2; y < DUNGEON_DIM * 2; y++) {
                floor.add(new DungeonTileModelInstance(floorModel, 0, 0f, 0f, x - 1.5f, -.05f, y - 1.5f));
            }
        }
        for (int x = -DUNGEON_DIM * 2; x < DUNGEON_DIM * 2; x++) {
            for (int y = -DUNGEON_DIM * 2; y < DUNGEON_DIM * 2; y++) {
                ceiling.add(new DungeonTileModelInstance(ceilingModel, 0, 0f, 0f, x - 1.5f, 1.05f, y - 1.5f));
            }
        }

        if (this.map == Map.WIZARDRY4) {
            TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
            TiledMap wiz4CastleFloorCeilingMap = loader.load("assets/data/wiz4-castle-floor-ceiling-map.tmx");
            TiledMapTileLayer floor1 = (TiledMapTileLayer) wiz4CastleFloorCeilingMap.getLayers().get("floor1");
            TiledMapTileLayer floor2 = (TiledMapTileLayer) wiz4CastleFloorCeilingMap.getLayers().get("floor2");
            TiledMapTileLayer floor3 = (TiledMapTileLayer) wiz4CastleFloorCeilingMap.getLayers().get("floor3");
            TiledMapTileLayer ceiling1 = (TiledMapTileLayer) wiz4CastleFloorCeilingMap.getLayers().get("ceiling1");
            TiledMapTileLayer ceiling2 = (TiledMapTileLayer) wiz4CastleFloorCeilingMap.getLayers().get("ceiling2");
            TiledMapTileLayer ceiling3 = (TiledMapTileLayer) wiz4CastleFloorCeilingMap.getLayers().get("ceiling3");

            castleFloorAndCeiling(wiz4CastleLevel0ModelInstances, floor1, floorModel, grassModel, -.05f);
            castleFloorAndCeiling(wiz4CastleLevel0ModelInstances, ceiling1, ceilingModel, null, 1.05f);
            castleFloorAndCeiling(wiz4CastleLevel0ModelInstances, floor2, floorModel, grassModel, 1.15f);
            castleFloorAndCeiling(wiz4CastleLevel0ModelInstances, ceiling2, ceilingModel, null, 2.25f);
            castleFloorAndCeiling(wiz4CastleLevel0ModelInstances, floor3, floorModel, grassModel, 2.35f);
            castleFloorAndCeiling(wiz4CastleLevel0ModelInstances, ceiling3, ceilingModel, null, 3.35f);

            castleFloorAndCeiling(wiz4CastleLevel12ModelInstances, floor1, floorModel, grassModel, -1.15f);
            castleFloorAndCeiling(wiz4CastleLevel12ModelInstances, ceiling1, ceilingModel, null, -.15f);
            castleFloorAndCeiling(wiz4CastleLevel12ModelInstances, floor2, floorModel, grassModel, -.05f);
            castleFloorAndCeiling(wiz4CastleLevel12ModelInstances, ceiling2, ceilingModel, null, 1.05f);
            castleFloorAndCeiling(wiz4CastleLevel12ModelInstances, floor3, floorModel, grassModel, 1.15f);
            castleFloorAndCeiling(wiz4CastleLevel12ModelInstances, ceiling3, ceilingModel, null, 2.25f);

            castleFloorAndCeiling(wiz4CastleLevel13ModelInstances, floor1, floorModel, grassModel, -2.25f);
            castleFloorAndCeiling(wiz4CastleLevel13ModelInstances, ceiling1, ceilingModel, null, -1.25f);
            castleFloorAndCeiling(wiz4CastleLevel13ModelInstances, floor2, floorModel, grassModel, -1.15f);
            castleFloorAndCeiling(wiz4CastleLevel13ModelInstances, ceiling2, ceilingModel, null, -.15f);
            castleFloorAndCeiling(wiz4CastleLevel13ModelInstances, floor3, floorModel, grassModel, -.05f);
            castleFloorAndCeiling(wiz4CastleLevel13ModelInstances, ceiling3, ceilingModel, null, 1.05f);

            for (int x = 0; x < DUNGEON_DIM; x++) {
                for (int y = 0; y < DUNGEON_DIM; y++) {
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

            DungeonTileModelInstance sk = new DungeonTileModelInstance(sky, 0, 0, 0);
            this.wiz4CastleLevel0ModelInstances.add(sk);
            this.wiz4CastleLevel12ModelInstances.add(sk);
            this.wiz4CastleLevel13ModelInstances.add(sk);

            for (int x = -DUNGEON_DIM * 2; x < DUNGEON_DIM * 2; x++) {
                for (int y = -DUNGEON_DIM * 2; y < DUNGEON_DIM * 2; y++) {
                    wiz4CastleLevel0ModelInstances.add(new DungeonTileModelInstance(grassModel, 0, 0, 0, x, -.06f, y));
                    wiz4CastleLevel12ModelInstances.add(new DungeonTileModelInstance(grassModel, 0, 0, 0, x, -1.16f, y));
                    wiz4CastleLevel13ModelInstances.add(new DungeonTileModelInstance(grassModel, 0, 0, 0, x, -2.26f, y));
                }
            }

        }

        for (int level = 0; level < this.map.scenario().levels().length; level++) {
            for (int x = 0; x < DUNGEON_DIM; x++) {
                for (int y = 0; y < DUNGEON_DIM; y++) {
                    MazeCell cell = this.map.scenario().levels()[level].cells[x][y];
                    addBlock(level, cell, x, y);
                    if (this.map != Map.WIZARDRY4 || (level == 4 || level == 6 || level == 8)) {
                        //duplicated for wrapping
                        addBlock(level, cell, x + DUNGEON_DIM, y);
                        addBlock(level, cell, x - DUNGEON_DIM, y);
                        addBlock(level, cell, x, y + DUNGEON_DIM);
                        addBlock(level, cell, x, y - DUNGEON_DIM);
                        addBlock(level, cell, x + DUNGEON_DIM, y + DUNGEON_DIM);
                        addBlock(level, cell, x - DUNGEON_DIM, y - DUNGEON_DIM);
                        addBlock(level, cell, x + DUNGEON_DIM, y - DUNGEON_DIM);
                        addBlock(level, cell, x - DUNGEON_DIM, y + DUNGEON_DIM);
                    }
                }
            }
        }

        //prune uneeded
        floor.removeIf(mi -> mi.transform.val[Matrix4.M03] > 30
                || mi.transform.val[Matrix4.M23] < -10
                || mi.transform.val[Matrix4.M03] < -10
                || mi.transform.val[Matrix4.M23] > 30);

        ceiling.removeIf(mi -> mi.transform.val[Matrix4.M03] > 30
                || mi.transform.val[Matrix4.M23] < -10
                || mi.transform.val[Matrix4.M03] < -10
                || mi.transform.val[Matrix4.M23] > 30);

        modelInstances.removeIf(mi -> mi.transform.val[Matrix4.M03] > 30
                || mi.transform.val[Matrix4.M23] < -10
                || mi.transform.val[Matrix4.M03] < -10
                || mi.transform.val[Matrix4.M23] > 30);

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

        float z = 0.5f;
        if (cell.northWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y);
            instance.transform.setFromEulerAngles(270, 0, 0).trn(1 + x - .025f, z, 1 + y - .5f);
            modelInstances.add(instance);
        }
        if (cell.southWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y);
            instance.transform.setFromEulerAngles(90, 0, 0).trn(x + .025f, z, y + .5f);
            modelInstances.add(instance);
        }
        if (cell.eastWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y);
            instance.transform.setFromEulerAngles(0, 0, 180).trn(x + .5f, z, 1 + y - .025f);
            modelInstances.add(instance);
        }
        if (cell.westWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y, x + .5f, z, y + .025f);
            modelInstances.add(instance);
        }

        if (cell.northDoor) {
            if (cell.northWall) {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y);
                instance.transform.setFromEulerAngles(270, 0, 0).trn(1 + x - .025f, z, 1 + y - .5f);
                modelInstances.add(instance);
            } else {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y);
                instance.transform.setFromEulerAngles(90, 0, 0).trn(1 + x - .025f, z, 1 + y - .5f);
                modelInstances.add(instance);
            }
        }
        if (cell.southDoor) {
            if (cell.southWall) {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y);
                instance.transform.setFromEulerAngles(90, 0, 0).trn(x + .025f, z, y + .5f);
                modelInstances.add(instance);
            } else {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y);
                instance.transform.setFromEulerAngles(270, 0, 0).trn(x + .025f, z, y + .5f);
                modelInstances.add(instance);
            }
        }
        if (cell.eastDoor) {
            if (cell.eastWall) {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y);
                instance.transform.setFromEulerAngles(0, 0, 180).trn(x + .5f, z, 1 + y - .025f);
                modelInstances.add(instance);
            } else {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y, x - .5f + 1, z, y - .025f + 1);
                modelInstances.add(instance);
            }
        }
        if (cell.westDoor) {
            if (cell.westWall) {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y);
                instance.transform.setToTranslation(x + .5f, z, y + .025f);
                modelInstances.add(instance);
            } else {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y);
                instance.transform.setFromEulerAngles(0, 180, 180).trn(x + .5f, z, y + .025f);
                modelInstances.add(instance);
            }
        }

        if (cell.stairs || cell.elevator) {
            modelInstances.add(new DungeonTileModelInstance(ladderModel, level, x, y, x + .5f, 0, y + .5f));
            if (cell.elevator) {
                modelInstances.add(new DungeonTileModelInstance(manhole, level, x, y, x + .5f, 0, y + .5f));
                modelInstances.add(new DungeonTileModelInstance(manhole, level, x, y, x + .5f, 1, y + .5f));
            } else if (cell.address.level < cell.addressTo.level) {//down
                modelInstances.add(new DungeonTileModelInstance(manhole, level, x, y, x + .5f, 0, y + .5f));
            } else {//up
                modelInstances.add(new DungeonTileModelInstance(manhole, level, x, y, x + .6f, 1, y + .5f));
            }
        }

        if (cell.summoningCircle != null) {
            modelInstances.add(new DungeonTileModelInstance(pentagram, level, x, y, x + .5f, 0, y + .5f));
        }
        if (cell.message != null || cell.function != null) {
            modelInstances.add(new DungeonTileModelInstance(letterM, level, x, y, x + .5f, .5f, y + .5f, true));
        }
    }

    private void rotateBlock(int level, MazeCell cell, float x, float y) {
        if (this.map.scenario().levels()[level].rotate(cell)) {
            addBlock(level, cell, x, y, true);
        }
    }

    private void castleFloorAndCeiling(List<DungeonTileModelInstance> list, TiledMapTileLayer layer, Model flm, Model grm, float z) {
        for (int x = 0; x < DUNGEON_DIM; x++) {
            for (int y = 0; y < DUNGEON_DIM; y++) {
                TiledMapTileLayer.Cell c = layer.getCell(x, DUNGEON_DIM - 1 - y);
                if (c != null) {
                    if (c.getTile().getId() == 993) {
                        list.add(new DungeonTileModelInstance(grm, 0, 0, 0, DUNGEON_DIM - 1 - y + .5f, z, x + .5f));
                    } else {
                        list.add(new DungeonTileModelInstance(flm, 0, 0, 0, DUNGEON_DIM - 1 - y + .5f, z, x + .5f));
                    }
                }
            }
        }
    }

    private void addCastleCell(List<DungeonTileModelInstance> list, int level, MazeCell cell, float x, float y, float z) {

        if (cell.northWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y);
            instance.transform.setFromEulerAngles(270, 0, 0).trn(1 + x - .025f, z, 1 + y - .5f);
            list.add(instance);
        }
        if (cell.southWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y);
            instance.transform.setFromEulerAngles(90, 0, 0).trn(x + .025f, z, y + .5f);
            list.add(instance);
        }
        if (cell.eastWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y);
            instance.transform.setFromEulerAngles(0, 0, 180).trn(x + .5f, z, 1 + y - .025f);
            list.add(instance);
        }
        if (cell.westWall) {
            DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y, x + .5f, z, y + .025f);
            list.add(instance);
        }

        if (cell.northDoor) {
            if (cell.northWall) {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y);
                instance.transform.setFromEulerAngles(270, 0, 0).trn(1 + x - .025f, z, 1 + y - .5f);
                list.add(instance);
            } else {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y);
                instance.transform.setFromEulerAngles(90, 0, 0).trn(1 + x - .025f, z, 1 + y - .5f);
                list.add(instance);
            }
        }
        if (cell.southDoor) {
            if (cell.southWall) {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y);
                instance.transform.setFromEulerAngles(90, 0, 0).trn(x + .025f, z, y + .5f);
                list.add(instance);
            } else {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y);
                instance.transform.setFromEulerAngles(270, 0, 0).trn(x + .025f, z, y + .5f);
                list.add(instance);
            }
        }
        if (cell.eastDoor) {
            if (cell.eastWall) {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y);
                instance.transform.setFromEulerAngles(0, 0, 180).trn(x + .5f, z, 1 + y - .025f);
                list.add(instance);
            } else {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y, x - .5f + 1, z, y - .025f + 1);
                list.add(instance);
            }
        }
        if (cell.westDoor) {
            if (cell.westWall) {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(wall, level, x, y, x + .5f, z, y + .025f);
                list.add(instance);
            } else {
                DungeonTileModelInstance instance = new DungeonTileModelInstance(doorModel, level, x, y);
                instance.transform.setFromEulerAngles(0, 180, 180).trn(x + .5f, z, y + .025f);
                list.add(instance);
            }
        }

        if (cell.stairs || cell.elevator) {
            list.add(new DungeonTileModelInstance(ladderModel, level, x, y, x + .5f, z - .5f, y + .5f));
            if (level == 13 || cell.address.level > cell.addressTo.level) {//down
                list.add(new DungeonTileModelInstance(manhole, level, x, y, x + .5f, z - .5f, y + .5f));
            } else {//up
                list.add(new DungeonTileModelInstance(manhole, level, x, y, x + .6f, z + .5f, y + .5f));
            }
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
                torch.set(flame.r, flame.g, flame.b, currentPos.x, currentPos.y + .35f, currentPos.z, 6);
            } else {
                torch.set(darkness.r, darkness.g, darkness.b, currentPos.x, currentPos.y + .35f, currentPos.z, 0.003f);
            }
            camera.far = 10f;
        } else {
            camera.far = 100f;
        }

        Gdx.gl.glViewport(32, 64, MAP_WIDTH, MAP_HEIGHT);

        camera.update();

        modelBatch.begin(camera);

        if (this.map != Map.WIZARDRY4 || (this.currentLevel >= 1 && this.currentLevel <= 11)) {
            for (ModelInstance i : floor) {
                modelBatch.render(i, environment);
            }
            for (ModelInstance i : ceiling) {
                modelBatch.render(i, environment);
            }
            for (DungeonTileModelInstance i : modelInstances) {
                if (i.getLevel() == currentLevel) {
                    i.render(modelBatch, environment);
                }
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

        int x = (Math.round(currentPos.x) - 1);
        int y = (Math.round(currentPos.z) - 1);
        if (this.map == Map.WIZARDRY4) {
            String lbl = String.format(WER_LEVEL_DESC[currentLevel] + " - Level %d [%d, %d]", currentLevel + 1, x, y).toUpperCase();
            Andius.font16.draw(batch, lbl, 280, Andius.SCREEN_HEIGHT - 12);
        } else {
            String lbl = String.format(this.map.getLabel() + " - Level %d [%d, %d]", currentLevel + 1, x, y).toUpperCase();
            Andius.font16.draw(batch, lbl, 280, Andius.SCREEN_HEIGHT - 12);
        }

        if (showMiniMap) {
            batch.draw(MINI_MAP_BACKGROUND, XALIGNMM - 3, YALIGNMM + 3);
            batch.draw(miniMap, XALIGNMM, YALIGNMM);
        }

        batch.end();

        stage.act();
        stage.draw();

    }

    private void createMiniMap() {

        if (miniMap != null) {
            miniMap.dispose();
        }

        Pixmap pixmap = new Pixmap(MM_BKGRND_DIM, MM_BKGRND_DIM, Format.RGBA8888);
        for (int x = 0; x < DUNGEON_DIM; x++) {
            for (int y = 0; y < DUNGEON_DIM; y++) {
                MazeCell cell = this.map.scenario().levels()[this.currentLevel].cells[x][y];

                if (cell.darkness) {
                    pixmap.setColor(Color.PURPLE);
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM, MINI_DIM, MINI_DIM);
                }
                if (cell.rock) {
                    pixmap.setColor(Color.BROWN);
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM, MINI_DIM, MINI_DIM);
                }
                if (cell.chute) {
                    pixmap.drawPixmap(
                            this.miniMapIconsPixmap,
                            x * MINI_DIM,
                            y * MINI_DIM,
                            arrows[2][3].getRegionX(),
                            arrows[2][3].getRegionY(),
                            arrows[2][3].getRegionWidth(),
                            arrows[2][3].getRegionHeight()
                    );
                }
                if (cell.encounterID >= 0) {
                    pixmap.setColor(Color.RED);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 5);
                }
                if (cell.wanderingEncounterID != -1) {
                    pixmap.setColor(Color.PINK);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 3);
                }
                if (cell.message != null || cell.function != null) {
                    pixmap.drawPixmap(
                            this.miniMapIconsPixmap,
                            x * MINI_DIM,
                            y * MINI_DIM,
                            arrows[0][0].getRegionX(),
                            arrows[0][0].getRegionY(),
                            arrows[0][0].getRegionWidth(),
                            arrows[0][0].getRegionHeight()
                    );
                }

                if (cell.tbd) {
                    pixmap.drawPixmap(
                            this.miniMapIconsPixmap,
                            x * MINI_DIM,
                            y * MINI_DIM,
                            arrows[3][3].getRegionX(),
                            arrows[3][3].getRegionY(),
                            arrows[3][3].getRegionWidth(),
                            arrows[3][3].getRegionHeight()
                    );
                }

                if (cell.itemRequired > 0) {
                    pixmap.setColor(Color.FOREST);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 4);
                }

                if (cell.itemObtained > 0) {
                    pixmap.setColor(Color.TEAL);
                    pixmap.fillRectangle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 4, 4);
                }

                if (cell.pit) {
                    pixmap.setColor(Color.BROWN);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 5);
                    pixmap.setColor(Color.WHITE);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 2);
                } else if (cell.damage != null) {
                    pixmap.drawPixmap(
                            this.miniMapIconsPixmap,
                            x * MINI_DIM,
                            y * MINI_DIM,
                            arrows[1][0].getRegionX(),
                            arrows[1][0].getRegionY(),
                            arrows[1][0].getRegionWidth(),
                            arrows[1][0].getRegionHeight()
                    );
                }
                if (cell.cage) {
                    pixmap.drawPixmap(
                            this.miniMapIconsPixmap,
                            x * MINI_DIM,
                            y * MINI_DIM,
                            arrows[1][1].getRegionX(),
                            arrows[1][1].getRegionY(),
                            arrows[1][1].getRegionWidth(),
                            arrows[1][1].getRegionHeight()
                    );
                }
                if (cell.spinner) {
                    pixmap.drawPixmap(
                            this.miniMapIconsPixmap,
                            x * MINI_DIM,
                            y * MINI_DIM,
                            arrows[2][2].getRegionX(),
                            arrows[2][2].getRegionY(),
                            arrows[2][2].getRegionWidth(),
                            arrows[2][2].getRegionHeight()
                    );
                }

                if (cell.teleport) {
                    pixmap.setColor(Color.FOREST);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 5);
                    pixmap.setColor(Color.WHITE);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 2);
                }

                pixmap.setColor(Color.DARK_GRAY);
                if (cell.northWall) {
                    pixmap.fillRectangle(x * MINI_DIM + MINI_DIM - 1, y * MINI_DIM, 1, MINI_DIM);
                }
                if (cell.southWall) {
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM, 1, MINI_DIM);
                }
                if (cell.eastWall) {
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM + MINI_DIM - 1, MINI_DIM, 1);
                }
                if (cell.westWall) {
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM, MINI_DIM, 1);
                }

                pixmap.setColor(Color.PINK);
                if (cell.hiddenNorthDoor) {
                    pixmap.fillRectangle(x * MINI_DIM + MINI_DIM - 1, y * MINI_DIM, 1, MINI_DIM);
                }
                if (cell.hiddenSouthDoor) {
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM, 1, MINI_DIM);
                }
                if (cell.hiddenEastDoor) {
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM + MINI_DIM - 1, MINI_DIM, 1);
                }
                if (cell.hiddenWestDoor) {
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM, MINI_DIM, 1);
                }

                pixmap.setColor(Color.YELLOW);
                if (cell.northDoor && !cell.hiddenNorthDoor) {
                    pixmap.fillRectangle(x * MINI_DIM + MINI_DIM - 1, y * MINI_DIM, 1, MINI_DIM);
                }
                if (cell.southDoor && !cell.hiddenSouthDoor) {
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM, 1, MINI_DIM);
                }
                if (cell.eastDoor && !cell.hiddenEastDoor) {
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM + MINI_DIM - 1, MINI_DIM, 1);
                }
                if (cell.westDoor && !cell.hiddenWestDoor) {
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM, MINI_DIM, 1);
                }

                if (cell.stairs || cell.elevator) {
                    drawLadderTriangle(cell, pixmap, x, y);
                }

                if (cell.summoningCircle != null) {
                    pixmap.drawPixmap(
                            this.miniMapIconsPixmap,
                            x * MINI_DIM,
                            y * MINI_DIM,
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
    }

    private void drawLadderTriangle(MazeCell cell, Pixmap pixmap, int x, int y) {
        int cx = x * MINI_DIM;
        int cy = y * MINI_DIM;
        pixmap.setColor(Color.YELLOW);
        if (cell.elevator) {
            pixmap.drawPixmap(
                    this.miniMapIconsPixmap,
                    cx,
                    cy,
                    arrows[3][2].getRegionX(),
                    arrows[3][2].getRegionY(),
                    arrows[3][2].getRegionWidth(),
                    arrows[3][2].getRegionHeight()
            );

        } else if (cell.stairs) {
            if (cell.address.level > cell.addressTo.level) {//up
                pixmap.drawPixmap(
                        this.miniMapIconsPixmap,
                        cx,
                        cy,
                        arrows[3][0].getRegionX(),
                        arrows[3][0].getRegionY(),
                        arrows[3][0].getRegionWidth(),
                        arrows[3][0].getRegionHeight()
                );
            } else {//down
                pixmap.drawPixmap(
                        this.miniMapIconsPixmap,
                        cx,
                        cy,
                        arrows[3][1].getRegionX(),
                        arrows[3][1].getRegionY(),
                        arrows[3][1].getRegionWidth(),
                        arrows[3][1].getRegionHeight()
                );
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
            if (currentDir == Direction.EAST) {
                t = east;
            }
            if (currentDir == Direction.WEST) {
                t = west;
            }
            if (currentDir == Direction.SOUTH) {
                t = south;
            }
            batch.draw(t, getX(), getY());
        }

    }

    private void moveMiniMapIcon() {
        miniMapIcon.setX(XALIGNMM + (Math.round(currentPos.x) - 1) * MINI_DIM);
        miniMapIcon.setY(YALIGNMM + MM_BKGRND_DIM - (Math.round(currentPos.z)) * MINI_DIM);
    }

    @Override
    public void partyDeath() {
        mainGame.setScreen(startScreen);
    }

    @Override
    public boolean keyDown(int keycode) {
        //for peeking
        boolean d = (currentDir == Direction.NORTH || currentDir == Direction.SOUTH);
        switch (keycode) {
            case Keys.W:
                camera.rotate(new Vector3(d ? 1 : 0, 0, d ? 0 : 1), 30f);
                break;
            case Keys.A:
                camera.rotate(new Vector3(0, 1, 0), 30f);
                break;
            case Keys.S:
                camera.rotate(new Vector3(d ? 1 : 0, 0, d ? 0 : 1), -30f);
                break;
            case Keys.D:
                camera.rotate(new Vector3(0, 1, 0), -30f);
                break;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {

        //for peeking
        boolean d = (currentDir == Direction.NORTH || currentDir == Direction.SOUTH);
        switch (keycode) {
            case Keys.W:
                camera.rotate(new Vector3(d ? 1 : 0, 0, d ? 0 : 1), -30f);
                break;
            case Keys.A:
                camera.rotate(new Vector3(0, 1, 0), -30f);
                break;
            case Keys.S:
                camera.rotate(new Vector3(d ? 1 : 0, 0, d ? 0 : 1), 30f);
                break;
            case Keys.D:
                camera.rotate(new Vector3(0, 1, 0), 30f);
                break;
        }

        int x = (Math.round(currentPos.x) - 1);
        int y = (Math.round(currentPos.z) - 1);
        MazeCell cell = this.map.scenario().levels()[this.currentLevel].cells[x][y];

        float tdur = .7f;

        if (keycode == Keys.LEFT) {

            if (currentDir == Direction.EAST) {
                stage.addAction(new PanCameraAction(camera, tdur, 90, 0));
                currentDir = Direction.NORTH;
            } else if (currentDir == Direction.WEST) {
                stage.addAction(new PanCameraAction(camera, tdur, 270, 180));
                currentDir = Direction.SOUTH;
            } else if (currentDir == Direction.NORTH) {
                stage.addAction(new PanCameraAction(camera, tdur, 0, -90));
                currentDir = Direction.WEST;
            } else if (currentDir == Direction.SOUTH) {
                stage.addAction(new PanCameraAction(camera, tdur, 180, 90));
                currentDir = Direction.EAST;
            }
            return false;

        } else if (keycode == Keys.RIGHT) {

            if (currentDir == Direction.EAST) {
                stage.addAction(new PanCameraAction(camera, tdur, 90, 180));
                currentDir = Direction.SOUTH;
            } else if (currentDir == Direction.WEST) {
                stage.addAction(new PanCameraAction(camera, tdur, 270, 360));
                currentDir = Direction.NORTH;
            } else if (currentDir == Direction.NORTH) {
                stage.addAction(new PanCameraAction(camera, tdur, 0, 90));
                currentDir = Direction.EAST;
            } else if (currentDir == Direction.SOUTH) {
                stage.addAction(new PanCameraAction(camera, tdur, 180, 270));
                currentDir = Direction.WEST;
            }
            return false;

        } else if (keycode == Keys.UP) {

            boolean skipProgression = false;

            //forward
            if (currentDir == Direction.EAST) {
                x = x + 1;
                if (x > DUNGEON_DIM - 1) {
                    x = 0;
                    skipProgression = true;
                }
            } else if (currentDir == Direction.WEST) {
                x = x - 1;
                if (x < 0) {
                    x = DUNGEON_DIM - 1;
                    skipProgression = true;
                }
            } else if (currentDir == Direction.NORTH) {
                y = y - 1;
                if (y < 0) {
                    y = DUNGEON_DIM - 1;
                    skipProgression = true;
                }
            } else if (currentDir == Direction.SOUTH) {
                y = y + 1;
                if (y > DUNGEON_DIM - 1) {
                    y = 0;
                    skipProgression = true;
                }
            }

            try {
                move(cell, currentDir, x, y, skipProgression);
            } catch (Throwable e) {
                partyDeath();
            }
            return false;

        } else if (keycode == Keys.DOWN) {
            boolean skipProgression = false;

            //backwards
            if (currentDir == Direction.EAST) {
                x = x - 1;
                if (x < 0) {
                    x = DUNGEON_DIM - 1;
                    skipProgression = true;
                }
            } else if (currentDir == Direction.WEST) {
                x = x + 1;
                if (x > DUNGEON_DIM - 1) {
                    x = 0;
                    skipProgression = true;
                }
            } else if (currentDir == Direction.NORTH) {
                y = y + 1;
                if (y > DUNGEON_DIM - 1) {
                    y = 0;
                    skipProgression = true;
                }
            } else if (currentDir == Direction.SOUTH) {
                y = y - 1;
                if (y < 0) {
                    y = DUNGEON_DIM - 1;
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
            if (cell.elevator || (cell.stairs)) {
                teleport(cell.addressTo, false);
            }
            return false;
        } else if (keycode == Keys.I) {
            if (!this.map.scenario().levels()[currentLevel].cells[x][y].darkness) {
                isTorchOn = !isTorchOn;
            } else {
                log("The torch fails to light and darkness remains!");
            }

        } else if (keycode == Keys.P) {

            showMiniMap = !showMiniMap;

        } else if (keycode == Keys.S) {

            if (cell.summoningCircle != null) {
                SummoningCircleScreen ssc = new SummoningCircleScreen(CTX.saveGame.players[0], cell.summoningCircle);
                mainGame.setScreen(ssc);
            }

        } else if (keycode == Keys.M) {

        } else if (keycode == Keys.Z) {

            return false;

        } else if (keycode == Keys.SPACE) {

            log("Pass");

            try {
                pass(x, y);
            } catch (Throwable e) {
                e.printStackTrace();
                partyDeath();
            }

        }

        finishTurn(x, y);

        return false;
    }

    private void move(MazeCell currentCell, Direction dir, int dx, int dy, boolean skipProgression) {

        WizardryData.MazeLevel[] levels = this.map.scenario().levels();
        MazeCell destinationCell = levels[currentLevel].cells[dx][dy];

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
                Sounds.play(Sound.PC_STRUCK);
                if (this.map == Map.WIZARDRY4 && (this.currentLevel == 12 || this.currentLevel == 13)) {
                    teleport(new MazeAddress(0, dx, dy), false);
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

        if (destinationCell.riddleAnswers != null && !destinationCell.riddleAnswers.isEmpty()) {
            new RiddleDialog(CTX, this, destinationCell).show(this.stage);
            return;
        }

        boolean moved = false;

        if (dir == Direction.EAST && (currentCell.hiddenNorthDoor || !currentCell.northWall)) {
            currentPos.x = dx + .5f;
            currentPos.z = dy + .5f;
            if (skipProgression) {
                this.camera.position.set(currentPos.x, .5f, currentPos.z);
            }
            stage.addAction(new MoveCameraAction(camera, .5f, dx + .5f, dy + .5f));
            if (dir == currentDir) {
                camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
            }
            moved = true;
        }

        if (dir == Direction.WEST && (currentCell.hiddenSouthDoor || !currentCell.southWall)) {
            currentPos.x = dx + .5f;
            currentPos.z = dy + .5f;
            if (skipProgression) {
                this.camera.position.set(currentPos.x, .5f, currentPos.z);
            }
            stage.addAction(new MoveCameraAction(camera, .5f, dx + .5f, dy + .5f));
            if (dir == currentDir) {
                camera.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
            }
            moved = true;
        }

        if (dir == Direction.NORTH && (currentCell.hiddenWestDoor || !currentCell.westWall)) {
            currentPos.x = dx + .5f;
            currentPos.z = dy + .5f;
            if (skipProgression) {
                this.camera.position.set(currentPos.x, .5f, currentPos.z);
            }
            stage.addAction(new MoveCameraAction(camera, .5f, dx + .5f, dy + .5f));
            if (dir == currentDir) {
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
            }
            moved = true;
        }

        if (dir == Direction.SOUTH && (currentCell.hiddenEastDoor || !currentCell.eastWall)) {
            currentPos.x = dx + .5f;
            currentPos.z = dy + .5f;
            if (skipProgression) {
                this.camera.position.set(currentPos.x, .5f, currentPos.z);
            }
            stage.addAction(new MoveCameraAction(camera, .5f, dx + .5f, dy + .5f));
            if (dir == currentDir) {
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
            }
            moved = true;
        }

        if (moved) {

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

            if (destinationCell.rotateDirection != -1) {
                rotateBlock(currentLevel, destinationCell, dx, dy);
                createMiniMap();
            }

            fight(destinationCell, currentCell, this.map.scenario().levels()[currentLevel].defeated);

            finishTurn(dx, dy);
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

        if (currentCell.riddleAnswers != null && !currentCell.riddleAnswers.isEmpty()) {
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
                    //DoGooder dogooder = this.map.scenario().characters().get(destCell.encounterID);
                    //Wiz4CombatScreen cs = new Wiz4CombatScreen(CTX.saveGame.players[0], CTX.saveGame.players[0].summonedMonsters, dogooder, destCell, fromCell);
                    //mainGame.setScreen(cs);
                }
            } else {
                int wanderingEncounterID = -1;
                if (Utils.percentChance(33)) {
                    wanderingEncounterID = this.map.scenario().levels()[currentLevel].getRandomMonster();
                    if (defeated.contains(wanderingEncounterID)) {
                        wanderingEncounterID = -1;
                    }
                    if (wanderingEncounterID > 0) {
                        //DoGooder dogooder = this.map.scenario().characters().get(wanderingEncounterID);
                        //Wiz4CombatScreen cs = new Wiz4CombatScreen(CTX.saveGame.players[0], CTX.saveGame.players[0].summonedMonsters, dogooder, destCell, fromCell);
                        //mainGame.setScreen(cs);
                    }
                }
            }

        } else {
            boolean treasure = destCell.hasTreasureChest || Utils.percentChance(33);
            boolean wandering = destCell.wanderingEncounterID != -1 && treasure;
            if (destCell.encounterID != -1 || wandering) {
                int encounterID = destCell.encounterID != -1 ? destCell.encounterID : destCell.wanderingEncounterID;
                Monster monster = this.map.scenario().monsters().get(encounterID);
                //andius.objects.Actor actor = new andius.objects.Actor(monster.name, null);
                //MutableMonster mm = new MutableMonster(monster);
                //actor.set(mm, Role.MONSTER, 1, 1, 1, 1, Constants.MovementBehavior.ATTACK_AVATAR);
                //TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
                //TiledMap tm = loader.load("assets/data/combat1.tmx");
                //CombatScreen cs = new CombatScreen(CTX, this.map, tm, actor, currentLevel + 1, false);
                //mainGame.setScreen(cs);
                //mainGame.setScreen(new WizardryCombatScreen(CTX, this.map, monster, currentLevel + 1, treasure, null, null));
            }
        }
    }

    @Override
    public void teleport(int level, int x, int y) {
        if (x >= DUNGEON_DIM) {
            x = DUNGEON_DIM - x;
        }
        if (x < 0) {
            x = DUNGEON_DIM + x;
        }
        if (y >= DUNGEON_DIM) {
            y = DUNGEON_DIM - y;
        }
        if (y < 0) {
            y = DUNGEON_DIM + y;
        }
        if (x >= DUNGEON_DIM) {
            x = DUNGEON_DIM - 1;
        }
        if (y >= DUNGEON_DIM) {
            y = DUNGEON_DIM - 1;
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

        if (addr.level <= 0 && addr.row == 0 && addr.column == 0) {
            //return to castle
            setMapPixelCoords(null, this.map.scenario().getStartX(), this.map.scenario().getStartY(), this.map.scenario().getStartLevel());
            Andius.mainGame.setScreen(Map.WORLD.getScreen());
            return;
        } else {
            to = addr;
        }

        int dx = to.row;
        int dy = to.column;

        if (to.level == currentLevel + 1) {//same level
            currentPos.x = dx + .5f;
            currentPos.z = dy + .5f;
            //this.camera.position.set(currentPos.x, .5f, currentPos.z);
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

            if (this.map.scenario().levels()[currentLevel].cells[x][y].encounterID > 0) {
                this.map.scenario().levels()[currentLevel].cells[x][y].encounterID = -1;
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
                            //this is not persisted to save file
                            this.map.scenario().levels()[currentLevel].defeated.add(id);
                        }
                    }
                }
            }

            if (this.map != Map.WIZARDRY4) {
                List<String> removedMonsters = CTX.saveGame.removedActors.get(this.map);
                if (removedMonsters == null) {
                    removedMonsters = new ArrayList<>();
                    CTX.saveGame.removedActors.put(this.map, removedMonsters);
                }
                //pesisted to save file
                removedMonsters.add(currentLevel + ":M:" + x + ":" + y);
            }
        }
    }

    @Override
    public void finishTurn(int currentX, int currentY) {
        CTX.endTurn(this.map);
    }

    private class DungeonTileModelInstance extends ModelInstance {

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
}
