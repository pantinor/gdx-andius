package andius;

import andius.objects.ClassType;
import andius.objects.Item;
import andius.objects.Spells;
import andius.objects.SaveGame;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.ArrayList;

public class CampVendorUiSmokeMain extends Game {
    private int frames;
    private Context context;
    private CampScreen camp;
    private VendorScreen vendor;
    private Item equipment;

    public static void main(String[] args) {
        var cfg = new LwjglApplicationConfiguration();
        cfg.width = 1024; cfg.height = 768; cfg.forceExit = false;
        new LwjglApplication(new CampVendorUiSmokeMain(), cfg);
    }

    private Object field(Object object, String name) throws Exception {
        var field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }

    private void click(Actor actor, int button) {
        Stage stage = actor.getStage();
        stage.draw();
        Vector2 point = stage.stageToScreenCoordinates(actor.localToStageCoordinates(new Vector2(actor.getWidth() / 2, actor.getHeight() / 2)));
        stage.touchDown(Math.round(point.x), Math.round(point.y), 0, button);
        stage.touchUp(Math.round(point.x), Math.round(point.y), 0, button);
    }

    private Actor row(Object table) { return ((Table) table).getCells().first().getActor(); }

    private void snapshot(String name) {
        Pixmap image = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        PixmapIO.writePNG(Gdx.files.local("target/" + name + ".png"), image, -1, true);
        image.dispose();
    }

    @Override public void create() {
        try {
            new Andius().create();
            context = new Context();
            SaveGame save = new SaveGame();
            save.players = new SaveGame.CharacterRecord[6];
            for (int i = 0; i < 6; i++) {
                var player = SaveGame.generatePlayer(30, ClassType.BISHOP, "Alexandria " + (i + 1));
                save.players[i] = player;

                player.gold = 100000;
                player.inventory.clear();
                player.weapon = null;
                player.knownSpells.clear();
                player.knownSpells.add(Spells.DIOS);
                for (Spells spell : Spells.values()) if (spell != Spells.DIOS) player.knownSpells.add(spell);
                java.util.Arrays.fill(player.magePoints, 9);
                java.util.Arrays.fill(player.clericPoints, 9);
                for (Item item : WizardryData.PMO_ITEMS) {
                    if (item.cost > 0 && player.inventory.size() < 14) player.inventory.add(item.clone());
                }
            }
            context.setSaveGame(save);
            Andius.CTX = context;
            equipment = WizardryData.PMO_ITEMS.stream().filter(item -> item.type == Item.ItemType.WEAPON && item.canUse(ClassType.BISHOP)).findFirst().orElseThrow().clone();
            save.players[0].inventory.add(0, equipment);
            camp = new CampScreen(context, Constants.Map.BRITAIN);
            setScreen(camp);
        } catch (Throwable failure) { failure.printStackTrace(); System.exit(1); }
    }

    @Override public void render() {
        try {
            super.render(); frames++;
            if (frames == 10) {
                snapshot("camp-redesign");
                Object player = field(camp, "selectedPlayer");
                click(row(field(player, "invTable")), 0);
                click((Actor) field(player, "weaponIcon"), 0);
                if (context.players()[0].weapon != equipment) throw new AssertionError("Equipment click failed");
                click((Actor) field(player, "weaponIcon"), 1);
                if (context.players()[0].weapon != null || !context.players()[0].inventory.contains(equipment)) throw new AssertionError("Unequip failed");
                click(row(field(player, "invTable")), 0);
            }
            if (frames == 11) {
                click((Actor) field(camp, "trade"), 0);
            }
            if (frames == 20) {
                snapshot("camp-trade-dialog");
                var dialog = field(camp, "traderSlider");
                var list = (com.badlogic.gdx.scenes.scene2d.ui.List<?>) field(dialog, "tradeSelection");
                int before = context.players()[1].inventory.size();
                Vector2 point = list.getStage().stageToScreenCoordinates(list.localToStageCoordinates(new Vector2(30, list.getHeight() - list.getItemHeight() * 1.5f)));
                list.getStage().touchDown(Math.round(point.x), Math.round(point.y), 0, 0);
                list.getStage().touchUp(Math.round(point.x), Math.round(point.y), 0, 0);
                if (context.players()[1].inventory.size() != before + 1) throw new AssertionError("Trade failed");
            }
            if (frames == 35) {
                Object player = field(camp, "selectedPlayer");
                Object spell = row(field(player, "spellTable"));
                context.players()[0].hp = 1;
                click((Actor) field(spell, "cast"), 0);
            }
            if (frames == 45) {
                snapshot("camp-cast-dialog");
                Object dialog = field(camp, "castTargetSlider");
                var list = (com.badlogic.gdx.scenes.scene2d.ui.List<?>) field(dialog, "targetSelection");
                Vector2 point = list.getStage().stageToScreenCoordinates(list.localToStageCoordinates(new Vector2(30, list.getHeight() - list.getItemHeight() / 2)));
                list.getStage().touchDown(Math.round(point.x), Math.round(point.y), 0, 0);
                list.getStage().touchUp(Math.round(point.x), Math.round(point.y), 0, 0);
                if (context.players()[0].hp <= 1) throw new AssertionError("Camp healing failed");
                if (((Actor) dialog).getStage() != null) throw new AssertionError("Spell target popup still blocks input");
                click(((Table) field(camp, "playerTable")).getCells().get(1).getActor(), 0);
                if (field(field(camp, "selectedPlayer"), "p") != context.players()[1])
                    throw new AssertionError("Camp controls did not regain focus after casting");
            }
            if (frames == 46) {
                Object player = field(camp, "selectedPlayer");
                click((Actor) field(row(field(player, "spellTable")), "cast"), 0);
                var dialog = (com.badlogic.gdx.scenes.scene2d.ui.Dialog) field(camp, "castTargetSlider");
                if (dialog.getStage() == null || dialog.getColor().a != 1f)
                    throw new AssertionError("Reopened target popup must be visible");
                click(dialog.getButtonTable().getCells().first().getActor(), 0);
                if (dialog.getStage() != null) throw new AssertionError("Cancelled target popup still blocks input");
                click(((Table) field(camp, "playerTable")).getCells().first().getActor(), 0);
                if (field(field(camp, "selectedPlayer"), "p") != context.players()[0])
                    throw new AssertionError("Camp controls did not regain focus after cancellation");
            }
            if (frames == 47) {
                Object player = field(camp, "selectedPlayer");
                click((Actor) field(row(field(player, "spellTable")), "cast"), 0);
                var dialog = (com.badlogic.gdx.scenes.scene2d.ui.Dialog) field(camp, "castTargetSlider");
                if (dialog.getStage() == null || dialog.getColor().a != 1f)
                    throw new AssertionError("Repeated target popup must remain visible");
                context.players()[0].hp = 1;
                var list = (com.badlogic.gdx.scenes.scene2d.ui.List<?>) field(dialog, "targetSelection");
                Vector2 point = list.getStage().stageToScreenCoordinates(list.localToStageCoordinates(new Vector2(30, list.getHeight() - list.getItemHeight() / 2)));
                list.getStage().touchDown(Math.round(point.x), Math.round(point.y), 0, 0);
                list.getStage().touchUp(Math.round(point.x), Math.round(point.y), 0, 0);
                if (context.players()[0].hp <= 1 || dialog.getStage() != null)
                    throw new AssertionError("Repeated cast did not heal and close cleanly");
            }
            if (frames == 60) {
                var stock = new ArrayList<Item>();
                for (Item item : WizardryData.PMO_ITEMS) if (item.cost > 0) { var clone = item.clone(); clone.stock = 2; stock.add(clone); }
                vendor = new VendorScreen(context, Constants.Map.BRITAIN, stock, "Boltac's Trading Post");
                setScreen(vendor);
            }
            if (frames == 70) {
                snapshot("vendor-redesign");
                Object player = field(vendor, "selectedPlayer");
                for (var cell : ((Table) field(player, "invTable")).getCells()) {
                    if (cell.getActor() instanceof Label) continue;
                    Item item = (Item) field(cell.getActor(), "item");
                    if (item.type != Item.ItemType.WEAPON || !item.canUse(context.players()[0].classType)) continue;
                    click(cell.getActor(), 0);
                    Actor slot = (Actor) field(player, "weaponIcon");
                    Vector2 pointer = slot.getStage().stageToScreenCoordinates(slot.localToStageCoordinates(new Vector2(20, 20)));
                    slot.getStage().mouseMoved(Math.round(pointer.x), Math.round(pointer.y));
                    vendor.render(0);
                    click(slot, 0);
                    vendor.render(0);
                    Label hover = (Label) field(vendor, "equipmentName");
                    if (!item.name.contentEquals(hover.getText())) throw new AssertionError("Hover did not update after equipping");
                    click(slot, 1);
                    vendor.render(0);
                    if (!hover.getText().toString().contains("Empty")) throw new AssertionError("Hover did not update after unequipping");
                    slot.getStage().mouseMoved(10, 10);
                    vendor.render(0);
                    if (!"Equipment".contentEquals(hover.getText())) throw new AssertionError("Hover did not reset on exit");
                    break;
                }
                click(row(field(vendor, "vendorTable")), 0);
            }
            if (frames == 71) {
                int count = context.players()[0].inventory.size();
                int gold = context.players()[0].gold;
                Item merchandise = (Item) field(field(vendor, "selectedVendorItem"), "item");
                click((Actor) field(vendor, "buy"), 0);
                if (context.players()[0].inventory.size() != count + 1) throw new AssertionError("Purchase failed");
                if (context.players()[0].gold != gold - merchandise.cost || merchandise.stock != 1)
                    throw new AssertionError("Purchase price or stock was not updated");
                click(row(field(field(vendor, "selectedPlayer"), "invTable")), 0);
            }
            if (frames == 72) {
                int count = context.players()[0].inventory.size();
                click((Actor) field(vendor, "sell"), 0);
                if (context.players()[0].inventory.size() != count - 1) throw new AssertionError("Sale failed");
                click((Actor) field(vendor, "pool"), 0);
                for (int i = 1; i < 6; i++) if (context.players()[i].gold != 0) throw new AssertionError("Gold pooling failed");
                @SuppressWarnings("unchecked")
                var filter = (com.badlogic.gdx.scenes.scene2d.ui.SelectBox<Item.ItemType>) field(vendor, "filter");
                for (Item.ItemType type : Item.ItemType.values()) {
                    filter.setSelected(type);
                    for (var cell : ((Table) field(vendor, "vendorTable")).getCells()) {
                        if (cell.getActor() instanceof Label) continue;
                        Item item = (Item) field(cell.getActor(), "item");
                        if (type != Item.ItemType.ANY && item.type != type) throw new AssertionError("Merchant filter failed");
                    }
                }
                filter.setSelected(Item.ItemType.ANY);
                Gdx.graphics.setWindowedMode(1280, 800);
            }
            if (frames == 85) {
                snapshot("vendor-resized");
                System.out.println("PASS: camp/vendor render, equip/unequip, trade, targeted healing, buy/sell, gold pooling, and resize");
                Gdx.app.exit();
            }
        } catch (Throwable failure) { failure.printStackTrace(); System.exit(1); }
    }
}
