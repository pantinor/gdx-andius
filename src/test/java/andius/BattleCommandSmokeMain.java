package andius;

import andius.objects.ClassType;
import andius.objects.MutableMonster;
import andius.objects.SaveGame;
import andius.objects.Spells;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import java.lang.reflect.Field;

/** Runs the real OpenGL UI without sending desktop mouse or keyboard input. */
public class BattleCommandSmokeMain extends Game {
    private WizardryCombatScreen combat;
    private Stage stage;
    private int frames;

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Battle Command smoke check";
        cfg.width = 1024;
        cfg.height = 768;
        cfg.forceExit = false;
        new LwjglApplication(new BattleCommandSmokeMain(), cfg);
    }

    @Override public void create() {
        try {
            new Andius().create();
            Andius.playMusic = false;
            Context context = new Context();
            context.setSaveGame(new SaveGame());
            context.saveGame.players = new SaveGame.CharacterRecord[] {
                SaveGame.generatePlayer(10, ClassType.FIGHTER, "Fred"),
                SaveGame.generatePlayer(10, ClassType.FIGHTER, "Sam"),
                SaveGame.generatePlayer(10, ClassType.NINJA, "Jack"),
                SaveGame.generatePlayer(10, ClassType.PRIEST, "Joe"),
                SaveGame.generatePlayer(10, ClassType.MAGE, "Jane"),
                SaveGame.generatePlayer(10, ClassType.THIEF, "Frank")
            };
            context.players()[3].knownSpells.add(Spells.DIOS);
            Andius.CTX = context;
            var opponent = WizardryData.PMO_MONSTERS.get(19);
            combat = new WizardryCombatScreen(context, Constants.Map.WIZARDRY1,
                    opponent.name, opponent, 1, true, null, null);
            stage = field(combat, "stage");
            setScreen(combat);
        } catch (Throwable failure) {
            fail(failure);
        }
    }

    @Override public void render() {
        super.render();
        if (++frames != 30) return;
        try {
            Gdx.input.getInputProcessor().keyDown(Input.Keys.NUM_4);
            Actor selected = field(combat, "selectedPlayer");
            check((int) field(selected.getParent(), "index") == 3, "Keyboard party selection");
            Table drawer = field(combat, "abilityDrawer");
            check(!drawer.isVisible(), "Ability popover starts closed");
            click(field(combat, "abilities"));
            check(drawer.isVisible(), "Ability popover opens");
            com.badlogic.gdx.scenes.scene2d.ui.List<?> spells = field(combat, "spellsList");
            int dios = -1;
            for (int i = 0; i < spells.getItems().size; i++) {
                if (field(spells.getItems().get(i), "spell") == Spells.DIOS) { dios = i; break; }
            }
            check(dios >= 0, "Healing spell available");
            spells.setSelectedIndex(dios);
            for (EventListener listener : spells.getListeners()) {
                if (listener instanceof ClickListener click) click.clicked(new InputEvent(), 0, 0);
            }
            check(combat.actions.get(3).spell == Spells.DIOS, "Spell order assigned");
            check(!drawer.isVisible(), "Choosing a spell closes popover");
            Table party = field(combat, "playersTable");
            click(party.getChildren().first());
            check(combat.actions.get(3).playerTarget == combat.ctx.players()[0], "Ally target assigned");
            click(field(combat, "attack"));
            check(combat.actions.get(3).spell == null && combat.actions.get(3).playerTarget == null,
                    "Melee clears spell and ally target");
            Table targets = field(combat, "monstersTable");
            click(targets.getChildren().first());
            check(combat.actions.get(3).monsterTarget != null, "Enemy target assigned");
            click(field(combat, "reset"));
            check(combat.actions.stream().allMatch(a -> a.spell == null && a.monsterTarget == null), "Clear orders");
            Object targetCard = targets.getChildren().first();
            Object health = field(targetCard, "health");
            ModelInstance model = field(health, "modelInstance");
            PerspectiveCamera camera = field(combat, "camera");
            Vector3 position = model.calculateBoundingBox(new BoundingBox()).getCenter(new Vector3()).mul(model.transform);
            camera.project(position, WizardryCombatScreen.SCENE_X, WizardryCombatScreen.SCENE_Y,
                    WizardryCombatScreen.SCENE_WIDTH, WizardryCombatScreen.SCENE_HEIGHT);
            clickStage(position.x, position.y);
            check(combat.actions.get(0).monsterTarget != null, "3D model picking");
            MutableMonster reinforcement = new MutableMonster(combat.opponent);
            combat.addMonster(reinforcement);
            combat.removeMonster(reinforcement);
            check(!combat.monsters.contains(reinforcement), "Reinforcement removed");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.NUM_5);
            combat.render(0);
            snapshot("target/battle-command-preview.png");
            click(field(combat, "abilities"));
            combat.render(0);
            snapshot("target/battle-command-abilities.png");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ESCAPE);
            check(!drawer.isVisible(), "Escape closes popover");
            click(field(combat, "fight"));
            combat.render(1);
            check((int) field(combat, "turn") == 2, "Turn resolved");
            snapshot("target/battle-command-after-turn.png");
            combat.resize(1280, 720);
            check(stage.getViewport().getScreenWidth() == 960 && stage.getViewport().getScreenHeight() == 720,
                    "Widescreen letterboxing");
            combat.resize(1024, 768);
            while (combat.monsters.size() < 24) combat.addMonster(new MutableMonster(combat.opponent));
            combat.render(0);
            snapshot("target/battle-command-crowded.png");
            for (Actor actor : targets.getChildren()) {
                ModelInstance instance = field(field(actor, "health"), "modelInstance");
                if (instance == null) continue;
                Vector3 center = instance.calculateBoundingBox(new BoundingBox()).getCenter(new Vector3()).mul(instance.transform);
                camera.project(center, WizardryCombatScreen.SCENE_X, WizardryCombatScreen.SCENE_Y,
                        WizardryCombatScreen.SCENE_WIDTH, WizardryCombatScreen.SCENE_HEIGHT);
                check(center.x >= WizardryCombatScreen.SCENE_X
                        && center.x <= WizardryCombatScreen.SCENE_X + WizardryCombatScreen.SCENE_WIDTH,
                        "Crowded encounter fits the graphical frame");
            }
            combat.end(false);
            combat.render(0);
            check(((TextButton) field(combat, "fight")).getStage() == null
                    && ((TextButton) field(combat, "exit")).getStage() == stage, "End encounter controls");
            System.out.println("PASS: party selection, spell, ally target, melee, enemy target, reset, 3D picking, reinforcements, turn resolution, resize, end controls");
            Gdx.app.exit();
        } catch (Throwable failure) {
            fail(failure);
        }
    }

    private void click(Actor actor) {
        Vector2 point = actor.localToStageCoordinates(new Vector2(actor.getWidth() / 2, actor.getHeight() / 2));
        clickStage(point.x, point.y);
    }

    private void clickStage(float x, float y) {
        Vector2 point = new Vector2(x, y);
        stage.stageToScreenCoordinates(point);
        stage.touchDown(Math.round(point.x), Math.round(point.y), 0, Input.Buttons.LEFT);
        stage.touchUp(Math.round(point.x), Math.round(point.y), 0, Input.Buttons.LEFT);
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

    private static void fail(Throwable failure) {
        failure.printStackTrace();
        System.exit(1);
    }

    @Override public void dispose() {
        if (combat != null) combat.dispose();
    }
}
