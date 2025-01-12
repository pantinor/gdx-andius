
import andius.Andius;
import andius.CombatScreen;
import andius.Constants;
import static andius.Constants.CLASSPTH_RSLVR;
import andius.Constants.Role;
import static andius.Constants.SAVE_FILENAME;
import andius.Context;
import andius.TibianSprite;
import static andius.WizardryData.PMO_MONSTERS;
import andius.objects.Actor;
import andius.objects.Monster;
import andius.objects.MutableMonster;
import andius.objects.SaveGame;
import andius.objects.Spells;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class CombatTestMain extends Game {

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "CombatTestMain";
        cfg.width = 1024;
        cfg.height = 768;
        new LwjglApplication(new CombatTestMain(), cfg);
    }

    @Override
    public void create() {

        try {

            Andius a = new Andius();
            a.create();

//            for (int level = 0; level < PMO_LEVELS.length; level++) {
//                System.out.println("\nLevel " + (level + 1));
//                for (int x = 0; x < DUNGEON_DIM; x++) {
//                    for (int y = 0; y < DUNGEON_DIM; y++) {
//                        WizardryData.MazeCell cell = PMO_LEVELS[level].cells[x][y];
//                        if (cell.monsterID >= 0) {
//                            System.out.println("MID: " + PMO_MONSTERS.get(cell.monsterID).name);
//                        }
//                        if (cell.itemObtained > 0) {
//                            System.out.println("OBTAINED: " + PMO_ITEMS.get(cell.itemObtained).name);
//                        }
//                        if (cell.itemRequired > 0) {
//                            System.out.println("REQUIRED: " + PMO_ITEMS.get(cell.itemRequired).name);
//                        }
//                    }
//                }
//            }
//
//            for (Reward r : PMO_REWARDS) {
//                System.out.println(r);
//            }

            Context ctx = new Context();
            SaveGame sg = SaveGame.read(SAVE_FILENAME);
            ctx.setSaveGame(sg);

            for (int j = 0; j < 6; j++) {
//                ctx.saveGame.players[j].level = 1;
//                ctx.saveGame.players[j].hp = 12;
//                ctx.saveGame.players[j].maxhp = 12;
//                ctx.saveGame.players[j].gold = 3000;
//
//                ctx.saveGame.players[j].exp = 12000;
                ctx.saveGame.players[j].magePoints = new int[]{5, 5, 5, 5, 5, 5, 5};
                ctx.saveGame.players[j].clericPoints = new int[]{5, 5, 5, 5, 5, 5, 5};
                //ctx.saveGame.players[j].magePoints = new int[]{1, 1, 1, 1, 1, 1, 1};
                //ctx.saveGame.players[j].clericPoints = new int[]{1, 1, 1, 1, 1, 1, 1};
                for (Spells s : Spells.values()) {
                    ctx.saveGame.players[j].knownSpells.add(s);
                }
//
//                ctx.saveGame.players[j].str = 8;
//                ctx.saveGame.players[j].intell = 8;
//                ctx.saveGame.players[j].piety = 8;
//                ctx.saveGame.players[j].vitality = 8;
//                ctx.saveGame.players[j].agility = 8;
//                ctx.saveGame.players[j].luck = 8;

                //ctx.saveGame.players[j].armor = ITEMS_MAP.get("ROBES").clone();
                //ctx.saveGame.players[j].weapon = ITEMS_MAP.get("MACE+1").clone();
                //ctx.saveGame.players[j].weapon = ITEMS_MAP.get("MAGIC BOW").clone();
                //ctx.saveGame.players[j].helm = ITEMS_MAP.get("HELM").clone();
                //ctx.saveGame.players[j].shield = ITEMS_MAP.get("LARGE SHIELD").clone();
                //ctx.saveGame.players[j].glove = ITEMS_MAP.get("SILVER GLOVES").clone();
                //ctx.saveGame.players[j].item1 = ITEMS_MAP.get("ROD OF FLAME").clone();
                //ctx.saveGame.players[j].item2 = ITEMS_MAP.get("WERDNAS AMULET").clone();
                //ctx.saveGame.players[j].spellPresets[0] = Spells.DIOS;
            }

            TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
            TiledMap tm = loader.load("assets/data/combat1.tmx");

            int level = 9;

            //String[] monsters = MONSTER_ENCOUNTER_LEVEL[level - 1];
            //int idx = Utils.RANDOM.nextInt(monsters.length);
            //Monster monster = Andius.MONSTER_MAP.get(monsters[idx]);
            Monster monster = PMO_MONSTERS.get(3);

            Actor actor = new Actor(monster.name, null);
            
            MutableMonster mm = new MutableMonster(monster);
            actor.set(mm, Role.MONSTER, 1, 1, 1, 1, Constants.MovementBehavior.ATTACK_AVATAR);

            CombatScreen cs = new CombatScreen(ctx, Constants.Map.CAVE, tm, actor, level, true);
            setScreen(cs);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
