package andius;

import andius.Andius.ExplosionDrawable;
import static andius.Andius.MONSTERS;
import static andius.Andius.REWARDS;
import static andius.Andius.mainGame;
import static andius.Andius.startScreen;
import static andius.Constants.TILE_DIM;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.MutableMonster;
import andius.objects.ProjectileActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;

import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;
import utils.PartyDeathException;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.utils.Align;
import utils.Utils;

public class CombatScreen extends BaseScreen {

    public static int AREA_CREATURES = 16;
    public static int AREA_PLAYERS = 6;
    public final static int MAP_DIM = 13;

    private final MutableMonster[] crSlots;

    private final CursorActor cursor;
    public final Map contextMap;
    public final andius.objects.Actor opponent;
    private final Monster crType;
    private final Context context;
    private final TiledMap tmap;
    private final OrthogonalTiledMapRenderer renderer;
    private final SpriteBatch batch;
    private final SecondaryInputProcessor sip;
    private final Viewport mapViewPort;
    private final GlyphLayout layout = new GlyphLayout(Andius.smallFont, "", Color.WHITE, 65, Align.left, true);

    public final List<andius.objects.Actor> enemies = new ArrayList<>();
    public final List<andius.objects.Actor> partyMembers = new ArrayList<>();
    private int activeIndex;

    private Texture enemy_hud;
    int[] hud_enmy_x = new int[]{50, 89 + 40, 168 + 40, 247 + 40, 326 + 40, 405 + 40, 484 + 40, 563 + 40};

    public CombatScreen(Context context, Map contextMap, TiledMap tmap, andius.objects.Actor opponent) {

        this.contextMap = contextMap;
        this.opponent = opponent;
        this.crType = opponent.getMonster();
        this.context = context;
        this.tmap = tmap;
        this.renderer = new OrthogonalTiledMapRenderer(this.tmap);

        this.enemy_hud = new Texture(Gdx.files.classpath("assets/data/enemy_hud.png"));

        MapProperties prop = tmap.getProperties();
        mapPixelHeight = prop.get("height", Integer.class) * TILE_DIM;

        camera = new OrthographicCamera(Andius.MAP_VIEWPORT_DIM, Andius.MAP_VIEWPORT_DIM);

        mapViewPort = new ScreenViewport(camera);

        stage = new Stage();
        stage.setViewport(mapViewPort);

        batch = new SpriteBatch();

        sip = new SecondaryInputProcessor();
        crSlots = new MutableMonster[AREA_CREATURES];

        fillCreatureTable();

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

            andius.objects.Actor actor = new andius.objects.Actor(crSlots[index].getIcon());
            actor.set(crSlots[index], Role.MONSTER, sx, sy, x, y, MovementBehavior.ATTACK_AVATAR);
            enemies.add(actor);
//            if (enemies.size() > 0) {
//                break;
//            }
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

            andius.objects.Actor actor = new andius.objects.Actor(context.players()[index].classType.getIcon());
            actor.set(this.context.players()[index], sx, sy, x, y);
            partyMembers.add(actor);
//            if (partyMembers.size() > 0) {
//                break;
//            }
        }

        cursor = new CursorActor();
        cursor.set(this.partyMembers.get(0).getX(), this.partyMembers.get(0).getY());
        stage.addActor(cursor);
        cursor.addAction(forever(sequence(fadeOut(1), fadeIn(1))));

        newMapPixelCoords = getMapPixelCoords(6, 6);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
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

    private void fillCreatureTable() {

        int numCreatures = this.crType.getGroupSize().roll();
        for (int i = 0; i < numCreatures; i++) {
            int j = 0;
            do {
                j = rand.nextInt(AREA_CREATURES);
            } while (crSlots[j] != null);
            crSlots[j] = new MutableMonster(this.crType);
        }

        int numPartners = 0;
        boolean hasPartner = this.rand.nextInt(100) + 1 < this.crType.getPartnerOdds();
        if (hasPartner) {
            Monster partner = MONSTERS.get(this.crType.getPartnerID());
            numPartners = partner.getGroupSize().roll();
        }

        if (numPartners > 0 && numCreatures + numPartners > AREA_CREATURES) {
            numPartners = AREA_CREATURES - numCreatures;
        }

        for (int i = 0; i < numPartners; i++) {
            int j = 0;
            do {
                j = rand.nextInt(AREA_CREATURES);
            } while (crSlots[j] != null);
            crSlots[j] = new MutableMonster(MONSTERS.get(this.crType.getPartnerID()));
        }

    }

    @Override
    public Vector3 getMapPixelCoords(int x, int y) {
        Vector3 v = new Vector3(x * TILE_DIM, mapPixelHeight - y * TILE_DIM, 0);
        return v;
    }

    @Override
    public Vector3 getCurrentMapCoords() {
        return null;
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

        renderer.getBatch().begin();
        for (andius.objects.Actor cr : enemies) {
            renderer.getBatch().draw(cr.getAnimation().getKeyFrame(time, true), cr.getX(), cr.getY() + 8);
        }

        for (andius.objects.Actor p : partyMembers) {
            if (p.getPlayer().status != Status.DEAD) {
                renderer.getBatch().draw(p.getAnimation().getKeyFrame(time, true), p.getX(), p.getY());
            } else {
                //renderer.getBatch().draw(Exodus.corpse, p.getX(), p.getY());
            }
        }

        renderer.getBatch().end();

        batch.begin();
        batch.draw(Andius.backGround, 0, 0);
        batch.draw(this.enemy_hud, 40, 6);

        int y = 78;
        int count = 0, idx = 0;
        for (int i = 0; i < AREA_CREATURES; i++) {
            if (crSlots[i] != null && crSlots[i].getCurrentHitPoints() > 0) {
                layout.setText(Andius.smallFont, crSlots[i].getName().toUpperCase());
                Andius.smallFont.draw(batch, layout, hud_enmy_x[idx], y);
                batch.draw(crSlots[i].getHealthBar(), hud_enmy_x[idx] - 2, count > 7 ? 14 : 45);
                Andius.smallFont.draw(batch, "" + crSlots[i].getMaxHitPoints(), hud_enmy_x[idx] + 58, y - 13);
                count++;
                idx++;
                if (count == 8) {
                    idx = 0;
                    y = 46;
                }
            }
        }

        Andius.HUD.render(batch, Andius.CTX);

//        if (context.getAura().getType() != AuraType.NONE) {
//            Exodus.font.draw(batch, context.getAura().getType().toString(), 430, Exodus.SCREEN_HEIGHT - 7);
//        }
        batch.end();

        stage.act();
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        mapViewPort.update(width, height, false);
    }

    @Override
    public boolean keyUp(int keycode) {

        andius.objects.Actor active = getActivePartyMember();

        try {
            if (keycode == Keys.SPACE || active.isDisabled()) {
                log("Pass");
            } else if (keycode == Keys.UP) {
                if (preMove(active, Direction.NORTH)) {
                    active.setWy(active.getWy() - 1);
                    active.setY(active.getY() + TILE_DIM);
                }
            } else if (keycode == Keys.DOWN) {
                if (preMove(active, Direction.SOUTH)) {
                    active.setWy(active.getWy() + 1);
                    active.setY(active.getY() - TILE_DIM);
                }
            } else if (keycode == Keys.RIGHT) {
                if (preMove(active, Direction.EAST)) {
                    active.setWx(active.getWx() + 1);
                    active.setX(active.getX() + TILE_DIM);
                }
            } else if (keycode == Keys.LEFT) {
                if (preMove(active, Direction.WEST)) {
                    active.setWx(active.getWx() - 1);
                    active.setX(active.getX() - TILE_DIM);
                }
            } else if (keycode == Keys.A) {
                Gdx.input.setInputProcessor(sip);
                sip.init(active, keycode, active.getWx(), active.getWy());
                return false;
            } else if (keycode == Keys.C) {
            } else if (keycode == Keys.N) {
            } else if (keycode == Keys.U) {
            } else if (keycode == Keys.R) {
            } else if (keycode == Keys.W) {
            }

            finishPlayerTurn();

            //} catch (PartyDeathException e) {
            //    this.returnScreen.partyDeath();
        } finally {

        }

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

    private void finishPlayerTurn() {

        //remove dead creatures
        Iterator<andius.objects.Actor> iter = this.enemies.iterator();
        while (iter.hasNext()) {
            andius.objects.Actor c = iter.next();
            if (c.getMonster().getCurrentHitPoints() <= 0) {
                iter.remove();
            }
        }

        boolean roundIsDone = isRoundDone() || this.enemies.isEmpty();

        andius.objects.Actor next = getAndSetNextActivePlayer();
        if (next != null) {
            cursor.setVisible(true);
            cursor.set(next.getX(), next.getY());
        } else {
            cursor.setVisible(false);
        }

        if (roundIsDone) {
            finishTurn(0, 0);
        }
    }

    @Override
    public void finishTurn(int currentX, int currentY) {

        try {
            //party.endTurn(Maps.get(combatMap.getId()), combatMap.getType());

            //context.getAura().passTurn();
            if (this.enemies.isEmpty()) {
                end();
                return;
            }

            if (this.partyMembers.isEmpty()) {
                end();
                return;
            }

            //boolean quick = context.getAura().getType() == AuraType.QUICKNESS;
            if (true) {
                SequenceAction seq = Actions.action(SequenceAction.class);
                for (andius.objects.Actor cr : this.enemies) {
                    seq.addAction(Actions.run(new CreatureActionsAction(cr)));
                    seq.addAction(Actions.delay(.04f));
                }
                seq.addAction(Actions.run(new FinishCreatureAction()));
                stage.addAction(seq);
            }
        } catch (Exception e) {
            //this.returnScreen.partyDeath();
        }

    }

    private class CreatureActionsAction implements Runnable {

        private andius.objects.Actor cr;

        public CreatureActionsAction(andius.objects.Actor cr) {
            super();
            this.cr = cr;
        }

        @Override
        public void run() {
            try {
                creatureAction(cr);
            } catch (PartyDeathException e) {
                //CombatScreen.this.returnScreen.partyDeath();
            }
        }
    }

    private class RemoveCreatureAction implements Runnable {

        private andius.objects.Actor cr;

        public RemoveCreatureAction(andius.objects.Actor cr) {
            this.cr = cr;
        }

        @Override
        public void run() {
            Andius.HUD.add(String.format("%s turns his toes up to the daises!", cr.getMonster().name));
            CombatScreen.this.enemies.remove(cr);
        }
    }

    private class FinishCreatureAction implements Runnable {

        @Override
        public void run() {
            //enable input again
            Gdx.input.setInputProcessor(CombatScreen.this);
            //if (!party.isAnyoneAlive()) {
            //    end();
            //}
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

        boolean isWon = this.enemies.isEmpty();
        if (isWon) {
            int goldRewardId = 0;
            int chestRewardId = 0;
            int exp = 0;

            for (Monster m : crSlots) {
                if (m != null) {
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
            mainGame.setScreen(new RewardScreen(this.context, this.contextMap, 1, exp, REWARDS.get(goldRewardId), REWARDS.get(chestRewardId)));
            this.contextMap.getScreen().endCombat(isWon, this.opponent);
        } else {
            mainGame.setScreen(startScreen);
        }
        this.dispose();
    }

    private boolean rangedAttackAt(AttackVector target, andius.objects.Actor attacker) throws PartyDeathException {

//        PartyMember defender = null;
//        for (PartyMember p : party.getMembers()) {
//            if (p.combatCr.currentX == target.x && p.combatCr.currentY == target.y) {
//                defender = p;
//                break;
//            }
//        }
//
//        if (defender == null) {
//            return false;
//        }
//
//        AttackResult res = (this.contextMap == Maps.EXODUS && defender.getPlayer().armor != ArmorType.EXOTIC ? AttackResult.HIT : Utils.attackHit(attacker, defender));
//
//        TileEffect effect = TileEffect.NONE;
//        Color col = Color.WHITE;
//
//        if (attacker.rangedAttackIs("poison_field")) {
//            effect = TileEffect.POISON;
//            col = Color.GREEN;
//        } else if (attacker.rangedAttackIs("magic_flash")) {
//            col = Color.CYAN;
//        } else if (attacker.rangedAttackIs("fire_field")) {
//            effect = TileEffect.FIRE;
//            col = Color.RED;
//        } else if (attacker.rangedAttackIs("energy_field")) {
//            effect = TileEffect.ELECTRICITY;
//            col = Color.BLUE;
//        } else if (attacker.rangedAttackIs("rocks")) {
//            col = Color.BROWN;
//        }
//
//        final ProjectileActor p = new ProjectileActor(this, col, attacker.currentX, attacker.currentY, res);
//        Vector3 v = getMapPixelCoords(defender.combatCr.currentX, defender.combatCr.currentY);
//        p.addAction(sequence(moveTo(v.x, v.y, .3f), new Action() {
//            public boolean act(float delta) {
//                switch (p.res) {
//                    case HIT:
//                        p.resultTexture = Exodus.hitTile;
//                        Sounds.play(Sound.NPC_MAGIC_STRUCK);
//                        wounded = true;
//                        break;
//                    case MISS:
//                        p.resultTexture = Exodus.missTile;
//                        Sounds.play(Sound.EVADE);
//                        break;
//                }
//                return true;
//            }
//        }, fadeOut(.2f), removeActor(p)));
//
//        stage.addActor(p);
//
//        switch (effect) {
//            case ELECTRICITY:
//                Sounds.play(Sound.LIGHTNING);
//                log("Electrified!");
//                Utils.dealDamage(attacker, defender);
//                break;
//            case FIRE:
//            case LAVA:
//                Sounds.play(Sound.FIREBALL);
//                log("Fiery Hit!");
//                Utils.dealDamage(attacker, defender);
//                break;
//            case NONE:
//                break;
//            case POISON:
//            case POISONFIELD:
//                if (rand.nextInt(2) == 0 && defender.getPlayer().status != StatusType.POISONED) {
//                    Sounds.play(Sound.POISON_EFFECT);
//                    log("Poisoned!");
//                    defender.getPlayer().status = StatusType.POISONED;
//                }
//                break;
//            default:
//                break;
//
//        }
//
//        if (res == AttackResult.HIT) {
//            Utils.dealDamage(attacker, defender);
//        }
//        return res == AttackResult.HIT;
        return false;
    }

    @Override
    public void partyDeath() {
        //not used here
    }

    /**
     * Return false if to remove from map.
     */
    private boolean creatureAction(andius.objects.Actor creature) throws PartyDeathException {

        Gdx.input.setInputProcessor(null);

//        if (creature.negates()) {
//            context.setAura(AuraType.NEGATE, 2);
//        }
        CombatAction action = CombatAction.ATTACK;
//        if (creature.getTeleports() && rand.nextInt(100) <= 25) {
//            action = CombatAction.TELEPORT;
//        } else if (creature.getRanged() && rand.nextInt(100) <= 33) {
//            action = CombatAction.RANGED;
//        } else {
//            action = CombatAction.ATTACK;
//        }

        DistanceWrapper dist = new DistanceWrapper(0);
        andius.objects.Actor target = nearestPartyMember(creature.getWx(), creature.getWy(), dist, action == CombatAction.RANGED);
        if (target == null) {
            return true;
        }

        if (action == CombatAction.ATTACK && dist.getVal() > 1) {
            action = CombatAction.ADVANCE;
        }

//        if (creature.getCamouflage() && !hideOrShow(creature)) {
//            return true;
//        }
        switch (action) {
            case ATTACK: {
                Sounds.play(Sound.NPC_ATTACK);
//                AttackResult res = Utils.attackHit(creature, target);
//                if (res == AttackResult.HIT) {
//                    Sounds.play(Sound.PC_STRUCK);
//                    if (!Utils.dealDamage(creature, target)) {
//                        target = null;
//                    }
//                }
                break;
            }

//            case TELEPORT: {//only wisp teleports
//                boolean valid = false;
//                int rx = 0, ry = 0, count = 0;
//                while (!valid && count < 5) {
//                    rx = rand.nextInt(combatMap.getWidth());
//                    ry = rand.nextInt(combatMap.getHeight());
//                    Tile t = combatMap.getTile(rx, ry);
//                    if (t != null) {
//                        valid = true;
//                        TileRule rule = t.getRule();
//                        if (rule != null && rule.has(TileAttrib.creatureunwalkable)) {
//                            valid = false;
//                        }
//                    }
//                    count++;
//                }
//                if (valid) {
//                    moveCreature(action, creature, rx, ry);
//                }
//                break;
//            }
//            case RANGED: {
//
//                // figure out which direction to fire the weapon
//                int dirmask = Utils.getRelativeDirection(MapBorderBehavior.fixed, combatMap.getWidth(), combatMap.getHeight(), target.combatCr.currentX, target.combatCr.currentY, creature.currentX, creature.currentY);
//
//                Sounds.play(Sound.NPC_ATTACK);
//
//                List<AttackVector> path = Utils.getDirectionalActionPath(combatMap, dirmask, creature.currentX, creature.currentY, 1, 11, false, false, false);
//                for (AttackVector v : path) {
//                    if (rangedAttackAt(v, creature)) {
//                        break;
//                    }
//                }
//
//                break;
//            }
            case FLEE:
            case ADVANCE: {

                moveCreature(action, creature, target.getWx(), target.getWy());

                //is map OOB
                if (creature.getWx() >= MAP_DIM || creature.getWy() < 0
                        || creature.getWy() >= MAP_DIM || creature.getWy() < 0) {
                    log(String.format("%s Flees!", creature.getMonster().getName()));
                    Sounds.play(Sound.EVADE);
                    return false;
                }

                break;
            }
        }

        return true;
    }

    private andius.objects.Actor nearestPartyMember(int fromX, int fromY, DistanceWrapper dist, boolean ranged) {
        andius.objects.Actor opponent = null;
        int d = 0;
        int leastDist = 0xFFFF;
        //boolean jinx = context.getAura().getType() == AuraType.JINX;

        for (int i = 0; i < this.partyMembers.size(); i++) {

            andius.objects.Actor pm = this.partyMembers.get(i);
            if (pm.getPlayer().status == Status.DEAD) {
                continue;
            }

            //if (!jinx) {
            if (ranged) {
                d = Utils.distance(fromX, fromY, pm.getWx(), pm.getWy());
            } else {
                d = Utils.movementDistance(fromX, fromY, pm.getWx(), pm.getWy());
            }

            /* skip target 50% of time if same distance */
            if (d < leastDist || (d == leastDist && rand.nextInt(2) == 0)) {
                opponent = pm;
                leastDist = d;
            }
            //}
        }

        if (opponent != null) {
            dist.setVal(leastDist);
        }

        return opponent;
    }

    private class DistanceWrapper {

        private int val;

        public DistanceWrapper(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }

        public void setVal(int val) {
            this.val = val;
        }
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
        }
        if (dir == Direction.SOUTH) {
            cr.setWy(++ny);
            cr.setY(cr.getY() - TILE_DIM);
        }
        if (dir == Direction.EAST) {
            cr.setWx(++nx);
            cr.setX(cr.getX() + TILE_DIM);
        }
        if (dir == Direction.WEST) {
            cr.setWx(--nx);
            cr.setX(cr.getX() - TILE_DIM);
        }

        //System.out.println(String.format("cx=%d cy=%d tx=%d ty=%d",cr.getWx(),cr.getWy(),targetX, targetY));
        return true;

    }

    private Texture getCursorTexture() {
        Pixmap pixmap = new Pixmap(TILE_DIM, TILE_DIM, Format.RGBA8888);
        pixmap.setColor(Color.YELLOW);
        int w = 4;
        pixmap.fillRectangle(0, 0, w, TILE_DIM);
        pixmap.fillRectangle(TILE_DIM - w, 0, w, TILE_DIM);
        pixmap.fillRectangle(w, 0, TILE_DIM - 2 * w, w);
        pixmap.fillRectangle(w, TILE_DIM - w, TILE_DIM - 2 * w, w);
        return new Texture(pixmap);
    }

    private class CursorActor extends Actor {

        Texture texture;
        boolean visible = true;

        CursorActor() {
            texture = getCursorTexture();
        }

        void set(float x, float y) {
            setX(x);
            setY(y);
        }

        @Override
        public void setVisible(boolean v) {
            this.visible = v;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {

            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

            if (visible) {
                batch.draw(texture, getX(), getY());
            }
        }

    }

    private andius.objects.Actor getActivePartyMember() {
        return this.partyMembers.get(activeIndex);
    }

    private boolean isRoundDone() {
        int tmp = activeIndex;
        tmp++;
        if (tmp >= this.partyMembers.size()) {
            return true;
        }
        boolean noMoreAble = true;;
        for (int i = tmp; i < this.partyMembers.size(); i++) {
            if (!this.partyMembers.get(i).isDisabled()) {
                noMoreAble = false;
            }
        }
        return noMoreAble;
    }

    private andius.objects.Actor getAndSetNextActivePlayer() {
        boolean allbad = true;
        for (int i = 0; i < this.partyMembers.size(); i++) {
            if (!this.partyMembers.get(i).isDisabled()) {
                allbad = false;
            }
        }
        if (allbad) {
            activeIndex = 0;
            return null;
        }

        andius.objects.Actor p = null;
        boolean flag = true;
        while (flag) {
            this.activeIndex++;
            if (activeIndex >= this.partyMembers.size()) {
                activeIndex = 0;
            }
            if (!this.partyMembers.get(activeIndex).isDisabled()) {
                p = this.partyMembers.get(activeIndex);
                flag = false;
            }
        }
        return p;
    }

    private void animateAttack(andius.objects.Actor attacker, Direction dir) {

        AttackVector av = attack(attacker, dir);
        int tx = av.x * TILE_DIM;
        int ty = mapPixelHeight - av.y * TILE_DIM - TILE_DIM;

        boolean magicalStrike = false;
        ProjectileActor p = new ProjectileActor(magicalStrike ? Color.CYAN : Color.RED, attacker.getX(), attacker.getY());
        p.addAction(Actions.sequence(Actions.moveTo(tx, ty, av.distance * .1f), Actions.removeActor(p)));

        SequenceAction seq = Actions.action(SequenceAction.class);
        seq.addAction(Actions.run(new PlaySoundAction(Sound.PC_ATTACK)));
        seq.addAction(Actions.run(new AddActorAction(stage, p)));

        if (av.result == AttackResult.HIT) {
            Actor d = new ExplosionDrawable(magicalStrike ? Andius.explBlue : Andius.explGray);
            d.setX(tx + 12);
            d.setY(ty + 12);
            d.addAction(Actions.sequence(Actions.delay(.5f), Actions.removeActor()));

            seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
            seq.addAction(Actions.run(new AddActorAction(stage, d)));
            if (av.victim.getMonster().getCurrentHitPoints() <= 0) {
                seq.addAction(Actions.run(new RemoveCreatureAction(av.victim)));
            }
        }

        seq.addAction(Actions.run(new Runnable() {
            @Override
            public void run() {
                finishPlayerTurn();
            }
        }));

        stage.addAction(seq);
    }

    private AttackVector attack(andius.objects.Actor attacker, Direction dir) {

        Item weapon = attacker.getPlayer().weapon;
        int range = weapon.range == 0 ? 1 : weapon.range;

        List<AttackVector> path = getDirectionalActionPath(MAP_DIM, MAP_DIM, dir.getMask(), attacker.getWx(), attacker.getWy(), 0, range);

        AttackVector target = null;
        for (int i = 0; i < path.size(); i++) {
            target = path.get(i);
            for (andius.objects.Actor c : this.enemies) {
                if (c.getWx() == target.x && c.getWy() == target.y) {
                    target.victim = c;
                    break;
                }
            }
            if (target.victim != null) {
                for (int j = 0; j < weapon.extraSwings + 1; j++) {
                    target.result = Utils.attackHit(attacker.getPlayer(), target.victim.getMonster());
                    if (target.result == AttackResult.HIT) {
                        int damage = Utils.dealDamage(attacker.getPlayer(), target.victim.getMonster());
                        Andius.HUD.add(String.format("%s strikes %s for %d damage!", attacker.getPlayer().name, target.victim.getMonster().name, damage));
                    }
                }
            }
        }

        if (target != null && target.result != null && weapon.numberUses > 0) {
            weapon.use();
            if (weapon.numberUses <= 0) {
                Andius.HUD.add("Your weapon has broken!");
                attacker.getPlayer().weapon = Andius.ITEMS_MAP.get("HANDS").clone();
            }
        }

        return target;
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

    private class SecondaryInputProcessor extends InputAdapter {

        private int code;
        private int x;
        private int y;
        private andius.objects.Actor player;

        public void init(andius.objects.Actor player, int code, int x, int y) {

            this.player = player;
            this.code = code;
            this.x = x;
            this.y = y;

            switch (code) {
                case Keys.A:
                    log("ATTACK> ");
                    break;

            }
        }

        @Override
        public boolean keyUp(int keycode) {
            Direction dir = Direction.NORTH;

            int nx = this.x, ny = this.y;

            switch (keycode) {
                case Keys.UP:
                    dir = Direction.NORTH;
                    ny = ny - 1;
                    break;
                case Keys.DOWN:
                    dir = Direction.SOUTH;
                    ny = ny + 1;
                    break;
                case Keys.LEFT:
                    dir = Direction.WEST;
                    nx = nx - 1;
                    break;
                case Keys.RIGHT:
                    dir = Direction.EAST;
                    nx = nx + 1;
                    break;
            }

            if (this.code == Keys.A) {

                logAppend(dir.toString());

                animateAttack(this.player, dir);

                Gdx.input.setInputProcessor(new InputMultiplexer(CombatScreen.this, stage));

                return false;

            } else if (this.code == Keys.U) {

            }

            finishTurn(nx, ny);
            return false;
        }

    }

}
