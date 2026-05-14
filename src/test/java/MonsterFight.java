
import andius.Combat;
import andius.Combat.Action;
import andius.Constants;
import andius.Constants.SpellArea;
import andius.Context;
import andius.WizardryData;
import andius.objects.ClassType;
import static andius.objects.ClassType.BISHOP;
import static andius.objects.ClassType.FIGHTER;
import static andius.objects.ClassType.LORD;
import static andius.objects.ClassType.MAGE;
import static andius.objects.ClassType.NINJA;
import static andius.objects.ClassType.PRIEST;
import static andius.objects.ClassType.SAMURAI;
import static andius.objects.ClassType.THIEF;
import andius.objects.Monster;
import andius.objects.MutableMonster;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Sound;
import andius.objects.Spells;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.GdxNativesLoader;
import java.io.File;
import utils.Loggable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class MonsterFight {

    private static final int RESULT_LOSS = 0;
    private static final int RESULT_WIN = 1;
    private static final int RESULT_TIMEOUT = -100;
    private static final int RESULT_SKIPPED = -999;

    private static final String TIMEOUT_NONE = "";
    private static final String TIMEOUT_STALEMATE = "STALE";
    private static final String TIMEOUT_SLOW_PROGRESS = "SLOW";
    private static final String TIMEOUT_PARTY_CANNOT_REACH = "REACH";
    private static final String TIMEOUT_MONSTERS_ADDED = "HELP";
    private static final String TIMEOUT_HEALING_OR_REGEN = "HEAL";
    private static final String TIMEOUT_UNKNOWN = "UNK";

    public static void main(String[] args) throws Exception {

        initGdxForHeadlessRun();

        int maxLevel = intArg(args, 0, 10);
        int maxRounds = intArg(args, 1, 30);
        int maxConsecutiveTimeouts = intArg(args, 2, 3);

        System.out.println("MonsterFight profiler run");
        System.out.println("maxLevel=" + maxLevel);
        System.out.println("maxRounds=" + maxRounds);
        System.out.println("maxConsecutiveTimeouts=" + maxConsecutiveTimeouts);
        System.out.println();

        runScenario(Constants.Map.WIZARDRY1, maxLevel, maxRounds, maxConsecutiveTimeouts);
        runScenario(Constants.Map.WIZARDRY2, maxLevel, maxRounds, maxConsecutiveTimeouts);
        runScenario(Constants.Map.WIZARDRY3, maxLevel, maxRounds, maxConsecutiveTimeouts);
    }

    private static void initGdxForHeadlessRun() {

        GdxNativesLoader.load();

        Gdx.gl = mock(GL20.class);
        Gdx.graphics = new MockGraphics();
        Gdx.files = mock(Files.class);

        doAnswer(inv -> {
            String filename = inv.getArgument(0, String.class);

            File file = new File("src/main/resources", filename);
            if (!file.exists()) {
                file = new File("target/classes", filename);
            }

            return new FileHandle(file);
        }).when(Gdx.files).classpath(anyString());

        Constants.class.getClass();
        WizardryData.class.getClass();
    }

    private static void runScenario(
            Constants.Map map,
            int maxLevel,
            int maxRounds,
            int maxConsecutiveTimeouts) throws Exception {

        Spells[] spellsArray = Spells.values();

        printFightHeader(map, maxLevel);

        int totalWins = 0;
        int totalLosses = 0;
        int totalTimeouts = 0;
        int totalSkipped = 0;

        long scenarioStart = System.nanoTime();

        for (int m = 0; m < map.scenario().monsters().size(); m++) {

            long monsterStart = System.nanoTime();

            Monster monster = map.scenario().monsters().get(m);

            FightResult[] rowResults = new FightResult[maxLevel];
            int consecutiveTimeouts = 0;

            for (int lvl = 0; lvl < maxLevel; lvl++) {

                FightResult fightResult = runSingleFight(
                        map,
                        monster,
                        lvl,
                        maxRounds,
                        spellsArray
                );

                rowResults[lvl] = fightResult;

                if (fightResult.result == RESULT_WIN) {
                    totalWins++;
                    consecutiveTimeouts = 0;
                } else if (fightResult.result == RESULT_TIMEOUT) {
                    totalTimeouts++;
                    consecutiveTimeouts++;
                } else {
                    totalLosses++;
                    consecutiveTimeouts = 0;
                }

                if (consecutiveTimeouts >= maxConsecutiveTimeouts) {
                    for (int skip = lvl + 1; skip < maxLevel; skip++) {
                        rowResults[skip] = new FightResult(RESULT_SKIPPED, 0, TIMEOUT_NONE);
                        totalSkipped++;
                    }
                    break;
                }
            }

            long monsterMillis = (System.nanoTime() - monsterStart) / 1_000_000L;
            printFightRow(m, monster, rowResults, monsterMillis);
            System.out.flush();
        }

        long scenarioMillis = (System.nanoTime() - scenarioStart) / 1_000_000L;

        System.out.println();
        System.out.println(String.format(
                "Summary: %d wins, %d losses, %d timeouts, %d skipped, %d ms",
                totalWins,
                totalLosses,
                totalTimeouts,
                totalSkipped,
                scenarioMillis
        ));
        System.out.println();
    }

    private static FightResult runSingleFight(
            Constants.Map map,
            Monster monster,
            int lvl,
            int maxRounds,
            Spells[] spellsArray) {

        Context ctx = new Context();
        ctx.setSaveGame(new SaveGame());
        ctx.saveGame.players = new CharacterRecord[6];
        ctx.saveGame.players[0] = SaveGame.generatePlayer(lvl + 1, ClassType.FIGHTER, "fred");
        ctx.saveGame.players[1] = SaveGame.generatePlayer(lvl + 1, ClassType.FIGHTER, "same");
        ctx.saveGame.players[2] = SaveGame.generatePlayer(lvl + 1, ClassType.FIGHTER, "jack");
        ctx.saveGame.players[3] = SaveGame.generatePlayer(lvl + 1, ClassType.PRIEST, "joe");
        ctx.saveGame.players[4] = SaveGame.generatePlayer(lvl + 1, ClassType.MAGE, "jane");
        ctx.saveGame.players[5] = SaveGame.generatePlayer(lvl + 1, ClassType.MAGE, "frank");

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

        int startingMonsterCount = livingMonsterCount(combat);
        int startingMonsterHp = totalMonsterHp(combat);
        int startingPartyHp = totalPartyHp(ctx);

        combat.setLogs(logs);

        int round = 0;

        while (round < maxRounds) {
            round++;

            boolean monstersAlive = false;
            for (MutableMonster e : combat.monsters) {
                if (!e.isDead()) {
                    monstersAlive = true;
                    break;
                }
            }

            if (!monstersAlive) {
                break;
            }

            if (ctx.allDead()) {
                break;
            }

            assignCasterSpells(combat, spellsArray);

            combat.fight();
        }

        int endingMonsterCount = livingMonsterCount(combat);
        int endingMonsterHp = totalMonsterHp(combat);
        int endingPartyHp = totalPartyHp(ctx);
        int endingLivingPlayers = livingPlayerCount(ctx);
        int endingLivingFrontPlayers = livingFrontPlayerCount(combat);

        int result = ctx.allDead() ? RESULT_LOSS : RESULT_WIN;
        String timeoutReason = TIMEOUT_NONE;

        if (round == maxRounds) {
            result = RESULT_TIMEOUT;

            boolean monsterHpChanged = endingMonsterHp != startingMonsterHp;
            boolean partyHpChanged = endingPartyHp != startingPartyHp;
            boolean monstersAdded = endingMonsterCount > startingMonsterCount;
            boolean monsterHpWentDown = endingMonsterHp < startingMonsterHp;
            boolean monsterHpWentUpOrSame = endingMonsterHp >= startingMonsterHp;

            if (!monsterHpChanged && !partyHpChanged) {
                timeoutReason = TIMEOUT_STALEMATE;

            } else if (monstersAdded) {
                timeoutReason = TIMEOUT_MONSTERS_ADDED;

            } else if (endingLivingFrontPlayers == 0 && endingLivingPlayers > 0) {
                timeoutReason = TIMEOUT_PARTY_CANNOT_REACH;

            } else if (monsterHpWentUpOrSame && partyHpChanged) {
                timeoutReason = TIMEOUT_HEALING_OR_REGEN;

            } else if (monsterHpWentDown) {
                timeoutReason = TIMEOUT_SLOW_PROGRESS;

            } else {
                timeoutReason = TIMEOUT_UNKNOWN;
            }
        }

        return new FightResult(result, round, timeoutReason);
    }

    private static void assignCasterSpells(Combat combat, Spells[] spellsArray) {

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
                case BISHOP:
                    a.spell = null;
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
                    break;
            }
        }
    }

    private static void printFightHeader(Constants.Map map, int maxPartyLevel) {

        System.out.println();
        System.out.println("Fight Results for Scenario: " + map.scenario());
        System.out.println("Legend: WIN n = party won, LOSS n = party died, TIMEOUT n REASON = unresolved at round cap, SKIP = skipped after repeated timeouts");
        System.out.println("Reasons: STALE=no HP change, SLOW=monster HP falling, REACH=front row dead/back row alive, HELP=monsters added, HEAL=monster HP not falling, UNK=unknown");
        System.out.println();

        StringBuilder header = new StringBuilder();
        header.append(String.format(
                "%-4s %-35s %-10s",
                "ID",
                "Monster",
                "MonsterLv"
        ));

        for (int lvl = 0; lvl < maxPartyLevel; lvl++) {
            header.append(String.format(" %-16s", "PartyLv" + (lvl + 1)));
        }

        header.append(String.format(" %-8s", "Millis"));

        System.out.println(header);
        System.out.println("-".repeat(header.length()));
        System.out.flush();
    }

    private static void printFightRow(
            int monsterId,
            Monster monster,
            FightResult[] rowResults,
            long monsterMillis) {

        StringBuilder row = new StringBuilder();
        row.append(String.format(
                "%-4d %-35s %-10d",
                monsterId,
                truncate(monster.getName(), 35),
                monster.getLevel()
        ));

        for (int lvl = 0; lvl < rowResults.length; lvl++) {
            FightResult fightResult = rowResults[lvl];

            String cell;

            if (fightResult == null) {
                cell = "";
            } else if (fightResult.result == RESULT_WIN) {
                cell = "WIN " + fightResult.rounds;
            } else if (fightResult.result == RESULT_TIMEOUT) {
                cell = "TIMEOUT " + fightResult.rounds + " " + fightResult.timeoutReason;
            } else if (fightResult.result == RESULT_SKIPPED) {
                cell = "SKIP";
            } else {
                cell = "LOSS " + fightResult.rounds;
            }

            row.append(String.format(" %-16s", cell));
        }

        row.append(String.format(" %-8d", monsterMillis));

        System.out.println(row);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 1) + "…";
    }

    private static class FightResult {

        private final int result;
        private final int rounds;
        private final String timeoutReason;

        private FightResult(int result, int rounds, String timeoutReason) {
            this.result = result;
            this.rounds = rounds;
            this.timeoutReason = timeoutReason;
        }
    }

    private static int livingMonsterCount(Combat combat) {
        int count = 0;
        for (MutableMonster monster : combat.monsters) {
            if (!monster.isDead()) {
                count++;
            }
        }
        return count;
    }

    private static int totalMonsterHp(Combat combat) {
        int total = 0;
        for (MutableMonster monster : combat.monsters) {
            if (!monster.isDead()) {
                total += Math.max(0, monster.getCurrentHitPoints());
            }
        }
        return total;
    }

    private static int livingPlayerCount(Context ctx) {
        int count = 0;
        for (CharacterRecord player : ctx.players()) {
            if (!player.isDead()) {
                count++;
            }
        }
        return count;
    }

    private static int totalPartyHp(Context ctx) {
        int total = 0;
        for (CharacterRecord player : ctx.players()) {
            if (!player.isDead()) {
                total += Math.max(0, player.hp);
            }
        }
        return total;
    }

    private static int livingFrontPlayerCount(Combat combat) {
        int count = 0;
        for (int i = 0; i < Math.min(3, combat.players.size()); i++) {
            if (!combat.players.get(i).isDead()) {
                count++;
            }
        }
        return count;
    }

    private static int intArg(String[] args, int index, int defaultValue) {
        if (args == null || args.length <= index) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
