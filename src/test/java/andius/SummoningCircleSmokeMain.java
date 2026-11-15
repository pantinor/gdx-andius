package andius;

import andius.WizardryData.SummoningCircle;
import andius.objects.ClassType;
import andius.objects.Dice;
import andius.objects.Monster;
import andius.objects.MonsterModels;
import andius.objects.MutableMonster;
import andius.objects.SaveGame;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.utils.ScreenUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import utils.DungeonRoom;

/** Exercises the summoning controls and actual 3D scene in an OpenGL window. */
public class SummoningCircleSmokeMain extends Game {
    private SummoningCircleScreen summoning;
    private Stage stage;
    private int frames;
    private List<Matrix4> staticTransforms;
    private MutableMonster legacySummon;
    private Monster legacySnapshot, legacyCanonical;
    private int legacyArmourClass;

    public static void main(String[] args) {
        var config = new LwjglApplicationConfiguration();
        config.title = "Summoning circle 3D smoke check";
        config.width = 1024;
        config.height = 768;
        config.forceExit = false;
        new LwjglApplication(new SummoningCircleSmokeMain(), config);
    }

    @Override public void create() {
        try {
            new Andius().create();
            createCircle(0);
        } catch (Throwable error) { fail(error); }
    }

    private void createCircle(int existingCount) throws Exception {
        var player = SaveGame.generatePlayer(10, ClassType.MAGE, "Werdna");
        int[] icons = {MonsterModels.DRAGON.ordinal(), MonsterModels.SLIME_SLUG.ordinal(),
                MonsterModels.ROBES_MAN.ordinal()};
        for (int i = 0; i < existingCount; i++) {
            player.summonedMonsters.add(new MutableMonster(monsterWithIcon(icons[i % icons.length])));
        }
        openCircle(player);
    }

    private void openCircle(SaveGame.CharacterRecord player) throws Exception {
        SummoningCircleScreen previous = summoning;
        summoning = new SummoningCircleScreen(player, SummoningCircle.CIRCLE10);
        stage = field(summoning, "stage");
        setScreen(summoning);
        if (previous != null) {
            check((boolean) field(previous, "disposed"), "Leaving the previous circle releases its owned resources");
            previous.dispose(); // Calling dispose again must be harmless.
        }
    }

    private void createLegacyCircle() throws Exception {
        var player = SaveGame.generatePlayer(10, ClassType.MAGE, "Werdna");
        legacyCanonical = monsterWithIcon(MonsterModels.SKELETON.ordinal());
        legacySnapshot = new Monster();
        legacySnapshot.clone(legacyCanonical);
        legacySnapshot.monsterId = legacyCanonical.monsterId;
        legacySnapshot.iconId = 0;
        legacySnapshot.armourClass -= 4;
        legacyArmourClass = legacySnapshot.armourClass;
        legacySnapshot.hitPoints = new Dice(0, 0, 19);
        legacySummon = new MutableMonster(legacySnapshot);
        legacySummon.adjustHitPoints(-7);
        player.summonedMonsters.add(legacySummon);
        openCircle(player);
    }

    private static Monster monsterWithIcon(int icon) {
        return WizardryData.WER_MONSTERS.stream().filter(monster -> monster.getIconId() == icon)
                .findFirst().orElseThrow(() -> new AssertionError("No WER monster for icon " + icon));
    }

    private void choose(String selector, int icon) throws Exception {
        SelectBox<?> select = field(summoning, selector);
        for (int i = 0; i < select.getItems().size; i++) {
            Monster monster = field(select.getItems().get(i), "m");
            if (monster != null && monster.getIconId() == icon) {
                select.setSelectedIndex(i);
                return;
            }
        }
        throw new AssertionError("Dropdown has no monster for icon " + icon);
    }

    private void chooseMixedGroups() throws Exception {
        choose("select1", MonsterModels.DRAGON.ordinal());
        choose("select2", MonsterModels.SLIME_SLUG.ordinal());
        choose("select3", MonsterModels.ROBES_MAN.ordinal());
    }

    @Override public void render() {
        try {
            super.render();
            switch (++frames) {
                case 10 -> {
                    check(models().isEmpty(), "Fresh circle has no summoned models");
                    verifyScene();
                    snapshot("target/summoning-empty.png");
                    chooseMixedGroups();
                    click(field(summoning, "summon"));
                }
                case 20 -> {
                    verifyRolledGroups();
                    verifyScene();
                    snapshot("target/summoning-mixed.png");
                    staticTransforms = models().stream().map(model -> new Matrix4(model.transform)).toList();
                }
                case 30 -> {
                    for (int i = 0; i < models().size(); i++) {
                        check(java.util.Arrays.equals(staticTransforms.get(i).val, models().get(i).transform.val),
                                "Summoning view stays static");
                    }
                    List<MutableMonster> previous = new ArrayList<>(summoning.player.summonedMonsters);
                    ((SelectBox<?>) field(summoning, "select1")).setSelectedIndex(0);
                    click(field(summoning, "summon"));
                    check(summoning.player.summonedMonsters.equals(previous), "Divider selection preserves existing summons");
                    chooseMixedGroups();
                    click(field(summoning, "summon"));
                    check(!summoning.player.summonedMonsters.contains(previous.get(0)), "Resummoning replaces existing units");
                }
                case 40 -> {
                    verifyRolledGroups();
                    verifyScene();
                    snapshot("target/summoning-resummoned.png");
                    createCircle(24);
                }
                case 50 -> {
                    check(models().size() == 24, "Reopening circle displays all existing summons");
                    verifyScene();
                    snapshot("target/summoning-crowded.png");
                    Gdx.graphics.setWindowedMode(1280, 720);
                }
                case 60 -> {
                    check(stage.getViewport().getScreenWidth() == 960 && stage.getViewport().getScreenHeight() == 720,
                            "Resized controls use a centered 4:3 viewport");
                    verifyScene();
                    snapshot("target/summoning-resized.png");
                    chooseMixedGroups();
                    click(field(summoning, "summon"));
                }
                case 70 -> {
                    verifyRolledGroups();
                    verifyScene();
                    createLegacyCircle();
                }
                case 80 -> {
                    check(summoning.player.summonedMonsters.get(0) == legacySummon
                            && legacySummon.baseType() == legacySnapshot, "Legacy saved summon objects are preserved");
                    check(legacySnapshot.iconId == 0 && legacySnapshot.monsterId == legacyCanonical.monsterId
                            && legacySnapshot.armourClass == legacyArmourClass,
                            "Choosing a canonical model does not mutate saved monster stats");
                    check(legacySummon.getCurrentHitPoints() == 12 && legacySummon.getMaxHitPoints() == 19,
                            "Rendering a saved summon preserves its current and maximum HP");
                    verifyScene();
                    snapshot("target/summoning-legacy-snapshot.png");
                    System.out.println("PASS: empty circle, mixed 3D model selection, actual summon clicks, divider guard, "
                            + "resummoning, static camera, grounded nonoverlapping formation, centered pentagram, bounded dungeon room, "
                            + "lower camera angle and larger model framing, 24 existing summons, resized input, "
                            + "resource disposal, and stale saved model IDs");
                    Gdx.app.exit();
                }
                default -> { }
            }
        } catch (Throwable error) { fail(error); }
    }

    private void verifyRolledGroups() throws Exception {
        int minimum = 0, maximum = 0;
        for (String name : new String[] {"select1", "select2", "select3"}) {
            SelectBox<?> select = field(summoning, name);
            Monster monster = field(select.getSelected(), "m");
            minimum += monster.getGroupSize().min();
            maximum += monster.getGroupSize().max();
        }
        check(models().size() >= minimum && models().size() <= maximum, "Summoned count follows group rolls");
        check(summoning.player.summonedMonsters.stream().map(MutableMonster::icon).distinct().count() == 3,
                "Three different monster models are summoned");
    }

    private void verifyScene() throws Exception {
        List<ModelInstance> models = models();
        check(models.size() == summoning.player.summonedMonsters.size(), "Every summoned unit has a 3D model");
        PerspectiveCamera camera = field(summoning, "camera");
        float pitch = (float) Math.toDegrees(Math.asin(-camera.direction.y));
        check(pitch >= 20 && pitch <= 30, "Camera uses the lower downward viewing angle");
        ModelInstance pentagram = field(summoning, "pentagram");
        DungeonRoom.Layout room = field(summoning, "room");
        check(!room.floors().isEmpty(), "Summoning dungeon has a floor");
        check(Float.isFinite(room.left()) && Float.isFinite(room.right()) && Float.isFinite(room.back())
                && Float.isFinite(room.front()) && room.left() < room.right() && room.back() < room.front(),
                "Dungeon room has finite boundaries");
        BoundingBox pentagramBounds = worldBounds(pentagram);
        float floorTop = worldBounds(room.floors().get(0)).max.y;
        check(room.bounds().max.y > floorTop + .3f && room.instances().size() > room.floors().size() + 1,
                "Dungeon room includes walls above its floor");
        check(room.instances().stream().anyMatch(SummoningCircleSmokeMain::hasDoorWood),
                "Dungeon room includes the combat room's wooden door");
        for (ModelInstance tile : room.floors()) {
            BoundingBox tileBounds = worldBounds(tile);
            check(insideRoom(tileBounds, room), "Floor tiles stay within the bounded dungeon room");
        }
        check(pentagramBounds.getWidth() > 0 && pentagramBounds.getDepth() > 0
                && Math.abs(pentagramBounds.max.y - floorTop) < .025f,
                "The 3D pentagram rests on the dungeon floor");
        if (models.isEmpty()) verifyInView(pentagramBounds, camera);
        List<BoundingBox> placed = new ArrayList<>();
        BoundingBox formationBounds = new BoundingBox().inf();
        float[] projectedFormation = {Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
        for (int i = 0; i < models.size(); i++) {
            ModelInstance model = models.get(i);
            MutableMonster unit = summoning.player.summonedMonsters.get(i);
            int expectedIcon = unit == legacySummon ? legacyCanonical.getIconId() : unit.icon();
            check(model.model == MonsterModels.values()[expectedIcon].model(),
                    "Summoned unit uses its own monster model");
            BoundingBox bounds = worldBounds(model);
            check(Math.abs(bounds.min.y - floorTop) < .025f, "Monster feet are grounded above the floor");
            check(insideRoom(bounds, room), "Summoned formation stays inside the dungeon walls");
            formationBounds.ext(bounds);
            for (BoundingBox previous : placed) {
                boolean overlapX = bounds.min.x < previous.max.x - .0001f && bounds.max.x > previous.min.x + .0001f;
                boolean overlapZ = bounds.min.z < previous.max.z - .0001f && bounds.max.z > previous.min.z + .0001f;
                check(!(overlapX && overlapZ), "Model footprints are separated");
            }
            placed.add(bounds);
            float[] projected = verifyInView(bounds, camera);
            projectedFormation[0] = Math.min(projectedFormation[0], projected[0]);
            projectedFormation[1] = Math.min(projectedFormation[1], projected[1]);
            projectedFormation[2] = Math.max(projectedFormation[2], projected[2]);
            projectedFormation[3] = Math.max(projectedFormation[3], projected[3]);
        }
        if (!models.isEmpty()) {
            Vector3 formationCenter = formationBounds.getCenter(new Vector3());
            Vector3 pentagramCenter = pentagramBounds.getCenter(new Vector3());
            check(Math.abs(formationCenter.x - pentagramCenter.x) < .001f
                    && Math.abs(formationCenter.z - pentagramCenter.z) < .001f,
                    "Pentagram is centered beneath the formation");
            check(projectedFormation[2] - projectedFormation[0] >= camera.viewportWidth * .65f
                    || projectedFormation[3] - projectedFormation[1] >= camera.viewportHeight * .65f,
                    "Summoned models fill at least 65 percent of the view's width or height");
        }
    }

    private static boolean insideRoom(BoundingBox bounds, DungeonRoom.Layout room) {
        return bounds.min.x >= room.left() - .001f && bounds.max.x <= room.right() + .001f
                && bounds.min.z >= room.back() - .001f && bounds.max.z <= room.front() + .001f;
    }

    private static boolean hasDoorWood(ModelInstance instance) {
        for (var node : instance.nodes) {
            for (var part : node.parts) {
                if (part.meshPart.id.contains("wood")) return true;
            }
        }
        return false;
    }

    private static float[] verifyInView(BoundingBox bounds, PerspectiveCamera camera) {
        float[] extent = {Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
        for (int corner = 0; corner < 8; corner++) {
            Vector3 point = new Vector3((corner & 1) == 0 ? bounds.min.x : bounds.max.x,
                    (corner & 2) == 0 ? bounds.min.y : bounds.max.y,
                    (corner & 4) == 0 ? bounds.min.z : bounds.max.z);
            camera.project(point, 0, 0, camera.viewportWidth, camera.viewportHeight);
            check(point.x >= 0 && point.x <= camera.viewportWidth && point.y >= 0 && point.y <= camera.viewportHeight
                    && point.z >= 0 && point.z <= 1, "Model or empty-circle pentagram fits inside camera");
            extent[0] = Math.min(extent[0], point.x);
            extent[1] = Math.min(extent[1], point.y);
            extent[2] = Math.max(extent[2], point.x);
            extent[3] = Math.max(extent[3], point.y);
        }
        return extent;
    }

    private static BoundingBox worldBounds(ModelInstance instance) {
        return instance.calculateBoundingBox(new BoundingBox()).mul(instance.transform);
    }

    private List<ModelInstance> models() throws Exception { return field(summoning, "summonedModels"); }

    private void click(Actor actor) {
        Vector2 position = actor.localToStageCoordinates(new Vector2(actor.getWidth() / 2, actor.getHeight() / 2));
        stage.stageToScreenCoordinates(position);
        stage.touchDown(Math.round(position.x), Math.round(position.y), 0, Input.Buttons.LEFT);
        stage.touchUp(Math.round(position.x), Math.round(position.y), 0, Input.Buttons.LEFT);
        stage.act(0);
    }

    private static void snapshot(String path) {
        Pixmap pixels = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        PixmapIO.writePNG(Gdx.files.local(path), pixels, -1, true);
        pixels.dispose();
    }

    @SuppressWarnings("unchecked") private static <T> T field(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(object);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void fail(Throwable error) { error.printStackTrace(); System.exit(1); }
    @Override public void dispose() { if (summoning != null) summoning.dispose(); }
}
