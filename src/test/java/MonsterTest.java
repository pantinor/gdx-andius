
import andius.Constants;
import static andius.Constants.LEVEL_PROGRESSION_TABLE;
import andius.Constants.SpellArea;
import andius.Context;
import static andius.WizardryData.PMO_ITEMS_MAP;
import andius.objects.ClassType;
import static andius.objects.ClassType.BISHOP;
import static andius.objects.ClassType.FIGHTER;
import static andius.objects.ClassType.LORD;
import static andius.objects.ClassType.MAGE;
import static andius.objects.ClassType.NINJA;
import static andius.objects.ClassType.PRIEST;
import static andius.objects.ClassType.SAMURAI;
import static andius.objects.ClassType.THIEF;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.MutableMonster;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Spells;
import com.badlogic.gdx.graphics.Color;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import utils.Combat;
import utils.Combat.Action;
import utils.Utils;

public class MonsterTest {

    //@Test
    public void monsters() throws Exception {

        InputStream is2 = this.getClass().getResourceAsStream("/assets/json/leglyl-monsters.json");
        String json = IOUtils.toString(is2);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        List<Monster> monsters = gson.fromJson(json, new TypeToken<java.util.List<Monster>>() {
        }.getType());

        Collections.sort(monsters);
        System.out.println("ID\tNAME          \tTYPE      \tLVL\tEXP\tHPMX\tAC\tDAMG                      \tMAGE\tPRST\tSPED\tGOLD\tREWD\tLVLDR\tHEAL\tBRTH\tPARTID\tGRPSZ");
        for (Monster m : monsters) {
            System.out.println(m);
        }

    }

    //@Test
    public void items() throws Exception {

        InputStream is2 = this.getClass().getResourceAsStream("/assets/json/items.json");
        String json = IOUtils.toString(is2);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        List<Item> items = gson.fromJson(json, new TypeToken<java.util.List<Item>>() {
        }.getType());

        Collections.sort(items);

        System.out.println("NAME               \tTYPE     \tCOST     \tDAMG\tAC\tSWINGS\tSPELL     \tHITMOD\tREGN\tVENDOR\tUSABLE");
        for (Item m : items) {
            System.out.println(m);
        }

    }

    @DataProvider(name = "maps")
    public Constants.Map[][] maps() {
        Constants.Map[] maps = Constants.Map.values();
        Constants.Map[][] data = new Constants.Map[maps.length][1];
        for (int i = 0; i < maps.length; i++) {
            data[i][0] = maps[i];
        }
        return data;
    }

    //@Test(dataProvider = "maps")
    public void testFights(Constants.Map map) throws Exception {

        if (map.getTmxFile() != null) {
            throw new SkipException("Skipped");
        }

        int maxLevel = 20;
        int[][] results = new int[map.scenario().monsters().size()][maxLevel];

        Spells[] spellsArray = Spells.values();

        for (int m = 0; m < map.scenario().monsters().size(); m++) {
            for (int lvl = 0; lvl < 20; lvl++) {
                Monster monster = map.scenario().monsters().get(m);
                Context ctx = new Context();
                ctx.setSaveGame(new SaveGame());
                ctx.saveGame.players = new SaveGame.CharacterRecord[6];
                ctx.saveGame.players[0] = generatePlayer(lvl + 1, ClassType.FIGHTER, "fred");
                ctx.saveGame.players[1] = generatePlayer(lvl + 1, ClassType.FIGHTER, "same");
                ctx.saveGame.players[2] = generatePlayer(lvl + 1, ClassType.FIGHTER, "jack");
                ctx.saveGame.players[3] = generatePlayer(lvl + 1, ClassType.PRIEST, "joe");
                ctx.saveGame.players[4] = generatePlayer(lvl + 1, ClassType.MAGE, "jane");
                ctx.saveGame.players[5] = generatePlayer(lvl + 1, ClassType.THIEF, "frank");

                Combat combat = new Combat(ctx, map, monster, 1) {
                    @Override
                    public void log(String s) {
                    }

                    @Override
                    public void log(String s, Color c) {
                    }
                };

                int round = 0;
                while (round < 50) {

                    round++;

                    boolean alive = false;
                    for (MutableMonster e : combat.monsters) {
                        if (!e.isDead()) {
                            alive = true;
                        }
                    }

                    if (!alive) {
                        break;
                    }

                    if (ctx.allDead()) {
                        break;
                    }

                    for (Action a : combat.actions) {
                        switch (a.player.classType) {
                            case SAMURAI:
                            case LORD:
                            case NINJA:
                            case FIGHTER:
                            case THIEF:
                                break;
                            case MAGE:
                            case PRIEST:
                            case BISHOP: {
                                for (int i = spellsArray.length - 1; i >= 0; i--) {
                                    Spells s = spellsArray[i];
                                    if (s.getArea() != SpellArea.COMBAT) {
                                        continue;
                                    }
                                    if (s.getHitCount() <= 0) {
                                        continue;
                                    }
                                    if (a.player.canCast(s)) {
                                        a.spell = s;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }

                    combat.fight();
                }

                if (ctx.allDead()) {
                    results[m][lvl] = 0;
                } else {
                    results[m][lvl] = 1;
                }

                if (round == 50) {
                    System.out.println("Tough fight cannot win with " + monster.getName() + " at level " + lvl);
                    results[m][lvl] = -9;
                    break;
                }
            }
        }

        printResults(map, results);
    }

    //@Test
    public void testGeneratePlayer() throws Exception {
        CharacterRecord p = generatePlayer(1, ClassType.FIGHTER, "fred");
    }

    private SaveGame.CharacterRecord generatePlayer(int lvl, ClassType ctype, String name) {

        SaveGame.CharacterRecord p = new SaveGame.CharacterRecord();

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

        p.exp = tmp;

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

    private void printResults(Constants.Map map, int[][] results) {

        System.out.println("Results for Scenario " + map.scenario());

        Integer[] indices = new Integer[map.scenario().monsters().size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }

        Arrays.sort(indices, Comparator.comparingInt(i -> Arrays.stream(results[(int) i]).sum()).reversed());

        for (int index : indices) {
            int[] row = results[index];
            System.out.println(String.format("%d [%d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d] %s %d",
                    index,
                    row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8], row[9],
                    row[10], row[11], row[12], row[13], row[14], row[15], row[16], row[17], row[18], row[19],
                    map.scenario().monsters().get(index).getName(),
                    map.scenario().monsters().get(index).getLevel())
            );
        }
    }

}
