package andius.dialogs;

import andius.BaseScreen;
import andius.Context;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Sound;
import andius.objects.Sounds;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import utils.Utils;

public class FountainDialog extends Dialog {

    public FountainDialog(Context ctx, BaseScreen screen, short fountainType) {
        super(ctx, screen);

        scrollPane.add("You see a fountain of water.  Do you drink from it?");
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
                        if (fountainType == 1) {//cure
                            for (CharacterRecord p : ctx.players()) {
                                p.status.set(Status.POISONED, 0);
                            }
                            screen.log("Hmmm--Delicious!");
                            Sounds.play(Sound.HEALING);
                        }
                        if (fountainType == 2) {//heal
                            for (CharacterRecord p : ctx.players()) {
                                p.adjustHP(p.maxhp);
                                p.status.reset();
                            }
                            screen.log("Ahh-Refreshing!");
                            Sounds.play(Sound.HEALING);
                        }
                        if (fountainType == 3) {//damage acid
                            for (CharacterRecord p : ctx.players()) {
                                p.adjustHP(-Utils.RANDOM.nextInt(5, 26));
                            }
                            screen.log("Bleck--Nasty!");
                            Sounds.play(Sound.PC_STRUCK);
                        }
                        if (fountainType == 4) {//poisoned
                            for (CharacterRecord p : ctx.players()) {
                                p.status.set(Status.POISONED, 5);
                            }
                            screen.log("Argh-Choke-Gasp!");
                            Sounds.play(Sound.POISON_EFFECT);
                        }
                    }

                    hide();
                }
            }
        });

    }

}
