package andius.dialogs;

import andius.BaseScreen;
import andius.Context;
import andius.objects.Sound;
import andius.objects.Sounds;
import andius.WizardryData.Scenario;
import static andius.WizardryData.getMessage;
import andius.objects.Dialog;
import andius.objects.Item;
import andius.objects.Item.ItemType;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import java.util.concurrent.atomic.AtomicBoolean;

public class GatesOfHellDialog extends Dialog {

    public GatesOfHellDialog(Context ctx, BaseScreen screen) {
        super(ctx, screen);

        final AtomicBoolean chimesOwned = new AtomicBoolean(ctx.partyHasItem(102, 4) != null);
        final AtomicBoolean diaryOwned = new AtomicBoolean(ctx.partyHasItem(101, 4) != null);
        final AtomicBoolean candleOwned = new AtomicBoolean(ctx.partyHasItem(103, 4) != null);
        final AtomicBoolean kaOwned = new AtomicBoolean(ctx.partyHasItem(6, 4) != null);
        final AtomicBoolean hhgOwned = new AtomicBoolean(ctx.partyHasItem(4, 4) != null);

        final AtomicBoolean bootsEquipped = new AtomicBoolean();
        for (CharacterRecord rec : ctx.players()) {
            if (rec.item1 != null && rec.item1.id == 5 && rec.item1.scenarioID == 4) {
                bootsEquipped.set(true);
            }
            if (rec.item2 != null && rec.item2.id == 5 && rec.item2.scenarioID == 4) {
                bootsEquipped.set(true);
            }
        }

        final AtomicBoolean chimesUsed = new AtomicBoolean();
        final AtomicBoolean diaryUsed = new AtomicBoolean();
        final AtomicBoolean candleUsed = new AtomicBoolean();

        if (hhgOwned.get()) {
            scrollPane.add("The gates of hell remain shut!", Color.SCARLET);
            input.setTextFieldListener(new TextField.TextFieldListener() {
                @Override
                public void keyTyped(TextField tf, char key) {
                    if (key == '\r') {
                        if (tf.getText().length() == 0) {
                            hide();
                            return;
                        }
                    }
                }
            });
        } else {

            scrollPane.add(getMessage(Scenario.WER.messages(), 62).getText(), Color.SCARLET);
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

                        if (response.equals("bribe")) {
                            scrollPane.add(getMessage(Scenario.WER.messages(), 69).getText(), Color.SCARLET);
                            ctx.poolGold(ctx.players()[0]);
                            ctx.players()[0].adjustGold(-200);
                            Sounds.play(Sound.NEGATIVE_EFFECT);
                        } else if (response.equals("use")) {
                            for (CharacterRecord rec : ctx.players()) {
                                for (Item i : rec.inventory) {
                                    if (i.scenarioID == 4 && (i.type == ItemType.SPECIAL || i.type == ItemType.MISC)) {
                                        scrollPane.add(i.name, Color.GREEN);
                                    }
                                }
                            }
                            scrollPane.add(" ");
                            scrollPane.add("Which item will you use?", Color.WHITE);
                        } else if (response.equals("run")) {
                            hide();
                        } else if (response.equals("chimes")) {
                            if (chimesOwned.get()) {
                                scrollPane.add(getMessage(Scenario.WER.messages(), 63).getText(), Color.SCARLET);
                                chimesUsed.set(true);
                                ctx.removeItemFromParty(102, 4);
                                Sounds.play(Sound.TRIGGER);
                                scrollPane.add("What else will you use?", Color.WHITE);
                            } else {
                                scrollPane.add("You don't have that!", Color.WHITE);
                            }
                        } else if (response.equals("diary")) {
                            if (chimesUsed.get()) {
                                if (diaryOwned.get()) {
                                    scrollPane.add(getMessage(Scenario.WER.messages(), 64).getText(), Color.SCARLET);
                                    diaryUsed.set(true);
                                    ctx.removeItemFromParty(101, 4);
                                    Sounds.play(Sound.TRIGGER);
                                    scrollPane.add("What else will you use?", Color.WHITE);
                                } else {
                                    scrollPane.add("You don't have that!", Color.WHITE);
                                }
                            } else {
                                scrollPane.add("No effect!", Color.WHITE);
                            }
                        } else if (response.equals("candle")) {
                            if (diaryUsed.get()) {
                                if (candleOwned.get()) {
                                    scrollPane.add(getMessage(Scenario.WER.messages(), 65).getText(), Color.SCARLET);
                                    candleUsed.set(true);
                                    ctx.removeItemFromParty(103, 4);
                                    Sounds.play(Sound.TRIGGER);
                                } else {
                                    scrollPane.add("You don't have that!", Color.WHITE);
                                }
                            } else {
                                scrollPane.add("No effect!", Color.WHITE);
                            }
                        } else if (candleUsed.get() && response.equals("yes")) {
                            scrollPane.add(getMessage(Scenario.WER.messages(), 66).getText(), Color.SCARLET);
                            if (kaOwned.get()) {
                                if (bootsEquipped.get()) {
                                    scrollPane.add(getMessage(Scenario.WER.messages(), 68).getText(), Color.GREEN);
                                    Sounds.play(Sound.DIVINE_INTERVENTION);

                                    Item hhg = Scenario.WER.items().get(4);
                                    ctx.players()[0].inventory.add(hhg);
                                    screen.log(ctx.players()[0].name.toUpperCase() + " obtained " + hhg.genericName);

                                } else {
                                    scrollPane.add(getMessage(Scenario.WER.messages(), 67).getText(), Color.SCARLET);
                                    Sounds.play(Sound.INFERNO);
                                    ctx.players()[0].adjustHP(-1 * ctx.players()[0].maxhp);
                                }
                            } else {
                                Sounds.play(Sound.INFERNO);
                                ctx.players()[0].adjustHP(-1 * ctx.players()[0].maxhp);
                            }
                        } else if (candleUsed.get() && response.equals("no")) {
                            hide();
                        } else {
                            scrollPane.add("Nothing happens", Color.WHITE);
                        }

                        tf.setText("");

                    }
                }
            });
        }

    }

}
