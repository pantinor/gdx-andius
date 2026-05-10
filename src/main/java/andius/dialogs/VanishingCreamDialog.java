package andius.dialogs;

import andius.BaseScreen;
import andius.Context;
import andius.WizardryData.Scenario;
import static andius.WizardryData.getMessage;
import andius.objects.Item;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Sound;
import andius.objects.Sounds;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VanishingCreamDialog extends Dialog {

    private static final int CREAM_BARTER_COST = 50000;

    private final List<BarterItem> pendingBarter = new ArrayList<>();
    private final Set<String> declinedBarters = new HashSet<>();
    private int declinedBarterCount = 0;

    public VanishingCreamDialog(Context ctx, BaseScreen screen) {
        super(ctx, screen);

        final Item vanishingCream = ctx.partyHasItem(11, 4);

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
                if (key != '\r') {
                    return;
                }

                if (tf.getText().length() == 0) {
                    hide();
                    return;
                }

                String response = tf.getText().trim().toLowerCase();
                tf.setText("");

                scrollPane.add(response, Color.WHITE);
                scrollPane.add(" ");

                if (vanishingCream != null) {
                    hide();
                    return;
                }

                if (response.equals("yes") || response.equals("ok")) {
                    if (!pendingBarter.isEmpty()) {
                        completeBarter(ctx, screen);
                        hide();
                        return;
                    }

                    if (tryPayWithGold(ctx, screen)) {
                        hide();
                        return;
                    }

                    showNextBarterOffer(ctx, screen);
                    return;
                }

                if (response.equals("no") && !pendingBarter.isEmpty()) {
                    declinedBarterCount++;

                    if (declinedBarterCount >= 3) {
                        screen.log("No deal, then.");
                        Sounds.play(Sound.EVADE);
                        hide();
                        return;
                    }

                    declinedBarters.add(barterKey(pendingBarter));
                    pendingBarter.clear();

                    showNextBarterOffer(ctx, screen);
                    return;
                }

                hide();
            }
        });
    }

    private boolean tryPayWithGold(Context ctx, BaseScreen screen) {
        CharacterRecord player = ctx.players()[0];

        if (player == null || player.gold < CREAM_BARTER_COST) {
            return false;
        }

        ctx.poolGold(player);
        player.adjustGold(-CREAM_BARTER_COST);

        giveCream(ctx, screen);
        return true;
    }

    private void showNextBarterOffer(Context ctx, BaseScreen screen) {
        pendingBarter.addAll(findAcceptableBarter(ctx.players(), CREAM_BARTER_COST, declinedBarters));

        if (pendingBarter.isEmpty()) {
            screen.log("Sorry no deal... I need 50000 gold worth of treasure!");
            Sounds.play(Sound.EVADE);
            hide();
            return;
        }

        scrollPane.add("I'll accept these items as barter:");
        scrollPane.add(" ");

        for (BarterItem barterItem : pendingBarter) {
            scrollPane.add(String.format("%s - %d gold",
                    barterItem.item.name,
                    barterItem.item.cost));
        }

        scrollPane.add(" ");
        scrollPane.add(String.format("Total value: %d gold", totalCost(pendingBarter)));
        scrollPane.add(" ");
        scrollPane.add("Trade all these items for the cream?");
    }

    private static List<BarterItem> findAcceptableBarter(
            CharacterRecord[] players,
            int requiredCost,
            Set<String> declinedBarters) {

        List<BarterItem> allItems = new ArrayList<>();

        if (players == null) {
            return allItems;
        }

        for (int playerIndex = 0; playerIndex < players.length; playerIndex++) {
            CharacterRecord player = players[playerIndex];

            if (player == null || player.inventory == null) {
                continue;
            }

            for (int itemIndex = 0; itemIndex < player.inventory.size(); itemIndex++) {
                Item item = player.inventory.get(itemIndex);

                if (item != null && item.cost > 0) {
                    allItems.add(new BarterItem(player, item, playerIndex, itemIndex));
                }
            }
        }

        if (allItems.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.shuffle(allItems);

        long totalAvailable = totalCost(allItems);
        if (totalAvailable < requiredCost) {
            return Collections.emptyList();
        }

        int maxCost = (int) Math.min(totalAvailable, Integer.MAX_VALUE);
        boolean[] reachable = new boolean[maxCost + 1];
        int[] previousTotal = new int[maxCost + 1];
        int[] itemIndexUsed = new int[maxCost + 1];

        for (int i = 0; i <= maxCost; i++) {
            previousTotal[i] = -1;
            itemIndexUsed[i] = -1;
        }

        reachable[0] = true;

        for (int i = 0; i < allItems.size(); i++) {
            int cost = (int) allItems.get(i).item.cost;

            if (cost <= 0 || cost > maxCost) {
                continue;
            }

            for (int total = maxCost - cost; total >= 0; total--) {
                if (reachable[total] && !reachable[total + cost]) {
                    reachable[total + cost] = true;
                    previousTotal[total + cost] = total;
                    itemIndexUsed[total + cost] = i;
                }
            }
        }

        for (int bestTotal = requiredCost; bestTotal <= maxCost; bestTotal++) {
            if (!reachable[bestTotal]) {
                continue;
            }

            List<BarterItem> best = buildBarterList(allItems, bestTotal, previousTotal, itemIndexUsed);

            if (!best.isEmpty() && !declinedBarters.contains(barterKey(best))) {
                return best;
            }
        }

        return Collections.emptyList();
    }

    private static List<BarterItem> buildBarterList(
            List<BarterItem> allItems,
            int bestTotal,
            int[] previousTotal,
            int[] itemIndexUsed) {

        List<BarterItem> best = new ArrayList<>();
        int total = bestTotal;

        while (total > 0) {
            int itemIndex = itemIndexUsed[total];

            if (itemIndex < 0) {
                return Collections.emptyList();
            }

            best.add(allItems.get(itemIndex));
            total = previousTotal[total];
        }

        return best;
    }

    private static String barterKey(List<BarterItem> barter) {
        List<String> keys = new ArrayList<>();

        for (BarterItem barterItem : barter) {
            keys.add(barterItem.playerIndex + ":" + barterItem.itemIndex + ":" + barterItem.item.id);
        }

        Collections.sort(keys);
        return String.join("|", keys);
    }

    private static long totalCost(List<BarterItem> barter) {
        long total = 0;

        for (BarterItem barterItem : barter) {
            total += barterItem.item.cost;
        }

        return total;
    }

    private void completeBarter(Context ctx, BaseScreen screen) {
        for (BarterItem barterItem : pendingBarter) {
            barterItem.player.inventory.remove(barterItem.item);
        }

        giveCream(ctx, screen);
    }

    private void giveCream(Context ctx, BaseScreen screen) {
        Item cream = Scenario.WER.items().get(11);
        ctx.players()[0].inventory.add(cream);

        screen.log(ctx.players()[0].name.toUpperCase() + " obtained " + cream.genericName);
        Sounds.play(Sound.POSITIVE_EFFECT);
    }

    private static class BarterItem {

        private final CharacterRecord player;
        private final Item item;
        private final int playerIndex;
        private final int itemIndex;

        private BarterItem(CharacterRecord player, Item item, int playerIndex, int itemIndex) {
            this.player = player;
            this.item = item;
            this.playerIndex = playerIndex;
            this.itemIndex = itemIndex;
        }
    }
}
