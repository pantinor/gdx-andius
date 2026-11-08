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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** Checks the actual Wizardry IV layout and input flow in an OpenGL window. */
public class Wiz4CombatSmokeMain extends Game {
    private Wiz4CombatScreen combat;
    private Stage stage;
    private int frames;

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Wizardry IV combat smoke check";
        config.width = 1024;
        config.height = 768;
        config.forceExit = false;
        new LwjglApplication(new Wiz4CombatSmokeMain(), config);
    }

    @Override public void create() {
        try {
            new Andius().create();
            createEncounter(9);
        } catch (Throwable error) { fail(error); }
    }

    private void createEncounter(int allyCount) throws Exception {
        createEncounter(allyCount, WizardryData.WER4_CHARS.get(448));
    }

    private void createEncounter(int allyCount, andius.objects.DoGooder opponent) throws Exception {
        Context context = new Context();
        context.setSaveGame(new SaveGame());
        var player = SaveGame.generatePlayer(20, ClassType.MAGE, "Werdna");
        player.knownSpells.add(Spells.DIOS);
        player.clericPoints[0] = 9;
        context.saveGame.players = new SaveGame.CharacterRecord[] {player};
        Andius.CTX = context;
        List<MutableMonster> allies = new ArrayList<>();
        for (int i = 0; i < allyCount; i++) allies.add(new MutableMonster(WizardryData.WER_MONSTERS.get(108)));
        Wiz4CombatScreen previous = combat;
        combat = new Wiz4CombatScreen(player, allies, opponent, null, null, "Layout preview");
        stage = field(combat, "stage");
        setScreen(combat);
        if (previous != null) previous.dispose();
    }

    @Override public void render() {
        super.render();
        if (++frames != 30) return;
        try {
            TextButton cast = field(combat, "cast");
            check(cast.isDisabled(), "Cast disabled without an ability");
            Table enemies = field(combat, "enemiesTable");
            Actor first = enemies.getChildren().first();
            click(first);
            check(combat.pickEnemy() == field(first, "mm"), "Portrait card selects enemy");
            check(enemies.getColumns() == 2, "Enemy party uses two columns");
            var spells = (com.badlogic.gdx.scenes.scene2d.ui.List<?>) field(combat, "spells");
            int dios = -1;
            for (int i = 0; i < spells.getItems().size; i++) {
                if (field(spells.getItems().get(i), "spell") == Spells.DIOS) { dios = i; break; }
            }
            check(dios >= 0, "Healing spell listed");
            spells.setSelectedIndex(dios);
            combat.render(0);
            check(!cast.isDisabled(), "Cast enabled after choosing a spell");
            snapshot("target/wiz4-combat-preview.png");
            click(cast);
            check((int) field(combat, "round") == 2, "Cast resolves a turn");
            combat.render(0);
            snapshot("target/wiz4-combat-after-cast.png");
            createEncounter(0);
            click(field(combat, "fight"));
            check((int) field(combat, "round") == 2, "Fight resolves a turn without summons");
            combat.resize(1280, 720);
            check(stage.getViewport().getScreenWidth() == 960 && stage.getViewport().getScreenHeight() == 720,
                    "Widescreen letterboxing");
            combat.resize(1024, 768);
            combat.end(false);
            combat.render(0);
            check(((TextButton) field(combat, "exit")).getStage() == stage
                    && ((TextButton) field(combat, "fight")).getStage() == null, "End encounter controls");
            var largestParty = WizardryData.WER4_CHARS.stream().filter(p -> p.partyMembers != null)
                    .max(java.util.Comparator.comparingInt(p -> p.partyMembers.length)).orElseThrow();
            createEncounter(12, largestParty);
            combat.render(0);
            snapshot("target/wiz4-combat-crowded.png");
            Table crowded = field(combat, "enemiesTable");
            Actor last = crowded.getChildren().peek();
            var scroll = (com.badlogic.gdx.scenes.scene2d.ui.ScrollPane) field(combat, "enemiesScroll");
            scroll.scrollTo(last.getX(), last.getY(), last.getWidth(), last.getHeight());
            scroll.updateVisualScroll();
            combat.render(0);
            click(last);
            check(combat.pickEnemy() == field(last, "mm"), "Scrolled enemy card selects its target");
            Table allies = field(combat, "monstersTable");
            Actor lastAlly = allies.getChildren().peek();
            var allyScroll = (com.badlogic.gdx.scenes.scene2d.ui.ScrollPane) field(combat, "monstersScroll");
            check(allyScroll.getMaxY() > 0, "Long summon roster scrolls");
            allyScroll.scrollTo(lastAlly.getX(), lastAlly.getY(), lastAlly.getWidth(), lastAlly.getHeight());
            allyScroll.updateVisualScroll();
            combat.render(0);
            Vector2 allyCenter = lastAlly.localToStageCoordinates(new Vector2(lastAlly.getWidth() / 2, lastAlly.getHeight() / 2));
            check(allyCenter.y >= allyScroll.getY() && allyCenter.y <= allyScroll.getY() + allyScroll.getHeight(),
                    "Last summoned ally is reachable");
            System.out.println("PASS: target selection, enemy grid, spell selection, cast, fight, empty summon roster, resize, end controls, crowded party scrolling ("
                    + largestParty.partyMembers.length + " enemies)");
            Gdx.app.exit();
        } catch (Throwable error) { fail(error); }
    }

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
    @Override public void dispose() { if (combat != null) combat.dispose(); }
}
