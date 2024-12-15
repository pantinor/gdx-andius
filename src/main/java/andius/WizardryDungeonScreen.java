package andius;

import static andius.Andius.CTX;
import static andius.Andius.mainGame;
import static andius.Constants.CLASSPTH_RSLVR;
import static andius.WizardryData.DUNGEON_DIM;
import static andius.WizardryData.LEVELS;
import andius.WizardryData.MazeAddress;
import andius.WizardryData.MazeCell;
import andius.objects.Item;
import andius.objects.Monster;
import utils.MoveCameraAction;
import andius.objects.MutableMonster;
import utils.PanCameraAction;
import andius.objects.SaveGame;
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
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.UBJsonReader;

public class WizardryDungeonScreen extends BaseScreen {

    private static final int MAP_WIDTH = 672;
    private static final int MAP_HEIGHT = 672;

    private final Environment environment = new Environment();
    private final ModelBuilder builder = new ModelBuilder();

    private ModelBatch modelBatch;
    private SpriteBatch batch;

    private CameraInputController inputController;
    private AssetManager assets;

    //3d models
    private Model fountainModel;
    private Model ladderModel;
    private Model chestModel;
    private Model orbModel;
    private Model avatarModel;
    private Model wall, door, manhole;

    private boolean showMiniMap = true;

    private final Vector3 vdll = new Vector3(.04f, .04f, .04f);
    private final Vector3 nll = new Vector3(.96f, .58f, 0.08f);
    private PointLight torch;
    ModelInstance torchInstance;
    boolean isTorchOn = false;

    private final List<DungeonTileModelInstance> modelInstances = new ArrayList<>();
    private final List<ModelInstance> floor = new ArrayList<>();
    private final List<ModelInstance> ceiling = new ArrayList<>();
    private static Texture MINI_MAP_TEXTURE;
    private static final int MINI_DIM = 10;
    private static final int MM_BKGRND_DIM = MINI_DIM * DUNGEON_DIM;
    private static final int XALIGNMM = 16;
    private static final int YALIGNMM = 550;

    private int currentLevel = 0;
    private final Vector3 currentPos = new Vector3();
    private Direction currentDir = Direction.EAST;

    private Texture miniMap;
    private MiniMapIcon miniMapIcon;

    public WizardryDungeonScreen() {
        this.stage = new Stage();

        assets = new AssetManager(CLASSPTH_RSLVR);
        assets.load("assets/graphics/dirt.png", Texture.class);
        assets.load("assets/graphics/door.png", Texture.class);
        assets.load("assets/graphics/mortar.png", Texture.class);
        assets.load("assets/graphics/rock.png", Texture.class);

        assets.update(2000);

        //convert the collada dae format to the g3db format (do not use the obj format)
        //export from sketchup to collada dae format, then open dae in blender and export to the fbx format, then convert fbx to the g3db like below
        //C:\Users\Paul\Desktop\blender>fbx-conv-win32.exe -o G3DB ./Chess/pawn.fbx ./pawn.g3db
        ModelLoader<?> gloader = new G3dModelLoader(new UBJsonReader());
        fountainModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/fountain2.g3db"));
        ladderModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/ladder.g3db"));
        chestModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/chest.g3db"));
        orbModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/orb.g3db"));
        avatarModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/wizard.g3db"));

        Pixmap pixmap = new Pixmap(MM_BKGRND_DIM, MM_BKGRND_DIM, Format.RGBA8888);
        pixmap.setColor(0.8f, 0.7f, 0.5f, .8f);
        pixmap.fillRectangle(0, 0, MM_BKGRND_DIM, MM_BKGRND_DIM);
        MINI_MAP_TEXTURE = new Texture(pixmap);
        pixmap.dispose();

        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.05f, 0.05f, 0.05f, 1f));

        this.torch = new PointLight().set(1f, 0.8f, 0.6f, 4f, 4f, 4f, 5f);
        environment.add(this.torch);

        modelBatch = new ModelBatch();
        batch = new SpriteBatch();

        camera = new PerspectiveCamera(67f, MAP_WIDTH, MAP_HEIGHT);
        camera.near = 0.1f;
        camera.far = 1000f;

        Model torchModel = builder.createSphere(.1f, .1f, .1f, 10, 10, new Material(ColorAttribute.createDiffuse(1, 1, 1, 1)), Usage.Position);
        this.torchInstance = new ModelInstance(torchModel);

        manhole = builder.createCylinder(.75f, .02f, .75f, 32, new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)), Usage.Position | Usage.Normal);

        for (int x = 0; x < DUNGEON_DIM + 4; x++) {
            for (int y = 0; y < DUNGEON_DIM + 4; y++) {
                Model sf = builder.createBox(1.1f, 1, 1.1f, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/rock.png", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
                floor.add(new ModelInstance(sf, new Vector3(x - 1.5f, -.5f, y - 1.5f)));
            }
        }
        for (int x = 0; x < DUNGEON_DIM + 4; x++) {
            for (int y = 0; y < DUNGEON_DIM + 4; y++) {
                Model sf = builder.createBox(1.1f, 1, 1.1f, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/dirt.png", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
                ceiling.add(new ModelInstance(sf, new Vector3(x - 1.5f, 1.5f, y - 1.5f)));
            }
        }

        createWallsAndDoorModels();

        for (int level = 0; level < LEVELS.length; level++) {
            for (int x = 0; x < DUNGEON_DIM; x++) {
                for (int y = 0; y < DUNGEON_DIM; y++) {
                    MazeCell cell = LEVELS[level].cells[x][y];
                    addBlock(level, cell, x, y);
                }
            }
        }

        miniMapIcon = new MiniMapIcon();
        miniMapIcon.setOrigin(5, 5);

        stage.addActor(miniMapIcon);

        addButtons(Map.WIZARDRY1);

        setStartPosition();
        camera.position.set(currentPos);
        camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
        //camera.position.set(10, 10, 10);
        //camera.lookAt(10, 0, 10);
        //this.isTorchOn = true;
        //this.showMiniMap = true;
        //inputController = new CameraInputController(camera);
        //inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
        //inputController.translateUnits = 10f;
        //createAxes();
        //Gdx.input.setInputProcessor(inputController);
    }

    private void setStartPosition() {
        for (int y = 0; y < DUNGEON_DIM; y++) {
            for (int x = 0; x < DUNGEON_DIM; x++) {
                MazeCell cell = LEVELS[this.currentLevel].cells[x][y];
                if (cell.stairs && cell.address.level > cell.addressTo.level) {//up stairs location
                    setMapPixelCoords(null, x, y, currentLevel + 1);
                }
            }
        }
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
        v.set(x, .5f, y);
    }

    @Override
    public void save(SaveGame saveGame) {
        Vector3 v = new Vector3();
        getCurrentMapCoords(v);
        CTX.saveGame.map = Map.WIZARDRY1;
        CTX.saveGame.wx = 75;
        CTX.saveGame.wy = 95;
        CTX.saveGame.x = (int) v.x;
        CTX.saveGame.y = (int) v.z;
        CTX.saveGame.level = currentLevel + 1;
        CTX.saveGame.direction = this.currentDir;
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
            camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
        } else if (currentDir == Direction.SOUTH) {
            camera.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
        }
    }

    @Override
    public void log(String s) {
        Andius.HUD.add(s);
    }

    @Override
    public void show() {
        syncRemovedMonsters();
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
        createMiniMap();
        moveMiniMapIcon();
    }

    private void syncRemovedMonsters() {

        List<String> removedMonsters = CTX.saveGame.removedActors.get(Map.WIZARDRY1);
        if (removedMonsters == null) {
            removedMonsters = new ArrayList<>();
            CTX.saveGame.removedActors.put(Map.WIZARDRY1, removedMonsters);
        }

        for (int level = 0; level < LEVELS.length; level++) {
            for (int x = 0; x < DUNGEON_DIM; x++) {
                for (int y = 0; y < DUNGEON_DIM; y++) {
                    MazeCell cell = LEVELS[level].cells[x][y];
                    if (removedMonsters.contains(level + ":M:" + x + ":" + y)) {
                        cell.tempMonsterID = -1;
                    }
                }
            }
        }
    }

    @Override
    public void hide() {
    }

    private void createWallsAndDoorModels() {
        ModelBuilder builder = new ModelBuilder();
        Material mortar = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/mortar.png", Texture.class)));
        Material dr = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/door.png", Texture.class)));
        Material gr = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        Material bl = new Material(ColorAttribute.createDiffuse(Color.BLUE));
        Material yl = new Material(ColorAttribute.createDiffuse(Color.YELLOW));
        Material red = new Material(ColorAttribute.createDiffuse(Color.RED));

        wall = builder.createBox(1.090f, 1, 0.05f, mortar, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        door = builder.createBox(1.090f, 1, 0.05f, dr, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
    }

    private void addBlock(int level, MazeCell cell, float x, float y) {
        float z = 0.5f;
        if (cell.northWall) {
            ModelInstance instance = new ModelInstance(wall);
            instance.transform.setFromEulerAngles(270, 0, 0).trn(1 + x - .025f, z, 1 + y - .5f);
            modelInstances.add(new DungeonTileModelInstance(instance, level));
        }
        if (cell.southWall) {
            ModelInstance instance = new ModelInstance(wall);
            instance.transform.setFromEulerAngles(90, 0, 0).trn(x + .025f, z, y + .5f);
            modelInstances.add(new DungeonTileModelInstance(instance, level));
        }
        if (cell.eastWall) {
            ModelInstance instance = new ModelInstance(wall);
            instance.transform.setFromEulerAngles(0, 0, 180).trn(x + .5f, z, 1 + y - .025f);
            modelInstances.add(new DungeonTileModelInstance(instance, level));
        }
        if (cell.westWall) {
            ModelInstance instance = new ModelInstance(wall, x + .5f, z, y + .025f);
            modelInstances.add(new DungeonTileModelInstance(instance, level));
        }

        if (cell.northDoor) {
            if (cell.northWall) {
                ModelInstance instance = new ModelInstance(wall);
                instance.transform.setFromEulerAngles(270, 0, 0).trn(1 + x - .025f, z, 1 + y - .5f);
                modelInstances.add(new DungeonTileModelInstance(instance, level));
            } else {
                ModelInstance instance = new ModelInstance(door, 1 + x - .025f, z, 1 + y - .5f);
                instance.transform.setFromEulerAngles(90, 0, 0).trn(1 + x - .025f, z, 1 + y - .5f);
                modelInstances.add(new DungeonTileModelInstance(instance, level));
            }
        }
        if (cell.southDoor) {
            if (cell.southWall) {
                ModelInstance instance = new ModelInstance(wall);
                instance.transform.setFromEulerAngles(90, 0, 0).trn(x + .025f, z, y + .5f);
                modelInstances.add(new DungeonTileModelInstance(instance, level));
            } else {
                ModelInstance instance = new ModelInstance(door, x + .025f, z, y + .5f);
                instance.transform.setFromEulerAngles(270, 0, 0).trn(x + .025f, z, y + .5f);
                modelInstances.add(new DungeonTileModelInstance(instance, level));
            }
        }
        if (cell.eastDoor) {
            if (cell.eastWall) {
                ModelInstance instance = new ModelInstance(wall);
                instance.transform.setFromEulerAngles(0, 0, 180).trn(x + .5f, z, 1 + y - .025f);
                modelInstances.add(new DungeonTileModelInstance(instance, level));
            } else {
                ModelInstance instance = new ModelInstance(door, x - .5f + 1, z, y - .025f + 1);
                modelInstances.add(new DungeonTileModelInstance(instance, level));
            }
        }
        if (cell.westDoor) {
            if (cell.westWall) {
                ModelInstance instance = new ModelInstance(wall, x + .5f, z, y + .025f);
                modelInstances.add(new DungeonTileModelInstance(instance, level));
            } else {
                ModelInstance instance = new ModelInstance(door);
                instance.transform.setFromEulerAngles(0, 0, 180).trn(x + .5f, z, y + .025f);
                modelInstances.add(new DungeonTileModelInstance(instance, level));
            }
        }

        if (cell.stairs || cell.elevator) {
            ModelInstance instance = new ModelInstance(ladderModel, x + .5f, 0, y + .5f);
            instance.nodes.get(0).scale.set(.060f, .060f, .060f);
            instance.calculateTransforms();
            modelInstances.add(new DungeonTileModelInstance(instance, level));
            if (cell.elevator) {
                modelInstances.add(new DungeonTileModelInstance(new ModelInstance(manhole, x + .5f, 0, y + .5f), level));
                modelInstances.add(new DungeonTileModelInstance(new ModelInstance(manhole, x + .5f, 1, y + .5f), level));
            } else if (cell.address.level < cell.addressTo.level) {//down
                modelInstances.add(new DungeonTileModelInstance(new ModelInstance(manhole, x + .5f, 0, y + .5f), level));
            } else {//up
                modelInstances.add(new DungeonTileModelInstance(new ModelInstance(manhole, x + .5f, 1, y + .5f), level));
            }
        }
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        if (isTorchOn) {
            float intensity = MathUtils.lerp(5, 6, MathUtils.random());
            torch.set(nll.x, nll.y, nll.z, currentPos.x, currentPos.y + .35f, currentPos.z, intensity);
        } else {
            torch.set(vdll.x, vdll.y, vdll.z, currentPos.x, currentPos.y + .35f, currentPos.z, 0.003f);
        }

        Gdx.gl.glViewport(32, 64, MAP_WIDTH, MAP_HEIGHT);

        camera.update();

        modelBatch.begin(camera);

        modelBatch.render(this.torchInstance, environment);

        for (ModelInstance i : floor) {
            modelBatch.render(i, environment);
        }
        for (ModelInstance i : ceiling) {
            modelBatch.render(i, environment);
        }
        for (DungeonTileModelInstance i : modelInstances) {
            if (i.getLevel() == currentLevel) {
                modelBatch.render(i.getInstance(), environment);
            }
        }

        modelBatch.end();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.begin();

        batch.draw(Andius.backGround, 0, 0);

        Andius.HUD.render(batch, Andius.CTX);

        int x = (Math.round(currentPos.x) - 1);
        int y = (Math.round(currentPos.z) - 1);
        String lbl = String.format("Level %d [%d, %d]", currentLevel + 1, x, y);
        Andius.largeFont.draw(batch, lbl, 515, Andius.SCREEN_HEIGHT - 7);

        if (showMiniMap) {
            batch.draw(MINI_MAP_TEXTURE, XALIGNMM, YALIGNMM);
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
                MazeCell cell = LEVELS[this.currentLevel].cells[x][y];

                if (cell.itemRequired > 0) {
                    pixmap.setColor(Color.FOREST);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 4);
                }

                if (cell.itemObtained > 0) {
                    pixmap.setColor(Color.LIME);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 4);
                }

                if (cell.monsterID >= 0) {
                    pixmap.setColor(Color.SCARLET);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 3);
                }

                if (cell.tempMonsterID >= 0) {
                    pixmap.setColor(Color.RED);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 2);
                }

                if (cell.darkness) {
                    pixmap.setColor(Color.PURPLE);
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM, MINI_DIM, MINI_DIM);
                }

                pixmap.setColor(Color.DARK_GRAY);
                if (cell.northWall) {
                    pixmap.fillRectangle(x * MINI_DIM + MINI_DIM, y * MINI_DIM, 1, MINI_DIM);
                }
                if (cell.southWall) {
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM, 1, MINI_DIM);
                }
                if (cell.eastWall) {
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM + MINI_DIM, MINI_DIM, 1);
                }
                if (cell.westWall) {
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM, MINI_DIM, 1);
                }

                pixmap.setColor(Color.PINK);
                if (cell.northDoor) {
                    pixmap.fillRectangle(x * MINI_DIM + MINI_DIM, y * MINI_DIM, 1, MINI_DIM);
                }
                if (cell.southDoor) {
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM, 1, MINI_DIM);
                }
                if (cell.eastDoor) {
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM + MINI_DIM, MINI_DIM, 1);
                }
                if (cell.westDoor) {
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM, MINI_DIM, 1);
                }

                if (cell.stairs || cell.elevator) {
                    drawLadderTriangle(cell, pixmap, x, y);
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
            pixmap.fillTriangle(cx + 0, cy + 5, cx + 5, cy + 0, cx + MINI_DIM, cy + 5);
            pixmap.fillTriangle(cx + 0, cy + 5, cx + 5, cy + MINI_DIM, cx + MINI_DIM, cy + 5);
        } else if (cell.stairs && cell.address.level > cell.addressTo.level) {//up
            pixmap.fillTriangle(cx + 0, cy + MINI_DIM, cx + 5, cy + 0, cx + MINI_DIM, cy + MINI_DIM);
        } else if (cell.stairs && cell.address.level < cell.addressTo.level) {//down
            pixmap.fillTriangle(cx + 0, cy + 0, cx + 5, cy + MINI_DIM, cx + MINI_DIM, cy + 0);
        }
    }

    private Texture createMiniMapIcon(Direction dir) {
        Pixmap pixmap = new Pixmap(MINI_DIM, MINI_DIM, Format.RGBA8888);
        pixmap.setColor(Color.GREEN);
        if (dir == Direction.EAST) {
            pixmap.fillTriangle(0, 0, 0, MINI_DIM, MINI_DIM, 5);
        } else if (dir == Direction.NORTH) {
            pixmap.fillTriangle(0, MINI_DIM, 5, 0, MINI_DIM, MINI_DIM);
        } else if (dir == Direction.WEST) {
            pixmap.fillTriangle(MINI_DIM, 0, 0, 5, MINI_DIM, MINI_DIM);
        } else if (dir == Direction.SOUTH) {
            pixmap.fillTriangle(0, 0, 5, MINI_DIM, MINI_DIM, 0);
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private class MiniMapIcon extends Actor {

        Texture north;
        Texture south;
        Texture east;
        Texture west;

        public MiniMapIcon() {
            super();
            //could not get rotateBy to work so needed to do it this way
            this.north = createMiniMapIcon(Direction.NORTH);
            this.east = createMiniMapIcon(Direction.EAST);
            this.west = createMiniMapIcon(Direction.WEST);
            this.south = createMiniMapIcon(Direction.SOUTH);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (!showMiniMap) {
                return;
            }
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            Texture t = north;
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

        public void dispose() {
            north.dispose();
            east.dispose();
            west.dispose();
            south.dispose();
        }
    }

    private void moveMiniMapIcon() {
        miniMapIcon.setX(XALIGNMM + (Math.round(currentPos.x) - 1) * MINI_DIM);
        miniMapIcon.setY(YALIGNMM + MM_BKGRND_DIM - (Math.round(currentPos.z)) * MINI_DIM);
    }

    @Override
    public void partyDeath() {
    }

    @Override
    public boolean keyUp(int keycode) {

        int x = (Math.round(currentPos.x) - 1);
        int y = (Math.round(currentPos.z) - 1);
        MazeCell cell = LEVELS[this.currentLevel].cells[x][y];

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

            //forward
            if (currentDir == Direction.EAST) {
                x = x + 1;
                if (x > DUNGEON_DIM - 1) {
                    x = 0;
                }
            } else if (currentDir == Direction.WEST) {
                x = x - 1;
                if (x < 0) {
                    x = DUNGEON_DIM - 1;
                }
            } else if (currentDir == Direction.NORTH) {
                y = y - 1;
                if (y < 0) {
                    y = DUNGEON_DIM - 1;
                }
            } else if (currentDir == Direction.SOUTH) {
                y = y + 1;
                if (y > DUNGEON_DIM - 1) {
                    y = 0;
                }
            }

            try {
                move(cell, currentDir, x, y);
            } catch (Throwable e) {
                partyDeath();
            }
            return false;

        } else if (keycode == Keys.DOWN) {

            //backwards
            if (currentDir == Direction.EAST) {
                x = x - 1;
                if (x < 0) {
                    x = DUNGEON_DIM - 1;
                }
            } else if (currentDir == Direction.WEST) {
                x = x + 1;
                if (x > DUNGEON_DIM - 1) {
                    x = 0;
                }
            } else if (currentDir == Direction.NORTH) {
                y = y + 1;
                if (y > DUNGEON_DIM - 1) {
                    y = 0;
                }
            } else if (currentDir == Direction.SOUTH) {
                y = y - 1;
                if (y < 0) {
                    y = DUNGEON_DIM - 1;
                }
            }

            try {
                move(cell, Direction.reverse(currentDir), x, y);
            } catch (Throwable e) {
                partyDeath();
            }
            return false;

        } else if (keycode == Keys.K) {
            if (cell.elevator || (cell.stairs && cell.address.level > cell.addressTo.level)) {
                currentLevel--;
                if (currentLevel < 0) {
                    currentLevel = 0;
                    Andius.mainGame.setScreen(Map.WORLD.getScreen());
                } else {
                    createMiniMap();
                }
            }
            return false;

        } else if (keycode == Keys.D) {
            if (cell.elevator || (cell.stairs && cell.address.level < cell.addressTo.level)) {
                currentLevel++;
                if (currentLevel >= LEVELS.length) {
                    currentLevel = LEVELS.length - 1;
                } else {
                    createMiniMap();
                }
            }
            return false;

        } else if (keycode == Keys.I) {
            if (!LEVELS[currentLevel].cells[x][y].darkness) {
                isTorchOn = !isTorchOn;
            } else {
                log("The torch fails to light and darkness remains!");
            }

        } else if (keycode == Keys.G || keycode == Keys.R || keycode == Keys.W || keycode == Keys.C || keycode == Keys.S) {

        } else if (keycode == Keys.P) {

            showMiniMap = !showMiniMap;

        } else if (keycode == Keys.V) {

        } else if (keycode == Keys.M) {

        } else if (keycode == Keys.Z) {

            return false;

        } else {
            log("Pass");
        }

        finishTurn(x, y);

        return false;
    }

    private void move(MazeCell cell, Direction dir, int dx, int dy) {

        boolean moved = false;
        boolean teleport = false;

        if (LEVELS[currentLevel].cells[dx][dy].message != null) {
            if (LEVELS[currentLevel].cells[dx][dy].itemRequired <= 0) {
                animateText(LEVELS[currentLevel].cells[dx][dy].message.getText(), Color.GREEN, 100, 300, 100, 400, 3);
            }
        }

        if (LEVELS[currentLevel].cells[dx][dy].itemRequired > 0) {
            Item item = Andius.ITEMS.get(LEVELS[currentLevel].cells[dx][dy].itemRequired);
            boolean owned = false;
            for (int i = 0; i < Andius.CTX.players().length && item != null; i++) {
                if (Andius.CTX.players()[i].inventory.contains(item)) {
                    owned = true;
                }
            }
            if (!owned) {
                Sounds.play(Sound.NEGATIVE_EFFECT);
                animateText(LEVELS[currentLevel].cells[dx][dy].message.getText(), Color.GREEN, 100, 300, 100, 400, 3);
                return;
            }
        }

        if (LEVELS[currentLevel].cells[dx][dy].itemObtained > 0) {
            Item item = Andius.ITEMS.get(LEVELS[currentLevel].cells[dx][dy].itemObtained);
            boolean owned = false;
            for (int i = 0; i < Andius.CTX.players().length && item != null; i++) {
                if (Andius.CTX.players()[i].inventory.contains(item)) {
                    owned = true;
                }
            }
            if (!owned) {
                log("Party found " + item.genericName + ". ");
                Andius.CTX.players()[0].inventory.add(item);
            }
        }

        if (LEVELS[currentLevel].cells[dx][dy].teleport) {
            MazeAddress to = LEVELS[currentLevel].cells[dx][dy].addressTo;
            dx = to.row;
            dy = to.column;
            if (to.level == currentLevel + 1) {
                teleport = true;
            } else {
                currentLevel = to.level - 1;
                currentPos.x = dx + .5f;
                currentPos.z = dy + .5f;
                createMiniMap();
                return;
            }
        }

        if (dir == Direction.EAST && (cell.hiddenNorthDoor || !cell.northWall || teleport)) {
            currentPos.x = dx + .5f;
            currentPos.z = dy + .5f;
            stage.addAction(new MoveCameraAction(camera, .5f, dx + .5f, dy + .5f));
            if (dir == currentDir) {
                camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
            }
            moveMiniMapIcon();
            moved = true;
        }

        if (dir == Direction.WEST && (cell.hiddenSouthDoor || !cell.southWall || teleport)) {
            currentPos.x = dx + .5f;
            currentPos.z = dy + .5f;
            stage.addAction(new MoveCameraAction(camera, .5f, dx + .5f, dy + .5f));
            if (dir == currentDir) {
                camera.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
            }
            moveMiniMapIcon();
            moved = true;
        }

        if (dir == Direction.NORTH && (cell.hiddenWestDoor || !cell.westWall || teleport)) {
            currentPos.x = dx + .5f;
            currentPos.z = dy + .5f;
            stage.addAction(new MoveCameraAction(camera, .5f, dx + .5f, dy + .5f));
            if (dir == currentDir) {
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
            }
            moveMiniMapIcon();
            moved = true;
        }

        if (dir == Direction.SOUTH && (cell.hiddenEastDoor || !cell.eastWall || teleport)) {
            currentPos.x = dx + .5f;
            currentPos.z = dy + .5f;
            stage.addAction(new MoveCameraAction(camera, .5f, dx + .5f, dy + .5f));
            if (dir == currentDir) {
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
            }
            moveMiniMapIcon();
            moved = true;
        }

        if (moved) {

            if (LEVELS[currentLevel].cells[dx][dy].darkness) {
                isTorchOn = false;
            }

            if (LEVELS[currentLevel].cells[dx][dy].monsterID != -1 || LEVELS[currentLevel].cells[dx][dy].tempMonsterID != -1) {

                Monster monster = null;
                if (LEVELS[currentLevel].cells[dx][dy].monsterID != -1) {
                    monster = Andius.MONSTERS.get(LEVELS[currentLevel].cells[dx][dy].monsterID);
                } else {
                    monster = Andius.MONSTERS.get(LEVELS[currentLevel].cells[dx][dy].tempMonsterID);
                }

                andius.objects.Actor actor = new andius.objects.Actor(0, monster.name, TibianSprite.animation(monster.getIconId()));

                MutableMonster mm = new MutableMonster(monster);
                actor.set(mm, Role.MONSTER, 1, 1, 1, 1, Constants.MovementBehavior.ATTACK_AVATAR);

                TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
                TiledMap tm = loader.load("assets/data/combat1.tmx");
                CombatScreen cs = new CombatScreen(CTX, Constants.Map.WIZARDRY1, tm, actor, currentLevel + 1);
                mainGame.setScreen(cs);
            }

            finishTurn(dx, dy);
        }
    }

    @Override
    public void endCombat(boolean isWon, andius.objects.Actor opponent) {
        if (isWon) {
            int x = (Math.round(currentPos.x) - 1);
            int y = (Math.round(currentPos.z) - 1);
            LEVELS[currentLevel].cells[x][y].tempMonsterID = -1;

            List<String> removedMonsters = CTX.saveGame.removedActors.get(Map.WIZARDRY1);
            removedMonsters.add(currentLevel + ":M:" + x + ":" + y);
        }
    }

    @Override
    public void finishTurn(int currentX, int currentY) {
        CTX.endTurn(Constants.Map.WIZARDRY1);
    }

    private class DungeonTileModelInstance {

        private ModelInstance instance;
        private int level;

        public DungeonTileModelInstance(ModelInstance instance, int level) {
            this.instance = instance;
            this.level = level;
        }

        public ModelInstance getInstance() {
            return instance;
        }

        public int getLevel() {
            return level;
        }

    }

}
