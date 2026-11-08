package andius;

import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import static andius.Andius.mainGame;
import static andius.Andius.startScreen;
import andius.WizardryData.MazeCell;
import andius.objects.ClassType;
import andius.objects.HealthCursor;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.MonsterModels;
import andius.objects.MutableMonster;
import andius.objects.Reward;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Sound;
import andius.objects.Sounds;
import andius.objects.Spells;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import utils.AutoFocusScrollPane;
import utils.LogScrollPane;
import utils.ObjLoader;
import utils.Utils;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import utils.SkinUtils;

public class WizardryCombatScreen extends Combat implements Screen, Constants {

    private static final Color INK = Color.valueOf("0c141d");
    private static final Color GOLD = Color.valueOf("dbbb7c");
    private static final Color MUTED = Color.valueOf("92a7b7");
    private final com.badlogic.gdx.scenes.scene2d.ui.Skin combatSkin = SkinUtils.createCombatSkin(Andius.skin);
    private final Texture uiPixel = Utils.fillRectangle(1, 1, Color.WHITE, 1);
    private final List<Model> ownedModels = new ArrayList<>();
    private Label commandHint, encounterStatus, abilityTitle;
    private TextButton attack, abilities;
    private Table abilityDrawer;
    static final int SCENE_X = 8, SCENE_Y = 184, SCENE_WIDTH = 794, SCENE_HEIGHT = 540;
    private boolean encounterEnded;
    private int turn = 1;
    private int nextMonsterNumber = 1;

    /** A separate presentation of the existing Wizardry combat rules. */
    private void buildCommandUI(String encounterName) {
        panel(0, 730, 1024, 38, INK);
        panel(810, 216, 206, 508, INK);
        panel(8, 8, 794, 128, INK);
        panel(810, 8, 206, 200, INK);
        panel(8, 140, 794, 38, INK);
        caption(encounterName.toUpperCase(), 18, 738, 650, 22, GOLD, "default-16");
        encounterStatus = caption("", 718, 738, 298, 22, GOLD, "default-12");
        caption("TARGETS", 820, 698, 190, 20, GOLD, "default-16");
        caption("JOURNAL", 820, 184, 190, 20, GOLD, "default-12");
        commandHint = caption("", 240, 146, 208, 28, MUTED, "default-12");
        commandHint.setWrap(true);
        attack = new TextButton("Melee", combatSkin, "default-12");
        attack.setBounds(16, 145, 78, 28);
        attack.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (selectedPlayer.getParent() instanceof PlayerListing listing) {
                    Action order = actions.get(listing.index);
                    order.spell = null;
                    order.item = null;
                    order.dispel = false;
                    order.playerTarget = null;
                    spellsList.getSelection().clear();
                    abilityDrawer.setVisible(false);
                }
            }
        });
        stage.addActor(attack);
        styleButton(fight, true);
        styleButton(reset, false);
        styleButton(flee, false);
        styleButton(exit, true);
        styleButton(attack, false);
        abilities = new TextButton("Spells / Items", combatSkin, "default-12");
        abilities.setBounds(102, 145, 124, 28);
        styleButton(abilities, false);
        abilities.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                abilityDrawer.setVisible(!abilityDrawer.isVisible());
                if (abilityDrawer.isVisible()) abilityDrawer.toFront();
            }
        });
        stage.addActor(abilities);
        abilityDrawer = new Table(combatSkin);
        abilityDrawer.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new TextureRegion(uiPixel)).tint(INK));
        abilityDrawer.setBounds(16, 190, 284, 330);
        abilityTitle = new Label("", combatSkin, "default-12");
        abilityTitle.setColor(GOLD);
        abilityTitle.setEllipsis(true);
        abilityDrawer.add(abilityTitle).width(260).height(30).pad(8).row();
        abilityDrawer.add(spellsScroll).expand().fill().pad(8);
        abilityDrawer.setVisible(false);
        stage.addActor(abilityDrawer);
        stage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (abilityDrawer.isVisible() && abilityDrawer.isAscendantOf(event.getTarget())) return false;
                if (encounterEnded || x < SCENE_X || x > SCENE_X + SCENE_WIDTH || y < SCENE_Y || y > SCENE_Y + SCENE_HEIGHT) return false;
                com.badlogic.gdx.math.collision.Ray ray = camera.getPickRay(
                        x, Gdx.graphics.getHeight() - y, SCENE_X, SCENE_Y, SCENE_WIDTH, SCENE_HEIGHT);
                MonsterListing closest = null;
                float distance = Float.MAX_VALUE;
                BoundingBox bounds = new BoundingBox();
                Vector3 hit = new Vector3();
                for (MonsterListing listing : monsterOrderedList) {
                    ModelInstance instance = listing.health.modelInstance;
                    if (instance == null || listing.mm.isDead()) continue;
                    instance.calculateBoundingBox(bounds);
                    bounds.mul(instance.transform);
                    if (com.badlogic.gdx.math.Intersector.intersectRayBounds(ray, bounds, hit)
                            && ray.origin.dst2(hit) < distance) {
                        closest = listing;
                        distance = ray.origin.dst2(hit);
                    }
                }
                if (closest != null) selectMonster(closest);
                return closest != null;
            }
            @Override public boolean keyDown(InputEvent event, int key) {
                if (key == com.badlogic.gdx.Input.Keys.ESCAPE && abilityDrawer.isVisible()) {
                    abilityDrawer.setVisible(false);
                    return true;
                }
                if (encounterEnded) return false;
                if (key >= com.badlogic.gdx.Input.Keys.NUM_1 && key <= com.badlogic.gdx.Input.Keys.NUM_6) {
                    selectPlayer(key - com.badlogic.gdx.Input.Keys.NUM_1);
                    return true;
                }
                return false;
            }
        });
        logs.add("Choose a party member, assign an ability or melee attack, then choose a target. Resolve turn when ready.", MUTED);
    }

    private void styleButton(TextButton button, boolean primary) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(button.getStyle());
        com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable pixel =
                new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(uiPixel));
        style.up = pixel.tint(Color.valueOf(primary ? "35635e" : "1c2d3c"));
        style.over = pixel.tint(Color.valueOf(primary ? "477e72" : "30495b"));
        style.down = pixel.tint(Color.valueOf("597b79"));
        style.disabled = pixel.tint(INK);
        style.fontColor = primary ? Color.WHITE : GOLD;
        style.overFontColor = style.fontColor;
        style.downFontColor = style.fontColor;
        style.checkedFontColor = style.fontColor;
        style.disabledFontColor = MUTED;
        button.setStyle(style);
    }

    private Image selection(int width, int height, Color color) {
        Image image = new Image(uiPixel) {
            @Override public void draw(Batch batch, float parentAlpha) {
                Color previous = new Color(batch.getColor());
                batch.setColor(color.r, color.g, color.b, parentAlpha);
                batch.draw(uiPixel, getX(), getY(), width, 1);
                batch.draw(uiPixel, getX(), getY() + height - 1, width, 1);
                batch.draw(uiPixel, getX(), getY(), 2, height);
                batch.draw(uiPixel, getX() + width - 2, getY(), 2, height);
                batch.setColor(previous);
            }
        };
        image.setSize(width, height);
        image.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        return image;
    }

    private void selectMonster(MonsterListing listing) {
        if (encounterEnded || listing.mm.isDead()) return;
        selectedMonster.remove();
        listing.addActor(selectedMonster);
        if (selectedPlayer.getParent() instanceof PlayerListing player) setAction(player.index, listing.mm);
    }

    private void panel(float x, float y, float width, float height, Color color) {
        Image surface = new Image(uiPixel);
        surface.setColor(color);
        surface.setBounds(x, y, width, height);
        surface.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        stage.addActor(surface);
        surface.toBack();
        Image rule = new Image(uiPixel);
        rule.setColor(GOLD.r, GOLD.g, GOLD.b, .45f);
        rule.setBounds(x, y + height - 1, width, 1);
        rule.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        stage.addActor(rule);
    }

    private Label caption(String text, float x, float y, float width, float height, Color color, String style) {
        Label label = new Label(text, combatSkin, style);
        label.setColor(color);
        label.setBounds(x, y, width, height);
        label.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        stage.addActor(label);
        return label;
    }

    private void updateCommandUI() {
        long living = monsters.stream().filter(m -> !m.isDead()).count();
        encounterStatus.setText(encounterEnded ? (ctx.enabledCount() == 0 ? "PARTY FALLEN" : living == 0 ? "VICTORY" : "WITHDRAWN")
                : "TURN " + turn + "   /   " + living + " HOSTILES");
        if (selectedPlayer.getParent() instanceof PlayerListing listing) {
            abilityTitle.setText(String.format("%s / M %d / P %d", listing.player.name.toUpperCase(),
                    java.util.Arrays.stream(listing.player.magePoints).sum(),
                    java.util.Arrays.stream(listing.player.clericPoints).sum()));
            Action action = actions.get(listing.index);
            Spells spell = action.spell != null ? action.spell : action.item != null ? action.item.spell : null;
            commandHint.setText(encounterEnded ? "Encounter complete."
                    : spell != null && spell.getTarget() == SpellTarget.PERSON ? spell + ": choose an ally"
                    : listing.player.name + ": " + orderSummary(action));
        }
        attack.setDisabled(encounterEnded);
        abilities.setDisabled(encounterEnded);
        for (MonsterListing listing : monsterOrderedList) {
            ModelInstance instance = listing.health.modelInstance;
            listing.marker.setVisible(instance != null && !listing.mm.isDead());
            if (instance == null || listing.mm.isDead()) continue;
            Vector3 position = listing.markerPosition;
            listing.modelBounds.getCenter(position);
            position.y = listing.modelBounds.max.y + .035f;
            position.mul(instance.transform);
            camera.project(position, SCENE_X, SCENE_Y, SCENE_WIDTH, SCENE_HEIGHT);
            listing.marker.setPosition(position.x - 10, position.y);
            listing.marker.setColor(selectedMonster.getParent() == listing ? Color.SALMON : GOLD);
            listing.marker.setVisible(position.x > SCENE_X + 10 && position.x < SCENE_X + SCENE_WIDTH - 10
                    && position.y > SCENE_Y && position.y < SCENE_Y + SCENE_HEIGHT - 20
                    && (living <= 12 || listing.number <= 5 || selectedMonster.getParent() == listing));
        }
    }

    @Override public void fight() {
        if (encounterEnded) return;
        abilityDrawer.setVisible(false);
        super.fight();
        turn++;
    }

    private String orderSummary(Action action) {
        if (action == null) return "Melee > auto";
        String command = action.spell != null ? action.spell.toString()
                : action.item != null ? action.item.spell.toString() : action.dispel ? "Dispel" : "Melee";
        String target = "auto";
        if (action.playerTarget != null) target = action.playerTarget.name;
        else if (action.monsterTarget != null && !action.monsterTarget.isDead()) {
            for (MonsterListing listing : monsterOrderedList) {
                if (listing.mm == action.monsterTarget) target = String.format("#%02d", listing.number);
            }
        }
        return command + " > " + target;
    }

    private final ModelBatch modelBatch;
    private final Environment environment = new Environment();
    private final PointLight pointLight1 = new PointLight();
    private final PerspectiveCamera camera;
    private final Color flame = new Color(0xeac4a1ff);

    private final java.util.List<MonsterListing> monsterOrderedList = new ArrayList<>();
    private static final int MONSTER_LISTING_PER_ROW = 1;

    private final BoundingBox sceneBounds = new BoundingBox();
    private final Vector3 sceneCenter = new Vector3();
    private final Vector3 sceneDims = new Vector3();

    private final java.util.List<ModelInstance> wallAndFloor = new ArrayList<>();
    private ModelInstance wmi00, wmi01, wmi02, wmi03, door;
    private ModelInstance wmi10, wmi11, wmi12, wmi13;
    private ModelInstance wmi20, wmi21, wmi22, wmi23;

    private final Stage stage;
    private final com.badlogic.gdx.scenes.scene2d.ui.List<SpellLabel> spellsList;
    private final Table monstersTable;
    private final Table playersTable;
    private final AutoFocusScrollPane playersScroll;
    private final AutoFocusScrollPane monstersScroll;
    private final AutoFocusScrollPane spellsScroll;
    private final LogScrollPane logs;
    private final TextButton fight;
    private final TextButton reset;
    private final TextButton flee;
    private final TextButton exit;

    private static final int PLAYER_LISTING_WIDTH = 256;
    private static final int MONSTER_LISTING_WIDTH = 186;
    private static final int MONSTER_CARD_HEIGHT = 40;
    private static final int LINE_HEIGHT = 18;
    private static final int LOG_WIDTH = 190;
    private final Image selectedMonster = selection(MONSTER_LISTING_WIDTH, MONSTER_CARD_HEIGHT, Color.SALMON);
    private final Image selectedPlayer = selection(PLAYER_LISTING_WIDTH, LINE_HEIGHT * 3, GOLD);

    private MazeCell destCell, fromCell;
    private final boolean hasTreasure;

    public WizardryCombatScreen(Context context, Map contextMap, String mname, Monster opponent, int level,
            boolean hasTreasure, MazeCell destCell, MazeCell fromCell) {
        super(context, contextMap, opponent, level);

        this.destCell = destCell;
        this.fromCell = fromCell;
        this.hasTreasure = hasTreasure;

        this.camera = new PerspectiveCamera(50, SCENE_WIDTH, SCENE_HEIGHT);

        DefaultShader.Config config = new DefaultShader.Config();
        config.vertexShader = Gdx.files.internal("assets/dungeon.vertex.glsl").readString();
        config.fragmentShader = Gdx.files.internal("assets/dungeon.fragment.glsl").readString();
        config.numDirectionalLights = 4;

        pointLight1.set(flame.r, flame.g, flame.b, 1, 2, 2, 10f);
        environment.add(pointLight1);

        this.modelBatch = new ModelBatch(new DefaultShaderProvider(config));

        Model modelDoor = ObjLoader.loadModel("assets/graphics/door.obj", "door", .1f);
        Model modelWallCorner = ObjLoader.loadModel("assets/graphics/dungeon-walls.obj", "wall-corner", .1f);
        Model modelWallStraight = ObjLoader.loadModel("assets/graphics/dungeon-walls.obj", "wall-straight", .1f);
        Model modelWallFloor = ObjLoader.loadModel("assets/graphics/dungeon-walls.obj", "wall-floor", .1f);

        ownedModels.add(modelDoor); ownedModels.add(modelWallCorner);
        ownedModels.add(modelWallStraight); ownedModels.add(modelWallFloor);
        this.door = new ModelInstance(modelDoor);
        this.wmi00 = new ModelInstance(modelWallCorner);
        this.wmi01 = new ModelInstance(modelWallStraight);
        this.wmi02 = new ModelInstance(modelWallStraight);
        this.wmi03 = new ModelInstance(modelWallCorner);
        this.wmi10 = new ModelInstance(modelWallStraight);
        this.wmi11 = new ModelInstance(modelWallFloor);
        this.wmi12 = new ModelInstance(modelWallFloor);
        this.wmi13 = new ModelInstance(modelWallStraight);
        this.wmi20 = new ModelInstance(modelWallStraight);
        this.wmi21 = new ModelInstance(modelWallFloor);
        this.wmi22 = new ModelInstance(modelWallFloor);
        this.wmi23 = new ModelInstance(modelWallStraight);

        this.wallAndFloor.add(door);
        this.wallAndFloor.add(wmi00);
        this.wallAndFloor.add(wmi01);
        this.wallAndFloor.add(wmi02);
        this.wallAndFloor.add(wmi03);
        this.wallAndFloor.add(wmi10);
        this.wallAndFloor.add(wmi11);
        this.wallAndFloor.add(wmi12);
        this.wallAndFloor.add(wmi13);
        this.wallAndFloor.add(wmi20);
        this.wallAndFloor.add(wmi21);
        this.wallAndFloor.add(wmi22);
        this.wallAndFloor.add(wmi23);
        // Continue the floor underneath the wider encounter camera.
        for (int row = 0; row < 4; row++) {
            for (int col = -1; col < 3; col++) {
                ModelInstance floor = new ModelInstance(modelWallFloor);
                floor.transform.setToTranslation(.0118f + col * 1.2f, -.15f, 2.424f + row * 1.2f);
                wallAndFloor.add(floor);
            }
        }

        Table logTable = new Table(combatSkin);
        logTable.setBackground("dimmer");
        this.logs = new LogScrollPane(combatSkin, logTable, LOG_WIDTH, "default-12");
        setLogs(this.logs);

        this.stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT));
        //this.stage.setDebugAll(true);

        this.monstersTable = new Table(combatSkin);
        this.monstersTable.top().defaults().pad(2).expandX().center();
        this.monstersScroll = new AutoFocusScrollPane(this.monstersTable, combatSkin);
        this.monstersScroll.setScrollingDisabled(true, false);
        for (MutableMonster mm : monsters) {
            addListing(mm);
        }

        this.playersTable = new Table(combatSkin);
        this.playersTable.top().defaults().pad(2).expandX().center();
        this.playersScroll = new AutoFocusScrollPane(playersTable, combatSkin);
        this.playersScroll.setScrollingDisabled(true, false);
        for (int i = 0; i < context.players().length; i++) {
            CharacterRecord p = this.ctx.players()[i];
            this.playersTable.add(new PlayerListing(i, p)).width(PLAYER_LISTING_WIDTH).center();
            if (i == 2) {
                this.playersTable.row();
            }
        }

        this.spellsList = new com.badlogic.gdx.scenes.scene2d.ui.List(combatSkin, "default-12-padded-clear");
        this.spellsScroll = new AutoFocusScrollPane(this.spellsList, combatSkin);

        this.spellsList.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (encounterEnded) return;
                PlayerListing pl = (PlayerListing) selectedPlayer.getParent();
                SpellLabel sl = spellsList.getSelected();
                if (pl != null && sl != null) {
                    abilityDrawer.setVisible(false);
                    if (sl.spell != null) {
                        setAction(pl.index, sl.spell);
                    }
                    if (sl.item != null) {
                        setAction(pl.index, sl.item);
                    }
                    if (sl.dispel) {
                        setAction(pl.index, true);
                    }
                }
            }
        });

        this.monstersScroll.setBounds(816, 224, 196, 468);
        this.playersScroll.setBounds(16, 14, 778, 116);
        this.logs.setBounds(816, 16, LOG_WIDTH, 164);
        this.fight = new TextButton("Resolve Turn", combatSkin, "default-16");
        this.fight.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                fight();
            }
        });
        this.fight.setBounds(664, 145, 130, 28);

        this.reset = new TextButton("Clear", combatSkin, "default-16");
        this.reset.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                abilityDrawer.setVisible(false);
                selectedPlayer.remove();
                selectedMonster.remove();

                spellsList.getSelection().clear();
                spellsList.getItems().clear();

                actions.clear();
                for (CharacterRecord p : ctx.players()) {
                    addAction(p);
                }

                spellsList.invalidateHierarchy();
                selectPlayer(0);
            }
        });
        this.reset.setBounds(552, 145, 104, 28);

        this.flee = new TextButton("Retreat", combatSkin, "default-16");
        this.flee.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                int temp = 38 - (3 * level);

                int enabledCount = ctx.enabledCount();
                if (ctx.enabledCount() < 4) {
                    temp += (20 - (5 * enabledCount));
                }

                if (level == 10) {
                    temp = -1;
                }

                int roll = Utils.getRandomBetween(0, 99);

                if (roll > temp) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    fight();
                } else {
                    Sounds.play(Sound.EVADE);
                    end(true);
                }

            }
        });
        this.flee.setBounds(456, 145, 88, 28);

        this.exit = new TextButton("Continue", combatSkin, "default-16");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (ctx.pickRandomEnabledPlayer() == null) {
                    Map.releaseAllMaps();
                    mainGame.setScreen(startScreen);
                } else {
                    if (pickMonster() == null) {

                        List<CharacterRecord> lastMenStanding = new ArrayList<>();
                        for (CharacterRecord p : players) {
                            if (!p.isDisabled()) {
                                lastMenStanding.add(p);
                            }
                        }

                        int goldRewardId = 0;
                        int chestRewardId = 0;
                        int exp = 0;

                        for (MutableMonster mm : monsters) {
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

                        WizardryCombatScreen.this.contextMap.getScreen().endCombat(true, WizardryCombatScreen.this.opponent);

                        for (CharacterRecord p : lastMenStanding) {
                            if (!p.isDead()) {
                                p.awardXP(exp / lastMenStanding.size());
                                WizardryCombatScreen.this.contextMap.getScreen().log(String.format("%s gained %d experience points.", p.name.toUpperCase(), exp / lastMenStanding.size()));
                            }
                        }

                        if (WizardryCombatScreen.this.hasTreasure) {
                            mainGame.setScreen(new RewardScreen(WizardryCombatScreen.this.ctx, WizardryCombatScreen.this.contextMap, chestRewardId));
                        } else {
                            Reward reward = contextMap.scenario().rewards().get(goldRewardId);
                            int goldAmt = reward.goldAmount();
                            for (SaveGame.CharacterRecord c : lastMenStanding) {
                                c.adjustGold(goldAmt / lastMenStanding.size());
                                WizardryCombatScreen.this.contextMap.getScreen().log(String.format("%s found %d gold.", c.name.toUpperCase(), goldAmt));
                            }
                            mainGame.setScreen(WizardryCombatScreen.this.contextMap.getScreen());
                        }

                    } else {
                        mainGame.setScreen(WizardryCombatScreen.this.contextMap.getScreen());
                    }
                }
            }
        });
        this.exit.setBounds(664, 145, 130, 28);

        this.stage.addActor(this.fight);
        this.stage.addActor(this.flee);
        this.stage.addActor(this.reset);
        this.stage.addActor(this.logs);

        this.stage.addActor(this.monstersScroll);
        this.stage.addActor(this.playersScroll);

        buildCommandUI(mname);
        selectPlayer(0);
    }
    private void sceneOrientation() {

        sceneBounds.inf();

        int MODELS_PER_ROW = 5;
        float SPACING = 0.4f;

        int index = 0;
        int count = this.monsterOrderedList.size();
        int rows = (count + MODELS_PER_ROW - 1) / MODELS_PER_ROW;
        float zStart = 0.5f + (rows - 1) * SPACING;

        for (MonsterListing l : this.monsterOrderedList) {
            int row = index / MODELS_PER_ROW;
            int col = index % MODELS_PER_ROW;
            float x = 0.3f + col * SPACING;
            float z = zStart - row * SPACING;
            if (l.health.modelInstance != null) l.health.modelInstance.transform.setToTranslation(x, 0, z);
            index++;
            if (l.health.modelInstance != null) {
                sceneBounds.ext(new BoundingBox(l.modelBounds).mul(l.health.modelInstance.transform));
            }
        }

        if (!sceneBounds.isValid()) sceneBounds.ext(1.1f, 0.5f, 0.5f);
        sceneBounds.getCenter(sceneCenter);
        sceneBounds.getDimensions(sceneDims);

        float tanHalfFov = MathUtils.tan(camera.fieldOfView * MathUtils.degreesToRadians / 2);
        float distance = Math.max(sceneDims.y / (2 * tanHalfFov),
                sceneDims.x / (2 * tanHalfFov * SCENE_WIDTH / SCENE_HEIGHT));
        camera.position.set(sceneCenter.x, sceneCenter.y + .22f, sceneBounds.max.z + Math.max(.85f, distance * 1.15f));
        camera.lookAt(sceneCenter.x, sceneCenter.y, sceneCenter.z);

        float FH = -0.15f;
        wmi00.transform.setToTranslation(0, FH, 0);
        wmi00.transform.rotate(Vector3.Y, 180);
        wmi01.transform.setToTranslation(0.007f, FH, 0.023f);
        wmi01.transform.rotate(Vector3.Y, 90);
        wmi02.transform.setToTranslation(1.2098f, FH, 0.0209f);
        wmi02.transform.rotate(Vector3.Y, 90);
        wmi03.transform.setToTranslation(2.425f, FH, 0.00548f);
        wmi03.transform.rotate(Vector3.Y, 90);

        wmi10.transform.setToTranslation(0.017f, FH, 1.216f);
        wmi10.transform.rotate(Vector3.Y, 180);
        wmi11.transform.setToTranslation(0.0118f, FH, 0.0288f);
        wmi12.transform.setToTranslation(1.2114f, FH, 0.0255f);
        wmi13.transform.setToTranslation(2.4028f, FH, 0.00115f);

        wmi20.transform.setToTranslation(0.017f, FH, 2.4275f);
        wmi20.transform.rotate(Vector3.Y, 180);
        wmi21.transform.setToTranslation(0.0118f, FH, 1.2226f);
        wmi22.transform.setToTranslation(1.2114f, FH, 1.2245f);
        wmi23.transform.setToTranslation(2.4028f, FH, 1.2149f);

        door.transform.setToTranslation(0.0478f, 0, -0.9024f);
        door.transform.rotate(Vector3.Y, 180);

        camera.near = 0.1f;
        camera.far = 200;
        camera.update();

    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        camera.viewportWidth = SCENE_WIDTH;
        camera.viewportHeight = SCENE_HEIGHT;
        camera.update();
        sceneOrientation();
    }

    @Override
    public void render(float delta) {
        stage.getViewport().apply();
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);

        com.badlogic.gdx.math.Vector2 origin = stage.stageToScreenCoordinates(new com.badlogic.gdx.math.Vector2(SCENE_X, SCENE_Y));
        com.badlogic.gdx.math.Vector2 corner = stage.stageToScreenCoordinates(new com.badlogic.gdx.math.Vector2(SCENE_X + SCENE_WIDTH, SCENE_Y + SCENE_HEIGHT));
        com.badlogic.gdx.graphics.glutils.HdpiUtils.glViewport(Math.round(origin.x),
                Gdx.graphics.getHeight() - Math.round(origin.y),
                Math.round(corner.x - origin.x), Math.round(origin.y - corner.y));
        camera.update();
        modelBatch.begin(camera);
        for (ModelInstance w : this.wallAndFloor) {
            modelBatch.render(w, environment);
        }
        for (MonsterListing l : monsterOrderedList) {
            if (l.health.modelInstance != null) {
                modelBatch.render(l.health.modelInstance, environment);
            }
        }
        modelBatch.end();

        stage.getViewport().apply();
        updateCommandUI();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void end(boolean fled) {
        super.end(fled);

        this.fight.remove();
        this.reset.remove();
        this.flee.remove();

        if (fled && this.fromCell != null) {
            Screen dungeon = this.contextMap.getScreen();
            mainGame.setScreen(dungeon);
            if (dungeon instanceof TmxDungeonScreen) {
                ((TmxDungeonScreen) dungeon).teleport(this.fromCell.address, false);
            } else if (dungeon instanceof WizardryDungeonScreen) {
                ((WizardryDungeonScreen) dungeon).teleport(this.fromCell.address, false);
            } else {
                throw new IllegalStateException("Context map screen is not a dungeon screen: " + dungeon);
            }
        }

        encounterEnded = true;
        abilityDrawer.setVisible(false);
        this.stage.addActor(this.exit);
    }

    @Override
    public void addMonster(MutableMonster mm) {
        super.addMonster(mm);
        addListing(mm);
        if (camera != null) sceneOrientation();
    }

    private void addListing(MutableMonster mm) {
        MonsterListing ml = new MonsterListing(mm);
        this.monsterOrderedList.add(ml);
        this.monstersTable.add(ml).width(MONSTER_LISTING_WIDTH).center();
        if (this.monstersTable.getCells().size % MONSTER_LISTING_PER_ROW == 0) {
            this.monstersTable.row();
        }
    }

    @Override
    public void removeMonster(MutableMonster mm) {
        super.removeMonster(mm);

        Iterator<MonsterListing> iter = this.monsterOrderedList.iterator();
        while (iter.hasNext()) {
            MonsterListing l = iter.next();
            if (l.mm == mm) {
                if (selectedMonster.getParent() == l) selectedMonster.remove();
                l.marker.remove();
                iter.remove();
            }
        }

        Cell found = null;
        for (Cell cell : this.monstersTable.getCells()) {
            Actor actor = cell.getActor();
            if (actor instanceof MonsterListing ml) {
                if (ml.mm == mm) {
                    found = cell;
                    break;
                }
            }
        }

        if (found != null) {

            found.getActor().remove();
            monstersTable.getCells().removeValue(found, true);

            List<Actor> cells = new ArrayList<>();
            for (Cell c : monstersTable.getCells().toArray(Cell.class)) {
                cells.add(c.getActor());
            }

            monstersTable.reset();

            monstersTable.top().defaults().pad(2).expandX().center();

            int i = 0;
            for (Actor a : cells) {
                monstersTable.add(a).pad(2);
                i++;
                if (i % MONSTER_LISTING_PER_ROW == 0) {
                    monstersTable.row();
                }
            }

            monstersTable.layout();
        }
        sceneOrientation();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(this.stage));
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        modelBatch.dispose();
        for (Model model : ownedModels) model.dispose();
        uiPixel.dispose();
        combatSkin.dispose();
    }

    @Override
    public void log(String s) {
        this.logs.add(s.toUpperCase());
    }

    @Override
    public void log(String s, Color c) {
        this.logs.add(s.toUpperCase(), c);
    }

    @Override
    public void playSound(Sound sound) {
        Sounds.play(sound);
    }

    private class PlayerStatusLabel extends Label {

        private final CharacterRecord rec;

        public PlayerStatusLabel(CharacterRecord rec) {
            super("", combatSkin, "default-12");
            this.rec = rec;
            setColor(rec.status.color());
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.CharArray getText() {
            return new com.badlogic.gdx.utils.CharArray(
                    String.format("HP: %d /%d  AC: %d  ST: %s", rec.hp, rec.maxhp, rec.calculateAC(), rec.isDead() ? "DEAD" : rec.status));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

        @Override
        public Color getColor() {
            return rec.isDead() ? Color.SCARLET : rec.status.color();
        }

    }

    private class PlayerOrderLabel extends Label {

        private final CharacterRecord rec;

        public PlayerOrderLabel(CharacterRecord rec) {
            super("", combatSkin, "default-12");
            this.rec = rec;
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.CharArray getText() {
            return new com.badlogic.gdx.utils.CharArray(orderSummary(getAction(rec)));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

    }

    private class PlayerListing extends Group {

        final int index;
        final Label l1;
        final PlayerStatusLabel l2;
        final PlayerOrderLabel l3;
        final ListingBackground health;
        final CharacterRecord player;

        PlayerListing(int index, CharacterRecord rec) {
            this.index = index;
            this.player = rec;
            this.health = new ListingBackground(uiPixel, PLAYER_LISTING_WIDTH, null);
            rec.healthCursor = this.health;
            rec.healthCursor.adjust(rec.hp, rec.maxhp);

            this.l1 = new Label("", combatSkin, "default-12");
            this.l2 = new PlayerStatusLabel(rec);
            this.l3 = new PlayerOrderLabel(rec);
            this.l3.setColor(GOLD);
            this.l1.setEllipsis(true);
            this.l2.setEllipsis(true);
            this.l3.setEllipsis(true);

            String d1 = String.format("%d  %s  /  %s %d", index + 1, rec.name.toUpperCase(), rec.classType, rec.level);
            this.l1.setText(d1);

            addActor(this.health);
            addActor(this.l1);
            addActor(this.l2);
            addActor(this.l3);

            float x = 3;
            this.l1.setBounds(x, LINE_HEIGHT * 2, PLAYER_LISTING_WIDTH - 6, LINE_HEIGHT);
            this.l2.setBounds(x, LINE_HEIGHT * 1, PLAYER_LISTING_WIDTH - 6, LINE_HEIGHT);
            this.l3.setBounds(x, LINE_HEIGHT * 0, PLAYER_LISTING_WIDTH - 6, LINE_HEIGHT);

            this.setSize(PLAYER_LISTING_WIDTH, LINE_HEIGHT * 3f);

            this.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (encounterEnded) return;
                    SpellLabel sl = spellsList.getSelected();
                    PlayerListing acting = (PlayerListing) selectedPlayer.getParent();

                    if (acting != null && acting != PlayerListing.this && sl != null && !sl.dispel) {
                        Spells selectedSpell = sl.spell != null ? sl.spell : sl.item != null ? sl.item.spell : null;
                        if (selectedSpell != null && selectedSpell.getTarget() == SpellTarget.PERSON) {
                            setAction(acting.index, PlayerListing.this.player);
                            spellsList.getSelection().clear();
                            return;
                        }
                    }

                    selectPlayer(index);
                }
            });
        }

    }

    private void selectPlayer(int index) {
        if (encounterEnded) return;
        for (Actor actor : playersTable.getChildren()) {
            if (actor instanceof PlayerListing listing && listing.index == index) {
                selectedPlayer.remove();
                listing.addActor(selectedPlayer);

                spellsList.getItems().clear();
                spellsList.getSelection().clear();
                selectedMonster.remove();
                for (MonsterListing monster : monsterOrderedList) {
                    if (actions.get(index).monsterTarget == monster.mm) monster.addActor(selectedMonster);
                }

                for (Spells s : listing.player.knownSpells) {
                    addSpell(s);
                }
                if (listing.player.classType == ClassType.PRIEST
                        || (listing.player.classType == ClassType.LORD && listing.player.level >= 9)
                        || (listing.player.classType == ClassType.BISHOP && listing.player.level >= 4)) {
                    spellsList.getItems().add(new SpellLabel());
                    spellsList.invalidateHierarchy();
                }
                if (listing.player.weapon != null && listing.player.weapon.spell != null) {
                    addSpell(listing.player.weapon);
                }
                if (listing.player.helm != null && listing.player.helm.spell != null) {
                    addSpell(listing.player.helm);
                }
                if (listing.player.armor != null && listing.player.armor.spell != null) {
                    addSpell(listing.player.armor);
                }
                if (listing.player.item1 != null && listing.player.item1.spell != null) {
                    addSpell(listing.player.item1);
                }
                if (listing.player.item2 != null && listing.player.item2.spell != null) {
                    addSpell(listing.player.item2);
                }
                for (Item i : listing.player.inventory) {
                    if (i.spell != null) {
                        addSpell(i);
                    }
                }
                return;
            }
        }
    }
    private class MonsterStatusLabel extends Label {

        private final MutableMonster mm;

        public MonsterStatusLabel(MutableMonster mm) {
            super("", combatSkin, "default-12");
            this.mm = mm;
            setColor(mm.status().color());
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.CharArray getText() {
            return new com.badlogic.gdx.utils.CharArray(
                    String.format("HP: %d / %d AC: %d  ST: %s", mm.getCurrentHitPoints(), mm.getMaxHitPoints(), mm.getArmourClass(), mm.getCurrentHitPoints() <= 0 ? "DEAD" : mm.status()));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

        @Override
        public Color getColor() {
            return mm.getCurrentHitPoints() <= 0 ? Color.SCARLET : mm.status().color();
        }

    }

    private class MonsterListing extends Group {

        final int number = nextMonsterNumber++;
        final Label marker;
        final BoundingBox modelBounds = new BoundingBox();
        final Vector3 markerPosition = new Vector3();

        final Label l1;
        final MonsterStatusLabel l2;
        final ListingBackground health;
        final Monster m;
        final MutableMonster mm;

        MonsterListing(MutableMonster mm) {
            this.mm = mm;
            this.m = (Monster) mm.baseType();

            this.health = new ListingBackground(uiPixel,
                    MONSTER_LISTING_WIDTH,
                    new ModelInstance(MonsterModels.values()[m.getIconId()].model()));
            this.health.modelInstance.calculateBoundingBox(modelBounds);
            this.marker = new Label(String.format("%02d", number), combatSkin, "default-12");
            this.marker.setSize(20, 20);
            this.marker.setAlignment(com.badlogic.gdx.utils.Align.center);
            this.marker.addListener(new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) {
                    selectMonster(MonsterListing.this);
                }
            });
            stage.addActor(marker);

            mm.setHealthCursor(this.health);
            this.health.adjust(mm.getCurrentHitPoints(), mm.getMaxHitPoints());

            this.l1 = new Label("", combatSkin, "default-12");
            this.l2 = new MonsterStatusLabel(mm);
            this.l1.setEllipsis(true);
            this.l2.setEllipsis(true);

            String d1 = String.format("%02d  %s", number, m.name.toUpperCase());
            this.l1.setText(d1);

            addActor(this.health);
            addActor(this.l1);
            addActor(this.l2);

            float x = 3;
            this.l1.setBounds(x, 20, MONSTER_LISTING_WIDTH - 6, LINE_HEIGHT);
            this.l2.setBounds(x, 3, MONSTER_LISTING_WIDTH - 6, LINE_HEIGHT);

            this.setSize(MONSTER_LISTING_WIDTH, MONSTER_CARD_HEIGHT);

            this.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectMonster(MonsterListing.this);
                }
            });

        }

    }

    private class ListingBackground extends HealthCursor {

        ModelInstance modelInstance;
        final TextureRegion healthGreen;
        final int width;
        final int height;
        public ListingBackground(Texture texture, int width, ModelInstance modelInstance) {
            this.modelInstance = modelInstance;
            this.width = width;
            this.height = modelInstance == null ? LINE_HEIGHT * 3 : MONSTER_CARD_HEIGHT;
            this.healthGreen = new TextureRegion(texture, 0, 0, width, LINE_HEIGHT * 3);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color previous = new Color(batch.getColor());
            batch.setColor(0.065f, 0.095f, 0.12f, parentAlpha);
            batch.draw(uiPixel, getX(), getY(), width, height);
            batch.setColor(0.20f, 0.75f, 0.63f, parentAlpha);
            batch.draw(uiPixel, getX(), getY(), healthGreen.getRegionWidth(), 3);
            batch.setColor(previous);
        }
        @Override
        public void adjust(int hp, int maxhp) {
            double percent = maxhp <= 0 ? 0 : (double) Math.max(0, hp) / maxhp;
            double bar = percent * (double) this.width;
            if (hp < 0) {
                bar = 0;
            }
            if (bar > this.width) {
                bar = this.width;
            }

            if (bar <= 0) {
                if (this.modelInstance != null) {
                    final ModelInstance dyingInstance = this.modelInstance;
                    this.addAction(Actions.sequence(
                            new ModelFadeAndRiseAction(dyingInstance),
                            Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    ListingBackground.this.modelInstance = null;
                                }
                            })
                    ));
                }
            }

            healthGreen.setRegion(0, 0, (int) bar, LINE_HEIGHT * 3);
        }

        private static class ModelFadeAndRiseAction extends TemporalAction {

            private final ModelInstance instance;
            private final Vector3 tmp = new Vector3();
            private float start;
            private float startAlpha;

            ModelFadeAndRiseAction(ModelInstance instance) {
                this.instance = instance;
                setDuration(1);
            }

            @Override
            protected void begin() {
                if (instance == null) {
                    return;
                }

                instance.transform.getTranslation(tmp);
                start = tmp.y;

                startAlpha = extractAlpha(instance);
                ensureBlending(instance, startAlpha);
            }

            @Override
            protected void update(float percent) {
                if (instance == null) {
                    return;
                }

                instance.transform.getTranslation(tmp);
                tmp.y = MathUtils.lerp(start, start + .45f, percent);
                instance.transform.setTranslation(tmp);

                float alpha = MathUtils.lerp(startAlpha, 0.05f, percent);
                applyAlpha(instance, alpha);
            }

            private static float extractAlpha(ModelInstance instance) {
                for (Material mat : instance.materials) {
                    BlendingAttribute ba = (BlendingAttribute) mat.get(BlendingAttribute.Type);
                    if (ba != null) {
                        return ba.opacity;
                    }

                    ColorAttribute ca = (ColorAttribute) mat.get(ColorAttribute.Diffuse);
                    if (ca != null) {
                        return ca.color.a;
                    }
                }
                return 1f;
            }

            private static void ensureBlending(ModelInstance instance, float alpha) {
                for (Material mat : instance.materials) {
                    BlendingAttribute ba = (BlendingAttribute) mat.get(BlendingAttribute.Type);
                    if (ba == null) {
                        mat.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, alpha));
                    }
                }
            }

            private static void applyAlpha(ModelInstance instance, float alpha) {
                for (Material mat : instance.materials) {
                    BlendingAttribute ba = (BlendingAttribute) mat.get(BlendingAttribute.Type);
                    if (ba == null) {
                        ba = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, alpha);
                        mat.set(ba);
                    } else {
                        ba.opacity = alpha;
                    }

                    ColorAttribute ca = (ColorAttribute) mat.get(ColorAttribute.Diffuse);
                    if (ca != null) {
                        ca.color.a = alpha;
                    }
                }
            }
        }
    }

    private class SpellLabel extends Label {

        final Spells spell;
        final Item item;
        final boolean dispel;

        public SpellLabel(Spells spell) {
            super(spell.toString(), combatSkin, "default-12");
            this.item = null;
            this.spell = spell;
            this.dispel = false;
        }

        public SpellLabel(Item it) {
            super(it.name, combatSkin, "default-12");
            this.item = it;
            this.spell = null;
            this.dispel = false;
        }

        public SpellLabel() {
            super("Dispel Undead", combatSkin, "default-12");
            this.item = null;
            this.spell = null;
            this.dispel = true;
        }

        @Override
        public String toString() {
            if (this.dispel) {
                return "Dispel Undead";
            }
            if (this.spell != null) {
                return this.spell.label() + "    " + this.spell.getName();
            }
            return this.item.name + " - " + this.item.spell + "  " + this.item.spell.getName();
        }

    }

    private void addSpell(Spells s) {
        if (s.getArea() == SpellArea.COMBAT || s.getArea() == SpellArea.ANY_TIME) {
            SpellLabel label = new SpellLabel(s);
            spellsList.getItems().add(label);
            spellsList.invalidateHierarchy();
        }
    }

    private void addSpell(Item i) {
        if (i.spell.getArea() == SpellArea.COMBAT || i.spell.getArea() == SpellArea.ANY_TIME) {
            SpellLabel label = new SpellLabel(i);
            spellsList.getItems().add(label);
            spellsList.invalidateHierarchy();
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

}
