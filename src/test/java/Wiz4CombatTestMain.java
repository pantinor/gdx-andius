
import andius.Andius;
import static andius.Constants.SAVE_FILENAME;
import andius.Context;
import andius.Wiz4CombatScreen;
import static andius.WizardryData.WER4_CHARS;
import andius.objects.SaveGame;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

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
            
            //ctx.saveGame.players[3].hp = 0;

//            ctx.setSaveGame(new SaveGame());
//            ctx.saveGame.players = new CharacterRecord[6];
//            if (true) {
//                for (int j = 0; j < ctx.saveGame.players.length; j++) {
//                    ctx.saveGame.players[j] = new CharacterRecord();
//                    ctx.saveGame.players[j].name = "player" + j;
//                    ctx.saveGame.players[j].classType = ClassType.values()[Utils.getRandomBetween(0, 7)];
//                    int lvl = Utils.getRandomBetween(1, 7);
//                    ctx.saveGame.players[j].level = lvl;
//                    ctx.saveGame.players[j].hp = 12 * lvl;
//                    ctx.saveGame.players[j].maxhp = 12 * lvl;
//                    ctx.saveGame.players[j].gold = 3000;
//
//                    ctx.saveGame.players[j].str = Utils.getRandomBetween(10, 17);
//                    ctx.saveGame.players[j].intell = Utils.getRandomBetween(10, 17);
//                    ctx.saveGame.players[j].piety = Utils.getRandomBetween(10, 17);
//                    ctx.saveGame.players[j].vitality = Utils.getRandomBetween(10, 17);
//                    ctx.saveGame.players[j].agility = Utils.getRandomBetween(10, 17);
//                    ctx.saveGame.players[j].luck = Utils.getRandomBetween(10, 17);
//
//                    ctx.saveGame.players[j].armor = PMO_ITEMS_MAP.get("ROBES");
//                    ctx.saveGame.players[j].weapon = PMO_ITEMS_MAP.get("STAFF");
//                    //ctx.saveGame.players[j].weapon = PMO_ITEMS_MAP.get("MAGIC BOW");
//                    //ctx.saveGame.players[j].helm = PMO_ITEMS_MAP.get("HELM");
//                    //ctx.saveGame.players[j].shield = PMO_ITEMS_MAP.get("LARGE SHIELD");
//                    //ctx.saveGame.players[j].glove = PMO_ITEMS_MAP.get("SILVER GLOVES");
//                    //ctx.saveGame.players[j].item1 = PMO_ITEMS_MAP.get("ROD OF FLAME");
//                    //ctx.saveGame.players[j].item2 = PMO_ITEMS_MAP.get("WERDNAS AMULET");
//
//                    ctx.saveGame.players[j].magePoints = new int[]{5, 5, 5, 5, 5, 5, 5};
//                    ctx.saveGame.players[j].clericPoints = new int[]{5, 5, 5, 5, 5, 5, 5};
//
//                    for (Spells s : Spells.values()) {
//                        ctx.saveGame.players[j].knownSpells.add(s);
//                    }
//
//                }
//            }
            //ctx.players()[0].inventory.add(WER_ITEMS.get(7));
//            List<MutableMonster> mms = new ArrayList<>();
//            mms.add(new MutableMonster(PMO_MONSTERS.get(3)));
//            mms.add(new MutableMonster(PMO_MONSTERS.get(3)));
//            mms.add(new MutableMonster(PMO_MONSTERS.get(3)));
//            mms.add(new MutableMonster(PMO_MONSTERS.get(4)));
//            mms.add(new MutableMonster(PMO_MONSTERS.get(4)));
//            mms.add(new MutableMonster(PMO_MONSTERS.get(4)));
//            mms.add(new MutableMonster(PMO_MONSTERS.get(6)));
//            mms.add(new MutableMonster(PMO_MONSTERS.get(6)));
//            mms.add(new MutableMonster(PMO_MONSTERS.get(6)));
//            TextureAtlas iconAtlas = new TextureAtlas(Gdx.files.classpath("assets/json/wiz4ibm.atlas"));
//            Map<Integer, String> icons = new HashMap<>();
//            for (DoGooder dg : WER4_CHARS) {
//                icons.put(dg.id, dg.iconID);
//            }
            setScreen(new Wiz4CombatScreen(ctx.saveGame.players[0], ctx.saveGame.players[0].summonedMonsters, WER4_CHARS.get(72), null, null));
            //setScreen(new WizardryCombatScreen(ctx, Constants.Map.WIZARDRY1, PMO_MONSTERS.get(9), 1, true, null, null));
            //setScreen(new Wiz4RewardScreen(ctx.saveGame.players[0], WER4_CHARS.get(0)));
            //setScreen(new EquipmentScreen(ctx, Constants.Map.WORLD));
            //setScreen(new CampScreen(ctx, Constants.Map.WORLD));
            //setScreen(new Wiz4RewardScreen(ctx.saveGame.players[0], WER4_CHARS.get(180)));
            //setScreen(new RewardScreen(ctx, Constants.Map.WIZARDRY1, 10));
            //setScreen(new VendorScreen(ctx, Role.MERCHANT2, Constants.Map.CAVE, "paul"));
            //setScreen(new SummoningCircleScreen(ctx.saveGame.players[0], SummoningCircle.CIRCLE1));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
