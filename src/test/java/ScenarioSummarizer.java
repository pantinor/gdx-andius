
import andius.Andius;
import andius.WizardryData;
import andius.WizardryData.MazeLevel;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.Reward;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import utils.Utils;

public class ScenarioSummarizer extends InputAdapter implements ApplicationListener {

    // Best-effort mapping from encounterID -> a notable reward item id (typically a quest/key item).
    private java.util.Map<Integer, Integer> encounterRewardItemByEncounterId = java.util.Collections.emptyMap();
    private final Map<Integer, List<SpecialCell>> specials = new HashMap<>();
    private int scenarioMaxLevel = -1;

    private WizardryData.Scenario scenario;
    private List<Node> walkthrough;

    private String[] hudEventByStep = new String[0];
    private int[] hudNextObjectiveItemByStep = new int[0];
    private String[] hudRequiredByStep = new String[0];
    private String[] hudOwnedByStep = new String[0];
    private int walkIdx = 0;
    private float walkAccum = 0f;

    private static final float STEP_SECONDS = 0.5f;
    private static final float HUD_HEIGHT = 180f;
    private static final int LEVEL_HOP_PENALTY = 50;
    private static final int PIT_PENALTY = 500;
    private static final int DAMAGE_PENALTY = 120;

    private OrthographicCamera camera;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout hudLayout;

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "ScenarioSummarizer";

        Graphics.DisplayMode displayMode = LwjglApplicationConfiguration.getDesktopDisplayMode();

        cfg.width = displayMode.width - 20;
        cfg.height = displayMode.height - 100;

        cfg.x = 0;
        cfg.y = 0;

        new LwjglApplication(new ScenarioSummarizer(), cfg);
    }

    @Override
    public void create() {

        Andius a = new Andius();
        a.create();

        WizardryData.Scenario sc = WizardryData.Scenario.PMO;
        this.scenario = sc;
        this.scenarioMaxLevel = sc.levels().length - 1;

        for (MazeLevel lvl : sc.levels()) {

            int level = lvl.level - 1;

            specials.put(level, new ArrayList<>());

            final int dim = lvl.dimension;
            for (int x = 0; x < dim; x++) {
                for (int y = 0; y < dim; y++) {
                    WizardryData.MazeCell c = lvl.cells[x][y];
                    if (!isSpecialCell(c)) {
                        continue;
                    }
                    specials.get(level).add(new SpecialCell(level, x, y, specialTags(sc, level, x, y)));
                }
            }

        }

        for (int lvl : specials.keySet()) {
            System.out.println("Level " + (lvl + 1));
            for (SpecialCell s : specials.get(lvl)) {
                System.out.println("\t" + s);
            }
        }

        List<Node> nodes = findWalkthroughAllSpecials(sc, 0, sc.getStartX(), sc.getStartY(), sc.levels().length - 1);
        this.walkthrough = nodes;
        buildHudAnnotations(sc, nodes);

        if (nodes != null) {
            for (Node n : nodes) {
                System.out.println(n);
            }
        } else {
            System.out.println("No nodes found - exiting.");
        }

        this.camera = new OrthographicCamera();
        this.shapes = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.hudLayout = new GlyphLayout();

        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.camera.update();

        Gdx.input.setInputProcessor(this);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
    }

    private void buildHudAnnotations(WizardryData.Scenario sc, java.util.List<Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            hudEventByStep = new String[0];
            hudNextObjectiveItemByStep = new int[0];
            hudRequiredByStep = new String[0];
            hudOwnedByStep = new String[0];
            return;
        }

        final int n = nodes.size();
        String[] events = new String[n];
        int[] firstItemGainedAt = new int[n];

        final int[] requiredInWalk = collectRequiredItemsInWalk(sc, nodes);
        String[] reqStatus = new String[n];
        String[] ownedStatus = new String[n];

        java.util.HashSet<Integer> owned = new java.util.HashSet<>();

        for (int i = 0; i < n; i++) {
            Node cur = nodes.get(i);
            WizardryData.MazeLevel lvl = sc.levels()[cur.lvl];
            WizardryData.MazeCell c = lvl.cells[cur.x][cur.y];

            java.util.ArrayList<String> msgs = new java.util.ArrayList<>(2);

            if (c != null) {
                if (c.encounterID >= 0) {
                    msgs.add("Encounter #" + c.encounterID);

                    Integer rid = encounterRewardItemByEncounterId.get(c.encounterID);
                    if (rid != null && rid > 0 && !owned.contains(rid)) {
                        owned.add(rid);
                        msgs.add("Obtained (reward) " + itemLabel(sc, rid));
                        if (firstItemGainedAt[i] == 0) {
                            firstItemGainedAt[i] = rid;
                        }
                    }
                }

                if (c.wanderingEncounterID >= 0) {
                    msgs.add("Wandering encounter #" + c.wanderingEncounterID);
                }
                if (c.pit) {
                    msgs.add("Pit");
                }
                if (c.damage != null) {
                    msgs.add("Damage");
                }

                if (c.itemRequired > 0) {
                    if (owned.contains(c.itemRequired)) {
                        msgs.add("Used obtained " + itemLabel(sc, c.itemRequired) + " to pass gate");
                    } else {
                        msgs.add("Gate requires " + itemLabel(sc, c.itemRequired));
                    }
                }

                if (c.itemObtained > 0 && !owned.contains(c.itemObtained)) {
                    owned.add(c.itemObtained);
                    msgs.add("Obtained " + itemLabel(sc, c.itemObtained));
                    if (firstItemGainedAt[i] == 0) {
                        firstItemGainedAt[i] = c.itemObtained;
                    }
                }

                if (c.itemObtainedFromRiddle > 0 && !owned.contains(c.itemObtainedFromRiddle)) {
                    owned.add(c.itemObtainedFromRiddle);
                    msgs.add("Obtained (riddle) " + itemLabel(sc, c.itemObtainedFromRiddle));
                    if (firstItemGainedAt[i] == 0) {
                        firstItemGainedAt[i] = c.itemObtainedFromRiddle;
                    }
                }

                if (c.encounterGiveItem > 0 && !owned.contains(c.encounterGiveItem)) {
                    boolean ok = true;
                    if (c.encounterTakeItem > 0 && !owned.contains(c.encounterTakeItem)) {
                        ok = false;
                    }
                    if (ok) {
                        if (c.encounterTakeItem > 0 && owned.contains(c.encounterTakeItem)) {
                            owned.remove(c.encounterTakeItem);
                            msgs.add("Used " + itemLabel(sc, c.encounterTakeItem) + " (consumed)");
                        }
                        owned.add(c.encounterGiveItem);
                        msgs.add("Obtained (encounter) " + itemLabel(sc, c.encounterGiveItem));
                        if (firstItemGainedAt[i] == 0) {
                            firstItemGainedAt[i] = c.encounterGiveItem;
                        }
                    }
                } else if (c.encounterTakeItem > 0 && owned.contains(c.encounterTakeItem) && c.encounterGiveItem <= 0) {
                    owned.remove(c.encounterTakeItem);
                    msgs.add("Used " + itemLabel(sc, c.encounterTakeItem) + " (consumed)");
                }

                if (c.tradeItem2 > 0 && !owned.contains(c.tradeItem2)) {
                    boolean ok = true;
                    if (c.tradeItem1 > 0 && !owned.contains(c.tradeItem1)) {
                        ok = false;
                    }
                    if (ok) {
                        if (c.tradeItem1 > 0 && owned.contains(c.tradeItem1)) {
                            owned.remove(c.tradeItem1);
                            msgs.add("Traded " + itemLabel(sc, c.tradeItem1));
                        }
                        owned.add(c.tradeItem2);
                        msgs.add("Obtained (trade) " + itemLabel(sc, c.tradeItem2));
                        if (firstItemGainedAt[i] == 0) {
                            firstItemGainedAt[i] = c.tradeItem2;
                        }
                    }
                }
            }

            events[i] = msgs.isEmpty() ? "" : String.join("   |   ", msgs);

            // Persistent item status lines for the HUD (required gates + current inventory)
            if (requiredInWalk.length == 0) {
                reqStatus[i] = "Required items: none";
            } else {
                java.util.ArrayList<String> status = new java.util.ArrayList<>(requiredInWalk.length);
                int have = 0;
                for (int rid : requiredInWalk) {
                    boolean haveIt = owned.contains(rid);
                    if (haveIt) {
                        have++;
                    }
                    status.add((haveIt ? "[x] " : "[ ] ") + itemShortLabel(sc, rid));
                }
                reqStatus[i] = String.format("Required items (%d/%d): %s", have, requiredInWalk.length, joinLimited(status, 8));
            }

            if (owned.isEmpty()) {
                ownedStatus[i] = "Owned items: (none)";
            } else {
                java.util.ArrayList<Integer> ownedIds = new java.util.ArrayList<>(owned);
                java.util.Collections.sort(ownedIds);
                java.util.ArrayList<String> names = new java.util.ArrayList<>(ownedIds.size());
                for (Integer oid : ownedIds) {
                    names.add(itemShortLabel(sc, oid.intValue()));
                }
                ownedStatus[i] = "Owned items (" + owned.size() + "): " + joinLimited(names, 10);
            }
        }

        int[] nextObjective = new int[n];
        int nextId = 0;
        for (int i = n - 1; i >= 0; i--) {
            nextObjective[i] = nextId;
            if (firstItemGainedAt[i] != 0) {
                nextId = firstItemGainedAt[i];
            }
        }

        hudEventByStep = events;
        hudNextObjectiveItemByStep = nextObjective;
        hudRequiredByStep = reqStatus;
        hudOwnedByStep = ownedStatus;
    }

    private java.util.List<Node> findWalkthroughAllSpecials(WizardryData.Scenario sc, int startLevelIdx, int startX, int startY, int targetLevelIdx) {

        final int L = sc.levels().length;
        final int dim = sc.levels()[0].dimension;

        // ---------- Gather items (required + obtainable) across all levels ----------
        java.util.LinkedHashSet<Integer> requiredItems = new java.util.LinkedHashSet<>();
        java.util.LinkedHashSet<Integer> obtainableItems = new java.util.LinkedHashSet<>();

        for (int lvlIdx = startLevelIdx; lvlIdx <= targetLevelIdx && lvlIdx < L; lvlIdx++) {
            WizardryData.MazeLevel lvl = sc.levels()[lvlIdx];
            for (int x = 0; x < dim; x++) {
                for (int y = 0; y < dim; y++) {
                    WizardryData.MazeCell c = lvl.cells[x][y];
                    if (c == null) {
                        continue;
                    }
                    if (c.itemRequired > 0) {
                        requiredItems.add(c.itemRequired);
                    }
                    if (c.itemObtained > 0) {
                        obtainableItems.add(c.itemObtained);
                    }
                    if (c.itemObtainedFromRiddle > 0) {
                        obtainableItems.add(c.itemObtainedFromRiddle);
                    }
                    if (c.encounterGiveItem > 0) {
                        obtainableItems.add(c.encounterGiveItem);
                    }
                    if (c.tradeItem2 > 0) {
                        obtainableItems.add(c.tradeItem2);
                    }
                    // Inputs that may be consumed.
                    if (c.tradeItem1 > 0) {
                        obtainableItems.add(c.tradeItem1);
                    }
                    if (c.encounterTakeItem > 0) {
                        obtainableItems.add(c.encounterTakeItem);
                    }
                }
            }
        }

        this.encounterRewardItemByEncounterId = buildEncounterRewardItemByEncounterId(sc);
        obtainableItems.addAll(this.encounterRewardItemByEncounterId.values());

        // ---------- Bit layout for item possession ----------
        int xyBits = bitsNeeded(dim);
        int lvlBits = bitsNeeded(L);
        int metaBits = (xyBits * 2) + lvlBits;
        int maxMaskBits = 63 - metaBits;
        if (maxMaskBits <= 0) {
            return null;
        }

        java.util.HashMap<Integer, Integer> bitByItem = new java.util.HashMap<>();
        int bit = 0;

        for (Integer id : requiredItems) {
            if (bit >= maxMaskBits) {
                System.out.println("Too many required items for 64-bit mask; cannot build walkthrough.");
                return null;
            }
            if (!bitByItem.containsKey(id)) {
                bitByItem.put(id, bit++);
            }
        }
        for (Integer id : obtainableItems) {
            if (bit >= maxMaskBits) {
                break;
            }
            if (!bitByItem.containsKey(id)) {
                bitByItem.put(id, bit++);
            }
        }

        java.util.HashSet<Integer> remaining = new java.util.HashSet<>();
        for (int lvlIdx = startLevelIdx; lvlIdx <= targetLevelIdx && lvlIdx < L; lvlIdx++) {
            java.util.List<SpecialCell> list = specials.get(lvlIdx);
            if (list == null) {
                continue;
            }
            for (SpecialCell s : list) {
                remaining.add(specialKey(s.lvl, s.x, s.y, dim));
            }
        }

        java.util.ArrayList<Node> walk = new java.util.ArrayList<>();

        final java.util.Map<Integer, Integer> emptyGateBits = java.util.Collections.emptyMap();
        long curMask = 0L;
        curMask = applyPickupMask(sc, startLevelIdx, startX, startY, curMask, bitByItem, emptyGateBits, xyBits, lvlBits);

        int curLvl = startLevelIdx;
        int curX = startX;
        int curY = startY;
        int totalCost = 0;

        walk.add(new Node(curLvl, curX, curY, curMask, totalCost));
        remaining.remove(specialKey(curLvl, curX, curY, dim));

        int stuckCount = 0;
        final int MAX_STUCK = 50; // avoid infinite loops on truly unreachable objectives

        while (!remaining.isEmpty() && stuckCount < MAX_STUCK) {
            DijkstraResult toNext = dijkstraToAnySpecial(sc, curLvl, curX, curY, curMask, remaining, bitByItem, xyBits, lvlBits);

            if (toNext != null && toNext.path != null && toNext.path.size() > 1) {
                // Append with cost offset and consume any visited specials along the subpath.
                for (int i = 1; i < toNext.path.size(); i++) {
                    Node n = toNext.path.get(i);
                    int absCost = totalCost + n.cost;
                    walk.add(new Node(n.lvl, n.x, n.y, n.mask, absCost));
                    remaining.remove(specialKey(n.lvl, n.x, n.y, dim));
                }

                Node last = walk.get(walk.size() - 1);
                totalCost = last.cost;
                curLvl = last.lvl;
                curX = last.x;
                curY = last.y;
                curMask = last.mask;
                stuckCount = 0;
                continue;
            }

            // No reachable special from here with the current inventory.
            // Keep the "malor"-style fallback descent so we can push deeper and (hopefully) pick up gating items.
            stuckCount++;

            if (curLvl < targetLevelIdx) {
                int[] sd = findBestStairsDown(sc, curLvl, dim, targetLevelIdx);
                if (sd == null) {
                    break;
                }

                int stairsX = sd[0], stairsY = sd[1];
                int toLvl = sd[2], toX = sd[3], toY = sd[4];

                // Teleport to stairs square (bypass walls/doors). This is the “malor” fallback.
                totalCost += LEVEL_HOP_PENALTY;
                curMask = applyPickupMask(sc, curLvl, stairsX, stairsY, curMask, bitByItem, emptyGateBits, xyBits, lvlBits);
                walk.add(new Node(curLvl, stairsX, stairsY, curMask, totalCost));
                remaining.remove(specialKey(curLvl, stairsX, stairsY, dim));

                // Take stairs down.
                totalCost += 1 + LEVEL_HOP_PENALTY + hazardPenalty(sc, toLvl, toX, toY);
                curLvl = toLvl;
                curX = toX;
                curY = toY;
                curMask = applyPickupMask(sc, curLvl, curX, curY, curMask, bitByItem, emptyGateBits, xyBits, lvlBits);
                walk.add(new Node(curLvl, curX, curY, curMask, totalCost));
                remaining.remove(specialKey(curLvl, curX, curY, dim));
            } else {
                break;
            }
        }

        // Ensure the final walkthrough ends on the last level
        if (curLvl != targetLevelIdx) {
            DijkstraResult toLast = dijkstraToLevel(sc, curLvl, curX, curY, curMask, targetLevelIdx, bitByItem, xyBits, lvlBits);
            if (toLast != null && toLast.path != null && toLast.path.size() > 1) {
                for (int i = 1; i < toLast.path.size(); i++) {
                    Node n = toLast.path.get(i);
                    int absCost = totalCost + n.cost;
                    walk.add(new Node(n.lvl, n.x, n.y, n.mask, absCost));
                }
            } else {
                // Last-ditch: just force descend with the same fallback as above.
                while (curLvl < targetLevelIdx) {
                    int[] sd = findBestStairsDown(sc, curLvl, dim, targetLevelIdx);
                    if (sd == null) {
                        break;
                    }

                    int stairsX = sd[0], stairsY = sd[1];
                    int toLvl = sd[2], toX = sd[3], toY = sd[4];

                    totalCost += LEVEL_HOP_PENALTY;
                    curMask = applyPickupMask(sc, curLvl, stairsX, stairsY, curMask, bitByItem, emptyGateBits, xyBits, lvlBits);
                    walk.add(new Node(curLvl, stairsX, stairsY, curMask, totalCost));

                    totalCost += 1 + LEVEL_HOP_PENALTY + hazardPenalty(sc, toLvl, toX, toY);
                    curLvl = toLvl;
                    curX = toX;
                    curY = toY;
                    curMask = applyPickupMask(sc, curLvl, curX, curY, curMask, bitByItem, emptyGateBits, xyBits, lvlBits);
                    walk.add(new Node(curLvl, curX, curY, curMask, totalCost));
                }
            }
        }

        return walk;
    }

    private static final class DijkstraResult {

        final java.util.List<Node> path;

        DijkstraResult(java.util.List<Node> path) {
            this.path = path;
        }
    }

    private DijkstraResult dijkstraToAnySpecial(
            WizardryData.Scenario sc,
            int startLvl, int startX, int startY,
            long startMask,
            java.util.Set<Integer> remainingSpecialKeys,
            java.util.Map<Integer, Integer> bitByItem,
            int xyBits, int lvlBits) {

        final int L = sc.levels().length;
        final int dim = sc.levels()[0].dimension;
        final java.util.Map<Integer, Integer> emptyGateBits = java.util.Collections.emptyMap();

        java.util.HashMap<Long, Integer> dist = new java.util.HashMap<>();
        java.util.HashMap<Long, Long> prev = new java.util.HashMap<>();

        java.util.PriorityQueue<Node> pq = new java.util.PriorityQueue<>(
                java.util.Comparator.comparingInt(n -> n.cost));

        long startState = packState(startLvl, startX, startY, startMask, xyBits, lvlBits);
        dist.put(startState, 0);
        pq.add(new Node(startLvl, startX, startY, startMask, 0));

        // dir: 0=N, 1=S, 2=W, 3=E
        final int[] dx = {+1, -1, 0, 0};  // row
        final int[] dy = {0, 0, -1, +1};  // col

        long goalState = -1L;

        while (!pq.isEmpty()) {
            Node cur = pq.poll();

            long curState = packState(cur.lvl, cur.x, cur.y, cur.mask, xyBits, lvlBits);
            Integer known = dist.get(curState);
            if (known == null || cur.cost != known) {
                continue;
            }

            if (remainingSpecialKeys.contains(specialKey(cur.lvl, cur.x, cur.y, dim))) {
                goalState = curState;
                break;
            }

            WizardryData.MazeCell c = sc.levels()[cur.lvl].cells[cur.x][cur.y];
            if (c == null) {
                continue;
            }

            // 1) normal moves
            for (int dir = 0; dir < 4; dir++) {
                int nx = cur.x + dx[dir];
                int ny = cur.y + dy[dir];
                if (nx < 0 || ny < 0 || nx >= dim || ny >= dim) {
                    continue;
                }
                if (!passableDir(c, dir)) {
                    continue;
                }
                if (!canEnter(sc, cur.lvl, nx, ny, cur.mask, bitByItem)) {
                    continue;
                }

                long nmask = applyPickupMask(sc, cur.lvl, nx, ny, cur.mask, bitByItem, emptyGateBits, xyBits, lvlBits);
                int ncost = cur.cost + 1 + hazardPenalty(sc, cur.lvl, nx, ny);

                long nstate = packState(cur.lvl, nx, ny, nmask, xyBits, lvlBits);
                Integer old = dist.get(nstate);
                if (old == null || ncost < old) {
                    dist.put(nstate, ncost);
                    prev.put(nstate, curState);
                    pq.add(new Node(cur.lvl, nx, ny, nmask, ncost));
                }
            }

            // 2) special transitions out of this cell
            if (c.teleport || c.stairs || c.chute) {
                WizardryData.MazeAddress to = c.addressTo;
                if (to != null && to.level > 0) {
                    int toLvlIdx = to.level - 1;
                    if (toLvlIdx >= 0 && toLvlIdx < L) {
                        int nx = to.row, ny = to.column;
                        if (nx >= 0 && ny >= 0 && nx < dim && ny < dim
                                && canEnter(sc, toLvlIdx, nx, ny, cur.mask, bitByItem)) {

                            long nmask = applyPickupMask(sc, toLvlIdx, nx, ny, cur.mask, bitByItem, emptyGateBits, xyBits, lvlBits);
                            int ncost = cur.cost + 1 + LEVEL_HOP_PENALTY + hazardPenalty(sc, toLvlIdx, nx, ny);

                            long nstate = packState(toLvlIdx, nx, ny, nmask, xyBits, lvlBits);
                            Integer old = dist.get(nstate);
                            if (old == null || ncost < old) {
                                dist.put(nstate, ncost);
                                prev.put(nstate, curState);
                                pq.add(new Node(toLvlIdx, nx, ny, nmask, ncost));
                            }
                        }
                    }
                }
            }

            if (c.elevator) {
                int lo = Math.min(c.elevatorFrom, c.elevatorTo);
                int hi = Math.max(c.elevatorFrom, c.elevatorTo);

                for (int raw = lo; raw <= hi; raw++) {
                    if (raw <= 0) {
                        continue;
                    }
                    int toLvlIdx = raw - 1;
                    if (toLvlIdx < 0 || toLvlIdx >= L) {
                        continue;
                    }
                    int nx = cur.x, ny = cur.y;
                    if (!canEnter(sc, toLvlIdx, nx, ny, cur.mask, bitByItem)) {
                        continue;
                    }

                    long nmask = applyPickupMask(sc, toLvlIdx, nx, ny, cur.mask, bitByItem, emptyGateBits, xyBits, lvlBits);
                    int ncost = cur.cost + 1 + LEVEL_HOP_PENALTY + hazardPenalty(sc, toLvlIdx, nx, ny);

                    long nstate = packState(toLvlIdx, nx, ny, nmask, xyBits, lvlBits);
                    Integer old = dist.get(nstate);
                    if (old == null || ncost < old) {
                        dist.put(nstate, ncost);
                        prev.put(nstate, curState);
                        pq.add(new Node(toLvlIdx, nx, ny, nmask, ncost));
                    }
                }
            }
        }

        if (goalState < 0) {
            return null;
        }

        java.util.ArrayList<Node> path = new java.util.ArrayList<>();
        long curState = goalState;
        while (true) {
            int[] u = unpackState(curState, xyBits, lvlBits);
            long mask = unpackMask(curState, xyBits, lvlBits);
            int cost = dist.get(curState);
            path.add(new Node(u[0], u[1], u[2], mask, cost));

            Long p = prev.get(curState);
            if (p == null) {
                break;
            }
            curState = p.longValue();
        }

        java.util.Collections.reverse(path);
        return new DijkstraResult(path);
    }

    private DijkstraResult dijkstraToLevel(
            WizardryData.Scenario sc,
            int startLvl, int startX, int startY,
            long startMask,
            int targetLevelIdx,
            java.util.Map<Integer, Integer> bitByItem,
            int xyBits, int lvlBits) {

        final int L = sc.levels().length;
        final int dim = sc.levels()[0].dimension;
        final java.util.Map<Integer, Integer> emptyGateBits = java.util.Collections.emptyMap();

        java.util.HashMap<Long, Integer> dist = new java.util.HashMap<>();
        java.util.HashMap<Long, Long> prev = new java.util.HashMap<>();

        java.util.PriorityQueue<Node> pq = new java.util.PriorityQueue<>(
                java.util.Comparator.comparingInt(n -> n.cost));

        long startState = packState(startLvl, startX, startY, startMask, xyBits, lvlBits);
        dist.put(startState, 0);
        pq.add(new Node(startLvl, startX, startY, startMask, 0));

        // dir: 0=N, 1=S, 2=W, 3=E
        final int[] dx = {+1, -1, 0, 0};
        final int[] dy = {0, 0, -1, +1};

        long goalState = -1L;

        while (!pq.isEmpty()) {
            Node cur = pq.poll();

            long curState = packState(cur.lvl, cur.x, cur.y, cur.mask, xyBits, lvlBits);
            Integer known = dist.get(curState);
            if (known == null || cur.cost != known) {
                continue;
            }

            if (cur.lvl == targetLevelIdx) {
                goalState = curState;
                break;
            }

            WizardryData.MazeCell c = sc.levels()[cur.lvl].cells[cur.x][cur.y];
            if (c == null) {
                continue;
            }

            for (int dir = 0; dir < 4; dir++) {
                int nx = cur.x + dx[dir];
                int ny = cur.y + dy[dir];
                if (nx < 0 || ny < 0 || nx >= dim || ny >= dim) {
                    continue;
                }
                if (!passableDir(c, dir)) {
                    continue;
                }
                if (!canEnter(sc, cur.lvl, nx, ny, cur.mask, bitByItem)) {
                    continue;
                }

                long nmask = applyPickupMask(sc, cur.lvl, nx, ny, cur.mask, bitByItem, emptyGateBits, xyBits, lvlBits);
                int ncost = cur.cost + 1 + hazardPenalty(sc, cur.lvl, nx, ny);

                long nstate = packState(cur.lvl, nx, ny, nmask, xyBits, lvlBits);
                Integer old = dist.get(nstate);
                if (old == null || ncost < old) {
                    dist.put(nstate, ncost);
                    prev.put(nstate, curState);
                    pq.add(new Node(cur.lvl, nx, ny, nmask, ncost));
                }
            }

            if (c.teleport || c.stairs || c.chute) {
                WizardryData.MazeAddress to = c.addressTo;
                if (to != null && to.level > 0) {
                    int toLvlIdx = to.level - 1;
                    if (toLvlIdx >= 0 && toLvlIdx < L) {
                        int nx = to.row, ny = to.column;
                        if (nx >= 0 && ny >= 0 && nx < dim && ny < dim
                                && canEnter(sc, toLvlIdx, nx, ny, cur.mask, bitByItem)) {

                            long nmask = applyPickupMask(sc, toLvlIdx, nx, ny, cur.mask, bitByItem, emptyGateBits, xyBits, lvlBits);
                            int ncost = cur.cost + 1 + LEVEL_HOP_PENALTY + hazardPenalty(sc, toLvlIdx, nx, ny);

                            long nstate = packState(toLvlIdx, nx, ny, nmask, xyBits, lvlBits);
                            Integer old = dist.get(nstate);
                            if (old == null || ncost < old) {
                                dist.put(nstate, ncost);
                                prev.put(nstate, curState);
                                pq.add(new Node(toLvlIdx, nx, ny, nmask, ncost));
                            }
                        }
                    }
                }
            }

            if (c.elevator) {
                int lo = Math.min(c.elevatorFrom, c.elevatorTo);
                int hi = Math.max(c.elevatorFrom, c.elevatorTo);
                for (int raw = lo; raw <= hi; raw++) {
                    if (raw <= 0) {
                        continue;
                    }
                    int toLvlIdx = raw - 1;
                    if (toLvlIdx < 0 || toLvlIdx >= L) {
                        continue;
                    }
                    int nx = cur.x, ny = cur.y;
                    if (!canEnter(sc, toLvlIdx, nx, ny, cur.mask, bitByItem)) {
                        continue;
                    }
                    long nmask = applyPickupMask(sc, toLvlIdx, nx, ny, cur.mask, bitByItem, emptyGateBits, xyBits, lvlBits);
                    int ncost = cur.cost + 1 + LEVEL_HOP_PENALTY + hazardPenalty(sc, toLvlIdx, nx, ny);
                    long nstate = packState(toLvlIdx, nx, ny, nmask, xyBits, lvlBits);
                    Integer old = dist.get(nstate);
                    if (old == null || ncost < old) {
                        dist.put(nstate, ncost);
                        prev.put(nstate, curState);
                        pq.add(new Node(toLvlIdx, nx, ny, nmask, ncost));
                    }
                }
            }
        }

        if (goalState < 0) {
            return null;
        }

        java.util.ArrayList<Node> path = new java.util.ArrayList<>();
        long curState = goalState;
        while (true) {
            int[] u = unpackState(curState, xyBits, lvlBits);
            long mask = unpackMask(curState, xyBits, lvlBits);
            int cost = dist.get(curState);
            path.add(new Node(u[0], u[1], u[2], mask, cost));

            Long p = prev.get(curState);
            if (p == null) {
                break;
            }
            curState = p.longValue();
        }
        java.util.Collections.reverse(path);
        return new DijkstraResult(path);
    }

    private static boolean isSpecialCell(WizardryData.MazeCell c) {
        if (c == null) {
            return false;
        }
        if (c.rock) {
            return false;
        }

        // “Special” in the sense of “interesting for a walkthrough”.
        return c.stairs
                || c.teleport
                || c.chute
                || c.elevator
                //|| c.pit
                //|| c.spinner
                //|| c.cage
                //|| c.darkness
                //|| c.spellsBlocked
                //|| (c.damage != null)
                || (c.message != null)
                || (c.function != null)
                || (c.feeAmount > 0)
                || (c.itemRequired > 0)
                || (c.itemObtained > 0)
                || (c.itemObtainedFromRiddle > 0)
                || (c.encounterID >= 0)
                //|| (c.wanderingEncounterID >= 0)
                || (c.encounterGiveItem > 0)
                || (c.tradeItem1 > 0)
                || (c.tradeItem2 > 0)
                || (c.fountainType >= 0)
                || (c.markType >= 0)
                || (c.chestType >= 0)
                //|| c.lair
                //|| c.hasTreasureChest
                || (c.summoningCircle != null);
    }

    private String specialTags(WizardryData.Scenario sc, int lvlIdx, int x, int y) {
        WizardryData.MazeCell c = sc.levels()[lvlIdx].cells[x][y];
        if (c == null) {
            return "";
        }
        java.util.ArrayList<String> tags = new java.util.ArrayList<>(4);

        if (c.itemObtained > 0) {
            tags.add("get:" + itemLabel(sc, c.itemObtained));
        }
        if (c.itemObtainedFromRiddle > 0) {
            tags.add("riddle:" + itemLabel(sc, c.itemObtainedFromRiddle));
        }
        if (c.itemRequired > 0) {
            tags.add("gate:" + itemLabel(sc, c.itemRequired));
        }
        if (c.encounterID >= 0) {
            tags.add("encounter" + (c.encounterID >= 0 ? ("#" + c.encounterID) : ""));
        }
        if (c.wanderingEncounterID >= 0) {
            tags.add("wander#" + c.wanderingEncounterID);
        }
        if (c.encounterGiveItem > 0) {
            tags.add("enc+" + itemLabel(sc, c.encounterGiveItem));
        }
        if (c.tradeItem2 > 0) {
            tags.add("trade" + (c.tradeItem1 > 0 ? (":" + itemLabel(sc, c.tradeItem1) + "→" + itemLabel(sc, c.tradeItem2))
                    : ("→" + itemLabel(sc, c.tradeItem2))));
        }
        if (c.stairs) {
            tags.add("stairs");
        }
        if (c.teleport) {
            tags.add("teleport");
        }
        if (c.chute) {
            tags.add("chute");
        }
        if (c.elevator) {
            tags.add("elevator");
        }
        if (c.pit) {
            tags.add("pit");
        }
        if (c.damage != null) {
            tags.add("damage");
        }
        if (c.darkness) {
            tags.add("dark");
        }
        if (c.spellsBlocked) {
            tags.add("no-spells");
        }

        return String.join(",", tags);
    }

    private static final class SpecialCell {

        final int lvl;
        final int x;
        final int y;
        final String tags;

        SpecialCell(int lvl, int x, int y, String tags) {
            this.lvl = lvl;
            this.x = x;
            this.y = y;
            this.tags = tags;
        }

        @Override
        public String toString() {
            return String.format("(L%d @ %d,%d) %s", (lvl + 1), x, y, tags);
        }
    }

    private static int specialKey(int lvl, int x, int y, int dim) {
        return (lvl * dim * dim) + (x * dim) + y;
    }

    private int hazardPenalty(WizardryData.Scenario sc, int lvlIdx, int x, int y) {
        WizardryData.MazeCell c = sc.levels()[lvlIdx].cells[x][y];
        if (c == null) {
            return 0;
        }
        int p = 0;
        if (c.pit) {
            p += PIT_PENALTY;
        }
        if (c.damage != null) {
            p += DAMAGE_PENALTY;
        }
        if (c.darkness) {
            p += 5;
        }
        if (c.spellsBlocked) {
            p += 5;
        }
        return p;
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (scenario == null || walkthrough == null || walkthrough.isEmpty()) {
            return;
        }

        if (walkIdx < walkthrough.size() - 1) {
            float dt = Gdx.graphics.getDeltaTime();
            walkAccum += dt;
            while (walkAccum >= STEP_SECONDS && walkIdx < walkthrough.size() - 1) {
                walkAccum -= STEP_SECONDS;
                walkIdx++;
            }
        }

        Node cur = walkthrough.get(walkIdx);
        MazeLevel lvl = scenario.levels()[cur.lvl];
        int dim = lvl.dimension;

        float pad = 20f;
        float availW = Gdx.graphics.getWidth() - 2f * pad;
        float availH = Gdx.graphics.getHeight() - 2f * pad - HUD_HEIGHT;
        float cell = Math.max(1f, Math.min(availW, availH) / dim);
        float gridW = cell * dim;
        float gridH = cell * dim;
        float ox = (Gdx.graphics.getWidth() - gridW) / 2f;
        float oy = pad;

        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shapes.begin(ShapeType.Filled);
        for (int x = 0; x < dim; x++) {           // x = row (north/south)
            for (int y = 0; y < dim; y++) {       // y = col (east/west)
                WizardryData.MazeCell c = lvl.cells[x][y];
                boolean rock = (c == null) || c.rock;

                shapes.setColor(rock ? Color.DARK_GRAY : Color.valueOf("1A1A1A"));

                // Y-up: screenY = x, screenX = y
                float px = ox + y * cell;
                float py = oy + x * cell;
                shapes.rect(px, py, cell, cell);
            }
        }
        shapes.end();

        float doorLen = cell * 0.45f;
        float doorOff = (cell - doorLen) * 0.5f;

        shapes.begin(ShapeType.Line);

        for (int x = 0; x < dim; x++) {
            for (int y = 0; y < dim; y++) {
                WizardryData.MazeCell c = lvl.cells[x][y];
                if (c == null) {
                    continue;
                }

                float left = ox + y * cell;
                float bottom = oy + x * cell;
                float right = left + cell;
                float top = bottom + cell;

                float cx = left + cell * 0.5f;
                float cy = bottom + cell * 0.5f;
                float r = cell * 0.15f;
                float m = cell * 0.22f;

                shapes.setColor(Color.LIGHT_GRAY);

                if (c.northWall) {
                    shapes.line(left, top, right, top);
                }
                if (c.southWall) {
                    shapes.line(left, bottom, right, bottom);
                }
                if (c.westWall) {
                    shapes.line(left, bottom, left, top);
                }
                if (c.eastWall) {
                    shapes.line(right, bottom, right, top);
                }

                if (c.northDoor) {
                    shapes.setColor(c.hiddenNorthDoor ? Color.ORANGE : Color.GREEN);
                    shapes.line(left + doorOff, top, left + doorOff + doorLen, top);
                }
                if (c.southDoor) {
                    shapes.setColor(c.hiddenSouthDoor ? Color.ORANGE : Color.GREEN);
                    shapes.line(left + doorOff, bottom, left + doorOff + doorLen, bottom);
                }
                if (c.westDoor) {
                    shapes.setColor(c.hiddenWestDoor ? Color.ORANGE : Color.GREEN);
                    shapes.line(left, bottom + doorOff, left, bottom + doorOff + doorLen);
                }
                if (c.eastDoor) {
                    shapes.setColor(c.hiddenEastDoor ? Color.ORANGE : Color.GREEN);
                    shapes.line(right, bottom + doorOff, right, bottom + doorOff + doorLen);
                }

                if (c.stairs) {
                    shapes.setColor(Color.PURPLE);
                    int toLvlIdx = (c.addressTo != null && c.addressTo.level > 0)
                            ? (c.addressTo.level - 1)
                            : cur.lvl;
                    if (toLvlIdx > cur.lvl) {
                        shapes.triangle(
                                cx, bottom + m, // apex (down)
                                left + m, top - m, // base-left
                                right - m, top - m // base-right
                        );
                    } else if (toLvlIdx < cur.lvl) {
                        shapes.triangle(
                                cx, top - m, // apex (up)
                                left + m, bottom + m,// base-left
                                right - m, bottom + m// base-right
                        );
                    } else {
                        shapes.triangle(cx, top - m, cx - (cell * 0.22f), cy, cx + (cell * 0.22f), cy);
                        shapes.triangle(cx, bottom + m, cx - (cell * 0.22f), cy, cx + (cell * 0.22f), cy);
                    }
                }

                if (c.elevator) {
                    shapes.setColor(Color.VIOLET);
                    shapes.triangle(cx, top - m, cx - (cell * 0.22f), cy, cx + (cell * 0.22f), cy);
                    shapes.triangle(cx, bottom + m, cx - (cell * 0.22f), cy, cx + (cell * 0.22f), cy);
                }
                if (c.message != null) {
                    shapes.setColor(Color.YELLOW);
                    shapes.circle(cx, cy, r, 20);
                }
                if (c.teleport) {
                    shapes.setColor(Color.FOREST);
                    shapes.circle(cx, cy, r, 20);
                }
                if (c.itemRequired > 0) {
                    shapes.setColor(Color.BLUE);
                    shapes.circle(cx, cy, r, 20);
                }
                if (c.itemObtained > 0 || c.itemObtainedFromRiddle > 0) {
                    shapes.setColor(Color.SKY);
                    shapes.circle(cx, cy, r, 20);
                }
                if (c.encounterID > 0 || c.encounterGiveItem > 0 || c.encounterTakeItem > 0) {
                    shapes.setColor(Color.RED);
                    shapes.circle(cx, cy, r, 20);
                }
            }
        }

        shapes.end();

        // ---------- Animated current position ----------
        float fx = cur.x;
        float fy = cur.y;

        if (walkIdx < walkthrough.size() - 1) {
            Node nxt = walkthrough.get(walkIdx + 1);
            boolean sameLvl = (nxt.lvl == cur.lvl);
            int md = Math.abs(nxt.x - cur.x) + Math.abs(nxt.y - cur.y);
            if (sameLvl && md == 1) {
                float t = walkAccum / STEP_SECONDS;
                fx = cur.x + (nxt.x - cur.x) * t;
                fy = cur.y + (nxt.y - cur.y) * t;
            }
        }

        float cx = ox + (fy + 0.5f) * cell; // x-axis is column (y)
        float cy = oy + (fx + 0.5f) * cell; // y-axis is row (x)

        shapes.begin(ShapeType.Filled);
        shapes.setColor(Color.YELLOW);
        float r = Math.max(3f, cell * 0.22f);
        shapes.circle(cx, cy, r, 20);
        shapes.end();

        // ---------- HUD ----------
        batch.begin();
        font.setColor(Color.WHITE);

        String hud1 = String.format(
                "Maze level: %d/%d   Node: %d/%d   Pos(row=x,col=y): (%d,%d)   Mask: 0x%X",
                (cur.lvl + 1), (scenarioMaxLevel + 1),
                walkIdx, (walkthrough.size() - 1),
                cur.x, cur.y, cur.mask
        );

        int nextItemId = (hudNextObjectiveItemByStep != null && walkIdx < hudNextObjectiveItemByStep.length)
                ? hudNextObjectiveItemByStep[walkIdx] : 0;
        String hud2 = "Next objective item: " + itemLabel(scenario, nextItemId);

        String hudReq = (hudRequiredByStep != null && walkIdx < hudRequiredByStep.length)
                ? hudRequiredByStep[walkIdx] : "";

        String hudOwned = (hudOwnedByStep != null && walkIdx < hudOwnedByStep.length)
                ? hudOwnedByStep[walkIdx] : "";

        String hud3 = (hudEventByStep != null && walkIdx < hudEventByStep.length)
                ? hudEventByStep[walkIdx] : "";

        float topY = Gdx.graphics.getHeight() - pad;
        float lh = font.getLineHeight();
        float maxW = Gdx.graphics.getWidth() - 2f * pad;

        font.draw(batch, fitHudLine(hud1, maxW), pad, topY);
        font.draw(batch, fitHudLine(hud2, maxW), pad, topY - lh);

        if (hudReq != null && !hudReq.isEmpty()) {
            font.draw(batch, fitHudLine(hudReq, maxW), pad, topY - 2f * lh);
        }
        if (hudOwned != null && !hudOwned.isEmpty()) {
            font.draw(batch, fitHudLine(hudOwned, maxW), pad, topY - 3f * lh);
        }

        if (hud3 != null && !hud3.isEmpty()) {
            font.draw(batch, fitHudLine(hud3, maxW), pad, topY - 4f * lh);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (camera != null) {
            camera.setToOrtho(false, width, height);
            camera.update();
        }
    }

    @Override
    public void dispose() {
        if (shapes != null) {
            shapes.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
        if (font != null) {
            font.dispose();
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    private static final class Node {

        final int lvl, x, y;
        final long mask;
        final int cost;

        Node(int lvl, int x, int y, long mask, int cost) {
            this.lvl = lvl;
            this.x = x;
            this.y = y;
            this.mask = mask;
            this.cost = cost;
        }

        @Override
        public String toString() {
            return "Node{" + "lvl=" + lvl + ", x=" + x + ", y=" + y + ", mask=0x"
                    + java.lang.Long.toHexString(mask) + ", cost=" + cost + '}';
        }

    }

    private static boolean passableDir(WizardryData.MazeCell c, int dir) {
        switch (dir) {
            case 0:
                return !(c.northWall && !c.northDoor); // N
            case 1:
                return !(c.southWall && !c.southDoor); // S
            case 2:
                return !(c.westWall && !c.westDoor);  // W
            case 3:
                return !(c.eastWall && !c.eastDoor);  // E
            default:
                return false;
        }
    }

    private static int bitOrNeg(java.util.Map<Integer, Integer> bitByItem, int itemId) {
        Integer b = bitByItem.get(itemId);
        return (b == null) ? -1 : b.intValue();
    }

    private static int bitsNeeded(int n) {
        // smallest b such that 2^b >= n
        int b = 0;
        int v = 1;
        while (v < n) {
            v <<= 1;
            b++;
        }
        return Math.max(1, b);
    }

    private static int packCellKey(int lvlIdx, int x, int y, int xyBits, int lvlBits) {
        // lvlBits is not strictly needed here, but kept for symmetry with packState.
        return (lvlIdx << (xyBits * 2)) | (x << xyBits) | y;
    }

    private static long packState(int lvlIdx, int x, int y, long mask, int xyBits, int lvlBits) {
        // Layout (low -> high):
        //   y (xyBits) | x (xyBits) | lvl (lvlBits) | mask (remaining bits)
        int shiftX = xyBits;
        int shiftLvl = xyBits * 2;
        int shiftMask = shiftLvl + lvlBits;

        long meta = ((long) (lvlIdx & ((1 << lvlBits) - 1)) << shiftLvl)
                | ((long) (x & ((1 << xyBits) - 1)) << shiftX)
                | (long) (y & ((1 << xyBits) - 1));

        return (mask << shiftMask) | meta;
    }

    private static int[] unpackState(long state, int xyBits, int lvlBits) {
        long yMask = (1L << xyBits) - 1L;
        long lvlMask = (1L << lvlBits) - 1L;

        int y = (int) (state & yMask);
        int x = (int) ((state >> xyBits) & yMask);
        int lvl = (int) ((state >> (xyBits * 2)) & lvlMask);

        return new int[]{lvl, x, y};
    }

    private static long unpackMask(long state, int xyBits, int lvlBits) {
        int shiftMask = (xyBits * 2) + lvlBits;
        return state >>> shiftMask;
    }

    private long applyPickupMask(
            WizardryData.Scenario sc, int lvlIdx, int x, int y,
            long mask,
            java.util.Map<Integer, Integer> bitByItem,
            java.util.Map<Integer, Integer> gateBitByCell,
            int xyBits, int lvlBits) {

        WizardryData.MazeCell c = sc.levels()[lvlIdx].cells[x][y];
        if (c == null) {
            return mask;
        }

        if (c.itemObtained > 0) {
            int b = bitOrNeg(bitByItem, c.itemObtained);
            if (b >= 0) {
                mask |= (1L << b);
            }
        }

        if (c.itemObtainedFromRiddle > 0) {
            int b = bitOrNeg(bitByItem, c.itemObtainedFromRiddle);
            if (b >= 0) {
                mask |= (1L << b);
            }
        }

        // Encounter rewards: often modeled as "take item" then "give item".
        if (c.encounterGiveItem > 0) {
            boolean ok = true;
            if (c.encounterTakeItem > 0) {
                int takeBit = bitOrNeg(bitByItem, c.encounterTakeItem);
                if (takeBit >= 0 && (mask & (1L << takeBit)) == 0L) {
                    ok = false;
                } else if (takeBit >= 0) {
                    mask &= ~(1L << takeBit); // consumed
                }
            }
            if (ok) {
                int giveBit = bitOrNeg(bitByItem, c.encounterGiveItem);
                if (giveBit >= 0) {
                    mask |= (1L << giveBit);
                }
            }
        } else if (c.encounterTakeItem > 0) {
            // Pure "take item" encounter.
            int takeBit = bitOrNeg(bitByItem, c.encounterTakeItem);
            if (takeBit >= 0) {
                mask &= ~(1L << takeBit);
            }
        }

        // Trade: if you have tradeItem1, you can receive tradeItem2 (and usually lose tradeItem1).
        if (c.tradeItem2 > 0) {
            boolean ok = true;
            if (c.tradeItem1 > 0) {
                int t1 = bitOrNeg(bitByItem, c.tradeItem1);
                if (t1 >= 0 && (mask & (1L << t1)) == 0L) {
                    ok = false;
                } else if (t1 >= 0) {
                    mask &= ~(1L << t1); // traded away
                }
            }
            if (ok) {
                int t2 = bitOrNeg(bitByItem, c.tradeItem2);
                if (t2 >= 0) {
                    mask |= (1L << t2);
                }
            }
        }

        if (c.itemRequired > 0) {
            int cellKey = packCellKey(lvlIdx, x, y, xyBits, lvlBits);
            Integer gb = gateBitByCell.get(cellKey);
            if (gb != null) {
                mask |= (1L << gb);
            }
        }

        if (c.encounterID >= 0 && encounterRewardItemByEncounterId != null && !encounterRewardItemByEncounterId.isEmpty()) {
            Integer rid = encounterRewardItemByEncounterId.get(c.encounterID);
            if (rid != null && rid > 0) {
                int rb = bitOrNeg(bitByItem, rid);
                if (rb >= 0) {
                    mask |= (1L << rb);
                }
            }
        }

        return mask;
    }

    private boolean canEnter(WizardryData.Scenario sc, int lvlIdx, int x, int y, long mask, java.util.Map<Integer, Integer> bitByItem) {

        WizardryData.MazeCell c = sc.levels()[lvlIdx].cells[x][y];
        if (c == null) {
            return false;
        }
        if (c.rock) {
            return false;
        }

        if (c.itemRequired > 0) {
            int b = bitOrNeg(bitByItem, c.itemRequired);
            if (b >= 0) {
                if ((mask & (1L << b)) == 0L) {
                    return false; // gated
                }
            }
        }

        return true;
    }

    private static int[] findBestStairsDown(WizardryData.Scenario sc, int lvlIdx, int dim, int targetLevelIdx) {
        int bestToLvl = -1;
        int bestX = -1, bestY = -1;
        int bestToX = -1, bestToY = -1;

        WizardryData.MazeLevel lvl = sc.levels()[lvlIdx];
        for (int x = 0; x < dim; x++) {
            for (int y = 0; y < dim; y++) {
                WizardryData.MazeCell c = lvl.cells[x][y];
                if (c == null || !c.stairs || c.addressTo == null || c.addressTo.level <= 0) {
                    continue;
                }

                int toLvlIdx = c.addressTo.level - 1; // 1-based -> 0-based
                if (toLvlIdx <= lvlIdx) {
                    continue;     // not "down"
                }
                if (toLvlIdx > targetLevelIdx) {
                    continue;
                }

                if (toLvlIdx > bestToLvl) {
                    bestToLvl = toLvlIdx;
                    bestX = x;
                    bestY = y;
                    bestToX = c.addressTo.row;
                    bestToY = c.addressTo.column;
                }
            }
        }

        if (bestToLvl < 0) {
            return null;
        }
        return new int[]{bestX, bestY, bestToLvl, bestToX, bestToY};
    }

    private String itemLabel(WizardryData.Scenario sc, int itemId) {
        if (itemId <= 0) {
            return "none";
        }
        if (sc == null) {
            return "#" + itemId;
        }
        try {
            Item it = sc.item(itemId);
            if (it != null && it.name != null && !it.name.isEmpty()) {
                return String.format("%s (#%d)", it.name, itemId);
            }
        } catch (Throwable t) {
            // Best-effort only; keep the walkthrough running even if lookup fails.
        }
        return "#" + itemId;
    }

    private String itemShortLabel(WizardryData.Scenario sc, int itemId) {
        if (itemId <= 0) {
            return "none";
        }
        if (sc == null) {
            return "#" + itemId;
        }
        try {
            Item it = sc.item(itemId);
            if (it != null && it.name != null && !it.name.isEmpty()) {
                return it.name;
            }
        } catch (Throwable t) {
            // Best-effort only.
        }
        return "#" + itemId;
    }

    private static String joinLimited(java.util.List<String> parts, int maxItems) {
        if (parts == null || parts.isEmpty()) {
            return "";
        }
        if (maxItems <= 0 || parts.size() <= maxItems) {
            return String.join(", ", parts);
        }
        java.util.List<String> head = parts.subList(0, maxItems);
        return String.join(", ", head) + String.format(" ...(+%d)", (parts.size() - maxItems));
    }

    private int[] collectRequiredItemsInWalk(WizardryData.Scenario sc, java.util.List<Node> nodes) {
        java.util.HashSet<Integer> req = new java.util.HashSet<>();
        if (sc == null || nodes == null) {
            return new int[0];
        }
        for (Node n : nodes) {
            WizardryData.MazeCell c = sc.levels()[n.lvl].cells[n.x][n.y];
            if (c != null && c.itemRequired > 0) {
                req.add(c.itemRequired);
            }
        }

        req.addAll(this.encounterRewardItemByEncounterId.values());

        int[] out = new int[req.size()];
        int i = 0;
        for (Integer id : req) {
            out[i++] = id;
        }
        java.util.Arrays.sort(out);
        return out;
    }

    private String fitHudLine(String s, float maxWidth) {
        if (s == null) {
            return "";
        }
        if (hudLayout == null || font == null) {
            return s;
        }
        hudLayout.setText(font, s);
        if (hudLayout.width <= maxWidth) {
            return s;
        }

        final String suffix = "...";
        int lo = 0;
        int hi = s.length();
        while (lo + 1 < hi) {
            int mid = (lo + hi) >>> 1;
            String cand = s.substring(0, mid) + suffix;
            hudLayout.setText(font, cand);
            if (hudLayout.width <= maxWidth) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        if (lo <= 0) {
            return suffix;
        }
        return s.substring(0, lo) + suffix;
    }

    private java.util.Map<Integer, Integer> buildEncounterRewardItemByEncounterId(WizardryData.Scenario sc) {

        java.util.HashMap<Integer, Integer> out = new java.util.HashMap<>();
        if (sc == null || sc.levels() == null) {
            return out;
        }

        final int dim = sc.levels()[0].dimension;

        // 1) Explicit scripted encounters that directly give an item.
        java.util.HashSet<Integer> encounterIdsPresent = new java.util.HashSet<>();
        for (WizardryData.MazeLevel lvl : sc.levels()) {
            for (int x = 0; x < dim; x++) {
                for (int y = 0; y < dim; y++) {
                    WizardryData.MazeCell c = lvl.cells[x][y];
                    if (c == null) {
                        continue;
                    }

                    if (c.encounterID >= 0) {
                        encounterIdsPresent.add(c.encounterID);
                    }
                    if (c.encounterID >= 0 && c.encounterGiveItem > 0) {
                        out.put(c.encounterID, c.encounterGiveItem);
                    }
                }
            }
        }

        // 2) Monster chest rewards (if the encounterID looks like a monster id).
        if (sc.monsters() == null || sc.rewards() == null) {
            return out;
        }

        for (Integer encIdObj : encounterIdsPresent) {
            if (encIdObj == null) {
                continue;
            }

            final int encounterId = encIdObj.intValue();
            if (out.containsKey(encounterId)) {
                continue;
            }
            Monster m = lookupMonsterByEncounterId(sc, encounterId);
            if (m == null) {
                continue;
            }

            final int rewardId = m.getChestReward();
            if (rewardId < 0) {
                continue;
            }

            Reward reward = lookupReward(sc.rewards(), rewardId);
            if (reward == null || reward.getRewardDetails() == null || reward.getRewardDetails().isEmpty()) {
                continue;
            }

            int itemId = pickRewardItemId(reward);
            if (itemId > 0) {
                out.put(encounterId, itemId);
            }
        }

        return out;
    }

    private static Monster lookupMonsterByEncounterId(WizardryData.Scenario sc, int encounterId) {
        if (sc == null || sc.monsters() == null) {
            return null;
        }

        // Preferred: treat encounterId as a monsterId.
        Monster m = sc.monster(encounterId);
        if (m != null) {
            return m;
        }

        // Fallback: sometimes content uses list indices rather than monsterIds.
        if (encounterId >= 0 && encounterId < sc.monsters().size()) {
            try {
                return sc.monsters().get(encounterId);
            } catch (Throwable t) {
                return null;
            }
        }
        return null;
    }

    private static Reward lookupReward(java.util.List<Reward> rewards, int rewardId) {
        if (rewards == null || rewards.isEmpty()) {
            return null;
        }

        // Preferred: match by id field.
        for (Reward r : rewards) {
            if (r != null && r.getId() == rewardId) {
                return r;
            }
        }

        // Fallbacks: list indices (0-based or 1-based).
        if (rewardId >= 0 && rewardId < rewards.size()) {
            return rewards.get(rewardId);
        }
        if (rewardId > 0 && (rewardId - 1) >= 0 && (rewardId - 1) < rewards.size()) {
            return rewards.get(rewardId - 1);
        }

        return null;
    }

    private static int pickRewardItemId(Reward reward) {
        if (reward == null || reward.getRewardDetails() == null || reward.isIsChest()) {
            return 0;
        }
        int bestItem = 0;
        for (Reward.RewardDetails d : reward.getRewardDetails()) {
            if (d == null || d.itemReward == null || d.odds != 100) {
                continue;
            }
            bestItem = d.itemReward.min;
        }

        return bestItem;
    }

}
