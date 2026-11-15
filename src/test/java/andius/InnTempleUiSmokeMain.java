package andius;

import andius.objects.ClassType;
import andius.objects.SaveGame;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.utils.ScreenUtils;

public class InnTempleUiSmokeMain extends Game {
    private int frames;
    private Context context;
    private InnScreen inn;
    private TempleScreen temple;

    public static void main(String[] args) {
        var cfg = new LwjglApplicationConfiguration();
        cfg.width = 1024; cfg.height = 768; cfg.forceExit = false;
        new LwjglApplication(new InnTempleUiSmokeMain(), cfg);
    }

    private Object field(Object object, String name) throws Exception {
        var field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }

    private void click(Actor actor) {
        Stage stage = actor.getStage();
        stage.draw();
        Vector2 point = stage.stageToScreenCoordinates(actor.localToStageCoordinates(new Vector2(actor.getWidth() / 2, actor.getHeight() / 2)));
        stage.touchDown(Math.round(point.x), Math.round(point.y), 0, 0);
        stage.touchUp(Math.round(point.x), Math.round(point.y), 0, 0);
    }

    private void snapshot(String name) {
        Pixmap image = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        PixmapIO.writePNG(Gdx.files.local("target/" + name + ".png"), image, -1, true);
        image.dispose();
    }

    @Override public void create() {
        try {
            new Andius().create();
            context = new Context();
            SaveGame save = new SaveGame();
            save.players = new SaveGame.CharacterRecord[6];
            for (int i = 0; i < 6; i++) {
                save.players[i] = SaveGame.generatePlayer(1, ClassType.MAGE, "Alexandria " + (i + 1));

                save.players[i].maxhp = 10;
                save.players[i].hp = 6;
                save.players[i].gold = 100;
            }
            save.players[0].gold = 25;
            context.setSaveGame(save);
            Andius.CTX = context;
            inn = new InnScreen(context, Constants.Map.BRITAIN);
            setScreen(inn);
        } catch (Throwable failure) { failure.printStackTrace(); System.exit(1); }
    }

    @Override public void render() {
        try {
            super.render(); frames++;
            if (frames == 10) {
                snapshot("inn-redesign");
                var rooms = (List<?>) field(inn, "roomSelection");
                rooms.setSelectedIndex(1);
            }
            if (frames == 11) {
                click((Actor) field(inn, "go"));
                if (context.players()[0].hp != 8 || context.players()[0].gold != 5)
                    throw new AssertionError("Inn healing must stop when funds run out");
                click((Actor) field(inn, "pool"));
                if (context.players()[0].gold != 505) throw new AssertionError("Inn gold pooling failed");
            }
            if (frames == 12) {
                click((Actor) field(inn, "go"));
                if (context.players()[0].hp != 10 || context.players()[0].gold != 485)
                    throw new AssertionError("Inn recovery and pricing changed");
                var rooms = (List<?>) field(inn, "roomSelection");
                rooms.setSelectedIndex(0);
                java.util.Arrays.fill(context.players()[0].magePoints, 0);
            }
            if (frames == 13) {
                click((Actor) field(inn, "go"));
                if (java.util.Arrays.stream(context.players()[0].magePoints).sum() == 0)
                    throw new AssertionError("Free rest must restore magic");
                context.players()[0].hp = 5;
                temple = new TempleScreen(context, Constants.Map.BRITAIN);
                setScreen(temple);
            }
            if (frames == 25) {
                snapshot("temple-redesign");
                click((Actor) field(temple, "offer"));
                if (context.players()[0].hp != 10 || context.players()[0].gold != 435)
                    throw new AssertionError("Temple healing or donation changed");
                context.players()[0].status.set(Constants.Status.POISONED, 1);
            }
            if (frames == 26) {
                click((Actor) field(temple, "offer"));
                if (context.players()[0].status.has(Constants.Status.POISONED) || context.players()[0].gold != 335)
                    throw new AssertionError("Temple cure failed");
                context.players()[0].hp = 5;
                context.players()[0].gold = 0;
            }
            if (frames == 27) {
                if (!((com.badlogic.gdx.scenes.scene2d.ui.TextButton) field(temple, "offer")).isDisabled())
                    throw new AssertionError("Unaffordable offering should be disabled");
                context.players()[1].gold = 200;
                ((List<?>) field(temple, "titherSelection")).setSelectedIndex(1);
            }
            if (frames == 28) {
                click((Actor) field(temple, "offer"));
                if (context.players()[0].hp != 10 || context.players()[1].gold != 150
                        || field(temple, "tither") != context.players()[1])
                    throw new AssertionError("Selected donor must pay and remain selected");
                Gdx.graphics.setWindowedMode(1280, 800);
            }
            if (frames == 40) {
                snapshot("temple-resized");
                System.out.println("PASS: inn rest/healing/pricing/fund limit/magic recovery/pooling; temple healing/cures/donor selection/affordability; resized layouts");
                Gdx.app.exit();
            }
        } catch (Throwable failure) { failure.printStackTrace(); System.exit(1); }
    }
}
