package andius;

import static andius.Andius.CTX;
import static andius.Andius.mainGame;
import static andius.Constants.CLASSPTH_RSLVR;
import static andius.WizardryData.DUNGEON_DIM;
import andius.WizardryData.MazeAddress;
import andius.WizardryData.MazeCell;
import static andius.WizardryData.PMO_MONSTERS;
import static andius.WizardryData.WER_LEVEL_DESC;
import andius.objects.DoGooder;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.MutableMonster;
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
import utils.Utils;

public class WizardryDungeonScreen extends BaseScreen {

    private static final int MAP_WIDTH = 672;
    private static final int MAP_HEIGHT = 672;

    private final Environment environment = new Environment();
    private final ModelBuilder builder = new ModelBuilder();
    private final ModelBatch modelBatch;
    private final SpriteBatch batch;

    private CameraInputController inputController;
    private final AssetManager assets;

    private Model ladderModel;
    private Model doorModel, pentagram;
    private Model wall, manhole;

    private final Color darkness = Color.DARK_GRAY;
    private final Color flame = new Color(0xf59414ff);
    private PointLight torch;
    boolean isTorchOn = false;

    private final List<DungeonTileModelInstance> modelInstances = new ArrayList<>();
    private final List<ModelInstance> floor = new ArrayList<>();
    private final List<ModelInstance> ceiling = new ArrayList<>();
    private static Texture MINI_MAP_TEXTURE;
    private final TextureRegion[][] arrows = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/arrows.png")), 15, 15);
    private final Pixmap miniMapIconsPixmap;
    private static final int MINI_DIM = 15;
    private static final int MM_BKGRND_DIM = MINI_DIM * DUNGEON_DIM + MINI_DIM / 2;
    private static final int XALIGNMM = 705;
    private static final int YALIGNMM = 415;

    public int currentLevel = 0;
    private final Vector3 currentPos = new Vector3();
    private Direction currentDir = Direction.EAST;
    private boolean loadedMazeData;

    private boolean showMiniMap = true;
    private Texture miniMap;
    private final MiniMapIcon miniMapIcon;
    private final Constants.Map map;

    public WizardryDungeonScreen(Constants.Map map) {
        this.map = map;
        this.stage = new Stage();
        this.assets = new AssetManager(CLASSPTH_RSLVR);

        int dim = MM_BKGRND_DIM + MINI_DIM;
        Pixmap pixmap = new Pixmap(dim, dim, Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fillRectangle(0, 0, dim, dim);
        pixmap.setColor(0x404040ff);
        pixmap.fillRectangle(2, 2, dim - 4, dim - 4);
        pixmap.setColor(0x606060ff);
        pixmap.fillRectangle(4, 4, dim - 8, dim - 8);
        pixmap.setColor(0xabababff);
        pixmap.fillRectangle(6, 6, dim - 12, dim - 12);

        MINI_MAP_TEXTURE = new Texture(pixmap);
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
        assets.load("assets/graphics/wood-door-texture.png", Texture.class);
        assets.update(2000);

        Material mortar = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/mortar.png", Texture.class)));
        Material mortar2 = new Material(TextureAttribute.createDiffuse(Utils.reverse(assets.get("assets/graphics/mortar.png", Texture.class))));
        Material wood = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/wood-door-texture.png", Texture.class)));
        Material dirt = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/dirt.png", Texture.class)));
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

        wall = builder.createBox(1.090f, 1, 0.05f, mortar, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        Model walldoor = builder.createBox(1.090f, 1, 0.05f, mortar2, Usage.Position | Usage.Normal | Usage.TextureCoordinates);

        manhole = builder.createCylinder(.5f, .02f, .5f, 32, new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)), Usage.Position | Usage.Normal);

        builder.begin();
        builder.node("door-wall", walldoor);
        builder.node("door-main", doorModel);
        doorModel = builder.end();

        for (int x = -DUNGEON_DIM * 2; x < DUNGEON_DIM * 2; x++) {
            for (int y = -DUNGEON_DIM * 2; y < DUNGEON_DIM * 2; y++) {
                Model sf = builder.createBox(1.1f, 1, 1.1f, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/rock.png", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
                floor.add(new ModelInstance(sf, new Vector3(x - 1.5f, -.5f, y - 1.5f)));
            }
        }
        for (int x = -DUNGEON_DIM * 2; x < DUNGEON_DIM * 2; x++) {
            for (int y = -DUNGEON_DIM * 2; y < DUNGEON_DIM * 2; y++) {
                Model sf = builder.createBox(1.1f, 1, 1.1f, dirt, Usage.Position | Usage.TextureCoordinates | Usage.Normal);
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
                ModelInstance instance = new ModelInstance(doorModel);
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
                ModelInstance instance = new ModelInstance(doorModel);
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
                ModelInstance instance = new ModelInstance(doorModel);
                instance.transform.setToTranslation(x - .5f + 1, z, y - .025f + 1);
                modelInstances.add(new DungeonTileModelInstance(instance, level));
            }
        }
        if (cell.westDoor) {
            if (cell.westWall) {
                ModelInstance instance = new ModelInstance(wall, x + .5f, z, y + .025f);
                modelInstances.add(new DungeonTileModelInstance(instance, level));
            } else {
                ModelInstance instance = new ModelInstance(doorModel);
                instance.transform.setFromEulerAngles(0, 0, 360).trn(x + .5f, z, y + .025f);
                modelInstances.add(new DungeonTileModelInstance(instance, level));
            }
        }

        if (cell.stairs || cell.elevator) {
            ModelInstance ladder = new ModelInstance(ladderModel, x + .5f, 0, y + .5f);
            modelInstances.add(new DungeonTileModelInstance(ladder, level));
            if (cell.elevator) {
                modelInstances.add(new DungeonTileModelInstance(new ModelInstance(manhole, x + .5f, 0, y + .5f), level));
                modelInstances.add(new DungeonTileModelInstance(new ModelInstance(manhole, x + .5f, 1, y + .5f), level));
            } else if (cell.address.level < cell.addressTo.level) {//down
                modelInstances.add(new DungeonTileModelInstance(new ModelInstance(manhole, x + .5f, 0, y + .5f), level));
            } else {//up
                modelInstances.add(new DungeonTileModelInstance(new ModelInstance(manhole, x + .6f, 1, y + .5f), level));
            }
        }

        if (cell.summoningCircle1 || cell.summoningCircle2 || cell.summoningCircle3 || cell.summoningCircle4
                || cell.summoningCircle5 || cell.summoningCircle6 || cell.summoningCircle7
                || cell.summoningCircle8 || cell.summoningCircle9 || cell.summoningCircle10) {
            ModelInstance penta = new ModelInstance(pentagram, x + .5f, 0, y + .5f);
            modelInstances.add(new DungeonTileModelInstance(penta, level));
        }
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        if (isTorchOn) {
            float intensity = 6;//MathUtils.lerp(5, 7, MathUtils.random());
            torch.set(flame.r, flame.g, flame.b, currentPos.x, currentPos.y + .35f, currentPos.z, intensity);
            //float intensity = 500;
            //torch.set(nll.x, nll.y, nll.z, 10, 10, 10, intensity);
        } else {
            torch.set(darkness.r, darkness.g, darkness.b, currentPos.x, currentPos.y + .35f, currentPos.z, 0.003f);
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
        if (this.map == Map.WIZARDRY4) {
            String lbl = String.format(WER_LEVEL_DESC[currentLevel] + " - Level %d [%d, %d]", currentLevel + 1, x, y).toUpperCase();
            Andius.font16.draw(batch, lbl, 280, Andius.SCREEN_HEIGHT - 12);
        } else {
            String lbl = String.format(this.map.getLabel() + " - Level %d [%d, %d]", currentLevel + 1, x, y).toUpperCase();
            Andius.font16.draw(batch, lbl, 280, Andius.SCREEN_HEIGHT - 12);
        }

        if (showMiniMap) {
            batch.draw(MINI_MAP_TEXTURE, XALIGNMM - 10, Andius.SCREEN_HEIGHT - MINI_MAP_TEXTURE.getHeight() - 36);
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

                if (cell.lair) {
                    pixmap.setColor(Color.PINK);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 3);
                }

                if (cell.message != null) {
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

                if (cell.itemRequired > 0) {
                    pixmap.setColor(Color.FOREST);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 4);
                }

                if (cell.itemObtained > 0) {
                    pixmap.setColor(Color.LIME);
                    pixmap.fillCircle(x * MINI_DIM + MINI_DIM / 2, y * MINI_DIM + MINI_DIM / 2, 4);
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

                if (cell.summoningCircle1 || cell.summoningCircle2 || cell.summoningCircle3 || cell.summoningCircle4
                        || cell.summoningCircle5 || cell.summoningCircle6 || cell.summoningCircle7
                        || cell.summoningCircle8 || cell.summoningCircle9 || cell.summoningCircle10) {
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
                teleport(cell.addressTo, true);
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

            try {
                move(cell, currentDir, x, y, true);
            } catch (Throwable e) {
                partyDeath();
            }

        }

        finishTurn(x, y);

        return false;
    }

    private void move(MazeCell cell, Direction dir, int dx, int dy, boolean skipProgression) {

        WizardryData.MazeLevel[] levels = this.map.scenario().levels();

        if (levels[currentLevel].cells[dx][dy].teleport) {
            MazeAddress to = levels[currentLevel].cells[dx][dy].addressTo;
            teleport(to, true);
            return;
        }

        if (levels[currentLevel].cells[dx][dy].chute) {
            MazeAddress to = levels[currentLevel].cells[dx][dy].addressTo;
            teleport(to, true);
            return;
        }

        if (levels[currentLevel].cells[dx][dy].itemRequired > 0) {
            Item item = this.map.scenario().items().get(levels[currentLevel].cells[dx][dy].itemRequired);
            CharacterRecord owner = Andius.CTX.getOwner(item);
            if (owner == null) {
                Andius.HUD.log(levels[currentLevel].cells[dx][dy].message.getText(), Color.YELLOW);
                Sounds.play(Sound.NEGATIVE_EFFECT);
                if (levels[currentLevel].cells[dx][dy].addressTo != null) {
                    MazeAddress to = levels[currentLevel].cells[dx][dy].addressTo;
                    teleport(to, true);
                }
                return;
            }
        } else {
            if (levels[currentLevel].cells[dx][dy].message != null && levels[currentLevel].cells[dx][dy].addressTo != null) {
                Andius.HUD.log(levels[currentLevel].cells[dx][dy].message.getText(), Color.YELLOW);
                MazeAddress to = levels[currentLevel].cells[dx][dy].addressTo;
                teleport(to, true);
                return;
            }
        }

        if (levels[currentLevel].cells[dx][dy].riddleAnswers != null && !levels[currentLevel].cells[dx][dy].riddleAnswers.isEmpty()) {
            new RiddleDialog(CTX, this, levels[currentLevel].cells[dx][dy]).show(this.stage);
            return;
        }

        boolean moved = false;

        if (dir == Direction.EAST && (cell.hiddenNorthDoor || !cell.northWall)) {
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

        if (dir == Direction.WEST && (cell.hiddenSouthDoor || !cell.southWall)) {
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

        if (dir == Direction.NORTH && (cell.hiddenWestDoor || !cell.westWall)) {
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

        if (dir == Direction.SOUTH && (cell.hiddenEastDoor || !cell.eastWall)) {
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

            if (levels[currentLevel].cells[dx][dy].tradeItem1 > 0) {
                Item item1 = this.map.scenario().items().get(levels[currentLevel].cells[dx][dy].tradeItem1);
                Item item2 = this.map.scenario().items().get(levels[currentLevel].cells[dx][dy].tradeItem2);
                CharacterRecord owner = Andius.CTX.getOwner(item1);
                if (owner != null) {
                    Andius.HUD.log(levels[currentLevel].cells[dx][dy].message.getText(), Color.GREEN);
                    log(String.format("%s traded %s for %s", owner.name, item1.genericName, item2.genericName));
                    owner.inventory.remove(item1);
                    owner.inventory.add(item2);
                    Sounds.play(Sound.POSITIVE_EFFECT);
                } else {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                }
                showMessage = false;
            }

            if (levels[currentLevel].cells[dx][dy].itemObtained > 0) {
                Item item = this.map.scenario().items().get(levels[currentLevel].cells[dx][dy].itemObtained);
                CharacterRecord owner = Andius.CTX.getOwner(item);
                if (owner == null) {
                    Andius.HUD.log(levels[currentLevel].cells[dx][dy].message.getText(), Color.GREEN);
                    CharacterRecord cr = Andius.CTX.pickRandomEnabledPlayer();
                    log(String.format("%s found a %s", cr.name, item.genericName));
                    cr.inventory.add(item);
                    Sounds.play(Sound.POSITIVE_EFFECT);
                }
                showMessage = false;
            }

            if (levels[currentLevel].cells[dx][dy].riddleAnswers != null && levels[currentLevel].cells[dx][dy].riddleAnswers.isEmpty()) {
                showMessage = false;
            }

            if (levels[currentLevel].cells[dx][dy].itemRequired > 0
                    && Andius.CTX.getOwner(this.map.scenario().items().get(levels[currentLevel].cells[dx][dy].itemRequired)) != null) {
                showMessage = false;
            }

            if (levels[currentLevel].cells[dx][dy].darkness) {
                isTorchOn = false;
            }

            if (showMessage && levels[currentLevel].cells[dx][dy].message != null) {
                Andius.HUD.log(levels[currentLevel].cells[dx][dy].message.getText(), Color.GREEN);
            }

            fight(levels[currentLevel].cells[dx][dy], this.map.scenario().levels()[currentLevel].defeated);

            finishTurn(dx, dy);
        }
    }

    private void fight(MazeCell cell, List<Integer> defeated) {
        if (this.map == Map.WIZARDRY4) {

            int wanderingEncounterID = -1;
            if (cell.lair && cell.encounterID < 0 && Utils.randomBoolean()) {
                wanderingEncounterID = this.map.scenario().levels()[currentLevel].getRandomMonster();
                if (defeated.contains(wanderingEncounterID)) {
                    wanderingEncounterID = -1;
                }
            }

            if ((cell.encounterID != -1 && !defeated.contains(cell.encounterID)) || wanderingEncounterID != -1) {

                int encounterID = cell.encounterID != -1 ? cell.encounterID : wanderingEncounterID;

                DoGooder dogooder = this.map.scenario().characters().get(encounterID);

                List<MutableMonster> mms = new ArrayList<>();
                mms.add(new MutableMonster(PMO_MONSTERS.get(3)));
                mms.add(new MutableMonster(PMO_MONSTERS.get(3)));
                mms.add(new MutableMonster(PMO_MONSTERS.get(3)));
                mms.add(new MutableMonster(PMO_MONSTERS.get(4)));
                mms.add(new MutableMonster(PMO_MONSTERS.get(4)));
                mms.add(new MutableMonster(PMO_MONSTERS.get(4)));
                mms.add(new MutableMonster(PMO_MONSTERS.get(6)));
                mms.add(new MutableMonster(PMO_MONSTERS.get(6)));
                mms.add(new MutableMonster(PMO_MONSTERS.get(6)));

                Wiz4CombatScreen cs = new Wiz4CombatScreen(CTX.saveGame.players[0], mms, dogooder);
                mainGame.setScreen(cs);
            }
        } else {
            boolean wandering = cell.wanderingEncounterID != -1 && (cell.hasTreasureChest || Utils.randomBoolean());
            if (cell.encounterID != -1 || wandering) {
                int encounterID = cell.encounterID != -1 ? cell.encounterID : cell.wanderingEncounterID;
                Monster monster = this.map.scenario().monsters().get(encounterID);
                andius.objects.Actor actor = new andius.objects.Actor(monster.name, null);
                MutableMonster mm = new MutableMonster(monster);
                actor.set(mm, Role.MONSTER, 1, 1, 1, 1, Constants.MovementBehavior.ATTACK_AVATAR);

                TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
                TiledMap tm = loader.load("assets/data/combat1.tmx");
                CombatScreen cs = new CombatScreen(CTX, this.map, tm, actor, currentLevel + 1, false);
                mainGame.setScreen(cs);
            }
        }
    }

    @Override
    public void teleport(int level, int stepsX, int stepsY) {
        int x = (Math.round(currentPos.x) - 1) + stepsX;
        int y = (Math.round(currentPos.z) - 1) + stepsY;
        int z = currentLevel + level + 1;

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
        teleport(new MazeAddress(z, x, y), true);
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

            if (this.map.scenario().levels()[currentLevel].cells[x][y].encounterID > 0) {
                this.map.scenario().levels()[currentLevel].cells[x][y].encounterID = -1;
            } else {
                if (this.map == Map.WIZARDRY4) {
                    DoGooder dg = (DoGooder) opponent;
                    for (int id : dg.partyMembers) {
                        if (!this.map.scenario().levels()[currentLevel].defeated.contains(id)) {
                            this.map.scenario().levels()[currentLevel].defeated.add(id);
                        }
                    }
                }
            }

            List<String> removedMonsters = CTX.saveGame.removedActors.get(this.map);
            if (removedMonsters == null) {
                removedMonsters = new ArrayList<>();
                CTX.saveGame.removedActors.put(this.map, removedMonsters);
            }

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
