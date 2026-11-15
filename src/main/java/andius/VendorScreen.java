package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import andius.objects.ClassType;
import andius.objects.Icons;
import andius.objects.Item;
import andius.objects.Item.ItemType;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import java.util.ArrayList;
import java.util.List;
import utils.AutoFocusScrollPane;
import utils.ManagementUi;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import static utils.SkinUtils.GOLD;
import static utils.SkinUtils.INK;
import static utils.SkinUtils.MUTED;
import utils.Utils;

public class VendorScreen implements Screen, Constants {

    private final Context context;
    private final List<Item> vendorItems;
    private final String vendorName;

    private final ManagementUi ui = new ManagementUi();
    private Table equipmentTable;
    private InventoryImage hoveredEquipment;
    private Label equipmentName, itemDescription, selectionCaption;
    private Label shopDescription;
    private com.badlogic.gdx.scenes.scene2d.ui.SelectBox<ItemType> filter;
    private final Stage stage;

    private final TextButton exit;
    private final TextButton sell;
    private final TextButton buy;
    private final TextButton pool;

    private ItemListing selectedItem;
    private VendorItem selectedVendorItem;
    private PlayerIndex selectedPlayer;
    private ItemType currentFilter = ItemType.ANY;

    private final AutoFocusScrollPane playerPane;
    private AutoFocusScrollPane invPane;
    private final AutoFocusScrollPane vendorPane;

    private final Table playerTable;
    private Table vendorTable;

    private Image playerFocusIndicator, itemFocusIndicator, vendorFocusIndicator;

    private final Texture redBackgrnd = Utils.fillRectangle(10, 10, Color.valueOf("6b3340"), .42f);
    private final Texture clearBackgrnd = Utils.fillRectangle(10, 10, Color.CLEAR, 0f);

    private static final int WD = 304;
    private static final int HT = 60;
    private static final int DIM = 44;
    private static final int PDIM = 84;

    public VendorScreen(Context context, Constants.Map contextMap, List<Item> vendorItems, String vendorName) {
        this.context = context;
        this.vendorItems = vendorItems;
        this.vendorName = vendorName;
        this.stage = new Stage(new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT));
        //this.stage.setDebugAll(true);

        playerFocusIndicator = ui.selection(WD, HT);
        playerFocusIndicator.setWidth(264);
        playerFocusIndicator.setHeight(PDIM);

        itemFocusIndicator = ui.selection(WD, HT);
        itemFocusIndicator.setWidth(WD);
        itemFocusIndicator.setHeight(HT);

        vendorFocusIndicator = ui.selection(WD, HT);
        vendorFocusIndicator.setWidth(WD);
        vendorFocusIndicator.setHeight(HT);

        playerTable = new Table(ui.skin);
        playerTable.top().left();
        for (int i = 0; i < this.context.players().length; i++) {
            PlayerIndex pi = new PlayerIndex(this.context.players()[i]);
            playerTable.add(pi);
            playerTable.row();
            if (selectedPlayer == null) {
                selectedPlayer = pi;
                selectedPlayer.addActor(playerFocusIndicator);
                playerFocusIndicator.toBack();
            }
        }

        playerTable.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override public void clicked(InputEvent event, float x, float y) {
                Actor actor = event.getTarget();
                while (actor != null && !(actor instanceof PlayerIndex)) actor = actor.getParent();
                if (actor instanceof PlayerIndex player) selectPlayer(player);
            }
        });

        playerPane = new AutoFocusScrollPane(playerTable, ui.skin);
        playerPane.setScrollingDisabled(true, false);

        invPane = new AutoFocusScrollPane(selectedPlayer.invTable, ui.skin);
        invPane.setScrollingDisabled(true, false);

        vendorTable = new Table(ui.skin);
        vendorTable.align(Align.top);

        filterVendorItems(ItemType.ANY);

        vendorPane = new AutoFocusScrollPane(vendorTable, ui.skin);
        vendorPane.setScrollingDisabled(true, false);

        this.buy = new TextButton("BUY", ui.skin);
        this.buy.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedVendorItem != null && selectedPlayer.p.gold >= selectedVendorItem.item.cost) {

                    Item vendorIt = selectedVendorItem.item;
                    Item playerIt = vendorIt.clone();

                    selectedPlayer.p.adjustGold(-vendorIt.cost);
                    selectedPlayer.p.inventory.add(playerIt);

                    if (vendorIt.stock > 0) {
                        vendorIt.stock--;
                        if (vendorIt.stock == 0) {
                            selectedVendorItem.remove();
                            selectedVendorItem = null;
                            vendorFocusIndicator.remove();
                        }
                    }

                    selectedPlayer.setInventoryTable();

                    filterVendorItems(currentFilter);

                    Sounds.play(Sound.TRIGGER);
                } else {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                }
            }
        });
        this.sell = new TextButton("SELL", ui.skin);
        this.sell.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedItem != null) {

                    Item sold = selectedItem.item;

                    selectedPlayer.p.inventory.remove(sold);
                    selectedPlayer.p.adjustGold(sold.cost / 2);

                    itemFocusIndicator.remove();
                    selectedItem = null;

                    selectedPlayer.setInventoryTable();

                    filterVendorItems(currentFilter);

                    Sounds.play(Sound.TRIGGER);
                } else {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                }
            }
        });
        this.exit = new TextButton("EXIT", ui.skin);
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(contextMap.getScreen());
            }
        });
        this.pool = new TextButton("POOL", ui.skin);
        this.pool.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                for (CharacterRecord p : context.players()) {
                    if (p != selectedPlayer.p) {
                        int gold = p.gold;
                        p.adjustGold(-gold);
                        selectedPlayer.p.adjustGold(gold);
                    }
                }
            }
        });

        buildLayout();
        showEquipment();
        stage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override public boolean keyDown(InputEvent event, int keycode) {
                if (event.isHandled()) return false;
                TextButton action = switch (keycode) {
                    case Input.Keys.B -> buy;
                    case Input.Keys.S -> sell;
                    case Input.Keys.P -> pool;
                    case Input.Keys.E -> exit;
                    default -> null;
                };
                if (action == null || action.isDisabled()) return false;
                action.fire(new ChangeListener.ChangeEvent());
                return true;
            }
        });
    }

    private void filterVendorItems(ItemType type) {
        currentFilter = type;

        vendorTable.clear();
        vendorTable.clearListeners();

        if (vendorFocusIndicator.getParent() != null) {
            vendorFocusIndicator.getParent().removeActor(vendorFocusIndicator);
        }

        selectedVendorItem = null;

        vendorTable.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {

                if (event.toString().equals("touchDown")) {
                    if (vendorFocusIndicator.getParent() != null) {
                        vendorFocusIndicator.getParent().removeActor(vendorFocusIndicator);
                    }
                    if (event.getTarget() instanceof VendorItem) {
                        selectedVendorItem = (VendorItem) event.getTarget();
                        selectedVendorItem.addActor(vendorFocusIndicator);
                        vendorFocusIndicator.toBack();
                    } else if (event.getTarget().getParent() instanceof VendorItem) {
                        selectedVendorItem = (VendorItem) event.getTarget().getParent();
                        selectedVendorItem.addActor(vendorFocusIndicator);
                        vendorFocusIndicator.toBack();
                    }
                }

                return false;
            }
        });

        java.util.List<Item> sellables = new ArrayList<>();
        for (Item it : vendorItems) {
            if (it.stock != 0 && !sellables.contains(it)) {
                if (type == ItemType.ANY || type.equals(it.type)) {
                    sellables.add(it);
                }
            }
        }

        sellables.sort((it1, it2) -> Long.compare(it1.cost, it2.cost));

        for (Item it : sellables) {
            vendorTable.add(new VendorItem(it));
            vendorTable.row();
        }

        if (sellables.isEmpty()) vendorTable.add(new Label("No merchandise in this category.", ui.skin)).width(WD).padTop(14);

        if (selectedPlayer != null) {
            for (Cell cell : vendorTable.getCells()) {
                if (cell.getActor() instanceof VendorItem) {
                    VendorItem vi = (VendorItem) cell.getActor();
                    vi.setUsable(selectedPlayer.p.classType);
                }
            }
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(INK.r, INK.g, INK.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        refreshUI();
        stage.getViewport().apply();
        stage.act(Math.min(delta, .1f));
        stage.draw();
    }

    private void selectPlayer(PlayerIndex player) {
        if (player == selectedPlayer) return;
        selectedPlayer = player;
        playerFocusIndicator.remove();
        player.addActor(playerFocusIndicator);
        playerFocusIndicator.toBack();
        selectedItem = null;
        itemFocusIndicator.remove();
        invPane.setActor(player.invTable);
        for (Cell<?> cell : vendorTable.getCells()) {
            if (cell.getActor() instanceof VendorItem item) item.setUsable(player.p.classType);
        }
        showEquipment();
    }

    private void showEquipment() {
        hoveredEquipment = null;
        refreshEquipmentName();
        equipmentTable.clearChildren();
        InventoryImage[] slots = {selectedPlayer.weaponIcon, selectedPlayer.armorIcon, selectedPlayer.helmIcon,
            selectedPlayer.shieldIcon, selectedPlayer.glovesIcon, selectedPlayer.item1Icon, selectedPlayer.item2Icon};
        String[] names = {"Hand", "Body", "Head", "Shield", "Gloves", "Item I", "Item II"};
        for (int i = 0; i < slots.length; i++) {
            Table slot = new Table();
            slot.background(ui.surface("0c1822"));
            slot.add(slots[i]).size(DIM, DIM).row();
            Label caption = new Label(names[i], ui.skin);
            caption.setColor(MUTED);
            slot.add(caption).height(20);
            equipmentTable.add(slot).width(DIM).padRight(i == 6 ? 0 : 1);
        }
    }

    private void buildLayout() {
        ui.label(stage, "Merchants' hall", "default-24", GOLD, 24, 714, 700, 34);
        ui.label(stage, vendorName, "default", MUTED, 24, 696, 700, 20);
        ui.panel(stage, "01  /  The company", "Select an adventurer", 24, 288);
        ui.panel(stage, "02  /  Equipment & pack", "", 328, 328);
        ui.panel(stage, "03  /  Merchant stock", "Prices in gold", 672, 328);
        playerPane.setBounds(36, 108, 264, 516);
        playerPane.setFadeScrollBars(false);
        stage.addActor(playerPane);
        selectionCaption = ui.label(stage, "", "default", MUTED, 340, 638, 304, 20);
        equipmentTable = ui.equipment();
        stage.addActor(equipmentTable);
        equipmentName = ui.label(stage, "Equipment", "default", GOLD, 340, 534, 304, 20);
        invPane.setBounds(340, 256, 304, 270);
        invPane.setFadeScrollBars(false);
        stage.addActor(invPane);
        itemDescription = ui.description(stage, 340, 108, 304, 134);
        filter = new com.badlogic.gdx.scenes.scene2d.ui.SelectBox<>(ui.skin);
        filter.setItems(ItemType.ANY, ItemType.WEAPON, ItemType.ARMOR, ItemType.SHIELD,
                ItemType.HELMET, ItemType.GAUNTLET, ItemType.MISC, ItemType.SPECIAL);
        filter.setBounds(684, 590, 304, 34);
        filter.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { filterVendorItems(filter.getSelected()); }
        });
        stage.addActor(filter);
        vendorPane.setBounds(684, 256, 304, 322);
        vendorPane.setFadeScrollBars(false);
        stage.addActor(vendorPane);
        shopDescription = ui.description(stage, 684, 108, 304, 134);
        sell.setText("Sell"); sell.setBounds(328, 24, 96, 38);
        pool.setText("Pool gold"); pool.setBounds(432, 24, 120, 38);
        buy.setText("Buy"); buy.setBounds(672, 24, 120, 38);
        stage.addActor(sell); stage.addActor(pool); stage.addActor(buy);
        ui.primary(buy);
        exit.setText("Return to journey");
        exit.setBounds(850, 24, 150, 38);
        stage.addActor(exit);
    }

    private void refreshEquipmentName() {
        equipmentName.setText(hoveredEquipment == null ? "Equipment"
                : hoveredEquipment.item == null ? hoveredEquipment.type + " / Empty" : hoveredEquipment.item.name);
    }

    private void refreshUI() {
        refreshEquipmentName();
        selectionCaption.setText(selectedPlayer.p.name + "  /  " + selectedPlayer.p.gold + " gold");
        itemDescription.setText(selectedItem == null ? "Select an item to inspect it." : selectedItem.item.name
                + "\n" + selectedItem.item.vendorDescription() + "\n" + selectedItem.item.briefDescription()
                + "\nSell value  " + selectedItem.item.cost / 2 + " gold");
        sell.setDisabled(selectedItem == null);
        buy.setDisabled(selectedVendorItem == null || selectedVendorItem.item.stock == 0 || selectedPlayer.p.gold < selectedVendorItem.item.cost);
        pool.setDisabled(context.players().length < 2);
        shopDescription.setText(selectedVendorItem == null ? "Select merchandise to inspect its price and properties."
                : selectedVendorItem.item.name + "\n" + selectedVendorItem.item.cost + " gold\n"
                    + selectedVendorItem.item.vendorDescription() + "\n" + selectedVendorItem.item.briefDescription()
                    + (selectedVendorItem.item.canUse(selectedPlayer.p.classType) ? "" : "\nNot usable by this class."));
    }


    private class PlayerIndex extends Group {

        private final CharacterRecord p;
        private final PlayerLabel label;
        private final Image avatar;
        private final InventoryImage weaponIcon;
        private final InventoryImage armorIcon;
        private final InventoryImage helmIcon;
        private final InventoryImage shieldIcon;
        private final InventoryImage glovesIcon;
        private final InventoryImage item1Icon;
        private final InventoryImage item2Icon;

        private final Table invTable = new Table(ui.skin);

        public PlayerIndex(CharacterRecord p) {
            this.p = p;
            this.label = new PlayerLabel();
            this.avatar = new Image(andius.objects.Portraits.get(p));

            this.weaponIcon = new InventoryImage(p.weapon, ItemType.WEAPON, 1);
            this.armorIcon = new InventoryImage(p.armor, ItemType.ARMOR, 2);
            this.helmIcon = new InventoryImage(p.helm, ItemType.HELMET, 3);
            this.shieldIcon = new InventoryImage(p.shield, ItemType.SHIELD, 4);
            this.glovesIcon = new InventoryImage(p.glove, ItemType.GAUNTLET, 5);
            this.item1Icon = new InventoryImage(p.item1, ItemType.MISC, 6);
            this.item2Icon = new InventoryImage(p.item2, ItemType.MISC, 7);

            addActor(this.avatar);
            addActor(this.label);

            setSize(264, PDIM);
            this.avatar.setScaling(Scaling.fit);
            this.avatar.setBounds(4, 4, 76, 76);
            this.label.setBounds(88, 0, 176, PDIM);
            this.label.setEllipsis(true);

            setInventoryTable();

        }

        private void setInventoryTable() {

            if (invPane != null) {
                invPane.clearChildren();
            }

            invTable.align(Align.top);
            invTable.clearChildren();
            invTable.clearListeners();

            for (Item it : p.inventory) {
                invTable.add(new ItemListing(it, p));
                invTable.row();
            }
            if (p.inventory.isEmpty()) invTable.add(new Label("Your pack is empty.", ui.skin)).width(WD).padTop(14);

            invTable.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    itemFocusIndicator.remove();
                    if (event.getTarget() instanceof ItemListing) {
                        selectedItem = (ItemListing) event.getTarget();
                        selectedItem.addActor(itemFocusIndicator);
                        itemFocusIndicator.toBack();

                    } else if (event.getTarget().getParent() instanceof ItemListing) {
                        selectedItem = (ItemListing) event.getTarget().getParent();
                        selectedItem.addActor(itemFocusIndicator);
                        itemFocusIndicator.toBack();

                    }
                }
            });

            if (invPane != null) {
                invPane.setActor(invTable);
            }
        }

        private class PlayerLabel extends Label {

            public PlayerLabel() {
                super("", ui.skin, "default");
                setColor(p.status.color());
                setText(getText());
            }

            @Override public com.badlogic.gdx.utils.CharArray getText() {
                return new com.badlogic.gdx.utils.CharArray(p.name + "\nLv " + p.level + " " + p.race + " " + p.classType
                        + "\nHP " + p.hp + "/" + p.maxhp + "  AC " + p.calculateAC() + "  " + p.status
                        + "\nG " + p.gold + "  XP " + p.exp);
            }

            @Override
            public void draw(Batch batch, float parentAlpha) {
                setText(getText());
                super.draw(batch, parentAlpha);
            }

            @Override
            public Color getColor() {
                return p.isDead() ? Color.RED : p.status.color();
            }
        }

    }

    private class ItemListing extends Group {

        final Item item;
        final Image icon;
        final Label name;
        final Label detail;
        final CharacterRecord p;

        ItemListing(Item item, CharacterRecord p) {
            this.p = p;
            this.item = item;

            this.icon = new Image(Icons.get(item));
            this.name = new Label(item.name, ui.skin, "emphasis");
            this.detail = new Label(item.vendorDescription(), ui.skin, "default");

            if (!item.canUse(p.classType)) {
                Image unusable = new Image(new TextureRegionDrawable(new TextureRegion(redBackgrnd)));
                addActor(unusable);
                unusable.setSize(WD, HT);
            }

            addActor(this.icon);
            addActor(this.name);
            addActor(this.detail);

            this.icon.setScaling(Scaling.none);
            this.icon.setBounds(6, 8, DIM, DIM);
            this.name.setBounds(DIM + 14, 30, WD - DIM - 22, 22);
            this.name.setEllipsis(true);
            this.detail.setBounds(DIM + 14, 8, WD - DIM - 22, 20);
            this.detail.setEllipsis(true);

            this.setSize(WD, HT);
        }

    }

    private class VendorItem extends Group {

        final Item item;
        final Image icon;
        final Label name;
        final Label detail;
        final Label price;
        final Image canusebkgnd = new Image();

        VendorItem(Item item) {
            this.item = item;

            this.icon = new Image(Icons.get(item));
            this.name = new Label(item.name, ui.skin, "emphasis");
            this.detail = new Label(item.vendorDescription(), ui.skin, "default");
            this.price = new Label("" + item.cost, ui.skin, "emphasis");
            this.canusebkgnd.setDrawable(new TextureRegionDrawable(new TextureRegion(clearBackgrnd)));

            addActor(this.icon);
            addActor(this.name);
            addActor(this.detail);
            addActor(this.price);
            addActor(this.canusebkgnd);

            this.icon.setScaling(Scaling.none);
            this.icon.setBounds(6, 8, DIM, DIM);
            this.name.setBounds(DIM + 14, 30, WD - DIM - 22, 22);
            this.name.setEllipsis(true);
            this.detail.setBounds(DIM + 14, 8, WD - DIM - 22, 20);
            this.detail.setEllipsis(true);
            this.name.setWidth(176);
            this.price.setBounds(238, 30, 60, 22);
            this.price.setAlignment(Align.right);
            this.price.setColor(GOLD);
            this.price.setEllipsis(true);
            this.canusebkgnd.setBounds(0, 0, WD, HT);
            this.canusebkgnd.setTouchable(Touchable.disabled);
            this.canusebkgnd.toBack();

            this.setSize(WD, HT);
        }

        public void setUsable(ClassType ct) {
            boolean canUse = item.canUse(ct);
            if (!canUse) {
                canusebkgnd.setDrawable(new TextureRegionDrawable(new TextureRegion(redBackgrnd)));
            } else {
                canusebkgnd.setDrawable(new TextureRegionDrawable(new TextureRegion(clearBackgrnd)));
            }
        }

    }

    private class InventoryImage extends Image {

        private Item item;
        private final ItemType type;
        private final int slot;

        public InventoryImage(Item item, ItemType type, int slot) {
            super(Icons.get(item));
            setScaling(Scaling.none);
            if (item == null) setDrawable(null);
            this.item = item;
            this.type = type;
            this.slot = slot;
            addListener(new ClickListener(Input.Buttons.LEFT) {
                @Override public void clicked(InputEvent event, float x, float y) {
                    if (selectedItem == null) return;
                    if (type == selectedItem.item.type && selectedItem.item.canUse(selectedPlayer.p.classType)) {
                        equip(selectedPlayer.p, selectedItem.item);
                        itemFocusIndicator.remove(); selectedItem = null;
                        selectedPlayer.setInventoryTable();
                        invPane.setActor(selectedPlayer.invTable);
                    } else Sounds.play(Sound.NEGATIVE_EFFECT);
                }
            });
            addListener(new ClickListener(Input.Buttons.RIGHT) {
                @Override public void clicked(InputEvent event, float x, float y) {
                    unequip(selectedPlayer.p);
                    itemFocusIndicator.remove(); selectedItem = null;
                    selectedPlayer.setInventoryTable();
                    invPane.setActor(selectedPlayer.invTable);
                }
            });
            addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                @Override public void enter(InputEvent event, float x, float y, int pointer, Actor from) {
                    if (pointer != -1) return;
                    hoveredEquipment = InventoryImage.this;
                    refreshEquipmentName();
                }
                @Override public void exit(InputEvent event, float x, float y, int pointer, Actor to) {
                    if (pointer != -1 || hoveredEquipment != InventoryImage.this) return;
                    hoveredEquipment = null;
                    refreshEquipmentName();
                }
            });
        }

        public void equip(CharacterRecord p, Item equip) {
            this.setDrawable(new TextureRegionDrawable(Icons.get(equip)));

            Item current = this.item;
            this.item = equip;

            if (slot == 1) {
                p.weapon = equip;
            }
            if (slot == 2) {
                p.armor = equip;
            }
            if (slot == 3) {
                p.helm = equip;
            }
            if (slot == 4) {
                p.shield = equip;
            }
            if (slot == 5) {
                p.glove = equip;
            }
            if (slot == 6) {
                p.item1 = equip;
            }
            if (slot == 7) {
                p.item2 = equip;
            }

            p.inventory.remove(equip);

            if (current != null) {
                p.inventory.add(current);
            }

            Sounds.play(Sound.TRIGGER);
        }

        public void unequip(CharacterRecord p) {
            this.setDrawable(null);

            Item current = this.item;

            this.item = null;

            if (slot == 1) {
                p.weapon = null;
            }
            if (slot == 2) {
                p.armor = null;
            }
            if (slot == 3) {
                p.helm = null;
            }
            if (slot == 4) {
                p.shield = null;
            }
            if (slot == 5) {
                p.glove = null;
            }
            if (slot == 6) {
                p.item1 = null;
            }
            if (slot == 7) {
                p.item2 = null;
            }

            if (current != null) {
                p.inventory.add(current);
                Sounds.play(Sound.TRIGGER);
            }

        }

    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        redBackgrnd.dispose();
        clearBackgrnd.dispose();
        ui.dispose();
    }

}
