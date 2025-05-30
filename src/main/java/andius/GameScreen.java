package andius;

import andius.objects.Sound;
import andius.objects.Sounds;
import andius.objects.Direction;
import andius.dialogs.ConversationDialog;
import static andius.Andius.CTX;
import static andius.Andius.mainGame;
import static andius.Constants.TILE_DIM;
import static andius.WizardryData.BS_ITEMS;
import static andius.WizardryData.DQ_ITEMS;
import static andius.WizardryData.KOD_ITEMS;
import static andius.WizardryData.LEG_ITEMS;
import static andius.WizardryData.PMO_ITEMS;
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
import java.util.Iterator;
import java.util.List;
import utils.PartyDeathException;
import utils.TmxMapRenderer;
import utils.TmxMapRenderer.CreatureLayer;
import utils.Utils;

public class GameScreen extends BaseScreen {

    private final Map map;
    private final TmxMapRenderer renderer;
    private final Batch batch;
    private final Viewport mapViewPort;
    private int currentDirection;
    private boolean removedActors;

    public GameScreen(Map map) {

        this.map = map;

        batch = new SpriteBatch();

        stage = new Stage(viewport);

        camera = new OrthographicCamera(Andius.MAP_VIEWPORT_DIM, Andius.MAP_VIEWPORT_DIM);

        mapViewPort = new ScreenViewport(camera);

        addButtons(this.map);

        renderer = new TmxMapRenderer(this.map, this.map.getTiledMap(), 1f);

        renderer.registerCreatureLayer(new CreatureLayer() {
            @Override
            public void render(float time) {
                renderer.getBatch().draw(Andius.game_scr_avatar.getKeyFrames()[currentDirection], newMapPixelCoords.x - 20, newMapPixelCoords.y - TILE_DIM + 12);
                for (Actor a : GameScreen.this.map.getBaseMap().actors) {
                    if (renderer.shouldRenderCell(currentRoomId, a.getWx(), a.getWy())) {
                        renderer.getBatch().draw(a.getIcon(), a.getX() - 0, a.getY() + 0);
                    }
                }
            }
        });

        mapPixelHeight = this.map.getBaseMap().getHeight() * TILE_DIM;

        setMapPixelCoords(newMapPixelCoords, this.map.getStartX(), this.map.getStartY(), 0);

        if (this.map.getRoomIds() != null) {
            currentRoomId = this.map.getRoomIds()[this.map.getStartX()][this.map.getStartY()][0];
        }

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
        setMapPixelCoords(newMapPixelCoords, saveGame.x, saveGame.y, 0);
        if (this.map.getRoomIds() != null) {
            currentRoomId = this.map.getRoomIds()[saveGame.x][saveGame.y][0];
        }
        removeActors(saveGame);
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

    @Override
    public void show() {
        setRoomName();
        removeActors(CTX.saveGame);
        Andius.HUD.addActor(this.stage);
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
    }

    @Override
    public void hide() {

    }

    @Override
    public void log(String s) {
        Andius.HUD.log(s);
    }

    @Override
    public void render(float delta) {

        time += delta;

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (renderer == null) {
            return;
        }

        camera.position.set(newMapPixelCoords.x + 3 * TILE_DIM + 24 + 8, newMapPixelCoords.y - 1 * TILE_DIM, 0);

        camera.update();

        renderer.setView(camera.combined,
                camera.position.x - TILE_DIM * 10,
                camera.position.y - TILE_DIM * 6,
                Andius.MAP_VIEWPORT_DIM,
                Andius.MAP_VIEWPORT_DIM);

        renderer.render();

        batch.begin();

        batch.draw(Andius.backGround, 0, 0);
        Andius.HUD.render(batch, Andius.CTX);

        //Vector3 v = new Vector3();
        //setCurrentMapCoords(v);
        //Andius.smallFont.draw(batch, String.format("%s, %s\n", v.x, v.y), 200, Andius.SCREEN_HEIGHT - 32);
        if (this.roomName != null) {
            Andius.font16.draw(batch, String.format("%s", this.roomName), 300, Andius.SCREEN_HEIGHT - 12);
        }

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
        v.set(x * TILE_DIM, mapPixelHeight - y * TILE_DIM, 0);
    }

    @Override
    public void getCurrentMapCoords(Vector3 v) {
        Vector3 tmp = camera.unproject(new Vector3(TILE_DIM * 7, TILE_DIM * 8, 0), 48, 96, Andius.MAP_VIEWPORT_DIM, Andius.MAP_VIEWPORT_DIM);
        v.set(Math.round(tmp.x / TILE_DIM) - 3, ((mapPixelHeight - Math.round(tmp.y) - TILE_DIM) / TILE_DIM) - 0, 0);
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
            newMapPixelCoords.y = newMapPixelCoords.y + TILE_DIM;
            v.y -= 1;
        } else if (keycode == Keys.DOWN) {
            if (!preMove(v, Direction.SOUTH)) {
                return false;
            }
            this.currentDirection = 0;
            newMapPixelCoords.y = newMapPixelCoords.y - TILE_DIM;
            v.y += 1;
        } else if (keycode == Keys.RIGHT) {
            if (!preMove(v, Direction.EAST)) {
                return false;
            }
            this.currentDirection = 1;
            newMapPixelCoords.x = newMapPixelCoords.x + TILE_DIM;
            v.x += 1;
        } else if (keycode == Keys.LEFT) {
            if (!preMove(v, Direction.WEST)) {
                return false;
            }
            this.currentDirection = 3;
            newMapPixelCoords.x = newMapPixelCoords.x - TILE_DIM;
            v.x -= 1;
        } else if (keycode == Keys.NUM_1 || keycode == Keys.PAGE_DOWN || keycode == Keys.NUM_2 || keycode == Keys.PAGE_UP) {//elevators
            Portal p = this.map.getBaseMap().getPortal((int) v.x, (int) v.y, (keycode == Keys.NUM_2 || keycode == Keys.PAGE_UP));
            if (p != null && p.getMap() != this.map) {
                Vector3 dv = p.getDest();
                int dx = (int) dv.x;
                int dy = (int) dv.y;
                if (dx >= 0 && dy >= 0) {
                    if (p.getMap().getRoomIds() != null) {
                        p.getMap().getScreen().currentRoomId = p.getMap().getRoomIds()[dx][dy][0];
                    }
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
                    if (p.getMap().getRoomIds() != null) {
                        p.getMap().getScreen().currentRoomId = p.getMap().getRoomIds()[dx][dy][0];
                    }
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
                        if ("REWARD".equals(obj.getName())) {
                            StringBuilder sb = new StringBuilder();
                            Iterator<String> iter2 = obj.getProperties().getKeys();
                            while (iter2.hasNext()) {
                                String key = iter2.next();
                                if (key.startsWith("item")) {
                                    Item found = this.map.scenario().item((obj.getProperties().get(key, String.class)));
                                    sb.append("Party found ").append(found.genericName).append(". ");
                                    Andius.CTX.players()[0].inventory.add(found);
                                }
                            }
                            animateText(sb.toString(), Color.GREEN);
                            messagesLayer.getObjects().remove(obj);
                            TiledMapTileLayer layer = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("props");
                            TiledMapTileLayer.Cell cell = layer.getCell((int) v.x, this.map.getBaseMap().getHeight() - 1 - (int) v.y);
                            if (cell != null) {
                                cell.setTile(null);
                            }
                            return false;
                        }
                    }
                }
            }

            //random treasure chest
            TiledMapTileLayer layer = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("props");
            TiledMapTileLayer.Cell cell = layer.getCell((int) v.x, this.map.getBaseMap().getHeight() - 1 - (int) v.y);
            if (cell != null && cell.getTile() != null && cell.getTile().getId() == (609 + 1)) { //gold pile tile id
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

        for (int i = 0; i < pos.length; i++) {
            Actor a = this.map.getBaseMap().getCreatureAt((int) pos[i].x, (int) pos[i].y);
            if (a != null) {
                return a;
            }
        }
        return null;
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

        TiledMapTileLayer layer = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("floor");
        TiledMapTileLayer.Cell cell = layer.getCell(nx, this.map.getBaseMap().getHeight() - 1 - ny);
        if (cell == null) {
            //Sounds.play(Sound.BLOCKED);
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
                    String msg = obj.getProperties().get("type", String.class);

                    animateText(msg, Color.WHITE);

                    String heal = obj.getProperties().get("heal", String.class);
                    if (heal != null) {
                        Sounds.play(Sound.HEALING);
                        for (int i = 0; i < CTX.players().length; i++) {
                            CTX.players()[i].adjustHP(15);
                            CTX.players()[i].status.reset();
                            SaveGame.setSpellPoints(CTX.players()[i]);
                        }
                        return false;
                    }

                    String itemRequired = obj.getProperties().get("itemRequired", String.class);
                    if (itemRequired != null) {
                        Item found = this.map.scenario().item(itemRequired);
                        boolean owned = false;
                        for (int i = 0; i < Andius.CTX.players().length && found != null; i++) {
                            if (Andius.CTX.players()[i].inventory.contains(found)) {
                                owned = true;
                            }
                        }
                        if (!owned) {
                            Sounds.play(Sound.NEGATIVE_EFFECT);
                            animateText("Cannot pass!", Color.RED);
                            return false;
                        }
                    }

                    String itemObtained = obj.getProperties().get("itemObtained", String.class);
                    if (itemObtained != null) {
                        Item found = this.map.scenario().item(itemObtained);
                        boolean owned = false;
                        for (int i = 0; i < Andius.CTX.players().length; i++) {
                            if (Andius.CTX.players()[i].inventory.contains(found)) {
                                owned = true;
                            }
                        }
                        if (found != null && !owned) {
                            Sounds.play(Sound.POSITIVE_EFFECT);
                            Andius.CTX.players()[0].inventory.add(found);
                            animateText(Andius.CTX.players()[0].name + " obtained a " + found.name + "!", Color.GREEN);
                        }
                    }
                }

            }
        }

        Portal p = this.map.getBaseMap().getPortal((int) nx, (int) ny);
        if (p != null && p.getMap() == this.map) { //go to a portal on the same map ie ali-baba map has this
            Vector3 dv = p.getDest();
            if (this.map.getRoomIds() != null) {
                currentRoomId = this.map.getRoomIds()[(int) dv.x][(int) dv.y][0];
                setRoomName();
            }
            setMapPixelCoords(newMapPixelCoords, (int) dv.x, (int) dv.y, 0);

            for (Actor act : this.map.getBaseMap().actors) {//so follower can follow thru portal
                if (act.getMovement() == MovementBehavior.FOLLOW_AVATAR) {
                    int dist = Utils.movementDistance(act.getWx(), act.getWy(), (int) nx, (int) ny);
                    if (dist < 5) {
                        act.setWx((int) dv.x);
                        act.setWy((int) dv.y);
                        Vector3 pixelPos = new Vector3();
                        setMapPixelCoords(pixelPos, act.getWx(), act.getWy(), 0);
                        act.setX(pixelPos.x);
                        act.setY(pixelPos.y);
                    }
                }
            }
            return false;
        }

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

        if (this.map.getRoomIds() != null && this.map.getRoomIds()[x][y][1] == 0) {
            this.currentRoomId = this.map.getRoomIds()[x][y][0];
            setRoomName();
        }

        try {
            this.map.getBaseMap().moveObjects(this.map, this, x, y);

            CTX.endTurn(this.map);

        } catch (PartyDeathException t) {
            partyDeath();
        }
    }

    @Override
    public void partyDeath() {
    }

    private void setRoomName() {
        MapLayer roomsLayer = this.map.getTiledMap().getLayers().get("rooms");
        if (roomsLayer != null) {
            Iterator<MapObject> iter = roomsLayer.getObjects().iterator();
            while (iter.hasNext()) {
                MapObject obj = iter.next();
                int id = obj.getProperties().get("id", Integer.class);
                String name = obj.getName();
                if (id == this.currentRoomId) {
                    this.roomName = name;
                    return;
                }
            }
        }
        this.roomName = null;
    }

    @Override
    public void teleport(int level, int stepsX, int stepsY) {

    }

}
