package andius.dialogs;

import andius.BaseScreen;
import andius.Context;
import andius.WizardryData.Message;
import andius.WizardryData.Scenario;
import andius.objects.Item;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Sound;
import andius.objects.Sounds;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class MarkDialog extends Dialog {

    public MarkDialog(Context ctx, BaseScreen screen, short itemId, Message msg, short markId) {
        super(ctx, screen);

        boolean markOwned = ctx.partyHasItem(Scenario.PMO.items().get(itemId));

        scrollPane.add(msg.getText());
        scrollPane.add(" ");
        scrollPane.add("It radiates heat and feels like it will burn if touched.  Do you touch it?");
        scrollPane.add(" ");

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

                    if (response.startsWith("y") || response.equals("ok")) {

                        for (CharacterRecord p : ctx.players()) {
                            int currentHp = p.hp;
                            int damage = Math.max(10, (currentHp * 30) / 100);
                            damage = Math.min(damage, currentHp - 1);//avoid killing them outright
                            p.adjustHP(-damage);
                        }

                        Sounds.play(Sound.PC_STRUCK);

                        if (!markOwned) {

                            if (markId == 1) {//kings
                                for (CharacterRecord p : ctx.players()) {
                                    p.awardXP(2550);
                                    screen.log(String.format("%s gained %d experience points.", p.name.toUpperCase(), 2550));
                                }
                            }
                            if (markId == 2) {//fire
                            }
                            if (markId == 3) {//force
                            }
                            if (markId == 4) {//snake
                            }

                            Item m = Scenario.PMO.items().get(itemId);
                            CharacterRecord p = ctx.players()[0];
                            p.inventory.add(m);
                            screen.log(p.name.toUpperCase() + " obtained " + m.genericName);
                        }

                    }

                    hide();
                }
            }
        });

    }

}
