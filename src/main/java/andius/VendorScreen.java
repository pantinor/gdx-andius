package andius;

import static andius.Andius.mainGame;
import andius.WizardryData.Scenario;
import andius.objects.ClassType;
import andius.objects.Item;
import andius.objects.Item.ItemType;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
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
import utils.Utils;

public class VendorScreen implements Screen, Constants {

    private final Context context;
    private final String vendorName;
    private final Texture hud;
    private final SpriteBatch batch;
    private final Stage stage;

    private final List<PlayerIndex> playerSelection;

    private final TextButton exit;
    private final TextButton sell;
    private final TextButton buy;
    private final TextButton pool;
    private final TextButton unequip;
    private final TextButton cancel;

    private final TextureRegion[] invIcons = new TextureRegion[67 * 12];

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
        this.hud = new Texture(Gdx.files.classpath("assets/data/vendor.png"));
        this.batch = new SpriteBatch();
        this.stage = new Stage();

        focusIndicator = new Image(Utils.fillRectangle(w, h, Color.YELLOW, .35f));
        focusIndicator.setWidth(w);
        focusIndicator.setHeight(h);

        vendorFocusInd = new Image(Utils.fillRectangle(w, h, Color.YELLOW, .35f));
        vendorFocusInd.setWidth(w);
        vendorFocusInd.setHeight(h);

        invDesc = new Label("", Andius.skin, "larger");

        FileHandle fh = Gdx.files.classpath("assets/data/inventory.png");
        Texture tx = new Texture(fh);
        int canvasGridWidth = tx.getWidth() / 44;
        int canvasGridHeight = tx.getHeight() / 44;

        TextureRegion[][] inv = TextureRegion.split(tx, 44, 44);
        for (int row = 0; row < canvasGridHeight; row++) {
            for (int col = 0; col < canvasGridWidth; col++) {
                Image img = new Image(inv[row][col]);
                img.setName("" + (row * canvasGridWidth + col));
                invIcons[row * canvasGridWidth + col] = inv[row][col];
            }
        }

        this.playerSelection = new List<>(Andius.skin, "larger");
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

        java.util.List<Item> sellables = new ArrayList<>();
        for (Scenario sc : Scenario.values()) {
            for (Item it : sc.items()) {
                if (it.stock != 0 && !sellables.contains(it)) {
                    sellables.add(it);
                }
            }
        }

        Collections.sort(sellables, new Comparator<Item>() {
            @Override
            public int compare(Item it1, Item it2) {
                if (it1.type == it2.type) {
                    if (it1.name.equalsIgnoreCase(it2.name)) {
                        return Long.compare(it1.cost, it2.cost);
                    } else {
                        return it1.name.compareTo(it2.name);
                    }
                }
                return it1.type.toString().compareTo(it2.type.toString());
            }
        });

        for (Item it : sellables) {
            if (role == Role.MERCHANT1 && it.cost <= 500) {
                vendorTable.add(new VendorItem(it));
                vendorTable.row();
            } else if (role == Role.MERCHANT2 && it.cost <= 10000) {
                vendorTable.add(new VendorItem(it));
                vendorTable.row();
            } else if (role == Role.MERCHANT) {
                vendorTable.add(new VendorItem(it));
                vendorTable.row();
            }
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
        }
        );
        vendorPane = new AutoFocusScrollPane(vendorTable, Andius.skin);
        vendorPane.setScrollingDisabled(true, false);

        this.cancel = new TextButton("LEAVE", Andius.skin, "red-larger");
        this.cancel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(contextMap.getScreen());
            }
        });

        this.exit = new TextButton("SAVE", Andius.skin, "red-larger");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                for (PlayerIndex pi : VendorScreen.this.playerSelection.getItems()) {
                    pi.save();
                }
                mainGame.setScreen(contextMap.getScreen());
            }
        });

        this.unequip = new TextButton("UNEQUIP", Andius.skin, "red-larger");
        this.unequip.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedImage != null && selectedImage.getUserObject() != null) {
                    Item it = (Item) selectedImage.getUserObject();
                    selectedPlayer.invTable.add(new ItemListing(it, selectedPlayer.character));
                    selectedPlayer.invTable.row();
                    selectedImage.setDrawable(new TextureRegionDrawable(icon(null)));
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

        this.pool = new TextButton("POOL GOLD", Andius.skin, "red-larger");
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

        this.buy = new TextButton("BUY", Andius.skin, "red-larger");
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

        this.sell = new TextButton("SELL", Andius.skin, "red-larger");
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

        this.playerPane.setBounds(77, Andius.SCREEN_HEIGHT - 289, 159, 235);
        this.invDesc.setBounds(90, 440, w, 25);
        this.invPane.setBounds(326, 23, w, 690);
        this.vendorPane.setBounds(713, 23, w, 690);

        this.buy.setBounds(630, 600, 65, 40);
        this.sell.setBounds(630, 550, 65, 40);

        this.pool.setBounds(40, 70, 100, 40);
        this.unequip.setBounds(160, 70, 100, 40);
        this.exit.setBounds(40, 20, 100, 40);
        this.cancel.setBounds(160, 20, 100, 40);

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

    }

    @Override

    public void show() {
        Gdx.input.setInputProcessor(stage);

    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(this.hud, 0, 0);
        batch.end();

        stage.act();
        stage.draw();

        batch.begin();
        if (selectedImage != null) {
            batch.draw(highlighter, selectedImage.getX() - 3, selectedImage.getY() - 4);
        }
        Andius.largeFont.draw(batch, this.vendorName, 447, 750);
        Andius.largeFont.draw(batch, "AC", 70, Andius.SCREEN_HEIGHT - 530);
        Andius.largeFont.draw(batch, "DAMG", 60, Andius.SCREEN_HEIGHT - 574);
        Andius.largeFont.draw(batch, "GOLD", 60, Andius.SCREEN_HEIGHT - 616);

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
            avatar.setX(132);
            avatar.setY(Andius.SCREEN_HEIGHT - 444);

            weaponIcon = make(ItemType.WEAPON, sp.weapon, icon(sp.weapon), 67, Andius.SCREEN_HEIGHT - 386);
            armorIcon = make(ItemType.ARMOR, sp.armor, icon(sp.armor), 67, Andius.SCREEN_HEIGHT - 442);
            helmIcon = make(ItemType.HELMET, sp.helm, icon(sp.helm), 133, Andius.SCREEN_HEIGHT - 386);
            shieldIcon = make(ItemType.SHIELD, sp.shield, icon(sp.shield), 199, Andius.SCREEN_HEIGHT - 386);
            glovesIcon = make(ItemType.GAUNTLET, sp.glove, icon(sp.glove), 199, Andius.SCREEN_HEIGHT - 442);
            item1Icon = make(ItemType.MISC, sp.item1, icon(sp.item1), 106, Andius.SCREEN_HEIGHT - 499);
            item2Icon = make(ItemType.MISC, sp.item2, icon(sp.item2), 160, Andius.SCREEN_HEIGHT - 499);

            acLabel = new Label("" + character.calculateAC(), Andius.skin, "larger");
            acLabel.setX(125);
            acLabel.setY(Andius.SCREEN_HEIGHT - 557);

            damageLabel = new Label(character.weapon != null ? character.weapon.damage.toString() : "1d2", Andius.skin, "larger");
            damageLabel.setX(125);
            damageLabel.setY(Andius.SCREEN_HEIGHT - 600);

            goldLabel = new Label("" + character.gold, Andius.skin, "larger");
            goldLabel.setX(125);
            goldLabel.setY(Andius.SCREEN_HEIGHT - 645);

            this.status = new PlayerStatusLabel(character);
            this.status.setX(64);
            this.status.setY(740);

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
                        ((Image) event.getTarget()).setDrawable(new TextureRegionDrawable(icon(selectedItem.item)));

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

    private TextureRegion icon(Item it) {
        return (it == null ? invIcons[803] : invIcons[it.iconID]);
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

            this.icon = new Image(icon(item));
            this.label = new Label(item.name, Andius.skin, "larger");
            this.price = new Label("" + item.cost / 2, Andius.skin, "larger");

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

            this.icon = new Image(invIcons[item.iconID]);
            this.label = new Label(item.name, Andius.skin, "larger");
            this.price = new Label("" + item.cost, Andius.skin, "larger");
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
            super("", Andius.skin, "larger");
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
        public Color getColor() {
            return rec.isDead() ? Color.RED : rec.status.color();
        }

    }

}
