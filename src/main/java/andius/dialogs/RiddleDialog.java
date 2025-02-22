package andius.dialogs;

import andius.Context;
import andius.WizardryData.MazeCell;
import andius.WizardryDungeonScreen;
import andius.objects.Dialog;
import andius.objects.Item;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class RiddleDialog extends Dialog {

    private final WizardryDungeonScreen screen;
    private final MazeCell cell;

    public RiddleDialog(Context ctx, WizardryDungeonScreen screen, MazeCell cell) {
        super(ctx, screen);
        this.screen = screen;
        this.cell = cell;

        input.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField tf, char key) {

                if (key == '\r') {

                    if (tf.getText().length() == 0) {
                        hide();
                        return;
                    }

                    String response = tf.getText().trim().toLowerCase();

                    boolean correct = false;
                    for (String answer : cell.riddleAnswers) {
                        if (response.equalsIgnoreCase(answer)) {
                            correct = true;
                            if (cell.itemObtainedFromRiddle > 0) {
                                Item it = screen.map.scenario().items().get(cell.itemObtainedFromRiddle);
                                screen.log(ctx.players()[0].name.toUpperCase() + " obtained " + it.genericName);
                                ctx.saveGame.players[0].inventory.add(it);
                            }
                            break;
                        }
                    }

                    if (correct) {
                        scrollPane.add("Correct!");
                        cell.riddleAnswers.clear();
                        hide();
                    } else {
                        scrollPane.add(cell.message.getText());
                    }

                    tf.setText("");
                }
            }
        });

        scrollPane.add(this.cell.message.getText());
    }

}
