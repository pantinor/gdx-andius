package andius;

import andius.objects.ClassType;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

/** Exercises returning party members without writing the roster or save game. */
public class ManageRosterSmokeMain extends Game {
    public static void main(String[] args) {
        var configuration = new LwjglApplicationConfiguration();
        configuration.width = 1024;
        configuration.height = 768;
        configuration.forceExit = false;
        new LwjglApplication(new ManageRosterSmokeMain(), configuration);
    }

    private CharacterRecord character(Object slot) throws Exception {
        var field = slot.getClass().getDeclaredField("character");
        field.setAccessible(true);
        return (CharacterRecord) field.get(slot);
    }

    @Override public void create() {
        try {
            new Andius().create();
            SaveGame save = new SaveGame();
            save.players = new CharacterRecord[] {
                SaveGame.generatePlayer(1, ClassType.FIGHTER, "FRED"),
                SaveGame.generatePlayer(1, ClassType.FIGHTER, "JACK")
            };
            ManageScreen manage = new ManageScreen(null, Andius.skin, save);
            for (int i = 0; i < manage.registry.getItems().size; i++) {
                character(manage.registry.getItems().get(i)).name = "RESERVE " + i;
            }
            CharacterRecord existing = character(manage.registry.getItems().first());
            existing.name = "FRED";
            existing.exp = 123;
            CharacterRecord fred = character(manage.partyFormation.getItems().first());
            fred.exp = 456;
            Object lastSlot = manage.registry.getItems().peek();
            character(lastSlot).name = "(empty)";
            manage.render(0);
            if (manage.remove.isDisabled()) throw new AssertionError("Empty roster row should allow return");
            manage.remove.fire(new ChangeEvent());
            if (character(manage.registry.getItems().first()) != existing || existing.exp != 123)
                throw new AssertionError("Same-name reserve was overwritten");
            if (character(lastSlot) != fred || fred.exp != 456)
                throw new AssertionError("Party member did not move into the empty row");
            if (!"(empty)".equals(character(manage.partyFormation.getItems().first()).name))
                throw new AssertionError("Returned party slot was not cleared");

            manage.partyFormation.setSelectedIndex(1);
            CharacterRecord jack = character(manage.partyFormation.getSelected());
            manage.render(0);
            if (!manage.remove.isDisabled()) throw new AssertionError("Full roster must disable return");
            manage.remove.fire(new ChangeEvent());
            if (character(manage.partyFormation.getSelected()) != jack || character(lastSlot) != fred)
                throw new AssertionError("Full-roster guard must preserve both records");

            manage.registry.setSelectedIndex(manage.registry.getItems().size - 1);
            manage.clear.fire(new ChangeEvent());
            manage.render(0);
            if (manage.remove.isDisabled()) throw new AssertionError("Freeing a row should re-enable return");
            manage.remove.fire(new ChangeEvent());
            if (character(lastSlot) != jack) throw new AssertionError("Freed row was not used");
            manage.render(0);
            if (!manage.remove.isDisabled()) throw new AssertionError("Empty party slot must disable return");
            manage.dispose();
            System.out.println("PASS: same-name reserve preserved, empty row used, full roster blocked, and return re-enabled after deletion");
            Gdx.app.exit();
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }
}
