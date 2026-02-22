
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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Vibe Coded Tool to analyze a scenario and run a walk thru of it
 * automatically.
 *
 */
public class ScenarioSummarizer extends InputAdapter implements ApplicationListener {

    // Best-effort mapping from encounterID -> a notable reward item id (typically a quest/key item).
    private Map<Integer, Integer> encounterRewardItemByEncounterId = Collections.emptyMap();
    private final Map<Integer, List<SpecialCell>> specials = new HashMap<>();
    private int scenarioMaxLevel = -1;

    private WizardryData.Scenario scenario;
    private List<Node> walkthrough;

    private int walkIdx = 0;
    private float walkAccum = 0f;

    private static final float STEP_SECONDS = 0.5f;
    private static final float HUD_HEIGHT = 180f;
    private static final int PIT_PENALTY = 500;
    private static final int DAMAGE_PENALTY = 120;
    private static final int STEP_WEIGHT = 5000;
    private static final int HOP_TIEBREAKER = 1;

    private OrthographicCamera camera;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout hudLayout;

    private String[] hudEventByStep = new String[0];
    private int[] hudNextObjectiveItemByStep = new int[0];
    private String[] hudRequiredByStep = new String[0];
    private String[] hudOwnedByStep = new String[0];

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

        WizardryData.Scenario sc = WizardryData.Scenario.LEG;
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
            System.out.println("Specials for Level " + (lvl + 1));
            for (SpecialCell s : specials.get(lvl)) {
                System.out.println("\t" + s);
            }
        }

        List<Node> nodes = findWalkthrough(sc, 0, sc.getStartX(), sc.getStartY(), sc.levels().length - 1);
        this.walkthrough = nodes;

        buildHudAnnotations(sc, nodes);

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

        int nextItemId = (hudNextObjectiveItemByStep != null && walkIdx < hudNextObjectiveItemByStep.length) ? hudNextObjectiveItemByStep[walkIdx] : 0;
        String hud2 = "Next objective item: " + itemLabel(scenario, nextItemId);
        String hudReq = (hudRequiredByStep != null && walkIdx < hudRequiredByStep.length) ? hudRequiredByStep[walkIdx] : "";
        String hudOwned = (hudOwnedByStep != null && walkIdx < hudOwnedByStep.length) ? hudOwnedByStep[walkIdx] : "";
        String hud3 = (hudEventByStep != null && walkIdx < hudEventByStep.length) ? hudEventByStep[walkIdx] : "";

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

    private List<Node> findWalkthrough(WizardryData.Scenario sc, int startLevelIdx, int startX, int startY, int targetLevelIdx) {

        final int L = sc.levels().length;
        final int dim = sc.levels()[0].dimension;

        // ---------- Gather items (required + obtainable) across all levels ----------
        LinkedHashSet<Integer> requiredItems = new LinkedHashSet<>();
        LinkedHashSet<Integer> obtainableItems = new LinkedHashSet<>();

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

        HashMap<Integer, Integer> bitByItem = new HashMap<>();
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

        HashSet<Integer> remaining = new HashSet<>();
        for (int lvlIdx = startLevelIdx; lvlIdx <= targetLevelIdx && lvlIdx < L; lvlIdx++) {
            List<SpecialCell> list = specials.get(lvlIdx);
            if (list == null) {
                continue;
            }
            for (SpecialCell s : list) {
                remaining.add(specialKey(s.lvl, s.x, s.y, dim));
            }
        }

        ArrayList<Node> walk = new ArrayList<>();

        final Map<Integer, Integer> emptyGateBits = Collections.emptyMap();
        long curMask = 0L;
        curMask = applyPickupMask(sc, startLevelIdx, startX, startY, curMask, bitByItem, emptyGateBits, xyBits, lvlBits);

        // Seed inventory with any prerequisite items that the scenario does not model as obtainable.
        // This helps the walkthrough planner include trades/encounters that require such items.
        long seedMask = seedUnobtainableRequiredInputs(sc, startLevelIdx, targetLevelIdx, bitByItem, encounterRewardItemByEncounterId);
        curMask |= seedMask;

        int curLvl = startLevelIdx;
        int curX = startX;
        int curY = startY;
        int totalCost = 0;

        walk.add(new Node(curLvl, curX, curY, curMask, totalCost));
        remaining.remove(specialKey(curLvl, curX, curY, dim));

        // Direction deltas: N=0, S=1, W=2, E=3
        final int[] dx = {1, -1, 0, 0};
        final int[] dy = {0, 0, -1, 1};

        // Track which specials have been visited (item obtained)
        HashSet<Integer> visited = new HashSet<>();
        // Track specials we skipped due to pathfinding blockage (gated/unreachable)
        HashSet<Integer> deferred = new HashSet<>();

        // ---- Inner Dijkstra: find shortest path from (curLvl,curX,curY) to (tLvl,tX,tY)
        // Returns list of Nodes from start (exclusive) to target (inclusive), or null if unreachable.
        // Uses wall/door passability, level transitions via stairs/chute/teleport/elevator.
        // Hidden doors are visible (passable) to the walker.
        Function<long[], List<Node>> dijkstraTo = (args) -> {
            int fromLvl = (int) args[0];
            int fromX = (int) args[1];
            int fromY = (int) args[2];
            int toLvl = (int) args[3];
            int toX = (int) args[4];
            int toY = (int) args[5];
            long startMask = args[6];

            // State: (lvl, x, y, mask) -> packed long
            // Cost map
            HashMap<Long, Long> costMap = new HashMap<>();
            // Parent map for path reconstruction
            HashMap<Long, Long> parent = new HashMap<>();

            // Priority queue: [cost, lvl, x, y, mask]
            PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));

            long startState = packState(fromLvl, fromX, fromY, startMask, xyBits, lvlBits);
            costMap.put(startState, 0L);
            pq.offer(new long[]{0, fromLvl, fromX, fromY, startMask});

            long goalState = -1L;

            while (!pq.isEmpty()) {
                long[] cur2 = pq.poll();
                long cost2 = cur2[0];
                int lvl2 = (int) cur2[1];
                int x2 = (int) cur2[2];
                int y2 = (int) cur2[3];
                long mask2 = cur2[4];

                long state2 = packState(lvl2, x2, y2, mask2, xyBits, lvlBits);
                Long best = costMap.get(state2);
                if (best != null && cost2 > best) {
                    continue;
                }

                if (lvl2 == toLvl && x2 == toX && y2 == toY) {
                    goalState = state2;
                    break;
                }

                WizardryData.MazeCell cell2 = sc.levels()[lvl2].cells[x2][y2];
                if (cell2 == null) {
                    continue;
                }

                // --- Handle level transitions from current cell ---
                // Stairs / chute / teleport: move to addressTo
                if ((cell2.stairs || cell2.chute || cell2.teleport) && cell2.addressTo != null && cell2.addressTo.level > 0) {
                    int nextLvl2 = cell2.addressTo.level - 1;
                    int nextX2 = cell2.addressTo.row;
                    int nextY2 = cell2.addressTo.column;
                    if (nextLvl2 >= 0 && nextLvl2 < L
                            && nextX2 >= 0 && nextX2 < dim
                            && nextY2 >= 0 && nextY2 < dim) {
                        WizardryData.MazeCell dest2 = sc.levels()[nextLvl2].cells[nextX2][nextY2];
                        if (dest2 != null && !dest2.rock) {
                            long newMask2 = applyPickupMask(sc, nextLvl2, nextX2, nextY2, mask2, bitByItem, Collections.emptyMap(), xyBits, lvlBits);
                            boolean forced = (cell2.teleport || cell2.chute);
                            long hopCost = forced ? 0L : (STEP_WEIGHT + HOP_TIEBREAKER);
                            long newCost2 = cost2 + hopCost + (long) hazardPenalty(sc, nextLvl2, nextX2, nextY2) * STEP_WEIGHT;
                            long newState2 = packState(nextLvl2, nextX2, nextY2, newMask2, xyBits, lvlBits);
                            Long prevCost2 = costMap.get(newState2);
                            if (prevCost2 == null || newCost2 < prevCost2) {
                                costMap.put(newState2, newCost2);
                                parent.put(newState2, state2);
                                pq.offer(new long[]{newCost2, nextLvl2, nextX2, nextY2, newMask2});
                            }
                        }
                    }
                }

                // Elevator: can move to any floor between elevatorFrom and elevatorTo
                if (cell2.elevator) {
                    int eFrom = Math.min(cell2.elevatorFrom, cell2.elevatorTo);
                    int eTo = Math.max(cell2.elevatorFrom, cell2.elevatorTo);
                    for (int eLvl = eFrom; eLvl <= eTo && eLvl < L; eLvl++) {
                        if (eLvl == lvl2) {
                            continue;
                        }
                        // Elevator lands on same x,y position on destination floor
                        int ex2 = x2, ey2 = y2;
                        if (ex2 < 0 || ex2 >= dim || ey2 < 0 || ey2 >= dim) {
                            continue;
                        }
                        WizardryData.MazeCell eDest = sc.levels()[eLvl].cells[ex2][ey2];
                        if (eDest == null || eDest.rock) {
                            continue;
                        }
                        long newMask2 = applyPickupMask(sc, eLvl, ex2, ey2, mask2, bitByItem, Collections.emptyMap(), xyBits, lvlBits);
                        long newCost2 = cost2 + STEP_WEIGHT + HOP_TIEBREAKER + hazardPenalty(sc, eLvl, ex2, ey2) * STEP_WEIGHT;
                        long newState2 = packState(eLvl, ex2, ey2, newMask2, xyBits, lvlBits);
                        Long prevCost2 = costMap.get(newState2);
                        if (prevCost2 == null || newCost2 < prevCost2) {
                            costMap.put(newState2, newCost2);
                            parent.put(newState2, state2);
                            pq.offer(new long[]{newCost2, eLvl, ex2, ey2, newMask2});
                        }
                    }
                }

                // --- Regular cardinal moves ---
                // Wizardry only checks the EXITING cell's wall, not the neighbour's entry wall.
                // Walls are stored on one side only, so a bidirectional check incorrectly
                // blocks valid moves (e.g. a cell with only a West wall blocks entry from East).
                for (int dir = 0; dir < 4; dir++) {
                    if (!passableDir(cell2, dir)) {
                        continue;
                    }

                    int nx2 = x2 + dx[dir];
                    int ny2 = y2 + dy[dir];

                    boolean out = (nx2 < 0 || nx2 >= dim || ny2 < 0 || ny2 >= dim);
                    if (out) {
                        // Only allow wrapping if we are actually going through a door/hidden door.
                        // (Prevents accidental wrap on maps that just forgot boundary walls.)
                        if (!passableDir(cell2, dir)) {
                            continue;
                        }

                        nx2 = wrap(nx2, dim);
                        ny2 = wrap(ny2, dim);
                    }

                    WizardryData.MazeCell neighbor = sc.levels()[lvl2].cells[nx2][ny2];
                    if (neighbor == null || neighbor.rock) {
                        continue;
                    }
                    if (!canEnter(sc, lvl2, nx2, ny2, mask2, bitByItem)) {
                        continue;
                    }

                    long newMask2 = applyPickupMask(sc, lvl2, nx2, ny2, mask2, bitByItem,
                            Collections.emptyMap(), xyBits, lvlBits);
                    long newCost2 = cost2 + STEP_WEIGHT + hazardPenalty(sc, lvl2, nx2, ny2) * STEP_WEIGHT;
                    long newState2 = packState(lvl2, nx2, ny2, newMask2, xyBits, lvlBits);

                    Long prevCost2 = costMap.get(newState2);
                    if (prevCost2 == null || newCost2 < prevCost2) {
                        costMap.put(newState2, newCost2);
                        parent.put(newState2, state2);
                        pq.offer(new long[]{newCost2, lvl2, nx2, ny2, newMask2});
                    }
                }
            }

            if (goalState < 0) {
                // ── Dijkstra failure diagnostics ──────────────────────────────
                String fPos = "L" + (fromLvl + 1) + "(" + fromX + "," + fromY + ")";
                String tPos = "L" + (toLvl + 1) + "(" + toX + "," + toY + ")";
                System.out.println("     [DIJKSTRA FAIL] No path from " + fPos + " to " + tPos);
                System.out.println("       States visited: " + costMap.size());
                boolean goalCellVisited = false;
                for (Long stateKey : costMap.keySet()) {
                    int[] up = unpackState(stateKey, xyBits, lvlBits);
                    if (up[0] == toLvl && up[1] == toX && up[2] == toY) {
                        goalCellVisited = true;
                        System.out.println("       Goal cell WAS visited mask=0x"
                                + Long.toHexString(unpackMask(stateKey, xyBits, lvlBits)));
                    }
                }
                if (!goalCellVisited) {
                    System.out.println("       Goal cell NEVER reached. Neighbours of " + tPos + ":");
                    WizardryData.MazeCell tgt = sc.levels()[toLvl].cells[toX][toY];
                    if (tgt == null) {
                        System.out.println("         target cell is NULL!");
                    } else {
                        System.out.println("         N-wall=" + tgt.northWall + "(door=" + tgt.northDoor + ")"
                                + " S=" + tgt.southWall + "(door=" + tgt.southDoor + ")"
                                + " W=" + tgt.westWall + "(door=" + tgt.westDoor + ")"
                                + " E=" + tgt.eastWall + "(door=" + tgt.eastDoor + ")"
                                + " itemReq=" + tgt.itemRequired);
                        String[] dname = {"N", "S", "W", "E"};
                        int[] ddx2 = {1, -1, 0, 0};
                        int[] ddy2 = {0, 0, -1, 1};
                        for (int d2 = 0; d2 < 4; d2++) {
                            int ax = toX + ddx2[d2], ay = toY + ddy2[d2];
                            if (ax < 0 || ax >= dim || ay < 0 || ay >= dim) {
                                continue;
                            }
                            String aPos = "L" + (toLvl + 1) + "(" + ax + "," + ay + ")";
                            WizardryData.MazeCell adj = sc.levels()[toLvl].cells[ax][ay];
                            if (adj == null || adj.rock) {
                                System.out.println("         " + dname[d2] + " " + aPos + " -> rock/null");
                                continue;
                            }
                            boolean adjVisited = false;
                            for (Long sk2 : costMap.keySet()) {
                                int[] up2 = unpackState(sk2, xyBits, lvlBits);
                                if (up2[0] == toLvl && up2[1] == ax && up2[2] == ay) {
                                    adjVisited = true;
                                    break;
                                }
                            }
                            System.out.println("         " + dname[d2] + " " + aPos
                                    + " visited=" + adjVisited
                                    + " adjCanExit=" + passableDir(adj, d2)
                                    + " itemReq=" + adj.itemRequired
                                    + " teleport=" + adj.teleport + " rock=" + adj.rock);
                        }
                    }
                }
                return null; // unreachable
            }

            // Reconstruct path by walking parent links back to the seed node.
            // The seed was never inserted into parent map, so par==null marks it.
            LinkedList<long[]> pathStates = new LinkedList<>();
            long cur3 = goalState;
            while (true) {
                int[] unpacked = unpackState(cur3, xyBits, lvlBits);
                long m3 = unpackMask(cur3, xyBits, lvlBits);
                long cost3 = costMap.getOrDefault(cur3, 0L);
                pathStates.addFirst(new long[]{unpacked[0], unpacked[1], unpacked[2], m3, cost3});
                Long par = parent.get(cur3);
                if (par == null) {
                    break; // reached seed
                }
                cur3 = par;
            }
            // Strip start cell if it ended up as first entry
            if (!pathStates.isEmpty()) {
                long[] first = pathStates.getFirst();
                if ((int) first[0] == fromLvl && (int) first[1] == fromX && (int) first[2] == fromY) {
                    pathStates.removeFirst();
                }
            }

            List<Node> result = new ArrayList<>(pathStates.size());
            for (long[] ps : pathStates) {
                result.add(new Node((int) ps[0], (int) ps[1], (int) ps[2], ps[3], (int) ps[4]));
            }
            return result;
        };

        // ---- Helper: collect item specials on a given level that give items ----
        // Returns list of [lvl, x, y] for specials that could be item sources
        Function<Integer, List<int[]>> itemSpecialsOnLevel = (lvlIdx) -> {
            List<int[]> result = new ArrayList<>();
            List<SpecialCell> spList = specials.get(lvlIdx);
            if (spList == null) {
                return result;
            }
            for (SpecialCell s : spList) {
                WizardryData.MazeCell mc = sc.levels()[s.lvl].cells[s.x][s.y];
                if (mc == null) {
                    continue;
                }
                boolean hasItem = mc.itemObtained > 0 || mc.itemObtainedFromRiddle > 0
                        || mc.encounterGiveItem > 0 || mc.tradeItem2 > 0
                        || (mc.encounterID >= 0 && encounterRewardItemByEncounterId.containsKey(mc.encounterID));
                if (hasItem) {
                    result.add(new int[]{s.lvl, s.x, s.y});
                }
            }
            return result;
        };

        // ---- Attempt to collect all item specials on all levels, with backtracking ----
        // We do multiple passes: first collect all reachable items on all levels in order,
        // then check if any deferred items became reachable with newly acquired items.
        // Collect all item-bearing special locations across all levels
        LinkedHashSet<Integer> pendingSpecials = new LinkedHashSet<>();
        for (int lvlIdx = startLevelIdx; lvlIdx <= targetLevelIdx && lvlIdx < L; lvlIdx++) {
            List<int[]> lvlItems = itemSpecialsOnLevel.apply(lvlIdx);
            for (int[] loc : lvlItems) {
                pendingSpecials.add(specialKey(loc[0], loc[1], loc[2], dim));
            }
        }

        // Mark start cell visited
        visited.add(specialKey(curLvl, curX, curY, dim));

        System.out.println("START: " + pos(curLvl, curX, curY)
                + "  [" + debugCellDescription(sc, curLvl, curX, curY) + "]");
        System.out.println("       Inventory: " + debugInventory(sc, curMask, bitByItem));
        System.out.println();

        boolean madeProgress = true;
        int passCount = 0;
        final int MAX_PASSES = 20;

        while (madeProgress && passCount < MAX_PASSES) {
            madeProgress = false;
            passCount++;

            List<Integer> candidates = new ArrayList<>(pendingSpecials);
            candidates.removeAll(visited);
            if (candidates.isEmpty()) {
                break;
            }

            System.out.println("----------------------------------------------------------------");
            System.out.println("PASS " + passCount + " | from " + pos(curLvl, curX, curY)
                    + " | candidates=" + candidates.size() + " | deferred=" + deferred.size());
            System.out.println("  Inventory: " + debugInventory(sc, curMask, bitByItem));
            System.out.println();

            for (int specKey : candidates) {
                int sk = specKey;
                int sLvl = sk / (dim * dim);
                int sX = (sk % (dim * dim)) / dim;
                int sY = sk % dim;

                System.out.println("  >> TARGET: " + pos(sLvl, sX, sY)
                        + "  [" + debugCellDescription(sc, sLvl, sX, sY) + "]");
                System.out.println("     from: " + pos(curLvl, curX, curY)
                        + "  inventory: " + debugInventory(sc, curMask, bitByItem));

                // Trade specials should only be attempted when actionable; otherwise defer until we have the required input item.
                WizardryData.MazeCell targetCell = sc.levels()[sLvl].cells[sX][sY];
                if (isTradeCell(targetCell)) {
                    // If the trade cannot currently execute (missing required input item), defer it so we can come back later.
                    if (!isTradeActionable(targetCell, bitByItem, curMask)) {
                        System.out.println("     [DEFERRED] trade requires " + itemLabel(sc, targetCell.tradeItem1));
                        System.out.println();
                        deferred.add(specKey);
                        continue;
                    }
                    // If the trade is actionable but would not change inventory (e.g., already have output and no input consumption tracked),
                    // treat it as already satisfied to avoid wasting a visit.
                    if (!tradeWouldChangeMask(targetCell, bitByItem, curMask)) {
                        System.out.println("     [SKIP] trade yields no new items (" + itemLabel(sc, targetCell.tradeItem2) + ")");
                        System.out.println();
                        visited.add(specKey);
                        deferred.remove(specKey);
                        madeProgress = true;
                        continue;
                    }
                }

                List<Node> path = dijkstraTo.apply(new long[]{curLvl, curX, curY, sLvl, sX, sY, curMask});

                if (path == null || path.isEmpty()) {
                    WizardryData.MazeCell mc = sc.levels()[sLvl].cells[sX][sY];
                    String why = (mc != null && mc.itemRequired > 0)
                            ? " -- GATE requires " + itemLabel(sc, mc.itemRequired) : "";
                    System.out.println("     [DEFERRED]" + why);
                    System.out.println();
                    deferred.add(specKey);
                    continue;
                }

                System.out.println("     Path: " + path.size() + " steps");
                long prevMask = curMask;
                int prevLvl2 = curLvl;
                int stepNum = 0;
                for (Node pathNode : path) {
                    stepNum++;
                    walk.add(pathNode);
                    remaining.remove(specialKey(pathNode.lvl, pathNode.x, pathNode.y, dim));
                    boolean lvlChange = pathNode.lvl != prevLvl2;
                    String cellDesc = debugCellDescription(sc, pathNode.lvl, pathNode.x, pathNode.y);
                    System.out.println("     step " + stepNum + ": " + pos(pathNode.lvl, pathNode.x, pathNode.y)
                            + (lvlChange ? "  [LEVEL CHANGE from L" + (prevLvl2 + 1) + "]" : "")
                            + "  [" + cellDesc + "]");
                    if (pathNode.mask != prevMask) {
                        System.out.println("              ** Inventory -> "
                                + debugInventory(sc, pathNode.mask, bitByItem));
                    }
                    prevMask = pathNode.mask;
                    prevLvl2 = pathNode.lvl;
                }

                Node last = path.get(path.size() - 1);
                long maskBefore = last.mask;
                curLvl = last.lvl;
                curX = last.x;
                curY = last.y;
                curMask = last.mask;
                curMask = applyPickupMask(sc, curLvl, curX, curY, curMask, bitByItem,
                        Collections.emptyMap(), xyBits, lvlBits);

                System.out.println("     ARRIVED " + pos(curLvl, curX, curY));
                if (curMask != maskBefore) {
                    System.out.println("     ** Items gained! Before: " + debugInventory(sc, maskBefore, bitByItem));
                    System.out.println("                      After:  " + debugInventory(sc, curMask, bitByItem));
                } else {
                    System.out.println("     Inventory: " + debugInventory(sc, curMask, bitByItem));
                }
                System.out.println();

                boolean completed = true;
                if (isTradeCell(targetCell)) {
                    // A trade is only considered completed if it actually changes inventory.
                    completed = (curMask != maskBefore);
                }

                if (completed) {
                    visited.add(specKey);
                    deferred.remove(specKey);
                    madeProgress = true;
                } else {
                    System.out.println("     [DEFERRED] trade not executed (missing input item or no inventory change)");
                    System.out.println();
                    deferred.add(specKey);
                }
            }

            // Check if any deferred items are now reachable with updated mask
            List<Integer> newlyReachable = new ArrayList<>();
            for (int specKey : deferred) {
                if (visited.contains(specKey)) {
                    continue;
                }
                int sk = specKey;
                int sLvl = sk / (dim * dim);
                int sX = (sk % (dim * dim)) / dim;
                int sY = sk % dim;
                List<Node> path = dijkstraTo.apply(new long[]{curLvl, curX, curY, sLvl, sX, sY, curMask});
                if (path != null && !path.isEmpty()) {
                    newlyReachable.add(specKey);
                }
            }

            if (!newlyReachable.isEmpty()) {
                System.out.println("  [RETRY] " + newlyReachable.size()
                        + " previously-deferred specials now reachable. Inventory: "
                        + debugInventory(sc, curMask, bitByItem));
                System.out.println();
                for (int specKey : newlyReachable) {
                    int sk = specKey;
                    int sLvl = sk / (dim * dim);
                    int sX = (sk % (dim * dim)) / dim;
                    int sY = sk % dim;

                    System.out.println("  >> RETRY: " + pos(sLvl, sX, sY)
                            + "  [" + debugCellDescription(sc, sLvl, sX, sY) + "]");

                    // Trade specials should only be attempted when actionable; otherwise keep deferred until we have the required input item.
                    WizardryData.MazeCell targetCell = sc.levels()[sLvl].cells[sX][sY];
                    if (isTradeCell(targetCell)) {
                        if (!isTradeActionable(targetCell, bitByItem, curMask)) {
                            System.out.println("     [STILL BLOCKED] trade requires " + itemLabel(sc, targetCell.tradeItem1));
                            System.out.println();
                            continue;
                        }
                        if (!tradeWouldChangeMask(targetCell, bitByItem, curMask)) {
                            System.out.println("     [SKIP] trade yields no new items (" + itemLabel(sc, targetCell.tradeItem2) + ")");
                            System.out.println();
                            visited.add(specKey);
                            deferred.remove(specKey);
                            madeProgress = true;
                            continue;
                        }
                    }

                    List<Node> path = dijkstraTo.apply(new long[]{curLvl, curX, curY, sLvl, sX, sY, curMask});
                    if (path == null || path.isEmpty()) {
                        System.out.println("     [STILL BLOCKED]");
                        System.out.println();
                        continue;
                    }

                    System.out.println("     Path: " + path.size() + " steps");
                    long prevMask = curMask;
                    int prevLvl2 = curLvl;
                    int stepNum = 0;
                    for (Node pathNode : path) {
                        stepNum++;
                        walk.add(pathNode);
                        remaining.remove(specialKey(pathNode.lvl, pathNode.x, pathNode.y, dim));
                        boolean lvlChange = pathNode.lvl != prevLvl2;
                        System.out.println("     step " + stepNum + ": " + pos(pathNode.lvl, pathNode.x, pathNode.y)
                                + (lvlChange ? "  [LEVEL CHANGE from L" + (prevLvl2 + 1) + "]" : "")
                                + "  [" + debugCellDescription(sc, pathNode.lvl, pathNode.x, pathNode.y) + "]");
                        if (pathNode.mask != prevMask) {
                            System.out.println("              ** Inventory -> "
                                    + debugInventory(sc, pathNode.mask, bitByItem));
                        }
                        prevMask = pathNode.mask;
                        prevLvl2 = pathNode.lvl;
                    }

                    Node last = path.get(path.size() - 1);
                    long maskBefore = last.mask;
                    curLvl = last.lvl;
                    curX = last.x;
                    curY = last.y;
                    curMask = last.mask;
                    curMask = applyPickupMask(sc, curLvl, curX, curY, curMask, bitByItem,
                            Collections.emptyMap(), xyBits, lvlBits);

                    System.out.println("     ARRIVED " + pos(curLvl, curX, curY));
                    if (curMask != maskBefore) {
                        System.out.println("     ** Items gained! Before: " + debugInventory(sc, maskBefore, bitByItem));
                        System.out.println("                      After:  " + debugInventory(sc, curMask, bitByItem));
                    } else {
                        System.out.println("     Inventory: " + debugInventory(sc, curMask, bitByItem));
                    }
                    System.out.println();

                    boolean completed = true;
                    if (isTradeCell(targetCell)) {
                        // A trade is only considered completed if it actually changes inventory.
                        completed = (curMask != maskBefore);
                    }

                    if (completed) {
                        visited.add(specKey);
                        deferred.remove(specKey);
                        madeProgress = true;
                    } else {
                        System.out.println("     [DEFERRED] trade not executed (missing input item or no inventory change)");
                        System.out.println();
                        deferred.add(specKey);
                    }
                }
            }

            pendingSpecials.removeAll(visited);
        }

        // Final summary
        System.out.println("================================================================");
        System.out.println("WALKTHROUGH COMPLETE  nodes=" + walk.size() + "  passes=" + passCount);
        System.out.println("  Final position:  " + pos(curLvl, curX, curY));
        System.out.println("  Final inventory: " + debugInventory(sc, curMask, bitByItem));
        System.out.println("  Visited: " + visited.size() + "  Still deferred: " + deferred.size());
        if (!deferred.isEmpty()) {
            System.out.println("[HUD ERRORS] Could not obtain:");
            for (int specKey : deferred) {
                int sk = specKey;
                int sLvl = sk / (dim * dim);
                int sX = (sk % (dim * dim)) / dim;
                int sY = sk % dim;
                System.out.println("  [MISSED] " + pos(sLvl, sX, sY)
                        + "  [" + debugCellDescription(sc, sLvl, sX, sY) + "]");
            }
        }
        System.out.println("================================================================");

        return walk;
    }

    private void buildHudAnnotations(WizardryData.Scenario sc, List<Node> nodes) {
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

        HashSet<Integer> owned = new HashSet<>();

        for (int i = 0; i < n; i++) {
            Node cur = nodes.get(i);
            WizardryData.MazeLevel lvl = sc.levels()[cur.lvl];
            WizardryData.MazeCell c = lvl.cells[cur.x][cur.y];

            ArrayList<String> msgs = new ArrayList<>(2);

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
                ArrayList<String> status = new ArrayList<>(requiredInWalk.length);
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
                ArrayList<Integer> ownedIds = new ArrayList<>(owned);
                Collections.sort(ownedIds);
                ArrayList<String> names = new ArrayList<>(ownedIds.size());
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

    private long applyPickupMask(
            WizardryData.Scenario sc, int lvlIdx, int x, int y,
            long mask,
            Map<Integer, Integer> bitByItem,
            Map<Integer, Integer> gateBitByCell,
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

    private Map<Integer, Integer> buildEncounterRewardItemByEncounterId(WizardryData.Scenario sc) {

        HashMap<Integer, Integer> out = new HashMap<>();
        if (sc == null || sc.levels() == null) {
            return out;
        }

        final int dim = sc.levels()[0].dimension;

        // 1) Explicit scripted encounters that directly give an item.
        HashSet<Integer> encounterIdsPresent = new HashSet<>();
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

    private int[] collectRequiredItemsInWalk(WizardryData.Scenario sc, List<Node> nodes) {
        HashSet<Integer> req = new HashSet<>();
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
        Arrays.sort(out);
        return out;
    }

    private static long seedUnobtainableRequiredInputs(WizardryData.Scenario sc, int startLevelIdx, int targetLevelIdx, 
            Map<Integer, Integer> bitByItem, Map<Integer, Integer> encounterRewardItemByEncounterId) {

        if (sc == null || sc.levels() == null) {
            return 0L;
        }

        int L = sc.levels().length;
        int from = Math.max(0, startLevelIdx);
        int to = Math.min(targetLevelIdx, L - 1);

        Set<Integer> produced = new HashSet<>();
        Set<Integer> requiredInputs = new HashSet<>();

        for (int lvl = from; lvl <= to; lvl++) {
            WizardryData.MazeLevel ml = sc.levels()[lvl];
            if (ml == null || ml.cells == null) {
                continue;
            }

            int dim = ml.cells.length;
            for (int x = 0; x < dim; x++) {
                for (int y = 0; y < dim; y++) {
                    WizardryData.MazeCell c = ml.cells[x][y];
                    if (c == null) {
                        continue;
                    }

                    // Required inputs (things you must already have to do something)
                    if (c.tradeItem1 > 0) {
                        requiredInputs.add(c.tradeItem1);
                    }
                    if (c.encounterTakeItem > 0) {
                        requiredInputs.add(c.encounterTakeItem);
                    }

                    // Produced items (things you can obtain within the modeled scenario)
                    if (c.itemObtained > 0) {
                        produced.add(c.itemObtained);
                    }
                    if (c.itemObtainedFromRiddle > 0) {
                        produced.add(c.itemObtainedFromRiddle);
                    }
                    if (c.encounterGiveItem > 0) {
                        produced.add(c.encounterGiveItem);
                    }
                    if (c.tradeItem2 > 0) {
                        produced.add(c.tradeItem2);
                    }

                    if (c.encounterID >= 0 && encounterRewardItemByEncounterId != null && !encounterRewardItemByEncounterId.isEmpty()) {
                        Integer rid = encounterRewardItemByEncounterId.get(c.encounterID);
                        if (rid != null && rid > 0) {
                            produced.add(rid);
                        }
                    }
                }
            }
        }

        ArrayList<Integer> seeds = new ArrayList<>();
        for (Integer req : requiredInputs) {
            if (req != null && req > 0 && !produced.contains(req)) {
                seeds.add(req);
            }
        }
        Collections.sort(seeds);

        long seedMask = 0L;
        if (!seeds.isEmpty()) {
            List<String> seededLabels = new ArrayList<>();
            List<String> untrackedLabels = new ArrayList<>();

            for (int itemId : seeds) {
                int b = bitOrNeg(bitByItem, itemId);
                if (b >= 0) {
                    seedMask |= (1L << b);
                    seededLabels.add(itemLabel(sc, itemId));
                } else {
                    untrackedLabels.add(itemLabel(sc, itemId));
                }
            }

            if (!seededLabels.isEmpty()) {
                System.out.println("[SEED] Unobtainable required input items (not produced by get/encounter/trade): "
                        + String.join(", ", seededLabels));
            }
            if (!untrackedLabels.isEmpty()) {
                System.out.println("[SEED] NOTE: Some unobtainable required inputs are not tracked in bitByItem and cannot be seeded: "
                        + String.join(", ", untrackedLabels));
            }
            if (!seededLabels.isEmpty() || !untrackedLabels.isEmpty()) {
                System.out.println();
            }
        }

        return seedMask;
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

    private static String specialTags(WizardryData.Scenario sc, int lvlIdx, int x, int y) {
        WizardryData.MazeCell c = sc.levels()[lvlIdx].cells[x][y];
        if (c == null) {
            return "";
        }
        ArrayList<String> tags = new ArrayList<>(4);

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

    private static int hazardPenalty(WizardryData.Scenario sc, int lvlIdx, int x, int y) {
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

    private static boolean canEnter(WizardryData.Scenario sc, int lvlIdx, int x, int y, long mask, Map<Integer, Integer> bitByItem) {

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

    private static String pos(int lvl, int x, int y) {
        return "L" + (lvl + 1) + "(" + x + "," + y + ")";
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

    private static Reward lookupReward(List<Reward> rewards, int rewardId) {
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

    private static String itemLabel(WizardryData.Scenario sc, int itemId) {
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
        }
        return "#" + itemId;
    }

    private static String itemShortLabel(WizardryData.Scenario sc, int itemId) {
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
        }
        return "#" + itemId;
    }

    private static String joinLimited(List<String> parts, int maxItems) {
        if (parts == null || parts.isEmpty()) {
            return "";
        }
        if (maxItems <= 0 || parts.size() <= maxItems) {
            return String.join(", ", parts);
        }
        List<String> head = parts.subList(0, maxItems);
        return String.join(", ", head) + String.format(" ...(+%d)", (parts.size() - maxItems));
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

    private static int wrap(int v, int dim) {
        int r = v % dim;
        return r < 0 ? r + dim : r;
    }

    private static boolean passableDir(WizardryData.MazeCell c, int dir) {
        switch (dir) {
            case 0: // N
                return !(c.northWall && !(c.northDoor || c.hiddenNorthDoor));
            case 1: // S
                return !(c.southWall && !(c.southDoor || c.hiddenSouthDoor));
            case 2: // W
                return !(c.westWall && !(c.westDoor || c.hiddenWestDoor));
            case 3: // E
                return !(c.eastWall && !(c.eastDoor || c.hiddenEastDoor));
            default:
                return false;
        }
    }

    private static int bitOrNeg(Map<Integer, Integer> bitByItem, int itemId) {
        Integer b = bitByItem.get(itemId);
        return (b == null) ? -1 : b.intValue();
    }

    private static boolean hasItem(Map<Integer, Integer> bitByItem, long mask, int itemId) {
        if (itemId <= 0) {
            return false;
        }
        int b = bitOrNeg(bitByItem, itemId);
        return b >= 0 && (mask & (1L << b)) != 0L;
    }

    private static boolean isTradeCell(WizardryData.MazeCell c) {
        return c != null && c.tradeItem2 > 0;
    }

    private static boolean isTradeActionable(WizardryData.MazeCell c, Map<Integer, Integer> bitByItem, long mask) {
        if (!isTradeCell(c)) {
            return false;
        }
        if (c.tradeItem1 <= 0) {
            return true;
        }
        return hasItem(bitByItem, mask, c.tradeItem1);
    }

    private static boolean tradeWouldChangeMask(WizardryData.MazeCell c, Map<Integer, Integer> bitByItem, long mask) {
        if (!isTradeCell(c)) {
            return false;
        }
        if (!isTradeActionable(c, bitByItem, mask)) {
            return false;
        }

        long after = mask;

        if (c.tradeItem1 > 0) {
            int t1b = bitOrNeg(bitByItem, c.tradeItem1);
            if (t1b >= 0) {
                after &= ~(1L << t1b); // traded away
            }
        }

        int t2b = bitOrNeg(bitByItem, c.tradeItem2);
        if (t2b >= 0) {
            after |= (1L << t2b); // received
        }

        return after != mask;
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

    private static String debugInventory(WizardryData.Scenario sc, long mask, Map<Integer, Integer> bitByItem) {
        if (mask == 0L) {
            return "(empty)";
        }
        TreeMap<Integer, Integer> sorted = new TreeMap<>();
        for (Map.Entry<Integer, Integer> e : bitByItem.entrySet()) {
            if ((mask & (1L << e.getValue())) != 0L) {
                sorted.put(e.getKey(), e.getValue());
            }
        }
        if (sorted.isEmpty()) {
            return "(empty)";
        }
        StringBuilder sb = new StringBuilder();
        for (Integer id : sorted.keySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(itemLabel(sc, id));
        }
        return sb.toString();
    }

    private static String debugCellDescription(WizardryData.Scenario sc, int lvlIdx, int x, int y) {
        WizardryData.MazeCell c = sc.levels()[lvlIdx].cells[x][y];
        if (c == null) {
            return "null-cell";
        }
        ArrayList<String> parts = new ArrayList<>();
        if (c.itemObtained > 0) {
            parts.add("gives=" + itemLabel(sc, c.itemObtained));
        }
        if (c.itemObtainedFromRiddle > 0) {
            parts.add("riddle=" + itemLabel(sc, c.itemObtainedFromRiddle));
        }
        if (c.encounterGiveItem > 0) {
            parts.add("enc-gives=" + itemLabel(sc, c.encounterGiveItem));
        }
        if (c.encounterTakeItem > 0) {
            parts.add("enc-takes=" + itemLabel(sc, c.encounterTakeItem));
        }
        if (c.tradeItem2 > 0) {
            parts.add("trade " + itemLabel(sc, c.tradeItem1) + "->" + itemLabel(sc, c.tradeItem2));
        }
        if (c.itemRequired > 0) {
            parts.add("GATE=" + itemLabel(sc, c.itemRequired));
        }
        if (c.encounterID >= 0) {
            parts.add("encounter#" + c.encounterID);
        }
        if (c.stairs) {
            parts.add("stairs->L" + (c.addressTo != null ? c.addressTo.level : "?"));
        }
        if (c.chute) {
            parts.add("chute->L" + (c.addressTo != null ? c.addressTo.level : "?"));
        }
        if (c.teleport) {
            parts.add("teleport->L" + (c.addressTo != null ? c.addressTo.level : "?"));
        }
        if (c.elevator) {
            parts.add("elevator[" + c.elevatorFrom + "-" + c.elevatorTo + "]");
        }
        if (c.pit) {
            parts.add("PIT");
        }
        if (c.damage != null) {
            parts.add("DAMAGE");
        }
        if (c.darkness) {
            parts.add("dark");
        }
        return parts.isEmpty() ? "plain" : String.join(", ", parts);
    }

}
