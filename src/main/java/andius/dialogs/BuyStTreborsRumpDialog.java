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

public class BuyStTreborsRumpDialog extends Dialog {

    public BuyStTreborsRumpDialog(Context ctx, BaseScreen screen) {
        super(ctx, screen);

        final Item rump = ctx.partyHasItem(105, 4);
        final Item jeweledAmulet = ctx.partyHasItem(52, 4);

        if (rump == null) {
            scrollPane.add(getMessage(Scenario.WER.messages(), 212).getText());
            scrollPane.add("Buy some \"You Know What\" (Y/N)?");
        } else {
            scrollPane.add("How is the rump working out for you?");
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

                    if (response.equals("yes") || response.equals("ok") && rump == null) {

                        if (jeweledAmulet != null) {
                            ctx.removeItemFromParty(52, 4);
                            Item r = Scenario.WER.items().get(105);
                            ctx.players()[0].inventory.add(r);
                            screen.log(ctx.players()[0].name.toUpperCase() + " obtained " + r.genericName);
                            Sounds.play(Sound.POSITIVE_EFFECT);
                        } else if (ctx.players()[0].gold >= 100) {
                            ctx.poolGold(ctx.players()[0]);
                            ctx.players()[0].adjustGold(-100);

                            Item r = Scenario.WER.items().get(105);
                            ctx.players()[0].inventory.add(r);
                            screen.log(ctx.players()[0].name.toUpperCase() + " obtained " + r.genericName);
                            Sounds.play(Sound.POSITIVE_EFFECT);
                        }
                    }

                    hide();
                }
            }
        });

    }

}
