package andius;

import andius.WizardryData.Scenario;
import static andius.WizardryData.getMessage;
import andius.objects.Dialog;
import andius.objects.Item;
import andius.objects.Item.ItemType;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import java.util.concurrent.atomic.AtomicBoolean;

public class GatesOfHellDialog extends Dialog {

    public GatesOfHellDialog(Context ctx, BaseScreen screen) {
        super(ctx, screen);

        final AtomicBoolean chimesOwned = new AtomicBoolean();
        final AtomicBoolean diaryOwned = new AtomicBoolean();
        final AtomicBoolean candleOwned = new AtomicBoolean();
        final AtomicBoolean kaOwned = new AtomicBoolean();
        final AtomicBoolean bootsEquipped = new AtomicBoolean();
        final AtomicBoolean hhgOwned = new AtomicBoolean();

        for (CharacterRecord rec : ctx.players()) {
            for (Item i : rec.inventory) {
                if (i.scenarioID == 4) {
                    if (i.id == 101) {
                        diaryOwned.set(true);
                    }
                    if (i.id == 102) {
                        chimesOwned.set(true);
                    }
                    if (i.id == 103) {
                        candleOwned.set(true);
                    }
                    if (i.id == 6) {
                        kaOwned.set(true);
                    }
                    if (i.id == 4) {
                        hhgOwned.set(true);
                    }
                }
            }
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
            scrollPane.add("The gates of hell remain shut!");
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

            scrollPane.add(getMessage(Scenario.WER.messages(), 62).getText());

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

                        if (response.startsWith("b")) {
                            scrollPane.add(getMessage(Scenario.WER.messages(), 69).getText());
                        }

                        if (response.startsWith("u")) {
                            scrollPane.add("Which item will you use?");
                            for (CharacterRecord rec : ctx.players()) {
                                for (Item i : rec.inventory) {
                                    if (i.scenarioID == 4 && (i.type == ItemType.SPECIAL || i.type == ItemType.MISC)) {
                                        scrollPane.add(i.name);
                                    }
                                }
                            }
                        }

                        if (response.startsWith("r")) {
                            hide();
                        }

                        if (response.contains("chimes")) {
                            if (chimesOwned.get()) {
                                scrollPane.add(getMessage(Scenario.WER.messages(), 63).getText());
                                chimesUsed.set(true);
                                scrollPane.add(" ");
                                scrollPane.add("What else will you use?");
                            } else {
                                scrollPane.add("You don't have that!");
                            }
                        }

                        if (response.contains("diary")) {
                            if (chimesUsed.get()) {
                                if (diaryOwned.get()) {
                                    scrollPane.add(getMessage(Scenario.WER.messages(), 64).getText());
                                    diaryUsed.set(true);
                                    scrollPane.add(" ");
                                    scrollPane.add("What else will you use?");
                                } else {
                                    scrollPane.add("You don't have that!");
                                }
                            } else {
                                scrollPane.add("No effect!");
                            }
                        }

                        if (response.contains("candle")) {
                            if (diaryUsed.get()) {
                                if (candleOwned.get()) {
                                    scrollPane.add(getMessage(Scenario.WER.messages(), 65).getText());
                                    candleUsed.set(true);
                                } else {
                                    scrollPane.add("You don't have that!");
                                }
                            } else {
                                scrollPane.add("No effect!");
                            }
                        }

                        if (candleUsed.get() && response.startsWith("y")) {
                            scrollPane.add(getMessage(Scenario.WER.messages(), 66).getText());
                            if (kaOwned.get()) {
                                if (bootsEquipped.get()) {
                                    scrollPane.add(getMessage(Scenario.WER.messages(), 68).getText());
                                    Sounds.play(Sound.DIVINE_INTERVENTION);

                                    Item hhg = Scenario.WER.items().get(4);
                                    ctx.players()[0].inventory.add(hhg);
                                    screen.log(ctx.players()[0].name.toUpperCase() + " obtained " + hhg.genericName);

                                } else {
                                    scrollPane.add(getMessage(Scenario.WER.messages(), 67).getText());
                                    Sounds.play(Sound.INFERNO);
                                    ctx.players()[0].adjustHP(-1 * ctx.players()[0].maxhp);
                                }
                            } else {
                                Sounds.play(Sound.INFERNO);
                                ctx.players()[0].adjustHP(-1 * ctx.players()[0].maxhp);
                            }
                        }

                        if (candleUsed.get() && response.startsWith("n")) {
                            hide();
                        }

                        tf.setText("");

                    }
                }
            });
        }

    }

}
