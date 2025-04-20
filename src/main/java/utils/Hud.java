package utils;

import andius.Andius;
import andius.Context;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.Color;
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
}
