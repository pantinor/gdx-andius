package andius;

import andius.dialogs.ConversationDialog;
import andius.objects.ClassType;
import andius.objects.Conversations.Conversation;
import andius.objects.SaveGame;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class ConversationInputSmokeMain extends Game {
    public static void main(String[] args) {
        var configuration = new LwjglApplicationConfiguration();
        configuration.width = 1024;
        configuration.height = 768;
        configuration.forceExit = false;
        new LwjglApplication(new ConversationInputSmokeMain(), configuration);
    }

    @Override public void create() {
        try {
            new Andius().create();
            Andius.CTX = new Context();
            SaveGame save = new SaveGame();
            save.players = new SaveGame.CharacterRecord[] {SaveGame.generatePlayer(1, ClassType.FIGHTER, "Fred")};
            Andius.CTX.setSaveGame(save);
            GameScreen screen = (GameScreen) Constants.Map.BRITAIN.getScreen();
            screen.show();
            Conversation conversation = new Conversation();
            conversation.name = "Guide";
            conversation.description = "A helpful traveler.";
            conversation.story = java.util.List.of("First reply", "Second reply");
            ConversationDialog dialog = new ConversationDialog(Andius.CTX, screen, conversation);
            dialog.show(screen.getStage(), null);
            var inputField = andius.dialogs.Dialog.class.getDeclaredField("input");
            inputField.setAccessible(true);
            TextField input = (TextField) inputField.get(dialog);
            var index = ConversationDialog.class.getDeclaredField("index");
            index.setAccessible(true);
            for (char key : "hello".toCharArray()) Gdx.input.getInputProcessor().keyTyped(key);
            if (!"hello".equals(input.getText()) || index.getInt(dialog) != 0)
                throw new AssertionError("Typing must preserve text without advancing");
            Gdx.input.getInputProcessor().keyDown(Keys.ENTER);
            Gdx.input.getInputProcessor().keyTyped('\r');
            Gdx.input.getInputProcessor().keyUp(Keys.ENTER);
            if (!input.getText().isEmpty() || index.getInt(dialog) != 1)
                throw new AssertionError("Return must clear the input and advance exactly once");
            if (screen.getStage().getKeyboardFocus() != input)
                throw new AssertionError("Input focus must remain in the conversation");
            for (char key : "name".toCharArray()) Gdx.input.getInputProcessor().keyTyped(key);
            Gdx.input.getInputProcessor().keyTyped('\b');
            if (!"nam".equals(input.getText()) || index.getInt(dialog) != 1)
                throw new AssertionError("Editing a reply must not advance the story");
            Gdx.input.getInputProcessor().keyTyped('\n');
            if (index.getInt(dialog) != 2) throw new AssertionError("Line-feed Return must also submit");
            Gdx.input.getInputProcessor().keyTyped('x');
            if (dialog.getActions().size != 0) throw new AssertionError("Typing must not close the conversation");
            Gdx.input.getInputProcessor().keyTyped('\r');
            for (int frame = 0; frame < 10; frame++) screen.getStage().act(.1f);
            if (dialog.getStage() != null) throw new AssertionError("Return after the final reply must close the dialog");
            if (!(Gdx.input.getInputProcessor() instanceof com.badlogic.gdx.InputMultiplexer))
                throw new AssertionError("Exploration input was not restored");
            System.out.println("PASS: typing, editing, Return submission, input focus, final close, and exploration input restoration");
            Gdx.app.exit();
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }
}
