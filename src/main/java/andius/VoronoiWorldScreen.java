package andius;

import static andius.Andius.mapAtlas;
import static andius.Constants.MOON_PHASES;
import static andius.Constants.MOON_SECONDS_PER_PHASE;
import static andius.Constants.WORLD_TILE_DIM;
import andius.objects.Portal;
import andius.voronoi.graph.Center;
import andius.voronoi.graph.Corner;
import andius.voronoi.graph.VoronoiRenderer;
import andius.voronoi.groundshapes.Blob;
import andius.voronoi.groundshapes.HeightAlgorithm;
import andius.voronoi.groundshapes.Perlin;
import andius.voronoi.groundshapes.Radial;
import andius.voronoi.nodes.Voronoi;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.HashMap;
import java.util.Random;

public class VoronoiWorldScreen extends BaseScreen {

    private static final int MAP_BOUNDS = 2048;
    private static final int SITES_AMOUNT = 8000;
    private static final int LLOYD_RELAXATIONS = 2;
    private static final long SEED = 123L;
    private static final String ALGORITHM = "perlin";

    private final Voronoi data;
    private final RendererImpl renderer;
    private final Viewport mapViewPort;
    private final Batch mapBatch, batch;
    private Corner currentLocation;
    public static Animation castle;
    private final TextureAtlas moonPhaseAtlas;
    private static int phaseIndex = 0, phaseCount = 0, trammelphase = 0, trammelSubphase = 0, feluccaphase = 0;
    public GameTimer gameTimer = new GameTimer();
    public java.util.Map<Moongate, Corner> moongateMap = new HashMap<>();

    public VoronoiWorldScreen() {

        castle = new Animation(.4f, mapAtlas.findRegions("castle_gray"));

        Random r = new Random(SEED);
        HeightAlgorithm algorithm = getAlgorithmImplementation(r, ALGORITHM);
        data = new Voronoi(SITES_AMOUNT, MAP_BOUNDS, MAP_BOUNDS, r);

        batch = new SpriteBatch();
        stage = new Stage(viewport);

        moonPhaseAtlas = new TextureAtlas(Gdx.files.classpath("assets/data/moon-atlas.txt"));

        SequenceAction seq1 = Actions.action(SequenceAction.class);
        seq1.addAction(Actions.delay(.25f));
        seq1.addAction(Actions.run(gameTimer));
        stage.addAction(Actions.forever(seq1));

        camera = new OrthographicCamera(Andius.MAP_VIEWPORT_DIM, Andius.MAP_VIEWPORT_DIM);
        camera.setToOrtho(true);
        camera.zoom = 0.6f;

        mapViewPort = new ScreenViewport(camera);
        renderer = new RendererImpl(data, LLOYD_RELAXATIONS, r, algorithm);
        mapBatch = renderer.getBatch();

        currentLocation = renderer.getCorner(3892);
        newMapPixelCoords.set((float) currentLocation.loc.x, (float) currentLocation.loc.y, 0f);

        renderer.getCorner(3892).portal = new Portal(Constants.Map.BRITANIA, 0, 0, 15, 31, null, false, false);

        moongateMap.put(Moongate.GATE_0, renderer.getCorner(4487));
        moongateMap.put(Moongate.GATE_1, renderer.getCorner(5529));
        moongateMap.put(Moongate.GATE_2, renderer.getCorner(11776));
        moongateMap.put(Moongate.GATE_3, renderer.getCorner(8361));
        moongateMap.put(Moongate.GATE_4, renderer.getCorner(12145));
        moongateMap.put(Moongate.GATE_5, renderer.getCorner(4324));
        moongateMap.put(Moongate.GATE_6, renderer.getCorner(12289));
        moongateMap.put(Moongate.GATE_7, renderer.getCorner(4098));

    }

    @Override
    public void show() {
        gameTimer.active = true;
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
    }

    @Override
    public void hide() {
        gameTimer.active = false;
    }

    @Override
    public void setMapPixelCoords(Vector3 v, int x, int y) {
    }

    @Override
    public void setCurrentMapCoords(Vector3 v) {
    }

    public class GameTimer implements Runnable {

        public boolean active = true;

        @Override
        public void run() {
            if (active) {

                updateMoons();

            }
        }
    }

    @Override
    public void render(float delta) {

        time += delta;

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glLineWidth(1);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        if (renderer == null) {
            return;
        }

        camera.position.set(
                (float) this.newMapPixelCoords.x + 102,
                (float) (this.newMapPixelCoords.y + 23),
                0);

        camera.update();

        renderer.setView(camera.combined,
                camera.position.x - 500,
                camera.position.y - 230,
                Andius.MAP_VIEWPORT_DIM,
                Andius.MAP_VIEWPORT_DIM);

        renderer.render(time, this.currentLocation);

        for (Moongate g : Moongate.values()) {
            TextureRegion t = g.getCurrentTexture();
            if (t != null) {
                Corner c = moongateMap.get(g);
                mapBatch.begin();
                mapBatch.draw(t, (float) c.loc.x, (float) (c.loc.y), 11, 2, 24f, 24f, 0.6f, 0.6f, 90, false);
                mapBatch.end();
            }
        }

        batch.begin();

        batch.draw(Andius.backGround, 0, 0);
        batch.draw(Andius.world_scr_avatar.getKeyFrame(time, true), WORLD_TILE_DIM * 14, WORLD_TILE_DIM * 17);
        batch.draw(moonPhaseAtlas.findRegion("PHASE_" + trammelphase), 348, Andius.SCREEN_HEIGHT - 32, 20, 20);
        batch.draw(moonPhaseAtlas.findRegion("PHASE_" + feluccaphase), 372, Andius.SCREEN_HEIGHT - 32, 20, 20);

        Andius.HUD.render(batch, Andius.CTX);

        //Andius.font.draw(batch, String.format("%s, %s, %s\n", newMapPixelCoords.x, newMapPixelCoords.y, this.currentLocation.index), 200, Andius.SCREEN_HEIGHT - 32);

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
    public boolean keyUp(int keycode) {
        Corner current = this.currentLocation;

        if (keycode == Input.Keys.UP) {
            Corner next = preMove(current, Direction.NORTH);
            if (next == null) {
                return false;
            }
            this.currentLocation = next;
            postMove(Direction.NORTH, next);
        } else if (keycode == Input.Keys.DOWN) {
            Corner next = preMove(current, Direction.SOUTH);
            if (next == null) {
                return false;
            }
            this.currentLocation = next;
            postMove(Direction.SOUTH, next);
        } else if (keycode == Input.Keys.RIGHT) {
            Corner next = preMove(current, Direction.EAST);
            if (next == null) {
                return false;
            }
            this.currentLocation = next;
            postMove(Direction.EAST, next);
        } else if (keycode == Input.Keys.LEFT) {
            Corner next = preMove(current, Direction.WEST);
            if (next == null) {
                return false;
            }
            this.currentLocation = next;
            postMove(Direction.WEST, next);
        } else if (keycode == Input.Keys.E) {
            Portal p = this.currentLocation.portal;
            if (p != null) {
                Andius.mainGame.setScreen(p.getMap().getScreen());
            }
        }

        return false;
    }

    private Corner preMove(Corner current, Direction dir) {

        Corner next = null;

        if (dir == Direction.NORTH) {
            next = current.getClosestNeighbor(current.loc.x, current.loc.y - 256);
        }
        if (dir == Direction.SOUTH) {
            next = current.getClosestNeighbor(current.loc.x, current.loc.y + 256);
        }
        if (dir == Direction.WEST) {
            next = current.getClosestNeighbor(current.loc.x - 256, current.loc.y);
        }
        if (dir == Direction.EAST) {
            next = current.getClosestNeighbor(current.loc.x + 256, current.loc.y);
        }

        if (next != null && (next.ocean || next.elevation > 0.8)) {
            Sounds.play(Sound.BLOCKED);
            return null;
        }

        return next;
    }

    private void postMove(Direction dir, Corner next) {

        newMapPixelCoords.set((float) next.loc.x, (float) (next.loc.y), 0f);

        //check for active moongate portal
        for (Moongate g : Moongate.values()) {
            Corner c = moongateMap.get(g);
            if (c.equals(next) && g.getCurrentTexture() != null) {
                Sounds.play(Sound.WAVE);
                Moongate d = getDestinationForMoongate(g);
                Corner tmp = moongateMap.get(d);
                this.currentLocation = tmp;
                newMapPixelCoords.set((float) tmp.loc.x, (float) (tmp.loc.y), 0f);
                return;
            }
        }
    }

    @Override
    public void log(String t) {
    }

    @Override
    public void finishTurn(int currentX, int currentY) {
    }

    @Override
    public void partyDeath() {
    }

    private void updateMoons() {

        phaseIndex++;
        if (phaseIndex >= MOON_PHASES * MOON_SECONDS_PER_PHASE * 4) {
            phaseIndex = 0;
        }

        phaseCount = (phaseIndex / (4 * MOON_SECONDS_PER_PHASE));
        feluccaphase = phaseCount % 8;
        trammelphase = phaseCount / 3;
        if (trammelphase > 7) {
            trammelphase = 7;
        }
        trammelSubphase = phaseIndex % (MOON_SECONDS_PER_PHASE * 4 * 3);

        for (Moongate g : Moongate.values()) {
            g.setCurrentTexture(null);
        }

        Moongate gate = Moongate.values()[trammelphase];
        TextureAtlas.AtlasRegion texture = null;
        if (trammelSubphase == 0) {
            texture = Andius.moongateTextures.get(0);
        } else if (trammelSubphase == 1) {
            texture = Andius.moongateTextures.get(1);
        } else if (trammelSubphase == 2) {
            texture = Andius.moongateTextures.get(2);
        } else if (trammelSubphase == 3) {
            texture = Andius.moongateTextures.get(3);
        } else if ((trammelSubphase > 3) && (trammelSubphase < (MOON_SECONDS_PER_PHASE * 4 * 3) - 3)) {
            texture = Andius.moongateTextures.get(3);
        } else if (trammelSubphase == (MOON_SECONDS_PER_PHASE * 4 * 3) - 3) {
            texture = Andius.moongateTextures.get(2);
        } else if (trammelSubphase == (MOON_SECONDS_PER_PHASE * 4 * 3) - 2) {
            texture = Andius.moongateTextures.get(1);
        } else if (trammelSubphase == (MOON_SECONDS_PER_PHASE * 4 * 3) - 1) {
            texture = Andius.moongateTextures.get(0);
        }
        gate.setCurrentTexture(texture);

    }

    private Moongate getDestinationForMoongate(Moongate m) {

        int destGate = m.ordinal();

        if (feluccaphase == m.getD1()) {
            destGate = m.getD1();
        } else if (feluccaphase == m.getD2()) {
            destGate = m.getD2();
        } else if (feluccaphase == m.getD3()) {
            destGate = m.getD3();
        }

        return Moongate.values()[destGate];
    }

    private HeightAlgorithm getAlgorithmImplementation(Random r, String name) {
        HashMap<String, Integer> implementations = new HashMap<>();
        implementations.put("random", 0);
        implementations.put("radial", 1);
        implementations.put("blob", 2);
        implementations.put("perlin", 3);
        int i = implementations.getOrDefault(name, 0);
        if (i == 0) {
            i = 1 + r.nextInt(implementations.size() - 1);
        }
        switch (i) {
            case 1:
                return new Radial(1.07,
                        r.nextInt(5) + 1,
                        r.nextDouble() * 2 * Math.PI,
                        r.nextDouble() * 2 * Math.PI,
                        r.nextDouble() * .5 + .2);
            case 2:
                return new Blob();
            case 3:
                return new Perlin(r, 7, 256, 256);
            default:
                throw new RuntimeException("Method \"getAlgorithmImplementation()\" is broken. "
                        + "Check implementations map and switch statement. Their values and cases must match.");
        }
    }

    public static enum ColorData {

        OCEAN(0x44447aff), LAKE(0x336699ff), BEACH(0xa09077ff), SNOW(0xffffffff),
        TUNDRA(0xbbbbaaff), BARE(0x888888ff), SCORCHED(0x555555ff), TAIGA(0x99aa77ff),
        SHURBLAND(0x889977ff), TEMPERATE_DESERT(0xc9d29bff),
        TEMPERATE_RAIN_FOREST(0x448855ff), TEMPERATE_DECIDUOUS_FOREST(0x679459ff),
        GRASSLAND(0x88aa55ff), SUBTROPICAL_DESERT(0xd2b98bff), SHRUBLAND(0x889977ff),
        ICE(0x99ffffff), MARSH(0x2f6666ff), TROPICAL_RAIN_FOREST(0x337755ff),
        TROPICAL_SEASONAL_FOREST(0x559944ff), COAST(0x33335aff),
        LAKESHORE(0x225588ff), RIVER(0x225588ff);
        public Color color;

        ColorData(int color) {
            this.color = new Color(color);
        }
    }

    public class RendererImpl extends VoronoiRenderer {

        public RendererImpl(Voronoi v, int numLloydRelaxations, Random r, HeightAlgorithm algorithm) {
            super(v, numLloydRelaxations, r, algorithm);
            OCEAN = ColorData.OCEAN.color;
            LAKE = ColorData.LAKE.color;
            BEACH = ColorData.BEACH.color;
            RIVER = ColorData.RIVER.color;
        }

        @Override
        protected Color getColor(Enum biome) {
            return ((ColorData) biome).color;
        }

        @Override
        protected Enum getBiome(Center p) {
            if (p.ocean) {
                return ColorData.OCEAN;
            } else if (p.water) {
                if (p.elevation < 0.1) {
                    return ColorData.MARSH;
                }
                if (p.elevation > 0.8) {
                    return ColorData.ICE;
                }
                return ColorData.LAKE;
            } else if (p.coast) {
                return ColorData.BEACH;
            } else if (p.elevation > 0.8) {
                if (p.moisture > 0.50) {
                    return ColorData.SNOW;
                } else if (p.moisture > 0.33) {
                    return ColorData.TUNDRA;
                } else if (p.moisture > 0.16) {
                    return ColorData.BARE;
                } else {
                    return ColorData.SCORCHED;
                }
            } else if (p.elevation > 0.6) {
                if (p.moisture > 0.66) {
                    return ColorData.TAIGA;
                } else if (p.moisture > 0.33) {
                    return ColorData.SHRUBLAND;
                } else {
                    return ColorData.TEMPERATE_DESERT;
                }
            } else if (p.elevation > 0.3) {
                if (p.moisture > 0.83) {
                    return ColorData.TEMPERATE_RAIN_FOREST;
                } else if (p.moisture > 0.50) {
                    return ColorData.TEMPERATE_DECIDUOUS_FOREST;
                } else if (p.moisture > 0.16) {
                    return ColorData.GRASSLAND;
                } else {
                    return ColorData.TEMPERATE_DESERT;
                }
            } else if (p.moisture > 0.66) {
                return ColorData.TROPICAL_RAIN_FOREST;
            } else if (p.moisture > 0.33) {
                return ColorData.TROPICAL_SEASONAL_FOREST;
            } else if (p.moisture > 0.16) {
                return ColorData.GRASSLAND;
            } else {
                return ColorData.SUBTROPICAL_DESERT;
            }
        }
    }
}
