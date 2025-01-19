
import andius.Andius;
import static andius.Constants.SAVE_FILENAME;
import andius.Context;
import andius.Wiz4CombatScreen;
import andius.Wiz4RewardScreen;
import static andius.WizardryData.PMO_MONSTERS;
import static andius.WizardryData.WER4_CHARS;
import andius.objects.MutableMonster;
import andius.objects.SaveGame;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import java.util.ArrayList;
import java.util.List;

public class Wiz4CombatTestMain extends Game {

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Wiz4CombatTestMain";
        cfg.width = 1024;
        cfg.height = 768;
        new LwjglApplication(new Wiz4CombatTestMain(), cfg);
    }

    @Override
    public void create() {

        try {

            Andius a = new Andius();
            a.create();

            Context ctx = new Context();
            SaveGame sg = SaveGame.read(SAVE_FILENAME);
            ctx.setSaveGame(sg);

            for (int j = 0; j < 6; j++) {
//                ctx.saveGame.players[j].magePoints = new int[]{5, 5, 5, 5, 5, 5, 5};
//                ctx.saveGame.players[j].clericPoints = new int[]{5, 5, 5, 5, 5, 5, 5};
//                for (Spells s : Spells.values()) {
//                    ctx.saveGame.players[j].knownSpells.add(s);
//                }
            }

            List<MutableMonster> mms = new ArrayList<>();
            mms.add(new MutableMonster(PMO_MONSTERS.get(3)));
            mms.add(new MutableMonster(PMO_MONSTERS.get(3)));
            mms.add(new MutableMonster(PMO_MONSTERS.get(3)));
            mms.add(new MutableMonster(PMO_MONSTERS.get(4)));
            mms.add(new MutableMonster(PMO_MONSTERS.get(4)));
            mms.add(new MutableMonster(PMO_MONSTERS.get(4)));
            mms.add(new MutableMonster(PMO_MONSTERS.get(6)));
            mms.add(new MutableMonster(PMO_MONSTERS.get(6)));
            mms.add(new MutableMonster(PMO_MONSTERS.get(6)));

            setScreen(new Wiz4CombatScreen(ctx.saveGame.players[0], mms, WER4_CHARS.get(372)));
            //setScreen(new Wiz4RewardScreen(ctx.saveGame.players[0], WER4_CHARS.get(0)));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
