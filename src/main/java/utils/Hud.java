package utils;

import andius.Andius;
import andius.Context;
import andius.objects.MutableMonster;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Hud {

    private final LogScrollPane logs;
    static final int LOG_AREA_WIDTH = 270;
    private final List<Integer> magicPoints = new ArrayList<>();

    public Hud() {
        logs = new LogScrollPane(Andius.skin, new Table(), LOG_AREA_WIDTH);
        logs.setBounds(728, 30, LOG_AREA_WIDTH, 320);
    }

    public void addActor(Stage stage) {
        logs.remove();
        stage.addActor(logs);
        logs.setZIndex(0);
    }

    public void log(String s) {
        logs.add(s, Color.WHITE);
    }

    public void log(String s, Color c) {
        logs.add(s, c);
    }

    public void render(Batch batch, Context ctxt) {

        int y = Andius.SCREEN_HEIGHT - 56;
        int py = Andius.SCREEN_HEIGHT - 103;

        for (CharacterRecord rec : ctxt.saveGame.players) {

            Andius.font12.setColor(rec.isDead() ? Color.RED : rec.status.color());

            if (rec.hp > 0 && rec.hp < 2) {
                Andius.font12.setColor(Color.SALMON);
            }

            batch.draw(Andius.faceTiles[rec.portaitIndex], 727, py);

            String d = String.format("%s  LVL %d  %s  %s", rec.name.toUpperCase(), rec.level, rec.race.toString(), rec.classType.toString());
            Andius.font12.draw(batch, d, 790, y);

            d = String.format("HP: %d /%d AC: %d ST: %s", rec.hp, rec.maxhp, rec.calculateAC(), rec.status.toString());
            Andius.font12.draw(batch, d, 790, y - 12);

            d = String.format("GOLD: %d EXP: %d", rec.gold, rec.exp);
            Andius.font12.draw(batch, d, 790, y - 24);

            int[] ms = rec.magePoints;
            int[] cs = rec.clericPoints;
            magicPoints.clear();
            Arrays.stream(ms).forEach(magicPoints::add);
            Arrays.stream(cs).forEach(magicPoints::add);
            int sum = magicPoints.stream().mapToInt(Integer::intValue).sum();
            if (sum > 0) {
                d = String.format("M: %d %d %d %d %d %d %d P: %d %d %d %d %d %d %d",
                        ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6], cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]);
                Color tmp = Andius.font12.getColor();
                Andius.font12.setColor(Color.SKY);
                Andius.font12.draw(batch, d, 790, y - 36);
                Andius.font12.setColor(tmp);
            }

            y -= 60;
            py -= 60;

        }

    }

    public void renderSummonedMonsters(Batch batch, Context ctxt) {

        if (smb == null) {
            smb = summondMonstersBackground();
        }

        batch.draw(smb, 722, Andius.SCREEN_HEIGHT - 110 - 298);

        Andius.font12.setColor(Color.WHITE);

        float y = Andius.SCREEN_HEIGHT - 102;
        int count = 0;
        for (MutableMonster mm : ctxt.saveGame.players[0].summonedMonsters) {
            Andius.font12.setColor(mm.isDead() ? Color.RED : mm.status().color());

            String str = String.format("%s %d %d/%d %s (%d, %d)",
                    mm.name().toUpperCase(), mm.getLevel(),
                    mm.getCurrentHitPoints(), mm.getMaxHitPoints(),
                    mm.getCurrentHitPoints() <= 0 ? "DEAD" : mm.status(),
                    mm.getCurrentMageSpellLevel(), mm.getCurrentPriestSpellLevel());
            Andius.font12.draw(batch, str, 730, y -= 16);

            count++;
            if (count > 17) {
                break;
            }
        }

        Andius.font12.setColor(Color.WHITE);
    }

    private Texture smb = null;

    private Texture summondMonstersBackground() {

        Color DARKEST = new Color(0x2e2e2eff);
        Color DARK = new Color(0x575757ff);
        Color LIGHT = new Color(0x7a7a7aff);
        Color LIGHTEST = new Color(0xabababff);

        Pixmap pix = new Pixmap(280, 298, Pixmap.Format.RGBA8888);

        int ix = 5;
        int iy = 5;
        int iw = 270;
        int ih = 288;

        pix.setColor(Color.BLACK);
        pix.fillRectangle(ix - 5, iy - 5, iw + 10, ih + 10);

        pix.setColor(DARK);
        pix.fillRectangle(ix - 4, iy - 4, iw + 8, ih + 8);

        pix.setColor(LIGHT);
        pix.fillRectangle(ix - 3, iy - 3, iw + 6, ih + 6);

        pix.setColor(LIGHTEST);
        pix.fillRectangle(ix - 2, iy - 2, iw + 4, ih + 4);

        pix.setColor(Color.BLACK);
        pix.fillRectangle(ix - 1, iy - 1, iw + 2, ih + 2);

        pix.setColor(DARKEST);
        pix.fillRectangle(ix, iy, iw, ih);

        Texture t = new Texture(pix);
        pix.dispose();
        return t;
    }
}
