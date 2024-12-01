/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius;

import andius.objects.Item;
import andius.objects.MutableMonster;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Spells;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Paul
 */
public class CombatHud {

    private final Map<andius.objects.Actor, PlayerListing> map = new HashMap<>();
    private PlayerListing current;
    private final CombatScreen screen;
    private final TextureRegion[] invIcons = new TextureRegion[67 * 12];
    private final Texture[] partyHealthFrames = new Texture[6];
    private final Texture[] monsterHealthFrames = new Texture[16];
    private final GlyphLayout glyph = new GlyphLayout(Andius.smallFont, "", Color.WHITE, 32, Align.left, true);

    public CombatHud(CombatScreen screen, Set<andius.objects.Actor> players) {
        this.screen = screen;

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

        for (andius.objects.Actor a : players) {
            map.put(a, new PlayerListing(a));
        }

        for (int x = 0; x < partyHealthFrames.length; x++) {
            partyHealthFrames[x] = createHealthFrame(128, 32);
        }
        for (int x = 0; x < monsterHealthFrames.length; x++) {
            monsterHealthFrames[x] = createHealthFrame(86, 36);
        }

    }

    public void set(andius.objects.Actor player, Stage stage) {
        if (current != null) {
            current.remove();
        }
        PlayerListing pl = map.get(player);
        if (pl != null) {
            current = pl;
            current.set();
            stage.addActor(current);
        }
    }

    private class PlayerListing extends Group {

        final Image icon;
        final ImageButton[] slotButtons = new ImageButton[10];
        final Label[] slotTooltips = new Label[10];
        final Label l1;
        final Label l2;
        final Label l3;
        final Label l4;
        final andius.objects.Actor player;
        final CharacterRecord rec;

        final static int WEAPON_IDX = 0;
        final static int ARMOR_IDX = 1;
        final static int HELM_IDX = 2;
        final static int SHIELD_IDX = 3;
        final static int GLOVES_IDX = 4;
        final static int ITEM1_IDX = 5;
        final static int ITEM2_IDX = 6;

        final ImageButton[] itemButtons = new ImageButton[7];
        final Label[] itemTooltips = new Label[7];

        PlayerListing(andius.objects.Actor player) {
            this.player = player;
            this.rec = player.getPlayer();
            this.icon = new Image(Andius.faceTiles[rec.portaitIndex]);
            this.l1 = new Label("", Andius.skin, "hudLogFont");
            this.l2 = new Label("", Andius.skin, "hudLogFont");
            this.l3 = new Label("", Andius.skin, "hudLogFont");
            this.l4 = new Label("", Andius.skin, "hudLogFont");

            for (int i = 0; i < 10; i++) {
                if (rec.spellPresets[i] != null && rec.knownSpells.contains(rec.spellPresets[i])) {
                    TextureRegionDrawable t1 = new TextureRegionDrawable(Andius.invIcons[rec.spellPresets[i].getIcon()]);
                    slotButtons[i] = new ImageButton(t1, t1.tint(Color.LIGHT_GRAY));
                    slotTooltips[i] = new Label(rec.spellPresets[i].getTag(), Andius.skin, "hudSmallFont");
                    SpellSlotListener l = new SpellSlotListener(player, rec.spellPresets[i], i, slotTooltips[i]);
                    slotButtons[i].addListener(l);
                    addActor(slotTooltips[i]);
                    addActor(slotButtons[i]);
                }
            }
            addActor(this.icon);
            addActor(this.l1);
            addActor(this.l2);
            addActor(this.l3);
            addActor(this.l4);

            float x = getX();
            this.icon.setPosition(x, 124);

            this.l1.setPosition(x + 60, 112 + 56);
            this.l2.setPosition(x + 60, 112 + 40);
            this.l3.setPosition(x + 60, 112 + 24);
            this.l4.setPosition(x + 40, -12);

            x = getX() + 15;
            for (int i = 0; i < 5; i++) {
                if (this.slotButtons[i] != null) {
                    this.slotButtons[i].setPosition(x, 44 + 3);
                    this.slotTooltips[i].setPosition(getX() + 15, 92);
                }
                x = x + 44 + 3;
            }
            x = getX() + 15;
            for (int i = 0; i < 5; i++) {
                if (this.slotButtons[i + 5] != null) {
                    this.slotButtons[i + 5].setPosition(x, 0);
                    this.slotTooltips[i + 5].setPosition(getX() + 15, 92);
                }
                x = x + 44 + 3;
            }

            x = getX() + 15;
            int y = -200;

            setItemButton(WEAPON_IDX, rec.weapon, x, y + 96);
            setItemButton(ARMOR_IDX, rec.armor, x + 48, y + 96);
            setItemButton(HELM_IDX, rec.helm, x + 96, y + 96);
            setItemButton(SHIELD_IDX, rec.shield, x, y + 48);
            setItemButton(GLOVES_IDX, rec.glove, x + 48, y + 48);
            setItemButton(ITEM1_IDX, rec.item1, x, y + 0);
            setItemButton(ITEM2_IDX, rec.item2, x + 48, y + 0);

            this.setBounds(734, 534, 265, 177);
        }

        private void setItemButton(int i, Item item, float x, float y) {
            if (item != null) {
                TextureRegionDrawable t1 = new TextureRegionDrawable(invIcons[item.iconID]);
                itemButtons[i] = new ImageButton(t1, t1.tint(Color.LIGHT_GRAY));
                itemButtons[i].setX(x);
                itemButtons[i].setY(y);
                itemTooltips[i] = new Label(item.name, Andius.skin, "hudSmallFont");
                itemTooltips[i].setPosition(x, y);
                ItemListener l = new ItemListener(player, item, itemTooltips[i]);
                itemButtons[i].addListener(l);
                addActor(itemTooltips[i]);
                addActor(itemButtons[i]);
            }
        }

        public void set() {
            String d1 = String.format("%s", rec.name.toUpperCase());
            String d2 = String.format("%s  LVL %d  %s", rec.race.toString(), rec.level, rec.classType.toString());
            String d3 = String.format("HP: %d /%d  AC: %d  ST: %s", rec.hp, rec.maxhp, rec.calculateAC(), rec.status);
            int[] ms = rec.magePoints;
            int[] cs = rec.clericPoints;
            String d4 = String.format("M: %d %d %d %d %d %d %d  P: %d %d %d %d %d %d %d",
                    ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6], cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]);
            this.l1.setText(d1);
            this.l2.setText(d2);
            this.l3.setText(d3);
            this.l4.setText(d4);
        }

    }

    private class ItemListener extends InputListener {

        final andius.objects.Actor player;
        Item item;
        private final Label tooltip;

        ItemListener(andius.objects.Actor player, Item item, Label tooltip) {
            this.player = player;
            this.item = item;
            this.tooltip = tooltip;
            tooltip.setVisible(false);
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (item != null && item.spell != null ) {
                tooltip.setText(item.name);
                screen.initCast(item.spell, player);
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

    private class SpellSlotListener extends InputListener {

        final andius.objects.Actor player;
        Spells spell;
        final int slot;
        private final Label tooltip;

        SpellSlotListener(andius.objects.Actor player, Spells spell, int slot, Label tooltip) {
            this.player = player;
            this.spell = spell;
            this.slot = slot;
            this.tooltip = tooltip;
            tooltip.setVisible(false);
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (spell != null) {
                screen.initCast(spell, player);
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

    public void render(SpriteBatch batch, MutableMonster[] crSlots, Set<andius.objects.Actor> partyMembers) {
        int x = 8;
        for (int i = 0; i < 6; i++) {
            batch.draw(partyHealthFrames[i], x, Andius.SCREEN_HEIGHT - 34);
            x += 128 + 3;
        }
        x = 8;
        for (int i = 0; i < 8; i++) {
            batch.draw(monsterHealthFrames[i], x, 44);
            x += 86 + 1;
        }
        x = 8;
        for (int i = 8; i < 16; i++) {
            batch.draw(monsterHealthFrames[i], x, 6);
            x += 86 + 1;
        }

        x = 8;
        for (andius.objects.Actor p : partyMembers) {
            String txt = String.format("%s (%d) %d %s",
                    p.getName(),
                    p.getPlayer().calculateAC(),
                    p.getPlayer().maxhp,
                    p.getPlayer().status.isDisabled() ?  p.getPlayer().status.toString() : "");

            glyph.setText(Andius.smallFont, txt, Color.WHITE, 124, Align.left, true);
            Andius.smallFont.draw(batch, glyph, x + 3,  Andius.SCREEN_HEIGHT - 4);

            batch.draw(p.getHealthBar(), x + 2,  Andius.SCREEN_HEIGHT - 31);
            x += 128 + 3;
        }

        x = 10;
        int count = 0;
        for (int i = 0; i < 16; i++) {
            if (crSlots[i] != null && crSlots[i].getCurrentHitPoints() > 0) {

                String txt = String.format("%s [%d - %d] %s",
                        crSlots[i].getName(),
                        crSlots[i].getLevel(),
                        crSlots[i].getMaxHitPoints(),
                        crSlots[i].status().isDisabled() ? crSlots[i].status().toString() : "");

                glyph.setText(Andius.smallFont, txt, Color.WHITE, 80, Align.left, true);
                Andius.smallFont.draw(batch, glyph, x, count < 8 ? 84 : 46);

                batch.draw(crSlots[i].getHealthBar(), x, count < 8 ? 46 : 8);

                count++;
                x += 86 + 1;
                if (count == 8) {
                    x = 10;
                }
            }
        }
    }

    private Texture createHealthFrame(int w, int h) {

        int lw = 2;

        Pixmap pix = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        pix.setColor(Color.DARK_GRAY);
        pix.fillRectangle(0, 0, w, lw);//top
        pix.fillRectangle(0, h - lw, w, lw);//bottom
        pix.fillRectangle(0, 0, lw, h);//left
        pix.fillRectangle(w - lw, 0, lw, h);//right

        Texture t = new Texture(pix);
        pix.dispose();
        return t;
    }

}
