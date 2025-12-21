
import andius.Andius;
import andius.Constants;
import andius.Context;
import andius.EnhancedWizardryCombatScreen;
import andius.WizardryData;
import static andius.WizardryData.PMO_MONSTERS;
import static andius.WizardryData.WER_MONSTERS;
import andius.objects.ClassType;
import andius.objects.DoGooder;
import andius.objects.Monster;
import andius.objects.MutableCharacter;
import andius.objects.MutableMonster;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
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

            for (DoGooder dg : WizardryData.WER4_CHARS) {
                MutableCharacter mutc = new MutableCharacter(dg);
                if (mutc.getMonsterType() == null) {
                    System.out.println(dg);
                }
            }

            for (Monster m : WizardryData.PMO_MONSTERS) {
                MutableMonster mum = new MutableMonster(m);
                if (mum.getMonsterType() == null) {
                    System.out.println(m);
                }
            }

            Andius.CTX = new Context();
            Context ctx = Andius.CTX;

            //SaveGame sg = SaveGame.read(SAVE_FILENAME);
            //ctx.setSaveGame(sg);
            int lvl = 5;
            ctx.setSaveGame(new SaveGame());
            ctx.saveGame.players = new CharacterRecord[6];
            ctx.saveGame.players[0] = SaveGame.generatePlayer(lvl, ClassType.FIGHTER, "fred");
            ctx.saveGame.players[1] = SaveGame.generatePlayer(lvl, ClassType.FIGHTER, "same");
            ctx.saveGame.players[2] = SaveGame.generatePlayer(lvl, ClassType.NINJA, "jack");
            ctx.saveGame.players[3] = SaveGame.generatePlayer(lvl, ClassType.PRIEST, "joe");
            ctx.saveGame.players[4] = SaveGame.generatePlayer(lvl, ClassType.MAGE, "jane");
            ctx.saveGame.players[5] = SaveGame.generatePlayer(lvl, ClassType.THIEF, "frank");

            //ctx.saveGame.players = new CharacterRecord[1];
            //ctx.saveGame.players[0] = SaveGame.generatePlayer(lvl, ClassType.MAGE, "jane");
//
//                    for (Spells s : Spells.values()) {
//                        ctx.saveGame.players[j].knownSpells.add(s);
//                    }
//
//            ctx.players()[0].inventory.add(WER_ITEMS.get(7));
            List<MutableMonster> mms = new ArrayList<>();
            mms.add(new MutableMonster(WER_MONSTERS.get(108)));
            mms.add(new MutableMonster(WER_MONSTERS.get(108)));
            mms.add(new MutableMonster(WER_MONSTERS.get(108)));
            mms.add(new MutableMonster(WER_MONSTERS.get(108)));
            mms.add(new MutableMonster(WER_MONSTERS.get(108)));
            mms.add(new MutableMonster(WER_MONSTERS.get(108)));
            mms.add(new MutableMonster(WER_MONSTERS.get(108)));
            mms.add(new MutableMonster(WER_MONSTERS.get(108)));
            mms.add(new MutableMonster(WER_MONSTERS.get(108)));

            //setScreen(new Wiz4CombatScreen(ctx.saveGame.players[0], mms, WER4_CHARS.get(448), null, null));
            //setScreen(new Wiz4CombatScreen(ctx.saveGame.players[0], mms, WER4_CHARS.get(454), null, null));
            //setScreen(new WizardryCombatScreen(ctx, Constants.Map.WIZARDRY1, PMO_MONSTERS.get(8).getName(), PMO_MONSTERS.get(8), 1, true, null, null));
            //setScreen(new Wiz4RewardScreen(ctx.saveGame.players[0], WER4_CHARS.get(0)));
            //setScreen(new EquipmentScreen(ctx, Constants.Map.WORLD));
            //setScreen(new CampScreen(ctx, Constants.Map.WORLD));
            //setScreen(new Wiz4RewardScreen(ctx.saveGame.players[0], WER4_CHARS.get(180)));
            //setScreen(new RewardScreen(ctx, Constants.Map.WIZARDRY1, 18));
            //setScreen(new VendorScreen(ctx, Constants.Map.CAVE, DQ_ITEMS, "paul"));
            //setScreen(new SummoningCircleScreen(ctx.saveGame.players[0], SummoningCircle.CIRCLE1));
            //setScreen(new ManageScreen(null, Andius.skin, new SaveGame()));
            setScreen(new EnhancedWizardryCombatScreen(ctx, Constants.Map.WIZARDRY1, PMO_MONSTERS.get(11).getName(), PMO_MONSTERS.get(11), 1, true, null, null));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
