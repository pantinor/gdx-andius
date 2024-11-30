/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius;

import static andius.Andius.mainGame;
import andius.objects.ClassType;
import andius.objects.Item;
import andius.objects.Item.ItemType;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Spells;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
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
import utils.Utils;

/**
 *
 * @author Paul
 */
public class EquipmentScreen implements Screen, Constants {

    private final Context context;

    private final Texture hud;
    private final SpriteBatch batch;
    private final Stage stage;

    private final List<PlayerIndex> playerSelection;

    private final TextButton exit;
    private final TextButton drop;
    private final TextButton unequip;
    private final TextButton trade;
    private final TextButton cancel;

    private final TradeSliderBox traderSlider;

    private final TextureRegion[] invIcons = new TextureRegion[67 * 12];

    private final Texture redBackgrnd = Utils.fillRectangle(w, h, Color.RED, .25f);
    private final Texture clearBackgrnd = Utils.fillRectangle(w, h, Color.CLEAR, 0f);

    private static final int w = 246;
    private static final int h = 50;
    private static final int dim = 44;

    Image selectedImage;
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

    ItemListing selectedItem;
    PlayerIndex selectedPlayer;
    SpellListing selectedSpell;
    private final GlyphLayout SPDESCLAYOUT = new GlyphLayout(Andius.font, "", Color.WHITE, 226, Align.left, true);

    ScrollPane invPane, spellPane;
    Image focusIndicator, spellFocusInd;
    Label invDesc;

    public EquipmentScreen(Context context, final Map contextMap) {
        this.context = context;

        this.hud = new Texture(Gdx.files.classpath("assets/data/equipment.png"));
        this.batch = new SpriteBatch();
        this.stage = new Stage();

        focusIndicator = new Image(Utils.fillRectangle(246, 50, Color.YELLOW, .35f));
        focusIndicator.setWidth(246);
        focusIndicator.setHeight(50);

        spellFocusInd = new Image(Utils.fillRectangle(246, 50, Color.YELLOW, .35f));
        spellFocusInd.setWidth(246);
        spellFocusInd.setHeight(50);

        traderSlider = new TradeSliderBox();

        invDesc = new Label("", Andius.skin, "larger");
        invDesc.setBounds(284, Andius.SCREEN_HEIGHT - 170, w, h);

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

        ScrollPane sp1 = new ScrollPane(this.playerSelection, Andius.skin);
        sp1.setBounds(93, Andius.SCREEN_HEIGHT - 372, 165, 241);

        invPane = new ScrollPane(playerSelection.getSelected().invTable, Andius.skin);
        invPane.setBounds(485, Andius.SCREEN_HEIGHT - 551, 246, 420);

        spellPane = new ScrollPane(playerSelection.getSelected().spellTable, Andius.skin);
        spellPane.setBounds(753, Andius.SCREEN_HEIGHT - 551, 246, 420);

        this.cancel = new TextButton("CNCL", Andius.skin, "red-larger");
        this.cancel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(contextMap.getScreen());
            }
        });
        this.cancel.setBounds(100, 220, 65, 40);

        this.exit = new TextButton("APPLY", Andius.skin, "red-larger");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                for (PlayerIndex pi : EquipmentScreen.this.playerSelection.getItems()) {
                    pi.save();
                }
                mainGame.setScreen(contextMap.getScreen());
            }
        });
        this.exit.setBounds(175, 220, 65, 40);

        this.trade = new TextButton("TRAD", Andius.skin, "red-larger");
        this.trade.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                traderSlider.show();
            }
        });
        this.trade.setBounds(250, 220, 65, 40);

        this.unequip = new TextButton("REMV", Andius.skin, "red-larger");
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
        this.unequip.setBounds(325, 220, 65, 40);

        this.drop = new TextButton("DROP", Andius.skin, "red-larger");
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
        this.drop.setBounds(400, 220, 65, 40);

        stage.addActor(sp1);
        stage.addActor(invPane);
        stage.addActor(spellPane);
        stage.addActor(exit);
        stage.addActor(cancel);
        stage.addActor(trade);
        stage.addActor(drop);
        stage.addActor(invDesc);
        stage.addActor(unequip);
        stage.addActor(traderSlider);

        Label inventory = new Label("Inventory", Andius.skin, "larger");
        Label splBk = new Label("Known Spells", Andius.skin, "larger");
        Label presets = new Label("Spell Presets", Andius.skin, "larger");
        inventory.setBounds(512, Andius.SCREEN_HEIGHT - 160, 20, 100);
        splBk.setBounds(770, Andius.SCREEN_HEIGHT - 160, 20, 100);
        presets.setBounds(826, Andius.SCREEN_HEIGHT - 735, 20, 100);
        stage.addActor(inventory);
        stage.addActor(splBk);
        stage.addActor(presets);

        ChangeListener cl = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                selectedItem = null;
                selectedSpell = null;
                if (selectedPlayer != null) {
                    for (Actor a : selectedPlayer.icons) {
                        a.remove();
                    }
                    for (Actor a : selectedPlayer.slots) {
                        a.remove();
                    }
                    for (Actor a : selectedPlayer.slotTooltips) {
                        a.remove();
                    }
                }
                selectedPlayer = playerSelection.getSelected();
                for (Actor a : selectedPlayer.icons) {
                    stage.addActor(a);
                }
                for (Actor a : selectedPlayer.slots) {
                    stage.addActor(a);
                }
                for (Actor a : selectedPlayer.slotTooltips) {
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
        Andius.largeFont.draw(batch, "AC", 310, Andius.SCREEN_HEIGHT - 393 + 24);
        Andius.largeFont.draw(batch, "DAMG", 300, Andius.SCREEN_HEIGHT - 437 + 24);
        Andius.largeFont.draw(batch, "GOLD", 300, Andius.SCREEN_HEIGHT - 480 + 24);

        Andius.largeFont.draw(batch, "EXP", 76, Andius.SCREEN_HEIGHT - 620);
        Andius.largeFont.draw(batch, "HP", 82, Andius.SCREEN_HEIGHT - 666);
        Andius.largeFont.draw(batch, "MAXHP", 50, Andius.SCREEN_HEIGHT - 710);

        Andius.largeFont.draw(batch, "STR", 240, Andius.SCREEN_HEIGHT - 620);
        Andius.largeFont.draw(batch, "INT", 242, Andius.SCREEN_HEIGHT - 666);
        Andius.largeFont.draw(batch, "PTY", 240, Andius.SCREEN_HEIGHT - 710);

        Andius.largeFont.draw(batch, "VIT", 387, Andius.SCREEN_HEIGHT - 620);
        Andius.largeFont.draw(batch, "AGI", 387, Andius.SCREEN_HEIGHT - 666);
        Andius.largeFont.draw(batch, "LCK", 385, Andius.SCREEN_HEIGHT - 710);

        if (selectedSpell != null) {
            Andius.font.draw(batch, SPDESCLAYOUT, 520, Andius.SCREEN_HEIGHT - 579);
        }

        batch.end();

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
        final Label spptsL;
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
        final Image[] slots = new Image[10];
        final Label[] slotTooltips = new Label[10];

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

            spellTable.align(Align.top);

            for (Spells spell : character.knownSpells) {
                spellTable.add(new SpellListing(spell));
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
                            SPDESCLAYOUT.setText(Andius.font, selectedSpell.spell.getDescription(), Color.WHITE, 226, Align.left, true);
                            selectedSpell.addActor(spellFocusInd);
                        } else if (event.getTarget().getParent() instanceof SpellListing) {
                            selectedSpell = (SpellListing) event.getTarget().getParent();
                            SPDESCLAYOUT.setText(Andius.font, selectedSpell.spell.getDescription(), Color.WHITE, 226, Align.left, true);
                            selectedSpell.addActor(spellFocusInd);
                        }
                    }

                    return false;
                }
            }
            );

            avatar = new Image(Andius.faceTiles[sp.portaitIndex]);
            avatar.setX(348);
            avatar.setY(Andius.SCREEN_HEIGHT - 279);

            weaponIcon = make(ItemType.WEAPON, sp.weapon, icon(sp.weapon), 283, Andius.SCREEN_HEIGHT - 276);
            armorIcon = make(ItemType.ARMOR, sp.armor, icon(sp.armor), 283, Andius.SCREEN_HEIGHT - 220);
            helmIcon = make(ItemType.HELMET, sp.helm, icon(sp.helm), 349, Andius.SCREEN_HEIGHT - 220);
            shieldIcon = make(ItemType.SHIELD, sp.shield, icon(sp.shield), 415, Andius.SCREEN_HEIGHT - 220);
            glovesIcon = make(ItemType.GAUNTLET, sp.glove, icon(sp.glove), 415, Andius.SCREEN_HEIGHT - 276);
            item1Icon = make(ItemType.MISC, sp.item1, icon(sp.item1), 322, Andius.SCREEN_HEIGHT - 333);
            item2Icon = make(ItemType.MISC, sp.item2, icon(sp.item2), 376, Andius.SCREEN_HEIGHT - 333);

            String d2 = String.format("%s  LVL %d  %s", character.race.toString(), character.level, character.classType.toString());
            classL = new Label(d2, Andius.skin, "larger");
            classL.setX(90);
            classL.setY(Andius.SCREEN_HEIGHT - 420);

            int[] ms = character.magePoints;
            int[] cs = character.clericPoints;
            String d4 = String.format("M: %d %d %d %d %d %d %d  P: %d %d %d %d %d %d %d", 
                    ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6], cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]);
            spptsL = new Label(d4, Andius.skin, "larger");
            spptsL.setX(90);
            spptsL.setY(Andius.SCREEN_HEIGHT - 600);

            acLabel = new Label("" + character.calculateAC(), Andius.skin, "larger");
            acLabel.setX(360);
            acLabel.setY(Andius.SCREEN_HEIGHT - 393);

            damageLabel = new Label(character.weapon != null ? character.weapon.damage.toString() : "1d2", Andius.skin, "larger");
            damageLabel.setX(360);
            damageLabel.setY(Andius.SCREEN_HEIGHT - 437);

            goldLabel = new Label("" + character.gold, Andius.skin, "larger");
            goldLabel.setX(360);
            goldLabel.setY(Andius.SCREEN_HEIGHT - 480);

            expL = new Label("" + character.exp, Andius.skin, "larger");
            expL.setX(115);
            expL.setY(Andius.SCREEN_HEIGHT - 620 - 23);

            hpL = new Label("" + character.hp, Andius.skin, "larger");
            hpL.setX(115);
            hpL.setY(Andius.SCREEN_HEIGHT - 666 - 23);

            mxhpL = new Label("" + character.maxhp, Andius.skin, "larger");
            mxhpL.setX(115);
            mxhpL.setY(Andius.SCREEN_HEIGHT - 710 - 23);

            strL = new Label("" + character.str, Andius.skin, "larger");
            strL.setX(287);
            strL.setY(Andius.SCREEN_HEIGHT - 620 - 23);

            intL = new Label("" + character.intell, Andius.skin, "larger");
            intL.setX(287);
            intL.setY(Andius.SCREEN_HEIGHT - 666 - 23);

            ptyL = new Label("" + character.piety, Andius.skin, "larger");
            ptyL.setX(287);
            ptyL.setY(Andius.SCREEN_HEIGHT - 710 - 23);

            vitL = new Label("" + character.vitality, Andius.skin, "larger");
            vitL.setX(427);
            vitL.setY(Andius.SCREEN_HEIGHT - 620 - 23);

            agiL = new Label("" + character.agility, Andius.skin, "larger");
            agiL.setX(427);
            agiL.setY(Andius.SCREEN_HEIGHT - 666 - 23);

            lckL = new Label("" + character.luck, Andius.skin, "larger");
            lckL.setX(427);
            lckL.setY(Andius.SCREEN_HEIGHT - 710 - 23);

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

            int x = 762;
            for (int i = 0; i < 5; i++) {
                slots[i] = make(character.spellPresets[i], x, Andius.SCREEN_HEIGHT - 614, i);
                x = x + 44 + 3;
            }
            x = 762;
            for (int i = 0; i < 5; i++) {
                slots[i + 5] = make(character.spellPresets[i + 5], x, Andius.SCREEN_HEIGHT - 660, i + 5);
                x = x + 44 + 3;
            }
        }

        private Image make(ItemType type, Item it, TextureRegion tr, int x, int y) {
            Image im = new Image(tr);
            im.setX(x);
            im.setY(y);
            im.setUserObject(it);
            im.addListener(new InvItemChangeListener(type));
            return im;
        }

        private Image make(Spells spell, int x, int y, int slot) {
            Image im = new Image(spell != null && character.knownSpells.contains(spell) ? invIcons[spell.getIcon()] : invIcons[803]);
            im.setX(x);
            im.setY(y);
            im.setUserObject(spell);
            slotTooltips[slot] = new Label(spell != null && character.knownSpells.contains(spell) ? spell.getTag() : "", Andius.skin, "hudSmallFont");
            slotTooltips[slot].setX(x);
            slotTooltips[slot].setY(y + 44);
            im.addListener(new SpellChangeListener(spell, slot, slotTooltips[slot]));
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

            for (int i = 0; i < 10; i++) {
                if (slots[i].getUserObject() != null) {
                    character.spellPresets[i] = (Spells) slots[i].getUserObject();
                } else {
                    character.spellPresets[i] = null;
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

    private TextureRegion icon(Item it) {
        return (it == null ? invIcons[803] : invIcons[it.iconID]);
    }

    private class ItemListing extends Group {

        Item item;
        final Image icon;
        final Label label;
        final Image canusebkgnd;
        final CharacterRecord rec;

        ItemListing(Item item, CharacterRecord rec) {
            this.rec = rec;
            this.item = item;

            this.icon = new Image(icon(item));
            this.label = new Label(item.name, Andius.skin, "larger");
            this.canusebkgnd = new Image();

            boolean canUse = item.canUse(rec.classType);
            if (!canUse) {
                canusebkgnd.setDrawable(new TextureRegionDrawable(new TextureRegion(redBackgrnd)));
            } else {
                canusebkgnd.setDrawable(new TextureRegionDrawable(new TextureRegion(clearBackgrnd)));
            }

            addActor(this.icon);
            addActor(this.label);
            addActor(this.canusebkgnd);

            this.icon.setBounds(getX() + 3, getY() + 3, dim, dim);
            this.label.setPosition(getX() + dim + 10, getY() + 10);
            this.canusebkgnd.setBounds(getX(), getY(), w, h);
            this.setBounds(getX(), getY(), w, h);

        }

    }

    private class SpellListing extends Group {

        Spells spell;
        final Image icon;
        final Label label;

        SpellListing(Spells spell) {
            this.spell = spell;

            this.icon = new Image(invIcons[spell.getIcon()]);
            this.label = new Label(String.format("%d - %s", spell.getLevel(), spell.toString().toUpperCase()), Andius.skin, "larger");

            addActor(this.icon);
            addActor(this.label);

            this.icon.setBounds(getX() + 3, getY() + 3, dim, dim);
            this.label.setPosition(getX() + dim + 10, getY() + 10);

            this.setBounds(getX(), getY(), w, h);

        }

    }

    private class TradeSliderBox extends Group {

        final int width = 200;
        final int height = 240;
        private final List<TradeIndex> tradeSelection;

        TradeSliderBox() {

            Image background = new Image(Utils.fillRectangle(width, height, Color.BROWN, .75f));
            background.setBounds(getX(), getY(), width, height);
            addActor(background);

            this.tradeSelection = new List<>(Andius.skin, "larger");
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
                            return false;
                        }
                        Sounds.play(Sound.TRIGGER);
                        for (PlayerIndex pi : playerSelection.getItems()) {
                            if (pi.character == tradeSelection.getSelected().character) {
                                pi.invTable.add(new ItemListing(selectedItem.item, selectedItem.rec));
                                pi.invTable.row();
                                break;
                            }
                        }
                        selectedItem.removeActor(focusIndicator);
                        selectedPlayer.invTable.removeActor(selectedItem);
                        selectedItem = null;
                        TradeSliderBox.this.hide();
                    }
                    return false;
                }
            });

            ScrollPane tradePane = new ScrollPane(this.tradeSelection, Andius.skin);
            tradePane.setBounds(getX(), getY(), width, height);
            addActor(tradePane);

            setBounds(Andius.SCREEN_WIDTH, 200, width, height);
        }

        void show() {
            if (getActions().size > 0) {
                clearActions();
            }
            setPosition(Andius.SCREEN_WIDTH, 200);
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
