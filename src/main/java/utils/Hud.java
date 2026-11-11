package utils;

import andius.Andius;
import andius.Context;
import andius.objects.MutableMonster;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import java.util.Arrays;

public class Hud implements Disposable {

    private final LogScrollPane logs;
    static final int LOG_AREA_WIDTH = 308;
    static final int LOG_AREA_HEIGHT = 224;
    private static final int PARTY_X = 702, PARTY_WIDTH = 308, PARTY_ROW_HEIGHT = 84;
    private final Skin skin = SkinUtils.createAdventureSkin(Andius.skin);
    private final BitmapFont body = skin.getFont("body");
    private final BitmapFont emphasis = skin.getFont("emphasis");
    private final Texture panelPixel = Utils.fillRectangle(1, 1, Color.WHITE, 1);
    private static final Color PANEL = Color.valueOf("20252b");
    private static final Color BORDER = Color.valueOf("48515b");
    private final Table summonedTable = new Table();
    private final AutoFocusScrollPane summonedScroll = new AutoFocusScrollPane(summonedTable, skin);
    private String summonedContent = "";

    public Hud() {
        Table table = new Table(skin);
        table.setBackground("dimmer");
        logs = new LogScrollPane(skin, table, LOG_AREA_WIDTH, "default-12");
        float y = 10;
        logs.setBounds(702, y, LOG_AREA_WIDTH, LOG_AREA_HEIGHT);
        summonedTable.top().left();
        summonedScroll.setBounds(PARTY_X, 246, PARTY_WIDTH, 392);
        summonedScroll.setScrollingDisabled(true, false);
        summonedScroll.setVisible(false);
    }

    public Skin getSkin() {
        return skin;
    }

    public void renderLocation(Batch batch, String title, int east, int north) {
        renderHeader(batch, title, "East / West " + east + "    North / South " + north);
    }

    public void renderDungeonLocation(Batch batch, String title, int level, int east, int north, String direction) {
        renderHeader(batch, title, "Level " + level + "    [" + east + ", " + north + "]    " + direction);
    }

    private void renderHeader(Batch batch, String title, String detail) {
        BitmapFont heading = skin.getFont("heading");
        heading.setColor(Color.valueOf("dbbb7c"));
        drawLine(batch, heading, title, 104, Andius.SCREEN_HEIGHT - 24, 544, Align.center);
        body.setColor(Color.WHITE);
        drawLine(batch, body, detail,
                104, Andius.SCREEN_HEIGHT - 38, 544, Align.center);
        heading.setColor(Color.WHITE);
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
        logs.add(s.toUpperCase(), Color.WHITE);
    }

    public void log(String s, Color c) {
        logs.add(s.toUpperCase(), c);
    }

    public void render(Batch batch, Context ctxt) {
        summonedScroll.setVisible(false);
        Color batchColor = new Color(batch.getColor());
        batch.setColor(PANEL);
        batch.draw(panelPixel, 697, 5, 318, 752);
        float y = Andius.SCREEN_HEIGHT - 16;
        for (CharacterRecord rec : ctxt.saveGame.players) {
            batch.setColor(BORDER);
            batch.draw(panelPixel, PARTY_X - 2, y - 80, PARTY_WIDTH + 4, PARTY_ROW_HEIGHT - 2);
            batch.setColor(PANEL);
            batch.draw(panelPixel, PARTY_X - 1, y - 79, PARTY_WIDTH + 2, PARTY_ROW_HEIGHT - 4);
            batch.setColor(batchColor);
            Color color = rec.isDead() ? Color.RED : rec.status.color();
            if (rec.hp > 0 && rec.hp < 2) {
                color = Color.SALMON;
            }
            body.setColor(color);
            emphasis.setColor(color);
            batch.draw(Andius.faceTiles[rec.portaitIndex], PARTY_X, y - 64);
            float textX = PARTY_X + 70;
            float textWidth = PARTY_WIDTH - 70;
            drawLine(batch, emphasis, rec.name.toUpperCase(), textX, y, textWidth, Align.left);
            drawLine(batch, body, String.format("LVL %d  %s  %s", rec.level, rec.race, rec.classType),
                    textX, y - 17, textWidth, Align.left);
            drawLine(batch, body, String.format("HP %d / %d   AC %d", rec.hp, rec.maxhp, rec.calculateAC()),
                    textX, y - 34, textWidth, Align.left);
            drawLine(batch, body, String.format("GOLD %d  EXP %d  %s", rec.gold, rec.exp, rec.status),
                    textX, y - 51, textWidth, Align.left);
            int[] ms = rec.magePoints;
            int[] cs = rec.clericPoints;
            boolean hasMagic = Arrays.stream(ms).sum() + Arrays.stream(cs).sum() > 0;
            if (hasMagic) {
                body.setColor(Color.SKY);
                String points = "M " + spellPoints(ms) + "   P " + spellPoints(cs);
                drawLine(batch, body, points, PARTY_X, y - 68, PARTY_WIDTH, Align.left);
            }
            y -= PARTY_ROW_HEIGHT;
        }
        body.setColor(Color.WHITE);
        emphasis.setColor(Color.WHITE);
        batch.setColor(batchColor);
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
        Color previous = new Color(batch.getColor());
        batch.setColor(BORDER);
        batch.draw(panelPixel, 697, 240, 318, 430);
        batch.setColor(PANEL);
        batch.draw(panelPixel, 698, 241, 316, 428);
        batch.setColor(previous);
        BitmapFont heading = skin.getFont("heading");
        heading.setColor(Color.valueOf("dbbb7c"));
        drawLine(batch, heading, "SUMMONED ALLIES", PARTY_X + 6, 660, PARTY_WIDTH - 12, Align.left);
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
