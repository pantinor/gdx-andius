package andius;

import static andius.Andius.CTX;
import static andius.Andius.mainGame;
import static andius.Constants.CLASSPTH_RSLVR;
import static andius.WizardryData.DUNGEON_DIM;
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
import com.badlogic.gdx.graphics.VertexAttributes;
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
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.UBJsonReader;
import java.util.Iterator;

public class WizardryDungeonScreen extends BaseScreen {

    private static final int MAP_WIDTH = 672;
    private static final int MAP_HEIGHT = 672;

    private final Environment environment = new Environment();
    private final ModelBuilder builder = new ModelBuilder();
    private final ModelBatch modelBatch;
    private final SpriteBatch batch;

    private CameraInputController inputController;
    private final AssetManager assets;

    private Model fountainModel;
    private Model ladderModel;
    private Model chestModel;
    private Model orbModel;
    private Model avatarModel;
    private Model wall, door, manhole;

    private final Vector3 vdll = new Vector3(.04f, .04f, .04f);
    private final Vector3 nll = new Vector3(.96f, .58f, 0.08f);
    private PointLight torch;
    boolean isTorchOn = false;

    private final List<DungeonTileModelInstance> modelInstances = new ArrayList<>();
    private final List<ModelInstance> floor = new ArrayList<>();
    private final List<ModelInstance> ceiling = new ArrayList<>();
    private static Texture MINI_MAP_TEXTURE;
    private final TextureRegion[][] arrows = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/arrows.png")), 10, 10);
    private final Pixmap miniMapIconsPixmap;
    private static final int MINI_DIM = 10;
    private static final int MM_BKGRND_DIM = MINI_DIM * DUNGEON_DIM;
    private static final int XALIGNMM = 16;
    private static final int YALIGNMM = 550;

    private int currentLevel = 0;
    private final Vector3 currentPos = new Vector3();
    private Direction currentDir = Direction.EAST;

    private boolean showMiniMap = true;
    private Texture miniMap;
    private final MiniMapIcon miniMapIcon;
    private final Constants.Map map;

    public WizardryDungeonScreen(Constants.Map map) {
        this.map = map;
        this.stage = new Stage();
        this.assets = new AssetManager(CLASSPTH_RSLVR);

        Pixmap pixmap = new Pixmap(MM_BKGRND_DIM, MM_BKGRND_DIM, Format.RGBA8888);
        pixmap.setColor(0.8f, 0.7f, 0.5f, .8f);
        pixmap.fillRectangle(0, 0, MM_BKGRND_DIM, MM_BKGRND_DIM);
        MINI_MAP_TEXTURE = new Texture(pixmap);
        pixmap.dispose();
        arrows[3][2].getTexture().getTextureData().prepare();
        this.miniMapIconsPixmap = arrows[3][2].getTexture().getTextureData().consumePixmap();

        this.torch = new PointLight().set(1f, 0.8f, 0.6f, 4f, 4f, 4f, 5f);
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.05f, 0.05f, 0.05f, 1f));
        this.environment.add(this.torch);

        this.modelBatch = new ModelBatch();
        this.batch = new SpriteBatch();

        this.camera = new PerspectiveCamera(67f, MAP_WIDTH, MAP_HEIGHT);
        this.camera.near = 0.1f;
        this.camera.far = 10f;

        load();

        this.miniMapIcon = new MiniMapIcon();
        this.miniMapIcon.setOrigin(4, 4);

        stage.addActor(miniMapIcon);
        Andius.HUD.addActor(stage);

        addButtons(this.map);

        setStartPosition();
        camera.position.set(currentPos);
        camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
//        currentLevel = 2;
//        camera.position.set(10, 20, 10);
//        camera.lookAt(10, 0, 10);
//        this.isTorchOn = true;
//        this.showMiniMap = false;
//        inputController = new CameraInputController(camera);
//        inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
//        inputController.translateUnits = 10f;
//        Gdx.input.setInputProcessor(inputController);
//        createAxes();
    }

    private void setStartPosition() {
        for (int y = 0; y < DUNGEON_DIM; y++) {
            for (int x = 0; x < DUNGEON_DIM; x++) {
                MazeCell cell = this.map.scenario().levels()[this.currentLevel].cells[x][y];
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
        CTX.saveGame.map = this.map;
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
        Andius.HUD.log(s);
    }

    @Override
    public void show() {
        syncRemovedMonsters();
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
        createMiniMap();
        moveMiniMapIcon();
    }

    private void syncRemovedMonsters() {

        List<String> removedMonsters = CTX.saveGame.removedActors.get(this.map);
        if (removedMonsters == null) {
            removedMonsters = new ArrayList<>();
            CTX.saveGame.removedActors.put(this.map, removedMonsters);
        }

        for (int level = 0; level < this.map.scenario().levels().length; level++) {
            for (int x = 0; x < DUNGEON_DIM; x++) {
                for (int y = 0; y < DUNGEON_DIM; y++) {
                    MazeCell cell = this.map.scenario().levels()[level].cells[x][y];
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

    private void load() {

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

        Material mortar = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/mortar.png", Texture.class)));
        Material dr = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/door.png", Texture.class)));
        Material gr = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        Material bl = new Material(ColorAttribute.createDiffuse(Color.BLUE));
        Material yl = new Material(ColorAttribute.createDiffuse(Color.YELLOW));
        Material red = new Material(ColorAttribute.createDiffuse(Color.RED));

        wall = builder.createBox(1.090f, 1, 0.05f, mortar, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        door = builder.createBox(1.090f, 1, 0.05f, dr, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        manhole = builder.createCylinder(.75f, .02f, .75f, 32, new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)), Usage.Position | Usage.Normal);

        for (int x = -DUNGEON_DIM * 2; x < DUNGEON_DIM * 2; x++) {
            for (int y = -DUNGEON_DIM * 2; y < DUNGEON_DIM * 2; y++) {
                Model sf = builder.createBox(1.1f, 1, 1.1f, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/rock.png", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
                floor.add(new ModelInstance(sf, new Vector3(x - 1.5f, -.5f, y - 1.5f)));
            }
        }
        for (int x = -DUNGEON_DIM * 2; x < DUNGEON_DIM * 2; x++) {
            for (int y = -DUNGEON_DIM * 2; y < DUNGEON_DIM * 2; y++) {
                Model sf = builder.createBox(1.1f, 1, 1.1f, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/dirt.png", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
                ceiling.add(new ModelInstance(sf, new Vector3(x - 1.5f, 1.5f, y - 1.5f)));
            }
        }

        for (int level = 0; level < this.map.scenario().levels().length; level++) {
            for (int x = 0; x < DUNGEON_DIM; x++) {
                for (int y = 0; y < DUNGEON_DIM; y++) {
                    MazeCell cell = this.map.scenario().levels()[level].cells[x][y];
                    addBlock(level, cell, x, y);
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

        //prune uneeded
        Iterator<ModelInstance> iter = floor.iterator();
        Iterator<ModelInstance> iter2 = ceiling.iterator();
        Iterator<DungeonTileModelInstance> iter3 = modelInstances.iterator();
        while (iter.hasNext()) {
            ModelInstance mi = iter.next();
            if (mi.transform.val[Matrix4.M03] > 30 || mi.transform.val[Matrix4.M23] < -10
                    || mi.transform.val[Matrix4.M03] < -10 || mi.transform.val[Matrix4.M23] > 30) {
                iter.remove();
            }
        }
        while (iter2.hasNext()) {
            ModelInstance mi = iter2.next();
            if (mi.transform.val[Matrix4.M03] > 30 || mi.transform.val[Matrix4.M23] < -10
                    || mi.transform.val[Matrix4.M03] < -10 || mi.transform.val[Matrix4.M23] > 30) {
                iter2.remove();
            }
        }
        while (iter3.hasNext()) {
            DungeonTileModelInstance mi = iter3.next();
            if (mi.getX() > 30 || mi.getY() < -10
                    || mi.getX() < -10 || mi.getY() > 30) {
                iter3.remove();
            }
        }
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
            float intensity = 6;//MathUtils.lerp(5, 7, MathUtils.random());
            torch.set(nll.x, nll.y, nll.z, currentPos.x, currentPos.y + .35f, currentPos.z, intensity);
            //float intensity = 120;
            //torch.set(nll.x, nll.y, nll.z, 10, 10, 10, intensity);
        } else {
            torch.set(vdll.x, vdll.y, vdll.z, currentPos.x, currentPos.y + .35f, currentPos.z, 0.003f);
        }

        Gdx.gl.glViewport(32, 64, MAP_WIDTH, MAP_HEIGHT);

        camera.update();

        modelBatch.begin(camera);

        //modelBatch.render(axesInstance);
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
                MazeCell cell = this.map.scenario().levels()[this.currentLevel].cells[x][y];
                
                if (cell.darkness) {
                    pixmap.setColor(Color.PURPLE);
                    pixmap.fillRectangle(x * MINI_DIM, y * MINI_DIM, MINI_DIM, MINI_DIM);
                }
                if (cell.monsterID >= 0) {
                    pixmap.setColor(Color.RED);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 3);
                }

                if (cell.tempMonsterID >= 0) {
                    pixmap.setColor(Color.ORANGE);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 2);
                }

                if (cell.itemRequired > 0) {
                    pixmap.setColor(Color.FOREST);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 3);
                }

                if (cell.itemObtained > 0) {
                    pixmap.setColor(Color.LIME);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 3);
                }

                if (cell.pit) {
                    pixmap.drawPixmap(
                            this.miniMapIconsPixmap,
                            x * MINI_DIM + 1,
                            y * MINI_DIM + 1,
                            arrows[3][0].getRegionX(),
                            arrows[3][0].getRegionY(),
                            arrows[3][0].getRegionWidth(),
                            arrows[3][0].getRegionHeight()
                    );
                }

                if (cell.teleport) {
                    pixmap.drawPixmap(
                            this.miniMapIconsPixmap,
                            x * MINI_DIM + 1,
                            y * MINI_DIM + 1,
                            arrows[2][1].getRegionX(),
                            arrows[2][1].getRegionY(),
                            arrows[2][1].getRegionWidth(),
                            arrows[2][1].getRegionHeight()
                    );
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
            pixmap.drawPixmap(
                    this.miniMapIconsPixmap,
                    cx,
                    cy,
                    arrows[3][3].getRegionX(),
                    arrows[3][3].getRegionY(),
                    arrows[3][3].getRegionWidth(),
                    arrows[3][3].getRegionHeight()
            );
        } else if (cell.stairs && cell.address.level > cell.addressTo.level) {//up
            pixmap.drawPixmap(
                    this.miniMapIconsPixmap,
                    cx,
                    cy,
                    arrows[3][2].getRegionX(),
                    arrows[3][2].getRegionY(),
                    arrows[3][2].getRegionWidth(),
                    arrows[3][2].getRegionHeight()
            );
        } else if (cell.stairs && cell.address.level < cell.addressTo.level) {//down
            pixmap.drawPixmap(
                    this.miniMapIconsPixmap,
                    cx,
                    cy,
                    arrows[2][3].getRegionX(),
                    arrows[2][3].getRegionY(),
                    arrows[2][3].getRegionWidth(),
                    arrows[2][3].getRegionHeight()
            );
        }
    }

    private class MiniMapIcon extends Actor {

        TextureRegion north;
        TextureRegion south;
        TextureRegion east;
        TextureRegion west;

        public MiniMapIcon() {
            super();
            this.north = arrows[1][0];
            this.east = arrows[0][0];
            this.west = arrows[0][1];
            this.south = arrows[1][1];
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
        miniMapIcon.setX(XALIGNMM + (Math.round(currentPos.x) - 1) * MINI_DIM + 1);
        miniMapIcon.setY(YALIGNMM + MM_BKGRND_DIM - (Math.round(currentPos.z)) * MINI_DIM);
    }

    @Override
    public void partyDeath() {
    }

    @Override
    public boolean keyUp(int keycode) {

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
                if (currentLevel >= this.map.scenario().levels().length) {
                    currentLevel = this.map.scenario().levels().length - 1;
                } else {
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

    private void move(MazeCell cell, Direction dir, int dx, int dy, boolean skipProgression) {

        boolean moved = false;
        boolean teleport = false;
        WizardryData.MazeLevel[] levels = this.map.scenario().levels();

        if (levels[currentLevel].cells[dx][dy].message != null) {
            if (levels[currentLevel].cells[dx][dy].itemRequired <= 0) {
                animateText(levels[currentLevel].cells[dx][dy].message.getText(), Color.GREEN, 100, 300, 100, 400, 3);
            }
        }

        if (levels[currentLevel].cells[dx][dy].itemRequired > 0) {
            Item item = this.map.scenario().items().get(levels[currentLevel].cells[dx][dy].itemRequired);
            boolean owned = false;
            for (int i = 0; i < Andius.CTX.players().length && item != null; i++) {
                if (Andius.CTX.players()[i].inventory.contains(item)) {
                    owned = true;
                }
            }
            if (!owned) {
                Sounds.play(Sound.NEGATIVE_EFFECT);
                animateText(levels[currentLevel].cells[dx][dy].message.getText(), Color.GREEN, 100, 300, 100, 400, 3);
                return;
            }
        }

        if (levels[currentLevel].cells[dx][dy].itemObtained > 0) {
            Item item = this.map.scenario().items().get(levels[currentLevel].cells[dx][dy].itemObtained);
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

        if (levels[currentLevel].cells[dx][dy].teleport) {
            MazeAddress to = levels[currentLevel].cells[dx][dy].addressTo;
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
            if (skipProgression) {
                this.camera.position.set(currentPos.x, .5f, currentPos.z);
            }
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
            if (skipProgression) {
                this.camera.position.set(currentPos.x, .5f, currentPos.z);
            }
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
            if (skipProgression) {
                this.camera.position.set(currentPos.x, .5f, currentPos.z);
            }
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
            if (skipProgression) {
                this.camera.position.set(currentPos.x, .5f, currentPos.z);
            }
            stage.addAction(new MoveCameraAction(camera, .5f, dx + .5f, dy + .5f));
            if (dir == currentDir) {
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
            }
            moveMiniMapIcon();
            moved = true;
        }

        if (moved) {

            if (levels[currentLevel].cells[dx][dy].darkness) {
                isTorchOn = false;
            }

            if (levels[currentLevel].cells[dx][dy].monsterID != -1 || levels[currentLevel].cells[dx][dy].tempMonsterID != -1) {

                Monster monster = null;
                if (levels[currentLevel].cells[dx][dy].monsterID != -1) {
                    monster = this.map.scenario().monsters().get(levels[currentLevel].cells[dx][dy].monsterID);
                } else {
                    monster = this.map.scenario().monsters().get(levels[currentLevel].cells[dx][dy].tempMonsterID);
                }

                andius.objects.Actor actor = new andius.objects.Actor(0, monster.name, TibianSprite.animation(monster.getIconId()));

                MutableMonster mm = new MutableMonster(monster);
                actor.set(mm, Role.MONSTER, 1, 1, 1, 1, Constants.MovementBehavior.ATTACK_AVATAR);

                TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
                TiledMap tm = loader.load("assets/data/combat1.tmx");
                CombatScreen cs = new CombatScreen(CTX, this.map, tm, actor, currentLevel + 1);
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
            this.map.scenario().levels()[currentLevel].cells[x][y].tempMonsterID = -1;

            List<String> removedMonsters = CTX.saveGame.removedActors.get(this.map);
            removedMonsters.add(currentLevel + ":M:" + x + ":" + y);
        }
    }

    @Override
    public void finishTurn(int currentX, int currentY) {
        CTX.endTurn(this.map);
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

        public float getX() {
            return this.instance.transform.val[Matrix4.M03];
        }

        public float getY() {
            return this.instance.transform.val[Matrix4.M23];
        }

    }

    final float GRID_MIN = 0;//-1 * 20;
    final float GRID_MAX = 20;//1 * 20;
    final float GRID_STEP = 1;
    public Model axesModel;
    public ModelInstance axesInstance;

    private void createAxes() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        // grid
        MeshPartBuilder builder = modelBuilder.part("grid", GL30.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, new Material());
        builder.setColor(Color.LIGHT_GRAY);
        for (float t = GRID_MIN; t <= GRID_MAX; t += GRID_STEP) {
            builder.line(t, 0, GRID_MIN, t, 0, GRID_MAX);
            builder.line(GRID_MIN, 0, t, GRID_MAX, 0, t);
        }
        // axes
        builder = modelBuilder.part("axes", GL30.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, new Material());
        builder.setColor(Color.RED);
        builder.line(0, 0, 0, 50, 0, 0);
        builder.setColor(Color.GREEN);
        builder.line(0, 0, 0, 0, 50, 0);
        builder.setColor(Color.BLUE);
        builder.line(0, 0, 0, 0, 0, 50);
        axesModel = modelBuilder.end();
        axesInstance = new ModelInstance(axesModel);
    }

}
