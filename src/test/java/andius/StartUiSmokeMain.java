package andius;

import andius.objects.ClassType;
import andius.objects.SaveGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import java.nio.file.Files;
import java.nio.file.Path;

public class StartUiSmokeMain extends Andius {
    private int frames;
    private Path fixture;
    private StartScreen start;

    public static void main(String[] args) {
        var cfg = new LwjglApplicationConfiguration();
        cfg.width = 1024; cfg.height = 768; cfg.forceExit = false;
        new LwjglApplication(new StartUiSmokeMain(), cfg);
    }
    private Object field(String name) throws Exception {
        var field = StartScreen.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(start);
    }
    private void snapshot(String name) {
        Pixmap image = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        PixmapIO.writePNG(Gdx.files.local("target/" + name + ".png"), image, -1, true);
        image.dispose();
    }
    private void cleanup() throws Exception { if (fixture != null) Files.deleteIfExists(fixture); }
    @Override @SuppressWarnings("unchecked") public void create() {
        try {
            super.create();
            start = (StartScreen) getScreen();
            fixture = Files.createTempFile(Path.of("."), "party-start-ui-", ".json");
            SaveGame save = new SaveGame();
            save.map = Constants.Map.BRITAIN; save.x = 5; save.y = 4;
            save.players = new SaveGame.CharacterRecord[6];
            for (int i = 0; i < 6; i++) {
                save.players[i] = SaveGame.generatePlayer(1, ClassType.FIGHTER, new String[] {"Fred", "Sam", "Jack", "Joe", "Jane", "Frank"}[i]);

            }
            Files.writeString(fixture, new com.google.gson.Gson().toJson(save));
            start.show();
            ((List<String>) field("saveGameSelection")).setSelected(fixture.getFileName().toString());
        } catch (Throwable failure) { fail(failure); }
    }
    private void fail(Throwable failure) {
        failure.printStackTrace();
        try { cleanup(); } catch (Exception cleanupFailure) { cleanupFailure.printStackTrace(); }
        System.exit(1);
    }
    @Override @SuppressWarnings("unchecked") public void render() {
        try {
            super.render(); frames++;
            if (frames == 10) {
                snapshot("start-redesign");
                Gdx.input.getInputProcessor().keyDown(Keys.M);
                if (!(getScreen() instanceof ManageScreen)) throw new AssertionError("M must open company management");
            }
            if (frames == 11) {
                Actor cancel = ((ManageScreen) getScreen()).cancel;
                Stage stage = cancel.getStage();
                Vector2 point = stage.stageToScreenCoordinates(cancel.localToStageCoordinates(new Vector2(20, 20)));
                stage.touchDown(Math.round(point.x), Math.round(point.y), 0, 0);
                stage.touchUp(Math.round(point.x), Math.round(point.y), 0, 0);
                if (getScreen() != start) throw new AssertionError("Cancel must return to start");
                if (!fixture.getFileName().toString().equals(((List<String>) field("saveGameSelection")).getSelected()))
                    throw new AssertionError("Returning from management lost the selected save");
            }
            if (frames == 12) {
                Gdx.input.getInputProcessor().keyDown(Keys.J);
                if (!(getScreen() instanceof GameScreen)) throw new AssertionError("J must load the selected expedition");
            }
            if (frames == 20) {
                setScreen(start);
                Gdx.graphics.setWindowedMode(1280, 800);
            }
            if (frames == 30) {
                snapshot("start-resized");
                ((List<String>) field("saveGameSelection")).clearItems();
                if (!((TextButton) field("journey")).isDisabled()) throw new AssertionError("No saves must disable Journey");
                if (((TextButton) field("manage")).isDisabled()) throw new AssertionError("Management must remain available without saves");
            }
            if (frames == 35) {
                snapshot("start-empty");
                cleanup();
                start.show();
                var saves = (List<String>) field("saveGameSelection");
                if (saves.getItems().contains("party.json", false)) saves.setSelected("party.json");
                Gdx.graphics.setWindowedMode(1024, 768);
            }
            if (frames == 40) {
                snapshot("start-redesign");
                System.out.println("PASS: start rendering, save details, Manage/return, saved selection, Journey shortcut, resize, and empty-save state");
                Gdx.app.exit();
            }
        } catch (Throwable failure) { fail(failure); }
    }
}
