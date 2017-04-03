/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius;

import andius.objects.Item;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Spells;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Paul
 */
public class CombatHud {

    private final Map<andius.objects.Actor, PlayerListing> map = new HashMap<>();
    private PlayerListing current;
    private final CombatScreen screen;
    private final TextureRegion[] invIcons = new TextureRegion[67 * 12];

    public CombatHud(CombatScreen screen, List<andius.objects.Actor> players) {
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

    }

    public void set(andius.objects.Actor player, Stage stage) {
        if (current != null) {
            current.remove();
        }
        PlayerListing pl = map.get(player);
        current = pl;
        current.set();
        stage.addActor(current);
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
        final Image weaponIcon;
        final Image armorIcon;
        final Image helmIcon;
        final Image shieldIcon;
        final Image glovesIcon;
        final Image item1Icon;
        final Image item2Icon;

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
            weaponIcon = image(rec.weapon, x, y + 96);
            armorIcon = image(rec.armor, x + 48, y + 96);
            helmIcon = image(rec.helm, x + 96, y + 96);
            shieldIcon = image(rec.shield, x, y + 48);
            glovesIcon = image(rec.glove, x + 48, y + 48);
            item1Icon = image(rec.item1, x, y + 0);
            item2Icon = image(rec.item2, x + 48, y + 0);

            addActor(this.weaponIcon);
            addActor(this.armorIcon);
            addActor(this.helmIcon);
            addActor(this.shieldIcon);
            addActor(this.glovesIcon);
            addActor(this.item1Icon);
            addActor(this.item2Icon);

            this.setBounds(734, 534, 265, 177);
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

        private Image image(Item it, float x, float y) {
            TextureRegion tr = (it == null ? invIcons[803] : invIcons[it.iconID]);
            Image im = new Image(tr);
            im.setX(x);
            im.setY(y);
            return im;
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

}
