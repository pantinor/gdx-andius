package andius;

import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import andius.WizardryData.MazeCell;
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
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.CharArray;
import java.util.ArrayList;
import java.util.Arrays;
import utils.AutoFocusScrollPane;
import utils.FrameMaker;
import utils.Utils;

public class CampScreen implements Screen, Constants {

    private final Context context;
    private final Map map;
    private final Texture background;
    private final SpriteBatch batch;
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

    private final Texture redBackgrnd = Utils.fillRectangle(10, 10, Color.RED, .25f);

    private final GlyphLayout SPDESCLAYOUT = new GlyphLayout(Andius.font12, "", Color.WHITE, 226, Align.left, true);
    private final GlyphLayout ITEMDESCLAYOUT = new GlyphLayout(Andius.font12, "", Color.WHITE, 226, Align.left, true);

    private final TextButton exit;
    private final TextButton drop;
    private final TextButton use;
    private final TextButton trade;

    private static final int WD = 246;
    private static final int HT = 50;
    private static final int DIM = 44;
    private static final int PDIM = 60;

    public CampScreen(Context context, Map map) {
        this.context = context;
        this.map = map;
        this.batch = new SpriteBatch();
        this.stage = new Stage();
        //this.stage.setDebugAll(true);

        playerFocusIndicator = new Image(Utils.fillRectangle(10, 10, Color.GREEN, .25f));
        playerFocusIndicator.setWidth(600);
        playerFocusIndicator.setHeight(PDIM);

        itemFocusIndicator = new Image(Utils.fillRectangle(10, 10, Color.GREEN, .25f));
        itemFocusIndicator.setWidth(WD);
        itemFocusIndicator.setHeight(HT);

        spellFocusIndicator = new Image(Utils.fillRectangle(10, 10, Color.GREEN, .25f));
        spellFocusIndicator.setWidth(WD);
        spellFocusIndicator.setHeight(HT);

        traderSlider = new TradeSliderBox();
        castTargetSlider = new CastTargetSliderBox();

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

                    spellPane.clearChildren();
                    spellPane.setActor(selectedPlayer.spellTable);
                    selectedSpell = null;
                    spellFocusIndicator.remove();
                }

                InventoryImage ii = selectedPlayer.itemClicked(x, y);
                if (ii != null) {
                    if (selectedItem != null) {
                        if (ii.type == selectedItem.item.type && selectedItem.item.canUse(selectedPlayer.p.classType)) {
                            ii.equip(selectedPlayer.p, selectedItem.item);
                            itemFocusIndicator.remove();
                            selectedItem = null;
                            invPane.clearChildren();
                            selectedPlayer.setInventoryTable();
                            invPane.setActor(selectedPlayer.invTable);
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

                    spellPane.clearChildren();
                    spellPane.setActor(selectedPlayer.spellTable);
                    selectedSpell = null;
                    spellFocusIndicator.remove();
                }

                InventoryImage ii = selectedPlayer.itemClicked(x, y);
                if (ii != null) {
                    ii.unequip(selectedPlayer.p);
                    invPane.clearChildren();
                    selectedPlayer.setInventoryTable();
                    invPane.setActor(selectedPlayer.invTable);
                }
            }
        });

        playerPane = new AutoFocusScrollPane(playerTable, Andius.skin);
        playerPane.setScrollingDisabled(true, false);

        invPane = new AutoFocusScrollPane(selectedPlayer.invTable, Andius.skin);
        invPane.setScrollingDisabled(true, false);

        spellPane = new AutoFocusScrollPane(selectedPlayer.spellTable, Andius.skin);
        spellPane.setScrollingDisabled(true, false);

        int x = 350;
        this.drop = new TextButton("DROP", Andius.skin, "default-16-green");
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
        this.drop.setBounds(x, 200, 80, 40);
        x += 84;
        this.use = new TextButton("USE", Andius.skin, "default-16-green");
        this.use.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedItem != null && selectedItem.item != null) {
                    boolean used = false;

                    if (selectedItem.item.spell != null) {
                        SpellUtil.useItem(selectedItem.item, selectedPlayer.p);
                        used = true;
                    }

                    if (selectedItem.item.id == 87 && selectedItem.item.scenarioID == 4) {
                        map.getScreen().teleport(0, -1, 0);//get out of jail free card
                        used = true;
                    }

                    if (selectedItem.item.id == 4 && selectedItem.item.scenarioID == 4) {//HHG
                        Vector3 v = new Vector3();
                        map.getScreen().getCurrentMapCoords(v);
                        if (v.x == 15 && v.y == 15 && v.z == 1) {
                            if (context.partyHasItem(11, 4) != null) {//cleansing oil
                                WizardryDungeonScreen sc = (WizardryDungeonScreen) map.getScreen();
                                MazeCell cell1 = sc.cell(15, 15, 1);
                                MazeCell cell2 = sc.cell(16, 15, 1);
                                cell1.northWall = false;
                                cell2.southWall = false;
                                sc.addBlock(1, cell1, 15, 15, true);
                                sc.addBlock(1, cell2, 16, 15, true);
                                Sounds.play(Sound.EXPLOSION);
                                selectedPlayer.p.removeItem(4, 4);
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
        this.use.setBounds(x, 200, 80, 40);
        x = 350;
        this.exit = new TextButton("EXIT", Andius.skin, "default-16");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(map.getScreen());
            }
        });
        this.exit.setBounds(x, 150, 80, 40);
        x += 84;
        this.trade = new TextButton("TRADE", Andius.skin, "default-16-green");
        this.trade.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (context.players().length > 1) {
                    traderSlider.setZIndex(Integer.MAX_VALUE);
                    traderSlider.show();
                }
            }
        });
        this.trade.setBounds(x, 150, 80, 40);

        FrameMaker fm = new FrameMaker(SCREEN_WIDTH, SCREEN_HEIGHT);

        fm.setBounds(playerPane, 20, 375, 600, 376);
        fm.setBounds(invPane, 20, 10, 246, 348);
        fm.setBounds(spellPane, 640, 151, 246, 600);

        stage.addActor(playerPane);
        stage.addActor(invPane);
        stage.addActor(spellPane);

        stage.addActor(exit);
        stage.addActor(trade);
        stage.addActor(drop);
        stage.addActor(use);

        stage.addActor(traderSlider);
        stage.addActor(castTargetSlider);

        this.background = fm.build();

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

        batch.begin();

        if (selectedPlayer != null) {
            int x = 360;
            Andius.font14.draw(batch, selectedPlayer.p.weapon != null ? selectedPlayer.p.weapon.name : "-", 285, x);
            Andius.font14.draw(batch, selectedPlayer.p.armor != null ? selectedPlayer.p.armor.name : "-", 285, x -= 15);
            Andius.font14.draw(batch, selectedPlayer.p.helm != null ? selectedPlayer.p.helm.name : "-", 285, x -= 15);
            Andius.font14.draw(batch, selectedPlayer.p.shield != null ? selectedPlayer.p.shield.name : "-", 285, x -= 15);
            Andius.font14.draw(batch, selectedPlayer.p.glove != null ? selectedPlayer.p.glove.name : "-", 285, x -= 15);
            Andius.font14.draw(batch, selectedPlayer.p.item1 != null ? selectedPlayer.p.item1.name : "-", 285, x -= 15);
            Andius.font14.draw(batch, selectedPlayer.p.item2 != null ? selectedPlayer.p.item2.name : "-", 285, x -= 15);
        }

        if (selectedSpell != null) {
            Andius.font14.draw(batch, SPDESCLAYOUT, 650, 130);
        }

        if (selectedItem != null) {
            Andius.font12.draw(batch, ITEMDESCLAYOUT, 285, 130);
        }

        batch.end();

        stage.act();
        stage.draw();

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
        private final Table spellTable = new Table(Andius.skin);

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
            this.label.setBounds(x += dim + 3, 0, 210, PDIM);
            this.weaponIcon.setPosition(x += 210, 5);
            this.armorIcon.setPosition(x += dim, 5);
            this.helmIcon.setPosition(x += dim, 5);
            this.shieldIcon.setPosition(x += dim, 5);
            this.glovesIcon.setPosition(x += dim, 5);
            this.item1Icon.setPosition(x += dim, 5);
            this.item2Icon.setPosition(x += dim, 5);

            setInventoryTable();

            spellTable.align(Align.top);

            for (Spells spell : p.knownSpells) {
                SpellListing l = new SpellListing(spell);
                spellTable.add(l);
                spellTable.row();
            }

            spellTable.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    spellFocusIndicator.remove();
                    if (event.getTarget() instanceof SpellListing) {
                        selectedSpell = (SpellListing) event.getTarget();
                        selectedSpell.addActor(spellFocusIndicator);
                        SPDESCLAYOUT.setText(Andius.font14, selectedSpell.spell.getDescription(), Color.WHITE, 226, Align.left, true);
                    } else if (event.getTarget().getParent() instanceof SpellListing) {
                        selectedSpell = (SpellListing) event.getTarget().getParent();
                        selectedSpell.addActor(spellFocusIndicator);
                        SPDESCLAYOUT.setText(Andius.font14, selectedSpell.spell.getDescription(), Color.WHITE, 226, Align.left, true);
                    }
                }
            });

        }

        private void setInventoryTable() {

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
        }

        private class PlayerLabel extends Label {

            private final java.util.List<Integer> magicPoints = new ArrayList<>();

            public PlayerLabel() {
                super("", Andius.skin, "default-12");
                setColor(p.status.color());
                setText(getText());
            }

            @Override
            public CharArray getText() {

                CharArray sb = new com.badlogic.gdx.utils.CharArray();

                sb.append(String.format("%s  LVL %d  %s  %s\n", p.name.toUpperCase(), p.level, p.race.toString(), p.classType.toString()));
                sb.append(String.format("HP: %d /%d AC: %d ST: %s\n", p.hp, p.maxhp, p.calculateAC(), p.status.toString()));
                sb.append(String.format("GOLD: %d EXP: %d\n", p.gold, p.exp));

                int[] ms = p.magePoints;
                int[] cs = p.clericPoints;
                magicPoints.clear();
                Arrays.stream(ms).forEach(magicPoints::add);
                Arrays.stream(cs).forEach(magicPoints::add);
                int sum = magicPoints.stream().mapToInt(Integer::intValue).sum();
                if (sum > 0) {
                    sb.append(String.format("M: %d %d %d %d %d %d %d P: %d %d %d %d %d %d %d",
                            ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6], cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]));
                }

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

    private class SpellListing extends Group {

        Spells spell;
        final Image icon;
        final Label label;
        TextButton cast;

        SpellListing(Spells spell) {
            this.spell = spell;

            this.icon = new Image(Icons.get(spell.getIcon()));
            this.label = new Label(String.format("%d - %s", spell.getLevel(), spell.toString().toUpperCase()), Andius.skin, "default-16");

            addActor(this.icon);
            addActor(this.label);

            this.icon.setBounds(getX() + 3, getY() + 3, DIM, DIM);
            this.label.setPosition(getX() + DIM + 10, getY() + 14);

            if (spell.getArea() != SpellArea.COMBAT && spell.getArea() != SpellArea.LOOTING) {
                this.cast = new TextButton("CAST", Andius.skin, "default-16-red");
                this.cast.setBounds(getX() + DIM + 130, getY() + 10, 60, 25);
                addActor(this.cast);
                this.cast.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                        if (spell == Spells.MALOR) {
                            new TeleportDialog(context, map.getScreen()).show(stage);
                        } else {
                            castTargetSlider.spell = spell;
                            castTargetSlider.setZIndex(Integer.MAX_VALUE);
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

    private class TradeSliderBox extends Group {

        final int width = 200;
        final int height = 150;
        private final List<TradeIndex> tradeSelection;

        public TradeSliderBox() {

            Image background = new Image(Utils.fillRectangle(width, height, Color.BROWN, .75f));
            background.setBounds(getX(), getY(), width, height);
            addActor(background);

            this.tradeSelection = new List<>(Andius.skin, "default-16");
            TradeIndex[] names = new TradeIndex[context.players().length];
            for (int i = 0; i < context.players().length; i++) {
                names[i] = new TradeIndex(context.players()[i]);
            }
            this.tradeSelection.setItems(names);

            this.tradeSelection.addListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    if (event.toString().equals("touchDown")) {
                        if (selectedItem == null || selectedPlayer.p == tradeSelection.getSelected().character) {
                            Sounds.play(Sound.NEGATIVE_EFFECT);
                        } else {
                            Sounds.play(Sound.TRIGGER);

                            selectedItem.p.inventory.remove(selectedItem.item);
                            tradeSelection.getSelected().character.inventory.add(selectedItem.item);

                            for (Cell cell : playerTable.getCells()) {
                                Actor actor = cell.getActor();
                                if (actor instanceof PlayerIndex) {
                                    PlayerIndex pi = (PlayerIndex) actor;
                                    if (pi.p == tradeSelection.getSelected().character) {
                                        pi.setInventoryTable();
                                    }
                                }
                            }

                            itemFocusIndicator.remove();
                            selectedItem = null;
                            invPane.clearChildren();
                            selectedPlayer.setInventoryTable();
                            invPane.setActor(selectedPlayer.invTable);
                        }
                        TradeSliderBox.this.hide();
                    }
                    return false;
                }
            });

            ScrollPane tradePane = new ScrollPane(this.tradeSelection, Andius.skin);
            tradePane.setBounds(getX(), getY(), width, height);
            addActor(tradePane);

            setBounds(Andius.SCREEN_WIDTH, 50, width, height);
        }

        void show() {
            if (getActions().size > 0) {
                clearActions();
            }
            setPosition(Andius.SCREEN_WIDTH, 50);
            addAction(Actions.sequence(Actions.show(), Actions.moveBy(-width, 0, 1f, Interpolation.sine)));
        }

        void hide() {
            if (getActions().size > 0) {
                clearActions();
            }
            addAction(Actions.sequence(Actions.moveBy(width, 0, 1f, Interpolation.sine), Actions.hide()));
        }

        private class TradeIndex {

            final CharacterRecord character;

            public TradeIndex(CharacterRecord character) {
                this.character = character;
            }

            @Override
            public String toString() {
                return character.name.toUpperCase();
            }
        }

    }

    private class CastTargetSliderBox extends Group {

        final int width = 200;
        final int height = 150;
        private final List<TargetIndex> targetSelection;
        private Spells spell;

        public CastTargetSliderBox() {

            Image background = new Image(Utils.fillRectangle(width, height, Color.SKY, .75f));
            background.setBounds(getX(), getY(), width, height);
            addActor(background);

            this.targetSelection = new List<>(Andius.skin, "default-16");
            TargetIndex[] names = new TargetIndex[context.players().length];
            for (int i = 0; i < context.players().length; i++) {
                names[i] = new TargetIndex(context.players()[i]);
            }
            this.targetSelection.setItems(names);

            this.targetSelection.addListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    if (event.toString().equals("touchDown")) {
                        if (spell != null && selectedPlayer.p != null && targetSelection.getSelected().character != null) {
                            SpellUtil.campCast(selectedPlayer.p, spell, targetSelection.getSelected().character);
                        }
                        CastTargetSliderBox.this.hide();
                    }
                    return false;
                }
            });

            ScrollPane tradePane = new ScrollPane(this.targetSelection, Andius.skin);
            tradePane.setBounds(getX(), getY(), width, height);
            addActor(tradePane);

            setBounds(Andius.SCREEN_WIDTH, 50, width, height);
            setZIndex(Integer.MAX_VALUE);
        }

        void show() {
            if (getActions().size > 0) {
                clearActions();
            }
            setPosition(Andius.SCREEN_WIDTH, 50);
            addAction(Actions.sequence(Actions.show(), Actions.moveBy(-width, 0, 1f, Interpolation.sine)));
        }

        void hide() {
            if (getActions().size > 0) {
                clearActions();
            }
            addAction(Actions.sequence(Actions.moveBy(width, 0, 1f, Interpolation.sine), Actions.hide()));
        }

        private class TargetIndex {

            final CharacterRecord character;

            public TargetIndex(CharacterRecord character) {
                this.character = character;
            }

            @Override
            public String toString() {
                return character.name.toUpperCase();
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

}
