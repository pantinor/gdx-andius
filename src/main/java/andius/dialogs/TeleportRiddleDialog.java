package andius.dialogs;

import andius.BaseScreen;
import andius.Context;
import andius.WizardryData.MazeAddress;
import andius.WizardryData.MazeCell;
import andius.WizardryData.Message;
import andius.WizardryDungeonScreen;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class TeleportRiddleDialog extends Dialog {

    private final WizardryDungeonScreen screen;

    public TeleportRiddleDialog(Context ctx, BaseScreen sc, MazeCell cell, Message message, MazeAddress teleportTo) {
        super(ctx, sc);
        this.screen = (WizardryDungeonScreen) sc;

        input.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField tf, char key) {

                if (key == '\r') {

                    String response = tf.getText().trim().toLowerCase();

                    boolean correct = false;
                    for (String answer : cell.riddleAnswers) {
                        if (response.equalsIgnoreCase(answer)) {
                            correct = true;
                            break;
                        }
                    }

                    tf.setText("");

                    if (correct) {
                        scrollPane.add("Correct!");
                        cell.message = null;
                        cell.function = null;
                        cell.riddleAnswers = null;
                        hide();
                    } else {
                        hide();
                        screen.teleport(teleportTo, true);
                    }

                }
            }
        });

        scrollPane.add(message.getText());
    }

}
