package andius;

import andius.WizardryData.MazeCell;
import andius.objects.Dialog;
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
                                screen.log(ctx.saveGame.players[0].name.toUpperCase() + " found an item!");
                                ctx.saveGame.players[0].inventory.add(screen.map.scenario().items().get(cell.itemObtainedFromRiddle));
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
