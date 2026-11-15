package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import andius.WizardryData.SummoningCircle;
import andius.objects.Monster;
import andius.objects.MonsterModels;
import andius.objects.MutableMonster;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Spells;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.ArrayList;
import java.util.List;
import utils.CombatFormation;
import utils.ObjLoader;
import utils.Utils;
import utils.ManagementUi;
import static utils.SkinUtils.GOLD;
import static utils.SkinUtils.INK;

public class SummoningCircleScreen implements Screen, Constants {

    static final int SCENE_X = 16, SCENE_Y = 16, SCENE_WIDTH = 992, SCENE_HEIGHT = 512;
    private static final float CAMERA_PITCH = 28;
    private static final float MONSTER_Y = .014f;
    private final ManagementUi ui = new ManagementUi();
    private final Stage stage;
    public final CharacterRecord player;
    private final SummoningCircle summoningId;
    private final SelectBox<MonsterItem> select1, select2, select3;
    private final TextButton summon;
    private final TextButton exit;
    private final PerspectiveCamera camera = new PerspectiveCamera(40, SCENE_WIDTH, SCENE_HEIGHT);
    private final Environment environment = new Environment();
    private final ModelBatch modelBatch;
    private final Model pentagramModel, floorModel;
    private final ModelInstance pentagram;
    private final List<ModelInstance> floorTiles = new ArrayList<>();
    private final List<ModelInstance> summonedModels = new ArrayList<>();
    private boolean disposed;

    public SummoningCircleScreen(CharacterRecord player, SummoningCircle summoningId) {
        this.summoningId = summoningId;
        this.player = player;
        this.stage = new Stage(new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT));

        DefaultShader.Config config = new DefaultShader.Config();
        config.vertexShader = Gdx.files.internal("assets/dungeon.vertex.glsl").readString();
        config.fragmentShader = Gdx.files.internal("assets/dungeon.fragment.glsl").readString();
        config.numDirectionalLights = 2;
        config.numPointLights = 0;
        config.numSpotLights = 0;

        this.modelBatch = new ModelBatch(new DefaultShaderProvider(config));

        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, .32f, .36f, .40f, 1));
        environment.add(new DirectionalLight().set(.85f, .88f, .90f, -.4f, -1, -.25f));
        environment.add(new DirectionalLight().set(.40f, .30f, .22f, .5f, -.5f, .7f));

        this.pentagramModel = ObjLoader.loadModel("assets/graphics/pentagram.obj", "pentagram", .1f);
        this.floorModel = ObjLoader.loadModel("assets/graphics/dungeon-walls.obj", "wall-floor", .1f);
        this.pentagram = new ModelInstance(pentagramModel);

        if (!player.summoningCircles.contains(summoningId)) {
            player.summoningCircles.add(summoningId);
            player.level = summoningId.ordinal() + 1;
            player.str = Utils.adjustValueMax(player.str, 1, 18);
            player.intell = Utils.adjustValueMax(player.intell, 1, 18);
            player.piety = Utils.adjustValueMax(player.piety, 1, 18);
            player.vitality = Utils.adjustValueMax(player.vitality, 1, 18);
            player.agility = Utils.adjustValueMax(player.agility, 1, 18);
            player.luck = Utils.adjustValueMax(player.luck, 1, 18);
            player.maxhp = player.level * 10;
            for (int i = 0; i <= 6; i++) {
                if (i + 1 > player.level) {
                    break;
                }
                player.magePoints[i] = 9;
            }
            player.knownSpells.clear();
            if (player.magePoints[0] > 0) {
                for (int i = 1; i <= 4; i++) {
                    player.knownSpells.add(Spells.values()[i - 1]);
                }
            }
            if (player.magePoints[1] > 0) {
                for (int i = 5; i <= 6; i++) {
                    player.knownSpells.add(Spells.values()[i - 1]);
                }
            }
            if (player.magePoints[2] > 0) {
                for (int i = 7; i <= 8; i++) {
                    player.knownSpells.add(Spells.values()[i - 1]);
                }
            }
            if (player.magePoints[3] > 0) {
                for (int i = 9; i <= 11; i++) {
                    player.knownSpells.add(Spells.values()[i - 1]);
                }
            }
            if (player.magePoints[4] > 0) {
                for (int i = 12; i <= 14; i++) {
                    player.knownSpells.add(Spells.values()[i - 1]);
                }
            }
            if (player.magePoints[5] > 0) {
                for (int i = 15; i <= 18; i++) {
                    player.knownSpells.add(Spells.values()[i - 1]);
                }
            }
            if (player.magePoints[6] > 0) {
                for (int i = 19; i <= 21; i++) {
                    player.knownSpells.add(Spells.values()[i - 1]);
                }
            }
        }

        for (int i = 0; i <= 6; i++) {
            if (i + 1 > player.level) {
                break;
            }
            player.magePoints[i] = 9;
        }

        this.player.adjustHP(this.player.maxhp);

        Array<MonsterItem> arr = new Array<>();
        for (int j = SummoningCircle.values().length - 1; j >= 0; j--) {
            SummoningCircle sc = SummoningCircle.values()[j];
            if (sc.ordinal() <= this.summoningId.ordinal()) {
                arr.add(new MonsterItem("--- " + sc.display() + " Level"));
                for (int i = sc.low(); i <= sc.high(); i++) {
                    arr.add(new MonsterItem(WizardryData.Scenario.WER.monsters().get(i)));
                }
            }
        }

        select1 = new SelectBox<>(ui.skin);
        select2 = new SelectBox<>(ui.skin);
        select3 = new SelectBox<>(ui.skin);

        select1.setItems(arr);
        select1.setSelected(arr.first());
        select2.setItems(arr);
        select2.setSelected(arr.first());
        select3.setItems(arr);
        select3.setSelected(arr.first());

        this.summon = new TextButton("Summon", ui.skin);
        this.summon.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                summon();
            }
        });

        this.exit = new TextButton("Leave", ui.skin);
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(Map.WIZARDRY4.getScreen());
            }
        });
        caption("The " + this.summoningId.display() + " Level Pentagram Glows...", 700);
        caption("and the Gate Opens!", 662);
        caption("Call Forth 3 Groups of Monsters!", 608);

        this.select1.setBounds(24, 548, 228, 38);
        this.select2.setBounds(264, 548, 228, 38);
        this.select3.setBounds(504, 548, 228, 38);
        this.summon.setBounds(744, 548, 132, 38);
        this.exit.setBounds(888, 548, 112, 38);

        this.stage.addActor(this.select1);
        this.stage.addActor(this.select2);
        this.stage.addActor(this.select3);

        ui.primary(summon);
        this.stage.addActor(summon);
        this.stage.addActor(exit);

        rebuildScene();

    }

    private void caption(String text, float y) {
        Label label = ui.label(stage, text, "default-24", GOLD, 24, y, SCREEN_WIDTH - 48, 36);
        label.setAlignment(Align.center);
    }

    public void summon() {

        Monster m1 = this.select1.getSelected().m;
        Monster m2 = this.select2.getSelected().m;
        Monster m3 = this.select3.getSelected().m;

        if (m1 == null || m2 == null || m3 == null) {
            return;
        }

        this.player.summonedMonsters.clear();

        Sounds.play(Sound.SPIRITS);

        int roll1 = m1.getGroupSize().roll();
        int roll2 = m2.getGroupSize().roll();
        int roll3 = m3.getGroupSize().roll();

        for (int i = 1; i <= roll1; i++) {
            this.player.summonedMonsters.add(new MutableMonster(m1));
        }

        for (int i = 1; i <= roll2; i++) {
            this.player.summonedMonsters.add(new MutableMonster(m2));
        }

        for (int i = 1; i <= roll3; i++) {
            this.player.summonedMonsters.add(new MutableMonster(m3));
        }

        rebuildScene();

    }

    private void rebuildScene() {
        summonedModels.clear();
        List<BoundingBox> footprints = new ArrayList<>();
        List<Monster> types = WizardryData.Scenario.WER.monsters();
        for (MutableMonster monster : player.summonedMonsters) {
            int id = monster.monster().getMonsterId();
            int icon = id >= 0 && id < types.size() ? types.get(id).getIconId() : monster.icon();
            ModelInstance instance = new ModelInstance(MonsterModels.values()[icon].model());
            BoundingBox bounds = instance.calculateBoundingBox(new BoundingBox());
            float groundedY = MONSTER_Y - bounds.min.y;
            instance.transform.setToTranslation(0, groundedY, 0);
            bounds.min.y += groundedY;
            bounds.max.y += groundedY;
            summonedModels.add(instance);
            footprints.add(bounds);
        }

        CombatFormation.Layout formation = CombatFormation.layout(footprints, (float) SCENE_WIDTH / SCENE_HEIGHT, camera.fieldOfView, .1f, CAMERA_PITCH);
        Vector3 translation = new Vector3();
        for (int i = 0; i < summonedModels.size(); i++) {
            ModelInstance instance = summonedModels.get(i);
            instance.transform.getTranslation(translation);
            Vector3 position = formation.positions().get(i);
            instance.transform.setTranslation(position.x, translation.y, position.z);
        }

        BoundingBox scene = new BoundingBox(formation.bounds());
        Vector3 center = scene.getCenter(new Vector3());
        float diameter = Math.max(2, (float) Math.hypot(scene.getWidth(), scene.getDepth()) + .5f);
        
        BoundingBox sigil = pentagram.calculateBoundingBox(new BoundingBox());
        float scale = 1;//diameter / Math.min(sigil.getWidth(), sigil.getDepth());
        pentagram.transform.setToScaling(scale, 1, scale);
        pentagram.transform.setTranslation(
                center.x - (sigil.min.x + sigil.max.x) / 2 * scale,
                .008f - sigil.max.y, 
                center.z - (sigil.min.z + sigil.max.z) / 2 * scale);
        sigil.mul(pentagram.transform);
        scene.ext(sigil);

        BoundingBox framing = new BoundingBox(summonedModels.isEmpty() ? scene : formation.bounds());
        Vector3 margin = framing.getDimensions(new Vector3()).scl(.04f);
        framing.min.sub(margin);
        framing.max.add(margin);
        
        CombatFormation.Layout fitted = CombatFormation.fitBounds(framing, (float) SCENE_WIDTH / SCENE_HEIGHT, camera.fieldOfView, .1f, CAMERA_PITCH);
        camera.position.set(fitted.cameraPosition());
        camera.up.set(Vector3.Y);
        camera.lookAt(fitted.cameraTarget());
        camera.near = .1f;
        camera.far = Math.max(100, camera.position.dst(fitted.cameraTarget()) + diameter * 3);
        camera.update();
        
        buildFloor(scene);
    }

    private void buildFloor(BoundingBox scene) {
        floorTiles.clear();
        BoundingBox tile = new ModelInstance(floorModel).calculateBoundingBox(new BoundingBox());
        float width = tile.getWidth(), depth = tile.getDepth();
        // Extend past the view's ground intersections so the static scene has no exposed edges.
        float reach = camera.position.dst(scene.getCenter(new Vector3())) + scene.getWidth() + scene.getDepth();
        float centerX = (scene.min.x + scene.max.x) / 2;
        float centerZ = (scene.min.z + scene.max.z) / 2;
        for (Vector3 corner : camera.frustum.planePoints) {
            float dy = corner.y - camera.position.y;
            if (dy < 0) {
                float distance = -camera.position.y / dy;
                float x = camera.position.x + (corner.x - camera.position.x) * distance;
                float z = camera.position.z + (corner.z - camera.position.z) * distance;
                reach = Math.max(reach, Math.max(Math.abs(x - centerX), Math.abs(z - centerZ)) + 1);
            }
        }
        float tileScale = Math.max(1, reach / 12);
        width *= tileScale;
        depth *= tileScale;
        for (float x = centerX - reach; x < centerX + reach; x += width) {
            for (float z = centerZ - reach; z < centerZ + reach; z += depth) {
                ModelInstance instance = new ModelInstance(floorModel);
                instance.transform.setToScaling(tileScale, 1, tileScale);
                instance.transform.setTranslation(x - tile.min.x * tileScale, -tile.max.y, z - tile.min.z * tileScale);
                floorTiles.add(instance);
            }
        }
    }

    @Override
    public void show() {
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        stage.getViewport().apply();
        Gdx.gl.glClearColor(INK.r, INK.g, INK.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        stage.act(delta);

        Vector2 origin = stage.stageToScreenCoordinates(new Vector2(SCENE_X, SCENE_Y));
        Vector2 corner = stage.stageToScreenCoordinates(new Vector2(SCENE_X + SCENE_WIDTH, SCENE_Y + SCENE_HEIGHT));
        HdpiUtils.glViewport(Math.round(origin.x), Gdx.graphics.getHeight() - Math.round(origin.y),
                Math.round(corner.x - origin.x), Math.round(origin.y - corner.y));
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);
        modelBatch.begin(camera);
        modelBatch.render(floorTiles, environment);
        modelBatch.render(pentagram, environment);
        modelBatch.render(summonedModels, environment);
        modelBatch.end();

        stage.getViewport().apply();
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            mainGame.setScreen(Map.WIZARDRY4.getScreen());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            summon();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        // Dungeon entry creates a fresh summoning screen for each visit.
        dispose();
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        stage.dispose();
        modelBatch.dispose();
        pentagramModel.dispose();
        floorModel.dispose();
        ui.dispose();
    }

    private class MonsterItem {

        private final Monster m;
        private String dividerLabel;

        public MonsterItem(Monster m) {
            this.m = m;
        }

        public MonsterItem(String label) {
            this.dividerLabel = label;
            this.m = null;
        }

        @Override
        public String toString() {
            if (this.m != null) {
                return this.m.name.toUpperCase();
            } else {
                return dividerLabel;
            }
        }

    }

}
