package utils;

import andius.Andius;
import andius.Constants.Status;
import andius.Context;
import andius.objects.SaveGame.CharacterRecord;
import java.util.List;
import org.apache.commons.collections.iterators.ReverseListIterator;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;

public class Hud {

    final List<String> logs = new FixedSizeArrayList<>(25);

    static final int LOG_AREA_WIDTH = 270;
    static final int LOG_AREA_TOP = 355;
    static final int LOG_X = 735;
    private GlyphLayout layout = new GlyphLayout(Andius.hudLogFont, "", Color.WHITE, LOG_AREA_WIDTH - 5, Align.left, true);

    public void append(String s) {
        synchronized (logs) {
            if (logs.isEmpty()) {
                logs.add("");
            }
            String l = logs.get(logs.size() - 1);
            l = l + s;
            logs.remove(logs.size() - 1);
            logs.add(l);
        }
    }

    public void logDeleteLastChar() {
        synchronized (logs) {
            if (logs.isEmpty()) {
                return;
            }
            String l = logs.get(logs.size() - 1);
            l = l.substring(0, l.length() - 1);
            logs.remove(logs.size() - 1);
            logs.add(l);
        }
    }

    public void add(String s) {
        synchronized (logs) {
            logs.add(s);
        }
    }

    public void render(Batch batch, Context ctxt) {

        int y = Andius.SCREEN_HEIGHT - 50;
        int py = Andius.SCREEN_HEIGHT - 103;

        for (CharacterRecord rec : ctxt.saveGame.players) {

            Andius.smallFont.setColor(Color.WHITE);

            if (rec.status == Status.POISONED) {
                Andius.smallFont.setColor(Color.GREEN);
            }
            if (rec.status == Status.AFRAID) {
                Andius.smallFont.setColor(Color.ORANGE);
            }
            if (rec.status == Status.ASLEEP) {
                Andius.smallFont.setColor(Color.PINK);
            }
            if (rec.status == Status.ASHES) {
                Andius.smallFont.setColor(Color.LIGHT_GRAY);
            }
            if (rec.status == Status.PARALYZED) {
                Andius.smallFont.setColor(Color.YELLOW);
            }
            if (rec.status == Status.STONED) {
                Andius.smallFont.setColor(Color.LIGHT_GRAY);
            }
            if (rec.status == Status.DEAD) {
                Andius.smallFont.setColor(Color.SCARLET);
            }
            if (rec.hp > 0 && rec.hp < 2) {
                Andius.smallFont.setColor(Color.RED);
            }

            batch.draw(Andius.faceTiles[rec.portaitIndex], 727, py);

            CharacterRecord r = rec;

            String d = String.format("%s  LVL %d  %s  %s", r.name.toUpperCase(), rec.level, r.race.toString(), r.classType.toString());
            Andius.smallFont.draw(batch, d, 790, y);

            d = String.format("HP: %d /%d AC: %d ST: %s", rec.hp, rec.maxhp, rec.calculateAC(), rec.status);
            Andius.smallFont.draw(batch, d, 790, y - 11);

            d = String.format("GOLD: %d EXP: %d", rec.gold, rec.exp);
            Andius.smallFont.draw(batch, d, 790, y - 22);

            int[] ms = rec.magePoints;
            int[] cs = rec.clericPoints;
            d = String.format("M: %d/%d/%d/%d/%d/%d/%d P: %d/%d/%d/%d/%d/%d/%d",
                    ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6], cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]);
            Andius.smallFont.draw(batch, d, 790, y - 33);

            y -= 60;
            py -= 60;

        }

        Andius.hudLogFont.setColor(Color.WHITE);
        y = 28;

        synchronized (logs) {
            ReverseListIterator iter = new ReverseListIterator(logs);
            while (iter.hasNext()) {
                String next = (String) iter.next();
                layout.setText(Andius.hudLogFont, next);
                y += layout.height + 4;
                if (y > LOG_AREA_TOP) {
                    break;
                }
                Andius.hudLogFont.draw(batch, layout, LOG_X, y);
            }
        }
    }
}
