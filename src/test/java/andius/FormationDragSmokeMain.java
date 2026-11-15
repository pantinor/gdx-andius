package andius;

import andius.dialogs.ReorderPartyDialog;
import andius.objects.ClassType;
import andius.objects.SaveGame;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;

/** Sends real stage pointer events through the popup's drag listeners. */
public class FormationDragSmokeMain extends Game {
    private Stage stage;
    private List<?> list;
    private SaveGame save;

    public static void main(String[] args) {
        var configuration = new LwjglApplicationConfiguration();
        configuration.width = 1024;
        configuration.height = 768;
        configuration.forceExit = false;
        new LwjglApplication(new FormationDragSmokeMain(), configuration);
    }

    private Vector2 point(float y) {
        return stage.stageToScreenCoordinates(list.localToStageCoordinates(new Vector2(list.getWidth() / 2, y)));
    }

    private float row(int index) {
        return list.getHeight() - (index + .5f) * list.getItemHeight();
    }

    private void drag(int from, float toY) {
        Vector2 start = point(row(from));
        Vector2 finish = point(toY);
        stage.touchDown(Math.round(start.x), Math.round(start.y), 0, 0);
        stage.touchDragged(Math.round(start.x + 12), Math.round(start.y), 0);
        stage.touchDragged(Math.round(finish.x), Math.round(finish.y), 0);
        stage.touchUp(Math.round(finish.x), Math.round(finish.y), 0, 0);
    }

    private void expect(String order) {
        StringBuilder names = new StringBuilder();
        for (var character : save.players) names.append(character.name);
        if (!order.contentEquals(names)) throw new AssertionError("Expected " + order + ", got " + names);
    }

    @Override public void create() {
        try {
            new Andius().create();
            Andius.CTX = new Context();
            save = new SaveGame();
            save.players = new SaveGame.CharacterRecord[6];
            for (int i = 0; i < 6; i++) save.players[i] = SaveGame.generatePlayer(1, ClassType.FIGHTER, String.valueOf((char) ('A' + i)));
            Andius.CTX.setSaveGame(save);
            BaseScreen screen = Constants.Map.BRITAIN.getScreen();
            screen.resize(1024, 768);
            screen.show();
            stage = screen.getStage();
            ReorderPartyDialog dialog = new ReorderPartyDialog(Andius.CTX, screen);
            dialog.show(stage);
            dialog.clearActions();
            dialog.setColor(1, 1, 1, 1);
            stage.act(0);
            stage.draw();
            var field = ReorderPartyDialog.class.getDeclaredField("playerSelection");
            field.setAccessible(true);
            list = (List<?>) field.get(dialog);
            drag(0, row(5)); expect("BCDEFA");
            drag(5, row(0)); expect("ABCDEF");
            drag(1, row(1)); expect("ABCDEF");
            drag(0, list.getHeight() - 6 * list.getItemHeight() - 12); expect("ABCDEF");
            if (list.getSelectedIndex() != 0) throw new AssertionError("Cancelled drag lost original selection");
            drag(0, list.getHeight() - 2 * list.getItemHeight()); expect("BCADEF");
            if (list.getSelectedIndex() != 2) throw new AssertionError("Moved character must remain selected");
            System.out.println("PASS: quick pointer drags down/up, stable intervening order, same-row drop, blank-space cancellation, and row-boundary drop");
            Gdx.app.exit();
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }
}
