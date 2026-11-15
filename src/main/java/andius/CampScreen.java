package andius;

import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import andius.WizardryData.MazeCell;
import andius.WizardryData.Scenario;
import andius.dialogs.TeleportDialog;
import andius.objects.Icons;
import andius.objects.Item;
import andius.objects.Item.ItemType;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Sound;
import andius.objects.Sounds;
import andius.objects.SpellUtil;
import andius.objects.Spells;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.CharArray;
import java.util.ArrayDeque;
import java.util.Arrays;
import utils.AutoFocusScrollPane;
import utils.ManagementUi;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import static utils.SkinUtils.GOLD;
import static utils.SkinUtils.INK;
import static utils.SkinUtils.MUTED;
import utils.Utils;

public class CampScreen implements Screen, Constants {

    private final Context context;
    private final Map map;
    private final ManagementUi ui = new ManagementUi();
    private Table equipmentTable;
    private InventoryImage hoveredEquipment;
    private Label equipmentName, itemDescription, selectionCaption;
    private Label spellDescription, spellCaption;
    private final Stage stage;

    private final Table playerTable;
    private final AutoFocusScrollPane playerPane;
    private final AutoFocusScrollPane invPane;
    private final AutoFocusScrollPane spellPane;

    private PlayerIndex selectedPlayer;
    private ItemListing selectedItem;
    private SpellListing selectedSpell;

    private Image playerFocusIndicator, itemFocusIndicator, spellFocusIndicator;

    private final TradeSliderBox traderSlider;
    private final CastTargetSliderBox castTargetSlider;

    private final Texture redBackgrnd = Utils.fillRectangle(10, 10, Color.valueOf("6b3340"), .42f);

    private final TextButton exit;
    private final TextButton drop;
    private final TextButton use;
    private final TextButton trade;

    private static final int WD = 304;
    private static final int HT = 60;
    private static final int DIM = 44;
    private static final int PDIM = 84;

    public CampScreen(Context context, Map map) {
        this.context = context;
        this.map = map;
        this.stage = new Stage(new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT));
        //this.stage.setDebugAll(true);

        playerFocusIndicator = ui.selection(WD, HT);
        playerFocusIndicator.setWidth(264);
        playerFocusIndicator.setHeight(PDIM);

        itemFocusIndicator = ui.selection(WD, HT);
        itemFocusIndicator.setWidth(WD);
        itemFocusIndicator.setHeight(HT);

        spellFocusIndicator = ui.selection(WD, HT);
        spellFocusIndicator.setWidth(WD);
        spellFocusIndicator.setHeight(HT);

        traderSlider = new TradeSliderBox();
        castTargetSlider = new CastTargetSliderBox();

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

        spellPane = new AutoFocusScrollPane(selectedPlayer.spellTable, ui.skin);
        spellPane.setScrollingDisabled(true, false);

        this.drop = new TextButton("DROP", ui.skin);
        this.drop.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedItem != null) {
                    selectedPlayer.p.inventory.remove(selectedItem.item);
                    itemFocusIndicator.remove();
                    selectedItem = null;
                    invPane.clearChildren();
                    selectedPlayer.setInventoryTable();
                    invPane.setActor(selectedPlayer.invTable);
                    Sounds.play(Sound.TRIGGER);
                } else {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                }
            }
        });

        this.use = new TextButton("USE", ui.skin);
        this.use.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedItem != null && selectedItem.item != null) {
                    boolean used = false;

                    if (selectedItem.item.spell != null) {
                        SpellUtil.useItem(selectedItem.item, selectedPlayer.p);
                        used = true;
                    }

                    if (selectedItem.item.id == 87 && selectedItem.item.scenarioID == 4) {//get out of jail free card
                        if (map.getScreen() instanceof WizardryDungeonScreen sc) {
                            Vector3 pos = new Vector3();
                            sc.getCurrentMapCoords(pos);

                            int currentX = (int) pos.x;
                            int currentY = (int) pos.y;
                            int currentLevelIndex = (int) pos.z;
                            int currentLevelNumber = currentLevelIndex + 1;

                            Coord safe = findNearestSafeDungeonCell(map, currentLevelIndex, currentX, currentY);

                            if (safe != null) {
                                sc.setMapPixelCoords(null, safe.x, safe.y, currentLevelNumber);
                                selectedPlayer.p.removeItem(87, 4);
                                mainGame.setScreen(sc);
                                used = true;
                            } else {
                                Sounds.play(Sound.NEGATIVE_EFFECT);
                            }
                        }
                    }

                    //holy hand grenade
                    if (selectedItem.item.id == 4 && selectedItem.item.scenarioID == Scenario.WER.ordinal()) {
                        Vector3 v = new Vector3();
                        map.getScreen().getCurrentMapCoords(v);
                        if (v.x == 15 && v.y == 15 && v.z == 1) {
                            if (context.partyHasItem(11, Scenario.WER.ordinal()) != null) {//cleansing oil
                                WizardryDungeonScreen sc = (WizardryDungeonScreen) map.getScreen();
                                MazeCell cell1 = sc.cell(15, 15, 1);
                                MazeCell cell2 = sc.cell(16, 15, 1);
                                cell1.northWall = false;
                                cell2.southWall = false;
                                sc.addBlock(1, cell1, 15, 15, true);
                                sc.addBlock(1, cell2, 16, 15, true);
                                Sounds.play(Sound.EXPLOSION);
                                selectedPlayer.p.removeItem(4, 4);
                                context.saveGame.hhgUsed = true;
                                mainGame.setScreen(map.getScreen());
                            } else {
                                Sounds.play(Sound.NEGATIVE_EFFECT);
                            }
                        } else {
                            Sounds.play(Sound.NEGATIVE_EFFECT);
                        }
                    }

                    if (used && selectedItem.item.changeChance > 0) {
                        boolean decayed = Utils.percentChance(selectedItem.item.changeChance);
                        if (decayed) {
                            Item changeTo = map.scenario().items().get(selectedItem.item.changeTo);
                            selectedPlayer.p.inventory.remove(selectedItem.item);
                            if (changeTo.id != 0) {
                                selectedPlayer.p.inventory.add(changeTo);
                            }
                            itemFocusIndicator.remove();
                            selectedItem = null;
                            invPane.clearChildren();
                            selectedPlayer.setInventoryTable();
                            invPane.setActor(selectedPlayer.invTable);
                        }
                    }

                    if (!used) {
                        Sounds.play(Sound.NEGATIVE_EFFECT);
                    }
                } else {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                }
            }
        });

        this.exit = new TextButton("EXIT", ui.skin);
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(map.getScreen());
            }
        });

        this.trade = new TextButton("TRADE", ui.skin);
        this.trade.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (context.players().length > 1) {
                    traderSlider.show();
                }
            }
        });

        buildLayout();
        showEquipment();

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
        selectedSpell = null;
        spellFocusIndicator.remove();
        spellPane.setActor(player.spellTable);
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
        ui.label(stage, "Camp & company", "default-24", GOLD, 24, 714, 700, 34);
        ui.label(stage, "Prepare your companions for the road ahead.", "default", MUTED, 24, 696, 700, 20);
        ui.panel(stage, "01  /  The company", "Select an adventurer", 24, 288);
        ui.panel(stage, "02  /  Equipment & pack", "", 328, 328);
        ui.panel(stage, "03  /  Spellbook", "Known spells and camp magic", 672, 328);
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
        spellPane.setBounds(684, 256, 304, 368);
        spellPane.setFadeScrollBars(false);
        stage.addActor(spellPane);
        spellDescription = ui.description(stage, 684, 108, 304, 134);
        spellCaption = ui.label(stage, "", "default", MUTED, 36, 76, 800, 18);
        drop.setText("Drop"); drop.setBounds(328, 24, 96, 38);
        use.setText("Use"); use.setBounds(432, 24, 96, 38);
        trade.setText("Trade"); trade.setBounds(536, 24, 120, 38);
        stage.addActor(drop); stage.addActor(use); stage.addActor(trade);
        ui.primary(use);
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
                );
        drop.setDisabled(selectedItem == null);
        use.setDisabled(selectedItem == null);
        trade.setDisabled(selectedItem == null || context.players().length < 2);
        spellDescription.setText(selectedSpell == null ? "Select a spell to inspect it." : selectedSpell.spell.getName()
                + "\n" + selectedSpell.spell.getDescription());
        spellCaption.setText("Mage  " + points(selectedPlayer.p.magePoints) + "    /    Priest  " + points(selectedPlayer.p.clericPoints));
        for (Cell<?> cell : selectedPlayer.spellTable.getCells()) {
            if (cell.getActor() instanceof SpellListing spell && spell.cast != null)
                spell.cast.setDisabled(selectedPlayer.p.isDisabled() || !selectedPlayer.p.canCast(spell.spell));
        }
    }

    private String points(int[] values) {
        return Arrays.toString(values).replace("[", "").replace("]", "").replace(", ", "/");
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
        private final Table spellTable = new Table(ui.skin);

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

            spellTable.align(Align.top);

            for (Spells spell : p.knownSpells) {
                SpellListing l = new SpellListing(spell);
                spellTable.add(l);
                spellTable.row();
            }
            if (p.knownSpells.isEmpty()) spellTable.add(new Label("No known spells.", ui.skin)).width(WD).padTop(14);

            spellTable.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    spellFocusIndicator.remove();
                    if (event.getTarget() instanceof SpellListing) {
                        selectedSpell = (SpellListing) event.getTarget();
                        selectedSpell.addActor(spellFocusIndicator);
                        spellFocusIndicator.toBack();

                    } else if (event.getTarget().getParent() instanceof SpellListing) {
                        selectedSpell = (SpellListing) event.getTarget().getParent();
                        selectedSpell.addActor(spellFocusIndicator);
                        spellFocusIndicator.toBack();

                    }
                }
            });
        }

        private void setInventoryTable() {
            invTable.align(Align.top);
            invTable.clearChildren();

            for (Item it : p.inventory) {
                invTable.add(new ItemListing(it, p));
                invTable.row();
            }
            if (p.inventory.isEmpty()) invTable.add(new Label("Your pack is empty.", ui.skin)).width(WD).padTop(14);
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

    private class SpellListing extends Group {

        Spells spell;
        final Image icon;
        final Label label;
        TextButton cast;

        SpellListing(Spells spell) {
            this.spell = spell;

            this.icon = new Image(Icons.get(spell.getIcon()));
            this.label = new Label(spell.getName(), ui.skin, "emphasis");

            addActor(this.icon);
            addActor(this.label);

            this.icon.setScaling(Scaling.none);
            this.icon.setBounds(6, 8, DIM, DIM);
            this.label.setBounds(58, 28, 166, 22);
            this.label.setEllipsis(true);
            Label level = new Label("Level " + spell.getLevel() + " / " + spell.getType(), ui.skin);
            level.setColor(MUTED);
            level.setBounds(58, 8, 166, 20);
            addActor(level);

            if (spell.getArea() != SpellArea.COMBAT && spell.getArea() != SpellArea.LOOTING) {
                this.cast = new TextButton("CAST", ui.skin);
                this.cast.setText("Cast");
                this.cast.setBounds(236, 14, 62, 32);
                addActor(this.cast);
                this.cast.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                        if (spell == Spells.MALOR) {
                            TeleportDialog dialog = new TeleportDialog(context, map.getScreen(), () -> {
                                Andius.mainGame.setScreen(map.getScreen());
                            });
                            dialog.show(stage);
                        } else {
                            castTargetSlider.spell = spell;
                            castTargetSlider.show();
                        }
                    }
                });
            }

            this.setSize(WD, HT);
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

    private class CampDialog extends com.badlogic.gdx.scenes.scene2d.ui.Dialog {
        CampDialog(String title) {
            super(title, ui.skin);
        }

        void show() {
            getColor().a = 1f;
            super.show(stage, null);
            setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
        }

        @Override public void hide() {
            super.hide(null);
        }
    }

    private class TradeSliderBox extends CampDialog {
        private final List<CharacterRecord> tradeSelection;

        TradeSliderBox() {
            super("Trade item");
            pad(36, 20, 16, 20);
            tradeSelection = new List<>(ui.skin);
            tradeSelection.setItems(context.players());
            tradeSelection.addListener(new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) {
                    int index = tradeSelection.getItemIndexAt(y);
                    if (index < 0 || selectedItem == null) return;
                    CharacterRecord recipient = context.players()[index];
                    if (recipient == selectedPlayer.p) { Sounds.play(Sound.NEGATIVE_EFFECT); return; }
                    selectedItem.p.inventory.remove(selectedItem.item);
                    recipient.inventory.add(selectedItem.item);
                    for (Cell<?> cell : playerTable.getCells()) {
                        if (cell.getActor() instanceof PlayerIndex player) player.setInventoryTable();
                    }
                    selectedItem = null; itemFocusIndicator.remove();
                    invPane.setActor(selectedPlayer.invTable);
                    Sounds.play(Sound.TRIGGER);
                    hide();
                }
            });
            getContentTable().add(tradeSelection).width(300).padBottom(12);
            getButtonTable().defaults().width(110).height(34);
            button("Cancel");
            key(Input.Keys.ESCAPE, false);
        }

    }

    private class CastTargetSliderBox extends CampDialog {
        private final List<CharacterRecord> targetSelection;
        private Spells spell;

        CastTargetSliderBox() {
            super("Choose a spell target");
            pad(36, 20, 16, 20);
            targetSelection = new List<>(ui.skin);
            targetSelection.setItems(context.players());
            targetSelection.addListener(new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) {
                    int index = targetSelection.getItemIndexAt(y);
                    if (index < 0 || spell == null) return;
                    SpellUtil.campCast(selectedPlayer.p, spell, context.players()[index]);
                    hide();
                }
            });
            getContentTable().add(targetSelection).width(300).padBottom(12);
            getButtonTable().defaults().width(110).height(34);
            button("Cancel");
            key(Input.Keys.ESCAPE, false);
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
        ui.dispose();
    }

    private static class Coord {

        final int x;
        final int y;

        Coord(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private Coord findNearestSafeDungeonCell(Map map, int levelIndex, int startX, int startY) {
        WizardryData.MazeLevel level = map.scenario().levels()[levelIndex];
        WizardryData.MazeCell[][] cells = level.cells;
        int dim = cells.length;

        if (startX < 0 || startY < 0 || startX >= dim || startY >= dim) {
            return null;
        }

        boolean[][] visited = new boolean[dim][dim];
        ArrayDeque<Coord> queue = new ArrayDeque<>();

        queue.add(new Coord(startX, startY));
        visited[startX][startY] = true;

        while (!queue.isEmpty()) {
            Coord c = queue.removeFirst();
            WizardryData.MazeCell cell = cells[c.x][c.y];

            if (!(c.x == startX && c.y == startY) && isSafeTeleportCell(cell)) {
                return c;
            }

            addNeighbor(queue, visited, cells, dim, c.x, c.y - 1);
            addNeighbor(queue, visited, cells, dim, c.x, c.y + 1);
            addNeighbor(queue, visited, cells, dim, c.x - 1, c.y);
            addNeighbor(queue, visited, cells, dim, c.x + 1, c.y);
        }

        return null;
    }

    private boolean isSafeTeleportCell(WizardryData.MazeCell cell) {
        return !cell.rock
                && !cell.pit
                && !cell.chute
                && !cell.teleport
                && !cell.cage
                && cell.encounterID < 0
                && cell.damage == null
                && cell.itemRequired <= 0
                && cell.function == null;
    }

    private void addNeighbor(ArrayDeque<Coord> queue, boolean[][] visited, WizardryData.MazeCell[][] cells, int dim, int toX, int toY) {
        if (toX < 0 || toY < 0 || toX >= dim || toY >= dim) {
            return;
        }
        if (visited[toX][toY]) {
            return;
        }
        if (cells[toX][toY].rock) {
            return;
        }
        visited[toX][toY] = true;
        queue.addLast(new Coord(toX, toY));
    }

}
