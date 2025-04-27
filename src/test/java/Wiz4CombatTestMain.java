
import andius.Andius;
import static andius.Constants.LEVEL_PROGRESSION_TABLE;
import static andius.Constants.SAVE_FILENAME;
import andius.Context;
import andius.Wiz4CombatScreen;
import static andius.WizardryData.PMO_ITEMS_MAP;
import static andius.WizardryData.PMO_MONSTERS;
import static andius.WizardryData.WER4_CHARS;
import static andius.WizardryData.WER_MONSTERS;
import andius.objects.ClassType;
import static andius.objects.ClassType.BISHOP;
import static andius.objects.ClassType.FIGHTER;
import static andius.objects.ClassType.LORD;
import static andius.objects.ClassType.MAGE;
import static andius.objects.ClassType.NINJA;
import static andius.objects.ClassType.PRIEST;
import static andius.objects.ClassType.SAMURAI;
import static andius.objects.ClassType.THIEF;
import andius.objects.MutableMonster;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import java.util.ArrayList;
import java.util.List;
import utils.Utils;

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

            Andius.CTX = new Context();
            Context ctx = Andius.CTX;

            SaveGame sg = SaveGame.read(SAVE_FILENAME);
            ctx.setSaveGame(sg);

//            int lvl = 2;
//            ctx.setSaveGame(new SaveGame());
//            ctx.saveGame.players = new CharacterRecord[6];
//            ctx.saveGame.players[0] = generatePlayer(lvl + 1, ClassType.FIGHTER, "fred");
//            ctx.saveGame.players[1] = generatePlayer(lvl + 1, ClassType.FIGHTER, "same");
//            ctx.saveGame.players[2] = generatePlayer(lvl + 1, ClassType.FIGHTER, "jack");
//            ctx.saveGame.players[3] = generatePlayer(lvl + 1, ClassType.PRIEST, "joe");
//            ctx.saveGame.players[4] = generatePlayer(lvl + 1, ClassType.MAGE, "jane");
//            ctx.saveGame.players[5] = generatePlayer(lvl + 1, ClassType.THIEF, "frank");
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

            //setScreen(new Wiz4CombatScreen(ctx.saveGame.players[0], ctx.saveGame.players[0].summonedMonsters, WER4_CHARS.get(448), null, null));
            setScreen(new Wiz4CombatScreen(ctx.saveGame.players[0], mms, WER4_CHARS.get(454), null, null));

            //setScreen(new WizardryCombatScreen(ctx, Constants.Map.WIZARDRY1, PMO_MONSTERS.get(77), 1, true, null, null));
            //setScreen(new Wiz4RewardScreen(ctx.saveGame.players[0], WER4_CHARS.get(0)));
            //setScreen(new EquipmentScreen(ctx, Constants.Map.WORLD));
            //setScreen(new CampScreen(ctx, Constants.Map.WORLD));
            //setScreen(new Wiz4RewardScreen(ctx.saveGame.players[0], WER4_CHARS.get(180)));
            //setScreen(new RewardScreen(ctx, Constants.Map.WIZARDRY1, 18));
            //setScreen(new VendorScreen(ctx, Role.MERCHANT2, Constants.Map.CAVE, "paul"));
            //setScreen(new SummoningCircleScreen(ctx.saveGame.players[0], SummoningCircle.CIRCLE1));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private CharacterRecord generatePlayer(int lvl, ClassType ctype, String name) {

        CharacterRecord p = new CharacterRecord();

        p.name = name;
        p.classType = ctype;
        p.level = 1;
        p.hp = 12;
        p.gold = 3000;

        int tmp = 0;
        if (lvl <= 12) {
            tmp = LEVEL_PROGRESSION_TABLE[lvl][ctype.ordinal()];
        } else {
            for (int i = 13; i <= lvl; i++) {
                tmp += LEVEL_PROGRESSION_TABLE[0][ctype.ordinal()];
            }
        }

        p.exp = tmp - 5;

        p.str = Utils.getRandomBetween(10, 18);
        p.intell = Utils.getRandomBetween(10, 18);
        p.piety = Utils.getRandomBetween(10, 18);
        p.vitality = Utils.getRandomBetween(10, 18);
        p.agility = Utils.getRandomBetween(10, 18);
        p.luck = Utils.getRandomBetween(10, 18);

        int expnextlvl = p.checkAndSetLevel();
        while (expnextlvl >= 0) {
            p.maxhp += p.getMoreHP();

            p.str = SaveGame.gainOrLose(p.str);
            p.intell = SaveGame.gainOrLose(p.intell);
            p.piety = SaveGame.gainOrLose(p.piety);
            p.vitality = SaveGame.gainOrLose(p.vitality);
            p.agility = SaveGame.gainOrLose(p.agility);
            p.luck = SaveGame.gainOrLose(p.luck);

            SaveGame.setSpellPoints(p);

            SaveGame.tryLearn(p);

            expnextlvl = p.checkAndSetLevel();
        }

        SaveGame.setSpellPoints(p);

        p.hp = p.maxhp;

        switch (ctype) {
            case SAMURAI:
            case LORD:
            case FIGHTER:
                p.armor = PMO_ITEMS_MAP.get("PLATE MAIL");
                p.weapon = PMO_ITEMS_MAP.get("LONG SWORD");
                p.helm = PMO_ITEMS_MAP.get("HELM");
                //p.glove = PMO_ITEMS_MAP.get("SILVER GLOVES");
                break;
            case MAGE:
                p.armor = PMO_ITEMS_MAP.get("ROBES");
                p.weapon = PMO_ITEMS_MAP.get("STAFF");
                break;
            case PRIEST:
                p.armor = PMO_ITEMS_MAP.get("BREAST PLATE");
                p.weapon = PMO_ITEMS_MAP.get("ANOINTED FLAIL");
                break;
            case THIEF:
                p.armor = PMO_ITEMS_MAP.get("LEATHER + 1");
                p.weapon = PMO_ITEMS_MAP.get("SHORT SWORD");
                break;
            case BISHOP:
                p.armor = PMO_ITEMS_MAP.get("ROBES");
                p.weapon = PMO_ITEMS_MAP.get("STAFF");
                break;
            case NINJA:
                p.armor = PMO_ITEMS_MAP.get("ROBES");
                p.weapon = PMO_ITEMS_MAP.get("STAFF");
                break;
        }

        return p;
    }

}
