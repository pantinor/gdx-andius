package andius;

import static andius.Andius.mainGame;
import static andius.WizardryData.WER4_CHARS;
import static andius.WizardryData.WER_ITEMS;
import andius.objects.DoGooder;
import andius.objects.Icons;
import andius.objects.Item;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import utils.AutoFocusScrollPane;
import utils.LogScrollPane;
import utils.ManagementUi;
import static utils.SkinUtils.GOLD;
import static utils.SkinUtils.INK;
import static utils.SkinUtils.MUTED;

public class Wiz4RewardScreen implements Screen, Constants {
    private final ManagementUi ui = new ManagementUi();
    private final Stage stage = new Stage(new FitViewport(Andius.SCREEN_WIDTH, Andius.SCREEN_HEIGHT));
    private final Texture chestTexture = new Texture(Gdx.files.classpath("assets/data/rewards.png"));
    public final CharacterRecord player;
    public final DoGooder opponent;
    private final List<ItemLabel> items = new List<>(ui.skin);
    private final AutoFocusScrollPane itemsScroll;
    private final TextButton take = new TextButton("Take item", ui.skin);
    private final TextButton exit = new TextButton("Leave remaining", ui.skin);
    private final LogScrollPane receipt = new LogScrollPane(ui.skin, new Table(), 304, "default");
    private Label itemTitle, itemDescription, inventorySummary, itemCount, emptyState;
    private Image itemIcon;
    private int takenCount;

    public Wiz4RewardScreen(CharacterRecord player, DoGooder opponent) {
        this.player = player;
        this.opponent = opponent;
        java.util.List<ItemLabel> labels = new java.util.ArrayList<>();
        for (int id : opponent.partyMembers) {
            DoGooder member = WER4_CHARS.get(id);
            for (int itemId : member.items) labels.add(new ItemLabel(WER_ITEMS.get(itemId)));
        }
        labels.sort((a, b) -> {
            int type = Integer.compare(a.item.type.ordinal(), b.item.type.ordinal());
            return type != 0 ? type : a.item.name.compareToIgnoreCase(b.item.name);
        });
        items.setItems(labels.toArray(new ItemLabel[0]));
        itemsScroll = new AutoFocusScrollPane(items, ui.skin);
        itemsScroll.setScrollingDisabled(true, false);
        itemsScroll.setFadeScrollBars(false);
        take.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { take(); }
        });
        exit.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { leave(); }
        });
        buildLayout();
        stage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override public boolean keyDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, int keycode) {
                if (event.isHandled()) return false;
                if (keycode == Input.Keys.T && !take.isDisabled()) { take(); return true; }
                if (keycode == Input.Keys.L) { leave(); return true; }
                return false;
            }
        });
    }

    private void buildLayout() {
        ui.label(stage, "Victory spoils", "default-24", GOLD, 24, 714, 700, 34);
        ui.label(stage, opponent.name + "  /  Recover what the fallen have left behind.", "default", MUTED, 24, 696, 850, 20);
        ui.panel(stage, "01  /  The victor", "Treasures for the road ahead", 24, 288);
        Image chest = new Image(new TextureRegion(chestTexture, 270, 0, 128, 108));
        chest.setScaling(Scaling.none);
        chest.setBounds(104, 508, 128, 108);
        chest.setTouchable(Touchable.disabled);
        stage.addActor(chest);
        Image portrait = new Image(andius.objects.Portraits.get(player));
        portrait.setScaling(Scaling.fit);
        portrait.setBounds(36, 368, 76, 76);
        portrait.setTouchable(Touchable.disabled);
        stage.addActor(portrait);
        ui.label(stage, player.name, "emphasis", Color.WHITE, 124, 414, 176, 24);
        ui.label(stage, "Level " + player.level + " / " + player.classType, "default", MUTED, 124, 390, 176, 20);
        inventorySummary = ui.description(stage, 36, 190, 264, 138);
        Label note = ui.description(stage, 36, 108, 264, 66);
        note.setText("Choose an item to inspect it. Taken items are added to your pack; anything left behind is abandoned.");
        ui.panel(stage, "02  /  Recovered items", "Select an item to inspect", 328, 328);
        itemsScroll.setBounds(340, 108, 304, 482);
        stage.addActor(itemsScroll);
        itemCount = ui.label(stage, "", "default", GOLD, 340, 594, 304, 24);
        emptyState = ui.label(stage, "No items remain to claim.", "default", MUTED, 340, 542, 304, 28);
        ui.panel(stage, "03  /  The spoils", "Item properties and collection record", 672, 328);
        itemIcon = new Image();
        itemIcon.setScaling(Scaling.none);
        itemIcon.setBounds(684, 562, 44, 44);
        itemIcon.setTouchable(Touchable.disabled);
        stage.addActor(itemIcon);
        itemTitle = ui.label(stage, "", "emphasis", Color.WHITE, 740, 562, 248, 44);
        itemDescription = ui.description(stage, 684, 358, 304, 184);
        ui.label(stage, "COLLECTED", "default", GOLD, 684, 316, 304, 24);
        receipt.setBounds(684, 108, 304, 196);
        receipt.setFadeScrollBars(false);
        stage.addActor(receipt);
        take.setBounds(328, 24, 172, 38);
        exit.setBounds(850, 24, 150, 38);
        ui.primary(take);
        stage.addActor(take);
        stage.addActor(exit);
    }

    private void take() {
        ItemLabel selected = items.getSelected();
        if (selected == null) return;
        player.inventory.add(selected.item.clone());
        Array<ItemLabel> remaining = new Array<>(items.getItems());
        remaining.removeValue(selected, true);
        items.setItems(remaining);
        takenCount++;
        receipt.add(player.name + " claimed " + selected.item.name + ".");
    }

    private void leave() { mainGame.setScreen(Map.WIZARDRY4.getScreen()); }

    private void refreshUI() {
        ItemLabel selected = items.getSelected();
        take.setDisabled(selected == null);
        emptyState.setVisible(items.getItems().isEmpty());
        itemCount.setText(items.getItems().size + " unclaimed / " + takenCount + " collected");
        exit.setText(items.getItems().isEmpty() ? "Continue journey" : "Leave remaining");
        inventorySummary.setText("Health  " + player.hp + " / " + player.maxhp + "\nGold carried  " + player.gold
                + "\nItems in pack  " + player.inventory.size() + "\n\nCollected from this encounter  " + takenCount);
        itemIcon.setVisible(selected != null);
        itemTitle.setText(selected == null ? "Nothing left to claim" : selected.item.name);
        itemDescription.setText(selected == null ? "The spoils have been gathered. Your journey awaits."
                : selected.item.type + "\n" + selected.item.vendorDescription() + "\n" + selected.item.briefDescription()
                    + (selected.item.canUse(player.classType) ? "" : "\nNot usable by your class."));
        if (selected != null) itemIcon.setDrawable(new TextureRegionDrawable(Icons.get(selected.item)));
    }

    private static class ItemLabel {
        final Item item;
        ItemLabel(Item item) { this.item = item; }
        @Override public String toString() { return item.name; }
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float delta) {
        Gdx.gl.glClearColor(INK.r, INK.g, INK.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        refreshUI();
        stage.getViewport().apply();
        stage.act(Math.min(delta, .1f));
        stage.draw();
    }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }
    @Override public void dispose() { stage.dispose(); chestTexture.dispose(); ui.dispose(); }
}
