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
    static final int LOG_X = 738;

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

        batch.draw(Andius.faceTiles[pm.getCharRec().portaitIndex], 738, Andius.SCREEN_HEIGHT - 119);

        CharacterRecord r = pm.getCharRec();

        String d = String.format("%s  LVL %d  %s  %s", r.name.toUpperCase(), pm.getCharRec().level, r.race.toString(), r.classType.toString());
        Andius.smallFont.draw(batch, d, 805, Andius.SCREEN_HEIGHT - 51);

        d = String.format("%d", pm.getCharRec().health);
        Andius.smallFont.draw(batch, d, 805, Andius.SCREEN_HEIGHT - 79);

        //d = String.format("%d /%d", pm.getCharRec().mana, pm.getCharRec().getMaxMana());
        //Andius.smallFont.draw(batch, d, 805, Andius.SCREEN_HEIGHT - 107);

        d = String.format("%d", pm.getCharRec().gold);
        Andius.smallFont.draw(batch, d, 805, Andius.SCREEN_HEIGHT - 135);

        d = String.format("%d", pm.getCharRec().exp);
        Andius.smallFont.draw(batch, d, 908, Andius.SCREEN_HEIGHT - 135);

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
