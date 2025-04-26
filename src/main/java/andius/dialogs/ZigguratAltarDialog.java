package andius.dialogs;

import andius.BaseScreen;
import andius.Context;
import andius.objects.Sound;
import andius.objects.Sounds;
import andius.WizardryData.Scenario;
import andius.objects.Item;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ZigguratAltarDialog extends Dialog {

    public ZigguratAltarDialog(Context ctx, BaseScreen screen) {
        super(ctx, screen);

        if (ctx.partyHasItem(7, 4) == null && ctx.partyHasItem(8, 4) == null && ctx.partyHasItem(9, 4) == null) {

            scrollPane.add("Here at the top of the Ziggurat is a decrepit Temple.");
            scrollPane.add("Inside the Temple is a plain altar.");
            scrollPane.add("Carved into the top of the altar are three depressions.");
            scrollPane.add("All of the depressions are empty.");
            scrollPane.add(" ");
            scrollPane.add("You may make an offering of Gold, an Item, or you may leave.", Color.YELLOW);

            Item i1 = null, i2 = null, i3 = null;
            List<Item> owned = new ArrayList<>();
            List<Item> offered = new ArrayList<>();

            for (CharacterRecord rec : ctx.players()) {
                for (Item i : rec.inventory) {
                    if (i.scenarioID == 4 && i.id == 1) {
                        i1 = i;
                        owned.add(i);//bloodstone
                    }
                    if (i.scenarioID == 4 && i.id == 2) {
                        i2 = i;
                        owned.add(i);//lander turq
                    }
                    if (i.scenarioID == 4 && i.id == 3) {
                        i3 = i;
                        owned.add(i);//amber dragon stone
                    }
                }
            }

            Item blood = i1;
            Item turq = i2;
            Item amber = i3;
            AtomicBoolean messageIntoned = new AtomicBoolean();

            scrollPane.add(" ");
            scrollPane.add("What item will you offer?");
            for (Item i : owned) {
                scrollPane.add(i.name, Color.FOREST);
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

                        if (owned.size() == 3) {
                            if (response.contains("blood") && !offered.contains(blood)) {
                                offered.add(blood);
                                Sounds.play(Sound.TRIGGER);
                                scrollPane.add("The item nestles in a depression. It is as if it has become part of the altar.", Color.YELLOW);
                                if (offered.size() != 3) {
                                    scrollPane.add(" ");
                                    scrollPane.add("What else will you offer?");
                                }
                            } else if (response.contains("turq") && !offered.contains(turq)) {
                                offered.add(turq);
                                Sounds.play(Sound.TRIGGER);
                                scrollPane.add("The item nestles in a depression. It is as if it has become part of the altar.", Color.YELLOW);
                                if (offered.size() != 3) {
                                    scrollPane.add(" ");
                                    scrollPane.add("What else will you offer?");
                                }
                            } else if (response.contains("amber") && !offered.contains(amber)) {
                                offered.add(amber);
                                Sounds.play(Sound.TRIGGER);
                                scrollPane.add("The item nestles in a depression. It is as if it has become part of the altar.", Color.YELLOW);
                                if (offered.size() != 3) {
                                    scrollPane.add(" ");
                                    scrollPane.add("What else will you offer?");
                                }
                            } else {
                                scrollPane.add("Nothing happens..");
                                hide();
                            }

                            if (offered.size() == 3 && !messageIntoned.get()) {
                                scrollPane.add("The Temple is no longer the ruin that it was when you first found it.");
                                scrollPane.add("Now it is bright and whole.  Power radiates.  The modest altar is respendent with Gold and Jewels.  You feel the Presence!");
                                scrollPane.add(" ");
                                scrollPane.add("Suspended above the altar are three Magical Swords.  A godlike voice, coming from everywhere at once, intones...");
                                scrollPane.add(" ");
                                scrollPane.add("Take ye one of these Swords as a reward for restoring unto Me my sacred Temple!");
                                scrollPane.add(" ");
                                scrollPane.add("Which Sword will you take?");
                                scrollPane.add("The Green Sword,", Color.GREEN);
                                scrollPane.add("the Blue Sword,", Color.BLUE);
                                scrollPane.add("or the Amber Sword?", Color.GOLDENROD);
                                scrollPane.add(" ");

                                messageIntoned.set(true);
                            } else if (messageIntoned.get()) {
                                if (response.contains("green")) {
                                    Item eastWindSword = Scenario.WER.items().get(7);
                                    ctx.players()[0].inventory.add(eastWindSword);
                                    screen.log(ctx.players()[0].name.toUpperCase() + " obtained " + eastWindSword.genericName);
                                    Sounds.play(Sound.POSITIVE_EFFECT);
                                    ctx.removeItemFromParty(1, 4);
                                    ctx.removeItemFromParty(2, 4);
                                    ctx.removeItemFromParty(3, 4);
                                    owned.clear();
                                } else if (response.contains("blue")) {
                                    Item westWindSword = Scenario.WER.items().get(8);
                                    ctx.players()[0].inventory.add(westWindSword);
                                    screen.log(ctx.players()[0].name.toUpperCase() + " obtained " + westWindSword.genericName);
                                    Sounds.play(Sound.POSITIVE_EFFECT);
                                    ctx.removeItemFromParty(1, 4);
                                    ctx.removeItemFromParty(2, 4);
                                    ctx.removeItemFromParty(3, 4);
                                    owned.clear();
                                } else if (response.contains("amber")) {
                                    Item dragonsClaw = Scenario.WER.items().get(9);
                                    ctx.players()[0].inventory.add(dragonsClaw);
                                    screen.log(ctx.players()[0].name.toUpperCase() + " obtained " + dragonsClaw.genericName);
                                    Sounds.play(Sound.POSITIVE_EFFECT);
                                    ctx.removeItemFromParty(1, 4);
                                    ctx.removeItemFromParty(2, 4);
                                    ctx.removeItemFromParty(3, 4);
                                    owned.clear();
                                }
                            }

                        } else {
                            hide();
                        }

                        tf.setText("");

                    }
                }
            });

        } else {
            scrollPane.add("Here at the top of the Ziggurat is a Temple.");
            scrollPane.add("Inside the Temple is a resplendent and bejeweled altar.");
            scrollPane.add("There are three stones already embedded in the top of the altar.");

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

}
