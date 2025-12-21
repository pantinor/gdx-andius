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
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import utils.AutoFocusScrollPane;
import utils.FrameMaker;
import utils.Utils;

public class VendorScreen implements Screen, Constants {

    private final Context context;
    private final List<Item> vendorItems;
    private final String vendorName;

    private final Texture background;
    private final SpriteBatch batch;
    private final Stage stage;

    private final TextButton exit;
    private final TextButton sell;
    private final TextButton buy;
    private final TextButton pool;

    private Image selectedImage;
    private ItemListing selectedItem;
    private VendorItem selectedVendorItem;
    private PlayerIndex selectedPlayer;

    private AutoFocusScrollPane playerPane;
    private AutoFocusScrollPane invPane;
    private AutoFocusScrollPane vendorPane;

    private Table playerTable;
    private Table vendorTable;
    private Label invDesc;

    private Image playerFocusIndicator, itemFocusIndicator, vendorFocusIndicator;

    private final Texture redBackgrnd = Utils.fillRectangle(10, 10, Color.RED, .25f);
    private final Texture clearBackgrnd = Utils.fillRectangle(10, 10, Color.CLEAR, 0f);

    private final GlyphLayout ITEMDESCLAYOUT = new GlyphLayout(Andius.font12, "", Color.WHITE, 226, Align.left, true);

    private static final int WD = 246;
    private static final int HT = 50;
    private static final int DIM = 44;
    private static final int PDIM = 60;

    public VendorScreen(Context context, Constants.Map contextMap, List<Item> vendorItems, String vendorName) {
        this.context = context;
        this.vendorItems = vendorItems;
        this.vendorName = vendorName;
        this.batch = new SpriteBatch();
        this.stage = new Stage();
        //this.stage.setDebugAll(true);

        FrameMaker fm = new FrameMaker(SCREEN_WIDTH, SCREEN_HEIGHT);

        playerFocusIndicator = new Image(Utils.fillRectangle(10, 10, Color.GREEN, .25f));
        playerFocusIndicator.setWidth(600);
        playerFocusIndicator.setHeight(PDIM);

        itemFocusIndicator = new Image(Utils.fillRectangle(10, 10, Color.GREEN, .25f));
        itemFocusIndicator.setWidth(WD);
        itemFocusIndicator.setHeight(HT);

        vendorFocusIndicator = new Image(Utils.fillRectangle(10, 10, Color.GREEN, .25f));
        vendorFocusIndicator.setWidth(300);
        vendorFocusIndicator.setHeight(HT);

        invDesc = new Label("", Andius.skin, "default-16");

        playerTable = new Table(Andius.skin);
        playerTable.left().setFillParent(true);
        for (int i = 0; i < this.context.players().length; i++) {
            PlayerIndex pi = new PlayerIndex(this.context.players()[i]);
            playerTable.add(pi);
            playerTable.row();
            if (selectedPlayer == null) {
                selectedPlayer = pi;
                selectedPlayer.addActor(playerFocusIndicator);
            }
        }

        playerTable.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                if (selectedPlayer != event.getTarget().getParent()) {
                    selectedPlayer = (PlayerIndex) event.getTarget().getParent();
                    playerFocusIndicator.remove();
                    selectedPlayer.addActor(playerFocusIndicator);

                    invPane.clearChildren();
                    invPane.setActor(selectedPlayer.invTable);
                    selectedItem = null;
                    itemFocusIndicator.remove();

                    for (Cell cell : vendorTable.getCells()) {
                        if (cell.getActor() instanceof VendorItem) {
                            VendorItem vi = (VendorItem) cell.getActor();
                            vi.setUsable(selectedPlayer.p.classType);
                        }
                    }
                }

                InventoryImage ii = selectedPlayer.itemClicked(x, y);
                if (ii != null) {
                    if (selectedItem != null) {
                        if (ii.type == selectedItem.item.type && selectedItem.item.canUse(selectedPlayer.p.classType)) {
                            ii.equip(selectedPlayer.p, selectedItem.item);
                            itemFocusIndicator.remove();
                            selectedItem = null;
                            selectedPlayer.setInventoryTable();
                        } else {
                            Sounds.play(Sound.NEGATIVE_EFFECT);
                        }
                    }
                }
            }
        });

        playerTable.addListener(new ClickListener(Input.Buttons.RIGHT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                if (selectedPlayer != event.getTarget().getParent()) {
                    selectedPlayer = (PlayerIndex) event.getTarget().getParent();
                    playerFocusIndicator.remove();
                    selectedPlayer.addActor(playerFocusIndicator);

                    invPane.clearChildren();
                    invPane.setActor(selectedPlayer.invTable);
                    selectedItem = null;
                    itemFocusIndicator.remove();

                    for (Cell cell : vendorTable.getCells()) {
                        if (cell.getActor() instanceof VendorItem) {
                            VendorItem vi = (VendorItem) cell.getActor();
                            vi.setUsable(selectedPlayer.p.classType);
                        }
                    }
                }

                InventoryImage ii = selectedPlayer.itemClicked(x, y);
                if (ii != null) {
                    ii.unequip(selectedPlayer.p);
                    invPane.clearChildren();
                    selectedPlayer.setInventoryTable();
                }
            }
        });

        playerPane = new AutoFocusScrollPane(playerTable, Andius.skin);
        playerPane.setScrollingDisabled(true, false);

        invPane = new AutoFocusScrollPane(selectedPlayer.invTable, Andius.skin);
        invPane.setScrollingDisabled(true, false);

        vendorTable = new Table(Andius.skin);
        vendorTable.align(Align.top);

        filterVendorItems(ItemType.ANY);

        vendorPane = new AutoFocusScrollPane(vendorTable, Andius.skin);
        vendorPane.setScrollingDisabled(true, false);

        int x = 280;
        this.buy = new TextButton("BUY", Andius.skin, "default-16-green");
        this.buy.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedVendorItem != null && selectedPlayer.p.gold >= selectedVendorItem.item.cost) {

                    selectedPlayer.p.adjustGold(-selectedVendorItem.item.cost);
                    selectedPlayer.p.inventory.add(selectedVendorItem.item);

                    selectedPlayer.setInventoryTable();

                    Sounds.play(Sound.TRIGGER);
                } else {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                }
            }
        });
        this.buy.setBounds(x, 200, 80, 40);
        x += 84;
        this.sell = new TextButton("SELL", Andius.skin, "default-16-green");
        this.sell.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedItem != null) {
                    Item it = item(selectedItem.item.id);
                    if (it != null) {
                        selectedItem.removeActor(vendorFocusIndicator);
                        selectedPlayer.invTable.removeActor(selectedItem);
                        selectedPlayer.p.adjustGold(selectedItem.item.cost / 2);
                        if (it.stock == 0) {
                            it.stock = 1;
                            vendorTable.add(new VendorItem(it));
                            vendorTable.row();
                        } else if (it.stock == -1) {
                            //nothing - always in stock
                        } else {
                            it.stock++;
                        }
                        selectedItem = null;
                        Sounds.play(Sound.TRIGGER);
                    } else {
                        Sounds.play(Sound.NEGATIVE_EFFECT);
                    }
                } else {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                }
            }
        });
        this.sell.setBounds(x, 200, 80, 40);
        x = 280;
        this.exit = new TextButton("EXIT", Andius.skin, "default-16");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(contextMap.getScreen());
            }
        });
        this.exit.setBounds(x, 150, 80, 40);
        x += 84;
        this.pool = new TextButton("POOL", Andius.skin, "default-16-green");
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
        this.pool.setBounds(x, 150, 80, 40);

        fm.setBounds(playerPane, 20, 375, 600, 376);
        fm.setBounds(invPane, 20, 10, 246, 348);
        fm.setBounds(vendorPane, 660, 10, 300, 740);

        stage.addActor(playerPane);
        stage.addActor(invPane);
        stage.addActor(vendorPane);

        stage.addActor(sell);
        stage.addActor(pool);
        stage.addActor(buy);
        stage.addActor(exit);

        ButtonGroup<CheckBox> filterGroup = new ButtonGroup<>();
        filterGroup.setMaxCheckCount(1);
        filterGroup.setMinCheckCount(1);
        filterGroup.setUncheckLast(true);

        int y = 360;
        for (ItemType type : ItemType.values()) {
            CheckBox cb = new CheckBox(type.toString(), Andius.skin, "default-16");
            cb.setUserObject(type);
            cb.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    filterVendorItems((ItemType) actor.getUserObject());
                }
            });
            cb.setPosition(500, y -= 30);
            filterGroup.add(cb);
            stage.addActor(cb);
        }

        this.background = fm.build();
    }

    private void filterVendorItems(ItemType type) {

        vendorTable.clear();

        if (vendorFocusIndicator.getParent() != null) {
            vendorFocusIndicator.getParent().removeActor(vendorFocusIndicator);
        }

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
                    } else if (event.getTarget().getParent() instanceof VendorItem) {
                        selectedVendorItem = (VendorItem) event.getTarget().getParent();
                        selectedVendorItem.addActor(vendorFocusIndicator);
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

        Collections.sort(sellables, new Comparator<Item>() {
            @Override
            public int compare(Item it1, Item it2) {
                return Long.compare(it1.cost, it2.cost);
            }
        });

        for (Item it : sellables) {
            vendorTable.add(new VendorItem(it));
            vendorTable.row();
        }

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

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(this.background, 0, 0);
        batch.end();

        stage.act();
        stage.draw();

        batch.begin();

        Andius.font16.draw(batch, this.vendorName.toUpperCase(), 400, 30);

        if (selectedPlayer != null) {
            int y = 360;
            Andius.font14.draw(batch, selectedPlayer.p.weapon != null ? selectedPlayer.p.weapon.name : "-", 285, y);
            Andius.font14.draw(batch, selectedPlayer.p.armor != null ? selectedPlayer.p.armor.name : "-", 285, y -= 15);
            Andius.font14.draw(batch, selectedPlayer.p.helm != null ? selectedPlayer.p.helm.name : "-", 285, y -= 15);
            Andius.font14.draw(batch, selectedPlayer.p.shield != null ? selectedPlayer.p.shield.name : "-", 285, y -= 15);
            Andius.font14.draw(batch, selectedPlayer.p.glove != null ? selectedPlayer.p.glove.name : "-", 285, y -= 15);
            Andius.font14.draw(batch, selectedPlayer.p.item1 != null ? selectedPlayer.p.item1.name : "-", 285, y -= 15);
            Andius.font14.draw(batch, selectedPlayer.p.item2 != null ? selectedPlayer.p.item2.name : "-", 285, y -= 15);
        }

        if (selectedItem != null) {
            Andius.font12.draw(batch, ITEMDESCLAYOUT, 285, 130);
        }

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            this.pool.toggle();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            this.sell.toggle();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            this.buy.toggle();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            this.exit.toggle();
        }

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

        private final Table invTable = new Table(Andius.skin);

        public PlayerIndex(CharacterRecord p) {
            this.p = p;
            this.label = new PlayerLabel();
            this.avatar = new Image(Andius.faceTiles[p.portaitIndex]);

            this.weaponIcon = new InventoryImage(p.weapon, ItemType.WEAPON, 1);
            this.armorIcon = new InventoryImage(p.armor, ItemType.ARMOR, 2);
            this.helmIcon = new InventoryImage(p.helm, ItemType.HELMET, 3);
            this.shieldIcon = new InventoryImage(p.shield, ItemType.SHIELD, 4);
            this.glovesIcon = new InventoryImage(p.glove, ItemType.GAUNTLET, 5);
            this.item1Icon = new InventoryImage(p.item1, ItemType.MISC, 6);
            this.item2Icon = new InventoryImage(p.item2, ItemType.MISC, 7);

            addActor(this.avatar);
            addActor(this.label);
            addActor(this.weaponIcon);
            addActor(this.armorIcon);
            addActor(this.helmIcon);
            addActor(this.shieldIcon);
            addActor(this.glovesIcon);
            addActor(this.item1Icon);
            addActor(this.item2Icon);

            setBounds(0, 0, 0, PDIM + 2);

            float x = 0;
            float dim = 48;
            this.avatar.setPosition(x, 5);
            this.label.setBounds(x += dim + 3, 0, 210, HT);
            this.weaponIcon.setPosition(x += 210, 5);
            this.armorIcon.setPosition(x += dim, 5);
            this.helmIcon.setPosition(x += dim, 5);
            this.shieldIcon.setPosition(x += dim, 5);
            this.glovesIcon.setPosition(x += dim, 5);
            this.item1Icon.setPosition(x += dim, 5);
            this.item2Icon.setPosition(x += dim, 5);

            setInventoryTable();

        }

        private void setInventoryTable() {

            if (invPane != null) {
                invPane.clearChildren();
            }

            invTable.align(Align.top);
            invTable.clear();

            for (Item it : p.inventory) {
                invTable.add(new ItemListing(it, p));
                invTable.row();
            }

            invTable.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    itemFocusIndicator.remove();
                    if (event.getTarget() instanceof ItemListing) {
                        selectedItem = (ItemListing) event.getTarget();
                        selectedItem.addActor(itemFocusIndicator);
                        ITEMDESCLAYOUT.setText(Andius.font12, selectedItem.item.briefDescription(), Color.WHITE, 226, Align.left, true);
                    } else if (event.getTarget().getParent() instanceof ItemListing) {
                        selectedItem = (ItemListing) event.getTarget().getParent();
                        selectedItem.addActor(itemFocusIndicator);
                        ITEMDESCLAYOUT.setText(Andius.font12, selectedItem.item.briefDescription(), Color.WHITE, 226, Align.left, true);
                    }
                }
            });

            if (invPane != null) {
                invPane.setActor(invTable);
            }
        }

        private class PlayerLabel extends Label {

            private final java.util.List<Integer> magicPoints = new ArrayList<>();

            public PlayerLabel() {
                super("", Andius.skin, "default-12");
                setColor(p.status.color());
                setText(getText());
            }

            @Override
            public com.badlogic.gdx.utils.CharArray getText() {

                com.badlogic.gdx.utils.CharArray sb = new com.badlogic.gdx.utils.CharArray();

                sb.append(String.format("%s  LVL %d  %s  %s\n", p.name.toUpperCase(), p.level, p.race.toString(), p.classType.toString()));
                sb.append(String.format("HP: %d /%d AC: %d ST: %s\n", p.hp, p.maxhp, p.calculateAC(), p.status.toString()));
                sb.append(String.format("GOLD: %d EXP: %d\n", p.gold, p.exp));

                return sb;
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

        private InventoryImage itemClicked(float x, float y) {
            if (weaponIcon.hit(x - weaponIcon.getX(), y - getY() - weaponIcon.getY(), false) != null) {
                return weaponIcon;
            }
            if (armorIcon.hit(x - armorIcon.getX(), y - getY() - armorIcon.getY(), false) != null) {
                return armorIcon;
            }
            if (helmIcon.hit(x - helmIcon.getX(), y - getY() - helmIcon.getY(), false) != null) {
                return helmIcon;
            }
            if (shieldIcon.hit(x - shieldIcon.getX(), y - getY() - shieldIcon.getY(), false) != null) {
                return shieldIcon;
            }
            if (glovesIcon.hit(x - glovesIcon.getX(), y - getY() - glovesIcon.getY(), false) != null) {
                return glovesIcon;
            }
            if (item1Icon.hit(x - item1Icon.getX(), y - getY() - item1Icon.getY(), false) != null) {
                return item1Icon;
            }
            if (item2Icon.hit(x - item2Icon.getX(), y - getY() - item2Icon.getY(), false) != null) {
                return item2Icon;
            }
            return null;
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
            this.name = new Label(item.name, Andius.skin, "default-16");
            this.detail = new Label(item.vendorDescription(), Andius.skin, "default-12");

            if (!item.canUse(p.classType)) {
                Image unusable = new Image(new TextureRegionDrawable(new TextureRegion(redBackgrnd)));
                addActor(unusable);
                unusable.setSize(WD, HT);
            }

            addActor(this.icon);
            addActor(this.name);
            addActor(this.detail);

            this.icon.setBounds(3, 3, DIM, DIM);
            this.name.setPosition(DIM + 10, 20);
            this.detail.setPosition(DIM + 10, 3);

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
            this.name = new Label(item.name, Andius.skin, "default-16");
            this.detail = new Label(item.vendorDescription(), Andius.skin, "default-12");
            this.price = new Label("" + item.cost, Andius.skin, "default-16");
            this.canusebkgnd.setDrawable(new TextureRegionDrawable(new TextureRegion(clearBackgrnd)));

            addActor(this.icon);
            addActor(this.name);
            addActor(this.detail);
            addActor(this.price);
            addActor(this.canusebkgnd);

            this.icon.setBounds(3, 3, DIM, DIM);
            this.name.setPosition(DIM + 10, 20);
            this.detail.setPosition(DIM + 10, 3);
            this.price.setPosition(215, 20);
            this.canusebkgnd.setBounds(0, 0, 300, HT);

            this.setSize(280, HT);
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
            this.item = item;
            this.type = type;
            this.slot = slot;
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
                p.item2 = equip;
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
            this.setDrawable(new TextureRegionDrawable(Icons.get(Icons.QUESTION_MARK)));

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
                p.item2 = null;
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
    }

    public Item item(int id) {
        for (Item it : vendorItems) {
            if (it.id == id) {
                return it;
            }
        }
        return null;
    }

    public Item item(String name) {
        for (Item it : vendorItems) {
            if (it.name.equalsIgnoreCase(name)) {
                return it;
            }
        }
        return null;
    }

}
