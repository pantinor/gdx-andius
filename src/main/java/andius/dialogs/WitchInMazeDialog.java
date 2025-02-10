package andius.dialogs;

import andius.BaseScreen;
import andius.Context;
import andius.objects.Sound;
import andius.objects.Sounds;
import andius.WizardryData.Scenario;
import static andius.WizardryData.getMessage;
import andius.objects.Dialog;
import andius.objects.Item;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import java.util.ArrayList;
import java.util.List;

public class WitchInMazeDialog extends Dialog {

    public WitchInMazeDialog(Context ctx, BaseScreen screen) {
        super(ctx, screen);

        scrollPane.add(getMessage(Scenario.WER.messages(), 191).getText());
        scrollPane.add(getMessage(Scenario.WER.messages(), 192).getText());

        scrollPane.add(" ");

        List<String> texts = getMessage(Scenario.WER.messages(), 193).text();
        List<String> found = new ArrayList<>();

        scrollPane.add("You need");
        scrollPane.add(texts.get(2));
        scrollPane.add(texts.get(3));
        scrollPane.add(texts.get(4));
        scrollPane.add(texts.get(5));
        scrollPane.add(texts.get(6));
        scrollPane.add(texts.get(7));

        boolean alreadyHaveDabOfPuce = false;

        for (CharacterRecord rec : ctx.players()) {
            for (Item i : rec.inventory) {
                if (i.scenarioID == 4 && i.id == 13) {
                    found.add(i.name);//camphor aromatic ball
                }
                if (i.scenarioID == 4 && i.id == 11) {
                    found.add(i.name);//spanish unguent oil of ole
                }
                if (i.scenarioID == 4 && i.id == 12) {
                    found.add(i.name);//tannic acid witching rod
                }
                if (i.scenarioID == 4 && i.id == 72) {
                    found.add(i.name);//blender blade cusinart
                }
                if (i.scenarioID == 4 && i.id == 57) {
                    found.add(i.name);//rabbit fur magicians hat
                }
                if (i.scenarioID == 4 && i.id == 88) {
                    found.add(i.name);//fe2 golden pyrite
                }
                if (i.scenarioID == 4 && i.id == 18) {
                    alreadyHaveDabOfPuce = true;
                }
            }
        }

        scrollPane.add(" ");
        scrollPane.add("You have");

        for (String i : found) {
            scrollPane.add(i);
        }

        if (found.size() == 6) {
            if (!alreadyHaveDabOfPuce) {
                scrollPane.add(getMessage(Scenario.WER.messages(), 195).getText());
                scrollPane.add(getMessage(Scenario.WER.messages(), 196).getText());
                Item dabOfPuce = Scenario.WER.items().get(18);
                ctx.players()[0].inventory.add(dabOfPuce);
                screen.log(ctx.players()[0].name.toUpperCase() + " obtained " + dabOfPuce.genericName);
                Sounds.play(Sound.POSITIVE_EFFECT);
            } else {
                Sounds.play(Sound.NEGATIVE_EFFECT);
            }
        } else {
            scrollPane.add(getMessage(Scenario.WER.messages(), 194).getText());
        }

        input.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField tf, char key) {
                if (key == '\r') {
                    if (tf.getText().length() == 0) {
                        hide();
                        return;
                    }
                    hide();
                }
            }
        });

    }

}
