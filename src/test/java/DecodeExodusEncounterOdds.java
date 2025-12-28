import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decode the last 30 bytes of each Exodus dungeon level (EncounterOdds[3]) and print
 * human-readable encounter tables using renumbered Ultima monsterIds.
 *
 * Usage:
 *   javac DecodeExodusEncounterOdds.java
 *   java DecodeExodusEncounterOdds <WizardryData.java> <ultima-monsters-sorted-by-exp-renumbered.json>
 *
 * Optional:
 *   java DecodeExodusEncounterOdds <WizardryData.java> <monsters.json> --only DOOM
 *   java DecodeExodusEncounterOdds <WizardryData.java> <monsters.json> --only FIRE
 */
public class DecodeExodusEncounterOdds {

    // Matches: private static final String ULT_EX_DOOM_0 = "....";
    private static final Pattern EX_CONST =
            Pattern.compile("private\\s+static\\s+final\\s+String\\s+(ULT_EX_[A-Z]+_\\d)\\s*=\\s*\"([0-9A-Fa-f]+)\";");

    // Minimal JSON extraction for monsterId/name/exp (no external libs)
    private static final Pattern MONSTER_OBJ = Pattern.compile("\\{[^\\{\\}]*\\}");
    private static final Pattern MONSTER_ID = Pattern.compile("\"monsterId\"\\s*:\\s*(\\d+)");
    private static final Pattern MONSTER_EXP = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");
    private static final Pattern MONSTER_NAME = Pattern.compile("\"name\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    private static final DecimalFormat PCT = new DecimalFormat("0.00");

    private static final double TIER_COMMON = 0.75;      // 3/4
    private static final double TIER_UNCOMMON = 0.1875;  // 3/16
    private static final double TIER_RARE = 0.0625;      // 1/16

    public static void main(String[] args) throws Exception {
        Path wizardryDataJava = Path.of("D:\\work\\gdx-andius\\src\\main\\java\\andius\\WizardryData.java");
        Path monstersJson = Path.of("D:\\work\\gdx-andius\\src\\main\\resources\\assets\\json\\ultima-monsters.json");

        Map<Integer, MonsterInfo> monsters = loadMonsters(monstersJson);
        String javaText = Files.readString(wizardryDataJava, StandardCharsets.UTF_8);

        Map<String, List<LevelReport>> byDungeon = new TreeMap<>();
        Matcher m = EX_CONST.matcher(javaText);
        int found = 0;

        while (m.find()) {
            found++;
            String constName = m.group(1);          // e.g., ULT_EX_DOOM_0
            String hex = m.group(2);

            String dungeon = extractDungeon(constName); // DOOM
            int level = extractLevel(constName);        // 1..8 (suffix + 1)

            byte[] bytes = hexToBytes(hex);
            if (bytes.length < 30) {
                System.err.println("Skipping " + constName + " (too short: " + bytes.length + " bytes)");
                continue;
            }

            int tailOffset = bytes.length - 30;
            EncounterOdds common = new EncounterOdds(bytes, tailOffset);
            EncounterOdds uncommon = new EncounterOdds(bytes, tailOffset + 10);
            EncounterOdds rare = new EncounterOdds(bytes, tailOffset + 20);

            LevelReport rep = new LevelReport(constName, dungeon, level, bytesToHex(bytes, tailOffset, 30), common, uncommon, rare);
            byDungeon.computeIfAbsent(dungeon, k -> new ArrayList<>()).add(rep);
        }

        if (found == 0) {
            System.err.println("No ULT_EX_*_* constants found. Are you pointing at the correct WizardryData.java?");
            System.exit(2);
        }

        // Sort levels within each dungeon
        for (List<LevelReport> levels : byDungeon.values()) {
            levels.sort(Comparator.comparingInt(a -> a.level));
        }

        printReport(byDungeon, monsters);
    }

    private static void printReport(Map<String, List<LevelReport>> byDungeon, Map<Integer, MonsterInfo> monsters) {
        System.out.println("=== Exodus Dungeon Encounter Odds Decoder ===");
        System.out.println("Tier selection probabilities (matches getRandomMonster logic):");
        System.out.println("  Common   : " + PCT.format(TIER_COMMON * 100) + "%");
        System.out.println("  Uncommon : " + PCT.format(TIER_UNCOMMON * 100) + "%");
        System.out.println("  Rare     : " + PCT.format(TIER_RARE * 100) + "%");
        System.out.println();

        for (Map.Entry<String, List<LevelReport>> e : byDungeon.entrySet()) {
            String dungeon = e.getKey();
            List<LevelReport> levels = e.getValue();

            System.out.println("============================================================");
            System.out.println("DUNGEON: " + dungeon);
            System.out.println("============================================================");

            for (LevelReport rep : levels) {
                System.out.println();
                System.out.println(rep.constName + "  (Level " + rep.level + ")");
                System.out.println("Last 30 bytes (encounter odds): " + rep.tailHex);

                printTier("Common", TIER_COMMON, rep.common, monsters);
                printTier("Uncommon", TIER_UNCOMMON, rep.uncommon, monsters);
                printTier("Rare", TIER_RARE, rep.rare, monsters);
            }

            System.out.println();
        }
    }

    private static void printTier(String label, double tierProb, EncounterOdds odds, Map<Integer, MonsterInfo> monsters) {
        System.out.println("  --- " + label + " (" + PCT.format(tierProb * 100) + "%) ---");
        System.out.println("  Fields: minEnemy=" + odds.minEnemy
                + ", rangeSize=" + odds.rangeSize
                + ", totExtraRanges=" + odds.totExtraRanges
                + ", extraRangeOffset=" + odds.extraRangeOffset
                + ", extraRangeOdds=" + odds.extraRangeOdds + "%");

        List<Integer> ids = odds.getAllIdsSorted();
        if (ids.isEmpty()) {
            System.out.println("  Monsters: (none)");
            return;
        }

        // Per-monster probability within this tier (then overall probability including tierProb)
        Map<Integer, Double> withinTier = odds.perMonsterProbabilityWithinTier();
        List<Integer> sorted = new ArrayList<>(withinTier.keySet());
        Collections.sort(sorted);

        System.out.println("  Monsters:");
        for (int id : sorted) {
            MonsterInfo info = monsters.get(id);
            String name = (info == null) ? "(unknown)" : info.name;
            String exp = (info == null) ? "" : (" exp=" + info.exp);

            double pTier = withinTier.getOrDefault(id, 0.0);
            double pOverall = pTier * tierProb;

            System.out.println("    - id " + id + ": " + name + exp
                    + " | within-tier " + PCT.format(pTier * 100) + "%, overall " + PCT.format(pOverall * 100) + "%");
        }
    }

    // ===== Data structures =====

    private static final class LevelReport {
        final String constName;
        final String dungeon;
        final int level;
        final String tailHex;
        final EncounterOdds common, uncommon, rare;

        LevelReport(String constName, String dungeon, int level, String tailHex,
                    EncounterOdds common, EncounterOdds uncommon, EncounterOdds rare) {
            this.constName = constName;
            this.dungeon = dungeon;
            this.level = level;
            this.tailHex = tailHex;
            this.common = common;
            this.uncommon = uncommon;
            this.rare = rare;
        }
    }

    private static final class EncounterOdds {
        final int minEnemy;
        final int extraRangeOffset;
        final int totExtraRanges;
        final int rangeSize;
        final int extraRangeOdds; // percent 0..100 (used as: RANDOM.nextInt(100) < extraRangeOdds)

        EncounterOdds(byte[] buffer, int offset) {
            // Mirrors EndianUtils.readSwappedShort(...) in your code: little-endian short
            this.minEnemy = readLeShortAsInt(buffer, offset);
            this.extraRangeOffset = readLeShortAsInt(buffer, offset + 2);
            this.totExtraRanges = readLeShortAsInt(buffer, offset + 4);
            this.rangeSize = readLeShortAsInt(buffer, offset + 6);
            this.extraRangeOdds = readLeShortAsInt(buffer, offset + 8);
        }

        List<Integer> getAllIdsSorted() {
            if (rangeSize <= 0) return Collections.emptyList();
            ArrayList<Integer> ids = new ArrayList<>(Math.max(0, (totExtraRanges + 1) * rangeSize));
            for (int rangeNo = 0; rangeNo <= totExtraRanges; rangeNo++) {
                int base = minEnemy + extraRangeOffset * rangeNo;
                for (int k = 0; k < rangeSize; k++) {
                    ids.add(base + k);
                }
            }
            Collections.sort(ids);
            return ids;
        }

        /**
         * Probability of selecting each monster *within this tier*, based on:
         *   int rangeNo = 0;
         *   while (RANDOM.nextInt(100) < extraRangeOdds && rangeNo < totExtraRanges) ++rangeNo;
         *   return minEnemy + RANDOM.nextInt(rangeSize) + extraRangeOffset * rangeNo;
         */
        Map<Integer, Double> perMonsterProbabilityWithinTier() {
            Map<Integer, Double> out = new HashMap<>();
            if (rangeSize <= 0) return out;

            double step = clamp01(extraRangeOdds / 100.0);
            int n = Math.max(0, totExtraRanges);

            for (int rangeNo = 0; rangeNo <= n; rangeNo++) {
                double pRange;
                if (rangeNo < n) {
                    // truncated geometric: success^k * (1-success)
                    pRange = Math.pow(step, rangeNo) * (1.0 - step);
                } else {
                    // last bucket: success^n (loop stops at n)
                    pRange = Math.pow(step, n);
                }

                int base = minEnemy + extraRangeOffset * rangeNo;
                double pEach = pRange / rangeSize;
                for (int k = 0; k < rangeSize; k++) {
                    int id = base + k;
                    out.put(id, out.getOrDefault(id, 0.0) + pEach);
                }
            }

            return out;
        }
    }

    private static final class MonsterInfo {
        final int id;
        final String name;
        final int exp;

        MonsterInfo(int id, String name, int exp) {
            this.id = id;
            this.name = name;
            this.exp = exp;
        }
    }

    // ===== Parsing helpers =====

    private static String extractDungeon(String constName) {
        // ULT_EX_DOOM_0 -> DOOM
        String[] parts = constName.split("_");
        if (parts.length < 4) return constName;
        return parts[2];
    }

    private static int extractLevel(String constName) {
        // suffix is 0..7, level is 1..8
        int idx = constName.lastIndexOf('_');
        if (idx < 0) return -1;
        String suffix = constName.substring(idx + 1);
        int s = Integer.parseInt(suffix);
        return s + 1;
    }

    private static Map<Integer, MonsterInfo> loadMonsters(Path jsonPath) throws IOException {
        String s = Files.readString(jsonPath, StandardCharsets.UTF_8);
        Map<Integer, MonsterInfo> out = new HashMap<>();

        Matcher objM = MONSTER_OBJ.matcher(s);
        while (objM.find()) {
            String obj = objM.group();
            Integer id = findInt(MONSTER_ID, obj);
            if (id == null) continue;
            String name = findString(MONSTER_NAME, obj);
            if (name == null) name = "(unnamed)";
            Integer exp = findInt(MONSTER_EXP, obj);
            if (exp == null) exp = 0;
            out.put(id, new MonsterInfo(id, unescapeJsonString(name), exp));
        }

        return out;
    }

    private static Integer findInt(Pattern p, String s) {
        Matcher m = p.matcher(s);
        if (!m.find()) return null;
        return Integer.parseInt(m.group(1));
    }

    private static String findString(Pattern p, String s) {
        Matcher m = p.matcher(s);
        if (!m.find()) return null;
        return m.group(1);
    }

    // Minimal JSON string unescape for common escapes used in names
    private static String unescapeJsonString(String in) {
        StringBuilder sb = new StringBuilder(in.length());
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            if (i + 1 >= in.length()) {
                sb.append('\\');
                break;
            }
            char n = in.charAt(++i);
            switch (n) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'u':
                    if (i + 4 < in.length()) {
                        String hex = in.substring(i + 1, i + 5);
                        try {
                            sb.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        } catch (NumberFormatException e) {
                            sb.append("\\u").append(hex);
                            i += 4;
                        }
                    } else {
                        sb.append("\\u");
                    }
                    break;
                default:
                    sb.append(n);
            }
        }
        return sb.toString();
    }

    // ===== Byte/hex helpers =====

    private static int readLeShortAsInt(byte[] b, int off) {
        int lo = b[off] & 0xFF;
        int hi = b[off + 1] & 0xFF;
        short s = (short) (lo | (hi << 8));
        return s; // sign-extended if negative (matches short behavior); fine for your data
    }

    private static byte[] hexToBytes(String hex) {
        String h = hex.trim();
        if ((h.length() & 1) != 0) throw new IllegalArgumentException("Odd hex length: " + h.length());
        byte[] out = new byte[h.length() / 2];
        for (int i = 0; i < out.length; i++) {
            int hi = Character.digit(h.charAt(i * 2), 16);
            int lo = Character.digit(h.charAt(i * 2 + 1), 16);
            if (hi < 0 || lo < 0) throw new IllegalArgumentException("Bad hex at index " + (i * 2));
            out[i] = (byte) ((hi << 4) | lo);
        }
        return out;
    }

    private static String bytesToHex(byte[] b, int off, int len) {
        StringBuilder sb = new StringBuilder(len * 2);
        for (int i = 0; i < len; i++) {
            int v = b[off + i] & 0xFF;
            sb.append(Character.forDigit((v >>> 4) & 0xF, 16));
            sb.append(Character.forDigit(v & 0xF, 16));
        }
        return sb.toString().toUpperCase(Locale.ROOT);
    }

    private static double clamp01(double x) {
        if (x < 0) return 0;
        if (x > 1) return 1;
        return x;
    }
}
