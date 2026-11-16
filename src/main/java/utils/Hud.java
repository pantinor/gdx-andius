package utils;

import andius.Andius;
import andius.Context;
import andius.objects.MutableMonster;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import java.util.Arrays;
import static utils.SkinUtils.BORDER;
import static utils.SkinUtils.GOLD;
import static utils.SkinUtils.MUTED;
import static utils.SkinUtils.PANEL;
import static utils.SkinUtils.createSkin;

public class Hud implements Disposable {

    private final LogScrollPane logs;

    public static final int MAP_X = 24, MAP_Y = 80, MAP_SIZE = 624;
    public static final int TILE_MAP_X = 48, TILE_MAP_Y = 88, TILE_MAP_WIDTH = 576, TILE_MAP_HEIGHT = 608;

    static final int LOG_AREA_WIDTH = 304;
    static final int LOG_AREA_HEIGHT = 112;

    private static final int PARTY_X = 684, PARTY_WIDTH = 304, PARTY_ROW_HEIGHT = 84;

    private final Skin skin = createSkin(Andius.skin);
    private final BitmapFont body = skin.getFont("body");
    private final BitmapFont emphasis = skin.getFont("emphasis");
    private final Texture panelPixel = Utils.fillRectangle(1, 1, Color.WHITE, 1);

    private final Table summonedTable = new Table();
    private final AutoFocusScrollPane summonedScroll = new AutoFocusScrollPane(summonedTable, skin);
    private String summonedContent = "";

    public Hud() {
        styleControls();
        Table table = new Table(skin);
        logs = new LogScrollPane(skin, table, LOG_AREA_WIDTH, "default-12");
        logs.setBounds(PARTY_X, 36, LOG_AREA_WIDTH, LOG_AREA_HEIGHT);
        logs.setFadeScrollBars(false);
        summonedTable.top().left();
        summonedScroll.setStyle(skin.get(ScrollPane.ScrollPaneStyle.class));
        summonedScroll.setBounds(PARTY_X, 204, PARTY_WIDTH, 368);
        summonedScroll.setScrollingDisabled(true, false);
        summonedScroll.setFadeScrollBars(false);
        summonedScroll.setVisible(false);
    }

    private Drawable drawable(String color) {
        return new TextureRegionDrawable(new TextureRegion(panelPixel)).tint(Color.valueOf(color));
    }

    private void styleControls() {
        TextButton.TextButtonStyle button = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        button.up = drawable("223443");
        button.over = drawable("344c5d");
        button.down = drawable("66583f");
        button.fontColor = Color.valueOf("e7e6df");
        skin.add("default", button);
        Window.WindowStyle window = new Window.WindowStyle(skin.get(Window.WindowStyle.class));
        window.background = drawable("15232f");
        window.titleFontColor = GOLD;
        skin.add("default", window);
        TextField.TextFieldStyle field = new TextField.TextFieldStyle(Andius.skin.get("default-16", TextField.TextFieldStyle.class));
        field.font = emphasis;
        field.background = drawable("0c1822");
        field.focusedBackground = drawable("263e4c");
        skin.add("default", field);
        com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle list = new com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle(
                skin.get("default-12-padded-clear", com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle.class));
        BaseDrawable selection = (BaseDrawable) drawable("344b59");
        selection.setTopHeight(6);
        selection.setBottomHeight(6);
        selection.setLeftWidth(8);
        selection.setRightWidth(8);
        list.selection = selection;
        list.fontColorSelected = GOLD;
        skin.add("default-12-padded-clear", list);
        ScrollPane.ScrollPaneStyle scroll = new ScrollPane.ScrollPaneStyle();
        BaseDrawable track = (BaseDrawable) drawable("0c1822");
        track.setMinWidth(6);
        BaseDrawable knob = (BaseDrawable) drawable("7f755f");
        knob.setMinWidth(6);
        knob.setMinHeight(24);
        scroll.vScroll = track;
        scroll.vScrollKnob = knob;
        skin.add("default", scroll);
    }

    public void applyMapViewport(Viewport ui) {
        applyMapViewport(ui, false);
    }

    public void applyMapViewport(Viewport ui, boolean tileMap) {
        float scale = ui.getScreenWidth() / (float) Andius.SCREEN_WIDTH;
        int x = tileMap ? TILE_MAP_X : MAP_X;
        int y = tileMap ? TILE_MAP_Y : MAP_Y;
        int width = tileMap ? TILE_MAP_WIDTH : MAP_SIZE;
        int height = tileMap ? TILE_MAP_HEIGHT : MAP_SIZE;
        HdpiUtils.glViewport(ui.getScreenX() + Math.round(x * scale),
                ui.getScreenY() + Math.round(y * scale),
                Math.round(width * scale), Math.round(height * scale));
    }

    public void renderTileMapBackground(Batch batch) {
        rect(batch, Color.BLACK, MAP_X, MAP_Y, MAP_SIZE, MAP_SIZE);
    }

    public void renderTileMapSurround(Batch batch) {
        rect(batch, Color.BLACK, MAP_X, MAP_Y, TILE_MAP_X - MAP_X, MAP_SIZE);
        rect(batch, Color.BLACK, TILE_MAP_X + TILE_MAP_WIDTH, MAP_Y, MAP_X + MAP_SIZE - TILE_MAP_X - TILE_MAP_WIDTH, MAP_SIZE);
        rect(batch, Color.BLACK, TILE_MAP_X, MAP_Y, TILE_MAP_WIDTH, TILE_MAP_Y - MAP_Y);
        rect(batch, Color.BLACK, TILE_MAP_X, TILE_MAP_Y + TILE_MAP_HEIGHT, TILE_MAP_WIDTH,
                MAP_Y + MAP_SIZE - TILE_MAP_Y - TILE_MAP_HEIGHT);
    }

    private void rect(Batch batch, Color color, float x, float y, float width, float height) {
        float previous = batch.getPackedColor();
        batch.setColor(color);
        batch.draw(panelPixel, x, y, width, height);
        batch.setPackedColor(previous);
    }

    public Skin getSkin() {
        return skin;
    }

    public void renderLocation(Batch batch, String title, int east, int north) {
        renderHeader(batch, title, "East " + east + "  North " + north);
    }

    public void renderDungeonLocation(Batch batch, String title, int level, int east, int north) {
        renderHeader(batch, title, "Level " + level + "  East  " + east + "  North " + north);
    }

    private void renderHeader(Batch batch, String title, String detail) {
        BitmapFont titleFont = skin.getFont("title");
        titleFont.setColor(GOLD);
        drawLine(batch, titleFont, title, MAP_X, 746, MAP_SIZE, Align.left);
        body.setColor(MUTED);
        drawLine(batch, body, detail, MAP_X, 720, MAP_SIZE, Align.left);
        titleFont.setColor(Color.WHITE);
        body.setColor(Color.WHITE);
    }

    private static void drawLine(Batch batch, BitmapFont font, String text, float x, float y, float width, int alignment) {
        font.draw(batch, text, x, y, 0, text.length(), width, alignment, false, "...");
    }

    public void addActor(Stage stage) {
        logs.remove();
        stage.addActor(logs);
        logs.setZIndex(0);
        summonedScroll.remove();
        stage.addActor(summonedScroll);
        summonedScroll.setZIndex(1);
    }

    public void log(String s) {
        logs.add(s, Color.WHITE);
    }

    public void log(String s, Color c) {
        logs.add(s, c);
    }

    public void render(Batch batch, Context ctxt) {
        render(batch, ctxt, true);
    }

    public void render(Batch batch, Context ctxt, boolean showGoldAndExperience) {
        summonedScroll.setVisible(false);
        rect(batch, BORDER, MAP_X - 1, MAP_Y - 1, MAP_SIZE + 2, 1);
        rect(batch, BORDER, MAP_X - 1, MAP_Y + MAP_SIZE, MAP_SIZE + 2, 1);
        rect(batch, BORDER, MAP_X - 1, MAP_Y, 1, MAP_SIZE);
        rect(batch, BORDER, MAP_X + MAP_SIZE, MAP_Y, 1, MAP_SIZE);
        rect(batch, PANEL, 672, 192, 328, 512);
        rect(batch, BORDER, 672, 703, 328, 1);
        rect(batch, PANEL, 672, 24, 328, 152);
        rect(batch, BORDER, 672, 175, 328, 1);
        BitmapFont heading = skin.getFont("heading");
        heading.setColor(GOLD);
        drawLine(batch, heading, "The expedition", 672, 744, 328, Align.left);
        body.setColor(MUTED);
        drawLine(batch, body, ctxt.players().length + " adventurers  /  marching order", 672, 720, 328, Align.left);
        drawLine(batch, heading, "Journal", PARTY_X, 164, PARTY_WIDTH, Align.left);
        heading.setColor(Color.WHITE);

        float top = 696;
        for (int slot = 0; slot < 6; slot++) {
            float bottom = top - 76;
            if (slot < ctxt.players().length) {
                CharacterRecord rec = ctxt.players()[slot];
                Color status = rec.isDead() ? Color.SALMON : rec.status.color();
                batch.draw(andius.objects.Portraits.get(rec), PARTY_X, bottom, 76, 76);
                float textX = PARTY_X + 88;
                float textWidth = PARTY_WIDTH - 88;
                emphasis.setColor(Color.valueOf("e7e6df"));
                drawLine(batch, emphasis, rec.name, textX, top - 2, textWidth, Align.left);
                body.setColor(MUTED);
                drawLine(batch, body, "Lv " + rec.level + "  " + rec.race + " " + rec.classType,
                        textX, top - 19, textWidth, Align.left);
                body.setColor(status);
                drawLine(batch, body, "HP " + rec.hp + "/" + rec.maxhp + "   AC " + rec.calculateAC() + "   " + rec.status,
                        textX, top - 36, textWidth, Align.left);
                body.setColor(MUTED);
                boolean magic = Arrays.stream(rec.magePoints).sum() + Arrays.stream(rec.clericPoints).sum() > 0;
                if (magic) {
                    drawLine(batch, body, "Mage  " + spellPoints(rec.magePoints), textX, top - 53, textWidth, Align.left);
                    drawLine(batch, body, "Priest  " + spellPoints(rec.clericPoints), textX, top - 70, textWidth, Align.left);
                } else if (showGoldAndExperience) {
                    drawLine(batch, body, "Gold " + rec.gold, textX, top - 53, textWidth, Align.left);
                    drawLine(batch, body, "Experience " + rec.exp, textX, top - 70, textWidth, Align.left);
                }
                rect(batch, Color.valueOf("293c46"), PARTY_X, bottom - 3, 76, 3);
                float health = Math.max(0, Math.min(1, rec.hp / (float) Math.max(1, rec.maxhp)));
                rect(batch, health < .25f ? Color.SALMON : Color.valueOf("59b39a"), PARTY_X, bottom - 3, 76 * health, 3);
            } else {
                body.setColor(MUTED);
                drawLine(batch, body, (slot + 1) + "  /  Empty formation slot", PARTY_X + 8, top - 32, PARTY_WIDTH - 16, Align.left);
            }
            if (slot < 5) {
                rect(batch, Color.valueOf("253745"), PARTY_X, bottom - 7, PARTY_WIDTH, 1);
            }
            top -= PARTY_ROW_HEIGHT;
        }
        body.setColor(Color.WHITE);
        emphasis.setColor(Color.WHITE);
    }

    private static String spellPoints(int[] points) {
        StringBuilder text = new StringBuilder();
        for (int point : points) {
            if (text.length() > 0) {
                text.append('/');
            }
            text.append(point);
        }
        return text.toString();
    }

    @Override
    public void dispose() {
        logs.remove();
        summonedScroll.remove();
        skin.dispose();
        panelPixel.dispose();
    }

    public void renderSummonedMonsters(Batch batch, Context ctxt) {
        rect(batch, PANEL, 672, 192, 328, 420);
        rect(batch, BORDER, 672, 611, 328, 1);
        BitmapFont heading = skin.getFont("heading");
        heading.setColor(GOLD);
        drawLine(batch, heading, "Summoned allies", PARTY_X, 596, PARTY_WIDTH, Align.left);
        heading.setColor(Color.WHITE);

        StringBuilder content = new StringBuilder();
        for (MutableMonster monster : ctxt.saveGame.players[0].summonedMonsters) {
            content.append(monster.name()).append('|').append(monster.getLevel()).append('|')
                    .append(monster.getCurrentHitPoints()).append('|').append(monster.getMaxHitPoints()).append('|')
                    .append(monster.status()).append('|').append(monster.getCurrentMageSpellLevel()).append('|')
                    .append(monster.getCurrentPriestSpellLevel()).append('\n');
        }
        String updated = content.toString();
        if (!updated.equals(summonedContent)) {
            summonedContent = updated;
            summonedTable.clearChildren();
            for (MutableMonster monster : ctxt.saveGame.players[0].summonedMonsters) {
                String text = String.format("%s  LVL %d\nHP %d / %d  %s  M %d  P %d",
                        monster.name(), monster.getLevel(), monster.getCurrentHitPoints(), monster.getMaxHitPoints(),
                        monster.isDead() ? "DEAD" : monster.status(),
                        monster.getCurrentMageSpellLevel(), monster.getCurrentPriestSpellLevel());
                Label label = new Label(text, skin, "default-12");
                label.setWrap(true);
                label.setColor(monster.isDead() ? Color.RED : monster.status().color());
                summonedTable.add(label).width(PARTY_WIDTH - 18).pad(4).left().row();
            }
        }
        summonedScroll.setVisible(true);
    }
}
