package andius.dialogs;

import static andius.Andius.CTX;
import static andius.Andius.mainGame;
import andius.BaseScreen;
import andius.Context;
import andius.Wiz4CombatScreen;
import andius.WizardryData;
import andius.WizardryData.Scenario;
import static andius.WizardryData.WER4_CHARS;
import static andius.WizardryData.getMessage;
import andius.WizardryDungeonScreen;
import andius.objects.DoGooder;
import andius.objects.Item;
import andius.objects.Sound;
import andius.objects.Sounds;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class DukesDialog extends Dialog {

    public DukesDialog(Context ctx, BaseScreen screen) {
        super(ctx, screen);

        Item rose = ctx.partyHasItem(17, 4);
        Item arrow = ctx.partyHasItem(108, 4);
        Item orb = ctx.partyHasItem(109, 4);
        Item horn = ctx.partyHasItem(110, 4);
        Item ring = ctx.partyHasItem(111, 4);

        if (rose != null && arrow != null && orb != null && horn != null && ring != null) {
            scrollPane.add(getMessage(Scenario.WER.messages(), 185).getText());
            scrollPane.add(" ");
            scrollPane.add(getMessage(Scenario.WER.messages(), 186).getText());
            scrollPane.add(" ");
            scrollPane.add(getMessage(Scenario.WER.messages(), 187).getText());
        } else {
            scrollPane.add(getMessage(Scenario.WER.messages(), 189).getText());
            hide();
            Sounds.play(Sound.NEGATIVE_EFFECT);
            DoGooder dg = WER4_CHARS.get(466);
            WizardryData.MazeCell fromCell = ((WizardryDungeonScreen) screen).currentCell();
            Wiz4CombatScreen cs = new Wiz4CombatScreen(CTX.saveGame.players[0], CTX.saveGame.players[0].summonedMonsters, dg, null, fromCell,
                    "You do not have the Crystal Rose, the Arrow of Truth, the Orb of Dreams, the Rallying Horn, or the Signet Ring!");
            mainGame.setScreen(cs);
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

                    WizardryDungeonScreen wsc = (WizardryDungeonScreen) screen;

                    if (response.equals("yes") || response.equals("ok")) {
                        hide();
                        Sounds.play(Sound.POSITIVE_EFFECT);
                        wsc.log(getMessage(Scenario.WER.messages(), 190).getText());
                        WizardryData.MazeCell cell = wsc.cell(15, 9, 13);
                        cell.function = null;
                    } else {
                        hide();
                        Sounds.play(Sound.NEGATIVE_EFFECT);
                        scrollPane.add(getMessage(Scenario.WER.messages(), 189).getText());
                        DoGooder dg = WER4_CHARS.get(466);
                        WizardryData.MazeCell fromCell = ((WizardryDungeonScreen) screen).currentCell();
                        Wiz4CombatScreen cs = new Wiz4CombatScreen(CTX.saveGame.players[0], CTX.saveGame.players[0].summonedMonsters, dg, null, fromCell, null);
                        mainGame.setScreen(cs);
                    }

                }
            }
        });

    }

}
