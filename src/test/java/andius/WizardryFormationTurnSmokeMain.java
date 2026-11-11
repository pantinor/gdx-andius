package andius;

import andius.objects.ClassType;
import andius.objects.MutableMonster;
import andius.objects.SaveGame;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ScreenUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** Real turns with monster 12; deterministic casualties exercise the fade/reflow boundary. */
public class WizardryFormationTurnSmokeMain extends Game {
    private WizardryCombatScreen combat;
    private int frames;

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Wizardry monster 12 formation check";
        config.width = 1024;
        config.height = 768;
        config.forceExit = false;
        new LwjglApplication(new WizardryFormationTurnSmokeMain(), config);
    }

    @Override public void create() {
        try {
            new Andius().create();
            Context context = new Context();
            context.setSaveGame(new SaveGame());
            context.saveGame.players = new SaveGame.CharacterRecord[6];
            for (int i = 0; i < 6; i++) {
                context.saveGame.players[i] = SaveGame.generatePlayer(30, ClassType.FIGHTER, "Fighter " + (i + 1));
            }
            Andius.CTX = context;
            var coin = WizardryData.PMO_MONSTERS.get(12);
            combat = new WizardryCombatScreen(context, Constants.Map.WIZARDRY1, coin.name, coin, 2, false, null, null);
            while (combat.monsters.size() > 30) combat.removeMonster(combat.monsters.get(combat.monsters.size() - 1));
            while (combat.monsters.size() < 30) combat.addMonster(new MutableMonster(coin));
            setScreen(combat);
        } catch (Throwable failure) { fail(failure); }
    }

    @Override public void render() {
        super.render();
        if (++frames != 30) return;
        try {
            combat.render(0);
            List<ModelInstance> fixedRoom = new ArrayList<>((List<ModelInstance>) field(combat, "wallAndFloor"));
            List<com.badlogic.gdx.math.Matrix4> roomTransforms = fixedRoom.stream()
                    .map(m -> new com.badlogic.gdx.math.Matrix4(m.transform)).toList();
            check(combat.monsters.size() == 30, "Start with 30 creeping coins");
            checkFormation();
            snapshot("target/monster12-30-targets.png");
            PerspectiveCamera camera = field(combat, "camera");
            Vector3 center = field(combat, "sceneCenter");
            float initialDistance = camera.position.dst(center);
            for (int remaining : new int[] {20, 12, 6}) {
                List<MutableMonster> living = combat.monsters.stream().filter(m -> !m.isDead()).toList();
                for (int i = remaining; i < living.size(); i++) {
                    MutableMonster monster = living.get(i);
                    monster.adjustHitPoints(-monster.getCurrentHitPoints());
                    monster.adjustHealthCursor();
                }
                Vector3 before = new Vector3(camera.position);
                combat.fight();
                combat.render(.5f);
                check(camera.position.epsilonEquals(before, .0001f), "Camera waits while death models fade");
                combat.render(.6f);
                combat.render(0);
                check(!(boolean) field(combat, "formationDirty"), "Reflow completes after all fade callbacks");
                checkFormation();
                long alive = combat.monsters.stream().filter(m -> !m.isDead()).count();
                System.out.println("Turn complete: " + alive + " surviving coins, camera distance " + camera.position.dst(center));
                snapshot("target/monster12-after-" + remaining + ".png");
            }
            check(camera.position.dst(center) < initialDistance * .8f, "Camera closes in as the formation shrinks");
            List<MutableMonster> finalSurvivors = combat.monsters.stream().filter(m -> !m.isDead()).toList();
            for (int i = 1; i < finalSurvivors.size(); i++) {
                finalSurvivors.get(i).adjustHitPoints(-finalSurvivors.get(i).getCurrentHitPoints());
                finalSurvivors.get(i).adjustHealthCursor();
            }
            combat.render(1.1f);
            combat.render(0);
            checkFormation();
            check(camera.position.dst(center) >= 1.19f, "Lone survivor is not framed too closely");
            snapshot("target/monster12-one-survivor.png");
            System.out.println("PASS: 30 monster-12 targets, fade completion before reflow, three real turns, living-only formation and closer camera");
            Gdx.app.exit();
        } catch (Throwable failure) { fail(failure); }
    }

    private void checkFormation() throws Exception {
        List<?> listings = field(combat, "monsterOrderedList");
        List<BoundingBox> occupied = new ArrayList<>();
        PerspectiveCamera camera = field(combat, "camera");
        for (Object listing : listings) {
            MutableMonster monster = field(listing, "mm");
            ModelInstance instance = field(field(listing, "health"), "modelInstance");
            if (monster.isDead()) {
                check(instance == null, "Dead creatures disappear before reflow");
                continue;
            }
            check(instance != null, "Living creature keeps its model");
            BoundingBox box = instance.calculateBoundingBox(new BoundingBox()).mul(instance.transform);
            for (BoundingBox previous : occupied) {
                check(!(box.min.x < previous.max.x && box.max.x > previous.min.x
                        && box.min.z < previous.max.z && box.max.z > previous.min.z), "Survivors remain separated");
            }
            occupied.add(box);
            for (int corner = 0; corner < 8; corner++) {
                Vector3 point = new Vector3((corner & 1) == 0 ? box.min.x : box.max.x,
                        (corner & 2) == 0 ? box.min.y : box.max.y, (corner & 4) == 0 ? box.min.z : box.max.z);
                camera.project(point, WizardryCombatScreen.SCENE_X, WizardryCombatScreen.SCENE_Y,
                        WizardryCombatScreen.SCENE_WIDTH, WizardryCombatScreen.SCENE_HEIGHT);
                check(point.x >= WizardryCombatScreen.SCENE_X && point.x <= WizardryCombatScreen.SCENE_X + WizardryCombatScreen.SCENE_WIDTH
                        && point.y >= WizardryCombatScreen.SCENE_Y && point.y <= WizardryCombatScreen.SCENE_Y + WizardryCombatScreen.SCENE_HEIGHT,
                        "All surviving model corners fit in the viewport");
            }
        }
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
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static void fail(Throwable failure) { failure.printStackTrace(); System.exit(1); }
    @Override public void dispose() { if (combat != null) combat.dispose(); }
}
