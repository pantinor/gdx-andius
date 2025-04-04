package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import andius.WizardryData.MazeCell;
import static andius.WizardryData.WER_ITEMS;
import andius.dialogs.TeleportDialog;
import andius.objects.ClassType;
import andius.objects.Icons;
import andius.objects.Item;
import andius.objects.Item.ItemType;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.SpellUtil;
import andius.objects.Spells;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
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
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;
import utils.AutoFocusScrollPane;
import utils.FrameMaker;
import utils.Utils;

public class EquipmentScreen implements Screen, Constants {

    private final Context context;
    private final Map map;
    private final Texture background;
    private final SpriteBatch batch;
    private final Stage stage;

    private final List<PlayerIndex> playerSelection;

    private final TextButton exit;
    private final TextButton drop;
    private final TextButton use;
    private final TextButton unequip;
    private final TextButton trade;
    private final TextButton cancel;

    private final TradeSliderBox traderSlider;
    private final CastTargetSliderBox castTargetSlider;

    private final TextureRegion[] invIcons = new TextureRegion[67 * 12];

    private final Texture redBackgrnd = Utils.fillRectangle(w, h, Color.RED, .25f);
    private final Texture clearBackgrnd = Utils.fillRectangle(w, h, Color.CLEAR, 0f);
    private final static Texture highlighter;

    private static final int w = 246;
    private static final int h = 50;
    private static final int dim = 44;

    private Image selectedImage;

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

    private ItemListing selectedItem;
    private PlayerIndex selectedPlayer;
    private SpellListing selectedSpell;

    private AutoFocusScrollPane invPane, spellPane;
    private Image focusIndicator, spellFocusInd;
    private Label invDesc;

    private final GlyphLayout SPDESCLAYOUT = new GlyphLayout(Andius.font12, "", Color.WHITE, 226, Align.left, true);
    private final GlyphLayout ITEMDESCLAYOUT = new GlyphLayout(Andius.font12, "", Color.WHITE, 226, Align.left, true);

    public EquipmentScreen(Context context, Map map) {
        this.context = context;
        this.map = map;
        this.batch = new SpriteBatch();
        this.stage = new Stage();
        //this.stage.setDebugAll(true);

        FrameMaker fm = new FrameMaker(SCREEN_WIDTH, SCREEN_HEIGHT);

        focusIndicator = new Image(Utils.fillRectangle(246, 50, Color.YELLOW, .35f));
        focusIndicator.setWidth(246);
        focusIndicator.setHeight(50);

        spellFocusInd = new Image(Utils.fillRectangle(246, 50, Color.YELLOW, .35f));
        spellFocusInd.setWidth(246);
        spellFocusInd.setHeight(50);

        traderSlider = new TradeSliderBox();
        castTargetSlider = new CastTargetSliderBox();

        invDesc = new Label("", Andius.skin, "default-16");
        invDesc.setBounds(284, Andius.SCREEN_HEIGHT - 415, w, h);

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

        this.playerSelection = new List<>(Andius.skin, "default-16");
        PlayerIndex[] names = new PlayerIndex[this.context.players().length];
        for (int i = 0; i < this.context.players().length; i++) {
            names[i] = new PlayerIndex(this.context.players()[i]);
        }
        this.playerSelection.setItems(names);

        AutoFocusScrollPane sp1 = new AutoFocusScrollPane(this.playerSelection, Andius.skin);

        invPane = new AutoFocusScrollPane(playerSelection.getSelected().invTable, Andius.skin);
        invPane.setScrollingDisabled(true, false);

        spellPane = new AutoFocusScrollPane(playerSelection.getSelected().spellTable, Andius.skin);
        spellPane.setScrollingDisabled(true, false);

        int x = 30;
        this.unequip = new TextButton("UNEQUIP", Andius.skin, "default-16");
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
        this.unequip.setBounds(x, 290, 80, 40);
        x += 84;
        this.drop = new TextButton("DROP", Andius.skin, "default-16");
        this.drop.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedItem != null) {
                    selectedItem.removeActor(focusIndicator);
                    selectedPlayer.invTable.removeActor(selectedItem);
                    selectedItem = null;
                    Sounds.play(Sound.TRIGGER);
                } else {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                }
            }
        });
        this.drop.setBounds(x, 290, 80, 40);
        x += 84;
        this.use = new TextButton("USE", Andius.skin, "default-16");
        this.use.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (selectedItem != null && selectedItem.item != null) {
                    boolean used = false;

                    if (selectedItem.item.spell != null) {
                        SpellUtil.useItem(selectedItem.item, selectedPlayer.character);
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
                                selectedPlayer.character.removeItem(4, 4);
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
                            Item changeTo = WER_ITEMS.get(selectedItem.item.changeTo);
                            selectedPlayer.character.removeItem(selectedItem.item.id, selectedItem.item.scenarioID);
                            if (changeTo.id != 0) {
                                selectedPlayer.character.inventory.add(changeTo);
                            }
                            selectedItem.removeActor(focusIndicator);
                            selectedPlayer.invTable.removeActor(selectedItem);
                            selectedItem = null;
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
        this.use.setBounds(x, 290, 80, 40);
        x = 30;
        this.cancel = new TextButton("LEAVE", Andius.skin, "default-16");
        this.cancel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(map.getScreen());
            }
        });
        this.cancel.setBounds(x, 240, 80, 40);
        x += 84;
        this.exit = new TextButton("SAVE", Andius.skin, "default-16");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                for (PlayerIndex pi : EquipmentScreen.this.playerSelection.getItems()) {
                    pi.save();
                }
                mainGame.setScreen(map.getScreen());
            }
        });
        this.exit.setBounds(x, 240, 80, 40);
        x += 84;
        this.trade = new TextButton("TRADE", Andius.skin, "default-16");
        this.trade.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (context.players().length > 1) {
                    traderSlider.setZIndex(Integer.MAX_VALUE);
                    traderSlider.show();
                }
            }
        });
        this.trade.setBounds(x, 240, 80, 40);

        fm.setBounds(sp1, 45, Andius.SCREEN_HEIGHT - 372, 180, 250);
        fm.setBounds(spellPane, 753, Andius.SCREEN_HEIGHT - 551, 246, 420);
        fm.setBounds(invPane, 485, Andius.SCREEN_HEIGHT - 551, 246, 420);

        stage.addActor(sp1);
        stage.addActor(invPane);
        stage.addActor(spellPane);
        stage.addActor(exit);
        stage.addActor(cancel);
        stage.addActor(trade);
        stage.addActor(drop);
        stage.addActor(use);
        stage.addActor(invDesc);
        stage.addActor(unequip);
        stage.addActor(traderSlider);
        stage.addActor(castTargetSlider);

        Label inventory = new Label("Inventory", Andius.skin, "default-16");
        Label splBk = new Label("Known Spells", Andius.skin, "default-16");
        inventory.setBounds(512, Andius.SCREEN_HEIGHT - 160, 20, 100);
        splBk.setBounds(770, Andius.SCREEN_HEIGHT - 160, 20, 100);
        
        stage.addActor(inventory);
        stage.addActor(splBk);

        ChangeListener cl = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                selectedItem = null;
                selectedSpell = null;
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

                spellPane.clearChildren();
                spellPane.setWidget(selectedPlayer.spellTable);
            }
        };
        playerSelection.addListener(cl);
        cl.changed(null, null);

        fm.setBounds(null, 30, 60, 310, 150);

        fm.setBounds(null, 260, 540, 48, 48);
        fm.setBounds(null, 330, 540, 48, 48);
        fm.setBounds(null, 400, 540, 48, 48);

        fm.setBounds(null, 260, 470, 48, 48);
        fm.setBounds(null, 330, 470, 48, 48);
        fm.setBounds(null, 400, 470, 48, 48);

        fm.setBounds(null, 260, 400, 48, 48);
        fm.setBounds(null, 400, 400, 48, 48);

        this.background = fm.build();
    }

    @Override

    public void show() {
        Gdx.input.setInputProcessor(stage);

    }

    private TextureRegion icon(Item it) {
        return Icons.get(it);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(this.background, 0, 0);
        batch.end();

        batch.begin();
        if (selectedImage != null) {
            batch.draw(highlighter, selectedImage.getX() - 3, selectedImage.getY() - 4);
        }

        float x = 40;
        float y = 180;
        Andius.font16.draw(batch, "EXP", x, y - 0);
        Andius.font16.draw(batch, "HP", x, y - 20);
        Andius.font16.draw(batch, "MAXHP", x, y - 40);
        Andius.font16.draw(batch, "AC", x, y - 60);
        Andius.font16.draw(batch, "DAMG", x, y - 80);
        Andius.font16.draw(batch, "GOLD", x, y - 100);
        x += 180;
        Andius.font16.draw(batch, "STR", x, y - 0);
        Andius.font16.draw(batch, "INT", x, y - 20);
        Andius.font16.draw(batch, "PTY", x, y - 40);
        Andius.font16.draw(batch, "VIT", x, y - 60);
        Andius.font16.draw(batch, "AGI", x, y - 80);
        Andius.font16.draw(batch, "LCK", x, y - 100);

        if (selectedSpell != null) {
            Andius.font14.draw(batch, SPDESCLAYOUT, 750, 175);
        }

        if (selectedItem != null) {
            Andius.font12.draw(batch, ITEMDESCLAYOUT, 500, Andius.SCREEN_HEIGHT - 579);
        }

        batch.end();

        stage.act();
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            this.drop.toggle();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            this.trade.toggle();
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
        final Table spellTable = new Table(Andius.skin);
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

        final Label classL;
        final PlayerMagicPointsLabel spptsL;
        final Label expL;
        final Label hpL;
        final Label mxhpL;
        final Label strL;
        final Label intL;
        final Label ptyL;
        final Label vitL;
        final Label agiL;
        final Label lckL;

        final Actor[] icons = new Actor[22];

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
                            ITEMDESCLAYOUT.setText(Andius.font12, selectedItem.item.briefDescription(), Color.WHITE, 226, Align.left, true);
                        } else if (event.getTarget().getParent() instanceof ItemListing) {
                            selectedItem = (ItemListing) event.getTarget().getParent();
                            selectedItem.addActor(focusIndicator);
                            ITEMDESCLAYOUT.setText(Andius.font12, selectedItem.item.briefDescription(), Color.WHITE, 226, Align.left, true);
                        }
                    }

                    return false;
                }
            }
            );

            spellTable.align(Align.top);

            for (Spells spell : character.knownSpells) {
                SpellListing l = new SpellListing(spell);
                spellTable.add(l);
                spellTable.row();
            }

            spellTable.addListener(new EventListener() {
                @Override
                public boolean handle(Event event) {

                    if (event.toString().equals("touchDown")) {
                        if (spellFocusInd.getParent() != null) {
                            spellFocusInd.getParent().removeActor(spellFocusInd);
                        }
                        if (event.getTarget() instanceof SpellListing) {
                            selectedSpell = (SpellListing) event.getTarget();
                            SPDESCLAYOUT.setText(Andius.font14, selectedSpell.spell.getDescription(), Color.WHITE, 226, Align.left, true);
                            selectedSpell.addActor(spellFocusInd);
                        } else if (event.getTarget().getParent() instanceof SpellListing) {
                            selectedSpell = (SpellListing) event.getTarget().getParent();
                            SPDESCLAYOUT.setText(Andius.font14, selectedSpell.spell.getDescription(), Color.WHITE, 226, Align.left, true);
                            selectedSpell.addActor(spellFocusInd);
                        }
                    }

                    return false;
                }
            });

            avatar = new Image(Andius.faceTiles[sp.portaitIndex]);

            armorIcon = make(ItemType.ARMOR, sp.armor, icon(sp.armor), 260 + 2, 540 + 2);
            helmIcon = make(ItemType.HELMET, sp.helm, icon(sp.helm), 330 + 2, 540 + 2);
            shieldIcon = make(ItemType.SHIELD, sp.shield, icon(sp.shield), 400 + 2, 540 + 2);

            weaponIcon = make(ItemType.WEAPON, sp.weapon, icon(sp.weapon), 260 + 2, 470 + 2);
            avatar.setPosition(330, 470);
            glovesIcon = make(ItemType.GAUNTLET, sp.glove, icon(sp.glove), 400 + 2, 470 + 2);

            item1Icon = make(ItemType.MISC, sp.item1, icon(sp.item1), 260 + 2, 400 + 2);
            item2Icon = make(ItemType.MISC, sp.item2, icon(sp.item2), 400 + 2, 400 + 2);

            classL = new PlayerStatusLabel(character);
            classL.setPosition(250, 610);

            spptsL = new PlayerMagicPointsLabel(this.character);
            spptsL.setX(40);
            spptsL.setY(197);

            expL = new Label("" + character.exp, Andius.skin, "default-16");
            expL.setPosition(115, 164);

            hpL = new Label("" + character.hp, Andius.skin, "default-16");
            hpL.setPosition(115, 164 - 20);

            mxhpL = new Label("" + character.maxhp, Andius.skin, "default-16");
            mxhpL.setPosition(115, 164 - 40);

            acLabel = new Label("" + character.calculateAC(), Andius.skin, "default-16");
            acLabel.setPosition(115, 164 - 60);

            damageLabel = new Label(character.weapon != null ? character.weapon.damage.toString() : "1d2", Andius.skin, "default-16");
            damageLabel.setPosition(115, 164 - 80);

            goldLabel = new Label("" + character.gold, Andius.skin, "default-16");
            goldLabel.setPosition(115, 164 - 100);

            strL = new Label("" + character.str, Andius.skin, "default-16");
            strL.setPosition(265, 164);

            intL = new Label("" + character.intell, Andius.skin, "default-16");
            intL.setPosition(265, 164 - 20);

            ptyL = new Label("" + character.piety, Andius.skin, "default-16");
            ptyL.setPosition(265, 164 - 40);

            vitL = new Label("" + character.vitality, Andius.skin, "default-16");
            vitL.setPosition(265, 164 - 60);

            agiL = new Label("" + character.agility, Andius.skin, "default-16");
            agiL.setPosition(265, 164 - 80);

            lckL = new Label("" + character.luck, Andius.skin, "default-16");
            lckL.setPosition(265, 164 - 100);

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
            icons[11] = expL;
            icons[12] = hpL;
            icons[13] = mxhpL;
            icons[14] = strL;
            icons[15] = intL;
            icons[16] = ptyL;
            icons[17] = vitL;
            icons[18] = agiL;
            icons[19] = lckL;
            icons[20] = classL;
            icons[21] = spptsL;
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

                        if (old != null && !"BROKEN ITEM".equals(old.name)) {
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

        private class SpellChangeListener extends InputListener {

            Spells spell;
            final int slot;
            private final Label tooltip;

            SpellChangeListener(Spells spell, int slot, Label tooltip) {
                this.spell = spell;
                this.slot = slot;
                this.tooltip = tooltip;
                tooltip.setVisible(false);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (selectedSpell != null) {
                    Sounds.play(Sound.TRIGGER);
                    event.getTarget().setUserObject(selectedSpell.spell);
                    ((Image) event.getTarget()).setDrawable(new TextureRegionDrawable(invIcons[selectedSpell.spell.getIcon()]));
                    this.tooltip.setText(selectedSpell.spell.getTag());
                    this.spell = selectedSpell.spell;
                } else {
                    event.getTarget().setUserObject(null);
                    ((Image) event.getTarget()).setDrawable(new TextureRegionDrawable(invIcons[803]));
                    this.tooltip.setText("");
                    this.spell = null;
                }
                return false;
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                tooltip.setVisible(true);
                tooltip.toFront();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                tooltip.setVisible(false);
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

        final Item item;
        final Image icon;
        final Label name;
        final Label detail;
        final Image canusebkgnd;
        final CharacterRecord rec;

        ItemListing(Item item, CharacterRecord rec) {
            this.rec = rec;
            this.item = item;

            this.icon = new Image(icon(item));
            this.name = new Label(item.name, Andius.skin, "default-16");
            this.detail = new Label(item.vendorDescription(), Andius.skin, "default-12");
            this.canusebkgnd = new Image();

            boolean canUse = item.canUse(rec.classType);
            if (!canUse) {
                canusebkgnd.setDrawable(new TextureRegionDrawable(new TextureRegion(redBackgrnd)));
            } else {
                canusebkgnd.setDrawable(new TextureRegionDrawable(new TextureRegion(clearBackgrnd)));
            }

            addActor(this.icon);
            addActor(this.name);
            addActor(this.detail);
            addActor(this.canusebkgnd);

            this.icon.setBounds(3, 3, dim, dim);
            this.name.setPosition(dim + 10, 20);
            this.detail.setPosition(dim + 10, 3);
            this.canusebkgnd.setBounds(getX(), getY(), w, h);
            this.setBounds(getX(), getY(), w, h);

        }

    }

    private class SpellListing extends Group {

        Spells spell;
        final Image icon;
        final Label label;
        TextButton cast;

        SpellListing(Spells spell) {
            this.spell = spell;

            this.icon = new Image(invIcons[spell.getIcon()]);
            this.label = new Label(String.format("%d - %s", spell.getLevel(), spell.toString().toUpperCase()), Andius.skin, "default-16");

            addActor(this.icon);
            addActor(this.label);

            this.icon.setBounds(getX() + 3, getY() + 3, dim, dim);
            this.label.setPosition(getX() + dim + 10, getY() + 14);

            if (spell.getArea() != SpellArea.COMBAT && spell.getArea() != SpellArea.LOOTING) {
                this.cast = new TextButton("CAST", Andius.skin, "default-16-red");
                this.cast.setBounds(getX() + dim + 130, getY() + 10, 60, 25);
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

            this.setBounds(getX(), getY(), w, h);

        }

    }

    private class TradeSliderBox extends Group {

        final int width = 200;
        final int height = 150;
        private final List<TradeIndex> tradeSelection;

        TradeSliderBox() {

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
                        if (selectedItem == null || selectedPlayer.character == tradeSelection.getSelected().character) {
                            Sounds.play(Sound.NEGATIVE_EFFECT);
                        } else {
                            Sounds.play(Sound.TRIGGER);
                            for (PlayerIndex pi : playerSelection.getItems()) {
                                if (pi.character == tradeSelection.getSelected().character) {
                                    pi.invTable.add(new ItemListing(selectedItem.item, pi.character));
                                    pi.invTable.row();
                                    break;
                                }
                            }
                            selectedItem.removeActor(focusIndicator);
                            selectedPlayer.invTable.removeActor(selectedItem);
                            selectedItem = null;
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

            final SaveGame.CharacterRecord character;

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

        CastTargetSliderBox() {

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
                        if (spell != null && selectedPlayer.character != null && targetSelection.getSelected().character != null) {
                            SpellUtil.campCast(selectedPlayer.character, spell, targetSelection.getSelected().character);
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

            final SaveGame.CharacterRecord character;

            public TargetIndex(CharacterRecord character) {
                this.character = character;
            }

            @Override
            public String toString() {
                return character.name.toUpperCase();
            }
        }

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
        public StringBuilder getText() {
            return new StringBuilder(String.format("%s  LVL %d  %s  %s", rec.race.toString(), rec.level,
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

    private class PlayerMagicPointsLabel extends Label {

        private final CharacterRecord rec;

        public PlayerMagicPointsLabel(CharacterRecord rec) {
            super("", Andius.skin, "default-16");
            this.rec = rec;
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.StringBuilder getText() {
            int[] ms = rec.magePoints;
            int[] cs = rec.clericPoints;
            return new com.badlogic.gdx.utils.StringBuilder(
                    String.format("M: %d %d %d %d %d %d %d  P: %d %d %d %d %d %d %d",
                            ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6], cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
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
