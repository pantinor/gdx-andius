package andius;

import static andius.Constants.TILE_DIM;
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

public class CombatHud {

    private static final Texture BCKGRND = statsBackground();
    private final Map<andius.objects.Actor, PlayerListing> map = new HashMap<>();
    private PlayerListing current;
    private final CombatScreen screen;
    private final TextureRegion[] invIcons = new TextureRegion[67 * 12];
    private final GlyphLayout glyph = new GlyphLayout(Andius.font, "", Color.WHITE, 32, Align.left, true);

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
            if (item != null && item.spell != null) {
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

    public void drawStats(SpriteBatch batch, andius.objects.Actor pl) {

        CharacterRecord rec = pl.getPlayer();
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%s  LVL %d\n", rec.name.toUpperCase(), rec.level));
        sb.append(String.format("%s  %s\n", rec.race.toString(), rec.classType.toString()));
        sb.append(String.format("HP: %d / %d  AC: %d\n", rec.hp, rec.maxhp, rec.calculateAC()));
        sb.append(String.format("ST: %s\n", rec.status.toString()));
        int[] ms = rec.magePoints;
        int[] cs = rec.clericPoints;
        sb.append(String.format("MG Pts: %d %d %d %d %d %d %d\n",
                ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6]));
        sb.append(String.format("PR Pts: %d %d %d %d %d %d %d\n",
                cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]));

        int dim = TILE_DIM * 3;

        glyph.setText(Andius.hudLogFont, sb.toString(), rec.status.color(), dim, Align.left, true);
        batch.draw(BCKGRND, pl.getX() - TILE_DIM, pl.getY() - TILE_DIM * 2);
        Andius.hudLogFont.draw(batch, glyph, pl.getX() - TILE_DIM + 4, pl.getY() + TILE_DIM - 5);
    }

    public void drawStatsMonster(SpriteBatch batch, andius.objects.Actor mon) {

        MutableMonster rec = mon.getMonster();
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%s  \n", rec.name.toUpperCase()));
        sb.append(String.format("LVL %d  %s\n", rec.getLevel(), rec.getType().toString()));
        sb.append(String.format("HP: %d / %d  AC: %d\n", rec.getCurrentHitPoints(), rec.getMaxHitPoints(), rec.getArmourClass()));
        sb.append(String.format("ST: %s\n", rec.status().toString()));
        sb.append(String.format("MG LVL: %d \n", rec.getCurrentMageSpellLevel()));
        sb.append(String.format("PR LVL: %d \n", rec.getPriestSpellLevel()));

        int dim = TILE_DIM * 3;

        glyph.setText(Andius.hudLogFont, sb.toString(), rec.status().color(), dim, Align.left, true);
        batch.draw(BCKGRND, mon.getX() - TILE_DIM, mon.getY() + TILE_DIM * 4);
        Andius.hudLogFont.draw(batch, glyph, mon.getX() - TILE_DIM + 4, mon.getY() + TILE_DIM * 7 - 10);
    }

    private static Texture statsBackground() {
        Pixmap pixmap = new Pixmap(TILE_DIM * 3, TILE_DIM * 3, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.1f, 0.1f, 0.1f, 0.7f));
        pixmap.fillRectangle(0, 0, TILE_DIM * 3, TILE_DIM * 3);
        return new Texture(pixmap);
    }

}
