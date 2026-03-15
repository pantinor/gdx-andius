package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
import andius.objects.Direction;
import andius.dialogs.ConversationDialog;
import static andius.Andius.CTX;
import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import static andius.Constants.TILE_DIM;
import static andius.WizardryData.BS_ITEMS;
import static andius.WizardryData.DQ_ITEMS;
import static andius.WizardryData.KOD_ITEMS;
import static andius.WizardryData.LEG_ITEMS;
import static andius.WizardryData.PMO_ITEMS;
import andius.WizardryData.Scenario;
import static andius.WizardryData.WER_ITEMS;
import andius.objects.Actor;
import andius.objects.Conversations.Conversation;
import andius.objects.Item;
import andius.objects.Portal;
import andius.objects.SaveGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import utils.FrameMaker;
import utils.PartyDeathException;
import utils.SpreadFOV;
import utils.TmxMapRenderer;
import utils.TmxMapRenderer.CreatureLayer;
import utils.Utils;

public class GameScreen extends BaseScreen {

    private static final int SCALED_DIM = TILE_DIM * 2;
    private static final int VIEWPORT_DIM = SCALED_DIM * 17;

    private final Texture background;
    private final Map map;
    private final TmxMapRenderer renderer;
    private final Batch batch;
    private final Viewport mapViewPort;

    private int currentDirection;
    private boolean removedActors;

    public GameScreen(Map map) {

        this.map = map;

        FrameMaker fm = new FrameMaker(SCREEN_WIDTH, SCREEN_HEIGHT);
        Texture frame = new Texture(Gdx.files.classpath("assets/data/world_frame.png"));
        frame.getTextureData().prepare();
        fm.drawPixmap(frame.getTextureData().consumePixmap(), 0, 0);
        fm.emptyFrame(SCALED_DIM * 3, SCALED_DIM * 3, VIEWPORT_DIM, VIEWPORT_DIM);
        this.background = fm.build();

        batch = new SpriteBatch();

        stage = new Stage(viewport);

        camera = new OrthographicCamera(Andius.MAP_VIEWPORT_DIM, Andius.MAP_VIEWPORT_DIM);

        mapViewPort = new ScreenViewport(camera);

        addButtons(this.map);

        TiledMapTileLayer mapLayer = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("map");
        float[][] shadowMap = new float[mapLayer.getWidth()][mapLayer.getHeight()];
        for (int y = 0; y < mapLayer.getHeight(); y++) {
            for (int x = 0; x < mapLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = mapLayer.getCell(x, mapLayer.getHeight() - 1 - y);
                int val = cell.getTile().getId() - 1;
                shadowMap[x][y] = (val == 6 || val == 8 || val == 73 || val == 127 || val == 58 || val == 59 ? 1 : 0);
            }
        }

        SpreadFOV fov = new SpreadFOV(shadowMap);

        renderer = new TmxMapRenderer(this.map, this.map.getTiledMap(), 2f, fov);

        renderer.registerCreatureLayer(new CreatureLayer() {
            Vector3 pos = new Vector3();

            @Override
            public void render(float time) {
                for (Actor a : GameScreen.this.map.getBaseMap().actors) {
                    setMapPixelCoords(pos, a.getWx(), a.getWy(), 0);
                    float packed = renderer.getColor(mapLayer, a.getWx(), a.getWy());
                    if (packed != Color.BLACK.toFloatBits()) {
                        renderer.getBatch().draw(a.getAnim().getKeyFrame(time, true), pos.x, pos.y, SCALED_DIM, SCALED_DIM);
                    }
                }
            }
        });

        mapPixelHeight = this.map.getBaseMap().getHeight() * SCALED_DIM;

        setMapPixelCoords(newMapPixelCoords, this.map.getStartX(), this.map.getStartY(), 0);

    }

    @Override
    public void render(float delta) {

        time += delta;

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (renderer == null) {
            return;
        }

        this.camera.position.x = newMapPixelCoords.x + SCALED_DIM * 5;
        this.camera.position.y = newMapPixelCoords.y + SCALED_DIM * 1;

        camera.update();

        this.renderer.setView(camera.combined,
                camera.position.x - SCALED_DIM * 13,
                camera.position.y - SCALED_DIM * 9,
                VIEWPORT_DIM,
                VIEWPORT_DIM);

        renderer.render();

        batch.begin();

        batch.draw(this.background, 0, 0);
        batch.draw(Andius.world_scr_avatar.getKeyFrame(time, true), SCALED_DIM * 11, SCALED_DIM * 11, SCALED_DIM, SCALED_DIM);

        Andius.HUD.render(batch, Andius.CTX);

        Vector3 v = new Vector3();
        getCurrentMapCoords(v);
        Andius.font14.draw(batch, String.format("%s, %s\n", v.x, v.y), 200, Andius.SCREEN_HEIGHT - 32);

        batch.end();

        stage.act();
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        mapViewPort.update(width, height, false);
    }

    @Override
    public void setMapPixelCoords(Vector3 v, int x, int y, int z) {
        v.set(x * SCALED_DIM, mapPixelHeight - SCALED_DIM - y * SCALED_DIM, 0);
    }

    @Override
    public void getCurrentMapCoords(Vector3 v) {
        Vector3 tmp = camera.unproject(new Vector3(SCALED_DIM * 11, SCALED_DIM * 11, 0), SCALED_DIM * 3, SCALED_DIM * 3, VIEWPORT_DIM, VIEWPORT_DIM);
        float y = Math.round((mapPixelHeight - tmp.y) / SCALED_DIM) + 2;
        float x = Math.round(tmp.x / SCALED_DIM) - 4;
        v.set(x, y, 0);
    }

    @Override
    public void save(SaveGame saveGame) {
        Vector3 v = new Vector3();
        getCurrentMapCoords(v);
        CTX.saveGame.map = this.map;
        CTX.saveGame.x = (int) v.x;
        CTX.saveGame.y = (int) v.y;
        CTX.saveGame.level = 0;
        CTX.saveGame.direction = Direction.NORTH;
    }

    @Override
    public void load(SaveGame saveGame) {
        removeActors(saveGame);

        setMapPixelCoords(newMapPixelCoords, saveGame.x, saveGame.y, 0);
        renderer.getFOV().calculateFOV(saveGame.x, saveGame.y, 72);
    }

    @Override
    public void show() {
        removeActors(CTX.saveGame);

        Andius.HUD.addActor(this.stage);
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));

        Vector3 v = new Vector3();
        getCurrentMapCoords(v);

        renderer.getFOV().calculateFOV((int) v.x, (int) v.y, 72);
    }

    @Override
    public void hide() {

    }

    @Override
    public void log(String s) {
        Andius.HUD.log(s);
    }

    @Override
    public boolean keyUp(int keycode) {
        Vector3 v = new Vector3();
        getCurrentMapCoords(v);

        if (keycode == Keys.UP) {
            if (!preMove(v, Direction.NORTH)) {
                return false;
            }
            this.currentDirection = 2;
            newMapPixelCoords.y = newMapPixelCoords.y + SCALED_DIM;
            v.y -= 1;
        } else if (keycode == Keys.DOWN) {
            if (!preMove(v, Direction.SOUTH)) {
                return false;
            }
            this.currentDirection = 0;
            newMapPixelCoords.y = newMapPixelCoords.y - SCALED_DIM;
            v.y += 1;
        } else if (keycode == Keys.RIGHT) {
            if (!preMove(v, Direction.EAST)) {
                return false;
            }
            this.currentDirection = 1;
            newMapPixelCoords.x = newMapPixelCoords.x + SCALED_DIM;
            v.x += 1;
        } else if (keycode == Keys.LEFT) {
            if (!preMove(v, Direction.WEST)) {
                return false;
            }
            this.currentDirection = 3;
            newMapPixelCoords.x = newMapPixelCoords.x - SCALED_DIM;
            v.x -= 1;
        } else if (keycode == Keys.NUM_1 || keycode == Keys.PAGE_DOWN || keycode == Keys.NUM_2 || keycode == Keys.PAGE_UP) {//elevators
            Portal p = this.map.getBaseMap().getPortal((int) v.x, (int) v.y, (keycode == Keys.NUM_2 || keycode == Keys.PAGE_UP));
            if (p != null && p.getMap() != this.map) {
                Vector3 dv = p.getDest();
                int dx = (int) dv.x;
                int dy = (int) dv.y;
                if (dx >= 0 && dy >= 0) {
                    p.getMap().getScreen().setMapPixelCoords(p.getMap().getScreen().newMapPixelCoords, dx, dy, 0);
                }
                Andius.mainGame.setScreen(p.getMap().getScreen());
            }
            return false;
        } else if (keycode == Keys.E || keycode == Keys.K) {//stairs
            Portal p = this.map.getBaseMap().getPortal((int) v.x, (int) v.y);
            if (p != null && p.getMap() != this.map && !p.isElevator()) {
                Vector3 dv = p.getDest();
                int dx = (int) dv.x;
                int dy = (int) dv.y;
                if (dx >= 0 && dy >= 0) {
                    p.getMap().getScreen().setMapPixelCoords(p.getMap().getScreen().newMapPixelCoords, dx, dy, 0);
                }
                Andius.mainGame.setScreen(p.getMap().getScreen());
            }
            return false;
        } else if (keycode == Keys.G) {

            MapLayer messagesLayer = this.map.getTiledMap().getLayers().get("messages");
            if (messagesLayer != null) {
                Iterator<MapObject> iter = messagesLayer.getObjects().iterator();
                while (iter.hasNext()) {
                    MapObject obj = iter.next();
                    float mx = obj.getProperties().get("x", Float.class) / TILE_DIM;
                    float my = obj.getProperties().get("y", Float.class) / TILE_DIM;
                    if (v.x == mx && this.map.getBaseMap().getHeight() - v.y - 1 == my) {
                        if ("reward".equals(obj.getName())) {
                            String itemName = obj.getProperties().get("type", String.class);
                            if (itemName != null) {
                                String scenName = obj.getProperties().get("scenario") != null ? obj.getProperties().get("scenario", String.class) : "PMO";
                                Scenario sc = Scenario.valueOf(scenName);
                                Item item = sc.item(itemName);
                                StringBuilder sb = new StringBuilder();
                                sb.append("Party found ").append(item.genericName).append(". ");
                                Andius.CTX.players()[0].inventory.add(item);
                                animateText(sb.toString(), Color.GREEN);
                                messagesLayer.getObjects().remove(obj);
                            }
                            return false;
                        }
                    }
                }
            }

            //random treasure chest
            TiledMapTileLayer layer = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("map");
            TiledMapTileLayer.Cell cell = layer.getCell((int) v.x, this.map.getBaseMap().getHeight() - 1 - (int) v.y);
            if (cell.getTile().getId() - 1 == 60) {
                RewardScreen rs = new RewardScreen(CTX, this.map, rand.nextInt(20));
                mainGame.setScreen(rs);
                cell.setTile(null);
                return false;
            }
        } else if (keycode == Keys.T) {
            Actor a = getTalkActor(v.x, v.y);
            if (a != null) {
                if (a.getRole() == Role.TEMPLE) {
                    TempleScreen rs = new TempleScreen(CTX, this.map);
                    mainGame.setScreen(rs);
                    return false;
                } else if (a.getRole() == Role.MERCHANT_PMO) {
                    VendorScreen rs = new VendorScreen(CTX, this.map, PMO_ITEMS, a.getName());
                    mainGame.setScreen(rs);
                    return false;
                } else if (a.getRole() == Role.MERCHANT_KOD) {
                    VendorScreen rs = new VendorScreen(CTX, this.map, KOD_ITEMS, a.getName());
                    mainGame.setScreen(rs);
                    return false;
                } else if (a.getRole() == Role.MERCHANT_LEG) {
                    VendorScreen rs = new VendorScreen(CTX, this.map, LEG_ITEMS, a.getName());
                    mainGame.setScreen(rs);
                    return false;
                } else if (a.getRole() == Role.MERCHANT_WER) {
                    VendorScreen rs = new VendorScreen(CTX, this.map, WER_ITEMS, a.getName());
                    mainGame.setScreen(rs);
                    return false;
                } else if (a.getRole() == Role.MERCHANT_DQ) {
                    VendorScreen rs = new VendorScreen(CTX, this.map, DQ_ITEMS, a.getName());
                    mainGame.setScreen(rs);
                    return false;
                } else if (a.getRole() == Role.MERCHANT_BS) {
                    VendorScreen rs = new VendorScreen(CTX, this.map, BS_ITEMS, a.getName());
                    mainGame.setScreen(rs);
                    return false;
                } else if (a.getRole() == Role.INNKEEPER) {
                    InnScreen rs = new InnScreen(CTX, this.map);
                    mainGame.setScreen(rs);
                    return false;
                } else {
                    Conversation c = Andius.CONVERSATIONS.get(this.map.toString(), a.getName());
                    if (c != null) {
                        new ConversationDialog(CTX, this, c).show(this.stage);
                    }
                }
            }

        } else if (keycode == Keys.ESCAPE) {

            Utils.animateText(stage, Andius.skin, "default-16", Constants.HELP_KEYS, Color.YELLOW, 20, 100, 20, 200);
        }

        finishTurn((int) v.x, (int) v.y);

        return false;
    }

    private Actor getTalkActor(float x, float y) {
        Vector2[] pos = new Vector2[13];
        pos[0] = new Vector2(x, y);
        pos[1] = new Vector2(x - 1, y);
        pos[2] = new Vector2(x + 1, y);
        pos[3] = new Vector2(x, y - 1);
        pos[4] = new Vector2(x, y + 1);
        pos[5] = new Vector2(x + 1, y + 1);
        pos[6] = new Vector2(x + 1, y - 1);
        pos[7] = new Vector2(x - 1, y + 1);
        pos[8] = new Vector2(x - 1, y - 1);

        pos[9] = new Vector2(x - 2, y);
        pos[10] = new Vector2(x + 2, y);
        pos[11] = new Vector2(x, y + 2);
        pos[12] = new Vector2(x, y - 2);

        List<Actor> nearby = new ArrayList<>();
        for (int i = 0; i < pos.length; i++) {
            Actor a = this.map.getBaseMap().getCreatureAt((int) pos[i].x, (int) pos[i].y);
            if (a != null) {
                nearby.add(a);
            }
        }

        if (nearby.isEmpty()) {
            return null;
        }

        if (nearby.size() > 1) {
            EnumSet<Role> priorityRoles = EnumSet.of(
                    Role.TEMPLE,
                    Role.INNKEEPER,
                    Role.MERCHANT_PMO,
                    Role.MERCHANT_KOD,
                    Role.MERCHANT_LEG,
                    Role.MERCHANT_WER,
                    Role.MERCHANT_DQ,
                    Role.MERCHANT_BS
            );
            for (Actor a : nearby) {
                if (priorityRoles.contains(a.getRole())) {
                    return a;
                }
            }
        }

        return nearby.get(0);
    }

    private boolean preMove(Vector3 current, Direction dir) {

        int nx = (int) current.x;
        int ny = (int) current.y;

        if (dir == Direction.NORTH) {
            ny = (int) current.y - 1;
        }
        if (dir == Direction.SOUTH) {
            ny = (int) current.y + 1;
        }
        if (dir == Direction.WEST) {
            nx = (int) current.x - 1;
        }
        if (dir == Direction.EAST) {
            nx = (int) current.x + 1;
        }

        if (nx > this.map.getBaseMap().getWidth() - 1 || nx < 0 || ny > this.map.getBaseMap().getHeight() - 1 || ny < 0) {
            Andius.mainGame.setScreen(Map.WORLD.getScreen());
            return false;
        }

        TiledMapTileLayer layer = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("map");
        TiledMapTileLayer.Cell cell = layer.getCell(nx, this.map.getBaseMap().getHeight() - 1 - ny);
        int val = cell.getTile().getId() - 1;

        Item wingedboots = Scenario.WER.item("WINGED BOOTS");
        Item markFire = Scenario.PMO.item("MARK OF FIRE");
        Item markForce = Scenario.PMO.item("MARK OF FORCE");

        boolean blocked = val == 0 || val == 1 || val == 2 || val == 8 || val == 57 || (val >= 96 && val <= 127);

        if (!CTX.partyHasItem(markFire) && val == 70) {
            Sounds.play(Sound.BLOCKED);
            return false;
        } else if (!CTX.partyHasItem(markForce) && val == 69) {
            Sounds.play(Sound.BLOCKED);
            return false;
        } else if (blocked && !CTX.partyHasItem(wingedboots)) {
            Sounds.play(Sound.BLOCKED);
            return false;
        }

        MapLayer messagesLayer = this.map.getTiledMap().getLayers().get("messages");
        if (messagesLayer != null) {
            Iterator<MapObject> iter = messagesLayer.getObjects().iterator();
            while (iter.hasNext()) {
                MapObject obj = iter.next();
                float mx = obj.getProperties().get("x", Float.class) / TILE_DIM;
                float my = obj.getProperties().get("y", Float.class) / TILE_DIM;
                if (nx == mx && this.map.getBaseMap().getHeight() - 1 - ny == my) {

                    if (obj.getName().equals("message")) {
                        String msg = obj.getProperties().get("type", String.class);
                        Sounds.play(Sound.POSITIVE_EFFECT);
                        animateText(msg, Color.WHITE);
                    }

                    if (obj.getName().equals("heal")) {
                        Sounds.play(Sound.HEALING);
                        for (int i = 0; i < CTX.players().length; i++) {
                            CTX.players()[i].adjustHP(15);
                            CTX.players()[i].status.reset();
                            SaveGame.setSpellPoints(CTX.players()[i]);
                        }
                        return false;
                    }

                    if (obj.getName().equals("itemRequired")) {
                        String itemName = obj.getProperties().get("type", String.class);
                        String scenName = obj.getProperties().get("scenario") != null ? obj.getProperties().get("scenario", String.class) : "PMO";
                        Scenario sc = Scenario.valueOf(scenName);
                        Item item = sc.item(itemName);
                        if (!CTX.partyHasItem(item)) {
                            Sounds.play(Sound.NEGATIVE_EFFECT);
                            return false;
                        } else {
                            Sounds.play(Sound.POSITIVE_EFFECT);
                        }
                    }
                }
            }
        }

        Portal p = this.map.getBaseMap().getPortal((int) nx, (int) ny);
        if (p != null && p.getMap() == this.map) { //go to a portal on the same map ie ali-baba map has this
            Vector3 dv = p.getDest();
            setMapPixelCoords(newMapPixelCoords, (int) dv.x, (int) dv.y, 0);
            for (Actor act : this.map.getBaseMap().actors) {//so follower can follow thru portal
                if (act.getMovement() == MovementBehavior.FOLLOW_AVATAR) {
                    int dist = Utils.movementDistance(act.getWx(), act.getWy(), (int) nx, (int) ny);
                    if (dist < 5) {
                        act.setWx((int) dv.x);
                        act.setWy((int) dv.y);
                    }
                }
            }
            return false;
        }

        renderer.getFOV().calculateFOV(nx, ny, 72);

        return true;
    }

    @Override
    public void endCombat(boolean isWon, Object opponent) {
        if (isWon) {
            this.map.getBaseMap().removeCombatActor();
        }
    }

    @Override
    public void finishTurn(int x, int y) {

        if (x < 0 || y < 0) {
            return;
        }

        try {
            this.map.getBaseMap().moveObjects(this.map, x, y);
            CTX.endTurn(this.map);
        } catch (PartyDeathException t) {
            partyDeath();
        }
    }

    @Override
    public void partyDeath() {
    }

    @Override
    public void teleport(int level, int stepsX, int stepsY) {

    }

    private void removeActors(SaveGame saveGame) {
        if (this.removedActors) {
            return;
        }
        List<String> l = saveGame.removedActors.get(this.map);
        if (l != null && this.map.getBaseMap() != null) {
            Iterator<Actor> iter = this.map.getBaseMap().actors.iterator();
            while (iter.hasNext()) {
                Actor a = iter.next();
                if (l.contains(a.hash())) {
                    iter.remove();
                }
            }
        }
        this.removedActors = true;
    }

}
