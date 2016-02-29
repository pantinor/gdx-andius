package utils;

import andius.Andius;
import andius.Constants.Status;
import andius.objects.Player;
import andius.objects.SaveGame.CharacterRecord;
import java.util.List;
import org.apache.commons.collections.iterators.ReverseListIterator;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;

public class LogDisplay {

    final List<String> logs = new FixedSizeArrayList<>(20);

    static final int LOG_AREA_WIDTH = 265;
    static final int LOG_AREA_TOP = 396;
    static final int LOG_X = 726;

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

    public void render(Batch batch, Player pm) {

        float y = Andius.SCREEN_HEIGHT - 48 - 75;

        Andius.smallFont.setColor(Color.WHITE);

        if (pm.getCharRec().status == Status.POISONED) {
            Andius.smallFont.setColor(Color.GREEN);
        }
        if (pm.getCharRec().status == Status.ASH) {
            Andius.smallFont.setColor(Color.FIREBRICK);
        }
        if (pm.getCharRec().status == Status.DEAD) {
            Andius.smallFont.setColor(Color.DARK_GRAY);
        }
        if (pm.getCharRec().health > 0 && pm.getCharRec().health < 2) {
            Andius.smallFont.setColor(Color.RED);
        }

        batch.draw(Andius.faceTiles[pm.getCharRec().portaitIndex], LOG_X + 3, y + 5);

        CharacterRecord r = pm.getCharRec();

        String d = String.format("%s  %s  %s", r.name.toUpperCase(), r.race.toString(), r.profession.toString());
        Andius.smallFont.draw(batch, d, LOG_X + 64, y + 65);

        d = String.format("HLTH: %d %s LVL: %d EXP: %d", pm.getCharRec().health, pm.getCharRec().status.toString().charAt(0), pm.getCharRec().getLevel(), pm.getCharRec().exp);
        Andius.smallFont.draw(batch, d, LOG_X + 64, y + 45);

        d = String.format("GOLD: %d MANA: %d/%d", pm.getCharRec().gold, pm.getCharRec().mana, pm.getCharRec().getMaxMana());
        Andius.smallFont.draw(batch, d, LOG_X + 64, y + 25);

        Andius.smallFont.setColor(Color.WHITE);
        y = 44;

        synchronized (logs) {
            ReverseListIterator iter = new ReverseListIterator(logs);
            while (iter.hasNext()) {
                String next = (String) iter.next();
                GlyphLayout layout = new GlyphLayout(Andius.font, next, Color.WHITE, LOG_AREA_WIDTH - 8, Align.left, true);
                y += layout.height + 10;
                if (y > LOG_AREA_TOP) {
                    break;
                }
                Andius.font.draw(batch, layout, LOG_X + 8, y);
            }
        }
    }
}
