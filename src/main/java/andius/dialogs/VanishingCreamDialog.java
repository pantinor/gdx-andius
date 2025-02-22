package andius.dialogs;

import andius.BaseScreen;
import andius.Context;
import andius.objects.Sound;
import andius.objects.Sounds;
import andius.WizardryData.Scenario;
import static andius.WizardryData.getMessage;
import andius.objects.Dialog;
import andius.objects.Item;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class VanishingCreamDialog extends Dialog {

    public VanishingCreamDialog(Context ctx, BaseScreen screen) {
        super(ctx, screen);

        final Item vanishingCream = ctx.partyHasItem(11, 4);
        final Item jeweledAmulet = ctx.partyHasItem(52, 4);

        if (vanishingCream == null) {
            scrollPane.add(getMessage(Scenario.WER.messages(), 208).getText());
            scrollPane.add(" ");
            scrollPane.add("Want some?");
        } else {
            scrollPane.add("How is the cream working out for you?");
            scrollPane.add(" ");
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

                    if (response.equals("yes") || response.equals("ok") && vanishingCream == null) {

                        if (jeweledAmulet != null) {
                            ctx.removeItemFromParty(52, 4);
                            Item cream = Scenario.WER.items().get(11);
                            ctx.players()[0].inventory.add(cream);
                            screen.log(ctx.players()[0].name.toUpperCase() + " obtained " + cream.genericName);
                            Sounds.play(Sound.POSITIVE_EFFECT);
                        } else if (ctx.players()[0].gold >= 50000) {
                            ctx.poolGold(ctx.players()[0]);
                            ctx.players()[0].adjustGold(-50000);

                            Item cream = Scenario.WER.items().get(11);
                            ctx.players()[0].inventory.add(cream);
                            screen.log(ctx.players()[0].name.toUpperCase() + " obtained " + cream.genericName);
                            Sounds.play(Sound.POSITIVE_EFFECT);
                        }
                    }

                    hide();
                }
            }
        });

    }

}
