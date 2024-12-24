package utils;

import andius.Andius;
import andius.Context;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class Hud {

    private final LogScrollPane logs;
    static final int LOG_AREA_WIDTH = 275;
    
    public Hud() {
        logs = new LogScrollPane(Andius.skin, new Table(), LOG_AREA_WIDTH);
        logs.setBounds(732, 30, LOG_AREA_WIDTH, 320);
    }

    public void addActor(Stage stage) {
        stage.addActor(logs);
    }

    public void log(String s) {
        logs.add(s);
    }

    public void render(Batch batch, Context ctxt) {

        int y = Andius.SCREEN_HEIGHT - 50;
        int py = Andius.SCREEN_HEIGHT - 103;

        for (CharacterRecord rec : ctxt.saveGame.players) {

            Andius.smallFont.setColor(rec.isDead() ? Color.RED : rec.status.color());

            if (rec.hp > 0 && rec.hp < 2) {
                Andius.smallFont.setColor(Color.SALMON);
            }

            batch.draw(Andius.faceTiles[rec.portaitIndex], 727, py);

            String d = String.format("%s  LVL %d  %s  %s", rec.name.toUpperCase(), rec.level, rec.race.toString(), rec.classType.toString());
            Andius.smallFont.draw(batch, d, 790, y);

            d = String.format("HP: %d /%d AC: %d ST: %s", rec.hp, rec.maxhp, rec.calculateAC(), rec.status.toString());
            Andius.smallFont.draw(batch, d, 790, y - 11);

            d = String.format("GOLD: %d EXP: %d", rec.gold, rec.exp);
            Andius.smallFont.draw(batch, d, 790, y - 22);

            int[] ms = rec.magePoints;
            int[] cs = rec.clericPoints;
            d = String.format("M: %d %d %d %d %d %d %d P: %d %d %d %d %d %d %d",
                    ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6], cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]);
            Andius.smallFont.draw(batch, d, 790, y - 33);

            y -= 60;
            py -= 60;

        }

    }
}
