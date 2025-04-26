package andius.dialogs;

import andius.BaseScreen;
import andius.Context;
import andius.objects.Sound;
import andius.objects.Sounds;
import andius.WizardryData.Scenario;
import static andius.WizardryData.getMessage;
import andius.objects.Item;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class BathInPoolDialog extends Dialog {

    public BathInPoolDialog(Context ctx, BaseScreen screen) {
        super(ctx, screen);

        final Item maintCap = ctx.partyHasItem(20, 4);
        final Item soap = ctx.partyHasItem(120, 4);

        if (maintCap == null) {
            scrollPane.add(getMessage(Scenario.WER.messages(), 151).getText());
        } else {
            scrollPane.add(getMessage(Scenario.WER.messages(), 152).getText());
            scrollPane.add(" ");
            scrollPane.add("Your Maintanence Cap instill in you the knowledge you need to repair the fountain!", Color.YELLOW);
            scrollPane.add("Will You Wade (Y/N)?");
        }

        input.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField tf, char key) {
                if (key == '\r') {
                    if (tf.getText().length() == 0) {
                        hide();
                        return;
                    }

                    String response = tf.getText().trim().toLowerCase();
                    scrollPane.add(response, Color.WHITE);
                    scrollPane.add(" ");

                    if (response.equals("yes") || response.equals("ok") && soap == null) {
                        if (maintCap != null) {
                            ctx.removeItemFromParty(20, 4);
                            Item r = Scenario.WER.items().get(120);
                            ctx.players()[0].inventory.add(r);
                            screen.log(getMessage(Scenario.WER.messages(), 153).getText());
                            screen.log(ctx.players()[0].name.toUpperCase() + " obtained " + r.genericName);
                            Sounds.play(Sound.HEALING);
                            ctx.players()[0].summonedMonsters.clear();
                        }
                    }

                    hide();
                }
            }
        });

    }

}
