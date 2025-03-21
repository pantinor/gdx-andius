package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
import andius.objects.Direction;
import andius.Andius.ExplosionDrawable;
import static andius.Andius.mainGame;
import static andius.Andius.startScreen;
import static andius.Constants.HITMSGS;
import static andius.Constants.TILE_DIM;
import static andius.WizardryData.DUNGEON_DIM;
import andius.WizardryData.MazeAddress;
import andius.objects.PlayerCursor;
import andius.objects.Dice;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.MonsterCursor;
import andius.objects.Mutable;
import andius.objects.MutableMonster;
import andius.objects.ProjectileActor;
import andius.objects.Reward;
import andius.objects.SaveGame;
import andius.objects.SpellUtil;
import andius.objects.Spells;
import java.util.Iterator;
import java.util.List;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;
import utils.Utils;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import utils.LogScrollPane;

public class CombatScreen extends BaseScreen {

    public static int AREA_CREATURES = 28;
    public final static int MAP_DIM = 13;

    private final Mutable[] crSlots;

    public final Map contextMap;
    public final boolean hasTreasure;
    public final andius.objects.Actor opponent;
    private final Context context;
    private final TiledMap tmap;
    private final OrthogonalTiledMapRenderer renderer;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final SpriteBatch batch;
    private final CombatInputProcessor cip;
    private final Viewport mapViewPort;

    public final Set<andius.objects.Actor> enemies = new LinkedHashSet<>();
    public final Set<andius.objects.Actor> partyMembers = new LinkedHashSet<>();
    private int activeIndex = -1;

    private Texture frame;
    private final LogScrollPane logs;
    public final Stage hudStage;
    private final CombatHud hud;

    public CombatScreen(Context context, Map contextMap, TiledMap tmap, andius.objects.Actor opponent, int level, boolean hasTreasure) {

        this.contextMap = contextMap;
        this.hasTreasure = hasTreasure;
        this.opponent = opponent;
        this.context = context;
        this.tmap = tmap;
        this.renderer = new OrthogonalTiledMapRenderer(this.tmap);

        this.frame = new Texture(Gdx.files.classpath("assets/data/combat_frame.png"));

        this.logs = new LogScrollPane(Andius.skin, new Table(), 275);
        this.logs.setBounds(732, 27, 275, 273);
        this.hudStage = new Stage();
        this.hudStage.addActor(this.logs);

        MapProperties prop = tmap.getProperties();
        mapPixelHeight = prop.get("height", Integer.class) * TILE_DIM;

        camera = new OrthographicCamera(Andius.MAP_VIEWPORT_DIM, Andius.MAP_VIEWPORT_DIM);

        mapViewPort = new ScreenViewport(camera);

        stage = new Stage();
        stage.setViewport(mapViewPort);

        batch = new SpriteBatch();

        cip = new CombatInputProcessor();
        crSlots = new Mutable[AREA_CREATURES];

        if (this.opponent.getEnemy() instanceof MutableMonster) {
            Monster m = (Monster) opponent.getEnemy().baseType();
            fillCreatureTable(level, m);
        }

        MapLayer mLayer = tmap.getLayers().get("Monster Positions");
        Iterator<MapObject> iter = mLayer.getObjects().iterator();
        while (iter.hasNext()) {
            MapObject obj = iter.next();
            int index = Integer.parseInt(obj.getName()) - 1;
            float x = obj.getProperties().get("x", Float.class);
            float y = obj.getProperties().get("y", Float.class);
            int sx = (int) (x / TILE_DIM);
            int sy = MAP_DIM - 1 - (int) (y / TILE_DIM);

            if (crSlots[index] == null) {
                continue;
            }

            andius.objects.Actor actor = new andius.objects.Actor(crSlots[index].name());
            actor.set(crSlots[index], Role.MONSTER, sx, sy, x, y, MovementBehavior.ATTACK_AVATAR, null);

            MonsterCursor cursor = new MonsterCursor();
            cursor.setX(x);
            cursor.setY(y);
            stage.addActor(cursor);
            actor.getEnemy().setHealthCursor(cursor);

            enemies.add(actor);
        }

        MapLayer pLayer = tmap.getLayers().get("Player Positions");
        iter = pLayer.getObjects().iterator();
        while (iter.hasNext()) {
            MapObject obj = iter.next();
            int index = Integer.parseInt(obj.getName()) - 1;
            float x = obj.getProperties().get("x", Float.class);
            float y = obj.getProperties().get("y", Float.class);
            int sx = (int) (x / TILE_DIM);
            int sy = MAP_DIM - 1 - (int) (y / TILE_DIM);

            if (index >= context.players().length) {
                continue;
            }
            if (this.context.players()[index].isDead()) {
                continue;
            }

            andius.objects.Actor actor = new andius.objects.Actor(context.players()[index].name);

            actor.set(this.context.players()[index], sx, sy, x - 20, y + 8);
            actor.setDirection(2);

            PlayerCursor cursor = new PlayerCursor();
            cursor.setX(x);
            cursor.setY(y);
            stage.addActor(cursor);
            actor.setPlayerCursor(cursor);
            actor.adjustHP(0);
            partyMembers.add(actor);
        }

        hud = new CombatHud(this, this.partyMembers);
        andius.objects.Actor pm = getAndSetNextActivePlayer();
        hud.set(pm, hudStage);

        setMapPixelCoords(newMapPixelCoords, 6, 6, 0);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(this, hudStage));
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        renderer.dispose();
        batch.dispose();
    }

    @Override
    public void log(String s) {
        this.logs.add(s);
        this.contextMap.getScreen().log(s);
    }

    private void fillCreatureTable(int level, Monster monster) {
        int maxGroups = Math.min(level + 1, 4);
        int numCreatures = monster.getGroupSize().roll();
        for (int i = 0; i < numCreatures && nextOpenSlot() != -1; i++) {
            int j = nextOpenSlot();
            crSlots[j] = new MutableMonster(monster);
        }
        addPartners(monster, 1, maxGroups);
    }

    private void addPartners(Monster monster, int groupCount, int maxGroups) {
        if (groupCount > maxGroups) {
            return;
        }

        if (nextOpenSlot() == -1) {
            return;
        }

        if (monster.getPartnerOdds() == 0) {
            return;
        }

        Monster partner = this.contextMap.scenario().monsters().get(monster.getPartnerID());

        int numPartners = 0;
        boolean hasPartner = this.rand.nextInt(100) + 1 < monster.getPartnerOdds();
        if (hasPartner) {
            numPartners = partner.getGroupSize().roll();
        } else {
            return;
        }

        for (int i = 0; i < numPartners && nextOpenSlot() != -1; i++) {
            int j = nextOpenSlot();
            crSlots[j] = new MutableMonster(this.contextMap.scenario().monsters().get(monster.getPartnerID()));
        }

        addPartners(partner, groupCount + 1, maxGroups);
    }

    private int nextOpenSlot() {
        for (int i = 0; i < AREA_CREATURES; i++) {
            if (crSlots[i] == null) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void setMapPixelCoords(Vector3 v, int x, int y, int z) {
        v.set(x * TILE_DIM, mapPixelHeight - y * TILE_DIM, 0);
    }

    @Override
    public void getCurrentMapCoords(Vector3 v) {
    }

    @Override
    public void render(float delta) {

        time += delta;

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.position.set(newMapPixelCoords.x + 3 * TILE_DIM + 24 + 8, newMapPixelCoords.y - 1 * TILE_DIM, 0);

        camera.update();

        renderer.setView(camera.combined,
                camera.position.x - TILE_DIM * 10,
                camera.position.y - TILE_DIM * 6,
                Andius.MAP_VIEWPORT_DIM,
                Andius.MAP_VIEWPORT_DIM);

        renderer.render();

        stage.act();
        stage.draw();

        renderer.getBatch().begin();
        for (andius.objects.Actor cr : enemies) {
            float tx = cr.getX() + TILE_DIM / 2;
            float ty = cr.getY() - TILE_DIM + TILE_DIM / 2;
            renderer.getBatch().draw(cr.getIcon(), tx, ty);
        }

        int x = 0;
        float cx = 0, cy = 0;
        for (andius.objects.Actor p : partyMembers) {
            if (!p.getPlayer().isDead()) {
                renderer.getBatch().draw(p.getIcon(), p.getX(), p.getY());
            }
            if (x == this.activeIndex) {
                cx = p.getPlayerCursor().getX() + TILE_DIM / 2;
                cy = p.getPlayerCursor().getY() + TILE_DIM / 2;
            }
            x++;
        }

        renderer.getBatch().end();

//        for (andius.objects.Actor cr : enemies) {
//            TextureRegion tr = cr.getIcon();
//            float tx = cr.getX() + TILE_DIM / 2;
//            float ty = cr.getY() - TILE_DIM + TILE_DIM / 2;
//            tx -= cr.iconCenter().x;
//            ty += cr.iconCenter().y;
//            Gdx.gl.glLineWidth(1);
//            shapeRenderer.setProjectionMatrix(camera.combined);
//            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//            shapeRenderer.setColor(255, 255, 0, .50f);//yellow
//            shapeRenderer.box(tx, ty, 0, tr.getRegionWidth(), tr.getRegionHeight(), 0);
//            shapeRenderer.setColor(0, 255, 0, .50f);
//            shapeRenderer.circle(tx + tr.getRegionWidth() / 2, ty + tr.getRegionHeight() / 2, 3);
//            shapeRenderer.setColor(255, 0, 0, .50f);
//            shapeRenderer.circle(tx + cr.iconCenter().x, ty + cr.iconCenter().y, 3);
//            shapeRenderer.end();
//        }
        batch.begin();
        batch.draw(this.frame, 0, 0);
        batch.end();

        int pointerx = (int) currentMousePos.x - 1 * TILE_DIM;
        int pointery = (int) (Andius.SCREEN_HEIGHT - currentMousePos.y - 2 * TILE_DIM);

        if (pointerx > MAP_DIM * TILE_DIM || pointerx < 0 || pointery > MAP_DIM * TILE_DIM || pointery < 0) {
            //off map
        } else {
            if (cip.active) {
                Gdx.gl.glLineWidth(3);
                Gdx.gl.glEnable(GL20.GL_BLEND);
                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.begin(ShapeType.Line);
                shapeRenderer.setColor(255, 255, 0, .50f);//yellow
                for (andius.objects.Actor c : this.enemies) {
                    float regx = Math.abs(c.getEnemy().getHealthCursor().getX() + TILE_DIM / 2 - pointerx);
                    float regy = Math.abs(c.getEnemy().getHealthCursor().getY() + TILE_DIM / 2 - pointery);
                    if (regx < 20 && regy < 20) {
                        Item weapon = cip.player.getPlayer().weapon == null ? Item.HANDS : cip.player.getPlayer().weapon;
                        int range = weapon.range == 0 ? 1 : weapon.range;
                        float a = Math.abs(cip.player.getWx() - c.getWx());
                        float b = Math.abs(cip.player.getWy() - c.getWy());
                        double distance = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
                        if ((double) range + 0.5 >= distance) {
                            shapeRenderer.setColor(255, 0, 0, .50f);//red
                        } else {
                            shapeRenderer.setColor(200, 200, 200, .50f);//grey
                        }
                        break;
                    }
                }
                for (andius.objects.Actor c : this.partyMembers) {
                    float regx = Math.abs(c.getPlayerCursor().getX() + TILE_DIM / 2 - pointerx);
                    float regy = Math.abs(c.getPlayerCursor().getY() + TILE_DIM / 2 - pointery);
                    if (regx < 20 && regy < 20) {
                        shapeRenderer.setColor(0, 255, 0, .50f);//green
                        break;
                    }
                }
                shapeRenderer.line(cx, cy, pointerx, pointery);
                shapeRenderer.end();
            } else {
                for (andius.objects.Actor c : this.enemies) {
                    float regx = Math.abs(c.getEnemy().getHealthCursor().getX() + TILE_DIM / 2 - pointerx);
                    float regy = Math.abs(c.getEnemy().getHealthCursor().getY() + TILE_DIM / 2 - pointery);
                    if (regx < 20 && regy < 20) {
                        batch.begin();
                        hud.drawStatsMonster(batch, c);
                        batch.end();
                        break;
                    }
                }
                for (andius.objects.Actor c : this.partyMembers) {
                    float regx = Math.abs(c.getPlayerCursor().getX() + TILE_DIM / 2 - pointerx);
                    float regy = Math.abs(c.getPlayerCursor().getY() + TILE_DIM / 2 - pointery);
                    if (regx < 20 && regy < 20) {
                        batch.begin();
                        hud.drawStats(batch, c);
                        batch.end();
                        break;
                    }
                }
            }
        }

        hudStage.act();
        hudStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        mapViewPort.update(width, height, false);
    }

    @Override
    public boolean keyUp(int keycode) {

        andius.objects.Actor active = getActivePartyMember();

        if (active != null) {
            if (active.getPlayer().isDisabled()) {
                log("Incapacitated!");
            } else if (keycode == Keys.SPACE) {
                log("Pass");
            } else if (keycode == Keys.UP) {
                if (preMove(active, Direction.NORTH)) {
                    active.setWy(active.getWy() - 1);
                    active.setY(active.getY() + TILE_DIM);
                    active.setDirection(2);
                    active.getPlayerCursor().setY(active.getPlayerCursor().getY() + TILE_DIM);
                }
            } else if (keycode == Keys.DOWN) {
                if (preMove(active, Direction.SOUTH)) {
                    active.setWy(active.getWy() + 1);
                    active.setY(active.getY() - TILE_DIM);
                    active.setDirection(0);
                    active.getPlayerCursor().setY(active.getPlayerCursor().getY() - TILE_DIM);
                }
            } else if (keycode == Keys.RIGHT) {
                if (preMove(active, Direction.EAST)) {
                    active.setWx(active.getWx() + 1);
                    active.setX(active.getX() + TILE_DIM);
                    active.setDirection(1);
                    active.getPlayerCursor().setX(active.getPlayerCursor().getX() + TILE_DIM);
                }
            } else if (keycode == Keys.LEFT) {
                if (preMove(active, Direction.WEST)) {
                    active.setWx(active.getWx() - 1);
                    active.setX(active.getX() - TILE_DIM);
                    active.setDirection(3);
                    active.getPlayerCursor().setX(active.getPlayerCursor().getX() - TILE_DIM);
                }
            } else if (keycode == Keys.A) {
                Gdx.input.setInputProcessor(cip);
                cip.init(active, keycode, null, active.getWx(), active.getWy());
                return false;
            } else if (keycode == Keys.N) {
            } else if (keycode == Keys.U) {
            } else if (keycode == Keys.R) {
            } else if (keycode == Keys.W) {
            }
        }

        finishPlayerTurn();

        return false;

    }

    private boolean preMove(andius.objects.Actor active, Direction dir) {

        int nx = active.getWx(), ny = active.getWy();
        switch (dir) {
            case WEST:
                nx--;
                break;
            case NORTH:
                ny--;
                break;
            case EAST:
                nx++;
                break;
            case SOUTH:
                ny++;
                break;
        }

        if (nx < 0 || ny < 0 || nx >= MAP_DIM || ny >= MAP_DIM) {
            this.partyMembers.remove(active);
            active.getPlayerCursor().remove();
            Sounds.play(Sound.FLEE);
            return false;
        }

        int mask = getValidMovesMask(active.getWx(), active.getWy(), active);
        if (!Direction.isDirInMask(dir, mask)) {
            Sounds.play(Sound.BLOCKED);
            return false;
        }

        return true;
    }

    public void finishPlayerTurn() {

        //remove dead creatures
        Iterator<andius.objects.Actor> iter = this.enemies.iterator();
        while (iter.hasNext()) {
            andius.objects.Actor c = iter.next();
            if (c.getEnemy().getCurrentHitPoints() <= 0) {
                iter.remove();
                c.getEnemy().getHealthCursor().remove();
            }
        }

        boolean roundIsDone = isRoundDone() || this.enemies.isEmpty();

        andius.objects.Actor next = getAndSetNextActivePlayer();
        if (next != null) {
            hud.set(next, hudStage);
        }

        if (roundIsDone) {
            finishTurn(0, 0);
        }
    }

    @Override
    public void finishTurn(int currentX, int currentY) {

        if (this.enemies.isEmpty()) {
            end();
            return;
        }

        if (this.partyMembers.isEmpty()) {
            end();
            return;
        }

        Gdx.input.setInputProcessor(null);

        final SequenceAction seq = Actions.action(SequenceAction.class);

        for (andius.objects.Actor cr : this.enemies) {
            seq.addAction(Actions.run(new Runnable() {
                @Override
                public void run() {
                    creatureAction(seq, cr);
                }
            }));
            seq.addAction(Actions.delay(.04f));
        }

        seq.addAction(Actions.run(new Runnable() {
            @Override
            public void run() {
                Gdx.input.setInputProcessor(new InputMultiplexer(CombatScreen.this, hudStage));
                if (partyMembers.isEmpty()) {
                    end();
                } else if (getNextAblePartyMember() == null) {
                    for (andius.objects.Actor a : partyMembers) {
                        a.getPlayer().decrementStatusEffects();
                    }
                    finishTurn(0, 0);
                }
            }
        }));

        stage.addAction(seq);

    }

    @Override
    public void save(SaveGame saveGame) {
    }

    @Override
    public void load(SaveGame saveGame) {
    }

    public static class RemoveCreatureAction implements Runnable {

        private final andius.objects.Actor cr;
        private final CombatScreen screen;

        public RemoveCreatureAction(CombatScreen screen, andius.objects.Actor cr) {
            this.cr = cr;
            this.screen = screen;
        }

        @Override
        public void run() {
            screen.enemies.remove(cr);
            cr.getEnemy().getHealthCursor().remove();
        }
    }

    private int getValidMovesMask(int x, int y, andius.objects.Actor self) {
        int mask = 0;

        TiledMapTileLayer layer = (TiledMapTileLayer) this.tmap.getLayers().get("floor");
        TiledMapTileLayer.Cell north = layer.getCell(x, MAP_DIM - 1 - y + 1);
        TiledMapTileLayer.Cell south = layer.getCell(x, MAP_DIM - 1 - y - 1);
        TiledMapTileLayer.Cell west = layer.getCell(x - 1, MAP_DIM - 1 - y + 0);
        TiledMapTileLayer.Cell east = layer.getCell(x + 1, MAP_DIM - 1 - y + 0);

        mask = addToMask(Direction.NORTH, mask, north, x, y - 1, self);
        mask = addToMask(Direction.SOUTH, mask, south, x, y + 1, self);
        mask = addToMask(Direction.WEST, mask, west, x - 1, y, self);
        mask = addToMask(Direction.EAST, mask, east, x + 1, y, self);

        return mask;
    }

    private int addToMask(Direction dir, int mask, TiledMapTileLayer.Cell cell, int x, int y, andius.objects.Actor self) {
        if (cell != null) {

            for (andius.objects.Actor c : this.enemies) {
                if (c.equals(self)) {
                    continue;
                }
                if (c.getWx() == x && c.getWy() == y) {
                    return mask;
                }
            }

            for (andius.objects.Actor c : this.partyMembers) {
                if (c.equals(self)) {
                    continue;
                }
                if (c.getWx() == x && c.getWy() == y) {
                    return mask;
                }
            }

            mask = Direction.addToMask(dir, mask);
        }
        return mask;
    }

    public void end() {

        java.util.List<SaveGame.CharacterRecord> lastMenStanding = new ArrayList<>();
        for (andius.objects.Actor player : partyMembers) {
            player.getPlayer().acmodifier1 = 0;
            if (!player.getPlayer().isDisabled()) {
                lastMenStanding.add(player.getPlayer());
            }
        }

        boolean isWon = this.enemies.isEmpty();
        if (isWon) {
            int goldRewardId = 0;
            int chestRewardId = 0;
            int exp = 0;

            for (Mutable mm : crSlots) {
                if (mm != null) {
                    Monster m = (Monster) mm.baseType();
                    if (m.getExp() > exp) {
                        exp = m.getExp();
                    }
                    if (m.getGoldReward() > goldRewardId) {
                        goldRewardId = m.getGoldReward();
                    }
                    if (m.getChestReward() > chestRewardId) {
                        chestRewardId = m.getChestReward();
                    }
                }
            }

            this.contextMap.getScreen().endCombat(isWon, this.opponent);

            for (SaveGame.CharacterRecord c : lastMenStanding) {
                c.awardXP(exp / lastMenStanding.size());
                log(String.format("%s gained %d experience points.", c.name.toUpperCase(), exp / lastMenStanding.size()));
            }

            if (this.hasTreasure) {
                mainGame.setScreen(new RewardScreen(this.context, this.contextMap, 1, exp, chestRewardId));
            } else {
                Reward gold = contextMap.scenario().rewards().get(goldRewardId);
                int goldAmt = gold.goldAmount();
                for (SaveGame.CharacterRecord c : lastMenStanding) {
                    c.adjustGold(goldAmt / lastMenStanding.size());
                    this.contextMap.getScreen().log(String.format("%s found %d gold.", c.name.toUpperCase(), goldAmt));
                }
                mainGame.setScreen(this.contextMap.getScreen());
            }

        } else {
            boolean anyoneAlive = false;
            for (SaveGame.CharacterRecord ch : this.context.players()) {
                if (!ch.isDead()) {
                    anyoneAlive = true;
                    break;
                }
            }

            if (anyoneAlive) {
                this.contextMap.getScreen().endCombat(false, this.opponent);
                mainGame.setScreen(this.contextMap.getScreen());
            } else {
                mainGame.setScreen(startScreen);
            }
        }
    }

    public void castCombatMalor() {
        //end combat random teleport to same lavel coordinates
        if (this.contextMap.getScreen() instanceof WizardryDungeonScreen) {
            end();
            WizardryDungeonScreen scr = (WizardryDungeonScreen) this.contextMap.getScreen();
            int randx = Utils.RANDOM.nextInt(DUNGEON_DIM);
            int randy = Utils.RANDOM.nextInt(DUNGEON_DIM);
            scr.teleport(new MazeAddress(scr.currentLevel + 1, randx, randy), true);
        } else {
            Sounds.play(Sound.EVADE);
        }
    }

    @Override
    public void teleport(int level, int stepsX, int stepsY) {

    }

    @Override
    public void partyDeath() {
        //not used here
    }

    private void creatureAction(SequenceAction seq, andius.objects.Actor creature) {

        try {

            if (creature.getEnemy().status().isDisabled()) {
                return;
            }

            AtomicInteger dist = new AtomicInteger(0);
            final andius.objects.Actor target = nearestPartyMember(creature.getWx(), creature.getWy(), dist);
            if (target == null) {
                return;
            }

            CombatAction action = CombatAction.ATTACK;
            Spells spell = null;

            if (creature.getEnemy().breath() != Breath.NONE && rand.nextInt(100) < 60) {
                action = CombatAction.BREATH;
            }

            if (creature.getEnemy().getCurrentMageSpellLevel() > 0 && !creature.getEnemy().status().has(Status.SILENCED) && rand.nextInt(100) < 75) {
                spell = creature.getEnemy().castMageSpell();
                action = CombatAction.CAST;
            }

            if (action != CombatAction.CAST) {
                if (creature.getEnemy().getCurrentPriestSpellLevel() > 0 && !creature.getEnemy().status().has(Status.SILENCED) && rand.nextInt(100) < 75) {
                    spell = creature.getEnemy().castPriestSpell();
                    action = CombatAction.CAST;
                }
            }

            if (action == CombatAction.ATTACK && dist.get() > 1) {
                action = CombatAction.ADVANCE;
            }

            switch (action) {
                case BREATH:
                    log(String.format("%s breathes %s", creature.getEnemy().name(), creature.getEnemy().breath()));
                    for (andius.objects.Actor pm : partyMembers) {
                        int d = creature.getEnemy().getCurrentHitPoints() / 2;
                        if (pm.getPlayer().savingThrowBreath()) {
                            log(String.format("%s made a saving throwing throw against %s", pm.getPlayer().name, creature.getEnemy().breath()));
                            d = d / 2;
                        }
                        damagePlayer(seq, creature, pm, d);
                    }
                    break;
                case ATTACK:
                    Sounds.play(Sound.NPC_ATTACK);
                    boolean hit = Utils.attackHit(creature.getEnemy(), target.getPlayer());
                    if (hit) {
                        for (Dice dice : creature.getEnemy().getDamage()) {
                            damagePlayer(seq, creature, target, dice.roll());
                        }
                    } else {
                        log(String.format("%s misses %s", creature.getEnemy().name(), target.getPlayer().name));
                    }
                    break;
                case CAST: {
                    log(String.format("%s casts %s", creature.getEnemy().name(), spell));
                    SpellUtil.spellMonsterCast(this, seq, spell, creature, target);
                    break;
                }
                case FLEE:
                case ADVANCE: {
                    moveCreature(action, creature, target.getWx(), target.getWy());
                    if (creature.getWx() >= MAP_DIM || creature.getWy() < 0
                            || creature.getWy() >= MAP_DIM || creature.getWy() < 0) {
                        log(String.format("%s Flees!", creature.getEnemy().name()));
                        Sounds.play(Sound.EVADE);
                        return;
                    }
                    break;
                }
            }
        } finally {
            creature.getEnemy().processStatusAffects();
        }

    }

    private void damagePlayer(SequenceAction seq, andius.objects.Actor creature, andius.objects.Actor target, int damage) {
        target.adjustHP(-damage);
        log(String.format("%s %s %s for %d damage!",
                creature.getEnemy().name(),
                HITMSGS[rand.nextInt(HITMSGS.length)],
                target.getPlayer().name,
                damage));

        Actor d = new ExplosionDrawable(Andius.EXPLMAP.get(Color.RED));
        d.setX(target.getX() + 12);
        d.setY(target.getY() + 12);
        d.addAction(Actions.sequence(Actions.delay(.3f), Actions.removeActor()));

        seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
        seq.addAction(Actions.run(new AddActorAction(stage, d)));

        if (target.getPlayer().isDead()) {
            seq.addAction(Actions.run(new Runnable() {
                @Override
                public void run() {
                    partyMembers.remove(target);
                    target.getPlayerCursor().remove();
                }
            }));
        }

        stage.addAction(seq);
    }

    private andius.objects.Actor nearestPartyMember(int fromX, int fromY, AtomicInteger dist) {
        andius.objects.Actor nearest = null;
        int d = 0;
        int leastDist = 0xFFFF;

        for (andius.objects.Actor pm : partyMembers) {

            if (pm.getPlayer().isDead()) {
                continue;
            }

            d = Utils.movementDistance(fromX, fromY, pm.getWx(), pm.getWy());

            /* skip target 50% of time if same distance */
            if (d < leastDist || (d == leastDist && rand.nextInt(2) == 0)) {
                nearest = pm;
                leastDist = d;
            }
        }

        if (nearest != null) {
            dist.set(leastDist);
        }

        return nearest;
    }

    private boolean moveCreature(CombatAction action, andius.objects.Actor cr, int targetX, int targetY) {

        int nx = cr.getWx();
        int ny = cr.getWy();

        int mask = getValidMovesMask(nx, ny, cr);
        Direction dir;

        if (action == CombatAction.FLEE) {
            dir = Utils.getPath(targetX, targetY, mask, false, nx, ny);
        } else {
            dir = Utils.getPath(targetX, targetY, mask, true, nx, ny);
        }

        if (dir == Direction.NORTH) {
            cr.setWy(--ny);
            cr.setY(cr.getY() + TILE_DIM);
            cr.getEnemy().getHealthCursor().setY(cr.getEnemy().getHealthCursor().getY() + TILE_DIM);
        }
        if (dir == Direction.SOUTH) {
            cr.setWy(++ny);
            cr.setY(cr.getY() - TILE_DIM);
            cr.getEnemy().getHealthCursor().setY(cr.getEnemy().getHealthCursor().getY() - TILE_DIM);
        }
        if (dir == Direction.EAST) {
            cr.setWx(++nx);
            cr.setX(cr.getX() + TILE_DIM);
            cr.getEnemy().getHealthCursor().setX(cr.getEnemy().getHealthCursor().getX() + TILE_DIM);
        }
        if (dir == Direction.WEST) {
            cr.setWx(--nx);
            cr.setX(cr.getX() - TILE_DIM);
            cr.getEnemy().getHealthCursor().setX(cr.getEnemy().getHealthCursor().getX() - TILE_DIM);
        }

        return true;

    }

    private andius.objects.Actor getActivePartyMember() {
        return !this.partyMembers.isEmpty() && activeIndex < this.partyMembers.size() ? getPartyMember(activeIndex) : null;
    }

    private andius.objects.Actor getNextAblePartyMember() {
        int tmp = activeIndex;
        if (tmp >= this.partyMembers.size()) {
            tmp = 0;
        }
        for (int i = tmp; i < partyMembers.size(); i++) {
            andius.objects.Actor a = getPartyMember(i);
            if (!a.getPlayer().isDisabled()) {
                return a;
            }
            tmp++;
        }

        return null;
    }

    private boolean isRoundDone() {
        int tmp = activeIndex;
        tmp++;
        if (tmp >= this.partyMembers.size()) {
            return true;
        }
        boolean allDeadRoundIsDone = true;
        for (andius.objects.Actor player : partyMembers) {
            if (!player.getPlayer().isDead()) {
                allDeadRoundIsDone = false;
            }
        }
        return allDeadRoundIsDone;
    }

    private andius.objects.Actor getAndSetNextActivePlayer() {

        for (andius.objects.Actor p : partyMembers) {
            PlayerCursor ca = p.getPlayerCursor();
            ca.setVisible(false);
        }

        this.activeIndex++;
        if (activeIndex >= this.partyMembers.size()) {
            activeIndex = 0;
        }

        andius.objects.Actor p = getPartyMember(activeIndex);
        if (p != null) {
            p.getPlayerCursor().setVisible(true);
            p.getPlayer().decrementStatusEffects();
        }

        return p;

    }

    private andius.objects.Actor getPartyMember(int index) {
        int i = 0;
        for (andius.objects.Actor player : partyMembers) {
            if (index == i) {
                return player;
            }
            i++;
        }
        return null;
    }

    private void animateWeaponAttack(andius.objects.Actor attacker, Direction dir, andius.objects.Actor target) {

        AttackVector av = null;
        if (dir != null) {
            av = attack(attacker, dir);
        } else {
            av = attack(attacker, target);
        }

        int tx = av.x * TILE_DIM;
        int ty = mapPixelHeight - av.y * TILE_DIM - TILE_DIM;

        final SequenceAction seq = Actions.action(SequenceAction.class);

        final ProjectileActor p = new ProjectileActor(Color.YELLOW, attacker.getX() + 20, attacker.getY() - 8);

        Action after = new Action() {
            @Override
            public boolean act(float delta) {
                p.remove();
                stage.addAction(seq);
                return false;
            }
        };

        if (av.result == AttackResult.HIT) {

            Actor d = new ExplosionDrawable(Andius.EXPLMAP.get(Color.RED));
            d.setX(tx + 12);
            d.setY(ty + 12);
            d.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

            p.addAction(Actions.sequence(
                    Actions.run(new PlaySoundAction(Sound.PC_ATTACK)),
                    Actions.moveTo(tx, ty, av.distance * .1f),
                    Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)),
                    Actions.delay(.1f),
                    Actions.run(new AddActorAction(stage, d)),
                    after));

        } else {

            p.addAction(Actions.sequence(
                    Actions.run(new PlaySoundAction(Sound.PC_ATTACK)),
                    Actions.moveTo(tx, ty, av.distance * .1f),
                    Actions.delay(.1f),
                    Actions.run(new PlaySoundAction(Sound.EVADE)),
                    after));
        }

        seq.addAction(Actions.run(new Runnable() {
            @Override
            public void run() {
                finishPlayerTurn();
            }
        }));

        stage.addActor(p);
    }

    private AttackVector attack(andius.objects.Actor attacker, andius.objects.Actor target) {
        Item weapon = attacker.getPlayer().weapon == null ? Item.HANDS : attacker.getPlayer().weapon;
        int range = weapon.range == 0 ? 1 : weapon.range;

        AttackVector av = getDirectionalActionPath(target, attacker.getWx(), attacker.getWy(), range);
        av.result = AttackResult.MISS;
        if (av.victim != null && av.victim.getEnemy() != null) {
            for (int j = 0; j < attacker.getPlayer().extraSwings(); j++) {
                boolean hit = Utils.attackHit(attacker.getPlayer(), av.victim.getEnemy());
                if (hit) {
                    av.result = AttackResult.HIT;
                    int damage = Utils.dealDamage(weapon, av.victim.getEnemy());
                    log(av.victim.getEnemy().getDamageDescription(attacker.getPlayer().name, damage));
                } else {
                    log(String.format("%s misses %s", attacker.getPlayer().name.toUpperCase(), av.victim.getEnemy().name().toUpperCase()));
                }
            }
        } else {
            log("No target.");
        }

        return av;
    }

    private AttackVector attack(andius.objects.Actor attacker, Direction dir) {

        Item weapon = attacker.getPlayer().weapon == null ? Item.HANDS : attacker.getPlayer().weapon;
        int range = weapon.range == 0 ? 1 : weapon.range;

        List<AttackVector> path = getDirectionalActionPath(MAP_DIM, MAP_DIM, dir.mask(), attacker.getWx(), attacker.getWy(), 0, range);

        AttackVector av = null;
        for (int i = 0; i < path.size(); i++) {
            av = path.get(i);
            for (andius.objects.Actor c : this.enemies) {
                if (c.getWx() == av.x && c.getWy() == av.y) {
                    av.victim = c;
                    break;
                }
            }
            if (av.victim != null) {
                av.result = AttackResult.MISS;
                for (int j = 0; j < attacker.getPlayer().extraSwings(); j++) {
                    boolean hit = Utils.attackHit(attacker.getPlayer(), av.victim.getEnemy());
                    if (hit) {
                        av.result = AttackResult.HIT;
                        int damage = Utils.dealDamage(weapon, av.victim.getEnemy());
                        log(av.victim.getEnemy().getDamageDescription(attacker.getPlayer().name, damage));
                    } else {
                        log(String.format("%s misses %s", attacker.getPlayer().name.toUpperCase(), av.victim.getEnemy().name().toUpperCase()));
                    }
                }
            } else {
                log("No target.");
            }
        }

        return av;
    }

    private List<AttackVector> getDirectionalActionPath(int mapWidth, int mapHeight, int dirmask, int x, int y, int minDistance, int maxDistance) {

        List<AttackVector> path = new ArrayList<>();

        /*
         * try every tile in the given direction, up to the given range.
         * Stop when the the range is exceeded, or the action is blocked.
         */
        int nx = x;
        int ny = y;
        int count = 1;

        for (int distance = minDistance; distance <= maxDistance; distance++) {

            /* make sure our action isn't taking us off the map */
            if (nx > mapWidth - 1 || nx < 0 || ny > mapHeight - 1 || ny < 0) {
                break;
            }

            boolean blocked = false;
            TiledMapTileLayer layer = (TiledMapTileLayer) this.tmap.getLayers().get("floor");
            TiledMapTileLayer.Cell cell = layer.getCell(nx, MAP_DIM - 1 - ny + 1);
            if (cell != null) {
                for (andius.objects.Actor c : this.enemies) {
                    if (c.getWx() == nx && c.getWy() == ny) {
                        blocked = true;
                    }
                }
            } else {
                blocked = true;
            }

            if (blocked) {
                path.add(new AttackVector(nx, ny, count));
                break;
            } else {
                path.add(new AttackVector(nx, ny, count));
            }

            if (Direction.isDirInMask(Direction.NORTH, dirmask)) {
                ny--;
            }
            if (Direction.isDirInMask(Direction.SOUTH, dirmask)) {
                ny++;
            }
            if (Direction.isDirInMask(Direction.EAST, dirmask)) {
                nx++;
            }
            if (Direction.isDirInMask(Direction.WEST, dirmask)) {
                nx--;
            }
            count++;
        }

        return path;
    }

    private AttackVector getDirectionalActionPath(andius.objects.Actor target, int srcX, int srcY, int maxDistance) {
        int targetX = target.getWx();
        int targetY = target.getWy();

        int a = Math.abs(srcX - targetX);
        int b = Math.abs(srcY - targetY);

        double distance = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
        AttackVector av = new AttackVector(targetX, targetY, (int) Math.round(distance));
        if ((double) maxDistance + 0.5 < distance) {
            log("Too far away! ");
        } else {
            av.victim = target;
        }
        return av;
    }

    public void initCast(Spells spell, andius.objects.Actor player, boolean isItem) {
        Sounds.play(Sound.TRIGGER);
        switch (spell.getTarget()) {
            case MONSTER:
            case PERSON:
                Gdx.input.setInputProcessor(cip);
                cip.init(player, Keys.C, spell, player.getWx(), player.getWy());
                break;
            case NONE:
            case VARIABLE:
            case CASTER:
            case PARTY:
            case GROUP:
                SpellUtil.spellCast(this, context, spell, player, null, isItem);
                break;
            default:
                break;
        }
    }

    private class CombatInputProcessor extends InputAdapter {

        private int code;
        private int x;
        private int y;
        private andius.objects.Actor player;
        private Spells spell;
        private boolean active;

        public void init(andius.objects.Actor player, int code, Spells spell, int x, int y) {

            this.spell = spell;
            this.player = player;
            this.code = code;
            this.x = x;
            this.y = y;
            this.active = true;

            switch (code) {
                case Keys.A:
                    log("ATTACK> ");
                    break;
                case Keys.C:
                    log("CAST> ");
                    break;

            }
        }

        @Override
        public boolean keyUp(int keycode) {
            this.active = false;

            Gdx.input.setInputProcessor(new InputMultiplexer(CombatScreen.this, hudStage));

            Direction dir = Direction.NORTH;

            switch (keycode) {
                case Keys.UP:
                    dir = Direction.NORTH;
                    break;
                case Keys.DOWN:
                    dir = Direction.SOUTH;
                    break;
                case Keys.LEFT:
                    dir = Direction.WEST;
                    break;
                case Keys.RIGHT:
                    dir = Direction.EAST;
                    break;
            }

            log(dir.toString());

            if (this.code == Keys.A) {

                animateWeaponAttack(this.player, dir, null);

            } else if (this.code == Keys.C) {

                SpellUtil.spellCast(CombatScreen.this, CombatScreen.this.context, spell, this.player, null, false);

            }

            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            this.active = false;

            Gdx.input.setInputProcessor(new InputMultiplexer(CombatScreen.this, hudStage));

            int pointerx = screenX - 1 * TILE_DIM;
            int pointery = (Andius.SCREEN_HEIGHT - screenY - 2 * TILE_DIM);

            andius.objects.Actor target = null;

            for (andius.objects.Actor c : enemies) {
                float regx = Math.abs(c.getEnemy().getHealthCursor().getX() + TILE_DIM / 2 - pointerx);
                float regy = Math.abs(c.getEnemy().getHealthCursor().getY() + TILE_DIM / 2 - pointery);
                if (regx < 20 && regy < 20) {
                    target = c;
                    break;
                }
            }

            if (target == null) {
                for (andius.objects.Actor c : partyMembers) {
                    float regx = Math.abs(c.getPlayerCursor().getX() + TILE_DIM / 2 - pointerx);
                    float regy = Math.abs(c.getPlayerCursor().getY() + TILE_DIM / 2 - pointery);
                    if (regx < 20 && regy < 20) {
                        target = c;
                        break;
                    }
                }
            }

            if (target == null) {
                Sounds.play(Sound.NEGATIVE_EFFECT);
                log(String.format("%s %s %s",
                        player.getPlayer().name,
                        HITMSGS[rand.nextInt(HITMSGS.length)],
                        " at nothing."));
                finishPlayerTurn();
                return false;
            }

            if (this.code == Keys.A) {

                animateWeaponAttack(this.player, null, target);

            } else if (this.code == Keys.C) {

                SpellUtil.spellCast(CombatScreen.this, CombatScreen.this.context, spell, this.player, target, false);

            }

            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            currentMousePos.set(screenX, screenY);
            return false;
        }

    }

}
