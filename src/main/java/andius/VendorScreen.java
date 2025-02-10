package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import andius.WizardryData.Scenario;
import andius.objects.ClassType;
import andius.objects.Icons;
import andius.objects.Item;
import andius.objects.Item.ItemType;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import utils.AutoFocusScrollPane;
import utils.FrameMaker;
import utils.Utils;

public class VendorScreen implements Screen, Constants {

    private final Context context;
    private final String vendorName;
    private final Role vendorRole;
    private final Texture background;
    private final SpriteBatch batch;
    private final Stage stage;

    private final List<PlayerIndex> playerSelection;

    private final TextButton exit;
    private final TextButton sell;
    private final TextButton buy;
    private final TextButton pool;
    private final TextButton unequip;
    private final TextButton cancel;

    private final Texture redBackgrnd = Utils.fillRectangle(w, h, Color.RED, .25f);
    private final Texture clearBackgrnd = Utils.fillRectangle(w, h, Color.CLEAR, 0f);

    private static final int w = 290;
    private static final int h = 50;
    private static final int dim = 44;
    private static Texture highlighter;

    static {
        Pixmap pix = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        pix.setColor(Color.YELLOW);
        pix.fillRectangle(0, 0, 50, 3);
        pix.fillRectangle(0, 47, 50, 3);
        pix.fillRectangle(0, 0, 3, 50);
        pix.fillRectangle(47, 0, 3, 50);
        highlighter = new Texture(pix);
        pix.dispose();
    }

    Image selectedImage;
    ItemListing selectedItem;
    VendorItem selectedVendorItem;
    PlayerIndex selectedPlayer;
    AutoFocusScrollPane playerPane;
    AutoFocusScrollPane invPane;
    AutoFocusScrollPane vendorPane;
    Table vendorTable;
    Image focusIndicator;
    Image vendorFocusInd;
    Label invDesc;

    public VendorScreen(Context context, Role role, Constants.Map contextMap, String vendorName) {
        this.context = context;
        this.vendorName = vendorName;
        this.vendorRole = role;
        this.batch = new SpriteBatch();
        this.stage = new Stage();
        //this.stage.setDebugAll(true);

        FrameMaker fm = new FrameMaker(SCREEN_WIDTH, SCREEN_HEIGHT);

        focusIndicator = new Image(Utils.fillRectangle(w, h, Color.YELLOW, .35f));
        focusIndicator.setWidth(w);
        focusIndicator.setHeight(h);

        vendorFocusInd = new Image(Utils.fillRectangle(w, h, Color.YELLOW, .35f));
        vendorFocusInd.setWidth(w);
        vendorFocusInd.setHeight(h);

        invDesc = new Label("", Andius.skin, "default-16");

        this.playerSelection = new List<>(Andius.skin, "default-16");
        PlayerIndex[] names = new PlayerIndex[this.context.players().length];
        for (int i = 0; i < this.context.players().length; i++) {
            names[i] = new PlayerIndex(this.context.players()[i]);
        }
        this.playerSelection.setItems(names);

        playerPane = new AutoFocusScrollPane(this.playerSelection, Andius.skin);
        invPane = new AutoFocusScrollPane(playerSelection.getSelected().invTable, Andius.skin);
        invPane.setScrollingDisabled(true, false);

        vendorTable = new Table(Andius.skin);
        vendorTable.align(Align.top);

        filterVendorItems(ItemType.ANY);

        vendorPane = new AutoFocusScrollPane(vendorTable, Andius.skin);
        vendorPane.setScrollingDisabled(true, false);

        this.cancel = new TextButton("LEAVE", Andius.skin, "default-16");
        this.cancel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(contextMap.getScreen());
            }
        });

        this.exit = new TextButton("SAVE", Andius.skin, "default-16");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                for (PlayerIndex pi : VendorScreen.this.playerSelection.getItems()) {
                    pi.save();
                }
                mainGame.setScreen(contextMap.getScreen());
            }
        });

        this.unequip = new TextButton("UNEQUIP", Andius.skin, "default-16");
        this.unequip.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedImage != null && selectedImage.getUserObject() != null) {
                    Item it = (Item) selectedImage.getUserObject();
                    selectedPlayer.invTable.add(new ItemListing(it, selectedPlayer.character));
                    selectedPlayer.invTable.row();
                    selectedImage.setDrawable(new TextureRegionDrawable(Icons.get(it)));
                    selectedImage.setUserObject(null);
                    selectedImage = null;

                    selectedPlayer.acLabel.setText("" + selectedPlayer.calculateAC());

                    if (selectedPlayer.weaponIcon.getUserObject() != null) {
                        Item weap = (Item) selectedPlayer.weaponIcon.getUserObject();
                        selectedPlayer.damageLabel.setText(weap.damage.toString());
                    } else {
                        selectedPlayer.damageLabel.setText("1d2");
                    }
                    Sounds.play(Sound.TRIGGER);
                } else {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                }
            }
        });

        this.pool = new TextButton("POOL GOLD", Andius.skin, "default-16");
        this.pool.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                for (PlayerIndex pi : playerSelection.getItems()) {
                    if (pi != selectedPlayer) {
                        int gold = pi.character.gold;
                        pi.character.adjustGold(-gold);
                        pi.goldLabel.setText("0");
                        selectedPlayer.character.adjustGold(gold);
                    }
                }
                selectedPlayer.goldLabel.setText("" + selectedPlayer.character.gold);
            }
        });

        this.buy = new TextButton("BUY", Andius.skin, "default-16-green");
        this.buy.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedVendorItem != null && selectedPlayer.character.gold >= selectedVendorItem.item.cost) {
                    selectedPlayer.invTable.add(new ItemListing(selectedVendorItem.item.clone(), selectedPlayer.character));
                    selectedPlayer.invTable.row();
                    invPane.setScrollPercentY(100);
                    invPane.layout();
                    selectedPlayer.character.adjustGold(-selectedVendorItem.item.cost);
                    selectedPlayer.goldLabel.setText("" + selectedPlayer.character.gold);
                    if (selectedVendorItem.item.stock == -1) {
                        //nothing - always in stock
                    } else {
                        selectedVendorItem.item.stock--;
                        if (selectedVendorItem.item.stock == 0) {
                            selectedVendorItem.removeActor(focusIndicator);
                            vendorTable.removeActor(selectedVendorItem);
                        }
                    }
                    Sounds.play(Sound.TRIGGER);
                } else {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                }
            }
        });

        this.sell = new TextButton("SELL", Andius.skin, "default-16-green");
        this.sell.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedItem != null) {
                    Item it = Scenario.getItem(selectedItem.item.id, selectedItem.item.name);
                    if (it != null) {
                        selectedItem.removeActor(focusIndicator);
                        selectedPlayer.invTable.removeActor(selectedItem);
                        selectedPlayer.character.adjustGold(selectedItem.item.cost / 2);
                        selectedPlayer.goldLabel.setText("" + selectedPlayer.character.gold);
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

        this.invDesc.setBounds(90, 440, w, 25);

        fm.setBounds(this.playerPane, 50, Andius.SCREEN_HEIGHT - 289, 155, 235);
        fm.setBounds(this.invPane, 280, 23, w, 690);
        fm.setBounds(this.vendorPane, 713, 23, w, 690);

        this.buy.setBounds(600, 600, 70, 30);
        this.sell.setBounds(600, 550, 70, 30);

        this.pool.setBounds(30, 70, 100, 30);
        this.exit.setBounds(30, 30, 100, 30);

        this.unequip.setBounds(150, 70, 100, 30);
        this.cancel.setBounds(150, 30, 100, 30);

        stage.addActor(playerPane);
        stage.addActor(invDesc);
        stage.addActor(invPane);
        stage.addActor(vendorPane);
        stage.addActor(sell);
        stage.addActor(pool);
        stage.addActor(buy);
        stage.addActor(exit);
        stage.addActor(cancel);
        stage.addActor(unequip);

        ButtonGroup<CheckBox> filterGroup = new ButtonGroup<>();
        filterGroup.setMaxCheckCount(1);
        filterGroup.setMinCheckCount(1);
        filterGroup.setUncheckLast(true);

        int y = 500;
        for (ItemType type : ItemType.values()) {
            CheckBox cb = new CheckBox(type.toString(), Andius.skin, "default-16");
            cb.setUserObject(type);
            cb.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    filterVendorItems((ItemType) actor.getUserObject());
                }
            });
            cb.setPosition(590, y -= 30);
            filterGroup.add(cb);
            stage.addActor(cb);
        }

        ChangeListener cl = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                selectedItem = null;
                if (selectedPlayer != null) {
                    for (Actor a : selectedPlayer.icons) {
                        a.remove();
                    }
                }
                selectedPlayer = playerSelection.getSelected();
                for (Actor a : selectedPlayer.icons) {
                    stage.addActor(a);
                }
                invPane.clearChildren();
                invPane.setWidget(selectedPlayer.invTable);

                for (Cell cell : vendorTable.getCells()) {
                    if (cell.getActor() instanceof VendorItem) {
                        VendorItem vi = (VendorItem) cell.getActor();
                        vi.setUsable(selectedPlayer.character.classType);
                    }
                }
            }
        };

        playerSelection.addListener(cl);
        cl.changed(null, null);

        fm.setBounds(null, 50, 125, 150, 80);

        fm.setBounds(null, 40, 375, 48, 48);
        fm.setBounds(null, 110, 375, 48, 48);
        fm.setBounds(null, 180, 375, 48, 48);

        fm.setBounds(null, 40, 305, 48, 48);
        fm.setBounds(null, 110, 305, 48, 48);
        fm.setBounds(null, 180, 305, 48, 48);

        fm.setBounds(null, 40, 235, 48, 48);
        fm.setBounds(null, 180, 235, 48, 48);

        this.background = fm.build();
    }

    private void filterVendorItems(ItemType type) {

        vendorTable.clear();

        if (vendorFocusInd.getParent() != null) {
            vendorFocusInd.getParent().removeActor(vendorFocusInd);
        }

        vendorTable.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {

                if (event.toString().equals("touchDown")) {
                    if (vendorFocusInd.getParent() != null) {
                        vendorFocusInd.getParent().removeActor(vendorFocusInd);
                    }
                    if (event.getTarget() instanceof VendorItem) {
                        selectedVendorItem = (VendorItem) event.getTarget();
                        selectedVendorItem.addActor(vendorFocusInd);
                    } else if (event.getTarget().getParent() instanceof VendorItem) {
                        selectedVendorItem = (VendorItem) event.getTarget().getParent();
                        selectedVendorItem.addActor(vendorFocusInd);
                    }
                }

                return false;
            }
        });

        java.util.List<Item> sellables = new ArrayList<>();
        for (Scenario sc : Scenario.values()) {
            for (Item it : sc.items()) {
                if (it.stock != 0 && !sellables.contains(it)) {
                    if (type == ItemType.ANY || type.equals(it.type)) {
                        sellables.add(it);
                    }
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
            if (this.vendorRole == Role.MERCHANT1 && it.cost <= 500) {
                vendorTable.add(new VendorItem(it));
                vendorTable.row();
            } else if (this.vendorRole == Role.MERCHANT2 && it.cost <= 10000) {
                vendorTable.add(new VendorItem(it));
                vendorTable.row();
            } else if (this.vendorRole == Role.MERCHANT) {
                vendorTable.add(new VendorItem(it));
                vendorTable.row();
            }
        }

        if (selectedPlayer != null) {
            for (Cell cell : vendorTable.getCells()) {
                if (cell.getActor() instanceof VendorItem) {
                    VendorItem vi = (VendorItem) cell.getActor();
                    vi.setUsable(selectedPlayer.character.classType);
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
        if (selectedImage != null) {
            batch.draw(highlighter, selectedImage.getX() - 3, selectedImage.getY() - 4);
        }

        Andius.font16.draw(batch, this.vendorName.toUpperCase(), 400, 750);
        Andius.font16.draw(batch, "AC", 60, 190);
        Andius.font16.draw(batch, "DAMG", 60, 170);
        Andius.font16.draw(batch, "GOLD", 60, 150);

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

        if (Gdx.input.isKeyJustPressed(Input.Keys.U)) {
            this.unequip.toggle();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            this.cancel.toggle();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            this.exit.toggle();
        }

    }

    private class PlayerIndex {

        final SaveGame.CharacterRecord character;
        final Table invTable = new Table(Andius.skin);

        final Label status;

        final Image avatar;
        final Image weaponIcon;
        final Image armorIcon;
        final Image helmIcon;
        final Image shieldIcon;
        final Image glovesIcon;
        final Image item1Icon;
        final Image item2Icon;
        final Label acLabel;
        final Label damageLabel;
        final Label goldLabel;

        final Actor[] icons = new Actor[12];

        PlayerIndex(SaveGame.CharacterRecord sp) {
            this.character = sp;

            invTable.align(Align.top);

            for (Item it : character.inventory) {
                invTable.add(new ItemListing(it, sp));
                invTable.row();
            }

            invTable.addListener(new EventListener() {
                @Override
                public boolean handle(Event event) {

                    if (event.toString().equals("touchDown")) {
                        if (focusIndicator.getParent() != null) {
                            focusIndicator.getParent().removeActor(focusIndicator);
                        }
                        if (event.getTarget() instanceof ItemListing) {
                            selectedItem = (ItemListing) event.getTarget();
                            selectedItem.addActor(focusIndicator);
                        } else if (event.getTarget().getParent() instanceof ItemListing) {
                            selectedItem = (ItemListing) event.getTarget().getParent();
                            selectedItem.addActor(focusIndicator);
                        }
                    }

                    return false;
                }
            }
            );

            avatar = new Image(Andius.faceTiles[sp.portaitIndex]);

            armorIcon = make(ItemType.ARMOR, sp.armor, Icons.get(sp.armor), 40 + 2, 375 + 2);
            helmIcon = make(ItemType.HELMET, sp.helm, Icons.get(sp.helm), 110 + 2, 375 + 2);
            shieldIcon = make(ItemType.SHIELD, sp.shield, Icons.get(sp.shield), 180 + 2, 375 + 2);

            weaponIcon = make(ItemType.WEAPON, sp.weapon, Icons.get(sp.weapon), 40, 305 + 2);
            avatar.setPosition(110, 305);
            glovesIcon = make(ItemType.GAUNTLET, sp.glove, Icons.get(sp.glove), 180, 305 + 2);

            item1Icon = make(ItemType.MISC, sp.item1, Icons.get(sp.item1), 40 + 2, 235 + 2);
            item2Icon = make(ItemType.MISC, sp.item2, Icons.get(sp.item2), 180 + 2, 235 + 2);

            acLabel = new Label("" + character.calculateAC(), Andius.skin, "default-16");
            acLabel.setPosition(130, 174);

            damageLabel = new Label(character.weapon != null ? character.weapon.damage.toString() : "1d2", Andius.skin, "default-16");
            damageLabel.setPosition(130, 154);

            goldLabel = new Label("" + character.gold, Andius.skin, "default-16");
            goldLabel.setPosition(130, 134);

            this.status = new PlayerStatusLabel(character);
            this.status.setPosition(50, 740);

            icons[0] = avatar;
            icons[1] = weaponIcon;
            icons[2] = armorIcon;
            icons[3] = helmIcon;
            icons[4] = shieldIcon;
            icons[5] = glovesIcon;
            icons[6] = item1Icon;
            icons[7] = item2Icon;
            icons[8] = acLabel;
            icons[9] = damageLabel;
            icons[10] = goldLabel;
            icons[11] = status;
        }

        private Image make(ItemType type, Item it, TextureRegion tr, int x, int y) {
            Image im = new Image(tr);
            im.setX(x);
            im.setY(y);
            im.setUserObject(it);
            im.addListener(new InvItemChangeListener(type));
            return im;
        }

        private void save() {

            character.weapon = (Item) weaponIcon.getUserObject();
            character.armor = (Item) armorIcon.getUserObject();
            character.helm = (Item) helmIcon.getUserObject();
            character.shield = (Item) shieldIcon.getUserObject();
            character.glove = (Item) glovesIcon.getUserObject();
            character.item1 = (Item) item1Icon.getUserObject();
            character.item2 = (Item) item2Icon.getUserObject();

            character.inventory.clear();
            for (Actor a : invTable.getChildren()) {
                if (a instanceof ItemListing) {
                    ItemListing il = (ItemListing) a;
                    character.inventory.add(il.item);
                }
            }

        }

        @Override
        public String toString() {
            return character.name.toUpperCase();
        }

        private class InvItemChangeListener implements EventListener {

            final ItemType type;

            InvItemChangeListener(ItemType type) {
                this.type = type;
            }

            @Override
            public boolean handle(Event event) {
                if (selectedItem != null && event.toString().equals("touchDown")) {
                    if (this.type == selectedItem.item.type && selectedItem.item.canUse(PlayerIndex.this.character.classType)) {
                        Sounds.play(Sound.TRIGGER);
                        Item old = (Item) event.getTarget().getUserObject();
                        event.getTarget().setUserObject(selectedItem.item);
                        ((Image) event.getTarget()).setDrawable(new TextureRegionDrawable(Icons.get(selectedItem.item)));

                        if (old != null && !old.name.equals("BROKEN ITEM")) {
                            PlayerIndex.this.invTable.add(new ItemListing(old, selectedItem.rec));
                            PlayerIndex.this.invTable.row();
                        }

                        selectedItem.removeActor(focusIndicator);
                        PlayerIndex.this.invTable.removeActor(selectedItem);
                        selectedItem = null;

                        acLabel.setText("" + calculateAC());

                        if (PlayerIndex.this.weaponIcon.getUserObject() != null) {
                            Item weap = (Item) PlayerIndex.this.weaponIcon.getUserObject();
                            selectedPlayer.damageLabel.setText(weap.damage.toString());
                        } else {
                            selectedPlayer.damageLabel.setText("1d2");
                        }

                    } else {
                        Sounds.play(Sound.NEGATIVE_EFFECT);
                    }
                } else if (event.toString().equals("enter")) {
                    Item i = (Item) event.getTarget().getUserObject();
                    invDesc.setText(i != null ? i.name : "");
                    selectedImage = (Image) event.getTarget();
                } else if (event.toString().equals("exit")) {
                    invDesc.setText("");
                }

                return false;
            }

        }

        private int calculateAC() {
            int ac = 10;

            Item weapon = (Item) weaponIcon.getUserObject();
            Item armor = (Item) armorIcon.getUserObject();
            Item helm = (Item) helmIcon.getUserObject();
            Item glove = (Item) glovesIcon.getUserObject();
            Item shield = (Item) shieldIcon.getUserObject();
            Item item1 = (Item) item1Icon.getUserObject();
            Item item2 = (Item) item2Icon.getUserObject();

            if (weapon != null) {
                ac -= weapon.armourClass;
            }
            if (armor != null) {
                ac -= armor.armourClass;
            }
            if (helm != null) {
                ac -= helm.armourClass;
            }
            if (glove != null) {
                ac -= glove.armourClass;
            }
            if (shield != null) {
                ac -= shield.armourClass;
            }
            if (item1 != null) {
                ac -= item1.armourClass;
            }
            if (item2 != null) {
                ac -= item2.armourClass;
            }
            if (this.character.classType == ClassType.NINJA) {
                ac = (this.character.level / 3) - 2;
            }
            return ac;
        }
    }

    private class ItemListing extends Group {

        Item item;
        final Image icon;
        final Label label;
        final Label price;
        final Image canusebkgnd;
        final CharacterRecord rec;

        ItemListing(Item item, CharacterRecord rec) {
            this.rec = rec;
            this.item = item;

            this.icon = new Image(Icons.get(item));
            this.label = new Label(item.name, Andius.skin, "default-16");
            this.price = new Label("" + item.cost / 2, Andius.skin, "default-16");

            this.canusebkgnd = new Image();
            boolean canUse = item.canUse(rec.classType);
            if (!canUse) {
                canusebkgnd.setDrawable(new TextureRegionDrawable(new TextureRegion(redBackgrnd)));
            } else {
                canusebkgnd.setDrawable(new TextureRegionDrawable(new TextureRegion(clearBackgrnd)));
            }

            addActor(this.icon);
            addActor(this.label);
            addActor(this.price);
            addActor(this.canusebkgnd);

            float x = getX();
            this.icon.setBounds(x + 3, getY() + 3, dim, dim);
            this.label.setPosition(x += 54, getY() + 10);
            this.price.setPosition(x += 175, getY() + 10);
            this.canusebkgnd.setBounds(getX(), getY(), w, h);
            this.setBounds(getX(), getY(), w, h);

        }

    }

    private class VendorItem extends Group {

        final Item item;
        final Image icon;
        final Label label;
        final Label price;
        final Image canusebkgnd = new Image();

        VendorItem(Item item) {
            this.item = item;

            this.icon = new Image(Icons.get(item));
            this.label = new Label(item.name, Andius.skin, "default-16");
            this.price = new Label("" + item.cost, Andius.skin, "default-16");
            this.canusebkgnd.setDrawable(new TextureRegionDrawable(new TextureRegion(clearBackgrnd)));

            addActor(this.icon);
            addActor(this.label);
            addActor(this.price);
            addActor(this.canusebkgnd);

            float x = getX();
            this.icon.setBounds(x + 3, getY() + 3, dim, dim);
            this.label.setPosition(x += 54, getY() + 10);
            this.price.setPosition(x += 175, getY() + 10);
            this.canusebkgnd.setBounds(getX(), getY(), w, h);
            this.setBounds(getX(), getY(), w, h);
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

    private class PlayerStatusLabel extends Label {

        private final CharacterRecord rec;

        public PlayerStatusLabel(CharacterRecord rec) {
            super("", Andius.skin, "default-16");
            this.rec = rec;
            setColor(rec.status.color());
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.StringBuilder getText() {
            return new com.badlogic.gdx.utils.StringBuilder(String.format("%s  LVL %d  %s  %s", rec.race.toString(), rec.level,
                    rec.classType.toString(), rec.isDead() ? "DEAD" : rec.status));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

        @Override
        public Color getColor() {
            return rec.isDead() ? Color.RED : rec.status.color();
        }

    }

}
