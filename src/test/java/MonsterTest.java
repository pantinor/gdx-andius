
import andius.Constants;
import andius.Constants.SpellArea;
import andius.Context;
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
import andius.objects.Spells;
import com.badlogic.gdx.graphics.Color;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import andius.Combat;
import andius.Combat.Action;
import andius.WizardryData;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Sound;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;
import utils.Loggable;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.GdxNativesLoader;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import utils.Utils;

public class MonsterTest {

    static {

        GdxNativesLoader.load();
        Gdx.gl = mock(GL20.class);
        Gdx.graphics = new MockGraphics();
        Gdx.files = mock(Files.class);

        doAnswer(inv -> {
            String filename = inv.getArgument(0, String.class);
            return new FileHandle(filename);
        }).when(Gdx.files).classpath(anyString());

        Utils.class.getClass();
        Utils.CLASSPTH_RSLVR = (String fileName) -> Gdx.files.classpath("./src/main/resources/" + fileName);

        Constants.class.getClass();
        WizardryData.class.getClass();
    }

    @Test
    public void monsters() throws Exception {

        InputStream is2 = this.getClass().getResourceAsStream("/assets/json/ultima-monsters.json");
        String json = IOUtils.toString(is2);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        List<Monster> monsters = gson.fromJson(json, new TypeToken<java.util.List<Monster>>() {
        }.getType());

        Collections.sort(monsters);

        // Header aligned to Monster.toString()’s column widths
        String header = String.format(
                "%s\t%-55s\t%-15s\t%-4s\t%-4s\t%-5s\t%-3s\t%-25s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                "ID",
                "NAME",
                "TYPE",
                "LVL",
                "EXP",
                "HPMX",
                "AC",
                "DAMG",
                "MAGE",
                "PRST",
                "SPED",
                "GOLD",
                "REWD",
                "LVLDR",
                "HEAL",
                "BRTH",
                "PARTID",
                "GRPSZ",
                "ICON"
        );
        //System.out.println(header);

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

    @Test(dataProvider = "maps")
    public void testFights(Constants.Map map) throws Exception {

        if (map.getTmxFile() != null) {
            throw new SkipException("Skipped");
        }

        int maxLevel = 1;
        int[][][] results = new int[map.scenario().monsters().size()][maxLevel][2];

        Spells[] spellsArray = Spells.values();

        for (int m = 0; m < map.scenario().monsters().size(); m++) {
            for (int lvl = 0; lvl < maxLevel; lvl++) {
                Monster monster = map.scenario().monsters().get(m);
                Context ctx = new Context();
                ctx.setSaveGame(new SaveGame());
                ctx.saveGame.players = new CharacterRecord[6];
                ctx.saveGame.players[0] = SaveGame.generatePlayer(lvl + 1, ClassType.FIGHTER, "fred");
                ctx.saveGame.players[1] = SaveGame.generatePlayer(lvl + 1, ClassType.FIGHTER, "same");
                ctx.saveGame.players[2] = SaveGame.generatePlayer(lvl + 1, ClassType.FIGHTER, "jack");
                ctx.saveGame.players[3] = SaveGame.generatePlayer(lvl + 1, ClassType.PRIEST, "joe");
                ctx.saveGame.players[4] = SaveGame.generatePlayer(lvl + 1, ClassType.MAGE, "jane");
                ctx.saveGame.players[5] = SaveGame.generatePlayer(lvl + 1, ClassType.THIEF, "frank");

                Loggable logs = new Loggable() {
                    @Override
                    public void add(String s) {
                    }

                    @Override
                    public void add(String s, Color c) {
                    }
                };

                Combat combat = new Combat(ctx, map, monster, lvl) {
                    @Override
                    public void log(String s) {
                    }

                    @Override
                    public void log(String s, Color c) {
                    }

                    @Override
                    public void playSound(Sound sound) {
                    }
                };

                combat.setLogs(logs);

                int round = 0;
                while (round < 100) {

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

                results[m][lvl][0] = ctx.allDead() ? 0 : 1;
                results[m][lvl][1] = round;

                if (round == 100) {
                    System.out.println("Tough fight cannot win with " + monster.getName() + " at level " + lvl);
                    results[m][lvl][0] = -100;
                }
            }
        }

        sortResults(map, results);
    }

    private void printResults(Constants.Map map, int[][][] results) {
        System.out.println("Results for Scenario " + map.scenario());

        int[] wins = new int[map.scenario().monsters().size()];
        for (int i = 0; i < wins.length; i++) {
            int[][] leveltally = results[i];
            int sum = 0;
            for (int j = 0; j < leveltally.length; j++) {
                sum += leveltally[j][0];
            }
            wins[i] = sum;
        }

        int[] rounds = new int[map.scenario().monsters().size()];
        for (int i = 0; i < rounds.length; i++) {
            int[][] leveltally = results[i];
            int sum = 0;
            for (int j = 0; j < leveltally.length; j++) {
                sum += leveltally[j][1];
            }
            rounds[i] = sum;
        }

        int[][] indicesWithSums = new int[map.scenario().monsters().size()][2];
        for (int i = 0; i < indicesWithSums.length; i++) {
            indicesWithSums[i][0] = i;
            indicesWithSums[i][1] = wins[i];
        }

        Arrays.sort(indicesWithSums, (a, b) -> Integer.compare(b[1], a[1]));

        for (int i = 0; i < indicesWithSums.length; i++) {
            int monsterId = indicesWithSums[i][0];
            int[][] row = results[monsterId];
            StringBuilder rowBuilder = new StringBuilder(String.format("Monster ID %d [", monsterId));

            for (int[] pair : row) {
                rowBuilder.append(String.format("%d ", pair[0]));
            }

            rowBuilder.append("] [");

            for (int[] pair : row) {
                rowBuilder.append(String.format("%d ", pair[1]));
            }

            rowBuilder.append(String.format("] %s level %d",
                    map.scenario().monsters().get(monsterId).getName(),
                    map.scenario().monsters().get(monsterId).getLevel()));

            System.out.println(rowBuilder);
        }
    }

    private void sortResults(Constants.Map map, int[][][] results) {

        System.out.println("\n\nResults for Scenario " + map.scenario());

        int monsterCount = results.length;

        int[][] aggregateData = new int[monsterCount][4];
        for (int monsterID = 0; monsterID < monsterCount; monsterID++) {
            int totalWins = 0;
            int totalRounds = 0;

            for (int level = 0; level < results[monsterID].length; level++) {
                totalWins += results[monsterID][level][0];   // Wins
                totalRounds += results[monsterID][level][1]; // Rounds
                aggregateData[monsterID][3]++; //level
            }

            aggregateData[monsterID][0] = monsterID;       // MonsterID
            aggregateData[monsterID][1] = totalWins;      // Total Wins
            aggregateData[monsterID][2] = totalRounds;    // Total Rounds
        }

        Arrays.sort(aggregateData, (a, b) -> {

            // Sort by total rounds (ascending)
            int roundComparison = Integer.compare(a[2], b[2]);
            if (roundComparison != 0) {
                return roundComparison;
            }

            // Sort by total wins (ascending)
            int winComparison = Integer.compare(a[1], b[1]);
            if (winComparison != 0) {
                return winComparison;
            }

            // Sort by monsterID (ascending, as a fallback)
            return Integer.compare(a[0], b[0]);
        });

        for (int i = 0; i < aggregateData.length; i++) {
            String name = map.scenario().monsters().get(aggregateData[i][0]).getName();
            System.out.println(name + ", Total Wins: " + aggregateData[i][1] + ", Total Rounds: " + aggregateData[i][2] + ", Characters fought up to level: " + aggregateData[i][3]);
        }
    }

    @Test
    public void testGenerateSaveGameTeamAtLevel() throws Exception {
        int lvl = 1;
        Context ctx = new Context();
        ctx.setSaveGame(new SaveGame());
        ctx.saveGame.players = new CharacterRecord[6];
        ctx.saveGame.players[0] = SaveGame.generatePlayer(lvl, ClassType.FIGHTER, "FRED");
        ctx.saveGame.players[1] = SaveGame.generatePlayer(lvl, ClassType.FIGHTER, "SAM");
        ctx.saveGame.players[2] = SaveGame.generatePlayer(lvl, ClassType.FIGHTER, "JACK");
        ctx.saveGame.players[3] = SaveGame.generatePlayer(lvl, ClassType.PRIEST, "JOE");
        ctx.saveGame.players[4] = SaveGame.generatePlayer(lvl, ClassType.MAGE, "JANE");
        ctx.saveGame.players[5] = SaveGame.generatePlayer(lvl, ClassType.THIEF, "FRANK");

        ctx.saveGame.saveGameFileName = "party-team.json";
        ctx.saveGame.write();
    }

    //@Test
    public void testGenerateSaveGameMageOnlyAtLevel() throws Exception {
        Context ctx = new Context();
        ctx.setSaveGame(new SaveGame());
        ctx.saveGame.players = new CharacterRecord[1];

        int level = 6;
        ctx.saveGame.players[0] = SaveGame.generatePlayer(level, ClassType.MAGE, "jane");

        assertEquals(ctx.saveGame.players[0].level, level);

        ctx.saveGame.map = Constants.Map.WORLD;
        ctx.saveGame.wx = 10;
        ctx.saveGame.wy = 54;

        ctx.saveGame.saveGameFileName = "party-mage.json";
        ctx.saveGame.write();
    }

}
