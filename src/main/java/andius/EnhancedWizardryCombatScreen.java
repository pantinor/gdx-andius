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
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import utils.AutoFocusScrollPane;
import utils.FrameMaker;
import utils.LogScrollPane;
import utils.Utils;

public class EnhancedWizardryCombatScreen extends Combat implements Screen, Constants {

    private final ModelBatch modelBatch;
    private final Environment environment = new Environment();
    private final PointLight pointLight1 = new PointLight();
    private final PointLight pointLight2 = new PointLight();

    private final PerspectiveCamera camera;

    private final java.util.List<MonsterListing> monsterOrderedList = new ArrayList<>();
    private static final int MONSTERS_PER_ROW = 5;

    private final BoundingBox sceneBounds = new BoundingBox();
    private final Vector3 sceneCenter = new Vector3();
    private final Vector3 sceneDims = new Vector3();
    private ModelInstance floorPlane;
    private final Color flame = new Color(0xeac4a1ff);

    private final Stage stage;
    private final com.badlogic.gdx.scenes.scene2d.ui.List<SpellLabel> spellsList;
    private final com.badlogic.gdx.scenes.scene2d.ui.List<ActionLabel> actionsList;
    private final Table monstersTable;
    private final Table playersTable;
    private final AutoFocusScrollPane playersScroll;
    private final ScrollPane actionsScroll;
    private final AutoFocusScrollPane monstersScroll;
    private final AutoFocusScrollPane spellsScroll;
    private final LogScrollPane logs;
    private final TextButton fight;
    private final TextButton reset;
    private final TextButton flee;
    private final TextButton exit;

    private static final int PLAYER_LISTING_WIDTH = 215;
    private static final int MONSTER_LISTING_WIDTH = 185;
    private static final int LINE_HEIGHT = 15;
    private static final int LOG_WIDTH = 335;
    private static final int LOG_HEIGHT = 150;

    private final Image selectedMonster = new Image(Utils.fillRectangle(MONSTER_LISTING_WIDTH, LINE_HEIGHT * 3, Color.RED, .25f));
    private final Image selectedPlayer = new Image(Utils.fillRectangle(PLAYER_LISTING_WIDTH, LINE_HEIGHT * 3, Color.YELLOW, .25f));
    private static final Texture PLAYER_GREEN = Utils.fillRectangle(PLAYER_LISTING_WIDTH, LINE_HEIGHT * 3, Color.GREEN, .2f);
    private static final Texture MONSTER_GREEN = Utils.fillRectangle(MONSTER_LISTING_WIDTH, LINE_HEIGHT * 3, Color.GREEN, .2f);

    private MazeCell destCell, fromCell;
    private final boolean hasTreasure;

    public EnhancedWizardryCombatScreen(Context context, Map contextMap, String mname, Monster opponent, int level,
            boolean hasTreasure, MazeCell destCell, MazeCell fromCell) {
        super(context, contextMap, opponent, level);

        this.destCell = destCell;
        this.fromCell = fromCell;
        this.hasTreasure = hasTreasure;

        FrameMaker fm = new FrameMaker(SCREEN_WIDTH, SCREEN_HEIGHT);

        environment.set(ColorAttribute.createAmbient(flame.r, flame.g, flame.b, 1f));
        environment.add(pointLight1);
        environment.add(pointLight2);

        this.camera = new PerspectiveCamera(67, SCREEN_WIDTH, SCREEN_HEIGHT);

        DefaultShader.Config config = new DefaultShader.Config();
        config.vertexShader = Gdx.files.internal("assets/dungeon.vertex.glsl").readString();
        config.fragmentShader = Gdx.files.internal("assets/dungeon.fragment.glsl").readString();

        this.modelBatch = new ModelBatch(new DefaultShaderProvider(config));

        ModelBuilder builder = new ModelBuilder();
        Texture floorTex = new Texture(Gdx.files.classpath("assets/graphics/rock.png"));
        floorTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        Material mfloor = new Material(TextureAttribute.createDiffuse(floorTex), IntAttribute.createCullFace(GL20.GL_NONE));
        this.floorPlane = new ModelInstance(Utils.createPlaneModel(builder, 5, mfloor, false));

        Table logTable = new Table(Andius.skin);
        this.logs = new LogScrollPane(Andius.skin, logTable, LOG_WIDTH, "default-12");
        setLogs(this.logs);

        this.stage = new Stage();
        //this.stage.setDebugAll(true);
        createAxes();

        this.monstersTable = new Table(Andius.skin);
        this.monstersTable.top().defaults().pad(2).expandX().center();
        this.monstersScroll = new AutoFocusScrollPane(this.monstersTable, Andius.skin);
        this.monstersScroll.setScrollingDisabled(true, true);
        for (MutableMonster mm : monsters) {
            add(mm);
        }

        this.playersTable = new Table(Andius.skin);
        this.playersTable.top().defaults().pad(2).expandX().center();
        this.playersScroll = new AutoFocusScrollPane(playersTable, Andius.skin);
        this.playersScroll.setScrollingDisabled(true, true);
        for (int i = 0; i < context.players().length; i++) {
            CharacterRecord p = this.ctx.players()[i];
            this.playersTable.add(new PlayerListing(i, p)).width(PLAYER_LISTING_WIDTH).center();
            if (i == 2) {
                this.playersTable.row();
            }
        }

        this.actionsList = new com.badlogic.gdx.scenes.scene2d.ui.List(Andius.skin, "default-12-padded");
        this.actionsList.getSelection().setDisabled(true);
        this.actionsScroll = new ScrollPane(this.actionsList, Andius.skin);
        for (Action action : this.actions) {
            this.actionsList.getItems().add(new ActionLabel(action));
        }

        this.spellsList = new com.badlogic.gdx.scenes.scene2d.ui.List(Andius.skin, "default-12-padded");
        this.spellsScroll = new AutoFocusScrollPane(this.spellsList, Andius.skin);

        CharacterRecord player = this.ctx.players()[0];

        for (Spells s : player.knownSpells) {
            addSpell(s);
        }
        if (player.weapon != null && player.weapon.spell != null) {
            addSpell(player.weapon);
        }
        if (player.helm != null && player.helm.spell != null) {
            addSpell(player.helm);
        }
        if (player.armor != null && player.armor.spell != null) {
            addSpell(player.armor);
        }
        if (player.item1 != null && player.item1.spell != null) {
            addSpell(player.item1);
        }
        if (player.item2 != null && player.item2.spell != null) {
            addSpell(player.item2);
        }
        for (Item i : player.inventory) {
            if (i.spell != null) {
                addSpell(i);
            }
        }

        this.spellsList.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                PlayerListing pl = (PlayerListing) selectedPlayer.getParent();
                SpellLabel sl = spellsList.getSelected();
                if (pl != null && sl != null) {
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

        int x = 345;
        this.fight = new TextButton("FIGHT", Andius.skin, "default-16");
        this.fight.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                fight();
            }
        });
        this.fight.setBounds(SCREEN_WIDTH - 85, 335, 80, 40);

        this.reset = new TextButton("RESET", Andius.skin, "default-16");
        this.reset.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                actionsList.getItems().clear();
                actions.clear();
                for (CharacterRecord p : ctx.players()) {
                    actionsList.getItems().add(new ActionLabel(addAction(p)));
                }
            }
        });
        this.reset.setBounds(SCREEN_WIDTH - 85, 290, 80, 40);

        this.flee = new TextButton("FLEE", Andius.skin, "default-16");
        this.flee.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                end(true);
            }
        });
        this.flee.setBounds(SCREEN_WIDTH - 85, 245, 80, 40);

        this.exit = new TextButton("EXIT", Andius.skin, "default-16");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (ctx.pickRandomEnabledPlayer() == null) {
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

                        EnhancedWizardryCombatScreen.this.contextMap.getScreen().endCombat(true, EnhancedWizardryCombatScreen.this.opponent);

                        for (CharacterRecord p : lastMenStanding) {
                            if (!p.isDead()) {
                                p.awardXP(exp / lastMenStanding.size());
                                EnhancedWizardryCombatScreen.this.contextMap.getScreen().log(String.format("%s gained %d experience points.", p.name.toUpperCase(), exp / lastMenStanding.size()));
                            }
                        }

                        if (EnhancedWizardryCombatScreen.this.hasTreasure) {
                            mainGame.setScreen(new RewardScreen(EnhancedWizardryCombatScreen.this.ctx, EnhancedWizardryCombatScreen.this.contextMap, chestRewardId));
                        } else {
                            Reward reward = contextMap.scenario().rewards().get(goldRewardId);
                            int goldAmt = reward.goldAmount();
                            for (SaveGame.CharacterRecord c : lastMenStanding) {
                                c.adjustGold(goldAmt / lastMenStanding.size());
                                EnhancedWizardryCombatScreen.this.contextMap.getScreen().log(String.format("%s found %d gold.", c.name.toUpperCase(), goldAmt));
                            }
                            mainGame.setScreen(EnhancedWizardryCombatScreen.this.contextMap.getScreen());
                        }

                    } else {
                        mainGame.setScreen(EnhancedWizardryCombatScreen.this.contextMap.getScreen());
                    }
                }
            }
        });
        this.exit.setBounds(SCREEN_WIDTH - 85, 200, 80, 40);

        int monsterCount = this.monstersTable.getCells().size;
        int monsterRows = (monsterCount + MONSTERS_PER_ROW - 1) / MONSTERS_PER_ROW;
        float monstersPanelHeight = monsterRows * (LINE_HEIGHT * 3 + 6) + 12;
        fm.setBounds(this.monstersScroll, 0, SCREEN_HEIGHT - monstersPanelHeight, SCREEN_WIDTH, monstersPanelHeight);

        int playerRows = context.players().length > 3 ? 2 : 1;
        float playersPanelHeight = playerRows * (LINE_HEIGHT * 3 + 6) + 3;
        fm.setBounds(this.playersScroll, 0, 0, SCREEN_WIDTH - 350, playersPanelHeight);

        fm.setBounds(this.spellsScroll, 5, playersPanelHeight + 3, PLAYER_LISTING_WIDTH, 100);
        fm.setBounds(this.actionsScroll, 5, playersPanelHeight + 6 + 100, LOG_WIDTH, 115);
        fm.setBounds(this.logs, SCREEN_WIDTH - LOG_WIDTH - 5, 3, LOG_WIDTH, LOG_HEIGHT);

        this.stage.addActor(this.fight);
        this.stage.addActor(this.flee);
        this.stage.addActor(this.reset);
        this.stage.addActor(this.logs);

        this.stage.addActor(this.monstersScroll);
        this.stage.addActor(this.playersScroll);
        this.stage.addActor(this.actionsScroll);
        this.stage.addActor(this.spellsScroll);

        Label actionLabel = new Label("Action", Andius.skin, "default-16");
        actionLabel.setPosition(326, 730);
        //this.stage.addActor(actionLabel);

        Label monLabel = new Label(mname, Andius.skin, "default-16");
        monLabel.setPosition(715, 730);
        //this.stage.addActor(monLabel);

        Label spellLabel = new Label("Spells", Andius.skin, "default-16");
        spellLabel.setPosition(10, 380);
        //this.stage.addActor(spellLabel);

    }

    private void refitCameraToModels() {

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
            float x = 0.5f + col * SPACING;
            float z = zStart - row * SPACING;
            l.health.modelInstance.transform.setToTranslation(x, 0, z);
            index++;
            sceneBounds.ext(x, 0.5f, z);
        }

        sceneBounds.getCenter(sceneCenter);
        sceneBounds.getDimensions(sceneDims);

        camera.position.set(sceneCenter.x, 0.5f, sceneDims.z + 2);
        camera.lookAt(sceneCenter.x, 0.3f, sceneCenter.z);

        floorPlane.transform.setToTranslation(sceneCenter.x, 0f, sceneCenter.z);
        pointLight1.set(flame.r, flame.g, flame.b, sceneCenter.x, 2, sceneDims.z + 1, 20f);
        pointLight2.set(flame.r, flame.g, flame.b, sceneCenter.x, -2, sceneDims.z + 1, 20f);

        camera.near = 0.1f;
        camera.far = 100;
        camera.update();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        refitCameraToModels();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);

        camera.update();

        modelBatch.begin(camera);
        //modelBatch.render(axesInstance);
        modelBatch.render(floorPlane, environment);
        for (MonsterListing l : monsterOrderedList) {
            if (l.health.modelInstance != null) {
                modelBatch.render(l.health.modelInstance, environment);
            }
        }
        modelBatch.end();

        stage.act();
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

        this.stage.addActor(this.exit);
    }

    private void add(MutableMonster mm) {
        MonsterListing ml = new MonsterListing(mm);
        this.monsterOrderedList.add(ml);
        this.monstersTable.add(ml).width(MONSTER_LISTING_WIDTH).center();
        if (this.monstersTable.getCells().size % MONSTERS_PER_ROW == 0) {
            this.monstersTable.row();
        }
    }

    @Override
    public void addMonster(MutableMonster mm) {
        super.addMonster(mm);
        add(mm);
    }

    @Override
    public void removeMonster(MutableMonster mm) {
        super.removeMonster(mm);

        Iterator<MonsterListing> iter = this.monsterOrderedList.iterator();
        while (iter.hasNext()) {
            MonsterListing l = iter.next();
            if (l.mm == mm) {
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
                if (i % MONSTERS_PER_ROW == 0) {
                    monstersTable.row();
                }
            }

            monstersTable.layout();
        }
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
    }

    @Override
    public void log(String s) {
        this.logs.add(s);
    }

    @Override
    public void log(String s, Color c) {
        this.logs.add(s, c);
    }

    @Override
    public void playSound(Sound sound) {
        Sounds.play(sound);
    }

    private class PlayerStatusLabel extends Label {

        private final CharacterRecord rec;

        public PlayerStatusLabel(CharacterRecord rec) {
            super("", Andius.skin, "default-12");
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

    private class PlayerMagicPointsLabel extends Label {

        private final CharacterRecord rec;

        public PlayerMagicPointsLabel(CharacterRecord rec) {
            super("", Andius.skin, "default-12");
            this.rec = rec;
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.CharArray getText() {
            int[] ms = rec.magePoints;
            int[] cs = rec.clericPoints;
            return new com.badlogic.gdx.utils.CharArray(
                    String.format("M: %d %d %d %d %d %d %d  P: %d %d %d %d %d %d %d",
                            ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6], cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]));
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
        final PlayerMagicPointsLabel l3;
        final ListingBackground health;
        final CharacterRecord player;

        PlayerListing(int index, CharacterRecord rec) {
            this.index = index;
            this.player = rec;
            this.health = new ListingBackground(PLAYER_GREEN, PLAYER_LISTING_WIDTH, null);
            rec.healthCursor = this.health;
            rec.healthCursor.adjust(rec.hp, rec.maxhp);

            this.l1 = new Label("", Andius.skin, "default-12");
            this.l2 = new PlayerStatusLabel(rec);
            this.l3 = new PlayerMagicPointsLabel(rec);

            String d1 = String.format("%s  %s  LVL %d  %s", rec.name.toUpperCase(), rec.race.toString(), rec.level, rec.classType.toString());
            this.l1.setText(d1);

            addActor(this.health);
            addActor(this.l1);
            addActor(this.l2);
            addActor(this.l3);

            float x = 3;
            this.l1.setBounds(x, LINE_HEIGHT * 2, PLAYER_LISTING_WIDTH, LINE_HEIGHT);
            this.l2.setBounds(x, LINE_HEIGHT * 1, PLAYER_LISTING_WIDTH, LINE_HEIGHT);
            this.l3.setBounds(x, LINE_HEIGHT * 0, PLAYER_LISTING_WIDTH, LINE_HEIGHT);

            this.setSize(PLAYER_LISTING_WIDTH, LINE_HEIGHT * 3f);

            this.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectedPlayer.remove();
                    PlayerListing.this.addActor(selectedPlayer);

                    spellsList.getItems().clear();

                    for (Spells s : player.knownSpells) {
                        addSpell(s);
                    }
                    if (player.classType == ClassType.PRIEST
                            || (player.classType == ClassType.LORD && player.level >= 3)
                            || (player.classType == ClassType.BISHOP && player.level >= 8)) {
                        spellsList.getItems().add(new SpellLabel());
                    }
                    if (player.weapon != null && player.weapon.spell != null) {
                        addSpell(player.weapon);
                    }
                    if (player.helm != null && player.helm.spell != null) {
                        addSpell(player.helm);
                    }
                    if (player.armor != null && player.armor.spell != null) {
                        addSpell(player.armor);
                    }
                    if (player.item1 != null && player.item1.spell != null) {
                        addSpell(player.item1);
                    }
                    if (player.item2 != null && player.item2.spell != null) {
                        addSpell(player.item2);
                    }
                    for (Item i : player.inventory) {
                        if (i.spell != null) {
                            addSpell(i);
                        }
                    }

                }
            });
        }

    }

    private class MonsterStatusLabel extends Label {

        private final MutableMonster mm;

        public MonsterStatusLabel(MutableMonster mm) {
            super("", Andius.skin, "default-12");
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

    private class MonsterMagicPointsLabel extends Label {

        private final MutableMonster mm;

        public MonsterMagicPointsLabel(MutableMonster mm) {
            super("", Andius.skin, "default-12");
            this.mm = mm;
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.CharArray getText() {
            return new com.badlogic.gdx.utils.CharArray(String.format("MG: %d  PR: %d ", mm.getCurrentMageSpellLevel(), mm.getCurrentPriestSpellLevel()));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

    }

    private class MonsterListing extends Group {

        final Label l1;
        final MonsterStatusLabel l2;
        final MonsterMagicPointsLabel l3;
        final ListingBackground health;
        final Monster m;
        final MutableMonster mm;

        MonsterListing(MutableMonster mm) {
            this.mm = mm;
            this.m = (Monster) mm.baseType();

            this.health = new ListingBackground(MONSTER_GREEN,
                    MONSTER_LISTING_WIDTH,
                    new ModelInstance(MonsterModels.values()[m.getIconId()].model()));

            mm.setHealthCursor(this.health);
            this.health.adjust(mm.getCurrentHitPoints(), mm.getMaxHitPoints());

            this.l1 = new Label("", Andius.skin, "default-12");
            this.l2 = new MonsterStatusLabel(mm);
            this.l3 = new MonsterMagicPointsLabel(mm);

            String d1 = String.format("%s  LVL %d", m.name.toUpperCase(), mm.getLevel());
            this.l1.setText(d1);

            addActor(this.health);
            addActor(this.l1);
            addActor(this.l2);
            addActor(this.l3);

            float x = 3;
            this.l1.setBounds(x, LINE_HEIGHT * 2, MONSTER_LISTING_WIDTH, LINE_HEIGHT);
            this.l2.setBounds(x, LINE_HEIGHT * 1, MONSTER_LISTING_WIDTH, LINE_HEIGHT);
            this.l3.setBounds(x, LINE_HEIGHT * 0, MONSTER_LISTING_WIDTH, LINE_HEIGHT);

            this.setSize(MONSTER_LISTING_WIDTH, LINE_HEIGHT * 3f);

            this.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectedMonster.remove();
                    MonsterListing.this.addActor(selectedMonster);

                    PlayerListing pl = (PlayerListing) selectedPlayer.getParent();
                    if (pl != null) {
                        setAction(pl.index, MonsterListing.this.mm);
                    }
                }
            });

        }

    }

    private class ListingBackground extends HealthCursor {

        ModelInstance modelInstance;
        final TextureRegion healthGreen;
        final int width;

        public ListingBackground(Texture texture, int width, ModelInstance modelInstance) {
            this.modelInstance = modelInstance;
            this.width = width;
            this.healthGreen = new TextureRegion(texture, 0, 0, width, LINE_HEIGHT * 3);
        }

        @Override
        public void adjust(int hp, int maxhp) {
            double percent = (double) hp / maxhp;
            double bar = percent * (double) this.width;
            if (hp < 0) {
                bar = 0;
            }
            if (bar > this.width) {
                bar = this.width;
            }

            if (bar <= 0) {
                this.modelInstance = null;
            }

            healthGreen.setRegion(0, 0, (int) bar, LINE_HEIGHT * 3);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            batch.draw(healthGreen, getX(), getY());
        }

    }

    private class SpellLabel extends Label {

        final Spells spell;
        final Item item;
        final boolean dispel;

        public SpellLabel(Spells spell) {
            super(spell.toString(), Andius.skin, "default-12");
            this.item = null;
            this.spell = spell;
            this.dispel = false;
        }

        public SpellLabel(Item it) {
            super(it.name, Andius.skin, "default-12");
            this.item = it;
            this.spell = null;
            this.dispel = false;
        }

        public SpellLabel() {
            super("Dispel Undead", Andius.skin, "default-12");
            this.item = null;
            this.spell = null;
            this.dispel = true;
        }

        @Override
        public String toString() {
            if (this.dispel) {
                return "Dispel Undead";
            }
            return this.spell != null ? this.spell.label() : this.item.name + " - " + this.item.spell;
        }

    }

    private void addSpell(Spells s) {
        if (s.getArea() == SpellArea.COMBAT || s.getArea() == SpellArea.ANY_TIME) {
            SpellLabel label = new SpellLabel(s);
            spellsList.getItems().add(label);
        }
    }

    private void addSpell(Item i) {
        if (i.spell.getArea() == SpellArea.COMBAT || i.spell.getArea() == SpellArea.ANY_TIME) {
            SpellLabel label = new SpellLabel(i);
            spellsList.getItems().add(label);
        }
    }

    private class ActionLabel extends Label {

        final Action action;

        public ActionLabel(Action action) {
            super("", Andius.skin, "default-12");
            this.action = action;
        }

        @Override
        public String toString() {
            return this.action.toString();
        }

    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    final float GRID_MIN = 0f;
    final float GRID_MAX = 20f;
    final float GRID_STEP = .5f;
    public Model axesModel;
    public ModelInstance axesInstance;

    private void createAxes() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        // grid
        MeshPartBuilder builder = modelBuilder.part("grid", GL30.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, new Material());
        builder.setColor(Color.LIGHT_GRAY);
        for (float t = GRID_MIN; t <= GRID_MAX; t += GRID_STEP) {
            builder.line(t, 0, GRID_MIN, t, 0, GRID_MAX);
            builder.line(GRID_MIN, 0, t, GRID_MAX, 0, t);
        }

        // axes
        builder = modelBuilder.part("axes", GL30.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, new Material());
        builder.setColor(Color.RED);
        builder.line(0, 0, 0, 500, 0, 0);
        builder.setColor(Color.GREEN);
        builder.line(0, 0, 0, 0, 500, 0);
        builder.setColor(Color.BLUE);
        builder.line(0, 0, 0, 0, 0, 500);

        axesModel = modelBuilder.end();
        axesInstance = new ModelInstance(axesModel);
    }

}
